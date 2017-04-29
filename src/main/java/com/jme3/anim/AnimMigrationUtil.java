package com.jme3.anim;

import com.jme3.animation.*;

/**
 * Created by Nehon on 03/07/2016.
 */
public class AnimMigrationUtil {


//    public static AnimationClip fromAnimation(Animation anim){
//        AnimationClip clip = new AnimationClip(anim.getName(), anim.getLength());
//        for (Track track : anim.getTracks()) {
//            //only use boneTrack for now but this will change...
//            if(track instanceof com.jme3.animation.BoneTrack){
//                clip.addTrack(track);
//            }
//        }
//        return clip;
//    }

    public static AnimationManager fromAnimControl(AnimControl control){
        AnimationManager manager = new AnimationManager(control.getSkeleton());
        for (String name : control.getAnimationNames()) {
            Animation anim = control.getAnim(name);
            manager.addAnimation(stripSpatialAnim(anim));
        }

        return manager;
    }

    private static Animation stripSpatialAnim(Animation anim) {
        Track[] tracks = anim.getTracks();
        float length =0;
        for (Track track : tracks) {
            if (track instanceof SpatialTrack) {
                continue;
            }
            float[] times = track.getKeyFrameTimes();
            length = Math.max(length, times[times.length - 1]);
        }

        Animation newAnim = new Animation(anim.getName(), length);

        for (Track track : tracks) {
            if (track instanceof SpatialTrack) {
                continue;
            }
            newAnim.addTrack(track);
        }
        return newAnim;
    }


}
