package com.jme3.anim.blending;

import com.jme3.animation.Animation;
import com.jme3.animation.AnimationMask;

/**
 * Created by Nehon on 15/07/2016.
 */
public class BlendingData {

    private float weight = 1;
    private float time = 0;
    private Animation animation = null;
    private AnimationMask mask;

    public BlendingData() {
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getTime() {
        return time;
    }

    public void setTime(float time) {
        this.time = time;
    }

    public Animation getAnimation() {
        return animation;
    }

    public void setAnimation(Animation animation) {
        this.animation = animation;
    }

    public void setMask(AnimationMask mask) {
        this.mask = mask;
    }

    public AnimationMask getMask() {
        return mask;
    }
}


