package com.legendapl.lightning.service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.jaspersoft.jasperserver.dto.resources.ClientReportUnit;
import com.legendapl.lightning.model.ExcelDefinition;
import com.legendapl.lightning.model.ExcelJob;

import net.sf.jasperreports.engine.JRChild;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.design.JRDesignBand;
import net.sf.jasperreports.engine.design.JRDesignComponentElement;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.xml.JRXmlLoader;

/**
 * Excel連携機能に伴う各種の処理(帳票のCSVエクスポート、バリデーションチェック、データの貼り付け等)を行う統合的なサービスクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public abstract class ExcelCooperationService {

	/** ロガー */
	protected static Logger logger = Logger.getLogger(MethodHandles.lookup().lookupClass());

	/** Excel貼り付け定義のモデルクラス */
	protected ExcelDefinition excelDefinition;
	/** JasperPrintオブジェクト */
	protected JasperPrint jasperPrint;

	/**
	 * コンストラクタ―
	 * 
	 * @param excelDefinition
	 * @param jasperPrint
	 * 
	 */
	public ExcelCooperationService(ExcelJob excelJob, int definitionIndex, JasperPrint jasperPrint) {
		this.jasperPrint = jasperPrint;
		this.excelDefinition = excelJob.getExcelDefinitionList().get(definitionIndex);
	}

	/**
	 * 列ヘッダのリスト(カラム一覧)を返すメソッド
	 * 
	 * @return columnList
	 */
	public static List<String> getColumnList(String reportUri) {
		Document doc = getDoc(reportUri);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		// 列ヘッダのPath
		String location = "/jasperReport/columnHeader/band";
		// 列ヘッダのラベルのPath
		String columnHeaderLabelLocation = "/jasperReport/columnHeader/band/staticText";
		// 列ヘッダのテキストフィールドのPath
		String columnHeaderTextFieldLocation = "/jasperReport/columnHeader/band/textField";

		// 表コンポーネントの場合、コンポーネント内部の列ヘッダ部分を取得する
		TableBand componentFlag = isGraphComponent(reportUri, null);
		switch (componentFlag) {
		case sumarryBand:
			columnHeaderLabelLocation = "/jasperReport/summary/band/componentElement/table/column/columnHeader/staticText";
			break;
		case titleBand:
			columnHeaderLabelLocation = "/jasperReport/title/band/componentElement/table/column/columnHeader/staticText";
			break;
		case nonComponent:
			break;
		default:
			return null;
		}

		// 列ヘッダのラベル or テキストフィールドのリスト
		List<String> columnList = new ArrayList<String>();

		NodeList entries;
		if (componentFlag.equals(TableBand.nonComponent)) {
			try {
				// 列ヘッダにラベル or テキストフィールド以外のコンポーネントが存在した場合はfalseを返す
				entries = (NodeList) xpath.evaluate(location, doc, XPathConstants.NODESET);
				for (int i = 0; i < entries.item(0).getChildNodes().getLength(); i++) {
					if (entries.item(0).getChildNodes().item(i).hasChildNodes()) {
						String nodeName = entries.item(0).getChildNodes().item(i).getNodeName();
						if (!(nodeName.equals("staticText") || nodeName.equals("textField")))
							return null;
					}
				}
			} catch (XPathExpressionException e) {
				logger.error(e.getMessage(), e);
			}
		}

		try {
			// 列ヘッダのラベル or テキストフィールドのリストを作成する

			/**
			 * jrxmlから値を抽出した際に実際の表示順になっていないことがあるので<br>
			 * コンポーネントのX座標を同時に取得して昇順でソートする。
			 * 
			 * https://www.sejuku.net/blog/16176
			 */
			Map<String, Integer> columnMap = new HashMap<String, Integer>();
			// ラベル一覧取得
			entries = (NodeList) xpath.evaluate(columnHeaderLabelLocation, doc, XPathConstants.NODESET);
			for (int i = 0; i < entries.getLength(); i++) {
				/**
				 * キー(カラム名)が重複する可能性があるので、添え字を追加し、リスト格納時にリプレイスする
				 */
				columnMap.put(xpath.evaluate("text", entries.item(i)) + ": " + i,
						Integer.parseInt(xpath.evaluate("reportElement/@x", entries.item(i))));
			}
			if (componentFlag.equals(TableBand.nonComponent)) {
				// テキストフィールド一覧取得
				entries = (NodeList) xpath.evaluate(columnHeaderTextFieldLocation, doc, XPathConstants.NODESET);
				for (int i = 0; i < entries.getLength(); i++) {
					/**
					 * キー(カラム名)が重複する可能性があるので、添え字を追加し、リスト格納時にリプレイスする
					 */
					columnMap.put(xpath.evaluate("textFieldExpression", entries.item(i)) + ": " + i,
							Integer.parseInt(xpath.evaluate("reportElement/@x", entries.item(i))));
				}
			}
			// Map.Entryのリストを作成する
			List<Entry<String, Integer>> list_entries = new ArrayList<Entry<String, Integer>>(columnMap.entrySet());

			// 比較関数Comparatorを使用してMap.Entryの値を比較する(昇順)
			Collections.sort(list_entries, new Comparator<Entry<String, Integer>>() {
				public int compare(Entry<String, Integer> obj1, Entry<String, Integer> obj2) {
					// 昇順ソート
					return obj1.getValue().compareTo(obj2.getValue());
				}
			});

			// ループで要素順に値を取得する
			for (Entry<String, Integer> entry : list_entries) {
				columnList.add(entry.getKey().replaceAll(": [0-9]+$", ""));
			}

		} catch (XPathExpressionException e) {
			logger.error(e.getMessage(), e);
		}

		return columnList;
	}

	/**
	 * 詳細バンドのリスト(カラム一覧)を返すメソッド
	 * 
	 * @return detailList
	 */
	public static List<String> getDetailList(String reportUri) {

		Document doc = getDoc(reportUri);

		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();

		// 詳細バンドのテキストフィールドのPath
		String location = "/jasperReport/detail/band";
		String detailTextFieldLocation = "/jasperReport/detail/band/textField";

		TableBand componentFlag = isGraphComponent(reportUri, null);
		if (null == componentFlag)
			return null;

		if (componentFlag.equals(TableBand.nonComponent)) {
			try {
				// 詳細バンドにテキストフィールド以外のコンポーネントが存在した場合はnullを返す
				NodeList entries = (NodeList) xpath.evaluate(location, doc, XPathConstants.NODESET);
				for (int i = 0; i < entries.item(0).getChildNodes().getLength(); i++) {
					if (entries.item(0).getChildNodes().item(i).hasChildNodes()) {
						String nodeName = entries.item(0).getChildNodes().item(i).getNodeName();
						if (!(nodeName.equals("textField")))
							return null;
					}
				}
			} catch (XPathExpressionException e) {
				logger.error(e.getMessage(), e);
			}
		}

		// 詳細バンドのテキストフィールドリスト
		List<String> detailList = new ArrayList<String>();

		Map<Integer, String> detailMap = new HashMap<Integer, String>();

		try {
			// 詳細バンドのテキストフィールドのリストを作成する
			NodeList entries = (NodeList) xpath.evaluate(detailTextFieldLocation, doc, XPathConstants.NODESET);

			entries = (NodeList) xpath.evaluate(detailTextFieldLocation, doc, XPathConstants.NODESET);
			for (int i = 0; i < entries.getLength(); i++) {
				detailMap.put(Integer.parseInt(xpath.evaluate("reportElement/@x", entries.item(i))),
						xpath.evaluate("textFieldExpression", entries.item(i)));
			}

			// キーのリストを作る
			List<Integer> keylist = new ArrayList<Integer>(detailMap.keySet());
			// 昇順ソート
			Collections.sort(keylist);

			int i = 0;
			for (Integer key : keylist) {
				detailList.add(i + ": " + detailMap.get(key));
				i++;
			}

		} catch (XPathExpressionException e) {
			logger.error(e.getMessage(), e);
		}

		return detailList;
	}

	/**
	 * XPathでjrxmlを解析するためのオブジェクト(Docment)を返す。
	 * 
	 * @param reportUri
	 * @return doc
	 */
	public static Document getDoc(String reportUri) {
		ClientReportUnit clientReportUnit = ExecuteAPIService.getClientReportUnit(reportUri);
		String jrxmlPath = ExecuteAPIService.getJrxml(clientReportUnit);

		// XML文書(jrxml)の読み込み
		DocumentBuilder builder;
		Document doc = null;
		try {
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(new File(jrxmlPath));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return doc;
	}

	/**
	 * Excelへデータの挿入を行う
	 * 
	 * @param targetColumns
	 * @return csvStr
	 */
	public abstract String executeJob(String targetColumns, String outputFilePath);

	/**
	 * 以下を条件に列ヘッダが有効であるかチェックを行うメソッド<br>
	 * 有効である場合はtrueを返し、無効の場合はfalseを返す。
	 * 
	 * [列ヘッダにテキストコンポーネント以外存在しない]<br>
	 * 且つ<br>
	 * [列ヘッダと詳細バンドのコンポーネント数が一致する]<br>
	 * 
	 * @return boolean
	 */
	public static Boolean isEnableHeader(String reportUri) {

		/** 列ヘッダのラベル or テキストフィールドのリスト */
		List<String> columnList = getColumnList(reportUri);
		/** 詳細バンドのテキストフィールドリスト */
		List<String> detailList = getDetailList(reportUri);

		// 表コンポーネントの場合を使用している場合は無条件でtrueとする
		TableBand flag = isGraphComponent(reportUri, null);
		switch (flag) {
		case sumarryBand:
		case titleBand:
			return true;
		case nonComponent:
			// 詳細バンドのリストが取得できない段階でその帳票は実行不可とする
			if (null == detailList || detailList.size() == 0) {
				return null;
			}

			// 列ヘッダのリストが取得できないので無効とする。
			if (null == columnList || columnList.size() == 0) {
				return false;
			}

			// 列ヘッダと詳細バンドのリストのサイズを照合し、一致しなければ無効とする
			if (columnList.size() != detailList.size()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Excelジョブに指定した帳票が表コンポーネントであるか判定するメソッド<br>
	 * 表コンポーネントの場合はtrue, コンポーネントを使用していない場合はfalseを返す。<br>
	 * パラメータにレポートのURIもしくはローカルに保存したjrxmlファイルのパスを受け取る
	 * 
	 * @param reportUri
	 * @param jrxmlPath
	 * @return boolean
	 */
	public static TableBand isGraphComponent(String reportUri, String jrxmlPath) {
		if (!StringUtils.isEmpty(reportUri)) {
			ClientReportUnit clientReportUnit = ExecuteAPIService.getClientReportUnit(reportUri);
			jrxmlPath = ExecuteAPIService.getJrxml(clientReportUnit);
		}
		JasperDesign jasperDesign = null;
		JasperReport jasperReport = null;
		try {
			jasperDesign = JRXmlLoader.load(jrxmlPath);
			jasperReport = JasperCompileManager.compileReport(jasperDesign);
		} catch (JRException e) {
			logger.error(e.getMessage(), e);
			return null;
		}

		// タイトルのNULLチェック
		if (null != jasperReport.getTitle() && jasperReport.getTitle().getChildren().size() != 0) {
			// タイトルバンドを取得
			JRDesignBand band = (JRDesignBand) jasperDesign.getTitle();
			// タイトルバンドの要素を取得
			JRDesignComponentElement jrDesignComponentElement;
			for (JRChild jrChild : band.getChildren()) {
				// JRDesignComponentElement(表コンポーネントやクロス集計コンポーネント)にキャストできるかチェックする
				if (jrChild instanceof JRDesignComponentElement) {
					jrDesignComponentElement = (JRDesignComponentElement) jrChild;
					// 取得したコンポーネントがStandardTable(表コンポーネント)であるかチェックする
					if (jrDesignComponentElement.getComponent().getClass().getSimpleName().equals("StandardTable")) {
						return TableBand.titleBand;
					}
				}
			}
		}

		// サマリーのNULLチェック
		if (null != jasperReport.getSummary() && jasperReport.getSummary().getChildren().size() != 0) {
			// タイトルバンドを取得
			JRDesignBand band = (JRDesignBand) jasperDesign.getSummary();
			// タイトルバンドの要素を取得
			JRDesignComponentElement jrDesignComponentElement;
			for (JRChild jrChild : band.getChildren()) {
				// JRDesignComponentElement(表コンポーネントやクロス集計コンポーネント)にキャストできるかチェックする
				if (jrChild instanceof JRDesignComponentElement) {
					jrDesignComponentElement = (JRDesignComponentElement) jrChild;
					// 取得したコンポーネントがStandardTable(表コンポーネント)であるかチェックする
					if (jrDesignComponentElement.getComponent().getClass().getSimpleName().equals("StandardTable")) {
						return TableBand.sumarryBand;
					}
				}
			}
		}
		return TableBand.nonComponent;
	}

	public enum TableBand {
		titleBand, sumarryBand, nonComponent
	};

	/**
	 * 出力先のExcelファイルを生成し、パスを返す。
	 * 
	 * @param input
	 * @param output
	 * @param rewriteFlag
	 * @return outputFilePath
	 */
	public static String copyFile(String input, String output, boolean rewriteFlag) {
		String outputFilePath = null;

		// 参照元と出力先が等しく、上書きする設定であれば何もしない。
		if (input.equals(output) && rewriteFlag) {
			outputFilePath = input;
			return outputFilePath;
		}

		Path inputFile = Paths.get(input);
		Path outputFile = null;
		File file = null;

		// 参照元と出力先が等しいが上書きしない設定の場合(日時をファイル名に追記してローテ―トする)
		if (input.equals(output) && !rewriteFlag) {
			// 既にファイルが存在していた場合
			if (new File(output).exists()) {
				String extension = ".xlsx";
				if (output.endsWith(".xlsm"))
					extension = ".xlsm";

				boolean repeatedFlag = true;
				int rotationNum = 1;
				while (repeatedFlag) {
					file = new File(output.replace(extension, "") + "(" + rotationNum + ")" + extension);
					if (file.exists())
						rotationNum++;
					else
						repeatedFlag = false;
				}
				outputFile = Paths.get(file.toString());
				outputFilePath = file.toString();
			}
			// まだファイルが存在しない場合
			else {
				outputFile = Paths.get(output);
				outputFilePath = output;
			}
		}

		// 参照元と出力先が異なり、上書きする設定の場合
		if (!input.equals(output) && rewriteFlag) {
			outputFile = Paths.get(output);
			outputFilePath = output;
		}

		// 参照元と出力先が異なり、上書きしない設定の場合(日時をファイル名に追記してローテ―トする)
		if (!input.equals(output) && !rewriteFlag) {
			// 既にファイルが存在していた場合
			if (new File(output).exists()) {
				String extension = ".xlsx";
				if (output.endsWith(".xlsm"))
					extension = ".xlsm";

				boolean repeatedFlag = true;
				int rotationNum = 1;
				while (repeatedFlag) {
					file = new File(output.replace(extension, "") + "(" + rotationNum + ")" + extension);
					if (file.exists())
						rotationNum++;
					else
						repeatedFlag = false;
				}
				outputFile = Paths.get(file.toString());
				outputFilePath = file.toString();
			}
			// まだファイルが存在しない場合
			else {
				outputFile = Paths.get(output);
				outputFilePath = output;
			}
		}

		try {
			Files.copy(inputFile, outputFile, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			return null;
		}

		return outputFilePath;
	}

	/**
	 * ファイルを読み込み、その中身をバイト配列で取得する<br>
	 * http://propg.ee-mall.info/%E3%83%97%E3%83%AD%E3%82%B0%E3%83%A9%E3%83%9F%E3%83%B3%E3%82%B0/java/java-%E3%83%95%E3%82%A1%E3%82%A4%E3%83%AB%E3%82%92%E8%AA%AD%E3%82%93%E3%81%A7%E3%83%90%E3%82%A4%E3%83%88%E9%85%8D%E5%88%97%E3%81%AB%E5%85%A5%E3%82%8C%E3%82%8B/
	 *
	 * @param filePath
	 *            対象ファイルパス
	 * @return 読み込んだバイト配列
	 * @throws Exception
	 *             ファイルが見つからない、アクセスできないときなど
	 */
	public static byte[] readFileToByte(String filePath) {
		byte[] b = new byte[1];
		try {
			FileInputStream fis = new FileInputStream(filePath);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (fis.read(b) > 0) {
				baos.write(b);
			}
			baos.close();
			fis.close();
			b = baos.toByteArray();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return b;
	}

}
