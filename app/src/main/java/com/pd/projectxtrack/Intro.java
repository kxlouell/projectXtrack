package com.pd.projectxtrack;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.ViewUtils;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

public class Intro extends AppCompatActivity {
    ObjectAnimator animation;
    AnimationSet replaceAnimation;
    ImageView xtrackLogo;
    TextView xtrackText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);



    }

    public void init(){
        xtrackLogo = (ImageView) findViewById(R.id.xtrack_logo);
        xtrackText = (TextView) findViewById(R.id.xtrack_text);
    }

    public void animateLogo(){

    }
    public void replace(View view,int xTo, int yTo, float xScale, float yScale) {

        // create set of animations
        replaceAnimation = new AnimationSet(false);
        // animations should be applied on the finish line
        replaceAnimation.setFillAfter(true);

        // create scale animation
        ScaleAnimation scale = new ScaleAnimation(1.0f, xScale, 1.0f, yScale);
        scale.setDuration(1000);

        // create translation animation
        TranslateAnimation trans = new TranslateAnimation(0, 0,
                TranslateAnimation.ABSOLUTE, xTo , 0, 0,
                TranslateAnimation.ABSOLUTE, yTo );
        trans.setDuration(1000);

        // add new animations to the set
        replaceAnimation.addAnimation(scale);
        replaceAnimation.addAnimation(trans);

        replaceAnimation.start();
    }

}