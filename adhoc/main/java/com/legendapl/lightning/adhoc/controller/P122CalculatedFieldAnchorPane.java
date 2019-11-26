package com.legendapl.lightning.adhoc.controller;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.calculate.field.LexicalParser;
import com.legendapl.lightning.adhoc.calculate.field.SyntaxParser;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.CalculateFunction;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.factory.AdhocBuildTreeFactory;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.ShareDataService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class P122CalculatedFieldAnchorPane extends C100AdhocBaseAnchorPane {

	@FXML
	private Button addButton;
	@FXML
	private Button subtractButton;
	@FXML
	private Button multiplyButton;
	@FXML
	private Button divideButton;
	@FXML
	private Button modButton;
	@FXML
	private Button parenLeftButton;
	@FXML
	private Button parenRightButton;
	@FXML
	private Button colonButton;
	@FXML
	private Button andButton;
	@FXML
	private Button orButton;
	@FXML
	private Button notButton;
	@FXML
	private Button inButton;
	@FXML
	private Button equalButton;
	@FXML
	private Button notEqualButton;
	@FXML
	private Button greaterThanButton;
	@FXML
	private Button lessThanButton;
	@FXML
	private Button greaterEqualButton;
	@FXML
	private Button lessEqualButton;
	@FXML
	private Button checkButton;
	@FXML
	private Button createButton;
	@FXML
	private Button cancleButton;
	@FXML
	private Label errorLabel;

	@FXML
	private Label fieldNameTitle;


	@FXML
	private TextField fieldName;
	@FXML
	private TextArea equation;
	@FXML
	private ListView<Field> fieldAndMeasureList;
	@FXML
	private ListView<CalculateFunction> functionList;
	@FXML
	private Label functionInfo;
	@FXML
	private CheckBox showFunctionParam;

	/*------------- Data --------------*/

	private Adhoc adhoc = ShareDataService.loadAdhoc();

	private ObservableList<CalculateFunction> functions = FXCollections.observableArrayList(CalculateFunction.values());

	private ObservableList<Field> fieldAndMeasures = FXCollections.observableArrayList(AdhocUtils.getFieldsByRoot(adhoc));

	private Map<String, Field> label2Field = new HashMap<>();

	private Map<String, Integer> label2Count = new HashMap<>();

	private Map<Field, String> field2Label = new HashMap<>();

	private Map<String, DataType> label2DataType = new HashMap<>();

	private DataType currentDataType = DataType.UNKNOW;

//	private static final Pattern fieldPattern = Pattern.compile("Field\\(\"([^\\s]+)\"\\)");

	private SyntaxParser parser;

	private LexicalParser lexicalParser;


	@Override
	public void initialize(URL location, ResourceBundle resources) {
		initButton();
		initOtherControl();

		fieldName.setText(ShareDataService.isCalculatedField() ? AdhocUtils.getString("P122.field.name") : AdhocUtils.getString("P122.measure.name"));
		fieldNameTitle.setText(ShareDataService.isCalculatedField() ? AdhocUtils.getString("P122.title.field") : AdhocUtils.getString("P122.title.measure"));

		fieldAndMeasures.forEach(field -> {
			String label = field.getLabel();
			if(label2Count.containsKey(label)) {
				label2Count.put(label, label2Count.get(label) + 1);
				label += label2Count.get(label);
			} else {
				label2Count.put(label, 0);
			}
			label2Field.put(label, field);
			label2DataType.put(label, field.getDataType());
			field2Label.put(field, label);
		});

		lexicalParser = new LexicalParser(label2Field);

		parser = new SyntaxParser(lexicalParser);

		parser.setLabel2Field(label2Field);

		Collections.sort(functions, new Comparator<CalculateFunction>(){

			@Override
			public int compare(CalculateFunction o1, CalculateFunction o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});

	}

	private void initOtherControl() {
		functionList.setItems(functions);
		fieldAndMeasureList.setItems(fieldAndMeasures);

		fieldAndMeasureList.setCellFactory(field -> {
			return new ListCell<Field>() {
				@Override
				protected void updateItem(Field item, boolean empty) {
					super.updateItem(item, empty);
					if(getIndex() >= 0 && getIndex() < fieldAndMeasures.size())
						setText(item.getLabel());
				}

			};
		});

		fieldAndMeasureList.setOnMouseClicked(click -> {
			if(click.getClickCount() == 2) {
				Field field = fieldAndMeasureList.getSelectionModel().getSelectedItem();
				insertIntoEquation("\"" + field2Label.get(field) + "\"");
			}
		});

		functionList.getSelectionModel().selectedItemProperty().addListener((ob, old, newValue) -> {
			functionInfo.setText(newValue + "\n" + newValue.getDescription() + "\n\n" + newValue.getExampleWithParam());
		});

		functionList.setOnMouseClicked(click -> {
			if(click.getClickCount() == 2) {
				addFunctionText(functionList.getSelectionModel().getSelectedItem(), showFunctionParam.isSelected());
			}
		});
	}

	private void addFunctionText(CalculateFunction function, boolean selected) {
		insertIntoEquation((selected ? function.getExampleWithParam() : function.getExample()));
	}

	private void insertIntoEquation(String insert) {
		int caretPosition = equation.getCaretPosition();
		String currentText = equation.getText();
		equation.setText(currentText.substring(0, caretPosition) + insert + currentText.substring(caretPosition));
		equation.positionCaret(caretPosition + insert.length());
	}

	private void initButton() {

		addButton.setOnAction(event -> {
			insertIntoEquation(" + ");
		});

		subtractButton.setOnAction(event -> {
			insertIntoEquation(" - ");
		});

		multiplyButton.setOnAction(event -> {
			insertIntoEquation(" * ");
		});

		divideButton.setOnAction(event -> {
			insertIntoEquation(" / ");
		});

		modButton.setOnAction(event -> {
			insertIntoEquation(" % ");
		});

		parenLeftButton.setOnAction(event -> {
			insertIntoEquation(" ( ");
		});

		parenRightButton.setOnAction(event -> {
			insertIntoEquation(" ) ");
		});

		colonButton.setOnAction(event -> {
			insertIntoEquation(" : ");
		});

		andButton.setOnAction(event -> {
			insertIntoEquation(" AND ");
		});

		orButton.setOnAction(event -> {
			insertIntoEquation(" OR ");
		});

		notButton.setOnAction(event -> {
			insertIntoEquation(" NOT ");
		});

		inButton.setOnAction(event -> {
			insertIntoEquation(" IN ");
		});

		equalButton.setOnAction(event -> {
			insertIntoEquation(" == ");
		});

		notEqualButton.setOnAction(event -> {
			insertIntoEquation(" != ");
		});

		greaterThanButton.setOnAction(event -> {
			insertIntoEquation(" > ");
		});

		lessThanButton.setOnAction(event -> {
			insertIntoEquation(" < ");
		});

		greaterEqualButton.setOnAction(event -> {
			insertIntoEquation(" >= ");
		});

		lessEqualButton.setOnAction(event -> {
			insertIntoEquation(" <= ");
		});

		checkButton.setOnAction(event -> {
			if(checkSyntax()) {
				errorLabel.setText("TRUE");
				errorLabel.setStyle("-fx-background-color: green;");
			} else {
				errorLabel.setText("FALSE");
				errorLabel.setStyle("-fx-background-color: red;");
			}
		});

		createButton.setOnAction(event -> {
			Field field = new Field();
			if(checkSyntax() && checkFieldName()) {
				field.setLabel(fieldName.getText());
				field.setCalculatedExpression(parser.getJexlExpression());
				field.setDataType(currentDataType);
				field.setId(distinctId(0));
				field.setResourceId(field.getId());
				field.setSubFieldResourceId(lexicalParser.getSubFields());
				AdhocBuildTreeFactory.insertTree(field, ShareDataService.isCalculatedField() ? AdhocModelType.FIELD : AdhocModelType.MEASURE);
				((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
			}
		});

		cancleButton.setOnAction(event -> {
			((Stage)(((Button)event.getSource()).getScene().getWindow())).close();
		});

	}

	private String distinctId(int suffix) {
		String fieldNameLabel = fieldName.getText() + (suffix == 0 ? "" : suffix);
		for(int i=0;i<fieldAndMeasures.size();i++) {
			if(fieldAndMeasures.get(i).getResourceId().equals(fieldNameLabel)) {
				return distinctId(++suffix);
			}
		}
		return fieldNameLabel;
	}

	private boolean checkFieldName() {
		String fieldNameLabel = fieldName.getText();
		for(int i=0;i<fieldAndMeasures.size();i++) {
			if(fieldAndMeasures.get(i).getLabel().equals(fieldNameLabel))
				return false;
		}
		return true;
	}

	private boolean checkSyntax() {
		currentDataType = parser.dotest(equation.getText());
		return currentDataType != DataType.UNKNOW;
	}

}
