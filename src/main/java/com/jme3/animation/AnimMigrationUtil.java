package com.jme3.animation;

/**
 * Created by Nehon on 03/07/2016.
 */
public class AnimMigrationUtil {


    public static AnimationClip fromAnimation(Animation anim){
        AnimationClip clip = new AnimationClip(anim.getName(), anim.getLength());
        for (Track track : anim.getTracks()) {
            if(track instanceof BoneTrack){
                BoneTrack bt = (BoneTrack)track;
                AnimBoneTrack abt = new AnimBoneTrack(bt.getTargetBoneIndex(),bt.getTimes(),bt.getTranslations(),bt.getRotations(), bt.getScales());
                clip.addTrack(abt);
            }
        }
        return clip;
    }

    public static AnimationManager fromAnimControl(AnimControl control){
        AnimationManager manager = new AnimationManager(control.getSkeleton());
        for (String name : control.getAnimationNames()) {
            Animation anim = control.getAnim(name);
            manager.addAnimationClip(fromAnimation(anim));

            AnimationSequence sequence = manager.createAnimationSequence(name, name);
        }

        return manager;
    }


}
