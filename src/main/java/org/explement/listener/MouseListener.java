package org.explement.listener;

import org.explement.controller.PrimaryController;

import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseInputListener;

import javafx.application.Platform;
import javafx.stage.Screen;

public class MouseListener implements NativeMouseInputListener {
	private PrimaryController controller;

	public MouseListener(PrimaryController controller) {
		this.controller = controller;
	}

	public void nativeMouseReleased(NativeMouseEvent e) {
		if (controller.getColorService().isGrabbing()) {
			Platform.runLater(() -> {
				double scaleX = Screen.getPrimary().getOutputScaleX();
				double scaleY = Screen.getPrimary().getOutputScaleY();

				int fxX = (int) Math.round(e.getX() / scaleX);
				int fxY = (int) Math.round(e.getY() / scaleY);

				controller.handleClick(fxX, fxY);
			});
		}
	}

}