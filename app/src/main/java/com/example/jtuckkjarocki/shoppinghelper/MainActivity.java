package com.example.jtuckkjarocki.shoppinghelper;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.textclassifier.TextLinks;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    // Permission List
    private String[] REQUEST_PERMISSIONS = new String[]{Manifest.permission.CAMERA};
    // Permission Request Code
    private int RESULT_PERMISSIONS = 0x9000;

    ArrayList<String> allProducts = new ArrayList<>();
    ArrayList<Double> productPrices = new ArrayList<>();

    TextView tv;
    TextView totalText;
    RecyclerView rv;
    RecyclerView.Adapter rvAdapter;
    RecyclerView.LayoutManager rvLayoutManager;


    private AlertDialog.Builder alert;
    private EditText etPrice;
    private TextView tvBarcode;
    private EditText etName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // tv = findViewById(R.id.txt_product);
        tv = findViewById(R.id.txt_barcodename);
        totalText = findViewById(R.id.txt_totalvalue);

        // set up recycler view with Product ArrayList<String>
        rv = findViewById(R.id.productRecycler);
        rvLayoutManager = new LinearLayoutManager(getApplicationContext());
        rv.setLayoutManager(rvLayoutManager);
        rvAdapter = new ProductAdapter(getApplicationContext(), allProducts);
        rv.setAdapter(rvAdapter);

//        findViewById(R.id.btn_show_preview_activity).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(isPermissionGranted()) startCameraPreviewActivity();
//            }
//        });
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
        String barcode = "";
        final String[] title = {""};

        if (tv.getText() != null && tv.getText().length() > 0) {
            barcode = tv.getText().toString();
            RequestQueue queue = Volley.newRequestQueue(this);
            String url = "https://api.upcdatabase.org/product/" + barcode + "?apikey=290359EB4A6757A0767C6E66C4DC65CE";


            String finalBarcode = barcode;
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    JSONObject responseJSON = null;
                    try {
                        JSONObject json = (JSONObject) new JSONTokener(response).nextValue();
                        // Get title of product
                        title[0] = (String) json.get("title");
                       // tv.setText("Response is: " + title[0]);

                        DisplayAlertBox(finalBarcode, title[0]);
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



          //  DisplayAlertBox(barcode, title[0]);
        }
    }

    protected void DisplayAlertBox(final String barcode, final String text)
    {

        alert = new AlertDialog.Builder(MainActivity.this);

        // set up base layout
        LinearLayout layout = new LinearLayout(this);
        LinearLayout.LayoutParams parms = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setLayoutParams(parms);

        layout.setGravity(Gravity.CLIP_VERTICAL);
        layout.setPadding(2,2,2,2);

        // Add barcode
        TextView b = new TextView(MainActivity.this);
        b.setText("Barcode: ");

        tvBarcode = new TextView(MainActivity.this);
        tvBarcode.setText(barcode);

        layout.addView(b, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(tvBarcode, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Add product name
        TextView n = new TextView(MainActivity.this);
        n.setText("Name: ");

        etName = new EditText(MainActivity.this);
        etName.setText(text);

        layout.addView(n, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(etName, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Add product price
        TextView p = new TextView(MainActivity.this);
        p.setText("Price: ");

        etPrice = new EditText(MainActivity.this);
        etPrice.setRawInputType(Configuration.KEYBOARD_12KEY);

        layout.addView(p, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(etPrice, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

        // Add to alert box
        alert.setView(layout);
        alert.setTitle("Product Information");
        alert.setMessage("Set the missing information");


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Do something with value...
                String product = etName.getText().toString() + ", $" + etPrice.getText().toString();

                if(product != "") {
                    if (!allProducts.contains(product)) {
                        allProducts.add(product);
                        productPrices.add(Double.parseDouble(etPrice.getText().toString()));
                        calculateTotal();
                    }
                }

            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // Cancelled
                Toast.makeText(getApplicationContext(),"Adding Product Cancelled!",Toast.LENGTH_SHORT).show();
            }
        });

        alert.show();
    }

    void calculateTotal(){
        double runningTotal = 0;

        for (int i = 0; i < productPrices.size(); i++)
        {
            runningTotal += productPrices.get(i);
        }

        totalText.setText("$" + Double.toString(runningTotal));

    }

}
