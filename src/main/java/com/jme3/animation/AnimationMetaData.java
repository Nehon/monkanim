package com.jme3.animation;

/**
 * Created by Nehon on 09/07/2016.
 * This class contains informations for the underlying animations structure.
 * Before the AnimControl was passed to the lower layers of the animation system to be able to get the skeleton.
 * I could have passed the skeleton directly but f one day we support pose animation we might need other information.
 */
public class AnimationMetaData {
    private Skeleton skeleton;

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public void setSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
    }
}
