package com.legendapl.lightning.adhoc.filter.editor;

import java.math.BigDecimal;
import java.util.regex.Pattern;

import com.legendapl.lightning.adhoc.common.AdhocExceptions.NoSuchDataTypeException;
import com.legendapl.lightning.adhoc.common.AdhocExceptions.NotIntegerException;
import com.legendapl.lightning.adhoc.common.AdhocExceptions.NotNumberException;
import com.legendapl.lightning.adhoc.common.AdhocExceptions.OutOfRangeException;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class NumberRangeEditor extends GeneralEditor{

	private TextField textFieldLow;
	private VBox vBoxLow;
	private TextField textFieldHigh;
	private VBox vBoxHigh;
	private VBox vBox;
	private static Text labelLow = new Text();
	private static Text labelHigh = new Text();
	private Field field;
	private boolean numberIlligal;
	private boolean intIlligal;
	private boolean outOfRange;

	static {
		labelLow.wrappingWidthProperty().set(200);
		labelLow.setFill(Color.RED);
		labelHigh.wrappingWidthProperty().set(200);
		labelHigh.setFill(Color.RED);
	}


	public NumberRangeEditor(Field field) {
		super(field);
		this.field = field;
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		textFieldLow = new TextField();
		textFieldHigh = new TextField();
		vBoxLow = new VBox(5);
		vBoxHigh = new VBox(5);
		vBoxLow.getChildren().add(textFieldLow);
		vBoxHigh.getChildren().add(textFieldHigh);
		textFieldLow.setPrefSize(200, 30);
		textFieldHigh.setPrefSize(200, 30);
		if(filter != null && filter.getLowValue() != null && filter.getHighValue() != null) {
			textFieldLow.setText((String)filter.getLowValue());
			textFieldHigh.setText((String)filter.getHighValue());
		}
		vBox = new VBox(10);
		vBox.getChildren().addAll(vBoxLow, vBoxHigh);
		editorPane.getChildren().add(vBox);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setLowValue(textFieldLow.getText());
		filter.setHighValue(textFieldHigh.getText());
		filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + filter.getLowValue() + " and " + filter.getHighValue() + "}");
	}

	@Override
	public boolean checkFilter(Filter filter) {
		vBoxLow.getChildren().remove(labelLow);
		vBoxHigh.getChildren().remove(labelHigh);
		if(checkNumber(vBoxLow, labelLow, textFieldLow)) {
			if(checkNumber(vBoxHigh, labelHigh, textFieldHigh)) {
				switch(field.getDataType()) {
				case LONG:
					long lowLong = Long.parseLong(textFieldLow.getText());
					long highLong = Long.parseLong(textFieldHigh.getText());
					if(lowLong >= highLong) {
						String errorMessage = AdhocUtils.format(AdhocUtils.getString(NUMBER_RANGE_ERROR), lowLong, highLong);
						labelHigh.setText(errorMessage);
						vBoxHigh.getChildren().add(labelHigh);
						return false;
					}
					break;
				case DOUBLE:
					double lowDouble = Double.parseDouble(textFieldLow.getText());
					double highDouble = Double.parseDouble(textFieldHigh.getText());
					if(lowDouble >= highDouble) {
						String errorMessage = AdhocUtils.format(AdhocUtils.getString(NUMBER_RANGE_ERROR), lowDouble, highDouble);
						labelHigh.setText(errorMessage);
						vBoxHigh.getChildren().add(labelHigh);
						return false;
					}
					break;
				case FLOAT:
					float lowFloat = Float.parseFloat(textFieldLow.getText());
					float highFloat = Float.parseFloat(textFieldHigh.getText());
					if(lowFloat >= highFloat) {
						String errorMessage = AdhocUtils.format(AdhocUtils.getString(NUMBER_RANGE_ERROR), lowFloat, highFloat);
						labelHigh.setText(errorMessage);
						vBoxHigh.getChildren().add(labelHigh);
						return false;
					}
					break;
				case INTEGER:
					int lowInt = Integer.parseInt(textFieldLow.getText());
					int highInt = Integer.parseInt(textFieldHigh.getText());
					if(lowInt >= highInt) {
						String errorMessage = AdhocUtils.format(AdhocUtils.getString(NUMBER_RANGE_ERROR), lowInt, highInt);
						labelHigh.setText(errorMessage);
						vBoxHigh.getChildren().add(labelHigh);
						return false;
					}
					break;
				case SHORT:
					short lowShort = Short.parseShort(textFieldLow.getText());
					short highShort = Short.parseShort(textFieldHigh.getText());
					if(lowShort >= highShort) {
						String errorMessage = AdhocUtils.format(AdhocUtils.getString(NUMBER_RANGE_ERROR), lowShort, highShort);
						labelHigh.setText(errorMessage);
						vBoxHigh.getChildren().add(labelHigh);
						return false;
					}
					break;
				case BIGDECIMAL:
					BigDecimal lowBigDecimal = BigDecimal.valueOf(Double.parseDouble(textFieldLow.getText()));
					BigDecimal highBigDecimal = BigDecimal.valueOf(Double.parseDouble(textFieldHigh.getText()));
					if(highBigDecimal.compareTo(lowBigDecimal) != 1) {
						String errorMessage = AdhocUtils.format(AdhocUtils.getString(NUMBER_RANGE_ERROR), lowBigDecimal, highBigDecimal);
						labelHigh.setText(errorMessage);
						vBoxHigh.getChildren().add(labelHigh);
						return false;
					}
					break;
				default:
					throw new NoSuchDataTypeException();
				}
			} else {
				return false;
			}
		} else {
			return false;
		}
		fillFilter(filter);
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

	private void isOutOfRange(TextField textField, DataType dataType) {
		if(dataType == DataType.INTEGER || dataType == DataType.SHORT || dataType == DataType.LONG ) {
			isInteger(textField.getText());
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

	private boolean checkNumber(VBox vBox, Text label, TextField textField) {
		try {
			numberIlligal = false;
			intIlligal = false;
			outOfRange = false;
			isNumber(textField.getText());
			isOutOfRange(textField, field.getDataType());

		} catch(NotNumberException e) {
			numberIlligal = true;
		} catch(NotIntegerException e) {
			intIlligal = true;
		} catch(OutOfRangeException e) {
			outOfRange = true;
		}

		String errorMessage;
		if (numberIlligal) {
			errorMessage = AdhocUtils.format(AdhocUtils.getString(WRONG_NUMBER_FORMAT), textField.getText());
			label.setText(errorMessage);
			if (!vBox.getChildren().contains(label)) {
				vBox.getChildren().add(label);
			}
			return false;
		}
		if (intIlligal) {
			errorMessage = AdhocUtils.format(AdhocUtils.getString(INCORRECT_INTEGER_VALUE));
			label.setText(errorMessage);
			if (!vBox.getChildren().contains(label)) {
				vBox.getChildren().add(label);
			}
			return false;
		}

		if (outOfRange) {
			errorMessage = AdhocUtils.format(AdhocUtils.getString(NUMBER_OUT_OF_BOUNDS));
			label.setText(errorMessage);
			vBox.getChildren().add(label);
			return false;
		}
		return true;
	}

}
