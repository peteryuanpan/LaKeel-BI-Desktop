package com.legendapl.lightning.adhoc.filter.editor;

import java.time.LocalDate;

import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationResult;
import org.controlsfx.validation.Validator;

import com.jfoenix.controls.JFXDatePicker;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class DateRangeEditor extends GeneralEditor{

	private EditorCommon editorCommon = new EditorCommon();
	private JFXDatePicker datePickerBefore;
	private VBox vBoxBefore;
	private JFXDatePicker datePickerAfter;
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

	public DateRangeEditor(Field field) {
		super(field);
		isRange = true;
		labelBefore.setText(beforeMessage);
		labelAfter.setText(afterMessage);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		datePickerBefore = new JFXDatePicker();
		vBoxBefore = new VBox(5);
		vBoxBefore.getChildren().add(datePickerBefore);
		datePickerAfter = new JFXDatePicker();
		vBoxAfter = new VBox(5);
		vBoxAfter.getChildren().add(datePickerAfter);
		if(filter != null && filter.getLowValue()!= null && filter.getHighValue() != null) {
			datePickerBefore.setValue((LocalDate) filter.getLowValue());
			datePickerAfter.setValue((LocalDate) filter.getHighValue());
		}
		vBox = new VBox(10);
		datePickerBefore.setPrefSize(200, 30);
		datePickerAfter.setPrefSize(200, 30);
		vBox.getChildren().addAll(vBoxBefore, vBoxAfter);
		editorPane.getChildren().add(vBox);
		Validator<LocalDate> validatorBefore = (control, value) -> {
		 	boolean isDate = false;
		 	try {
		 		LocalDate.parse(datePickerBefore.getValue().toString());
				isDate = false;
			} catch (Exception e) {
				isDate = true;
			}
            return ValidationResult.fromMessageIf(control, beforeMessage, Severity.ERROR, isDate);
	    };
	    support.registerValidator(datePickerBefore, true, validatorBefore);

	    Validator<LocalDate> validatorAfter = (control, value) -> {
		 	boolean isDate = false;
		 	try {
		 		LocalDate dateBefore = LocalDate.parse(
						datePickerBefore.getValue().toString());
		 		LocalDate dateAfter = LocalDate.parse(
						datePickerAfter.getValue().toString());
				isDate = !dateAfter.isAfter(dateBefore);
			} catch (Exception e) {
				isDate = true;
			}

            return ValidationResult.fromMessageIf(control, afterMessage, Severity.ERROR, isDate);
	    };
	    support1.registerValidator(datePickerAfter, true, validatorAfter);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setLowValue(datePickerBefore.getValue());
		filter.setHighValue(datePickerAfter.getValue());
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + datePickerBefore.getValue() + " and " +
		datePickerAfter.getValue() + "}");
	}

	protected void showValidationStyle(String errorMessage, Boolean isbeforeFlg) {
		if (isbeforeFlg) {
			labelBefore.setText(errorMessage);
			vBoxBefore.getChildren().add(labelBefore);
		} else {
			labelAfter.setText(errorMessage);
			vBoxAfter.getChildren().add(labelAfter);
		}
	}

	private Boolean doDataFormartCheck() {
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
		// 日付チェック
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

	public boolean checkFilter(Filter filter) {
		vBoxBefore.getChildren().remove(labelBefore);
		vBoxAfter.getChildren().remove(labelAfter);
		if (!doDataFormartCheck()) {
			return false;
		}
		try {
			LocalDate dateBefore = LocalDate.parse(datePickerBefore.getValue().toString(),
					AdhocConstants.DataTime.dateFormatter);
			LocalDate dateAfter = LocalDate.parse(datePickerAfter.getValue().toString(),
					AdhocConstants.DataTime.dateFormatter);
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
}
