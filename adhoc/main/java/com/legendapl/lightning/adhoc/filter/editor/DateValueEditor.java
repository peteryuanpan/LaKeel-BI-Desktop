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

public class DateValueEditor extends GeneralEditor{

	private EditorCommon editorCommon = new EditorCommon();
	private JFXDatePicker datePicker;
	private VBox vBox;
	private static Text label = new Text();
	private String errorMessage = AdhocUtils.getString(WRONG_DATE_FORMAT);

	static {
		label.wrappingWidthProperty().set(200);
		label.setFill(Color.RED);
	}

	public DateValueEditor(Field field) {
		super(field);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {

		datePicker = new JFXDatePicker();
		datePicker.setPrefSize(200, 30);
		vBox = new VBox(5);
		vBox.getChildren().add(datePicker);
		if(filter != null && filter.getValue() != null && !"[NULL]".equals(filter.getValue())) {
			datePicker.setValue((LocalDate) filter.getValue());
		}
		editorPane.getChildren().add(vBox);
		Validator<LocalDate> validator = (control, value) -> {
		 	boolean isDate = false;
		 	try {
		 		LocalDate.parse(datePicker.getValue().toString());
				isDate = false;
			} catch (Exception e) {
				isDate = true;
			}
            return ValidationResult.fromMessageIf(control,errorMessage ,Severity.ERROR , isDate);
	    };
	    support.registerValidator(datePicker, true, validator);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValue(datePicker.getValue());
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + datePicker.getValue() + "}");
	}

	@Override
	public boolean checkFilter(Filter filter) {
		vBox.getChildren().remove(label);
		// NULLの場合かつOP=次と等しいと次と等しくない、チェックしない
		if (datePicker.getValue() == null) {
			if (editorCommon.doCheckNull(filter)) {
				return true;
			}
		}
		if (!support.getValidationResult().getMessages().isEmpty()) {
			if (!vBox.getChildren().contains(label)) {
				label.setText(errorMessage);
				vBox.getChildren().add(label);
			}
			return false;
		}
		// 日付チェック
		String pattern = AdhocConstants.StringDataTime.string_dateFormat;
		if (!editorCommon.isValidDate(datePicker.getValue().toString(), pattern)) {
			label.setText(AdhocUtils.getString(DATE_FORMAT_ERROR));
			vBox.getChildren().add(label);
			return false;
		}
		fillFilter(filter);
		return true;
	}
}
