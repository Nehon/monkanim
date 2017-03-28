package com.jme3.anim.blending;

import com.jme3.animation.*;

import java.util.*;

/**
 * Created by Nehon on 05/07/2016.
 */
public interface BlendSpace {

    void blend(List<Anim> anims, List<AnimationData> weightedAnims, float globalWeight, float time, AnimationMask mask);

}
