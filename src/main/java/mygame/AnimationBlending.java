package mygame;

import com.jme3.app.*;
import com.jme3.audio.AudioListenerState;
import com.jme3.renderer.RenderManager;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import jme3_ext_xbuf.XbufLoader;

/**
 * test
 *
 * @author normenhansen
 */
public class AnimationBlending extends SimpleApplication {




    public static void main(String[] args) {
        AnimationBlending app = new AnimationBlending();
        app.start();
    }

    public AnimationBlending(){
        super(new StatsAppState(),new AudioListenerState(), new DebugKeysAppState(), new AnimAppState(), new GuiAppState());
    }


    @Override
    public void simpleInitApp() {

        GuiGlobals.initialize(this);
        BaseStyles.loadGlassStyle();
        GuiGlobals.getInstance().getStyles().setDefaultStyle("glass");

        assetManager.registerLoader(XbufLoader.class, "xbuf");

    }



    @Override
    public void simpleUpdate(float tpf) {

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}