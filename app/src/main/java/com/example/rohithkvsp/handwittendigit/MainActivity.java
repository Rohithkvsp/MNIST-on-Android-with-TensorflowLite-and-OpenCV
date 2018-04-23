package com.example.rohithkvsp.handwittendigit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Point;

import org.opencv.imgproc.Imgproc;


import java.io.IOException;
import java.util.List;


public class MainActivity extends AppCompatActivity implements OnClickListener, CvCameraViewListener2 {

    private CameraView mOpenCvCameraView;
    private static final String TAG = "APP::Activity";
    private int mCameraIndex = 0;
    private Mat mRgba;
    private Mat intermediate;
    private Mat CNN_input;
    private Classifier classifier;

    private int CAMERA = 10;

    private Button bt;
    private TextView tv;
    private boolean PressedOnce = true;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        } else {
            System.loadLibrary("process");

        }
    }


    private BaseLoaderCallback mLoaderCallback=new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            super.onManagerConnected(status);
            switch(status)
            {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();

                }break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        final FrameLayout layout = new FrameLayout(this);
        layout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        setContentView(layout);



        mOpenCvCameraView = new CameraView(this,mCameraIndex);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        layout.addView(mOpenCvCameraView);

        bt = new Button(this);
        bt.setText("classify");
        bt.setId(12345);
        bt.getBackground().setAlpha(64);
        bt.setOnClickListener(this);
        bt.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.CENTER));
        layout.addView(bt);
        tv = new TextView(this);

        tv.setTextColor(Color.WHITE);
        tv.setTextSize(20f);
        tv.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.TOP+Gravity.RIGHT));
        layout.addView(tv);

        if(Build.VERSION.SDK_INT >= 23)
            askForPermission(Manifest.permission.CAMERA,CAMERA); //ask for camera permission





    }



    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } else {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else{

            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("LongLogTag")
    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case 12345:
                if(PressedOnce)
                {

                    Log.v("Button click CNN_input size ", CNN_input.rows()+" X "+CNN_input.cols()+" X "+CNN_input.channels()+" "+CNN_input.type());
                    //classify cropped image
                    if(classifier!=null) {
                        classifier.classifyMat(CNN_input);
                        if(classifier.getdigit()!=-1)
                            tv.setText("Digit: " + classifier.getdigit() + " Prob: " + classifier.getProb() + "          ");
                        else
                            tv.setText("tf model not loaded, please reopen the app");
                    }
                    bt.setText("Back"); ///change button text to Restart
                    PressedOnce = false;
                    mOpenCvCameraView.disableView(); //disable camera
                    mOpenCvCameraView.stopCamera();
                }
                else
                {
                    tv.setText("");
                    bt.setText("Classify"); ///change button text to Restart
                    mOpenCvCameraView.startcamera();
                    PressedOnce = true;
                }

                break;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if(Build.VERSION.SDK_INT< 23)
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);

        hideSystemUI(); //hide UI
        try {
            classifier = new Classifier(MainActivity.this);

        } catch (IOException e) {
            Log.e(TAG, "Failed to initialize an image classifier.", e);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.stopCamera();
        }
        if(classifier!=null) {
            classifier.close();
        }

    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub

        super.onDestroy();
        if(mOpenCvCameraView!=null) {
            mOpenCvCameraView.disableView();
            mOpenCvCameraView.stopCamera();
        }

    }



    @Override
    public void onCameraViewStarted(int width, int height) {

        String caption = Integer.valueOf(mOpenCvCameraView.getWidth()).toString() + "x" + Integer.valueOf(mOpenCvCameraView.getHeight()).toString();
        Log.v("screen_size ",caption);
        List<Size> mResolutionList = mOpenCvCameraView.getResolutionList();
        Camera.Size mSize = null;
        for (Camera.Size size : mResolutionList) {
            Log.i(TAG, "Available resolution: "+size.width+" "+size.height);
            if(size.width<=1280&&size.height<=960) { //change the resolution, 1280x960 and 1440x1080 are working fine in nexus 5x
                mSize = size;
                Log.i(TAG, "selected resolution: "+size.width+" "+size.height);
                break;
            }
        }

        mRgba = new Mat();
        intermediate = new Mat();
        CNN_input = new Mat();
    }



    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba=inputFrame.rgba();
        if (Build.MODEL.equalsIgnoreCase("Nexus 5X")) //flip the frame on nexus5x
            Core.flip(mRgba, mRgba,-1);


        int top = mRgba.rows()/2 - 140;
        int left = mRgba.cols() / 2 - 140;
        int height = 140*2;
        int width = 140*2;
        Mat topcorner;

        ///prepocess frame


        Mat gray = inputFrame.gray();
        //draw cropped region
        Imgproc.rectangle(mRgba, new Point(mRgba.cols()/2 - 150, mRgba.rows() / 2 - 150), new Point(mRgba.cols() / 2 + 150, mRgba.rows() / 2 + 150), new Scalar(255,255,255),1);
        //crop frame
        Mat graytemp = gray.submat(top, top + height, left, left + width);
        //blur the cropped frame to remove noise
        Imgproc.GaussianBlur(graytemp, graytemp, new org.opencv.core.Size(7,7),2 , 2);
        //convert gray frame to binary using apadative thresold
        Imgproc.adaptiveThreshold(graytemp, intermediate, 255, Imgproc.ADAPTIVE_THRESH_GAUSSIAN_C, Imgproc.THRESH_BINARY_INV, 5, 5);
        Mat element1 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(9,9));
        //dilate the frame
        Imgproc.dilate(intermediate, intermediate, element1);

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new org.opencv.core.Size(3,3));
        //erode the frame
        Imgproc.erode(intermediate, intermediate, element);

        Imgproc.resize(intermediate, CNN_input, new org.opencv.core.Size(28,28));///CNN input
        //Log.v("CNN_input size ", CNN_input.rows()+" X "+CNN_input.cols()+" X "+CNN_input.channels()+" "+CNN_input.type());
        //show preprocessed cropped resion at top left
        topcorner= mRgba.submat(0,   height, 0, width);
        ///cover grayscale to BGRA
        Imgproc.cvtColor(intermediate, topcorner, Imgproc.COLOR_GRAY2BGRA, 4);

        //classifier.classifyMat(CNN_input);
        //Imgproc.putText(mRgba, "Digit: "+classifier.getdigit()+ " Prob: "+classifier.getProb(), new Point(top, left), 3, 3, new Scalar(255, 0, 0, 255), 2);


        graytemp.release();
        topcorner.release();

        return mRgba;
    }

    @Override
    public void onCameraViewStopped() {


        if(intermediate!=null)
            intermediate.release();
        if(CNN_input!=null)
            CNN_input.release();
        if(mRgba!=null)
            mRgba.release();

    }

    private void hideSystemUI() {
        int windowVisibility = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            windowVisibility |= View.SYSTEM_UI_FLAG_IMMERSIVE;
        }

        getWindow().getDecorView().setSystemUiVisibility(windowVisibility);
    }

}