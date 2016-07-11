package com.jme3.animation;

import com.jme3.scene.Spatial;

/**
 * Created by Nehon on 09/07/2016.
 * This class contains information for the underlying animations structure.
 * Before the AnimControl was passed to the lower layers of the animation system to be able to get the skeleton.
 * I could have passed the skeleton directly but if one day we support pose animation we might need other information.
 */
public class AnimationMetaData {
    /**
    * needed for bone animation
    */
    private Skeleton skeleton;
    /**
     * needed for spatial animation
     */
    private Spatial spatial;

    public Skeleton getSkeleton() {
        return skeleton;
    }

    public void setSkeleton(Skeleton skeleton) {
        this.skeleton = skeleton;
    }

    public Spatial getSpatial() {
        return spatial;
    }

    public void setSpatial(Spatial spatial) {
        this.spatial = spatial;
    }
}
