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
import android.widget.TextView;
import android.widget.Toast;

import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

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
        HttpURLConnection connection = null;
        BufferedReader reader = null;
      //  String url = "https://www.barcodable.com/ean/" + tv.getText();
       // String url = "http://upcdatabase.org/product/0013000006408/917F055552E36AEB3F1EC2E45CA9595F";
        try {
           // Document doc = (Document) Jsoup.connect(url).get();

           // URL url = new URL("http://upcdatabase.org/api/json/0013000006408/917F055552E36AEB3F1EC2E45CA9595F");
           // URL url = new URL("https://upcdatabase.org/");

           // URL url = new URL("https://upcdatabase.org/product/013000006408?apikey=917F055552E36AEB3F1EC2E45CA9595F");

            URL url = new URL("https://api.upcdatabase.org/product/013000006408??apikey=917F055552E36AEB3F1EC2E45CA9595F");

          //  String PRODUCT = "product/0013000006408?apikey=917F055552E36AEB3F1EC2E45CA9595F";
           // String params = "917F055552E36AEB3F1EC2E45CA9595F";

           // String urlstring = "https//api.upcdatabase.org/product/0013000006408?apikey=" + java.net.URLEncoder.encode(params, "UTF-8");

           // java.net.URL url = new java.net.URL(urlstring);

            connection = (HttpURLConnection) url.openConnection();

/*            connection.setRequestMethod("GET");

          //  connection.setRequestProperty("Product", PRODUCT);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {

                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String responseLine;
                StringBuffer response = new StringBuffer();

                while ((responseLine = reader.readLine()) != null){
                    response.append(responseLine +"\n");
                }

                reader.close();

                String temp = response.toString();
            }*/
            connection.connect();

            InputStream stream = connection.getInputStream();

            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line = "";

            while((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
                Log.d("Response: ", ">" + line);
            }

            //return buffer.toString();

            String temp = buffer.toString();

          //  Elements elements = (Elements) doc.getElementsByTagName("class");

/*            for (Element para : elements)
            {
                if (para.className() == "detailtitle")
                {
                    tv2.setText(para.text());
                    Log.i("DOCUMENT ELEMENT: ", para.text());
                    return;
                }
            }*/

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
