package id.shenx.health;

import android.app.Activity;
import android.view.WindowManager;

import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

/**
 * Plugin kecil khusus SHENX HEALTH.
 * keepAwake(): menjaga layar tetap menyala selama timer latihan berjalan,
 * supaya hitungan jalan/napas tidak terganggu saat layar mau mati sendiri.
 */
@CapacitorPlugin(name = "Shenx")
public class ShenxPlugin extends Plugin {

    @PluginMethod
    public void keepAwake(final PluginCall call) {
        final boolean on = Boolean.TRUE.equals(call.getBoolean("value", Boolean.TRUE));
        final Activity activity = getActivity();
        if (activity == null) {
            call.reject("No activity");
            return;
        }
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (on) {
                    activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                } else {
                    activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                }
            }
        });
        call.resolve();
    }
}
