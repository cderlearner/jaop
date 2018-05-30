package com.github.aop.weave;

import com.github.aop.eh.Eh;
import com.github.aop.filter.Filter;
import com.github.aop.listener.EventListener;
import com.github.aop.listener.EventListenerHandlers;
import com.github.aop.util.ReflectUtils;
import com.github.aop.util.AopUtils;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class WeaveManager implements IWeaveManager{

    private final EventListenerHandlers listenerHandlers = EventListenerHandlers.getSingleton();

    private static final IWeaveManager instance = new WeaveManager();

    private WeaveManager() {}

    public static IWeaveManager getSingleton() {
        return instance;
    }

    @Override
    public Class<?> weave(final Class targetClass,
                          final String targetJavaMethodName,
                          final EventListener listener) throws Throwable{

        final int listenerId = listenerHandlers.createAndActiveListenerId(listener);

        final ClassLoader loader = newClassLoader(targetClass);

        final byte[] srcByteCodeArray = toByteArray(targetClass);

        final Transformer enhancer = new DefaultTransformer(
                new Filter() {
                    @Override
                    public boolean doClassFilter(final int access,
                                                 final String javaClassName,
                                                 final String superClassTypeJavaClassName,
                                                 final String[] interfaceTypeJavaClassNameArray) {
                        return true;
                    }

                    @Override
                    public boolean doMethodFilter(final int access,
                                                  final String javaMethodName,
                                                  final String[] parameterTypeJavaClassNameArray,
                                                  final String[] throwsTypeJavaClassNameArray) {
                        return javaMethodName.equals(targetJavaMethodName);
                    }
                },
                listenerId
        );

        byte[] data = enhancer.toByteCodeArray(loader, srcByteCodeArray);

        FileOutputStream fos = new FileOutputStream("E:\\ljx\\asm\\LJX$$HHH.class");
        fos.write(data);
        fos.close();

        return ReflectUtils.defineClass(loader, targetClass.getName(), data);
    }

    protected ClassLoader newClassLoader(Class clazz) {
        return new ProxyClassLoader(clazz.getClassLoader());
    }


    @Override
    public Class weave(Class targetClass, Filter filter, String targetMethodName, EventListener eventListener) throws Throwable {
        //TODO
        return null;
    }

    @Override
    public Class weaveExten(Class targetClass, String targetMethodName, EventListener eventListener) throws Throwable {

        final byte[] srcByteCodeArray = enhancer(targetClass);

        final int listenerId = listenerHandlers.createAndActiveListenerId(eventListener);

        final ClassLoader loader = newClassLoader(targetClass);

        final Transformer enhancer = new DefaultTransformer(
                new Filter() {
                    @Override
                    public boolean doClassFilter(final int access,
                                                 final String javaClassName,
                                                 final String superClassTypeJavaClassName,
                                                 final String[] interfaceTypeJavaClassNameArray) {
                        return true;
                    }

                    @Override
                    public boolean doMethodFilter(final int access,
                                                  final String javaMethodName,
                                                  final String[] parameterTypeJavaClassNameArray,
                                                  final String[] throwsTypeJavaClassNameArray) {
                        return javaMethodName.equals(targetMethodName);
                    }
                },
                listenerId
        );

        byte[] data = enhancer.toByteCodeArray(loader, srcByteCodeArray);

        return ReflectUtils.defineClass(loader, targetClass.getName(), data);
    }

    @Override
    public Class weave(Class targetClass, List<String> targetMethodNames, EventListener eventListener) throws Throwable {
        //TODO
        return null;
    }

    private byte[] enhancer(Class targetClass) throws Throwable{
        byte[] sourceCode = toByteArray(targetClass);
        ClassReader cr = new ClassReader(sourceCode);
        ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_MAXS);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public void visit(int version, int access, String className, String signature, String superClass, String[] interfaces) {
                super.visit(version, access, className, signature, superClass, interfaces);
            }
        };
        Eh eh = new Eh();
        eh.setSuperclass(targetClass);
        eh.generateClass(cv);
        return cw.toByteArray();
    }

    private byte[] toByteArray(final Class<?> targetClass) throws IOException {
        final InputStream is = targetClass.getResourceAsStream("/" + AopUtils.toInternalClassName(targetClass.getName()).concat(".class"));
        try {
            return IOUtils.toByteArray(is);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }



}
