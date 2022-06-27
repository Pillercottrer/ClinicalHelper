package com.ehbmed.clinicalhelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.Type;
import android.util.Log;
import android.util.Size;
import android.view.OrientationEventListener;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import kotlin.text.Regex;

public class CameraActivity extends AppCompatActivity {

    private PreviewView previewView;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final int CAMERA_REQUEST_CODE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        previewView = findViewById(R.id.cameraPreviewView);
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindImageAnalysis(cameraProvider);
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));

    }

    public static Bitmap convertYUV(byte[] data, int width, int height, Rect crop) {
        if (crop == null) {
            crop = new Rect(0, 0, width, height);
        }
        Bitmap image = Bitmap.createBitmap(crop.width(), crop.height(), Bitmap.Config.ARGB_8888);
        int yv = 0, uv = 0, vv = 0;

        for (int y = crop.top; y < crop.bottom; y += 1) {
            for (int x = crop.left; x < crop.right; x += 1) {
                yv = data[y * width + x] & 0xff;
                uv = (data[width * height + (x / 2) * 2 + (y / 2) * width + 1] & 0xff) - 128;
                vv = (data[width * height + (x / 2) * 2 + (y / 2) * width] & 0xff) - 128;
                image.setPixel(x, y, convertPixel(yv, uv, vv));
            }
        }
        return image;
    }


    //https://www.demo2s.com/android/android-bitmap-converts-yuv-image-in-byte-array-to-bitmap-by-convertin.html
    //https://blog.minhazav.dev/how-to-convert-yuv-420-sp-android.media.Image-to-Bitmap-or-jpeg/
    Bitmap yuv420ToBitmap(InputImage image) {
        if (image.getFormat() != ImageFormat.YUV_420_888) {
            throw new IllegalArgumentException("Invalid image format");
        }
        Bitmap image_bitmap = Bitmap.createBitmap(image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);



        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        // ARGB array needed by Bitmap static factory method I use below.
        int[] argbArray = new int[imageWidth * imageHeight];
        ByteBuffer yBuffer = image.getPlanes()[0].getBuffer();
        yBuffer.position(0);

        // A YUV Image could be implemented with planar or semi planar layout.
        // A planar YUV image would have following structure:
        // YYYYYYYYYYYYYYYY
        // ................
        // UUUUUUUU
        // ........
        // VVVVVVVV
        // ........
        //
        // While a semi-planar YUV image would have layout like this:
        // YYYYYYYYYYYYYYYY
        // ................
        // UVUVUVUVUVUVUVUV   <-- Interleaved UV channel
        // ................
        // This is defined by row stride and pixel strides in the planes of the
        // image.

        // Plane 1 is always U & plane 2 is always V
        // https://developer.android.com/reference/android/graphics/ImageFormat#YUV_420_888
        ByteBuffer uBuffer = image.getPlanes()[1].getBuffer();
        uBuffer.position(0);
        ByteBuffer vBuffer = image.getPlanes()[2].getBuffer();
        vBuffer.position(0);

        // The U/V planes are guaranteed to have the same row stride and pixel
        // stride.
        int yRowStride = image.getPlanes()[0].getRowStride();
        int yPixelStride = image.getPlanes()[0].getPixelStride();
        int uvRowStride = image.getPlanes()[1].getRowStride();
        int uvPixelStride = image.getPlanes()[1].getPixelStride();

        int r, g, b;
        int yValue, uValue, vValue;

        for (int y = 0; y < imageHeight; ++y) {
            for (int x = 0; x < imageWidth; ++x) {
                int yIndex = (y * yRowStride) + (x * yPixelStride);
                // Y plane should have positive values belonging to [0...255]
                yValue = (yBuffer.get(yIndex) & 0xff);

                int uvx = x / 2;
                int uvy = y / 2;
                // U/V Values are subsampled i.e. each pixel in U/V chanel in a
                // YUV_420 image act as chroma value for 4 neighbouring pixels
                int uvIndex = (uvy * uvRowStride) +  (uvx * uvPixelStride);

                // U/V values ideally fall under [-0.5, 0.5] range. To fit them into
                // [0, 255] range they are scaled up and centered to 128.
                // Operation below brings U/V values to [-128, 127].
                uValue = (uBuffer.get(uvIndex) & 0xff) - 128;
                vValue = (vBuffer.get(uvIndex) & 0xff) - 128;

                // Compute RGB values per formula above.
                r = (int) (yValue + 1.370705f * vValue);
                g = (int) (yValue - (0.698001f * vValue) - (0.337633f * uValue));
                b = (int) (yValue + 1.732446f * uValue);
                r = clamp(r, 0, 255);
                g = clamp(g, 0, 255);
                b = clamp(b, 0, 255);

                // Use 255 for alpha value, no transparency. ARGB values are
                // positioned in each byte of a single 4 byte integer
                // [AAAAAAAARRRRRRRRGGGGGGGGBBBBBBBB]
                int argbIndex = y * imageWidth + x;
                argbArray[argbIndex] = (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255);

                image_bitmap.setPixel(x,y, (255 << 24) | (r & 255) << 16 | (g & 255) << 8 | (b & 255));
            }
        }
        return image_bitmap;
    }

    public static int clamp(int val, int min, int max) {
        return Math.max(min, Math.min(max, val));
    }

    public static int convertPixel(int y, int u, int v) {
        int r = (int) (y + 1.13983f * v);
        int g = (int) (y - .39485f * u - .58060f * v);
        int b = (int) (y + 2.03211f * u);
        r = (r > 255) ? 255 : (r < 0) ? 0 : r;
        g = (g > 255) ? 255 : (g < 0) ? 0 : g;
        b = (b > 255) ? 255 : (b < 0) ? 0 : b;

        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }

    InputImage cropImageNewNew(InputImage image)
    {
        Bitmap bitmap = yuv420ToBitmap(image);
        //Matrix matrix = new Matrix();
        //matrix.postRotate(image.getRotationDegrees());
        bitmap = Bitmap.createBitmap(bitmap, (int)(bitmap.getWidth()*0.27), (int)(bitmap.getHeight()*0.25),  (int)(bitmap.getWidth()*(0.5-0.27)), (int)(bitmap.getHeight()*(0.6-0.25)));
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        //bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        int x = bitmap.getWidth();
        int y = bitmap.getHeight();
        return InputImage.fromBitmap(bitmap, image.getRotationDegrees());

    }

    InputImage cropImageNew(InputImage image)
    {
        // 1 - Convert image to Bitmap
        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        // 2 - Rotate the Bitmap
        if(image.getRotationDegrees() != 0) {
            Matrix rotationMatrix = new Matrix();
            rotationMatrix.postRotate(image.getRotationDegrees());
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), rotationMatrix, true);
        }

        // 3 - Crop the Bitmap
        bitmap = Bitmap.createBitmap(bitmap, (int)(bitmap.getWidth()*CustomOnDrawView.cropx1), (int)(bitmap.getHeight()*CustomOnDrawView.cropy1), (int)(bitmap.getWidth()*(CustomOnDrawView.cropx2-CustomOnDrawView.cropx1)), (int)(bitmap.getHeight()*(CustomOnDrawView.cropy2-CustomOnDrawView.cropy1)));

        return InputImage.fromBitmap(bitmap, image.getRotationDegrees());
    }

    InputImage cropImage(InputImage image)
    {
        Bitmap bitmapRepresentation = image.getBitmapInternal();
        if(bitmapRepresentation != null)
        {
            /*
            // 2 - Rotate the Bitmap
            if(image.getRotationDegrees() != 0) {
                Matrix rotationMatrix = new Matrix();
                rotationMatrix.postRotate(image.getRotationDegrees());
                bitmapRepresentation = Bitmap.createBitmap(bitmapRepresentation, 0, 0, bitmapRepresentation.getWidth(), bitmapRepresentation.getHeight(), rotationMatrix, true);
            }
            */
            bitmapRepresentation = Bitmap.createBitmap(bitmapRepresentation, (int)(bitmapRepresentation.getWidth()*CustomOnDrawView.cropx1), (int)(bitmapRepresentation.getHeight()*CustomOnDrawView.cropy1), (int)(bitmapRepresentation.getWidth()*(CustomOnDrawView.cropx2-CustomOnDrawView.cropx1)), (int)(bitmapRepresentation.getHeight()*(CustomOnDrawView.cropy2-CustomOnDrawView.cropy1)));


            return InputImage.fromBitmap(bitmapRepresentation, image.getRotationDegrees());
        }
        else
        {
            return image;
        }
    }

    //https://beakutis.medium.com/using-googles-mlkit-and-camerax-for-lightweight-barcode-scanning-bb2038164cdc
    //https://medium.com/swlh/introduction-to-androids-camerax-with-java-ca384c522c5
    //https://github.com/googlesamples/mlkit/blob/e949654bef09e97b237c7692349c52ee06e9fd9e/android/vision-quickstart/app/src/main/java/com/google/mlkit/vision/demo/java/VisionProcessorBase.java#L178-L181
    private void readTextFromPaper(InputImage image, ImageProxy proxy)
    {
        TextRecognizer recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        //Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bg_1_2);
        //InputImage image = InputImage.fromBitmap(bitmap, 0);

        //crop image
        image = cropImageNewNew(image);
        ArrayList<String> elements = new ArrayList<>();
        //process the image
        Task<Text> result =
                recognizer.process(image)
                        .addOnSuccessListener(new OnSuccessListener<Text>() {
                            @Override
                            public void onSuccess(Text visionText) {

                                for (Text.TextBlock block : visionText.getTextBlocks()) {
                                    Rect boundingBox = block.getBoundingBox();
                                    Point[] cornerPoints = block.getCornerPoints();
                                    String text = block.getText();

                                    for (Text.Line line: block.getLines()) {
                                        // ...
                                        for (Text.Element element: line.getElements()) {
                                            // ...
                                            //store values and check that pH, pCO", pO2 etc are there
                                            elements.add(element.getText());
                                            Log.d("readout", element.getText());
                                        }
                                    }
                                }
                                try {
                                    checkIfFinished(elements);
                                } catch (ExecutionException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                    }
                                })
                        .addOnCompleteListener(new OnCompleteListener<Text>() {
                            @SuppressLint("UnsafeOptInUsageError")
                            @Override
                            public void onComplete(@NonNull Task<Text> task) {
                                //readText(task.getResult());
                                //recognizer.close();
                                proxy.getImage().close();
                                proxy.close();
                            }
                        });

    }

    private void checkIfFinished(ArrayList<String> strings) throws ExecutionException, InterruptedException {
        ArrayList<String> numbers = new ArrayList<>();
        for(String string : strings)
        {
            //String[] split = string.split(" ");
            //String[] split2 = string.split("(?<=\\D)(?=\\d)");
            //String[] split3 = string.split("(^\\d*\\.\\d+|\\d+\\.\\d*$)(?<=\\D)");
            //String hehehe = string.replaceAll("[^0-9]^\\.","");

            String potentialNum = "";
            if(string.contains("m"))
            {
                //continue working here.
                String[] lol = string.split("m");
                String number = lol[0];
                potentialNum = number.replaceAll("[^\\d.]","");
            }
            else
            {
                potentialNum = string.replaceAll("[^\\d.]","");
            }
            //String ala = string.replaceAll("[^\\d.]","");
            /*


            for(String ele : split)
            {
                String asd = ele;
                if(ele.matches("^\\d+\\.?\\d+"))
                {
                    numbers.add(Double.parseDouble(ele));
                }
            }
             */

            if(!potentialNum.isEmpty())
            {
                //numbers.add(Double.parseDouble(potentialNum)); // double
                numbers.add(potentialNum); //string
            }
            /*
            if(ala.matches("^\\d+\\.?\\d+"))
            {
                //numbers.add(Double.parseDouble(ala));
            }
            */

        }
         if(numbers.size() == 8)
        {
            String success= "success";
            Intent data = new Intent();
            data.putStringArrayListExtra("array", numbers);
            setResult(RESULT_OK, data);
            //---close the activity---
            finish();
            //cameraProviderFuture.get().unbindAll();
            //previewView.setVisibility(View.INVISIBLE);
        }
    }

    private void readText(Text text)
    {
        //get text
        String resultText = text.getText();
        for (Text.TextBlock block : text.getTextBlocks()) {
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

    private void bindImageAnalysis(@NonNull ProcessCameraProvider cameraProvider) {
        //targetsize before 1280, 720
        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder().setTargetResolution(new Size(960, 1280))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
        //imageAnalysis.setAnalyzer(ContextCompat.getMainExecutor(this), new YourAnalyzer());
        final ExecutorService executorService = Executors.newFixedThreadPool(2);
        imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), new YourAnalyzer());
        /*
        OrientationEventListener orientationEventListener = new OrientationEventListener(this) {
            @Override
            public void onOrientationChanged(int orientation) {
                textView.setText(Integer.toString(orientation));
            }
        };
        orientationEventListener.enable();
        */
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        cameraProvider.bindToLifecycle((LifecycleOwner)this, cameraSelector,
                imageAnalysis, preview);
    }

    private class YourAnalyzer implements ImageAnalysis.Analyzer {

        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                //readTextInImage(image);
                readTextFromPaper(image, imageProxy);
            }
        }
    /*

        @Override
        public void analyze(@NonNull ImageProxy image) {

        }
        */
    }

}