package com.jme3.animation;

/**
 * Created by Nehon on 05/07/2016.
 * An AnimationMask is defining a subset of elements on which an animation will be applied.
 * Most used implementation is the SkeletonMask that defines a subset of bones in a skeleton.
 */
public interface AnimationMask {

    /**
     * Should return the weight (0 to 1) of the given index. This represent how much the element that it reference should be affected by the animation.
     * @param index the index to lookup in the mask
     * @return the weight of the given index.
     */
    float getWeight(int index);

}
