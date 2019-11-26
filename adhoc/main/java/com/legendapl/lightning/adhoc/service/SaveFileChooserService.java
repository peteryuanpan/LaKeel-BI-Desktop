package com.legendapl.lightning.adhoc.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.common.AdhocUtils;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Stage;

/**
 * 保存するサービス
 * 
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.23
 */
public abstract class SaveFileChooserService {

	protected Logger logger = Logger.getLogger(getClass());
	
	protected FileChooser fileChooser;
	protected Property property;
	
	public SaveFileChooserService() {
		this.property = new Property();
	}
	
	protected void initFileChooser() {
		
		this.fileChooser = new FileChooser();
		fileChooser.setTitle(getProperty().getTitle());
		fileChooser.getExtensionFilters().clear();
		
		this.getProperty().getFilterTypes().forEach(filterType -> {
			ExtensionFilter extensionFilter = new ExtensionFilter(filterType.getDescription(), "*" + filterType.getExtension());
			fileChooser.getExtensionFilters().add(extensionFilter);
		});
		
		String initPath = this.getProperty().getInitialDirectoryPath();
		if (null != initPath) {
			File file = new File(initPath);
			if (!file.exists()) file.mkdirs();
			fileChooser.setInitialDirectory(file);
		}
		
		String initName = this.getProperty().getInitialFileName();
		if (null != initName) {
			fileChooser.setInitialFileName(initName);
		}
	}
	
	/**
	 * ファイルチューザーで保存する
	 */
	public boolean save(Stage stage) {
		
		// ファイルセレクタを取得する
		initFileChooser();
		
		// エクスポートファイルを取得する
		File outputFile = null;
		try {
			outputFile = fileChooser.showSaveDialog(stage);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showErrorNotInBack(AdhocUtils.getString("ERROR_SAVE_TITLE"), AdhocUtils.getString("ERROR_SAVE_SELECT_PATH"));
			save(stage);
			return false;
		}
		
		// 保存をキャンセル場合
		if (null == outputFile) {
			logger.info("Save canceled.");
			return false;
		}
		
		//　customer
		try {
			doCustomerCheck(outputFile);
		} catch (Exception e) {
			logger.error(e.getMessage());
			AlertWindowService.showErrorNotInBack(AdhocUtils.getString("ERROR_SAVE_TITLE"), e.getMessage());
			save(stage);
			return false;
		}
		
		// 保存
		if (!save(outputFile)) {
			return save(stage);
		};
		return true;
	}
	
	/**
	 * カスタマイズしたチェックするプロセスです。
	 * @param outputFile
	 * @throws Exception
	 */
	abstract protected void doCustomerCheck(File outputFile) throws Exception;
	
	/**
	 * ファイルチューザーがないで保存する
	 * @param outputFile
	 */
	public boolean save(File outputFile) {
		
		// エクスポートファイルパスを出力する
		logger.info("Choosen file: " + outputFile.getAbsolutePath());
		
		// 保存パスが不正場合
		if (null != getProperty().getStartWithPath()) {
			File rootfolder = new File(getProperty().getStartWithPath());
			if (!rootfolder.exists()) {
				rootfolder.mkdirs();
			}
			if (!outputFile.getParentFile().getAbsolutePath().startsWith(rootfolder.getAbsolutePath())) {
				String detailErrors = AdhocUtils.format(AdhocUtils.getString("ERROR_SAVE_ILLEGAL_PATH"), rootfolder.getAbsolutePath());
				logger.error(detailErrors);
				AlertWindowService.showErrorNotInBack(AdhocUtils.getString("ERROR_SAVE_TITLE"), detailErrors);
				return false;
			}
		}
		
		// データをエクスポートする
		try {
			if (!outputFile.exists()) {
				new File(outputFile.getParent()).mkdirs();
			}
			doSave(outputFile);
		}  catch(Exception e) {
			doFailed(outputFile, e);
			return false;
		}
		
		// エクスポートに成功しました
		doSuccess(outputFile);
		return true;
	}
	
	/**
	 * ファイルを保存するプロセスです。
	 * @param outputFile
	 * @throws Exception
	 */
	abstract protected void doSave(File outputFile) throws Exception;
	
	/**
	 * ファイルを保存に失敗後でのプロセスです。
	 * @param outputFile
	 */
	abstract protected void doFailed(File outputFile, Exception e);
	
	/**
	 * ファイルを保存に成功後でのプロセスです。
	 * @param outputFile
	 */
	abstract protected void doSuccess(File outputFile);
	
	/**
	 * 拡張子のタイプです。(.xml, .xlsx, .pdf, ...)
	 */
	public static enum ExtensionFilterType {
		XML("XML Files", ".xml"),
		XLSX("XLSX Files", ".xlsx"),
		PDF("PDF Files", ".pdf")
		// TODO
		;
		private String description;
		private String extension;
		
		private ExtensionFilterType(String description, String extension) {
			this.description = description;
			this.extension = extension;
		}
		public String getDescription() {
			return description;
		}
		public String getExtension() {
			return extension;
		}
	}
	
	/**
	 * プロパティーセッターです。
	 */
	public class Property {
		private String title;
		private List<ExtensionFilterType> filterTypes;
		private String initialDirectoryPath;
		private String initialFileName;
		private String startWithPath;
		public Property() {
			this.filterTypes = new ArrayList<>();
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public List<ExtensionFilterType> getFilterTypes() {
			return filterTypes;
		}
		public void setFilterTypes(List<ExtensionFilterType> filterTypes) {
			this.filterTypes = filterTypes;
		}
		public String getInitialDirectoryPath() {
			return initialDirectoryPath;
		}
		public void setInitialDirectoryPath(String initialDirectoryPath) {
			this.initialDirectoryPath = initialDirectoryPath;
		}
		public String getInitialFileName() {
			return initialFileName;
		}
		public void setInitialFileName(String initialFileName) {
			this.initialFileName = initialFileName;
		}
		public String getStartWithPath() {
			return startWithPath;
		}
		public void setStartWithPath(String startWithPath) {
			this.startWithPath = startWithPath;
		}
	}

	public Property getProperty() {
		if (null == property) {
			this.property = new Property();
		}
		return property;
	}
	
	/**
	 * get real file name (excluding ExtensionFilterType)
	 * @param fileName
	 * @param filterType
	 * @return
	 */
	public static String getRealFileName(String fileName, ExtensionFilterType filterType) {
		if (null != fileName && null != filterType) {
			String extension = filterType.getExtension();
			if (fileName.endsWith(extension)) {
				fileName = fileName.substring(0, fileName.length() - extension.length());
			}
		}
		return fileName;
	}
	
}
