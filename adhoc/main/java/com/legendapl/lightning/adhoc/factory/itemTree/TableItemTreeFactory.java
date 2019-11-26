package com.legendapl.lightning.adhoc.factory.itemTree;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.Geometry;
import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.CalculateType;
import com.legendapl.lightning.adhoc.common.DataFormat;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.custom.LayoutLabel;
import com.legendapl.lightning.adhoc.custom.LayoutLabel.Note;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.custom.CustomTooltip;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.service.AdhocLogService;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Region;

/**
 * テーブルアイテムツリー工場
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/4/1
 */
public class TableItemTreeFactory extends ItemTreeFactory {
	
	/*-------------------------------------Data-----------------------------------------*/
	
	private static Integer lastInsertIndex = -1;
	private static Boolean isAValidDragDropOperation = false;
	private static LayoutBackup<TableField> backup = null;
	
	public static class ItemInterval extends Item {
		public ItemInterval() {
			super();
			this.setLabel(AdhocUtils.getString("P121.table.interval"));
			this.setResourceId(TableIntervalId);
		}
	}
	
	/*-------------------------------------API-----------------------------------------*/
	
	public void initModel() {
		// field
		setTreeViewSelectedItemProperty(fieldTreeView);
		fieldTreeView.setCellFactory(treeView -> new FieldTreeCell());
		fieldTreeView.refresh();
		// value
		if (!containItemInterval()) {
			valueSuperRoot.getChildren().add(new TreeItem<>(new ItemInterval()));
		}
		setTreeViewSelectedItemProperty(valueTreeView);
		valueTreeView.setCellFactory(treeView -> new ValueTreeCell());
		valueTreeView.refresh();
	}
	
	public Boolean containItemInterval() {
		for (TreeItem<BaseNode> child : valueSuperRoot.getChildren()) {
			if (child.getValue() instanceof ItemInterval) {
				return true;
			}
		}
		return false;
	}
	
	public void removeAllItemInterval() {
		List<TreeItem<BaseNode>> removeChildren = new ArrayList<>();
		valueSuperRoot.getChildren().forEach(child -> {
			if (child.getValue() instanceof ItemInterval) {
				removeChildren.add(child);
			}
		});
		valueSuperRoot.getChildren().removeAll(removeChildren);
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
					MenuItem moveTreeItemGroupToColumn = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemGroupToColumn.label"));
					moveTreeItemGroupToColumn.visibleProperty().bind(moveTreeItemGroupToColumnVisible(fieldTreeView));
					moveTreeItemGroupToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn(AdhocModelType.FIELD, fieldTreeView));
					contextMenu.getItems().setAll(moveTreeItemGroupToColumn);
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> handleActionOnMouseClickedTreeCell(event, AdhocModelType.FIELD, fieldTreeView));
					// onDragAndDropped
					this.setOnDragDetected(event -> handleActionOnDragDetectedItemTree(event, AdhocModelType.FIELD, fieldTreeView, this));
					this.setOnDragDone(event -> handleActionOnDragDoneItemTree(event, fieldTreeView));
				} else if (item instanceof Item) { // Item
					// initialize
					this.setText(item.getLabel());
					this.setTooltip(new Tooltip(item.getResourceId()));
					// contextMenu
					ContextMenu contextMenu = new ContextMenu();
					MenuItem moveTreeItemGroupToColumn = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemGroupToColumn.label"));
					moveTreeItemGroupToColumn.visibleProperty().bind(moveTreeItemGroupToColumnVisible(fieldTreeView));
					moveTreeItemGroupToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn(AdhocModelType.FIELD, fieldTreeView));
					MenuItem moveTreeItemToColumn = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemToColumn.label"));
					moveTreeItemToColumn.visibleProperty().bind(moveTreeItemToLayoutVisible(fieldTreeView));
					moveTreeItemToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn(AdhocModelType.FIELD, fieldTreeView));
					MenuItem moveTreeItemToGroup = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemToGroup.label"));
					moveTreeItemToGroup.visibleProperty().bind(moveTreeItemToLayoutVisible(fieldTreeView));
					moveTreeItemToGroup.setOnAction(event -> handleActionMoveTreeItemToGroup(AdhocModelType.FIELD, fieldTreeView));
					MenuItem moveTreeItemToMeasure = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemToMeasure.label"));
					moveTreeItemToMeasure.visibleProperty().bind(moveTreeItemToOppsiteVisible(fieldTreeView));
					moveTreeItemToMeasure.setOnAction(event -> handleActionMoveTreeItemToOppsite(fieldTreeView, valueTreeView));
					MenuItem createFilterToPane = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.CreateFilterToPane.label"));
					createFilterToPane.visibleProperty().bind(createFilterToPaneVisible(fieldTreeView));
					createFilterToPane.setOnAction(event -> handleActionCreateFilterToPane(fieldTreeView));
					MenuItem createSortToPane = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.CreateSortToPane.label"));
					createSortToPane.visibleProperty().bind(createSortToPaneVisible(fieldTreeView));
					createSortToPane.setOnAction(event -> handleActionCreateSortToPane(fieldTreeView));
					contextMenu.getItems().setAll(moveTreeItemGroupToColumn, moveTreeItemToColumn, moveTreeItemToGroup, moveTreeItemToMeasure, createFilterToPane/*, createSortToPane*/); // TODO
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> handleActionOnMouseClickedTreeCell(event, AdhocModelType.FIELD, fieldTreeView));
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
					MenuItem moveTreeItemGroupToColumn = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemGroupToColumn.label"));
					moveTreeItemGroupToColumn.visibleProperty().bind(moveTreeItemGroupToColumnVisible(valueTreeView));
					moveTreeItemGroupToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn(AdhocModelType.MEASURE, valueTreeView));
					contextMenu.getItems().setAll(moveTreeItemGroupToColumn);
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> handleActionOnMouseClickedTreeCell(event, AdhocModelType.MEASURE, valueTreeView));
					// onDragAndDropped
					this.setOnDragDetected(event -> handleActionOnDragDetectedItemTree(event, AdhocModelType.MEASURE, valueTreeView, this));
					this.setOnDragDone(event -> handleActionOnDragDoneItemTree(event, valueTreeView));
				} else if (item instanceof Item) { // Item
					// initialize
					this.setText(item.getLabel());
					this.setTooltip(new Tooltip(item.getResourceId()));
					// contextMenu
					ContextMenu contextMenu = new ContextMenu();
					MenuItem moveTreeItemGroupToColumn = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemGroupToColumn.label"));
					moveTreeItemGroupToColumn.visibleProperty().bind(moveTreeItemGroupToColumnVisible(valueTreeView));
					moveTreeItemGroupToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn(AdhocModelType.MEASURE, valueTreeView));
					MenuItem moveTreeItemToColumn = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemToColumn.label"));
					moveTreeItemToColumn.visibleProperty().bind(moveTreeItemToLayoutVisible(valueTreeView));
					moveTreeItemToColumn.setOnAction(event -> handleActionMoveTreeItemToColumn(AdhocModelType.MEASURE, valueTreeView));
					MenuItem moveTreeItemToField = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveTreeItemToField.label"));
					moveTreeItemToField.visibleProperty().bind(moveTreeItemToOppsiteVisible(valueTreeView));
					moveTreeItemToField.setOnAction(event -> handleActionMoveTreeItemToOppsite(valueTreeView, fieldTreeView));
					MenuItem createFilterToPane = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.CreateFilterToPane.label"));
					createFilterToPane.visibleProperty().bind(createFilterToPaneVisible(valueTreeView));
					createFilterToPane.setOnAction(event -> handleActionCreateFilterToPane(valueTreeView));
					MenuItem createSortToPane = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.CreateSortToPane.label"));
					createSortToPane.visibleProperty().bind(createSortToPaneVisible(valueTreeView));
					createSortToPane.setOnAction(event -> handleActionCreateSortToPane(valueTreeView));
					contextMenu.getItems().setAll(moveTreeItemGroupToColumn, moveTreeItemToColumn, moveTreeItemToField, createFilterToPane/*, createSortToPane*/); // TODO
					this.setContextMenu(contextMenu);
					// onMouseClicked
					this.setOnMouseClicked(event -> handleActionOnMouseClickedTreeCell(event, AdhocModelType.MEASURE, valueTreeView));
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

	private ObservableValue<? extends Boolean> moveTreeItemGroupToColumnVisible(TreeView<BaseNode> treeView) {
		return new BooleanBinding() {
			{
				super.bind(treeView.getSelectionModel().getSelectedItems());;
			}
			@Override protected boolean computeValue() {
				List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
				for (TreeItem<BaseNode> selectedItem : selectedItems) {
					if (selectedItem.getValue() instanceof DBNode) {
						return true;
					}
				}
				return false;
			}
		};
	}

	private ObservableValue<? extends Boolean> moveTreeItemToLayoutVisible(TreeView<BaseNode> treeView) {
		return new BooleanBinding() {
			{
				super.bind(treeView.getSelectionModel().getSelectedItems());;
			}
			@Override protected boolean computeValue() {
				List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
				for (TreeItem<BaseNode> selectedItem : selectedItems) {
					if (selectedItem.getValue() instanceof DBNode) {
						return false;
					}
				}
				return true;
			}
		};
	}

	private void handleActionMoveTreeItemToColumn(AdhocModelType sourceType, TreeView<BaseNode> treeView) {
		handleActionMoveTreeItemToLayout(sourceType, treeView, columnFlow, tableColumns);
	}

	private void handleActionMoveTreeItemToGroup(AdhocModelType sourceType, TreeView<BaseNode> treeView) {
		handleActionMoveTreeItemToLayout(sourceType, treeView, rowFlow, tableRows);
	}
	
	private void handleActionMoveTreeItemToLayout(
			AdhocModelType sourceType, TreeView<BaseNode> treeView,
			LayoutFlowPane flow, List<TableField> fields) {
		
		List<TableField> newFields = getFieldsBySelectedItems(sourceType, treeView);
		Integer indexNode = flow.getChildren().size();
		addLayoutChildren(AdhocModelType.ITEMTREE, sourceType, flow, indexNode, fields, null, newFields);
	}
	
	private List<TableField> getFieldsBySelectedItems(AdhocModelType sourceType, TreeView<BaseNode> treeView) {
		List<TableField> newFields = new ArrayList<>();
		List<Field> fields = getFieldsBySelectedItems(treeView);
		fields.forEach(f -> {
			Item item = getItemByResId(treeView.getRoot(), f.getResourceId());
			TableField field;
			if (f instanceof TableField) {
				field = new TableField((TableField)f, sourceType, item);
			} else {
				field = new TableField(f, sourceType, item);
			}
			newFields.add(field);
		});
		return newFields;
	}
	
	@Override protected List<Field> getFieldsByRoot(TreeItem<BaseNode> root) {
		List<Field> fields = super.getFieldsByRoot(root);
		AdhocUtils.getAllItems(root).forEach(item -> {
			if (item.getValue() instanceof ItemInterval) {
				fields.add(getTableFieldInterval());
			}
		});
		return fields;
	}
	
	private TableField getTableFieldInterval() {
		TableField field = new TableField();
		field.setInterval(true);
		field.setLabel(AdhocUtils.getString("P121.table.interval"));
		field.setResourceId(TableIntervalId);
		field.setModelType(AdhocModelType.MEASURE);
		return field;
	}

	private void handleActionOnMouseClickedTreeCell(MouseEvent event, AdhocModelType sourceType, TreeView<BaseNode> treeView) {
		if (!checkMouseClickedTreeCell(event, sourceType, treeView)) return;
		if (MouseButton.PRIMARY == event.getButton()) {
			if (event.getClickCount() > 1) {
				handleActionMoveTreeItemToColumn(sourceType, treeView);
				treeView.getSelectionModel().clearSelection();
			}
		}
	}
	
	private void handleActionOnDragDetectedItemTree(
			MouseEvent event, AdhocModelType sourceType, 
			TreeView<BaseNode> treeView, TreeCell<BaseNode> treeCell) {
		
		if (AdhocModelType.FIELD == sourceType || AdhocModelType.MEASURE == sourceType) {
			TreeItem<BaseNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
			List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
			if (null != selectedItem && !selectedItems.isEmpty()) {
				// initialize
				List<TableField> fields = getFieldsBySelectedItems(sourceType, treeView);
				backup = new LayoutBackup<TableField>(null, sourceType, columnFlow, rowFlow, tableColumns, tableRows, fields.get(0));
				backup.label = getLayoutLabel(sourceType, fields.get(0));
				backup.label.setGraphic(null);
				backup.label.setText(1 == selectedItems.size() ?
						selectedItem.getValue().getLabel() : 
						AdhocUtils.format(AdhocUtils.getString("P121.CrossTableModel.Constants.ManyItemsSelected"), selectedItems.size()));
				backup.selectedFields = fields;
				lastInsertIndex = -1;
				
				// handleAction
				columnFlow.setOnDragOver(e -> handleActionFromItemTreeOnDragOverFlowPane(e, columnFlow));
				columnFlow.setOnDragDropped(e -> handleActionFromItemTreeOnDragDroppedFlowPane(e, columnFlow));
				rowFlow.setOnDragOver(e -> handleActionFromItemTreeOnDragOverFlowPane(e, rowFlow));
				rowFlow.setOnDragDropped(e -> handleActionFromItemTreeOnDragDroppedFlowPane(e, rowFlow));
				
				// create dragBoard
				Dragboard dragBoard = treeCell.startDragAndDrop(TransferMode.COPY_OR_MOVE);
				dragBoard.setDragView((new Scene(backup.label)).getRoot().snapshot(null, null), 0, event.getY());
		        ClipboardContent clipboardContent = new ClipboardContent();
		        clipboardContent.putString(backup.label.getId());
		        dragBoard.setContent(clipboardContent);
			}
		}
	}
	
	private void handleActionFromItemTreeOnDragOverFlowPane(DragEvent event, LayoutFlowPane flow) {
		handleActionFromAnyOnDragOverFlowPane(event, flow);
	}
	
	private void handleActionFromItemTreeOnDragDroppedFlowPane(DragEvent event, LayoutFlowPane flow) {
		Platform.runLater(() -> {
			if (rowFlow == flow && AdhocModelType.MEASURE == backup.sourceType) { // ※グループにメジャーを追加できません。
				AlertWindowService.showInfo(AdhocUtils.getString("INFO_CANNOT_ADD_MEASURE_TO_GROUP"));
				return;
			}
			
			Integer index = AdhocUtils.getLastObjectIndex(flow.getChildren(), EmptyGridPane.class);
			if (flow == backup.flow) {
				addLayoutChildren(AdhocModelType.ITEMTREE, backup.sourceType, backup.flow, index, 
						backup.fields, backup.field, backup.selectedFields);
				return;
			} else {
				addLayoutChildren(AdhocModelType.ITEMTREE, backup.sourceType, backup.oppFlow, index,
						backup.oppFields, backup.field, backup.selectedFields);
				return;
			}
		});
	}
	
	private void handleActionOnDragDoneItemTree(DragEvent event, TreeView<BaseNode> treeView) {
		Platform.runLater(() -> {
			treeView.getSelectionModel().clearSelection();
		});
	}
	
	/*-------------------------------------Layout-----------------------------------------*/
	
	public LayoutLabel getLayoutLabel(AdhocModelType sourceType, LayoutFlowPane flow, LayoutLabel label) {
		if (null != label && label.getField() instanceof TableField) {
			TableField field = (TableField) label.getField();
			LayoutLabel newLabel = getLayoutLabel(sourceType, flow, field);
			if (null != newLabel) {
				TableField newField = (TableField) label.getField();
				newLabel.updateNote(label.getNote());
				newLabel.getNote().setVisible(columnFlow == flow && newField.isCalculated());
				return newLabel;
			}
		}
		return null;
	}
	
	public LayoutLabel getLayoutLabel(AdhocModelType sourceType, LayoutFlowPane flow, TableField field) {
		if (columnFlow == flow) {
			return getLayoutLabel(sourceType, columnFlow, rowFlow, tableColumns, tableRows, field);
		} else if (rowFlow == flow) {
			return getLayoutLabel(sourceType, rowFlow, columnFlow, tableRows, tableColumns, field);
		} else {
			return null;
		}
	}
	
	private LayoutLabel getLayoutLabel(
			AdhocModelType sourceType, LayoutFlowPane flow, LayoutFlowPane oppFlow,
			List<TableField> fields, List<TableField> oppFields, TableField field) {
		
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
		// calculateSwitch, calculateType, dataFormat
		MenuItem calculateSwitch = new MenuItem();
		Menu calculateType = getMenuCalculateType(flow, label, field);
		calculateSwitch.setOnAction(event -> handleActionChangeCalculateSwitch(calculateSwitch, calculateType, flow, label, field));
		updateMenuItemCalculate(calculateSwitch, calculateType, flow, label, field);
		Menu dataFormat = getMenuDataFormat(flow, label, field);
		dataFormat.setVisible(MenuDataFormatVisible(dataFormat, field));
		// delete, move, createFilter
		MenuItem delete = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.DeleteLayoutChild.label"));
		MenuItem moveOpp = new MenuItem(AdhocUtils.getString((columnFlow == flow) ?
				"P121.TableModel.MenuItem.MoveColumnChildOppsite.label" : "P121.TableModel.MenuItem.MoveGroupChildOppsite.label"));
		MenuItem moveLeft = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveLayoutChildLeft.label"));
		MenuItem moveRight = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.MoveLayoutChildRight.label"));
		MenuItem createFilter = new MenuItem(AdhocUtils.getString("P121.TableModel.MenuItem.CreateFilterToPane.label"));
		label.setMenuItemDelete(delete);
		delete.setOnAction(event -> handleActionDeleteLayoutChild(flow, label, fields, field));
		moveOpp.setVisible(moveColumnChildOppsiteVisible(sourceType, field));
		moveOpp.setOnAction(event -> handleActionMoveLayoutChildOppsite(sourceType, flow, oppFlow, label, fields, oppFields, field));
		label.setMenuItemMoveLeft(moveLeft);
		moveLeft.setVisible(moveLayoutChildLeftVisible(flow, label));
		moveLeft.setOnAction(event -> handleActionMoveLayoutChildLeft(flow, label, fields, field));
		moveRight.setVisible(moveLayoutChildRightVisible(flow, label));
		label.setMenuItemMoveRight(moveRight);
		moveRight.setOnAction(event -> handleActionMoveLayoutChildRight(flow, label, fields, field));
		createFilter.setVisible(createFilterToPaneVisible(field));
		createFilter.setOnAction(event -> handleActionCreateFilterToPane(field));
		contextMenu.getItems().setAll(delete, calculateSwitch, calculateType, dataFormat, createFilter, moveOpp, moveLeft, moveRight);
		label.setContextMenu(contextMenu);
		// set OnMouseClicked
		label.setOnMouseClicked(event -> {
			if (MouseButton.SECONDARY == event.getButton()) {
				updateMenuItemCalculate(calculateSwitch, calculateType, flow, label, field);
				dataFormat.setVisible(MenuDataFormatVisible(dataFormat, field));
				moveOpp.setVisible(moveColumnChildOppsiteVisible(sourceType, field));
				moveLeft.setVisible(moveLayoutChildLeftVisible(flow, label));
				moveRight.setVisible(moveLayoutChildRightVisible(flow, label));
				createFilter.setVisible(createFilterToPaneVisible(field));
				contextMenu.getItems().setAll(delete, calculateSwitch, calculateType, dataFormat, createFilter, moveOpp, moveLeft, moveRight);
			}
		});
		// set DragAndDropped
		LayoutBackup<TableField> backup = new LayoutBackup<TableField>(label, sourceType, flow, oppFlow, fields, oppFields, field);
		label.setOnDragDetected(event -> handleActionOnDragDetectedLayoutChild(event, backup));
		label.setOnDragDone(event -> handleActionOnDragDoneLayoutChild(event, backup));
		return label;
	}
	
	private void handleActionChangeCalculateSwitch(
			MenuItem calculateSwitch, Menu calculateType, 
			LayoutFlowPane flow, LayoutLabel label, TableField field) {
		
		StatementFactory.runLater(
				() -> handleActionChangeCalculateSwitchImpl(calculateSwitch, calculateType, flow, label, field),
				() -> handleActionChangeCalculateSwitchImpl(calculateSwitch, calculateType, flow, label, field)
		);
	}
	
	private void handleActionChangeCalculateSwitchImpl(
			MenuItem calculateSwitch, Menu calculateType, 
			LayoutFlowPane flow, LayoutLabel label, TableField field) {

		field.setCalculated(!field.isCalculated());
		Integer index = indexOf(flow, label);
		
		if (field.isCalculated()) {
			AdhocLogService.addCalculateSwitch(flow, label, index);
			label.getNote().setVisible(true);
		} else {
			AdhocLogService.deleteCalculateSwitch(flow, label, index);
			label.getNote().setVisible(false);
		}
		updateMenuItemCalculate(calculateSwitch, calculateType, flow, label, field);
	}
	
	private void updateMenuItemCalculate(
			MenuItem calculateSwitch, Menu calculateType, 
			LayoutFlowPane flow, LayoutLabel label, TableField field) {
		
		if (field.isCalculated()) {
			calculateSwitch.setText(AdhocUtils.getString("P121.TableModel.MenuItem.DeleteCalculateSwitch.label"));
			calculateSwitch.setVisible(columnFlow == flow && !field.isInterval());
			calculateType.setVisible(calculateSwitch.isVisible() && !field.isInterval());
			
			String name = field.getCalculateType().toString();
			CustomTooltip tooltip = (CustomTooltip) label.getTooltip();
			tooltip.getProperty().setCalculateType(calculateType.isVisible() ? name: null);
			
		} else {
			calculateSwitch.setText(AdhocUtils.getString("P121.TableModel.MenuItem.AddCalculateSwitch.label"));
			calculateSwitch.setVisible(columnFlow == flow && !field.isInterval());
			calculateType.setVisible(false);
			
			CustomTooltip tooltip = (CustomTooltip) label.getTooltip();
			tooltip.getProperty().setCalculateType(null);
		}
	}
	
	private Menu getMenuCalculateType(LayoutFlowPane flow, LayoutLabel label, TableField field) {
		Menu menu = new Menu(AdhocUtils.getString("P121.TableModel.Menu.ChangeCaculateType.label"));
		List<CalculateType> calculations = field.getDataType().getCalculateTypes();
		if (null != calculations) {
			for (CalculateType calculateType : calculations) {
				MenuItem menuItem = new MenuItem(calculateType.toString());
				menuItem.setId(calculateType.getId());
				menuItem.setOnAction(event -> handleActionChangeCalculateType(flow, label, menu, menuItem, field, calculateType));
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
			TableField field, CalculateType calculateType) {
		
		CalculateType lastCalculateType = field.getCalculateType();
		MenuItem lastMenuItem = (MenuItem) getMenuItemById(menu, lastCalculateType.getId());
		
		Note lastNote = label.cloneNote();
		
		if (lastCalculateType != calculateType) {
			Note newNote = label.cloneNote();
			newNote.setCalculateType(calculateType.toString());
			StatementFactory.runLater(
					() -> handleActionChangeCalculateTypeImpl(flow, label, newNote, menu, menuItem, field, lastCalculateType, calculateType),
					() -> handleActionChangeCalculateTypeImpl(flow, label, lastNote, menu, lastMenuItem, field, calculateType, lastCalculateType)
			);
		} else {
			AdhocLogService.sleep = true;
			handleActionChangeCalculateTypeImpl(flow, label, lastNote, menu, menuItem, field, lastCalculateType, calculateType);
			AdhocLogService.sleep = false;
		}
	}
	
	private void handleActionChangeCalculateTypeImpl(
			LayoutFlowPane flow, LayoutLabel label, Note note, Menu menu, MenuItem menuItem,
			TableField field, CalculateType lastCalculateType, CalculateType calculateType) {
		
		Integer index = indexOf(flow, label);
		AdhocLogService.changeCalculateType(flow, label, index, lastCalculateType, calculateType);
		
		label.updateNote(note);
		field.setCalculateType(calculateType);
		
		CustomTooltip tooltip = (CustomTooltip) label.getTooltip();
		tooltip.getProperty().setCalculateType(calculateType.toString());
		updateMenuItemIcon(menu, menuItem);
	}
	
	private boolean MenuDataFormatVisible(Menu menu, TableField field) {
		return !menu.getItems().isEmpty() && !field.isInterval();
	}
	
	private Menu getMenuDataFormat(LayoutFlowPane flow, LayoutLabel label, TableField field) {
		Menu menu = new Menu(AdhocUtils.getString("P121.TableModel.Menu.ChangeDataFormat.label"));
		label.setMenuDataFormat(menu);
		List<DataFormat> dataFormats = field.getDataType().getDataFormats();
		if (null != dataFormats) {
			for (DataFormat dataFormat : dataFormats) {
				MenuItem menuItem = new MenuItem(dataFormat.toString());
				menuItem.setId(dataFormat.getId());
				menuItem.setOnAction(event -> handleActionChangeDataFormat(flow, label, menu, menuItem, field, dataFormat));
				menu.getItems().add(menuItem);
			}
		}
		if (null != field.getDataFormat()) {
			fireMenuItem(menu, field.getDataFormat().getId());
		}
		return menu;
	}
	
	private void handleActionChangeDataFormat(
			LayoutFlowPane flow, LayoutLabel label, Menu menu, MenuItem menuItem, 
			TableField field, DataFormat dataFormat) {
		
		DataFormat lastDataFormat = field.getDataFormat();
		MenuItem lastMenuItem = (MenuItem) getMenuItemById(menu, lastDataFormat.getId());
		if (lastDataFormat != dataFormat) {
			StatementFactory.runLater(
					() -> handleActionChangeDataFormatImpl(flow, label, menu, menuItem, field, lastDataFormat, dataFormat),
					() -> handleActionChangeDataFormatImpl(flow, label, menu, lastMenuItem, field, dataFormat, lastDataFormat)
			);
		} else {
			AdhocLogService.sleep = true;
			handleActionChangeDataFormatImpl(flow, label, menu, menuItem, field, lastDataFormat, dataFormat);
			AdhocLogService.sleep = false;
		}
	}
	
	private void handleActionChangeDataFormatImpl(
			LayoutFlowPane flow, LayoutLabel label, Menu menu, MenuItem menuItem, 
			TableField field, DataFormat lastDataFormat, DataFormat dataFormat) {
		
		Integer index = indexOf(flow, label);
		AdhocLogService.changeDataFormat(flow, label, index, lastDataFormat, dataFormat);
		field.setDataFormat(dataFormat);
		CustomTooltip tooltip = (CustomTooltip) label.getTooltip();
		tooltip.getProperty().setDataFormat(dataFormat.toString());
		updateMenuItemIcon(menu, menuItem);
	}
	
	private boolean moveColumnChildOppsiteVisible(AdhocModelType sourceType, TableField field) {
		return AdhocModelType.FIELD == sourceType && !field.isInterval();
	}

	private void handleActionMoveLayoutChildOppsite(
			AdhocModelType type,
			LayoutFlowPane flow, LayoutFlowPane oppFlow, LayoutLabel label,
			List<TableField> fields, List<TableField> oppFields, TableField field) {

		LayoutLabel oppLabel = getLayoutLabel(type, oppFlow, label);
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
			List<TableField> fields, List<TableField> oppFields, TableField field, Integer indexField) {
		
		AdhocLogService.moveLayoutChild(flow, oppFlow, label, indexOf(flow, label), indexField);
		flow.getChildren().remove(label);
		fields.remove(field);
		if (columnFlow == flow && null != getFieldByResId(oppFields, field.getResourceId())) { // ※同じアイテムを2回以上追加することはできません
			return;
		}
		oppFlow.getChildren().add(indexLabel, oppLabel);
		oppFields.add(indexField, field);
	}
	
	private boolean createFilterToPaneVisible(TableField field) {
		return !field.isInterval();
	}
	
	private void handleActionOnDragDetectedLayoutChild(MouseEvent event, LayoutBackup<TableField> _backup) {
		// initialize
		backup = _backup;
		backup.index = backup.flow.getChildren().indexOf(backup.label);
		lastInsertIndex = -1;
		isAValidDragDropOperation = false;
		// handleAction
		backup.flow.setOnDragOver(e -> handleActionFromLayoutOnDragOverFlowPane(e, backup.flow));
		backup.flow.setOnDragDropped(e -> handleActionFromLayoutOnDragDroppedFlowPane(e, backup.flow));
		backup.oppFlow.setOnDragOver(e -> handleActionFromLayoutOnDragOverFlowPane(e, backup.oppFlow));
		backup.oppFlow.setOnDragDropped(e -> handleActionFromLayoutOnDragDroppedFlowPane(e, backup.oppFlow));
		// create dragBoard
		Dragboard dragBoard = backup.label.startDragAndDrop(TransferMode.COPY_OR_MOVE);
		dragBoard.setDragView(backup.label.snapshot(null, null), event.getX(), event.getY());
        ClipboardContent clipboardContent = new ClipboardContent();
        clipboardContent.putString(backup.label.getId());
        dragBoard.setContent(clipboardContent);
		// fire MenuItem delete
        StatementFactory.onlyPush = true;
        fireMenuItem(backup.label.getMenuItemDelete());
	}
	
	private void handleActionFromLayoutOnDragOverFlowPane(DragEvent event, LayoutFlowPane flow) {
		handleActionFromAnyOnDragOverFlowPane(event, flow);
	}
	
	private void handleActionFromLayoutOnDragDroppedFlowPane(DragEvent event, LayoutFlowPane flow) {
		Platform.runLater(() -> {
			if (columnFlow == backup.flow && rowFlow == flow) {
				if (null != getFieldByResId(backup.oppFields, backup.field.getResourceId())) { // ※すでにグループに存在しているフィールドは移動できません。
					isAValidDragDropOperation = false;
					AlertWindowService.showInfo(AdhocUtils.getString("INFO_CANNOT_ADD_SAME_FIELD_TO_GROUP"));
					return;
				}
				if (AdhocModelType.MEASURE == backup.sourceType) { // ※グループにメジャーを追加できません。
					isAValidDragDropOperation = false;
					AlertWindowService.showInfo(AdhocUtils.getString("INFO_CANNOT_ADD_MEASURE_TO_GROUP"));
					return;
				}
			}
			
			Integer index = AdhocUtils.getLastObjectIndex(flow.getChildren(), EmptyGridPane.class);
			if (flow.getChildren().size() > 0 && flow.getChildren().size() == index) { // return to back
				isAValidDragDropOperation = false;
				return;
			}
			
			if (flow == backup.flow && index == backup.index) { // return to back
				isAValidDragDropOperation = false;
				return;
			}
			
			if (flow == backup.flow) {
				isAValidDragDropOperation = true;
				addLayoutChildren(AdhocModelType.LAYOUT, backup.sourceType, backup.flow, index,
						backup.fields, backup.field, backup.selectedFields);
				return;
			} else {
				isAValidDragDropOperation = true;
				addLayoutChildren(AdhocModelType.LAYOUT, backup.sourceType, backup.oppFlow, index, 
						backup.oppFields, backup.field, backup.selectedFields);
				return;
			}
		});
	}
	
	private void handleActionOnDragDoneLayoutChild(DragEvent event, LayoutBackup<TableField> backup) {
		Platform.runLater(() -> {
			if (!isAValidDragDropOperation) {
				addLayoutChildren(AdhocModelType.LAYOUT, backup.sourceType, backup.flow, backup.index,
						backup.fields, backup.field, backup.selectedFields);
			}
		});
	}
	
	private void addLayoutChildren(
			AdhocModelType modelType, AdhocModelType sourceType, LayoutFlowPane flow, Integer index, 
			List<TableField> fields, TableField field, List<TableField> selectedFields) {
		
		AdhocUtils.removeAllTargetChild(columnFlow.getChildren(), EmptyGridPane.class);
		AdhocUtils.removeAllTargetChild(rowFlow.getChildren(), EmptyGridPane.class);
		
		switch (modelType) {
		case LAYOUT:
			LayoutFlowPane backupFlow = backup.flow;
			Integer backupIndex = backup.index;
			Node insertNode = getLayoutLabel(sourceType, flow, backup.label);
			handleActionAddLayoutChild(flow, insertNode, index, fields, field, index);
			StatementFactory.handle(isAValidDragDropOperation,
					() -> AdhocLogService.moveLayoutChild(backupFlow, flow, insertNode, backupIndex, index),
					() -> AdhocLogService.moveLayoutChild(flow, backupFlow, insertNode, index, backupIndex));
			break;
		case ITEMTREE:
			List<Node> insertNodes = new ArrayList<>();
			List<TableField> insertFields = new ArrayList<>();
			for (TableField selectedField : selectedFields) {
				if (rowFlow == flow) { // ※同じアイテムを2回以上追加することはできません
					if (null != getFieldByResId(fields, selectedField.getResourceId())) continue;
					if (null != getFieldByResId(getFieldsByNodes(insertNodes), selectedField.getResourceId())) continue;
				}
				insertNodes.add(getLayoutLabel(sourceType, flow, selectedField));
				insertFields.add(selectedField);
			}
			if (!insertNodes.isEmpty() && !insertFields.isEmpty()) {
				handleActionAddLayoutChildren(flow, insertNodes, index, fields, insertFields, index);
			}
			break;
		default:
			break;
		}
	}
	
	private void handleActionFromAnyOnDragOverFlowPane(DragEvent event, LayoutFlowPane flow) {
		// initialize
		event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
		Platform.runLater(() -> {
			// xIndex
			Point xPoint = Geometry.createPoint(event.getX(), event.getY());
			List<Region> regions = getRegionsByNodes(flow.getChildren());
			List<Polygon> rectangles = Geometry.createRectangles(regions);
			List<Polygon> removeRectangles = new ArrayList<>();
			rectangles.forEach(rec -> {
				if (xPoint.getY() < Geometry.getRectanglePoints(rec).get(0).getY() ||
					xPoint.getY() > Geometry.getRectanglePoints(rec).get(3).getY()) { // Attention to X-Y !
					removeRectangles.add(rec);
				}
			});
			List<Polygon> newRectangles = AdhocUtils.createNewListRemoveNull(rectangles);
			newRectangles.removeAll(removeRectangles);
			Integer xIndex = rectangles.indexOf(Geometry.getClosestRectangle(xPoint, newRectangles));
			
			// handle
			if (xIndex < 0) {
				lastInsertIndex = -1;
				AdhocUtils.removeAllTargetChild(flow.getChildren(), EmptyGridPane.class);
			} else {
				Point mPoint = Geometry.getMiddlePoint(rectangles.get(xIndex));
				Vertex mVertex = Geometry.createVertex(mPoint, Geometry.createPoint(mPoint.getX(), mPoint.getY() + 10));
				Vertex xVertex = Geometry.createVertex(mPoint, xPoint);
				if (Geometry.crossProduct(mVertex, xVertex) < 0) {
					xIndex = xIndex + 1;
				}
				
				if (lastInsertIndex != xIndex) {
					lastInsertIndex = xIndex;
					if (!isNeighborEmptyGridPane(flow, xIndex)) {
						AdhocUtils.removeAllTargetChild(flow.getChildren(), EmptyGridPane.class);
						EmptyGridPane emptyGridPane = new EmptyGridPane(backup.label.getWidth(), backup.label.getHeight());
						if (xIndex >= 0 && xIndex <= flow.getChildren().size()) {
							flow.getChildren().add(xIndex, emptyGridPane);
						}
					}
				}
			}
		});
	}
	
}
