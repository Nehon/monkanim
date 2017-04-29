package monkanim;

import com.jme3.anim.*;
import com.jme3.anim.blending.LinearBlendSpace;
import com.jme3.anim.interpolator.AnimInterpolators;
import com.jme3.animation.*;
import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.light.*;
import com.jme3.math.*;
import com.jme3.scene.*;
import com.jme3.util.clone.Cloner;

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
        Spatial rig = s.getChild("rig");
        rootNode.addLight(new DirectionalLight(new Vector3f(-1, -1, -1).normalizeLocal()));
        rootNode.addLight(new DirectionalLight(new Vector3f(1, -1, 1).normalizeLocal(),new ColorRGBA(0.5f,0.5f,0.5f,1.0f)));

        // dumpSceneGraph(rootNode, "");


        AnimControl control = rig.getControl(AnimControl.class);

        //create an AnimationManager from an AnimController.
        manager = AnimMigrationUtil.fromAnimControl(control);

        //removing old stuff and adding new stuff
        SkeletonControl skelControl = rig.getControl(SkeletonControl.class);
        Skeleton skeleton = skelControl.getSkeleton();
//System.err.println(skeleton.getBone(0));
        rig.removeControl(AnimControl.class);
        rig.removeControl(SkeletonControl.class);

        rig.addControl(manager);
        rig.addControl(skelControl);

        //creating a layer that affects the left arm.
        //Note that there is a default laylaer that affects the entire skeleton
        manager.addLayer("wave").withMask(SkeletonMask.fromBone(skeleton, "shoulder.L"));

        //state machine
        //creating a state for each animation (optional of course but for the demo
        manager.createState("idle").forAnims("idle");
        manager.createState("walk").forAnims("walk");
        manager.createState("jog").forAnims("jog");
        manager.createState("kick").forAnims("kick");
        manager.createState("run").forAnims("run");
        manager.createState("wave").forAnims("wave");
        manager.createState("walk_poses").forAnims("walk_poses")
                .setTranslationInterpolator(0, AnimInterpolators.CatmullRom)
//                .setRotationInterpolator(0, AnimInterpolators.NLerp)
                //.setTimeInterpolator(0, AnimInterpolators.easeInOutElastic)
                .setLength(1.8f);
//        manager.createState("crouch").forAnims("crouch")
//                //.setTimeInterpolator(0, AnimInterpolators.easeOutBounce)
//                //.setTranslationInterpolator(0, AnimInterpolators.CatmullRom)
//                .setLength(0.5f);
//        manager.createState("stand").forAnims("stand")
//                //.setTranslationInterpolator(0, AnimInterpolators.CatmullRom)
//                //  .setTimeInterpolator(0, AnimInterpolators.easeOutBounce)
//                .setLength(0.5f);

        manager.createState("run_poses").forAnims("run_poses")
                .setTranslationInterpolator(0, AnimInterpolators.CatmullRom)
                //.setRotationInterpolator(0, AnimInterpolators.NLerp)
                .setTimeInterpolator(0, AnimInterpolators.easeOutElastic)
                .setLength(1.8f);

        manager.createState("idle-crouch").forAnims("Idle-crouch");

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
        manager.findState(ANY_STATE).interruptTo("walk_poses").when(() -> currentState.equals("walk_poses"));
        manager.findState(ANY_STATE).interruptTo("run_poses").when(() -> currentState.equals("run_poses"));
        //manager.findState(ANY_STATE).interruptTo("crouch").when(() -> currentState.equals("crouch"));
        manager.findState(ANY_STATE).interruptTo("idle-crouch").when(() -> currentState.equals("idle-crouch"));
        //manager.findState("crouch").transitionTo("idle-crouch");
        //manager.findState(ANY_STATE).interruptTo("stand").when(() -> currentState.equals("stand"));
//        manager.findState("stand").transitionTo("idle");

        //Special case, when the wave state ends, it will return to "anystate", in that case the state from which wave was triggered
        //For example if you go from walk to wave, when wave ends, it will transition back to walk.
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


//        Cloner cloner = new Cloner();
//        Spatial clonedRig = cloner.clone(rig);
//        rootNode.attachChild(clonedRig);
//        manager = clonedRig.getControl(AnimationManager.class);

        rootNode.attachChild(rig);
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
