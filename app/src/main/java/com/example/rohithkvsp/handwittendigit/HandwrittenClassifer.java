package com.example.rohithkvsp.handwittendigit;

import android.app.Activity;
import android.util.Log;

import java.io.IOException;

/**
 * Created by rohithkvsp on 4/21/18.
 */

public class HandwrittenClassifer extends ImageClassifier {

    private static final String TAG = "TfLiteCameraDemo";


    private float[][] labelProbArray = null;
    private static int digit = -1;
    private static float  prob = 0.0f;

    /**
     * Initializes an {@code ImageClassifier}.
     *
     * @param activity
     */
    HandwrittenClassifer(Activity activity) throws IOException {
        super(activity);
        labelProbArray = new float[1][10];
    }

    @Override
    protected String getModelPath() {
        return "mnist.tflite";
    }



    @Override
    protected int getImageSizeX() {
        return 28;
    }

    @Override
    protected int getImageSizeY() {
        return 28;
    }

    @Override
    protected int getNumBytesPerChannel() {
        return 4;
    }







    @Override
    protected void runInference() {
        Log.e(TAG, "Inference doing");
        if(imgData != null)
            tflite.run(imgData, labelProbArray);
        Log.e(TAG, "Inference done "+argmax(labelProbArray[0]));
    }

    private static   int argmax(float[] probs) {
        int maxIdx = -1;
        float maxProb = 0.0f;
        for (int i = 0; i < probs.length; i++) {
            if (probs[i] > maxProb) {
                maxProb = probs[i];
                maxIdx = i;
            }
        }
        prob = maxProb;
        digit = maxIdx;

        return maxIdx;
    }
    @Override
    protected  int getdigit()
    {
       return digit;
    }

    @Override
    protected  float  getProb()
    {
       return prob;
    }
}
