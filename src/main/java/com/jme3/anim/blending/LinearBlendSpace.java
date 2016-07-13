package com.jme3.anim.blending;

import com.jme3.animation.*;
import com.jme3.math.FastMath;
import com.simsilica.lemur.core.*;


import java.util.*;

/**
 * Created by Nehon on 11/07/2016.
 */
public class LinearBlendSpace implements BlendSpace {

    private float value;

    @Override
    public void blend(List<Anim> animations, Map<Animation, Float> weightedAnimMap, float globalWeight) {
        if(animations.size() == 1 || value == 0){
            animations.get(0).resolve(weightedAnimMap, globalWeight);
            return;
        }
        if(value == 1){
            animations.get(animations.size() - 1).resolve(weightedAnimMap, globalWeight);
            return;
        }

        float scaledWeight = value * (animations.size() - 1);
        int highIndex = (int) FastMath.ceil(scaledWeight);
        int lowIndex = highIndex - 1;

//        System.err.println(name + "x" + globalWeight + ":" + animations.get(lowIndex) +":" + ((1 - (scaledWeight - lowIndex)) * globalWeight) + ", "+ animations.get(highIndex) +":" + ((scaledWeight - lowIndex) * globalWeight));
        animations.get(lowIndex).resolve(weightedAnimMap, (1 - (scaledWeight - lowIndex)) * globalWeight);
        animations.get(highIndex).resolve(weightedAnimMap, (scaledWeight - lowIndex) * globalWeight);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
