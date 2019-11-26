package com.legendapl.lightning.adhoc.custom;

import com.legendapl.lightning.adhoc.common.OperationType;
import com.legendapl.lightning.adhoc.filter.editor.GeneralEditor;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class FilterVBox extends VBox {

	private Button button;
	private Label label;
	private GeneralEditor generalEditor;
	private ChoiceBox<OperationType> choiceBox;
	private AnchorPane anchorPane;
	
	private Field field;
	private String resourceId;
	
	private ContextMenu filterMenu;
	private MenuItem moveUpFilter;
	private MenuItem moveDownFilter;
	private MenuItem deleteFilter;

	public Button getButton() {
		return button;
	}

	public void setButton(Button button) {
		this.button = button;
	}

	public Label getLabel() {
		return label;
	}

	public void setLabel(Label label) {
		this.label = label;
	}

	public GeneralEditor getGeneralEditor() {
		return generalEditor;
	}

	public void setGeneralEditor(GeneralEditor generalEditor) {
		this.generalEditor = generalEditor;
	}

	public ChoiceBox<OperationType> getChoiceBox() {
		return choiceBox;
	}

	public void setChoiceBox(ChoiceBox<OperationType> choiceBox) {
		this.choiceBox = choiceBox;
	}
	
	public AnchorPane getAnchorPane() {
		return anchorPane;
	}

	public void setAnchorPane(AnchorPane anchorPane) {
		this.anchorPane = anchorPane;
	}
	
	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}

	public String getResourceId() {
		return resourceId;
	}

	public void setResourceId(String resourceId) {
		this.resourceId = resourceId;
	}
	
	public ContextMenu getFilterMenu() {
		return filterMenu;
	}

	public void setFilterMenu(ContextMenu filterMenu) {
		this.filterMenu = filterMenu;
	}

	public MenuItem getMoveUpFilter() {
		return moveUpFilter;
	}

	public void setMoveUpFilter(MenuItem moveUpFilter) {
		this.moveUpFilter = moveUpFilter;
	}

	public MenuItem getMoveDownFilter() {
		return moveDownFilter;
	}

	public void setMoveDownFilter(MenuItem moveDownFilter) {
		this.moveDownFilter = moveDownFilter;
	}

	public MenuItem getDeleteFilter() {
		return deleteFilter;
	}

	public void setDeleteFilter(MenuItem deleteFilter) {
		this.deleteFilter = deleteFilter;
	}

}
