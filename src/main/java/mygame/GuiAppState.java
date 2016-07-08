package mygame;

import com.jme3.app.*;
import com.jme3.app.state.BaseAppState;
import com.jme3.math.Vector3f;
import com.simsilica.lemur.*;
import com.simsilica.lemur.core.*;

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
        Button walkButton = buttonContainer.addChild(new Button("Walk"));
        walkButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
                setAnim("walk");
            }
        });
        walkButton = buttonContainer.addChild(new Button("Run"));
        walkButton.addClickCommands(new Command<Button>() {
            @Override
            public void execute( Button source ) {
                setAnim("run");
            }
        });
        walkButton = buttonContainer.addChild(new Button("Both"));
        walkButton.addClickCommands(new Command<Button>() {
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
        float value = animState.getManager().getActiveSequence().getValue();
        if(anim.equals("Move")){
            animLabel.setText("walk: " + String.format("%.0f", (1 - value) * 100f) + "%, run: " + String.format("%.0f", value * 100f) + "%" );
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
