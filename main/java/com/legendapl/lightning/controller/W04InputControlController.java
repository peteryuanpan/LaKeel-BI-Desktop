package com.legendapl.lightning.controller;

import java.awt.Desktop;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.math.NumberUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.jaspersoft.jasperserver.dto.resources.ClientDataType;
import com.jaspersoft.jasperserver.dto.resources.ClientInputControl;
import com.jaspersoft.jasperserver.dto.resources.ClientQuery;
import com.jaspersoft.jasperserver.dto.resources.ClientReportUnit;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXCheckBox;
import com.jfoenix.controls.JFXComboBox;
import com.jfoenix.controls.JFXDatePicker;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXRadioButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTimePicker;
import com.jfoenix.validation.NumberValidator;
import com.jfoenix.validation.RequiredFieldValidator;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.common.constants.ReportErrors;
import com.legendapl.lightning.model.ExcelDefinition;
import com.legendapl.lightning.model.ExcelJob;
import com.legendapl.lightning.service.ExcelCooperationService;
import com.legendapl.lightning.service.ExcelSXSSFService;
import com.legendapl.lightning.service.ExcelXSSFService;
import com.legendapl.lightning.service.ExecuteAPIService;
import com.legendapl.lightning.service.InputControlService;
import com.legendapl.lightning.service.ReportCreateServiceImpl;
import com.legendapl.lightning.service.ReportErrorService;
import com.legendapl.lightning.service.ReportExcecuteService;
import com.legendapl.lightning.validation.JFXDatePickerConverter;
import com.legendapl.lightning.validation.JFXTimePickerConverter;

import de.jensd.fx.glyphs.GlyphsBuilder;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingNode;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.IndexRange;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleGroup;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.design.JasperDesign;
import net.sf.jasperreports.engine.fill.JRSwapFileVirtualizer;
import net.sf.jasperreports.engine.xml.JRXmlLoader;
import net.sf.jasperreports.swing.JRViewer;
import net.sf.jasperreports.swing.JRViewerToolbar;
import net.sf.jasperreports.swing.JRViewerToolbarExport;
import net.sf.jasperreports.swing.JRViewerToolbox;
import net.sf.jasperreports.view.JasperViewer;

/**
 * 入力コントロールとJasperレポート表示画面のコントローラクラス
 *
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class W04InputControlController extends C00ControllerBase {

	@FXML
	private AnchorPane root;

	// 入力コントロールのSplitPane
	@FXML
	private SplitPane splitPane;

	// 入力コントロールのAnchorPane
	@FXML
	private AnchorPane inputControlAnchorPane;

	// 入力コントロールのGridPane
	@FXML
	private GridPane gridInputControl;

	// 入力コントロールのStackPane
	@FXML
	private StackPane inputStack;

	// 入力コントロールのHBOX(ボタン)
	@FXML
	private HBox inputControlHBox;

	// レポートの適用、キャンセルボタン
	@FXML
	private ScrollPane inputControlPane;

	// 表示を切り替えるアンカーペイン
	@FXML
	private AnchorPane ReportStackPane;

	// プログレスサークルを表示するStackPane
	@FXML
	private StackPane spinnerPane;
	// エクスポート完了通知を表示するStackPane
	@FXML
	private StackPane completePane;
	// JasperViewerを表示するSwingNode
	@FXML
	private SwingNode swingNode;
	// エクスポート失敗を表示するStackPane
	@FXML
	private StackPane failedPane;
	@FXML
	private StackPane excelPane;
	@FXML
	private TextArea errorLog;

	@FXML
	private VBox inputVBox;

	@FXML
	// 保存ボタン
	private JFXButton saveButton;

	// 入力コントロールの値(実際にレポート実行時に渡す引数)
	private HashMap<String, Object> inputValues;

	// バリデータ用スタイル
	private static final String EM1 = "1em";
	private static final String ERROR = "error";
	// 静的マップ<label, value>
	private LinkedHashMap<String, String> staticParameterMap;

	// 動的マップ<label, value>
	private LinkedHashMap<String, String> queryParameterMap;

	// 画面にバインドするアイテムリスト
	ObservableList<String> parameterItems;

	private String reportUri;

	private String exportFileformat;

	private String exportFileformatTmp;

	private Thread thread;

	private ClientReportUnit clientReportUnit;

	private File saveFile = null;

	private ArrayList<String> inputCotrolSQL = new ArrayList<String>();

	private Map<String, ClientInputControl> parameterMap;

	private Logger logger = Logger.getLogger(getClass());

	// 入力コントロールの位置を記録
	private HashMap<String, Integer> inputControlIndex = new HashMap<String, Integer>();

	// jrxmlの読み込み(JasperDesign = .jrxml)
	private JasperDesign jasperDesign;
	private Map<String, JRParameter> paramMap = new HashMap<String, JRParameter>();

	private JasperPrint jrPrint = null;
	private JasperViewer jasperViewer = null;
	private Task<JasperPrint> runReportTask;
	private HashMap<JasperPrint, JRSwapFileVirtualizer> hashMap;

	// デバイダーの位置
	private Double divider = null;

	/** Excelジョブ */
	private ExcelJob excelJob;

	/** Excel貼り付けの定義 */
	private ExcelDefinition excelDefinition;

	// レポート実行サーバ
	private ReportExcecuteService reportExcecute;

	// レポートエラーサーバ
	private ReportErrorService reportErrorService;

	// 初期化
	public void initialize(URL arg0, ResourceBundle arg1) {

		// preferencesを読み込み
		loadPreferences();

		try {
			String[] objectArray;

			if (null != object) {
				/** 通常実行 */
				if (object instanceof String) {
					reportUri = (String) object;
					/**
					 * Excelジョブの作成時に表示されるレポート実行画面には[保存]ボタンを表示する。<br>
					 * パラメータの受け渡し時に末尾に規定の文字列を付与して判定を行う。
					 */
					if (reportUri.contains(Constant.ExcelJob.SAVEPARAMETERKEY)) {
						saveButton.setVisible(true);
						saveButton.setDisable(true);
						reportUri = reportUri.replace("_save_parameter_", "");
					}
				}
				/** エクスポート */
				else if (object instanceof String[]) {
					objectArray = (String[]) object;
					reportUri = objectArray[0];
					exportFileformat = objectArray[1];
				}
				/** Excel貼り付け */
				else if (object instanceof ExcelJob) {
					this.excelJob = (ExcelJob) object;
					this.excelDefinition = excelJob.getExcelDefinitionList().get(0);
					reportUri = excelDefinition.getReportUri();
				}
				/** 本クラスの二次利用 レポート実行画面からも各種エクスポート */
				else if (object instanceof W04InputControlController) {
					exportOffline((W04InputControlController) object);
					return;
				}
				/** 未定義的な種類 */
				else {
					logger.warn("unkonw object type found.");
				}
			}
			setObject(null);

			// レポートエラーサーバを初期化する
			reportErrorService = new ReportErrorService();

			// 最初からプログレスバーが表示されてしまうので画面右側をクリアする
			if (null != ReportStackPane) {
				ReportStackPane.getChildren().clear();
			}

			// ClientReportUnitを取得するメソッド
			try {
				clientReportUnit = ExecuteAPIService.getClientReportUnit(reportUri);

			} catch (Exception e) {
				reportErrorService.clearErrorLog();
				reportErrorService.addError(e);
				setErrorLog(reportErrorService.getErrorLogWithFormat());
				Platform.runLater(() -> {
					inputControlPane.setVisible(false);
					inputControlHBox.getChildren().clear();
					ReportStackPane.getChildren().clear();
					ReportStackPane.getChildren().add(failedPane);
				});
				logger.error("Report does not exist.");
				return;
			}

			/**
			 * SplitPaneのdividerの位置の設定
			 *
			 * 初期化メソッドでは設定できない(値が反映されない)ため、別スレッドで設定を行う
			 */
			Thread thread = new Thread(new Task<Void>() {
				@Override
				protected Void call() throws Exception {
					Platform.runLater(() -> splitPane.setDividerPositions(divider));
					return null;
				}
			});

			// 入力コントロールを含まない場合、直接実行する。
			if (null == clientReportUnit.getInputControls()) {
				/**
				 * 入力コントロールパネルの操作 <br>
				 * ・クリアボタンの消去 <br>
				 * ・スクロールパネルの非表示<br>
				 * ・[適用]ボタンのラベルを[再実行]に変更
				 */
				inputControlPane.setVisible(false);
				ResourceBundle resourceBundle = ResourceBundle.getBundle(Constant.Application.MY_BUNDLE);
				JFXButton rerun = (JFXButton) inputControlHBox.getChildren().get(0);
				rerun.setText(resourceBundle.getString("W04.button.rerun"));
				inputControlHBox.getChildren().clear();
				inputControlHBox.getChildren().add(rerun);

				// レポートを実行し、実行結果(true/false）を格納し、リポジトリ画面へ制御を返却
				setObject(report(null));

				// デバイダーのセット
				divider = Double.parseDouble(preferences.getNonInputControlDivider());
				thread.start();

				return;
			}

			// デバイダーのセット(パラメータを含む場合)
			divider = Double.parseDouble(preferences.getInputControlDivider());
			thread.start();

			// jrxmlを取得
			String localJrxmlPath = ExecuteAPIService.getJrxml(clientReportUnit);

			jasperDesign = JRXmlLoader.load(localJrxmlPath);
			paramMap = jasperDesign.getParametersMap();

			// 入力コントロールをAPIにより取得
			if (null != clientReportUnit.getInputControls()) {

				parameterMap = ExecuteAPIService.getInputControlType(clientReportUnit);

				/** パラメータマップを作成し、パラメータ名だけ先に格納しておく */
				inputValues = new HashMap<String, Object>();
				int i = 0;

				for (Map.Entry<String, ClientInputControl> e : parameterMap.entrySet()) {

					// 非表示の入力コントロールは表示しない
					if (!e.getValue().isVisible())
						continue;

					// 入力コントロールに含まれるクエリを集約
					ClientQuery query = (ClientQuery) e.getValue().getQuery();
					if (null != query)
						inputCotrolSQL.add(query.getValue());

					// 入力コントロールを追加
					String parameterUri = clientReportUnit.getInputControls().get(i).getUri();
					inputValues.put(parameterUri.substring(parameterUri.lastIndexOf("/") + 1), null);
					addInputControlToPane(clientReportUnit, i * 2, e);

					// 入力コントロールの位置を記録
					inputControlIndex.put(e.getKey(), i * 2);

					i++;
				}

				logger.debug(inputControlIndex);

				for (Map.Entry<String, ClientInputControl> me : parameterMap.entrySet()) {
					// SQLにパラメータ名が含まれていない場合はスキップ
					if (!inputCotrolSQL.toString().contains("$P{" + me.getKey() + "}")
							&& !inputCotrolSQL.toString().contains(me.getKey() + "}")) {

						logger.debug(me.getKey() + "\t is not a $P Parameter.");
						continue;
					}
					logger.debug(me.getKey() + "\t is a $P Parameter.");
					// Labelをスキップしてカスケード設定するNodeを特定
					Node self = inputVBox.getChildren().get(inputControlIndex.get(me.getKey()) + 1);

					logger.debug(
							"[cascaded] parameter:" + inputVBox.getChildren().get(inputControlIndex.get(me.getKey())));

					addCascadeHandler(me, self);
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			// レポート実行時のエラーを表示
			showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
					myResource.getString("common.error.dialog.header"),
					myResource.getString("P02.report_error.dialog.body"));
			setObject(false);
		}
	}

	/**
	 * @param me
	 * @param self
	 * @param othersQuery
	 * @return
	 */
	private void addCascadeHandler(Map.Entry<String, ClientInputControl> me, Node self) {

		ClientQuery othersQuery;

		// パラメータを参照している入力コントロールを特定し、イベントハンドラを追加
		for (Map.Entry<String, ClientInputControl> other : parameterMap.entrySet()) {
			// 自分自身を除去
			if (me.equals(other)) {
				continue;
			}

			// クエリが設定されていないコントロール、またはパラメータ名が含まれていない場合はスキップ
			othersQuery = (ClientQuery) other.getValue().getQuery();

			if (null == othersQuery || (!othersQuery.getValue().contains("P{" + me.getKey() + "}")
					&& !othersQuery.getValue().contains(me.getKey() + "}"))) {
				continue;
			}
			// カスケード設定された入力コントロールの位置を取得
			int otherIndex = inputControlIndex.get(other.getKey());
			// カスケード設定された入力コントロール情報を表示
			logger.debug("  [cascade] key:" + other.getKey() + " index:" + otherIndex);

			// カスケード設定された入力コントロールを無効化
			inputVBox.getChildren().get(otherIndex + 1).setDisable(true);
			logger.debug(" [cascade] disabled:" + inputVBox.getChildren().get(otherIndex + 1));

			// 複数選択リストの場合、内部のリストに カスケード設定された入力コントロールに対して再描画を実施する処理を追加
			if (self instanceof C02MultiSelectionList<?>) {
				((C02MultiSelectionList<?>) self).list.focusedProperty().addListener((o, oldVal, newVal) -> {
					if (!newVal)
						refreshCascadeNode(me, other, otherIndex);
				});

				((C02MultiSelectionList<?>) self).selectAll.focusedProperty().addListener((o, oldVal, newVal) -> {
					if (!newVal)
						refreshCascadeNode(me, other, otherIndex);
				});
				((C02MultiSelectionList<?>) self).unselectAll.focusedProperty().addListener((o, oldVal, newVal) -> {
					if (!newVal)
						refreshCascadeNode(me, other, otherIndex);
				});
			} else {
				// カスケード設定された入力コントロールに対して再描画を実施する処理を追加
				self.focusedProperty().addListener((o, oldVal, newVal) -> {
					if (!newVal)
						refreshCascadeNode(me, other, otherIndex);
				});
			}
		}
		return;
	}

	/**
	 * @param me
	 * @param other
	 * @param otherIndex
	 */
	private void refreshCascadeNode(Map.Entry<String, ClientInputControl> me,
			Map.Entry<String, ClientInputControl> other, int otherIndex) {
		if (inputVBox.getChildren().get(otherIndex) instanceof Label) {
			logger.debug(
					"Refresh at " + otherIndex + ": " + ((Label) inputVBox.getChildren().get(otherIndex)).getText());
		}

		// 値入力があり、クエリ実行結果がnullでない場合は再描画
		if (null != inputValues.get(me.getKey()) && null != createValueNode(clientReportUnit, other)) {
			// ノードを削除
			inputVBox.getChildren().remove((int) otherIndex);
			inputVBox.getChildren().remove((int) otherIndex);

			// 新規ノードを追加
			addInputControlToPane(clientReportUnit, otherIndex, other);
			logger.debug("inputValue:" + inputValues.get(me.getKey()));

			// 再帰的にイベントハンドラを追加
			addCascadeHandler(other, inputVBox.getChildren().get(inputControlIndex.get(other.getKey()) + 1));
		}
	}

	/**
	 * 入力コントロールを画面に追加
	 *
	 * @param clientReportUnit
	 * @param index
	 * @param e
	 * @return
	 */
	private void addInputControlToPane(ClientReportUnit clientReportUnit, int index,
			Map.Entry<String, ClientInputControl> e) {
		// 必須入力の場合は（必須）または (required)をラベルに追加
		Label inputLabel = new Label(
				e.getValue().isMandatory() && !e.getValue().getLabel().contains(Constant.Graphic.REQURIED_CHECK_TEXT)
						? e.getValue().getLabel() + myResource.getString("common.label.required")
						: e.getValue().getLabel());
		inputLabel.setWrapText(true);
		inputLabel.setTextAlignment(TextAlignment.JUSTIFY);

		Node labelNode = inputLabel;
		Node valueNode = createValueNode(clientReportUnit, e);

		if (valueNode != null) {
			// 見出しのGridPaneへの追加
			inputVBox.getChildren().add(index, labelNode);
			logger.debug("added:" + inputVBox.getChildren().get(index));
			// 入力コントロール要素のGridPaneへの追加
			inputVBox.getChildren().add(index + 1, valueNode);
			logger.debug("added:" + inputVBox.getChildren().get(index + 1));
		}
	}

	/**
	 * @param clientReportUnit
	 * @param entry
	 * @return
	 */
	private Node createValueNode(ClientReportUnit clientReportUnit, Map.Entry<String, ClientInputControl> entry) {
		Node valueNode = null;

		switch (entry.getValue().getType()) {
		case 1: // チェックボックス
			valueNode = createCheckbox(entry);
			break;
		case 2: // 単一値入力
			ClientDataType type = (ClientDataType) entry.getValue().getDataType();
			switch (type.getType()) {
			case text:
				valueNode = createTextField(entry);
				break;
			case number:
				valueNode = createNumberTextField(entry);
				break;
			case date:
				valueNode = createDatePicker(entry);
				break;
			case time:
				valueNode = createTimePicker(entry);
				break;
			case datetime:
				valueNode = createDateTimePicker(entry);
				break;
			}
			break;
		case 3: // 単一選択リスト
			valueNode = createSingleSelectionList(entry);
			break;
		case 4: // 単一選択リスト(SQL)
			valueNode = createSingleSelectionQueryList(clientReportUnit, entry);
			break;
		case 6: // 複数選択リスト
			valueNode = createMultiSelectionList(entry);
			if (inputValues
					.get(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1)) == null)
				inputValues.put(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1),
						new ArrayList<>());
			break;
		case 7: // 複数選択リスト(SQL)
			valueNode = createMultiSelectionQueryList(clientReportUnit, entry);
			if (inputValues
					.get(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1)) == null)
				inputValues.put(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1),
						new ArrayList<>());
			break;
		case 8: // 単一選択ラジオボックス
			valueNode = createSingleSelectionRadioList(entry);
			break;
		case 9: // 単一選択ラジオボックス(SQL)
			valueNode = createSingleSelectionRadioQueryList(clientReportUnit, entry);
			break;
		case 10: // 複数選択チェックボックス
			valueNode = createMultiSelectionCheckList(entry);
			if (inputValues
					.get(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1)) == null)
				inputValues.put(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1),
						new ArrayList<>());
			break;
		case 11: // 複数選択チェックボックス(SQL)
			valueNode = createMultiSelectionCheckQueryList(clientReportUnit, entry);
			if (inputValues
					.get(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1)) == null)
				inputValues.put(entry.getValue().getUri().substring(entry.getValue().getUri().lastIndexOf("/") + 1),
						new ArrayList<>());
			break;
		}
		return valueNode;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createCheckbox(Map.Entry<String, ClientInputControl> entry) {
		JFXCheckBox checkBox = new JFXCheckBox(); // チェックボックス要素の生成

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));
		// 初期値としてfalseを適用
		inputValues.put(name, false);

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()
				&& "true".equals(paramMap.get(name).getDefaultValueExpression().getText())) {
			inputValues.put(name, true);
			checkBox.setSelected(true);
		}

		checkBox.selectedProperty().addListener((o, oldVal, newVal) -> {
			// 選択された場合追加
			if (checkBox.isSelected()) {
				logger.debug("added.");
				inputValues.put(name, true);
				// 選択解除された場合削除
			} else {
				inputValues.put(name, false);
				logger.debug("removed.");
			}
		});

		if (entry.getValue().isReadOnly()) {
			checkBox.setDisable(true);
		}

		return checkBox;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createTextField(Map.Entry<String, ClientInputControl> entry) {
		JFXTextField textField = new JFXTextField();
		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));
		if (entry.getValue().isMandatory()) {
			// 必須入力バリデータを作成
			RequiredFieldValidator requireValidator = new RequiredFieldValidator();
			requireValidator.setMessage(myResource.getString("common.message.required"));
			requireValidator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING)
					.size(EM1).styleClass(ERROR).build());
			textField.getValidators().add(requireValidator);
		}

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = NumberUtils.isNumber(paramMap.get(name).getDefaultValueExpression().getText())
					? paramMap.get(name).getDefaultValueExpression().getText()
					: paramMap.get(name).getDefaultValueExpression().getText().substring(1,
							paramMap.get(name).getDefaultValueExpression().getText().length() - 1);
			inputValues.put(name, value);
			textField.setText(value);
			// 帳票側に指定された型がIntegerの場合キャスト
			if ("java.lang.Integer".equals(paramMap.get(name).getValueClassName())) {
				inputValues.put(name, Integer.valueOf(value));
			}
		}

		// 値が変更された際にHasMapへ値を格納
		textField.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				if (!textField.validate()) {
					return;
				}
				;
				logger.debug("Typed: " + textField.getText());
				inputValues.put(name, textField.getText());

				// 帳票側に指定された型がIntegerの場合キャスト
				if ("java.lang.Integer".equals(paramMap.get(name).getValueClassName())) {
					inputValues.put(name, Integer.valueOf(textField.getText()));
				}
			}
		});
		if (entry.getValue().isReadOnly()) {
			textField.setEditable(false);
			textField.setDisable(true);
		}

		return textField;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createNumberTextField(Map.Entry<String, ClientInputControl> entry) {
		JFXTextField valueField = new JFXTextField() {
			private final Pattern numberPattern = Pattern.compile("\\d{1,3}");

			@Override
			public void replaceText(int start, int end, String text) {
				if (valid(start, end, text)) {
					super.replaceText(start, end, text);
				}
			}

			@Override
			public void replaceSelection(String text) {
				IndexRange selectionRange = getSelection();
				if (valid(selectionRange.getStart(), selectionRange.getEnd(), text)) {
					super.replaceSelection(text);
				}
			}

			private boolean valid(int start, int end, String text) {
				String attemptedText = getText().substring(0, start) + text + getText().substring(end);
				if (attemptedText.length() == 0) {
					return true;
				}
				if (numberPattern.matcher(attemptedText).matches()) {
					int value = Integer.parseInt(attemptedText);

					if (null == ((ClientDataType) entry.getValue().getDataType()).getMinValue()
							|| null == ((ClientDataType) entry.getValue().getDataType()).getMinValue())
						return true;

					if (value >= Integer.valueOf(((ClientDataType) entry.getValue().getDataType()).getMinValue())
							&& value <= Integer
									.valueOf(((ClientDataType) entry.getValue().getDataType()).getMaxValue())) {
						return true;
					}
				}
				return false;
			}
		};

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		if (entry.getValue().isMandatory()) {
			// 必須入力バリデータを作成
			RequiredFieldValidator requireValidator = new RequiredFieldValidator();
			requireValidator.setMessage(myResource.getString("common.message.required"));
			requireValidator.setIcon(GlyphsBuilder.create(FontAwesomeIconView.class).glyph(FontAwesomeIcon.WARNING)
					.size(EM1).styleClass(ERROR).build());
			valueField.getValidators().add(requireValidator);

		}

		// 数値バリデータを作成
		NumberValidator numberValidator = new NumberValidator();
		numberValidator.setMessage(myResource.getString("common.message.number_required"));
		valueField.getValidators().add(numberValidator);

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			inputValues.put(name, Integer.valueOf(paramMap.get(name).getDefaultValueExpression().getText()));
			valueField.setText(paramMap.get(name).getDefaultValueExpression().getText());
		}

		// 値が変更された際にHasMapへ値を格納
		valueField.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				if (!valueField.validate()) {
					return;
				}
				;
				logger.debug("Typed: " + valueField.getText());
				inputValues.put(name, Integer.valueOf(valueField.getText()));
			}
		});

		if (entry.getValue().isReadOnly()) {
			valueField.setEditable(false);
			valueField.setDisable(true);
		}

		return valueField;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createDatePicker(Map.Entry<String, ClientInputControl> entry) {
		JFXDatePicker datePicker = new JFXDatePicker();
		datePicker.setPromptText(myResource.getString("common.message.pick_date"));
		datePicker.setConverter(new JFXDatePickerConverter());
		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		// TODO:JFXDatePikcerのJFXDatePickerContentからLabelを取り除く

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()
				&& !paramMap.get(name).getDefaultValueExpression().getText().isEmpty()
				&& !"\"\"".equals(paramMap.get(name).getDefaultValueExpression().getText())
				&& paramMap.get(name).getDefaultValueExpression().getText().length() > 2) {

			Date date = new Date();
			switch (paramMap.get(name).getDefaultValueExpression().getText().substring(0, 3)) {
			case "NOW":// NOW()
			case "TOD":// TODAY()
				date = new Date();
				break;
			case "DAT":// DATE(year,month,day)
				String[] args = paramMap.get(name).getDefaultValueExpression().getText()
						.substring(5, paramMap.get(name).getDefaultValueExpression().getText().length() - 1).split(",");
				// date = new Date(Integer.valueOf(args[0]),
				// Integer.valueOf(args[1]), Integer.valueOf(args[2]));
				try {
					date = new SimpleDateFormat("yyyy-MM-dd").parse(args[0] + "-" + args[1] + "-" + args[2]);
				} catch (ParseException e) {
					logger.error(e.getMessage(), e);
				}
				logger.debug(args[0] + "-" + args[1] + "-" + args[2]);
				break;
			default:
				Pattern datePattern = Pattern.compile("([0-9]{4})[^0-9]([0-9]{1,2})[^0-9]([0-9]{1,2})");
				Matcher dateMatcher = datePattern.matcher(paramMap.get(name).getDefaultValueExpression().getText());
				if (dateMatcher.find()) {
					try {
						date = new SimpleDateFormat("yyyy-MM-dd")
								.parse(dateMatcher.group(1) + "-" + dateMatcher.group(2) + "-" + dateMatcher.group(3));
					} catch (ParseException e) {
						logger.error(e.getMessage(), e);
					}
				}
			}

			inputValues.put(name, date);
			datePicker.setValue(date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate());

			// 帳票側に指定された型がStringの場合キャスト
			if ("java.lang.String".equals(paramMap.get(name).getValueClassName())) {
				inputValues.put(name, new SimpleDateFormat("yyyy-MM-dd").format(date));// 人事台帳
			}

			// 帳票側に指定された型がTimestampの場合キャスト
			if ("java.sql.Timestamp".equals(paramMap.get(name).getValueClassName())) {
				inputValues.put(name, new Timestamp(date.getTime()));
			}
			// 帳票側に指定された方がTimeの場合キャスト
			if ("java.sql.Time".equals(paramMap.get(name).getValueClassName())) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.YEAR, 1970);
				cal.set(Calendar.MONTH, Calendar.JANUARY);
				cal.set(Calendar.DATE, 1);
				java.sql.Time t = new java.sql.Time(cal.getTimeInMillis());
				inputValues.put(name, t);
			}
		}

		// 入力コントロールに定義された型を取得
		ClientDataType type = (ClientDataType) entry.getValue().getDataType();

		// 値が変更された際にHasMapへ値を格納
		datePicker.valueProperty().addListener((o, oldVal, newVal) -> {
			if (newVal != null) {
				logger.debug("Typed: " + datePicker.getValue());
				if (null == datePicker.getValue()) {
					return;
				}

				Object inputDate = null;
				try {
					inputDate = (Date) new SimpleDateFormat("yyyy-MM-dd").parse(datePicker.getValue().toString());
				} catch (ParseException e) {
					logger.error(e.getMessage(), e);
				}

				// 最大最小値を設定
				try {
					if (null != type.getMaxValue()) {
						Date maxDate = new SimpleDateFormat("yyyy-MM-dd").parse(type.getMaxValue());
						if (0 > maxDate.compareTo(((Date) inputDate))) {
							logger.error("The Date is not valid range.");
							showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
									myResource.getString("W04.dialog.error.max_date"));
							datePicker.setValue(null);
							return;
						}
					}

					if (null != type.getMinValue()) {
						Date minDate = new SimpleDateFormat("yyyy-MM-dd").parse(type.getMinValue());
						if (0 < minDate.compareTo(((Date) inputDate))) {
							logger.error("The Date is not valid range.");
							showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
									myResource.getString("W04.dialog.error.min_date"));
							datePicker.setValue(null);
							return;
						}
					}
				} catch (ParseException e) {
					logger.error(e.getMessage(), e);
				}

				switch (paramMap.get(name).getValueClassName()) {
				case "java.lang.String":
					inputDate = datePicker.getValue().toString();

					break;
				case "java.sql.Timestamp":
					inputDate = new Timestamp(((Timestamp) inputDate).getTime());
					break;
				case "java.sql.Time":
					Calendar calToTime = Calendar.getInstance();
					calToTime.setTime((Date) inputDate);
					calToTime.set(Calendar.YEAR, 1970);
					calToTime.set(Calendar.MONTH, Calendar.JANUARY);
					calToTime.set(Calendar.DATE, 1);
					inputDate = new java.sql.Time(calToTime.getTimeInMillis());
					break;
				case "java.sql.Date":
					Calendar calToDate = Calendar.getInstance();
					calToDate.setTime((Date) inputDate);
					calToDate.set(Calendar.HOUR_OF_DAY, 0);
					calToDate.set(Calendar.MINUTE, 0);
					calToDate.set(Calendar.SECOND, 0);
					calToDate.set(Calendar.MILLISECOND, 0);
					inputDate = new java.sql.Date(calToDate.getTimeInMillis());
					break;
				}
				inputValues.put(name, inputDate);
			} else {
				inputValues.put(name, null);
			}
		});

		if (entry.getValue().isReadOnly()) {
			datePicker.setEditable(false);
			datePicker.setDisable(true);
		}

		return datePicker;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createTimePicker(Map.Entry<String, ClientInputControl> entry) {
		JFXTimePicker blueDatePicker = new JFXTimePicker();
		blueDatePicker.setConverter(new JFXTimePickerConverter());
		blueDatePicker.setDefaultColor(Color.valueOf("#3f51b5"));
		blueDatePicker.setOverLay(true);
		blueDatePicker.setDialogParent(inputStack);
		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));
		String timeType = paramMap.get(name).getValueClassName();
		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()
				&& !paramMap.get(name).getDefaultValueExpression().getText().isEmpty()
				&& paramMap.get(name).getDefaultValueExpression().getText().length() > 2) {
			Pattern datePattern = Pattern.compile("([0-9]{2})[:：]([0-9]{2})");
			Matcher dateMatcher = datePattern.matcher(paramMap.get(name).getDefaultValueExpression().getText());
			if (dateMatcher.find()) {
				blueDatePicker.setValue(
						LocalTime.of(Integer.parseInt(dateMatcher.group(1)), Integer.parseInt(dateMatcher.group(2))));

				LocalTime time = blueDatePicker.getValue();
				if ("java.lang.String".equals(timeType)) {
					inputValues.put(name, time.toString());
				}

				// 帳票側に指定された型がTimestampの場合キャスト
				if ("java.sql.Timestamp".equals(timeType)) {
					LocalDate date = LocalDate.of(1970, 1, 1);
					LocalDateTime dateTime = LocalDateTime.of(date, time);
					inputValues.put(name, java.sql.Timestamp.valueOf(dateTime));
				}
				// 帳票側に指定された方がTimeの場合キャスト
				if ("java.sql.Time".equals(timeType)) {
					inputValues.put(name, java.sql.Time.valueOf(time));
				}
			}
		}

		// 入力コントロールに定義された型を取得
		ClientDataType type = (ClientDataType) entry.getValue().getDataType();

		// 値が変更された際にHasMapへ値を格納
		blueDatePicker.valueProperty().addListener((o, oldVal, newVal) -> {
			if (newVal != null) {
				logger.debug("Typed: " + blueDatePicker.getValue());
				if (null != blueDatePicker.getValue()) {

					// 最大値最小値を設定
					if (null != type.getMaxValue()) {
					}
					if (null != type.getMinValue()) {
					}

					LocalTime time = blueDatePicker.getValue();
					// 帳票側に指定された型がStringの場合キャスト
					if ("java.lang.String".equals(timeType)) {
						inputValues.put(name, time.toString());
					}

					// 帳票側に指定された型がTimestampの場合キャスト
					if ("java.sql.Timestamp".equals(timeType)) {
						LocalDate date = LocalDate.of(1970, 1, 1);
						LocalDateTime dateTime = LocalDateTime.of(date, time);
						inputValues.put(name, java.sql.Timestamp.valueOf(dateTime));
					}
					// 帳票側に指定された方がTimeの場合キャスト
					if ("java.sql.Time".equals(timeType)) {
						inputValues.put(name, java.sql.Time.valueOf(time));
					}
				}
			} else {
				inputValues.put(name, null);
			}

		});

		if (entry.getValue().isReadOnly()) {
			blueDatePicker.setEditable(false);
			blueDatePicker.setDisable(true);
		}

		return blueDatePicker;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createDateTimePicker(Map.Entry<String, ClientInputControl> entry) {
		VBox dateTimeBox = new VBox();
		JFXDatePicker dateTimePicker_Date = new JFXDatePicker();
		dateTimePicker_Date.setConverter(new JFXDatePickerConverter());
		dateTimePicker_Date.setPromptText(myResource.getString("common.message.pick_date"));
		JFXTimePicker dateTimePicker_Time = new JFXTimePicker();
		dateTimePicker_Time.setConverter(new JFXTimePickerConverter());
		dateTimePicker_Time.setDefaultColor(Color.valueOf("#3f51b5"));
		dateTimePicker_Time.setOverLay(true);
		dateTimePicker_Time.setDialogParent(inputStack);

		if (entry.getValue().isReadOnly()) {
			dateTimePicker_Date.setEditable(false);
			dateTimePicker_Time.setEditable(false);
			dateTimePicker_Date.setDisable(true);
			dateTimePicker_Time.setDisable(true);
		}

		dateTimeBox.getChildren().add(dateTimePicker_Date);
		dateTimeBox.getChildren().add(dateTimePicker_Time);

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));
		String dateTimeType = paramMap.get(name).getValueClassName();
		// 入力コントロールに定義された型を取得
		// ClientDataType type = (ClientDataType)
		// entry.getValue().getDataType();

		// 値が変更された際にHasMapへ値を格納
		dateTimePicker_Date.valueProperty().addListener((o, oldVal, newVal) -> {
			dateTimeListener(dateTimePicker_Date, dateTimePicker_Time, name, dateTimeType);
		});

		dateTimePicker_Time.valueProperty().addListener((o, oldVal, newVal) -> {
			dateTimeListener(dateTimePicker_Date, dateTimePicker_Time, name, dateTimeType);
		});

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()
				&& !paramMap.get(name).getDefaultValueExpression().getText().isEmpty()
				&& paramMap.get(name).getDefaultValueExpression().getText().length() > 2) {
			String defaultDateTime = paramMap.get(name).getDefaultValueExpression().getText();
			Pattern datePattern = Pattern.compile("([0-9]{4})[^0-9]([0-9]{1,2})[^0-9]([0-9]{1,2})");
			Pattern timePattern = Pattern.compile("([0-9]{1,2})[:：]([0-9]{1,2})");
			Matcher dateMatcher = datePattern.matcher(defaultDateTime);
			Matcher timeMatcher = timePattern.matcher(defaultDateTime);
			if (dateMatcher.find()) {
				dateTimePicker_Date.setValue(LocalDate.of(Integer.parseInt(dateMatcher.group(1)),
						Integer.parseInt(dateMatcher.group(2)), Integer.parseInt(dateMatcher.group(3))));
			}
			if (timeMatcher.find()) {
				dateTimePicker_Time.setValue(
						LocalTime.of(Integer.parseInt(timeMatcher.group(1)), Integer.parseInt(timeMatcher.group(2))));
			}
		}
		return dateTimeBox;
	}

	private void dateTimeListener(JFXDatePicker dateTimePicker_Date, JFXTimePicker dateTimePicker_Time, String name,
			String dateTimeType) {
		Date date = null;

		try {
			boolean timeEmpty = null == dateTimePicker_Time.getValue();
			boolean dateEmpty = null == dateTimePicker_Date.getValue();
			if (timeEmpty && !dateEmpty) {
				date = new SimpleDateFormat("yyyy-MM-dd").parse(dateTimePicker_Date.getValue().toString());
				timeEmpty = true;
			} else if (!timeEmpty && !dateEmpty) {
				date = new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(
						dateTimePicker_Date.getValue().toString() + " " + dateTimePicker_Time.getValue().toString());
			} else if (!timeEmpty && dateEmpty) {
				date = new SimpleDateFormat("HH:mm").parse(dateTimePicker_Time.getValue().toString());
			} else {
				inputValues.put(name, null);
				return;
			}

			Object inputDate = null;
			switch (dateTimeType) {
			case "java.lang.String":
				if (timeEmpty && !dateEmpty) {
					inputDate = new SimpleDateFormat("yyyy-MM-dd").format(date);
				} else if (!timeEmpty && !dateEmpty) {
					inputDate = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(date);
				} else if (!timeEmpty && dateEmpty) {
					inputDate = new SimpleDateFormat("HH:mm").format(date);
				} else {
					inputDate = null;
				}
				break;
			case "java.sql.Timestamp":
				inputDate = new Timestamp(date.getTime());
				break;
			case "java.sql.Time":
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				cal.set(Calendar.YEAR, 1970);
				cal.set(Calendar.MONTH, Calendar.JANUARY);
				cal.set(Calendar.DATE, 1);
				inputDate = new java.sql.Time(cal.getTimeInMillis());
				break;
			case "java.sql.Date":
				Calendar calToDate = Calendar.getInstance();
				calToDate.setTime(date);
				calToDate.set(Calendar.SECOND, 0);
				calToDate.set(Calendar.MILLISECOND, 0);
				inputDate = new java.sql.Date(calToDate.getTimeInMillis());
				break;
			}
			inputValues.put(name, inputDate);

		} catch (ParseException e1) {
			logger.error(e1.getMessage(), e1);
		}
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createSingleSelectionList(Map.Entry<String, ClientInputControl> entry) {
		staticParameterMap = InputControlService.getStaticItems(entry.getValue());
		parameterItems = InputControlService.getLabelItemList(staticParameterMap);
		ObservableList<ObservableItem> staticItems = FXCollections.observableArrayList();
		staticItems.add(new ObservableItem("", ""));
		for (Map.Entry<String, String> map : staticParameterMap.entrySet()) {
			staticItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}

		JFXComboBox<ObservableItem> originalComboBox = new JFXComboBox<ObservableItem>(staticItems);

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = paramMap.get(name).getDefaultValueExpression().getText().substring(1,
					paramMap.get(name).getDefaultValueExpression().getText().length() - 1);
			inputValues.put(name, "java.lang.Integer".equals(paramMap.get(name).getValueClassName())
					? Integer.valueOf(value) : value);

			for (ObservableItem oi : staticItems) {
				if (oi.getLabel().equals(value)) {
					originalComboBox.getSelectionModel().select(staticItems.get(staticItems.indexOf(oi)));
					break;
				}
			}
		}

		// 値が変更された際にHasMapへ値を格納
		originalComboBox.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				if (null == originalComboBox.getValue()) {
					return;
				}
				if ("".equals(originalComboBox.getValue().getLabel())) {
					inputValues.put(name, null);
					return;
				}
				logger.debug("Typed: " + originalComboBox.getValue().getValue());
				logger.debug("Value:" + originalComboBox.getValue().getLabel());
				inputValues.put(name,
						"java.lang.Integer".equals(paramMap.get(name).getValueClassName())
								? Integer.valueOf(originalComboBox.getValue().getLabel())
								: originalComboBox.getValue().getLabel());
			}
		});

		if (entry.getValue().isReadOnly()) {
			originalComboBox.setEditable(false);
			originalComboBox.setDisable(true);
		}

		return originalComboBox;
	}

	/**
	 * @param clientReportUnit
	 * @param entry
	 * @return
	 */
	private Node createSingleSelectionQueryList(ClientReportUnit clientReportUnit,
			Map.Entry<String, ClientInputControl> entry) {
		queryParameterMap = InputControlService.getQueryItems(entry.getValue(), clientReportUnit, inputValues);

		// クエリ実行結果が空の場合、空のコンボボックスを追加
		if (null == queryParameterMap) {
			JFXComboBox<String> emptyComboBox = new JFXComboBox<String>();
			emptyComboBox.setDisable(true);
			return emptyComboBox;
		}
		ObservableList<ObservableItem> queryItems = FXCollections.observableArrayList();

		// 先頭に空の選択肢を追加
		if (!queryParameterMap.keySet().contains(""))
			queryItems.add(new ObservableItem("", ""));

		for (Map.Entry<String, String> map : queryParameterMap.entrySet()) {
			queryItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}
		queryParameterMap = null;

		JFXComboBox<ObservableItem> originalqueryComboBox = new JFXComboBox<ObservableItem>(queryItems);

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()
				&& !paramMap.get(name).getDefaultValueExpression().getText().isEmpty()
				&& paramMap.get(name).getDefaultValueExpression().getText().length() > 2) {
			String defaultKey = paramMap.get(name).getDefaultValueExpression().getText();
			defaultKey = defaultKey.substring(1, defaultKey.length() - 1);
			for (ObservableItem queryItem : queryItems) {
				if (queryItem.getLabel().equals(defaultKey)) {
					originalqueryComboBox.setValue(queryItem);
					inputValues.put(name, "java.lang.Integer".equals(paramMap.get(name).getValueClassName())
							? Integer.valueOf(defaultKey) : defaultKey);
					break;
				}
			}
		}

		// 値が変更された際にHasMapへ値を格納
		originalqueryComboBox.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				if (null == originalqueryComboBox.getValue()) {
					return;
				}
				if ("".equals(originalqueryComboBox.getValue().getLabel())) {
					inputValues.put(name, null);
					return;
				}
				logger.debug("Typed: " + originalqueryComboBox.getValue().getValue());
				logger.debug("Value:" + originalqueryComboBox.getValue().getLabel());
				logger.debug("Changed: " + name);
				inputValues.put(name,
						"java.lang.Integer".equals(paramMap.get(name).getValueClassName())
								? Integer.valueOf(originalqueryComboBox.getValue().getLabel())
								: originalqueryComboBox.getValue().getLabel());
			}
		});

		if (entry.getValue().isReadOnly()) {
			originalqueryComboBox.setEditable(false);
			originalqueryComboBox.setDisable(true);
		}

		return originalqueryComboBox;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createSingleSelectionRadioList(Map.Entry<String, ClientInputControl> entry) {
		ToggleGroup group;
		JFXListView<JFXRadioButton> radioList = new JFXListView<JFXRadioButton>();
		radioList.setPrefHeight(100);
		radioList.setMaxHeight(200);

		staticParameterMap = InputControlService.getStaticItems(entry.getValue());
		parameterItems = InputControlService.getLabelItemList(staticParameterMap);
		group = new ToggleGroup();

		ObservableList<ObservableItem> radioItems = FXCollections.observableArrayList();
		for (Map.Entry<String, String> map : staticParameterMap.entrySet()) {
			radioItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		for (ObservableItem item : radioItems) {
			JFXRadioButton radioButton = new JFXRadioButton(item.toString());
			radioButton.setToggleGroup(group);
			radioButton.setUserData(item.getLabel());

			// 値が変更された際にHasMapへ値を格納
			radioButton.focusedProperty().addListener((o, oldVal, newVal) -> {
				if (!newVal) {
					JFXRadioButton selectedButton = (JFXRadioButton) group.getSelectedToggle();

					if (null == selectedButton) {
						return;
					}
					logger.debug("Selected label:" + selectedButton.getText());
					logger.debug("Selected value:" + selectedButton.getUserData());

					inputValues.put(name, "java.lang.Integer".equals(paramMap.get(name).getValueClassName())
							? Integer.valueOf(selectedButton.getUserData().toString()) : selectedButton.getUserData());
				}
			});
			radioList.getItems().add(radioButton);
		}

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = paramMap.get(name).getDefaultValueExpression().getText().substring(1,
					paramMap.get(name).getDefaultValueExpression().getText().length() - 1);

			for (ObservableItem oi : radioItems) {
				if (oi.getLabel().equals(value)) {
					radioList.getItems().get(radioItems.indexOf(oi)).setSelected(true);
					inputValues.put(name, "java.lang.Integer".equals(paramMap.get(name).getValueClassName())
							? Integer.valueOf(value) : value);
					break;
				}
			}
		}

		if (entry.getValue().isReadOnly()) {
			radioList.setEditable(false);
			radioList.setDisable(true);
		}

		return radioList;
	}

	/**
	 * @param clientReportUnit
	 * @param entry
	 * @return
	 */
	private Node createSingleSelectionRadioQueryList(ClientReportUnit clientReportUnit,
			Map.Entry<String, ClientInputControl> entry) {
		ToggleGroup group;
		JFXListView<JFXRadioButton> radioList = new JFXListView<JFXRadioButton>();
		radioList.setPrefHeight(100);
		radioList.setMaxHeight(200);

		queryParameterMap = InputControlService.getQueryItems(entry.getValue(), clientReportUnit, inputValues);

		// クエリ実行結果が空の場合、空のラジオボタンリストを追加
		if (null == queryParameterMap) {
			JFXListView<JFXRadioButton> emptyRadioList = new JFXListView<JFXRadioButton>();
			emptyRadioList.setDisable(true);
			emptyRadioList.setPrefHeight(50);
			return emptyRadioList;
		}

		parameterItems = InputControlService.getLabelItemList(queryParameterMap);
		group = new ToggleGroup();

		ObservableList<ObservableItem> radioQueryItems = FXCollections.observableArrayList();
		for (Map.Entry<String, String> map : queryParameterMap.entrySet()) {
			radioQueryItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}
		queryParameterMap = null;

		for (ObservableItem item : radioQueryItems) {
			JFXRadioButton radioButton = new JFXRadioButton(item.toString());
			radioButton.setToggleGroup(group);

			radioButton.setUserData(item.getLabel());

			// 値が変更された際にHasMapへ値を格納
			radioButton.focusedProperty().addListener((o, oldVal, newVal) -> {
				if (!newVal) {
					JFXRadioButton selectedButton = (JFXRadioButton) group.getSelectedToggle();

					if (null == selectedButton) {
						return;
					}

					logger.debug("Selected label:" + selectedButton.getText());
					logger.debug("Selected value:" + selectedButton.getUserData());

					inputValues.put(
							entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1)),
							selectedButton.getUserData());
				}
			});

			radioList.getItems().add(radioButton);
		}

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = paramMap.get(name).getDefaultValueExpression().getText().substring(1,
					paramMap.get(name).getDefaultValueExpression().getText().length() - 1);

			for (ObservableItem oi : radioQueryItems) {
				if (oi.getLabel().equals(value)) {
					radioList.getItems().get(radioQueryItems.indexOf(oi)).setSelected(true);
					inputValues.put(name, "java.lang.Integer".equals(paramMap.get(name).getValueClassName())
							? Integer.valueOf(value) : value);
					break;
				}
			}

		}

		if (entry.getValue().isReadOnly()) {
			radioList.setEditable(false);
			radioList.setDisable(true);
		}

		return radioList;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createMultiSelectionCheckList(Map.Entry<String, ClientInputControl> entry) {
		JFXListView<JFXCheckBox> checkList = new JFXListView<JFXCheckBox>();
		checkList.setPrefHeight(100);
		checkList.setMaxHeight(200);
		staticParameterMap = InputControlService.getStaticItems(entry.getValue());
		parameterItems = InputControlService.getLabelItemList(staticParameterMap);

		ObservableList<ObservableItem> checkBoxItems = FXCollections.observableArrayList();
		for (Map.Entry<String, String> map : staticParameterMap.entrySet()) {
			checkBoxItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}

		// 値格納用リスト
		List<String> valueList = new ArrayList<String>();
		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));
		for (ObservableItem item : checkBoxItems) {
			JFXCheckBox multiCheckBox = new JFXCheckBox(item.toString());
			multiCheckBox.setUserData(item.getLabel());

			// 値が変更された際にHasMapへ値を格納
			multiCheckBox.selectedProperty().addListener((o, oldVal, newVal) -> {
				JFXCheckBox selectedButton = multiCheckBox;
				logger.debug("Selected label:" + selectedButton.getText());
				logger.debug("Selected value:" + selectedButton.getUserData());

				// 選択された場合追加
				if (selectedButton.isSelected()) {
					valueList.add((String) selectedButton.getUserData());
					logger.debug("added.");
				} else {
					// 既に含まれていた場合削除
					if (valueList.contains(selectedButton.getUserData())) {
						valueList.remove((String) selectedButton.getUserData());
						logger.debug("removed.");
					}
				}

				inputValues.put(name, valueList);
			});

			checkList.getItems().add(multiCheckBox);
		}

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = paramMap.get(name).getDefaultValueExpression().getText();
			value = value.substring(14, value.length() - 1);
			String[] paramaters = value.split(",");
			for (int i = 0; i < paramaters.length; i++) {
				paramaters[i] = paramaters[i].substring(1, paramaters[i].length() - 1);
				for (ObservableItem oi : checkBoxItems) {
					if (oi.getLabel().equals(paramaters[i])) {
						checkList.getItems().get(checkBoxItems.indexOf(oi)).setSelected(true);
						break;
					}
				}
			}
		}

		if (entry.getValue().isReadOnly()) {
			checkList.setEditable(false);
			checkList.setDisable(true);
		}

		return checkList;
	}

	/**
	 * @param clientReportUnit
	 * @param entry
	 * @return
	 */
	private Node createMultiSelectionCheckQueryList(ClientReportUnit clientReportUnit,
			Map.Entry<String, ClientInputControl> entry) {
		JFXListView<JFXCheckBox> checkList = new JFXListView<JFXCheckBox>();
		checkList.setPrefHeight(100);
		checkList.setMaxHeight(200);
		queryParameterMap = InputControlService.getQueryItems(entry.getValue(), clientReportUnit, inputValues);

		// クエリ実行結果が空の場合、空のチェックボックスリストを追加
		if (null == queryParameterMap) {
			JFXListView<JFXCheckBox> emptyCheckBoxList = new JFXListView<JFXCheckBox>();
			emptyCheckBoxList.setDisable(true);
			emptyCheckBoxList.setPrefHeight(50);
			return emptyCheckBoxList;
		}
		parameterItems = InputControlService.getLabelItemList(queryParameterMap);

		ObservableList<ObservableItem> checkBoxQueryItems = FXCollections.observableArrayList();
		for (Map.Entry<String, String> map : queryParameterMap.entrySet()) {
			checkBoxQueryItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}
		queryParameterMap = null;

		// 値格納用リスト
		List<String> valueQueryList = new ArrayList<String>();
		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));
		for (ObservableItem item : checkBoxQueryItems) {
			JFXCheckBox multiCheckBox = new JFXCheckBox(item.toString());
			multiCheckBox.setUserData(item.getLabel());

			// 値が変更された際にHasMapへ値を格納
			multiCheckBox.selectedProperty().addListener((o, oldVal, newVal) -> {
				// JFXCheckBox selectedButton =
				// checkList.getSelectionModel().getSelectedItem();
				JFXCheckBox selectedButton = multiCheckBox;
				logger.debug("Selected label:" + selectedButton.getText());
				logger.debug("Selected value:" + selectedButton.getUserData());

				// 選択された場合追加
				if (selectedButton.isSelected()) {
					valueQueryList.add((String) selectedButton.getUserData());
					logger.debug("added.");
				} else {
					// 既に含まれていた場合削除
					if (valueQueryList.contains(selectedButton.getUserData())) {
						valueQueryList.remove((String) selectedButton.getUserData());
						logger.debug("removed.");
					}
				}

				inputValues.put(name, valueQueryList);
			});

			checkList.getItems().add(multiCheckBox);
		}

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = paramMap.get(name).getDefaultValueExpression().getText();
			value = value.substring(14, value.length() - 1);
			String[] paramaters = value.split(",");
			for (int i = 0; i < paramaters.length; i++) {
				paramaters[i] = paramaters[i].substring(1, paramaters[i].length() - 1);
				for (ObservableItem oi : checkBoxQueryItems) {
					if (oi.getLabel().equals(paramaters[i])) {
						checkList.getItems().get(checkBoxQueryItems.indexOf(oi)).setSelected(true);
						break;
					}
				}
			}
		}

		if (entry.getValue().isReadOnly()) {
			checkList.setEditable(false);
			checkList.setDisable(true);
		}

		return checkList;
	}

	/**
	 * @param entry
	 * @return
	 */
	private Node createMultiSelectionList(Map.Entry<String, ClientInputControl> entry) {
		staticParameterMap = InputControlService.getStaticItems(entry.getValue());
		parameterItems = InputControlService.getLabelItemList(staticParameterMap);

		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		ObservableList<ObservableItem> multiStaticItems = FXCollections.observableArrayList();
		for (Map.Entry<String, String> map : staticParameterMap.entrySet()) {
			multiStaticItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}
		C02MultiSelectionList<ObservableItem> multiSelectBox = new C02MultiSelectionList<ObservableItem>(
				multiStaticItems);

		// 値が変更された際にHasMapへ値を格納
		multiSelectBox.list.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				if (multiSelectBox.getValue().isEmpty())
					return;
				List<String> valueList = new ArrayList<String>();
				for (ObservableItem s : multiSelectBox.getValue()) {
					valueList.add(s.getLabel());
				}
				logger.debug("Selected value:" + multiSelectBox.getValue().toString());
				inputValues.put(name, valueList);
			}
		});
		multiSelectBox.selectAll.setOnMouseClicked((MouseEvent event) -> {
			List<String> valueList = new ArrayList<String>();
			for (String value : staticParameterMap.keySet()) {
				valueList.add(value);
			}
			inputValues.put(name, valueList);
			logger.debug("All selected.");
		});
		multiSelectBox.unselectAll.setOnMouseClicked((MouseEvent event) -> {
			inputValues.put(name, null);
			logger.debug("Selection released.");
		});

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = paramMap.get(name).getDefaultValueExpression().getText();
			value = value.substring(14, value.length() - 1);
			String[] paramaters = value.split(",");
			List<String> valueList = new ArrayList<String>();
			for (int i = 0; i < paramaters.length; i++) {
				paramaters[i] = paramaters[i].substring(1, paramaters[i].length() - 1);
				for (ObservableItem oi : multiStaticItems) {
					if (oi.getLabel().equals(paramaters[i])) {
						multiSelectBox.list.getSelectionModel().select(multiStaticItems.indexOf(oi));
						valueList.add(oi.getLabel());
						break;
					}
				}
			}
			inputValues.put(name, valueList);
		}

		if (entry.getValue().isReadOnly()) {
			multiSelectBox.setEditable(false);
		}

		return multiSelectBox;
	}

	/**
	 * @param clientReportUnit
	 * @param entry
	 * @return
	 */
	private Node createMultiSelectionQueryList(ClientReportUnit clientReportUnit,
			Map.Entry<String, ClientInputControl> entry) {
		queryParameterMap = InputControlService.getQueryItems(entry.getValue(), clientReportUnit, inputValues);
		String name = entry.getValue().getUri().substring((entry.getValue().getUri().lastIndexOf("/") + 1));

		// クエリ実行結果が空の場合、空の複数選択スリストを追加
		if (null == queryParameterMap) {
			C02MultiSelectionList<ObservableItem> emptyMultiSelectionList = new C02MultiSelectionList<ObservableItem>();
			emptyMultiSelectionList.setDisable(true);
			return emptyMultiSelectionList;
		}

		parameterItems = InputControlService.getLabelItemList(queryParameterMap);

		ObservableList<ObservableItem> multiQueryItems = FXCollections.observableArrayList();
		for (Map.Entry<String, String> map : queryParameterMap.entrySet()) {
			multiQueryItems.add(new ObservableItem(map.getKey(), map.getValue()));
		}
		C02MultiSelectionList<ObservableItem> multiQuerySelectBox = new C02MultiSelectionList<ObservableItem>(
				multiQueryItems);
		// 値が変更された際にHasMapへ値を格納
		multiQuerySelectBox.list.focusedProperty().addListener((o, oldVal, newVal) -> {
			if (!newVal) {
				List<String> valueList = new ArrayList<String>();
				for (ObservableItem s : multiQuerySelectBox.getValue()) {
					valueList.add(s.getLabel());
				}
				logger.debug("Selected value:" + multiQuerySelectBox.getValue().toString());
				inputValues.put(name, valueList);
			}
		});

		multiQuerySelectBox.selectAll.setOnMouseClicked((MouseEvent event) -> {
			List<String> valueList = new ArrayList<String>();
			for (ObservableItem value : multiQueryItems) {
				valueList.add(value.getLabel());
			}
			inputValues.put(name, valueList);
			logger.debug("All selected.");
			logger.debug("inputValues" + valueList);

		});
		multiQuerySelectBox.unselectAll.setOnMouseClicked((MouseEvent event) -> {
			inputValues.put(name, null);
			logger.debug("Selection released.");
			logger.debug("inputValues null");
		});

		queryParameterMap = null;

		// 初期値の値が設定されている場合、初期値を設定
		if (null != paramMap.get(name) && null != paramMap.get(name).getDefaultValueExpression()) {
			String value = paramMap.get(name).getDefaultValueExpression().getText();
			value = value.substring(14, value.length() - 1);
			String[] paramaters = value.split(",");
			List<String> valueList = new ArrayList<String>();
			for (int i = 0; i < paramaters.length; i++) {
				paramaters[i] = paramaters[i].substring(1, paramaters[i].length() - 1);
				for (ObservableItem oi : multiQueryItems) {
					if (oi.getLabel().equals(paramaters[i])) {
						multiQuerySelectBox.list.getSelectionModel().select(multiQueryItems.indexOf(oi));
						valueList.add(oi.getLabel());
						break;
					}
				}
			}
			inputValues.put(name, valueList);
		}

		if (entry.getValue().isReadOnly()) {
			multiQuerySelectBox.setEditable(false);
		}

		return multiQuerySelectBox;
	}

	/**
	 * レポート実行画面からエクスポートする際は内部でJasperPrintオブジェクトを再利用して、リポジトリ画面からエクスポートする際よりパフォーマンスが良くする。
	 * 
	 * @param controller
	 * @return true/false
	 * @author panyuan
	 */
	private Boolean exportOffline(W04InputControlController controller) {

		/** 別のW04InputControlControllerから初期化する */
		this.reportUri = controller.reportUri;
		this.jrPrint = controller.jrPrint;
		this.exportFileformat = controller.exportFileformatTmp; // use Tmp!
		this.clientReportUnit = controller.clientReportUnit;
		this.reportExcecute = controller.reportExcecute;
		this.reportErrorService = controller.reportErrorService;
		this.inputValues = controller.inputValues;
		this.excelDefinition = controller.excelDefinition;
		this.excelJob = controller.excelJob;
		setObject(null);

		/**
		 * 入力コントロールパネルの操作 <br>
		 * ・クリアボタンの消去 <br>
		 * ・スクロールパネルの非表示<br>
		 * ・[適用]ボタンのラベルを[再実行]に変更<br>
		 * ・ボタンの動作をリセットする
		 */
		inputControlPane.setVisible(false);
		ResourceBundle resourceBundle = ResourceBundle.getBundle(Constant.Application.MY_BUNDLE);
		JFXButton rerun = (JFXButton) inputControlHBox.getChildren().get(0);
		rerun.setText(resourceBundle.getString("W04.button.rerun"));
		rerun.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent evt) {
				try {
					report(null, false);
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		});
		inputControlHBox.getChildren().clear();
		inputControlHBox.getChildren().add(rerun);

		/** エクスポート実行 */
		Boolean result = report(null, false);
		setObject(result);
		return result;
	}

	/**
	 * 適用ボタンが押下されたときのJasperレポートの生成処理<br>
	 * デフォルトでサーバーからデータを取得する
	 *
	 * @param event
	 * @return true/false
	 * @author panyuan
	 */
	public Boolean report(ActionEvent event) {
		return report(event, true);
	}

	/**
	 * 適用ボタンが押下されたときのJasperレポートの生成処理<br>
	 * 或いはサーバーからデータを取得する、或いは取得しない
	 *
	 * @param event
	 * @param getDataFlag
	 * @return true/false
	 */
	public Boolean report(ActionEvent event, boolean getDataFlag) {

		// nullの必須項目があればエラーを出力する
		if (null != inputValues) {
			for (Map.Entry<String, Object> e : inputValues.entrySet()) {
				if (null == parameterMap)
					break;

				if (parameterMap.get(e.getKey()).isMandatory()) {
					if (null == e.getValue()) {
						showDialog(AlertType.ERROR, myResource.getString("common.error.dialog.title"),
								myResource.getString("W04.error.message.execute"));
						return null;
					}
				}
			}
		}

		if (null == clientReportUnit) {
			if (!reportErrorService.hasError()) {
				reportErrorService.clearErrorLog();
				reportErrorService.addErrorLog(Arrays.asList(ReportErrors.ERROR_UNKNOW));
				setErrorLog(reportErrorService.getErrorLogWithFormat());
			}
			Platform.runLater(() -> {
				ReportStackPane.getChildren().clear();
				ReportStackPane.getChildren().add(failedPane);
			});
			logger.error("Report does not exist.");
			return null;
		}

		if (null != exportFileformat) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setInitialFileName(
					clientReportUnit.getUri().substring(clientReportUnit.getUri().lastIndexOf("/") + 1));
			fileChooser.setInitialDirectory(new File(StringUtils.isEmpty(Constant.ServerInfo.workspace)
					? System.getProperty("user.home") : Constant.ServerInfo.exportFolderPath));
			switch (exportFileformat) {
			case Constant.ExportFileformat.CSV:
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("CSV Files", "*.csv"));
				fileChooser.setTitle(myResource.getString("W04.file_chooser.csv.window.title"));
				break;
			case Constant.ExportFileformat.XLSX:
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("XLSX Files", "*.xlsx"));
				fileChooser.setTitle(myResource.getString("W04.file_chooser.xlsx.window.title"));
				break;
			case Constant.ExportFileformat.PDF:
				fileChooser.getExtensionFilters().addAll(new ExtensionFilter("PDF Files", "*.pdf"));
				fileChooser.setTitle(myResource.getString("W04.file_chooser.pdf.window.title"));
				break;
			default:
				logger.warn("unknow exportFileformat found.");
				break;
			}
			saveFile = fileChooser.showSaveDialog(null);
		}

		// exportFileformatに値が設定されているが、fileがnullの場合 = エクスポートのキャンセル
		if (saveFile == null && exportFileformat != null) {
			logger.info("Export cannceled.");
			// 入力コントロールを含まない場合
			if (null == clientReportUnit.getInputControls()) {
				return false;
			}
			return false;
		}

		// プログレスサークルを表示
		if (null != ReportStackPane) {
			Platform.runLater(() -> {
				ReportStackPane.getChildren().clear();
				ReportStackPane.getChildren().add(spinnerPane);
			});
		}

		logger.debug("inputValues");
		logger.debug(inputValues);

		runReportTask = new Task<JasperPrint>() {
			@Override
			protected JasperPrint call() throws Exception {
				// レポート実行時間を計測
				long start = System.currentTimeMillis();
				updateValue(null);

				boolean flag = true;
				while (!isCancelled() && flag) {

					if (getDataFlag) {
						// reportExcecuteを初期化して、jrPrintを取る
						if (excelDefinition != null) {
							reportExcecute = new ReportExcecuteService(excelDefinition, inputValues);
						} else {
							reportExcecute = new ReportExcecuteService(reportUri, saveFile, inputValues);
						}

						logger.info("Create JasperPrint started.");
						try {
							hashMap = reportExcecute.createJasperPrint();
							jrPrint = hashMap.keySet().iterator().next();
						} catch (Throwable e) {
							logger.error(e.getMessage(), e);
							reportExcecute.addError(e);
						}
						logger.info("Create JasperPrint finished.");

					} else {
						// レポート実行画面からエクスポートする場合はオフラインでも実行できる
						reportExcecute.setExportFile(saveFile);
						reportExcecute.clearErrorLog();
						ReportCreateServiceImpl reportCreateService = new ReportCreateServiceImpl();
						logger.info("Export JasperPrint offline started.");
						try {
							reportCreateService.exportJasperPrint(saveFile, jrPrint);
							reportExcecute.addErrorLog(reportCreateService.getErrorLog());
						} catch (Throwable e) {
							jrPrint = null;
							logger.error(e.getMessage(), e);
							reportExcecute.addError(e);
						}
						logger.info("Export JasperPrint offline finished.");
					}

					if (excelDefinition != null) {
						ExcelCooperationService excelCooperationService;
						if (excelDefinition.isLowMemoryFlag())
							excelCooperationService = new ExcelSXSSFService(excelJob, 0, jrPrint);
						else
							excelCooperationService = new ExcelXSSFService(excelJob, 0, jrPrint);

						String saveFilePath = null;
						if (excelDefinition.getTargetColumns().equals("ALL"))
							saveFilePath = excelCooperationService.executeJob(null, excelJob.getSaveExcelPath());
						else
							saveFilePath = excelCooperationService.executeJob(excelDefinition.getTargetColumns(),
									excelJob.getSaveExcelPath());

						if (hashMap != null) {
							JRSwapFileVirtualizer swapFile = hashMap.values().iterator().next();
							if (swapFile != null)
								swapFile.cleanup();
						}

						// ジョブの実行に失敗したらnullが返ってくるので、jrPrintをNULLにしてエラーペインを表示させる。
						if (StringUtils.isEmpty(saveFilePath)) {
							jrPrint = null;
							setObject(false);
						} else {
							Platform.runLater(() -> {
								// 現在のウィンドウ幅を取得し、セットする。
								preferences.setReportWindowHeight(String.valueOf(stage.getHeight()));
								preferences.setReportWindowWidth(String.valueOf(stage.getWidth()));

								// 現在のウィンドウの位置を取得し、セットする。
								preferences.setReportWindowX(String.valueOf(stage.getX()));
								preferences.setReportWindowY(String.valueOf(stage.getY()));

								// パラメータの有無を判断し、デバイダーの位置情報をセットする。
								if (null != clientReportUnit && null == clientReportUnit.getInputControls()) {
									preferences.setNonInputControlDivider(
											String.valueOf(splitPane.getDividerPositions()[0]));
								} else {
									preferences.setInputControlDivider(String.valueOf(splitPane.getDividerPositions()[0]));
								}

								// 上書き保存
								Marshaller m;
								try {
									m = context.createMarshaller();
									m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
									m.marshal(preferences, file);
								} catch (Exception e) {
									logger.error(e.getMessage(), e);
								}
								// Windowを閉じる
								inputControlAnchorPane.getScene().getWindow().hide();
							});
							setObject(true);
							return null;
						}
					}
					flag = false;
				}
				
				// キャンセル時
				if (flag) {
					updateMessage("Cancelled");
					return null;
				}
				
				// レポート実行失敗時
				if (null == jrPrint || reportExcecute.hasError()) {
					reportErrorService.clearErrorLog();
					reportErrorService.addErrorLog(reportExcecute.getErrorLog());
					setErrorLog(reportErrorService.getErrorLogWithFormat());
					Platform.runLater(() -> {
						ReportStackPane.getChildren().clear();
						ReportStackPane.getChildren().add(failedPane);
					});
					return null;
				}

				if (jrPrint.getPages().size() == 0) {
					jasperViewer = new JasperViewer(null);
					Platform.runLater(() -> showDialog(AlertType.INFORMATION,
							myResource.getString("common.confirmation.dialog.title"),
							myResource.getString("W04.message.no_pages")));
				} else {
					// レポートを生成する
					jasperViewer = new JasperViewer(jrPrint);
				}

				// 下にページ番号のフォントサイズを設定する
				Platform.runLater(() -> setBottomPageNumber(jasperViewer));

				// デフォルトで押されたステータスように「BtnFitWidth」というボタンを設定する
				Platform.runLater(() -> clickBtnFitWidthButton(jasperViewer));

				// エクスポートイベントを設定する
				Platform.runLater(() -> setExportOfflineActionEvent(jasperViewer));

				// レポートを表示する
				if (null == exportFileformat && excelDefinition == null) {
					Platform.runLater(() -> {
						ReportStackPane.getChildren().clear();
						ReportStackPane.getChildren().add(swingNode);
						swingNode.setContent(jasperViewer.getRootPane());
						// 保存ボタンを有効化
						saveButton.setDisable(false);
					});
				}
				
				// 完了画面を表示する
				if (null != exportFileformat && null != ReportStackPane) {
					Platform.runLater(() -> {
						ReportStackPane.getChildren().clear();
						ReportStackPane.getChildren().add(completePane);
					});
				}

				logger.info("Execution Time: " + (System.currentTimeMillis() - start) / 1000f + "s");

				/**
				 * レポート実行画面からエクスポートするため、jrPrintをnullにならないほうがいいでしょうか 
				 * @author panyuan
				 */
				// jrPrint = null;

				return null;
			}
		};

		thread = new Thread(runReportTask);
		thread.setDaemon(true);
		thread.start();
		logger.info("The report runned");
		logger.debug(inputValues);
		return true;
	}
	
	/**
	 * エラーログをセットする
	 * 
	 * @param log
	 * @author panyuan
	 */
	private void setErrorLog(String log) {
		Platform.runLater(() -> {
			errorLog.setText(log);
			errorLog.getStylesheets().add("view/error.css");
			errorLog.getStyleClass().add("report-error-text-font");
		});
	}

	/**
	 * 下にページ番号のフォントサイズを設定する<br>
	 * スタイル: BOLD<br>
	 * サイズ: 15<br>
	 * 
	 * @param jasperViewer
	 * @author panyuan
	 */
	private void setBottomPageNumber(JasperViewer jasperViewer) {
		try {
			JLayeredPane JLayeredPane1 = (JLayeredPane) jasperViewer.getRootPane().getComponent(1);
			JPanel JPanel1 = (JPanel) JLayeredPane1.getComponent(0);
			JPanel JPanel2 = (JPanel) JPanel1.getComponent(0);
			JRViewer JRViewer1 = (JRViewer) JPanel2.getComponent(0);
			JPanel pnlStatus = JRViewer1.getPnlStatus();
			JLabel JLabel1 = (JLabel) pnlStatus.getComponent(0);
			Font font = JLabel1.getFont();
			JLabel1.setFont(new Font(font.getName(), Font.BOLD, 15));

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * デフォルトで押されたステータスように「BtnFitWidth」というボタンを設定する<br>
	 * 
	 * @param jasperViewer
	 * @author panyuan
	 */
	private void clickBtnFitWidthButton(JasperViewer jasperViewer) {
		try {
			JLayeredPane JLayeredPane1 = (JLayeredPane) jasperViewer.getRootPane().getComponent(1);
			JPanel JPanel1 = (JPanel) JLayeredPane1.getComponent(0);
			JPanel JPanel2 = (JPanel) JPanel1.getComponent(0);
			JRViewer JRViewer1 = (JRViewer) JPanel2.getComponent(0);
			JRViewerToolbox tlbToolBox = JRViewer1.getTlbToolBox();
			JRViewerToolbar toolBarDefault = tlbToolBox.getToolBarDefault();
			JToggleButton btnFitWidthButton = toolBarDefault.getbBtnFitWidthButton();
			btnFitWidthButton.doClick();

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	/**
	 * オフラインでさまざまなエクスポートイベントを設定する<br>
	 * 1. csv<br>
	 * 2. xlsx<br>
	 * 3. pdf<br>
	 * 
	 * @param jasperViewer
	 * @author panyuan
	 */
	private void setExportOfflineActionEvent(JasperViewer jasperViewer) {
		try {
			JLayeredPane JLayeredPane1 = (JLayeredPane) jasperViewer.getRootPane().getComponent(1);
			JPanel JPanel1 = (JPanel) JLayeredPane1.getComponent(0);
			JPanel JPanel2 = (JPanel) JPanel1.getComponent(0);
			JRViewer JRViewer1 = (JRViewer) JPanel2.getComponent(0);
			JRViewerToolbox tlbToolBox = JRViewer1.getTlbToolBox();
			JRViewerToolbarExport toolBarExport = tlbToolBox.getToolBarExport();

			// csv
			JButton btnSaveCsv = toolBarExport.getBtnExpCsv();
			btnSaveCsv.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent event) {
					Platform.runLater(() -> {
						try {
							exportOffline(null, Constant.ExportFileformat.CSV);
						} catch (Throwable e) {
							logger.error(e.getMessage(), e);
						}
					});
				}
			});

			// xlsx
			JButton btnSaveXlsx = toolBarExport.getBtnExpXlsx();
			btnSaveXlsx.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent event) {
					Platform.runLater(() -> {
						try {
							exportOffline(null, Constant.ExportFileformat.XLSX);
						} catch (Throwable e) {
							logger.error(e.getMessage(), e);
						}
					});
				}
			});

			// pdf
			JButton btnSavePdf = toolBarExport.getBtnExpPdf();
			btnSavePdf.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent event) {
					Platform.runLater(() -> {
						try {
							exportOffline(null, Constant.ExportFileformat.PDF);
						} catch (Throwable e) {
							logger.error(e.getMessage(), e);
						}
					});
				}
			});

		} catch (Throwable e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * オフラインでさまざまなレポートをエクスポートする、<br>
	 * 1. csv<br>
	 * 2. xlsx<br>
	 * 3. pdf<br>
	 * 
	 * @param event
	 * @param exportFileformat
	 * @throws IOException
	 * @author panyuan
	 */
	private void exportOffline(java.awt.event.ActionEvent event, String exportFileformat) throws IOException {

		// エクスポートのパラメータの設定
		this.exportFileformatTmp = exportFileformat;
		setObject(this);

		// 入力コントロールがある場合は、レポート実行画面を表示
		Stage reportStage = showPane(event, "/view/W04InputControlAnchorPane.fxml", clientReportUnit.getLabel(),
				Modality.NONE, null);

		// 画面サイズの取得
		loadWindowSize();
		Double displayWidth = virtualBounds.getWidth();

		// ウィンドウ位置が記録されているか確認。
		if (preferences.getReportWindowY() != null && preferences.getReportWindowX() != null) {
			// 記録されたウィンドウ位置
			Double preferencesX = Double.parseDouble(preferences.getReportWindowX());
			Double preferencesY = Double.parseDouble(preferences.getReportWindowY());
			Double preferencesWidth = Double.parseDouble(preferences.getReportWindowWidth());
			Double preferencesHeight = Double.parseDouble(preferences.getReportWindowHeight());

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

		// エクスポートがキャンセルされた場合
		if (null != object && object instanceof Boolean && false == (Boolean) object) {
			setObject(null);
			return;
		}

		reportStage.setOnCloseRequest((WindowEvent t) -> {
			try {
				logger.debug("close&cancel");
				((W04InputControlController) (getController())).close(t);
			} catch (InterruptedException e) {
				logger.error(e.getMessage(), e);
			}
		});

		controller.setStage(reportStage);

		reportStageList.add(reportStage);
		reportStage.show();
	}

	/**
	 * クリアボタンボタンが押下されたときの処理
	 *
	 * @param event
	 * @throws InterruptedException
	 */
	public void clear(ActionEvent event) throws InterruptedException {
		inputVBox.getChildren().clear();
		inputControlAnchorPane.getChildren().clear();
		inputControlAnchorPane.getChildren().add(inputVBox);
		initialize(null, null);
		logger.info("Input control clear.");
	}

	/**
	 * 閉じるボタンが押下されたときの処理
	 *
	 * @param event
	 * @throws InterruptedException
	 */
	@SuppressWarnings("deprecation")
	public void cancel(ActionEvent event) throws InterruptedException {

		// TODO:レポート表示中の処理
		if (null != thread) {

			thread.interrupt();
			thread.stop();
			runReportTask = null;
			jasperViewer = null;
			thread = null;
		}

		try {
			// 現在のウィンドウ幅を取得し、セットする。
			preferences.setReportWindowHeight(String.valueOf(this.getStage().getHeight()));
			preferences.setReportWindowWidth(String.valueOf(this.getStage().getWidth()));

			// 現在のウィンドウの位置を取得し、セットする。
			preferences.setReportWindowX(String.valueOf(this.getStage().getX()));
			preferences.setReportWindowY(String.valueOf(this.getStage().getY()));

			// パラメータの有無を判断し、デバイダーの位置情報をセットする。
			if (null != clientReportUnit && null == clientReportUnit.getInputControls()) {
				preferences.setNonInputControlDivider(String.valueOf(splitPane.getDividerPositions()[0]));
			} else {
				preferences.setInputControlDivider(String.valueOf(splitPane.getDividerPositions()[0]));
			}

			// 上書き保存
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(preferences, file);
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
		}

		// TODO:レポート表示中タスクのチェック
		if (true) {
			// TODO:レポート描画中断確認ダイアログの表示
			// logger.debug("Check the report rendering task.");
			// TODO:OKののとき中断
		}

		if (hashMap != null) {
			JRSwapFileVirtualizer swapFile = hashMap.values().iterator().next();
			if (swapFile != null)
				swapFile.cleanup();
		}

		this.getStage().hide();
		this.getStage().close();
		logger.info("Close the report rendering stage.");
	}

	/**
	 * ウィンドウタイトルバーを利用して閉じた場合の処理
	 *
	 * @param t
	 * @throws InterruptedException
	 */
	@SuppressWarnings("deprecation")
	public void close(WindowEvent t) throws InterruptedException {

		// TODO:レポート表示中の処理
		if (null != thread) {

			thread.interrupt();
			thread.stop();
			jasperViewer = null;
			thread = null;
		}

		Stage stage = (Stage) t.getSource();

		try {
			// 現在のウィンドウ幅を取得し、セットする。
			preferences.setReportWindowHeight(String.valueOf(stage.getHeight()));
			preferences.setReportWindowWidth(String.valueOf(stage.getWidth()));

			// 現在のウィンドウの位置を取得し、セットする。
			preferences.setReportWindowX(String.valueOf(stage.getX()));
			preferences.setReportWindowY(String.valueOf(stage.getY()));

			// パラメータの有無を判断し、デバイダーの位置情報をセットする。
			if (null != clientReportUnit && null == clientReportUnit.getInputControls()) {
				preferences.setNonInputControlDivider(String.valueOf(splitPane.getDividerPositions()[0]));
			} else {
				preferences.setInputControlDivider(String.valueOf(splitPane.getDividerPositions()[0]));
			}

			// 上書き保存
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(preferences, file);
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
		}

		// TODO:レポート表示中タスクのチェック
		if (true) {
			// TODO:レポート描画中断確認ダイアログの表示
			// logger.debug("Check the report rendering task.");
			// TODO:OKののとき中断
		}

		setObject(null);

		if (hashMap != null) {
			JRSwapFileVirtualizer swapFile = hashMap.values().iterator().next();
			if (swapFile != null)
				swapFile.cleanup();
		}

		stage.hide();
		stage.close();
		logger.info("Close the report rendering stage by window close button.");
	}

	private class ObservableItem {

		private String label;
		private String value;

		ObservableItem(String label, String value) {
			this.label = label;
			this.value = value;
		}

		public String getLabel() {
			return this.label;
		}

		public String getValue() {
			return this.value;
		}

		public String toString() {
			return this.value;
		}
	}

	/**
	 * 保存ボタンを押下された場合の処理
	 * 
	 * @param event
	 * @throws InterruptedException
	 */
	public void save(ActionEvent event) throws InterruptedException {
		// パラメータをセットしてウィンドウを閉じる
		setObject(inputValues);
		cancel(null);

	}

	/**
	 * 開くボタンが押下されたときの処理
	 *
	 * @param event
	 * @throws InterruptedException
	 */
	public void openFile(ActionEvent event) throws InterruptedException {

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
					File file = new File(saveFile.getPath());
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
					logger.info("Open " + saveFile.getPath());
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
				return null;
			}
		}).start();

	}

	/**
	 * エラーログを開くボタンが押下されたときの処理
	 * 
	 * @param event
	 * @throws InterruptedException
	 * @author panyuan
	 */
	public void openErrorLog(ActionEvent event) throws InterruptedException {

		/**
		 * エラーログファイルを起動する
		 *
		 * ファイルによっては時間がかかるのでバックグラウンドスレッドで開く
		 */
		new Thread(new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				
				String logFilePath = System.getProperty("user.dir")+"\\log\\Lightning.log";
				try {
					File file = new File(logFilePath);
					Desktop desktop = Desktop.getDesktop();
					desktop.open(file);
					logger.info("Open " + logFilePath);
					
				} catch (IOException e) {
					logger.error("Open file by java.awt.Desktop failed. Try java.lang.Runtime.");
					try {
						Runtime.getRuntime().exec("rundll32 SHELL32.DLL,ShellExec_RunDLL " + logFilePath); 
						logger.info("Open " + logFilePath);
					} catch (IOException e2) {
						logger.error("Open file by java.lang.Runtime failed");
					}
				}
				return null;
			}
		}).start();

	}
}
