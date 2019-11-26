package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.ColumnSearchTrack;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableViewData;
import com.legendapl.lightning.adhoc.custom.CTCustomColumn;
import com.legendapl.lightning.adhoc.factory.AdhocBaseFactory;
import com.legendapl.lightning.adhoc.service.ThreadPoolService;

import javafx.application.Platform;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * クロス集計の列のデータを格納する辞書ツリー
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CTColumnTrieTree extends TrieTree<CTColumnTrieTreeNode> {

	private boolean rowEmpty;

	private List<TableColumn<CrossTableViewData, ?>> lastLayer = new ArrayList<>();

	private TableView<CrossTableViewData> tableView;

	public CTColumnTrieTree(TableView<CrossTableViewData> tableView) {
		this.tableView = tableView;
	}

	@Override
	protected CTColumnTrieTreeNode getInstance() {
		return new CTColumnTrieTreeNode();
	}

	@Override
	protected CTColumnTrieTreeNode getInstance(Comparable name) {
		return new CTColumnTrieTreeNode(name);
	}

	/**
     * 辞書ツリーをたどって、CrossTableCustomColumnを生成する
     */
	public void transferToTableView(boolean rowEmpty) {
		transferToTableView(rowEmpty, -1, false);
	}

	/**
     * 辞書ツリーのレイヤーを拡大または縮小してから、辞書ツリーをたどって、CrossTableCustomColumnを生成する
     */
	public void transferToTableView(boolean rowEmpty, int expandLayer, boolean expand) {
		this.rowEmpty = rowEmpty;
		lastLayer.clear();
		ColumnFilledSignal<TableColumn<CrossTableViewData, ?>> signal = new ColumnFilledSignal<>(root.childs.size());
		for (int i = 0; i < root.childs.size(); i++) {
			final int tempI = i;
			ThreadPoolService.getInstance().execute(new Processor<TableColumn<CrossTableViewData, ?>>(
				() -> transferToTableViewAfterNotify(root.childs.get(tempI), new ArrayList<Object>(), null, false, expandLayer, expand)
				, signal, tempI));
		}
		List<TableColumn<CrossTableViewData, ?>> columns = signal.check();
		Platform.runLater(() -> tableView.getColumns().addAll(columns));
	}

	/**
     * 指定された辞書ツリーノードをたどって、CrossTableViewDataを生成する
     */
	public TableColumn<CrossTableViewData, ?> transferToTableView(TrieTreeNode root, List<Object> list, CrossTableField measure, boolean isTotal) {
		List<Object> currentFields = new ArrayList<>(list);
		if (root.getLayer() == measureLayer) {
			measure = root.getMeasure();
		} else {
			currentFields.add(root.getFieldValue());
		}
		isTotal = isTotal || root.isTotal;
		CTCustomColumn<CrossTableViewData, String> column = new CTCustomColumn<>(root.fieldValue);
		Platform.runLater(() -> column.getStyleClass().setAll("blue-background"));
		column.setRoot((CTColumnTrieTreeNode) root);
		column.setExpanded(root.isExpand(measure));
		column.generateColumnIcon();
		ColumnSearchTrack track = new ColumnSearchTrack();
		track.setFieldNames(currentFields);
		track.setMeasure(measure);
		column.setTrack(track);
		column.setTotal(isTotal);
		if (root.isLeaf) {
			if (!rowEmpty) {
				CTCustomColumn<CrossTableViewData, String> columnEmpty = new CTCustomColumn<>(root.fieldValue);
				columnEmpty.setTrack(track);
				columnEmpty.setTotal(isTotal);
				Platform.runLater(() -> columnEmpty.getStyleClass().setAll("gray-background"));
				column.getColumns().add(columnEmpty);
				lastLayer.add(columnEmpty);
				Platform.runLater(() -> {
					initColumnCellFactory(columnEmpty);
				});
			} else {
				lastLayer.add(column);
				Platform.runLater(() -> {
					initColumnCellFactory(column);
				});
			}
		} else if (root.isExpand(measure) || root.getTotalNode() == null) {
			List<TableColumn<CrossTableViewData, ?>> childs = new ArrayList<>();
			for (TrieTreeNode child : root.getChilds()) {
				childs.add(transferToTableView(child, currentFields, measure, isTotal));
			}
			column.getColumns().addAll(childs);
			if (column.getChilds().isEmpty()) {
				column.setChilds(childs);
				column.setTotalChild(childs.get(childs.size() - 1));
			}
		} else {
			TableColumn<CrossTableViewData, ?> totalChild = transferToTableView(root.getTotalNode(), currentFields, measure, isTotal);
			column.getColumns().add(totalChild);
			column.setTotalChild(totalChild);
		}
		return column;
	}

	public List<TableColumn<CrossTableViewData, ?>> getLastLayer() {
		return lastLayer;
	}

	public void initColumnCellFactory(CTCustomColumn<CrossTableViewData, String> column) {
		column.setStyle("-fx-alignment: CENTER_RIGHT;");
		column.setCellFactory(cellData -> new CrossTableViewColumnCell(column));
	}
	
	/**
	 * CrossTable View Column's Cell Factory
	 */
	public class CrossTableViewColumnCell extends TableCell<CrossTableViewData, String> {
		
		private CTCustomColumn<CrossTableViewData, String> column;
		public CrossTableViewColumnCell(CTCustomColumn column) {
			this.column = column;
		}
		
		CrossTableViewData row;
		
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			
			row = getRow(getIndex());
			if (null != row) {
				
				updateStyle();
				
				setText(row.getColumnCellValue(column.getTrack()));
			}
		}
		
		CrossTableViewData getRow(Integer index) {
			if (index >= 0) {
				if (index < tableView.getItems().size()) return tableView.getItems().get(index);
				List<CrossTableViewData> tempData = AdhocBaseFactory.crossTableViewFactory.getTempData();
				if (index < tempData.size()) return tempData.get(index); // DO NOT REMOVE
			}
			return null;
		}
		
		void updateStyle() {
			
			if (containStyleTableCellEven()) {
				getStyleClass().setAll("table-cell-even");
			} else if (containStyleTableCellOdd()) {
				getStyleClass().setAll("table-cell-odd");
			}
			
			if (containStyleBoldFont()) {
				getStyleClass().add("bold-font");
			} else {
				getStyleClass().removeAll("bold-font");
			}
		}
		
		public boolean containStyleTableCellEven() {
			return getIndex() % 2 == 1;
		}
		
		public boolean containStyleTableCellOdd() {
			return getIndex() % 2 == 0;
		}
		
		public boolean containStyleBoldFont() {
			return column.isTotal() || row.isTotal();
		}
	}

	public TableColumn<CrossTableViewData, ?> transferToTableViewAfterNotify(TrieTreeNode root, List<Object> fields,
			CrossTableField measure, boolean isTotal, int expandLayer, boolean expand) {
		if(expandLayer != -1) {
			Platform.runLater(() -> {
				tableView.getColumns().remove(1, tableView.getColumns().size());
			});
			notify(root, null, expandLayer, expand);
		}
		return transferToTableView(root, fields, measure, isTotal);
	}

}
