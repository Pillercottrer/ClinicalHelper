package com.ehbmed.clinicalhelper;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Size;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

public class BlodgasCalculator extends AppCompatActivity {

    private EditText editText_pH, editText_pCO2, editText_HCO3, editText_BE, editText_pO2;
    private Button goButton, cameraAction;
    private TextView tv;
    private boolean arterialGas = true;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
    ActivityResultLauncher<Intent> someActivityResultLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blodgas_calculator);
        loadViews();
    }

    void loadViews()
    {
        tv = findViewById(R.id.textView_blodgas);
        editText_pH = findViewById(R.id.et_pH_bcalc);
        editText_pCO2 = findViewById(R.id.et_pCO2_bcalc);
        editText_pO2 = findViewById(R.id.et_pO2_bcalc);
        editText_BE = findViewById(R.id.et_BE_bcalc);
        editText_HCO3 = findViewById(R.id.et_HCO3_bcalc);

        goButton = findViewById(R.id.button_go_bcalc);
        cameraAction = findViewById(R.id.button_cameraaction_bcalc);

        goButton.setOnClickListener(v -> {
            //1: fetch pH value
            double pH = 0;
            boolean allFilledIn = true;
            if(!editText_pH.getText().toString().trim().isEmpty())
            {
                pH = Double.parseDouble(editText_pH.getText().toString());
            }
            else
            {
                allFilledIn = false;
                editText_pH.setError("Kan inte vara tom");
            }
            //2: fetch pCO2 value
            double pCO2 = 0;
            if(!editText_pCO2.getText().toString().trim().isEmpty())
            {
                pCO2 = Double.parseDouble(editText_pCO2.getText().toString());
            }
            else
            {
                allFilledIn = false;
                editText_pCO2.setError("Kan inte vara tom");
            }
            //3: fetch HCO3 value
            double HCO3 = 0;
            if(!editText_HCO3.getText().toString().trim().isEmpty())
            {
                HCO3 = Double.parseDouble(editText_HCO3.getText().toString());
            }
            else
            {
                allFilledIn = false;
                editText_HCO3.setError("Kan inte vara tom");
            }
            //4: fetch BE value
            double BE = 0;
            if(!editText_BE.getText().toString().trim().isEmpty())
            {
                BE = Double.parseDouble(editText_BE.getText().toString());
            }
            else
            {
                allFilledIn = false;
                editText_BE.setError("Kan inte vara tom");
            }
            //5: fetch pO2 value
            double pO2 = Double.parseDouble(editText_pO2.getText().toString());
            if(!editText_pO2.getText().toString().trim().isEmpty())
            {
                pO2 = Double.parseDouble(editText_pO2.getText().toString());
            }
            else
            {
                allFilledIn = false;
                editText_pO2.setError("Kan inte vara tom");
            }
            if(allFilledIn)
            {
                calculateActions(pH, pCO2, HCO3, BE, pO2);
            }
        });

        someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if (result.getResultCode() == Activity.RESULT_OK) {
                            // There are no request codes
                            Intent data = result.getData();
                            ArrayList<String> numbers = data.getStringArrayListExtra("array");
                            fillInNumbers(numbers);
                        }
                    }
                });

        //cameraAction.setOnClickListener(view -> readTextFromPaper());
        cameraAction.setOnClickListener(v -> {
            Intent myIntent = new Intent(getApplicationContext(), CameraActivity.class);
            someActivityResultLauncher.launch(myIntent);
        });


    }

    private void fillInNumbers(ArrayList<String> numbers)
    {
        editText_pH.setText(numbers.get(0));
        editText_pCO2.setText(numbers.get(1));
        editText_pO2.setText(numbers.get(2));
        editText_BE.setText(numbers.get(3));
        editText_HCO3.setText(numbers.get(4));

    }

    private void calculateActions(double pH, double pCO2, double HCO3, double BE, double pO2)
    {
        if(pH < 7.35) //We have acidos, heheheheheh.
        {
            if(pCO2 > 5.6) // we have respiratorisk acidos
            {
                tv.setText("Tolkning: \n" + "Respiratorisk acidos");
            }
            else //metabol acidos
            {
                tv.setText("Tolkning: \n" + "Metabol acidos");
            }
        }
        else if(pH >= 7.35 && pH <= 7.45)
        {
            tv.setText("Tolkning: \n" + "Normalt pH");
        }
        else
        {
            if(pCO2 < 5.1) // we have respiratorisk alkalos
            {
                tv.setText("Tolkning: \n" + "Respiratorisk alkalos");
            }
            else //metabol alkalos
            {
                tv.setText("Tolkning: \n" + "Metabol alkalos");
            }
        }
    }
    /*
    private void activateCamera()
    {
       //https://developer.android.com/training/camerax/preview
       //https://developers.google.com/ml-kit/vision/text-recognition/android

        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // No errors need to be handled for this Future.
                // This should never be reached.
            }
        }, ContextCompat.getMainExecutor(this));

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        // enable the following line if RGBA output is needed.
                        //.setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888)
                        .setTargetResolution(new Size(1280, 720))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                        .build();

        imageAnalysis.setAnalyzer(executor, new YourAnalyzer());

        cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, imageAnalysis, preview);
    }

    PreviewView previewView;

    private void bindPreview(ProcessCameraProvider cameraProvider)
    {
        previewView = findViewById(R.id.previewView);

        Preview preview = new Preview.Builder()
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector, preview);
    }
    */
    private void readTextFromPaper()
    {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_1_2);
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        //process the image
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {
                                // Task completed successfully
                                // ...
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                });

        //get text
        String resultText = result.getResult().getText();
        for (Text.TextBlock block : result.getResult().getTextBlocks()) {
            String blockText = block.getText();
            Point[] blockCornerPoints = block.getCornerPoints();
            Rect blockFrame = block.getBoundingBox();
            for (Text.Line line : block.getLines()) {
                String lineText = line.getText();
                Point[] lineCornerPoints = line.getCornerPoints();
                Rect lineFrame = line.getBoundingBox();
                for (Text.Element element : line.getElements()) {
                    String elementText = element.getText();
                    Point[] elementCornerPoints = element.getCornerPoints();
                    Rect elementFrame = element.getBoundingBox();
                }
            }
        }
    }

    private void readTextInImage(InputImage image)
    {

    }
    //https://medium.com/geekculture/getting-started-android-camerax-a84e138e2c00
    private class YourAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                readTextInImage(image);
            }
        }
    /*

        @Override
        public void analyze(@NonNull ImageProxy image) {

        }
        */
    }
}