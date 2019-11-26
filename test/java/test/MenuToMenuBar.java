package test;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.stage.Stage;

public class MenuToMenuBar extends Application {
	
    public static void main(String args[]) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        MenuBar bar = new MenuBar();
        stage.setScene(new Scene(bar));
        Menu menu = new Menu("Foo");

        MenuItem menuItem = new MenuItem("Baz");
        menu.getItems().add(menuItem);
        bar.getMenus().add(menu);

        // put a reference back to MenuBar in each Menu
        for (Menu each : bar.getMenus()) {
            each.getProperties().put(MenuBar.class.getCanonicalName(), bar);
        }

        menuItem.setOnAction((e) -> {
            // retrieve the MenuBar reference later...
            System.out.println(menuItem.getParentMenu().getProperties().get(MenuBar.class.getCanonicalName()));
        });
        stage.show();
    }
    
}