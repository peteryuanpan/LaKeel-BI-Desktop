package com.legendapl.lightning.adhoc.factory.export;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableViewData;
import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.controller.P121AdhocAnchorPane;
import com.legendapl.lightning.adhoc.custom.CTCustomColumn;
import com.legendapl.lightning.adhoc.custom.TableCustomColumn;
import com.legendapl.lightning.adhoc.factory.AdhocBaseFactory;

import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import net.jonathangiles.hacking.tableview.cellSpan.CellSpanTableView;

@SuppressWarnings("unchecked")
public class ExportFactory extends AdhocBaseFactory {
	
	/*----------All----------*/
	public static MenuButton exportButton;
	static MenuItem menuItemXlsx;
	static MenuItem menuItemXlsxAll;
	static MenuItem menuItemPdf;
	static MenuItem menuItemPdfAll;
	static CellSpanTableView<?> cellSpanTableView;
	
	/*----------Table----------*/
	static CellSpanTableView<TableViewData> tableView;
	static List<TableCustomColumn<TableViewData, ?>> tableCustomColumns;
	static List<TableViewData> tableViewDatas;
	
	/*----------CrossTable----------*/
	static CellSpanTableView<CrossTableViewData> crossTableView;
	static List<CTCustomColumn<CrossTableViewData, ?>> crossTableCustomColumns;
	static List<CrossTableViewData> crossTableViewDatas;
	
	/*----------Common----------*/
	static String lastParentPath = null;
	
	static String getInitialDirectoryPath() {
		return null != lastParentPath ?  lastParentPath : AdhocConstants.Application.WINDOWS_DESKTOP_PATH;
	}
	
	/**
	 * Initialize
	 */
	public static void init() {
		
		menuItemXlsx = new MenuItem(AdhocUtils.getString("P121.button.export.MenuItem.xlsx"));
		menuItemXlsxAll = new MenuItem(AdhocUtils.getString("P121.button.export.MenuItem.xlsx.All"));
		menuItemPdf = new MenuItem(AdhocUtils.getString("P121.button.export.MenuItem.pdf"));
		menuItemPdfAll = new MenuItem(AdhocUtils.getString("P121.button.export.MenuItem.pdf.All"));
		
		menuItemXlsx.getStyleClass().add("export-button-menuItem");
		menuItemXlsx.setOnAction(event -> ExportXlsxFactory.handleActionExportXlsx(event));
		
		menuItemXlsxAll.getStyleClass().add("export-button-menuItem");
		menuItemXlsxAll.setOnAction(event -> ExportXlsxFactory.handleActionExportXlsx(event));
		
		menuItemPdf.getStyleClass().add("export-button-menuItem");
		menuItemPdf.setOnAction(event -> ExportPdfFactory.handleActionExportPdf(event));
		
		menuItemPdfAll.getStyleClass().add("export-button-menuItem");
		menuItemPdfAll.setOnAction(event -> ExportPdfFactory.handleActionExportPdf(event));
		
		setMenuItemVisible(P121AdhocAnchorPane.viewModelType);
		
		// TODO: remove
		menuItemXlsxAll.setDisable(true);
		menuItemPdfAll.setDisable(true);
		
		exportButton.setDisable(true);
		exportButton.getItems().setAll(menuItemXlsx, menuItemXlsxAll, menuItemPdf, menuItemPdfAll);
	}
	
	// TODO
	public static void setMenuItemVisible(AdhocModelType viewModelType) {
		menuItemXlsxAll.setVisible(false);
		menuItemPdfAll.setVisible(false);
		//menuItemXlsxAll.setVisible(viewModelType == AdhocModelType.CROSSTABLE);
		//menuItemPdfAll.setVisible(viewModelType == AdhocModelType.CROSSTABLE);
	}
	
	static void initTableExport() {
		tableView = (CellSpanTableView<TableViewData>) cellSpanTableView;
		tableCustomColumns = new ArrayList<>();
		tableView.getColumns().forEach(column -> {
			tableCustomColumns.add((TableCustomColumn<TableViewData, ?>) column);
		});
		switch (P121AdhocAnchorPane.lastDataModelType) {
		case SIMPLEDATA:
			tableViewDatas = tableView.getItems();
			break;
		case FULLDATA:
		default:
			tableViewDatas = tableViewFactory.getTempData();
			break;
		}
	}
	
	static void initCrossTableExport() {
		crossTableView = (CellSpanTableView<CrossTableViewData>) cellSpanTableView;
		crossTableCustomColumns = new ArrayList<>();
		crossTableView.getColumns().forEach(column -> {
			crossTableCustomColumns.add((CTCustomColumn<CrossTableViewData, ?>) column);
		});
		switch (P121AdhocAnchorPane.lastDataModelType) {
		case SIMPLEDATA:
			crossTableViewDatas = crossTableView.getItems();
			break;
		case FULLDATA:
		default:
			crossTableViewDatas = crossTableViewFactory.getTempData();
			break;
		}
	}
	
	public static void setExportButton(MenuButton exportButton) {
		ExportFactory.exportButton = exportButton;
	}

	public static void setCellSpanTableView(CellSpanTableView<?> cellSpanTableView) {
		ExportFactory.cellSpanTableView = cellSpanTableView;
	}
	
}
