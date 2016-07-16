package com.jme3.anim.statemachine;

/**
 * Created by Nehon on 13/07/2016.
 */
public class Transition {

    private AnimState targetState;
    private TransitionCondition condition;
    private float duration = 0.3f;
    private float fromTime;

    public Transition(AnimState targetState) {
        this.targetState = targetState;
    }

    public Transition(AnimState targetState, TransitionCondition condition) {
        this.condition = condition;
        this.targetState = targetState;
    }

    public Transition(AnimState targetState, float duration, TransitionCondition condition) {
        this(targetState, condition);
        this.duration = duration;
    }

    public boolean evaluateCondition(){
        //if no condition then we consider it true;
        return condition == null || condition.isMet();
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
