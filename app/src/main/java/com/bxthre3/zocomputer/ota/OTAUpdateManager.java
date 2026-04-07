package com.bxthre3.zocomputer.ota;

import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;

public class OTAUpdateManager {
    private static final String UPDATE_URL = "https://brodiblanco.zo.space/api/ota/zocomputer";
    private static final String APK_FILENAME = "zo-computer-update.apk";
    
    private final Activity activity;
    private long downloadId = -1;
    
    private final BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (id == downloadId) {
                installUpdate();
            }
        }
    };
    
    public OTAUpdateManager(Activity activity) {
        this.activity = activity;
        activity.registerReceiver(downloadReceiver, 
            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }
    
    public void checkForUpdate() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(UPDATE_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                
                BufferedReader reader = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JSONObject json = new JSONObject(response.toString());
                int latestVersion = json.getInt("versionCode");
                String downloadUrl = json.getString("downloadUrl");
                String changelog = json.optString("changelog", "");
                
                int currentVersion = activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0).versionCode;
                
                if (latestVersion > currentVersion) {
                    activity.runOnUiThread(() -> promptUpdate(downloadUrl, changelog));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    private void promptUpdate(String downloadUrl, String changelog) {
        new androidx.appcompat.app.AlertDialog.Builder(activity)
            .setTitle("Update Available")
            .setMessage("A new version is available.\n\n" + changelog)
            .setPositiveButton("Update Now", (d, w) -> startDownload(downloadUrl))
            .setNegativeButton("Later", null)
            .show();
    }
    
    private void startDownload(String url) {
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setTitle("Zo Computer Update");
        request.setDescription("Downloading...");
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, APK_FILENAME);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        
        DownloadManager dm = (DownloadManager) activity.getSystemService(Context.DOWNLOAD_SERVICE);
        downloadId = dm.enqueue(request);
        Toast.makeText(activity, "Download started...", Toast.LENGTH_SHORT).show();
    }
    
    private void installUpdate() {
        File apkFile = new File(Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS), APK_FILENAME);
        
        Uri apkUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            apkUri = FileProvider.getUriForFile(activity, 
                activity.getPackageName() + ".provider", apkFile);
        } else {
            apkUri = Uri.fromFile(apkFile);
        }
        
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
        activity.startActivity(intent);
    }
    
    public void unregister() {
        activity.unregisterReceiver(downloadReceiver);
    }
}
