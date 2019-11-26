package com.legendapl.lightning.service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.legendapl.lightning.common.constants.ReportErrors;
import com.legendapl.lightning.model.ReportError;

/**
 * レポート実行画面で、エラー発生時エラー内容について表示するため
 * 
 * @author panyuan
 *
 */
public class ReportErrorService {
	
	protected Logger logger = Logger.getLogger(getClass());
	
	protected ResourceBundle mybundle = ResourceBundle.getBundle("MyBundle");
	
	protected List<ReportError> errorList;
	
	public ReportErrorService() {
		errorList = new ArrayList<ReportError>();
	}
	
	/**
	 * エラーログはnull場合は初期化する<br>
	 * さもないと、クリアーする
	 */
	public void clearErrorLog() {
		if (errorList == null) {
			errorList = new ArrayList<ReportError>();
		} else {
			errorList.clear();
		}
	}
	
	/**
	 * エラーログにエラーがあります
	 * 
	 * @return true/false
	 */
	public boolean hasError() {
		return errorList != null && !errorList.isEmpty();
	}
	
	/**
	 * エラーログを取る
	 * 
	 * @return List
	 */
	public List<ReportError> getErrorLog() {
		return errorList;
	}
	
	/**
	 * エラーログを追加
	 * 
	 * @param errorList
	 */
	public void addErrorLog(List<ReportError> errorList) {
		if (errorList != null) {
			this.errorList.addAll(errorList);
		}
	}
	
	/**
	 * エラーを判断して、エラーログを追加<br>
	 * <br>
	 * エラーにはいくつかの種類があります:<br>
	 * 1.&emsp;ライセンスエラー<br>
	 * 2.&emsp;エクスポートエラー<br>
	 * 3.&emsp;データベースエラー<br>
	 * 4.&emsp;サーバエラー<br>
	 * 5.&emsp;実行エラー<br>
	 * 6.&emsp;不明なエラー<br>
	 * ....<br>
	 * 
	 * @param e
	 */
	public void addError(Throwable e) {
		
		try {
			// e は null 場合、不明なエラー
			if (e == null) {
				errorList.add(ReportErrors.ERROR_UNKNOW);
				return;
			}
			
			boolean unknow = true;
			
			// ライセンスエラー
			if (e.getCause() instanceof com.jaspersoft.jasperreports.license.LicenseException) {
				addLicenseError(e.getCause());
				unknow = false;
				return;
			} else if (e.getCause()!=null && e.getCause().getCause() instanceof com.jaspersoft.jasperreports.license.LicenseException) {
				addLicenseError(e.getCause().getCause());
				unknow = false;
				return;
			}
			
			// エクスポートエラー
			if (e.getCause() instanceof java.io.FileNotFoundException) {
				errorList.add(ReportErrors.ERROR_EXPORT_IN_USE);
				unknow = false;
			}
			
			// データベースエラー	
			if (e.getCause() instanceof java.sql.SQLException ||
				e.getCause() instanceof org.mariadb.jdbc.internal.common.QueryException) {
				errorList.add(ReportErrors.ERROR_DATASOURCE);
				unknow = false;
			}
			
			// サーバエラー
			if (e.getCause() instanceof java.net.ConnectException ||
				e.getCause() instanceof java.net.SocketException) {
				errorList.add(ReportErrors.ERROR_SERVER);
				unknow = false;
			}
			
			// 実行エラー
			if (e.getCause() instanceof net.sf.jasperreports.engine.JRRuntimeException ||
				e.getCause() instanceof net.sf.jasperreports.engine.JRException ||
				e instanceof com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException ) {
				errorList.add(ReportErrors.ERROR_EXECUTE);
				unknow = false;
			}
			
			// 不明なエラー
			if (unknow) {
				errorList.add(ReportErrors.ERROR_UNKNOW);
			}
			
		} catch (Throwable e2) {
			logger.error("addError failed.");
			logger.error(e2.getMessage(), e2);
		}
	}
	
	/**
	 * ライセンスエラーを追加
	 * 
	 * @param e
	 */
	protected void addLicenseError(Throwable e) {
		
		try {
			/**
			 * Message examples:
			 * License not found at D:\svn\98.Lightning\src\Lightning\jasperserver.license.
			 * License not found at D:\software\20171220\1544\LaKeel BI Desktop\app\jasperserver.license
			 * License at D:\svn\98.Lightning\src\Lightning\jasperserver.license is corrupted.
			 */
			
			String message = e.getMessage();
			if (message.startsWith("License not found at ")) {
				message = message.replaceAll("License not found at ", "");
			} else if (message.startsWith("License at ")) {
				message = message.replaceAll("License at ", "");
			} else {
				logger.warn("Message replace failed for LicenseException");
			}
				
			if (message.contains(".license")) {
				message = message.substring(0, message.indexOf(".license")+".license".length());
			} else {
				logger.warn("Message not contain \".license\"");
			}
				
			String [] paths = message.split(Pattern.quote("\\"));
			String filename = paths[paths.length-1];
			String path = message.replaceAll(Pattern.quote("\\"+filename), "");
			
			logger.debug("message: " + e.getMessage());
			logger.debug("path: " + path);
			logger.debug("filename: " + filename);
			
			ReportError newReportError = new ReportError(ReportErrors.ERROR_LICENSE);
			String newTxt = MessageFormat.format(newReportError.getTxt(), filename, path);
			newReportError.setTxt(newTxt);
			
			errorList.add(newReportError);
			
		} catch (Throwable e2) {
			logger.error("Add license-error failed.");
			logger.error(e2.getMessage(), e2);
		}
	}
	
	/**
	 * 次のフォーマットようにエラーログを取る：<br>
	 * <br>
	 * エクスポートエラー<br>
	 * &emsp;説明<br>
	 * &emsp;&emsp;エクスポートファイルは別のプロセスが使用中です。<br>
	 * &emsp;問題解決策<br>
	 * &emsp;&emsp;・&emsp;該当ファイルを閉じた後、再度エクスポートして下さい。<br>
	 * 
	 * @return String
	 */
	public String getErrorLogWithFormat() {
		
		removeRepeatElements();
		
		String errorLog = new String();
		
		for (int i = 0; i < errorList.size(); i++) {
			
			ReportError error = errorList.get(i); 
			errorLog += error.getType()+"\n"+
						"    "+mybundle.getString("W04.ERROR.LOG.TITLE")+"\n"+
						"    "+"    "+error.getTitle()+"\n"+
						"    "+mybundle.getString("W04.ERROR.LOG.TXT")+"\n";
			
			String txts[] = error.getTxt().split(Pattern.quote("\n"));
			for (String txt : txts) {
				errorLog += "    "+"    ・ "+txt+"\n";
			}
			
			if (i+1 != errorList.size()) {
				errorLog += "\n\n";
			}
		}
		
		return errorLog;
	}
	
	/**
	 * 同じエラーを削除
	 */
	private void removeRepeatElements() {
		for (int i = 0; i < errorList.size(); i++) {
			for (int j = i+1; j < errorList.size(); ) {
				if (errorList.get(i).equals(errorList.get(j))) {
					errorList.remove(j);
				} else {
					j++;
				}
			}
		}
	}
}
