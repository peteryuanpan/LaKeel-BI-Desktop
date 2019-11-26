package com.legendapl.lightning.adhoc.filter.editor;

import java.time.LocalTime;

import com.jfoenix.controls.JFXTimePicker;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class TimeRangeEditor extends GeneralEditor {

	private EditorCommon editorCommon = new EditorCommon();
	private JFXTimePicker timePickerBefore;
	private VBox vBoxBefore;
	private JFXTimePicker timePickerAfter;
	private VBox vBoxAfter;
	private VBox vBox;

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

	public TimeRangeEditor(Field field) {
		super(field);
		isRange = true;
		labelBefore.setText(beforeMessage);
		labelAfter.setText(afterMessage);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		timePickerBefore = new JFXTimePicker();
		timePickerBefore.setIs24HourView(true);
		vBoxBefore = new VBox(5);
		vBoxBefore.getChildren().add(timePickerBefore);
		timePickerAfter = new JFXTimePicker();
		timePickerAfter.setIs24HourView(true);
		vBoxAfter = new VBox(5);
		vBoxAfter.getChildren().add(timePickerAfter);
		if (filter != null && filter.getLowValue() != null && filter.getHighValue() != null) {
			timePickerBefore.setValue((LocalTime) filter.getLowValue());
			timePickerAfter.setValue((LocalTime) filter.getHighValue());
		}
		vBox = new VBox(10);
		timePickerBefore.setPrefSize(200, 30);
		timePickerAfter.setPrefSize(200, 30);
		vBox.getChildren().addAll(vBoxBefore, vBoxAfter);
		editorPane.getChildren().add(vBox);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setLowValue(timePickerBefore.getValue());
		filter.setHighValue(timePickerAfter.getValue());
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {"
				+ timePickerBefore.getValue().format(AdhocConstants.DataTime.HHmmTimeFormatter) + " and "
				+ timePickerAfter.getValue().format(AdhocConstants.DataTime.HHmmTimeFormatter) + "}");
	}

	private Boolean doTimeFormartCheck() {
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

	public boolean checkFilter(Filter filter) {
		vBoxBefore.getChildren().remove(labelBefore);
		vBoxAfter.getChildren().remove(labelAfter);
		if (!doTimeFormartCheck()) {
			return false;
		}
		try {
			LocalTime timeBefore = LocalTime.parse(timePickerBefore.getValue().toString(),
					AdhocConstants.DataTime.HHmmTimeFormatter);
			LocalTime timeAfter = LocalTime.parse(timePickerAfter.getValue().toString(),
					AdhocConstants.DataTime.HHmmTimeFormatter);
			if (!timeAfter.isAfter(timeBefore)) {
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
}
