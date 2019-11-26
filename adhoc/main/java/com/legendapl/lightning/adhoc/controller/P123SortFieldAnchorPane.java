package com.legendapl.lightning.adhoc.controller;

import java.net.URL;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.controlsfx.control.ListSelectionView;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.SortData;
import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.ViewType;
import com.legendapl.lightning.adhoc.factory.AdhocBaseFactory;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.service.AdhocLogService;
import com.legendapl.lightning.adhoc.service.ShareDataService;
import com.legendapl.lightning.adhoc.service.SplitTreeService;
import com.legendapl.lightning.adhoc.service.TreeTransferService;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class P123SortFieldAnchorPane extends C100AdhocBaseAnchorPane {

	/*-------------------------data-----------------------------*/

	@FXML
	private ListSelectionView<AnchorPane> selectBox;
	@FXML
	private Button confirmButton;
	@FXML
	private Button cancelButton;

	private AnchorPane srAnchorPane;
	private AnchorPane tgAnchorPane;
	private TreeView<BaseNode> srTree;
	private TreeView<BaseNode> tgTree;

	private TreeItem<BaseNode> alSuperRoot;
	private TreeItem<BaseNode> srSuperRoot;
	private TreeItem<BaseNode> tgSuperRoot;

	private Button moveToTarget;
	private Button moveToSource;
	private Adhoc adhoc = ShareDataService.loadAdhoc();
	
	private Map<String, SortIconView> iconViewMap = new HashMap<>();
	private Map<String, Integer> selectOrder = new HashMap<>();
	
	/*-----------------------------------Initialize-----------------------------------*/

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		Platform.runLater(() -> {
			initLabel();
			initSource();
			initTarget();
			initButton();
			initTree();
		});
	}
	
	private void initLabel() {
		Label sourceHeader = (Label) selectBox.getSourceHeader();
		sourceHeader.setText(AdhocUtils.getString("P123.title.selectField"));
		Label targetHeader = (Label) selectBox.getTargetHeader();
		targetHeader.setText(AdhocUtils.getString("P123.title.selectSort"));
	}
	
	private void initSource() {
		
		srAnchorPane = new AnchorPane();
		srTree = new TreeView<BaseNode>();
		srSuperRoot = new TreeItem<BaseNode>(new DBNode());
		srSuperRoot.getValue().setViewType(ViewType.LEFT);
		srTree.setRoot(srSuperRoot);
		srTree.setShowRoot(false);
		srTree.getSelectionModel().clearSelection();
		srTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		srTree.setOnMouseClicked(event -> setOnMouseClickedSrTree(event));
		srTree.setCellFactory(treeView -> new SrTreeCell());
		
		setSrTreeSelectedItemProperty();
		
		GridPane gridPane = (GridPane) selectBox.getChildrenUnmodifiable().get(0);
		ListView<?> srListView = (ListView<?>) gridPane.getChildren().get(2);
		srTree.prefHeightProperty().bind(srListView.heightProperty());
		
		AnchorPane.setTopAnchor(srTree, (double) -5);
		AnchorPane.setBottomAnchor(srTree, (double) -5);
		AnchorPane.setLeftAnchor(srTree, (double) -10);
		AnchorPane.setRightAnchor(srTree, (double) -10);
		
		srAnchorPane.getChildren().add(srTree);
		selectBox.getSourceItems().clear();
		selectBox.getSourceItems().add(srAnchorPane);
	}
	
	private void setSrTreeSelectedItemProperty() {
		srTree.getSelectionModel().selectedItemProperty().addListener((record, oldVal, newVal) -> {
			if (null != newVal) {
				Integer maxSelectOrder = 0;
				for (Integer selectOrder : selectOrder.values()) {
					maxSelectOrder = Math.max(maxSelectOrder, selectOrder);
				};
				selectOrder.put(newVal.getValue().getResourceId(), 1 + maxSelectOrder);
			}
		});
	}
	
	private void initTarget() {

		tgAnchorPane = new AnchorPane();
		tgTree = new TreeView<BaseNode>();
		tgSuperRoot = new TreeItem<BaseNode>(new DBNode());
		tgSuperRoot.getValue().setViewType(ViewType.RIGHT);
		tgTree.setRoot(tgSuperRoot);
		tgTree.setShowRoot(false);
		tgTree.getSelectionModel().clearSelection();
		tgTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		tgTree.setOnMouseClicked(event -> setOnMouseClickedTgTree(event));
		tgTree.setCellFactory(treeView -> new TgTreeCell());

		GridPane gridPane = (GridPane) selectBox.getChildrenUnmodifiable().get(0);
		ListView<?> tgListView = (ListView<?>) gridPane.getChildren().get(2);
		tgTree.prefHeightProperty().bind(tgListView.heightProperty());

		HBox hBox = new HBox();
		VBox vBox = new VBox(5);
		setTargetButton(vBox);
		vBox.setMinWidth(40);
		hBox.getChildren().add(tgTree);
		hBox.getChildren().add(vBox);
		HBox.setMargin(tgTree, new Insets(-5, 0, -5, -10));
		HBox.setMargin(vBox, new Insets(-5, -10, -5, 0));
		AnchorPane.setLeftAnchor(hBox, 0.0);
		AnchorPane.setTopAnchor(hBox, 0.0);
		AnchorPane.setRightAnchor(hBox, 0.0);
		AnchorPane.setBottomAnchor(hBox, 0.0);
		vBox.setAlignment(Pos.TOP_CENTER);
		vBox.setPadding(new Insets(5, 5, 0, 0));
		vBox.setStyle("-fx-background-color: white");
		HBox.setHgrow(tgTree, Priority.ALWAYS);
		HBox.setHgrow(vBox, Priority.NEVER);
		tgAnchorPane.getChildren().add(hBox);
		selectBox.getTargetItems().clear();
		selectBox.getTargetItems().add(tgAnchorPane);
		vBox.setOnMouseClicked(event -> {
			event.consume();
		});
		hBox.setOnMouseClicked(event -> {
			event.consume();
		});
	}

	private void initButton() {
		GridPane gridPane = (GridPane) selectBox.getChildrenUnmodifiable().get(0);
		StackPane stackPane = (StackPane) gridPane.getChildren().get(4);
		VBox vbox = (VBox) stackPane.getChildren().get(0);
		moveToTarget = (Button) vbox.getChildren().get(0);
		moveToTarget.disableProperty().bind(moveToOppsiteButtonDisable(srTree));
		moveToTarget.setOnAction(event -> moveToTargetAction());
		moveToSource = (Button) vbox.getChildren().get(2);
		moveToSource.disableProperty().bind(moveToOppsiteButtonDisable(tgTree));
		moveToSource.setOnAction(event -> moveToSourceAction());
		vbox.getChildren().clear();
		vbox.getChildren().addAll(moveToTarget, moveToSource);
	}
	
	private void initTree() {
		alSuperRoot = TreeTransferService.copyTree(AdhocBaseFactory.alSuperRoot);
		SplitTreeService.setTreeViewType(alSuperRoot, ViewType.LEFT);
		setTgTreeViewType();
		SplitTreeService.insertTargetTree(alSuperRoot, srSuperRoot, ViewType.RIGHT);
		SplitTreeService.insertTargetTree(alSuperRoot, tgSuperRoot, ViewType.LEFT);
		initSelectOrder();
		resetTgTree();
		AdhocUtils.getAllItems(srSuperRoot).forEach(item -> item.setExpanded(true));
	}
	
	private void setTgTreeViewType() {
		if (null != adhoc.getColumnSort()) {
			for (SortData sortData : adhoc.getColumnSort()) {
				TreeItem<BaseNode> node = SplitTreeService.getNodeFromTreeByResId(alSuperRoot, sortData.getResourceId());
				if (null != node) {
					node.getValue().setViewType(ViewType.RIGHT);
					SortIconView icon = new SortIconView(sortData.getSortFlg(), inGroup(sortData));
					iconViewMap.put(sortData.getResourceId(), icon);
				}
			}
			SplitTreeService.setTreeViewTypeBySubTree(alSuperRoot);
		}
	}
	
	private void initSelectOrder() {
		if (null != adhoc.getColumnSort()) {
			for (int i = 0; i < adhoc.getColumnSort().size(); i ++) {
				SortData sortData = adhoc.getColumnSort().get(i);
				TreeItem<BaseNode> node = SplitTreeService.getNodeFromTreeByResId(alSuperRoot, sortData.getResourceId());
				if (null != node) {
					selectOrder.put(node.getValue().getResourceId(), 1 + i);
				}
			}
		}
	}
	
	private boolean inGroup(SortData sortData) {
		return inGroup(sortData.getResourceId());
	}
	
	private boolean inGroup(String resId) {
		for (TableField group : AdhocBaseFactory.tableRows) {
			if (resId.equals(group.getResourceId())) return true;
		}
		return false;
	}
	
	/*-----------------------------------TreeCell-----------------------------------*/

	private class SrTreeCell extends TreeCell<BaseNode> {
		@Override
		public void updateItem(BaseNode item, boolean empty) {
			super.updateItem(item, empty);
			if (null != item) {
				this.setText(item.getLabel());
				this.setTooltip(new Tooltip(item.getResourceId()));
			} else {
				this.setText(null);
				this.setTooltip(null);
			}
		}
	}
	
	private class TgTreeCell extends TreeCell<BaseNode> {
		
		@Override
		public void updateItem(BaseNode item, boolean empty) {
			super.updateItem(item, empty);
			if (null != item) {
				this.setText(item.getLabel());
				this.setTooltip(new Tooltip(item.getResourceId()));
				this.setIcon(item.getResourceId());
			} else {
				this.setText(null);
				this.setTooltip(null);
				this.setGraphic(null);
			}
		}
		
		void setIcon(String resId) {
			if (iconViewMap.get(resId) == null) {
				iconViewMap.put(resId, new SortIconView(true, inGroup(resId)));
			}
			SortIconView icon = iconViewMap.get(resId);
			this.setGraphic(icon);
		}
	}
	
	/*-----------------------------------Event-----------------------------------*/

	private ObservableValue<? extends Boolean> moveToOppsiteButtonDisable(TreeView<BaseNode> treeView) {
		return new BooleanBinding() {
			{
				super.bind(treeView.getSelectionModel().getSelectedItems());
			}
			@Override
			protected boolean computeValue() {
				List<TreeItem<BaseNode>> selectedItems = getSelectedItems(treeView);
				if (null == selectedItems || selectedItems.isEmpty()) return true;
				for (TreeItem<BaseNode> selectedItem : selectedItems) {
					if (selectedItem.getValue() instanceof DBNode) return true;
				}
				return false;
			}
		};
	}
	
	/**
	 * click srTree
	 * @param event
	 */
	private void setOnMouseClickedSrTree(MouseEvent event) {
		if (!checkOnMouseClickedTree(event, srTree)) return;
		if (MouseButton.PRIMARY.equals(event.getButton())) {
			if (2 == event.getClickCount()) {
				moveToTargetAction();
			}
		}
	}

	/**
	 * click tgTree
	 * @param event
	 */
	private void setOnMouseClickedTgTree(MouseEvent event) {
		if (!checkOnMouseClickedTree(event, tgTree)) return;
		if (MouseButton.PRIMARY.equals(event.getButton())) {
			if (2 == event.getClickCount()) {
				moveToSourceAction();
			}
		}
	}

	@SuppressWarnings("unchecked")
	private boolean checkOnMouseClickedTree(MouseEvent event, TreeView<BaseNode> treeView) {
		
		TreeItem<BaseNode> treeItem = treeView.getSelectionModel().getSelectedItem();
		if (null == treeItem) return false;
		if (null == treeItem.getValue()) return false;
		
		List<TreeItem<BaseNode>> selectedItems = treeView.getSelectionModel().getSelectedItems();
		if (null == selectedItems) return false;
		for (TreeItem<BaseNode> selectedItem : selectedItems) {
			if (selectedItem.getValue() instanceof DBNode) return false;
		}
		
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode) return false;
		if (clickedNode instanceof StackPane) return false;
		if (clickedNode instanceof SortIconView) return false;
		
		if (clickedNode instanceof TreeCell) {
			TreeCell<BaseNode> treeCell = (TreeCell<BaseNode>) clickedNode;
			if (null == treeCell.getText()) return false;
		}
		
		return true;
	}

	/**
	 * move tree item from source to target
	 */
	private void moveToTargetAction() {
		List<TreeItem<BaseNode>> selectedItems = getSelectedItems(srTree);
		clearSelection();
		splitTree(selectedItems, ViewType.RIGHT);
		resetTgTree();
		SplitTreeService.setTreeSelection(selectedItems, tgTree);
	}

	/**
	 * move tree item from target to source
	 */
	private void moveToSourceAction() {
		List<TreeItem<BaseNode>> selectedItems = getSelectedItems(tgTree);
		clearSelection();
		splitTree(selectedItems, ViewType.LEFT);
		resetTgTree();
		SplitTreeService.setTreeSelection(selectedItems, srTree);
	}
	
	private void splitTree(List<TreeItem<BaseNode>> selectedItems, ViewType FLAG_TO_SET) {
		SplitTreeService.splitTree(selectedItems, alSuperRoot, FLAG_TO_SET, srSuperRoot, ViewType.RIGHT, tgSuperRoot, ViewType.LEFT);
	}
	
	private void resetTgTree() {
		if (!tgSuperRoot.getChildren().isEmpty()) { // SHOULD BE NOT EMPTY
			List<TreeItem<BaseNode>> leaves = AdhocUtils.getAllLeaves(tgSuperRoot);
			tgSuperRoot.getChildren().clear();
			tgSuperRoot.getChildren().addAll(leaves);
			Collections.sort(tgSuperRoot.getChildren(), new SortBySelectOrder());
		}
	}
	
	private class SortBySelectOrder implements Comparator<TreeItem<BaseNode>> {
		@Override public int compare(TreeItem<BaseNode> treeItem1, TreeItem<BaseNode> treeItem2) {
			Integer selectOrder1 = selectOrder.get(treeItem1.getValue().getResourceId());
			Integer selectOrder2 = selectOrder.get(treeItem2.getValue().getResourceId());
			if (null == selectOrder1) selectOrder1 = 0;
			if (null == selectOrder2) selectOrder2 = 0;
			return selectOrder1 - selectOrder2;
	    }
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
	
	/**
	 * OK, save parameters
	 * @param event
	 */
	public void handleActionClickedConfirmButton(ActionEvent event) {
		doSaveSort();
		handleActionCloseWindow(event);
	}

	/**
	 * フィルタを上へ移動
	 * 
	 * @param vbox
	 */
	private void doSaveSort() {

		ObservableList<SortData> oldList = FXCollections.observableArrayList(adhoc.getColumnSort());
		ObservableList<SortData> newList = getNewSortList();

		if (canSave(oldList, newList)) {
			StatementFactory.runLater(
				() -> doSaveSortImpl(newList),
				() -> doSaveSortImpl(oldList)
			);
		}
	}

	private boolean canSave(ObservableList<SortData> oldList, ObservableList<SortData> newList) {
		if (oldList.size() != newList.size())
			return true;
		for (int i = 0; i < oldList.size(); i++) {
			if (!oldList.get(i).equals(newList.get(i)))
				return true;
		}
		return false;
	}

	private void doSaveSortImpl(ObservableList<SortData> sortList) {
		AdhocLogService.doSaveSort();
		adhoc.getColumnSort().clear();
		adhoc.getColumnSort().addAll(sortList);
		// DO NOT SET A NEW LIST
	}

	/**
	 * do save sort
	 */
	private ObservableList<SortData> getNewSortList() {

		ObservableList<SortData> sortList = FXCollections.observableArrayList();

		tgSuperRoot.getChildren().forEach(treeItem -> {

			SortIconView icon = getIcon(treeItem);
			SortData sort = new SortData();

			String resId = treeItem.getValue().getResourceId();
			sort.setResourceId(resId);
			sort.setSortFlg(null == icon ? true : icon.isAscending);

			String[] sortTemp = resId.split(Pattern.quote("."));
			sort.setTableId(sortTemp.length > 2 ? sortTemp[sortTemp.length - 2] : sortTemp[0]);
			sort.setColumnId(sortTemp[sortTemp.length - 1]);

			sortList.add(sort);
		});
		
		return sortList;
	}

	/**
	 * get Icon of treeItem
	 * @param treeItem
	 * @return
	 */
	private SortIconView getIcon(TreeItem<BaseNode> treeItem) {
		
		TgTreeCell tgTreeCell = (TgTreeCell) tgTree.getCellFactory().call(tgTree);
		tgTreeCell.updateItem(treeItem.getValue(), false);
		
		String resId = treeItem.getValue().getResourceId();
		SortIconView icon = iconViewMap.get(resId);
		
		return icon;
	}

	/**
	 * Cancel, not save
	 * @param event
	 */
	public void handleActionClickedCancelButton(ActionEvent event) {
		handleActionCloseWindow(event);
	}
	
	/**
	 * Close window
	 * @param event
	 */
	public void handleActionCloseWindow(ActionEvent event) {
		((Stage) (((Button) event.getSource()).getScene().getWindow())).close();
	}
	
	/**
	 * Target Button
	 * @param vBox
	 */
	private void setTargetButton(VBox vBox) {
		
		Button moveTop = new Button();
		moveTop.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.CHEVRON_DOUBLE_UP));
		moveTop.setOnAction(e -> moveTop(e));
		moveTop.disableProperty().bind(moveUpDisable());
		vBox.getChildren().add(moveTop);
		
		Button moveUp = new Button();
		moveUp.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.CHEVRON_UP));
		moveUp.setOnAction(e -> moveUp(e));
		moveUp.disableProperty().bind(moveUpDisable());
		vBox.getChildren().add(moveUp);

		Button moveDown = new Button();
		moveDown.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.CHEVRON_DOWN));
		moveDown.setOnAction(e -> moveDown(e));
		moveDown.disableProperty().bind(moveDownDisable());
		vBox.getChildren().add(moveDown);
		
		Button moveBottom = new Button();
		moveBottom.setGraphic(new MaterialDesignIconView(MaterialDesignIcon.CHEVRON_DOUBLE_DOWN));
		moveBottom.setOnAction(e -> moveBottom(e));
		moveBottom.disableProperty().bind(moveDownDisable());
		vBox.getChildren().add(moveBottom);
	}
	
	public void moveTop(ActionEvent event) {
		
		List<TreeItem<BaseNode>> items = tgSuperRoot.getChildren();
		TreeItem<BaseNode> item = tgTree.getSelectionModel().getSelectedItem();
		AdhocUtils.moveListElement(items, item, -1 * items.indexOf(item));
		selectItem(items, item);
	}
	
	public void moveUp(ActionEvent event) {
		
		List<TreeItem<BaseNode>> items = tgSuperRoot.getChildren();
		TreeItem<BaseNode> item = tgTree.getSelectionModel().getSelectedItem();
		AdhocUtils.moveListElement(items, item, -1);
		selectItem(items, item);
	}
	
	public void moveDown(ActionEvent event) {
		
		List<TreeItem<BaseNode>> items = tgSuperRoot.getChildren();
		TreeItem<BaseNode> item = tgTree.getSelectionModel().getSelectedItem();
		AdhocUtils.moveListElement(items, item, +1);
		selectItem(items, item);
	}
	
	public void moveBottom(ActionEvent event) {
		
		List<TreeItem<BaseNode>> items = tgSuperRoot.getChildren();
		TreeItem<BaseNode> item = tgTree.getSelectionModel().getSelectedItem();
		AdhocUtils.moveListElement(items, item, items.size() - items.indexOf(item) - 1);
		selectItem(items, item);
	}
	
	private void selectItem(List<TreeItem<BaseNode>> items, TreeItem<BaseNode> item) {
		tgTree.getSelectionModel().clearSelection();
		tgTree.getSelectionModel().select(items.indexOf(item));
	}
	
	private ObservableValue<? extends Boolean> moveUpDisable() {
		return new BooleanBinding() {
			{
				super.bind(tgTree.getSelectionModel().getSelectedItems());
			}
            @Override protected boolean computeValue() {
            	
            	if (moveButtonDisable()) return true;
            	
        		TreeItem<BaseNode> selectedItem = tgTree.getSelectionModel().getSelectedItem();
        		Integer index = tgSuperRoot.getChildren().indexOf(selectedItem);
        		
            	return index <= 0 || index >= tgSuperRoot.getChildren().size();
            }
		};
	}
	
	private ObservableValue<? extends Boolean> moveDownDisable() {
		return new BooleanBinding() {
			{
				super.bind(tgTree.getSelectionModel().getSelectedItems());
			}
            @Override protected boolean computeValue() {
            	
            	if (moveButtonDisable()) return true;
            	
        		TreeItem<BaseNode> selectedItem = tgTree.getSelectionModel().getSelectedItem();
        		Integer index = tgSuperRoot.getChildren().indexOf(selectedItem);
        		
            	return index < 0 || index + 1 >= tgSuperRoot.getChildren().size();
            }
		};
	}
	
	private boolean moveButtonDisable() {
		
		TreeItem<BaseNode> selectedItem = tgTree.getSelectionModel().getSelectedItem();
		List<TreeItem<BaseNode>> selectedItems = getSelectedItems(tgTree);
		
		if (null == selectedItem) return true;
    	if (null == selectedItems) return true;
    	if (selectedItems.size() != 1) return true;
    	
    	return false;
	}
	
}

/**
 * Sort Icon
 */
class SortIconView extends MaterialDesignIconView {
	
	static final MaterialDesignIcon AscendingIcon = MaterialDesignIcon.ARROW_UP_BOLD_CIRCLE_OUTLINE;
	static final MaterialDesignIcon DescendingIcon = MaterialDesignIcon.ARROW_DOWN_BOLD_CIRCLE_OUTLINE;
	static final MaterialDesignIcon GroupIcon = MaterialDesignIcon.GOOGLE;
	
	Boolean isAscending = true;
	Boolean isGroup = false;
	
	public SortIconView() {
		super();
		init(true, false);
	}
	
	public SortIconView(Boolean isAscending, Boolean isGroup) {
		super();
		init(isAscending, isGroup);
	}
	
	void init(Boolean isAscending, Boolean isGroup) {
		this.isAscending = isAscending;
		this.isGroup = isGroup;
		this.setIcon();
		this.setGlyphSize(20);
		if (!isGroup) {
			this.setCursor(Cursor.HAND);
			this.setOnMouseClicked(e -> handleAciontOnMouseClicked(e));
		}
	}
	
	void handleAciontOnMouseClicked(MouseEvent event) {
		isAscending = !isAscending;
		setIcon();
	}
	
	void setIcon() {
		this.setIcon(isGroup ? GroupIcon : isAscending ? AscendingIcon : DescendingIcon);
	}
}
