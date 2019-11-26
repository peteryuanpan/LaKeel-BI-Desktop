package com.legendapl.lightning.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.ServerInfo;
import com.legendapl.lightning.model.ExcelDefinition;

import net.sf.jasperreports.engine.DefaultJasperReportsContext;
import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.base.JRBaseComponentElement;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter;
import net.sf.jasperreports.engine.fill.AsynchronousFillHandle;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.util.JRSwapFile;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration;
import net.sf.jasperreports.web.servlets.AsyncJasperPrintAccessor;

/**
 * レポート実行(JasperPrintを生成する)を行うクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class ReportCreateServiceImpl extends ReportErrorService implements ReportCreateService {

	protected Logger logger = Logger.getLogger(getClass());

	public ReportCreateServiceImpl() {
		super();
	}

	/**
	 * 帳票実行に必要なパラメータを受け取り、JasperPrintを返すメソッド
	 * 
	 * @param url
	 * @param user
	 * @param password
	 * @param jrxmlPath
	 * @param exportFile
	 * @param params
	 * @param excelDefinition
	 * @return
	 */
	@Override
	public HashMap<JasperPrint, JRSwapFileVirtualizer> runReport(String url, String user, String password,
			String jrxmlPath, File exportFile, HashMap<String, Object> params, ExcelDefinition excelDefinition) {

		Connection m_con = null;
		// JasperPrintの生成(JasperPrint = .jrprint)
		JasperPrint jasperPrint;
		JRSwapFileVirtualizer virtualizer = null;

		try {
			DefaultJasperReportsContext.getInstance().setProperty("com.jaspersoft.jasperreports.license.location",
					System.getProperty("user.dir") + "\\jasperserver.license");

			// DB接続の定義
			if (null != url && null != user && null != password)
				m_con = DriverManager.getConnection(url, user, password);

			// 入力コントロールの定義
			if (null == params) {
				params = new HashMap<String, Object>();
			}

			// SwapVirtualizer
			JRSwapFile swap = new JRSwapFile(ServerInfo.tempDir, 4096, 64);
			virtualizer = new JRSwapFileVirtualizer(1, swap);
			params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			// GZipVirtualizer
			// JRGzipVirtualizer virtualizer = new JRGzipVirtualizer(1);
			// params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			// FileVirtualizer
			// JRFileVirtualizer virtualizer = new JRFileVirtualizer(1,
			// ServerInfo.tempDir);
			// params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);

			// jrxmlの読み込み(JasperDesign = .jrxml)
			JasperDesign jasperDesign = JRXmlLoader.load(jrxmlPath);

			// .jasperにコンパイル(JasperReport = .jasper)
			JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);

			// 行と列の情報が含まれている場合はExcel貼り付けの処理(抽出バンドの制限)を行う
			if (null != excelDefinition) {
				runReportForExcelDefinition(jrxmlPath, jasperReport, excelDefinition);
			}

			// 非同期実行のハンドルを作成
			AsynchronousFillHandle fillHandle = AsynchronousFillHandle.createHandle(jasperReport, params, m_con);

			// JasperPrintにアクセスするクラス(コンストラクタにハンドルを指定)
			AsyncJasperPrintAccessor asyncJasperPrintAccessor = new AsyncJasperPrintAccessor(fillHandle);

			// 非同期実行のリスナーとして上記のクラスを追加
			fillHandle.addFillListener(asyncJasperPrintAccessor); // ページ数の監視
			fillHandle.addListener(asyncJasperPrintAccessor); // ステータスの監視

			// JasperPrintの生成開始(非同期)
			fillHandle.startFill();
			jasperPrint = asyncJasperPrintAccessor.getFinalJasperPrint();
			asyncJasperPrintAccessor = null;
			fillHandle = null;

			// JasperPrintをexportFileへエクスポートする
			if (exportFile != null && null == excelDefinition) {
				exportJasperPrint(exportFile, jasperPrint);
			}

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			jasperPrint = null;
			addError(e);

		} finally {
			try {
				if (null != m_con) {
					m_con.close();
				}
			} catch (SQLException e) {
				logger.error("java.sql.Connection close failed.");
			}
		}
		HashMap<JasperPrint, JRSwapFileVirtualizer> hashMap = new HashMap<JasperPrint, JRSwapFileVirtualizer>();
		hashMap.put(jasperPrint, virtualizer);
		return hashMap;
	}

	/**
	 * 「jasperPrint」から「exportFile」へ帳票をエクスポートする
	 * 
	 * @param exportFile
	 * @author panyuan
	 * @throws Throwable
	 */
	public void exportJasperPrint(File exportFile, JasperPrint jasperPrint) {

		try {
			if (exportFile.getName().contains(".csv")) {
				// csvへのエクスポート
				JRCsvExporter csvExporter = new JRCsvExporter();
				csvExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				csvExporter.setExporterOutput(
						new SimpleWriterExporterOutput(exportFile.getPath(), Constant.ReportExecution.CSV_ENCODE));
				csvExporter.exportReport();

			} else if (exportFile.getName().contains(".pdf")) {
				// pdfへのエクスポート
				JRPdfExporter pdfExporter = new JRPdfExporter();
				pdfExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				pdfExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(exportFile.getPath()));
				pdfExporter.exportReport();

			} else if (exportFile.getName().contains(".xlsx")) {
				// xlsxへのエクスポート
				// インポートデータソース と エクスポート先 を設定する
				JRXlsxExporter xlsxExporter = new JRXlsxExporter();
				xlsxExporter.setExporterInput(new SimpleExporterInput(jasperPrint));
				xlsxExporter.setExporterOutput(new SimpleOutputStreamExporterOutput(exportFile.getPath()));
				// パラメータを設定する
				SimpleXlsxReportConfiguration xlsxReportConfig = new SimpleXlsxReportConfiguration();
				xlsxReportConfig.setOnePagePerSheet(false);
				xlsxReportConfig.setDetectCellType(true);
				xlsxReportConfig.setRemoveEmptySpaceBetweenRows(true);
				xlsxReportConfig.setRemoveEmptySpaceBetweenColumns(true);
				xlsxReportConfig.setWhitePageBackground(false);
				xlsxReportConfig.setIgnoreGraphics(false);
				xlsxReportConfig.setCollapseRowSpan(false);
				xlsxReportConfig.setIgnoreCellBorder(true);
				xlsxReportConfig.setFontSizeFixEnabled(true);
				xlsxReportConfig.setMaxRowsPerSheet(0);
				xlsxExporter.setConfiguration(xlsxReportConfig);
				// ローカルへエクスポート
				xlsxExporter.exportReport();

			} else {
				logger.warn("unknow exportFile Name found.");
			}

		} catch (Throwable e) {
			jasperPrint = null;
			logger.error(e.getMessage(), e);
			addError(e);
		}
	}

	/**
	 * 行と列の情報が含まれている場合はExcel貼り付けの処理(抽出バンドの制限)を行う
	 * 
	 * @param jrxmlPath
	 * @param jasperReport
	 * @author panyuan
	 * @since 2018.01.16
	 */
	private void runReportForExcelDefinition(String jrxmlPath, JasperReport jasperReport,
			ExcelDefinition excelDefinition) {

		ExcelCooperationService.TableBand isGraphComponent = ExcelCooperationService.isGraphComponent(null, jrxmlPath);

		// 共通して表示しないバンド
		jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.1", "pageHeader");
		jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.2", "columnFooter");
		jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.3", "pageFooter");
		jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.4", "background");

		// バンド内のコンポーネントが格納されるモデルクラス
		JRBaseComponentElement jrBaseComponentElement;
		int i = 0;
		List<JRChild> copyList;
		switch (isGraphComponent) {
		case titleBand:
			// 表コンポーネントの場合(タイトルバンド)
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.5", "detail");
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.6", "columnHeader");
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.7", "summary");

			/**
			 * タイトルバンド内の"表コンポーネント"以外のコンポーネントは全て消去する。
			 */
			copyList = new ArrayList<JRChild>(jasperReport.getTitle().getChildren());
			for (JRChild jrChild : copyList) {
				// JRDesignComponentElement(表コンポーネントやクロス集計コンポーネント)にキャストできるかチェックする
				if (jrChild instanceof JRBaseComponentElement) {
					jrBaseComponentElement = (JRBaseComponentElement) jrChild;
					// 取得したコンポーネントがStandardTable(表コンポーネント)であるかチェックする
					if (!jrBaseComponentElement.getComponent().getClass().getSimpleName().equals("StandardTable")) {
						// 表コンポーネント以外のコンポーネントであれば消去する。
						jasperReport.getTitle().getChildren().remove(i);
						continue;
					}
				} else {
					// 表コンポーネント以外のコンポーネントであれば消去する。
					jasperReport.getTitle().getChildren().remove(i);
					continue;
				}
				i++;
			}
			break;
		case sumarryBand:
			// 表コンポーネントの場合(サマリーバンド)
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.5", "detail");
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.6", "columnHeader");
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.7", "title");

			/**
			 * 詳細バンド内の"表コンポーネント"以外のコンポーネントは全て消去する。
			 */
			copyList = new ArrayList<JRChild>(jasperReport.getSummary().getChildren());
			for (JRChild jrChild : copyList) {
				// JRDesignComponentElement(表コンポーネントやクロス集計コンポーネント)にキャストできるかチェックする
				if (jrChild instanceof JRBaseComponentElement) {
					jrBaseComponentElement = (JRBaseComponentElement) jrChild;
					// 取得したコンポーネントがStandardTable(表コンポーネント)であるかチェックする
					if (!jrBaseComponentElement.getComponent().getClass().getSimpleName().equals("StandardTable")) {
						// 表コンポーネント以外のコンポーネントであれば消去する。
						jasperReport.getSummary().getChildren().remove(i);
						continue;
					}
				} else {
					// 表コンポーネント以外のコンポーネントであれば消去する。
					jasperReport.getSummary().getChildren().remove(i);
					continue;
				}
				i++;
			}
			break;
		default:
			// コンポーネント無しの帳票の場合
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.5", "summary");
			jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.6", "title");
			Boolean flag = ExcelCooperationService.isEnableHeader(excelDefinition.getReportUri());
			// 列ヘッダを含まない場合は列ヘッダを消去する
			if (null != flag && !flag) {
				jasperReport.setProperty("net.sf.jasperreports.export.csv.exclude.origin.band.7", "columnHeader");
			}
			break;
		}
	}
}