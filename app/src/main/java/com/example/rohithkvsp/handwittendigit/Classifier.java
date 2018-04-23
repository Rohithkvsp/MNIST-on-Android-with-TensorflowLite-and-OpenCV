package com.example.rohithkvsp.handwittendigit;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

import org.opencv.core.Mat;
import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;

/**
 * Created by rohithkvsp on 4/22/18.
 */

public class Classifier {

    private static final String TAG = "TfLite";
    private static final int DIM_BATCH_SIZE = 1;
    private static final int DIM_PIXEL_SIZE =1;
    private static final int  DIM_HEIGHT =28;
    private static final int DIM_WIDTH = 28;
    private static final int BYTES =4;

    protected Interpreter tflite;

    private static int digit = -1;
    private static float  prob = 0.0f;

    protected ByteBuffer imgData = null;
    private float[][] ProbArray = null;
    protected String ModelFile = "mnist.tflite";

    //allocate buffer and create interface
    Classifier(Activity activity) throws IOException {
        tflite = new Interpreter(loadModelFile(activity));
        imgData = ByteBuffer.allocateDirect(DIM_BATCH_SIZE * DIM_HEIGHT * DIM_WIDTH * DIM_PIXEL_SIZE * BYTES);
        imgData.order(ByteOrder.nativeOrder());
        ProbArray = new float[1][10];
        Log.d(TAG, " Tensorflow Lite Classifier.");
    }
    //load model
    private MappedByteBuffer loadModelFile(Activity activity) throws IOException {
        AssetFileDescriptor fileDescriptor = activity.getAssets().openFd(ModelFile);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    //classify mat
    public void classifyMat(Mat mat) {

        long startTime = SystemClock.uptimeMillis();

        convertMattoTfLiteInput(mat);
        runInference();

        long endTime = SystemClock.uptimeMillis();
        Log.d(TAG, "Timecost to put values into ByteBuffer and run inference " + Long.toString(endTime - startTime));
    }
    //convert opencv mat to tensorflowlite input
    private void convertMattoTfLiteInput(Mat mat)
    {
        imgData.rewind();
        int pixel = 0;
        for (int i = 0; i < DIM_HEIGHT; ++i) {
            for (int j = 0; j < DIM_WIDTH; ++j) {
                imgData.putFloat((float)mat.get(i,j)[0]);
            }
        }
    }

    //run interface
    private void runInference() {
        Log.e(TAG, "Inference doing");
        if(imgData != null)
            tflite.run(imgData, ProbArray);
        Log.e(TAG, "Inference done "+maxProbIndex(ProbArray[0]));
    }
   // find max prob and digit
    private  int maxProbIndex(float[] probs) {
        int maxIndex = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIndex = i;
            }
        }
        prob = maxProb;
        digit = maxIndex;

        return maxIndex;
    }
    //get predicted digit
    public int getdigit()
    {

        return digit;
    }
    //get predicted  prob
    public float getProb()
    {

        return prob;
    }
    //close interface
    public void close() {
        tflite.close();
        tflite = null;
    }
}
