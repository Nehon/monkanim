package com.jme3.anim;

import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;

/**
 * Created by Nehon on 13/07/2016.
 */
public class Transition implements Cloneable, JmeCloneable {

    public final static float DEFAULT_BLEND_DURATION = 0.4f;

    private AnimState targetState;
    protected TransitionTrigger trigger;
    private float duration = DEFAULT_BLEND_DURATION;
    private float fromTime;

    public Transition() {
    }

    public Transition(AnimState targetState) {
        this.targetState = targetState;
    }

    public Transition(AnimState targetState, TransitionTrigger trigger) {
        this.trigger = trigger;
        this.targetState = targetState;
    }

    public Transition(AnimState targetState, float duration, TransitionTrigger trigger) {
        this(targetState, trigger);
        this.duration = duration;
    }

    public Transition(AnimState targetState, float duration) {
        this(targetState);
        this.duration = duration;
    }


    public boolean evaluateTrigger(){
        //if no trigger then we consider it true;
        return trigger== null || trigger.evaluate();
    }

    public AnimState getTargetState() {
        return targetState;
    }

    public float getFromTime() {
        return fromTime;
    }

    public void setFromTime(float fromTime) {
        this.fromTime = fromTime;
    }

    public float getDuration() {
        return duration;
    }

    public void setDuration(float duration) {
        this.duration = duration;
    }

    public void setTargetState(AnimState targetState) {
        this.targetState = targetState;
    }

    public TransitionTrigger getTrigger() {
        return trigger;
    }

    public void setTrigger(TransitionTrigger trigger) {
        this.trigger = trigger;
    }

    public Transition when(TransitionTrigger trigger){
        setTrigger(trigger);
        return this;
    }

    public Transition in(float duration){
        setDuration(duration);
        return this;
    }

    @Override
    public Object jmeClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Can't clone AnimationLayer");
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        Transition transition = (Transition) original;
        this.targetState = cloner.clone(transition.targetState);
        this.trigger = transition.trigger;
    }
}
