package com.legendapl.lightning.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleStringProperty;

/**
 * Excelジョブを表示するテーブルビュー(JFXTreeTableView)のモデルクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
public class ExcelDefinitionTableRecord extends RecursiveTreeObject<ExcelDefinitionTableRecord> {

	/** 項番 */
	private SimpleStringProperty number = new SimpleStringProperty("");
	/** レポートURI */
	private SimpleStringProperty reportUri = new SimpleStringProperty("");
	/** レポートURI */
	private SimpleStringProperty reportLabel = new SimpleStringProperty("");
	/** セル */
	private SimpleStringProperty cell = new SimpleStringProperty("");
	/** シート */
	private SimpleStringProperty sheet = new SimpleStringProperty("");
	/** 選択したカラム */
	private SimpleStringProperty targetColumns = new SimpleStringProperty("");
	/** 状態 */
	private SimpleStringProperty progress = new SimpleStringProperty("");

	public SimpleStringProperty getReportUri() {
		return reportUri;
	}

	public void setReportUri(String reportUri) {
		this.reportUri.set(reportUri);
	}

	public SimpleStringProperty getCell() {
		return cell;
	}

	public void setCell(String cell) {
		this.cell.set(cell);
	}

	public SimpleStringProperty getSheet() {
		return sheet;
	}

	public void setSheet(String sheet) {
		this.sheet.set(sheet);
	}

	public SimpleStringProperty getTargetColumns() {
		return targetColumns;
	}

	public void setTargetColumns(String targetColumns) {
		this.targetColumns.set(targetColumns);
	}

	public SimpleStringProperty getProgress() {
		return progress;
	}

	public void setProgress(String progress) {
		this.progress.set(progress);
	}

	public SimpleStringProperty getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number.set(number);
	}

	public SimpleStringProperty getReportLabel() {
		return reportLabel;
	}

	public void setReportLabel(String reportLabel) {
		this.reportLabel.set(reportLabel);
	}

}
