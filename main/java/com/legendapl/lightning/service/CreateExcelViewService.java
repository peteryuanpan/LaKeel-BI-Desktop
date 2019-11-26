package com.legendapl.lightning.service;

import java.io.File;
import java.io.FileInputStream;
import java.time.LocalDate;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Color;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * ControlsFXのSpreadsheetViewを使用して、<br>
 * Excelのプレビュー画面の各種操作を行うサービスクラス<br>
 * <br>
 * 参考: https://stackoverflow.com/questions/42146856/embed-excel-in-javafx<br>
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class CreateExcelViewService {

	private Logger logger = Logger.getLogger(getClass());

	/** Excelファイルのパス */
	private String filePath;

	/** シートのインデックス */
	private int sheetIndex;

	/** 編集可能フラグ */
	private boolean editible;

	private FileInputStream inStream;
	private XSSFWorkbook poiWorkbook;
	private XSSFSheet poiSheet;

	/** Excelプレビューのコンポーネント */
	private SpreadsheetView theView;

	/**
	 * コンストラクタ
	 * 
	 * @param path
	 *            -> Excelのパス
	 * @param sheetIndex
	 *            -> シートのインデックス
	 * @param editable
	 *            -> 編集可否フラグ
	 */
	public CreateExcelViewService(String path, int sheetIndex, boolean editable) {
		filePath = path;
		this.editible = editable;
		this.sheetIndex = sheetIndex;
	}

	/**
	 * SpreadsheetView(Excelのプレビュー画面)を生成するメソッド
	 * 
	 * @throws Exception
	 */
	private void initializeView() throws Exception {
		GridBase grid = excelToGrid();

		theView = new SpreadsheetView(grid);
		theView.setEditable(editible);
	}

	/**
	 * SpreadsheetView(Excelのプレビュー画面)にバインドするデータ(GridBase)を返す。
	 * 
	 * @throws Exception
	 * @return grid
	 */
	public GridBase getGridBase() throws Exception {
		GridBase grid = excelToGrid();
		return grid;
	}

	/**
	 * SpreadsheetView(Excelのプレビュー画面)を返す。
	 * 
	 * @throws Exception
	 * @return theView
	 */
	public SpreadsheetView getView() throws Exception {
		initializeView();
		return theView;
	}

	/**
	 * Excelのシート一覧を返す。
	 * 
	 * @throws Exception
	 * @return sheetNames
	 */
	public ObservableList<String> getSheets() throws Exception {
		openBook();
		ObservableList<String> sheetNames = FXCollections.observableArrayList();

		if (null == poiWorkbook)
			return null;

		for (int i = 0; i < poiWorkbook.getNumberOfSheets(); i++) {
			sheetNames.add(poiWorkbook.getSheetName(i));
		}

		return sheetNames;

	}

	/**
	 * SpreadsheetViewを更新するメソッド
	 * 
	 * @throws Exception
	 */
	public void updateView() throws Exception {
		GridBase newgrid = excelToGrid();

		theView.setGrid(newgrid);

	}

	/**
	 * SpreadsheetView(Excelのプレビュー画面)にバインドするデータ(GridBase)を生成するメソッド
	 * 
	 * @throws Exception
	 * @return grid
	 */
	private GridBase excelToGrid() throws Exception {

		// Read the Excel document and collect the rows
		openBook();
		poiSheet = poiWorkbook.getSheetAt(sheetIndex);

		GridBase grid = new GridBase(100, 100);

		ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();

		Row poiRow;
		Cell cell;
		Object value = null;
		SpreadsheetCell spreadsheetCell = null;

		for (int row = 0; row < grid.getRowCount(); ++row) {
			final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
			poiRow = poiSheet.getRow(row);
			for (int column = 0; column < grid.getColumnCount(); ++column) {

				if (poiRow != null)
					cell = poiRow.getCell(column);
				else {
					cell = null;
				}

				StringBuffer styleBuffer = new StringBuffer();
				if (cell != null) {

					// 型に応じてセルの値を取得
					switch (cell.getCellTypeEnum()) {
					case STRING:
						value = cell.getRichStringCellValue().getString();
						break;
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							value = cell.getDateCellValue();
							break;
						} else {
							value = cell.getNumericCellValue();
							break;
						}
					case FORMULA:
						value = cell.getNumericCellValue();
						break;
					default:
						value = "";
					}

					Color bgColor = cell.getCellStyle().getFillForegroundColorColor();
					if (bgColor instanceof XSSFColor) {
						// RGB16進数表記へ変換
						byte[] bytes = ((XSSFColor) bgColor).getRGB();
						StringBuilder builder = new StringBuilder();
						for (byte b : bytes) {
							builder.append(String.format("%02x", b));
						}
						styleBuffer.append("-fx-background-color : \"#" + builder.toString() + "\"; ");
					}
					Font font = cell.getSheet().getWorkbook().getFontAt(cell.getCellStyle().getFontIndex());
					if (font instanceof XSSFFont) {
						XSSFFont xssfFont = (XSSFFont) font;
						// ボールド
						if (xssfFont.getBold()) {
							styleBuffer.append("-fx-font-weight: BOLD; ");
						}
						// イタリック
						if (xssfFont.getItalic()) {
							styleBuffer.append("-fx-font-style: italic; ");
						}
						// 取り消し線
						if (xssfFont.getStrikeout()) {
							styleBuffer.append("-fx-strikethrough: true; ");
						}
						// フォントサイズ
						styleBuffer.append("-fx-font-size: " + xssfFont.getFontHeightInPoints() + ".0px; ");
						// フォントファミリー
						styleBuffer.append("-fx-font-family: \"" + xssfFont.getFontName() + "\"; ");

						// フォントカラー
						XSSFColor foColor = xssfFont.getXSSFColor();
						if (foColor != null) {
							// RGB16進数表記へ変換
							byte[] bytes = foColor.getRGB();
							StringBuilder builder = new StringBuilder();
							if (null != bytes) {
								for (byte b : bytes) {
									builder.append(String.format("%02x", b));
								}
								styleBuffer.append("-fx-text-fill : \"#" + builder.toString() + "\"; ");
							}
						}

					}
				} else
					value = " "; // 空文字かnullだとwidthが適応されない

				styleBuffer.append("-fx-pref-width : " + poiSheet.getColumnWidth(column) / 30 + ".0 ; ");

				// 型に応じてセルの値を挿入
				if (value instanceof String)
					spreadsheetCell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, (String) value);
				else if (value instanceof LocalDate)
					spreadsheetCell = SpreadsheetCellType.DATE.createCell(row, column, 1, 1, (LocalDate) value);
				else if (value instanceof Double)
					spreadsheetCell = SpreadsheetCellType.DOUBLE.createCell(row, column, 1, 1, (Double) value);

				if (cell != null) {
					spreadsheetCell.setStyle(styleBuffer.toString());
				} else {
					// セルが存在しなかった場合
					spreadsheetCell.setStyle("-fx-pref-width : " + poiSheet.getColumnWidth(column) / 30 + ".0; ");
				}

				list.add(spreadsheetCell);

				value = null;
			}
			rows.add(list);
		}
		grid.setRows(rows);

		// セルを結合する
		final int size = poiSheet.getNumMergedRegions();
		for (int i = 0; i < size; i++) {
			final CellRangeAddress range = poiSheet.getMergedRegion(i);
			final int firstRow = range.getFirstRow();
			final int firstColumn = range.getFirstColumn();
			final int lastRow = range.getLastRow();
			final int lastColumn = range.getLastColumn();

			grid.spanColumn(lastColumn - firstColumn + 1, firstRow, firstColumn);
			grid.spanRow(lastRow - firstRow + 1, firstRow, firstColumn);
		}

		closeBook();

		return grid;
	}

	private void openBook() throws Exception {
		try {
			File myFile = new File(filePath);
			inStream = new FileInputStream(myFile);

			poiWorkbook = new XSSFWorkbook(inStream);

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}

	private void closeBook() throws Exception {

		try {
			poiWorkbook.close();
			inStream.close();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return;
		}
	}

}
