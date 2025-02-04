package com.keven1z.core.hook.spy;

import com.keven1z.core.graph.taint.TaintGraph;
import com.keven1z.core.hook.http.request.AbstractRequest;
import com.keven1z.core.vulnerability.report.ReportMessage;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

import static com.keven1z.core.Config.MAX_REPORT_QUEUE_SIZE;

public class HookThreadLocal {
    /**
     * hook锁，防止在hook过程中调用方法递归hook，导致栈溢出
     * 注意：当在初始化hook时，若在doSpy方法中存在class初始化，则该class并不会被{@link com.keven1z.core.hook.transforms.HookTransformer}中transform方法捕获，导致hook点的丢失
     */
    public static final ThreadLocal<Boolean> enableHookLock = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    /**
     * 是否存在疑似漏洞的污点图
     */
    public static final ThreadLocal<Boolean> isSuspectedTaint = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    /**
     * 请求是否结束
     */
    public static final ThreadLocal<Boolean> isRequestEnd = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };
    /**
     * 上报队列是否已满，若满不进行hook
     */
    public static final ThreadLocal<Boolean> IS_REPORT_QUEUE_FULL = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return false;
        }
    };

    /**
     * 当前线程的污点传播图数据
     */
    public static final ThreadLocal<TaintGraph> TAINT_GRAPH_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 当前线程请求流量
     */
    public static final ThreadLocal<AbstractRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    public static final int INVOKE_ID_INIT_VALUE = 1;

    public static final AtomicInteger INVOKE_ID = new AtomicInteger(INVOKE_ID_INIT_VALUE);

    public static final LinkedBlockingQueue<ReportMessage> REPORT_QUEUE = new LinkedBlockingQueue<>(MAX_REPORT_QUEUE_SIZE);
    /**
     * 请求消耗的时间计算
     */
    public static final ThreadLocal<Long> REQUEST_TIME_CONSUMED = new ThreadLocal<>();

}
