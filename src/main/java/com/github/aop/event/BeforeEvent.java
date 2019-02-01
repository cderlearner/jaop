package com.github.aop.event;

public class BeforeEvent extends InvokeEvent {

    /**
     * 触发调用事件的ClassLoader
     */
    public ClassLoader javaClassLoader;

    /**
     * 获取触发调用事件的类名称
     */
    public String javaClassName;

    /**
     * 获取触发调用事件的方法名称
     */
    public String javaMethodName;

    /**
     * 获取触发调用事件的方法签名
     */
    public String javaMethodDesc;

    /**
     * 获取触发调用事件的对象
     */
    public Object target;

    /**
     * 获取触发调用事件的方法参数
     */
    public Object[] argumentArray;

    /**
     * 构造调用BEFORE事件
     *
     * @param javaClassLoader 触发调用事件的ClassLoader
     * @param javaClassName   触发调用事件的类名称<span>java.lang.String</span>
     * @param javaMethodName  触发调用事件的方法名称
     * @param javaMethodDesc  触发调用事件的方法签名
     * @param target          触发调用事件的对象(静态方法为null)
     * @param argumentArray   触发调用事件的方法参数
     */
    public BeforeEvent(ClassLoader javaClassLoader, String javaClassName,
                       String javaMethodName, String javaMethodDesc,
                       Object target, Object[] argumentArray) {
        super(Type.BEFORE);
        this.javaClassLoader = javaClassLoader;
        this.javaClassName = javaClassName;
        this.javaMethodName = javaMethodName;
        this.javaMethodDesc = javaMethodDesc;
        this.target = target;
        this.argumentArray = argumentArray;
    }

}
