package com.jme3.anim.interpolator;

import com.jme3.animation.CompactQuaternionArray;
import com.jme3.animation.CompactVector3Array;
import com.jme3.math.EaseFunction;
import com.jme3.math.Quaternion;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;

/**
 * Created by nehon on 15/04/17.
 */
public class TrackInterpolator {

    public static final TrackInterpolator DEFAULT = new TrackInterpolator();

    private EaseFunction easeFunction;
    private AnimInterpolator<Vector3f> translationInterpolator = AnimInterpolators.LinearVec3f;
    private AnimInterpolator<Quaternion> rotationInterpolator = AnimInterpolators.NLerp;
    private AnimInterpolator<Vector3f> scaleInterpolator = AnimInterpolators.LinearVec3f;

    private TrackDataReader<Vector3f> translationReader = new TrackDataReader<>();
    private TrackDataReader<Quaternion> rotationReader = new TrackDataReader<>();
    private TrackDataReader<Vector3f> scaleReader = new TrackDataReader<>();

    private Transform transforms = new Transform();

    public Transform interpolate(float t, int currentIndex, CompactVector3Array translations, CompactQuaternionArray rotations, CompactVector3Array scales){
        if( easeFunction != null){
            t = easeFunction.apply(t);
        }
        if(translations != null) {
            translationReader.setData(translations);
            translationInterpolator.interpolate(t, currentIndex, translationReader, transforms.getTranslation());
        }
        if(rotations != null) {
            rotationReader.setData(rotations);
            rotationInterpolator.interpolate(t, currentIndex, rotationReader, transforms.getRotation());
        }
        if(scales != null){
            scaleReader.setData(scales);
            scaleInterpolator.interpolate(t, currentIndex, scaleReader, transforms.getScale());
        }
        return transforms;
    }

    public void setEaseFunction(EaseFunction easeFunction) {
        this.easeFunction = easeFunction;
    }

    public void setTranslationInterpolator(AnimInterpolator<Vector3f> translationInterpolator) {
        this.translationInterpolator = translationInterpolator;
    }

    public void setRotationInterpolator(AnimInterpolator<Quaternion> rotationInterpolator) {
        this.rotationInterpolator = rotationInterpolator;
    }

    public void setScaleInterpolator(AnimInterpolator<Vector3f> scaleInterpolator) {
        this.scaleInterpolator = scaleInterpolator;
    }
}
