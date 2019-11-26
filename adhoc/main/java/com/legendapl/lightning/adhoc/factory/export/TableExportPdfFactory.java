package com.legendapl.lightning.adhoc.factory.export;

import java.io.File;
import java.io.FileOutputStream;

import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;
import com.legendapl.lightning.adhoc.custom.TableCustomColumn;
import com.legendapl.lightning.adhoc.factory.TableViewFactory.TableViewColumnCell;
import com.lowagie.text.Cell;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javafx.scene.control.TableColumn;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class TableExportPdfFactory extends ExportPdfFactory {
	
	/*Export Data*/
	private static TableViewColumnCell tableViewColumnCell;
	private static int[] columnWidths;
	private static int tableWidth;
	private static int pageWidth;

	static void exportImpl(File outputFile) throws Exception {
		FileOutputStream os = new FileOutputStream(outputFile);
		initExport();
		initDocument(os);
		drawHeader();
		drawRows();
		document.add(datatable);
		document.close();
	}
	
	private static void initExport() throws Exception {
		ExportFactory.initTableExport();
		columnNum = tableCustomColumns.size();
		baseFont = BaseFont.createFont(FONT_NAME_HEISEI, FONT_CODE_UNIJIS, BaseFont.EMBEDDED);
	}
	
	private static void initDocument(FileOutputStream os) throws Exception {
		initParameter();
		document = new Document();
		PdfWriter.getInstance(document, os);
		document.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE);
		Rectangle rect = new Rectangle(pageWidth > PAGE_SIZE_WIDTH ? pageWidth : PAGE_SIZE_WIDTH, PAGE_SIZE_HEIGHT);
		document.setPageSize(rect);
        document.open();
        datatable = new PdfPTable(columnNum);
	}
	
	private static void initParameter() {
		tableWidth = 0;
		columnWidths = new int[columnNum];
		for (Integer i = 0; i < tableCustomColumns.size(); i ++) {
			TableCustomColumn<TableViewData, ?> column = tableCustomColumns.get(i);
        	columnWidths[i] = getColumnWidth(column.getWidth());
        	tableWidth = tableWidth + columnWidths[i];
        }
		pageWidth = tableWidth + MARGIN_SIZE * 2;
	}
	
	private static void drawHeader() throws Exception {
		
		for (TableCustomColumn column : tableCustomColumns) {
        	PdfPCell cell = getTableHeader(column.getText());
        	datatable.addCell(cell);
        }
		
		datatable.setHorizontalAlignment(Element.ALIGN_LEFT);
		datatable.setWidths(columnWidths);
		datatable.setTotalWidth(tableWidth);
		datatable.setLockedWidth(true);
		datatable.setHeaderRows(1);
	}
	
	private static void drawRows() throws Exception {
		
		for (Integer i = 0; i < tableViewDatas.size(); i ++) {
			
			TableViewData tableRow = tableViewDatas.get(i);			
			
			if (tableRow.isGroupContent()) drawGroup(tableRow, i);
			else if (tableRow.isLastRow()) drawSummary(tableRow, i);
			else drawNormal(tableRow, i);
		}
	}
	
	private static void drawGroup(TableViewData tableRow, Integer i) {
		
		if (tableCustomColumns.size() > 0) {
			
			updateTableCell(tableCustomColumns.get(0), i);
			
			PdfPCell cell = getTableCellStyle(tableViewColumnCell.getText(), "group", false);
			cell.setColspan(columnNum);
			
			datatable.addCell(cell);
		}
	}
	
	private static void drawSummary(TableViewData tableRow, Integer i) {
		
		for (Integer j = 0; j < tableCustomColumns.size(); j ++) {
			
			updateTableCell(tableCustomColumns.get(j), i);
			
			PdfPCell cell = getTableCellStyle(tableViewColumnCell.getText(), "sum", true);
			
			datatable.addCell(cell);
		}
	}
	
	private static void drawNormal(TableViewData tableRow, Integer i) {
		
		for (Integer j = 0; j < tableCustomColumns.size(); j ++) {

			updateTableCell(tableCustomColumns.get(j), i);
			
			String styleType = tableRow.isOddStyle() ? "odd" : "even";
			boolean isLast = i + 1 == tableViewDatas.size();
			
			PdfPCell cell = getTableCellStyle(tableViewColumnCell.getText(), styleType, isLast);
			datatable.addCell(cell);
		}
	}
	
	private static void updateTableCell(TableCustomColumn<TableViewData, ?> column, Integer index) {
		tableViewColumnCell = (TableViewColumnCell) column.getCellFactory().call((TableColumn)column);
		tableViewColumnCell.updateIndex(index);
	}
	
	static PdfPCell getTableHeader(String name) {
		
		PdfPCell cell = new PdfPCell();
		Font font = new Font(baseFont, 10, Font.BOLD);
		Phrase para = new Phrase(name, font);
		cell.setPhrase(para);
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setPaddingLeft(5);
		cell.setPaddingTop(5);
        cell.setBackgroundColor(ExportUtils.PColor.BLUE);
        cell.setFixedHeight(DEFAULT_HEIGHT_IN_POINTS);
        cell.setBorderColor(ExportUtils.PColor.DARK_GRAY);
        cell.setBorderWidth(1);
        
        return cell;
    }
	
	static PdfPCell getTableCellStyle(String value, String styleType, boolean isLast) {
		
		PdfPCell cell = getCommonPdfPCell();
		
        cell.setVerticalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(ExportUtils.PColor.DARK_GRAY);
        
        Font font = new Font(baseFont, 11, Font.NORMAL);
        
        if (styleType.equals("odd")) {
        	cell.setBackgroundColor(ExportUtils.PColor.WHITE);
        	cell.disableBorderSide(Cell.TOP);
        	
        } else if (styleType.equals("even")) {
        	cell.setBackgroundColor(ExportUtils.PColor.GRAY);
        	cell.disableBorderSide(Cell.TOP);
        	
        } else if (styleType.equals("group")) {
        	font = new Font(baseFont, 11, Font.BOLD);
        	cell.setBackgroundColor(ExportUtils.PColor.BLUE);
        	
        } else if (styleType.equals("sum")) {
        	font = new Font(baseFont, 11, Font.BOLD);
        	cell.setBackgroundColor(ExportUtils.PColor.BLUE);
        }
        
        if (!isLast) {
        	cell.disableBorderSide(Cell.BOTTOM);
        }
        
        Phrase para = new Phrase(value, font);
		cell.setPhrase(para);
		
        return cell;
	}
	
}
