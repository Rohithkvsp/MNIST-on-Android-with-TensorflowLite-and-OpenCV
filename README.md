# MNIST-on-Android-with-TensorflowLite-and-OpenCV

<b>Download APK</b>

Apk can be found in apk folder, download and install the apk.

Inorder to recognize hand written digits, Deep learning model was trained. I used tensorflow's [offical MNIST model](https://github.com/tensorflow/models/blob/master/official/mnist/mnist.py) and [MNIST data set](http://yann.lecun.com/exdb/mnist/) to train the Model.

I converted the <b>frozen model</b> to <b>mnist.tflite</b> (optimized [tensorflow lite](https://www.tensorflow.org/mobile/tflite) model for mobile devices)

On Android, recognizing hand written digits by in image or from camera feed is challenging. 
An Image with hand written digit can be feeded directly (downscaled image to model input size) to well trained deep learning model to predict the written digits, <b>But this won't work well!</b>.
Because model the Model was well trained on clean and prepocessed dataset, but Image with hand written digit taken from camera is not clean (contains noise) and not preprocessed.

Before feeding Image taken by the camera to Model is preprocessed. I used OpenCV fro preprocessing on Image or Frames.

<b>Prepocessing steps:</b>

  1) converting color image/frame to grayscale

  2) crop the center region of image/frame

  3) blur the frame (to remove noise)

  4) Apply Adapative thresolding (convering to binary image)

  5) Apply Image morphological operations (dilation and erosion)
  
  6) Down scale the cropped image to Model Input size
  
After the prepocessing, Down scaled image(which is OpenCV Mat) is converted to tensorflow lite input format.


  
 



