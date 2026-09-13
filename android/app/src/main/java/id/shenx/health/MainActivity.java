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

    /**
     * Capacitor tidak lagi otomatis memundurkan WebView saat tombol/gestur back
     * ditekan (harus ditangani manual sejak Capacitor 4+). Tanpa ini, tombol
     * back/swipe selalu langsung menutup app, mengabaikan riwayat navigasi
     * (history.pushState) yang dibuat di index.html.
     */
    @Override
    public void onBackPressed() {
        if (getBridge() != null && getBridge().getWebView() != null && getBridge().getWebView().canGoBack()) {
            getBridge().getWebView().goBack();
        } else {
            super.onBackPressed();
        }
    }
}
