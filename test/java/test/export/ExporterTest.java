package test.export;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.Constant.ServerInfo;

import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
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
 * 以下のファイルまたはURIを参照してください。
 * ReportCreateServiceImpl.java
 * https://www.tutorialspoint.com/jasper_reports/jasper_exporting_reports.htm
 */
public class ExporterTest {
	
	protected static Logger logger = Logger.getLogger(ExporterTest.class);

	public static void main(String[] args) {
		ExporterTest exporterTest = new ExporterTest();
		try {
			exporterTest.runReport();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void runReport() throws Exception {
		
		// jasperReport
		String jrxmlPath = "test/main.jrxml";
		JasperDesign jasperDesign = JRXmlLoader.load(jrxmlPath);
		JasperReport jasperReport = JasperCompileManager.compileReport(jasperDesign);
		
		// mysql
		String url = "jdbc:mysql://172.17.2.110:3306/lakeelbi";
		String user = "root";
		String password = "root";
		Connection m_con = DriverManager.getConnection(url, user, password);
		
		// parameters
		HashMap<String, Object> params = new HashMap<>();
		JRSwapFile swap = new JRSwapFile(ServerInfo.tempDir, 4096, 64);
		JRSwapFileVirtualizer virtualizer = new JRSwapFileVirtualizer(1, swap);
		params.put(JRParameter.REPORT_VIRTUALIZER, virtualizer);
		
		// fillHandle, AsyncJasperPrintAccessor
		AsynchronousFillHandle fillHandle = AsynchronousFillHandle.createHandle(jasperReport, params, m_con); // TODO
		AsyncJasperPrintAccessor asyncJasperPrintAccessor = new AsyncJasperPrintAccessor(fillHandle);
		fillHandle.addFillListener(asyncJasperPrintAccessor);
		fillHandle.addListener(asyncJasperPrintAccessor);
		fillHandle.startFill();
		
		// jasperPrint
		JasperPrint jasperPrint = asyncJasperPrintAccessor.getFinalJasperPrint();
		
		// export
		File exportFile = new File("test\\test.xlsx");
		exportJasperPrint(exportFile, jasperPrint);
		
		logger.info("Export Finished!");
	}
	
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
		}
	}
	
}
