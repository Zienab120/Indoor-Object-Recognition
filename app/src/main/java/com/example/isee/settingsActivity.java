package com.example.isee;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class settingsActivity extends AppCompatActivity {
    private TextToSpeech tts;
    protected static final int RESULT_SPEECH=1;
    float x1,x2,y1,y2;

    String choice;
    DBHelper DB;

    String vo;
    RadioButton male,female;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        DB=new DBHelper(this);
        male=findViewById(R.id.male);
        female=findViewById(R.id.female);

        TTS("Welcome to settings page ,say voice to change voice,say back to back to features page,say Exit for closing the application,To hear the list again swipe left,swipe right and say what you want".trim());

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
                    tts.stop();
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

                    TTS("say voice to change voice,say back to back to features page,say Exit for closing the application,To hear the list again swipe left,swipe right and say what you want".trim());
                }

                break;
        }
        return false;
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:
                if(requestCode==1){
                    ArrayList<String> text=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    choice=text.get(0);
                    if(choice.equals("back")){
                        Intent i = new Intent(settingsActivity.this, featuresActivity.class);
                        startActivity(i);
                    } else if (choice.equals("exit")) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    } else if (choice.equals("voice")) {

                        TTS("say female for female voice,male for male voice,swipe right and say what you want".trim());

                    } else if (choice.equals("female")) {
                        female.setChecked(true);
                        male.setChecked(false);

                        Cursor cursor=DB.getdata();
                        cursor.moveToNext();

                       DB.Update(cursor.getString(0),"female");

                    } else if (choice.equals("male")) {
                        female.setChecked(false);
                        male.setChecked(true);
                        Cursor cursor=DB.getdata();
                        cursor.moveToNext();
                        DB.Update(cursor.getString(0),"male");
                    }
                }
                break;
        }
    }

//    public void TTS(String input){
//        tts=new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                if (status == TextToSpeech.SUCCESS) {
//
//                    int result=tts.setLanguage(Locale.US);
//                    if(result==TextToSpeech.LANG_MISSING_DATA
//                            || result==TextToSpeech.LANG_NOT_SUPPORTED){
//                        Toast.makeText(getApplicationContext(),"This language is not supported",Toast.LENGTH_SHORT).show();
//                    }
//                }else{
//                    Toast.makeText(getApplicationContext(),"TTS Initialization failed",Toast.LENGTH_SHORT).show();
//
//                }
//
//
//
//                int rsl=tts.speak(input,TextToSpeech.QUEUE_FLUSH,null);
//                if(rsl==TextToSpeech.ERROR){
//                    Toast.makeText(getApplicationContext(),"Error in converting Text to Speech",Toast.LENGTH_SHORT).show();
//
//                }
//
//            }
//        });
//    }


    public void TTS(String input){
        Cursor cursor=DB.getdata();
        cursor.moveToNext();
        vo=cursor.getString(2);
        if(vo.equals("male")){
            female.setChecked(false);
            male.setChecked(true);
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
                    if(rsl==TextToSpeech.ERROR){
                        Toast.makeText(getApplicationContext(),"Error in converting Text to Speech",Toast.LENGTH_SHORT).show();

                    }

                }
            });

        } else if (vo.equals("female")) {
            female.setChecked(true);
            male.setChecked(false);

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
                    if(rsl==TextToSpeech.ERROR){
                        Toast.makeText(getApplicationContext(),"Error in converting Text to Speech",Toast.LENGTH_SHORT).show();

                    }

                }
            });
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        tts.stop();
        tts.shutdown();
    }
}