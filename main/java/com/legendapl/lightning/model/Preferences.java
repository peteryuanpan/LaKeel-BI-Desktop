package com.legendapl.lightning.model;

import javax.swing.filechooser.FileSystemView;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 設定格納モデル
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "preferences")
public class Preferences {

	/** タイトル */
	@XmlElement(name = "title", required = true)
	private String title = "Preference";

	/** フォントファミリー */
	@XmlElement(name = "fontFamily")
	// TODO:ENUM化
	private String fontFamily;

	/** フォントのサイズ */
	@XmlElement(name = "fontSize")
	private String fontSize;

	/** テーマカラー */
	@XmlElement(name = "ThemeColor")
	// TODO:ENUM化
	private String themeColor;

	/** 利用言語 */
	@XmlElement(name = "locale", required = true)
	// TODO:ENUM化
	private String locale = "";

	/** データソースの編集可否 */
	@XmlElement(name = "datasourceEditable")
	private Boolean datasourceEditable;

	/** ワークスペースフォルダ */
	@XmlElement(name = "workSpaceFolderPath", required = true)
	private String workSpaceFolderPath = System.getProperty("user.dir").replace("\\", "/") + "/work/";

	/** 標準エクスポート先フォルダ */
	@XmlElement(name = "defaultExportFolderPath") // 省略可能
	private String defaultExportFolderPath = FileSystemView.getFileSystemView().getDefaultDirectory().toString()
			.replace("\\", "/") + "/";

	/** メインウィンドウの高さ */
	@XmlElement(name = "mainWindowHeight")
	private String mainWindowHeight = "500.0";

	/** メインウィンドウの幅 */
	@XmlElement(name = "mainWindowWidth")
	private String mainWindowWidth = "800.0";

	/** メインウィンドウのX座標 */
	@XmlElement(name = "mainWindowX")
	private String mainWindowX = null;

	/** メインウィンドウのY座標 */
	@XmlElement(name = "mainWindowY")
	private String mainWindowY = null;

	/** レポート実行画面の高さ */
	@XmlElement(name = "reportWindowHeight")
	private String reportWindowHeight = "500.0";

	/** レポート実行画面の幅 */
	@XmlElement(name = "reportWindowWidth")
	private String reportWindowWidth = "800.0";

	/** レポート実行画面のX座標 */
	@XmlElement(name = "reportWindowX")
	private String reportWindowX = null;

	/** レポート実行画面のY座標 */
	@XmlElement(name = "reportWindoY")
	private String reportWindowY = null;

	/** レポート実行画面のデバイダーの位置 */
	// @XmlElement(name = "inputControlDivider")
	private String inputControlDivider = "0.35";

	/** レポート実行画面のデバイダーの位置(パラメータ無し) */
	// @XmlElement(name = "nonInputControlDivider")
	private String nonInputControlDivider = "0.2";

	/** データソース管理画面の高さ */
	@XmlElement(name = "dataSourceWindowHeight")
	private String dataSourceWindowHeight = "500.0";

	/** データソース管理画面の幅 */
	@XmlElement(name = "dataSourceWindowWidth")
	private String dataSourceWindowWidth = "800.0";

	/** データソース管理画面のX座標 */
	@XmlElement(name = "dataSourceWindowX")
	private String dataSourceWindowX = null;

	/** データソース管理画面のY座標 */
	@XmlElement(name = "dataSourceWindowY")
	private String dataSourceWindowY = null;

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getFontFamily() {
		return fontFamily;
	}

	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}

	public String getFontSize() {
		return fontSize;
	}

	public void setFontSize(String fontSize) {
		this.fontSize = fontSize;
	}

	public String getThemeColor() {
		return themeColor;
	}

	public void setThemeColor(String themeColor) {
		this.themeColor = themeColor;
	}

	public String getLocale() {
		return locale;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public String getWorkSpaceFolderPath() {
		return workSpaceFolderPath;
	}

	public void setWorkSpaceFolderPath(String workSpaceFolderPath) {
		this.workSpaceFolderPath = workSpaceFolderPath;
	}

	public String getDefaultExportFolderPath() {
		return defaultExportFolderPath;
	}

	public void setDefaultExportFolderPath(String defaultExportFolderPath) {
		this.defaultExportFolderPath = defaultExportFolderPath;
	}

	@Override
	public String toString() {
		return "Preferences [title=" + title + ", fontFamily=" + fontFamily + ", fontSize=" + fontSize + ", themeColor="
				+ themeColor + ", locale=" + locale + ", workSpaceFolderPath=" + workSpaceFolderPath
				+ ", defaultExportFolderPath=" + defaultExportFolderPath + "]";
	}

	public String getMainWindowHeight() {
		return mainWindowHeight;
	}

	public void setMainWindowHeight(String mainWindowHeight) {
		this.mainWindowHeight = mainWindowHeight;
	}

	public String getMainWindowWidth() {
		return mainWindowWidth;
	}

	public void setMainWindowWidth(String mainWindowWidth) {
		this.mainWindowWidth = mainWindowWidth;
	}

	public String getReportWindowHeight() {
		return reportWindowHeight;
	}

	public void setReportWindowHeight(String reportWindowHeight) {
		this.reportWindowHeight = reportWindowHeight;
	}

	public String getReportWindowWidth() {
		return reportWindowWidth;
	}

	public void setReportWindowWidth(String reportWindowWidth) {
		this.reportWindowWidth = reportWindowWidth;
	}

	public String getDataSourceWindowHeight() {
		return dataSourceWindowHeight;
	}

	public void setDataSourceWindowHeight(String dataSourceWindowHeight) {
		this.dataSourceWindowHeight = dataSourceWindowHeight;
	}

	public String getDataSourceWindowWidth() {
		return dataSourceWindowWidth;
	}

	public void setDataSourceWindowWidth(String dataSourceWindowWidth) {
		this.dataSourceWindowWidth = dataSourceWindowWidth;
	}

	public String getMainWindowX() {
		return mainWindowX;
	}

	public void setMainWindowX(String mainWindowX) {
		this.mainWindowX = mainWindowX;
	}

	public String getMainWindowY() {
		return mainWindowY;
	}

	public void setMainWindowY(String mainWindowY) {
		this.mainWindowY = mainWindowY;
	}

	public String getReportWindowX() {
		return reportWindowX;
	}

	public void setReportWindowX(String reportWindowX) {
		this.reportWindowX = reportWindowX;
	}

	public String getReportWindowY() {
		return reportWindowY;
	}

	public void setReportWindowY(String reportWindowY) {
		this.reportWindowY = reportWindowY;
	}

	public String getDataSourceWindowX() {
		return dataSourceWindowX;
	}

	public void setDataSourceWindowX(String dataSourceWindowX) {
		this.dataSourceWindowX = dataSourceWindowX;
	}

	public String getDataSourceWindowY() {
		return dataSourceWindowY;
	}

	public void setDataSourceWindowY(String dataSourceWindowY) {
		this.dataSourceWindowY = dataSourceWindowY;
	}

	public String getInputControlDivider() {
		return inputControlDivider;
	}

	public void setInputControlDivider(String inputControlDivider) {
		this.inputControlDivider = inputControlDivider;
	}

	public String getNonInputControlDivider() {
		return nonInputControlDivider;
	}

	public void setNonInputControlDivider(String nonInputControlDivider) {
		this.nonInputControlDivider = nonInputControlDivider;
	}

	public Boolean getDatasourceEditable() {
		return datasourceEditable;
	}

	public void setDatasourceEditable(Boolean datasourceEditable) {
		this.datasourceEditable = datasourceEditable;
	}

}
