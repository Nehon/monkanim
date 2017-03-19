package com.jme3.anim;

import com.jme3.animation.AnimationMask;

/**
 * Created by Nehon on 15/03/2017.
 */
public class AnimationLayer implements AnimationMask {

    private float weight = 0f;
    private String name = "default";
    private AnimationMask mask;
    private AnimState activeState;
    private int index;

    AnimationLayer nextLayer;

    public AnimationLayer() {
    }

    public AnimationLayer(String name) {
        this.name = name;
    }

    public AnimationLayer(String name, AnimationMask mask) {
        this.mask = mask;
        this.name = name;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public AnimationMask getMask() {
        return mask;
    }

    public void setMask(AnimationMask mask) {
        this.mask = mask;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIndex() {
        return index;
    }

    protected void setIndex(int index) {
        this.index = index;
    }

    public AnimationLayer withMask(AnimationMask mask) {
        setMask(mask);
        return this;
    }

    public AnimState getActiveState() {
        return activeState;
    }

    public void setActiveState(AnimState activeState) {
        this.activeState = activeState;
    }

    public AnimationLayer getNextLayer() {
        return nextLayer;
    }

    public void setNextLayer(AnimationLayer nextLayer) {
        this.nextLayer = nextLayer;
    }

    @Override
    public float getWeight(int index) {
        return (mask == null ? 1f : mask.getWeight(index)) * (weight - (nextLayer == null ? 0f : nextLayer.getWeight(index)));
    }
}
