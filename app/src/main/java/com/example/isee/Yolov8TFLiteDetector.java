package com.example.isee;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.FileUtil;
import org.tensorflow.lite.support.common.ops.CastOp;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Yolov8TFLiteDetector extends AppCompatActivity {
    private Context context;
    private String modelPath;

    protected static final int RESULT_SPEECH=1;
    private String labelPath;
    private String fucntion;
    private String searching;

//    String searchCls = "Fan";
    String clsName="";
    private DetectorListener detectorListener;

    private TextToSpeech tts;
    private Interpreter interpreter;
    private List<String> labels = new ArrayList<>();

    private int tensorWidth;
    private int tensorHeight;
    private int numChannel;
    private int numElements;

    private ImageProcessor imageProcessor = new ImageProcessor.Builder()
            .add(new NormalizeOp(INPUT_MEAN, INPUT_STANDARD_DEVIATION))
            .add(new CastOp(INPUT_IMAGE_TYPE))
            .build();

    public Yolov8TFLiteDetector(Context context, String modelPath, String labelPath, DetectorListener detectorListener, String function, String searching) {
        this.context = context;
        this.modelPath = "best_float16.tflite";
        this.labelPath = "labels.txt";
        this.detectorListener = detectorListener;
        this.fucntion = function;
        this.searching = searching;
    }



    public void setup() {
        try {
            MappedByteBuffer model = FileUtil.loadMappedFile(context, modelPath);
            byte[] modelBytes = new byte[model.remaining()];
            model.get(modelBytes);


//            byte[] model = FileUtil.loadMappedFile(context, modelPath).array();
            Interpreter.Options options = new Interpreter.Options();
//            options.setNumThreads(4);
            interpreter = new Interpreter(model, options);

            int[] inputShape = interpreter.getInputTensor(0).shape();
            int[] outputShape = interpreter.getOutputTensor(0).shape();

            tensorWidth = inputShape[1];
            tensorHeight = inputShape[2];
            numChannel = outputShape[1];
            numElements = outputShape[2];

            InputStream inputStream = context.getAssets().open(labelPath);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }

            reader.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clear() {
        if (interpreter != null) {
            interpreter.close();
            interpreter = null;
        }
    }

    public void detect(Bitmap frame) {
        if (interpreter == null || tensorWidth == 0 || tensorHeight == 0 || numChannel == 0 || numElements == 0) {
            return;
        }

        long inferenceTime = SystemClock.uptimeMillis();

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(frame, tensorWidth, tensorHeight, false);

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(resizedBitmap);
        TensorImage processedImage = imageProcessor.process(tensorImage);
        TensorBuffer output = TensorBuffer.createFixedSize(new int[]{1, numChannel, numElements}, OUTPUT_IMAGE_TYPE);
        interpreter.run(processedImage.getBuffer(), output.getBuffer());

        List<BoundingBox> bestBoxes = bestBox(output.getFloatArray());
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime;

        if (bestBoxes == null) {
            detectorListener.onEmptyDetect();
            return;
        }

        detectorListener.onDetect(bestBoxes, inferenceTime);

    }



    private List<BoundingBox> bestBox(float[] array) {
        List<BoundingBox> boundingBoxes = new ArrayList<>();

        for (int c = 0; c < numElements; c++) {
            float maxConf = -1.0f;
            int maxIdx = -1;
            int j = 4;
            int arrayIdx = c + numElements * j;

            while (j < numChannel) {
                if (array[arrayIdx] > maxConf) {
                    maxConf = array[arrayIdx];
                    maxIdx = j - 4;
                }
                j++;
                arrayIdx += numElements;
            }

            if (maxConf > CONFIDENCE_THRESHOLD) {
                clsName = labels.get(maxIdx);
                float cx = array[c];
                float cy = array[c + numElements];
                float w = array[c + numElements * 2];
                float h = array[c + numElements * 3];
                float x1 = cx - (w / 2F);
                float y1 = cy - (h / 2F);
                float x2 = cx + (w / 2F);
                float y2 = cy + (h / 2F);

                if (x1 < 0F || x1 > 1F || y1 < 0F || y1 > 1F || x2 < 0F || x2 > 1F || y2 < 0F || y2 > 1F) {
                    continue;
                }


                boundingBoxes.add(new BoundingBox(x1, y1, x2, y2, cx, cy, w, h, maxConf, maxIdx, clsName));
                if (clsName != null && (fucntion.equals("recognize") || fucntion.equals("recognise"))) {
                    Speaking(clsName);
//                    Searching("Fan".toLowerCase(), clsName.toLowerCase().toString());
                } else if (clsName != null && fucntion.equals("search")) {
//                    Searching(searchChoice.toLowerCase(), clsName.toLowerCase().toString());
                    Searching(searching, clsName.toLowerCase().toString());
                }
            }

        }

        if (boundingBoxes.isEmpty()) {
            return null;
        }

        return applyNMS(boundingBoxes);
    }


    private List<BoundingBox> applyNMS(List<BoundingBox> boxes) {
        List<BoundingBox> sortedBoxes = new ArrayList<>(boxes);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
        {
            sortedBoxes.sort((o1, o2) -> Float.compare(o2.getCnf(), o1.getCnf()));
        }

        List<BoundingBox> selectedBoxes = new ArrayList<>();

        while (!sortedBoxes.isEmpty()) {
            BoundingBox first = sortedBoxes.remove(0);
            selectedBoxes.add(first);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                sortedBoxes.removeIf(nextBox -> calculateIoU(first, nextBox) >= IOU_THRESHOLD);
            }
        }

        return selectedBoxes;
    }

    private float calculateIoU(BoundingBox box1, BoundingBox box2) {
        float x1 = Math.max(box1.getX1(), box2.getX1());
        float y1 = Math.max(box1.getY1(), box2.getY1());
        float x2 = Math.min(box1.getX2(), box2.getX2());
        float y2 = Math.min(box1.getY2(), box2.getY2());
        float intersectionArea = Math.max(0F, x2 - x1) * Math.max(0F, y2 - y1);
        float box1Area = box1.getW() * box1.getH();
        float box2Area = box2.getW() * box2.getH();
        return intersectionArea / (box1Area + box2Area - intersectionArea);
    }

    public interface DetectorListener {
        void onEmptyDetect();
        void onDetect(List<BoundingBox> boundingBoxes, long inferenceTime);
    }

    public void Speaking(String clsName){
        tts = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {

                // if No error is found then only it will run
                if (i != TextToSpeech.ERROR) {
                    // To Choose language of speech
                    tts.setLanguage(Locale.UK);
//                    tts.setSpeechRate(1.0f);
                    tts.speak(clsName,TextToSpeech.QUEUE_FLUSH,null);

                }
            }
        });

    }
//    @Override


//    String searchChoice ="";
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
////        switch (requestCode)
//        {
////            case RESULT_SPEECH:
//            if (requestCode == 1) {
//                ArrayList<String> text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                searchChoice = text.get(0);
//
////                    break;
//            }
//        }
//        if(fucntion.equals("search"))
//            Searching(searchChoice,clsName);
//        return searchChoice;
//    }

    public void Searching(String search, String found)
    {
        tts = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i != TextToSpeech.ERROR) {
                    tts.setLanguage(Locale.UK);
                    tts.speak("Say the object that you want to search for.",TextToSpeech.QUEUE_FLUSH,null);

                }
            }
        });

        if(!search.equals(found.toString()))
        {
            tts = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {

                    // if No error is found then only it will run
                    if (i != TextToSpeech.ERROR) {
                        // To Choose language of speech
                        tts.setLanguage(Locale.UK);
//                    tts.setSpeechRate(1.0f);
//                        tts.speak("The is a"+found+".",TextToSpeech.QUEUE_FLUSH,null);
                        tts.speak("There is a " + found + ". The " + search + " which you search for is not found here, please change your angle and search again.",TextToSpeech.QUEUE_FLUSH,null);

                    }
                }
            });
        }
        boolean check = false;
        if(search.equals(found.toString()))
            check = true;
        if(check)
        {
            tts = new TextToSpeech(context.getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int i) {

                    // if No error is found then only it will run
                    if (i != TextToSpeech.ERROR) {
                        // To Choose language of speech
                        tts.setLanguage(Locale.UK);
//                    tts.setSpeechRate(1.0f);
                        tts.speak("The " + search + " which you search for is here", TextToSpeech.QUEUE_FLUSH, null);

                    }
                }
            });
        }
    }




    private static final float INPUT_MEAN = 0f;
    private static final float INPUT_STANDARD_DEVIATION = 255f;
    private static final DataType INPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final DataType OUTPUT_IMAGE_TYPE = DataType.FLOAT32;
    private static final float CONFIDENCE_THRESHOLD = 0.3F;
    private static final float IOU_THRESHOLD = 0.5F;
}


