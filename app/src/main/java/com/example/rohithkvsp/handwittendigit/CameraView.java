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

    //get list of preview
    public List<Camera.Size> getResolutionList() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    //start camera by connecting connect camera
    void startcamera()
    {
        connectCamera(getWidth(), getHeight());
    }

    //stop the camera and its thread
    void stopCamera()
    {
        disconnectCamera();
    }

    // set resolution
    public void setResolution(Camera.Size resolution) {
        disconnectCamera(); //disconnect camera
        mMaxHeight = resolution.height;
        mMaxWidth = resolution.width;
        connectCamera(getWidth(), getHeight()); //connect camera

    }


}
