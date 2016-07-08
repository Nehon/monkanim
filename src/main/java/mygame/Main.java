package mygame;

import com.jme3.animation.*;
import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.*;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.*;

import jme3_ext_xbuf.XbufLoader;

import static java.awt.SystemColor.control;

/**
 * test
 *
 * @author normenhansen
 */
public class Main extends SimpleApplication {

    public static void main(String[] args) {
        Main app = new Main();
        app.start();
    }

    AnimationSequence sequence;
    @Override
    public void simpleInitApp() {

        assetManager.registerLoader(XbufLoader.class, "xbuf");
        Node s = (Node)assetManager.loadModel("Models/puppet.xbuf");

        rootNode.attachChild(s.getChild("rig"));
        rootNode.addLight(new DirectionalLight(new Vector3f(-1,-1,-1).normalizeLocal()));

        cam.setLocation(new Vector3f(4.1520014f, 1.6250974f, 1.3038764f));
        cam.setRotation(new Quaternion(0.04338501f, -0.8087327f, 0.060133625f, 0.58348364f));

        dumpSceneGraph(rootNode, "");

        Spatial rig = rootNode.getChild("rig");
        AnimControl control = rig.getControl(AnimControl.class);
//        AnimChannel channel = control.createChannel();
//        channel.setAnim("walk");
//        channel.setSpeed(1);
//        channel.setLoopMode(LoopMode.Loop);

        AnimationManager manager = AnimMigrationUtil.fromAnimControl(control);
        SkeletonControl skelControl = rig.getControl(SkeletonControl.class);
        rig.removeControl(AnimControl.class);
        rig.removeControl(SkeletonControl.class);
        rig.addControl(manager);
        rig.addControl(skelControl);

       // manager.setActiveSequence("run");

        sequence = new AnimationSequence("Move", "walk", "run");
        sequence.setValue(0.0f);
        sequence.setSpeed(1.0f);
        manager.addAnimationSequence(sequence);
        manager.setActiveSequence("Move");


        inputManager.addMapping("up", new KeyTrigger(KeyInput.KEY_UP));
        inputManager.addMapping("down", new KeyTrigger(KeyInput.KEY_DOWN));

        inputManager.addListener(new AnalogListener() {
            @Override
            public void onAnalog(String name, float value, float tpf) {
                if( name.equals("up") ){
                    sequence.setValue(sequence.getValue() + value * 0.3f);
                    sequence.setSpeed(sequence.getValue() * 2 + 1);
                }

                if( name.equals("down") ){
                    sequence.setValue(sequence.getValue() - value * 0.3f);
                    sequence.setSpeed(sequence.getValue() * 2 + 1);
                }
            }
        }, "up", "down");

        flyCam.setEnabled(false);
    }

    private void dumpSceneGraph(Spatial n, String indent){
        System.err.println(indent + n.toString());
        for (int i = 0; i < n.getNumControls(); i++) {
            System.err.println(indent + "  =>" + n.getControl(i).toString());
        }
        if(n instanceof Node){
            Node node = (Node)n;
            for (Spatial spatial : node.getChildren()) {
                dumpSceneGraph(spatial , indent + "    ");
            }
        }
    }

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}