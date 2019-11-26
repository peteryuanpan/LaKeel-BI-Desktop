package com.legendapl.lightning.adhoc.factory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.FilterType;
import com.legendapl.lightning.adhoc.common.OperationType;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.filter.editor.GeneralEditor;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;

/**
 *　フィルタの編集のページを作成する
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class EditPaneFactory {

	private AnchorPane editorPane;
	private Label label;
	private ChoiceBox<OperationType> choiceBox;
	private Field field;
	private Filter filter;
	private CheckBox checkBox;
	private GeneralEditor editor;
	private BorderPane filterEditPane;
	private ChangeListener<OperationType> listener = (ob, old, newValue) -> {
		Platform.runLater(() ->{
			editor = EditorFactory.getEditor(field, newValue);
			editorPane.getChildren().clear();
			editor.generateEditorPane(editorPane, filter);
			editorPane.autosize();
			filterEditPane.autosize();
		});
	};


	@SuppressWarnings("serial")
	static final protected Map<FilterType, ObservableList<OperationType>> filter2Op = Collections.unmodifiableMap(

			new HashMap<FilterType, ObservableList<OperationType>>() {
				{
					put(FilterType.BOOLEAN,
							FXCollections.observableArrayList(OperationType.equals, OperationType.isNotEqualTo));
					put(FilterType.TIME,
							FXCollections.observableArrayList(OperationType.equals, OperationType.isNotEqualTo,
									OperationType.isAfter, OperationType.isBefore, OperationType.isOnOrAfter,
									OperationType.isOnOrBefore, OperationType.isBetween, OperationType.isNotBetween));
					put(FilterType.TIMESTAMP,
							FXCollections.observableArrayList(OperationType.equals, OperationType.isNotEqualTo,
									OperationType.isAfter, OperationType.isBefore, OperationType.isOnOrAfter,
									OperationType.isOnOrBefore, OperationType.isBetween, OperationType.isNotBetween));
					put(FilterType.STRING, FXCollections.observableArrayList(OperationType.isOneOf,
							OperationType.isNotOneOf, OperationType.equals, OperationType.isNotEqualTo,
							OperationType.contains, OperationType.doesNotContain, OperationType.startsWith,
							OperationType.doesNotStartWith, OperationType.endsWith, OperationType.doesNotEndWith));
					put(FilterType.NUMBER,
							FXCollections.observableArrayList(OperationType.equals, OperationType.isNotEqualTo,
									OperationType.isGreaterThan, OperationType.lessThan,
									OperationType.isGreaterThanOrEqualTo, OperationType.isLessThanOrEqualTo,
									OperationType.isBetween, OperationType.isNotBetween));
					put(FilterType.DATE,
							FXCollections.observableArrayList(OperationType.equals, OperationType.isNotEqualTo,
									OperationType.isAfter, OperationType.isBefore, OperationType.isOnOrAfter,
									OperationType.isOnOrBefore, OperationType.isBetween, OperationType.isNotBetween));
				}
			});



	public EditPaneFactory(Label label, ChoiceBox<OperationType> choiceBox, AnchorPane editorPane, CheckBox checkBox, BorderPane filterEditPane) {
		this.label = label;
		this.choiceBox = choiceBox;
		this.editorPane = editorPane;
		this.checkBox = checkBox;
		this.filterEditPane = filterEditPane;
		choiceBox.valueProperty().addListener(listener);
	}

	public void generatePane(Field field, Filter filter) {
		Platform.runLater(() ->{
			choiceBox.valueProperty().removeListener(listener);
			this.filter = filter;
			this.field = field;
			label.setText(field.getLabel());
			label.setPrefWidth(AdhocUtils.getLabelWidth(label));
			choiceBox.setItems(filter2Op.get(field.getDataType().getFilterType()));
			choiceBox.setPrefWidth(AdhocUtils.getChoiceBoxWidth(choiceBox));
			if(filter != null) {
				checkBox.setSelected(false);
				choiceBox.setValue(filter.getOp());
				checkBox.setSelected(filter.getLock());
			} else {
				choiceBox.setValue(OperationType.equals);
			}
			editor = EditorFactory.getEditor(field, choiceBox.getValue());
			editorPane.getChildren().clear();
			editor.generateEditorPane(editorPane, filter);
			editorPane.autosize();
			filterEditPane.autosize();
			choiceBox.valueProperty().addListener(listener);
		});
	}

	public Filter generateFilter() {
		Filter filter = new Filter(field);
		filter.setOp(choiceBox.getValue());
		filter.setLock(checkBox.isSelected());
		if(editor.checkFilter(filter))
			return filter;
		return null;
	}
}
