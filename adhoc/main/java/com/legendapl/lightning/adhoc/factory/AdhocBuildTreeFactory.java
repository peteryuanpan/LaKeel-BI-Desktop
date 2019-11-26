package com.legendapl.lightning.adhoc.factory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.ViewType;
import com.legendapl.lightning.adhoc.controller.P121AdhocAnchorPane;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.service.SplitTreeService;
import com.legendapl.lightning.adhoc.service.TreeTransferService;

import javafx.scene.control.TreeItem;

public class AdhocBuildTreeFactory extends AdhocBaseFactory {
	
	public static void clearTree() {
		fieldTreeView.getSelectionModel().clearSelection();
		valueTreeView.getSelectionModel().clearSelection();
	}
	
	public static void expandTree() {
		AdhocUtils.getAllItems(fieldSuperRoot).forEach(item -> item.setExpanded(true));
		AdhocUtils.getAllItems(valueSuperRoot).forEach(item -> item.setExpanded(true));
	}
	
	public static void buildTree() {
		TreeTransferService.transferTree(alSuperRoot, Arrays.asList(adhoc.getTopicTree()));
		AdhocUtils.getAllItems(alSuperRoot).forEach(treeItem -> {
			Collections.sort(treeItem.getChildren(), new SortByRank());
		});
		SplitTreeService.setTreeViewTypeBySubTree(alSuperRoot);
		SplitTreeService.insertTargetTree(alSuperRoot, fieldSuperRoot, ViewType.BOTTOM);
		SplitTreeService.insertTargetTree(alSuperRoot, valueSuperRoot, ViewType.TOP);
		initItemTreeFactoryModel();
	}
	
	private static class SortByRank implements Comparator<TreeItem<BaseNode>> {
		@Override public int compare(TreeItem<BaseNode> node1, TreeItem<BaseNode> node2) {
			Integer rank1 = node1.getValue().getRank();
			Integer rank2 = node2.getValue().getRank();
			if (null == rank1) rank1 = 0;
			if (null == rank2) rank2 = 0;
			return rank1 - rank2;
		}
	}
	
	public static void insertTree(Field field, AdhocModelType modelType) {
		
		adhoc.getTopicTree().getFields().add(field);
		adhoc.getTopicTree().getResIdToField().put(field.getResourceId(), field);
		
		DBNode root = adhoc.getTopicTree().getRoot();
		Item item = new Item(field);
		item.setTreeId(adhoc.getTopicTree().getId());
		item.setRank(root.getNodes().size());
		
		switch (modelType) {
		case MEASURE:
			item.setViewType(ViewType.BOTTOM);
			break;
		case FIELD:
		default:
			item.setViewType(ViewType.TOP);
			break;
		}
		root.getItems().add(item);
		
		buildTree();
	}
	
	public static void moveTreeItemToMeasure(List<TreeItem<BaseNode>> selectedItems) {
		splitTree(selectedItems, ViewType.BOTTOM);
		SplitTreeService.setTreeExpanded(selectedItems, valueSuperRoot);
		initItemTreeFactoryModel();
	}
	
	public static void moveTreeItemToField(List<TreeItem<BaseNode>> selectedItems) {
		splitTree(selectedItems, ViewType.TOP);
		SplitTreeService.setTreeExpanded(selectedItems, fieldSuperRoot);
		initItemTreeFactoryModel();
	}
	
	static void splitTree(List<TreeItem<BaseNode>> selectedItems, ViewType FLAG_TO_SET) {
		SplitTreeService.splitTree(selectedItems, alSuperRoot, FLAG_TO_SET, fieldSuperRoot, ViewType.BOTTOM, valueSuperRoot, ViewType.TOP);
	}
	
	public static void initItemTreeFactoryModel() {
		switch (P121AdhocAnchorPane.viewModelType) {
		case TABLE:
			tableItemTreeFactory.initModel();
			break;
		case CROSSTABLE:
			crossTableFieldTreeFactory.initModel();
			crossTableValueTreeFactory.initModel();
			break;
		default:
			break;
		}
	}
	
}
