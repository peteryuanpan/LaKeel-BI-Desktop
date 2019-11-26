package com.legendapl.lightning.adhoc.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.ListSelectionView;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeCell;
import com.jfoenix.controls.JFXTreeView;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.ViewType;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.DBTree;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.ShareDataService;
import com.legendapl.lightning.adhoc.service.TreeTransferService;
import com.legendapl.lightning.adhoc.service.SplitTreeService;
import com.legendapl.lightning.tools.data.AdhocData;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * トピック編集画面のコントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class P111TopicSelectAnchorPane extends P110TopicBaseAnchorPane {
	
	 /*-------------------------data-----------------------------*/
	
	@FXML
	private ListSelectionView<AnchorPane> selectBox;
	
	private AnchorPane srAnchorPane;
	private AnchorPane tgAnchorPane;
	private JFXTreeView<BaseNode> srTree;
	private JFXTreeView<BaseNode> tgTree;
	
    private Button moveToTarget;
    private Button moveToSource;
    private Button moveAllToTarget;
    private Button moveAllToSource;
	
	private TreeItem<BaseNode> alSuperRoot;
	private TreeItem<BaseNode> srSuperRoot;
	private TreeItem<BaseNode> tgSuperRoot;
    
    private Topic topic;
    
    /*-------------------------initialize-----------------------------*/
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		super.initialize(location, resources);
		logger.debug("initialize");
		
		topic = ShareDataService.loadTopic();
		if (null == topic) {
			logger.warn("Topic is null.");
			return;
		}
		
		Platform.runLater(() -> {
			initLabel();
			initSource();
			initTarget();
			initMoveButton();
			initFieldButton();
			initTree();
		});
	}
	
	private void initLabel() {
		Label sourceHeader = (Label) selectBox.getSourceHeader();
		sourceHeader.setText(AdhocUtils.getString("P111.sourceHeader.label"));
		Label targetHeader = (Label) selectBox.getTargetHeader();
		targetHeader.setText(AdhocUtils.getString("P111.targetHeader.label"));
	}
	
	private void initSource() {
		srAnchorPane = new AnchorPane();
		srTree = new JFXTreeView<BaseNode>();
		srSuperRoot = new TreeItem<BaseNode>(new DBNode());
		srSuperRoot.getValue().setViewType(ViewType.LEFT);
		srTree.setRoot(srSuperRoot);
		srTree.setShowRoot(false);
		srTree.getSelectionModel().clearSelection();
		srTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		GridPane gridPane = (GridPane) selectBox.getChildrenUnmodifiable().get(0);
		ListView<?> srListView = (ListView<?>) gridPane.getChildren().get(2);
		srTree.prefHeightProperty().bind(srListView.heightProperty());
		srTree.setCellFactory(treeView -> new SrTreeCell());
		srTree.setOnMouseClicked(event -> setOnMouseClickedSrTree(event));
		AnchorPane.setTopAnchor(srTree, (double) -5);
		AnchorPane.setBottomAnchor(srTree, (double) -5);
		AnchorPane.setLeftAnchor(srTree, (double) -10);
		AnchorPane.setRightAnchor(srTree, (double) -10);
		srAnchorPane.getChildren().add(srTree);
		selectBox.getSourceItems().clear();
		selectBox.getSourceItems().add(srAnchorPane);
	}
	
	private void initTarget() {
		tgAnchorPane = new AnchorPane();
		tgTree = new JFXTreeView<BaseNode>();
		tgSuperRoot = new TreeItem<BaseNode>(new DBNode());
		tgSuperRoot.getValue().setViewType(ViewType.RIGHT);
		tgTree.setRoot(tgSuperRoot);
		tgTree.setShowRoot(false);
		tgTree.getSelectionModel().clearSelection();
		tgTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		GridPane gridPane = (GridPane) selectBox.getChildrenUnmodifiable().get(0);
		ListView<?> tgListView = (ListView<?>) gridPane.getChildren().get(2);
		tgTree.prefHeightProperty().bind(tgListView.heightProperty());
		tgTree.setOnMouseClicked(event -> setOnMouseClickedTgTree(event));
		AnchorPane.setTopAnchor(tgTree, (double) -5);
		AnchorPane.setBottomAnchor(tgTree, (double) -5);
		AnchorPane.setLeftAnchor(tgTree, (double) -10);
		AnchorPane.setRightAnchor(tgTree, (double) -10);
		tgAnchorPane.getChildren().add(tgTree);
		selectBox.getTargetItems().clear();
		selectBox.getTargetItems().add(tgAnchorPane);
	}
	
	private void initMoveButton() {
		GridPane gridPane = (GridPane) selectBox.getChildrenUnmodifiable().get(0);
		StackPane stackPane = (StackPane) gridPane.getChildren().get(4);
		VBox vbox = (VBox) stackPane.getChildren().get(0);
		moveToTarget = (Button) vbox.getChildren().get(0);
		moveToTarget.disableProperty().bind(moveToTargetButtonDisable());
		moveToTarget.setOnAction(event -> moveToTargetAction());
		moveToSource = (Button) vbox.getChildren().get(2);
		moveToSource.disableProperty().bind(moveToSourceButtonDisable());
		moveToSource.setOnAction(event -> moveToSourceAction());
		moveAllToTarget = (Button) vbox.getChildren().get(1);
		moveAllToTarget.disableProperty().bind(moveAllToTargetButtonDisable());
		moveAllToTarget.setOnAction(event -> moveAllToTargetAction());
		moveAllToSource = (Button) vbox.getChildren().get(3);
		moveAllToSource.disableProperty().bind(moveAllToSourceButtonDisable());
		moveAllToSource.setOnAction(event -> moveAllToSourceAction());
		vbox.getChildren().clear();
		vbox.getChildren().addAll(moveToTarget, moveToSource, moveAllToTarget, moveAllToSource);
	}
	
	private void initFieldButton() {
		SplitPane splitPane = (SplitPane) topicStage.getScene().getRoot().getChildrenUnmodifiable().get(0);
		StackPane stackPane = (StackPane) splitPane.getChildrenUnmodifiable().get(0);
		AnchorPane anchorPane = (AnchorPane) stackPane.getChildren().get(0);
		VBox vbox = (VBox) anchorPane.getChildren().get(0);
		selectField = (JFXButton) vbox.getChildren().get(0);
		filterField = (JFXButton) vbox.getChildren().get(1);
		filterField.disableProperty().bind(moveAllToSourceButtonDisable());
		filterField.setOnAction(event -> filterField(event));
		displayField = (JFXButton) vbox.getChildren().get(2);
		displayField.disableProperty().bind(moveAllToSourceButtonDisable());
		displayField.setOnAction(event -> displayField(event));
	}
	
	private void initTree() {
		alSuperRoot = new TreeItem<BaseNode>(new DBNode());
		TreeTransferService.transferTree(alSuperRoot, topic.getTrees());
		Collections.sort(alSuperRoot.getChildren(), new SortByNodeIds());
		SplitTreeService.setTreeViewType(alSuperRoot, ViewType.LEFT);
		if (null != topic.getTree() && null != topic.getTree().getRoot()) {
			SplitTreeService.setTreeViewType(topic.getTree().getRoot(), alSuperRoot, ViewType.RIGHT);
			SplitTreeService.setTreeViewTypeBySubTree(alSuperRoot);
		}
		SplitTreeService.insertTargetTree(alSuperRoot, srSuperRoot, ViewType.RIGHT);
		SplitTreeService.insertTargetTree(alSuperRoot, tgSuperRoot, ViewType.LEFT);
	}
	
	private class SortByNodeIds implements Comparator<TreeItem<BaseNode>> {
		@Override public int compare(TreeItem<BaseNode> node1, TreeItem<BaseNode> node2) {
			Integer index1 = topic.getNodeIds().indexOf(node1.getValue().getId());
			Integer index2 = topic.getNodeIds().indexOf(node2.getValue().getId());
			return index1 - index2;
		}
	}

	/*-------------------------TreeCell-----------------------------*/
	
	private class SrTreeCell extends JFXTreeCell<BaseNode> {
		@Override public void updateItem(BaseNode item, boolean empty) {
			super.updateItem(item, empty);
			boolean disable = false;
			TreeItem<BaseNode> treeItem = this.getTreeItem();
			if (treeItem != null) {
				if (!tgSuperRoot.getChildren().isEmpty()) {
					if (!treeItem.getValue().getTreeId().equals(
							tgSuperRoot.getChildren().get(0).getValue().getTreeId())) {
						disable = true;
					}
				}
			}
			this.setDisable(disable);
		}
	}
	
	/*-------------------------OnMouseClicked-----------------------------*/
	
	private void setOnMouseClickedSrTree(MouseEvent event) {
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode ||
			null == srTree.getSelectionModel() ||
			null == srTree.getSelectionModel().getSelectedItem() ||
			null == srTree.getSelectionModel().getSelectedItem().getValue()) {
			return;
		}
		if (clickedNode instanceof StackPane ||
			clickedNode instanceof JFXTreeView ||
			clickedNode instanceof JFXTreeCell) {
			return;
		}
		if (MouseButton.PRIMARY.equals(event.getButton()) && 2 == event.getClickCount()) {
			if (moveToTargetButtonDisable().getValue()) {
				AlertWindowService.showInfo(AdhocUtils.getString("INFO_TOPIC_SELECT_DOUBLE_CLICK_ADD_FAILED"));
				return;
			}
			moveToTargetAction();
		}
	}
	
	private void setOnMouseClickedTgTree(MouseEvent event) {
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode ||
			null == tgTree.getSelectionModel() ||
			null == tgTree.getSelectionModel().getSelectedItem() ||
			null == tgTree.getSelectionModel().getSelectedItem().getValue()) {
			return;
		}
		if (clickedNode instanceof StackPane ||
			clickedNode instanceof JFXTreeView ||
			clickedNode instanceof JFXTreeCell) {
			return;
		}
		if (MouseButton.PRIMARY.equals(event.getButton()) && 2 == event.getClickCount()) {
			moveToSourceAction();
		}
	}
	
	/*-------------------------moveToTargetAction-----------------------------*/
	/*-------------------------moveToSourceButton-----------------------------*/
	/*-------------------------moveAllToTargetAction-----------------------------*/
	/*-------------------------moveAllToSourceAction-----------------------------*/
	
	private ObservableValue<? extends Boolean> moveToTargetButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(srTree.getSelectionModel().getSelectedItems());
            }
            @Override protected boolean computeValue() {
				List<TreeItem<BaseNode>> selectedItems = AdhocUtils.createNewListRemoveNull(srTree.getSelectionModel().getSelectedItems());
				if (null == selectedItems || selectedItems.isEmpty()) {
					return true;
				}
				String firstTreeId = selectedItems.get(0).getValue().getTreeId();
				for (TreeItem<BaseNode> selectedItem : selectedItems) {
					if (!firstTreeId.equals(selectedItem.getValue().getTreeId())) {
						return true;
					}
				}
				return false;
			}
		};
	}
	
	private ObservableValue<? extends Boolean> moveToSourceButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(tgTree.getSelectionModel().getSelectedItems());
            }
            @Override protected boolean computeValue() {
				return tgTree.getSelectionModel().getSelectedItems().isEmpty();
			}
		};
	}
	
	private ObservableValue<? extends Boolean> moveAllToTargetButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(srSuperRoot.getChildren(), tgSuperRoot.getChildren());
            }
            @Override protected boolean computeValue() {
				if (srSuperRoot.getChildren().isEmpty()) {
					return true;
				}
				String treeId0 = srSuperRoot.getChildren().get(0).getValue().getTreeId();
				for (TreeItem<BaseNode> child : srSuperRoot.getChildren()) {
					if (!treeId0.equals(child.getValue().getTreeId())) {
						return true;
					}
				}
				if (!tgSuperRoot.getChildren().isEmpty()) {
					for (TreeItem<BaseNode> child : srSuperRoot.getChildren()) {
						if (!tgSuperRoot.getChildren().get(0).getValue().getTreeId().equals(
								child.getValue().getTreeId())) {
							return true;
						}
					}
				}
				return false;
			}
		};
	}
	
	private ObservableValue<? extends Boolean> moveAllToSourceButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(tgSuperRoot.getChildren());
            }
            @Override protected boolean computeValue() {
				return tgSuperRoot.getChildren().isEmpty();
			}
		};
	}
	
	private void moveToTargetAction() {
		Platform.runLater(() -> {
			List<TreeItem<BaseNode>> selectedItems = getSelectedItems(srTree);
			clearSelection();
			splitTree(selectedItems, ViewType.RIGHT);
			SplitTreeService.setTreeExpanded(selectedItems, tgSuperRoot);
			SplitTreeService.setTreeSelection(selectedItems, tgTree);
		});
	}
	
	private void moveToSourceAction() {
		Platform.runLater(() -> {
			List<TreeItem<BaseNode>> selectedItems = getSelectedItems(tgTree);
			clearSelection();
			splitTree(selectedItems, ViewType.LEFT);
			SplitTreeService.setTreeExpanded(selectedItems, srSuperRoot);
			SplitTreeService.setTreeSelection(selectedItems, srTree);
		});
	}
	
	private void splitTree(List<TreeItem<BaseNode>> selectedItems, ViewType FLAG_TO_SET) {
		SplitTreeService.splitTree(selectedItems, alSuperRoot, FLAG_TO_SET, srSuperRoot, ViewType.RIGHT, tgSuperRoot, ViewType.LEFT);
	}
	
	private void moveAllToTargetAction() {
		Platform.runLater(() -> {
			clearSelection();
			splitTree(ViewType.RIGHT);
		});
	}
	
	private void moveAllToSourceAction() {
		Platform.runLater(() -> {
			clearSelection();
			splitTree(ViewType.LEFT);
		});
	}
	
	private void splitTree(ViewType FLAG_TO_SET) {
		SplitTreeService.splitTree(alSuperRoot, FLAG_TO_SET, srSuperRoot, ViewType.RIGHT, tgSuperRoot, ViewType.LEFT);
	}
	
	private List<TreeItem<BaseNode>> getSelectedItems(TreeView<BaseNode> treeView) {
		List<TreeItem<BaseNode>> selectedItems = AdhocUtils.createNewListRemoveNull(treeView.getSelectionModel().getSelectedItems());
		TreeItem<BaseNode> selectedItem = treeView.getSelectionModel().getSelectedItem();
		if (null != selectedItems && selectedItems.isEmpty() && null != selectedItem) {
			selectedItems.add(selectedItem);
		}
		return selectedItems;
	}
	
	private void clearSelection() {
		clearSelection(srTree);
		clearSelection(tgTree);
	}
	
	private void clearSelection(TreeView<BaseNode> treeView) {
		treeView.getSelectionModel().clearSelection();
	}
	
	/*-------------------------FieldActionEvent-----------------------------*/
	
	@Override public void filterField(ActionEvent event) {
		try {
			clearPaneData();
			shareTopic();
			super.filterField(event);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override public void displayField(ActionEvent event) {
		try {
			clearPaneData();
			shareTopic();
			super.displayField(event);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	@Override public void save(ActionEvent event) {
		try {
			shareTopic();
			super.save(event);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private void clearPaneData() {
		AdhocData.roots.remove("/view/P112TopicFilterAnchorPane.fxml");
		AdhocData.roots.remove("/view/P113TopicDisplayAnchorPane.fxml");
	}
	
	/*-------------------------shareTopic-----------------------------*/
	
	private void shareTopic() {
		DBTree topicTree = null;
		if (!tgSuperRoot.getChildren().isEmpty()) {
			for (DBTree tree : topic.getTrees()) {
				if (tgSuperRoot.getChildren().get(0).getValue().getTreeId().equals(tree.getId())) {
					topicTree = new DBTree(tree);
					// set root
					DBNode root = new DBNode((DBNode)tgSuperRoot.getValue());
					root.setId("");
					root.setLabel("");
					root.setLabel2("");
					root.setResourceId(tree.getId());
					root.setTreeId(tree.getId());
					root.setRank(0);
					transferTopicTree(root, tgSuperRoot);
					if (null != topic.getTree() && null != topic.getTree().getRoot()) {
						if (topic.getTree().getRoot().getId().equals(root.getId())) {
							transferTopicTree(root, topic.getTree().getRoot());
						}
					}
					topicTree.setRoot(root);
					// set fields
					setFields(tree, topicTree);
					// set others
					topicTree.setOthers();
					// set filter
					setFilters(topicTree);
					break;
				}
			}
		}
		topic.setTree(topicTree);
		topic.setOthers();
		ShareDataService.share(topic);
	}
	
	private void transferTopicTree(DBNode root, TreeItem<BaseNode> superRoot) {
		List<Item> items = new ArrayList<>();
		List<DBNode> dbNodes = new ArrayList<>();
		for (TreeItem<BaseNode> child : superRoot.getChildren()) {
			BaseNode baseNode = child.getValue();
			if (baseNode instanceof Item) {
				Item item = new Item((Item)baseNode);
				items.add(item);
			} else if (baseNode instanceof DBNode) {
				DBNode dbNode = new DBNode((DBNode)baseNode);
				transferTopicTree(dbNode, child);
				dbNodes.add(dbNode);
			} 
		}
		for (int i = 0; i < items.size(); i ++) {
			items.get(i).setLabel2(items.get(i).getLabel());
			items.get(i).setRank(i);
		}
		for (int i = 0; i < dbNodes.size(); i ++) {
			dbNodes.get(i).setLabel2(dbNodes.get(i).getLabel());
			dbNodes.get(i).setRank(i + items.size());
		}
		root.setItems(items);
		root.setChildren(dbNodes);
	}
	
	private void transferTopicTree(BaseNode root, DBNode oldRoot) {
		for (BaseNode child : root.getNodes()) {
			transferTopicTree(child, oldRoot);
		}
		List<BaseNode> newNodes = new ArrayList<>();
		for (BaseNode child : root.getNodes()) {
			BaseNode keyChild = getNodeFromTreeById(oldRoot, child.getId());
			if (null != keyChild) {
				child.setLabel2(keyChild.getLabel2());
				child.setRank(keyChild.getRank());
				newNodes.add(child);
			}
		}
		Collections.sort(newNodes, new SortByRank());
		for (BaseNode child : root.getNodes()) {
			BaseNode keyChild = getNodeFromTreeById(oldRoot, child.getId());
			if (null == keyChild) {
				newNodes.add(child);
			}
		}
		for (int i = 0; i < newNodes.size(); i ++) {
			newNodes.get(i).setRank(i);
		}
	}
	
	public static BaseNode getNodeFromTreeById(BaseNode root, String keyId) {
		if (keyId.equals(root.getId())) {
			return root;
		}
		for (BaseNode child : root.getNodes()) {
			BaseNode keyChild = getNodeFromTreeById(child, keyId);
			if (null != keyChild) {
				return keyChild;
			}
		}
		return null;
	}
	
	private class SortByRank implements Comparator<BaseNode> {
	    @Override public int compare(BaseNode a, BaseNode b) {
	        return a.getRank().compareTo(b.getRank());
	    }
	}
	
	private void setFields(DBTree srTree, DBTree tgTree) {
		List<Field> fields = new ArrayList<>();
		for (Field field : srTree.getFields()) {
			if (findItemFromTreeByResId(tgTree.getRoot(), srTree.getId()+"."+field.getId())) {
				fields.add(new Field(field));
			}
		}
		tgTree.setFields(fields);
	}
	
	private boolean findItemFromTreeByResId(DBNode root, String keyResId) {
		for (DBNode child : root.getChildren()) {
			if (findItemFromTreeByResId(child, keyResId)) {
				return true;
			}
		}
		for (Item item : root.getItems()) {
			if (keyResId.equals(item.getResourceId())) {
				return true;
			}
		}
		return false;
	}
	
	private void setFilters(DBTree tree) {
		List<Filter> filters = new ArrayList<>();
		for (Filter filter : topic.getFilters()) {
			if (null != tree.getFieldByResId(filter.getResourceId())) {
				filters.add(filter);
			}
		}
		topic.setFilters(filters);
	}
	
}
