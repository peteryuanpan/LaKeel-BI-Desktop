package com.legendapl.lightning.adhoc.filter.editor;

import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.Validator;

import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class StringValueEditor extends GeneralEditor {

	private TextField textField;
	private VBox vBox;
	private static Text label = new Text();
	private String errorMessage;

	static {
		label.wrappingWidthProperty().set(200);
		label.setFill(Color.RED);
	}


	public StringValueEditor(Field field) {
		super(field);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		textField = TextFields.createClearableTextField();
		if(filter != null && filter.getValue() != null && !"[NULL]".equals(filter.getValue())) {
			textField.setText((String)filter.getValue());
		}
		vBox = new VBox(5);
		vBox.getChildren().add(textField);
		textField.setPrefSize(200, 30);
		editorPane.getChildren().add(vBox);
	    support.registerValidator(textField, true, Validator.createEmptyValidator("empty"));
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValue(textField.getText());
		filter.setHighValue("'" + textField.getText() + "'");
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + filter.getValue() + "}");
	}

	public boolean checkFilter(Filter filter) {
		vBox.getChildren().remove(label);
		if(!support.getValidationResult().getMessages().isEmpty() && textField.getText() == null) {
			errorMessage = AdhocUtils.getString(VALUE_SHOULD_BE_NOT_EMPTY);
			label.setText(errorMessage);
			if(!vBox.getChildren().contains(label)) {

				vBox.getChildren().add(label);
			}
			return false;
		}
		if(textField.getText().length() > AdhocConstants.Filter.MAX_LENGTH) {
			errorMessage = AdhocUtils.getString(TOO_LONG_TEXT);
			label.setText(errorMessage);
			if(!vBox.getChildren().contains(label)) {
				vBox.getChildren().add(label);
			}
			return false;
		}
		fillFilter(filter);
		return true;
	}

}
