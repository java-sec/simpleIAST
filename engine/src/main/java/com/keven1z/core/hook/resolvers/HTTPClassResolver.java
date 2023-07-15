package com.keven1z.core.hook.resolvers;

import com.keven1z.core.hook.server.detectors.ServerEnum;
import com.keven1z.core.hook.http.handlers.HttpRequestHandler;
import com.keven1z.core.hook.http.handlers.TomcatHttpHandler;
import com.keven1z.core.hook.spy.HookSpy;
import com.keven1z.core.log.ErrorType;
import com.keven1z.core.log.LogTool;
import com.keven1z.core.policy.Policy;
import com.keven1z.core.utils.PolicyUtils;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Http hook点的解析器
 *
 * @author keven1z
 * @date 2023/01/15
 */
public class HTTPClassResolver implements HandlerHookClassResolver {
    private final Map<ServerEnum, HttpRequestHandler> requestHandlerMap = new LinkedHashMap<>();

    public HTTPClassResolver() {
        requestHandlerMap.put(ServerEnum.TOMCAT, new TomcatHttpHandler());
    }

    @Override
    public void resolve(Object returnObject, Object thisObject, Object[] parameters, String className, String method, String desc, String policyName, boolean isEnter) {
        Policy policy = PolicyUtils.getHookedPolicy(className, method, desc, HookSpy.policyContainer.getHttp());
        if (policy == null) {
            if (LogTool.isDebugEnabled()) {
                LogTool.warn(ErrorType.POLICY_ERROR, "Can't match the policy,className:" + className + ",method:" + method + ",desc:" + desc);
            }
            return;
        }

        ServerEnum serverEnum = ServerEnum.valueOf(policyName);
        HttpRequestHandler httpRequestHandler = requestHandlerMap.get(serverEnum);
        if (httpRequestHandler == null) {
            LogTool.warn(ErrorType.RESOLVE_ERROR, "HttpRequestHandler is null");
            return;
        }
        httpRequestHandler.handler(thisObject, method, desc, parameters, isEnter);

    }

}
