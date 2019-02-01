package com.github.aop.weave;

import com.github.aop.eh.MyEnhancer;
import com.github.aop.exception.AOPException;
import com.github.aop.filter.Filter;
import com.github.aop.filter.MethodFilter;
import com.github.aop.filter.MethodSetFilter;
import com.github.aop.listener.EventListener;
import com.github.aop.listener.EventListenerHandlers;
import com.github.aop.util.AopUtils;
import com.github.aop.util.ReflectUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.HashSet;

/**
 * 字节码增强管理器
 * ljx
 */
public class WeaveManager implements IWeaveManager {

    private final EventListenerHandlers listenerHandlers = EventListenerHandlers.getSingleton();
    private static final IWeaveManager instance = new WeaveManager();

    private WeaveManager() { }

    public static IWeaveManager getSingleton() {
        return instance;
    }

    @Override
    public Class<?> weave(final Class targetClass,
                          final String targetJavaMethodName,
                          final EventListener listener) throws AOPException {
        Filter methodFilter = new MethodFilter(targetJavaMethodName);
        final ClassLoader loader = newClassLoader(targetClass);
        byte[] data = transform(targetClass, loader, listener, methodFilter);
        return ReflectUtils.defineClass(loader, targetClass.getName(), data);
    }

    @Override
    public Class weave(Class targetClass, Filter filter, String targetMethodName, EventListener eventListener) throws AOPException {
        final ClassLoader loader = newClassLoader(targetClass);
        byte[] data = transform(targetClass, loader, eventListener, filter);
        return ReflectUtils.defineClass(loader, targetClass.getName(), data);
    }

    @Override
    public Class weave(Class targetClass, List<String> targetMethodNames, EventListener eventListener) throws AOPException {
        MethodSetFilter methodSetFilter = new MethodSetFilter(new HashSet<>(targetMethodNames));
        final ClassLoader loader = newClassLoader(targetClass);
        byte[] data = transform(targetClass, loader, eventListener, methodSetFilter);
        return ReflectUtils.defineClass(loader, targetClass.getName(), data);
    }

    @Override
    public Class weaveSubClass(Class targetClass, String targetMethodName, EventListener eventListener) throws AOPException {
        final byte[] srcByteCodeArray = enhancer(targetClass);
        final int listenerId = listenerHandlers.createAndActiveListenerId(eventListener);
        final ClassLoader loader = newClassLoader(targetClass);
        final Transformer transformer = new DefaultTransformer(
                new MethodFilter(targetMethodName),
                listenerId
        );
        byte[] data = transformer.toByteCodeArray(loader, srcByteCodeArray);
        return ReflectUtils.defineClass(loader, targetClass.getName(), data);
    }

    private byte[] transform(Class targetClass,
                             ClassLoader loader,
                             EventListener listener,
                             Filter filter) throws AOPException {
        final int listenerId = listenerHandlers.createAndActiveListenerId(listener);
        final byte[] srcByteCodeArray = toByteArray(targetClass);
        final Transformer transformer = new DefaultTransformer(
                filter,
                listenerId
        );
        return transformer.toByteCodeArray(loader, srcByteCodeArray);
    }

    private byte[] enhancer(Class targetClass) throws AOPException {
        byte[] sourceCode = toByteArray(targetClass);
        ClassReader cr = new ClassReader(sourceCode);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public void visit(int version, int access, String className, String signature, String superClass, String[] interfaces) {
                super.visit(version, access, className, signature, superClass, interfaces);
            }
        };
        MyEnhancer eh = new MyEnhancer();
        eh.setSuperclass(targetClass);
        try {
            eh.generateClass(cv);
        } catch (Exception e) {
            throw new AOPException(e);
        }
        return cw.toByteArray();
    }

    private byte[] toByteArray(final Class<?> targetClass) throws AOPException {
        final InputStream is = targetClass.getResourceAsStream("/" + AopUtils.toInternalClassName(targetClass.getName()).concat(".class"));
        try {
            return IOUtils.toByteArray(is);
        } catch (IOException e) {
            throw new AOPException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private ClassLoader newClassLoader(Class clazz) {
        return new ProxyClassLoader(clazz.getClassLoader());
    }

}
