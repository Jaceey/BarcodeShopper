package com.example.jtuckkjarocki.shoppinghelper;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Permission List
    private String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    // Permission Request Code
    private int RESULT_PERMISSIONS = 0x9000;
    TextView tv;
    TextView tv2;

    private String PRODUCT_XPATH = "/html/body/div[2]/div/section[1]/div/div/div[1]/h2";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.txt_product);
        tv2 = findViewById(R.id.txt_barcodename);

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
        startActivityForResult(new Intent(this, CameraPreviewActivity.class), 1);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1)
        {
            if (resultCode == RESULT_OK) {
                tv.setText(data.getStringExtra("barcode"));
                getBarcodeInfo();
            }
        }

    }


    protected void getBarcodeInfo()
    {

        if (tv.getText() != null && tv.getText().length() > 0) {
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://api.upcdatabase.org/product/" + tv.getText().toString() + "?apikey=290359EB4A6757A0767C6E66C4DC65CE";


            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject responseJSON = null;
                    try {
                        JSONObject json = (JSONObject) new JSONTokener(response).nextValue();
                        // Get title of product
                        String title = (String) json.get("title");
                        tv.setText("Response is: " + title);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    tv.setText("That didn't work!");
                }
            });

            queue.add(stringRequest);
        }
    }


}
