package com.example.testfoodplanner.utils;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import androidx.recyclerview.widget.RecyclerView;
import android.content.Context;
import com.example.testfoodplanner.R;

public class AnimationHelper {

    /**
     * Add slide-up animation to RecyclerView items
     */
    public static void setRecyclerViewAnimation(Context context, RecyclerView recyclerView) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.item_animation_from_bottom);
        LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);
        layoutAnimationController.setDelay(0.1f);
        layoutAnimationController.setOrder(LayoutAnimationController.ORDER_NORMAL);
        recyclerView.setLayoutAnimation(layoutAnimationController);
    }

    /**
     * Run layout animation on RecyclerView
     */
    public static void runLayoutAnimation(RecyclerView recyclerView) {
        if (recyclerView.getLayoutAnimation() != null) {
            recyclerView.scheduleLayoutAnimation();
        }
    }
}
