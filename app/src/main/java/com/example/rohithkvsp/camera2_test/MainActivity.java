package com.example.rohithkvsp.camera2_test;

import android.Manifest;
import android.app.Activity;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Build;
import android.view.View;
import android.view.Surface;
import android.widget.Toast;
import android.view.TextureView;
import android.widget.Button;
import android.util.Log;
import android.util.Size;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import android.graphics.ImageFormat;

import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import android.os.Handler;
import android.os.HandlerThread;



public class MainActivity extends Activity {
    private int CAMERA = 10;

    private TextureView mTextureView;

    private Button mButton;

    private static String TAG ="CAMERA2_test";

    private static final int MAX_PREVIEW_WIDTH = 1920;


    private static final int MAX_PREVIEW_HEIGHT = 1080;

    private Size mPreviewSize;

    private String mCameraId;

    private HandlerThread mBackgroundThread;

    private Handler mBackgroundHandler;

    private CameraDevice mCameraDevice;//set this from CameraDevice.statecallback()

    private CameraCaptureSession mCaptureSession;

    private CaptureRequest.Builder captureRequestBuilder;

    private CaptureRequest captureRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextureView = (TextureView) findViewById(R.id.texture_view);
        mButton =(Button)findViewById(R.id.button);

    }


    TextureView.SurfaceTextureListener textureListener =new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.d(TAG,"onSurfaceTextureAvailable "+width+" , "+height);
            if(Build.VERSION.SDK_INT >= 23) {
                Log.d(TAG,"askForPermission ");
                askForPermission(Manifest.permission.CAMERA, CAMERA); //ask for camera permission
                setupCamera(height,width);
                openCamera();



            }

        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.d(TAG,"onSurfaceTextureSizeChanged "+width+" , "+height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG,"onSurfaceTextureDestroyed ");
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            Log.d(TAG,"onSurfaceTextureUpdated ");

        }
    };


    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG,"Camera Opened");
            mCameraDevice = cameraDevice;
            createPreviewSession();


        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            Log.d(TAG,"Camera Disconnected");
            mCameraDevice.close();
            mCameraDevice = null;

        }

        @Override
        public void onError(@NonNull CameraDevice camcameraDevice, int error) {
            Log.d(TAG,"Camera Error");
            mCameraDevice.close();
            mCameraDevice = null;

        }
    };





    private void setupCamera(int height, int width)
    {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission Not Granted ");
            return ;

        }
        Log.d(TAG,"Permission Granted ");

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            for (String cameraID: cameraManager.getCameraIdList())
            {
                Log.d(TAG,"cameraID "+cameraID);
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraID);
                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if(facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT)
                {
                    continue;
                }

                StreamConfigurationMap map = characteristics.get(characteristics.SCALER_STREAM_CONFIGURATION_MAP);

                /**Log.d(TAG,"total map.getOutputSizes(SurfaceTexture.class) sizes "+map.getOutputSizes(SurfaceTexture.class).length);

                for(Size size: map.getOutputSizes(SurfaceTexture.class))
                {
                    Log.d(TAG,"map.getOutputSizes(SurfaceTexture.class) "+size.getHeight()+" x "+size.getWidth());
                }
                Log.d(TAG,"total map.getOutputSizes(ImageFormat.JPEG) sizes "+map.getOutputSizes(ImageFormat.JPEG).length);
                for(Size size: map.getOutputSizes(ImageFormat.JPEG))
                {
                    Log.d(TAG,"map.getOutputSizes(ImageFormat.JPEG) "+size.getHeight()+" x "+size.getWidth());
                }
                 **/


                Size largest = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)),new CompareSizesByArea());
                Log.d(TAG,"Largest "+largest.getHeight()+" , "+largest.getWidth());

                Point displaySize = new Point();
                MainActivity.this.getWindowManager().getDefaultDisplay().getSize(displaySize);
                int rotatedPreviewWidth = width;
                int rotatedPreviewHeight = height;
                int maxPreviewWidth = displaySize.x;
                int maxPreviewHeight = displaySize.y;
                Log.d(TAG,"width :"+width+" height :"+height+" displaySize.x "+displaySize.x+" displaySize.y "+displaySize.y);

                if (maxPreviewWidth > MAX_PREVIEW_WIDTH) {
                    maxPreviewWidth = MAX_PREVIEW_WIDTH;
                }

                if (maxPreviewHeight > MAX_PREVIEW_HEIGHT) {
                    maxPreviewHeight = MAX_PREVIEW_HEIGHT;
                }

                Log.d(TAG,"corrected  width :"+maxPreviewWidth+" corrected height :"+maxPreviewHeight);
                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        rotatedPreviewWidth, rotatedPreviewHeight, maxPreviewWidth,
                        maxPreviewHeight, largest);

                Log.d(TAG," preview width :"+mPreviewSize.getWidth()+" height :"+mPreviewSize.getHeight());

                this.mCameraId = cameraID;
                return;

            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void openCamera()
    {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG,"Permission Not Granted ");
            return ;
        }

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraManager.openCamera(mCameraId,mStateCallback,mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private static Size chooseOptimalSize(Size[] choices, int textureViewWidth,
                                          int textureViewHeight, int maxWidth, int maxHeight, Size aspectRatio) {

        // Collect the supported resolutions that are at least as big as the preview Surface
        List<Size> bigEnough = new ArrayList<>();
        // Collect the supported resolutions that are smaller than the preview Surface
        List<Size> notBigEnough = new ArrayList<>();
        int w = aspectRatio.getWidth();
        int h = aspectRatio.getHeight();
        for (Size option : choices) {
            if (option.getWidth() <= maxWidth && option.getHeight() <= maxHeight &&
                    option.getHeight() == option.getWidth() * h / w) {
                if (option.getWidth() >= textureViewWidth &&
                        option.getHeight() >= textureViewHeight) {
                    bigEnough.add(option);
                } else {
                    notBigEnough.add(option);
                }
            }
        }

        // Pick the smallest of those big enough. If there is no one big enough, pick the
        // largest of those not big enough.
        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, new CompareSizesByArea());
        } else if (notBigEnough.size() > 0) {
            return Collections.max(notBigEnough, new CompareSizesByArea());
        } else {
            Log.d(TAG, "Couldn't find any suitable preview size");
            return choices[0];
        }
    }

     static class CompareSizesByArea implements Comparator<Size> {

        @Override
        public int compare(Size lhs, Size rhs) {
            // We cast here to ensure the multiplications won't overflow
            return Long.signum(
                    (long) lhs.getWidth() * lhs.getHeight() - (long) rhs.getWidth() * rhs.getHeight());
        }
    }


    private void createPreviewSession()
    {
        try {
            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
            surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(),mPreviewSize.getHeight());
            Surface previewSurface = new Surface(surfaceTexture);

            captureRequestBuilder = mCameraDevice.createCaptureRequest(mCameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(previewSurface);

            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),

                        new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                if(mCameraDevice == null)
                                {
                                    return;
                                }

                                try {
                                    mCaptureSession = cameraCaptureSession;
                                    captureRequestBuilder.set(
                                            CaptureRequest.CONTROL_AF_MODE,
                                            CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                    captureRequest = captureRequestBuilder.build();

                                    mCaptureSession.setRepeatingRequest(captureRequest,captureCallback,mBackgroundHandler);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }

                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                            }
                        },null);//mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }


    }


    private CameraCaptureSession.CaptureCallback captureCallback =
            new CameraCaptureSession.CaptureCallback() {

                @Override
                public void onCaptureProgressed(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull CaptureResult partialResult) {}

                @Override
                public void onCaptureCompleted(
                        @NonNull CameraCaptureSession session,
                        @NonNull CaptureRequest request,
                        @NonNull TotalCaptureResult result) {}
    };









    private void openBackgroundThread()
    {
        mBackgroundThread = new HandlerThread("camera background thread");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler( mBackgroundThread.getLooper());
    }

    private void closeCamera()
    {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }
    }

    private void closeBackgroundThread()
    {
        if(mBackgroundHandler!=null)
        {
            mBackgroundThread.quitSafely();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();
        //hideSystemUI(); //hide UI
        Log.d(TAG,"resume");

        openBackgroundThread();//start the backgroundthread

        if (mTextureView.isAvailable()) {
            Log.d(TAG,"mTextureView is Available");
        }
        else
        {
            mTextureView.setSurfaceTextureListener(textureListener); /// or call this from onCreate

        }

    }


    @Override
    protected void onStop() {
        super.onStop();
        closeCamera();
        closeBackgroundThread();
    }



    private void askForPermission(String permission, Integer requestCode) {
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);

            } else {

                ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
            }
        } /*else {

            Toast.makeText(this, "" + permission + " is already granted.", Toast.LENGTH_SHORT).show();
        }**/
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (ActivityCompat.checkSelfPermission(this, permissions[0]) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show();
        } else{

            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
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
