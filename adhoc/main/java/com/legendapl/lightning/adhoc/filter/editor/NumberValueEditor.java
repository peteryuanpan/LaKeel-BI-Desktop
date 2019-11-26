package com.legendapl.lightning.adhoc.filter.editor;

import java.util.regex.Pattern;

import com.legendapl.lightning.adhoc.common.AdhocExceptions.NoSuchDataTypeException;
import com.legendapl.lightning.adhoc.common.AdhocExceptions.NotIntegerException;
import com.legendapl.lightning.adhoc.common.AdhocExceptions.NotNumberException;
import com.legendapl.lightning.adhoc.common.AdhocExceptions.OutOfRangeException;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class NumberValueEditor extends GeneralEditor {

	private TextField textField;
	private VBox vBox;
	private static Text label = new Text();
	private String errorMessage;
	private Field field;
	private boolean numberIlligal;
	private boolean intIlligal;
	private boolean outOfRange;

	static {
		label.wrappingWidthProperty().set(200);
		label.setFill(Color.RED);
	}

	public NumberValueEditor(Field field) {
		super(field);
		this.field = field;
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		textField = new TextField();
		if (filter != null && filter.getValue() != null && !"[NULL]".equals(filter.getValue())) {
			textField.setText((String) filter.getValue());
		}
		vBox = new VBox(5);
		vBox.getChildren().add(textField);
		textField.setPrefSize(200, 30);
		editorPane.getChildren().add(vBox);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValue(textField.getText());
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + filter.getValue() + "}");
	}

	public boolean checkFilter(Filter filter) {
		vBox.getChildren().remove(label);
		if (!(textField.getText().isEmpty() && ("2".equals(String.valueOf(filter.getOp().getIndex()))
				|| "3".equals(String.valueOf(filter.getOp().getIndex()))))) {
			try {
				numberIlligal = false;
				intIlligal = false;
				outOfRange = false;
				isNumber(textField.getText());
				isOutOfRange(textField.getText(), field.getDataType());

			} catch (NotNumberException e) {
				numberIlligal = true;
			} catch (NotIntegerException e) {
				intIlligal = true;
			} catch (OutOfRangeException e) {
				outOfRange = true;
			}

			if (numberIlligal) {
				errorMessage = AdhocUtils.format(AdhocUtils.getString(WRONG_NUMBER_FORMAT), textField.getText());
				label.setText(errorMessage);
				if (!vBox.getChildren().contains(label)) {
					vBox.getChildren().add(label);
				}
				return false;
			}
			if (intIlligal) {
				errorMessage = AdhocUtils.getString(INCORRECT_INTEGER_VALUE);
				label.setText(errorMessage);
				if (!vBox.getChildren().contains(label)) {
					vBox.getChildren().add(label);
				}
				return false;
			}

			if (outOfRange) {
				errorMessage = AdhocUtils.getString(NUMBER_OUT_OF_BOUNDS);
				label.setText(errorMessage);
				if (!vBox.getChildren().contains(label)) {
					vBox.getChildren().add(label);
				}
				return false;
			}
			fillFilter(filter);
		} else {
			filter.setValue("[NULL]");
			filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + "[NULL]" + "}");
		}
		return true;
	}

	private void isNumber(String str) {
	    Pattern pattern = Pattern.compile("^[-\\+]?[0-9]+\\.?[0-9]*");
	    if(!pattern.matcher(str).matches()) {
	    	throw new NotNumberException();
	    }
	}

	private void isInteger(String str) {
	    Pattern pattern = Pattern.compile("^[-\\+]?[0-9]+");
	    if(!pattern.matcher(str).matches()) {
	    	throw new NotIntegerException();
	    }
	}

	private void isOutOfRange(String str, DataType dataType) {
		if(dataType == DataType.INTEGER || dataType == DataType.SHORT || dataType == DataType.LONG ) {
			isInteger(str);
		}
		switch (dataType) {
		case LONG:
			try {
				Long.parseLong(textField.getText());
			} catch(NumberFormatException e) {
				throw new OutOfRangeException();
			}
			break;

		case FLOAT:
			Float floatValue = Float.parseFloat(textField.getText());
			if(floatValue.isInfinite()) {
				throw new OutOfRangeException();
			}
			break;
		case INTEGER:
			try {
				Integer.parseInt(textField.getText());
			} catch(NumberFormatException e) {
				throw new OutOfRangeException();
			}
			break;
		case SHORT:
			try {
				Short.parseShort(textField.getText());
			} catch(NumberFormatException e) {
				throw new OutOfRangeException();
			}
			break;
		case BIGDECIMAL:
		case DOUBLE:
			Double doubleValue = Double.parseDouble(textField.getText());
			if(doubleValue.isInfinite()) {
				throw new OutOfRangeException();
			}
			break;
		default:
			throw new NoSuchDataTypeException();
		}
	}
}
