package com.example.jtuckkjarocki.shoppinghelper;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

public class MainActivity extends AppCompatActivity {

    // Permission List
    private String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    // Permission Request Code
    private int RESULT_PERMISSIONS = 0x9000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_show_preview_activity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isPermissionGranted()) startCameraPreviewActivity();
            }
        });
    }

    /** Go to camera preview */
    private void startCameraPreviewActivity(){
        startActivity(new Intent(this, CameraPreviewActivity.class));
    }

    /** Request permission and check */
    private boolean isPermissionGranted(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this, REQUEST_PERMISSIONS, RESULT_PERMISSIONS);
                return false;
            } else {
                return true;
            }
        }else{
            return true;
        }
    }

    /** Post-process for granted permissions */
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (RESULT_PERMISSIONS == requestCode) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
                startCameraPreviewActivity();
            } else {
                // Rejected
                Toast.makeText(this, R.string.err_permission_not_granted, Toast.LENGTH_SHORT).show();
            }
            return;
        }

    }

    /** ImageButton click event **/
    public void onReadProductClick(View view) {
        if(isPermissionGranted()) startCameraPreviewActivity();
    }
}
