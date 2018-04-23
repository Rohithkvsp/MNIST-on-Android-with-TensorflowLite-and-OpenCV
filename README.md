# MNIST-on-Android-with-TensorflowLite-and-OpenCV

<b>Download APK</b>

Apk can be found in apk folder, download and install the apk.

Inorder to recognize hand written digits, Deep learning model was trained. I used tensorflow's [offical MNIST model](https://github.com/tensorflow/models/blob/master/official/mnist/mnist.py) and [MNIST data set](http://yann.lecun.com/exdb/mnist/) to train the Model.

<b>Implemention</b>

I [converted](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/contrib/lite/toco) the <b>frozen model</b> to <b>mnist.tflite</b> (optimized [tensorflow lite](https://www.tensorflow.org/mobile/tflite) model for mobile devices)

On Android, recognizing hand written digits in image or camera feed is challenging. 
An Image with hand written digit can be feeded directly (downscaled image of model input size) to well trained deep learning model to predict the written digits, <b>But this won't work well!</b>.
Because the deep learning model was well trained on clean and prepocessed dataset, but Image with hand written digit taken from camera is not clean (contains noise) and not preprocessed!

Before feeding Image taken by the camera to Model, Image was preprocessed. I used <b>OpenCV for Android</b> for preprocessing on Image or Frames.

<b>Prepocessing steps:</b>

  1) converting color image/frame to grayscale

  2) crop the center region of image/frame

  3) blur the frame (to remove noise)

  4) Apply Adapative thresolding (convering to binary image)

  5) Apply Image morphological operations (dilation and erosion)
  
  6) Down scale the cropped image to Model Input size
  
After the prepocessing, Down scaled image(which is OpenCV Mat) is converted to tensorflow lite input format.

<b>Note:</b>
Make sure that written digits are large and fits the middle square region shown in the app.

Below shows screenshots from the App.

![screenshot0](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_0.png)


![screenshot1](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_1.png)


![screenshot2](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_2.png)


![screenshot3](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_3.png)


![screenshot4](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_4.png)


![screenshot5](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_5.png)


![screenshot6](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_6.png)


![screenshot7](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_7.png)


![screenshot8](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_8.png)


![screenshot9](https://github.com/Rohithkvsp/MNIST-on-Android-with-TensorflowLite-and-OpenCV/blob/master/screenshots/Screenshot_9.png)



  
 



