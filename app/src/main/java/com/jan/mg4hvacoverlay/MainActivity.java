package com.jan.mg4hvacoverlay;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.saicmotor.sdk.external.IPageService;

public class MainActivity extends AppCompatActivity {
    private static final String SYSTEMUI_ACTION =
            "com.android.systemui.saicmotor.action.StartActivity";
    private static final String SYSTEMUI_PACKAGE = "com.android.systemui";
    private static final String SYSTEMUI_SERVICE =
            "com.android.systemui.StartActivityService";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean opened;
    private boolean bound;

    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IPageService pageService = IPageService.Stub.asInterface(service);
            if (pageService == null) {
                fail("IPageService nicht verfuegbar");
                return;
            }

            try {
                pageService.openHvac();
                opened = true;
                Toast.makeText(MainActivity.this, R.string.overlay_opened, Toast.LENGTH_SHORT)
                        .show();
                finishSoon();
            } catch (RemoteException e) {
                fail("openHvac() fehlgeschlagen: " + e.getMessage());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        bindPageService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (bound) {
            unbindService(serviceConnection);
            bound = false;
        }
    }

    private void bindPageService() {
        Intent intent = new Intent();
        intent.setAction(SYSTEMUI_ACTION);
        intent.setComponent(new ComponentName(SYSTEMUI_PACKAGE, SYSTEMUI_SERVICE));

        try {
            bound = bindService(intent, serviceConnection, BIND_AUTO_CREATE);
        } catch (SecurityException e) {
            fail("Bind auf SystemUI blockiert: " + e.getMessage());
            return;
        }

        if (!bound) {
            fail("Bind auf com.android.systemui.StartActivityService fehlgeschlagen");
        }
    }

    private void fail(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        finishSoon();
    }

    private void finishSoon() {
        handler.postDelayed(() -> {
            finish();
            overridePendingTransition(0, 0);
        }, opened ? 60 : 800);
    }
}
