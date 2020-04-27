/*
 * Copyright (C) 2020 Shoonya Enterprises Inc. All rights Reserved.
 */
package io.esper.managedappconfigurationsample;

import android.app.Activity;
import android.content.RestrictionsManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    private final HandlerThread workerThread = new HandlerThread(TAG);
    private final Handler workerThreadHandler;

    {
        workerThread.start();
        workerThreadHandler = new Handler(workerThread.getLooper());
    }

    private TextView configsView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        configsView = findViewById(R.id.config);
        findViewById(R.id.fetch_config).setOnClickListener(v -> {
            try {
                fetchConfig();
            } catch (RuntimeException e) {
                Log.e(TAG, "fetchConfig", e);
                showToast("ERROR!! " + e);
            }
        });
    }

    private void showToast(String messageStr) {
        Toast.makeText(this, messageStr, Toast.LENGTH_SHORT).show();
    }

    private void showToast(@SuppressWarnings("SameParameterValue") int messageStrResId) {
        Toast.makeText(this, getResources().getString(messageStrResId),
                Toast.LENGTH_SHORT).show();
    }

    private void fetchConfig() {
        RestrictionsManager manager = (RestrictionsManager) getSystemService(RESTRICTIONS_SERVICE);
        if (manager == null) {
            Log.e(TAG, "fetchConfig: failed to access restrictions manager");
            return;
        }
        workerThreadHandler.post(() -> {
            /*
            Running inside a worker thread as DevicePolicyManager#getApplicationRestrictions
            performs disk IO operations.
             */
            String displayStr;
            boolean notFound = false, failed = false;
            try {
                Bundle config = manager.getApplicationRestrictions();
                if (config == null || config.isEmpty()) {
                    displayStr = getString(R.string.no_config_found);
                    notFound = true;
                } else {
                    displayStr = Utils.toString(config);
                }
            } catch (RuntimeException e) {
                displayStr = e.toString();
                failed = true;
            }
            String finalDisplayStr = displayStr;
            boolean success = !notFound && !failed;
            runOnUiThread(() -> {
                if (isDestroyed()) {
                    Log.d(TAG, "fetchConfig: activity is dead already");
                    return;
                }
                configsView.setText(finalDisplayStr);
                if (success) showToast(R.string.fetched);
            });
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        workerThread.quit();
    }
}
