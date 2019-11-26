package com.legendapl.lightning.adhoc.filter.editor;

import java.util.List;

import com.legendapl.lightning.adhoc.custom.AutoCompleteComboBoxListener;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;

public class ComboSelectEditor extends GeneralEditor {

	private ComboBox<String> comboBox;
	private Field field;
	private EditorCommon editorCommon = new EditorCommon();

	public ComboSelectEditor(Field field) {
		super(field);
		this.field = field;
	}

	@Override
	public void generateEditorPane(AnchorPane editorPane, Filter filter) {
		comboBox = new ComboBox<>();
		List<String> distinctList = dbService.getDistinctList(field);
		if(distinctList == null)
			return;
		// 空とNULLの場合アイテムを変更
		editorCommon.changeBlank(distinctList);
		ObservableList<String> list = FXCollections.observableArrayList(distinctList);
		comboBox.setItems(list);

		new AutoCompleteComboBoxListener<>(comboBox);
		if(filter != null) {
			comboBox.setValue((String) filter.getValue());
		} else {
			// 新規の場合デフォルト値がブランクを設定する
			// comboBox.setValue("[------]");
			// 新規の場合デフォルト値が[NULL]を設定する
			 comboBox.setValue("[NULL]");
		}
		comboBox.setPrefSize(200, 30);
		editorPane.getChildren().add(comboBox);
	}

	@Override
	public void fillFilter(Filter filter) {
		filter.setValue(comboBox.getValue());
		filter.setHighValue("'" + comboBox.getValue() + "'");
		if(filter.getValue() != null && filter.getValue()!= null)
			filter.setExpress(filter.getLabel() + " " + filter.getOp() + " {" + filter.getValue() + "}");
		else
			filter.setExpress(filter.getLabel() + " " + filter.getOp() + " { \"\" }");
	}

	@Override
	public boolean checkFilter(Filter filter) {
		fillFilter(filter);
		return true;
	}

}
