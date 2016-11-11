package com.orygenapps.sema.activity;

import android.app.Activity;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.orygenapps.sema.R;

/**
 * Created by starehe on 20/02/15.
 */
public class Fader {

    public static void fadeIn(Activity activity, int viewId) {

        // load animation XML resource under res/anim
        Animation animation  = AnimationUtils.loadAnimation(activity, R.anim.fade_in);
        if(animation == null){
            return;
        }
        // reset initialization state
        animation.reset();
        // find View by its id attribute in the XML
        View v = activity.findViewById(viewId);
        // cancel any pending animation and start this one
        if (v != null){
            v.clearAnimation();
            v.startAnimation(animation);
        }
    }

    public static void fadeOut(Activity activity, int viewId) {

        // load animation XML resource under res/anim
        Animation animation  = AnimationUtils.loadAnimation(activity, R.anim.fade_out);
        if(animation == null){
            return;
        }
        // reset initialization state
        animation.reset();
        // find View by its id attribute in the XML
        View v = activity.findViewById(viewId);
        // cancel any pending animation and start this one
        if (v != null){
            v.clearAnimation();
            v.startAnimation(animation);
        }
    }
}
