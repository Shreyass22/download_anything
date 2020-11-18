package com.example.downloadanything;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_STORAGE_CODE = 1000;

    EditText urlEt;
    Button downloadUrl;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        urlEt = findViewById(R.id.urlEt);
        downloadUrl = findViewById(R.id.downloadUrl);

        downloadUrl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //if os is Marshmallow or above, handle runtime permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) ==
                            PackageManager.PERMISSION_DENIED) {
                        //permission denied, request it
                        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                        //show popup for runtime permission
                        requestPermissions(permissions, PERMISSION_STORAGE_CODE);
                    }
                    else{
                        //permission already granted, perform download
                        startDownloading();
                    }
                }
                else {
                    //system os is less than marshmallow, perform download
                    startDownloading();
                }
            }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("Notification", "Notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }

    }

    private class DownloadFile extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            int count;
            try {
                URL url = new URL(strings[0]);
                URLConnection connection = url.openConnection();
                connection.connect();
                int filelen = connection.getContentLength();
                String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
                InputStream input = new BufferedInputStream(url.openStream());
                OutputStream output = new FileOutputStream(filepath + "/image.jpg");

                byte data[] = new byte[1024];
                long total = 0;

                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(String.valueOf((int) (total * 100) / filelen));
                    output.write(data, 0, count);
                }
                output.flush();
                output.close();
                input.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);

            progressDialog.setTitle("progress bar");
            progressDialog.setMessage("Downloading......");
            progressDialog.setIndeterminate(false);
            progressDialog.setMax(100);
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            progressDialog.show();

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            urlEt.setText("");
            sendNotification();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressDialog.setProgress(Integer.parseInt(values[0]));
        }


    }

    private void sendNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(MainActivity.this, "Notification")
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle("Notification")
                .setContentText("Thank you for downloading.")
                .setAutoCancel(true);

        NotificationManagerCompat managerCompat =  NotificationManagerCompat.from(MainActivity.this);
        managerCompat.notify(0, builder.build());

    }

    private void startDownloading() {
        // get url from edittext
        String url = urlEt.getText().toString().trim();
        String fileExtension = MimeTypeMap.getFileExtensionFromUrl(url);
//        fileName += "." + fileExtension;
        //create download request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // allow types of network to download files
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download"); // set title in  download notification
        request.setDescription("Downloading the content...."); // set desp in  download notification

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        // get current timestamp  as file name
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+ System.currentTimeMillis()+ "." + fileExtension);

        //get download service and enque file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
        new DownloadFile().execute(url);
    }

    //handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_STORAGE_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Permission Accepted...!", Toast.LENGTH_SHORT).show();
                    //permission granted from popup, perform donwload
                    startDownloading();
                }
                else {
                    //permission denied from popup, show error msg
                    Toast.makeText(this, "Permission Denied...!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}