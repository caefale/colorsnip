package org.explement;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

import org.explement.controller.PrimaryController;
import org.explement.listener.KeyListener;
import org.explement.listener.MouseListener;
import org.explement.service.ColorService;
import org.explement.service.impl.DefaultClipboardHandlerImpl;
import org.explement.service.impl.DefaultColorPickerImpl;

import com.github.kwhat.jnativehook.GlobalScreen;
import com.github.kwhat.jnativehook.NativeHookException;

/**
 * JavaFX App
 */
public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        registerNativeHook();

        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/org/explement/primary.fxml"));
        Parent root = fxmlLoader.load();
        PrimaryController controller = fxmlLoader.getController();

        // * Create services
        ColorService colorService = createColorService();
        controller.setColorService(colorService);
        // * Register listeners for service updates
        colorService.registerColorUpdateListener(() -> controller.updateUI());
        
        // Update UI AFTER creating the ColorService // ? (can't be called in initialize)
        controller.updateUI();

        stage.setTitle("ColorSnip");
        stage.getIcons().add(new Image(App.class.getResourceAsStream("/org/explement/icon.png")));  // * Use png for running Java, .ico for jpackage
        scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/org/explement/styles.css").toExternalForm());
        stage.setScene(scene);
        stage.show();

        MouseListener mouseListener = new MouseListener(controller);
        KeyListener keyListener = new KeyListener(controller);

        GlobalScreen.addNativeKeyListener(keyListener);
        GlobalScreen.addNativeMouseListener(mouseListener);

        stage.setOnCloseRequest(event -> {
            try {
                GlobalScreen.unregisterNativeHook();
            } catch (NativeHookException e) {
                e.printStackTrace();
            }
        });
    }

    static void setRoot(String fxml) throws IOException {
        scene.setRoot(loadFXML(fxml));
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource(fxml + ".fxml"));
        return fxmlLoader.load();
    }

    private void registerNativeHook() {
        try {
            GlobalScreen.registerNativeHook();
        } catch (NativeHookException ex) {
            System.err.println("Error registering native hook: " + ex.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        launch();
    }

    private ColorService createColorService() {
        DefaultColorPickerImpl colorPicker = new DefaultColorPickerImpl();
        DefaultClipboardHandlerImpl clipboardHandler = new DefaultClipboardHandlerImpl();
        return new ColorService(colorPicker, clipboardHandler);
    }

}