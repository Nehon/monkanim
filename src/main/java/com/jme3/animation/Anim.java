package com.jme3.animation;

import com.jme3.anim.blending.*;
import com.jme3.util.SafeArrayList;

import java.util.*;

/**
 * Created by Nehon on 09/07/2016.
 */
public interface Anim {

    float getLength();
    void resolve(BlendingDataPool weightedAnims, float globalWeight, float time, AnimationMask mask);

}
