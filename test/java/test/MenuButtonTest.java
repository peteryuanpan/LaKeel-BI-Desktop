package test;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MenuButtonTest extends Application {

    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
		try {
			SplitMenuButton splitMenuButton = new SplitMenuButton();
			splitMenuButton.setText("");
			FontAwesomeIconView ICON_SAVE = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
			ICON_SAVE.setGlyphSize(20);
			splitMenuButton.setGraphic(ICON_SAVE);
			MenuItem menuItem1 = new MenuItem("アドホックビューの保存");
			MenuItem menuItem2 = new MenuItem("アドホックビューに名前を付けて保存");
			splitMenuButton.getItems().add(menuItem1);
			splitMenuButton.getItems().add(menuItem2);
			splitMenuButton.getGraphic().setOnMouseClicked(event -> {
				System.out.println("Click graphic.");
				Node parent = splitMenuButton.getGraphic().getParent();
				if (null == parent) {
					System.out.println("Parent is null.");
				} else {
					System.out.println(parent.getClass().getName());
					parent.setOnMouseClicked(event2 -> {
						System.out.println("Click graphic parent.");
					});
				}
			});
			splitMenuButton.setOnMouseClicked(event -> {
				Object obj = event.getSource();
				System.out.println(obj.getClass().getName());
			});
			
			MenuButton menuButton = new MenuButton();
			menuButton.setText("");
			FontAwesomeIconView ICON_SAVE2 = new FontAwesomeIconView(FontAwesomeIcon.SAVE);
			ICON_SAVE2.setGlyphSize(20);
			menuButton.setGraphic(ICON_SAVE2);
			MenuItem menuItem11 = new MenuItem("アドホックビューの保存");
			MenuItem menuItem22 = new MenuItem("アドホックビューに名前を付けて保存");
			menuButton.getItems().add(menuItem11);
			menuButton.getItems().add(menuItem22);
			
			AnchorPane anchorPane = new AnchorPane();
			anchorPane.getChildren().add(splitMenuButton);
			AnchorPane.setLeftAnchor(splitMenuButton, 10.0);
			AnchorPane.setTopAnchor(splitMenuButton, 10.0);
			anchorPane.getChildren().add(menuButton);
			AnchorPane.setRightAnchor(menuButton, 10.0);
			AnchorPane.setTopAnchor(menuButton, 10.0);
			
			Scene scene = new Scene(anchorPane);
			primaryStage.setScene(scene);
			primaryStage.setTitle("MenuButtonTest");
			primaryStage.setWidth(300);
			primaryStage.setHeight(300);
			primaryStage.show();
			
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
    }
	
}
