package org.explement.controller;

import org.explement.App;
import org.explement.model.ColorModel;
import org.explement.service.ColorService;
import org.explement.utils.ColorType;
import org.explement.utils.SVGUtils;

import javafx.animation.Interpolator;
import javafx.animation.Transition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PrimaryController {
    @FXML 
    private Button grabColorButton, themeToggle, settingsButton;
    @FXML
    private TextField colorLabel;
    @FXML
    private ToggleButton rgbButton, hexButton, hsbButton;
    @FXML
    private ToggleGroup colorType;
    @FXML
    private StackPane colorSwatch;

    private ColorService colorService;
    private Transition colorSwatchAnimation;
    private long lastGrabTime = 0;

    private final ColorAdjust GRABCOLOR_BUTTON_HOVER_EFFECT = new ColorAdjust();
    private final DropShadow DROP_SHADOW = new DropShadow(BlurType.GAUSSIAN, Color.color(0, 0, 0, 0.15), 10, 0, 0, 1);
    private final double COLORSWATCH_ANIMATION_TIME = 90; // * ms

    @FXML
    private void initialize() {
        // * Setup icons
        SVGPath pipetteIcon = SVGUtils.getPipetteIcon();
        grabColorButton.setGraphic(wrapIcon(pipetteIcon, 25, 0.8));
        
        // * Set up hover and dropshadow effect for color swatch
        GRABCOLOR_BUTTON_HOVER_EFFECT.setBrightness(-0.05);
        DROP_SHADOW.setInput(GRABCOLOR_BUTTON_HOVER_EFFECT);
        colorSwatch.setEffect(DROP_SHADOW);

        // * Event handlers for color swatch
        colorSwatch.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
            DROP_SHADOW.setInput(GRABCOLOR_BUTTON_HOVER_EFFECT);
            GRABCOLOR_BUTTON_HOVER_EFFECT.setBrightness(-0.05);
            colorSwatch.setEffect(DROP_SHADOW);
        });

        colorSwatch.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
            DROP_SHADOW.setInput(null);
        });

        /* DISABLED MOUSE ANIMATIONS, NO PICKING COLOR
        colorSwatch.addEventHandler(MouseEvent.MOUSE_PRESSED, e -> {
            DROP_SHADOW.setInput(GRABCOLOR_BUTTON_HOVER_EFFECT);
            GRABCOLOR_BUTTON_HOVER_EFFECT.setBrightness(-0.12);
            colorSwatch.setEffect(DROP_SHADOW);
        });
        
        colorSwatch.addEventHandler(MouseEvent.MOUSE_RELEASED, e -> {
            DROP_SHADOW.setInput(GRABCOLOR_BUTTON_HOVER_EFFECT);
            GRABCOLOR_BUTTON_HOVER_EFFECT.setBrightness(-0.05);
            colorSwatch.setEffect(DROP_SHADOW);
        }); 
        */

        // * Make sure one toggle is always selected
        colorType.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                colorType.selectToggle(oldToggle);
            }
        });
    }

    private StackPane wrapIcon(SVGPath icon, double size, double scale) {
        StackPane wrapper = new StackPane(icon);
        wrapper.setPrefSize(size, size);
        wrapper.setMinSize(size, size);
        wrapper.setMaxSize(size, size);

        icon.setScaleX(scale);
        icon.setScaleY(scale);

        return wrapper;
    }

    @FXML
    public void toggleGrab() {
        colorService.setIsGrabbing(true);
        grabColorButton.setDisable(true);
        // Record the time when grabbing started to prevent immediate triggering
        lastGrabTime = System.currentTimeMillis();
    }

    public void handleClick(int x, int y) {
        if (!colorService.isGrabbing()) return;
       
        Color color = colorService.getColor(x, y);

        colorService.updateColor(color);
        playSound();
        updateUI();
        setStageOnTop();

        colorService.setIsGrabbing(false);
        grabColorButton.setDisable(false);
    }

    @FXML
    public void onRGBButtonActivation() {
        colorService.setColorType(ColorType.RGB);
        updateUI();
    }

    @FXML
    public void onHEXButtonActivation() {
        colorService.setColorType(ColorType.HEX);
        updateUI();
    }

    @FXML
    public void onHSBButtonActivation() {
        colorService.setColorType(ColorType.HSB);
        updateUI();
    }

    public ColorService getColorService() {
        return colorService;
    }

    public long getLastGrabTime() {
        return lastGrabTime;
    }

    public void setColorService(ColorService colorService) {
        this.colorService = colorService;
    }

    private void setStageOnTop() {
        Scene scene = colorLabel.getScene();
        Stage stage = (Stage) scene.getWindow();
        
        Platform.runLater(() -> {
            stage.setAlwaysOnTop(true);
            stage.setAlwaysOnTop(false);
        });
    }

    public void updateUI() {
        ColorModel colorModel = colorService.getColorModel();
        if (colorModel == null) {
            colorLabel.setText("rgba(255, 255, 255)");
            setSwatchColor(Color.WHITE, colorSwatch);
            colorService.setColorModel(new ColorModel(Color.WHITE, ColorType.RGB));
            return;
        }
        // System.out.println(colorModel.getColor());
        colorLabel.setText(colorModel.getColorCode());
        animateSwatchColor(colorSwatch, colorModel.getColor(), COLORSWATCH_ANIMATION_TIME);
    
    }

    public void setSwatchColor(Color color, StackPane colorSwatch) {
        colorSwatch.setBackground(
            new Background(
                new BackgroundFill(
                    color,
                    new CornerRadii(8),
                    Insets.EMPTY
                )
            )
        );
    }

    public void animateSwatchColor(StackPane colorSwatch, Color targetColor, double durationMillis) {
        Color startColor = Color.WHITE;
        if (colorSwatch.getBackground() != null && !colorSwatch.getBackground().getFills().isEmpty()) {
            var fill = colorSwatch.getBackground().getFills().get(0).getFill();
            if (fill instanceof Color c) {
                startColor = c;
            }
        }

        final Color finalStartColor = startColor;

        if (colorSwatchAnimation != null) {
            colorSwatchAnimation.stop();
        }
        
        colorSwatchAnimation = new Transition() {
            {
                setCycleDuration(Duration.millis(durationMillis));
                setInterpolator(Interpolator.EASE_BOTH);
            }

            @Override
            protected void interpolate(double frac) {
                Color interpolated = finalStartColor.interpolate(targetColor, frac);
                colorSwatch.setBackground(new Background(
                    new BackgroundFill(interpolated, new CornerRadii(8), Insets.EMPTY)
                ));
            }
        };

        colorSwatchAnimation.play();
    }

    private void playSound() {
        Media media = new Media(App.class.getResource("/org/explement/snip.mp3").toExternalForm());
        MediaPlayer mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(.4);
        mediaPlayer.play();
    }

}