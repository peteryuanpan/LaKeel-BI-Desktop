package com.legendapl.lightning.adhoc.factory;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.custom.LayoutFlowPane;
import com.legendapl.lightning.adhoc.custom.LayoutLabel;
import com.legendapl.lightning.adhoc.factory.itemTree.CrossTableItemTreeFactory;
import com.legendapl.lightning.adhoc.service.AdhocLogService;

import javafx.scene.Node;
import javafx.scene.layout.GridPane;

/**
 * グループに切り替えボタンを押す時、ライオウとバンドのデータを処理するサービス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.01
 */
public class LayoutSwitchFactory extends AdhocBaseFactory {
	
	public static void switchCrossTableLayout() {
		List<Node> oldColumnFlowChildren = AdhocUtils.createNewListRemoveNull(columnFlow.getChildren());
		List<Node> oldRowFlowChildren = AdhocUtils.createNewListRemoveNull(rowFlow.getChildren());
		List<Node> newColumnFlowChildren = new ArrayList<>();
		List<Node> newRowFlowChildren = new ArrayList<>();
		insertThisFlowChildrenByOppsite(columnFlow, newColumnFlowChildren, oldRowFlowChildren);
		insertThisFlowChildrenByOppsite(rowFlow, newRowFlowChildren, oldColumnFlowChildren);
		StatementFactory.runLater(
				() -> switchCrossTableLayoutImpl(newColumnFlowChildren, newRowFlowChildren),
				() ->switchCrossTableLayoutImpl(oldColumnFlowChildren, oldRowFlowChildren)
		);
	}
	
	private static void switchCrossTableLayoutImpl(List<Node> columnFlowChildren, List<Node> rowFlowChildren) {
		AdhocLogService.switchCrossTableLayout(columnFlow, rowFlow);
		columnFlow.getChildren().clear();
		rowFlow.getChildren().clear();
		columnFlow.getChildren().addAll(columnFlowChildren);
		rowFlow.getChildren().addAll(rowFlowChildren);
		CrossTableItemTreeFactory.refreshLayout();
	}
	
	private static void insertThisFlowChildrenByOppsite(LayoutFlowPane thisFlow, List<Node> thisFlowChildren, List<Node> oppFlowChildren) {
		oppFlowChildren.forEach(child -> {
			if (child instanceof LayoutLabel) {
				LayoutLabel label = (LayoutLabel) child;
				switch (label.getModelType()) {
				case FIELD:
					CrossTableField field = (CrossTableField) label.getField();
					thisFlowChildren.add(crossTableFieldTreeFactory.getLayoutLabel(thisFlow, field));
					break;
				default:
					break;
				}
				return;
			}
			if (child instanceof GridPane) {
				GridPane gridPane = (GridPane) child;
				LayoutLabel label = (LayoutLabel) gridPane.getChildren().get(0);
				switch (label.getModelType()) {
				case MEASURE:
					thisFlowChildren.add(crossTableValueTreeFactory.getLayoutLabelParent(thisFlow, label));
					break;
				case LAYOUT_POUND:
					thisFlowChildren.add(crossTableValueTreeFactory.getLayoutPoundParent(thisFlow));
					break;
				default:
					break;
				}
				return;
			}
		});
	}
	
}
