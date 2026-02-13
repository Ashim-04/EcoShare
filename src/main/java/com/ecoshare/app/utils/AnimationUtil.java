package com.ecoshare.app.utils;

import android.app.Activity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

import androidx.core.app.ActivityOptionsCompat;

import com.ecoshare.app.R;

public class AnimationUtil {

    public static void fadeIn(View view) {
        fadeIn(view, 300);
    }

    public static void fadeIn(View view, long duration) {
        if (view == null) return;
        
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(duration);
        fadeIn.setFillAfter(true);
        view.startAnimation(fadeIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void fadeOut(View view) {
        fadeOut(view, 300);
    }

    public static void fadeOut(View view, long duration) {
        if (view == null) return;
        
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(duration);
        fadeOut.setFillAfter(true);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(fadeOut);
    }

    public static void slideUp(View view) {
        slideUp(view, 300);
    }

    public static void slideUp(View view, long duration) {
        if (view == null) return;
        
        TranslateAnimation slideUp = new TranslateAnimation(
            0, 0, view.getHeight(), 0
        );
        slideUp.setDuration(duration);
        slideUp.setFillAfter(true);
        view.startAnimation(slideUp);
        view.setVisibility(View.VISIBLE);
    }

    public static void slideDown(View view) {
        slideDown(view, 300);
    }

    public static void slideDown(View view, long duration) {
        if (view == null) return;
        
        TranslateAnimation slideDown = new TranslateAnimation(
            0, 0, 0, view.getHeight()
        );
        slideDown.setDuration(duration);
        slideDown.setFillAfter(true);
        slideDown.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                view.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        view.startAnimation(slideDown);
    }

    public static void scaleIn(View view) {
        scaleIn(view, 200);
    }

    public static void scaleIn(View view, long duration) {
        if (view == null) return;
        
        ScaleAnimation scaleIn = new ScaleAnimation(
            0.0f, 1.0f, 
            0.0f, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        scaleIn.setDuration(duration);
        scaleIn.setFillAfter(true);
        view.startAnimation(scaleIn);
        view.setVisibility(View.VISIBLE);
    }

    public static void pulse(View view) {
        if (view == null) return;
        
        ScaleAnimation pulse = new ScaleAnimation(
            1.0f, 1.1f,
            1.0f, 1.1f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        );
        pulse.setDuration(100);
        pulse.setRepeatMode(Animation.REVERSE);
        pulse.setRepeatCount(1);
        view.startAnimation(pulse);
    }

    public static void shake(View view) {
        if (view == null || view.getContext() == null) return;
        
        Animation shake = AnimationUtils.loadAnimation(view.getContext(), 
            android.R.anim.fade_in);
        view.startAnimation(shake);
    }
}
