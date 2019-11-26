
package com.legendapl.lightning.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jaspersoft.jasperserver.jaxrs.client.core.exceptions.ResourceNotFoundException;
import com.jfoenix.controls.JFXListView;
import com.legendapl.lightning.adhoc.recentItem.AdhocHBoxCell;
import com.legendapl.lightning.adhoc.recentItem.AdhocRecentItem;
import com.legendapl.lightning.adhoc.recentItem.AdhocRecentItemService;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.model.FolderResource;
import com.legendapl.lightning.model.RecentItem;
import com.legendapl.lightning.service.ExecuteAPIService;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;

/**
 * ホーム画面のコントローラクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class P01HomeController extends C01ToolbarController {

	private Logger logger = Logger.getLogger(getClass());

	@FXML
	private JFXListView<HBox> RecentItemsListView;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		// 最近表示されたアイテムを画面に表示
		for (RecentItem item : serverInfo.getRecentItems()) {
			if (item.getClass() == RecentItem.class) {
				RecentItemsListView.getItems().add(new HBoxCell(item));
			} else if (item.getClass() == AdhocRecentItem.class) {
				if (AdhocRecentItemService.canAdd((AdhocRecentItem) item)) {
					RecentItemsListView.getItems().add(new AdhocHBoxCell((AdhocRecentItem)item));
				}
			}  else {
				// TODO
			}
		}

		// 最近表示されたアイテムをクリックした際のイベントを追加
		RecentItemsListView.setOnMouseClicked((event) -> {
			// マウス選択されたオブジェクトを取得
			Object obj = RecentItemsListView.getSelectionModel().getSelectedItem();

			// 選択されたオブジェクトがない場合は処理を抜ける
			if (null == obj) {
				return;
			}

			// 左クリックかつ、ダブルクリックでない場合は処理を抜ける。
			if (!(event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2)) {
				return;
			}
			
			//　アドホックモジュールのため
			if (obj.getClass().equals(AdhocHBoxCell.class)) {
				AdhocRecentItemService AdhocRecentItemService = new AdhocRecentItemService();
				AdhocRecentItemService.run((AdhocHBoxCell)obj);
				return;
			}

			// 選択位置を取得
			int index = RecentItemsListView.getFocusModel().getFocusedIndex();

			// オブジェクト（ラベル）を出力
			logger.debug(index + " " + obj.toString() + " is cliked");
			// 選択位置のオブジェクトを出力
			logger.debug(serverInfo.getRecentItems().get(index).toString());

			// リポジトリ画面へ遷移
			try {
				FolderResource folder = new FolderResource();
				String uri = serverInfo.getRecentItems().get(index).getReportURI();

				// レポートが存在するかを確認
				if (!StringUtils.isEmpty(uri)) {
					ExecuteAPIService.getClientReportUnit(uri);
				}
				folder.setUri(uri.substring(0, uri.lastIndexOf("/")));
				setObject(folder);

				double width = currentStage.getWidth();
				currentStage = showPane(event, "/view/P02RepoAnchorPane.fxml",
						getTitle("P02") + " (" + serverInfo.getName() + ")", null, currentStage);
				P02RepoController moveController = (P02RepoController) controller;
				// フォルダ遷移時に遷移先コントローラーのmoveFolderメソッドをバックグラウンドで実行
				Task<Void> task = new Task<Void>() {
					@Override
					protected Void call() throws Exception {
						moveController.moveFolder(folder, width);
						Platform.runLater(() -> {
							try {
								moveController.showReportPane(uri);
							} catch (IOException e) {
								logger.error(e.getMessage(), e);
							}
						});
						return null;
					}
				};
				Thread thread = new Thread(task);
				thread.setDaemon(true);
				thread.start();

			} catch (ResourceNotFoundException e) {
				showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
						messageRes.getString(Error.ERROR_P01_01));
				logger.error(e.getMessage(), e);
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		});

	}

	// 最近表示されたアイテムを表示するための内部クラス
	public static class HBoxCell extends HBox {
		Label reportLabel = new Label();
		MaterialDesignIconView icon = new MaterialDesignIconView();
		Label uriLabel = new Label();

		public HBoxCell(RecentItem item) {
			icon.setGlyphName(Constant.Graphic.HOME_REPORT_ICON);
			icon.setSize(Constant.Graphic.HOME_REPORT_ICON_SIZE);

			// 長さに応じてツールチップを追加
			if (70 < item.getReportLabel().length()) {
				reportLabel.setText(item.getReportLabel().substring(0, 70) + "...");
				Tooltip reportLabelTooltip = new Tooltip(item.getReportLabel());
				reportLabelTooltip.setMaxWidth(400);
				reportLabelTooltip.setWrapText(true);
				reportLabel.setTooltip(reportLabelTooltip);
			} else {
				reportLabel.setText(item.getReportLabel());
			}

			// urlラベルが右端に行くように、レポート名の幅を最大に拡大するように設定
			reportLabel.setMaxWidth(Double.MAX_VALUE);
			reportLabel.setPadding(new Insets(0, 0, 0, 10));
			HBox.setHgrow(reportLabel, Priority.ALWAYS);

			String fullUri = item.getReportURI().substring(0, item.getReportURI().lastIndexOf("/"));
			uriLabel.setText(fullUri);

			// 最大サイズとツールチップを追加
			uriLabel.setMaxWidth(250);
			Tooltip urlTooltip = new Tooltip(fullUri);
			urlTooltip.setMaxWidth(400);
			urlTooltip.setWrapText(true);
			uriLabel.setTooltip(urlTooltip);

			uriLabel.setPadding(new Insets(0, 10, 0, 10));
			uriLabel.getStyleClass().add("home-uri-label");

			this.getChildren().addAll(icon, reportLabel, uriLabel);
		}
	}
}
