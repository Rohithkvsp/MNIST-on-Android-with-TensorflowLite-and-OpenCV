package com.example.rohithkvsp.handwittendigit;

import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import org.opencv.android.JavaCameraView;
import java.util.List;



public class CameraView extends JavaCameraView {


    public CameraView(Context context, int cameraId) {
        super(context,cameraId);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    void startcamera()
    {
        connectCamera(getWidth(), getHeight()); //connect camera
    }


    void stopCamera()
    {
        disconnectCamera();//stop the thread
    }





    public void setResolution(Camera.Size resolution) {
        disconnectCamera(); //disconnect camera
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight()); //connect camera

    }



    public Camera.Size getResolution() {
        return mCamera.getParameters().getPreviewSize();
    }




}
