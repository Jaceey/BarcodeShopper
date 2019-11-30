package com.example.jtuckkjarocki.shoppinghelper;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;

import java.util.List;

import com.example.jtuckkjarocki.shoppinghelper.barcode.R;

/**
 * Camera Preview Activity
 * control preview screen and overlays
 */
//@SuppressWarnings("deprecation")
public class CameraPreviewActivity extends AppCompatActivity {

    private Camera mCamera;
    private CameraView camView;
    private OverlayView overlay;
    private AlertDialog.Builder builder;
    private AlertDialog dialog;
    private double overlayScale = -1;

    String barcodeValue = "";
    TextView tv;
    Button btn;


    private interface OnBarcodeListener {
        void onIsbnDetected(FirebaseVisionBarcode barcode);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Full Screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);

        getSupportActionBar().hide();

        // Fix orientation : portrait
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Set layout
        setContentView(R.layout.activity_camera_preview);

        btn = findViewById(R.id.btn_finish_preview);

        // Set ui button actions
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.putExtra("barcode", barcodeValue);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                setResult(RESULT_OK, intent);

                CameraPreviewActivity.this.finish();

                return;
            }
        });


        // Initialize Camera
        mCamera = getCameraInstance();

        // Set-up preview screen
        if(mCamera != null) {
            // Create overlay view
            overlay = new OverlayView(this);

            // Create barcode processor for ISBN
            CustomPreviewCallback camCallback = new CustomPreviewCallback(CameraView.PREVIEW_WIDTH, CameraView.PREVIEW_HEIGHT);
            camCallback.setBarcodeDetectedListener(new OnBarcodeListener() {
                @Override
                public void onIsbnDetected(FirebaseVisionBarcode barcode) {
                    overlay.setOverlay(fitOverlayRect(barcode.getBoundingBox()), barcode.getRawValue());
                    Log.i("BARCODE INFORMATION", barcode.getRawValue());
                    barcodeValue = barcode.getRawValue();

                    overlay.invalidate();
                }
            });

            // Create camera preview
            camView = new CameraView(this, mCamera);
            camView.setPreviewCallback(camCallback);

            // Add view to UI
            FrameLayout preview = findViewById(R.id.frm_preview);
            preview.addView(camView);
            preview.addView(overlay);
        }
    }

    public void confirmBarcodeScan(final CharSequence barcode){
        builder = new AlertDialog.Builder(CameraPreviewActivity.this);
        CharSequence message = getString(R.string.AlertDialogMessage) + "\n" + barcode;
        builder.setTitle(R.string.AlertDialogTitle).setMessage(message);

        // Set AlertDialog Listeners
        builder.setPositiveButton(R.string.AddItemBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User Clicks 'Add' Button
                Intent AddItemIntent = new Intent(CameraPreviewActivity.this, ItemAddActivity.class);
                AddItemIntent.putExtra("barcode",barcode);
                CameraPreviewActivity.this.startActivity(AddItemIntent);
            }
        });
        builder.setNegativeButton(R.string.CancelBtn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // User Clicks 'Cancel' Button.
            }
        });
        dialog = builder.create();
    }

    @Override
    protected void onDestroy() {
        try{
            if(mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        super.onDestroy();
    }

    /** Get facing back camera instance */
    public static Camera getCameraInstance()
    {
        int camId = -1;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < Camera.getNumberOfCameras(); ++i) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                camId = i;
                break;
            }
        }

        if(camId == -1) return null;

        Camera c=null;
        try{
            c=Camera.open(camId);
        }catch(Exception e){
            e.printStackTrace();
        }
        return c;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCamera.startPreview();
    }

    @Override
    public void onBackPressed() {
        //On back pressed just stop camera
        mCamera.stopPreview();
    }

    /** Calculate overlay scale factor */
    private Rect fitOverlayRect(Rect r) {
        if(overlayScale <= 0) {
            Camera.Size prevSize = camView.getPreviewSize();
            overlayScale = (double) overlay.getWidth()/(double)prevSize.height;
        }

        return new Rect((int)(r.left*overlayScale), (int)(r.top*overlayScale), (int)(r.right*overlayScale), (int)(r.bottom*overlayScale));
    }

    /** Post-processor for preview image streams */
    private class CustomPreviewCallback implements Camera.PreviewCallback, OnSuccessListener<List<FirebaseVisionBarcode>>, OnFailureListener {

        public void setBarcodeDetectedListener(OnBarcodeListener mBarcodeDetectedListener) {
            this.mBarcodeDetectedListener = mBarcodeDetectedListener;
        }

        // ML Kit instances
        private FirebaseVisionBarcodeDetectorOptions options;
        private FirebaseVisionBarcodeDetector detector;
        private FirebaseVisionImageMetadata metadata;

        /**
         * Event Listener for post processing
         *
         * We set up the detector only for EAN 13, UPC A and E barcode format and Product barcode type.
         * This OnBarcodeListener aims of notifying 'Product barcode is detected' to other class.
         */
        private OnBarcodeListener mBarcodeDetectedListener = null;

        /** size of input image */
        private int mImageWidth, mImageHeight;

        /**
         * Constructor
         * @param imageWidth preview image width (px)
         * @param imageHeight preview image height (px)
         */
        CustomPreviewCallback(int imageWidth, int imageHeight){
            mImageWidth = imageWidth;
            mImageHeight = imageHeight;

            // set-up detector options for find EAN-13 format (commonly used 1-D barcode)
            // and UPC A and E (found on nearly every retail product)
            options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                    .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_EAN_13,
                            FirebaseVisionBarcode.FORMAT_UPC_E,
                            FirebaseVisionBarcode.FORMAT_UPC_A)
                    .build();

            detector = FirebaseVision.getInstance().getVisionBarcodeDetector(options);

            // build detector
            metadata = new FirebaseVisionImageMetadata.Builder()
                    .setFormat(ImageFormat.NV21)
                    .setWidth(mImageWidth)
                    .setHeight(mImageHeight)
                    .setRotation(FirebaseVisionImageMetadata.ROTATION_90)
                    .build();
        }

        /** Start detector if camera preview shows */
        @Override public void onPreviewFrame(byte[] data, Camera camera) {
            try {
                detector.detectInImage(FirebaseVisionImage.fromByteArray(data, metadata))
                        .addOnSuccessListener(this)
                        .addOnFailureListener(this);
            } catch (Exception e) {
                Log.d("CameraView", "parse error");
            }
        }

        /** Barcode is detected successfully */
        @Override public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
            // Task completed successfully
            for (FirebaseVisionBarcode barcode: barcodes) {
                int valueType = barcode.getValueType();
                if (valueType == FirebaseVisionBarcode.TYPE_PRODUCT) {
                    mBarcodeDetectedListener.onIsbnDetected(barcode);

                    // If barcode matches - send data back to MainActivity and close
                    // CAN replace with new window and pass it the extra instead!

                    Intent intent = new Intent();
                    intent.putExtra("barcode", barcodeValue);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    setResult(RESULT_OK, intent);

                    CameraPreviewActivity.this.finish();
                    return;
                }
            }
        }

        /** Barcode is not recognized */
        @Override
        public void onFailure(@NonNull Exception e) {
            // Task failed with an exception
            Log.i("Barcode", "read fail");
        }
    }
}
