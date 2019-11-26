package com.legendapl.lightning.common.constants;

import java.util.ResourceBundle;

import com.legendapl.lightning.model.ReportError;

public final class ReportErrors {
	
	/**
	 *  多言語対応ため
	 */
	private static ResourceBundle mybundle = ResourceBundle.getBundle("MyBundle");
	private static ResourceBundle messages = ResourceBundle.getBundle("messages_ja");
	public static void changeLanguage(String language) {
		// TODO
		if (language.equals("日本語") || language.startsWith("ja")) {
			mybundle = ResourceBundle.getBundle("MyBundle");
			messages = ResourceBundle.getBundle("messages_ja");
		} else {
			mybundle = ResourceBundle.getBundle("MyBundle_en");
			messages = ResourceBundle.getBundle("messages_en");
		}
		changeLanguage();
	}
	
	public static ReportError ERROR_EXPORT_IN_USE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.EXPORT"),
															 		messages.getString("ERROR_W04_01_TITLE"),
															 		messages.getString("ERROR_W04_01_TXT"));
	
	public static ReportError ERROR_DATASOURCE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.DATASOURCE"),
															     messages.getString("ERROR_W04_02_TITLE"),
																 messages.getString("ERROR_W04_02_TXT"));
	
	public static ReportError ERROR_UNKNOW = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.UNKOWN"),
		     												 messages.getString("ERROR_W04_03_TITLE"),
		     												 messages.getString("ERROR_W04_03_TXT"));
	
	public static ReportError ERROR_EXECUTE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.EXECUTE"),
			 												  messages.getString("ERROR_W04_04_TITLE"),
			 												  messages.getString("ERROR_W04_04_TXT"));
	
	public static ReportError ERROR_SERVER = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.SERVER"),
			  												 messages.getString("ERROR_W04_05_TITLE"),
			  												 messages.getString("ERROR_W04_05_TXT"));
	
	public static ReportError ERROR_LICENSE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.LICENSE"),
															  messages.getString("ERROR_W04_06_TITLE"),
															  messages.getString("ERROR_W04_06_TXT"));
	
	/**
	 *  多言語対応ため
	 */
	private static void changeLanguage() {
		
		ERROR_EXPORT_IN_USE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.EXPORT"),
				 							  messages.getString("ERROR_W04_01_TITLE"),
				 							  messages.getString("ERROR_W04_01_TXT"));
		
		ERROR_DATASOURCE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.DATASOURCE"),
				  						   messages.getString("ERROR_W04_02_TITLE"),
				  						   messages.getString("ERROR_W04_02_TXT"));
		
		ERROR_UNKNOW = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.UNKOWN"),
				 					   messages.getString("ERROR_W04_03_TITLE"),
				 					   messages.getString("ERROR_W04_03_TXT"));
		
		ERROR_EXECUTE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.EXECUTE"),
				  					  	messages.getString("ERROR_W04_04_TITLE"),
				  					  	messages.getString("ERROR_W04_04_TXT"));
		
		ERROR_SERVER = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.SERVER"),
									   messages.getString("ERROR_W04_05_TITLE"),
									   messages.getString("ERROR_W04_05_TXT"));
		
		ERROR_LICENSE = new ReportError(mybundle.getString("W04.ERROR.LOG.TYPE.LICENSE"),
				  						messages.getString("ERROR_W04_06_TITLE"),
				  						messages.getString("ERROR_W04_06_TXT"));
	}
}
