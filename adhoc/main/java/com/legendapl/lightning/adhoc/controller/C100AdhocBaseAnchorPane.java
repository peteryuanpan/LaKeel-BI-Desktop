package com.legendapl.lightning.adhoc.controller;

import java.io.IOException;
import java.util.ResourceBundle;

import com.legendapl.lightning.controller.C00ControllerBase;
import com.legendapl.lightning.tools.data.AdhocData;

import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * 全て画面コントローラクラスの共通クラスです。
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public abstract class C100AdhocBaseAnchorPane extends C00ControllerBase {

	/**
	 * ADHOC表示システムの各画面
	 */
	public static Stage topicStage; // トピック編集画面
	public static Stage adhocStage; // アドホックビュー編集画面

	/**
	 * 新しいウィンドウを開くときに、newStageを使う
	 *
	 * @param title
	 *            イベント
	 * @param paneFXML
	 *            ウィンドウのタイトル
	 * @param stage
	 *            fxmlのアドレス
	 */
	protected void switchRoot(String title, String paneFXML, Stage stage) throws IOException {
		switchRoot(title, paneFXML, stage, true);
	}

	/**
	 * 新しいウィンドウを開くときに、newStageを使う
	 */
	protected void switchRoot(String title, String paneFXML, Stage stage, boolean putIntoMap) throws IOException {
		logger.debug("Swiching to new page : " + title);

		Parent newRoot = AdhocData.roots.get(paneFXML);
		if (null == newRoot) {
			logger.debug("Page not created, creating new page.");

			newRoot = newRoot(paneFXML);
			if (putIntoMap) {
				AdhocData.roots.put(paneFXML, newRoot);
			}
		} else {
			logger.debug("Page created, switch to this page.");
		}
		final Parent root = newRoot;
		Platform.runLater(() -> {
			Scene scene = root.getScene();
			if (null != scene) {
				//TODO ここ、何かできないか？
				scene.setRoot(new Parent() {
				});
			}
			stage.setTitle(title);
			stage.getScene().setRoot(root);
			root.requestFocus();
		});
	}

	/**
	 * 新しいウィンドウのルートを取得
	 */
	protected Parent newRoot(String paneFXML) throws IOException {
		
		// 呼び出すダイアログのFXMLを開く
		FXMLLoader fxmlloader = new FXMLLoader(C00ControllerBase.class.getResource(paneFXML), ResourceBundle.getBundle("AdhocBundleMessage"));
		
		// initializeを呼び出し
		Parent root = fxmlloader.load();
		
		// fxmlで指定されたControllerを格納
		setController(fxmlloader.getController());

		return root;
	}
}
