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
     * Should return the weight (0 to 1) of the given index. This represent how much the element that it reference should be affected by the animation.
     * @param boneIndex the index to lookup in the mask
     * @return the weight of the given index.
     */
    @Override
    public float getWeight(int boneIndex) {
        if (affectedBones == null) {
            return 1f;
        }
        return affectedBones.get(boneIndex)?1f:0f;
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

    public static SkeletonMask fromBone(Skeleton skeleton, String boneName){
        SkeletonMask m = new SkeletonMask();
        m.addFromBone(skeleton, boneName);
        return m;
    }

    private void recurseAddBone(Skeleton skeleton, Bone bone){
        affectedBones.set(skeleton.getBoneIndex(bone));
        for (Bone b : bone.getChildren()) {
            recurseAddBone(skeleton, b);
        }
    }
}
