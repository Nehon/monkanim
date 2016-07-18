/*
 * Copyright (c) 2009-2012 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.anim;

import com.jme3.anim.statemachine.AnimState;
import com.jme3.animation.*;
import com.jme3.export.*;
import com.jme3.renderer.*;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.AbstractControl;
import com.jme3.util.*;
import com.jme3.util.clone.*;

import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

/**
 * <code>AnimControl</code> is a Spatial control that allows manipulation
 * of skeletal animation.
 *
 * The control currently supports:
 * 1) Animation blending/transitions
 * 2) Multiple animation channels
 * 3) Multiple skins
 * 4) Animation event listeners
 * 5) Animated model cloning
 * 6) Animated model binary import/export
 * 7) Hardware skinning
 * 8) Attachments
 * 9) Add/remove skins
 *
 * Planned:
 * 1) Morph/Pose animation
 *
 * @author Kirill Vainer
 */
public final class AnimationManager extends AbstractControl implements Cloneable, JmeCloneable {

    /**
     * Skeleton object must contain corresponding data for the targets' weight buffers.
     */
    private AnimationMetaData metaData = new AnimationMetaData();

    private float globalSpeed = 1.0f;

    /**
     * List of animations
     */
    private Map<String, Animation> animationMap = new HashMap<>();
    /**
     * Sequences
     */
    private Map<String, AnimationSequence> sequences = new HashMap<>();
    /**
     * The flat map of animation with corresponding weight updated on each frame
     */
    private SafeArrayList<Animation> weightedAnims = new SafeArrayList<>(Animation.class);

    /**
     * Could help with blend space to have basic types parameter added by the user and pick the appropriate blend space
     * Not used right now
     */
    private Map<String, Object> parameters = new HashMap<>();
    /**
     * Animation mask list, that holdw all the masks used by this animation manager.
     */
    private Map<String, AnimationMask> masks = new HashMap<>();
    public final static AnimationMask DEFAULT_MASK = new AnimationMask() {
        @Override
        public boolean isAffected(int index) {
            return true;
        }
    };
    public final AnimState ANY_STATE = new AnimState("Any");

    /**
     * the list of all states of this Animation manager.
     */
    private Map<String, AnimState> stateMachine = new HashMap<>();
    /**
     * Whenever a state is activated it's added to this list, and will be updated on each frame.
     */
    private SafeArrayList<AnimState> activeStates = new SafeArrayList<>(AnimState.class);


    /**
     * Animation event listeners - This I hope to get rid of....
     */
    private transient ArrayList<AnimEventListener> listeners = new ArrayList<AnimEventListener>();

    /**
     * Creates a new animation manager for the given skeleton.
     * The method {@link AnimationManager#setAnimationsClips(HashMap) }
     * must be called after initialization in order for this class to be useful.
     *
     * @param skeleton The skeleton to animate
     */
    public AnimationManager(Skeleton skeleton) {
        this.metaData.setSkeleton(skeleton);
        reset();
    }

    /**
     * Serialization only. Do not use.
     */
    public AnimationManager() {
    }

    @Override
    public Object jmeClone() {
        AnimationManager clone = (AnimationManager) super.jmeClone();
        clone.parameters = new HashMap<>();
        clone.sequences = new HashMap<>();
        clone.masks = new HashMap<>();
        clone.listeners = new ArrayList<>();

        return clone;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original ) {
        super.cloneFields(cloner, original);

        this.metaData.setSkeleton(cloner.clone(((AnimationManager)original).metaData.getSkeleton()));

        for (Entry<String, Object> paramEntry : parameters.entrySet()) {
            this.parameters.put(paramEntry.getKey(),paramEntry.getValue());
        }

        for (Entry<String, AnimationSequence> animEntry : sequences.entrySet()) {
            this.sequences.put(animEntry.getKey(),animEntry.getValue());
        }

        for (Entry<String, AnimationMask> paramEntry : masks.entrySet()) {
            this.masks.put(paramEntry.getKey(),paramEntry.getValue());
        }

        // Note cloneForSpatial() never actually cloned the animation map... just its reference
        HashMap<String, Animation> newMap = new HashMap<>();

        // animationMap is cloned, but only ClonableTracks will be cloned as they need a reference to a cloned spatial
        for( Entry<String, Animation> e : animationMap.entrySet() ) {
            newMap.put(e.getKey(), cloner.clone(e.getValue()));
        }

        this.animationMap = newMap;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        metaData.setSpatial(spatial);
    }

    public AnimationSequence createAnimationSequence(String name){
        return createAnimationSequence(name, (String) null);
    }

    public AnimationSequence createAnimationSequence(String name, String... animNames){
        AnimationSequence sequence = new AnimationSequence(name);
        if(animNames != null) {
            for (String animName : animNames) {
                Anim clip = animationMap.get(animName);

                if (clip == null) {
                    //we couldn't find a clip with this name, let's look for a sequence
                    clip = sequences.get(animName);
                }
                if (clip == null) {
                    //we couldn't find a sequence either, let's throw an exception
                    throw new IllegalArgumentException("Can't find an animation clip or sequence with name " + animName);
                }
                sequence.addAnimation(clip);
            }
        }

        sequences.put(sequence.getName(), sequence);
        return sequence;
    }

    public AnimState createStateForSequence(String sequenceName){
        AnimState state = new AnimState(sequenceName);
        state.setSequence(sequences.get(sequenceName));
        return state;
    }

    public SafeArrayList<Animation> getWeightedAnims() {
        return weightedAnims;
    }

    public Map<String, AnimationSequence> getSequences() {
        return sequences;
    }

    /**
     * @param animations Set the animations that this <code>AnimControl</code>
     * will be capable of playing. The animations should be compatible
     * with the skeleton given in the constructor.
     */
    public void setAnimationsClips(HashMap<String, Animation> animations) {
        animationMap = animations;
    }

    /**
     * Retrieve an animation from the list of animations.
     * @param name The name of the animation to retrieve.
     * @return The animation corresponding to the given name, or null, if no
     * such named animation exists.
     */
    public Animation getAnimation(String name) {
        return animationMap.get(name);
    }

    /**
     * Adds an animation to be available for playing to this
     * <code>AnimManager</code>.
     * @param anim The animation to add.
     */
    public void addAnimation(Animation anim) {
        animationMap.put(anim.getName(), anim);
    }

    /**
     * Remove an animation so that it is no longer available for playing.
     * @param anim The animation to remove.
     */
    public void removeAnimation(Animation anim) {
        if (!animationMap.containsKey(anim.getName())) {
            throw new IllegalArgumentException("Given animation does not exist "
                    + "in this AnimControl");
        }

        animationMap.remove(anim.getName());
    }

    public AnimationMetaData getMetaData() {
        return metaData;
    }

    /**
     * Adds a new listener to receive animation related events.
     * @param listener The listener to add.
     */
    public void addListener(AnimEventListener listener) {
        if (listeners.contains(listener)) {
            throw new IllegalArgumentException("The given listener is already "
                    + "registed at this AnimControl");
        }

        listeners.add(listener);
    }

    /**
     * Removes the given listener from listening to events.
     * @param listener
     * @see AnimControl#addListener(com.jme3.animation.AnimEventListener)
     */
    public void removeListener(AnimEventListener listener) {
        if (!listeners.remove(listener)) {
            throw new IllegalArgumentException("The given listener is not "
                    + "registed at this AnimControl");
        }
    }

    final void reset() {
        if (metaData.getSkeleton() != null) {
            metaData.getSkeleton().resetAndUpdate();
        }
    }

    /**
     * @return The names of all animations that this <code>AnimControl</code>
     * can play.
     */
    public Collection<String> getAnimationNames() {
        return animationMap.keySet();
    }

    /**
     * Returns the length of the given named animation.
     * @param name The name of the animation
     * @return The length of time, in seconds, of the named animation.
     */
    public float getAnimationLength(String name) {
        Animation a = animationMap.get(name);
        if (a == null) {
            throw new IllegalArgumentException("The animation " + name
                    + " does not exist in this AnimControl");
        }

        return a.getLength();
    }

    public void setInitialState(AnimState state){
        activeStates.add(state);
    }

    /**
     * Internal use only.
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (metaData.getSkeleton() != null) {
            metaData.getSkeleton().reset(); // reset skeleton to bind pose
        }
        tpf *= globalSpeed;

        //Update States.
        //Note that we can have several active animState only if the have different masks...
        //There is no accurate way to check this as it depends on the transition conditions
        for (AnimState animState : activeStates.getArray()) {
            weightedAnims.clear();
            AnimState state = animState;
            AnimState newState = animState.update(ANY_STATE.getInterruptingTransitions(),ANY_STATE.getTransitions());
            if(animState != newState){
                //removing the old state and adding the new one to the activeState list.
                //Note that this is crucial to keep activeStates as a SafeArrayList, as ArraytList doesn't allow modifications while iterating over it.
                activeStates.remove(animState);
                activeStates.add(newState);
                state = newState;
            }
            state.resolve(weightedAnims, tpf);
            //maybe keep the mask from previous iteration and throw an exception if 2 states are active for the same mask...
            //or maybe do something clever and blend them at 0.5...we'll see

            //Update animations.
            TempVars vars = TempVars.get();
            for (Animation anim : weightedAnims.getArray()) {
                //System.err.println(anim.getName()+ ": " + animEntry.getValue());
                anim.setTime(anim.getBlendingData().getTime(), anim.getBlendingData().getWeight(), metaData, state.getMask(), vars);
            }
            vars.release();
        }

        if (metaData.getSkeleton() != null) {
            metaData.getSkeleton().updateWorldVectors();
        }
    }

    public SafeArrayList<AnimState> getActiveStates() {
        return activeStates;
    }

    /**
     * Internal use only.
     */
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(metaData.getSkeleton(), "skeleton", null);
        oc.writeStringSavableMap(animationMap, "animations", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        metaData.setSkeleton((Skeleton) in.readSavable("skeleton", null));
        HashMap<String, Animation> loadedAnimationMap = (HashMap<String, Animation>) in.readStringSavableMap("animations", null);
        if (loadedAnimationMap != null) {
            animationMap = loadedAnimationMap;
        }
    }

    public float getGlobalSpeed() {
        return globalSpeed;
    }

    public void setGlobalSpeed(float globalSpeed) {
        this.globalSpeed = globalSpeed;
    }
}
