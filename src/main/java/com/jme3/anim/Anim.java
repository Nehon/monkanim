package com.jme3.anim;

import java.util.Map;

/**
 * Created by Nehon on 09/07/2016.
 */
public interface Anim {

    float getLength();
    void resolve(Map<AnimationClip, Float> weightedAnimMap, float globalWeight);

}
