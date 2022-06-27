package com.ehbmed.clinicalhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ElectrolyteCalculator extends AppCompatActivity {

    private EditText editText_Na, editText_K, editText_krea;
    private Button goButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_electrolyte_calculator);
        loadViews();
    }

    void loadViews()
    {
        editText_Na = findViewById(R.id.et_Na_ecalc);
        editText_K = findViewById(R.id.et_K_ecalc);
        editText_krea = findViewById(R.id.et_krea_ecalc);
        goButton = findViewById(R.id.button_go_ecalc);

        goButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateActions();
            }
        });
    }
    private void calculateActions()
    {
        //1: fetch Na value
        double na = Double.parseDouble(editText_Na.getText().toString());
        //2: fetch K value
        double k = Double.parseDouble(editText_K.getText().toString());
        //3: fetch krea value
        double krea = Double.parseDouble(editText_krea.getText().toString());
    }

    private void getKaliumText()
    {

    }

    private void getNaText(double na)
    {
        if(na != 0)
        {
            if(na < 135) // hyponatremi under 135
            {
                
            }
            else if(na >= 135 && na <= 145) // mellan 135 och 145 normalt
            {

            }
            else // över 145 normalt
            {

            }
        }
    }

    private void getKreaText()
    {

    }
}