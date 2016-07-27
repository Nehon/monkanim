package com.jme3.anim;

import com.jme3.anim.blending.BlendingDataPool;
import com.jme3.animation.AnimationMask;
import com.jme3.util.SafeArrayList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Nehon on 13/07/2016.
 */
public class AnimState {

    private String name;
    private AnimationSequence sequence;
    private AnimationMask mask;
    private Transition incomingTransition;
    private AnimState fromState;
    private List<AnimState> subStates;
    private AnimationManager manager;
    private MaskWrapper maskWrapper = new MaskWrapper();

    private SafeArrayList<Transition> transitions = new SafeArrayList<>(Transition.class);
    private SafeArrayList<InterruptingTransition> interruptingTransitions = new SafeArrayList<>(InterruptingTransition.class);

    /**
     * Serialization only
     */
    public AnimState() {

    }


    AnimState(String name, AnimationManager manager) {
        this.name = name;
        this.manager = manager;
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

        AnimState nextState = null;


        if (sequence != null && !sequence.isFinished()) {
            nextState = updateInterruptingTransitions(additonnalIntTransisions);
        } else {
            nextState = updateTransitions(additionnalTransitions);
        }
        if(nextState == this) {
            //we are still on the same state, let's check for subStates.
            AnimState nextSubState = checkSubState(additonnalIntTransisions, additionnalTransitions);
            if(nextSubState != null) {
                if (nextState.getMask() == getMask()) {
                    //the sub state transitionned to the original mask meaning the parent state must transition.
                    nextState = nextSubState;

                } else {
                    //the sub state transitionned to another sub state
                    addSubState(nextSubState);
                }
            }
        }

        return nextState;
    }

    /**
     * this will follow the first transition found for sub states.
     * @param additonnalIntTransisions
     * @param additionnalTransitions
     * @return
     */
    private AnimState checkSubState(SafeArrayList<InterruptingTransition> additonnalIntTransisions,  SafeArrayList<Transition> additionnalTransitions) {

            //Still on this state, let's check the substates.
            if(subStates != null) {
                for (Iterator<AnimState> iter = subStates.iterator(); iter.hasNext();) {

                    AnimState subState = iter.next();
                    AnimState nextState = null;
                    nextState = subState.update(additonnalIntTransisions, additionnalTransitions);
                    if(nextState != subState){
                        // the sub state is done, let's remove it.
                        iter.remove();
                        //the sub state transitionned.
                        return nextState;
                    }
                }
            }

        return null;
    }

    private AnimState updateTransitions(SafeArrayList<Transition> additionnalTransitions) {
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

    private AnimState updateInterruptingTransitions(SafeArrayList<InterruptingTransition> additonnalIntTransisions) {
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
        //return the current state, this state is still active.
        return this;
    }

    public void resolve(BlendingDataPool weightedAnims, float tpf) {
        if (sequence == null) {
            return;
        }
        float weight = 1f;
        //compute blending from previous state
        if(incomingTransition != null && fromState != null){
            //updating the previous sequence
            fromState.getSequence().update(tpf);
            float time = fromState.getSequence().getTime();
            float start = Math.min(incomingTransition.getFromTime(), fromState.getSequence().getLength());
            float end = start + incomingTransition.getDuration();
            float duration = incomingTransition.getDuration();

            //computing the weight
            //note that this computes the weight of the new sequence that is currently fading in.
            weight = (time - start) / duration;
            //computing the weight of the previous sequence (1 - weight)
            // actually no... Seems that the correct value is 1.
            //float transitionWeight = FastMath.clamp(1f - weight, 0f, 1f);
            //resolving previous sequence with proper weight and time
            fromState.getSequence().resolve(weightedAnims, 1f, time, getResolvedMask());

            //the transition is done, we don't need it anymore.
            if(time > end){
                incomingTransition = null;
                fromState.getSequence().reset();
            }
        }

        //resolving and updating this state's sequence.
        sequence.update(tpf);
        sequence.resolve(weightedAnims, weight, sequence.getTime(), getResolvedMask());

        if(subStates != null) {
            for (AnimState subState : subStates) {
                subState.resolve(weightedAnims, tpf);
            }
        }
    }

    void addSubState(AnimState state){
        if(subStates == null){
            subStates = new ArrayList<>();
        }
        subStates.add(state);
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
                //check for masks, and add the new state as a substate if the mask is different.
                if (getMask() != newState.getMask() && newState.getMask() != fromState.getMask()) {
                    addSubState(newState);
                    //return null the state must stay active
                    return null;
                }
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

    void setManager(AnimationManager manager) {
        this.manager = manager;
    }

    public String getName() {
        return name;
    }

    public Transition transitionTo(String targetStateName){
        AnimState state = manager.findState(targetStateName);
        Transition transition = new Transition(state);
        addTransition(transition);
        return transition;
    }

    public Transition interruptTo(String targetStateName){
        AnimState state = manager.findState(targetStateName);
        InterruptingTransition transition = new InterruptingTransition(state);
        addTransition(transition);
        return transition;
    }

    /**
     * clears the incomingTransition and the fromState (previous state)
     */
    public void cleanup(){
        fromState = null;
        incomingTransition = null;
//        subStates.clear();
    }

    @Override
    public String toString() {
        return name;
    }

    AnimationMask getResolvedMask(){
        return maskWrapper;
    }

    private class MaskWrapper implements AnimationMask {


        @Override
        public boolean isAffected(int index) {
            boolean result = mask.isAffected(index);
            if(subStates != null){
                for (AnimState subState : subStates) {
                    result = result && !subState.getMask().isAffected(index);
                }
            }
            return result;
        }
    }
}
