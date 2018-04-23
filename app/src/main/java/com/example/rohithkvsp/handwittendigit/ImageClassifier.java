package com.example.rohithkvsp.handwittendigit;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.android.Utils;
import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import android.graphics.Bitmap;
import java.util.PriorityQueue;

/**
 * Created by rohithkvsp on 4/21/18.
 */

public abstract class ImageClassifier {

    /** Tag for the {@link Log}. */
    private static final String TAG = "TfLiteCameraDemo";

    /** Number of results to show in the UI. */
    private static final int RESULTS_TO_SHOW = 3;

    /** Dimensions of inputs. */
    private static final int DIM_BATCH_SIZE = 1;

    private static final int DIM_PIXEL_SIZE =1; //3 ; edited grayscale


    /** An instance of the driver class to run model inference with Tensorflow Lite. */
    protected Interpreter tflite;

    /** Labels corresponding to the output of the vision model. */
    private List<String> labelList;

    /** A ByteBuffer to hold image data, to be feed into Tensorflow Lite as inputs. */
    protected ByteBuffer imgData = null;

    private final int[] mImagePixels = new int[28 * 28];



    /** multi-stage low pass filter * */


    private static final int FILTER_STAGES = 3;
    private static final float FILTER_FACTOR = 0.4f;


    /** Initializes an {@code ImageClassifier}. */
    ImageClassifier(Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity));
        imgData =
                ByteBuffer.allocateDirect(
                        DIM_BATCH_SIZE
                                * getImageSizeX()
                                * getImageSizeY()
                                * DIM_PIXEL_SIZE
                                * getNumBytesPerChannel());
        imgData.order(ByteOrder.nativeOrder());






        Log.d(TAG, "Created a Tensorflow Lite Image Classifier.");
    }


    public void close() {
        tflite.close();
        tflite = null;
    }


    /** Memory-map the model file in Assets. */
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(getModelPath());
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    /** Writes Image data into a {@code ByteBuffer}. */
    public void classifyMat(Mat mat) {

       // Bitmap bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        //Utils.matToBitmap(mat, bmp);
        //Bitmap invert = ImageUtil.invert(bmp);
        /**Bitmap invert = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat, invert);
        Log.d("APP", invert.getHeight()+" X "+invert.getWidth());

        imgData.rewind();

        Log.e(TAG, "Classifer mat getWidth"+mat.cols()+" , mat "+mat.rows());

        // Convert the image to floating point.
        long startTime = SystemClock.uptimeMillis();
        for(int i =0;i<getImageSizeX();i++)
            invert.getPixels(mImagePixels, 0, invert.getWidth(), 0, 0,
                    invert.getWidth(), invert.getHeight());

        int pixel = 0;
        for (int i = 0; i < 28; ++i) {
            for (int j = 0; j < 28; ++j) {
                final int val = mImagePixels[pixel++];
                imgData.putFloat(convertToGreyScale(val));
            }
        }**/
        long startTime = SystemClock.uptimeMillis();
        imgData.rewind();
        int pixel = 0;
        for (int i = 0; i < 28; ++i) {
            for (int j = 0; j < 28; ++j) {
                imgData.putFloat((float)mat.get(i,j)[0]);
            }
        }


        runInference();

        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer and run inference " + Long.toString(endTime - startTime));
    }


    private float convertToGreyScale(int color) {
        return (((color >> 16) & 0xFF) + ((color >> 8) & 0xFF) + (color & 0xFF)) / 3.0f / 255.0f;
    }

    /**
     * Get the name of the model file stored in Assets.
     *
     * @return
     */
    protected abstract String getModelPath();


    /**
     * Get the image size along the x axis.
     *
     * @return
     */
    protected abstract int getImageSizeX();

    /**
     * Get the image size along the y axis.
     *
     * @return
     */
    protected abstract int getImageSizeY();

    /**
     * Get the number of bytes that is used to store a single color channel value.
     *
     * @return
     */
    protected abstract int getNumBytesPerChannel();


    protected abstract void runInference();

    /**
     * Get the total number of labels.
     *
     * @return
     */
    protected abstract int getdigit();

    protected abstract float  getProb();


}
