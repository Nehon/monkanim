package com.jme3.animation;

import com.jme3.anim.blending.*;

import java.util.*;

/**
 * Created by Nehon on 09/07/2016.
 */
public interface Anim {

    float getLength();

    void resolve(List<AnimationData> weightedAnims, float globalWeight, float time, AnimationMask mask);

}
