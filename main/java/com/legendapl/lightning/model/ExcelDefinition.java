package com.legendapl.lightning.model;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * Excel貼り付け定義モデル(単体)
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class ExcelDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	/** レポートURI */
	@XmlElement(name = "reportUri")
	private String reportUri;

	/** レポート名 */
	@XmlElement(name = "reportName")
	private String reportName;

	/** シート */
	@XmlElement(name = "sheet")
	private String sheet;

	/** セル */
	@XmlElement(name = "cell")
	private String cell;

	/** 貼り付ける列一覧(カンマ区切り) */
	@XmlElement(name = "targetColumns")
	private String targetColumns;

	/** 列ヘッダのフラグ */
	@XmlElement(name = "columnHeaderFlag")
	private boolean columnHeaderFlag;

	/** 実行モードのフラグ */
	@XmlElement(name = "LowMemoryFlag")
	private boolean LowMemoryFlag;

	/** セルのプレビュー選択フラグ */
	@XmlElement(name = "viewSelectCellFlag")
	private boolean viewSelectCellFlag;

	/** セルの入力フラグ */
	@XmlElement(name = "inputCellFlag")
	private boolean inputCellFlag;

	/** パラメーターのファイル名 */
	@XmlElement(name = "parameterFileName", required = true)
	private String parameterFileName;

	public String getReportUri() {
		return reportUri;
	}

	public void setReportUri(String reportUri) {
		this.reportUri = reportUri;
	}

	public String getSheet() {
		return sheet;
	}

	public void setSheet(String sheet) {
		this.sheet = sheet;
	}

	public String getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell = cell;
	}

	public String getTargetColumns() {
		return targetColumns;
	}

	public void setTargetColumns(String targetColumns) {
		this.targetColumns = targetColumns;
	}

	public boolean isColumnHeaderFlag() {
		return columnHeaderFlag;
	}

	public void setColumnHeaderFlag(boolean columnHeaderFlag) {
		this.columnHeaderFlag = columnHeaderFlag;
	}

	public boolean isViewSelectCellFlag() {
		return viewSelectCellFlag;
	}

	public void setViewSelectCellFlag(boolean viewSelectCellFlag) {
		this.viewSelectCellFlag = viewSelectCellFlag;
	}

	public boolean isInputCellFlag() {
		return inputCellFlag;
	}

	public void setInputCellFlag(boolean inputCellFlag) {
		this.inputCellFlag = inputCellFlag;
	}

	public String getParameterFileName() {
		return parameterFileName;
	}

	public void setParameterFileName(String parameterFileName) {
		this.parameterFileName = parameterFileName;
	}

	public String getReportName() {
		return reportName;
	}

	public void setReportName(String reportName) {
		this.reportName = reportName;
	}

	public boolean isLowMemoryFlag() {
		return LowMemoryFlag;
	}

	public void setLowMemoryFlag(boolean lowMemoryFlag) {
		LowMemoryFlag = lowMemoryFlag;
	}

}
