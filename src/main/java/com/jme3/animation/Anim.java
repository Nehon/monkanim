package com.jme3.animation;

import java.util.Map;

/**
 * Created by Nehon on 09/07/2016.
 */
public interface Anim {

    float getLength();
    void resolve(Map<Animation, Float> weightedAnimMap, float globalWeight);

}
