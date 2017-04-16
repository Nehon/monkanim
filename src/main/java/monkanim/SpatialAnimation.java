package monkanim;

import com.jme3.anim.AnimationManager;
import com.jme3.anim.interpolator.AnimInterpolators;
import com.jme3.animation.Anim;
import com.jme3.animation.Animation;
import com.jme3.animation.AnimationFactory;
import com.jme3.app.DebugKeysAppState;
import com.jme3.app.SimpleApplication;
import com.jme3.app.StatsAppState;
import com.jme3.audio.AudioListenerState;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.simsilica.lemur.GuiGlobals;
import com.simsilica.lemur.style.BaseStyles;
import javafx.animation.Interpolator;
import jme3_ext_xbuf.XbufLoader;
import utils.EraseTimer;

/**
 * test
 *
 * @author normenhansen
 */
public class SpatialAnimation extends SimpleApplication {

    public static void main(String[] args) {
        SpatialAnimation app = new SpatialAnimation();
        app.start();
    }



    @Override
    public void simpleInitApp() {
        setTimer(new EraseTimer());

        setPauseOnLostFocus(false);

        Geometry mobile = createGeom(0.2f, ColorRGBA.Cyan);

        AnimationFactory f = new AnimationFactory(0.099f,"Anim",30);
        f.addKeyFrameTranslation(0,new Vector3f(-1, 0, 0));
        f.addKeyFrameTranslation(1,new Vector3f(0, 1.732f, 0));
        f.addKeyFrameTranslation(2,new Vector3f(1, 0, 0));

        Geometry g1 = createGeom(0.1f, ColorRGBA.Red);
        g1.setLocalTranslation(-1,0,0);

        Geometry g2 = createGeom(0.1f, ColorRGBA.Red);
        g2.setLocalTranslation(0, 1.732f, 0);

        Geometry g3 = createGeom(0.1f, ColorRGBA.Red);
        g3.setLocalTranslation(1, 0, 0);

        Animation anim = f.buildAnimation();
        AnimationManager man = new AnimationManager();
        man.addAnimation(anim);

        man.createState("Anim").forAnims("Anim").setTranslationInterpolator(0,AnimInterpolators.CubicVec3f).setLength(5);

        man.startWith("Anim");
        mobile.addControl(man);
    }

    private Geometry createGeom(float size, ColorRGBA color) {
        Geometry geom = new Geometry("mobile",new Box(size,size,size));
        Material m = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        m.setColor("Color", color);
        geom.setMaterial(m);

        rootNode.attachChild(geom);
        return geom;
    }


    @Override
    public void simpleUpdate(float tpf) {

    }

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
}