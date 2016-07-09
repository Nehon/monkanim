package com.jme3.anim;

import com.jme3.math.FastMath;
import com.jme3.util.SafeArrayList;

import java.util.*;

/**
 * Created by Nehon on 03/07/2016.
 * An Animation Sequence is an animation blend tree.
 * It can go from one to n animations blended together.
 * on each frame the tree is flatten and weights are computed accordingly
 */
public class AnimationSequence implements Anim{

    private String name;
    private float value = 0;
    private float time;
    private float speed = 1;
    private float length = 0;

    private List<Anim> animations = new SafeArrayList<>(Anim.class);


    /**
     * Serialisation only
     */
    public AnimationSequence(){
    }

    AnimationSequence(String name) {
        this.name = name;
    }


    AnimationSequence(String name, Anim... animations) {
        this.name = name;
        addAnimations(animations);
    }

    public void addAnimation(Anim animation){
        animations.add(animation);
        if (animation.getLength() > length){
            length = animation.getLength();
        }
    }

    public void addAnimations(Anim... animations){
        for (Anim animation : animations) {
            addAnimation(animation);
        }
    }

    public void removeAnimation(Anim animation){
        animations.remove(animation);
        //recompute length
        length = 0;
        for (Anim anim : animations) {
            if (anim.getLength() > length){
                length = anim.getLength();
            }
        }
    }

    @Override
    public float getLength() {
        return length;
    }

    void update(float tpf){
        time += tpf * speed;
    }

    @Override
    public void resolve(Map<AnimationClip, Float> weightedAnimMap, float globalWeight){
        if(animations.isEmpty()){
            return;
        }
        if(animations.size() == 1 || value == 0){
            animations.get(0).resolve(weightedAnimMap, globalWeight);
            return;
        }
        if(value == 1){
            animations.get(animations.size() - 1).resolve(weightedAnimMap, globalWeight);
            return;
        }

        float scaledWeight = value * (animations.size() - 1);
        int highIndex = (int)FastMath.ceil(scaledWeight);
        int lowIndex = highIndex - 1;

//        System.err.println(name + "x" + globalWeight + ":" + animations.get(lowIndex) +":" + ((1 - (scaledWeight - lowIndex)) * globalWeight) + ", "+ animations.get(highIndex) +":" + ((scaledWeight - lowIndex) * globalWeight));
        animations.get(lowIndex).resolve(weightedAnimMap, (1 - (scaledWeight - lowIndex)) * globalWeight);
        animations.get(highIndex).resolve(weightedAnimMap, (scaledWeight - lowIndex) * globalWeight);
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
