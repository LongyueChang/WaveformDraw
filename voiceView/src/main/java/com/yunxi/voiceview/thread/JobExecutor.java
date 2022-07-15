package com.yunxi.voiceview.thread;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author : longyue
 * @data : 2022/3/1
 * @email : changyl@yunxi.tv
 */
public class JobExecutor implements Executor {
    private ThreadPoolExecutor threadPoolExecutor;

    public JobExecutor() {
        defaultInit();
    }

    public JobExecutor(int corePoolSize) {
        this.threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, 3, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new JobThreadFactory());
    }

    private void defaultInit() {
        threadPoolExecutor = new ThreadPoolExecutor(3, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(), new JobThreadFactory());
    }

    @Override
    public void execute(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        if (!threadPoolExecutor.isShutdown()) {
            try {
                this.threadPoolExecutor.execute(runnable);
            } catch (RejectedExecutionException e) {
                e.printStackTrace();
                defaultInit();
                this.threadPoolExecutor.execute(runnable);
            }
        } else {
            defaultInit();
            this.threadPoolExecutor.execute(runnable);
        }
    }

    private static class JobThreadFactory implements ThreadFactory {
        private int counter = 0;

        @Override
        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "update_" + counter++);
        }
    }

    public void shoutDown() {
        threadPoolExecutor.shutdown();
    }
}
