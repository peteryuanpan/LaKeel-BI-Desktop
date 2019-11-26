package com.legendapl.lightning.adhoc.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableViewData;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTRowTrieTree;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTRowTrieTreeNode;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTColumnTrieTree;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.TotalNode;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.GroupType;
import com.legendapl.lightning.adhoc.custom.CTTopLeftColumn;
import com.legendapl.lightning.adhoc.custom.CTTopLeftLeafColumn;
import com.legendapl.lightning.adhoc.custom.CTCustomColumn;
import com.legendapl.lightning.adhoc.custom.Expandable;
import com.legendapl.lightning.adhoc.model.Field;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;
import net.jonathangiles.hacking.tableview.cellSpan.CellSpanTableView;

/**
 *　クラス集計のデータを作成する
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CrossTableViewFactory extends AdhocBaseFactory {

	private CellSpanTableView<CrossTableViewData> tableView;

	private int valueIndex = 0;

	private String sqlStatement = "";

	private List<CrossTableViewData> tempData = new ArrayList<CrossTableViewData>();

	private static final int dataStep = 30;

	private static final int initTableViewNum = 75;

	private CTTopLeftColumn<CrossTableViewData, String> topLeftColumn;
	
	private List<TableColumn<CrossTableViewData, ?>> topLeftLeafColumns;

	private CTRowTrieTree rowTrieTree;
	
	private CTColumnTrieTree columnTrieTree;

	private boolean isRow = false;

	private List<Expandable> rowExpands = new ArrayList<>();
	private List<Expandable> columnExpands = new ArrayList<>();
	private final ChangeListener<Number> crossTableListener;

	private int rowSize;

	private boolean fullData;

	private ScrollBar verticalBar;
	private ScrollBar horizontalBar;

	private boolean inCrossTable = false;

	public CrossTableViewFactory(CellSpanTableView<CrossTableViewData> tableView) {
		super();
		this.tableView = tableView;
		crossTableListener = (ob, old, newValue) -> {
			if (inCrossTable && fullData && 1 == newValue.intValue()) {
				Platform.runLater(() -> {
					int temp = tableView.getItems().size();
					fecthMoreData();
					verticalBar.setValue(temp * 1.0 / tableView.getItems().size());
				});
			}
		};
	}

	public void fecthMoreData() {
		fecthMoreData(dataStep);
	}

	private void fecthMoreData(int step) {
		int realStep = Math.min(step, tempData.size() - tableView.getItems().size());
		if(realStep == 0) {
			return;
		}
		for (int i = 0; i < realStep; i++) {
			CrossTableViewData data = tempData.get(tableView.getItems().size());
			tableView.getItems().add(data);
		}
		tableView.applyCss();
		tableView.refresh();
	}

	/**
	 * クロス集計を表示するメイン函数
	 * @param event
	 */
	public boolean generateDataView(ActionEvent event) {
		// 1. judge
		if (!doJudgeAllFieldsEmpty()) return false;
		// 2. clear
		clearData();
		// 3. generateDataView
		generateDataViewImpl();
		// 4. other
		generateScrollBar();
		sqlStatement = rowTrieTree.getSqlStatement();
		return true;
	}
	
	private boolean doJudgeAllFieldsEmpty() {
		if (crossTableColumns.isEmpty() && crossTableRows.isEmpty() && crossTableValues.isEmpty()) {
			sqlStatement = "";
			Platform.runLater(() -> {
				tableView.setVisible(false);
			});
			return false;
		}
		return true;
	}
	
	private void clearData() {
		inCrossTable = true;
		Platform.runLater(() -> {
			if (horizontalBar != null) { // ScorllBar refresh
				horizontalBar.setValue(0);
			}
			tableView.getStylesheets().setAll("/view/crossTable.css");
			tableView.getColumns().clear();
			tableView.getItems().clear();
			tableView.setSpanModel(null);
			tableView.setVisible(true);
		});
		tempData.clear();
	}
	
	private void generateDataViewImpl() {
		
		generateColumnRowTrieTree();
		
		Platform.runLater(() -> {
			generateTopLeftColumn();
			generateTopLeftLeafColumn(!columnEmpty());
			if (columnEmpty()) generateOneEmptyColumn();
			insertTableViewItem();
		});
	}
	
	private void generateColumnRowTrieTree() {
		columnTrieTree = new CTColumnTrieTree(tableView);
		rowTrieTree = databaseService.initCrossTableData(crossTableColumns, crossTableRows, crossTableValues, columnTrieTree);
		addMeasureToTrieTree();
		columnTrieTree.transferToTableView(rowEmpty());
	}
	
	private void generateOneEmptyColumn() {
		CTCustomColumn<CrossTableViewData, String> emptyColumn = new CTCustomColumn<>();
		emptyColumn.getStyleClass().setAll("blue-background");
		tableView.getColumns().add(emptyColumn);
		columnTrieTree.initColumnCellFactory(emptyColumn);
	}

	/**
	 * generate top-left column:<br>
	 * +A<br>
	 * +B<br>
	 * メジャー<br>
	 */
	private void generateTopLeftColumn() {
		
		if (columnEmpty()) {
			topLeftColumn = null;
			return;
		}
		
		List<String> labels = new ArrayList<>();
		for (Field field : crossTableColumns) {
			labels.add(field.getLabel());
		}
		if (!isRow && valueIndex != 0) {
			labels.add(valueIndex - 1, AdhocUtils.getString("P121.crossTable.measure"));
		}
		
		generateTopLeftColumnImpl(labels);
	}
	
	private void generateTopLeftColumnImpl(List<String> labels) {
		if (!labels.isEmpty()) {
			columnExpands.clear();
			topLeftColumn = new CTTopLeftColumn<>(labels.get(0));
			columnIcon(topLeftColumn, 0);
			tableView.getColumns().add(0, topLeftColumn);
			for (int i = 1; i < labels.size(); i++) {
				CTTopLeftColumn<CrossTableViewData, String> column = new CTTopLeftColumn<>(labels.get(i));
				topLeftColumn.getColumns().setAll(Arrays.asList(column));
				topLeftColumn = column;
				columnIcon(topLeftColumn, i);
			}
		}
	}
	
	private void addMeasureToTrieTree() {
		if (isRow && valueIndex != 0) {
			rowTrieTree.addMeasure(valueIndex, crossTableValues);
		}
		if (!isRow && valueIndex != 0) {
			columnTrieTree.addMeasure(valueIndex, crossTableValues);
		}
	}

	private void generateTopLeftLeafColumn(boolean hasColumn) {
		topLeftLeafColumns = hasColumn ? topLeftColumn.getColumns() : tableView.getColumns();
		for (CrossTableField field : crossTableRows) {
			CTTopLeftLeafColumn<CrossTableViewData, String> column = new CTTopLeftLeafColumn<>(field.getLabel());
			column.setField(field);
			topLeftLeafColumns.add(column);
		}
		rowSize = crossTableRows.size();
		if (isRow && valueIndex != 0) {
			rowSize++;
			addMeasureToTopLeftLeafColumn(valueIndex);
		}
		generateTopLeftLeafColumnImpl();
	}

	private void addMeasureToTopLeftLeafColumn(int layer) {
		CTTopLeftLeafColumn<CrossTableViewData, String> rowColumn = new CTTopLeftLeafColumn<>(AdhocUtils.getString("P121.crossTable.measure"));
		Font font = new Font("Meiryo Bold", 14);
		double maxWidth = 20 + AdhocUtils.getLabelWidth(font, AdhocUtils.getString("P121.crossTable.measure"));
		for (int i = 0; i < crossTableValues.size(); i++) {
			Field field = crossTableValues.get(i);
			maxWidth = Math.max(maxWidth, AdhocUtils.getLabelWidth(font, field.getLabel()) + 20);
		}
		rowColumn.setIntWidth(maxWidth);
		topLeftLeafColumns.add(layer - 1, rowColumn);
	}

	private void generateTopLeftLeafColumnImpl() {
		if (rowEmpty()) {
			topLeftColumn.setCellFactory(cellData -> new TopLeftLeafColumnCellSpecial());
		} else {
			rowExpands.clear();
			for (int i = 0; i < topLeftLeafColumns.size(); i++) {
				final int tempI = i;
				CTTopLeftLeafColumn<CrossTableViewData, String> topLeftLeafColumn = (CTTopLeftLeafColumn) topLeftLeafColumns.get(i);
				rowIcon(topLeftLeafColumn, i);
				Field field = topLeftLeafColumn.getField();
				if (field != null && field.getCalculatedExpression() == null) {
					double width = databaseService.getMaxBitWidth(field);
					Font font = new Font("Meiryo Bold", 14);
					double widthTitle = AdhocUtils.getLabelWidth(font, field.getLabel());
					topLeftLeafColumn.setIntWidth(Math.max(width + 20, widthTitle + 20));
				}
				topLeftLeafColumn.getStyleClass().setAll("row-column");
				topLeftLeafColumn.setCellFactory(cellData -> new TopLeftLeafColumnCell(tempI, topLeftLeafColumn));
			}
		}
	}
	
	/**
	 * Top Left Leaf Column's Cell Factory
	 */
	public class TopLeftLeafColumnCellSpecial extends TableCell<CrossTableViewData, String> {
		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);
			getStyleClass().setAll("row-group");
			if (getIndex() == 0) {
				setText(AdhocUtils.getString("P121.crossTable.rowGroup"));
			} else {
				setText("");
			}
		}
	}
	
	/**
	 * Top Left Leaf Column's Cell Factory
	 */
	public class TopLeftLeafColumnCell extends TableCell<CrossTableViewData, String> {
		
		private Integer i;
		private CTTopLeftLeafColumn<CrossTableViewData, String> column;
		
		public TopLeftLeafColumnCell(Integer i, CTTopLeftLeafColumn column) {
			this.i = i;
			this.column = column;
		}
		
		CrossTableViewData previousRow;
		CrossTableViewData currentRow;
		CTRowTrieTreeNode previousNode;
		CTRowTrieTreeNode currentNode;
		
		void initRowData() {
			Integer index = getIndex();
			previousRow = getRow(index - 1);
			currentRow = getRow(index);
			previousNode = getRowNode(previousRow);
			currentNode = getRowNode(currentRow);
		}
		
		@Override
		public void updateItem(String item, boolean empty) {
			
			super.updateItem(item, empty);
			
			initRowData();
			
			if (currentRow != null && currentNode != null) {
				
				updateStyle();
				
				if (inEmptyRow()) updateEmpty(); 
				else updateNormal();
			}
		}
		
		CrossTableViewData getRow(Integer index) {
			if (index >= 0) {
				if (index < tableView.getItems().size()) return tableView.getItems().get(index);
				if (index < tempData.size()) return tempData.get(index); // DO NOT REMOVE
			}
			return null;
		}
		
		CTRowTrieTreeNode getRowNode(CrossTableViewData row) {
			return row != null ? row.getNode(i, rowTrieTree) : null;
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
			
			if (containStyleTopBorder()) {
				getStyleClass().add("top-border");
			} else {
				getStyleClass().removeAll("top-border");
			}
		}
		
		public boolean containStyleTableCellEven() {
			return i == rowSize - 1 && getIndex() % 2 == 1;
		}
		
		public boolean containStyleTableCellOdd() {
			return i == rowSize - 1 && getIndex() % 2 == 0;
		}
		
		public boolean containStyleBoldFont() {
			return currentRow.isTotal();
		}
		
		public boolean containStyleTopBorder() {
			return !inEmptyRow();
		}
		
		boolean inEmptyRow() {
			return previousRow != null && previousNode == currentNode;
		}
		
		void updateEmpty() {
			setText("");
			setGraphic(null);
		}
		
		void updateNormal() {
			
			CrossTableField field = column.getField();
			GroupType gt = field == null ? null : field.getGroupType();
			Object obj = currentRow.getFields().get(i);
			
			setText(gt == null ? AdhocUtils.toString(obj) : gt.parse(obj));
			
			if (currentNode.hasIcon()) updateGraphic();
		}
		
		void updateGraphic() {
			
			CrossTableField measure = null;
			if (rowTrieTree.getMeasureLayer() != -1 && rowTrieTree.getMeasureLayer() <= i + 1) {
				measure = currentRow.getMeasure();
			}

			updateGrpahic(measure);
		}
		
		void updateGrpahic(CrossTableField measure) {
			
			final FontAwesomeIconView view;
			
			if (currentNode.isExpand(measure)) {
				view = new FontAwesomeIconView(AdhocConstants.CrossTable.EXPANDEX_ICON);
				view.setOnMouseClicked(event -> {
					collapse(getIndex(), i+1, currentNode);
					currentNode.setExpand(measure, false);
				});
				
			} else {
				view = new FontAwesomeIconView(AdhocConstants.CrossTable.COLLAPSED_ICON);
				view.setOnMouseClicked(event -> {
					currentNode.setExpand(measure, true);
					expand(getIndex(), currentRow.getSearchList(i + 1), currentNode, measure);
				});
			}
			
			Platform.runLater(() -> setGraphic(view)); // DO NOT REMOVE Platform.runLater
		}
	}

	private void rowIcon(Expandable expand, final int index) {
		rowExpands.add(expand);
		if (index == topLeftLeafColumns.size() - 1 || index == rowTrieTree.getMeasureLayer() - 2) {
			expand.disExpand();
			return;
		}
		expand.initExpand();
		expand.register(() -> backW.run(() -> expandRows(index)), () -> backW.run(() -> collapseRows(index)));
	}

	private void columnIcon(Expandable expand, final int index) {
		columnExpands.add(expand);
		if (index == columnTrieTree.getDepth() - 1 || index == columnTrieTree.getMeasureLayer() - 2) {
			expand.disExpand();
			return;
		}
		expand.initExpand();
		expand.register(() -> backW.run(() -> expandColumns(index)), () -> backW.run(() -> collapseColumns(index)));
	}

	private void collapseRows(final int index) {
		for (int i = index; i < rowExpands.size(); i++) {
			Expandable column = rowExpands.get(i);
			Platform.runLater(() -> {
				column.changeExpandStatus(false);
			});
		}
		tempData.clear();
		tempData.addAll(rowTrieTree.transferToModels(index + 1, false));
		Platform.runLater(() -> {
			tableView.getItems().clear();
			tableView.getItems().setAll(tempData.subList(0, Math.min(initTableViewNum, tempData.size())));
			tableView.refresh();
		});
	}

	private void expandRows(final int index) {
		for (int i = 0; i <= index; i++) {
			Expandable column = rowExpands.get(i);
			Platform.runLater(() -> {
				column.changeExpandStatus(true);
			});
		}
		tempData.clear();
		tempData.addAll(rowTrieTree.transferToModels(index + 1, true));
		Platform.runLater(() -> {
			tableView.getItems().clear();
			tableView.getItems().setAll(tempData.subList(0, Math.min(initTableViewNum, tempData.size())));
			tableView.refresh();
		});
	}

	private void collapseColumns(int index) {
		for (int i = index; i < columnExpands.size(); i++) {
			Expandable column = columnExpands.get(i);
			Platform.runLater(() -> {
				column.changeExpandStatus(false);
			});
		}
		columnTrieTree.transferToTableView(rowEmpty(), index + 1, false);
		Platform.runLater(() -> {
			AdhocUtils.autoWidth(tableView);
			tableView.refresh();
		});
	}

	private void expandColumns(int index) {
		for (int i = 0; i <= index; i++) {
			Expandable column = columnExpands.get(i);
			Platform.runLater(() -> {
				column.changeExpandStatus(true);
			});
		}
		columnTrieTree.transferToTableView(rowEmpty(), index + 1, true);
		Platform.runLater(() -> {
			AdhocUtils.autoWidth(tableView);
			tableView.refresh();
		});
	}

	protected void expand(int rowNum, List<Object> treeSearchList, CTRowTrieTreeNode node, CrossTableField measure) {
		Platform.runLater(() -> {
			List<CrossTableViewData> datas = rowTrieTree.getModels(node, treeSearchList.subList(0, treeSearchList.size() - 1), measure);
			List<Object> fields = tempData.get(rowNum).getFields();
			for(int i=0;i<treeSearchList.size() - 1;i++) {
				datas.get(0).getFields().set(i, fields.get(i));
			}
			tempData.remove(rowNum);
			tableView.getItems().remove(rowNum);
			fecthMoreData(1);
			try {
				CrossTableViewData data = tableView.getItems().get(rowNum);
				while (data.getNode(treeSearchList.size()-1, rowTrieTree) == node) {
					tempData.remove(rowNum);
					tableView.getItems().remove(rowNum);
					fecthMoreData(1);
					data = tableView.getItems().get(rowNum);
				}
			} catch(IndexOutOfBoundsException e) {
				//データがない
			}
			tempData.addAll(rowNum, datas);
			tableView.getItems().addAll(rowNum, datas);
			tableView.refresh();
		});
	}

	protected void collapse(int rowNum, int columnNum, CTRowTrieTreeNode currentNode) {
		Platform.runLater(() -> {
			CrossTableViewData data = tableView.getItems().get(rowNum);
			List<Object> subFields = data.getFields().subList(0, columnNum);
			while (data.getNode(columnNum - 1, rowTrieTree) == currentNode) {
				if (data.getFields().get(columnNum) instanceof TotalNode) {
					List<Object> fields = data.getFields();
					for (int i = 0; i < columnNum; i++) {
						fields.set(i, subFields.get(i));
					}
					break;
				}
				tableView.getItems().remove(rowNum);
				tempData.remove(rowNum);
				fecthMoreData(1);
				data = tableView.getItems().get(rowNum);
			}
			tableView.refresh();
		});
	}
	
	private void insertTableViewItem() {
		tempData = rowTrieTree.transferToModels();
		tableView.getItems().clear();
		for (int i = 0; i < Math.min(initTableViewNum, tempData.size()); i++) {
			tableView.getItems().add(tempData.get(i));
		}
		if (tempData.isEmpty()) {
			CrossTableViewData emptyData = new CrossTableViewData();
			tableView.getItems().add(emptyData);
			tempData.add(emptyData);
		}
		AdhocUtils.autoWidth(tableView);
		tableView.refresh();
	}

	private void generateScrollBar() {
		Platform.runLater(() -> {
			if (verticalBar == null) {
				verticalBar = findScrollBar(tableView, Orientation.VERTICAL);
			}
			if (horizontalBar == null) {
				horizontalBar = findScrollBar(tableView, Orientation.HORIZONTAL);
			}
			if (verticalBar != null) {
				verticalBar.valueProperty().removeListener(crossTableListener);
				verticalBar.valueProperty().addListener(crossTableListener);
			}
		});
	}

	private ScrollBar findScrollBar(TableView<?> table, Orientation orientation) {
		Set<javafx.scene.Node> set = table.lookupAll(".scroll-bar");
		for (javafx.scene.Node node : set) {
			ScrollBar verticalBar = (ScrollBar) node;
			if (verticalBar.getOrientation() == orientation) {
				return verticalBar;
			}
		}
		return null;
	}

	public boolean isFullData() {
		return fullData;
	}

	public void setFullData(boolean fullData) {
		this.fullData = fullData;
	}

	private boolean columnEmpty() {
		return isRow ? crossTableColumns.isEmpty() : crossTableColumns.isEmpty() && crossTableValues.isEmpty();
	}

	private boolean rowEmpty() {
		return isRow ? crossTableRows.isEmpty() && crossTableValues.isEmpty() : crossTableRows.isEmpty();
	}

	/**
	 * begin with 1
	 */
	public int getValueIndex() {
		return valueIndex;
	}

	/**
	 * begin with 1
	 *
	 * @param valueIndex
	 */
	public void setValueIndex(int valueIndex) {
		this.valueIndex = valueIndex;
	}

	public String getSqlStatement() {
		return sqlStatement;
	}

	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	public boolean isRow() {
		return isRow;
	}

	public void setRow(boolean isRow) {
		this.isRow = isRow;
	}

	public void setInCrossTable(boolean inCrossTable) {
		this.inCrossTable = inCrossTable;
		Platform.runLater(() -> {
			if(horizontalBar != null)
				horizontalBar.setValue(0);
		});
		tempData.clear();
	}
	
	public List<CrossTableViewData> getTempData() {
		return tempData;
	}
	
}
