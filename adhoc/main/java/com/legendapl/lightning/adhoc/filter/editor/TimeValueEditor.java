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

public class TimeValueEditor extends GeneralEditor {

	private EditorCommon editorCommon = new EditorCommon();
	private JFXTimePicker timePicker;
	private VBox vBox;
	private static Text label = new Text();
	private String errorMessage = AdhocUtils.getString(WRONG_DATE_FORMAT);

	static {
		label.wrappingWidthProperty().set(200);
		label.setFill(Color.RED);
	}

	public TimeValueEditor(Field field) {
		super(field);
		label.setText(errorMessage);
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		timePicker = new JFXTimePicker();
		timePicker.setIs24HourView(true);
		timePicker.setPrefSize(200, 30);
		vBox = new VBox(5);
		vBox.getChildren().add(timePicker);
		if (filter != null && filter.getValue() != null && !"[NULL]".equals(filter.getValue())) {
			timePicker.setValue((LocalTime)filter.getValue());
		}
		editorPane.getChildren().add(vBox);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValue(timePicker.getValue());
		filter.setExpress(filter.getLabel() + " " + filter.getOp() +
				" {" + timePicker.getValue().format(AdhocConstants.DataTime.HHmmTimeFormatter) + "}");
	}

	public boolean checkFilter(Filter filter) {
		vBox.getChildren().remove(label);
		// NULLの場合かつOP=次と等しいと次と等しくない、チェックしない
		if (timePicker.getValue() == null) {
			if (editorCommon.doCheckNull(filter)) {
				return true;
			}
		}
		boolean isTime = false;
		try {
			LocalTime.parse(timePicker.getValue().toString(), AdhocConstants.DataTime.HHmmTimeFormatter);
			isTime = true;
		} catch(Exception e) {
			isTime = false;
		}
		
		if(!isTime) {
			if(!vBox.getChildren().contains(label)) {
				vBox.getChildren().add(label);
			}
			return false;
		}
		fillFilter(filter);
		return true;
	}
}
