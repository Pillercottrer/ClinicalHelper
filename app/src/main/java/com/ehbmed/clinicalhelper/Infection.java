package com.ehbmed.clinicalhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class Infection extends AppCompatActivity {

    Button b1, b2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_infection);
        loadViews();
    }

    void loadViews()
    {
        b1 = findViewById(R.id.infection_b1);
        b1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), SwipeView.class);
                myIntent.putExtra("numPages", 1);
                startActivity(myIntent);
            }
        });

        b2 = findViewById(R.id.infection_b2);
        b2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), SwipeView.class);
                myIntent.putExtra("numPages", 1);
                startActivity(myIntent);
            }
        });

    }
}
