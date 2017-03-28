package monkanim;

import com.jme3.anim.*;
import com.jme3.anim.blending.*;
import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.core.*;

import java.util.List;

/**
 * Created by Nehon on 08/07/2016.
 */
public class GuiAppState extends BaseAppState {

    Label speedLabel;
    Label globalSpeedLabel;
    Label animLabel;
    VersionedReference<Double> speedRef;
    VersionedReference<Double> globalSpeedRef;


    @Override
    protected void initialize(Application app) {


        // Create a simple container for our elements
        Container myWindow = new Container();
        ((SimpleApplication) app).getGuiNode().attachChild(myWindow);
        myWindow.setPreferredSize(new Vector3f(200, 70, 1));

        // Put it somewhere that we will see it.
        // Note: Lemur GUI elements grow down from the upper left corner.
        myWindow.setLocalTranslation(app.getCamera().getWidth() / 2 - 100, 100, 0);

        // Add some elements
        speedLabel = new Label("Speed: " + String.format("%.2f", 1f));
        animLabel = new Label("walk");
        myWindow.addChild(animLabel);
        myWindow.addChild(speedLabel);
        Slider slider = new Slider(new DefaultRangedValueModel(1, 3, 1), Axis.X);
        slider.setDelta(0.03f);
        myWindow.addChild(slider);
        speedRef = slider.getModel().createReference();


        Container buttonContainer = new Container();
        ((SimpleApplication) app).getGuiNode().attachChild(buttonContainer);
        buttonContainer.setLocalTranslation(100, 500, 0);

        AnimAppState animState = getState(AnimAppState.class);
        for (AnimState state : animState.getManager().getStates()) {
            Button button = buttonContainer.addChild(new Button(state.getName()));
            button.addClickCommands((Button source) -> setAnim(state.getName()));
        }
        Button button = buttonContainer.addChild(new Button("anim chain"));
        button.addClickCommands((Button source) -> setAnim("anim_chain"));

        // TopWindow
        Container topWindow = new Container();
        ((SimpleApplication) app).getGuiNode().attachChild(topWindow);
        topWindow.setPreferredSize(new Vector3f(200, 70, 1));
        topWindow.setLocalTranslation(app.getCamera().getWidth() / 2 - 100, 700, 0);

        topWindow.addChild(new Label("Global speed"));
        globalSpeedLabel = new Label("1.0");
        topWindow.addChild(globalSpeedLabel);
        Slider topSlider = new Slider(new DefaultRangedValueModel(0, 5, 1), Axis.X);
        topSlider.setDelta(0.1f);
        topWindow.addChild(topSlider);
        globalSpeedRef = topSlider.getModel().createReference();
    }

    private void setAnim(String anim) {
        AnimAppState animState = getState(AnimAppState.class);
        animState.setCurrentState(anim);

        animState.getManager().getLayers().forEach((name, layer) -> {
            if (layer.getActiveState() != null) {
                layer.getActiveState().setSpeed(speedRef.get().floatValue());
            }
        });

        setAnimLabel(animState, anim);
    }

    @Override
    public void update(float tpf) {

        if (speedRef.needsUpdate()) {
            speedLabel.setText("Speed: " + String.format("%.2f", speedRef.get()));
            AnimAppState animState = getState(AnimAppState.class);
            animState.getManager().getLayers().forEach((name, layer) -> {
                if (layer.getActiveState() != null) {
                    layer.getActiveState().setSpeed(speedRef.get().floatValue());
                }
            });
            animState.setBlendValue((speedRef.get().floatValue() - 1) / 2f);
            StringBuilder anim = new StringBuilder();
            animState.getManager().getLayers().forEach((name, layer) -> {
                if (layer.getActiveState() != null) {
                    anim.append(layer.getActiveState().getName()).append(" ");
                }
            });
            setAnimLabel(animState, anim.toString());
            speedRef.update();
        }

        if(globalSpeedRef.needsUpdate()){
            AnimAppState animState = getState(AnimAppState.class);
            animState.getManager().setGlobalSpeed(globalSpeedRef.get().floatValue());
            globalSpeedLabel.setText( String.format("%.2f", globalSpeedRef.get()));
            globalSpeedRef.update();
        }
    }

    private void setAnimLabel(AnimAppState animState, String anim) {
        animState.getManager().update(0);
        List<AnimationData> anims = animState.getManager().getDebugWeightedAnims();
        StringBuilder builder = new StringBuilder();
        for (AnimationData bData : anims) {
            builder.append(bData.getAnimation().getName()).append(": ").append(String.format("%.0f", bData.getWeight() * 100f)).append("%, ");
        }
        animLabel.setText(builder.toString());
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
