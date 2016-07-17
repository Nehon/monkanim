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


        AnimState idleState = manager.createStateForSequence("idle");
        manager.ANY_STATE.addTransition(new InterruptingTransition(idleState, () -> currentState.equals("idle")));


        AnimState walkState = manager.createStateForSequence("walk");
        manager.ANY_STATE.addTransition(new InterruptingTransition(walkState, () -> currentState.equals("walk")));

        AnimState jogState = manager.createStateForSequence("jog");
        manager.ANY_STATE.addTransition(new InterruptingTransition(jogState, () -> currentState.equals("jog")));

        AnimState kickState = manager.createStateForSequence("kick");
        manager.ANY_STATE.addTransition(new InterruptingTransition(kickState, () -> currentState.equals("kick")));

        AnimState runState = manager.createStateForSequence("run");
        manager.ANY_STATE.addTransition(new InterruptingTransition(runState, () -> currentState.equals("run")));

        AnimState state = manager.createStateForSequence("walk_jog_run");
        manager.ANY_STATE.addTransition(new InterruptingTransition(state, () -> currentState.equals("walk_jog_run")));

        state = manager.createStateForSequence("walk_jog");
        manager.ANY_STATE.addTransition(new InterruptingTransition(state, () -> currentState.equals("walk_jog")));

        state = manager.createStateForSequence("walk_jog_nestedRun");
        manager.ANY_STATE.addTransition(new InterruptingTransition(state, () -> currentState.equals("walk_jog_nestedRun")));

        manager.setInitialState(idleState);

        manager.ANY_STATE.addTransition(new InterruptingTransition(walkState, () -> currentState.equals("anim_chain")));
        walkState.addTransition(new Transition(jogState, () -> currentState.equals("anim_chain")));
        jogState.addTransition(new Transition(runState, () -> currentState.equals("anim_chain")));
        runState.addTransition(new Transition(kickState, () -> currentState.equals("anim_chain")));
        kickState.addTransition(new Transition(idleState, () -> currentState.equals("anim_chain")));


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
