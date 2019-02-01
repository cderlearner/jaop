package com.github.aop.eh;

import net.sf.cglib.core.ClassEmitter;
import net.sf.cglib.core.CodeEmitter;
import net.sf.cglib.core.*;
import net.sf.cglib.core.EmitUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;
import java.util.*;

/**
 * 代码增强器 生产子类
 */
public class MyEnhancer {
    private static final Type FACTORY =
            TypeUtils.parseType("com.github.aop.eh.CgFactory");

    private static final Signature SINGLE_NEW_INSTANCE =
            new Signature("newInstance", Constants.TYPE_OBJECT, new Type[0]);

    private Class[] interfaces;
    private Class superclass;
    private boolean useFactory = true;

    private String newClassName;
    private Type newType;

    public void setSuperclass(Class superclass) {
        if (superclass != null && superclass.isInterface()) {
            setInterfaces(new Class[]{superclass});
        } else if (superclass != null && superclass.equals(Object.class)) {
            // affects choice of ClassLoader
            this.superclass = null;
        } else {
            this.superclass = superclass;
        }

        newClassName = superclass.getName() + "$$EnhancerByLJX$$5078ddf5";
        newType = TypeUtils.parseType(newClassName);
    }

    public void setInterfaces(Class[] interfaces) {
        this.interfaces = interfaces;
    }

    public static void getMethods(Class superclass, Class[] interfaces, List methods) {
        getMethods(superclass, interfaces, methods, null, null);
    }

    private static void getMethods(Class superclass, Class[] interfaces, List methods, List interfaceMethods, Set forcePublic) {
        ReflectUtils.addAllMethods(superclass, methods);
        List target = (interfaceMethods != null) ? interfaceMethods : methods;
        if (interfaces != null) {
            for (int i = 0; i < interfaces.length; i++) {
                if (interfaces[i] != CgFactory.class) {
                    ReflectUtils.addAllMethods(interfaces[i], target);
                }
            }
        }
        if (interfaceMethods != null) {
            if (forcePublic != null) {
                forcePublic.addAll(MethodWrapper.createSet(interfaceMethods));
            }
            methods.addAll(interfaceMethods);
        }
        CollectionUtils.filter(methods, new RejectModifierPredicate(Constants.ACC_STATIC));
        CollectionUtils.filter(methods, new VisibilityPredicate(superclass, true));
        CollectionUtils.filter(methods, new DuplicatesPredicate());
        CollectionUtils.filter(methods, new RejectModifierPredicate(Constants.ACC_FINAL));
    }

    public void generateClass(ClassVisitor v) throws Exception {
        Class sc = (superclass == null) ? Object.class : superclass;

        if (TypeUtils.isFinal(sc.getModifiers()))
            throw new IllegalArgumentException("Cannot subclass final class " + sc.getName());
        List constructors = new ArrayList(Arrays.asList(sc.getDeclaredConstructors()));

        List actualMethods = new ArrayList();
        List interfaceMethods = new ArrayList();
        final Set forcePublic = new HashSet();
        getMethods(sc, interfaces, actualMethods, interfaceMethods, forcePublic);

        List methods = CollectionUtils.transform(actualMethods, new Transformer() {
            public Object transform(Object value) {
                Method method = (Method) value;
                int modifiers = Constants.ACC_FINAL
                        | (method.getModifiers()
                        & ~Constants.ACC_ABSTRACT
                        & ~Constants.ACC_NATIVE
                        & ~Constants.ACC_SYNCHRONIZED);
                if (forcePublic.contains(MethodWrapper.create(method))) {
                    modifiers = (modifiers & ~Constants.ACC_PROTECTED) | Constants.ACC_PUBLIC;
                }
                return ReflectUtils.getMethodInfo(method, modifiers);
            }
        });

        ClassEmitter e = new ClassEmitter(v);
        e.begin_class(Constants.V1_2,
                Constants.ACC_PUBLIC,
                newClassName,
                Type.getType(sc),
                (useFactory ?
                        TypeUtils.add(TypeUtils.getTypes(interfaces), FACTORY) :
                        TypeUtils.getTypes(interfaces)),
                Constants.SOURCE_FILE);
        List constructorInfo = CollectionUtils.transform(constructors, MethodInfoTransformer.getInstance());

        emitMethods(e, methods, actualMethods);
        emitConstructors(e, constructorInfo);


        if (useFactory) {
            emitNewInstance(e);
        }

        e.end_class();
    }

    private void emitConstructors(ClassEmitter ce, List constructors) {
        boolean seenNull = false;
        for (Iterator it = constructors.iterator(); it.hasNext(); ) {
            MethodInfo constructor = (MethodInfo) it.next();
            if (!"()V".equals(constructor.getSignature().getDescriptor())) {
                continue;
            }
            CodeEmitter e = EmitUtils.begin_method(ce, constructor, Constants.ACC_PUBLIC);
            e.load_this();
            e.dup();
            e.load_args();
            Signature sig = constructor.getSignature();
            seenNull = seenNull || sig.getDescriptor().equals("()V");
            e.super_invoke_constructor(sig);

            e.return_value();
            e.end_method();
        }
    }

    private Type getThisType(CodeEmitter e) {
        return newType;
    }

    private void emitNewInstance(ClassEmitter ce) {
        CodeEmitter e = ce.begin_method(Constants.ACC_PUBLIC, SINGLE_NEW_INSTANCE, null);
        emitCommonNewInstance(e);
    }

    private void emitCommonNewInstance(CodeEmitter e) {
        Type thisType = getThisType(e);
        e.new_instance(thisType);
        e.dup();
        e.invoke_constructor(thisType);
        e.return_value();
        e.end_method();
    }

    private void emitMethods(final ClassEmitter ce, List methods, List actualMethods) {
        final Map declToBridge = new HashMap();

        Iterator it1 = methods.iterator();
        Iterator it2 = (actualMethods != null) ? actualMethods.iterator() : null;

        while (it1.hasNext()) {
            MethodInfo method = (MethodInfo) it1.next();
            Method actualMethod = (it2 != null) ? (Method) it2.next() : null;

            // Optimization: build up a map of Class -> bridge methods in class
            // so that we can look up all the bridge methods in one pass for a class.
            if (TypeUtils.isBridge(actualMethod.getModifiers())) {
                Set bridges = (Set) declToBridge.get(actualMethod.getDeclaringClass());
                if (bridges == null) {
                    bridges = new HashSet();
                    declToBridge.put(actualMethod.getDeclaringClass(), bridges);
                }
                bridges.add(method.getSignature());
            }
        }

        for (Iterator it = methods.iterator(); it.hasNext(); ) {
            MethodInfo method = (MethodInfo) it.next();
            CodeEmitter e = EmitUtils.begin_method(ce, method);

            e.load_this();
            e.load_args();
            e.super_invoke(method.getSignature());
            e.return_value();
            e.end_method();
        }


    }

}
