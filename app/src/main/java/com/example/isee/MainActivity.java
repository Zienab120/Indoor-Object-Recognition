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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    protected static final int RESULT_SPEECH=1;

    private TextToSpeech tts;

    DBHelper DB;
    String welcomeMsg;
    String name;
    int exist=1;
    String vo;
    TextView welcome ;

float x1,x2,y1,y2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DB=new DBHelper(this);
        Cursor res=DB.getdata();


          tts=new TextToSpeech(this, new TextToSpeech.OnInitListener() {

        @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    {
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

                welcome=findViewById(R.id.welcome);

                if(res.getCount()==0){
                    exist=0;
                    welcome.setText("Welcome.....!");
                    welcomeMsg="Welcome to I See app , swipe right and say your name".trim();
                }else{
                    while (res.moveToNext()){
                        name=res.getString(0);
                        welcome.setText("Welcome "+name.substring(0, 1).toUpperCase()+ name.substring(1));
                        welcomeMsg="Hello"+name+",Welcome to I See app ,swipe left to listen the features of the app and swipe right and say what you want".trim();
                        vo=res.getString(2);
                        if(vo.equals("female")){
                            tts.setLanguage(Locale.US);
                        }
                    }

                }

                int rsl=tts.speak(welcomeMsg,TextToSpeech.QUEUE_FLUSH,null);
                if(rsl==TextToSpeech.ERROR){
                    Toast.makeText(getApplicationContext(),"Error in converting Text to Speech",Toast.LENGTH_SHORT).show();

                }

            }
        });



    }

    //onDestroy() is a method called by the framework when your activity is closing down. It is called to allow your activity to do any shut-down operations it may wish to do
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(tts!=null){
            tts.stop();
            tts.shutdown();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        tts.stop();
        tts.shutdown();
    }

    //swipe to left
public boolean onTouchEvent(MotionEvent touchEvent){
        switch (touchEvent.getAction()){
            case MotionEvent.ACTION_DOWN:
                x1=touchEvent.getX();
                y1=touchEvent.getY();
                break;
            case MotionEvent.ACTION_UP:
                x2=touchEvent.getX();
                y2=touchEvent.getY();
                if(x1 < x2) {
                    Intent i = new Intent(MainActivity.this, featuresActivity.class);
                    startActivity(i);
                } else if(x1 > x2 && exist==0) {
                    exist=1;
                    Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en_US");
                    try{
                        startActivityForResult(intent,RESULT_SPEECH);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(getApplicationContext(),"Your device doesn't support Speech to Text",Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                }

            break;
        }
    return false;
}


    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case RESULT_SPEECH:
                if(requestCode==1)
                {
                    ArrayList<String> text=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    name=text.get(0);
                    DB.Insert(name);
                    welcomeMsg="Welcome " +name+",swipe left to listen the features of the app and swipe right and say what you want".trim();
                    int rsl=tts.speak(welcomeMsg,TextToSpeech.QUEUE_FLUSH,null);
                    if(rsl==TextToSpeech.ERROR) {
                        Toast.makeText(getApplicationContext(), "Error in converting Text to Speech", Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }
}