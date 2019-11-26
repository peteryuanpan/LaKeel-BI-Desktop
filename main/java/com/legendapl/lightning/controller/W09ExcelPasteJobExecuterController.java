package com.legendapl.lightning.controller;

import java.awt.Desktop;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;

import com.jaspersoft.jasperserver.dto.resources.ClientReferenceableInputControl;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXSpinner;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.RecursiveTreeItem;
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;
import com.legendapl.lightning.common.logger.LoggerMessageKey;
import com.legendapl.lightning.model.ExcelDefinition;
import com.legendapl.lightning.model.ExcelDefinitionTableRecord;
import com.legendapl.lightning.model.ExcelJob;
import com.legendapl.lightning.service.ExcelCooperationService;
import com.legendapl.lightning.service.ExcelSXSSFService;
import com.legendapl.lightning.service.ExcelXSSFService;
import com.legendapl.lightning.service.ExecuteAPIService;
import com.legendapl.lightning.service.ReportExcecuteService;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;

/**
 * Excelジョブの実行画面 貼り付け定義の一覧を表示し、進捗を可視化する。
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class W09ExcelPasteJobExecuterController extends C01ToolbarController {

	@FXML
	private AnchorPane anchorPane;

	@FXML
	/** キャンセル処理中のスピナー */
	private StackPane cancelSpinnerPane;

	/** 完了ボタン */
	@FXML
	private JFXButton completeButton;
	/** 開くボタン */
	@FXML
	private JFXButton openButton;
	/** キャンセルボタン */
	@FXML
	private JFXButton cancelButton;

	/** Excel貼り付けジョブのテーブルビュー */
	@FXML
	private JFXTreeTableView<ExcelDefinitionTableRecord> excelJobTable;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> number;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> reportLabel;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> targetColumns;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> sheet;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> cell;
	@FXML
	private JFXTreeTableColumn<ExcelDefinitionTableRecord, String> progress;

	@FXML
	private VBox statusVBox;

	/** テーブルビューのレコードリスト */
	private ObservableList<ExcelDefinitionTableRecord> tableRecordList;

	/** ジョブのモデル */
	private ExcelJob excelJob;

	/** ジョブの実行スレッド */
	private Thread jobThread;

	/** レポート実行画面のステージ */
	private Stage reportStage;

	/** ループのインデックス */
	private int roopIndex = 0;

	/** 出力先のExcelパス */
	private String outputFilePath;

	/** キャンセルボタンの押下を判定するフラグ */
	private boolean interrupte = false;

	/**
	 * Platform.runLaterの処理とジョインする
	 * https://programamemo2.blogspot.jp/2013/02/platformrunandwait-javafx.html
	 */
	private CountDownLatch localCountDownLatch = new CountDownLatch(1);

	/** リストビューの列幅の比率 */
	public static final double numberRatio = 0.05;
	public static final double reportNameRatio = 0.3;
	public static final double targetColumnsRatio = 0.4;
	public static final double sheetRatio = 0.2;
	public static final double cellWidth = 50.0;
	public static final double progressWidth = 85.0;

	public MaterialDesignIconView getIcon(String glaphName) {
		MaterialDesignIconView icon = new MaterialDesignIconView();
		icon.setGlyphName(glaphName);
		icon.setGlyphSize(25);
		switch (glaphName) {
		case "CHECK": {
			icon.setStyle("-fx-fill: BLUE;");
			break;
		}
		case "CLOSE": {
			icon.setStyle("-fx-fill: RED;");
			break;
		}
		case "HISTORY": {
			icon.setStyle("-fx-fill: GREEN;");
			break;
		}

		}
		return icon;
	}

	public JFXSpinner getSpinner() {
		JFXSpinner spinner = new JFXSpinner();
		spinner.setRadius(8.5);
		return spinner;
	}

	/**
	 * 初期化
	 * 
	 */
	@SuppressWarnings({ "unchecked" })
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.debug("URL=" + arg0 + ", ResourceBulder=" + arg1);

		if (null != object) {
			if (object instanceof ExcelJob) {
				this.excelJob = (ExcelJob) object;
				setObject(null);
			}
		} else
			return;

		// Excelファイルが存在しない場合はエラーになる
		File excelFile = new File(excelJob.getExcelPath());
		if (!excelFile.exists()) {
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					messageRes.getString(LoggerMessageKey.Error.ERROR_W07_05));
			new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Platform.runLater(() -> {
						anchorPane.getScene().getWindow().hide();
					});
					return null;
				}
			}).start();
			return;
		}

		// 開くボタンと完了ボタンを無効にする
		openButton.setDisable(true);
		completeButton.setDisable(true);

		/**
		 * 列幅の設定とwindowのクローズイベントを設定
		 * 
		 * 初期化メソッドではノードが取得できないため、別スレッドで設定を行う
		 */
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				Platform.runLater(() -> {
					double activeWidth = getStage().getWidth() - 30 - cellWidth - progressWidth;// 30は両端からの余白
					number.setPrefWidth(activeWidth * numberRatio);
					reportLabel.setPrefWidth(activeWidth * reportNameRatio);
					targetColumns.setPrefWidth(activeWidth * targetColumnsRatio);
					sheet.setPrefWidth(activeWidth * sheetRatio);
					cell.setPrefWidth(cellWidth);
					progress.setPrefWidth(progressWidth);

					anchorPane.getScene().getWindow().setOnCloseRequest((WindowEvent t) -> {
						if (null != jobThread) {
							// キャンセルのフラグを立てる。
							interrupte = true;
							cancelSpinnerPane.setVisible(true);
							forcedClose();
							t.consume();
						}
					});

					excelJobTable.setEditable(false);
				});
				return null;
			}
		}).start();

		excelJobTable = (JFXTreeTableView<ExcelDefinitionTableRecord>) anchorPane.getChildren().get(1);

		// ジョブを選択していない時のメッセージを設定
		excelJobTable.setPlaceholder(new Label(myResource.getString("W08.table.empty_msg")));

		// テーブルビューにセット
		addTableViewItems();

		jobThread = new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {

				/**
				 * 全てのレコードに待機中アイコンを付与する<br>
				 */
				for (int i = 0; i < tableRecordList.size(); i++) {
					Platform.runLater(() -> statusVBox.getChildren().add(getIcon("HISTORY")));
				}

				/**
				 * 最初に一度だけ出力先のExcelのパスを生成する。<br>
				 * 
				 * 以下の処理では参照元のExcelをコピーして出力先のExcelを生成し、パスを取得を行うが、<br>
				 * 複数の定義が含まれるジョブの場合、毎回参照元のExcelをコピーしてしまうため、最後の定義の内容しか<br>
				 * 出力先のExcelに反映されないため。
				 */
				outputFilePath = ExcelCooperationService.copyFile(excelJob.getExcelPath(), excelJob.getSaveExcelPath(),
						excelJob.isRewriteFlag());

				/**
				 * ジョブの実行を行う。<br>
				 * 画面のテーブルビューのレコードの数だけループを回して実行する
				 */
				for (ExcelDefinitionTableRecord tableRecord : tableRecordList) {
					// スレッドの停止フラグが立っていたら以降の処理を行わない
					if (interrupte) {
						// キャンセルログ
						Platform.runLater(() -> anchorPane.getScene().getWindow().hide());
						logger.info("Interrupt processing and close the excelJob stage.");
						return null;
					}

					// ステータスを切り替える
					Platform.runLater(() -> {
						tableRecord.setProgress(myResource.getString("W09.table.status.running"));
						statusVBox.getChildren().remove(roopIndex);
						statusVBox.getChildren().add(roopIndex, getSpinner());
					});

					for (int i = 0; i < excelJob.getExcelDefinitionList().size(); i++) {
						ExcelDefinition excelDefinition = excelJob.getExcelDefinitionList().get(i);

						// シートとセルをキーにして定義を特定して情報を取得する
						if (excelDefinition.getSheet().equals(tableRecord.getSheet().get())
								&& excelDefinition.getCell().equals(tableRecord.getCell().get())) {

							// 開始ログ
							logger.info(excelDefinition.getReportUri() + ":　Start　pasting data to Excel.");

							// エラーを判定するフラグ
							boolean errorFlag = false;
							// 入力コントロール
							List<ClientReferenceableInputControl> inputControls;

							// 帳票が存在しない際は処理を中断する。
							try {
								inputControls = ExecuteAPIService.getClientReportUnit(excelDefinition.getReportUri())
										.getInputControls();
							} catch (Exception e) {
								logger.error(e.getMessage(), e);
								// ステータスを切り替える
								Platform.runLater(() -> {
									tableRecord.setProgress(myResource.getString("W09.table.status.failed"));
									statusVBox.getChildren().remove(roopIndex);
									statusVBox.getChildren().add(roopIndex, getIcon("CLOSE"));
								});
								// 失敗ログ
								logger.warn(excelDefinition.getReportUri() + ": Failed to paste data into Excel.");
								continue;
							}

							// パラメータが保存されている場合もしくはパラメータが存在しない帳票の場合はバックグラウンドで実行する
							if (!StringUtils.isEmpty(excelDefinition.getParameterFileName()) || null == inputControls) {
								HashMap<String, Object> params = null;
								if (!StringUtils.isEmpty(excelDefinition.getParameterFileName())) {
									// 保存されているパラメータファイルを取得する
									File filebytedata = new File(
											"work/excelJob/params/" + excelDefinition.getParameterFileName() + ".dat");
									// 保存したパラメータ(HashMap)を読み込む
									if (filebytedata.exists()) {

										// バイトデータを読み取り、dataMapに設定
										byte[] tempbytes = ExcelCooperationService
												.readFileToByte("work/excelJob/params/"
														+ excelDefinition.getParameterFileName() + ".dat");

										ByteArrayInputStream bais = new ByteArrayInputStream(tempbytes);
										ObjectInputStream ois = new ObjectInputStream(bais);
										params = (HashMap<String, Object>) ois.readObject();

									} else {
										logger.warn("\"" + excelDefinition.getParameterFileName()
												+ ".dat\" does not exists.");
										// パラメータファイルが存在しない際にエラーフラグを立てる
										errorFlag = true;
									}
								} else
									params = new HashMap<String, Object>();

								// 出力先のExcelファイルのパス
								String saveFilePath = null;

								// エラーフラグを立っていたら帳票実行及びジョブの実行を行わない
								if (!errorFlag) {
									// JasperPrintオブジェクトの生成をバックグラウンドで行う
									ReportExcecuteService reportExcecuteService = new ReportExcecuteService(
											excelDefinition, params);
									HashMap<JasperPrint, JRSwapFileVirtualizer> hashMap = reportExcecuteService
											.createJasperPrint();
									JasperPrint jrPrint = hashMap.keySet().iterator().next();

									// ExcelJobのサービスクラス
									ExcelCooperationService excelCooperationService;
									if (excelDefinition.isLowMemoryFlag())
										excelCooperationService = new ExcelSXSSFService(excelJob, i, jrPrint);
									else
										excelCooperationService = new ExcelXSSFService(excelJob, i, jrPrint);

									// ジョブを実行する
									if (excelDefinition.getTargetColumns().equals("ALL")) {
										saveFilePath = excelCooperationService.executeJob(null, outputFilePath);
									} else {
										saveFilePath = excelCooperationService
												.executeJob(excelDefinition.getTargetColumns(), outputFilePath);
									}

									JRSwapFileVirtualizer swapFile = hashMap.values().iterator().next();
									if (swapFile != null)
										swapFile.cleanup();
								}

								// ジョブの実行に失敗したらnullが返ってくるので、エラーを表示させる。
								if (StringUtils.isEmpty(saveFilePath)) {
									// ステータスを切り替える
									Platform.runLater(() -> {
										tableRecord.setProgress(myResource.getString("W09.table.status.failed"));
										statusVBox.getChildren().remove(roopIndex);
										statusVBox.getChildren().add(roopIndex, getIcon("CLOSE"));
									});
									// 失敗ログ
									logger.warn(excelDefinition.getReportUri() + ": Failed to paste data into Excel.");
								} else {
									// ステータスを切り替える
									Platform.runLater(() -> {
										tableRecord.setProgress(myResource.getString("W09.table.status.complete"));
										statusVBox.getChildren().remove(roopIndex);
										statusVBox.getChildren().add(roopIndex, getIcon("CHECK"));
									});
									// 終了ログ
									logger.info(excelDefinition.getReportUri() + ": Completed pasting data to Excel.");
								}

							}
							// パラメータが保存されていない場合(レポート実行画面の表示)
							else {

								// レポート実行画面にパラメータとして渡すExcelJobを生成する
								ExcelJob paramExcelJob = new ExcelJob(excelJob,
										excelJob.getExcelDefinitionList().get(i));
								paramExcelJob.setSaveExcelPath(outputFilePath);
								setObject(paramExcelJob);

								// レポート実行画面のタイトル(表示された画面がどの帳票化分かるようにインデックスを付与する)
								String title = roopIndex + 1 + ": " + excelDefinition.getReportUri();

								Platform.runLater(() -> {
									try {
										Window window = anchorPane.getScene().getWindow();
										reportStage = showPane(window, "/view/W04InputControlAnchorPane.fxml", title,
												Modality.APPLICATION_MODAL, null);

										// 画面サイズの取得
										loadWindowSize();
										Double displayWidth = virtualBounds.getWidth();

										// ウィンドウ位置が記録されているか確認。
										if (preferences.getReportWindowY() != null
												&& preferences.getReportWindowX() != null) {
											// 記録されたウィンドウ位置
											Double preferencesX = Double.parseDouble(preferences.getReportWindowX());
											Double preferencesY = Double.parseDouble(preferences.getReportWindowY());
											Double preferencesWidth = Double
													.parseDouble(preferences.getReportWindowWidth());
											Double preferencesHeight = Double
													.parseDouble(preferences.getReportWindowHeight());

											// preferences.xmlからウィンドウ幅を取得
											reportStage.setWidth(preferencesWidth);
											reportStage.setHeight(preferencesHeight);

											// 記録されている位置が現在のディスプレイの範囲以内か確認

											// サブディスプレイが左側の場合
											if (virtualBounds.x < 0) {
												if (preferencesX >= virtualBounds.x - preferencesHeight) {
													reportStage.setX(preferencesX);
													reportStage.setY(preferencesY);
												} else {
													reportStage.setX(0);
													reportStage.setY(0);
												}
											}
											// サブディスプレイが右側、もしくは無い場合
											if (virtualBounds.x >= 0) {
												if (0 <= preferencesX && preferencesX <= displayWidth - 20) {
													reportStage.setX(preferencesX);
													reportStage.setY(preferencesY);
												} else {
													reportStage.setX(0);
													reportStage.setY(0);
												}
											}
										}

										controller.setStage(reportStage);

										reportStage.setOnCloseRequest((WindowEvent t) -> {
											try {
												logger.debug("close&cancel");
												((W04InputControlController) (controller)).close(t);
											} catch (InterruptedException e) {
												logger.error(e.getMessage(), e);
											}
										});
									} catch (Exception e1) {
										logger.error(e1.getMessage(), e1);
										// ステータスを切り替える
										tableRecord.setProgress(myResource.getString("W09.table.status.failed"));
										statusVBox.getChildren().remove(roopIndex);
										statusVBox.getChildren().add(roopIndex, getIcon("CLOSE"));
									}
								});

								// スレッドの停止フラグが立っていたら以降の処理を行わない
								if (interrupte) {
									// キャンセルログ
									Platform.runLater(() -> anchorPane.getScene().getWindow().hide());
									logger.info("Interrupt processing and close the excelJob stage.");
									return null;
								}

								Platform.runLater(() -> {
									reportStageList.add(reportStage);
									// Windowがクローズされるまで待機する
									reportStage.showAndWait();

									if (object instanceof Boolean) {
										Boolean result = (boolean) object;
										setObject(null);
										if (result) {
											tableRecord.setProgress(myResource.getString("W09.table.status.complete"));
											statusVBox.getChildren().remove(roopIndex);
											statusVBox.getChildren().add(roopIndex, getIcon("CHECK"));
											// 終了ログ
											logger.info(
													excelDefinition.getReportUri() + ": Execute excelJob finished.");
										} else {
											tableRecord.setProgress(myResource.getString("W09.table.status.failed"));
											statusVBox.getChildren().remove(roopIndex);
											statusVBox.getChildren().add(roopIndex, getIcon("CLOSE"));
											// 失敗ログ
											logger.warn(excelDefinition.getReportUri()
													+ ": Failed to paste data into Excel.");
										}
									} else {
										tableRecord.setProgress(myResource.getString("W09.table.status.failed"));
										statusVBox.getChildren().remove(roopIndex);
										statusVBox.getChildren().add(roopIndex, getIcon("CLOSE"));
										// 失敗ログ
										logger.warn(
												excelDefinition.getReportUri() + ": Failed to paste data into Excel.");
									}

									localCountDownLatch.countDown();
								});

								// Platform.runLaterの処理とジョインする
								localCountDownLatch.await();
								// 初期化する
								localCountDownLatch = new CountDownLatch(1);
							}
							break;
						}
					}
					Platform.runLater(() -> roopIndex++);
				}
				openButton.setDisable(false);
				completeButton.setDisable(false);
				cancelButton.setDisable(true);

				return null;
			}
		});
		jobThread.start();

	}

	/**
	 * 指定されたジョブの詳細を取得し、テーブルビューのアイテムとして追加する。
	 * 
	 * @param path
	 */
	@SuppressWarnings("unchecked")
	private void addTableViewItems() {

		// TableViewのレコードを定義
		tableRecordList = FXCollections.observableArrayList();

		for (int i = 0; i < excelJob.getExcelDefinitionList().size(); i++) {
			ExcelDefinition excelDefinition = excelJob.getExcelDefinitionList().get(i);
			// ExcelDefinitionからテーブルビューのモデルクラス(ExcelDefinitionTableRecord)にデータをバインド
			ExcelDefinitionTableRecord excelDefinitionTableRecord = new ExcelDefinitionTableRecord();
			excelDefinitionTableRecord.setNumber(String.valueOf(i + 1));
			excelDefinitionTableRecord.setReportLabel(excelDefinition.getReportName());
			excelDefinitionTableRecord.setTargetColumns(excelDefinition.getTargetColumns());
			excelDefinitionTableRecord.setSheet(excelDefinition.getSheet());
			excelDefinitionTableRecord.setCell(excelDefinition.getCell());
			excelDefinitionTableRecord.setProgress(myResource.getString("W09.table.status.waiting"));

			// テーブルにアイテムを追加
			tableRecordList.add(excelDefinitionTableRecord);
		}

		TreeItem<ExcelDefinitionTableRecord> root = new RecursiveTreeItem<>(tableRecordList,
				RecursiveTreeObject::getChildren);

		number.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
			if (number.validateValue(param)) {
				return param.getValue().getValue().getNumber();
			} else {
				return number.getComputedValue(param);
			}
		});
		reportLabel
				.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
					if (reportLabel.validateValue(param)) {
						return param.getValue().getValue().getReportLabel();
					} else {
						return reportLabel.getComputedValue(param);
					}
				});
		targetColumns
				.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
					if (targetColumns.validateValue(param)) {
						return param.getValue().getValue().getTargetColumns();
					} else {
						return targetColumns.getComputedValue(param);
					}
				});
		sheet.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
			if (sheet.validateValue(param)) {
				return param.getValue().getValue().getSheet();
			} else {
				return sheet.getComputedValue(param);
			}
		});
		cell.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
			if (cell.validateValue(param)) {
				return param.getValue().getValue().getCell();
			} else {
				return cell.getComputedValue(param);
			}
		});
		progress.setCellValueFactory((TreeTableColumn.CellDataFeatures<ExcelDefinitionTableRecord, String> param) -> {
			if (progress.validateValue(param)) {
				return param.getValue().getValue().getProgress();
			} else {
				return progress.getComputedValue(param);
			}
		});

		// グルーピングを不可にする
		number.setContextMenu(null);
		reportLabel.setContextMenu(null);
		targetColumns.setContextMenu(null);
		sheet.setContextMenu(null);
		cell.setContextMenu(null);
		progress.setContextMenu(null);

		excelJobTable.getColumns().setAll(number, reportLabel, targetColumns, sheet, cell, progress);
		excelJobTable.setRoot(root);
		excelJobTable.setShowRoot(false);
		excelJobTable.setEditable(true);

	}

	/**
	 * [完了]ボタンのイベントハンドラ
	 * 
	 * @param event
	 */
	@SuppressWarnings("deprecation")
	public void complete(ActionEvent event) {
		if (null != jobThread) {
			jobThread.interrupt();
			jobThread.stop();
		}

		anchorPane.getScene().getWindow().hide();
		logger.info("Complete the process and close the excelJob stage.");

	}

	/**
	 * [開く]ボタンのイベントハンドラ
	 * 
	 * @param event
	 */
	public void open(ActionEvent event) {
		/**
		 * エクスポートしたファイルを起動する
		 * 
		 * ファイルによっては時間がかかるのでバックグラウンドスレッドで開く
		 */
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				// ファイルを開く
				try {
					File file = new File(outputFilePath);
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
					logger.info("Open " + outputFilePath);
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				return null;
			}
		}).start();
	}

	/**
	 * [キャンセル]ボタンのイベントハンドラ
	 * 
	 * @param event
	 */
	public void cancel(ActionEvent event) {

		if (null != jobThread) {
			// キャンセルのフラグを立てる。
			interrupte = true;
			cancelSpinnerPane.setVisible(true);
			forcedClose();
		}
	}

	/**
	 * 処理の中断時に応答がない場合に強制終了する。
	 */
	public void forcedClose() {
		new Thread(new Task<Void>() {
			@SuppressWarnings("deprecation")
			@Override
			protected Void call() throws Exception {
				Thread.sleep(60000);
				if (null != jobThread) {
					jobThread.stop();
					logger.warn("Forced to quit because there was no response.");
					Platform.runLater(() -> anchorPane.getScene().getWindow().hide());
				}
				return null;
			}
		}).start();
	}

}