package monkanim.compatibility;

import com.jme3.app.*;
import com.jme3.audio.AudioListenerState;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import jme3_ext_xbuf.XbufLoader;
import monkanim.*;

/**
 * This class makes sure the changes doesn't break the old system.
 *
 * @author nnehon
 */
public class OldAnimationSystem extends SimpleApplication {

    public static void main(String[] args) {
        OldAnimationSystem app = new OldAnimationSystem();
        app.start();
    }

    public OldAnimationSystem(){
        super(new StatsAppState(),new AudioListenerState(), new DebugKeysAppState(), new OldAnimAppState());
    }


    @Override
    public void simpleInitApp() {

        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        assetManager.registerLoader(XbufLoader.class, "xbuf");
        getCamera().setLocation(new Vector3f(4.1520014f, 1.6250974f, 1.3038764f));
        getCamera().setRotation(new Quaternion(0.04338501f, -0.8087327f, 0.060133625f, 0.58348364f));
    }



    @Override
    public void simpleUpdate(float tpf) {

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}