package id.shenx.health;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.view.WindowManager;

import androidx.core.content.FileProvider;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Plugin kecil khusus SHENX HEALTH.
 * keepAwake(): menjaga layar tetap menyala selama timer latihan berjalan.
 * getAppVersion(): versi APK terpasang, dipakai untuk bandingkan dengan rilis terbaru.
 * downloadAndInstall(): unduh APK rilis terbaru lalu buka installer Android (sideload,
 * bukan silent update — Android tetap minta konfirmasi pasang dari pengguna).
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

    @PluginMethod
    public void getAppVersion(final PluginCall call) {
        try {
            PackageInfo info = getContext().getPackageManager()
                    .getPackageInfo(getContext().getPackageName(), 0);
            JSObject ret = new JSObject();
            ret.put("versionName", info.versionName);
            ret.put("versionCode", info.versionCode);
            call.resolve(ret);
        } catch (Exception e) {
            call.reject("Gagal membaca versi", e);
        }
    }

    @PluginMethod
    public void downloadAndInstall(final PluginCall call) {
        final String url = call.getString("url");
        if (url == null || url.isEmpty()) {
            call.reject("url kosong");
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream in = null;
                FileOutputStream fos = null;
                try {
                    File out = new File(getContext().getCacheDir(), "shenx-update.apk");
                    HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                    conn.setInstanceFollowRedirects(true);
                    conn.connect();
                    int code = conn.getResponseCode();
                    if (code != HttpURLConnection.HTTP_OK) {
                        call.reject("Download gagal: HTTP " + code);
                        return;
                    }
                    in = conn.getInputStream();
                    fos = new FileOutputStream(out);
                    byte[] buf = new byte[8192];
                    int n;
                    while ((n = in.read(buf)) != -1) {
                        fos.write(buf, 0, n);
                    }
                    fos.flush();

                    final Activity activity = getActivity();
                    if (activity == null) {
                        call.reject("No activity");
                        return;
                    }
                    final Uri apkUri = FileProvider.getUriForFile(
                            getContext(), getContext().getPackageName() + ".fileprovider", out);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                            activity.startActivity(intent);
                            call.resolve();
                        }
                    });
                } catch (Exception e) {
                    call.reject("Gagal update: " + e.getMessage(), e);
                } finally {
                    try { if (in != null) in.close(); } catch (Exception ignored) {}
                    try { if (fos != null) fos.close(); } catch (Exception ignored) {}
                }
            }
        }).start();
    }
}
