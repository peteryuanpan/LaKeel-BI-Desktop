package com.legendapl.lightning.adhoc.factory;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.common.AdhocModelType;

import javafx.scene.Node;

/**
 * テーブルモジュールを変更時、ライオウとバンドのデータを処理するサービス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.25
 */
public class LayoutStoreFactory {

	private static AdhocModelType modelType;
	private static LayoutStore forTable;
	private static LayoutStore forCrossTable;
	
	public static void init(AdhocModelType type, List<Node> columnFlowChildren, List<Node> rowFlowChildren) {
		modelType = null;
		forTable = new LayoutStore();
		forCrossTable = new LayoutStore();
		convertToModel(type, columnFlowChildren, rowFlowChildren);
	}
	
	public static void convertToModel(AdhocModelType type, List<Node> columnFlowChildren, List<Node> rowFlowChildren) {
		switch (type) {
		case TABLE:
			convertToTableModel(columnFlowChildren, rowFlowChildren);
			break;
		case CROSSTABLE:
			convertToCrossTableModel(columnFlowChildren, rowFlowChildren);
			break;
		default:
			break;
		}
	}
	
	public static void convertToTableModel(List<Node> columnFlowChildren, List<Node> rowFlowChildren) {
		if (modelType != AdhocModelType.TABLE) {
			modelType = AdhocModelType.TABLE;
			convertModel(forTable, forCrossTable, columnFlowChildren, rowFlowChildren);
		}
	}
	
	public static void convertToCrossTableModel(List<Node> columnFlowChildren, List<Node> rowFlowChildren) {
		if (modelType != AdhocModelType.CROSSTABLE) {
			modelType = AdhocModelType.CROSSTABLE;
			convertModel(forCrossTable, forTable, columnFlowChildren, rowFlowChildren);
		}
	}
	
	private static void convertModel(
			LayoutStore thisLayout, LayoutStore oppLayout,
			List<Node> columnFlowChildren, List<Node> rowFlowChildren) {
		
		// columnFlow
		oppLayout.columnFlowChildren.clear();
		oppLayout.columnFlowChildren.addAll(columnFlowChildren);
		columnFlowChildren.clear();
		columnFlowChildren.addAll(thisLayout.columnFlowChildren);
		// rowFlow
		oppLayout.rowFlowChildren.clear();
		oppLayout.rowFlowChildren.addAll(rowFlowChildren);
		rowFlowChildren.clear();
		rowFlowChildren.addAll(thisLayout.rowFlowChildren);
	}
	
	public static class LayoutStore {
		private List<Node> columnFlowChildren;
		private List<Node> rowFlowChildren;
		public LayoutStore() {
			this.columnFlowChildren = new ArrayList<>();
			this.rowFlowChildren = new ArrayList<>();
		}
		public List<Node> getColumnFlowChildren() {
			return columnFlowChildren;
		}
		public List<Node> getRowFlowChildren() {
			return rowFlowChildren;
		}
	}
	
	public static LayoutStore getTableModel() {
		if (null == forTable) {
			forTable = new LayoutStore();
		}
		return forTable;
	}
	
	public static LayoutStore getCrossTableModel() {
		if (null == forCrossTable) {
			forCrossTable = new LayoutStore();
		}
		return forCrossTable;
	}
	
}
