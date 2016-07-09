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
package com.jme3.animation;

import com.jme3.export.*;
import com.jme3.renderer.*;
import com.jme3.scene.control.*;
import com.jme3.util.TempVars;
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
    private Skeleton skeleton;
    /**
     * List of animations
     */
    private Map<String, AnimationClip> animationMap = new HashMap<>();
    private Map<String, AnimationSequence> sequences = new HashMap<>();

    private Map<AnimationClip, Float> weightedAnimMap = new HashMap<>();

    private Map<String, Object> parameters = new HashMap<>();

    private Map<String, AnimationMask> masks = new HashMap<>();
    private AnimationSequence activeSequence;

    /**
     * Animation event listeners
     */
    private transient ArrayList<AnimEventListener> listeners = new ArrayList<AnimEventListener>();

    /**
     * Creates a new animation control for the given skeleton.
     * The method {@link AnimControl#setAnimations(HashMap) }
     * must be called after initialization in order for this class to be useful.
     *
     * @param skeleton The skeleton to animate
     */
    public AnimationManager(Skeleton skeleton) {
        this.skeleton = skeleton;
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
        clone.listeners = new ArrayList<AnimEventListener>();

        return clone;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original ) {
        super.cloneFields(cloner, original);

        this.skeleton = cloner.clone(skeleton);

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
        HashMap<String, AnimationClip> newMap = new HashMap<>();

        // animationMap is cloned, but only ClonableTracks will be cloned as they need a reference to a cloned spatial
        for( Entry<String, AnimationClip> e : animationMap.entrySet() ) {
            newMap.put(e.getKey(), cloner.clone(e.getValue()));
        }

        this.animationMap = newMap;
    }

    public AnimationSequence createAnimationSequence(String name){
        return createAnimationSequence(name, null);
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

    public void setActiveSequence(String sequenceName){
        activeSequence = sequences.get(sequenceName);
        activeSequence.reset();
    }

    public AnimationSequence getActiveSequence(){
        return activeSequence;
    }

    public Map<AnimationClip, Float> getWeightedAnimMap() {
        return weightedAnimMap;
    }

    /**
     * @param animations Set the animations that this <code>AnimControl</code>
     * will be capable of playing. The animations should be compatible
     * with the skeleton given in the constructor.
     */
    public void setAnimationsClips(HashMap<String, AnimationClip> animations) {
        animationMap = animations;
    }

    /**
     * Retrieve an animation from the list of animations.
     * @param name The name of the animation to retrieve.
     * @return The animation corresponding to the given name, or null, if no
     * such named animation exists.
     */
    public AnimationClip getAnimationClip(String name) {
        return animationMap.get(name);
    }

    /**
     * Adds an animation to be available for playing to this
     * <code>AnimManager</code>.
     * @param anim The animation to add.
     */
    public void addAnimationClip(AnimationClip anim) {
        animationMap.put(anim.getName(), anim);
    }

    /**
     * Remove an animation so that it is no longer available for playing.
     * @param anim The animation to remove.
     */
    public void removeAnimationClip(AnimationClip anim) {
        if (!animationMap.containsKey(anim.getName())) {
            throw new IllegalArgumentException("Given animation does not exist "
                    + "in this AnimControl");
        }

        animationMap.remove(anim.getName());
    }

    /**
     * @return The skeleton of this <code>AnimControl</code>.
     */
    public Skeleton getSkeleton() {
        return skeleton;
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
        if (skeleton != null) {
            skeleton.resetAndUpdate();
        }
    }

    /**
     * @return The names of all animations that this <code>AnimControl</code>
     * can play.
     */
    public Collection<String> getAnimationClipNames() {
        return animationMap.keySet();
    }

    /**
     * Returns the length of the given named animation.
     * @param name The name of the animation
     * @return The length of time, in seconds, of the named animation.
     */
    public float getAnimationClipLength(String name) {
        AnimationClip a = animationMap.get(name);
        if (a == null) {
            throw new IllegalArgumentException("The animation " + name
                    + " does not exist in this AnimControl");
        }

        return a.getLength();
    }

    /**
     * Internal use only.
     */
    @Override
    protected void controlUpdate(float tpf) {
        if (skeleton != null) {
            skeleton.reset(); // reset skeleton to bind pose
        }

        if(activeSequence != null) {
            TempVars vars = TempVars.get();
            activeSequence.update(tpf);

            weightedAnimMap.clear();
            activeSequence.resolve(weightedAnimMap, 1);

            float length = 0;
            for (Entry<AnimationClip, Float> animEntry : weightedAnimMap.entrySet()) {

                AnimationClip anim = animEntry.getKey();
                if(anim.getLength() > length){
                    length = anim.getLength();
                }

                anim.setTime(activeSequence.getTime(), animEntry.getValue(), this, null, vars);
            }
            vars.release();
            if(activeSequence.getTime() >= activeSequence.getLength()){
                activeSequence.reset();
            }

        }

        if (skeleton != null) {
            skeleton.updateWorldVectors();
        }
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
        oc.write(skeleton, "skeleton", null);
        oc.writeStringSavableMap(animationMap, "animations", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        InputCapsule in = im.getCapsule(this);
        skeleton = (Skeleton) in.readSavable("skeleton", null);
        HashMap<String, AnimationClip> loadedAnimationMap = (HashMap<String, AnimationClip>) in.readStringSavableMap("animations", null);
        if (loadedAnimationMap != null) {
            animationMap = loadedAnimationMap;
        }
    }
}
