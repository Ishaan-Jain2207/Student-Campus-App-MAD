package com.example.ishaan_student_app.util;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Utility managing disk I/O thread pool and main thread callbacks.
 */
public class AppExecutors {

    private static AppExecutors instance;

    private final Executor diskIO;
    private final Executor mainThread;

    public static synchronized AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors(Executors.newSingleThreadExecutor(), new MainThreadExecutor());
        }
        return instance;
    }

    public AppExecutors(Executor diskIO, Executor mainThread) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
    }

    public Executor diskIO() {
        return diskIO;
    }

    public Executor mainThread() {
        return mainThread;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
