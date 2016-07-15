package com.jme3.anim.statemachine;

/**
 * Created by Nehon on 13/07/2016.
 */
public class Transition {

    private AnimState targetState;
    private TransitionCondition condition;

    public Transition(AnimState targetState) {
        this.targetState = targetState;
    }

    public Transition(AnimState targetState, TransitionCondition condition) {
        this.condition = condition;
        this.targetState = targetState;
    }

    public boolean evaluateCondition(){
        //if no condition then we consider it true;
        return condition == null || condition.isMet();
    }

    public AnimState getTargetState() {
        return targetState;
    }
}
