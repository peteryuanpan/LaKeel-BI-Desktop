package com.legendapl.lightning.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.jaspersoft.jasperserver.dto.resources.ClientJdbcDataSource;
import com.jaspersoft.jasperserver.dto.resources.ClientReportUnit;
import com.legendapl.lightning.common.constants.ReportErrors;
import com.legendapl.lightning.model.ExcelDefinition;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;

/**
 * レポート実行を行うクラス
 *
 * @author Legend Applications, LaKeel BI development team.
 */
public class ReportExcecuteService extends ReportErrorService {

	private Logger logger = Logger.getLogger(getClass());
	private ReportCreateService reportCreateService;

	// レポートURI
	private String reportUri;

	// エクスポート先ファイル
	private File exportFile;

	// 入力コントロールの値
	private HashMap<String, Object> params;

	// Excelジョブ
	private ExcelDefinition excelDefinition;

	/**
	 * コンストラクタ (通常実行、エクスポート)
	 * 
	 * @param reportUri
	 * @param exportFile
	 * @param params
	 * @param excelPasteFlag
	 */
	public ReportExcecuteService(String reportUri, File exportFile, HashMap<String, Object> params) {
		super();
		this.reportUri = reportUri;
		this.exportFile = exportFile;
		if (null != params)
			this.params = new HashMap<String, Object>(params);
		else
			this.params = new HashMap<String, Object>();
	}

	/**
	 * コンストラクタ (Excelジョブ)
	 * 
	 * @param excelDefinition
	 * @param params
	 */
	public ReportExcecuteService(ExcelDefinition excelDefinition, HashMap<String, Object> params) {
		super();
		this.excelDefinition = excelDefinition;
		this.reportUri = excelDefinition.getReportUri();
		this.params = new HashMap<String, Object>(params);
	}

	// レポート
	private ClientReportUnit clientReportUnit;

	// レポート JDBC データベース
	private ClientJdbcDataSource clientJdbcDataSource;

	// ローカル ファイル パス
	private String localJrxmlPath;

	/**
	 * JasperPrintを生成<br>
	 *
	 * @param getData
	 * @return jasperPrint
	 */
	public HashMap<JasperPrint, JRSwapFileVirtualizer> createJasperPrint() {

		try {

			try {
				clientReportUnit = ExecuteAPIService.getClientReportUnit(reportUri);
				clientJdbcDataSource = ExecuteAPIService.getDatasource(clientReportUnit);
			} catch (Throwable e) {
				logger.error(e.getMessage(), e);
				if (e instanceof java.lang.NullPointerException) {
					addErrorLog(Arrays.asList(ReportErrors.ERROR_EXECUTE));
				} else {
					addError(e);
				}
				return null;
			}

			logger.debug(clientJdbcDataSource);
			if (null == clientJdbcDataSource) {
				logger.error("clientJdbcDataSource is null.");
				addErrorLog(Arrays.asList(ReportErrors.ERROR_DATASOURCE));
				return null;
			}

			// データソースを含まない帳票の場合
			if (null == clientJdbcDataSource.getConnectionUrl()) {
				// jrxmlを取得
				localJrxmlPath = ExecuteAPIService.getJrxml(clientReportUnit);
				// リソース取得
				ExecuteAPIService.getResourceFile(clientReportUnit, localJrxmlPath);
				pageWidth(localJrxmlPath, true);
				opacity(localJrxmlPath);
				// レポート実行(JasperPrintの生成)
				reportCreateService = new ReportCreateServiceImpl();
				HashMap<JasperPrint, JRSwapFileVirtualizer> hashMap = reportCreateService.runReport(null, null, null,
						localJrxmlPath, exportFile, params, excelDefinition);
				return hashMap;
			}

			// データソースのパスワードを取得
			DecryptionDatasourceService decryptionDatasourceService = new DecryptionDatasourceService();
			String password = decryptionDatasourceService.getPassword(clientJdbcDataSource);
			if (null == password) {
				logger.error("Password of clientJdbcDataSource is null.");
				addErrorLog(Arrays.asList(ReportErrors.ERROR_DATASOURCE));
				return null;
			}

			// ConnectionURLとユーザ名を取得
			String url = clientJdbcDataSource.getConnectionUrl();
			String user = clientJdbcDataSource.getUsername();

			// jrxmlを取得
			localJrxmlPath = ExecuteAPIService.getJrxml(clientReportUnit);
			if (null == localJrxmlPath) {
				logger.error("localJrxmlPath is null.");
				addErrorLog(Arrays.asList(ReportErrors.ERROR_EXECUTE));
				return null;
			}

			// リソース取得
			ExecuteAPIService.getResourceFile(clientReportUnit, localJrxmlPath);
			pageWidth(localJrxmlPath, true);
			opacity(localJrxmlPath);

			// レポート実行(JasperPrintの生成)
			reportCreateService = new ReportCreateServiceImpl();
			HashMap<JasperPrint, JRSwapFileVirtualizer> hashMap = reportCreateService.runReport(url, user, password,
					localJrxmlPath, exportFile, params, excelDefinition);
			addErrorLog(((ReportCreateServiceImpl) reportCreateService).getErrorLog());

			return hashMap;

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
			addError(e);
			return null;
		}
	}

	/**
	 * 
	 * @param uri
	 * @param start
	 * @return
	 * @throws Exception
	 * @author xuguangheng
	 */
	private int pageWidth(String uri, boolean start) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream is = new FileInputStream(uri);
		Document doc = db.parse(is);
		NodeList nodeList = doc.getElementsByTagName("subreport");
		int width = Integer.parseInt(doc.getElementsByTagName("jasperReport").item(0).getAttributes()
				.getNamedItem("pageWidth").getTextContent());
		int pageWidth = width;
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				NodeList childs = node.getChildNodes();
				int subWidth = 0;
				for (int j = 0; j < childs.getLength(); j++) {
					Node child = childs.item(j);
					if (child.getNodeName().equals("subreportExpression")) {
						String subUri = child.getTextContent().replace("\"", "");
						subWidth += pageWidth(subUri.substring(0, subUri.length() - 5) + "rxml", false);
					} else if (child.getNodeName().equals("reportElement")) {
						subWidth += Integer.parseInt(child.getAttributes().getNamedItem("x").getTextContent());
					}
				}
				if (width < subWidth) {
					width = subWidth;
				}

			}
		}
		if (start && pageWidth != width) {
			if (pageWidth != width)
				doc.getElementsByTagName("jasperReport").item(0).getAttributes().getNamedItem("pageWidth")
						.setTextContent(width + "");
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(doc);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			StreamResult result = new StreamResult(new FileOutputStream(uri));
			transformer.transform(domSource, result);
		}
		return width;
	}

	/**
	 * 
	 * @param uri
	 * @throws Exception
	 * @author xuguangheng
	 */
	private void opacity(String uri) throws Exception {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		InputStream is = new FileInputStream(uri);
		Document doc = db.parse(is);
		NodeList nodeList = doc.getElementsByTagName("hc:chartProperty");
		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
				Node node = nodeList.item(i);
				String xAxisLableColor = "xAxis.labels.style.color";
				String yAxisLableColor = "yAxis.labels.style.color";
				String AxisLableOpacity = "";
				String nodeName = node.getAttributes().getNamedItem("name").getTextContent();
				if (nodeName.equals(xAxisLableColor))
					AxisLableOpacity = "xAxis.labels.style.opacity";
				else if (nodeName.equals(yAxisLableColor)) {
					AxisLableOpacity = "yAxis.labels.style.opacity";
				}
				if (!AxisLableOpacity.isEmpty()) {
					if (node.getAttributes() != null && node.getAttributes().getNamedItem("value") != null
							&& node.getAttributes().getNamedItem("value").getTextContent() != null) {
						Pattern pattern = Pattern.compile("#([0-9a-fA-F]{2})([0-9a-fA-F]{6})");
						Matcher matcher = pattern.matcher(node.getAttributes().getNamedItem("value").getTextContent());
						if (matcher.find()) {
							node.getAttributes().getNamedItem("value").setTextContent("#" + matcher.group(2));
							Element chartProperty = doc.createElement("hc:chartProperty");
							double opacity = Integer.parseInt(matcher.group(1), 16) / 255.0;
							chartProperty.setAttribute("name", AxisLableOpacity);
							chartProperty.setAttribute("value", opacity + "");
							node.getParentNode().appendChild(chartProperty);
						}
					} else {
						NodeList childs = node.getChildNodes();
						for (int j = 0; j < childs.getLength(); j++) {
							Node child = childs.item(j);
							if (child.getNodeName().equals("hc:propertyExpression")) {
								Pattern pattern = Pattern.compile(
										"new java.awt.Color\\(([0-9]{1,3},[0-9]{1,3},[0-9]{1,3}),([0,9]{1,3})\\)");
								Matcher matcher = pattern.matcher(child.getTextContent());
								if (matcher.find()) {
									child.setTextContent("new java.awt.Color(" + matcher.group(1) + ")");
									Element chartProperty = doc.createElement("hc:chartProperty");
									double opacity = Integer.parseInt(matcher.group(2)) / 255.0;
									chartProperty.setAttribute("name", AxisLableOpacity);
									chartProperty.setAttribute("value", opacity + "");
									child.getParentNode().getParentNode().appendChild(chartProperty);
								}
							}
						}
					}

				}
			}
			TransformerFactory transformerFactory = TransformerFactory.newInstance();
			Transformer transformer = transformerFactory.newTransformer();
			DOMSource domSource = new DOMSource(doc);
			transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			StreamResult result = new StreamResult(new FileOutputStream(uri));
			transformer.transform(domSource, result);
		}
	}

	/**
	 * レポートのエクスポートファイルをセットする
	 * 
	 * @param exportFile
	 * @author panyuan
	 */
	public void setExportFile(File exportFile) {
		this.exportFile = exportFile;
	}
}