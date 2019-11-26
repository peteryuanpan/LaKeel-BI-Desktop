package com.legendapl.lightning.adhoc.factory.itemTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.legendapl.lightning.adhoc.adhocView.model.AdhocField;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.CalculateType;
import com.legendapl.lightning.adhoc.common.DataFormat;
import com.legendapl.lightning.adhoc.common.DataType;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.custom.LayoutLabel;
import com.legendapl.lightning.adhoc.custom.LayoutLabel.Note;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.custom.CustomTooltip;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.service.AdhocLogService;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;

/**
 * クロス集計メジャーツリー工場
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/4/1
 */
public class CrossTableValueTreeFactory extends CrossTableItemTreeFactory {
	
	/*-------------------------------------API-----------------------------------------*/

	public void initModel() {
		// value
		setTreeViewSelectedItemProperty(valueTreeView);
		tableItemTreeFactory.removeAllItemInterval();
		valueTreeView.setCellFactory(treeView -> new ValueTreeCell());
		valueTreeView.refresh();
	}

	/*-------------------------------------TreeCell-----------------------------------------*/

	private class ValueTreeCell extends TreeCell<BaseNode> {
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
					moveTreeItemGroupToColumn.setVisible(moveTreeItemToColumnVisible());
					moveTreeItemGroupToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn());
					MenuItem moveTreeItemGroupToRow = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToRow.label"));
					moveTreeItemGroupToRow.setVisible(moveTreeItemToRowVisible());
					moveTreeItemGroupToRow.setOnAction(event -> handleActionMoveTreeItemToRow());
					contextMenu.getItems().setAll(moveTreeItemGroupToColumn, moveTreeItemGroupToRow);
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> {
						if (MouseButton.SECONDARY == event.getButton()) {
							Platform.runLater(() ->{
								moveTreeItemGroupToColumn.setVisible(moveTreeItemToColumnVisible());
								moveTreeItemGroupToRow.setVisible(moveTreeItemToRowVisible());
								contextMenu.getItems().setAll(moveTreeItemGroupToColumn, moveTreeItemGroupToRow);
							});
						} else {
							handleActionOnMouseClickedTreeCell(event);
						}
					});
					// onDragAndDropped
					this.setOnDragDetected(event -> handleActionOnDragDetectedItemTree(event, AdhocModelType.MEASURE, valueTreeView, this));
					this.setOnDragDone(event -> handleActionOnDragDoneItemTree(event, valueTreeView));
				} else if (item instanceof Item) { // Item
					// initialize
					this.setText(item.getLabel());
					this.setTooltip(new Tooltip(item.getResourceId()));
					// contextMenu
					ContextMenu contextMenu = new ContextMenu();
					MenuItem moveTreeItemToColumn = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToColumn.label"));
					moveTreeItemToColumn.setVisible(moveTreeItemToColumnVisible());
					moveTreeItemToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn());
					MenuItem moveTreeItemToRow = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToRow.label"));
					moveTreeItemToRow.setVisible(moveTreeItemToRowVisible());
					moveTreeItemToRow.setOnAction(event -> handleActionMoveTreeItemToRow());
					MenuItem moveTreeItemToField = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveTreeItemToField.label"));
					moveTreeItemToField.visibleProperty().bind(moveTreeItemToOppsiteVisible(valueTreeView));
					moveTreeItemToField.setOnAction(event -> handleActionMoveTreeItemToOppsite(valueTreeView, fieldTreeView));
					MenuItem createFilterToPane = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.CreateFilterToPane.label"));
					createFilterToPane.visibleProperty().bind(createFilterToPaneVisible(valueTreeView));
					createFilterToPane.setOnAction(event -> handleActionCreateFilterToPane(valueTreeView));
					contextMenu.getItems().setAll(moveTreeItemToColumn, moveTreeItemToRow, moveTreeItemToField, createFilterToPane);
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> {
						if (MouseButton.SECONDARY == event.getButton()) {
							Platform.runLater(() ->{
								moveTreeItemToColumn.setVisible(moveTreeItemToColumnVisible());
								moveTreeItemToRow.setVisible(moveTreeItemToRowVisible());
								contextMenu.getItems().setAll(moveTreeItemToColumn, moveTreeItemToRow, moveTreeItemToField, createFilterToPane);
							});
						} else {
							handleActionOnMouseClickedTreeCell(event);
						}
					});
					// onDragAndDropped
					this.setOnDragDetected(event -> handleActionOnDragDetectedItemTree(event, AdhocModelType.MEASURE, valueTreeView, this));
					this.setOnDragDone(event -> handleActionOnDragDoneItemTree(event, valueTreeView));
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

	private boolean moveTreeItemToColumnVisible() {
		return null == AdhocUtils.getFirstObject(rowFlow.getChildren(), GridPane.class);
	}

	private boolean moveTreeItemToRowVisible() {
		return null == AdhocUtils.getFirstObject(columnFlow.getChildren(), GridPane.class);
	}

	private void handleActionMoveTreeItemToColumn() {
		handleActionMoveTreeItemToLayout(columnFlow);
	}

	private void handleActionMoveTreeItemToRow() {
		handleActionMoveTreeItemToLayout(rowFlow);
	}
	
	private void handleActionMoveTreeItemToLayout(LayoutFlowPane flow) {
		// get
		List<CrossTableField> newFields = getFieldsBySelectedItems(AdhocModelType.MEASURE, valueTreeView);
		// handle
		Integer lastGridPaneIndex = AdhocUtils.getLastObjectIndex(flow.getChildren(), GridPane.class);
		Integer indexNode = Math.min(flow.getChildren().size(), 1 + lastGridPaneIndex);
		addLayoutChildren(AdhocModelType.ITEMTREE, AdhocModelType.MEASURE, flow, indexNode, crossTableValues, null, newFields);
	}

	private void handleActionOnMouseClickedTreeCell(MouseEvent event) {
		if (!checkMouseClickedTreeCell(event, AdhocModelType.MEASURE, valueTreeView)) return;
		if (MouseButton.PRIMARY == event.getButton()) {
			if (event.getClickCount() > 1) {
				if (null != AdhocUtils.getFirstObject(rowFlow.getChildren(), GridPane.class)) {
					handleActionMoveTreeItemToRow();
				} else {
					handleActionMoveTreeItemToColumn();
				}
				Platform.runLater(() -> valueTreeView.getSelectionModel().clearSelection());
			}
		}
	}

	/*-------------------------------------Layout-----------------------------------------*/
	
	public GridPane getLayoutPoundParent(LayoutFlowPane flow) {
		if (columnFlow == flow) {
			return getLayoutPoundParent(columnFlow, rowFlow, crossTableValues, crossTableValues);
		} else if (rowFlow == flow) {
			return getLayoutPoundParent(rowFlow, columnFlow, crossTableValues, crossTableValues);
		} else {
			return null;
		}
	}

	private GridPane getLayoutPoundParent(
			LayoutFlowPane flow, LayoutFlowPane oppFlow,
			List<CrossTableField> fields, List<CrossTableField> oppFields) {

		// initialize
		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().setAll("layout-flow-pane-child-grid-pane");
		LayoutLabel label = new LayoutLabel();
		label.getStyleClass().setAll("layout-flow-pane-child-icon-pound-label");
		label.setId(LayoutPoundId);
		label.setModelType(AdhocModelType.LAYOUT_POUND);
		MaterialDesignIconView ICON_POUND = new MaterialDesignIconView(MaterialDesignIcon.POUND);
		ICON_POUND.setId(LayoutPoundId);
		label.setGraphic(ICON_POUND);
		label.setCursor(Cursor.HAND);
		gridPane.getChildren().add(label);
		gridPane.setId(label.getId());
		// set contextMenu
		ContextMenu contextMenu = new ContextMenu();
		MenuItem delete = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.DeleteLayoutChild.label"));
		MenuItem moveOpp = new MenuItem(AdhocUtils.getString(columnFlow == flow ?
				"P121.CrossTableModel.MenuItem.MoveColumnChildOppsite.label" : "P121.CrossTableModel.MenuItem.MoveRowChildOppsite.label"));
		MenuItem moveLeft = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveLayoutChildLeft.label"));
		MenuItem moveRight = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveLayoutChildRight.label"));
		label.setMenuItemDelete(delete);
		delete.setOnAction(event -> handleActionDeleteLayoutChild(flow, gridPane, fields, null));
		moveOpp.setOnAction(event -> handleActionMoveLayoutChildOppsite(flow, oppFlow, fields, oppFields));
		label.setMenuItemMoveLeft(moveLeft);
		moveLeft.setVisible(moveLayoutChildLeftVisible(flow, gridPane));
		moveLeft.setOnAction(event -> handleActionMoveLayoutChildLeft(flow, gridPane, fields, null));
		label.setMenuItemMoveRight(moveRight);
		moveRight.setVisible(moveLayoutChildRightVisible(flow, gridPane));
		moveRight.setOnAction(event -> handleActionMoveLayoutChildRight(flow, gridPane, fields, null));
		contextMenu.getItems().setAll(delete, moveOpp, moveLeft, moveRight);
		label.setContextMenu(contextMenu);
		label.setOnMouseClicked(event -> {
			if (MouseButton.SECONDARY == event.getButton()) {
				Platform.runLater(() ->{
					moveLeft.setVisible(moveLayoutChildLeftVisible(flow, gridPane));
					moveRight.setVisible(moveLayoutChildRightVisible(flow, gridPane));
					contextMenu.getItems().setAll(delete, moveOpp, moveLeft, moveRight);
				});
			}
		});
		// set onDragAndDropped
		LayoutBackup<CrossTableField> backup = new LayoutBackup<CrossTableField>(label, AdhocModelType.LAYOUT_POUND, flow, oppFlow, fields, oppFields, null);
		label.setOnDragDetected(event -> handleActionOnDragDetectedLayoutChild(event, backup));
		label.setOnDragDone(event -> handleActionOnDragDoneLayoutChild(event, backup));
		return gridPane;
	}
	
	public GridPane getLayoutLabelParent(LayoutFlowPane flow, GridPane gridPane) {
		LayoutLabel label = getLayoutLabel(gridPane);
		return getLayoutLabelParent(flow, label);
	}
	
	public GridPane getLayoutLabelParent(LayoutFlowPane flow, LayoutLabel label) {
		if (null != label && label.getField() instanceof CrossTableField) {
			CrossTableField field = (CrossTableField) label.getField();
			GridPane gridPane = getLayoutLabelParent(flow, field);
			LayoutLabel childLabel = getLayoutLabel(gridPane);
			if (null != childLabel) {
				childLabel.updateNote(label.getNote());
				return gridPane;
			}
		}
		return null;
	}
	
	public GridPane getLayoutLabelParent(LayoutFlowPane flow, CrossTableField field) {
		if (columnFlow == flow) {
			return getLayoutLabelParent(AdhocModelType.MEASURE, columnFlow, rowFlow, crossTableValues, crossTableValues, field);
		} else if (rowFlow == flow) {
			return getLayoutLabelParent(AdhocModelType.MEASURE, rowFlow, columnFlow, crossTableValues, crossTableValues, field);
		} else {
			return null;
		}
	}

	private GridPane getLayoutLabelParent(
			AdhocModelType sourceType, LayoutFlowPane flow, LayoutFlowPane oppFlow,
			List<CrossTableField> fields, List<CrossTableField> oppFields, CrossTableField field) {

		// initialize
		GridPane gridPane = new GridPane();
		gridPane.getStyleClass().setAll("layout-flow-pane-child-grid-pane");
		LayoutLabel label = getLayoutLabel(sourceType, field);
		gridPane.getChildren().add(label);
		gridPane.setId(label.getId());
		// set ICON_CLOSE OnMouseClicked
		MaterialDesignIconView ICON_CLOSE = (MaterialDesignIconView) label.getGraphic();
		ICON_CLOSE.setOnMouseClicked(event -> {
			if (MouseButton.PRIMARY == event.getButton()) {
				handleActionDeleteLayoutChild(flow, gridPane, fields, field);
			}
		});
		// set contextMenu
		ContextMenu contextMenu = new ContextMenu();
		// calculateType, dataFormat
		Map<List<DataFormat>, DataFormat> dataFormatMap= getDataFormatMap(field);
		Menu dataFormat = getMenuDataFormat(flow, label, field, dataFormatMap);
		dataFormat.setVisible(MenuDataFormatVisible(dataFormat));
		Menu calculateType = getMenuCalculateType(flow, label, field, dataFormatMap);
		// delete, move, createFilter
		MenuItem delete = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.DeleteLayoutChild.label"));
		MenuItem moveOpp = new MenuItem(AdhocUtils.getString(columnFlow == flow ?
				"P121.CrossTableModel.MenuItem.MoveColumnChildOppsite.label" : "P121.CrossTableModel.MenuItem.MoveRowChildOppsite.label"));
		MenuItem moveLeft = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveLayoutChildLeft.label"));
		MenuItem moveRight = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.MoveLayoutChildRight.label"));
		MenuItem createFilter = new MenuItem(AdhocUtils.getString("P121.CrossTableModel.MenuItem.CreateFilterToPane.label"));
		label.setMenuItemDelete(delete);
		delete.setOnAction(event -> handleActionDeleteLayoutChild(flow, gridPane, fields, field));
		moveOpp.setOnAction(event -> handleActionMoveLayoutChildOppsite(flow, oppFlow, fields, oppFields));
		label.setMenuItemMoveLeft(moveLeft);
		moveLeft.setVisible(moveLayoutChildLeftVisible(flow, gridPane));
		moveLeft.setOnAction(event -> handleActionMoveLayoutChildLeft(flow, gridPane, fields, field));
		label.setMenuItemMoveRight(moveRight);
		moveRight.setVisible(moveLayoutChildRightVisible(flow, gridPane));
		moveRight.setOnAction(event -> handleActionMoveLayoutChildRight(flow, gridPane, fields, field));
		createFilter.setOnAction(event -> handleActionCreateFilterToPane(field));
		contextMenu.getItems().setAll(delete, calculateType, dataFormat, createFilter, moveOpp, moveLeft, moveRight);
		label.setContextMenu(contextMenu);
		// set OnMouseClicked
		label.setOnMouseClicked(event -> {
			if (MouseButton.SECONDARY == event.getButton()) {
				Platform.runLater(() ->{
					dataFormat.setVisible(MenuDataFormatVisible(dataFormat));
					moveLeft.setVisible(moveLayoutChildLeftVisible(flow, gridPane));
					moveRight.setVisible(moveLayoutChildRightVisible(flow, gridPane));
					contextMenu.getItems().setAll(delete, calculateType, dataFormat, createFilter, moveOpp, moveLeft, moveRight);
				});
			}
		});
		// set OnDragAndDropped
		LayoutBackup<CrossTableField> backup = new LayoutBackup<CrossTableField>(label, sourceType, flow, oppFlow, fields, oppFields, field);
		label.setOnDragDetected(event -> handleActionOnDragDetectedLayoutChild(event, backup));
		label.setOnDragDone(event -> handleActionOnDragDoneLayoutChild(event, backup));
		return gridPane;
	}
	
	private Map<List<DataFormat>, DataFormat> getDataFormatMap(CrossTableField field) {
		Map<List<DataFormat>, DataFormat> map = new HashMap<>();
		DataType dataType = getDataType(field);
		DataFormat dataFormat = field.getDataFormat();
		if (null != dataType && null != dataFormat) { // put if needed
			map.put(dataType.getDataFormats(), dataFormat);
		}
		return map;
	}

	private Menu getMenuCalculateType(
			LayoutFlowPane flow, LayoutLabel label, CrossTableField field, 
			Map<List<DataFormat>, DataFormat> dataFormatMap) {
		
		Menu menu = new Menu(AdhocUtils.getString("P121.CrossTableModel.Menu.ChangeCaculateType.label"));
		List<CalculateType> calculations = field.getDataType().getCalculateTypes();
		if (null != calculations) {
			for (CalculateType calculateType : calculations) {
				MenuItem menuItem = new MenuItem(calculateType.toString());
				menuItem.setId(calculateType.getId());
				menuItem.setOnAction(event -> handleActionChangeCalculateType(flow, label, menu, menuItem, field, calculateType, dataFormatMap));
				menu.getItems().add(menuItem);
			}
		}
		if (null != field.getCalculateType()) {
			fireMenuItem(menu, field.getCalculateType().getId());
		}
		return menu;
	}
	
	private void handleActionChangeCalculateType(
			LayoutFlowPane flow, LayoutLabel label, Menu menu, MenuItem menuItem, 
			CrossTableField field, CalculateType calculateType, 
			Map<List<DataFormat>, DataFormat> dataFormatMap) {
		
		CalculateType lastCalculateType = field.getCalculateType();
		MenuItem lastMenuItem = (MenuItem) getMenuItemById(menu, lastCalculateType.getId());
		
		Note lastNote = label.cloneNote();
		
		if (lastCalculateType != calculateType) {
			Note newNote = label.cloneNote();
			newNote.setCalculateType(calculateType.toString());
			StatementFactory.runLater(
					() -> handleActionChangeCalculateTypeImpl(flow, label, newNote, menu, menuItem, field, lastCalculateType, calculateType, dataFormatMap),
					() -> handleActionChangeCalculateTypeImpl(flow, label, lastNote, menu, lastMenuItem, field, calculateType, lastCalculateType, dataFormatMap)
			);
		} else {
			AdhocLogService.sleep = true;
			handleActionChangeCalculateTypeImpl(flow, label, lastNote, menu, menuItem, field, lastCalculateType, calculateType, dataFormatMap);
			AdhocLogService.sleep = false;
		}
	}
	
	private void handleActionChangeCalculateTypeImpl(
			LayoutFlowPane flow, LayoutLabel label, Note note, Menu menu, MenuItem menuItem, 
			CrossTableField field, CalculateType lastCalculateType, CalculateType calculateType, 
			Map<List<DataFormat>, DataFormat> dataFormatMap) {
		
		Integer index = indexOf(flow, label);
		AdhocLogService.changeCalculateType(flow, label, index, lastCalculateType, calculateType);
		
		label.updateNote(note);
		field.setCalculateType(calculateType);
		
		CustomTooltip tooltip = (CustomTooltip) label.getTooltip();
		tooltip.getProperty().setCalculateType(calculateType.toString());
		updateMenuItemIcon(menu, menuItem);
		
		AdhocLogService.sleep = true;
		updateMenuDataFormat(flow, label, field, dataFormatMap);
		AdhocLogService.sleep = false;
	}
	
	private boolean MenuDataFormatVisible(Menu menu) {
		return !menu.getItems().isEmpty();
	}

	private Menu getMenuDataFormat(
			LayoutFlowPane flow, LayoutLabel label, CrossTableField field, 
			Map<List<DataFormat>, DataFormat> dataFormatMap) {
		
		Menu menu = new Menu(AdhocUtils.getString("P121.CrossTableModel.Menu.ChangeDataFormat.label"));
		label.setMenuDataFormat(menu);
		updateDataFormatList(flow, menu, label, field, dataFormatMap);
		return menu;
	}

	private void updateMenuDataFormat(
			LayoutFlowPane flow, LayoutLabel label, CrossTableField field, 
			Map<List<DataFormat>, DataFormat> dataFormatMap) {
		
		// set data format
		DataType dataType = getDataType(field);
		if (null != dataFormatMap.get(dataType.getDataFormats())) {
			field.setDataFormat(dataFormatMap.get(dataType.getDataFormats()));
		} else {
			field.setDataFormat(dataType.getDefaultDataFormat());
		}
		// handle
		updateDataFormatList(flow, label.getMenuDataFormat(), label, field, dataFormatMap);
	}

	private void updateDataFormatList(
			LayoutFlowPane flow, Menu menu, LayoutLabel label, CrossTableField field, 
			Map<List<DataFormat>, DataFormat> dataFormatMap) {
		
		DataType dataType = getDataType(field);
		List<MenuItem> items = new ArrayList<>();
		List<DataFormat> dataFormats = dataType.getDataFormats();
		if (null != dataFormats) {
			for (DataFormat dataFormat : dataFormats) {
				MenuItem menuItem = new MenuItem(dataFormat.toString());
				menuItem.setId(dataFormat.getId());
				menuItem.setOnAction(event -> handleActionChangeDataFormat(flow, label, menu, menuItem, field, dataFormat, dataFormatMap, dataType));
				items.add(menuItem);
			}
		}
		menu.getItems().setAll(items);
		if (null != dataFormatMap.get(dataType.getDataFormats())) {
			fireMenuItem(menu, dataFormatMap.get(dataType.getDataFormats()).getId());
		} else {
			fireMenuItem(menu, dataType.getDefaultDataFormat().getId());
		}
	}
	
	private void handleActionChangeDataFormat(
			LayoutFlowPane flow, LayoutLabel label, Menu menu, MenuItem menuItem, 
			CrossTableField field, DataFormat dataFormat, 
			Map<List<DataFormat>, DataFormat> dataFormatMap, DataType dataType) {
		
		DataFormat lastDataFormat = field.getDataFormat();
		MenuItem lastMenuItem = (MenuItem) getMenuItemById(menu, lastDataFormat.getId());
		if (lastDataFormat != dataFormat) {
			StatementFactory.runLater(
					() -> handleActionChangeDataFormatImpl(flow, label, menu, menuItem, field, lastDataFormat, dataFormat, dataFormatMap, dataType),
					() -> handleActionChangeDataFormatImpl(flow, label, menu, lastMenuItem, field, dataFormat, lastDataFormat, dataFormatMap, dataType)
			);
		} else {
			AdhocLogService.sleep = true;
			handleActionChangeDataFormatImpl(flow, label, menu, menuItem, field, lastDataFormat, dataFormat, dataFormatMap, dataType);
			AdhocLogService.sleep = false;
		}
	}
	
	private void handleActionChangeDataFormatImpl(
			LayoutFlowPane flow, LayoutLabel label, Menu menu, MenuItem menuItem, 
			CrossTableField field, DataFormat lastDataFormat, DataFormat dataFormat, 
			Map<List<DataFormat>, DataFormat> dataFormatMap, DataType dataType) {
		
		Integer index = indexOf(flow, label);
		AdhocLogService.changeDataFormat(flow, label, index, lastDataFormat, dataFormat);
		field.setDataFormat(dataFormat);
		dataFormatMap.put(dataType.getDataFormats(), dataFormat); // put
		CustomTooltip tooltip = (CustomTooltip) label.getTooltip();
		tooltip.getProperty().setDataFormat(dataFormat.toString());
		updateMenuItemIcon(menu, menuItem);
	}
	
	private void handleActionMoveLayoutChildOppsite(
			LayoutFlowPane flow, LayoutFlowPane oppFlow, List<CrossTableField> fields, List<CrossTableField> oppFields) {

		List<Node> nodes = AdhocUtils.getAllTargetChild(flow.getChildren(), GridPane.class);
		List<Node> oppNodes = new ArrayList<>();
		oppNodes.add(getLayoutPoundParent(oppFlow));
		for (int i = 1; i < nodes.size(); i ++) {
			oppNodes.add(getLayoutLabelParent(oppFlow, (GridPane) nodes.get(i)));
		}
		Integer indexNodes = oppFlow.getChildren().size();
		Integer indexNodesUndo = flow.getChildren().indexOf(nodes.get(0));
		StatementFactory.runLater(
				() -> handleActionMoveLayoutChildOppsiteImpl(flow, oppFlow, nodes, oppNodes, indexNodes),
				() -> handleActionMoveLayoutChildOppsiteImpl(oppFlow, flow, oppNodes, nodes, indexNodesUndo)
		);
	}
	
	private void handleActionMoveLayoutChildOppsiteImpl(
			LayoutFlowPane flow, LayoutFlowPane oppFlow, 
			List<Node> nodes, List<Node> oppNodes, Integer indexNodes) {
		
		AdhocLogService.moveLayoutChild(flow, oppFlow, nodes, indexOf(flow, nodes.get(0)), indexNodes);
		flow.getChildren().removeAll(nodes);
		oppFlow.getChildren().addAll(indexNodes, oppNodes);
	}
	
	public static DataType getDataType(AdhocField field) {
		switch (field.getModelType()) {
		case FIELD:
			return field.getDataType();
		case MEASURE:
			switch(field.getCalculateType()) {
			case None:
				return DataType.UNKNOW;
			case Average:
				return DataType.FLOAT;
			case CountAll:
			case CountDistinct:
				return DataType.INTEGER;
			default:
				return field.getDataType();
			}
		default:
			return DataType.UNKNOW;
		}
	}

}
