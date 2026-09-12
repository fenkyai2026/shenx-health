package id.shenx.health;

import android.os.Bundle;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Harus didaftarkan sebelum super.onCreate()
        registerPlugin(ShenxPlugin.class);
        super.onCreate(savedInstanceState);
    }
}
