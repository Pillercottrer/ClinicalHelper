package com.ehbmed.clinicalhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    Button b1, b2, b3, b4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //findViews();
        Intent myIntent = new Intent(getApplicationContext(), BlodgasCalculator.class);
        startActivity(myIntent);
    }

    void findViews()
    {
        b1 = findViewById(R.id.main_b1);
        b1.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), Infection.class);
            startActivity(myIntent);
        });
        b2 = findViewById(R.id.main_b11);
        b2.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), BlodgasCalculator.class);
            startActivity(myIntent);
        });
        b3 = findViewById(R.id.main_b2);
        b3.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), VattenBalansMenu.class);
            startActivity(myIntent);
        });

    }
}
