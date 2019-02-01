package com.github.aop.weave;

import com.github.aop.filter.Filter;
import com.github.aop.listener.EventListenerHandlers;
import com.github.aop.util.log.LoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;
import com.github.aop.util.AopUtils;
import com.github.aop.util.log.api.*;

public class MethodWriter extends ClassVisitor implements Opcodes {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public MethodWriter(int listenerId,
                        final String targetClassInternalName,
                        Filter filter,
                        final ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
        this.listenerId = listenerId;
        this.targetClassInternalName = targetClassInternalName;
        this.targetClassAsmType = Type.getObjectType(targetClassInternalName);
        this.targetJavaClassName = AopUtils.toJavaClassName(targetClassInternalName);
        this.filter = filter;
    }

    protected final Type TYPE_THROWABLE = Type.getType(Throwable.class);
    protected final Type TYPE_SPY = Type.getType(Spy.class);

    protected int listenerId;
    protected String targetJavaClassName;
    protected String targetClassInternalName;
    protected Type targetClassAsmType;
    protected Filter filter;

    @Override
    public void visit(int version, int access, String className, String signature, String superClass, String[] interfaces) {
        super.visit(version, access, className, signature, superClass, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String s, boolean b) {
        return super.visitAnnotation(s, b);
    }

    public MethodVisitor visitMethod(int access, String name, final String desc, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (isIgnore(mv, access, name, desc, exceptions)) {
            logger.debug("{}.{} was ignored, listener-id={}", targetClassInternalName, name, listenerId);
            return mv;
        }

        EventListenerHandlers.getSingleton().setClassMethodToListenerId(targetClassInternalName + name + desc, listenerId);
        logger.debug("{}.{} was matched, prepare to rewrite, listener-id={}", targetClassAsmType, name, listenerId);


        return new AdviceAdapter(Opcodes.ASM5, mv, access, name, desc) {

            private final Label startCatch = newLabel();
            private final Label endCatch = newLabel();

            /**
             * 将NULL压入栈
             */
            final protected void pushNull() {
                push((Type) null);
            }

            /**
             * 是否静态方法
             *
             * @return true:静态方法 / false:非静态方法
             */
            final protected boolean isStaticMethod() {
                return (methodAccess & ACC_STATIC) != 0;
            }


            /**
             * 加载this/null
             */
            final protected void loadThisOrPushNullIfIsStatic() {
                if (isStaticMethod()) {
                    pushNull();
                } else {
                    loadThis();
                }
            }

            /**
             * 加载异常
             */
            private void loadThrow() {
                dup();
            }

            /**
             * 是否抛出异常返回(通过字节码判断)
             *
             * @param opcode 操作码
             * @return true:以抛异常形式返回 / false:非抛异常形式返回(return)
             */
            private boolean isThrow(int opcode) {
                return opcode == ATHROW;
            }

            /**
             * 加载返回值
             * @param opcode 操作吗
             */
            private void loadReturn(int opcode) {
                switch (opcode) {

                    case RETURN: {
                        pushNull();
                        break;
                    }

                    case ARETURN: {
                        dup();
                        break;
                    }

                    case LRETURN:
                    case DRETURN: {
                        dup2();
                        box(Type.getReturnType(methodDesc));
                        break;
                    }

                    default: {
                        dup();
                        box(Type.getReturnType(methodDesc));
                        break;
                    }

                }
            }

            @Override
            protected void onMethodEnter() {

                //mark(startCatch);

                loadArgArray();

                push(listenerId);
                push(targetJavaClassName);
                push(name);
                push(desc);
                loadThisOrPushNullIfIsStatic();

                invokeStatic(TYPE_SPY, AsmMethods.ASM_METHOD_Spy$spyMethodOnBefore);
            }

            @Override
            protected void onMethodExit(int opcode) {
                if (!isThrow(opcode)) {
                    loadReturn(opcode);
                    push(listenerId);
                    invokeStatic(TYPE_SPY, AsmMethods.ASM_METHOD_Spy$spyMethodOnReturn);
                }
            }

            @Override
            public void visitMaxs(int maxStack, int maxLocals) {

                //mark(endCatch);
                //visitTryCatchBlock(startCatch, endCatch, mark(), TYPE_THROWABLE.getInternalName());

//                    loadThrow();
//                    push(listenerId);
//                    invokeStatic(TYPE_SPY, AsmMethods.ASM_METHOD_Spy$spyMethodOnThrows);

                //throwException();

                super.visitMaxs(maxStack, maxLocals);
            }
        };
    }

    /**
     * 是否需要忽略
     */
    private boolean isIgnore(MethodVisitor mv, int access, String name, String desc, String[] exceptions) {
        return null == mv
                || !filter.doMethodFilter(access, name, AopUtils.toJavaClassNameArray(getParameterTypeArray(desc)), AopUtils.toJavaClassNameArray(exceptions));
    }

    /**
     * 获取参数类型名称数组
     *
     * @return 参数类型名称数组
     */
    private String[] getParameterTypeArray(String desc) {
        final Type[] typeArray = Type.getArgumentTypes(desc);
        if (ArrayUtils.isEmpty(typeArray)) {
            return null;
        }
        final String[] typeDescArray = new String[typeArray.length];
        for (int index = 0; index < typeArray.length; index++) {
            typeDescArray[index] = typeArray[index].getClassName();
        }
        return typeDescArray;
    }

}
