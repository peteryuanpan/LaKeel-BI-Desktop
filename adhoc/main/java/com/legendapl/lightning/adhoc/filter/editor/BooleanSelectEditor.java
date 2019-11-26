package com.legendapl.lightning.adhoc.filter.editor;

import java.util.Arrays;

import org.controlsfx.validation.ValidationMessage;
import org.controlsfx.validation.Validator;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.custom.AutoCompleteComboBoxListener;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class BooleanSelectEditor extends GeneralEditor{


	private ComboBox<String> comboBox;
	private VBox vBox;
	private static Text text = new Text();
	private String illegalMessage = AdhocUtils.getString(INCORRECT_ATTRIBUTE_PATTERN);
	private String emptyMessage = AdhocUtils.getString(VALUE_SHOULD_BE_NOT_EMPTY);

	static {
		text.wrappingWidthProperty().set(200);
		text.setFill(Color.RED);
	}

	public BooleanSelectEditor(Field field) {
		super(field);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		comboBox = new ComboBox<>();
		vBox = new VBox(5);
		ObservableList<String> list = FXCollections.observableArrayList("true", "false", "[NULL]");
		comboBox.setItems(list);
		new AutoCompleteComboBoxListener<>(comboBox);
		if (filter != null && "[NULL]".equals(filter.getValue())) {
			comboBox.setValue((filter.getValue()).toString());
		} else if(filter != null && filter.getValue()!= null) {
			comboBox.setValue(((Boolean) filter.getValue()).toString());
		} else {
			// 新規の場合デフォルト値が[NULL]を設定する
			comboBox.setValue("[NULL]");
		}
		comboBox.setPrefSize(200, 30);
		vBox.getChildren().add(comboBox);
		editorPane.getChildren().add(vBox);
		Validator<String> validatorEmpty = Validator.createEmptyValidator(emptyMessage);
		Validator<String> validatorNotBoolean = Validator.createEqualsValidator(illegalMessage, Arrays.asList("true", "false"));
		support.registerValidator(comboBox, true, Validator.combine(validatorEmpty, validatorNotBoolean));
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValue(Boolean.parseBoolean(comboBox.getValue()));
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + filter.getValue() + "}");
	}

	public boolean checkFilter(Filter filter) {
		vBox.getChildren().remove(text);
		if(!support.getValidationResult().getMessages().isEmpty()) {
			if(!vBox.getChildren().contains(text)) {
				vBox.getChildren().add(text);
			}
			if(support.getValidationResult().getErrors().contains(ValidationMessage.error(comboBox, emptyMessage))) {
					text.setText(emptyMessage);
			} else {
				String opIndex = String.valueOf(filter.getOp().getIndex());
				String isNull = "[NULL]";
				// 次と等しいと次と等しくない場合 空をチェックしない
				if (!isNull.equals(comboBox.getValue()) || (!"2".equals(opIndex) && !"3".equals(opIndex))) {
				text.setText(illegalMessage);
				} else {
					vBox.getChildren().remove(text);
					filter.setValue(isNull);
					filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + filter.getValue() + "}");
					return true;
				}
			}
			return false;
		}
		fillFilter(filter);
		return true;
	}
}
