package test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MenuItemSecondExpand extends Application {

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			AnchorPane anchorPane = new AnchorPane();
			Label label = new Label("TEST");
			label.setPrefSize(100, 100);
			ContextMenu contextMenu = new ContextMenu();
			Menu menu1 = new Menu("1");
			Menu menu2 = new Menu("2");
			Menu menu3 = new Menu("3");
			Menu menu4 = new Menu("4");
			MenuItem menuItemTest1 = new MenuItem("TEST1");
			menu1.getItems().add(menu3);
			menu1.getItems().add(menu4);
			menu3.getItems().add(menuItemTest1);
			contextMenu.getItems().add(menu1);
			contextMenu.getItems().add(menu2);
			label.setContextMenu(contextMenu);
			anchorPane.getChildren().add(label);
			Scene scene = new Scene(anchorPane);
			primaryStage.setScene(scene);
			primaryStage.setTitle("MenuItemSecondExpand");
			primaryStage.setWidth(300);
			primaryStage.setHeight(300);
			primaryStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
}
