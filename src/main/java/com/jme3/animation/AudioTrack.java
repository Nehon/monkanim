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

import com.jme3.anim.interpolator.TrackInterpolator;
import com.jme3.audio.AudioNode;
import com.jme3.export.*;
import com.jme3.math.EaseFunction;
import com.jme3.scene.*;
import com.jme3.util.TempVars;
import com.jme3.util.clone.Cloner;

import java.io.IOException;
import java.util.logging.*;

import static java.awt.SystemColor.control;

/**
 * AudioTrack is a track to add to an existing animation, to play a sound during
 * an animations for example : gun shot, foot step, shout, etc...
 *
 * usage is
 * <pre>
 * AnimControl control model.getControl(AnimControl.class);
 * AudioTrack track = new AudioTrack(existionAudioNode, control.getAnim("TheAnim").getLength());
 * control.getAnim("TheAnim").addTrack(track);
 * </pre>
 *
 * This is mostly intended for short sounds, playInstance will be called on the
 * AudioNode at time 0 + startOffset.
 *
 *
 * @author Nehon
 */
@Deprecated
public class AudioTrack implements ClonableTrack {

    private static final Logger logger = Logger.getLogger(AudioTrack.class.getName());
    private AudioNode audio;
    private float startOffset = 0;
    private float length = 0;
    private boolean started = false;

    /**
     * default constructor for serialization only
     */
    public AudioTrack() {
    }

    /**
     * Creates an AudioTrack
     *
     * @param audio the AudioNode
     * @param length the length of the track (usually the length of the
     * animation you want to add the track to)
     */
    public AudioTrack(AudioNode audio, float length) {
        this.audio = audio;
        this.length = length;
        setUserData(this);
    }

    /**
     * Creates an AudioTrack
     *
     * @param audio the AudioNode
     * @param length the length of the track (usually the length of the
     * animation you want to add the track to)
     * @param startOffset the time in second when the sound will be played after
     * the animation starts (default is 0)
     */
    public AudioTrack(AudioNode audio, float length, float startOffset) {
        this(audio, length);
        this.startOffset = startOffset;
    }


    /**
     * Internal use only
     *
     * @see Track#setTime(float, float, com.jme3.animation.AnimationMetaData,
     * com.jme3.animation.AnimationMask, com.jme3.util.TempVars, EaseFunction timeEasingFunctio)
     */
    public void setTime(float time, float weight, AnimationMetaData metaData, AnimationMask mask, TempVars vars, TrackInterpolator interpolator) {

        if (time >= length) {
            if(started){
                stop();
            }
            return;
        }

        if (!started && time >= startOffset) {
            started = true;
            audio.playInstance();
        }
    }

    //stops the sound
    private void stop() {
        audio.stop();
        started = false;
    }

    /**
     * Return the length of the track
     *
     * @return length of the track
     */
    public float getLength() {
        return length;
    }

    @Override
    public float[] getKeyFrameTimes() {
        return new float[] { startOffset };
    }
    
    /**
     * Clone this track
     *
     * @return
     */
    @Override
    public Track clone() {
        return new AudioTrack(audio, length, startOffset);
    }

    /**
     * This method clone the Track and search for the cloned counterpart of the
     * original audio node in the given cloned spatial. The spatial is assumed
     * to be the Spatial holding the AnimControl controlling the animation using
     * this Track.
     *
     * @param spatial the Spatial holding the AnimControl
     * @return the cloned Track with proper reference
     */
    @Override
    public Track cloneForSpatial(Spatial spatial) {
        AudioTrack audioTrack = new AudioTrack();
        audioTrack.length = this.length;
        audioTrack.startOffset = this.startOffset;

        //searching for the newly cloned AudioNode
        audioTrack.audio = findAudio(spatial);
        if (audioTrack.audio == null) {
            logger.log(Level.WARNING, "{0} was not found in {1} or is not bound to this track", new Object[]{audio.getName(), spatial.getName()});
            audioTrack.audio = audio;
        }

        //setting user data on the new AudioNode and marking it with a reference to the cloned Track.
        setUserData(audioTrack);

        return audioTrack;
    }

    @Override   
    public Object jmeClone() {
        try {
            return super.clone();
        } catch( CloneNotSupportedException e ) {
            throw new RuntimeException("Error cloning", e);
        }
    }     


    @Override   
    public void cloneFields(Cloner cloner, Object original ) {
        // Duplicating the old cloned state from cloneForSpatial()
        this.started = false;
        this.audio = cloner.clone(audio);
    }
         
         
    /**    
     * recursive function responsible for finding the newly cloned AudioNode
     *
     * @param spat
     * @return
     */
    private AudioNode findAudio(Spatial spat) {
        if (spat instanceof AudioNode) {
            //spat is an AudioNode
            AudioNode em = (AudioNode) spat;
            //getting the UserData TrackInfo so check if it should be attached to this Track
            TrackInfo t = (TrackInfo) em.getUserData("TrackInfo");
            if (t != null && t.getTracks().contains(this)) {
                return em;
            }
            return null;

        } else if (spat instanceof Node) {
            for (Spatial child : ((Node) spat).getChildren()) {
                AudioNode em = findAudio(child);
                if (em != null) {
                    return em;
                }
            }
        }
        return null;
    }

    private void setUserData(AudioTrack audioTrack) {
        //fetching the UserData TrackInfo.
        TrackInfo data = (TrackInfo) audioTrack.audio.getUserData("TrackInfo");

        //if it does not exist, we create it and attach it to the AudioNode.
        if (data == null) {
            data = new TrackInfo();
            audioTrack.audio.setUserData("TrackInfo", data);
        }

        //adding the given Track to the TrackInfo.
        data.addTrack(audioTrack);
    }

    public void cleanUp() {
        TrackInfo t = (TrackInfo) audio.getUserData("TrackInfo");
        t.getTracks().remove(this);
        if (!t.getTracks().isEmpty()) {
            audio.setUserData("TrackInfo", null);
        }
    }

    /**
     *
     * @return the audio node used by this track
     */
    public AudioNode getAudio() {
        return audio;
    }

    /**
     * sets the audio node to be used for this track
     *
     * @param audio
     */
    public void setAudio(AudioNode audio) {
        if (this.audio != null) {
            TrackInfo data = (TrackInfo) audio.getUserData("TrackInfo");
            data.getTracks().remove(this);
        }
        this.audio = audio;
        setUserData(this);
    }

    /**
     *
     * @return the start offset of the track
     */
    public float getStartOffset() {
        return startOffset;
    }

    /**
     * set the start offset of the track
     *
     * @param startOffset
     */
    public void setStartOffset(float startOffset) {
        this.startOffset = startOffset;
    }

    /**
     * Internal use only serialization
     *
     * @param ex exporter
     * @throws IOException exception
     */
    public void write(JmeExporter ex) throws IOException {
        OutputCapsule out = ex.getCapsule(this);
        out.write(audio, "audio", null);
        out.write(length, "length", 0);
        out.write(startOffset, "startOffset", 0);
    }

    /**
     * Internal use only serialization
     *
     * @param im importer
     * @throws IOException Exception
     */
    public void read(JmeImporter im) throws IOException {
        InputCapsule in = im.getCapsule(this);
        audio = (AudioNode) in.readSavable("audio", null);
        length = in.readFloat("length", length);
        startOffset = in.readFloat("startOffset", 0);
    }
}
