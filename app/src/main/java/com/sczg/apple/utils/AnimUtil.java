package com.sczg.apple.utils;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.sczg.apple.App;
import com.sczg.apple.R;

public class AnimUtil {

    public static Animation alph02All(){
        return AnimationUtils.loadAnimation(App.getAppContext(), R.anim.alph02all);
    }

    public static  Animation alphHalf2All(){
        return AnimationUtils.loadAnimation(App.getAppContext(),R.anim.alph_half2all);
    }
}
