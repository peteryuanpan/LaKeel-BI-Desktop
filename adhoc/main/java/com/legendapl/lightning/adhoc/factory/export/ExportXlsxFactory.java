package com.legendapl.lightning.adhoc.factory.export;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.controller.C100AdhocBaseAnchorPane;
import com.legendapl.lightning.adhoc.controller.P121AdhocAnchorPane;
import com.legendapl.lightning.adhoc.factory.AdhocSaveFactory;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.SaveFileChooserService;

import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.VPos;

public class ExportXlsxFactory extends ExportFactory {
	
	static final Integer MAX_COLUMN_WIDTH = 255; // DO NOT FIX THE NUMBER
	static final Integer DEFAULT_COLUMN_WIDTH_RATIO = 38;
	static final Float DEFAULT_HEIGHT_IN_POINTS = (float) 25;
	
	/**
	 * convert JFX width to XLSX width
	 * @param width
	 * @return
	 */
	static final Integer getColumnWidth(double width) {
		Integer columnWidth = Math.min((int)(width + 0.5), MAX_COLUMN_WIDTH);
		return columnWidth * DEFAULT_COLUMN_WIDTH_RATIO;
	}
	
	/**
	 * create an XSSFRow
	 * @param index
	 * @return
	 */
	static XSSFRow createXSSFRow(Integer index) {
		XSSFRow XSSFRow = sheet.getRow(index);
		if (null == XSSFRow) {
			XSSFRow = sheet.createRow(index);
			XSSFRow.setHeightInPoints(DEFAULT_HEIGHT_IN_POINTS);
		}
		return XSSFRow;
	}
	
	/**
	 * create an XSSFCell
	 * @param XSSFRow
	 * @param index
	 * @return
	 */
	static XSSFCell createXSSFCell(XSSFRow XSSFRow, Integer index) {
		XSSFCell XSSFCell = XSSFRow.getCell(index);
		if (null == XSSFCell) {
			XSSFCell = XSSFRow.createCell(index);
		}
		return XSSFCell;
	}
	
	/**
	 * Adhoc Export Xlsx Service
	 */
	static class AdhocExportXlsxService extends SaveFileChooserService {
		public AdhocExportXlsxService() {
			super();
			this.getProperty().setTitle(AdhocUtils.getString("P121.button.export"));
			this.getProperty().setFilterTypes(Arrays.asList(ExtensionFilterType.XLSX));
		}
		@Override protected void doCustomerCheck(File outputFile) throws Exception {
			String fileName = SaveFileChooserService.getRealFileName(outputFile.getName(), ExtensionFilterType.XLSX);
			if (fileName.isEmpty()) { // 名前が空場合
				String detailErrors = AdhocUtils.getString("ERROR_SAVE_FILE_NAME_EMPTY");
				throw new RuntimeException(detailErrors);
			}
			if (fileName.contains(".")) { // 名前が「.」を含む場合
				String detailErrors = AdhocUtils.getString("ERROR_SAVE_FILE_NAME_ILLEGAL");
				throw new RuntimeException(detailErrors);
			}
		}
		@Override protected void doSave(File outputFile) throws Exception {
			lastParentPath = outputFile.getParent();
			export(outputFile);
		}
		@Override protected void doFailed(File outputFile, Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showErrorNotInBack(AdhocUtils.getString("ERROR_ADHOC_EXPORT"), e.getMessage());
		}
		@Override protected void doSuccess(File outputFile) {
			logger.info("Exported Adhoc.");
			AlertWindowService.showInfoNotInBack(AdhocUtils.getString("SUCCESS_ADHOC_EXPORT"));
		}
	}
	
	static AdhocExportXlsxService service = new AdhocExportXlsxService();
	
	/**
	 * handle action on exporting
	 * @param event
	 * @throws InterruptedException 
	 */
	public static void handleActionExportXlsx(ActionEvent event) {
		service.getProperty().setInitialFileName(AdhocSaveFactory.fileName);
		service.getProperty().setInitialDirectoryPath(getInitialDirectoryPath());
		service.save(C100AdhocBaseAnchorPane.adhocStage);
	}
	
	/*Export Data*/
	static XSSFWorkbook book;
	static XSSFSheet sheet;
	
	/**
	 * export outputFile
	 * @param outputFile
	 * @throws Exception
	 */
	private static void export(File outputFile) throws Exception {
		switch (P121AdhocAnchorPane.lastViewModelType) {
		case TABLE:
			TableExportXlsxFactory.exportImpl(outputFile);
			break;
		case CROSSTABLE:
			CrossTableExportXlsxFactory.exportImpl(outputFile);
			break;
		default:
			break;
		}
	}
	
	/**
	 * cell style pool
	 */
	static class CellStylePool {
		
		// TODO : Adjust Property's Instance if need
		/**
		 * property
		 */
		static class Property {
			
			String fontName = "DejaVu Sans";
			Short fontSize = 10;
			boolean fontBoldFlg = false;
			
			VPos vpos = VPos.TOP;
			HPos hpos = HPos.LEFT;
			
			XSSFColor topBorderColor = ExportUtils.XColor.DARK_GRAY;
			XSSFColor bottomBorderColor = ExportUtils.XColor.DARK_GRAY;
			XSSFColor leftBorderColor = ExportUtils.XColor.DARK_GRAY;
			XSSFColor rightBorderColor = ExportUtils.XColor.DARK_GRAY;
			
			BorderStyle topBorderStyle = BorderStyle.MEDIUM;
			BorderStyle bottomBorderStyle = BorderStyle.MEDIUM;
			BorderStyle leftBorderStyle = BorderStyle.MEDIUM;
			BorderStyle rightBorderStyle = BorderStyle.MEDIUM;
			
			XSSFColor fillForegroundColor = ExportUtils.XColor.WHITE;
			FillPatternType fillPatternType = FillPatternType.SOLID_FOREGROUND;
			
			public Property() {

			}
			
			@Override
			public int hashCode() {
				return 5201314;
			}

			@Override
			public boolean equals(Object obj) {
				if (this == obj) return true;
				if (null == obj) return false;
				if (obj.getClass() != Property.class) return false;
				Property pro = (Property) obj;
				if (fontName != pro.fontName) return false;
				if (fontSize != pro.fontSize) return false;
				if (fontBoldFlg != pro.fontBoldFlg) return false;
				if (vpos != pro.vpos) return false;
				if (hpos != pro.hpos) return false;
				if (topBorderColor != pro.topBorderColor) return false;
				if (bottomBorderColor != pro.bottomBorderColor) return false;
				if (leftBorderColor != pro.leftBorderColor) return false;
				if (rightBorderColor != pro.rightBorderColor) return false;
				if (topBorderStyle != pro.topBorderStyle) return false;
				if (bottomBorderStyle != pro.bottomBorderStyle) return false;
				if (leftBorderStyle != pro.leftBorderStyle) return false;
				if (rightBorderStyle != pro.rightBorderStyle) return false;
				if (fillForegroundColor != pro.fillForegroundColor) return false;
				if (fillPatternType != pro.fillPatternType) return false;
				return true;
			}
		}
		
		static Property createNewProperty() {
			return new Property();
		}
		
		/**
		 * map
		 */
		private static Map<Property, XSSFCellStyle> map = new HashMap<>();
		
		static void debug() {
			logger.info(map.size());
		}
		
		static void clear() {
			map.clear();
		}
		
		static void setCellStyle(XSSFCell XSSFCell, Property property) {
			
			if (map.get(property) == null) {
				map.put(property, getNewCellStyle(property));
			}
			
			XSSFCell.setCellStyle(map.get(property));
		}
		
		private static XSSFCellStyle getNewCellStyle(Property property) {
			
			XSSFCellStyle cellStyle = book.createCellStyle();
			
			Font cellFont = book.createFont();
			cellFont.setFontName(property.fontName);
			cellFont.setFontHeightInPoints(property.fontSize);
			cellFont.setBold(property.fontBoldFlg);
			cellStyle.setFont(cellFont);
			
			cellStyle.setVerticalAlignment(ExportUtils.getVerticalAlignment(property.vpos));
			cellStyle.setAlignment(ExportUtils.getHorizontalAlignment(property.hpos));
			
			cellStyle.setTopBorderColor(property.topBorderColor);
			cellStyle.setBottomBorderColor(property.bottomBorderColor);
			cellStyle.setLeftBorderColor(property.leftBorderColor);
			cellStyle.setRightBorderColor(property.rightBorderColor);
			
			cellStyle.setBorderTop(property.topBorderStyle);
			cellStyle.setBorderBottom(property.bottomBorderStyle);
			cellStyle.setBorderLeft(property.leftBorderStyle);
			cellStyle.setBorderRight(property.rightBorderStyle);
			
			cellStyle.setFillForegroundColor(property.fillForegroundColor);
			cellStyle.setFillPattern(property.fillPatternType);
			
			return cellStyle;
		}
	}
	
}

