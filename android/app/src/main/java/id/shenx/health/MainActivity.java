package id.shenx.health;

import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;

import com.getcapacitor.BridgeActivity;

public class MainActivity extends BridgeActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Harus didaftarkan sebelum super.onCreate()
        registerPlugin(ShenxPlugin.class);
        super.onCreate(savedInstanceState);

        /**
         * targetSdkVersion project ini tinggi (36), jadi Android memakai
         * "predictive back gesture" untuk swipe kiri/kanan — mekanisme ini TIDAK
         * lewat onBackPressed() klasik lagi, harus didaftarkan lewat
         * OnBackPressedDispatcher supaya swipe (bukan cuma tombol back) ikut
         * memundurkan WebView sesuai riwayat navigasi (history.pushState) di
         * index.html, alih-alih langsung menutup app.
         */
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (getBridge() != null && getBridge().getWebView() != null && getBridge().getWebView().canGoBack()) {
                    getBridge().getWebView().goBack();
                } else {
                    finish();
                }
            }
        });
    }
}
