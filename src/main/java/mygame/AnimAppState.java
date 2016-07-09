package mygame;

import com.jme3.animation.*;
import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.scene.*;

/**
 * Created by Nehon on 08/07/2016.
 */
public class AnimAppState extends BaseAppState {

    AnimationManager manager;
    @Override
    protected void initialize(Application app) {
        Node s = (Node)app.getAssetManager().loadModel("Models/puppet.xbuf");
        Node rootNode = ((SimpleApplication)app).getRootNode();
        rootNode.attachChild(s.getChild("rig"));
        rootNode.addLight(new DirectionalLight(new Vector3f(-1,-1,-1).normalizeLocal()));

        app.getCamera().setLocation(new Vector3f(4.1520014f, 1.6250974f, 1.3038764f));
        app.getCamera().setRotation(new Quaternion(0.04338501f, -0.8087327f, 0.060133625f, 0.58348364f));

        dumpSceneGraph(rootNode, "");

        Spatial rig = rootNode.getChild("rig");
        AnimControl control = rig.getControl(AnimControl.class);
//        AnimChannel channel = control.createChannel();
//        channel.setAnim("walk");
//        channel.setSpeed(1);
//        channel.setLoopMode(LoopMode.Loop);

        //create an AnnimationManager from an AnimController. This also createa a sequence for each animation
        manager = AnimMigrationUtil.fromAnimControl(control);

        //removing old stuff and adding new stuff
        SkeletonControl skelControl = rig.getControl(SkeletonControl.class);
        rig.removeControl(AnimControl.class);
        rig.removeControl(SkeletonControl.class);
        rig.addControl(manager);
        rig.addControl(skelControl);

        //Creating the blending sequence between walk and run
        AnimationSequence sequence = manager.createAnimationSequence("Move", "walk", "jog", "run");
        //value us initialized to 0
        sequence.setValue(0.0f);
        //speed initialized to 1
        sequence.setSpeed(1.0f);

        manager.setActiveSequence("walk");
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

    public AnimationManager getManager() {
        return manager;
    }

    @Override
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
