package com.jme3.anim.interpolator;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;

/**
 * Created by nehon on 15/04/17.
 */
public class AnimInterpolators {

    public static final AnimInterpolator<Quaternion> NLerp = new AnimInterpolator<Quaternion>() {
        private Quaternion next = new Quaternion();
        @Override
        public Quaternion interpolate(float t, int currentIndex, TrackDataReader<Quaternion> data,TrackTimeReader times, Quaternion store) {
            data.getEntry(currentIndex, store);
            data.getEntry(currentIndex + 1, next);
            store.nlerp(next, t);
            return store;
        }
    };

    public static final AnimInterpolator<Quaternion> SLerp = new AnimInterpolator<Quaternion>() {
        private Quaternion next = new Quaternion();
        @Override
        public Quaternion interpolate(float t, int currentIndex, TrackDataReader<Quaternion> data, TrackTimeReader times,Quaternion store) {
            data.getEntry(currentIndex, store);
            data.getEntry(currentIndex + 1, next);
            store.slerp(next, t);
            return store;
        }
    };

    public static final AnimInterpolator<Vector3f> LinearVec3f = new AnimInterpolator<Vector3f>() {
        private Vector3f next = new Vector3f();
        @Override
        public Vector3f interpolate(float t, int currentIndex, TrackDataReader<Vector3f> data, TrackTimeReader times,Vector3f store) {
            data.getEntry(currentIndex, store);
            data.getEntry(currentIndex + 1, next);
            store.interpolateLocal(next, t);
            return store;
        }
    };

//    /**
//     * Cubic hermit spline interpolation with finite difference tangents.
//     * https://en.wikipedia.org/wiki/Cubic_Hermite_spline
//     */
//    public static AnimInterpolator<Vector3f> CubicVec3f = new AnimInterpolator<Vector3f>() {
//        private Vector3f p0 = new Vector3f();
//        private Vector3f p1 = new Vector3f();
//        private Vector3f p2 = new Vector3f();
//        private Vector3f p3 = new Vector3f();
//
//        //tangents for p1 and p2
//        private Vector3f m1 = new Vector3f();
//        private Vector3f m2 = new Vector3f();

//
//        @Override
//        public Vector3f interpolate(float t, int currentIndex, TrackDataReader<Vector3f> data, TrackTimeReader times, Vector3f store) {
//            data.getEntry(currentIndex - 1, p0);
//            data.getEntry(currentIndex, p1);
//            data.getEntry(currentIndex + 1, p2);
//            data.getEntry(currentIndex + 2, p3);
//
//            float tm0 = times.getEntry(currentIndex - 1);
//            float tm1 = times.getEntry(currentIndex);
//            float tm2 = times.getEntry(currentIndex + 1);
//            float tm3 = times.getEntry(currentIndex + 2);
//
//            computeTangent(p0, p1, p2, tm0, tm1, tm2, m1);
//            computeTangent(p1, p2, p3, tm1, tm2, tm3, m2);
//
//            float t2 = t * t;
//            float t3 = t2 * t;
//            p1.multLocal(2f * t3 - 3f * t2 + 1f);
//            m1.multLocal(t3 - 2f * t2 + t);
//            p2.multLocal(-2f * t3 + 3f * t2);
//            m2.multLocal(t3 - t2);
//
//            store.set(p1).addLocal(m1).addLocal(p2).addLocal(m2);
//
//            return store;
//        }
//
//        private void computeTangent(Vector3f pk0,Vector3f pk,Vector3f pk1, float t0, float t, float t1, Vector3f tangent ){
//            tangent.x = computeTangent(pk0.x, pk.x, pk1.x, t0, t, t1);
//            tangent.y = computeTangent(pk0.y, pk.y, pk1.y, t0, t, t1);
//            tangent.z = computeTangent(pk0.z, pk.z, pk1.z, t0, t, t1);
//        }
//
//        private float computeTangent(float f0, float f, float f1, float t0, float t, float t1){
//            return ((f1 - f)/(t1 - t) + (f - f0) / (t - t0)) * 0.5f;
//        }
//    };

    /**
     * CatmullRom interpolation
     */
    public static final CatmullRomInterpolator CatmullRom = new CatmullRomInterpolator();
    public static class CatmullRomInterpolator extends AnimInterpolator<Vector3f> {
        private Vector3f p0 = new Vector3f();
        private Vector3f p1 = new Vector3f();
        private Vector3f p2 = new Vector3f();
        private Vector3f p3 = new Vector3f();
        private float tension = 0.7f;

        public CatmullRomInterpolator(float tension) {
            this.tension = tension;
        }

        public CatmullRomInterpolator() {
        }

        @Override
        public Vector3f interpolate(float t, int currentIndex, TrackDataReader<Vector3f> data, TrackTimeReader times, Vector3f store) {
            data.getEntry(currentIndex - 1, p0);
            data.getEntry(currentIndex, p1);
            data.getEntry(currentIndex + 1, p2);
            data.getEntry(currentIndex + 2, p3);

            FastMath.interpolateCatmullRom(t, tension, p0,p1,p2,p3, store);
            return store;
        }
    };

}
