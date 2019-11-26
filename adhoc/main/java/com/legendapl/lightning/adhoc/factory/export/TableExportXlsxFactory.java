package com.legendapl.lightning.adhoc.factory.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.custom.TableCustomColumn;
import com.legendapl.lightning.adhoc.factory.TableViewFactory.TableViewColumnCell;
import com.legendapl.lightning.adhoc.factory.export.ExportXlsxFactory.CellStylePool.Property;

import javafx.geometry.VPos;
import javafx.scene.control.TableColumn;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TableExportXlsxFactory extends ExportXlsxFactory {
	
	/*Export Data*/
	private static Integer lastColumnNum;
	private static TableViewColumnCell tableViewColumnCell;
	
	/**
	 * initialize before export
	 */
	private static void initExport() {
		ExportFactory.initTableExport();
		book = new XSSFWorkbook();
		sheet = book.createSheet("SHEET 1");
		lastColumnNum = tableCustomColumns.size() - 1;
		CellStylePool.clear();
	}

	/**
	 * export implementation
	 * @param outputFile
	 * @throws Exception 
	 */
	static void exportImpl(File outputFile) throws Exception {
		
		OutputStream os = new FileOutputStream(outputFile);
		
		initExport();
		drawHeader();
		drawColumns();
		
		book.write(os);
        book.close();
	}
	
	/**
	 * draw row header
	 * @param book
	 * @param sheet
	 */
	private static void drawHeader() {
		
		for (Integer j = 0; j < tableCustomColumns.size(); j ++) {
			
			TableCustomColumn<TableViewData, ?> column = tableCustomColumns.get(j);
			
			XSSFRow XSSFRow = createXSSFRow(0);
			drawHeaderCell(XSSFRow, j, column);
			
			sheet.setColumnWidth(j, getColumnWidth(column.getWidth()));
		}
	}
	
	/**
	 * draw a header cell
	 * @param column
	 * @param j
	 */
	private static void drawHeaderCell(XSSFRow XSSFRow, Integer j, TableCustomColumn column) {
		
		XSSFCell XSSFCell = createXSSFCell(XSSFRow, j);
		
		XSSFCell.setCellValue(column.getText());
		
		Property property = CellStylePool.createNewProperty();
		
		property.fillForegroundColor = ExportUtils.XColor.BLUE;
		property.vpos = VPos.CENTER;
		property.fontBoldFlg = true;
		
		CellStylePool.setCellStyle(XSSFCell, property);
	}
	
	/**
	 * draw columns
	 */
	private static void drawColumns() {
		
		for (Integer j = 0; j < tableCustomColumns.size(); j ++) {

			TableCustomColumn<TableViewData, ?> column = tableCustomColumns.get(j);
			
			drawColumn(column, j);
		}
	}
	
	/**
	 * draw a column
	 * @param column
	 * @param j - index of column
	 */
	private static void drawColumn(TableCustomColumn<TableViewData, ?> column, Integer j) {
		
		for (Integer i = 0; i < tableViewDatas.size(); i ++) {
			
			TableViewData row = tableViewDatas.get(i);
			
			tableViewColumnCell = (TableViewColumnCell) column.getCellFactory().call((TableColumn)column);
			tableViewColumnCell.updateIndex(i);
			
			XSSFRow XSSFRow = createXSSFRow(i + 1);
			drawColumnCell(XSSFRow, j, row, column);
			
			if (lastColumnNum > 0 && j == 0 && row.isGroupContent()) {
				sheet.addMergedRegion(new CellRangeAddress(XSSFRow.getRowNum(), XSSFRow.getRowNum(), 0, lastColumnNum));
			}
		}
	}
	
	/**
	 * draw a column cell
	 * @param XSSFRow
	 * @param j - index of column
	 * @param row
	 * @param column
	 */
	private static void drawColumnCell(XSSFRow XSSFRow, Integer j, TableViewData row, TableCustomColumn column) {
		
		XSSFCell XSSFCell = createXSSFCell(XSSFRow, j);
		
		XSSFCell.setCellValue(tableViewColumnCell.getText());
		
		Property property = CellStylePool.createNewProperty();
		
		property.vpos = tableViewColumnCell.getAlignment().getVpos();
		property.hpos = tableViewColumnCell.getAlignment().getHpos();
		
		boolean isLastFlg = tableViewDatas.indexOf(row) + 1 == tableViewDatas.size();
		property.bottomBorderStyle = row.isLastRow() || isLastFlg ? BorderStyle.MEDIUM : BorderStyle.NONE;
		
		if (row.isGroupContent()) {
			property.fillForegroundColor = ExportUtils.XColor.BLUE;
			property.topBorderStyle = BorderStyle.MEDIUM;
			property.fontBoldFlg = true;
			
		} else if (row.isLastRow()) {
			property.fillForegroundColor = ExportUtils.XColor.BLUE;
			property.topBorderStyle = BorderStyle.MEDIUM;
			property.fontBoldFlg = true;
			
		} else { // normal
			if (row.isOddStyle()) {
				property.fillForegroundColor = ExportUtils.XColor.WHITE;
				property.topBorderStyle = BorderStyle.NONE;
				property.fontBoldFlg = false;
				
			} else { // even
				property.fillForegroundColor = ExportUtils.XColor.GRAY;
				property.topBorderStyle = BorderStyle.NONE;
				property.fontBoldFlg = false;
				
			}
		}
		
		CellStylePool.setCellStyle(XSSFCell, property);
	}
	
}
