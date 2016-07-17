package com.jme3.anim.statemachine;

import com.jme3.anim.AnimationSequence;
import com.jme3.animation.*;
import com.jme3.util.SafeArrayList;

/**
 * Created by Nehon on 13/07/2016.
 */
public class AnimState {

    private String name;
    private AnimationSequence sequence;
    private AnimationMask mask;
    private Transition incomingTransition;
    private AnimState fromState;

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
            AnimState nextState = checkTransitionsForNextState(interruptingTransitions.getArray());
            if (nextState != null) {
                return nextState;
            }
            //check for additional interrupting transition (that comes from the anyState)
            nextState = checkTransitionsForNextState(additonnalIntTransisions.getArray());
            if (nextState != null) {
                return nextState;
            }
            //return the current state, nothing changed.
            return this;
        } else {
            //the sequence has finished, let's check the transitions
            AnimState nextState = checkTransitionsForNextState(transitions.getArray());
            if (nextState != null) {
                // maybe we should check also for interrupting transitions...
                return nextState;
            }
            //check for additional transition (that comes from the anyState)
            nextState = checkTransitionsForNextState(additionnalTransitions.getArray());
            if (nextState != null) {
                return nextState;
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
        float weight = 1f;
        //compute blending from previous state
        if(incomingTransition != null && fromState != null){
            //updating the previous sequence
            fromState.getSequence().update(tpf);
            float time = fromState.getSequence().getTime();
            float start = incomingTransition.getFromTime();
            float end = start + incomingTransition.getDuration();
            float duration = incomingTransition.getDuration();

            //computing the weight
            //note that this computes the weight of the new sequence that is currently fading in.
            weight = (time - start) / duration;
            //computing the weight of the previous sequence (1 - weight)
            float transitionWeight = Math.min(1f - weight, 1f);
            //resolving previous sequence with proper weight and time
            fromState.getSequence().resolve(weightedAnims, transitionWeight, time);

            //the transition is done, we don't need it anymore.
            if(time > end){
                incomingTransition = null;
                fromState.getSequence().reset();
            }
        }

        //resolving and updating this state's sequence.
        sequence.update(tpf);
        sequence.resolve(weightedAnims, weight, sequence.getTime());
    }

    /**
     * Checks all given transitions, and will return the target state of the first one for which the condition is met.
     * @param transitionsArray
     * @return
     */
    private AnimState checkTransitionsForNextState(Transition... transitionsArray) {
        for (Transition transition : transitionsArray) {
            //here we check that we are not trying to evaluate a transition that would go to the current state
            //This case is possible because of the ANY_STATE
            //Then evaluating the transition condition
            if (transition.evaluateTrigger()) {
                //the condition is met, let prepare and return the new state
                AnimState newState = transition.getTargetState();
                //for blending, we save the sequand current time in the transition
                transition.setFromTime(sequence.getTime());
                //saving the transition into the new state
                newState.setIncomingTransition(transition);
                //Saving the previous state (useful for blending and to return to a previous state.
                newState.setFromState(this);
                //clean up this state
                cleanup();
                //return the new state
                return newState;
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

    public Transition getIncomingTransition() {
        return incomingTransition;
    }

    public void setIncomingTransition(Transition incomingTransition) {
        this.incomingTransition = incomingTransition;
    }

    public void setFromState(AnimState fromState) {
        this.fromState = fromState;
    }

    public AnimState getFromState() {
        return fromState;
    }

    /**
     * clears the incomingTransition and the fromState (previous state)
     */
    private void cleanup(){
        fromState = null;
        incomingTransition = null;
    }
}
