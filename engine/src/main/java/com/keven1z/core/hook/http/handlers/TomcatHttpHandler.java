package com.keven1z.core.hook.http.handlers;


import com.keven1z.core.Config;
import com.keven1z.core.graph.taint.TaintGraph;
import com.keven1z.core.hook.http.request.AbstractRequest;
import com.keven1z.core.hook.http.request.CoyoteRequest;
import com.keven1z.core.hook.http.request.HttpServletRequest;
import com.keven1z.core.log.LogTool;
import com.keven1z.core.model.ApplicationModel;
import com.keven1z.core.utils.ReflectionUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.keven1z.core.hook.spy.HookThreadLocal.*;

/**
 * @author keven1z
 * @date 2023/01/15
 */
public class TomcatHttpHandler implements HttpRequestHandler {
    private final CopyOnWriteArrayList<String> REQUEST_CACHE = new CopyOnWriteArrayList<>();
    private static final int MAX_REQUEST_CACHE_SIZE = 1024;

    /**
     * 过滤静态页面
     */
    private static boolean filterHttp(String url) {
        for (String prefix : FILTER_HTTP_PREFIX) {
            if (url.endsWith(prefix)) {
                return true;
            }
        }
        return false;

    }

    @Override
    public void handler(Object thisObject, String method, String desc, Object[] parameters, boolean isEnter) {
        if ("service".equals(method)) {
            handlerRR(parameters, isEnter);
        } else if ("endRequest".equals(method)) {
            if (REQUEST_THREAD_LOCAL.get() != null) {
                this.OnRequestExit(thisObject);
            }
        }
    }

    /**
     * 处理request和response
     */
    private void handlerRR(Object[] parameters, boolean isEnter) {
        if (isEnter) {
            if (REQUEST_THREAD_LOCAL.get() == null) {
                this.OnRequestEnter(parameters[0], parameters[1]);
            }
        } else {
            if (REQUEST_THREAD_LOCAL.get() == null) {
                return;
            }
            System.out.println("[SimpleIAST] 请求消耗时间:" + (System.currentTimeMillis() - REQUEST_TIME_CONSUMED.get()) + "ms");
            REQUEST_TIME_CONSUMED.remove();
            if (LogTool.isDebugEnabled()) {
                logger.info("[" + REQUEST_THREAD_LOCAL.get().getRequestId() + "] Request exit,URL:" + REQUEST_THREAD_LOCAL.get().getRequestURLString());
            }
            isRequestEnd.set(true);
        }
    }

    /**
     * @param request  请求对象
     * @param response 响应对象
     */
    private void OnRequestEnter(Object request, Object response) {

        String standardStart = ApplicationModel.getApplicationInfo().get("StandardStart");
        AbstractRequest abstractRequest;
        if (Objects.equals(standardStart, "false")) {
            abstractRequest = new HttpServletRequest(request);
        } else {
            abstractRequest = new CoyoteRequest(request);
        }

        if (filterHttp(abstractRequest.getRequestURLString())) {
            return;
        }

        /*
         * 请求缓存，同一请求不再进行扫描
         */
        String requestURI = abstractRequest.getRequestURI();
        if (requestURI != null) {
            String[] strings = requestURI.split("/");
            String generalizationUri = requestURI;
            String lastUri = strings[strings.length - 1];
            if (lastUri.matches("-?\\d+(\\.\\d+)?")) {
                generalizationUri = generalizationUri.replace(lastUri, "{}");
            }

            if (REQUEST_CACHE.contains(generalizationUri)) {
                return;
            }
            if (REQUEST_CACHE.size() <= MAX_REQUEST_CACHE_SIZE) {
                REQUEST_CACHE.add(generalizationUri);
            }
        }


        REQUEST_TIME_CONSUMED.set(System.currentTimeMillis());
        REQUEST_THREAD_LOCAL.set(abstractRequest);
        TAINT_GRAPH_THREAD_LOCAL.set(new TaintGraph());
        if (LogTool.isDebugEnabled()) {
            logger.info("[" + REQUEST_THREAD_LOCAL.get().getRequestId() + "] Request Enter,URL:" + abstractRequest.getRequestURLString());
        }
    }

    private void OnRequestExit(Object thisObject) {
        System.out.println("[SimpleIAST] 请求消耗时间:" + (System.currentTimeMillis() - REQUEST_TIME_CONSUMED.get()) + "ms");
        REQUEST_TIME_CONSUMED.remove();
        if (LogTool.isDebugEnabled()) {
            logger.info("[" + REQUEST_THREAD_LOCAL.get().getRequestId() + "] Request exit,URL:" + REQUEST_THREAD_LOCAL.get().getRequestURLString());
        }
        isRequestEnd.set(true);
        //如果没有漏洞不获取报文
        if (!isSuspectedTaint.get()) {
            return;
        }
        try {
            Object inputBuffer = ReflectionUtils.getField(thisObject, "inputBuffer");
            Object byteBuffer = ReflectionUtils.invokeMethod(inputBuffer, "getByteBuffer", AbstractRequest.EMPTY_CLASS);
            byte[] bytes = (byte[]) ReflectionUtils.invokeMethod(byteBuffer, "array", AbstractRequest.EMPTY_CLASS);
            int limit = (Integer) ReflectionUtils.invokeMethod(byteBuffer, "limit", AbstractRequest.EMPTY_CLASS);
            limit = Math.min(limit, Config.MAX_REQUEST_MESSAGE_LENGTH);
            bytes = Arrays.copyOfRange(bytes, 0, limit);
            REQUEST_THREAD_LOCAL.get().setRequestDetail(bytes);
        } catch (Exception e) {
            logger.error("Failed to Get Request Body", e);
        }
    }
}
