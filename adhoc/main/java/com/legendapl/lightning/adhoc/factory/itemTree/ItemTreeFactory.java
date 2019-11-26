package com.legendapl.lightning.adhoc.factory.itemTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.legendapl.lightning.adhoc.adhocView.model.AdhocField;
import com.legendapl.lightning.adhoc.adhocView.model.SortData;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.controller.P121AdhocAnchorPane;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.custom.LayoutLabel;
import com.legendapl.lightning.adhoc.factory.AdhocBaseFactory;
import com.legendapl.lightning.adhoc.factory.AdhocBuildTreeFactory;
import com.legendapl.lightning.adhoc.factory.LayoutStoreFactory;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.factory.itemTree.TableItemTreeFactory.ItemInterval;
import com.legendapl.lightning.adhoc.custom.CustomTooltip;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.service.AdhocLogService;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;

/**
 * アイテムツリー工場
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/4/1
 */
public abstract class ItemTreeFactory extends AdhocBaseFactory {
	
	/*-------------------------------------Constants-----------------------------------------*/
	
	protected static final String LayoutPoundId = "LayoutPoundId";
	protected static final String EmptyRegionId = "EmptyRegionId";
	protected static final String TableIntervalId = "TableFieldIntervalId";
	protected static final MaterialDesignIconView ICON_CHECK = new MaterialDesignIconView(MaterialDesignIcon.CHECK);
	
	/*-------------------------------------Data-----------------------------------------*/
	
	protected static Map<String, Integer> selectOrder = new HashMap<>();
	
	protected class LayoutBackup<T extends AdhocField> {
		public LayoutLabel label;
		public Integer index;
		public AdhocModelType sourceType;
		public LayoutFlowPane flow;
		public LayoutFlowPane oppFlow;
		public List<LayoutLabel> selectedLabels;
		public List<T> fields;
		public List<T> oppFields;
		public T field;
		public List<T> selectedFields;
		public LayoutBackup() {}
		public LayoutBackup(
				LayoutLabel label, AdhocModelType sourceType, 
				LayoutFlowPane flow, LayoutFlowPane oppFlow,
				List<T> fields, List<T> oppFields, T field) {
			super();
			this.label = label;
			this.index = null;
			this.sourceType = sourceType;
			this.flow = flow;
			this.oppFlow = oppFlow;
			this.selectedLabels = new ArrayList<>();
			this.fields = fields;
			this.oppFields = oppFields;
			this.field = field;
			this.selectedFields = new ArrayList<>();
		}
	}
	
	protected class EmptyGridPane extends GridPane {
		public EmptyGridPane() {
			super();
			this.setId(EmptyRegionId);
		}
		public EmptyGridPane(double width, double height) {
			super();
			this.setId(EmptyRegionId);
			this.setPrefWidth(width);
			this.setPrefHeight(height);
		}
	}
	
	/*-------------------------------------protected-----------------------------------------*/
	
	protected boolean checkMouseClickedTreeCell(MouseEvent event, AdhocModelType sourceType, TreeView<BaseNode> treeView) {
		if (null == event ||
				null == event.getPickResult() ||
				null == event.getPickResult().getIntersectedNode()) {
			Platform.runLater(() -> treeView.getSelectionModel().clearSelection());
			return false;
		}
		TreeItem<BaseNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
		List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
		if (null == selectedItem || selectedItems.isEmpty()) return false;
		for (TreeItem<BaseNode> si : selectedItems) {
			if (si.getValue() instanceof DBNode) return false;
		}
		return true;
	}
	
	protected ObservableValue<? extends Boolean> moveTreeItemToOppsiteVisible(TreeView<BaseNode> treeView) {
		return new BooleanBinding() {
			{
				super.bind(treeView.getSelectionModel().getSelectedItems(),
							columnFlow.getChildren(),
							rowFlow.getChildren(),
							adhoc.getColumnSort());
			}
			@Override protected boolean computeValue() {
				List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
				for (TreeItem<BaseNode> item : selectedItems) {
					if (item.getValue() instanceof DBNode) return false;
					if (item.getValue() instanceof ItemInterval) return false;
					if (findTarget(columnFlow.getChildren(), item)) return false;
					if (findTarget(rowFlow.getChildren(), item)) return false;
					switch (P121AdhocAnchorPane.viewModelType) {
					case TABLE:
						if (findTarget(LayoutStoreFactory.getCrossTableModel().getColumnFlowChildren(), item)) return false;
						if (findTarget(LayoutStoreFactory.getCrossTableModel().getRowFlowChildren(), item)) return false;
						break;
					case CROSSTABLE:
	 					if (findTarget(LayoutStoreFactory.getTableModel().getColumnFlowChildren(), item)) return false;
						if (findTarget(LayoutStoreFactory.getTableModel().getRowFlowChildren(), item)) return false;
						break;
					default:
						break;
					}
					if (findTargetInSort(adhoc.getColumnSort(), item)) return false;
				}
				return true;
			}
			
			private boolean findTarget(List<Node> list, TreeItem<BaseNode> item) {
				return null != getTarget(list, item.getValue().getResourceId());
			}
			
			private LayoutLabel getTarget(List<Node> list, String id) {
				if (null != list && null != id) {
					for (Node node : list) {
						if (node instanceof LayoutLabel) {
							LayoutLabel label = (LayoutLabel) node;
							if (id.equals(label.getId())) {
								return label;
							}
						} else if (node instanceof GridPane) {
							GridPane gridPane = (GridPane) node;
							LayoutLabel target = getTarget(gridPane.getChildren(), id);
							if (null != target) {
								return target;
							}
						}
					}
				}
				return null;
			}
			
			private boolean findTargetInSort(List<SortData> list, TreeItem<BaseNode> item) {
				for (SortData sortData : list) {
					if (sortData.getResourceId().equals(item.getValue().getResourceId())) {
						return true;
					}
				}
				return false;
			}
		};
	}
	
	protected void handleActionMoveTreeItemToOppsite(TreeView<BaseNode> thisTreeView, TreeView<BaseNode> oppTreeView) {
		List<TreeItem<BaseNode>> selectedItems = getSelectedItems(thisTreeView);
		StatementFactory.runLater(
				() -> { // todo
					handleActionMoveTreeItemToOppsiteImpl(thisTreeView, oppTreeView, selectedItems);
				},
				() -> { // redo
					List<TreeItem<BaseNode>> newSelectedItems = getNewSelectedItems(thisTreeView, selectedItems);
					handleActionMoveTreeItemToOppsiteImpl(thisTreeView, oppTreeView, newSelectedItems);
				}, 
				() -> { // undo
					List<TreeItem<BaseNode>> newSelectedItems = getNewSelectedItems(oppTreeView, selectedItems);
					handleActionMoveTreeItemToOppsiteImpl(oppTreeView, thisTreeView, newSelectedItems);
				}
		);
	}
	
	private void handleActionMoveTreeItemToOppsiteImpl(
			TreeView<BaseNode> thisTreeView, TreeView<BaseNode> oppTreeView,
			List<TreeItem<BaseNode>> selectedItems) {
		// log
		AdhocLogService.moveTreeItemToOppsite(thisTreeView, oppTreeView, selectedItems);
		// handle
		AdhocBuildTreeFactory.clearTree(); // clear
		if (fieldTreeView == thisTreeView) {
			AdhocBuildTreeFactory.moveTreeItemToMeasure(selectedItems);
		} else if (valueTreeView == thisTreeView) {
			AdhocBuildTreeFactory.moveTreeItemToField(selectedItems);
		}
	}
	
	private List<TreeItem<BaseNode>> getNewSelectedItems(TreeView<BaseNode> treeView, List<TreeItem<BaseNode>> selectedItems) {
		List<TreeItem<BaseNode>> newSelectedItems = new ArrayList<>();
		selectedItems.forEach(selectedItem -> {
			AdhocUtils.getAllItems(treeView.getRoot()).forEach(item -> {
				if (item.getValue() instanceof Item) {
					if (selectedItem.getValue().getResourceId().equals(item.getValue().getResourceId())) {
						newSelectedItems.add(item);
					}
				}
			});
		});
		return newSelectedItems;
	}
	
	protected ObservableValue<? extends Boolean> createFilterToPaneVisible(TreeView<BaseNode> treeView) {
		return new BooleanBinding() {
			{
				super.bind(treeView.getSelectionModel().getSelectedItems());
			}
			@Override protected boolean computeValue() {
				List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
				for (TreeItem<BaseNode> selectedItem : selectedItems) {
					if (selectedItem.getValue() instanceof DBNode) return false;
					if (selectedItem.getValue() instanceof ItemInterval) return false;
				}
				return true;
			}
		};
	}
	
	protected void handleActionCreateFilterToPane(TreeView<BaseNode> treeView) {
		List<Field> fields = getFieldsBySelectedItems(treeView);
		handleActionCreateFilterToPane(fields);
	}
	
	protected void handleActionCreateFilterToPane(Field field) {
		handleActionCreateFilterToPane(Arrays.asList(field));
	}
	
	protected void handleActionCreateFilterToPane(List<Field> fields) {
		filterPaneFactory.addFilterByFields(fields);
	}
	
	protected ObservableValue<? extends Boolean> createSortToPaneVisible(TreeView<BaseNode> treeView) {
		return new BooleanBinding() {
			{
				super.bind(treeView.getSelectionModel().getSelectedItems());
			}
			@Override protected boolean computeValue() {
				List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
				if (selectedItems.size() != 1) return false;
				TreeItem<BaseNode> selectedItem = selectedItems.get(0);
				if (selectedItem.getValue() instanceof DBNode) return false;
				if (selectedItem.getValue() instanceof ItemInterval) return false;
				return true;
			}
		};
	}
	
	protected void handleActionCreateSortToPane(TreeView<BaseNode> treeView) {
		
		P121AdhocAnchorPane p121AdhocAnchorPane = new P121AdhocAnchorPane();
		p121AdhocAnchorPane.handleActionFieldClickSortButton(null);
	}
	
	protected LayoutLabel getLayoutLabel(AdhocModelType sourceType, Field field) {
		// initialize
		LayoutLabel label = new LayoutLabel();
		switch (sourceType) {
		case MEASURE:
			label.getStyleClass().setAll("layout-flow-pane-child-measure-label");
			break;
		case FIELD:
		default:
			label.getStyleClass().setAll("layout-flow-pane-child-field-label");
			break;
		}
		// set others
		label.setId(field.getResourceId());
		label.setModelType(sourceType);
		label.setField(field);
		label.setText(field.getLabel());
		CustomTooltip toolTip = new CustomTooltip();
		toolTip.getProperty().setLabel(field.getLabel());
		toolTip.getProperty().setResourceId(field.getResourceId());
		toolTip.getProperty().setModelType(sourceType.toString());
		toolTip.getProperty().setDataFormat(field.getDataType().getDefaultDataFormat().toString());
		label.setTooltip(toolTip);
		MaterialDesignIconView ICON_CLOSE = new MaterialDesignIconView(MaterialDesignIcon.CLOSE);
		label.setGraphic(ICON_CLOSE);
		label.setContentDisplay(ContentDisplay.RIGHT);
		label.setCursor(Cursor.HAND);
		return label;
	}
	
	protected void updateMenuItemIcon(Menu menu, MenuItem menuItem) {
		if (null != menu && null != menuItem) {
			menu.getItems().forEach(mi -> mi.setGraphic(null));
			menuItem.setGraphic(ICON_CHECK);
			MenuItem menuItem2 = getMenuItemById(menu, menuItem.getId());
			if (null != menuItem2) menuItem2.setGraphic(ICON_CHECK);
		}
	}
	
	protected void setTreeViewSelectedItemProperty(TreeView<BaseNode> treeView) {
		treeView.getSelectionModel().selectedItemProperty().addListener((record, oldVal, newVal) -> {
			if (null == newVal) {
				selectOrder.clear();
			} else {
				Integer maxSelectOrder = 0;
				for (Integer selectOrder : selectOrder.values()) {
					maxSelectOrder = Math.max(maxSelectOrder, selectOrder);
				};
				selectOrder.put(newVal.getValue().getResourceId(), 1 + maxSelectOrder);
			}
		});
	}
	
	protected List<Field> getFieldsBySelectedItems(TreeView<BaseNode> treeView) {
		List<Field> fields = new ArrayList<>();
		List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
		Collections.sort(selectedItems, new SortBySelectOrder());
		selectedItems.forEach(selectedItem -> {
			fields.addAll(getFieldsByRoot(selectedItem));
		});
		return fields;
	}
	
	protected class SortBySelectOrder implements Comparator<TreeItem<BaseNode>> {
		@Override public int compare(TreeItem<BaseNode> treeItem1, TreeItem<BaseNode> treeItem2) {
			Integer selectOrder1 = selectOrder.get(treeItem1.getValue().getResourceId());
			Integer selectOrder2 = selectOrder.get(treeItem2.getValue().getResourceId());
			if (null == selectOrder1) selectOrder1 = 0;
			if (null == selectOrder2) selectOrder2 = 0;
			return selectOrder1 - selectOrder2;
	    }
	}

	protected List<Field> getFieldsByRoot(TreeItem<BaseNode> root) {
		List<Field> fields = new ArrayList<>();
		AdhocUtils.getAllItems(root).forEach(item -> {
			if (item.getValue() instanceof Item) {
				Field field = adhoc.getTopicTree().getFieldByResId(item.getValue().getResourceId());
				if (null != field) {
					fields.add(field);
				}
			}
		});
		return fields;
	}
	
	protected List<Field> getFieldsByNodes(List<Node> nodes) {
		List<Field> fields = new ArrayList<>();
		if (null != nodes) {
			for (Node node : nodes) {
				if (node instanceof LayoutLabel) {
					LayoutLabel label = (LayoutLabel) node;
					if (null != label && null != label.getField()) {
						fields.add(label.getField());
					}
				} else if (node instanceof Parent) {
					Parent parent = (Parent) node;
					fields.addAll(getFieldsByNodes(parent.getChildrenUnmodifiable()));
				}
			}
		}
		return fields;
	}
	
	protected <T extends AdhocField> void handleActionAddLayoutChild(
			LayoutFlowPane flow, Node newFlowChild, List<T> fields, T newField) {
		
		handleActionAddLayoutChildren(flow, Arrays.asList(newFlowChild), fields, Arrays.asList(newField));
	}
	
	protected <T extends AdhocField> void handleActionAddLayoutChildren(
			LayoutFlowPane flow, List<Node> newFlowChildren, List<T> fields, List<T> newFields) {
		
		handleActionAddLayoutChildren(flow, newFlowChildren, flow.getChildren().size(), fields, newFields, fields.size());
	}
	
	protected <T extends AdhocField> void handleActionAddLayoutChild(
			LayoutFlowPane flow,Node newFlowChild, Integer indexNode, List<T> fields, T newField, Integer indexField) {
		
		handleActionAddLayoutChildren(flow, Arrays.asList(newFlowChild), indexNode, fields, Arrays.asList(newField), indexField);
	}

	protected <T extends AdhocField> void handleActionAddLayoutChildren(
			LayoutFlowPane flow, List<Node> newFlowChildren, Integer indexNode, List<T> fields, List<T> newFields, Integer indexField) {
		
		StatementFactory.runLater(
				() -> handleActionAddLayoutChildrenImpl(flow, newFlowChildren, indexNode, fields, newFields, indexField),
				() -> handleActionDeleteLayoutChildrenImpl(flow, newFlowChildren, fields, newFields)
		);
	}

	protected <T extends AdhocField> void handleActionAddLayoutChildrenImpl(
			LayoutFlowPane flow, List<Node> newFlowChildren, List<T> fields, List<T> newFields) {
		
		handleActionAddLayoutChildrenImpl(flow, newFlowChildren, flow.getChildren().size(), fields, newFields, fields.size());
	}

	protected <T extends AdhocField> void handleActionAddLayoutChildrenImpl(
			LayoutFlowPane flow, List<Node> newFlowChildren, Integer indexNode, List<T> fields, List<T> newFields, Integer indexField) {
		
		AdhocLogService.addLayoutChildren(flow, newFlowChildren, indexNode);
		if (indexNode >= 0 && indexNode <= flow.getChildren().size()) {
			flow.getChildren().addAll(indexNode, newFlowChildren);
		}
		if (indexField >= 0 && indexField <= fields.size()) {
			fields.addAll(indexField, newFields);
		}
	}

	protected <T extends AdhocField> void handleActionDeleteLayoutChild(
			LayoutFlowPane flow, Node deleteNode, List<T> fields, T deleteField) {
		
		handleActionDeleteLayoutChildren(flow, Arrays.asList(deleteNode), fields, Arrays.asList(deleteField));
	}
	
	protected <T extends AdhocField> void handleActionDeleteLayoutChildren(
			LayoutFlowPane flow, List<Node> deleteNodes, List<T> fields, List<T> deleteFields) {
		
		Integer indexNode = deleteNodes.isEmpty() ? -1 : flow.getChildren().indexOf(deleteNodes.get(0));
		Integer indexField = deleteFields.isEmpty() ? -1 : fields.indexOf(deleteFields.get(0));
		StatementFactory.runLater(
				() -> handleActionDeleteLayoutChildrenImpl(flow, deleteNodes, fields, deleteFields),
				() -> handleActionAddLayoutChildrenImpl(flow, deleteNodes, indexNode, fields, deleteFields, indexField)
		);
	}
	
	protected <T extends AdhocField> void handleActionDeleteLayoutChildrenImpl(
			LayoutFlowPane flow, List<Node> deleteNodes, List<T> fields, List<T> deleteFields) {
		
		AdhocLogService.deleteLayoutChildren(flow, deleteNodes, indexOf(flow, deleteNodes));
		flow.getChildren().removeAll(deleteNodes);
		fields.removeAll(deleteFields);
	}
	
	protected boolean moveLayoutChildLeftVisible(LayoutFlowPane flow, Node node) {
		int index = flow.getChildren().indexOf(node);
		return index > 0 && index < flow.getChildren().size();
	}

	protected boolean moveLayoutChildRightVisible(LayoutFlowPane flow, Node node) {
		int index = flow.getChildren().indexOf(node);
		return index >= 0 && index + 1 < flow.getChildren().size();
	}

	protected <T extends AdhocField> void handleActionMoveLayoutChildLeft(LayoutFlowPane flow, Node node, List<T> fields, T field) {
		StatementFactory.runLater(
				() -> handleActionMoveLayoutChildLeftImpl(flow, node, fields, field),
				() -> handleActionMoveLayoutChildRightImpl(flow, node, fields, field)
		);
	}
	
	protected <T extends AdhocField> void handleActionMoveLayoutChildLeftImpl(LayoutFlowPane flow, Node node, List<T> fields, T field) {
		AdhocLogService.moveLayoutChildLeft(flow, node, indexOf(flow, node));
		AdhocUtils.moveListElement(flow.getChildren(), node, -1);
		AdhocUtils.moveListElement(fields, field, -1);
	}
	
	protected <T extends AdhocField> void handleActionMoveLayoutChildRight(LayoutFlowPane flow, Node node, List<T> fields, T field) {
		StatementFactory.runLater(
				() -> handleActionMoveLayoutChildRightImpl(flow, node, fields, field),
				() -> handleActionMoveLayoutChildLeftImpl(flow, node, fields, field)
		);
	}

	protected <T extends AdhocField> void handleActionMoveLayoutChildRightImpl(LayoutFlowPane flow, Node node, List<T> fields, T field) {
		AdhocLogService.moveLayoutChildRight(flow, node, indexOf(flow, node));
		AdhocUtils.moveListElement(flow.getChildren(), node, +1);
		AdhocUtils.moveListElement(fields, field, +1);
	}
	
	protected boolean isNeighborEmptyGridPane(LayoutFlowPane flow, Integer index) {
		if (index - 1 >= 0 && index - 1 < flow.getChildren().size()) {
			if (flow.getChildren().get(index-1) instanceof EmptyGridPane) {
				return true;
			}
		}
		if (index >= 0 && index < flow.getChildren().size()) {
			if (flow.getChildren().get(index) instanceof EmptyGridPane) {
				return true;
			}
		}
		return false;
	}
	
	/*-------------------------------------static-----------------------------------------*/
	
	protected static Integer indexOf(Pane pane, List<Node> nodes) {
		if (nodes.isEmpty()) return -1;
		else return indexOf(pane, nodes.get(0));
	}
	
	protected static Integer indexOf(Pane pane, Node node) {
		for (int i = 0; i < pane.getChildren().size(); i ++) {
			Node child = pane.getChildren().get(i);
			if (child instanceof LayoutLabel) {
				if (child == node) return i;
			} else if (child instanceof Pane) {
				Integer index = indexOf((Pane)child, node);
				if (-1 != index) return index;
			}
		}
		return -1;
	}
	
	protected static Field getFieldByResId(List<? extends Field> list, String resId) {
		if (null != list) {
			for (Field field : list) {
				if (null != field && null != resId && null != field.getResourceId() &&
						resId.equals(field.getResourceId())) {
					return field;
				}
			}
		}
		return null;
	}
	
	protected static Item getItemByResId(TreeItem<BaseNode> root, String resId) {
		if (null != root && null != resId) {
			for (TreeItem<BaseNode> item : AdhocUtils.getAllItems(root)) {
				if (item.getValue() instanceof Item && resId.equals(item.getValue().getResourceId())) {
					return (Item) item.getValue();
				}
			}
		}
		return null;
	}
	
	protected static Item getItemByResId(BaseNode root, String resId) {
		if (null != root && null != resId) {
			for (Item item : root.getAllItems()) {
				if (resId.equals(item.getResourceId())) {
					return item;
				}
			}
		}
		return null;
	}
	
	protected static List<Region> getRegionsByNodes(List<Node> nodes) {
		List<Region> regions = new ArrayList<>();
		if (null != nodes) {
			for (Node node : nodes) {
				if (node instanceof Region) {
					regions.add((Region) node);
				}
			}
		}
		return regions;
	}
	
	protected static MenuItem getMenuItemById(Menu menu, String menuItemId) {
		if (null != menu && null != menuItemId) {
			for (MenuItem menuItem : menu.getItems()) {
				if (null != menuItem && menuItemId.equals(menuItem.getId())) {
					return menuItem;
				}
			}
		}
		return null;
	}
	
	protected static MenuItem getMenuItemById(Control control, String menuItemId) {
		if (null != control && null != menuItemId) {
			ContextMenu contextMenu = control.getContextMenu();
			return getMenuItemById(contextMenu, menuItemId);
		}
		return null;
	}
	
	protected static MenuItem getMenuItemById(ContextMenu contextMenu, String menuItemId) {
		if (null != contextMenu) {
			if (null != contextMenu.getItems()) {
				for (MenuItem menuItem : contextMenu.getItems()) {
					if (null != menuItem && menuItemId.equals(menuItem.getId())) {
						return menuItem;
					}
				}
			}
		}
		return null;
	}
	
	protected static void fireMenuItem(Object obj, String menuItemId) {
		MenuItem menuItem = null;
		if (obj instanceof Menu) menuItem = getMenuItemById((Menu)obj, menuItemId);
		else if (obj instanceof Control) menuItem = getMenuItemById((Control)obj, menuItemId);
		else if (obj instanceof ContextMenu) menuItem = getMenuItemById((ContextMenu)obj, menuItemId);
		else if (obj instanceof GridPane) {
			GridPane gridPane = (GridPane) obj;
			if (gridPane.getChildren().size() > 0) {
				fireMenuItem(gridPane.getChildren().get(0), menuItemId);
				return;
			}
		}
		fireMenuItem(menuItem);
	}
	
	protected static void fireMenuItem(MenuItem menuItem) {
		if (null != menuItem) {
			menuItem.fire();
		}
	}
	
	protected static List<TreeItem<BaseNode>> getSelectedItems(TreeView<BaseNode> treeView) {
		return AdhocUtils.createNewListRemoveNull(treeView.getSelectionModel().getSelectedItems());
	}
	
	protected static LayoutLabel getLayoutLabel(GridPane gridPane) {
		try {
			return (LayoutLabel) gridPane.getChildren().get(0);
		} catch (Exception e) {
			return null;
		}
	}
	
}
