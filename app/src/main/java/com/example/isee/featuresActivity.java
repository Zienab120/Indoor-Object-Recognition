package com.example.isee;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class featuresActivity extends AppCompatActivity {

    protected static final int RESULT_SPEECH=1;
    private TextToSpeech tts;
    float x1,x2,y1,y2;
    String choice;
    String vo;
    DBHelper DB;
    TextView Search,Settings,Recognize,Exit;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features);
        DB=new DBHelper(this);
        Search=findViewById(R.id.search);
        Settings=findViewById(R.id.settings);
        Recognize=findViewById(R.id.recognize);
        Exit=findViewById(R.id.exit);

        TTS("Welcome to home page ,say settings for update settings , search for followed by the object name to search for something,recognize for object recognition,say Exit for closing the application,To hear the list again swipe left,swipe right and say what you want".trim());
    }


    public boolean onTouchEvent(MotionEvent touchEvent){
        switch (touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1=touchEvent.getX();
                y1=touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2=touchEvent.getX();
                y2=touchEvent.getY();
                if(x1 > x2) {
//                    tts.stop();
//                    tts.shutdown();
                    Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en_US");
                    try{
                        startActivityForResult(intent,RESULT_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(),"Your device doesn't support Speech to Text",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                } else if (x1<x2) {
                    tts.stop();
//                    tts.shutdown();

                    TTS("say settings for update settings , search for followed by the object name to search for something,recognize for object recognition,say Exit for closing the application,To hear the list again swipe left,swipe right and say what you want".trim());
                }
                break;
        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:
                if(requestCode==1){
                    ArrayList<String> text=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    choice=text.get(0);
                    String[] newChoice = (choice.split(" "));
                     String[] searchChoice = (choice.split(" "));
                    if(newChoice[0].equals("settings")){
                        Intent i = new Intent(featuresActivity.this, settingsActivity.class);
                        startActivity(i);
                    } else if (newChoice[0].equals("exit")) {
                        moveTaskToBack(true);
                       android.os.Process.killProcess(android.os.Process.myPid());
                       System.exit(1);
                    } else if (newChoice[0].equals("recognize")||newChoice[0].equals("recognise")) {
                        TTS("Welcome to recognition page ,To finish recognizing, swipe left");
                        Intent i = new Intent(featuresActivity.this, camera.class);
                        i.putExtra("Recognition", newChoice[0]);
                        startActivity(i);
                    } else if (newChoice[0].equals("search")) {
                        TTS("Welcome to search page ,To finish searching, swipe left");
                        Intent i = new Intent(featuresActivity.this, camera.class);
                        i.putExtra("Recognition", newChoice[0]);
                        i.putExtra("Searching", searchChoice[2]);
                        startActivity(i);
                    }
                }
                break;
        }
    }


    public void TTS(String input){
        Cursor cursor=DB.getdata();
        cursor.moveToNext();
        vo=cursor.getString(2);
        if(vo.equals("male")){

            tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {

                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            for(Voice voice: tts.getVoices()){
                                Log.d("onInitInitializedVoice",voice.getName());
                            }
                            int result=tts.setLanguage(Locale.forLanguageTag("hi"));
                            if(result==TextToSpeech.LANG_MISSING_DATA
                                    || result==TextToSpeech.LANG_NOT_SUPPORTED){
                                Log.d("onInit","language is not installed");
                                tts.setLanguage(Locale.forLanguageTag("en"));
                            }else{
                                tts.setLanguage(Locale.forLanguageTag("hi"));

                                Voice voice=new Voice("hi-in-x-hid-network",new Locale("hi","IN"),400,200,false,null);
                                tts.setVoice(voice);

                            }
                        }

                    }else{
                        Log.d("OnInit:","initialization failed");
                    }



                    int rsl=tts.speak(input,TextToSpeech.QUEUE_FLUSH,null);
//                    Settings.setTextColor(Color.parseColor("#89CFF0"));
//                    Recognize.setTextColor(Color.WHITE);
//                    Search.setTextColor(Color.WHITE);
//                    Exit.setTextColor(Color.WHITE);
//                    Delay(2000,Search);
//                    Settings.setTextColor(Color.WHITE);
////                    Search.setTextColor(Color.parseColor("#89CFF0"));
//                    Recognize.setTextColor(Color.WHITE);
//                    Exit.setTextColor(Color.WHITE);
//                    Delay(2000,Recognize);
//                    Settings.setTextColor(Color.WHITE);
//                    Search.setTextColor(Color.WHITE);
////                    Recognize.setTextColor(Color.parseColor("#89CFF0"));
//                    Exit.setTextColor(Color.WHITE);
//                    Delay(2000,Exit);
//                    Settings.setTextColor(Color.WHITE);
//                    Search.setTextColor(Color.WHITE);
//                    Recognize.setTextColor(Color.WHITE);
//                    //////////
//                    Exit.setTextColor(Color.parseColor("#89CFF0"));
////                    startBlinking(Settings);
//                    Delay(5000,Search);
////                    startBlinking(Search);
//                    Delay(5000,Recognize);
////                    startBlinking(Recognize);
//                    Delay(5000,Exit);
////                    startBlinking(Exit);
////                    Delay(5000);

                    if(rsl==TextToSpeech.ERROR){
                        Toast.makeText(getApplicationContext(),"Error in converting Text to Speech",Toast.LENGTH_SHORT).show();

                    }

                }
            });

        } else if (vo.equals("female")) {


            tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    if (status == TextToSpeech.SUCCESS) {
                        int result=tts.setLanguage(Locale.US);
                        if(result==TextToSpeech.LANG_MISSING_DATA
                                || result==TextToSpeech.LANG_NOT_SUPPORTED){
                            Toast.makeText(getApplicationContext(),"This language is not supported",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Toast.makeText(getApplicationContext(),"TTS Initialization failed",Toast.LENGTH_SHORT).show();

                    }


                    int rsl=tts.speak(input,TextToSpeech.QUEUE_FLUSH,null);
//                    Settings.setTextColor(Color.parseColor("#89CFF0"));
//                    Recognize.setTextColor(Color.WHITE);
//                    Search.setTextColor(Color.WHITE);
//                    Exit.setTextColor(Color.WHITE);
//                    Delay(2000,Search);
//                    Settings.setTextColor(Color.WHITE);
////                    Search.setTextColor(Color.parseColor("#89CFF0"));
//                    Recognize.setTextColor(Color.WHITE);
//                    Exit.setTextColor(Color.WHITE);
//                    Delay(2000,Recognize);
//                    Settings.setTextColor(Color.WHITE);
//                    Search.setTextColor(Color.WHITE);
////                    Recognize.setTextColor(Color.parseColor("#89CFF0"));
//                    Exit.setTextColor(Color.WHITE);
//                    Delay(2000,Exit);
//                    Settings.setTextColor(Color.WHITE);
//                    Search.setTextColor(Color.WHITE);
//                    Recognize.setTextColor(Color.WHITE);
////                    Exit.setTextColor(Color.parseColor("#89CFF0"));




//                    startBlinking(Settings);
//                    Delay(2000,Search);

////                    startBlinking(Settings);
//                    Delay(5000,Search);
////                    startBlinking(Search);
//                    Delay(5000,Recognize);
////                    startBlinking(Recognize);
//                    Delay(5000,Exit);
////                    startBlinking(Exit);
////                    Delay(5000);
                    if(rsl==TextToSpeech.ERROR){
                        Toast.makeText(getApplicationContext(),"Error in converting Text to Speech",Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }

    }

    @SuppressLint("WrongConstant")
    public void startBlinking(TextView textView){
        ObjectAnimator animator=ObjectAnimator.ofInt(textView,"textColor",Color.WHITE,Color.parseColor("#89CFF0"),Color.WHITE);
        animator.setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.setRepeatMode(Animation.REVERSE);
        animator.setRepeatCount(1);
        animator.start();
    }


    public void Delay(int milliseconds,TextView textView){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                textView.setTextColor(Color.parseColor("#89CFF0"));

            }
        },milliseconds);


    }

//    @Override
////    protected void onStop() {
////        super.onStop();
////        tts.stop();
////        tts.shutdown();
////    }
}