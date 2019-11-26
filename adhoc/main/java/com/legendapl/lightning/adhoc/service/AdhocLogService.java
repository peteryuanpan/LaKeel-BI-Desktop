package com.legendapl.lightning.adhoc.service;

import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.CalculateType;
import com.legendapl.lightning.adhoc.common.DataFormat;
import com.legendapl.lightning.adhoc.common.GroupType;
import com.legendapl.lightning.adhoc.controller.P121AdhocAnchorPane;
import com.legendapl.lightning.adhoc.custom.FilterVBox;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.factory.StatementFactory;
import com.legendapl.lightning.adhoc.model.BaseNode;

import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public class AdhocLogService {
	
	static Logger logger = Logger.getLogger(AdhocLogService.class);

	public static Boolean sleep = false;
	
	private static String getStatementStackFormatString(String context) {
		return StatementFactory.getEventTypeName() + ", " + P121AdhocAnchorPane.viewModelType + ", " + context;
	}
	
	static String getId(Node node) {
		return getIds(Arrays.asList(node));
	}
	
	static String getIds(List<Node> list) {
		String ids = new String();
		for (Node node : list) {
			if (!ids.isEmpty()) ids += ", ";
			ids += node.getId().trim();
		}
		return ids;
	}
	
	static String getResId(TreeItem<BaseNode> treeItem) {
		return getResIds(Arrays.asList(treeItem));
	}
	
	static String getResIds(List<TreeItem<BaseNode>> list) {
		String ids = new String();
		for (TreeItem<BaseNode> treeItem : list) {
			if (!ids.isEmpty()) ids += ", ";
			ids += treeItem.getValue().getResourceId().trim();
		}
		return ids;
	}
	
	static String getLabal(FilterVBox vbox) {
		return getLabels(Arrays.asList(vbox));
	}
	
	static String getLabels(List<FilterVBox> vboxs) {
		String labels = new String();
		for (FilterVBox vbox : vboxs) {
			if (!labels.isEmpty()) labels += ", ";
			labels += vbox.getLabel().getText().trim();
		}
		return labels;
	}
	
	public static void info(String context) {
		if (AdhocLogService.sleep) return;
		if (StatementFactory.onlyPush) return;
		logger.info(getStatementStackFormatString(context));
	}
	
	public static void selectComboBoxValue(AdhocModelType oldValue, AdhocModelType newValue) {
		info(AdhocUtils.getString("AOL_handleActionComboBoxSelectValueImpl", oldValue, newValue));
	}
	
	public static void moveTreeItemToOppsite(TreeView<BaseNode> thisTreeView, TreeView<BaseNode> oppTreeView, List<TreeItem<BaseNode>> selectedItems) {
		info(AdhocUtils.getString("AOL_handleActionMoveTreeItemToOppsiteImpl", getId(thisTreeView), getId(oppTreeView), getResIds(selectedItems)));
	}
	
	public static void addLayoutChildren(LayoutFlowPane flow, List<Node> newFlowChildren, Integer indexNode) {
		info(AdhocUtils.getString("AOL_handleActionAddLayoutChildrenImpl", getId(flow), indexNode, getIds(newFlowChildren)));
	}
	
	public static void deleteLayoutChildren(LayoutFlowPane flow, List<Node> newFlowChildren, Integer indexNode) {
		info(AdhocUtils.getString("AOL_handleActionDeleteLayoutChildrenImpl", getId(flow), indexNode, getIds(newFlowChildren)));
	}
	
	public static void moveLayoutChildLeft(LayoutFlowPane flow, Node node, Integer index) {
		info(AdhocUtils.getString("AOL_handleActionMoveLayoutChildLeftImpl", getId(flow), index, getId(node)));
	}
	
	public static void moveLayoutChildRight(LayoutFlowPane flow, Node node, Integer index) {
		info(AdhocUtils.getString("AOL_handleActionMoveLayoutChildRightImpl", getId(flow), index, getId(node)));
	}
	
	public static void moveLayoutChild(LayoutFlowPane fromFlow, LayoutFlowPane toFlow, Node node, Integer fromIndex, Integer toIndex) {
		info(AdhocUtils.getString("AOL_handleActionMoveLayoutChildImpl", getId(fromFlow), fromIndex, getId(node), getId(toFlow), toIndex));
	}
	
	public static void moveLayoutChild(LayoutFlowPane fromFlow, LayoutFlowPane toFlow, List<Node> nodes, Integer fromIndex, Integer toIndex) {
		info(AdhocUtils.getString("AOL_handleActionMoveLayoutChildImpl", getId(fromFlow), fromIndex, getIds(nodes), getId(toFlow), toIndex));
	}
	
	public static void switchCrossTableLayout(LayoutFlowPane columnFlow, LayoutFlowPane rowFlow) {
		info(AdhocUtils.getString("AOL_switchCrossTableLayoutImpl", getId(columnFlow), getId(rowFlow)));
	}
	
	public static void addCalculateSwitch(LayoutFlowPane flow, Node node, Integer index) {
		info(AdhocUtils.getString("AOL_handleActionAddCalculateSwitchImpl", getId(flow), index, getId(node)));
	}
	
	public static void deleteCalculateSwitch(LayoutFlowPane flow, Node node, Integer index) {
		info(AdhocUtils.getString("AOL_handleActionDeleteCalculateSwitchImpl", getId(flow), index, getId(node)));
	}
	
	public static void changeCalculateType(LayoutFlowPane flow, Node node, Integer index, CalculateType lastCalculateType, CalculateType calculateType) {
		info(AdhocUtils.getString("AOL_handleActionChangeCalculateTypeImpl", getId(flow), index, getId(node), lastCalculateType, calculateType));
	}
	
	public static void changeDataFormat(LayoutFlowPane flow, Node node, Integer index, DataFormat lastDataFormat, DataFormat dataFormat) {
		info(AdhocUtils.getString("AOL_handleActionChangeDataFormatImpl", getId(flow), index, getId(node), lastDataFormat, dataFormat));
	}
	
	public static void changeGroupType(LayoutFlowPane flow, Node node, Integer index, GroupType lastGroupType, GroupType groupType) {
		info(AdhocUtils.getString("AOL_handleActionChangeGroupTypeImpl", getId(flow), index, getId(node), lastGroupType, groupType));
	}
	
	public static void addFilter(List<FilterVBox> vboxs, Integer index) {
		info(AdhocUtils.getString("AOL_handleActionAddFilterImpl", index, getLabels(vboxs)));
	}
	
	public static void deleteFilter(List<FilterVBox> vboxs) {
		info(AdhocUtils.getString("AOL_handleActionDeleteFilterImpl", getLabels(vboxs)));
	}
	
	public static void deleteAllFilter(List<FilterVBox> vboxs) {
		info(AdhocUtils.getString("AOL_handleActionDeleteAllFilterImpl", getLabels(vboxs)));
	}
	
	public static void moveFilterUp(FilterVBox vbox, Integer index) {
		info(AdhocUtils.getString("AOL_handleActionMoveFilterUpImpl", index, getLabal(vbox)));
	}
	
	public static void moveFilterDown(FilterVBox vbox, Integer index) {
		info(AdhocUtils.getString("AOL_handleActionMoveFilterDownImpl", index, getLabal(vbox)));
	}
	
	public static void moveFilter(FilterVBox vbox0, Integer index0, FilterVBox vbox1, Integer index1) {
		info(AdhocUtils.getString("AOL_handleActionMoveFilterImpl", index0, getLabal(vbox0), index1, getLabal(vbox1)));
	}
	
	public static void doSaveSort() {
		info(AdhocUtils.getString("AOL_doSaveSortImpl"));
	}
	
}
