package com.jme3.anim.interpolator;

import com.jme3.animation.CompactArray;
import com.jme3.animation.CompactVector3Array;

/**
 * Created by nehon on 15/04/17.
 */
public class TrackDataReader<T> {

    private CompactArray<T> data;

    protected void setData(CompactArray<T> data){
        this.data = data;
    }

    public T getEntry(int index, T store){
        return data.get(mod(index, data.getTotalObjectSize()), store);
    }

    private int mod(int val, int n) {
        return ((val%n)+n)%n;
    };

}
