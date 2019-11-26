package com.legendapl.lightning.adhoc.factory;

import org.controlsfx.control.PrefixSelectionComboBox;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.service.AdhocLogService;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.ListCell;

public class ComboBoxFactory extends AdhocBaseFactory {
	
	private static PrefixSelectionComboBox<AdhocModelType> viewComboBox;
	private static PrefixSelectionComboBox<AdhocModelType> dataComboBox;
	private static Boolean pushIntoStatementFactory = true;
	
	public static interface ComboBoxSelector {
		void handleActionViewComboBoxSelect(AdhocModelType newValue);
		void handleActionDataComboBoxSelect(AdhocModelType newValue);
	};
	private static ComboBoxSelector comboBoxSelector;
	
	public static void setViewComboBox(PrefixSelectionComboBox<AdhocModelType> viewComboBox) {
		ComboBoxFactory.viewComboBox = viewComboBox;
	}

	public static void setDataComboBox(PrefixSelectionComboBox<AdhocModelType> dataComboBox) {
		ComboBoxFactory.dataComboBox = dataComboBox;
	}

	public static void setComboBoxSelector(ComboBoxSelector comboBoxSelector) {
		ComboBoxFactory.comboBoxSelector = comboBoxSelector;
	}

	public static void init() {
		
		viewComboBox.setButtonCell(new ViewComboBoxButtonCell());
		viewComboBox.setCellFactory(list -> new ViewComboBoxListCell());
		dataComboBox.setButtonCell(new DataComboBoxButtonCell());
		dataComboBox.setCellFactory(listView -> new DataComboBoxListCell());
		
		// TODO : use better way
		viewComboBox.setPrefWidth(100);
		dataComboBox.setPrefWidth(120);
		
		viewComboBox.getItems().add(AdhocModelType.TABLE);
		viewComboBox.getItems().add(AdhocModelType.CROSSTABLE);
		viewComboBox.getSelectionModel().selectedItemProperty().addListener(
				(record, oldValue, newValue) -> handleActionViewComboBoxAddListener(record, oldValue, newValue));
		
		dataComboBox.getItems().add(AdhocModelType.SIMPLEDATA);
		dataComboBox.getItems().add(AdhocModelType.FULLDATA);
		dataComboBox.getSelectionModel().selectedItemProperty().addListener(
				(record, oldValue, newValue) -> handleActionDataComboBoxAddListener(record, oldValue, newValue));
	}

	private static class ViewComboBoxButtonCell extends ListCell<AdhocModelType> {
		@Override public void updateItem(AdhocModelType item, boolean empty) {
			super.updateItem(item, empty);
			this.getStylesheets().setAll("/view/adhocView.css");
			this.getStyleClass().setAll("ComboBoxButtonCell");
			this.setText(ComboBoxFactory.getText(item));
			this.autosize();
		}
	}

	private static class ViewComboBoxListCell extends ListCell<AdhocModelType> {
		@Override public void updateItem(AdhocModelType item, boolean empty) {
			super.updateItem(item, empty);
			this.setText(ComboBoxFactory.getText(item));
			this.autosize();
		}
	}
	
	private static class DataComboBoxButtonCell extends ListCell<AdhocModelType> {
		@Override public void updateItem(AdhocModelType item, boolean empty) {
			super.updateItem(item, empty);
			this.getStylesheets().setAll("/view/adhocView.css");
			this.getStyleClass().setAll("ComboBoxButtonCell");
			this.setText(ComboBoxFactory.getText(item));
			this.autosize();
		}
	}

	private static class DataComboBoxListCell extends ListCell<AdhocModelType> {
		@Override public void updateItem(AdhocModelType item, boolean empty) {
			super.updateItem(item, empty);
			this.setText(ComboBoxFactory.getText(item));
			this.autosize();
		}
	}
	
	private static String getText(AdhocModelType modelType) {
		if (null != modelType) {
			switch (modelType) {
			case CROSSTABLE:
				return AdhocUtils.getString("P121.viewComboBox.crossTable.label");
			case TABLE:
				return AdhocUtils.getString("P121.viewComboBox.table.label");
			case FULLDATA:
				return AdhocUtils.getString("P121.dataComboBox.fullData.label");
			case SIMPLEDATA:
				return AdhocUtils.getString("P121.dataComboBox.simpleData.label");
			default:
				return null;
			}
		}
		return null;
	}

	private static void handleActionViewComboBoxAddListener(
			ObservableValue<? extends AdhocModelType> record, AdhocModelType oldValue, AdhocModelType newValue) {
		handleActionComboBoxSelectValue(viewComboBox, oldValue, newValue);
	}

	private static void handleActionDataComboBoxAddListener(
			ObservableValue<? extends AdhocModelType> record, AdhocModelType oldValue, AdhocModelType newValue) {
		handleActionComboBoxSelectValue(dataComboBox, oldValue, newValue);
	}
	
	private static void handleActionComboBoxSelectValue(
			PrefixSelectionComboBox<AdhocModelType> comboBox, AdhocModelType oldValue, AdhocModelType newValue) {
		
		// push
		if (pushIntoStatementFactory && null != oldValue && null != newValue) {
			StatementFactory.runLater(
					() -> { // todo
						handleActionComboBoxSelectValueImpl(comboBox, oldValue, newValue);
					},
					() -> { // redo
						pushIntoStatementFactory = false;
						comboBox.getSelectionModel().select(newValue);
						handleActionComboBoxSelectValueImpl(comboBox, oldValue, newValue);
					},
					() -> { // undo
						pushIntoStatementFactory = false;
						comboBox.getSelectionModel().select(oldValue);
						handleActionComboBoxSelectValueImpl(comboBox, newValue, oldValue);
					}
			);
		}
	}
	
	private static void handleActionComboBoxSelectValueImpl(
			PrefixSelectionComboBox<AdhocModelType> comboBox, AdhocModelType oldValue, AdhocModelType newValue) {
		
		// log
		if (null != oldValue && null != newValue) {
			AdhocLogService.selectComboBoxValue(oldValue, newValue);
		}
		// handle
		if (comboBox == viewComboBox) comboBoxSelector.handleActionViewComboBoxSelect(newValue);
		else if (comboBox == dataComboBox) comboBoxSelector.handleActionDataComboBoxSelect(newValue);
		pushIntoStatementFactory = true;
	}
	
}
