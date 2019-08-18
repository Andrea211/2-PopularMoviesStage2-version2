package com.example.popularmovies;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executors;

public class Executor {

    // For Singleton instantiation
    private static final Object LOCK = new Object();
    private static Executor sInstance;
    private final java.util.concurrent.Executor diskIO;
    private final java.util.concurrent.Executor mainThread;
    private final java.util.concurrent.Executor networkIO;

    private Executor(java.util.concurrent.Executor diskIO, java.util.concurrent.Executor mainThread, java.util.concurrent.Executor networkIO) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
        this.networkIO = networkIO;
    }

    public static Executor getInstance() {
        if (sInstance == null) {
            synchronized (LOCK) {
                sInstance = new Executor(Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3),
                        new MainThreadExecutor());
            }
        }
        return sInstance;
    }

    public java.util.concurrent.Executor diskIO() {
        return diskIO;
    }

    public java.util.concurrent.Executor mainThread() {
        return mainThread;
    }

    public java.util.concurrent.Executor networkIO() {
        return networkIO;
    }

    private static class MainThreadExecutor implements java.util.concurrent.Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }

}
