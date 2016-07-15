package com.jme3.animation;

import com.jme3.util.SafeArrayList;

import java.util.Map;

/**
 * Created by Nehon on 09/07/2016.
 */
public interface Anim {

    float getLength();
    void resolve(SafeArrayList<Animation> weightedAnims, float globalWeight, float time);

}
