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

public class TimeStampRangeEditor extends GeneralEditor {

	private EditorCommon editorCommon = new EditorCommon();
	private JFXDatePicker datePickerBefore;
	private JFXTimePicker timePickerBefore;
	private HBox hBoxBefore;
	private JFXDatePicker datePickerAfter;
	private JFXTimePicker timePickerAfter;
	private HBox hBoxAfter;
	private VBox vBox;

	private VBox vBoxAfter;
	private VBox vBoxBefore;

	private static Text labelBefore = new Text();
	private static Text labelAfter = new Text();
	private String beforeMessage = AdhocUtils.getString(WRONG_DATE_FORMAT);
	private String afterMessage = AdhocUtils.getString(DATE_RANGE_ERROR);

	static {
		labelBefore.wrappingWidthProperty().set(200);
		labelBefore.setFill(Color.RED);
		labelAfter.wrappingWidthProperty().set(200);
		labelAfter.setFill(Color.RED);
	}

	public TimeStampRangeEditor(Field field) {
		super(field);
		labelBefore.setText(beforeMessage);
		labelAfter.setText(afterMessage);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		datePickerBefore = new JFXDatePicker();
		timePickerBefore = new JFXTimePicker();

		hBoxBefore = new HBox(10);
		datePickerBefore.setPrefSize(200, 30);
		timePickerBefore.setPrefSize(200, 30);
		hBoxBefore.getChildren().addAll(datePickerBefore, timePickerBefore);
		vBoxBefore = new VBox(5);
		vBoxBefore.getChildren().add(hBoxBefore);

		datePickerAfter = new JFXDatePicker();
		timePickerAfter = new JFXTimePicker();
		if(filter != null && filter.getLowValue() != null && filter.getHighValue() != null) {
			LocalDateTime timeStampBefore = (LocalDateTime) filter.getLowValue();
			timePickerBefore.setValue(timeStampBefore.toLocalTime());
			datePickerBefore.setValue(timeStampBefore.toLocalDate());
			LocalDateTime timeStampAfter = (LocalDateTime) filter.getHighValue();
			timePickerAfter.setValue(timeStampAfter.toLocalTime());
			datePickerAfter.setValue(timeStampAfter.toLocalDate());
		}
		hBoxAfter = new HBox(10);
		datePickerAfter.setPrefSize(200, 30);
		timePickerAfter.setPrefSize(200, 30);
		hBoxAfter.getChildren().addAll(datePickerAfter, timePickerAfter);
		vBoxAfter = new VBox(5);
		vBoxAfter.getChildren().add(hBoxAfter);
		vBox = new VBox(10);
		vBox.getChildren().addAll(vBoxBefore, vBoxAfter);
		editorPane.getChildren().add(vBox);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setLowValue(LocalDateTime.of(datePickerBefore.getValue(), timePickerBefore.getValue()));
		filter.setHighValue(LocalDateTime.of(datePickerAfter.getValue(), timePickerAfter.getValue()));
		filter.setExpress(filter.getLabel() + " " + filter.getOp() +
				" {" + ((LocalDateTime)filter.getLowValue()).format(AdhocConstants.DataTime.timestampFormatter) + " and "
				+ ((LocalDateTime)filter.getHighValue()).format(AdhocConstants.DataTime.timestampFormatter) + "}");
	}

	public boolean checkFilter(Filter filter) {
		vBoxBefore.getChildren().remove(labelBefore);
		vBoxAfter.getChildren().remove(labelAfter);
		if (!doFirstDataFormartCheck()) {
			return false;
		}
		if (!doFirstTimeFormartCheck()) {
			return false;
		}		
		if (!doSecondDataFormartCheck()) {
			return false;
		}
		if (!doSecondTimeFormartCheck()) {
			return false;
		}

		try {
			LocalDateTime dateBefore = LocalDateTime.parse(datePickerBefore.getValue().toString() + " " + timePickerBefore.getValue().toString(),
					AdhocConstants.DataTime.filterTimestampFormatter);
			LocalDateTime dateAfter = LocalDateTime.parse(datePickerAfter.getValue().toString() + " " + timePickerAfter.getValue().toString(),
					AdhocConstants.DataTime.filterTimestampFormatter);
			if (!dateAfter.isAfter(dateBefore)) {
				showValidationStyle(AdhocUtils.getString(DATE_RANGE_ERROR), false);
				return false;
			}
		} catch (Exception e) {
			showValidationStyle(AdhocUtils.getString(DATE_FORMAT_ERROR), false);
			return false;
		}
		fillFilter(filter);
		return true;
	};

	protected void showValidationStyle(String errorMessage, Boolean isbeforeFlg) {
		if (isbeforeFlg) {
			labelBefore.setText(errorMessage);
			vBoxBefore.getChildren().add(labelBefore);
		} else {
			labelAfter.setText(errorMessage);
			vBoxAfter.getChildren().add(labelAfter);
		}
	}

	private Boolean doFirstDataFormartCheck() {
		// 日付チェック
		String pattern = AdhocConstants.StringDataTime.string_dateFormat;
		String noData = AdhocUtils.getString(WRONG_DATE_FORMAT);
		String formatError = AdhocUtils.getString(DATE_FORMAT_ERROR);
		if (datePickerBefore.getValue() == null) {
			showValidationStyle(noData, true);
			return false;
		}
		if (!editorCommon.isValidDate(datePickerBefore.getValue().toString(), pattern)) {
			showValidationStyle(formatError, true);
			return false;
		}
		return true;
	}

	private Boolean doFirstTimeFormartCheck() {
		// 時間チェック
		String pattern = AdhocConstants.StringDataTime.string_HHmmTimeFormatter;
		String noData = AdhocUtils.getString(WRONG_DATE_FORMAT);
		String formatError = AdhocUtils.getString(DATE_FORMAT_ERROR);
		if (timePickerBefore.getValue() == null) {
			showValidationStyle(noData, true);
			return false;
		}
		if (!editorCommon.isValidDate(timePickerBefore.getValue().toString(), pattern)) {
			showValidationStyle(formatError, true);
			return false;
		}
		return true;
	}

	private Boolean doSecondDataFormartCheck() {
		// 日付チェック
		String pattern = AdhocConstants.StringDataTime.string_dateFormat;
		String noData = AdhocUtils.getString(WRONG_DATE_FORMAT);
		String formatError = AdhocUtils.getString(DATE_FORMAT_ERROR);
		if (datePickerAfter.getValue() == null) {
			showValidationStyle(noData, false);
			return false;
		}
		if (!editorCommon.isValidDate(datePickerAfter.getValue().toString(), pattern)) {
			showValidationStyle(formatError, false);
			return false;
		}
		return true;
	}

	private Boolean doSecondTimeFormartCheck() {
		// 時間チェック
		String pattern = AdhocConstants.StringDataTime.string_HHmmTimeFormatter;
		String noData = AdhocUtils.getString(WRONG_DATE_FORMAT);
		String formatError = AdhocUtils.getString(DATE_FORMAT_ERROR);
		// 時間チェック
		if (timePickerAfter.getValue() == null) {
			showValidationStyle(noData, false);
			return false;
		}
		if (!editorCommon.isValidDate(timePickerAfter.getValue().toString(), pattern)) {
			showValidationStyle(formatError, false);
			return false;
		}
		return true;
	}
}
