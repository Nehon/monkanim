package com.jme3.anim;

import com.jme3.animation.AnimationMask;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;

/**
 * Created by Nehon on 15/03/2017.
 */
public class AnimationLayer implements AnimationMask, Cloneable, JmeCloneable {

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

    @Override
    public Object jmeClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't clone AnimationLayer");
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        AnimationLayer layer = (AnimationLayer) original;
        //we can share the mask it should be stateless.
        this.mask = layer.mask;
        this.activeState = cloner.clone(activeState);
    }

}
