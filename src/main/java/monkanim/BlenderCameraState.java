/*
 * $Id$
 *
 * Copyright (c) 2016, Simsilica, LLC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package monkanim;

import com.jme3.app.Application;
import com.jme3.app.state.BaseAppState;
import com.jme3.input.KeyInput;
import com.jme3.math.*;
import com.jme3.renderer.Camera;
import com.jme3.scene.*;
import com.jme3.scene.control.CameraControl;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.input.*;

/**
 *  A standard app state for a blender like camera motion.
 *
 *  Press mouse wheel button and move the mouse to rotate the view.
 *  Press mouse wheel + left shift and move the mouse to pan the view.
 *  Use the mouse wheel to zoom in and out.
 *  Type NumPad0 to center the view on the selection, to scene center if no selection.
 *
 *  @author    Rémy Bouquet
 */
public class BlenderCameraState extends BaseAppState {

    public static final String GROUP = "Blender Camera";
    public static final FunctionId F_CENTER = new FunctionId(GROUP, "Center");
    public static final FunctionId F_VERTICAL_MOVE = new FunctionId(GROUP, "Vertical Move");
    public static final FunctionId F_HORIZONTAL_MOVE = new FunctionId(GROUP, "Horizontal Move");
    public static final FunctionId F_START_PAN = new FunctionId(GROUP, "Start pan");
    public static final FunctionId F_VERTICAL_ROTATE = new FunctionId(GROUP, "Vertical Rotate");
    public static final FunctionId F_HORIZONTAL_ROTATE = new FunctionId(GROUP, "Horizontal Rotate");
    public static final FunctionId F_ZOOM = new FunctionId(GROUP, "Zoom");

    //num pad short cuts for camera POV
    public static final FunctionId F_POV_FRONT = new FunctionId(GROUP, "Front POV");
    public static final FunctionId F_POV_BACK = new FunctionId(GROUP, "Back POV");
    public static final FunctionId F_POV_LEFT = new FunctionId(GROUP, "Laft POV");
    public static final FunctionId F_POV_RIGHT = new FunctionId(GROUP, "Right POV");
    public static final FunctionId F_POV_TOP = new FunctionId(GROUP, "Top POV");
    public static final FunctionId F_POV_BOTTOM = new FunctionId(GROUP, "Bottom POV");

    public static final FunctionId F_POV_ROTATE_LEFT = new FunctionId(GROUP, "Rotate POV Left");
    public static final FunctionId F_POV_ROTATE_RIGHT= new FunctionId(GROUP, "Rotate POV Right");
    public static final FunctionId F_POV_ROTATE_TOP = new FunctionId(GROUP, "Rotate POV Top");
    public static final FunctionId F_POV_ROTATE_BOTTOM = new FunctionId(GROUP, "Rotate POV Bottom");



    private final static float ROT_FACTOR = 0.1f;


    private Camera cam;
    private Node target;
    private CameraNode camNode;
    private Quaternion futureTargetRot;
    private Quaternion startTargetRot;
    private Vector3f futureTargetPos;
    private Vector3f startTargetPos;
    float time = 0;
    Quaternion tmpQuat = new Quaternion();

    private float delay = 0.25f;

    private InputHandler inputHandler = new InputHandler();

    public BlenderCameraState() {
        this(true);
    }

    public BlenderCameraState(boolean enabled ) {
        setEnabled(enabled);
    }

    public void setCamera( Camera cam ) {
        this.cam = cam;
    }

    public Camera getCamera() {
        return cam;
    }

    @Override
    protected void initialize( Application app ) {

        if( this.cam == null ) {
            this.cam = app.getCamera();
        }

        target = new Node("Blender cam target");
        camNode = new CameraNode("Blender cam holder", cam);
        camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camNode.setEnabled(false);
        target.attachChild(camNode);

        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.addAnalogListener(inputHandler, F_VERTICAL_MOVE, F_HORIZONTAL_MOVE, F_VERTICAL_ROTATE, F_HORIZONTAL_ROTATE, F_ZOOM);
        inputMapper.addStateListener(inputHandler, F_CENTER, F_START_PAN);

        // See if there are already mappings for these functions.
        if( !inputMapper.hasMappings(F_VERTICAL_ROTATE) && !inputMapper.hasMappings(F_HORIZONTAL_ROTATE)) {
            System.out.println("Initializing default mappings for:" + F_VERTICAL_ROTATE + " and " + F_HORIZONTAL_ROTATE);
            inputMapper.map(F_HORIZONTAL_ROTATE, Axis.MOUSE_X, Button.MOUSE_BUTTON3);
            inputMapper.map(F_VERTICAL_ROTATE, Axis.MOUSE_Y, Button.MOUSE_BUTTON3);
        }

        if( !inputMapper.hasMappings(F_VERTICAL_MOVE) && ! inputMapper.hasMappings(F_HORIZONTAL_MOVE)) {
            System.out.println("Initializing default mappings for:" + F_VERTICAL_MOVE + " and " + F_HORIZONTAL_MOVE);
            inputMapper.map(F_HORIZONTAL_MOVE, Axis.MOUSE_X, Button.MOUSE_BUTTON3, KeyInput.KEY_LSHIFT);
            inputMapper.map(F_VERTICAL_MOVE, Axis.MOUSE_Y, Button.MOUSE_BUTTON3, KeyInput.KEY_LSHIFT);
            inputMapper.map(F_START_PAN,Button.MOUSE_BUTTON3, KeyInput.KEY_LSHIFT);
        }

        if( !inputMapper.hasMappings(F_ZOOM)) {
            System.out.println("Initializing default mappings for:" + F_ZOOM);
            inputMapper.map(F_ZOOM, Axis.MOUSE_WHEEL);
        }

        if( !inputMapper.hasMappings(F_CENTER)) {
            System.out.println("Initializing default mappings for:" + F_CENTER);
            inputMapper.map(F_CENTER, KeyInput.KEY_NUMPAD0);
        }

        if( !inputMapper.hasMappings(F_POV_FRONT)){
            inputMapper.map(F_POV_FRONT, KeyInput.KEY_NUMPAD1);
            inputMapper.map(F_POV_BACK, KeyInput.KEY_NUMPAD1, KeyInput.KEY_LCONTROL);
            inputMapper.map(F_POV_BACK, KeyInput.KEY_NUMPAD1, KeyInput.KEY_RCONTROL);
            inputMapper.map(F_POV_RIGHT, KeyInput.KEY_NUMPAD3);
            inputMapper.map(F_POV_LEFT, KeyInput.KEY_NUMPAD3, KeyInput.KEY_RCONTROL);
            inputMapper.map(F_POV_LEFT, KeyInput.KEY_NUMPAD3, KeyInput.KEY_LCONTROL);
            inputMapper.map(F_POV_TOP, KeyInput.KEY_NUMPAD7);
            inputMapper.map(F_POV_BOTTOM, KeyInput.KEY_NUMPAD7, KeyInput.KEY_RCONTROL);
            inputMapper.map(F_POV_BOTTOM, KeyInput.KEY_NUMPAD7, KeyInput.KEY_LCONTROL);
        }

        inputMapper.addStateListener(new StateFunctionListener() {
            @Override
            public void valueChanged(FunctionId func, InputState value, double tpf) {
                if(func == F_POV_FRONT && value == InputState.Positive){
                    switchToFront();
                }
                if(func == F_POV_BACK && value == InputState.Positive){
                    switchToBack();
                }
                if(func == F_POV_LEFT && value == InputState.Positive){
                    switchToLeft();
                }
                if(func == F_POV_RIGHT && value == InputState.Positive){
                    switchToRight();
                }
                if(func == F_POV_TOP && value == InputState.Positive){
                    switchToTop();
                }
                if(func == F_POV_BOTTOM && value == InputState.Positive){
                    switchToBottom();
                }

            }
        }, F_POV_FRONT, F_POV_BACK, F_POV_LEFT, F_POV_RIGHT, F_POV_TOP, F_POV_BOTTOM);

    }

    public void switchToFront(){
        futureTargetRot = Quaternion.IDENTITY;
        System.out.println("Front " + futureTargetRot);
        startTargetRot = target.getLocalRotation();
    }
    public void switchToBack(){
        futureTargetRot = new Quaternion().fromAngleAxis(FastMath.PI,Vector3f.UNIT_Y);
        System.out.println("Back " + futureTargetRot);
        startTargetRot = target.getLocalRotation();
    }
    public void switchToLeft(){
        futureTargetRot = new Quaternion().fromAngleAxis(-FastMath.HALF_PI,Vector3f.UNIT_Y);
        System.out.println("Left " + futureTargetRot);
        startTargetRot = target.getLocalRotation();
    }
    public void switchToRight(){
        futureTargetRot = new Quaternion().fromAngleAxis(FastMath.HALF_PI,Vector3f.UNIT_Y);
        System.out.println("Right " + futureTargetRot);
        startTargetRot = target.getLocalRotation();
    }
    public void switchToTop(){
        futureTargetRot = new Quaternion().fromAngleAxis(-FastMath.HALF_PI,Vector3f.UNIT_X);
        System.out.println("Top " + futureTargetRot);
        startTargetRot = target.getLocalRotation();
    }
    public void switchToBottom(){
        futureTargetRot = new Quaternion().fromAngleAxis(FastMath.HALF_PI,Vector3f.UNIT_X);
        System.out.println("Bottom " + futureTargetRot);
        startTargetRot = target.getLocalRotation();
    }

    @Override
    protected void cleanup( Application app ) {
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.removeAnalogListener(inputHandler, F_VERTICAL_MOVE, F_HORIZONTAL_MOVE, F_VERTICAL_ROTATE, F_HORIZONTAL_ROTATE, F_ZOOM);
        inputMapper.removeStateListener(inputHandler, F_CENTER);
    }

    @Override
    protected void onEnable() {
        System.out.println(getClass().getName() + " Enabled");
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.activateGroup(GROUP);

        target.move(0,1,0);
        target.rotate(-25 * FastMath.DEG_TO_RAD,45 * FastMath.DEG_TO_RAD, 0);
        camNode.setLocalTranslation(0,0,4);
        camNode.lookAt(target.getWorldTranslation(),Vector3f.UNIT_Y);
        camNode.setEnabled(true);

    }

    @Override
    protected void onDisable() {
        System.out.println(getClass().getName() + " Disabled");
        InputMapper inputMapper = GuiGlobals.getInstance().getInputMapper();
        inputMapper.deactivateGroup(GROUP);

        target.removeFromParent();
        camNode.setEnabled(false);
    }


    @Override
    public void update( float tpf ) {
        target.updateLogicalState(tpf);
        target.updateGeometricState();

        if( futureTargetRot != null){
            time += tpf;
            if(time < delay) {
                tmpQuat.set(startTargetRot).nlerp(futureTargetRot, time / delay);
                target.setLocalRotation(tmpQuat);
            }else{
                time = 0;
                target.setLocalRotation(futureTargetRot);
                futureTargetRot = null;
            }
        }
        if( futureTargetPos != null){
            time += tpf;
            if(time < delay) {
                Vector3f v = new Vector3f();
                v.set(startTargetPos);
                v.interpolateLocal(futureTargetPos, time / delay);
                target.setLocalTranslation(v);
            }else{
                time = 0;
                target.setLocalTranslation(futureTargetPos);
                futureTargetPos = null;
            }
        }
    }

    @Override
    public void postRender() {
    }

    private class InputHandler implements AnalogFunctionListener, StateFunctionListener {

        private Vector3f tmpVec = new Vector3f();
        private Vector3f tmpVec2 = new Vector3f();
        private Quaternion tmpRot = new Quaternion();
        private Quaternion tmpRot2 = new Quaternion();
        private Quaternion tmpRot3 = new Quaternion();

        protected Vector3f xDelta = new Vector3f();
        protected Vector3f yDelta = new Vector3f();
        protected Vector2f lastCursor = new Vector2f();

        public void valueChanged( FunctionId func, InputState value, double tpf ) {
            if( func == F_CENTER ) {
                System.out.println("Function:" + func + "  value:" + value + "  tpf:" + tpf );
                if(value == InputState.Off){
                    futureTargetPos = Vector3f.ZERO;
                    startTargetPos = target.getWorldTranslation();
                }
            } else if(func == F_START_PAN && value == InputState.Positive){

                Vector3f up = cam.getUp();
                Vector3f right = cam.getLeft().negate();

                Vector3f originScreen = cam.getScreenCoordinates(target.getWorldTranslation());
                Vector3f xScreen = cam.getScreenCoordinates(target.getWorldTranslation().add(right));
                Vector3f yScreen = cam.getScreenCoordinates(target.getWorldTranslation().add(up));

                float x = xScreen.x - originScreen.x;
                float y = yScreen.y - originScreen.y;

                xDelta.set(right).divideLocal(x);
                yDelta.set(up).divideLocal(y);

                lastCursor.set(getApplication().getInputManager().getCursorPosition());
            }
        }

        public void valueActive( FunctionId func, double value, double tpf ) {
            if(func == F_HORIZONTAL_MOVE || func == F_VERTICAL_MOVE){
                Vector2f cursor = getApplication().getInputManager().getCursorPosition();
                float x = cursor.getX() - lastCursor.x;
                float y = cursor.getY() - lastCursor.y;

                target.move(xDelta.mult(-x).addLocal(yDelta.mult(-y)));

                lastCursor.set(cursor);
            } else if(func == F_HORIZONTAL_ROTATE || func == F_VERTICAL_ROTATE){
                //Extract horizontal rotation
                //get left vector and cross product it with unitY (to "flatten" the direction on the horizontal plane)
                target.getLocalRotation().getRotationColumn(0,tmpVec).crossLocal(Vector3f.UNIT_Y);
                tmpRot.lookAt(tmpVec, Vector3f.UNIT_Y);

                //Extract vertical rotation
                //get the direction of the rotation
                //rotate it by the inverse horizontal rotation.
                //get the rotation from that vector.
                target.getLocalRotation().getRotationColumn(2,tmpVec);
                tmpRot3.set(tmpRot).inverseLocal().mult(tmpVec, tmpVec);
                //Finding the up axis (negating it when we go backward to be able to completely rotate aroudn the target)
                tmpVec2.set(Vector3f.UNIT_Y).multLocal(FastMath.sign(tmpVec.getZ()));
                tmpRot2.lookAt(tmpVec, tmpVec2);

                //computing the additional rotation and combining it the right orders
                if(func == F_HORIZONTAL_ROTATE){
                    //the incremental horizontal rotation.
                    tmpRot3.fromAngleAxis((float)-value * ROT_FACTOR, Vector3f.UNIT_Y);
                    //applying the horizontal incremental rotation on the horizontal rotation.
                    tmpRot.multLocal(tmpRot3);
                    //applying the vertical rotation on the resulting horizontal rotation.
                    tmpRot.multLocal(tmpRot2);
                }else {
                    //the incremental vertical rotation
                    tmpRot3.fromAngleAxis((float)value * ROT_FACTOR, Vector3f.UNIT_X);
                    //applying the incremental vertical rotation on the vertical rotation.
                    tmpRot2.multLocal(tmpRot3);
                    //Applying the resulting vertical rotation on the horizontal rotation.
                    tmpRot.multLocal(tmpRot2);
                }

                //Setting the new rotation
                target.setLocalRotation(tmpRot);
            } else if(func == F_ZOOM){
                float factor = Math.min(camNode.getLocalTranslation().z * 0.1f, 2);
                tmpVec.set(camNode.getLocalTranslation()).addLocal(0,0,(float) -value * factor);
                tmpVec.z = Math.max(tmpVec.z, 1f);
                camNode.setLocalTranslation(tmpVec);
            }
        }
    }
}
