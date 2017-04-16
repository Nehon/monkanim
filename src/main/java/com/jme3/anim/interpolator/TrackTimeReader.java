package com.jme3.anim.interpolator;

import com.jme3.animation.CompactArray;

/**
 * Created by nehon on 15/04/17.
 */
public class TrackTimeReader {
    private float[] data;

    protected void setData(float[] data){
        this.data = data;
    }

    public float getEntry(int index){
        return data[mod(index, data.length)];
    }

    private int mod(int val, int n) {
        return ((val%n)+n)%n;
    };
}
