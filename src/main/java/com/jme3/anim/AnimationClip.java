/*
 * Copyright (c) 2009-2016 jMonkeyEngine
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

import com.jme3.animation.ClonableTrack;
import com.jme3.export.*;
import com.jme3.scene.Spatial;
import com.jme3.util.*;
import com.jme3.util.clone.*;

import java.io.IOException;
import java.util.Map;

/**
 * The animation class updates the animation target with the tracks of a given type.
 *
 * @author Kirill Vainer, Marcin Roguski (Kaelthas)
 */
public class AnimationClip implements Savable, Cloneable, JmeCloneable, Anim {

    /**
     * The name of the animation.
     */
    private String name;
    /**
     * The length of the animation.
     */
    private float length;
    /**
     * The tracks of the animation.
     */
    private SafeArrayList<AnimTrack> tracks = new SafeArrayList<AnimTrack>(AnimTrack.class);

    /**
     * The number of frames in the data tracks.
     */
    private int nbFrames = -1;

    /**
     * Serialization-only. Do not use.
     */
    public AnimationClip() {
    }

    /**
     * Creates a new <code>Animation</code> with the given name and length.
     *
     * @param name   The name of the animation.
     * @param length Length in seconds of the animation.
     */
    public AnimationClip(String name, float length) {
        this.name = name;
        this.length = length;
    }

    /**
     * The name of the bone animation
     *
     * @return name of the bone animation
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the length in seconds of this animation
     *
     * @return the length in seconds of this animation
     */
    @Override
    public float getLength() {
        return length;
    }

    @Override
    public void resolve(Map<AnimationClip, Float> weightedAnimMap, float globalWeight) {
        if(globalWeight == 0){
            return;
        }
        weightedAnimMap.put(this, globalWeight);
    }

    public int getNbFrames() {
        return nbFrames;
    }

    /**
     * This method sets the current time of the animation.
     * This method behaves differently for every known track type.
     * Override this method if you have your own type of track.
     *
     * @param time        the time of the animation
     * @param blendAmount the blend amount factor
     * @param control     the animation control
     * @param channel     the animation channel
     */
    void setTime(float time, float blendAmount, AnimationManager manager, AnimationMask mask, TempVars vars) {
        if (tracks == null) {
            return;
        }

        for (AnimTrack track : tracks) {
            track.setTime(time, blendAmount, manager, mask, vars);
        }
    }

    /**
     * Set the {@link AnimTrack}s to be used by this animation.
     *
     * @param tracksArray The tracks to set.
     */
    public void setTracks(AnimTrack[] tracksArray) {
        for (AnimTrack track : tracksArray) {
            addTrack(track);
        }
    }

    /**
     * Adds a track to this animation
     *
     * @param track the track to add
     */
    public void addTrack(AnimTrack track) {
        tracks.add(track);
       // if (nbFrames == -1) {
            nbFrames = track.getKeyFrameTimes().length;
//        } else if(nbFrames != track.getKeyFrameTimes().length){
//            throw new IllegalArgumentException("The track has a different number of frames than the ones already in the animation (new track: " + track.getKeyFrameTimes().length + ", animation: " + nbFrames + ")");
//        }

    }

    /**
     * removes a track from this animation
     *
     * @param track the track to remove
     */
    public void removeTrack(AnimTrack track) {
        tracks.remove(track);
        if (track instanceof ClonableTrack) {
            ((ClonableTrack) track).cleanUp();
        }
    }

    /**
     * Returns the tracks set in {@link #setTracks(AnimTrack[]) }.
     *
     * @return the tracks set previously
     */
    public AnimTrack[] getTracks() {
        return tracks.getArray();
    }

    /**
     * This method creates a clone of the current object.
     *
     * @return a clone of the current object
     */
    @Override
    public AnimationClip clone() {
        try {
            AnimationClip result = (AnimationClip) super.clone();
            result.tracks = new SafeArrayList<AnimTrack>(AnimTrack.class);
            for (AnimTrack track : tracks) {
                result.tracks.add(track.clone());
            }
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /**
     * @param spat
     * @return
     */
    public AnimationClip cloneForSpatial(Spatial spat) {
        try {
            AnimationClip result = (AnimationClip) super.clone();
            result.tracks = new SafeArrayList<AnimTrack>(AnimTrack.class);
            for (AnimTrack track : tracks) {
                result.tracks.add(track);
            }
            return result;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    @Override
    public Object jmeClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Error cloning", e);
        }
    }

    @Override
    public void cloneFields(Cloner cloner, Object original) {

        // There is some logic here that I'm copying but I'm not sure if
        // it's a mistake or not.  If a track is not a CloneableTrack then it
        // isn't cloned at all... even though they all implement clone() methods. -pspeed
        SafeArrayList<AnimTrack> newTracks = new SafeArrayList<>(AnimTrack.class);
        for (AnimTrack track : tracks) {
            if (track instanceof ClonableTrack) {
                newTracks.add(cloner.clone(track));
            } else {
                // this is the part that seems fishy 
                newTracks.add(track);
            }
        }
        this.tracks = newTracks;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[name=" + name + ", length=" + length + ", nbFrames=" + nbFrames + ']';
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write(name, "name", null);
        out.write(length, "length", 0f);
        out.write(nbFrames, "nbFrames", -1);
        out.write(tracks.getArray(), "tracks", null);
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        name = in.readString("name", null);
        length = in.readFloat("length", 0f);
        nbFrames = in.readInt("nbFrames", -1);
        Savable[] arr = in.readSavableArray("tracks", null);
        tracks = new SafeArrayList<AnimTrack>(AnimTrack.class);
        for (Savable savable : arr) {
            tracks.add((AnimTrack) savable);
        }
    }
}
