package com.jme3.anim;

import com.jme3.anim.blending.*;
import com.jme3.anim.interpolator.AnimInterpolator;
import com.jme3.animation.Anim;
import com.jme3.animation.AnimationMask;
import com.jme3.math.EaseFunction;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.util.SafeArrayList;
import com.jme3.util.clone.Cloner;
import com.jme3.util.clone.JmeCloneable;

import java.util.List;

/**
 * Created by Nehon on 13/07/2016.
 */
public class AnimState implements Anim, Cloneable, JmeCloneable {

    private String name;
    private AnimationLayer layer;
    private Transition incomingTransition;
    private AnimState fromState;
    private AnimationManager manager;
    private SafeArrayList<Transition> transitions = new SafeArrayList<>(Transition.class);
    private SafeArrayList<InterruptingTransition> interruptingTransitions = new SafeArrayList<>(InterruptingTransition.class);

    private float time;
    private float speed = 1;
    private float length = 0;
    private float lengthOverride = -1;
    private BlendSpace blendSpace = new LinearBlendSpace();

    private List<Anim> animations = new SafeArrayList<>(Anim.class);

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

    /**
     * Will check all transitions and find the new state if relevant.
     *
     * @param additonnalIntTransisions
     * @param additionnalTransitions
     * @return
     */
    public AnimState signal(SafeArrayList<InterruptingTransition> additonnalIntTransisions, SafeArrayList<Transition> additionnalTransitions) {

        AnimState nextState = null;

        if (!isFinished()) {
            nextState = checkInterruptingTransitions(additonnalIntTransisions);
        } else {
            nextState = checkTransitions(additionnalTransitions);
        }
        //TODO check for layers and perform layer blending if relevant
        return nextState;
    }

    /**
     * Checks classic transitions (when the state comes to an end)
     * @param additionnalTransitions
     * @return
     */
    private AnimState checkTransitions(SafeArrayList<Transition> additionnalTransitions) {
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
        //no available transition, let's loop the state.
        reset();
        return this;
    }

    /**
     * Checks interrupting transition (some event occurred that required this state to transition)
     *
     * @param additonnalIntTransisions
     * @return
     */
    private AnimState checkInterruptingTransitions(SafeArrayList<InterruptingTransition> additonnalIntTransisions) {
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

    /**
     * Updating the state
     *
     * @param weightedAnims
     * @param tpf
     */
    public void update(List<AnimationData> weightedAnims, float tpf) {
        float weight = 1f;
        //compute blending from previous state
        if (incomingTransition != null && fromState != null) {
            //updating the previous state
            //we want to do it only if the sate was on the same layer (regular case)
            //And also when fading out a state from a higher layer.
            if (fromState.getLayer() == layer || fromState.getLayer().getIndex() > layer.getIndex()) {
                fromState.updateTime(tpf);
            }
            float time = fromState.getTime();
            float start = Math.min(incomingTransition.getFromTime(), fromState.getLength());
            //depending on the time we set the transition, start time can be > to time, which leads to a negative weight
            //here we ensure start is always below time
            while (start > time) {
                start -= fromState.getLength();
            }

            float end = start + incomingTransition.getDuration();
            float duration = incomingTransition.getDuration();

            //computing the weight
            //note that this computes the weight of the new sequence that is currently fading in.
            weight = (time - start) / duration;

//System.err.println(fromState.getName() + " to " + getName() + ": " + weight);

            weight = FastMath.clamp(weight, 0f, 1f);
            //computing the weight of the previous sequence (1 - weight)
            float transitionWeight = 1f - weight;

            //Check if we are on the same layer
            if (fromState.getLayer() != layer) {
                //we are fading between layers. Layers will handle the transition and anims weight will be reset to 1
                if (fromState.getLayer().getIndex() < layer.getIndex()) {
                    //fading IN
                    //Note that the weight of the previous layer doesn't change
                    //the fading in layer
                    layer.setWeight(weight);
                } else {
                    //fading OUT
                    fromState.getLayer().setWeight(transitionWeight);
                    // resolve the state with 1.0 weight, once again blending is done in the layer.
                    fromState.resolve(weightedAnims, 1.0f, time, fromState.getLayer());
                }
                //reset the weight, blending is handled by layers here
                weight = 1;
            } else {
                //resolving previous sequence with proper weight and time
                fromState.resolve(weightedAnims, transitionWeight, time, fromState.getLayer());
            }


            //the transition is done, we don't need it anymore.
            if (time > end) {
                incomingTransition = null;
                //if we are on the same layer the previous state is done.
                //if the layer are different the previous state will continue to be updated.
                if (layer == fromState.getLayer() || layer.getIndex() < fromState.getLayer().getIndex()) {
                    fromState.reset();
//System.err.println(" ---------------------------- ");
//System.err.println(fromState.getName() + " reset");
//System.err.println(" ---------------------------- ");
                    if (layer.getIndex() < fromState.getLayer().getIndex()) {
                        fromState.getLayer().setActiveState(null);
                    }
                }
            }
        }

        //resolving and updating this state's anims.
        updateTime(tpf);
        resolve(weightedAnims, weight, time, getLayer());

    }

    private void updateTime(float tpf) {
        time += tpf * speed;
    }

    public AnimState forAnims(String... animNames) {
        for (String animName : animNames) {
            Anim clip = manager.getClip(animName);
            addAnimation(clip);
        }
        return this;
    }

    /**
     * Checks all given transitions, and will return the target state of the first one for which the condition is met.
     *
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
                //for blending, we save the sequence current time in the transition
                transition.setFromTime(time);
                //saving the transition into the new state
                newState.setIncomingTransition(transition);
                //Saving the previous state (useful for blending and to return to a previous state.
                newState.setFromState(this);
                //return the new state
                return newState;
            }
        }
        return null;
    }

    public AnimationMask getMask() {
        return layer;
    }

    public AnimationLayer getLayer() {
        return layer;
    }

    public void setLayer(AnimationLayer layer) {
        this.layer = layer;
    }

    public AnimState onLayer(String name) {
        AnimationLayer layer = manager.getLayer(name);
        if (layer == null) {
            throw new IllegalArgumentException("Cannot find layer with name " + name);
        }
        setLayer(layer);
        return this;
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

    public Transition transitionTo(String targetStateName) {
        AnimState state = manager.findState(targetStateName);
        Transition transition = new Transition(state);
        addTransition(transition);
        return transition;
    }

    public Transition interruptTo(String targetStateName) {
        AnimState state = manager.findState(targetStateName);
        InterruptingTransition transition = new InterruptingTransition(state);
        addTransition(transition);
        return transition;
    }

    /**
     * clears the incomingTransition and the fromState (previous state)
     */
    public void cleanup() {
        fromState = null;
        incomingTransition = null;
    }

    @Override
    public String toString() {
        return name;
    }

    public void addAnimation(Anim animation) {
        animations.add(animation);
        if (animation.getLength() > length) {
            length = animation.getLength();
        }
    }

    public void addAnimations(Anim... animations) {
        for (Anim animation : animations) {
            addAnimation(animation);
        }
    }

    public void removeAnimation(Anim animation) {
        animations.remove(animation);
        //recompute length
        length = 0;
        for (Anim anim : animations) {
            if (anim.getLength() > length) {
                length = anim.getLength();
            }
        }
    }

    @Override
    public float getLength() {
        if (lengthOverride != -1) {
            return lengthOverride;
        }
        return length;
    }

    public void setLength(float length) {
        this.lengthOverride = length;
        for (Anim animation : animations) {
            if (animation instanceof AnimationData) {
                AnimationData ad = (AnimationData) animation;
                float scale = length / ad.getAnimation().getLength();
                ad.setScale(scale);
                System.err.println(scale);
            } else if (animation instanceof AnimState) {
                ((AnimState) animation).setLength(length);
            }
        }
    }
    @Override
    public void resolve(List<AnimationData> weightedAnims, float globalWeight, float time, AnimationMask mask) {
        if (animations.isEmpty()) {
            return;
        }
        blendSpace.blend(animations, weightedAnims, globalWeight, time, mask);
    }

    public BlendSpace getBlendSpace() {
        return blendSpace;
    }

    public boolean isFinished() {
        return time >= getLength();
    }

    public void setBlendSpace(BlendSpace blendSpace) {
        this.blendSpace = blendSpace;
    }

    public AnimState withBlendSpace(BlendSpace blendSpace) {
        setBlendSpace(blendSpace);
        return this;
    }

    private AnimationData getAnimData(int index){
        Anim anim = animations.get(index);
        if( !(anim instanceof AnimationData )){
            throw new IllegalArgumentException("No animation data at this index");
        }

        return (AnimationData) anim;
    }


    public AnimState setTranslationInterpolator(int index, AnimInterpolator<Vector3f> interpolator){
        getAnimData(index).getTrackInterpolator().setTranslationInterpolator(interpolator);
        return this;
    }

    public AnimState setRotationInterpolator(int index, AnimInterpolator<Quaternion> interpolator){
        getAnimData(index).getTrackInterpolator().setRotationInterpolator(interpolator);
        return this;
    }

    public AnimState setScaleInterpolator(int index, AnimInterpolator<Vector3f> interpolator){
        getAnimData(index).getTrackInterpolator().setScaleInterpolator(interpolator);
        return this;
    }

    public AnimState setTimeInterpolator(int index, AnimInterpolator<Float> timeInterpolator){
        getAnimData(index).getTrackInterpolator().setTimeInterpolator(timeInterpolator);
        return this;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void reset() {
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
        AnimState state = (AnimState) original;
        this.layer = cloner.clone(state.getLayer());
        this.blendSpace = state.blendSpace;

        this.animations = new SafeArrayList<>(Anim.class);
        for (Anim animation : state.animations) {
            this.animations.add(cloner.clone(animation));
        }
        this.transitions = new SafeArrayList<>(Transition.class);
        for (Transition transition : state.transitions) {
            this.transitions.add(cloner.clone(transition));
        }
        this.interruptingTransitions = new SafeArrayList<>(InterruptingTransition.class);
        for (InterruptingTransition transition : state.interruptingTransitions) {
            this.interruptingTransitions.add(cloner.clone(transition));
        }
    }
}
