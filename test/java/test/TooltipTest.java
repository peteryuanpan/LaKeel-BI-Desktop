package test;

import java.lang.reflect.Field;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.util.Duration;

public class TooltipTest extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane anchorPane = new AnchorPane();
			Label label = new Label("TEST");
			Tooltip tooltip = new Tooltip("123");
			hackTooltipStartTiming(tooltip);
	        label.setTooltip(tooltip);
			Platform.runLater(() -> {
				//Scene scene = tooltip.getScene();
				//Pane pane = (Pane) scene.getRoot();
			});
			
			anchorPane.getChildren().add(label);
			Scene scene = new Scene(anchorPane);
			primaryStage.setScene(scene);
			primaryStage.setTitle("ToolTipTest");
			primaryStage.setWidth(300);
			primaryStage.setHeight(300);
			primaryStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	public static void hackTooltipStartTiming(Tooltip tooltip) {
	    try {
	        Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
	        fieldBehavior.setAccessible(true);
	        Object objBehavior = fieldBehavior.get(tooltip);
	        Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
	        fieldTimer.setAccessible(true);
	        Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);
	        objTimer.getKeyFrames().clear();
	        objTimer.getKeyFrames().add(new KeyFrame(new Duration(0)));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
}
