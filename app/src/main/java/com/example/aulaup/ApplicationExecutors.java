package com.example.aulaup;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ApplicationExecutors {

    private final Executor background;
    private final Executor mainThread;
    private final Executor network;

    public Executor getBackground() {
        return background;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    public Executor getNetworkThread() {
        return network;
    }

    public ApplicationExecutors() {
        this.background = Executors.newSingleThreadExecutor();
        this.mainThread = new MainThreadExecutor();
        this.network = Executors.newSingleThreadExecutor();
    }

    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(
                Looper.getMainLooper()
        );

        @Override
        public void execute(Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}