package monkanim;

import com.jme3.anim.*;
import com.jme3.anim.blending.LinearBlendSpace;
import com.jme3.animation.*;
import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.light.*;
import com.jme3.math.*;
import com.jme3.scene.*;

import static com.jme3.anim.AnimationManager.ANY_STATE;

/**
 * Created by Nehon on 08/07/2016.
 */
public class AnimAppState extends BaseAppState {

    AnimationManager manager;
    private String currentState = "";
    private LinearBlendSpace blendSpace = new LinearBlendSpace();

    @Override
    protected void initialize(Application app) {
        Node s = (Node) app.getAssetManager().loadModel("Models/puppet.xbuf");
        Node rootNode = ((SimpleApplication) app).getRootNode();
        rootNode.attachChild(s.getChild("rig"));
        rootNode.addLight(new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal()));
        rootNode.addLight(new DirectionalLight(new Vector3f(1, -1, 1).normalizeLocal(),new ColorRGBA(0.5f,0.5f,0.5f,1.0f)));

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

        //Bone b = skelControl.getSkeleton().getBone("Foot_Roll.R");
        rig.addControl(manager);
        rig.addControl(skelControl);

        //creating a layer that affects the left arm.
        //Note that there is a default layer that affects the entire skeleton
        manager.addLayer("wave").withMask(SkeletonMask.fromBone(skeleton, "shoulder.L"));

        //state machine
        //creating a state for each animation (optional of course but for the demo
        manager.createState("idle").forAnims("idle");
        manager.createState("walk").forAnims("walk");
        manager.createState("jog").forAnims("jog");
        manager.createState("kick").forAnims("kick");
        manager.createState("run").forAnims("run");
        manager.createState("wave").forAnims("wave");

        //creating a wave anim on the wave layer
        manager.createState("waveLayer").forAnims("wave").onLayer("wave");

        //creating a blended anim composed of the walk, jog and run anims. (will blend according to speed
        manager.createState("walk_jog_run").forAnims("walk", "jog", "run").withBlendSpace(blendSpace);
        //creating a blended anim composed of walk and jog anims, but with a fied blend value of 0.5
        ((LinearBlendSpace) (manager.createState("walk_jog").forAnims("walk", "jog").getBlendSpace())).setValue(0.5f);
        //Mixed blending between a previously defined state and an animation clip.
        manager.createState("walk_jog_nestedRun").forAnims("walk_jog", "run").withBlendSpace(blendSpace);

        //bootstrap state
        manager.startWith("idle");

        //defining transitions and events
        //Any state will intrrupt to the idel state when the currentState local variable is "idle". Note that the condition can be enything
        manager.findState(ANY_STATE).interruptTo("idle").when(() -> currentState.equals("idle"));
        manager.findState(ANY_STATE).interruptTo("walk").when(() -> currentState.equals("walk"));
        manager.findState(ANY_STATE).interruptTo("jog").when(() -> currentState.equals("jog"));
        manager.findState(ANY_STATE).interruptTo("kick").when(() -> currentState.equals("kick"));
        manager.findState(ANY_STATE).interruptTo("run").when(() -> currentState.equals("run"));
        manager.findState(ANY_STATE).interruptTo("wave").when(() -> currentState.equals("wave"));
        manager.findState(ANY_STATE).interruptTo("walk_jog_run").when(() -> currentState.equals("walk_jog_run"));
        manager.findState(ANY_STATE).interruptTo("walk_jog").when(() -> currentState.equals("walk_jog"));
        manager.findState(ANY_STATE).interruptTo("walk_jog_nestedRun").when(() -> currentState.equals("walk_jog_nestedRun"));

        //Special case, when the wave state ends, it will return to "anystate", in that case the state from which wave was triggered
        //Foexample if you go from walk to wave, when wave ends, it will transition bac to walk.
        manager.findState("wave").transitionTo(ANY_STATE);

        manager.findState(ANY_STATE).interruptTo("waveLayer").when(() -> currentState.equals("waveLayer"));
        //Here again a special case if a layered state transition back to ANY_STATE, if will just fade out to the previous layer.
        manager.findState("waveLayer").transitionTo(ANY_STATE);


        //A chain of states that will go on while currentState is "anim_chain"
        manager.findState(ANY_STATE).interruptTo("walk").when(() -> currentState.equals("anim_chain"));
        manager.findState("walk").transitionTo("jog").when(() -> currentState.equals("anim_chain"));
        manager.findState("jog").transitionTo("run").when(() -> currentState.equals("anim_chain"));
        manager.findState("run").transitionTo("kick").when(() -> currentState.equals("anim_chain")).in(0.5f);
        manager.findState("kick").transitionTo("idle").when(() -> currentState.equals("anim_chain"));


        //Chain
//        manager.findState(ANY_STATE).interruptTo("kick").when(() -> currentState.equals("anim_chain"));
//        manager.findState("kick").transitionTo("idle").when(() -> currentState.equals("anim_chain"));
//        manager.findState("idle").transitionTo("kick").when(() -> currentState.equals("anim_chain"));


//
//        for (int i = 0; i < 50; i++) {
//            Spatial sp = rig.clone();
//            rootNode.attachChild(sp);
//            sp.move(i,0,0);
//        }


    }

    public void setCurrentState(String currentState) {
        this.currentState = currentState;
    }

    private void dumpSceneGraph(Spatial n, String indent) {
        System.err.println(indent + n.toString());
        for (int i = 0; i < n.getNumControls(); i++) {
            System.err.println(indent + "  =>" + n.getControl(i).toString());
        }
        if (n instanceof Node) {
            Node node = (Node) n;
            for (Spatial spatial : node.getChildren()) {
                dumpSceneGraph(spatial, indent + "    ");
            }
        }
    }

    public void setBlendValue(float value) {
        blendSpace.setValue(value);
    }

    private void dumpSkeleton(Skeleton skeleton) {

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
