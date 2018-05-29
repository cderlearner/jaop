package com.github.aop.weave;

import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.aop.filter.Filter;
import com.github.aop.util.*;
import static com.github.aop.util.SandboxStringUtils.toJavaClassName;
import static com.github.aop.util.SandboxStringUtils.toJavaClassNameArray;

import static org.objectweb.asm.ClassReader.EXPAND_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class DefaultTransformer implements Transformer{

    private static final Logger logger = LoggerFactory.getLogger(DefaultTransformer.class);

    private Filter filter;
    private int listenerId;

    public DefaultTransformer(Filter filter, int listenerId) {
        this.filter = filter;
        this.listenerId = listenerId;
    }

    @Override
    public byte[] toByteCodeArray(ClassLoader targetClassLoader, byte[] srcByteArray) throws Throwable{
        final ClassReader cr = new ClassReader(srcByteArray);

        // 如果目标对象不在类匹配范围,则主动忽略增强
        if (isIgnoreClass(cr)) {
            logger.debug("class={} is ignore by filter, return origin bytecode", cr.getClassName());
            return null;
        }
        logger.debug("class={} is matched filter, prepare to enhance.", cr.getClassName());


        // 如果定义间谍类失败了,则后续不需要增强
        try {
            SpyUtils.init();
        } catch (Throwable cause) {
            logger.warn("define Spy to target ClassLoader={} failed. class={}", targetClassLoader, cr.getClassName(), cause);
            return null;
        }

        // 返回增强后字节码
        final byte[] returnByteCodeArray = weavingEvent(targetClassLoader, srcByteArray);
        logger.debug("enhance class={} success, before bytecode.size={}; after bytecode.size={}; reWriteMark={}",
                cr.getClassName(),
                ArrayUtils.getLength(srcByteArray),
                ArrayUtils.getLength(returnByteCodeArray)
        );


        return returnByteCodeArray;
    }


    /**
     * 编织事件方法
     *
     * @param sourceByteCodeArray 原始字节码数组
     * @return 编织后的字节码数组
     */
    private byte[] weavingEvent(final ClassLoader targetClassLoader,
                                final byte[] sourceByteCodeArray) {
        final ClassReader cr = new ClassReader(sourceByteCodeArray);
        final ClassWriter cw = createClassWriter(targetClassLoader, cr);
        cr.accept(
                new MethodWriter(
                        listenerId,
                        cr.getClassName(),
                        filter,
                        cw
                ),
                EXPAND_FRAMES
        );
        return cw.toByteArray();
    }

    /**
     * 创建ClassWriter for asm
     *
     * @param cr ClassReader
     * @return ClassWriter
     */
    private ClassWriter createClassWriter(final ClassLoader targetClassLoader,
                                          final ClassReader cr) {
        return new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS) {

            /*
             * 注意，为了自动计算帧的大小，有时必须计算两个类共同的父类。
             * 缺省情况下，ClassWriter将会在getCommonSuperClass方法中计算这些，通过在加载这两个类进入虚拟机时，使用反射API来计算。
             * 但是，如果你将要生成的几个类相互之间引用，这将会带来问题，因为引用的类可能还不存在。
             * 在这种情况下，你可以重写getCommonSuperClass方法来解决这个问题。
             *
             * 通过重写 getCommonSuperClass() 方法，更正获取ClassLoader的方式，改成使用指定ClassLoader的方式进行。
             * 规避了原有代码采用Object.class.getClassLoader()的方式
             */
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                Class<?> c, d;
                try {
                    c = Class.forName(toJavaClassName(type1), false, targetClassLoader);
                    d = Class.forName(toJavaClassName(type2), false, targetClassLoader);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (c.isAssignableFrom(d)) {
                    return type1;
                }
                if (d.isAssignableFrom(c)) {
                    return type2;
                }
                if (c.isInterface() || d.isInterface()) {
                    return "java/lang/Object";
                } else {
                    do {
                        c = c.getSuperclass();
                    } while (!c.isAssignableFrom(d));
                    return SandboxStringUtils.toInternalClassName(c.getName());
                }
            }

        };
    }


    private boolean isInterface(int access) {
        return (Opcodes.ACC_INTERFACE & access) == Opcodes.ACC_INTERFACE;
    }

    private boolean isAnnotation(int access) {
        return (Opcodes.ACC_ANNOTATION & access) == Opcodes.ACC_ANNOTATION;
    }

    private boolean isEnum(int access) {
        return (Opcodes.ACC_ENUM & access) == Opcodes.ACC_ENUM;
    }

    private boolean isNative(int access) {
        return (Opcodes.ACC_NATIVE & access) == Opcodes.ACC_NATIVE;
    }

    /**
     * 是否需要忽略的access
     *
     * @param access class's access
     * @return TRUE:需要被忽略；FALSE:不能被忽略
     */
    private boolean isIgnoreAccess(final int access) {

        // 接口就没有必要增强了吧
        return isInterface(access)

                // annotation目前没有增强的必要，因为一眼就能看出答案
                || isAnnotation(access)

                // 枚举类也没有增强的必要，也是一眼就能看出答案
                || isEnum(access)

                // Native的方法暂时不支持
                || isNative(access);
    }


    /**
     * 是否需要忽略增强的类
     * @param cr                ClassReader
     * @return true:忽略增强;false:需要增强
     */
    private boolean isIgnoreClass(final ClassReader cr) {
        final int access = cr.getAccess();

        // 对一些需要忽略的access进行过滤
        return isIgnoreAccess(access)

                // 按照Filter#doClassFilter()完成过滤
                || !filter.doClassFilter(access,
                toJavaClassName(cr.getClassName()),
                toJavaClassName(cr.getSuperName()),
                toJavaClassNameArray(cr.getInterfaces()));

    }

}
