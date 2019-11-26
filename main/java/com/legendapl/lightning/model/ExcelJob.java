package com.legendapl.lightning.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Excel貼り付け定義モデル(複数)
 * 
 * @author Legend Applications, LaKeel BI development team.
 */
@XmlRootElement(name = "excelJob")
@XmlAccessorType(XmlAccessType.FIELD)
public class ExcelJob {

	public ExcelJob() {
	}

	/**
	 * パラメータを保存していない帳票のジョブの実行時に利用するコンストラクタ<br>
	 * 
	 */
	public ExcelJob(ExcelJob excelJob, ExcelDefinition excelDefinition) {
		this.excelDefinitionList = new ArrayList<ExcelDefinition>();
		this.excelDefinitionList.add(excelDefinition);
		this.excelPath = excelJob.getExcelPath();
		this.jobPath = excelJob.getJobPath();
		this.rewriteFlag = excelJob.isRewriteFlag();
		this.saveExcelPath = excelJob.getSaveExcelPath();
	}

	/** Excel貼り付け定義モデル(単体)のリスト */
	@XmlElementWrapper(name = "excelDefinitionList")
	@XmlElement(name = "excelDefinition")
	private List<ExcelDefinition> excelDefinitionList;

	/** Excelファイルのフルパス */
	@XmlElement(name = "excelPath")
	private String excelPath;

	/** 上書きフラグ */
	@XmlElement(name = "rewriteFlag")
	private boolean rewriteFlag;

	/** 保存先のExcelパス */
	@XmlElement(name = "saveExcelPath")
	private String saveExcelPath;

	/** ジョブのフルパス */
	@XmlElement(name = "jobPath", required = true)
	private String jobPath;

	public List<ExcelDefinition> getExcelDefinitionList() {
		return this.excelDefinitionList;
	}

	public void setExcelDefinitionList(List<ExcelDefinition> excelDefinitionList) {
		this.excelDefinitionList = excelDefinitionList;
	}

	public String getExcelPath() {
		return excelPath;
	}

	public void setExcelPath(String excelPath) {
		this.excelPath = excelPath;
	}

	public boolean isRewriteFlag() {
		return rewriteFlag;
	}

	public void setRewriteFlag(boolean rewriteFlag) {
		this.rewriteFlag = rewriteFlag;
	}

	public String getSaveExcelPath() {
		return saveExcelPath;
	}

	public void setSaveExcelPath(String saveExcelPath) {
		this.saveExcelPath = saveExcelPath;
	}

	public String getJobPath() {
		return jobPath;
	}

	public void setJobPath(String jobPath) {
		this.jobPath = jobPath;
	}

}
