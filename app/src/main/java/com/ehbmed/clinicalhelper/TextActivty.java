package com.ehbmed.clinicalhelper;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class TextActivty extends AppCompatActivity {


    String rubrik, content;

    TextView tv1,tv2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_text_activty);
        Intent mIntent = getIntent();
        rubrik = mIntent.getStringExtra("rubrik");

        int bodyId = mIntent.getIntExtra("textIdBody", R.string.dygnsbehov);
        content = getString(bodyId);

        tv1 = findViewById(R.id.rubrik_tv);
        tv1.setText(rubrik);
        tv2 = findViewById(R.id.content_one_tv);
        tv2.setText(content);

    }


}