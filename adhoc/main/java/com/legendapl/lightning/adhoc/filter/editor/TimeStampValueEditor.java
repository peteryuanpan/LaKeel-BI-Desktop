package com.legendapl.lightning.adhoc.filter.editor;

import java.time.LocalDateTime;

import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXTimePicker;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class TimeStampValueEditor extends GeneralEditor {

	private EditorCommon editorCommon = new EditorCommon();
	private JFXDatePicker datePicker;
	private JFXTimePicker timePicker;
	private HBox hBox;
	private VBox vBox;
	private static Text label = new Text();
	private String errorMessage = AdhocUtils.getString(WRONG_DATE_FORMAT);

	static {
		label.wrappingWidthProperty().set(200);
		label.setFill(Color.RED);
	}

	public TimeStampValueEditor(Field field) {
		super(field);
		label.setText(errorMessage);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		datePicker = new JFXDatePicker();
		timePicker = new JFXTimePicker();
		if(filter != null && filter.getValue() != null && !"[NULL]".equals(filter.getValue())) {
			LocalDateTime timeStamp = (LocalDateTime) filter.getValue();
			timePicker.setValue(
					timeStamp.toLocalTime());
			datePicker.setValue(timeStamp.toLocalDate());
		}
		hBox = new HBox(10);
		datePicker.setPrefSize(200, 30);
		timePicker.setPrefSize(200, 30);
		hBox.getChildren().addAll(datePicker, timePicker);
		vBox = new VBox(5);
		vBox.getChildren().add(hBox);
		editorPane.getChildren().add(vBox);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValue(LocalDateTime.of(datePicker.getValue(), timePicker.getValue()));
		filter.setExpress(filter.getLabel() + " " + filter.getOp() +
				" {" + ((LocalDateTime)filter.getValue()).format(AdhocConstants.DataTime.timestampFormatter) + "}");
	}

	@Override
	public boolean checkFilter(Filter filter) {
		vBox.getChildren().remove(label);
		label.setText(errorMessage);
		boolean isTime = false;
		// NULLの場合かつOP=次と等しいと次と等しくない、チェックしない
		if (datePicker.getValue() == null && timePicker.getValue() == null) {
			if (editorCommon.doCheckNull(filter)) {
				return true;
			}
		}
		try {
			LocalDateTime.parse(datePicker.getValue().toString() + " " + timePicker.getValue().toString(),
					AdhocConstants.DataTime.filterTimestampFormatter);
			isTime = true;
		} catch (Exception e) {
			isTime = false;
		}
		if (!isTime) {
			if (!vBox.getChildren().contains(label)) {
				vBox.getChildren().add(label);
			}
			return false;
		}
		// 日付チェック
		String pattern = AdhocConstants.StringDataTime.string_filterTimestampFormatter;
		if (!editorCommon.isValidDate(datePicker.getValue().toString() + " " + timePicker.getValue().toString(), pattern)) {
			label.setText(AdhocUtils.getString(DATE_FORMAT_ERROR));
			vBox.getChildren().add(label);
			return false;
		}
		fillFilter(filter);
		return true;
	}
}
