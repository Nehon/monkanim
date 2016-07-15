package com.jme3.animation;

import java.util.BitSet;

import static xbuf_ext.AnimationsKf.AnimationKF.TargetKind.skeleton;

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
    public SkeletonMask(Skeleton skeleton, String... boneNames) {
        addBones(skeleton, boneNames);
    }


    /**
     * returns true if the given bone index reference a bone that should be affected by the animation.
     *
     * @param boneIndex the index of the bone to lookup
     * @return true if the given bone index reference a bone that should be affected by the animation.
     */
    @Override
    public boolean isAffected(int boneIndex) {
        if (affectedBones == null) {
            return false;
        }
        return affectedBones.get(boneIndex);
    }

    /**
     * Add bones to be influenced by this animation mask.
     */
    public void addBones(Skeleton skeleton, String... boneNames) {

        if (affectedBones == null) {
            affectedBones = new BitSet(skeleton.getBoneCount());
        }
        for (String boneName : boneNames) {
            affectedBones.set(skeleton.getBoneIndex(boneName));
        }
    }


    /**
     * Add a bone and all its sub skeleton bones to be influenced by this animation mask.
     */
    public void addFromBone(Skeleton skeleton, String boneName) {

        if (affectedBones == null) {
            affectedBones = new BitSet(skeleton.getBoneCount());
        }

        Bone bone = skeleton.getBone(boneName);
        recurseAddBone(skeleton, bone);

    }

    private void recurseAddBone(Skeleton skeleton, Bone bone){
        affectedBones.set(skeleton.getBoneIndex(bone));
        for (Bone b : bone.getChildren()) {
            recurseAddBone(skeleton, b);
        }
    }
}
