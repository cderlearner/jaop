package com.github.aop.filter;

public class MethodFilter implements Filter {

    private String targetMethodName;

    public MethodFilter(String targetMethodName) {
        this.targetMethodName = targetMethodName;
    }


    @Override
    public boolean doClassFilter(int access, String javaClassName, String superClassTypeJavaClassName, String[] interfaceTypeJavaClassNameArray) {
        return true;
    }

    @Override
    public boolean doMethodFilter(int access, String javaMethodName, String[] parameterTypeJavaClassNameArray, String[] throwsTypeJavaClassNameArray) {
        return javaMethodName.equals(targetMethodName);
    }
}
