package monkanim.compatibility;

import com.jme3.anim.*;
import com.jme3.animation.*;
import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.scene.*;

/**
 * Created by Nehon on 08/07/2016.
 */
public class OldAnimAppState extends BaseAppState {

    public static final float BLEND_TIME = 0.3f;

    @Override
    protected void initialize(Application app) {
        Node s = (Node)app.getAssetManager().loadModel("Models/puppet.xbuf");
        Node rootNode = ((SimpleApplication)app).getRootNode();
        rootNode.attachChild(s.getChild("rig"));
        rootNode.addLight(new DirectionalLight(new Vector3f(-1,-1,-1).normalizeLocal()));

        Spatial rig = rootNode.getChild("rig");
        AnimControl control = rig.getControl(AnimControl.class);
        AnimChannel channel = control.createChannel();
        channel.setAnim("walk");
        channel.setSpeed(1);
        channel.setLoopMode(LoopMode.DontLoop);

        control.addListener(new AnimEventListener() {
            @Override
            public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
                if(animName.equals("walk")){
                    channel.setAnim("jog", BLEND_TIME);

                    channel.setLoopMode(LoopMode.DontLoop);
                    return;
                }
                if(animName.equals("jog")){
                    channel.setAnim("run", BLEND_TIME);

                    channel.setLoopMode(LoopMode.DontLoop);
                    return;
                }
                if(animName.equals("run")){
                    channel.setAnim("kick", BLEND_TIME);
                    channel.setLoopMode(LoopMode.DontLoop);
                    return;
                }
                if(animName.equals("kick")){
                    channel.setAnim("walk", BLEND_TIME);
                    channel.setLoopMode(LoopMode.DontLoop);
                    return;
                }
            }

            @Override
            public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {

            }
        });


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
    protected void cleanup(Application app) {

    }

    @Override
    protected void onEnable() {

    }

    @Override
    protected void onDisable() {

    }
}
