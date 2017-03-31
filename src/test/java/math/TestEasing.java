package math;

import com.jme3.math.Easing;
import org.junit.Test;

public class TestEasing {

    @Test
    public void testEasing() {

        System.err.println("Linear");
        for (int i = 0; i < 11; i++) {
            System.err.print(((float) i / 10f) + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.linear.apply((float) i / 10f)) + ", ");
        }
        System.err.println("\ninQuad");
        for (int i = 0; i < 11; i++) {
            System.err.print(((float) i / 10f) + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.inQuad.apply((float) i / 10f)) + ", ");
        }
        System.err.println("\noutQuad");
        for (int i = 0; i < 11; i++) {
            System.err.print(((float) i / 10f) + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.outQuad.apply((float) i / 10f)) + ", ");
        }
        System.err.println("\ninOutQuad");
        for (int i = 0; i < 11; i++) {
            System.err.print(((float) i / 10f) + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.inOutQuad.apply((float) i / 10f)) + ", ");
        }
        System.err.println("\ninCubic");
        for (int i = 0; i < 11; i++) {
            System.err.print(((float) i / 10f) + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.inCubic.apply((float) i / 10f)) + ", ");
        }
        System.err.println("\ninQuart");
        for (int i = 0; i < 11; i++) {
            System.err.print(((float) i / 10f) + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.inQuart.apply((float) i / 10f)) + ", ");
        }


        System.err.println("\noutCubic");
        for (int i = 0; i < 11; i++) {
            System.err.print(i + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.outCubic.apply((float) i / 10f)) + ", ");
        }
        System.err.println("\noutQuart");
        for (int i = 0; i < 11; i++) {
            System.err.print(i + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.outQuart.apply((float) i / 10f)) + ", ");
        }

        System.err.println("\noutElastic");
        for (int i = 0; i < 11; i++) {
            System.err.print(i + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.outElastic.apply((float) i / 10f)) + ", ");
        }
        System.err.println("\noutBounce");
        for (int i = 0; i < 11; i++) {
            System.err.print(i + ", ");
        }
        System.err.println("");
        for (int i = 0; i < 11; i++) {
            System.err.print((Easing.outBounce.apply((float) i / 10f)) + ", ");
        }
    }
}