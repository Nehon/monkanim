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

import com.jme3.anim.blending.*;
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
 * <code>AnimationManager</code> is a Spatial control that allows manipulation
 * of skeletal animation.
 * <p>
 * The manager currently supports:
 * 1) Animation blending and animation transitions (with blending)
 * 2) Multiple animation layers
 * 3) Multiple skins
 * 4) Animation event listeners //TODO I hope it won't...but I guess I'll have to do something similar
 * 5) Animated model cloning //TODO not anymore...have to fix it
 * 6) Animated model binary import/export //TODO not anymore...have to fix it
 * 7) Hardware skinning
 * 8) Attachments
 * 9) Add/remove skins
 *
 * @author Rémy Bouquet (Nehon)
 */
public final class AnimationManager extends AbstractControl implements Cloneable, JmeCloneable {

    /**
     * Skeleton object must contain corresponding data for the targets' weight buffers.
     */
    private AnimationMetaData metaData = new AnimationMetaData();
    /**
     * the global speed of the motion
     */
    private float globalSpeed = 1.0f;
    /**
     * List of animations
     */
    private Map<String, Animation> animationMap = new HashMap<>();
    /**
     * The flat map of animation with corresponding weight updated on each frame
     */
    private List<AnimationData> weightedAnims = new ArrayList<>();

    private TreeMap<String, AnimationLayer> layers = new TreeMap<>();
    /**
     * Could help with blend space to have basic types parameter added by the user and pick the appropriate blend space
     * Not used right now
     */
    private Map<String, Object> parameters = new HashMap<>();
    /**
     * Animation mask list, that holdw all the masks used by this animation manager.
     */
    public AnimationLayer defaultLayer = new AnimationLayer();

    /**
     * The any state name
     */
    public final static String ANY_STATE = "Any State";
    /**
     * The anyState, not a proper state but used as dummy
     */
    private AnimState anyState = new AnimState(ANY_STATE, this);
    /**
     * the list of all states of this Animation manager.
     */
    private Map<String, AnimState> states = new HashMap<>();
    /**
     * Animation event listeners - This I hope to get rid of....
     */
    private transient ArrayList<AnimEventListener> listeners = new ArrayList<AnimEventListener>();

    /**
     * Creates a new animation manager for the given skeleton.
     * You must add animations to this class for it to be useful.
     *
     * @param skeleton The skeleton to animate
     */
    public AnimationManager(Skeleton skeleton) {
        layers.put(defaultLayer.getName(), defaultLayer);
        defaultLayer.setWeight(1f);
        this.metaData.setSkeleton(skeleton);

        AnimationLayer dummyLayer = new AnimationLayer("dummy");
        dummyLayer.setIndex(-1);
        this.anyState.setLayer(dummyLayer);
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
        clone.states = new HashMap<>();
        clone.layers = new TreeMap<>();
        clone.listeners = new ArrayList<>();
        clone.metaData = new AnimationMetaData();
        clone.weightedAnims = new ArrayList<>();
        return clone;
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {
        super.cloneFields(cloner, original);
        AnimationManager manager = (AnimationManager) original;

        this.metaData.setSkeleton(cloner.clone(manager.metaData.getSkeleton()));
        this.metaData.setSpatial(cloner.clone(manager.metaData.getSpatial()));

        this.anyState = cloner.clone(manager.anyState);

        this.defaultLayer = cloner.clone(manager.defaultLayer);

        for (Entry<String, Object> paramEntry : manager.parameters.entrySet()) {
            this.parameters.put(paramEntry.getKey(), cloner.clone(paramEntry.getValue()));
        }


        for (Entry<String, AnimationLayer> paramEntry : manager.layers.entrySet()) {
            this.layers.put(paramEntry.getKey(), cloner.clone(paramEntry.getValue()));
        }

        for (Entry<String, AnimState> stateEntry : manager.states.entrySet()) {
            AnimState state = cloner.clone(stateEntry.getValue());
            state.setManager(this);
            this.states.put(stateEntry.getKey(), state);
        }

        HashMap<String, Animation> newMap = new HashMap<>();

        // animationMap is cloned, but only ClonableTracks will be cloned as they need a reference to a cloned spatial
        for (Entry<String, Animation> e : animationMap.entrySet()) {
            newMap.put(e.getKey(), cloner.clone(e.getValue()));
        }

        this.animationMap = newMap;
    }

    @Override
    public void setSpatial(Spatial spatial) {
        super.setSpatial(spatial);
        //TODO this is only needed for Audio and effect tracks... may not be needed anymore.
        metaData.setSpatial(spatial);
    }

    /**
     * Creates a state with the given state name, for the given animations.
     *
     * @param stateName    the name of the state
     * @return the created state
     */
    public AnimState createStateForAnims(String stateName, String... animNames) {
        AnimState state = new AnimState(stateName, this);
        if (animNames.length == 0) {
            animNames = new String[]{stateName};
        }

        for (String animName : animNames) {
            Anim clip = getClip(animName);
            state.addAnimation(clip);
        }

        states.put(stateName, state);
        state.setLayer(defaultLayer);
        return state;
    }

    public AnimState createState(String stateName) {
        AnimState state = new AnimState(stateName, this);
        states.put(stateName, state);
        state.setLayer(defaultLayer);
        return state;
    }

    protected Anim getClip(String animName) {
        Animation anim = animationMap.get(animName);

        Anim clip;

        if (anim == null) {
            //we couldn't find an anim with this name, let's look for a state
            clip = states.get(animName);
        } else {
            clip = new AnimationData(anim);
        }
        if (clip == null) {
            //we couldn't find a state either, let's throw an exception
            throw new IllegalArgumentException("Can't find an animation clip or state with name " + animName);
        }
        return clip;
    }

    /**
     * Find a state with the given name. Throws an exception if the state is not found.
     *
     * @param stateName
     * @return
     */
    public AnimState findState(String stateName) {
        AnimState s1 = getState(stateName);
        if (s1 == null) {
            throw new IllegalArgumentException("Cannot find state with name " + stateName);
        }
        return s1;
    }

    /**
     * Returns the state with the given name.
     * returns null if the state is not found.
     *
     * @param stateName the name of the state
     * @return the state.
     */
    public AnimState getState(String stateName) {
        if (stateName.equals(ANY_STATE)) {
            return anyState;
        }
        return states.get(stateName);
    }

    /**
     * returns a read only collection of the states.
     *
     * @return the states.
     */
    public Collection<AnimState> getStates() {
        return states.values();
    }

    /**
     * Sets the initial state of this AnimationManager.
     *
     * @param state the initial state
     */
    //TODO this is not really great as it just set the state as the active states... using this while the animation is playing would just be weird...
    public void startWith(String state) {
        defaultLayer.setActiveState(findState(state));
    }


    /**
     * This should be used only for debugging purpose.
     * Returns the list of the animations with their computed blendingData.
     *
     * @return the list of animation with their blending data.
     */
    public List<AnimationData> getDebugWeightedAnims() {
        //TODO this can be dangerous if modified from the outside...
        return weightedAnims;
    }

    /**
     * Retrieve an animation from the list of animations.
     *
     * @param name The name of the animation to retrieve.
     * @return The animation corresponding to the given name, or null, if no
     * such named animation exists.
     */
    public Animation getAnimation(String name) {
        return animationMap.get(name);
    }

    /**
     * Adds an animation to be available for playing to this
     * <code>AnimationManager</code>.
     *
     * @param anim The animation to add.
     */
    public void addAnimation(Animation anim) {
        animationMap.put(anim.getName(), anim);
    }

    /**
     * Remove an animation so that it is no longer available for playing.
     *
     * @param anim The animation to remove.
     */
    public void removeAnimation(Animation anim) {
        if (!animationMap.containsKey(anim.getName())) {
            throw new IllegalArgumentException("Given animation does not exist "
                    + "in this AnimControl");
        }

        animationMap.remove(anim.getName());
    }

    /**
     * returns a read only collection of the animations of this AnimationManager.
     *
     * @return
     */
    public Collection<Animation> getAnimations() {
        return animationMap.values();
    }

    /**
     * returns the meta data of this <code>AnimationManager</code>.
     *
     * @return the meta data
     */
    public AnimationMetaData getMetaData() {
        return metaData;
    }


    /**
     * returns the global speed of this AnimationManager
     *
     * @return
     */
    public float getGlobalSpeed() {
        return globalSpeed;
    }

    /**
     * Sets the global speed of this animation manager.
     * Not that it will be multiplied with individual sequences speed.
     *
     * @param globalSpeed
     */
    public void setGlobalSpeed(float globalSpeed) {
        this.globalSpeed = globalSpeed;
    }


    public AnimationLayer addLayer(String name) {
        AnimationLayer layer = new AnimationLayer(name);
        addLayer(layer);
        return layer;
    }

    protected void addLayer(AnimationLayer layer) {
        layers.lastEntry().getValue().setNextLayer(layer);
        layer.setIndex(layers.size());
        layers.put(layer.getName(), layer);
    }

    protected AnimationLayer getLayer(String name) {
        return layers.get(name);
    }

    /**
     * Adds a new listener to receive animation related events.
     *
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
     *
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

    public TreeMap<String, AnimationLayer> getLayers() {
        return layers;
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

        weightedAnims.clear();
        //Update the active states.
        for (Entry<String, AnimationLayer> layerEntry : layers.entrySet()) {
            AnimationLayer layer = layerEntry.getValue();
            if (layer.getActiveState() == null) {
                continue;
            }

            AnimState newState = layer.getActiveState().signal(anyState.getInterruptingTransitions(), anyState.getTransitions());
            if (layer.getActiveState() != newState) {
                if (newState == anyState) {

                    if (layer == defaultLayer) {
                        //special case if we are on the default layer, use the fromState of the state
                        newState = layer.getActiveState().getFromState();
                        newState.setFromState(layer.getActiveState());
                        newState.setIncomingTransition(anyState.getIncomingTransition());
                    } else {
                        //else we transition to the any state ( basically we are going to fade out the higher layer)
                        anyState.setFromState(layer.getActiveState());
                    }
                }
                if (newState.getLayer() == layer) {
                    //layer are the same
                    //replacing the old state with the new one.
                    //if layer are different the 2 states will play in parallel
                    layer.getActiveState().cleanup();
                    layer.setActiveState(newState);
                } else {
                    if (newState != anyState) {
                        newState.getLayer().setActiveState(newState);
                    } else {
                        layer.setActiveState(newState);
                    }
                }
            }
            layer.getActiveState().update(weightedAnims, tpf);

        }

        //Update animations.
        TempVars vars = TempVars.get();
        for (AnimationData weightedAnim : weightedAnims) {
            weightedAnim.update(metaData, vars);
        }
        vars.release();

        if (metaData.getSkeleton() != null) {
            metaData.getSkeleton().updateWorldVectors();
        }
    }

    /**
     * Internal use only.
     */
    @Override
    protected void controlRender(RenderManager rm, ViewPort vp) {
    }

    //TODO fix write
    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        OutputCapsule oc = ex.getCapsule(this);
        oc.write(metaData.getSkeleton(), "skeleton", null);
        oc.writeStringSavableMap(animationMap, "animations", null);
    }

    //TODO fix read
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


}
