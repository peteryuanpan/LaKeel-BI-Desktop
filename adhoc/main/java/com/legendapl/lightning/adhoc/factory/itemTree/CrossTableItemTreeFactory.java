package com.legendapl.lightning.adhoc.factory.itemTree;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.Geometry;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.custom.LayoutLabel;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.service.AdhocLogService;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.triangulate.quadedge.Vertex;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * クロス集計アイテムツリー工場
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/4/1
 */
public abstract class CrossTableItemTreeFactory extends ItemTreeFactory {
	
	/*-------------------------------------Data-----------------------------------------*/
	
	protected static Integer lastInsertIndex = -1;
	protected static Boolean isAValidDragDropOperation = false;
	protected static LayoutBackup<CrossTableField> backup = null;
	
	/*-------------------------------------handleAction-----------------------------------------*/
	
	protected void handleActionDeleteLayoutChild(LayoutFlowPane flow, Node node, List<CrossTableField> fields, CrossTableField field) {
		
		if (node instanceof LayoutLabel) {
			super.handleActionDeleteLayoutChild(flow, node, fields, field);
			return;
		}
		
		if (node instanceof GridPane) {
			GridPane gridPane = (GridPane) node;
			LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
			List<Node> gridPanes = AdhocUtils.getAllTargetChild(flow.getChildren(), GridPane.class);
			switch (label.getModelType()) {
			case MEASURE:
				List<Node> deleteNodes = gridPanes.size() <= 2 ? gridPanes : Arrays.asList(gridPane);
				super.handleActionDeleteLayoutChildren(flow, deleteNodes, fields, Arrays.asList(field));
				break;
			case LAYOUT_POUND:
				List<CrossTableField> deleteFields = AdhocUtils.createNewListRemoveNull(fields);
				super.handleActionDeleteLayoutChildren(flow, gridPanes, fields, deleteFields);
				break;
			default:
				break;
			}
			return;
		}
	}
	
	protected boolean moveLayoutChildLeftVisible(LayoutFlowPane flow, Node node) {
		
		if (node instanceof LayoutLabel) {
			return super.moveLayoutChildLeftVisible(flow, node);
		}
		
		if (node instanceof GridPane) {
			GridPane gridPane = (GridPane) node;
			LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
			switch (label.getModelType()) {
			case MEASURE:
				Integer index = flow.getChildren().indexOf(node);
				if (index - 1 >= 0) {
					Node neighborChild = flow.getChildren().get(index - 1);
					if (neighborChild instanceof GridPane) {
						GridPane neighborGridPane = (GridPane) neighborChild;
						LayoutLabel neighborLabel = (LayoutLabel) neighborGridPane.getChildren().get(0);
						if (AdhocModelType.MEASURE == neighborLabel.getModelType()) {
							return true;
						}
					}
				}
				return false;
			case LAYOUT_POUND:
				return super.moveLayoutChildLeftVisible(flow, node);
			default:
				break;
			}
		}
		
		return false;
	}
	
	protected boolean moveLayoutChildRightVisible(LayoutFlowPane flow, Node node) {
		
		if (node instanceof LayoutLabel) {
			return super.moveLayoutChildRightVisible(flow, node);
		}
		
		if (node instanceof GridPane) {
			GridPane gridPane = (GridPane) node;
			LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
			Integer index;
			switch (label.getModelType()) {
			case MEASURE:
				index = flow.getChildren().indexOf(node);
				if (index + 1 < flow.getChildren().size()) {
					Node neighborChild = flow.getChildren().get(index + 1);
					if (neighborChild instanceof GridPane) {
						GridPane neighborGridPane = (GridPane) neighborChild;
						LayoutLabel neighborLabel = (LayoutLabel) neighborGridPane.getChildren().get(0);
						if (AdhocModelType.MEASURE == neighborLabel.getModelType()) {
							return true;
						}
					}
				}
				return false;
			case LAYOUT_POUND:
				index = AdhocUtils.getLastObjectIndex(flow.getChildren(), GridPane.class);
				if (index + 1 < flow.getChildren().size()) {
					return true;
				}
				return false;
			default:
				break;
			}
		}
		
		return false;
	}
	
	protected void handleActionMoveLayoutChildLeft(LayoutFlowPane flow, Node node, List<CrossTableField> fields, CrossTableField field) {
		StatementFactory.runLater(
				() -> handleActionMoveLayoutChildLeftImpl(flow, node, fields, field),
				() -> handleActionMoveLayoutChildRightImpl(flow, node, fields, field)
		);
	}
	
	protected void handleActionMoveLayoutChildLeftImpl(LayoutFlowPane flow, Node node, List<CrossTableField> fields, CrossTableField field) {
		if (node instanceof LayoutLabel) {
			LayoutLabel label = (LayoutLabel) node;
			Integer index = flow.getChildren().indexOf(label);
			Node neighborChild = flow.getChildren().get(index - 1);
			if (neighborChild instanceof LayoutLabel) {
				super.handleActionMoveLayoutChildLeftImpl(flow, node, fields, field);
				return;
			}
			if (neighborChild instanceof GridPane) {
				AdhocLogService.moveLayoutChildLeft(flow, node, indexOf(flow, node));
				Integer numGridPane = AdhocUtils.getAllTargetChild(flow.getChildren(), GridPane.class).size();
				AdhocUtils.moveListElement(flow.getChildren(), node, -1 * numGridPane);
				return;
			}
			return;
		}
		
		if (node instanceof GridPane) {
			GridPane gridPane = (GridPane) node;
			LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
			switch (label.getModelType()) {
			case MEASURE:
				super.handleActionMoveLayoutChildLeftImpl(flow, node, fields, field);
				break;
			case LAYOUT_POUND:
				Integer index = flow.getChildren().indexOf(gridPane);
				Node neighborChild = flow.getChildren().get(index - 1);
				if (neighborChild instanceof LayoutLabel) {
					LayoutLabel neighborLabel = (LayoutLabel) neighborChild;
					handleActionMoveLayoutChildRightImpl(flow, neighborLabel, null, null);
				}
				break;
			default:
				break;
			}
			return;
		}
	}
	
	protected void handleActionMoveLayoutChildRight(LayoutFlowPane flow, Node node, List<CrossTableField> fields, CrossTableField field) {
		StatementFactory.runLater(
				() -> handleActionMoveLayoutChildRightImpl(flow, node, fields, field),
				() -> handleActionMoveLayoutChildLeftImpl(flow, node, fields, field)
		);
	}
	
	protected void handleActionMoveLayoutChildRightImpl(LayoutFlowPane flow, Node node, List<CrossTableField> fields, CrossTableField field) {
		if (node instanceof LayoutLabel) {
			LayoutLabel label = (LayoutLabel) node;
			Integer index = flow.getChildren().indexOf(label);
			Node neighborChild = flow.getChildren().get(index + 1);
			if (neighborChild instanceof LayoutLabel) {
				// handle
				super.handleActionMoveLayoutChildRightImpl(flow, node, fields, field);
				return;
			}
			if (neighborChild instanceof GridPane) {
				AdhocLogService.moveLayoutChildRight(flow, node, indexOf(flow, node));
				Integer numGridPane = AdhocUtils.getAllTargetChild(flow.getChildren(), GridPane.class).size();
				AdhocUtils.moveListElement(flow.getChildren(), node, +1 * numGridPane);
				return;
			}
			return;
		}
		
		if (node instanceof GridPane) {
			GridPane gridPane = (GridPane) node;
			LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
			switch (label.getModelType()) {
			case MEASURE:
				super.handleActionMoveLayoutChildRightImpl(flow, node, fields, field);
				break;
			case LAYOUT_POUND:
				Integer index = AdhocUtils.getLastObjectIndex(flow.getChildren(), GridPane.class);
				Node neighborChild = flow.getChildren().get(index + 1);
				if (neighborChild instanceof LayoutLabel) {
					LayoutLabel neighborLabel = (LayoutLabel) neighborChild;
					handleActionMoveLayoutChildLeftImpl(flow, neighborLabel, null, null);
				}
				break;
			default:
				break;
			}
			return;
		}
	}
	
	protected void handleActionOnDragDetectedItemTree(
			MouseEvent event, AdhocModelType sourceType, 
			TreeView<BaseNode> treeView, TreeCell<BaseNode> treeCell) {
		
		if (AdhocModelType.FIELD == sourceType || AdhocModelType.MEASURE == sourceType) {
			TreeItem<BaseNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
			List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
			if (null != selectedItem && !selectedItems.isEmpty()) {
				// initialize
				List<CrossTableField> fields = getFieldsBySelectedItems(sourceType, treeView);
				backup = AdhocModelType.FIELD == sourceType ?
						new LayoutBackup<CrossTableField>(null, sourceType, columnFlow, rowFlow, crossTableColumns, crossTableRows, fields.get(0)) :
						new LayoutBackup<CrossTableField>(null, sourceType, columnFlow, rowFlow, crossTableValues, crossTableValues, fields.get(0));
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
	
	protected void handleActionFromItemTreeOnDragOverFlowPane(DragEvent event, LayoutFlowPane flow) {
		LayoutFlowPane oppositeFlowPane = columnFlow == flow ? rowFlow : columnFlow;
		if (AdhocModelType.MEASURE == backup.sourceType &&
				null != AdhocUtils.getLastObject(oppositeFlowPane.getChildren(), GridPane.class)) {
			event.acceptTransferModes(TransferMode.NONE);
		} else {
			handleActionFromAnyOnDragOverFlowPane(event, flow);
		}
	}
	
	protected void handleActionFromItemTreeOnDragDroppedFlowPane(DragEvent event, LayoutFlowPane flow) {
		Platform.runLater(() -> {
			Integer index = AdhocUtils.getLastObjectIndex(flow.getChildren(), EmptyGridPane.class);
			if (AdhocModelType.MEASURE == backup.sourceType &&
					flow.getChildren().size() == index &&
					null != AdhocUtils.getLastObject(flow.getChildren(), GridPane.class)) {
				index = 1 + AdhocUtils.getLastObjectIndex(flow.getChildren(), GridPane.class);
			}
			
			if (!canDropped(flow, index, backup.label)) { // illegal
				return;
			}
			
			if (flow == backup.flow) {
				addLayoutChildren(AdhocModelType.ITEMTREE, backup.sourceType, backup.flow, index,
						backup.fields, backup.field, backup.selectedFields);
			} else {
				addLayoutChildren(AdhocModelType.ITEMTREE, backup.sourceType, backup.oppFlow, index,
						backup.oppFields, backup.field, backup.selectedFields);
			}
		});
	}
	
	protected void handleActionOnDragDoneItemTree(DragEvent event, TreeView<BaseNode> treeView) {
		Platform.runLater(() -> {
			treeView.getSelectionModel().clearSelection();
		});
	}
	
	protected void handleActionOnDragDetectedLayoutChild(MouseEvent event, LayoutBackup<CrossTableField> _backup) {
		
		// initialize
		backup = _backup;
		if (AdhocModelType.LAYOUT_POUND == backup.sourceType ||
				(AdhocModelType.MEASURE == backup.sourceType && 
				2 == AdhocUtils.getAllTargetChild(backup.flow.getChildren(), GridPane.class).size()))
		{
			backup.index = AdhocUtils.getFirstObjectIndex(backup.flow.getChildren(), GridPane.class);
			List<Node> nodes = AdhocUtils.getAllTargetChild(backup.flow.getChildren(), GridPane.class);
			backup.selectedLabels = new ArrayList<>();
			for (int i = 1; i < nodes.size(); i ++) {
				backup.selectedLabels.add(getLayoutLabel((GridPane) nodes.get(i)));
			};
			List<Field> fields = getFieldsByNodes(nodes);
			backup.selectedFields = new ArrayList<>();
			for (int i = 1; i < fields.size(); i ++) {
				backup.selectedFields.add((CrossTableField) fields.get(i));
			};
		} else if (AdhocModelType.MEASURE == backup.sourceType) {
			backup.index = backup.flow.getChildren().indexOf(backup.label.getParent());
		} else { // AdhocModelType.FIELD == backup.sourceType
			backup.index = backup.flow.getChildren().indexOf(backup.label);
		}
		lastInsertIndex = -1;
		isAValidDragDropOperation = false;
		
		// handleAction
		backup.flow.setOnDragOver(e -> handleActionFromLayoutOnDragOverFlowPane(e, backup.flow));
		backup.flow.setOnDragDropped(e -> handleActionFromLayoutOnDragDroppedFlowPane(e, backup.flow));
		backup.oppFlow.setOnDragOver(e -> handleActionFromLayoutOnDragOverFlowPane(e, backup.oppFlow));
		backup.oppFlow.setOnDragDropped(e -> handleActionFromLayoutOnDragDroppedFlowPane(e, backup.oppFlow));
		
		// create dragBoard
		Dragboard dragBoard = backup.label.startDragAndDrop(TransferMode.COPY_OR_MOVE);
		switch (backup.sourceType) {
		case LAYOUT_POUND:
			// get
			String text = backup.label.getText();
			// set
			backup.label.getStyleClass().setAll("layout-flow-pane-child-measure-label");
			backup.label.setText(AdhocUtils.getString("P121.CrossTableModel.Constants.MeasureGroupText"));
			dragBoard.setDragView(backup.label.getParent().snapshot(null, null), event.getX(), event.getY());
			// back
			backup.label.getStyleClass().setAll("layout-flow-pane-child-icon-pound-label");
			backup.label.setText(text);
			break;
		case MEASURE:
		case FIELD:
			dragBoard.setDragView(backup.label.snapshot(null, null), event.getX(), event.getY());
			break;
		default:
			break;
		}
		ClipboardContent clipboardContent = new ClipboardContent();
		clipboardContent.putString(backup.label.getId());
		dragBoard.setContent(clipboardContent);
		
		// fire MenuItem delete
		StatementFactory.onlyPush = true;
		fireMenuItem(backup.label.getMenuItemDelete());
	}
	
	protected void handleActionFromLayoutOnDragOverFlowPane(DragEvent event, LayoutFlowPane flow) {
		if (flow == backup.oppFlow && AdhocModelType.MEASURE == getJudgeModelType(backup.flow, backup.label)) {
			event.acceptTransferModes(TransferMode.NONE);
		} else {
			handleActionFromAnyOnDragOverFlowPane(event, flow);
		}
	}
	
	protected void handleActionFromLayoutOnDragDroppedFlowPane(DragEvent event, LayoutFlowPane flow) {
		Platform.runLater(() -> {
			Integer index = AdhocUtils.getLastObjectIndex(flow.getChildren(), EmptyGridPane.class);
			if (!canDropped(flow, index, backup.label)) { // illegal
				isAValidDragDropOperation = false;
				return;
			}
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
			} else {
				isAValidDragDropOperation = true;
				addLayoutChildren(AdhocModelType.LAYOUT, backup.sourceType, backup.oppFlow, index,
						backup.oppFields, backup.field, backup.selectedFields);
			}
		});
	}
	
	protected void handleActionOnDragDoneLayoutChild(DragEvent event, LayoutBackup<CrossTableField> backup) {
		Platform.runLater(() -> {
			if (!isAValidDragDropOperation) {
				addLayoutChildren(AdhocModelType.LAYOUT, backup.sourceType, backup.flow, backup.index,
						backup.fields, backup.field, backup.selectedFields);
			}
		});
	}
	
	protected void addLayoutChildren(
			AdhocModelType modelType, AdhocModelType sourceType, LayoutFlowPane flow, Integer index, 
			List<CrossTableField> fields, CrossTableField field, List<CrossTableField> selectedFields) {
		
		AdhocUtils.removeAllTargetChild(columnFlow.getChildren(), EmptyGridPane.class);
		AdhocUtils.removeAllTargetChild(rowFlow.getChildren(), EmptyGridPane.class);
		
		List<Node> insertNodes;
		Node insertNode;
		switch (modelType) {
		case LAYOUT:
			LayoutFlowPane backupFlow = backup.flow;
			Integer backupIndex = backup.index;
			switch (getJudgeModelType(backup.flow, backup.label)) {
			case FIELD:
				insertNode = crossTableFieldTreeFactory.getLayoutLabel(flow, field);
				handleActionAddLayoutChild(flow, insertNode, index);
				StatementFactory.handle(isAValidDragDropOperation,
						() -> AdhocLogService.moveLayoutChild(backupFlow, flow, insertNode, backupIndex, index), 
						() -> AdhocLogService.moveLayoutChild(flow, backupFlow, insertNode, index, backupIndex));
				break;
			case MEASURE:
				insertNode = crossTableValueTreeFactory.getLayoutLabelParent(flow, backup.label);
				handleActionAddLayoutChild(flow, insertNode, index);
				StatementFactory.handle(isAValidDragDropOperation,
						() -> AdhocLogService.moveLayoutChild(backupFlow, flow, insertNode, backupIndex, index), 
						() -> AdhocLogService.moveLayoutChild(flow, backupFlow, insertNode, index, backupIndex));
				break;
			case LAYOUT_POUND:
				insertNodes = new ArrayList<>();
				if (null == AdhocUtils.getFirstObject(flow.getChildren(), GridPane.class)) {
					insertNodes.add(crossTableValueTreeFactory.getLayoutPoundParent(flow));
				}
				for (LayoutLabel label : backup.selectedLabels) {
					insertNodes.add(crossTableValueTreeFactory.getLayoutLabelParent(flow, label));
				}
				handleActionAddLayoutChildren(flow, insertNodes, index);
				StatementFactory.handle(isAValidDragDropOperation,
						() -> AdhocLogService.moveLayoutChild(backupFlow, flow, insertNodes, backupIndex, index), 
						() -> AdhocLogService.moveLayoutChild(flow, backupFlow, insertNodes, index, backupIndex));
				break;
			default:
				break;
			}
			break;
		case ITEMTREE:
			switch (sourceType) {
			case FIELD:
				insertNodes = new ArrayList<>();
				for (CrossTableField selectedField : selectedFields) {
					insertNodes.add(crossTableFieldTreeFactory.getLayoutLabel(flow, selectedField));
				}
				handleActionAddLayoutChildren(flow, insertNodes, index);
				break;
			case MEASURE:
				insertNodes = new ArrayList<>();
				if (null == AdhocUtils.getFirstObject(flow.getChildren(), GridPane.class)) {
					insertNodes.add(crossTableValueTreeFactory.getLayoutPoundParent(flow));
				}
				for (CrossTableField selectedField : selectedFields) {
					insertNodes.add(crossTableValueTreeFactory.getLayoutLabelParent(flow, selectedField));
				}
				handleActionAddLayoutChildren(flow, insertNodes, index);
				break;
			default:
				break;
			}
			break;
		default:
			break;
		}
	}
	
	private void handleActionAddLayoutChild(LayoutFlowPane flow, Node newFlowChild, Integer indexNode) {
		handleActionAddLayoutChildren(flow, Arrays.asList(newFlowChild), indexNode);
	}
	
	private void handleActionAddLayoutChildren(LayoutFlowPane flow, List<Node> newFlowChildren, Integer indexNode) {
		StatementFactory.runLater(
				() -> handleActionAddLayoutChildrenImpl(flow, newFlowChildren, indexNode),
				() -> handleActionDeleteLayoutChildrenImpl(flow, newFlowChildren)
		);
	}
	
	private void handleActionAddLayoutChildrenImpl(LayoutFlowPane flow, List<Node> newFlowChildren, Integer indexNode) {
		AdhocLogService.addLayoutChildren(flow, newFlowChildren, indexNode);
		flow.getChildren().addAll(indexNode, newFlowChildren);
		refreshLayout();
	}
	
	private void handleActionDeleteLayoutChildrenImpl(LayoutFlowPane flow, List<Node> oldFlowChildren) {
		AdhocLogService.deleteLayoutChildren(flow, oldFlowChildren, indexOf(flow, oldFlowChildren));
		flow.getChildren().removeAll(oldFlowChildren);
		refreshLayout();
	}
	
	public static void refreshLayout() {
		// clear
		crossTableColumns.clear();
		crossTableRows.clear();
		crossTableValues.clear();
		// convert
		AdhocUtils.getAllTargetChild(columnFlow.getChildren(), LayoutLabel.class)
		.forEach(child -> {
			LayoutLabel label = (LayoutLabel) child;
			if (null != label.getField()) {
				crossTableColumns.add((CrossTableField)label.getField());
			}
		});
		AdhocUtils.getAllTargetChild(rowFlow.getChildren(), LayoutLabel.class)
		.forEach(child -> {
			LayoutLabel label = (LayoutLabel) child;
			if (null != label.getField()) {
				crossTableRows.add((CrossTableField)label.getField());
			}
		});
		AdhocUtils.getAllTargetChild(columnFlow.getChildren(), GridPane.class)
		.forEach(child -> {
			GridPane gridPane = (GridPane) child;
			LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
			if (null != label.getField()) {
				crossTableValues.add((CrossTableField)label.getField());
			}
		});
		AdhocUtils.getAllTargetChild(rowFlow.getChildren(), GridPane.class)
		.forEach(child -> {
			GridPane gridPane = (GridPane) child;
			LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
			if (null != label.getField()) {
				crossTableValues.add((CrossTableField)label.getField());
			}
		});
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
						if (canInsert(flow, xIndex, backup.label)) {
							AdhocUtils.removeAllTargetChild(flow.getChildren(), EmptyGridPane.class);
							EmptyGridPane emptyGridPane = new EmptyGridPane(backup.label.getWidth(), backup.label.getHeight());
							if (AdhocModelType.MEASURE == backup.sourceType && 
									null != AdhocUtils.getFirstObject(flow.getChildren(), GridPane.class)) {
								emptyGridPane.getStyleClass().setAll("layout-flow-pane-child-grid-pane");
							}
							if (xIndex >= 0 && xIndex <= flow.getChildren().size()) {
								flow.getChildren().add(xIndex, emptyGridPane);
							}
						}
					}
				}
			}
		});
	}
	
	private boolean canInsert(LayoutFlowPane flow, Integer index, LayoutLabel label) {
		switch (getJudgeModelType(flow, label)) {
		case MEASURE:
			if (flowHasChildWithModelTypeAtIndex(flow, index - 1, AdhocModelType.LAYOUT_POUND) ||
					flowHasChildWithModelTypeAtIndex(flow, index - 1, AdhocModelType.MEASURE)) {
				return true;
			}
			break;
		case FIELD:
		case LAYOUT_POUND:
			if (0 == index ||
					flowHasChildWithModelTypeAtIndex(flow, index - 1, AdhocModelType.FIELD) ||
					flow.getChildren().size() == index ||
					flowHasChildWithModelTypeAtIndex(flow, index, AdhocModelType.FIELD)) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	private boolean canDropped(LayoutFlowPane flow, Integer index, LayoutLabel label) {
		switch (getJudgeModelType(flow, label)) {
		case MEASURE:
			if (flowHasChildWithModelTypeAtIndex(flow, index - 1, AdhocModelType.LAYOUT_POUND) ||
					flowHasChildWithModelTypeAtIndex(flow, index - 1, AdhocModelType.MEASURE)) {
				return true;
			}
			break;
		case FIELD:
		case LAYOUT_POUND:
			if (0 == index ||
					flowHasChildWithModelTypeAtIndex(flow, index - 1, AdhocModelType.FIELD) ||
					flow.getChildren().size() == index ||
					flow.getChildren().size() == index + 1 ||
					flowHasChildWithModelTypeAtIndex(flow, index + 1, AdhocModelType.FIELD)) {
				return true;
			}
			break;
		default:
			break;
		}
		return false;
	}
	
	private boolean flowHasChildWithModelTypeAtIndex(LayoutFlowPane flow, Integer index, final AdhocModelType ModelType) {
		if (index >= 0 && index < flow.getChildren().size()) {
			Node child = flow.getChildren().get(index);
			LayoutLabel label = null;
			if (child instanceof LayoutLabel) {
				label = (LayoutLabel) child;
			} else if (child instanceof GridPane) {
				GridPane gridPane = (GridPane) child;
				if (!gridPane.getChildren().isEmpty()) {
					label = (LayoutLabel) gridPane.getChildren().get(0);
				}
			}
			if (null != ModelType && null != label && ModelType == label.getModelType()) {
				return true;
			}
		}
		return false;
	}
	
	private AdhocModelType getJudgeModelType(LayoutFlowPane flow, LayoutLabel label) {
		if (AdhocModelType.MEASURE == label.getModelType() &&
				null == AdhocUtils.getLastObject(flow.getChildren(), GridPane.class)) {
			return AdhocModelType.LAYOUT_POUND;
		} else {
			return label.getModelType();
		}
	}
	
	/*-------------------------------------others-----------------------------------------*/
	
	protected List<CrossTableField> getFieldsBySelectedItems(AdhocModelType sourceType, TreeView<BaseNode> treeView) {
		List<CrossTableField> newFields = new ArrayList<>();
		List<Field> fields = getFieldsBySelectedItems(treeView);
		fields.forEach(f -> {
			Item item = getItemByResId(treeView.getRoot(), f.getResourceId());
			CrossTableField field = new CrossTableField(f, sourceType, item);
			newFields.add(field);
		});
		return newFields;
	}
	
}
