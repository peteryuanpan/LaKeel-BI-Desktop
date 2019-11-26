package com.legendapl.lightning.adhoc.filter.editor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class MultipleValuesForBooleanEditor extends GeneralEditor {

	private FilteredList<String> filteredData;
	private ListView<String> leftList;
	private ListView<String> rightList;
	ObservableList<String> unselectedData;
	ObservableList<String> selectedData;
	private TextField textField;
	private VBox vBox;
	private HBox hBox;
	private VBox vBoxRight;
	private Label leftTitle;
	private Label rightTitle;
	private Text errorLabel;
	private Field field;
	private EditorCommon editorCommon = new EditorCommon();

	public MultipleValuesForBooleanEditor(Field field) {
		super(field);
		this.field = field;
		leftTitle = new Label(AdhocUtils.getString("P112.filter.mutiple.left.title"));
		rightTitle = new Label(AdhocUtils.getString("P112.filter.mutiple.right.title"));
		errorLabel = new Text(AdhocUtils.getString(VALUE_SHOULD_BE_NOT_EMPTY));
		errorLabel.wrappingWidthProperty().set(200);
		errorLabel.setFill(Color.RED);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		initData(filter);
		textField.setPrefSize(150, 30);
		vBox = new VBox(5);
		vBox.getChildren().addAll(leftTitle, textField, leftList);
		vBoxRight = new VBox(5);
		vBoxRight.getChildren().addAll(rightTitle, rightList);
		hBox = new HBox(10);
		hBox.getChildren().addAll(vBox, vBoxRight);
		editorPane.getChildren().add(hBox);
	}

	private void initData(Filter filter) {
		List<String> distinctList = dbService.getDistinctList(field);

		if(distinctList == null)
			return;

		List<String> nullList = new ArrayList<String>();
		editorCommon.changeBlank(distinctList);
		// 1が「TRUE」に変更
		if (distinctList.contains("1")) {
			nullList.add("1");
			distinctList.removeAll(nullList);
			distinctList.add("true");
		}
		// 0が「TRUE」に変更
		if (distinctList.contains("0")) {
			nullList.add("0");
			distinctList.removeAll(nullList);
			distinctList.add("false");
		}
		unselectedData = FXCollections.observableArrayList(distinctList);
		selectedData = FXCollections.observableArrayList();
		if (filter != null && filter.getValues() != null) {
			List<String> selectedDataSaved = new ArrayList<String>();
			selectedDataSaved.addAll(filter.getValues());
			selectedData.addAll(selectedDataSaved);
			unselectedData.removeAll(selectedDataSaved);
		}
		Collections.sort(unselectedData);
		Collections.sort(selectedData);
		filteredData = new FilteredList<String>(unselectedData, p -> true);
		leftList = new ListView<>();
		rightList = new ListView<>();
		textField = new TextField();
		leftList.setPrefSize(150, 150);
		rightList.setPrefSize(150, 200);

		textField.textProperty().addListener((observable, oldValue, newValue) -> {
			filteredData.setPredicate(text -> {
				if (newValue == null || newValue.isEmpty()) {
					return true;
				}

				String lowerCaseFilter = newValue.toLowerCase();

				if (text.toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
				return false;
			});
		});
		leftList.setItems(filteredData);
		rightList.setItems(selectedData);
		leftList.setCellFactory(lv -> {
			ListCell<String> cell = new ListCell<String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
				}
			};
			cell.setOnMouseClicked(event -> {
				String item = cell.getItem();
				if (item != null) {
					unselectedData.remove(item);
					selectedData.add(item);
					Collections.sort(selectedData);
					leftList.getSelectionModel().clearSelection();
				}
			});
			return cell;
		});

		rightList.setCellFactory(lv -> {
			ListCell<String> cell = new ListCell<String>() {
				@Override
				public void updateItem(String item, boolean empty) {
					super.updateItem(item, empty);
					setText(item);
				}
			};
			cell.setOnMouseClicked(event -> {
				String item = cell.getItem();
				if (item != null) {
					selectedData.remove(item);
					unselectedData.add(item);
					Collections.sort(unselectedData);
					rightList.getSelectionModel().clearSelection();
				}
			});
			return cell;
		});
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValues(selectedData);
		//　Dekstopに表示するため
		String values = "";
		// SQLを生成するため
		String value = "";
		for (String obj : filter.getValues()) {
			values += obj+ ", ";
			value += "'" + obj + "', ";
		}
		values = values.substring(0, values.length() - 2);
		value = value.substring(0, value.length() - 2);
		filter.setHighValue(value);
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + values + "}");
	}

	@Override
	public boolean checkFilter(Filter filter) {
		vBoxRight.getChildren().remove(errorLabel);
		if (selectedData.isEmpty() && !vBoxRight.getChildren().contains(errorLabel)) {
			rightList.setStyle("-fx-background-color: #e6524c");
			vBoxRight.getChildren().add(errorLabel);
			return false;
		} else {
			fillFilter(filter);
			return true;
		}
	}
}
