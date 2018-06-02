package com.github.aop.weave;

import com.github.aop.log.LoggerFactory;
import org.apache.commons.lang3.ArrayUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import com.github.aop.filter.Filter;
import com.github.aop.util.*;
import com.github.aop.log.api.*;
import static com.github.aop.util.AopUtils.toJavaClassName;
import static com.github.aop.util.AopUtils.toJavaClassNameArray;

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
        logger.debug("enhance class={} success, before bytecode.size={}; after bytecode.size={}",
                cr.getClassName(),
                ArrayUtils.getLength(srcByteArray),
                ArrayUtils.getLength(returnByteCodeArray)
        );


        return returnByteCodeArray;
    }

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

    private ClassWriter createClassWriter(final ClassLoader targetClassLoader,
                                          final ClassReader cr) {
        return new ClassWriter(cr, COMPUTE_FRAMES | COMPUTE_MAXS) {

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
                    return AopUtils.toInternalClassName(c.getName());
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
