package com.jme3.anim.statemachine;

/**
 * Created by Nehon on 13/07/2016.
 */
public class Transition {

    private AnimState targetState;
    protected TransitionTrigger trigger;
    private float duration = 0.3f;
    private float fromTime;


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
}
