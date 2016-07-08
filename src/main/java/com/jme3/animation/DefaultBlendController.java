package com.jme3.animation;

/**
 * Created by Nehon on 02/07/2016.
 */
public class DefaultBlendController{

    private float factor = 0.5f;

    public DefaultBlendController() {
    }

    public DefaultBlendController(float factor) {
        this.factor = factor;
    }


    public float getBlendFactor(float time) {
        return factor;
    }
}
