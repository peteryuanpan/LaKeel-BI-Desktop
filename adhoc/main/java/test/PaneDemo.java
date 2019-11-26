package test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.Queue;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.sun.javafx.scene.control.skin.TableViewSkin;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PaneDemo extends Application {

	private TableView<String> table = new TableView<>();
	private Long timeStart;
	private Long timeEnd;
	private static ObservableList<TableColumn<String, ?>> lastLayer = FXCollections.observableArrayList();

	public static void main(String[] args) {
		launch(args);
	}

	private static int getTotal(int layer) {
		int total = 0;
		int start = 1;
		for (int i = 0; i < layer - 1; i++) {
			start *= 5;
			total += start;
		}
		System.out.println("Total Columns: " + (total + start * 5));
		return total;
	}

	@Override
	public void start(Stage stage) {

		Scene scene = new Scene(new Group());
		AnchorPane anchorPane = new AnchorPane();
		VBox vbox = new VBox();
		anchorPane.setPrefWidth(900);
		anchorPane.setPrefHeight(600);
		table.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
		table.getStylesheets().setAll("/view/crossTable.css");
		timeStart = System.currentTimeMillis();
		// init first layer
		TableColumn<String, String> Test1 = new TableColumn<>("test11111111111111111111");
		TableColumn<String, String> Test2 = new TableColumn<>("Test2");
		TableColumn<String, String> Test3 = new TableColumn<>("Test3");
		TableColumn<String, String> Test4 = new TableColumn<>("Test4");
		TableColumn<String, String> Test5 = new TableColumn<>("Test5");

		Queue<TableColumn<String, ?>> queue = new LinkedList<>();
		table.getColumns().addAll(Test1, Test2, Test3, Test4, Test5);
		table.getItems().add("test");
		queue.addAll(table.getColumns());

		int index = 0;
		// set the layer of the column tower
		int temp = getTotal(2);
		while (index < temp) {
			TableColumn<String, ?> root = queue.poll();
			TableColumn<String, String> test1 = new TableColumn<>("test11111111111111111111");
			TableColumn<String, String> test2 = new TableColumn<>("test2");
			TableColumn<String, String> test3 = new TableColumn<>("test3");
			TableColumn<String, String> test4 = new TableColumn<>("test4");
			TableColumn<String, String> test5 = new TableColumn<>("test5");

			root.getColumns().addAll(test1, test2, test3, test4, test5);
			lastLayer.addAll(test1, test2, test3, test4, test5);
			// root.prefWidthProperty().bind(test1.widthProperty().add(test2.widthProperty()).add(test3.widthProperty()).add(test4.widthProperty()).add(test5.widthProperty()));
			queue.addAll(root.getColumns());
			index++;
		}

		while (!queue.isEmpty()) {
			generateCellFactory((TableColumn<String, String>) queue.poll());
		}

		vbox.prefHeightProperty().bind(anchorPane.heightProperty());
		vbox.prefWidthProperty().bind(anchorPane.widthProperty());
		table.getItems().add("豚汁うどんと小まぐろ丼ｾｯﾄ");
//		table.getItems().add("tetstestwetsdtsedtwet");
//		table.getItems().add("tetstestwetsdtsedtwet");
//		table.getItems().add("tetstestwetsdtsedtwet");
//		table.getItems().add("tetstestwetsdtsedtwet");
//		table.getItems().add("tetstestwetsdtsedtwet");
//		table.getItems().add("tetstestwetsdtsedtwet");
		GUIUtils.autoFitTable(table);
		anchorPane.getChildren().add(vbox);
		Button button = new Button("Add a item");
		vbox.getChildren().add(button);
		vbox.getChildren().add(table);
		button.setOnAction(event -> {
			table.getItems().add("tetstestwetsdtsedtwet");
		});
		((Group) scene.getRoot()).getChildren().addAll(anchorPane);
		stage.setScene(scene);
		stage.show();

		Platform.runLater(() -> {
			TableColumn tes = new TableColumn<>();
			table.getColumns().add(tes);
			timeEnd = System.currentTimeMillis();
			System.out.println("Layout Time: " + (timeEnd - timeStart) + "ms");
		});
	}

	private <T> void generateCellFactory(TableColumn<T, String> column) {
		column.setCellFactory(cell -> {
			return new TableCell<T, String>() {
				@Override
				protected void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					if(getIndex() < table.getItems().size() && getIndex() >= 0)
						setText(table.getItems().get(getIndex()));
				}

			};
		});
	}

	public static class GUIUtils {
	    private static Method columnToFitMethod;

	    static {
	        try {
	            columnToFitMethod = TableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent", TableColumn.class, int.class);
	            columnToFitMethod.setAccessible(true);
	        } catch (NoSuchMethodException e) {
	            e.printStackTrace();
	        }
	    }

	    public static void autoFitTable(TableView<String> tableView) {
	        tableView.getItems().addListener(new ListChangeListener<Object>() {
	            @Override
	            public void onChanged(Change<?> c) {
	            	lastLayer.forEach(column -> {
	            		 try {
		                        columnToFitMethod.invoke(tableView.getSkin(), column, -1);
		                        Platform.runLater(() -> {
		                        	System.out.println(column.getWidth());
		                        });
		                    } catch (IllegalAccessException | InvocationTargetException e) {
		                        e.printStackTrace();
		                    }
	            	});

	            }
	        });
	    }
	}
}