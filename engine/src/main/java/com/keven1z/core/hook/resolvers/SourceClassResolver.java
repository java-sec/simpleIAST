package com.keven1z.core.hook.resolvers;

import com.keven1z.core.EngineController;
import com.keven1z.core.consts.PolicyConst;
import com.keven1z.core.hook.spy.HookSpy;
import com.keven1z.core.graph.taint.TaintData;
import com.keven1z.core.graph.taint.TaintGraph;
import com.keven1z.core.log.ErrorType;
import com.keven1z.core.log.LogTool;
import com.keven1z.core.policy.Policy;
import com.keven1z.core.policy.PolicyTypeEnum;
import com.keven1z.core.utils.ClassUtils;
import com.keven1z.core.utils.PolicyUtils;
import com.keven1z.core.utils.StringUtils;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;


import static com.keven1z.core.consts.CommonConst.OFF;
import static com.keven1z.core.consts.CommonConst.ON;
import static com.keven1z.core.hook.spy.HookThreadLocal.TAINT_GRAPH_THREAD_LOCAL;
import static com.keven1z.core.utils.PolicyUtils.getPositionObject;

/**
 * 污染源的解析器
 *
 * @author keven1z
 * @date 2023/01/15
 */
public class SourceClassResolver implements HandlerHookClassResolver {
    private static final String[] USER_PACKAGE_PREFIX = new String[]{"java", "javax", " org.spring".substring(1), " org.apache".substring(1), " io.undertow".substring(1)};

    @Override
    public void resolve(Object returnObject, Object thisObject, Object[] parameters, String className, String method, String desc, String policyName, boolean isEnter) {
        if (returnObject == null || returnObject.equals("")) {
            return;
        }

        Policy policy = PolicyUtils.getHookedPolicy(className, method, desc, HookSpy.policyContainer.getSource());
        if (policy == null) {
            if (LogTool.isDebugEnabled()) {
                LogTool.warn(ErrorType.POLICY_ERROR, "Can't match the policy,className:" + className + ",method:" + method + ",desc:" + desc);
            }
            return;
        }

        String to = policy.getTo();
        List<Object> taintObjects = getPositionObject(to, parameters, returnObject, thisObject);
        if (taintObjects.isEmpty()) {
            return;
        }
        String from = policy.getFrom();
        List<Object> formList = PolicyUtils.getPositionObject(from, parameters, returnObject, thisObject);
        if (formList.isEmpty()) {
            return;
        }
        //Source的to只允许一个
        Object taintObject = taintObjects.get(0);
        TaintData taintData = new TaintData(className, method, desc);
        taintData.setToObjectHashCode(System.identityHashCode(taintObject));
        taintData.setToValue(taintObject.toString());
        taintData.setType(PolicyTypeEnum.SOURCE);
        taintData.setFromValue(formList.get(0).toString());
        taintData.setTaintValueType(taintObject.getClass().getTypeName());
        TaintGraph taintGraph = TAINT_GRAPH_THREAD_LOCAL.get();
        taintGraph.addNode(taintData);

        //如果污染源为用户对象，则判断为bean对象，将其对象所有get方法加入hook策略
        if (!StringUtils.isStartsWithElementInArray(taintObject.getClass().getName(), USER_PACKAGE_PREFIX)) {
            addBeanObjectPolicy(taintObject.getClass());
        }

    }

    /**
     * 将污染对象的所有get方法作为污染源
     *
     * @param taintClass 污染对象
     */
    private void addBeanObjectPolicy(Class<?> taintClass) {
        List<Method> toBeTransformedMethods = getMethodToBeTransformed(taintClass);
        if (toBeTransformedMethods.isEmpty()) {
            return;
        }

        Instrumentation inst = EngineController.context.getInstrumentation();
        if (inst == null) {
            return;
        }
        Class<?>[] loadedClasses = inst.getAllLoadedClasses();
        for (Method method : toBeTransformedMethods) {
            String name = method.getName();
            String taintClassName = method.getDeclaringClass().getName();
            Policy policy = new Policy(taintClassName.replace(".", "/"), name, ClassUtils.classToSmali(method.getReturnType()));
            policy.setFrom(PolicyConst.O);
            policy.setTo(PolicyConst.R);
            policy.setState(ON);
            policy.setEnter(OFF);
            policy.setExit(ON);
            policy.setType(PolicyTypeEnum.SOURCE);
            HookSpy.policyContainer.addPolicy(policy);
            reTransform(taintClassName, inst, loadedClasses);
        }

    }

    public List<Method> getMethodToBeTransformed(Class<?> taintClass) {
        Method[] methods = taintClass.getMethods();
        List<Method> toBeTransformedMethods = new ArrayList<>();
        for (Method method : methods) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers) || Modifier.isNative(modifiers)) {
                continue;
            }

            String taintClassName = method.getDeclaringClass().getName();
            String name = method.getName();
            if (name.startsWith("get")) {
                if (ClassUtils.classIsInteger(method.getReturnType())){
                    continue;
                }

                if (StringUtils.isStartsWithElementInArray(taintClassName, USER_PACKAGE_PREFIX)) {
                    continue;
                }

                if (!PolicyUtils.isExistInPolicy(taintClassName.replace(".", "/"), HookSpy.policyContainer.getSource())) {
                    toBeTransformedMethods.add(method);
                }
            }
        }
        return toBeTransformedMethods;
    }

    private void reTransform(String transformClassName, Instrumentation inst, Class<?>[] loadedClasses) {
        for (Class<?> clazz : loadedClasses) {
            if (transformClassName.equals(clazz.getName())) {
                try {
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    LogTool.error(ErrorType.TRANSFORM_ERROR, "In the source stage,transform " + transformClassName + " error");
                }
            }
        }
    }
}
