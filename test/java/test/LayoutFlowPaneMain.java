package test;

import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class LayoutFlowPaneMain extends Application {

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		try {
			LayoutFlowPane flowPane = new LayoutFlowPane();
			flowPane.getStylesheets().setAll("/test/LayoutFlowPane.css");
			flowPane.getStyleClass().setAll("flow-pane");
			
			flowPane.setOnMouseClicked(event -> {
				return;
			});
			
			for (int i = 0; i < 20; i ++) {
				Label label = new Label("A");
				label.getStylesheets().setAll("/test/LayoutFlowPane.css");
				label.getStyleClass().setAll("flow-pane-child-field-label");
				flowPane.getChildren().add(label);
			}
			
			for (int i = 0; i < 20; i ++) {
				Label label = new Label("B");
				label.getStylesheets().setAll("/test/LayoutFlowPane.css");
				label.getStyleClass().setAll("flow-pane-child-measure-label");
				GridPane gridPane = new GridPane();
				gridPane.getStylesheets().setAll("/test/LayoutFlowPane.css");
				gridPane.getStyleClass().setAll("gridPane");
				gridPane.getChildren().add(label);
				flowPane.getChildren().add(gridPane);
			}
			
			for (int i = 0; i < 20; i ++) {
				Label label = new Label("A");
				label.getStylesheets().setAll("/test/LayoutFlowPane.css");
				label.getStyleClass().setAll("flow-pane-child-field-label");
				flowPane.getChildren().add(label);
			}
			
			HBox hbox = new HBox();
			hbox.getStylesheets().setAll("/test/LayoutFlowPane.css");
			hbox.getStyleClass().setAll("column-row-container");
			hbox.getChildren().add(flowPane);
			flowPane.prefWidthProperty().bind(hbox.widthProperty());
			
			AnchorPane anchorPane = new AnchorPane();
			anchorPane.getChildren().add(hbox);
			AnchorPane.setTopAnchor(hbox, 0.0);
			AnchorPane.setBottomAnchor(hbox, 0.0);
			AnchorPane.setLeftAnchor(hbox, 0.0);
			AnchorPane.setRightAnchor(hbox, 0.0);
			
			Scene scene = new Scene(anchorPane);
			primaryStage.setScene(scene);
			primaryStage.setTitle("layoutFlowPaneTest");
			primaryStage.setWidth(500);
			primaryStage.setHeight(300);
			primaryStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
}
