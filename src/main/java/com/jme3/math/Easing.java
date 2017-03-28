package com.jme3.math;

/**
 * Expose several Easing function from Robert Penner
 * Created by Nehon on 26/03/2017.
 */
public class Easing {

    /**
     * Linear Easing,basically returning the given value.
     */
    public static EaseFunction linear = new EaseFunction() {
        @Override
        public float apply(float value) {
            return value;
        }
    };

    /**
     * InQuad Easing. Quadratic
     */
    public static EaseFunction inQuad = new EaseFunction() {
        @Override
        public float apply(float value) {
            return value * value;
        }
    };

    /**
     * InCubic Easing. Cubic
     */
    public static EaseFunction inCubic = new EaseFunction() {
        @Override
        public float apply(float value) {
            return value * value * value;
        }
    };

    /**
     * InQuart Easing.
     */
    public static EaseFunction inQuart = new EaseFunction() {
        @Override
        public float apply(float value) {
            return value * value * value * value;
        }
    };

    /**
     * OutQuad Easing.
     */
    public static EaseFunction outQuad = new EaseFunction() {
        @Override
        public float apply(float value) {
            return -(value * (value - 2f));
        }
    };

    /**
     * OutCubic Easing.
     */
    public static EaseFunction outCubic = new EaseFunction() {
        @Override
        public float apply(float value) {
            value -= 1f;
            return value * value * value + 1f;
        }
    };

    /**
     * OutQuart Easing.
     */
    public static EaseFunction outQuart = new EaseFunction() {
        @Override
        public float apply(float value) {
            value -= 1f;
            return -(value * value * value * value - 1f);
        }
    };

    /**
     * OutElastic Easing.
     */
    public static EaseFunction outElastic = new EaseFunction() {
        @Override
        public float apply(float value) {
            return FastMath.pow(2f, -10f * value) * FastMath.sin((value - 0.3f / 4f) * (2f * FastMath.PI) / 0.3f) + 1f;
        }
    };

    /**
     * OutElastic Easing.
     */
    public static EaseFunction outBounce = new EaseFunction() {
        @Override
        public float apply(float value) {
            if (value < (1f / 2.75f)) {
                return (7.5625f * value * value);
            } else if (value < (2f / 2.75f)) {
                return (7.5625f * (value -= (1.5f / 2.75f)) * value + 0.75f);
            } else if (value < (2.5 / 2.75)) {
                return (7.5625f * (value -= (2.25f / 2.75f)) * value + 0.9375f);
            } else {
                return (7.5625f * (value -= (2.625f / 2.75f)) * value + 0.984375f);
            }
        }
    };

}
