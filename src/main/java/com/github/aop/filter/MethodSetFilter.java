package com.github.aop.filter;

import java.util.Set;

public class MethodSetFilter implements Filter{

    private Set<String> methodNameSet;

    public MethodSetFilter(Set<String> methodNameList) {
        this.methodNameSet = methodNameList;
    }

    @Override
    public boolean doClassFilter(int access, String javaClassName, String superClassTypeJavaClassName, String[] interfaceTypeJavaClassNameArray) {
        return true;
    }

    @Override
    public boolean doMethodFilter(int access, String javaMethodName, String[] parameterTypeJavaClassNameArray, String[] throwsTypeJavaClassNameArray) {

        if(methodNameSet.contains(javaMethodName)) {
            return true;
        }

        return false;
    }

}
