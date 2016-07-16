package com.jme3.anim.statemachine;

/**
 * Created by Nehon on 14/07/2016.
 */
public class InterruptingTransition extends Transition {

    public InterruptingTransition(AnimState targetState) {
        super(targetState);
    }

    public InterruptingTransition(AnimState targetState, TransitionCondition condition) {
        super(targetState, condition);
    }

    public InterruptingTransition(AnimState targetState, float duration, TransitionCondition condition) {
        super(targetState, duration, condition);
    }
}
