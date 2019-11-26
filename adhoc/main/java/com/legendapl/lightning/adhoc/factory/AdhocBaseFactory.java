package com.legendapl.lightning.adhoc.factory;

import java.util.List;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.factory.itemTree.CrossTableFieldTreeFactory;
import com.legendapl.lightning.adhoc.factory.itemTree.CrossTableValueTreeFactory;
import com.legendapl.lightning.adhoc.factory.itemTree.TableItemTreeFactory;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.service.BackRunService;
import com.legendapl.lightning.adhoc.service.DatabaseService;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;

public class AdhocBaseFactory {

	public static Adhoc adhoc;
	
	public static TreeView<BaseNode> fieldTreeView;
	public static TreeView<BaseNode> valueTreeView;
	public static TreeItem<BaseNode> alSuperRoot;
	public static TreeItem<BaseNode> fieldSuperRoot;
	public static TreeItem<BaseNode> valueSuperRoot;
	
	public static LayoutFlowPane columnFlow;
	public static LayoutFlowPane rowFlow;
	
	public static List<TableField> tableColumns;
	public static List<TableField> tableRows;
	
	public static List<CrossTableField> crossTableColumns;
	public static List<CrossTableField> crossTableRows;
	public static List<CrossTableField> crossTableValues;
	
	public static TableItemTreeFactory tableItemTreeFactory;
	public static CrossTableFieldTreeFactory crossTableFieldTreeFactory;
	public static CrossTableValueTreeFactory crossTableValueTreeFactory;
	public static FilterPaneFactory filterPaneFactory;
	
	public static TableViewFactory tableViewFactory;
	public static CrossTableViewFactory crossTableViewFactory;
	
	public static DatabaseService databaseService;
	
	public static BackRunService backW;
	public static StackPane spinnerPane;
	
	public static Logger logger = Logger.getLogger(AdhocBaseFactory.class);
	
	public static void setAdhoc(Adhoc adhoc) {
		AdhocBuildTreeFactory.adhoc = adhoc;
	}

	public static void setFieldTreeView(TreeView<BaseNode> fieldTreeView) {
		AdhocBuildTreeFactory.fieldTreeView = fieldTreeView;
	}

	public static void setValueTreeView(TreeView<BaseNode> valueTreeView) {
		AdhocBuildTreeFactory.valueTreeView = valueTreeView;
	}

	public static void setAlSuperRoot(TreeItem<BaseNode> alSuperRoot) {
		AdhocBuildTreeFactory.alSuperRoot = alSuperRoot;
	}

	public static void setFieldSuperRoot(TreeItem<BaseNode> fieldSuperRoot) {
		AdhocBuildTreeFactory.fieldSuperRoot = fieldSuperRoot;
	}

	public static void setValueSuperRoot(TreeItem<BaseNode> valueSuperRoot) {
		AdhocBuildTreeFactory.valueSuperRoot = valueSuperRoot;
	}

	public static void setColumnFlow(LayoutFlowPane columnFlow) {
		AdhocBaseFactory.columnFlow = columnFlow;
	}

	public static void setRowFlow(LayoutFlowPane rowFlow) {
		AdhocBaseFactory.rowFlow = rowFlow;
	}

	public static void setTableColumns(List<TableField> tableColumns) {
		AdhocBaseFactory.tableColumns = tableColumns;
	}

	public static void setTableRows(List<TableField> tableRows) {
		AdhocBaseFactory.tableRows = tableRows;
	}

	public static void setCrossTableColumns(List<CrossTableField> crossTableColumns) {
		AdhocBaseFactory.crossTableColumns = crossTableColumns;
	}

	public static void setCrossTableRows(List<CrossTableField> crossTableRows) {
		AdhocBaseFactory.crossTableRows = crossTableRows;
	}

	public static void setCrossTableValues(List<CrossTableField> crossTableValues) {
		AdhocBaseFactory.crossTableValues = crossTableValues;
	}
	
	public static void setTableItemTreeFactory(TableItemTreeFactory tableItemTreeFactory) {
		AdhocBaseFactory.tableItemTreeFactory = tableItemTreeFactory;
	}

	public static void setCrossTableFieldTreeFactory(CrossTableFieldTreeFactory crossTableFieldTreeFactory) {
		LayoutSwitchFactory.crossTableFieldTreeFactory = crossTableFieldTreeFactory;
	}

	public static void setCrossTableValueTreeFactory(CrossTableValueTreeFactory crossTableValueTreeFactory) {
		LayoutSwitchFactory.crossTableValueTreeFactory = crossTableValueTreeFactory;
	}

	public static void setFilterFactory(FilterPaneFactory filterPaneFactory) {
		AdhocBaseFactory.filterPaneFactory = filterPaneFactory;
	}
	
	public static void setTableViewFactory(TableViewFactory tableViewFactory) {
		AdhocBaseFactory.tableViewFactory = tableViewFactory;
	}

	public static void setCrossTableViewFactory(CrossTableViewFactory crossTableViewFactory) {
		AdhocBaseFactory.crossTableViewFactory = crossTableViewFactory;
	}

	public static void setDatabaseService(DatabaseService databaseService) {
		AdhocBaseFactory.databaseService = databaseService;
	}

	public static void setBackW(BackRunService backW) {
		AdhocBaseFactory.backW = backW;
	}

	public static void setSpinnerPane(StackPane spinnerPane) {
		AdhocBaseFactory.spinnerPane = spinnerPane;
	}
	
}
