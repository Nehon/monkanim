package com.jme3.anim;

/**
 * Created by Nehon on 14/07/2016.
 */
public class InterruptingTransition extends Transition {

    private boolean lastEvaluation;

    public InterruptingTransition() {
    }

    public InterruptingTransition(AnimState targetState) {
        super(targetState);
    }

    public InterruptingTransition(AnimState targetState, TransitionTrigger condition) {
        super(targetState, condition);
    }

    public InterruptingTransition(AnimState targetState, float duration, TransitionTrigger condition) {
        super(targetState, duration, condition);
    }

    public InterruptingTransition(AnimState targetState, float duration) {
        super(targetState, duration);
    }

    public boolean evaluateTrigger(){
        //if no trigger then we consider it true;
        if (trigger == null){
            return true;
        }
        boolean evaluation = trigger.evaluate();
        //we want to trigger the transition on the state chage from false to true
        //so we not only change that the evaluation is true, but also that it was false on the previous check.
        boolean result = evaluation == true && evaluation != lastEvaluation;
        lastEvaluation = evaluation;
        return result;
    }
}
