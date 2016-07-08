package com.jme3.animation;

import java.util.BitSet;

import static java.awt.SystemColor.control;

/**
 * Created by Nehon on 05/07/2016.
 * Crates a mask that defines a list of affected bones by an animation
 */
public class SkeletonMask implements AnimationMask {

    private BitSet affectedBones;

    /**
     * creates a default skeleton mask where all bones will be affected by an animation
     */
    public SkeletonMask() {
    }

    /**
     * creates a skeleton mask where the given bones will be affected by an animation
     */
    public SkeletonMask(AnimationManager manager, String... boneNames) {
        addBones(manager, boneNames);
    }


    /**
     * returns true if the given bone index reference a bone that should be affected by the animation.
     * @param boneIndex the index of the bone to lookup
     * @return true if the given bone index reference a bone that should be affected by the animation.
     */
    @Override
    public boolean isAffected(int boneIndex) {
        if(affectedBones == null){
            return false;
        }
        return affectedBones.get(boneIndex);
    }

    /**
     * Add bones to be influenced by this animation mask.
     */
    public void addBones(AnimationManager manager, String... boneNames) {

        Skeleton skeleton = manager.getSkeleton();

        if(affectedBones == null) {
            affectedBones = new BitSet(skeleton.getBoneCount());
        }
        for (String boneName : boneNames) {
            affectedBones.set(skeleton.getBoneIndex(boneName));
        }
    }

}
