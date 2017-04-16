package com.jme3.anim.interpolator;

import com.jme3.math.EaseFunction;

/**
 * Created by nehon on 15/04/17.
 */
public abstract class AnimInterpolator<T> {

    public abstract T interpolate(float t, int currentIndex, TrackDataReader<T> data, TrackTimeReader times, T store);

}