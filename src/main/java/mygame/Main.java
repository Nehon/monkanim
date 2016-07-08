package mygame;

import com.jme3.animation.*;
import com.jme3.app.*;
import com.jme3.audio.AudioListenerState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.*;
import com.jme3.light.DirectionalLight;
import com.jme3.math.*;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.*;

import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.core.VersionedReference;
import com.simsilica.lemur.style.BaseStyles;
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

    public Main(){
        super(new StatsAppState(),new AudioListenerState(), new DebugKeysAppState(),new GuiAppState(), new AnimAppState());
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