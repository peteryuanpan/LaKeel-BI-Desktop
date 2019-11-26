package com.legendapl.lightning.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.ServerInfo;
import com.legendapl.lightning.model.ExcelJob;
import com.orangesignal.csv.Csv;
import com.orangesignal.csv.CsvConfig;
import com.orangesignal.csv.handlers.StringArrayListHandler;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleCsvMetadataExporterConfiguration;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

public class ExcelSXSSFService extends ExcelCooperationService {

	public ExcelSXSSFService(ExcelJob excelJob, int definitionIndex, JasperPrint jasperPrint) {
		super(excelJob, definitionIndex, jasperPrint);
	}

	/**
	 * Excelへデータの挿入を行う
	 *
	 * @param targetColumns
	 * @return csvStr
	 */
	@Override
	public String executeJob(String targetColumns, String outputFilePath) {
		try {
			// 帳票が有効であるか判定する(無効な帳票はツール上で作成することが出来ないがxmlを直接変更することも可能なため、ここでチェックを行う)
			Boolean enableFlag = isEnableHeader(excelDefinition.getReportUri());
			if (null == enableFlag) {
				logger.error("This report can not be used.");
				return null;
			}

			/**
			 * CSVへエクスポートする。
			 *
			 */
			// csvへのエクスポーター
			JRCsvExporter csvExporter = new JRCsvExporter();
			// エクスポートの設定
			SimpleCsvMetadataExporterConfiguration conf = new SimpleCsvMetadataExporterConfiguration();

			conf.setFieldDelimiter(",");
			csvExporter.setConfiguration(conf);
			csvExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
			File csvFile = new File(ServerInfo.tempDir + "\\" + jasperPrint.getName() + ".csv");
			csvExporter.setExporterOutput(new SimpleWriterExporterOutput(csvFile, Constant.ReportExecution.CSV_ENCODE));
			csvExporter.exportReport();

			logger.debug("Export completed to CSV.");

			/**
			 * OrangeSignal(CSV操作ライブラリ)でCSVを2次元配列に格納する
			 *
			 */
			// InputStreamを生成する
			InputStream inputStream = new FileInputStream(csvFile);

			CsvConfig cfg = new CsvConfig(',', '"', '"');
			// CSVを配列のリスト(二次元配列)で取得する
			List<String[]> csvList = Csv.load(inputStream, "MS932", cfg, new StringArrayListHandler());

			/**
			 * POIで使用する各変数を定義する
			 */
			// FileInputStreamでxlsxファイルを読み込み、Javaオブジェクト(Workbook)に定義する
			FileInputStream fi = new FileInputStream(outputFilePath);
			// xlsxのワークブックを作成(closeしているのに警告が出る)
			@SuppressWarnings("resource")
			XSSFWorkbook xssfbook = new XSSFWorkbook(fi);
			fi.close();

			// ジョブに定義されたシートのインデックスを取得
			CreateExcelViewService createExcelViewService = new CreateExcelViewService(outputFilePath, 0, false);
			List<String> sheetList = createExcelViewService.getSheets();
			int sheetIndex = 0;
			for (int i = 0; i < sheetList.size(); i++) {
				if (excelDefinition.getSheet().equals(sheetList.get(i))) {
					sheetIndex = i;
					break;
				}
				// 該当のシートが存在しない場合
				if (i == sheetList.size() - 1) {
					logger.error("Sheet does not exist.");
					return null;
				}
			}
			// データの貼り付け先の"シート"
			Sheet sh = xssfbook.getSheetAt(sheetIndex);
			// データの貼り付け先の"行"
			Row ro;
			// データの貼り付け先の"セル(列)"
			Cell ce;
			// セルに適用するスタイル
			CellStyle cellStyle = null;
			// フォーマット
			DataFormat format = xssfbook.createDataFormat();

			// ジョブのセル位置から行と列のインデックスを取得する
			CellConversionService cellConversion = new CellConversionService();
			int columnIndex = cellConversion.getColumnIndex(excelDefinition.getCell());
			int rowIndex = cellConversion.getRowIndex(excelDefinition.getCell());

			// 数値か判定する正規表現
			Pattern p = Pattern.compile("^(\\-)?[0-9\\.\\,]*$");

			// 選択した列リスト
			List<String> targetColumnList = null;
			// 全ての列リスト
			List<String> hullColumnList = null;
			// 貼り付ける列のインデックスリスト
			List<Integer> targetColumnIndexList = null;
			//
			String[] firstArray = null;

			// 最初の行を判定するフラグ
			boolean firstLineFlag = true;
			// 最初のデータ行か判定するフラグ(1行目はヘッダ行にあたるため、初期値はfalse)→ヘッダを含まない場合は1行目からデータであるためtrue
			boolean firstDataFlag;
			if (excelDefinition.isColumnHeaderFlag())
				firstDataFlag = false;
			else
				firstDataFlag = true;

			// Excelの改ページの数
			int page = 1;

			/**
			 * 既存Excelファイルのデータ貼り付け範囲のセルのデータを全て消去する。<br>
			 * <br>
			 * POIのSXSSFWorkbookを使用することによりデータ貼り付けのパフォーマンスを向上させることが出来るが、<br>
			 * 既存セルの上書きが出来ないため、先にXSSFWorkbookで読み込み、対象のセルを消去する。
			 */
			// 列ヘッダのスタイルリスト
			List<CellStyle> headerCellStyleList = new ArrayList<CellStyle>();
			// データ行のスタイルリスト
			List<CellStyle> dataCellStyleList = new ArrayList<CellStyle>();
			int columnNum;
			// targetColumnsがNULLではない場合(=列選択がされている場合)
			if (!StringUtils.isEmpty(targetColumns))
				columnNum = new LinkedList<String>(Arrays.asList(targetColumns.split(","))).size();
			else
				columnNum = csvList.get(0).length;

			// 列ヘッダが有効の場合は列ヘッダのスタイルをコピーする
			if (excelDefinition.isColumnHeaderFlag()) {
				ro = sh.getRow(rowIndex);
				for (int i = 0; i <= columnNum; i++) {
					if (ro != null) {
						ce = ro.getCell(columnIndex + i);
						if (null != ce) {
							cellStyle = xssfbook.createCellStyle();
							cellStyle.cloneStyleFrom(ce.getCellStyle());
							short index = format.getFormat("General");
							cellStyle.setDataFormat(index);
							headerCellStyleList.add(cellStyle);// セルにスタイルが定義されている場合
						} else
							headerCellStyleList.add(sh.getColumnStyle(0));// セルにスタイルが定義されていない場合
					} else
						headerCellStyleList.add(sh.getColumnStyle(0));// セルにスタイルが定義されていない場合
				}
			}
			// データ行のスタイルをコピーする
			if (excelDefinition.isColumnHeaderFlag())
				ro = sh.getRow(rowIndex + 1);
			else
				ro = sh.getRow(rowIndex);

			for (int i = 0; i <= columnNum; i++) {
				if (ro != null) {
					ce = ro.getCell(columnIndex + i);
					if (null != ce) {
						cellStyle = xssfbook.createCellStyle();
						cellStyle.cloneStyleFrom(ce.getCellStyle());
						dataCellStyleList.add(cellStyle);// セルにスタイルが定義されている場合
					} else
						dataCellStyleList.add(sh.getColumnStyle(0));// セルにスタイルが定義されていない場合
				} else
					dataCellStyleList.add(sh.getColumnStyle(0));// セルにスタイルが定義されていない場合
			}

			// データが含まれるセルを全て消去する
			int rowNum = rowIndex;
			while (sh.getRow(rowNum) != null) {
				ro = sh.getRow(rowNum);
				sh.removeRow(ro);
				rowNum++;
			}

			// SXSSFWorkbookを作成(closeしているのに警告が出る)
			@SuppressWarnings("resource")
			SXSSFWorkbook book = new SXSSFWorkbook(xssfbook);
			// SXSSFWorkbookからシートを作成
			sh = book.getSheetAt(sheetIndex);

			/**
			 * Excelファイルへデータの貼り付けを行う。<br>
			 *
			 */
			for (int i = 0; i < csvList.size() - 1; i++) {// ライブラリの仕様か最終行に空白の配列が入るので-1をしている

				// 最初の1行(カラムヘッダーと仮定)
				if (i == 0 && firstLineFlag) {
					// ヘッダ行を配列として記録
					firstArray = csvList.get(i);

					// 全てのカラムリスト(csvのヘッダ行(最初の1行目)をカンマでスプリットする)
					hullColumnList = new LinkedList<String>(Arrays.asList(csvList.get(0)));
					// 選択したカラムのインデックスリスト
					targetColumnIndexList = new ArrayList<Integer>();

					// targetColumnsがNULLではない場合(=列選択がされている場合)
					if (!StringUtils.isEmpty(targetColumns)) {
						// 選択したカラムリスト
						targetColumnList = new LinkedList<String>(Arrays.asList(targetColumns.split(", ")));
						// 選択したカラムリストを総なめする
						for (int j = 0; j < targetColumnList.size(); j++) {
							// "選択したカラムリストの要素"と一致する"全てのカラムリストに格納された要素"のインデックスをリスト(=選択したカラムのインデックスリスト)に記録していく
							if (enableFlag) {
								for (int k = 0; k < hullColumnList.size(); k++) {
									if (targetColumnList.get(j).trim().equals(hullColumnList.get(k).trim())) {
										targetColumnIndexList.add(k);
										break;
									}
								}
							}
							// 列ヘッダが無効の場合
							else {
								Integer targetColumnIndex = Integer.parseInt(
										targetColumnList.get(j).substring(0, targetColumnList.get(j).indexOf(":")));
								targetColumnIndexList.add(targetColumnIndex);
							}
						}
					}
					// 全てのカラムをデフォルトの順番通りに選択している(ALL)の場合
					else {
						for (int index = 0; index < hullColumnList.size(); index++)
							targetColumnIndexList.add(index);
					}
				}

				// 列ヘッダを含まない場合はヘッダを挿入しない
				if (i == 0 && firstLineFlag && enableFlag && !excelDefinition.isColumnHeaderFlag()) {
					csvList.remove(i);
					i--;
					firstLineFlag = false;
					continue;
				}

				// 2回目以降の列ヘッダが登場した場合はデータを挿入せず、改ページを挿入する
				if (i != 0 && Arrays.equals(firstArray, csvList.get(i))) {
					// 改ページの上限値に達した場合はループを抜ける
					if (page <= 1020) {
						sh.setRowBreak(i + 2);
						page++;
					}
					csvList.remove(i);
					i--;
					continue;
				}

				// 行を取得する(該当の行が存在しなかったら新規作成する)
				ro = sh.getRow(rowIndex + i);
				if (null == ro) {
					sh.createRow(rowIndex + i);
					ro = sh.getRow(rowIndex + i);
				}

				// 選択したカラムのインデックスリストから順番に値を追加していく
				for (int j = 0; j < targetColumnIndexList.size(); j++) {

					// 列(セル)を取得する(該当のセルが存在しなかったら新規作成する)
					ce = ro.getCell(columnIndex + j);
					if (null == ce) {
						ro.createCell(columnIndex + j);
						ce = ro.getCell(columnIndex + j);
					}

					// 1行目(列ヘッダ)の場合
					if (i == 0 && excelDefinition.isColumnHeaderFlag()) {
						ce.setCellValue(csvList.get(i)[targetColumnIndexList.get(j)]);
						cellStyle = headerCellStyleList.get(j);
						short index = format.getFormat("General");
						cellStyle.setDataFormat(index);
						ce.setCellStyle(cellStyle);// セルにスタイルが定義されている場合
						ce.setCellType(ce.getCellTypeEnum());
						continue;
					}

					// 正規表現で数値かどうか判定する
					Matcher m = p.matcher(csvList.get(i)[targetColumnIndexList.get(j)]);

					if (m.find() && !StringUtils.isEmpty(csvList.get(i)[targetColumnIndexList.get(j)])) {
						// 数値の場合はDoubleにパースして値を挿入(カンマは消去する)
						ce.setCellValue(
								Double.parseDouble(csvList.get(i)[targetColumnIndexList.get(j)].replace(",", "")));
						// 最初のデータ(2行目)のスタイルをリストに追加(列ヘッダを含まない場合は1行目から)
						if (firstDataFlag) {
							cellStyle = dataCellStyleList.get(j);
							// カンマが含まれている数値の場合はカンマ区切りのフォーマットを追加してあげる
							if (csvList.get(i)[targetColumnIndexList.get(j)].contains(",")) {
								short index = format.getFormat("#,##0");
								cellStyle.setDataFormat(index);
							} else {
								short index = format.getFormat("General");
								cellStyle.setDataFormat(index);
							}
							dataCellStyleList.set(j, cellStyle);
						}
					} else {
						// 数値でない場合はStringのまま値を挿入する。
						ce.setCellValue(csvList.get(i)[targetColumnIndexList.get(j)]);
						// 最初のデータ(2行目)のスタイルをリストに追加(列ヘッダを含まない場合は1行目から)
						if (firstDataFlag) {
							cellStyle = dataCellStyleList.get(j);
							short index = format.getFormat("General");
							cellStyle.setDataFormat(index);
							dataCellStyleList.set(j, cellStyle);
						}
					}

					// スタイルを適用する
					ce.setCellType(ce.getCellTypeEnum());
					ce.setCellStyle(dataCellStyleList.get(j));
				}

				if (excelDefinition.isColumnHeaderFlag()) {
					// 2行目(最初のデータ行)の場合はスタイルをコピーするためフラグをたてる。
					if (i == 0)
						firstDataFlag = true;
					else
						firstDataFlag = false;
				} else {
					firstDataFlag = false;
				}

			}

			// ファイルに書き込み
			FileOutputStream out = new FileOutputStream(outputFilePath);
			book.write(out);
			// SXSSFWorkbookをcloseする
			book.dispose();
			// XSSFWorkbookをcloseする
			xssfbook.close();
			inputStream.close();
			out.close();
			// 一次出力したCSVファイルを消去する。
			csvFile.delete();

			logger.debug("All processing is completed.");

			return outputFilePath;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			jasperPrint = null;
			return null;
		}

	}

}
