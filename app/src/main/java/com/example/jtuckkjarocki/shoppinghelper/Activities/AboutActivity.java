package com.example.jtuckkjarocki.shoppinghelper.Activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
    }

    public void onClickBack(View view){
        finish();
    }
}
