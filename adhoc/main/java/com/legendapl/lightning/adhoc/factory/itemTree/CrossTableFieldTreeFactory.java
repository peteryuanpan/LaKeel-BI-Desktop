package com.legendapl.lightning.adhoc.factory.itemTree;

import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.GroupType;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.custom.LayoutLabel;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.custom.CustomTooltip;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.service.AdhocLogService;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * クロス集計フィールドツリー工場
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/4/1
 */
public class CrossTableFieldTreeFactory extends CrossTableItemTreeFactory {
	
	/*-------------------------------------Data-----------------------------------------*/
	
	private static Boolean lastToColumnForDoubleClick = false;
	
	/*-------------------------------------API-----------------------------------------*/
	
	public void initModel() {
		// field
		setTreeViewSelectedItemProperty(fieldTreeView);
		fieldTreeView.setCellFactory(treeView -> new FieldTreeCell());
		fieldTreeView.refresh();
	}
	
	/*-------------------------------------TreeCell-----------------------------------------*/
	
	private class FieldTreeCell extends TreeCell<BaseNode> {
		@Override public void updateItem(BaseNode item, boolean empty) {
			super.updateItem(item, empty);
			if (null != item && null != this.getTreeItem()) {
				if (item instanceof DBNode) { // DBNode
					// initialize
					this.setText(item.getLabel());
					this.setTooltip(new Tooltip(item.getResourceId()));
					// contextMenu
					ContextMenu contextMenu = new ContextMenu();
					MenuItem moveTreeItemGroupToColumn = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToColumn.label"));
					moveTreeItemGroupToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn());
					MenuItem moveTreeItemGroupToRow = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToRow.label"));
					moveTreeItemGroupToRow.setOnAction(event -> handleActionMoveTreeItemToRow());
					contextMenu.getItems().setAll(moveTreeItemGroupToColumn, moveTreeItemGroupToRow);
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> handleActionOnMouseClickedTreeCell(event));
					// onDragAndDropped
					this.setOnDragDetected(event -> handleActionOnDragDetectedItemTree(event, AdhocModelType.FIELD, fieldTreeView, this));
					this.setOnDragDone(event -> handleActionOnDragDoneItemTree(event, fieldTreeView));
				} else if (item instanceof Item) { // Item
					// initialize
					this.setText(item.getLabel());
					this.setTooltip(new Tooltip(item.getResourceId()));
					// contextMenu
					ContextMenu contextMenu = new ContextMenu();
					MenuItem moveTreeItemToColumn = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToColumn.label"));
					moveTreeItemToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn());
					MenuItem moveTreeItemToRow = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToRow.label"));
					moveTreeItemToRow.setOnAction(event -> handleActionMoveTreeItemToRow());
					MenuItem moveTreeItemToMeasure = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToMeasure.label"));
					moveTreeItemToMeasure.visibleProperty().bind(moveTreeItemToOppsiteVisible(fieldTreeView));
					moveTreeItemToMeasure.setOnAction(event -> handleActionMoveTreeItemToOppsite(fieldTreeView, valueTreeView));
					MenuItem createFilterToPane = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.CreateFilterToPane.label"));
					createFilterToPane.visibleProperty().bind(createFilterToPaneVisible(fieldTreeView));
					createFilterToPane.setOnAction(event -> handleActionCreateFilterToPane(fieldTreeView));
					contextMenu.getItems().setAll(moveTreeItemToColumn, moveTreeItemToRow, moveTreeItemToMeasure, createFilterToPane);
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> handleActionOnMouseClickedTreeCell(event));
					// onDragAndDropped
					this.setOnDragDetected(event -> handleActionOnDragDetectedItemTree(event, AdhocModelType.FIELD, fieldTreeView, this));
					this.setOnDragDone(event -> handleActionOnDragDoneItemTree(event, fieldTreeView));
				} else {
					this.setText(null);
					this.setTooltip(null);
					this.setContextMenu(null);
				}
			} else {
				this.setText(null);
				this.setTooltip(null);
				this.setContextMenu(null);
			}
		}
	}

	private void handleActionMoveTreeItemToColumn() {
		lastToColumnForDoubleClick = true;
		handleActionMoveTreeItemToLayout(columnFlow, crossTableColumns);
	}

	private void handleActionMoveTreeItemToRow() {
		lastToColumnForDoubleClick = false;
		handleActionMoveTreeItemToLayout(rowFlow, crossTableRows);
	}
	
	private void handleActionMoveTreeItemToLayout(LayoutFlowPane flow, List<CrossTableField> fields) {
		// get
		List<CrossTableField> newFields = getFieldsBySelectedItems(AdhocModelType.FIELD, fieldTreeView);
		// handle
		Integer indexNode = flow.getChildren().size();
		addLayoutChildren(AdhocModelType.ITEMTREE, AdhocModelType.FIELD, flow, indexNode, fields, null, newFields);
	}

	private void handleActionOnMouseClickedTreeCell(MouseEvent event) {
		if (!checkMouseClickedTreeCell(event, AdhocModelType.FIELD, fieldTreeView)) return;
		if (MouseButton.PRIMARY == event.getButton()) {
			if (event.getClickCount() > 1) {
				if (lastToColumnForDoubleClick) {
					handleActionMoveTreeItemToColumn();
				} else {
					handleActionMoveTreeItemToRow();
				}
				Platform.runLater(() -> fieldTreeView.getSelectionModel().clearSelection());
			}
		}
	}
	
	@Override protected void handleActionFromItemTreeOnDragDroppedFlowPane(DragEvent event, LayoutFlowPane flow) {
		lastToColumnForDoubleClick = (columnFlow == flow);
		super.handleActionFromItemTreeOnDragDroppedFlowPane(event, flow);
	}
	
	/*-------------------------------------Layout-----------------------------------------*/
	
	public LayoutLabel getLayoutLabel(LayoutFlowPane thisFlow, CrossTableField field) {
		if (columnFlow == thisFlow) {
			return getLayoutLabel(AdhocModelType.FIELD, columnFlow, rowFlow, crossTableColumns, crossTableRows, field);
		} else if (rowFlow == thisFlow) {
			return getLayoutLabel(AdhocModelType.FIELD, rowFlow, columnFlow, crossTableRows, crossTableColumns, field);
		} else {
			return null;
		}
	}

	private LayoutLabel getLayoutLabel(
			AdhocModelType sourceType, LayoutFlowPane flow, LayoutFlowPane oppFlow,
			List<CrossTableField> fields, List<CrossTableField> oppFields, CrossTableField field) {
		
		// initialize
		LayoutLabel label = getLayoutLabel(sourceType, field);
		// set ICON_CLOSE OnMouseClicked
		MaterialDesignIconView ICON_CLOSE = (MaterialDesignIconView) label.getGraphic();
		ICON_CLOSE.setOnMouseClicked(event -> {
			if (MouseButton.PRIMARY == event.getButton()) {
				handleActionDeleteLayoutChild(flow, label, fields, field);
			}
		});
		// set contextMenu
		ContextMenu contextMenu = new ContextMenu();
		// groupType
		Menu groupType = getMenuGroupType(flow, label, field);
		groupType.setVisible(MenuGroupTypeVisible(groupType));
		// delete, move, createFilter
		MenuItem delete = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.DeleteLayoutChild.label"));
		MenuItem moveOpp = new MenuItem(AdhocUtils.getString((columnFlow == flow) ? 
				"P121.CrossTableModel.MenuItem.MoveColumnChildOppsite.label" : "P121.CrossTableModel.MenuItem.MoveRowChildOppsite.label"));
		MenuItem moveLeft = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveLayoutChildLeft.label"));
		MenuItem moveRight = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveLayoutChildRight.label"));
		MenuItem createFilter = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.CreateFilterToPane.label"));
		label.setMenuItemDelete(delete);
		delete.setOnAction(event -> handleActionDeleteLayoutChild(flow, label, fields, field));
		moveOpp.setOnAction(event -> handleActionMoveLayoutChildOppsite(flow, oppFlow, label, fields, oppFields, field));
		label.setMenuItemMoveLeft(moveLeft);
		moveLeft.setVisible(moveLayoutChildLeftVisible(flow, label));
		moveLeft.setOnAction(event -> handleActionMoveLayoutChildLeft(flow, label, fields, field));
		label.setMenuItemMoveRight(moveRight);
		moveRight.setVisible(moveLayoutChildRightVisible(flow, label));
		moveRight.setOnAction(event -> handleActionMoveLayoutChildRight(flow, label, fields, field));
		createFilter.setOnAction(event -> handleActionCreateFilterToPane(field));
		contextMenu.getItems().setAll(delete, groupType, createFilter, moveOpp, moveLeft, moveRight);
		label.setContextMenu(contextMenu);
		// set OnMouseClicked
		label.setOnMouseClicked(event -> {
			if (MouseButton.SECONDARY == event.getButton()) {
				Platform.runLater(() ->{
					groupType.setVisible(MenuGroupTypeVisible(groupType));
					moveLeft.setVisible(moveLayoutChildLeftVisible(flow, label));
					moveRight.setVisible(moveLayoutChildRightVisible(flow, label));
					contextMenu.getItems().setAll(delete, groupType, createFilter, moveOpp, moveLeft, moveRight);
				});
			}
		});
		// set onDragAndDropped
		LayoutBackup<CrossTableField> backup = new LayoutBackup<CrossTableField>(label, sourceType, flow, oppFlow, fields, oppFields, field);
		label.setOnDragDetected(event -> handleActionOnDragDetectedLayoutChild(event, backup));
		label.setOnDragDone(event -> handleActionOnDragDoneLayoutChild(event, backup));
		return label;
	}
	
	private boolean MenuGroupTypeVisible(Menu menu) {
		return !menu.getItems().isEmpty();
	}
	
	private Menu getMenuGroupType(LayoutFlowPane flow, LayoutLabel label, CrossTableField field) {
		Menu menu = new Menu(AdhocUtils.getString("P121.CrossTableModel.Menu.ChangeGroupType.label"));
		List<GroupType> groupTypes = field.getDataType().getGroupTypes();
		if (null != groupTypes) {
			for (GroupType groupType : groupTypes) {
				MenuItem menuItem = new MenuItem(groupType.toString());
				menuItem.setId(groupType.getId());
				menuItem.setOnAction(event -> handleActionChangeGroupType(flow, label, menu, menuItem, field, groupType));
				menu.getItems().add(menuItem);
			}
		}
		if (null != field.getGroupType()) {
			fireMenuItem(menu, field.getGroupType().getId());
		}
		return menu;
	}
	
	private void handleActionChangeGroupType(
			LayoutFlowPane flow, LayoutLabel label, Menu menu, MenuItem menuItem,
			CrossTableField field, GroupType groupType) {
		
		GroupType lastGroupType = field.getGroupType();
		MenuItem lastMenuItem = (MenuItem) getMenuItemById(menu, lastGroupType.getId());
		if (lastGroupType != groupType) {
			StatementFactory.runLater(
					() -> handleActionChangeGroupTypeImpl(flow, label, menu, menuItem, field, lastGroupType, groupType),
					() -> handleActionChangeGroupTypeImpl(flow, label, menu, lastMenuItem, field, groupType, lastGroupType)
			);
		} else {
			AdhocLogService.sleep = true;
			handleActionChangeGroupTypeImpl(flow, label, menu, menuItem, field, lastGroupType, groupType);
			AdhocLogService.sleep = false;
		}
	}
	
	private void handleActionChangeGroupTypeImpl(
			LayoutFlowPane flow, LayoutLabel label, Menu menu, MenuItem menuItem,
			CrossTableField field, GroupType lastGroupType, GroupType groupType) {
		
		Integer index = indexOf(flow, label);
		AdhocLogService.changeGroupType(flow, label, index, lastGroupType, groupType);
		field.setGroupType(groupType);
		CustomTooltip tooltip = (CustomTooltip) label.getTooltip();
		tooltip.getProperty().setGroupType(groupType.toString());
		updateMenuItemIcon(menu, menuItem);
	}
	
	private void handleActionMoveLayoutChildOppsite(
			LayoutFlowPane flow,  LayoutFlowPane oppFlow, LayoutLabel label, 
			List<CrossTableField> fields, List<CrossTableField> oppFields, CrossTableField field) {

		LayoutLabel oppLabel = getLayoutLabel(oppFlow, field);
		Integer indexLabel = oppFlow.getChildren().size();
		Integer indexField = oppFields.size();
		Integer indexLabelUndo = flow.getChildren().indexOf(label);
		Integer indexFieldUndo = fields.indexOf(field);
		StatementFactory.runLater(
				() -> handleActionMoveLayoutChildOppsiteImpl(flow, oppFlow, label, oppLabel, indexLabel, fields, oppFields, field, indexField),
				() -> handleActionMoveLayoutChildOppsiteImpl(oppFlow, flow, oppLabel, label, indexLabelUndo, oppFields, fields, field, indexFieldUndo)
		);
	}
	
	private void handleActionMoveLayoutChildOppsiteImpl(
			LayoutFlowPane flow, LayoutFlowPane oppFlow,
			LayoutLabel label, LayoutLabel oppLabel, Integer indexLabel, 
			List<CrossTableField> fields, List<CrossTableField> oppFields, CrossTableField field, Integer indexField) {
		
		AdhocLogService.moveLayoutChild(flow, oppFlow, label, indexOf(flow, label), indexField);
		flow.getChildren().remove(label);
		fields.remove(field);
		oppFlow.getChildren().add(indexLabel, oppLabel);
		oppFields.add(indexField, field);
	}
	
}
