package com.jme3.anim.statemachine;

import com.jme3.anim.AnimationSequence;
import com.jme3.animation.*;
import com.jme3.util.SafeArrayList;

import java.util.Map;

/**
 * Created by Nehon on 13/07/2016.
 */
public class AnimState {

    private String name;
    private AnimationSequence sequence;
    private AnimationMask mask;

    private SafeArrayList<Transition> transitions = new SafeArrayList<>(Transition.class);
    private SafeArrayList<InterruptingTransition> interruptingTransitions = new SafeArrayList<>(InterruptingTransition.class);

    public AnimState(String name, AnimationSequence sequence, AnimationMask mask) {
        this.name = name;
        this.sequence = sequence;
        this.mask = mask;
    }

    public AnimState(String name) {
        this.name = name;
    }

    public void addTransition(Transition transition) {
        transitions.add(transition);
    }

    public void addTransition(InterruptingTransition transition) {
        interruptingTransitions.add(transition);
    }

    public void removeTransition(Transition transition) {
        transitions.remove(transition);
    }

    public void removeTransition(InterruptingTransition transition) {
        interruptingTransitions.remove(transition);
    }

    public AnimState update(SafeArrayList<InterruptingTransition> additonnalIntTransisions, SafeArrayList<Transition> additionnalTransitions) {

        if (sequence != null && !sequence.isFinished()) {
            //sequence is playing, but can be interrupted by an interrupting transition.
            AnimState transition = checkTransitionsForNextState(interruptingTransitions.getArray());
            if (transition != null) {
                return transition;
            }
            //check for additional interrupting transition (that comes from the anyState)
            transition = checkTransitionsForNextState(additonnalIntTransisions.getArray());
            if (transition != null) {
                return transition;
            }
            //return the current state, nothing changed.
            return this;
        } else {
            //the sequence has finished, let's check the transitions
            AnimState transition = checkTransitionsForNextState(transitions.getArray());
            if (transition != null) {
                // maybe we should check also for interrupting transitions...
                return transition;
            }
            //check for additional transition (that comes from the anyState)
            transition = checkTransitionsForNextState(additionnalTransitions.getArray());
            if (transition != null) {
                return transition;
            }
            //no available transition, let's loop the sequence.
            //and keep the same state.
            if (sequence != null) {
                sequence.reset();
            }
            return this;
        }
    }

    public void resolve(SafeArrayList<Animation> weightedAnims, float tpf) {
        if (sequence == null) {
            return;
        }
        //here if we are transitioning from another state the animations will be blended
        sequence.update(tpf);
        sequence.resolve(weightedAnims, 1.0f, sequence.getTime());
    }

    private AnimState checkTransitionsForNextState(Transition... transitionsArray) {
        for (Transition transition : transitionsArray) {

            if (transition.evaluateCondition()) {
                //Will need more logic here for transition blending.
                //return the new state
                return transition.getTargetState();
            }
        }
        return null;
    }

    public AnimationMask getMask() {
        return mask;
    }

    public void setMask(AnimationMask mask) {
        this.mask = mask;
    }

    public void setSequence(AnimationSequence sequence) {
        this.sequence = sequence;
    }

    public AnimationSequence getSequence() {
        return sequence;
    }

    public SafeArrayList<InterruptingTransition> getInterruptingTransitions() {
        return interruptingTransitions;
    }

    public SafeArrayList<Transition> getTransitions() {
        return transitions;
    }
}
