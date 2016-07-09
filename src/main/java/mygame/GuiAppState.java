package mygame;

import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.core.*;

import java.util.LinkedHashMap;

/**
 * Created by Nehon on 08/07/2016.
 */
public class GuiAppState extends BaseAppState {

    Label speedLabel;
    Label animLabel;
    VersionedReference<Double> speedRef ;

    @Override
    protected void initialize(Application app) {


        // Create a simple container for our elements
        Container myWindow = new Container();
        ((SimpleApplication)app).getGuiNode().attachChild(myWindow);
        myWindow.setPreferredSize(new Vector3f(200,70,1));

        // Put it somewhere that we will see it.
        // Note: Lemur GUI elements grow down from the upper left corner.
        myWindow.setLocalTranslation(app.getCamera().getWidth() / 2 - 100, 100, 0);

        // Add some elements
        speedLabel = new Label("Speed: " + String.format("%.2f", 1f));
        animLabel = new Label("walk");
        myWindow.addChild(animLabel);
        myWindow.addChild(speedLabel);
        Slider slider = new Slider(new DefaultRangedValueModel(1,3,1),Axis.X);
        slider.setDelta(0.03f);
        myWindow.addChild(slider);
        speedRef = slider.getModel().createReference();


        Container buttonContainer = new Container();
        ((SimpleApplication)app).getGuiNode().attachChild(buttonContainer);
        buttonContainer.setLocalTranslation(300, 500, 0);
        Button button = buttonContainer.addChild(new Button("Walk"));
        button.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
                setAnim("walk");
            }
        });
        button = buttonContainer.addChild(new Button("Jog"));
        button.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
                setAnim("jog");
            }
        });

        button = buttonContainer.addChild(new Button("Run"));
        button.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
                setAnim("run");
            }
        });


        button = buttonContainer.addChild(new Button("Mix all"));
        button.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
                setAnim("Move");
            }
        });

    }

    private void setAnim(String anim) {
        AnimAppState animState = getState(AnimAppState.class);
        animState.getManager().setActiveSequence(anim);

        animState.getManager().getActiveSequence().setSpeed(speedRef.get().floatValue());
        animState.getManager().getActiveSequence().setValue((speedRef.get().floatValue() - 1) / 2f);

        setAnimLabel(animState, anim);

    }

    @Override
    public void update(float tpf) {
        if(speedRef.needsUpdate()){
            speedLabel.setText("Speed: " + String.format("%.2f",speedRef.get()));
            AnimAppState animState = getState(AnimAppState.class);
            animState.getManager().getActiveSequence().setSpeed(speedRef.get().floatValue());
            animState.getManager().getActiveSequence().setValue((speedRef.get().floatValue() - 1) / 2f);
            String anim = animState.getManager().getActiveSequence().getName();
            setAnimLabel(animState, anim);
            speedRef.update();
        }
    }

    private void setAnimLabel(AnimAppState animState, String anim) {
        if(anim.equals("Move")){
            LinkedHashMap<String, Float> map = animState.getManager().getActiveSequence().flatten(0);
            StringBuilder builder = new StringBuilder();
            for (String key : map.keySet()) {
                builder.append(key).append(": ").append( String.format("%.0f", map.get(key) * 100f)).append("%, ");
            }
            animLabel.setText(builder.toString());
        } else {
            animLabel.setText(anim + ": 100%");
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
