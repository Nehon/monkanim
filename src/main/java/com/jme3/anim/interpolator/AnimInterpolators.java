package com.jme3.anim.interpolator;

import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * Created by nehon on 15/04/17.
 */
public class AnimInterpolators {

    public static AnimInterpolator<Quaternion> NLerp = new AnimInterpolator<Quaternion>() {
        private Quaternion next = new Quaternion();
        @Override
        public Quaternion interpolate(float t, int currentIndex, TrackDataReader<Quaternion> data, Quaternion store) {
            data.getEntry(currentIndex, store);
            data.getEntry(currentIndex + 1, next);
            store.nlerp(next, t);
            return store;
        }
    };

    public static AnimInterpolator<Quaternion> SLerp = new AnimInterpolator<Quaternion>() {
        private Quaternion next = new Quaternion();
        @Override
        public Quaternion interpolate(float t, int currentIndex, TrackDataReader<Quaternion> data, Quaternion store) {
            data.getEntry(currentIndex, store);
            data.getEntry(currentIndex + 1, next);
            store.slerp(next, t);
            return store;
        }
    };

    public static AnimInterpolator<Vector3f> LinearVec3f = new AnimInterpolator<Vector3f>() {
        private Vector3f next = new Vector3f();
        @Override
        public Vector3f interpolate(float t, int currentIndex, TrackDataReader<Vector3f> data, Vector3f store) {
            data.getEntry(currentIndex, store);
            data.getEntry(currentIndex + 1, next);
            store.interpolateLocal(next, t);
            return store;
        }
    };

    public static AnimInterpolator<Vector3f> CubicVec3f = new AnimInterpolator<Vector3f>() {
        private Vector3f next = new Vector3f();
        @Override
        public Vector3f interpolate(float t, int currentIndex, TrackDataReader<Vector3f> data, Vector3f store) {
            data.getEntry(currentIndex, store);
            data.getEntry(currentIndex + 1, next);
            store.interpolateLocal(next, t);
            return store;
        }
    };

}
