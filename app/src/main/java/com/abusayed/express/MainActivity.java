package com.abusayed.express;

import android.Manifest;
import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.content.pm.PackageManager;
import android.webkit.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.content.Context;
import android.view.Window;
import android.widget.Toast;

public class MainActivity extends Activity {
    WebView web;
    LocationManager lm;

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        web = new WebView(this);
        WebSettings s = web.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true);
        s.setGeolocationEnabled(true);
        s.setAllowFileAccess(true);
        s.setAllowContentAccess(true);
        s.setDatabaseEnabled(true);
        web.setWebViewClient(new WebViewClient());
        web.setWebChromeClient(new WebChromeClient() {
            @Override public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
                callback.invoke(origin, true, false);
            }
        });
        setContentView(web);
        web.loadUrl("file:///android_asset/index.html");
        requestLoc();
    }

    void requestLoc() {
        if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 42);
            return;
        }
        startLoc();
    }

    void startLoc() {
        try {
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                @Override public void onLocationChanged(Location l) {
                    if (web != null) {
                        String js = "window.dispatchEvent(new CustomEvent('nativeLocation',{detail:{lat:" + l.getLatitude() + ",lng:" + l.getLongitude() + "}}));";
                        web.evaluateJavascript(js, null);
                    }
                }
                @Override public void onStatusChanged(String p, int s, Bundle x) {}
                @Override public void onProviderEnabled(String p) {}
                @Override public void onProviderDisabled(String p) {}
            });
        } catch (Exception e) {
            Toast.makeText(this, "تعذر تشغيل الموقع", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int r, String[] p, int[] g) {
        super.onRequestPermissionsResult(r, p, g);
        if (r == 42 && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) startLoc();
    }

    @Override
    protected void onDestroy() {
        if (lm != null) {
            try { lm.removeUpdates((LocationListener) null); } catch (Exception ignored) {}
        }
        if (web != null) web.destroy();
        super.onDestroy();
    }
}
