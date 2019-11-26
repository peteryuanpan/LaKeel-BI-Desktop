package com.legendapl.lightning.adhoc.factory.export;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableViewData;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTColumnTrieTree.CrossTableViewColumnCell;
import com.legendapl.lightning.adhoc.custom.CTCustomColumn;
import com.legendapl.lightning.adhoc.factory.CrossTableViewFactory.TopLeftLeafColumnCell;
import com.legendapl.lightning.adhoc.factory.CrossTableViewFactory.TopLeftLeafColumnCellSpecial;
import com.legendapl.lightning.adhoc.factory.export.ExportUtils.ENode;
import com.legendapl.lightning.adhoc.factory.export.ExportXlsxFactory.CellStylePool.Property;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CrossTableExportXlsxFactory extends ExportXlsxFactory {
	
	/*Export Data*/
	private static ENode root;
	private static TableCell<CrossTableViewData, String> tableCell;
	private static TopLeftLeafColumnCell topLeftLeafColumnCell;
	private static TopLeftLeafColumnCellSpecial topLeftLeafColumnCellSpecial;
	private static CrossTableViewColumnCell crossTableViewColumnCell;
	
	/**
	 * initialize before export
	 */
	private static void initExport() {
		ExportFactory.initCrossTableExport();
		book = new XSSFWorkbook();
		sheet = book.createSheet("SHEET 1");
		root = new ENode(null, -1, 0, 0);
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
		buildTree(root, crossTableCustomColumns);
		draw(root);
		
		book.write(os);
        book.close();
	}
	
	/**
	 * draw head
	 * @param father
	 * @param columns
	 */
	private static void buildTree(ENode father, List<CTCustomColumn<CrossTableViewData, ?>> columns) {
		
		for (CTCustomColumn column : columns) {
			
			ENode lastChild = father.getLastChild();
			Integer c1 = null == lastChild ? father.c1 : lastChild.c2 + 1;
			ENode child = new ENode(column, father.r + 1, c1, c1);
			
			buildTree(child, column.getColumns());
			
			father.c1 = Math.min(father.c1, child.c1);
			father.c2 = Math.max(father.c2, child.c2);
			
			father.getChildren().add(child);
		}
	}
	
	/**
	 * draw a tree in pre-order way
	 * @param node
	 */
	private static void draw(ENode node) {
		
		if (node.r >= 0) drawHeaderCells(node);
		
		for (ENode child : node.getChildren()) {
			
			draw(child);
		}
		
		if (node.getChildren().isEmpty()) drawColumn(node);
	}
	
	/**
	 * draw header cells
	 * @param node
	 */
	private static void drawHeaderCells(ENode node) {
		
		XSSFRow XSSFRow = createXSSFRow(node.r);
		
		for (int c = node.c1; c <= node.c2; c ++) {
			
			drawHeaderCell(XSSFRow, c, node);
		}
		
		if (node.getColumnNum() > 1) {
			sheet.addMergedRegion(new CellRangeAddress(node.r, node.r, node.c1, node.c2));
		}
		
		if (node.getChildren().isEmpty()) {
			sheet.setColumnWidth(node.c1, getColumnWidth(node.column.getWidth()));
		}
	}
	
	/**
	 * draw a header cell
	 * @param XSSFRow
	 * @param j
	 * @param node
	 */
	private static void drawHeaderCell(XSSFRow XSSFRow, Integer j, ENode node) {
		
		XSSFCell XSSFCell = createXSSFCell(XSSFRow, j);
		
		Boolean containGray = node.column.getStyleClass().contains("gray-background"); // TODO : use better way
		
		String text = ExportUtils.inTopRightColumn(node) && containGray ? "" : node.column.getText();
		XSSFCell.setCellValue(text);
		
		Property property = CellStylePool.createNewProperty();
		
		property.fontBoldFlg = true;
		
		if (ExportUtils.inTopLeftColumn(node)) {
			property.fillForegroundColor = ExportUtils.XColor.WHITE;
			property.hpos = HPos.RIGHT;
			property.vpos = VPos.CENTER;
			
		} else if (ExportUtils.inTopLeftLeafColumn(node)) {
			property.fillForegroundColor = ExportUtils.XColor.BLUE;
			property.hpos = HPos.LEFT;
			property.vpos = VPos.TOP;
			
		} else {
			property.fillForegroundColor = containGray ? ExportUtils.XColor.GRAY : ExportUtils.XColor.BLUE;
			property.hpos = HPos.CENTER;
			property.vpos = VPos.CENTER;
		}
		
		CellStylePool.setCellStyle(XSSFCell, property);
	}
	
	/**
	 * draw a column
	 * @param node
	 */
	private static void drawColumn(ENode node) {
		
		for (Integer i = 0; i < crossTableViewDatas.size(); i ++) {
			
			//CrossTableViewData row = crossTableViewDatas.get(i);
			
			tableCell = (TableCell) node.column.getCellFactory().call((TableColumn)node.column);
			tableCell.updateIndex(i);
			
			Integer indexRow = i + node.r + 1;
			XSSFRow XSSFRow = createXSSFRow(indexRow);
			
			if (tableCell instanceof TopLeftLeafColumnCellSpecial) {
				drawTopLeftLeafColumnCellSpecial(XSSFRow, i, node.c1);
				
			} else if (ExportUtils.inTopLeftLeafColumn(node)) {
				drawTopLeftLeafColumnCell(XSSFRow, i, node.c1);
				
			} else {
				drawCrossTableViewColumnCell(XSSFRow, i, node.c1);
			}
		}
	}
	
	/**
	 * draw a top left leaf column cell special
	 * @param XSSFRow
	 * @param i
	 * @param j
	 */
	private static void drawTopLeftLeafColumnCellSpecial(XSSFRow XSSFRow, Integer i, Integer j) {
		
		topLeftLeafColumnCellSpecial = (TopLeftLeafColumnCellSpecial) tableCell;
		
		XSSFCell XSSFCell = createXSSFCell(XSSFRow, j);
		XSSFCell.setCellValue(topLeftLeafColumnCellSpecial.getText());
		
		Property property = CellStylePool.createNewProperty();
		
		property.fillForegroundColor = ExportUtils.XColor.GRAY;
		property.vpos = VPos.CENTER;
		property.hpos = HPos.LEFT;
		property.fontBoldFlg = true;
		
		CellStylePool.setCellStyle(XSSFCell, property);
	}
	
	/**
	 * draw a top left leaf column cell
	 * @param XSSFRow
	 * @param i
	 * @param j
	 */
	private static void drawTopLeftLeafColumnCell(XSSFRow XSSFRow, Integer i, Integer j) {
		
		topLeftLeafColumnCell = (TopLeftLeafColumnCell) tableCell;
		
		XSSFCell XSSFCell = createXSSFCell(XSSFRow, j);
		XSSFCell.setCellValue(topLeftLeafColumnCell.getText());
		
		Property property = CellStylePool.createNewProperty();
		
		property.fillForegroundColor = topLeftLeafColumnCell.containStyleTableCellEven() ? ExportUtils.XColor.GRAY : ExportUtils.XColor.WHITE;
		property.topBorderStyle = topLeftLeafColumnCell.containStyleTopBorder() ? BorderStyle.MEDIUM : BorderStyle.NONE;
		property.bottomBorderStyle = inLastRow(i) ? BorderStyle.MEDIUM : BorderStyle.NONE;
		property.fontBoldFlg = topLeftLeafColumnCell.containStyleBoldFont();
		
		CellStylePool.setCellStyle(XSSFCell, property);
	}
	
	/**
	 * draw a crossTableView column cell
	 * @param XSSFRow
	 * @param i
	 * @param j
	 */
	private static void drawCrossTableViewColumnCell(XSSFRow XSSFRow, Integer i, Integer j) {
		
		crossTableViewColumnCell = (CrossTableViewColumnCell) tableCell;
		
		XSSFCell XSSFCell = createXSSFCell(XSSFRow, j);
		XSSFCell.setCellValue(crossTableViewColumnCell.getText());
		
		Property property = CellStylePool.createNewProperty();
		
		property.fillForegroundColor = crossTableViewColumnCell.containStyleTableCellEven() ? ExportUtils.XColor.GRAY : ExportUtils.XColor.WHITE;
		property.topBorderStyle = BorderStyle.NONE;
		property.bottomBorderStyle = inLastRow(i) ? BorderStyle.MEDIUM : BorderStyle.NONE;
		property.fontBoldFlg = crossTableViewColumnCell.containStyleBoldFont();
		
		CellStylePool.setCellStyle(XSSFCell, property);
	}
	
	private static boolean inLastRow(Integer index) {
		return index == crossTableViewDatas.size() - 1;
	}
	
}
