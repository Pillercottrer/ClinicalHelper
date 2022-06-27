package com.ehbmed.clinicalhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class VattenBalansMenu extends AppCompatActivity {

    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vatten_balans_menu);
        findViewsById();
    }

    void findViewsById()
    {
        b1 = findViewById(R.id.vebalance_b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), TextActivty.class);
                myIntent.putExtra("rubrik", "Dygnsbehov");
                myIntent.putExtra("textIdBody", R.string.dygnsbehov);
                startActivity(myIntent);
            }
        });

        b2 = findViewById(R.id.vebalance_b2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), TextActivty.class);
                startActivity(myIntent);
            }
        });
    }
}