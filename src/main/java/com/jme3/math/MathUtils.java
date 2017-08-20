package com.jme3.math;

/**
 * Created by Nehon on 23/04/2017.
 */
public class MathUtils {

    public static Quaternion log(Quaternion q, Quaternion store) {
        float a = FastMath.acos(q.w);
        float sina = FastMath.sin(a);

        store.w = 0;
        if (sina > 0) {
            store.x = a * q.x / sina;
            store.y = a * q.y / sina;
            store.z = a * q.z / sina;
        } else {
            store.x = 0;
            store.y = 0;
            store.z = 0;
        }
        return store;
    }

    /**
     * Calculate the exponential of a pure quaternion.
     *
     * @param q input value (not null, unaffected, w=0)
     * @param store (modified if not null)
     * @return a unit quaternion (either storeResult or a new instance)
     */
    public static Quaternion exp(Quaternion q, Quaternion store) {
        assert q.getW() == 0f : q;
        if (store == null) {
            store = new Quaternion();
        }

        double qx = q.getX();
        double qy = q.getY();
        double qz = q.getZ();
        double theta = hypotenuse(qx, qy, qz);
        if (theta == 0.0) {
            store.loadIdentity();
        } else {
            float w = (float) Math.cos(theta);
            double scale = Math.sin(theta) / theta;
            float x = (float) (scale * qx);
            float y = (float) (scale * qy);
            float z = (float) (scale * qz);
            store.set(x, y, z, w);
        }

        return store;
    }

    /**
     * Interpolate between 2 unit quaternions using spherical linear (Slerp)
     * interpolation. This method is slower (but more accurate) than
     * {@link com.jme3.math.Quaternion#slerp(com.jme3.math.Quaternion, float)},
     * always produces a unit, and doesn't trash q1. The caller is responsible
     * for flipping the sign of q0 or q1 when it's appropriate to do so.
     *
     * @param q1 function value at t=0 (not null, unaffected, norm=1)
     * @param q2 function value at t=1 (not null, unaffected, norm=1)
     * @param t descaled parameter value (&ge;0, &le;1)
     * @param store (modified if not null)
     * @return an interpolated unit quaternion (either storeResult or a new
     * instance)
     */
    public static Quaternion slerpNoInvert(Quaternion q1, Quaternion q2, float t, Quaternion store) {
        if (store == null) {
            store = new Quaternion();
        }

        Quaternion q0inverse = conjugate(q1, null);
        Quaternion ratio = q0inverse.multLocal(q2);
        Quaternion power = pow(ratio, t, ratio);
        store.set(q1);
        store.multLocal(power);

        return store;
    }

    public static Quaternion slerp(Quaternion q1, Quaternion q2, float t, Quaternion store) {

        float dot = (q1.x * q2.x) + (q1.y * q2.y) + (q1.z * q2.z)
                + (q1.w * q2.w);

        if (dot < 0.0f) {
            // Negate the second quaternion and the result of the dot product
            q2.x = -q2.x;
            q2.y = -q2.y;
            q2.z = -q2.z;
            q2.w = -q2.w;
            dot = -dot;
        }

        // Set the first and second scale for the interpolation
        float scale0 = 1 - t;
        float scale1 = t;

        // Check if the angle between the 2 quaternions was big enough to
        // warrant such calculations
        if (dot < 0.9f) {// Get the angle between the 2 quaternions,
            // and then store the sin() of that angle
            float theta = FastMath.acos(dot);
            float invSinTheta = 1f / FastMath.sin(theta);

            // Calculate the scale for q1 and q2, according to the angle and
            // it's sine value
            scale0 = FastMath.sin((1 - t) * theta) * invSinTheta;
            scale1 = FastMath.sin((t * theta)) * invSinTheta;

            // Calculate the x, y, z and w values for the quaternion by using a
            // special
            // form of linear interpolation for quaternions.
            store.x = (scale0 * q1.x) + (scale1 * q2.x);
            store.y = (scale0 * q1.y) + (scale1 * q2.y);
            store.z = (scale0 * q1.z) + (scale1 * q2.z);
            store.w = (scale0 * q1.w) + (scale1 * q2.w);
        } else {
            store.x = (scale0 * q1.x) + (scale1 * q2.x);
            store.y = (scale0 * q1.y) + (scale1 * q2.y);
            store.z = (scale0 * q1.z) + (scale1 * q2.z);
            store.w = (scale0 * q1.w) + (scale1 * q2.w);
            store.normalizeLocal();
        }
        // Return the interpolated quaternion
        return store;
    }

    /**
     * Calculate Squad parameter "a" for a continuous 1st derivative at the
     * middle point of 3 specified control points.
     *
     * @param qnm1 previous control point (not null, unaffected, norm=1)
     * @param qn current control point (not null, unaffected, norm=1)
     * @param qnp1 following control point (not null, unaffected, norm=1)
     * @param store (modified if not null)
     * @return a unit quaternion for use as a Squad parameter (either
     * storeResult or a new instance)
     */
    public static Quaternion spline(Quaternion qnm1, Quaternion qn, Quaternion qnp1, Quaternion store) {
        if (store == null) {
            store = new Quaternion();
        }

        Quaternion q1c = conjugate(qn, null);
        Quaternion turn0 = q1c.mult(qnm1);
        Quaternion logTurn0 = log(turn0, turn0);
        Quaternion turn2 = q1c.mult(qnp1);
        Quaternion logTurn2 = log(turn2, turn2);
        Quaternion sum = logTurn2.addLocal(logTurn0);
        sum.multLocal(-0.25f);
        Quaternion exp = exp(sum, sum);
        store.set(qn);
        store.multLocal(exp);

        return store;
    }

    //! spherical cubic interpolation
    public static Quaternion squad(Quaternion q0, Quaternion q1, Quaternion a,
            Quaternion b, float t, Quaternion store) {
        slerpNoInvert(a, b, t, store);
        slerpNoInvert(q0, q1, t, a);
        return slerp(a, store, 2 * t * (1 - t), b);
    }

    /**
     * Clamp a double-precision value between 2 limits.
     *
     * @param dValue input value to be clamped
     * @param min lower limit of the clamp
     * @param max upper limit of the clamp
     * @return the value between min and max inclusive that is closest to fValue
     * @see com.jme3.math.FastMath#clamp(float,float,float)
     */
    private static double clamp(double dValue, double min, double max) {
        double result;
        if (dValue < min) {
            result = min;
        } else if (dValue > max) {
            result = max;
        } else {
            result = dValue;
        }

        return result;
    }

    /**
     * Calculate the conjugate of a quaternion. For unit quaternions, the
     * conjugate is a faster way to calculate the inverse.
     *
     * @param q input value (not null, unaffected)
     * @param storeResult (modified if not null)
     * @return a conjugate quaternion (either storeResult or a new instance)
     */
    private static Quaternion conjugate(Quaternion q, Quaternion storeResult) {
        if (storeResult == null) {
            storeResult = new Quaternion();
        }

        float qx = q.getX();
        float qy = q.getY();
        float qz = q.getZ();
        float qw = q.getW();
        storeResult.set(-qx, -qy, -qz, qw);

        return storeResult;
    }

    /**
     * Compute sqrt(x^2 + y^2 + z^2).
     *
     * @param x 1st input value
     * @param y 2nd input value
     * @param z 3nd input value
     * @return the positive square root of the sum of squares (&ge;0)
     * @see java.lang.Math#hypot(double, double)
     */
    private static double hypotenuse(double x, double y, double z) {
        double sum = x * x + y * y + z * z;
        double result = Math.sqrt(sum);

        assert result >= 0f : result;
        return result;
    }

    /**
     * Raise a unit quaternion to the specified real power.
     *
     * @param base input value (not null, unaffected, norm=1)
     * @param exponent the exponent
     * @param storeResult (modified if not null)
     * @return a unit quaternion (either storeResult or a new instance)
     */
    private static Quaternion pow(Quaternion base, float exponent,
            Quaternion storeResult) {
        if (storeResult == null) {
            storeResult = new Quaternion();
        }

        float baseW = base.getW();
        if (baseW >= 1f || baseW <= -1f || exponent == 0f) {
            storeResult.loadIdentity();
        } else {
            double baseX = base.getX();
            double baseY = base.getY();
            double baseZ = base.getZ();
            double sineTheta = hypotenuse(baseX, baseY, baseZ);
            sineTheta = clamp(sineTheta, 0.0, 1.0);
            if (sineTheta == 0.0) {
                storeResult.loadIdentity();
            } else {
                double theta = Math.asin(sineTheta);
                float w = (float) Math.cos(exponent * theta);
                double scale = Math.sin(exponent * theta) / sineTheta;
                float x = (float) (scale * baseX);
                float y = (float) (scale * baseY);
                float z = (float) (scale * baseZ);
                storeResult.set(x, y, z, w);
            }
        }

        return storeResult;
    }
}
