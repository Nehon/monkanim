package com.jme3.anim;

/**
 * Created by Nehon on 05/07/2016.
 * An AnimationMask is defining a subset of elements on which an animation will be applied.
 * Most used implementation is the SkeletonMask that defines a subset of bones in a skeleton.
 */
public interface AnimationMask {

    /**
     * Should return true if the given index is not masked and the element that it reference should be affected by the animation.
     * @param index the index to lookup in the mask
     * @return true if the given index is not masked.
     */
    boolean isAffected(int index);

}
