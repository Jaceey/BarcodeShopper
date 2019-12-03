package com.example.jtuckkjarocki.shoppinghelper.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

public class ItemAddActivity extends AppCompatActivity {

    EditText item_name_et;
    TextView upc_code_tv; // TextVIew because we don't want to edit the barcode
    EditText item_price_et;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_item);
        item_name_et = findViewById(R.id.Item_Name_et);
        item_price_et = findViewById(R.id.editTextPrice);
        upc_code_tv = findViewById(R.id.UPC_entry_tv);

        //Get Bundle which contains item name & item price
        Bundle bundle = getIntent().getExtras();
        upc_code_tv.setText(bundle.getString("upc"));
        //item_name_tv.setText(bundle.getString("name"));
        item_name_et.setText(bundle.getString("name"));
        double price = bundle.getDouble("price");
        item_price_et.setText(String.valueOf(price));

    }

    public void onSaveClick(View view) {
        Intent intent = new Intent();
        intent.putExtra("button", "SAVE");
        intent.putExtra("position", getIntent().getIntExtra("position", 0));
        intent.putExtra("barcode", upc_code_tv.getText().toString());
        intent.putExtra("name", item_name_et.getText().toString());
        intent.putExtra("price", item_price_et.getText().toString());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        setResult(RESULT_OK, intent);

        ItemAddActivity.this.finish();
    }

    public void onDeleteClick(View view) {
        Intent intent = new Intent();
        intent.putExtra("button", "DELETE");
        intent.putExtra("position", getIntent().getIntExtra("position", 0));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        setResult(RESULT_OK, intent);

        ItemAddActivity.this.finish();
    }
}