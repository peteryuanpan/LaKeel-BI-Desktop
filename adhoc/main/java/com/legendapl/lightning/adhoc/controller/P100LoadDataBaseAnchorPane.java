package com.legendapl.lightning.adhoc.controller;

import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.BackRunService;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.ModelType;
import com.legendapl.lightning.adhoc.model.DatabaseInfo;
import com.legendapl.lightning.adhoc.model.FolderResource;
import com.legendapl.lightning.adhoc.model.LoadDataRow;
import com.legendapl.lightning.tools.model.BaseModel;
import com.legendapl.lightning.tools.model.StringBase;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

/**
 * データを読み込む画面の共通コントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public abstract class P100LoadDataBaseAnchorPane extends C100AdhocMenuPane {

	@FXML
	protected AnchorPane anchorPane;
	@FXML
	protected StackPane spinnerPane;
	@FXML
	protected TreeView<FolderResource> folderTree;
	@FXML
	protected TreeView<FolderResource> fileList;
	@FXML
	private TreeTableView<LoadDataRow> treeTableView;
	@FXML
	protected Label fileLabel;
	@FXML
	protected Button refresh;
	@FXML
	protected Button cutover;

	protected Boolean turnTree = true;
	protected Boolean backRun = false;
	
	protected static final MaterialDesignIcon folderIcon = MaterialDesignIcon.FOLDER_OUTLINE;
	protected static final FontAwesomeIcon folderExpandedIcon = FontAwesomeIcon.FOLDER_OPEN_ALT;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
	}
	
	/**
	 * リフレッシュボタンのイベント
	 * @param event
	 */
	public void refreshFired(ActionEvent event) {
		logger.debug("Refresh: Loading list and tree folder.");
		loadListAndTree();
	}
	
	/**
	 * リフレッシュボタンの実装
	 */
	abstract protected void loadListAndTree();
	
	/**
	 * 小文字の辞書順でラベルを並べ替える
	 * 
	 * @param root
	 */
	protected void sortChildrenByLabel(TreeItem<FolderResource> root) {
		for (TreeItem<FolderResource> child : root.getChildren()) {
			sortChildrenByLabel(child);
		}
		Collections.sort(root.getChildren(), new Comparator<TreeItem<FolderResource>>() {
		    @Override public int compare(TreeItem<FolderResource> a, TreeItem<FolderResource> b) {
		        return a.getValue().getLabel().toLowerCase().compareTo(b.getValue().getLabel().toLowerCase());
		    }
		});
	}
	
	/**
	 * 小文字の辞書順でラベルを並べ替える
	 * 
	 * @param list
	 */
	protected void sortListByLabel(List<FolderResource> list) {
		Collections.sort(list, new Comparator<FolderResource>() {
		    @Override public int compare(FolderResource a, FolderResource b) {
		        return a.getLabel().toLowerCase().compareTo(b.getLabel().toLowerCase());
		    }
		});
	}
	
	/**
	 * <br>バックで実行する(実行中のメッセージ表示)</br>
	 * </br>
	 * 例:
	 * <pre>
	 *     backW.run(() -> {
	 *         doSomething();
	 *     });
	 * </pre>
	 */
	final protected BackRunService backW = new BackRunService(
			() -> setBackRunFlag(true),
			() -> setBackRunFlag(false)
	);

	
	/**
	 * フラグbackRunを設定
	 * @param flag
	 */
	protected void setBackRunFlag(boolean flag) {
		logger.debug("Set flag backRun : " + flag);

		backRun = flag;
		Platform.runLater(() -> {
			spinnerPane.setVisible(backRun);
		});
	}

	/**
	 * 切り替えボタンのイベント
	 * 
	 * @param event
	 */
	public void cutoverFired(ActionEvent event) {
		Platform.runLater(() -> {
			turnTree = !turnTree;
			if (turnTree) { // tree
				folderTree.setVisible(true);
				fileList.setVisible(false);
				fileLabel.setText(AdhocUtils.getString("P100.button.folder"));
				treeTableView.getSelectionModel().clearSelection();
			} else { // list
				folderTree.setVisible(false);
				fileList.setVisible(true);
				fileLabel.setText(AdhocUtils.getString("P100.button.list"));
				treeTableView.getSelectionModel().clearSelection();
			}
		});
	}
	
	/**
	 * データベースをチェク
	 * @param func
	 * @return
	 * @throws Exception
	 */
	protected DatabaseInfo getDatabase(GetDatabaseFunc func) throws Exception {
		String uri = "";
		DatabaseInfo database = null;
		try {
			database = func.get();
			uri = database.getLocalFileUri();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
					AdhocUtils.getString("ERROR_READ_DATABASE_FILE_NOT_EXSIT"), uri);
			throw new Exception(e);
		} catch (PasswordNullEmptyException e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
					AdhocUtils.getString("ERROR_READ_DATABASE_PASSWORLD_NULL_EMPTY"), uri);
			throw new Exception(e);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"),
					AdhocUtils.getString("ERROR_READ_GET_DATABASE_FROM_LOCAL"));
			throw new Exception(e);
		}
		return database;
	}
	
	/**
	 * データベースを取得
	 */
	interface GetDatabaseFunc {
		DatabaseInfo get() throws Exception;
	}
	
	/**
	 * テーブルにデータを設置します。
	 *
	 * @param dataList データリスト
	 * @param columns　TreeTableColumnリスト
	 * @param functions 値を取得するメソッド名リスト
	 * @param table TreeTableView
	 */
	protected <T extends BaseModel<T>> void showData(
			ObservableList<T> dataList,
			List<TreeTableColumn<T, String>> columns,
			List<String> functions,
			TreeTableView<T> table) {
		
		for (int i = 0; i < columns.size(); i++) {
			
			TreeTableColumn<T, String> column = columns.get(i);
			String function = functions.get(i);
			
			column.setCellFactory(col -> new LoadDataTreeTableCell<T>(function));
		}

		Platform.runLater(() -> {
			if (!columns.equals(table.getColumns())) {
				table.getColumns().setAll(columns);
			}
			columns.forEach(column -> {
				column.setContextMenu(null);
			});
			TreeItem<T> root = new RecursiveTreeItem<>(dataList, RecursiveTreeObject::getChildren);
			table.setShowRoot(false);
			table.setEditable(true);
			table.setRoot(root);
		});
	}
	
	/**
	 * Load Data Tree Table Cell
	 * @param <T>
	 */
	public class LoadDataTreeTableCell<T> extends TreeTableCell<T, String> {
		
		private String function;
		
		public LoadDataTreeTableCell(String function) {
			this.function = function;
		}
		
		@Override
		public void updateItem(String item, boolean empty) {
			
			super.updateItem(item, empty);
			
			String text = null;
			try {
				T cell = getCell(getIndex());
				if (cell != null) {
					Method method = cell.getClass().getMethod(function, new Class[0]);
					StringBase cellData = (StringBase) method.invoke(cell, new Object[0]);
					text = cellData.get();
				}
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
			this.setText(text);
		}
		
		T getCell(Integer index) {
			
			TreeItem<T> root = this.getTreeTableView().getRoot();
			if (null != root) {
				if (index >= 0 && index < root.getChildren().size()) {
					
					TreeItem<T> child = root.getChildren().get(index);
					if (null != child) {
						return child.getValue();
					}
				}
			}
			return null;
		}
	}

	/**
	 * トピック画面のタイトルを取得
	 * 
	 * @param windowId
	 * @return String
	 */
	protected String getTopicTitle(String windowId, String fileName) {
		return AdhocUtils.getString(windowId) + " (" + fileName + ")";
	}
	
	/**
	 * 
	 * @param node
	 */
	protected void setUnExpandedIcon(TreeItem<FolderResource> node) {
		MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
		graphic.setCursor(Cursor.HAND);
		node.setGraphic(graphic);
	}

	/**
	 * 
	 * @param node
	 */
	protected void addListener(TreeItem<FolderResource> node) {
		node.expandedProperty().addListener((e) -> {
			if (node.getChildren().isEmpty()) {
				setUnExpandedIcon(node);
			} else {
				if (node.isExpanded()) {
					node.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
				} else {
					MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
					graphic.setCursor(Cursor.HAND);
					node.setGraphic(graphic);
				}
			}
		});
	}
	
	/**
	 * 
	 */
	protected class PasswordNullEmptyException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public PasswordNullEmptyException() {
			super();
		}
	}
	
	/**
	 * 
	 * @param uri
	 */
	public static String getUriForShow(String uri) {
		if (uri != null) {
			if (!uri.startsWith("/")) {
				uri = "/".concat(uri);
			}
			uri = uri.replaceAll(Pattern.quote("\\"), "/");
			if (uri.endsWith(".xml")) {
				uri = uri.substring(0, uri.length() - 4);
			}
		}
		return uri;
	}
	
	/**
	 * 
	 * @param type
	 * @return
	 */
	public static String getBundleStringByType(ModelType type) {
		switch(type) {
		case TOPIC:
			return AdhocUtils.getString("P100.fileType.TOPIC");
		case ADHOC:
		default:
			return AdhocUtils.getString("P100.fileType.ADHOC");
		}
	}
	
	/**
	 * 
	 * @param bundle
	 * @return
	 */
	public static ModelType getTypeByBundleString(String bundle) {
		if (AdhocUtils.getString("P100.fileType.TOPIC").equals(bundle)) {
			return ModelType.TOPIC;
		} else if (AdhocUtils.getString("P100.fileType.ADHOC").equals(bundle)) {
			return ModelType.ADHOC;
		} else {
			return null;
		}
	}
	
	/**
	 * 
	 * @param resources
	 * @return
	 */
	protected static List<String> getDeleteUrisByResources(List<TreeItem<FolderResource>> resources) {
		List<String> deleteUris = new ArrayList<>();
		if (null != resources) {
			resources.forEach(resource -> {
				String deleteUri = resource.getValue().getUri();
				deleteUris.add(deleteUri);
			});
		}
		return deleteUris;
	}
	
	/**
	 * 
	 * @param rows
	 * @return
	 */
	protected static List<String> getDeleteUrisByRows(List<TreeItem<LoadDataRow>> rows) {
		List<String> deleteUris = new ArrayList<>();
		if (null != rows) {
			rows.forEach(row -> {
				String deleteUri = row.getValue().getUri();
				deleteUris.add(deleteUri);
			});
		}
		return deleteUris;
	}
	
}
