package com.legendapl.lightning.service;

import java.io.File;
import java.util.HashMap;

import com.legendapl.lightning.model.ExcelDefinition;

import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;

/**
 * レポート実行処理を行うインターフェース
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public interface ReportCreateService {

	/**
	 * レポート実行を行うメソッド
	 * 
	 * @param url
	 * @param user
	 * @param password
	 * @param jrxmlPath
	 * @param exportFile
	 * @param params
	 * @param excelPasteFlag
	 * @return
	 */
	public HashMap<JasperPrint, JRSwapFileVirtualizer> runReport(String url, String user, String password,
			String jrxmlPath, File exportFile, HashMap<String, Object> params, ExcelDefinition excelDefinition);

}
