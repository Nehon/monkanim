package com.jme3.anim.blending;

import com.jme3.anim.interpolator.FrameInterpolator;
import com.jme3.animation.*;
import com.jme3.util.TempVars;

import java.util.List;

/**
 * Created by Nehon on 15/07/2016.
 */
public class AnimationData implements Anim {

    private float weight = 1;
    private float time = 0;
    private Animation animation = null;
    private AnimationMask mask;
    private float scale = 1.0f;
    private FrameInterpolator interpolator = FrameInterpolator.DEFAULT;

    public AnimationData() {
    }

    public AnimationData(Animation anim) {
        this.animation = anim;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public float getTime() {
        return time / scale;
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

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setTrackInterpolator(FrameInterpolator interpolator) {
        this.interpolator = interpolator;
    }

    @Override
    public float getLength() {
        return animation.getLength() * scale;
    }

    public FrameInterpolator getTrackInterpolator(){
        if(interpolator == FrameInterpolator.DEFAULT){
            interpolator = new FrameInterpolator();
        }
        return interpolator;
    }

    @Override
    public void resolve(List<AnimationData> weightedAnims, float globalWeight, float time, AnimationMask mask) {
        if (globalWeight == 0) {
            return;
        }

        setWeight(globalWeight);
        setTime(time);
        setMask(mask);
        weightedAnims.add(this);
    }

    public void update(AnimationMetaData metadata, TempVars vars) {
        animation.setTime(getTime(), getWeight(), metadata, getMask(), vars, interpolator);
    }
}


