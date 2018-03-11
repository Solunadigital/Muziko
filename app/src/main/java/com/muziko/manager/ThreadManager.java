package com.muziko.manager;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Process;
import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import hugo.weaving.DebugLog;

/**
 * Created by Bradley on 14/05/2017.
 */
public class ThreadManager {
    private static final String TAG = ThreadManager.class.getName();
    private static ThreadManager instance;
    private HandlerThread muzikoMediaObserverThread;
    private Handler muzikoMediaObserverHandler;
    private ExecutorService muzikoBackgroundThreadPool;
    private ExecutorService muzikoAppStartPool;
    private ExecutorService muzikoTransferPool;
    private ScheduledExecutorService muzikoScheduledThreadPool;

    //no outer class can initialize this class's object
    private ThreadManager() {
    }

    public static ThreadManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new ThreadManager();
        }
        return instance;
    }

    public ExecutorService getMuzikoBackgroundThreadPool() {
        return muzikoBackgroundThreadPool;
    }

    public ExecutorService getMuzikoTransferPool() {
        return muzikoTransferPool;
    }

    public void init() {
        muzikoBackgroundThreadPool =
                Executors.newCachedThreadPool(new WorkerThreadFactory("MuzikoBackgroundThread"));

        muzikoTransferPool =
                Executors.newSingleThreadExecutor(new WorkerThreadFactory("muzikoTransferPool"));

        muzikoScheduledThreadPool =
                Executors.newScheduledThreadPool(
                        4, new WorkerThreadFactory("MuzikoContinuousThread"));

        muzikoAppStartPool =
                Executors.newSingleThreadExecutor(new WorkerThreadFactory("MuzikoAppStartThread"));
    }

    @DebugLog
    public void shutdownAppStartPool() {
        Log.i(TAG, "MuzikoAppStartThread shutting down");
        muzikoAppStartPool.shutdown();
    }

    @DebugLog
    public void submitTransferThreadPool(Runnable runnable) {
        muzikoTransferPool.submit(runnable);
    }

    public void submitToBackgroundThreadPool(Runnable runnable) {
        muzikoBackgroundThreadPool.submit(runnable);
    }

    public void submitToContinuousThreadPool(Runnable runnable, int delay) {
        muzikoScheduledThreadPool.schedule(runnable, delay, TimeUnit.MILLISECONDS);
    }

    public void submitToAppStartThreadPool(Runnable runnable) {
        try {
            muzikoAppStartPool.submit(runnable);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public Handler getMuzikoMediaObserverHandler() {

        if (muzikoMediaObserverThread == null || !muzikoMediaObserverThread.isAlive()) {
            muzikoMediaObserverThread =
                    new HandlerThread("MuzikoMediaObserverThread", Process.THREAD_PRIORITY_LOWEST);
            muzikoMediaObserverThread.start();
            muzikoMediaObserverHandler = new Handler(muzikoMediaObserverThread.getLooper());
        }
        return muzikoMediaObserverHandler;
    }

    private class WorkerThreadFactory implements ThreadFactory {
        private int counter = 0;
        private String prefix = "";

        public WorkerThreadFactory(String prefix) {
            this.prefix = prefix;
        }

        public Thread newThread(@NonNull Runnable runnable) {
            Thread thread = new Thread(runnable, prefix + "-" + counter++);
            thread.setUncaughtExceptionHandler((t1, e) -> Crashlytics.logException(e));

            return thread;
        }
    }

//    public static void runButNotOn(Runnable toRun, Thread notOn) {
//        if (Thread.currentThread() == notOn) {
//            THREADPOOL.submit(toRun);
//        } else {
//            toRun.run();
//        }
//    }
}
