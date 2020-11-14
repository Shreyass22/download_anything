package com.example.downloadanything;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_STORAGE_CODE = 1000;

    EditText urlEt;
    Button downloadUrl;

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
    }

    private void startDownloading() {
        // get url from edittext
        String url = urlEt.getText().toString().trim();

        //create download request
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        // allow types of network to download files
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
        request.setTitle("Download"); // set title in  download notification
        request.setDescription("Downloading the content...."); // set desp in  download notification

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, ""+ System.currentTimeMillis()); // get current timestamp  as file name

        //get download service and enque file
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
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