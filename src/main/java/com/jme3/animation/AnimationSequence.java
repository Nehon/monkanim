package com.jme3.animation;

import com.jme3.math.FastMath;
import com.jme3.util.SafeArrayList;

import java.util.*;

/**
 * Created by Nehon on 03/07/2016.
 * An Animation Sequence is an animation blend tree.
 * It can go from one to n animations blended together.
 * on each frame the tree is flatten and weights are computed accordingly
 */
public class AnimationSequence {

    private String name;
    private float value = 0;
    private LinkedHashMap<String, Float> flatMap;
    private float time;
    private float speed = 1;


    private List<String> animations = new SafeArrayList<>(String.class);

    public AnimationSequence(String name) {
        this.name = name;
    }

    public AnimationSequence(String name, String... animationNames) {
        this.name = name;
        addAnimations(animationNames);
    }

    public void addAnimation(String animationName){
        animations.add(animationName);
    }

    public void addAnimations(String... animationNames){
        for (String animationName : animationNames) {
            addAnimation(animationName);
        }
    }

    public LinkedHashMap<String, Float> flatten(float tpf){
        time += tpf * speed;
        if(flatMap == null){
            flatMap = new LinkedHashMap<>();
        }
        flatMap.clear();
        if(animations.size() == 1 || value == 0){
            flatMap.put(animations.get(0), 1f);
            return flatMap;
        }
        if(value == 1){
            flatMap.put(animations.get(animations.size() - 1), 1f);
            return flatMap;
        }

        float scaledWeight = value * (animations.size() - 1);
        int highIndex = (int)FastMath.ceil(scaledWeight);
        int lowIndex = highIndex - 1;

        flatMap.put(animations.get(lowIndex), 1 - (scaledWeight - lowIndex));
        flatMap.put(animations.get(highIndex), scaledWeight - lowIndex);

        return flatMap;
    }

    public void setValue(float value) {
        this.value = FastMath.clamp(value, 0, 1);
    }

    public float getValue() {
        return value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void reset(){
        time = 0;
    }

    public float getTime() {
        return time;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }
}
