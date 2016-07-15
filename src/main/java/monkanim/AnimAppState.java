package monkanim;

import com.jme3.anim.*;
import com.jme3.anim.blending.LinearBlendSpace;
import com.jme3.anim.statemachine.*;
import com.jme3.animation.*;
import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.*;
import com.jme3.scene.*;

/**
 * Created by Nehon on 08/07/2016.
 */
public class AnimAppState extends BaseAppState {

    AnimationManager manager;
    private String currentState = "";
    @Override
    protected void initialize(Application app) {
        Node s = (Node)app.getAssetManager().loadModel("Models/puppet.xbuf");
        Node rootNode = ((SimpleApplication)app).getRootNode();
        rootNode.attachChild(s.getChild("rig"));
        rootNode.addLight(new DirectionalLight(new Vector3f(-1,-1,-1).normalizeLocal()));

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
        Skeleton skeleton = skelControl.getSkeleton();
        System.err.println(skeleton.getBone(0));
        rig.removeControl(AnimControl.class);
        rig.removeControl(SkeletonControl.class);

        rig.addControl(manager);
        rig.addControl(skelControl);

        //Creating a blending sequence between walk, jog and run
        manager.createAnimationSequence("walk_jog_run", "walk", "jog", "run");

        //Creating a blending sequence between several sequences
        AnimationSequence seq = manager.createAnimationSequence("walk_jog", "walk", "jog");
        ((LinearBlendSpace)seq.getBlendSpace()).setValue(0.5f);
        manager.createAnimationSequence("walk_jog_nestedRun", "walk_jog", "run");

        //this is bad... I have to make a better API
        AnimState state = new AnimState("idle");
        state.setSequence(manager.getSequences().get("idle"));
        manager.setInitialState(state);

        state = new AnimState("walk");
        state.setSequence(manager.getSequences().get("walk"));
        manager.ANY_STATE.addTransition(new InterruptingTransition(state, () -> currentState.equals("walk")));

    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
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

    private void dumpSkeleton(Skeleton skeleton){

        for (int i = 0; i < skeleton.getBoneCount(); i++) {
            System.err.println(skeleton.getBone(i));
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
