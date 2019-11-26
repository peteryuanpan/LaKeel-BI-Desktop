package com.legendapl.lightning.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javax.ws.rs.ProcessingException;

import org.apache.commons.lang.StringUtils;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceListWrapper;
import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTreeView;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.model.FolderResource;
import com.legendapl.lightning.service.ExecuteAPIService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;

public class W10ReportSelectController extends C01ToolbarController {

	/** フォルダアイコン */
	private MaterialDesignIcon folderIcon = MaterialDesignIcon.FOLDER_OUTLINE;
	private FontAwesomeIcon folderExpandedIcon = FontAwesomeIcon.FOLDER_OPEN_ALT;
	private FontAwesomeIcon reportIcon = FontAwesomeIcon.FILE_ALT;

	/** バックグラウンドスレッドから設定されるAPIの実行結果 */
	public static List<ClientResourceLookup> resourceList;

	boolean timeOutFlag = false;

	@FXML
	private AnchorPane anchorPane;
	@FXML
	private JFXButton selectButton;
	@FXML
	private JFXTreeView<FolderResource> treeview;

	/**
	 * 初期化
	 * 
	 * @param arg0
	 * @param arg1
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("URL=" + arg0 + ", ResourceBulder=" + arg1);

		// TreeViewに設定するため、rootのTreeItemを作成
		TreeItem<FolderResource> root = new TreeItem<FolderResource>(new FolderResource("root"),
				new MaterialDesignIconView(folderIcon));
		root.setExpanded(true);

		if (serverInfo.getOrganizationName().isEmpty()) {
			// 組織名がない場合はルートフォルダを展開
			Task<Void> task = new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					timeOutFlag = addTreeItems(root, "/");
					return null;
				}
			};
			Thread thread = new Thread(task);
			thread.setDaemon(true);
			thread.start();
		} else {
			// rootフォルダを展開アイコンに変更
			root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
			// ▶トグルにより展開／折り畳みの際のイベントを追加
			root.expandedProperty().addListener((e) -> {
				if (root.isExpanded()) {
					root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
				} else {
					MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
					graphic.setCursor(Cursor.HAND);
					root.setGraphic(graphic);
				}
			});

			// 組織名がある場合は組織名のrootフォルダとPublicフォルダを追加
			FolderResource publicFoloder = new FolderResource("Public");
			publicFoloder.setUri("/public");
			publicFoloder.setResourceType("folder");
			MaterialDesignIconView public_graphic = new MaterialDesignIconView(folderIcon);
			public_graphic.setCursor(Cursor.HAND);
			root.getChildren().add(new TreeItem<FolderResource>(publicFoloder, public_graphic));

			FolderResource organizationFoloder = new FolderResource(serverInfo.getOrganizationName());
			organizationFoloder.setUri("/");
			organizationFoloder.setResourceType("folder");
			MaterialDesignIconView org_graphic = new MaterialDesignIconView(folderIcon);
			org_graphic.setCursor(Cursor.HAND);
			root.getChildren().add(new TreeItem<FolderResource>(organizationFoloder, org_graphic));
		}

		// フォルダ表示用TreeViewに取得したツリーを設定
		treeview.setRoot(root);

		// フォルダ選択時のイベントハンドラを追加
		treeview.setOnMouseClicked((e) -> handleMouseClicked(e));

	}

	/**
	 * 開くボタンクリック時の処理
	 * 
	 * @param event
	 */
	public void selectReport(ActionEvent event) {
		String uri = treeview.getSelectionModel().getSelectedItem().getValue().getUri();
		String label = treeview.getSelectionModel().getSelectedItem().getValue().getLabel();
		setObject(new String[] { uri, label });
		selectButton.getScene().getWindow().hide();
	}

	/**
	 * フォルダツリークリック時の処理
	 * 
	 * @param event
	 */
	private void handleMouseClicked(MouseEvent event) {
		Node node = event.getPickResult().getIntersectedNode();

		// 選択しているアイテムがない場合は処理を中断
		if (null == node || null == treeview.getSelectionModel()
				|| null == treeview.getSelectionModel().getSelectedItem()) {
			return;
		}
		FolderResource resource = treeview.getSelectionModel().getSelectedItem().getValue();

		// 選択したアイテムが帳票ならば選択ボタンを有効化する。
		if (resource.getResourceType().equals("reportUnit"))
			selectButton.setDisable(false);
		else
			selectButton.setDisable(true);

		// uriが設定されていない場合、またはrootフォルダに対してはフォルダ展開を禁止
		if (StringUtils.isEmpty(resource.getUri()))
			return;

		// クリックされた部分がフォルダアイコン、またはセルがダブルクリックされた場合、フォルダを展開
		if (!(node instanceof MaterialDesignIconView)
				&& !(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)) {
			return;
		}

		TreeItem<FolderResource> item = treeview.getSelectionModel().getSelectedItem();

		// レポ―トがダブルクリックされた場合は処理を中断
		if (item.getGraphic() instanceof FontAwesomeIconView
				&& ((FontAwesomeIconView) item.getGraphic()).getGlyphName().equals("FILE_ALT")) {
			return;
		}

		// 選択したフォルダをすでに開いていて、アイコン以外をダブルクリック場合はアイテムを追加せず、展開されたフォルダを折りたたむ
		if (0 != item.getChildren().size() && !(node instanceof MaterialDesignIconView)) {
			return;
		}

		item.setExpanded(true);

		Task<Void> task = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				timeOutFlag = addTreeItems(item, resource.getUri());
				return null;
			}
		};
		Thread thread = new Thread(task);
		thread.setDaemon(true);
		thread.start();

	}

	/**
	 * 指定されたパス以下のフォルダとレポートを取得し、ツリービューのアイテムとして追加する。
	 * 
	 * @param root
	 * @param path
	 * @return root
	 */
	private boolean addTreeItems(TreeItem<FolderResource> root, String path) {

		// アイテムをクリアする
		root.getChildren().clear();

		// ロード中のアイコンを設定
		JFXSpinner loadingFolderIcon = new JFXSpinner();
		loadingFolderIcon.setRadius(5);
		Platform.runLater(() -> {
			root.setGraphic(loadingFolderIcon);
		});

		try {
			ClientResourceListWrapper clientResourceListWrapper = ExecuteAPIService.getReportUnitAndDirectory(path);
			if (null != clientResourceListWrapper && null != clientResourceListWrapper.getResourceLookups()) {
				resourceList = clientResourceListWrapper.getResourceLookups();

				// 既に中身が入っていたり、他のスレッドにより追加結果が重複する場合の対策
				if (!root.getChildren().isEmpty()) {
					return false;
				}

				// 実行結果を1件ずつroot直下に追加(ディレクトリ)
				for (ClientResourceLookup resource : resourceList) {
					Platform.runLater(() -> {
						if (!resource.getResourceType().equals("reportUnit")) {
							MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
							graphic.setCursor(Cursor.HAND);
							root.getChildren().add(new TreeItem<FolderResource>(new FolderResource(resource), graphic));
						} else {
							FontAwesomeIconView graphic = new FontAwesomeIconView(reportIcon);
							root.getChildren().add(new TreeItem<FolderResource>(new FolderResource(resource), graphic));
						}
					});
				}
				root.setExpanded(true);

				// 展開後にフォルダアイコンを変更
				Platform.runLater(() -> {
					root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
				});

				// ▶トグルにより展開／折り畳みの際のイベントを追加
				root.expandedProperty().addListener((e) -> {
					if (root.isExpanded()) {
						root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
					} else {
						MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
						graphic.setCursor(Cursor.HAND);
						root.setGraphic(graphic);
					}
				});
				return true;
			} else {
				// フォルダの中にフォルダがない場合は、展開されたアイコンを設定
				Platform.runLater(() -> {
					root.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
				});
				return false;
			}

			// API実行時のタイムアウトエラー
		} catch (ProcessingException e) {
			logger.error(messageRes.getString(Error.ERROR_P02_01), e);
			Platform.runLater(() -> {
				// エラー発生時に開こうとしたフォルダのアイコンと待機中画面を元に戻す
				MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
				root.setGraphic(graphic);
				// エラーダイアログを表示
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(Error.ERROR_P02_01));
			});
			logger.error(messageRes.getString(Error.ERROR_P02_01));
			return false;

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return false;
		}

	}
}
