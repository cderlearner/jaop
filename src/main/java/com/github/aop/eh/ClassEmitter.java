package com.github.aop.eh;

import net.sf.cglib.core.ClassInfo;
import net.sf.cglib.core.Constants;
import net.sf.cglib.core.Signature;
import net.sf.cglib.core.TypeUtils;
import net.sf.cglib.transform.ClassTransformer;
import org.objectweb.asm.*;

import java.util.HashMap;
import java.util.Map;

public class ClassEmitter extends ClassTransformer {
    private ClassInfo classInfo;
    private Map fieldInfo;

    private MethodVisitor rawStaticInit;

    public ClassEmitter(ClassVisitor cv) {
        setTarget(cv);
    }

    public ClassEmitter() {
        super(Opcodes.ASM5);
    }

    public void setTarget(ClassVisitor cv) {
        this.cv = cv;
        fieldInfo = new HashMap();
    }

    public ClassInfo getClassInfo() {
        return classInfo;
    }

    public void begin_class(int version, final int access, String className, final Type superType, final Type[] interfaces, String source) {
        final Type classType = Type.getType("L" + className.replace('.', '/') + ";");
        classInfo = new ClassInfo() {
            public Type getType() {
                return classType;
            }

            public Type getSuperType() {
                return (superType != null) ? superType : Constants.TYPE_OBJECT;
            }

            public Type[] getInterfaces() {
                return interfaces;
            }

            public int getModifiers() {
                return access;
            }
        };
        cv.visit(version,
                access,
                classInfo.getType().getInternalName(),
                null,
                classInfo.getSuperType().getInternalName(),
                TypeUtils.toInternalNames(interfaces));
        if (source != null)
            cv.visitSource(source, null);
        init();
    }


    protected void init() {
    }

    public int getAccess() {
        return classInfo.getModifiers();
    }

    public Type getClassType() {
        return classInfo.getType();
    }

    public Type getSuperType() {
        return classInfo.getSuperType();
    }

    public void end_class() {
        cv.visitEnd();
    }

    public CodeEmitter begin_method(int access, Signature sig, Type[] exceptions) {
        if (classInfo == null)
            throw new IllegalStateException("classInfo is null! " + this);
        MethodVisitor v = cv.visitMethod(access,
                sig.getName(),
                sig.getDescriptor(),
                null,
                TypeUtils.toInternalNames(exceptions));
        if (sig.equals(Constants.SIG_STATIC) && !TypeUtils.isInterface(getAccess())) {
            rawStaticInit = v;
            MethodVisitor wrapped = new MethodVisitor(Opcodes.ASM5, v) {
                public void visitMaxs(int maxStack, int maxLocals) {
                    // ignore
                }

                public void visitInsn(int insn) {
                    if (insn != Constants.RETURN) {
                        super.visitInsn(insn);
                    }
                }
            };
            return new CodeEmitter(this, v, access, sig, exceptions);
        } else {
            return new CodeEmitter(this, v, access, sig, exceptions);
        }
    }

    public CodeEmitter begin_static() {
        return begin_method(Constants.ACC_STATIC, Constants.SIG_STATIC, null);
    }

    public void declare_field(int access, String name, Type type, Object value) {
        FieldInfo existing = (FieldInfo) fieldInfo.get(name);
        FieldInfo info = new FieldInfo(access, name, type, value);
        if (existing != null) {
            if (!info.equals(existing)) {
                throw new IllegalArgumentException("Field \"" + name + "\" has been declared differently");
            }
        } else {
            fieldInfo.put(name, info);
            cv.visitField(access, name, type.getDescriptor(), null, value);
        }
    }

    // TODO: make public?
    boolean isFieldDeclared(String name) {
        return fieldInfo.get(name) != null;
    }

    FieldInfo getFieldInfo(String name) {
        FieldInfo field = (FieldInfo) fieldInfo.get(name);
        if (field == null) {
            throw new IllegalArgumentException("Field " + name + " is not declared in " + getClassType().getClassName());
        }
        return field;
    }

    static class FieldInfo {
        int access;
        String name;
        Type type;
        Object value;

        public FieldInfo(int access, String name, Type type, Object value) {
            this.access = access;
            this.name = name;
            this.type = type;
            this.value = value;
        }

        public boolean equals(Object o) {
            if (o == null)
                return false;
            if (!(o instanceof FieldInfo))
                return false;
            FieldInfo other = (FieldInfo) o;
            if (access != other.access ||
                    !name.equals(other.name) ||
                    !type.equals(other.type)) {
                return false;
            }
            if ((value == null) ^ (other.value == null))
                return false;
            if (value != null && !value.equals(other.value))
                return false;
            return true;
        }

        public int hashCode() {
            return access ^ name.hashCode() ^ type.hashCode() ^ ((value == null) ? 0 : value.hashCode());
        }
    }

    public void visit(int version,
                      int access,
                      String name,
                      String signature,
                      String superName,
                      String[] interfaces) {
        begin_class(version,
                access,
                name.replace('/', '.'),
                TypeUtils.fromInternalName(superName),
                TypeUtils.fromInternalNames(interfaces),
                null); // TODO
    }

    public void visitEnd() {
        end_class();
    }

    public FieldVisitor visitField(int access,
                                   String name,
                                   String desc,
                                   String signature,
                                   Object value) {
        declare_field(access, name, Type.getType(desc), value);
        return null; // TODO
    }

    public MethodVisitor visitMethod(int access,
                                     String name,
                                     String desc,
                                     String signature,
                                     String[] exceptions) {
        return begin_method(access,
                new Signature(name, desc),
                TypeUtils.fromInternalNames(exceptions));
    }
}
