package com.legendapl.lightning.adhoc.factory;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.LayoutData;
import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.custom.LayoutLabel;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

/**
 * インポートとエクスポート時、ライオウとバンドのデータを処理するサービス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.23
 */
public class LayoutTransferFactory extends AdhocBaseFactory {
	
	/*-----------------------------------------------transfer-----------------------------------------------*/
	
	/**
	 * transfer to Adhoc from layout
	 * @param thisModelType
	 */
	public static void transferToAdhoc(AdhocModelType thisModelType) {
		LayoutData layoutData = getLayoutData(thisModelType);
		adhoc.setLayoutData(layoutData);
	}
	
	/**
	 * transfer to layout from Adhoc
	 * @param thisModelType
	 */
	public static void transferToLayout(AdhocModelType thisModelType) {
		// clear
		columnFlow.getChildren().clear();
		rowFlow.getChildren().clear();
		
		LayoutData layoutData = adhoc.getLayoutData();
		if (null != layoutData) {
			// Table
			LayoutStoreFactory.convertToTableModel(columnFlow.getChildren(), rowFlow.getChildren());
			transferToTableLayout(layoutData.getTableColumns(), columnFlow, tableColumns);
			transferToTableLayout(layoutData.getTableRows(), rowFlow, tableRows);
			
			// CrossTable
			LayoutStoreFactory.convertToCrossTableModel(columnFlow.getChildren(), rowFlow.getChildren());
			transferToCrossTableLayout(layoutData.getCrossTableColumns(), columnFlow, crossTableColumns);
			transferToCrossTableLayout(layoutData.getCrossTableRows(), rowFlow, crossTableRows);
			if (!layoutData.isRow()) {
				transferToCrossTableLayout(layoutData, columnFlow, crossTableValues);
			} else {
				transferToCrossTableLayout(layoutData, rowFlow, crossTableValues);
			}
			
			// revert
			LayoutStoreFactory.convertToModel(thisModelType, columnFlow.getChildren(), rowFlow.getChildren());
		}
	}
	
	/**
	 * transfer to table layout
	 * @param datas
	 * @param flow
	 * @param fields
	 */
	private static void transferToTableLayout(List<TableField> dataFields, LayoutFlowPane flow, List<TableField> fields) {
		for (TableField field : dataFields) {
			LayoutLabel label = tableItemTreeFactory.getLayoutLabel(field.getModelType(), flow, field);
			flow.getChildren().add(label);
			fields.add(field);
		}
	}
	
	/**
	 * transfer to crossTable layout for field
	 * @param datas
	 * @param flow
	 * @param fields
	 */
	private static void transferToCrossTableLayout(List<CrossTableField> dataFields, LayoutFlowPane flow, List<CrossTableField> fields) {
		for (CrossTableField field : dataFields) {
			LayoutLabel label = crossTableFieldTreeFactory.getLayoutLabel(flow, field);
			flow.getChildren().add(label);
			fields.add(field);
		}
	}
	
	/**
	 * transfer to crossTable layout for measure
	 * @param layoutData
	 * @param flow
	 * @param fields
	 */
	private static void transferToCrossTableLayout(LayoutData layoutData, LayoutFlowPane flow, List<CrossTableField> fields) {
		List<Node> valueNodes = new ArrayList<>();
		valueNodes.add(crossTableValueTreeFactory.getLayoutPoundParent(flow));
		for (CrossTableField field : layoutData.getCrossTableValues()) {
			GridPane gridPane = crossTableValueTreeFactory.getLayoutLabelParent(flow, field);
			valueNodes.add(gridPane);
			fields.add(field);
		}
		Integer index = layoutData.getValueIndex() - 1;
		if (index >= 0 && index <= flow.getChildren().size() && !valueNodes.isEmpty()) {
			flow.getChildren().addAll(index, valueNodes);
		}
	}
	
	/*-----------------------------------------------getLayoutData-----------------------------------------------*/
	
	/**
	 * get layout data
	 * @param thisModelType
	 * @return
	 */
	public static LayoutData getLayoutData(AdhocModelType thisModelType) {
		
		// initialize
		LayoutData layoutData = new LayoutData();
		layoutData.setTableColumns(tableColumns);
		layoutData.setTableRows(tableRows);
		layoutData.setCrossTableColumns(crossTableColumns);
		layoutData.setCrossTableRows(crossTableRows);
		layoutData.setCrossTableValues(crossTableValues);
		
		// convert to CrossTable
		LayoutStoreFactory.convertToCrossTableModel(columnFlow.getChildren(), rowFlow.getChildren());
		
		// valueIndex, row
		layoutData.setValueIndex(0);
		layoutData.setRow(false);
		
		GridPane firstGridPane = null;
		firstGridPane = (GridPane) AdhocUtils.getFirstObject(columnFlow.getChildren(), GridPane.class);
		if (null != firstGridPane) {
			int index = columnFlow.getChildren().indexOf(firstGridPane);
			layoutData.setValueIndex(index + 1);
			layoutData.setRow(false);
		}
		
		firstGridPane = (GridPane) AdhocUtils.getFirstObject(rowFlow.getChildren(), GridPane.class);
		if (null != firstGridPane) {
			int index = rowFlow.getChildren().indexOf(firstGridPane);
			layoutData.setValueIndex(index + 1);
			layoutData.setRow(true);
		}
		
		// revert
		LayoutStoreFactory.convertToModel(thisModelType, columnFlow.getChildren(), rowFlow.getChildren());

		return layoutData;
	}
	
}
