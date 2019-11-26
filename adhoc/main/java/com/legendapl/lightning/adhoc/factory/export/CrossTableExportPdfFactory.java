package com.legendapl.lightning.adhoc.factory.export;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableViewData;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTColumnTrieTree.CrossTableViewColumnCell;
import com.legendapl.lightning.adhoc.custom.CTCustomColumn;
import com.legendapl.lightning.adhoc.factory.CrossTableViewFactory.TopLeftLeafColumnCell;
import com.legendapl.lightning.adhoc.factory.CrossTableViewFactory.TopLeftLeafColumnCellSpecial;
import com.legendapl.lightning.adhoc.factory.export.ExportUtils.ENode;
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

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class CrossTableExportPdfFactory extends ExportPdfFactory {

	/*Export Data*/
	private static ENode root;
	private static List<ENode> bfsNodes;
	private static List<ENode> leafNodes;
	
	private static TableCell<CrossTableViewData, String> tableCell;
	private static TopLeftLeafColumnCell topLeftLeafColumnCell;
	private static TopLeftLeafColumnCellSpecial topLeftLeafColumnCellSpecial;
	private static CrossTableViewColumnCell crossTableViewColumnCell;
	
	private static int[] columnWidths;
	private static int tableWidth;
	private static int pageWidth;
	private static int pageHeight;
	
	static void exportImpl(File outputFile) throws Exception {
		
		FileOutputStream os = new FileOutputStream(outputFile);
		initExport();
		buildTree(root, crossTableCustomColumns);
		buildNodeList();
		initDocument(os);
		drawHeader();
		drawRows();
		
		document.add(datatable);
		document.close();
	}
	
	private static void initExport() throws Exception {
		ExportFactory.initCrossTableExport();
		tableCell = null;
		root = new ENode(null, -1, 0, 0);
		bfsNodes = new ArrayList<>();
		leafNodes = new ArrayList<>();
		baseFont = BaseFont.createFont(FONT_NAME_HEISEI, FONT_CODE_UNIJIS, BaseFont.EMBEDDED);
	}
	
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
	
	private static void buildNodeList() {
		
		Queue<ENode> queue = new LinkedList<>();
		for (ENode child : root.getChildren()) {
			queue.add(child);
		}
		
		while (!queue.isEmpty()) {
			
			ENode head = queue.poll();
			
			bfsNodes.add(head);
			if (head.getChildren().isEmpty()) leafNodes.add(head);
			
			for (ENode child : head.getChildren()) {
				queue.add(child);
			}
		}
	}
	
	private static void initDocument(FileOutputStream os) throws Exception {
		initParameter();
		document = new Document();
		PdfWriter.getInstance(document, os);
		document.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE);
		Rectangle rect = new Rectangle(pageWidth > PAGE_SIZE_WIDTH ? pageWidth : PAGE_SIZE_WIDTH, pageHeight);
		document.setPageSize(rect);
        document.open();
        datatable = new PdfPTable(columnNum);
	}
	
	private static void initParameter() {
		columnNum = leafNodes.size();
		rowNum = 1 + (leafNodes.size() > 0 ? leafNodes.get(0).r : 0) + crossTableViewDatas.size();
		columnWidths = new int[columnNum];
		tableWidth = 0;
		for (int i = 0; i < leafNodes.size(); i ++) {
			ENode node = leafNodes.get(i);
        	columnWidths[i] = getColumnWidth(node.column.getWidth());
        	tableWidth = tableWidth + columnWidths[i];
        }
		pageWidth = tableWidth + MARGIN_SIZE * 2;
		pageHeight = (int) (rowNum * DEFAULT_HEIGHT_IN_POINTS + 0.5) + MARGIN_SIZE * 2;
	}
	
	private static void drawHeader() throws Exception {
		
		for (ENode node : bfsNodes) {
			
			drawHeaderCell(node);
		};
		
		datatable.setHorizontalAlignment(Element.ALIGN_LEFT);
		datatable.setWidths(columnWidths);
		datatable.setTotalWidth(tableWidth);
		datatable.setLockedWidth(true);
		// DO NOT datatable.setHeaderRows(1);
	}
	
	private static void drawHeaderCell(ENode node) {
		
		PdfPCell cell = getCommonPdfPCell();
		
		cell.setColspan(node.getColumnNum());
		cell.setBorderColor(ExportUtils.PColor.DARK_GRAY);
		
		Boolean containGray = node.column.getStyleClass().contains("gray-background"); // TODO : use better way
		
		String text = ExportUtils.inTopRightColumn(node) && containGray ? "" : node.column.getText();
		Font font = new Font(baseFont, 10, Font.BOLD);
		cell.setPhrase(new Phrase(text, font));
		
		if (ExportUtils.inTopLeftColumn(node)) {
			cell.setBackgroundColor(ExportUtils.PColor.WHITE);
			cell.setHorizontalAlignment(Element.ALIGN_RIGHT);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			
		} else if (ExportUtils.inTopLeftLeafColumn(node)) {
			cell.setBackgroundColor(ExportUtils.PColor.BLUE);
			cell.setHorizontalAlignment(Element.ALIGN_LEFT);
			cell.setVerticalAlignment(Element.ALIGN_TOP);
			
		} else {
			cell.setBackgroundColor(containGray ? ExportUtils.PColor.GRAY : ExportUtils.PColor.BLUE);
			cell.setHorizontalAlignment(Element.ALIGN_CENTER);
			cell.setVerticalAlignment(Element.ALIGN_CENTER);
			
		}
		
		datatable.addCell(cell);
	}
	
	private static void drawRows() throws Exception {
		
		for (Integer i = 0; i < crossTableViewDatas.size(); i ++) {
			
			//CrossTableViewData row = crossTableViewDatas.get(i);
			
			for (int j = 0; j < leafNodes.size(); j ++) {
				
				ENode node = leafNodes.get(j);
				
				tableCell = (TableCell) node.column.getCellFactory().call((TableColumn)node.column);
				tableCell.updateIndex(i);
				
				if (tableCell instanceof TopLeftLeafColumnCellSpecial) {
					drawTopLeftLeafColumnCellSpecial(i, node.c1);
					
				} else if (ExportUtils.inTopLeftLeafColumn(node)) {
					drawTopLeftLeafColumnCell(i, node.c1);
					
				} else {
					drawCrossTableViewColumnCell(i, node.c1);
					
				}
			}
		}
	}
	
	private static void drawTopLeftLeafColumnCellSpecial(Integer i, Integer j) {
		
		topLeftLeafColumnCellSpecial = (TopLeftLeafColumnCellSpecial) tableCell;
		
		PdfPCell cell = getCommonPdfPCell();
		
		String text = topLeftLeafColumnCellSpecial.getText();
		Font font = new Font(baseFont, 10);
		font.setStyle(Font.BOLD);
		cell.setPhrase(new Phrase(text, font));
		
		cell.setBackgroundColor(ExportUtils.PColor.GRAY);
		cell.setBorderColor(ExportUtils.PColor.DARK_GRAY);
		
		cell.setVerticalAlignment(Element.ALIGN_CENTER);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		
		datatable.addCell(cell);
	}
	
	private static void drawTopLeftLeafColumnCell(Integer i, Integer j) {
		
		topLeftLeafColumnCell = (TopLeftLeafColumnCell) tableCell;
		
		PdfPCell cell = getCommonPdfPCell();
		
		String text = topLeftLeafColumnCell.getText();
		Font font = new Font(baseFont, 10);
		font.setStyle(topLeftLeafColumnCell.containStyleBoldFont() ? Font.BOLD : Font.NORMAL);
		cell.setPhrase(new Phrase(text, font));
		
		cell.setBackgroundColor(topLeftLeafColumnCell.containStyleTableCellEven() ? ExportUtils.PColor.GRAY : ExportUtils.PColor.WHITE);
		cell.setBorderColor(ExportUtils.PColor.DARK_GRAY);
		
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		
		if (!topLeftLeafColumnCell.containStyleTopBorder()) cell.disableBorderSide(Cell.TOP);
		if (!inLastRow(i)) cell.disableBorderSide(Cell.BOTTOM);
		
		datatable.addCell(cell);
	}
	
	private static void drawCrossTableViewColumnCell(Integer i, Integer j) {
		
		crossTableViewColumnCell = (CrossTableViewColumnCell) tableCell;
		
		PdfPCell cell = getCommonPdfPCell();
		
		String text = crossTableViewColumnCell.getText();
		Font font = new Font(baseFont, 10);
		font.setStyle(crossTableViewColumnCell.containStyleBoldFont() ? Font.BOLD : Font.NORMAL);
		cell.setPhrase(new Phrase(text, font));
		
		cell.setBackgroundColor(crossTableViewColumnCell.containStyleTableCellEven() ? ExportUtils.PColor.GRAY : ExportUtils.PColor.WHITE);
		cell.setBorderColor(ExportUtils.PColor.DARK_GRAY);
		
		cell.setVerticalAlignment(Element.ALIGN_RIGHT);
		cell.setHorizontalAlignment(Element.ALIGN_LEFT);
		
		cell.disableBorderSide(Cell.TOP);
		if (!inLastRow(i)) cell.disableBorderSide(Cell.BOTTOM);
		
		datatable.addCell(cell);
	}
	
	private static boolean inLastRow(Integer index) {
		return index == crossTableViewDatas.size() - 1;
	}
	
}
