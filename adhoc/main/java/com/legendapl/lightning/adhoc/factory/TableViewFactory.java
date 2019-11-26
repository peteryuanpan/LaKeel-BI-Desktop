package com.legendapl.lightning.adhoc.factory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.legendapl.lightning.adhoc.adhocView.model.TableField;
import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.custom.TableCustomColumn;
import com.legendapl.lightning.adhoc.dao.util.DataFormatUtils;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;
import net.jonathangiles.hacking.tableview.cellSpan.CellSpan;
import net.jonathangiles.hacking.tableview.cellSpan.CellSpanTableView;
import net.jonathangiles.hacking.tableview.cellSpan.SpanModel;

/**
 *　テーブルのデータを作成する
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class TableViewFactory extends AdhocBaseFactory {

	private CellSpanTableView<TableViewData> tableView;

	private String sqlStatement = "";

	private ObservableList<TableViewData> tempData = FXCollections.observableArrayList();

	private int currentIndex = 0;

	protected double maxColumnWidthSize = 0;

	private boolean fullData = false;

	private static final int dataStep = 20;

	public static final int sampleData = 50;

	private static final int initTableViewNum = 50;

	private ScrollBar verticalBar;

	private ScrollBar horizontalBar;

	private final ChangeListener<Number> tableListener;

	private boolean inTable = false;

	public TableViewFactory(CellSpanTableView tableView) {
		super();
		this.tableView = tableView;
		tableListener = (ob, old, newValue) -> {
			if (inTable && fullData && 1 == newValue.intValue()) {
				Platform.runLater(() -> {
					int temp = tableView.getItems().size();
					fecthMoreData();
					verticalBar.setValue(temp * 1.0 / tableView.getItems().size());
				});
			}
		};
	}

	public void fecthMoreData() {
		if (!fullData) return;
		for (int i = 0; i < Math.min(dataStep, tempData.size() - currentIndex); i++, currentIndex++) {
			TableViewData data = tempData.get(currentIndex);
			tableView.getItems().add(data);
			if (data.isGroupContent()) {
				Font font = new Font("Meiryo Bold", 14);
				double width = AdhocUtils.getLabelWidth(font, DataFormatUtils.parseGroupString(data.getGroupKey(), tableRows));
				if (width > maxColumnWidthSize) {
					maxColumnWidthSize = width;
					int columnSize = tableView.getColumns().size();
					for (TableColumn<?, ?> column : tableView.getColumns()) {
						double preWidth = Math.ceil(Math.max(column.getWidth(), maxColumnWidthSize / columnSize));
						column.setPrefWidth(preWidth);
					}
				}
			}
		}
	}

	/**
	 * テーブルを表示するメイン函数
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
		return true;
	}
	
	private boolean doJudgeAllFieldsEmpty() {
		if (tableColumns.isEmpty() && tableRows.isEmpty()) {
			sqlStatement = "";
			Platform.runLater(() -> {
				tableView.setVisible(false);
			});
			return false;
		}
		return true;
	}
	
	private void clearData() {
		inTable = true;
		Platform.runLater(() -> {
			if (horizontalBar != null) horizontalBar.setValue(0);
			tableView.getStylesheets().setAll("/view/tableView.css");
			tableView.getColumns().clear();
			tableView.getItems().clear();
			tableView.setVisible(true);
		});
		currentIndex = 0;
		maxColumnWidthSize = 0;
		tempData.clear();
	}

	private void generateDataViewImpl() {
		
		Platform.runLater(() -> generateTableColumns());
		
		insertTempData();
		
		Platform.runLater(() -> insertTableViewItem());
	}

	/**
	 *　列のコントロールのcell Factoryを作成して、cellの結合方法を決める。
	 */
	private void generateTableColumns() {
		if (tableColumns.isEmpty()) {
			TableField emptyField = new TableField();
			fieldColumn(emptyField);
		} else {
			for (TableField column : tableColumns) {
				fieldColumn(column);
			}
		}

		//cellの結合方法を決める。
		tableView.setSpanModel(new SpanModel() {
			private final CellSpan spanTwoColumns = new CellSpan(1, tableView.getColumns().size());
			@Override
			public CellSpan getCellSpanAt(int rowIndex, int columnIndex) {
				if (columnIndex == 0 && tableView.getItems().get(rowIndex).isGroupContent()) {
					return spanTwoColumns;
				} else {
					return null;
				}
			}
			@Override
			public boolean isCellSpanEnabled() {
				return true;
			}
		});
	}

	public void fieldColumn(TableField field) {
		TableCustomColumn<TableViewData, String> column = new TableCustomColumn<>();
		column.setField(field);
		column.setText(getColumnText(field));
		column.setGraphic(getColumnGraphic(field));
		column.setPrefWidth(getColumnPrefWidth(field));
		List<TableField> groups = new ArrayList<>(tableRows); // DO NOT MOVE
		column.setCellFactory(cellData -> new TableViewColumnCell(column, groups));
		tableView.getColumns().add(column);
	}
	
	/**
	 * Table View Column's Cell Factory
	 */
	public class TableViewColumnCell extends TableCell<TableViewData, String> {
		
		private TableCustomColumn<TableViewData, String> column;
		private List<TableField> groups;
		
		public TableViewColumnCell(TableCustomColumn<TableViewData, String> column, List<TableField> groups) {
			this.column = column;
			this.groups = groups;
		}
		
		TableViewData row;
		
		@Override
		public void updateItem(String item, boolean empty) {
			
			super.updateItem(item, empty);
			
			row = getRow(getIndex());
			if (null != row) {
				
				if (row.isGroupContent()) updateGroup();
				else if(row.isLastRow()) updateLast();
				else updateNormal();
			}
		}
		
		TableViewData getRow(Integer index) {
			if (index >= 0) {
				if (index < tableView.getItems().size()) return tableView.getItems().get(index);
				if (index < tempData.size()) return tempData.get(index); // DO NOT REMOVE
			}
			return null;
		}
		
		void updateGroup() {
			getStyleClass().setAll("group-row");
			setText(DataFormatUtils.parseGroupString(row.getGroupKey(), groups));
		}
		
		void updateLast() {
			getStyleClass().setAll("group-row");
			Object result = row.getValueByField(column.getField());
			setText(result == null ? "" : result.toString());
		}
		
		void updateNormal() {
			getStyleClass().setAll(row.isOddStyle() ? "table-cell-odd" : "table-cell-even");
			TableField field = column.getField();
			Object result = row.getValueByField(field);
			setText(result == null ? "" : field.getDataFormat().parse(result));
		}
	}
	
	private String getColumnText(TableField field) {
		if (field.isInterval()) {
			return null;
		}
		return field.getLabel();
	}
	
	private Node getColumnGraphic(TableField field) {
		if (field.isInterval()) {
			MaterialDesignIconView graphic = new MaterialDesignIconView(MaterialDesignIcon.DETAILS);
			graphic.setGlyphSize(10);
			return graphic;
		}
		return null;
	}
	
	private Double getColumnPrefWidth(TableField field) {
		if (field.isInterval()) {
			return 25.0;
		}
		if (field.getCalculatedExpression() != null) {
			// TODO: calculation
			return -1.0;
		}
		// normal
		double maxBitWidth = databaseService.getMaxBitWidth(field);
		Font font = new Font("Meiryo Bold", 14);
		double widthTitle = AdhocUtils.getLabelWidth(font, field.getLabel());
		return Math.ceil(Math.max(maxBitWidth + 20, widthTitle + 20));
	}
	
	private void insertTempData() {
		List<TableField> dbColumns = getTableFieldsRemoveAllInterval(tableColumns);
		List<TableField> dbGroups = getTableFieldsRemoveAllInterval(tableRows);
		if (!dbColumns.isEmpty() || !dbGroups.isEmpty()) {
			sqlStatement = databaseService.generateTableSQL(dbColumns, dbGroups);
			databaseService.fillTableData(dbColumns, tempData, fullData);
		}
	}

	private List<TableField> getTableFieldsRemoveAllInterval(List<TableField> fields) {
		List<TableField> newFields = new ArrayList<>();
		for (TableField field : fields) {
			if (!field.isInterval()) {
				newFields.add(field);
			}
		}
		return newFields;
	}

	private void insertTableViewItem() {
		int dataNum = fullData ? initTableViewNum : tempData.size();
		for (int i = 0; i < Math.min(dataNum, tempData.size()); i++, currentIndex++) {
			TableViewData data = tempData.get(currentIndex);
			tableView.getItems().add(data);
			if (data.isGroupContent()) {
				Font font = new Font("Meiryo Bold", 14);
				double width = AdhocUtils.getLabelWidth(font, DataFormatUtils.parseGroupString(data.getGroupKey(), tableRows));
				if (width > maxColumnWidthSize) {
					maxColumnWidthSize = width;
					int columnSize = tableView.getColumns().size();
					for (TableColumn<?, ?> column : tableView.getColumns()) {
						column.setPrefWidth(Math.ceil(Math.max(column.getWidth(), maxColumnWidthSize / columnSize)));
					}
				}
			}
		}
		if (tempData.isEmpty()) {
			TableViewData emptyData = new TableViewData();
			tableView.getItems().add(emptyData);
			tempData.add(emptyData);
		}
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
				verticalBar.valueProperty().removeListener(tableListener);
				verticalBar.valueProperty().addListener(tableListener);
			}
		});
	}

	private ScrollBar findScrollBar(TableView<?> table, Orientation orientation) {
	    Set<Node> set = table.lookupAll(".scroll-bar");
	    for( Node node: set) {
	        ScrollBar verticalBar = (ScrollBar) node;
	        if( verticalBar.getOrientation() == orientation) {
				return verticalBar;
	        }
	    }
	    return null;
	}

	public String getSqlStatement() {
		return sqlStatement;
	}

	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	public boolean isFullData() {
		return fullData;
	}

	public void setFullData(boolean fullData) {
		this.fullData = fullData;
	}

    public void setInTable(boolean inTable) {
		this.inTable = inTable;
		Platform.runLater(() -> {
			if (horizontalBar != null) {
				horizontalBar.setValue(0);
			}
		});
		tempData.clear();
	}

	public ObservableList<TableViewData> getTempData() {
		return tempData;
	}

}
