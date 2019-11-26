package com.legendapl.lightning.adhoc.factory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.FilterType;
import com.legendapl.lightning.adhoc.common.OperationType;
import com.legendapl.lightning.adhoc.custom.FilterVBox;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.filter.editor.GeneralEditor;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.service.AdhocLogService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Pos;
import javafx.geometry.Side;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;

public class FilterPaneFactory extends AdhocBaseFactory {
	
	static AnchorPane rightFilterAnchor;
	static ListView<FilterVBox> filterView;
	static TitledPane filterTitled;
	static Label filterError;
	static TextArea filterText;
	static Button filterReduceButton;
	static CheckBox filterCheck;

	private GeneralEditor editor;
	private ChangeListener<OperationType> listener;
	private ObservableList<FilterVBox> filterVBoxs = FXCollections.observableArrayList();
	private ContextMenu globalMenu;
	
	public FilterPaneFactory() {
		
		filterReduceButton.setOnAction(event -> handleActionClickedMenuButton(event));
		filterTitled.setCollapsible(false); //TODO 効果禁止
		initAddFilter();
	}
	
	private void initAddFilter() {
		List<Filter> filters = adhoc.getFilters();
		List<FilterVBox> vboxs = new ArrayList<>();
		for (Filter filter : filters) {
			Field field = adhoc.getFieldByResId(filter.getResourceId());
			String filterConnect = adhoc.getFilterConnect();
			FilterVBox vbox = getFilterVBox(field, filter, filterConnect);
			vboxs.add(vbox);
		}
		Platform.runLater(() -> { // DO NOT CHANGE TO addFilterImpl
			filterVBoxs.addAll(vboxs);
			filterView.setItems(filterVBoxs);
			refresh();
		});
	}
	
	/*----------------------------------------------Events----------------------------------------------*/
	
	/**
	 * 画面データからアドホックデータに保存します
	 */
	public void doSave() {
		adhoc.getFilters().clear();
		String inputText = filterText.getText();
		int filterSize = filterVBoxs.size();
		for (int k = 0; k < filterSize; k++) {
				Label labelName = filterVBoxs.get(k).getLabel();
				ChoiceBox<OperationType> cBox = filterVBoxs.get(k).getChoiceBox();
				GeneralEditor values = filterVBoxs.get(k).getGeneralEditor();
				String fieldId = filterVBoxs.get(k).getResourceId();
				Filter filter = new Filter(adhoc.getFieldByResId(fieldId));
				filter.setLabel(labelName.getText());
				filter.setOp((OperationType) cBox.getValue());
				filter.setLock(cBox.isDisable());
				if (values.checkFilter(filter)) {
					adhoc.getFilters().add(filter);
				}
		}
		inputText = inputText.toUpperCase().replaceAll("AND", "and").replaceAll("OR", "or").replace("NOT", "not");
		filterText.setText(inputText);
		adhoc.setFilterConnect(inputText);
		adhoc.setFilterCheckFlg(filterCheck.isSelected());
	}

	/**
	 * フィルタメニュー設定
	 * @param event
	 */
	public void handleActionClickedMenuButton(ActionEvent event) {
		Platform.runLater(() -> {
			if (null == globalMenu) globalMenu = getGlobalMenu();
			if (globalMenu.isShowing()) {
				globalMenu.hide();
			} else {
				globalMenu.show(filterReduceButton, Side.BOTTOM, 0, 0);
			}
		});
	}

	/**
	 * フィルタ全体のメニュー
	 * @return
	 */
	public ContextMenu getGlobalMenu() {
		ContextMenu contextMenu = new ContextMenu();
		MenuItem deleteAll = new MenuItem(AdhocUtils.getString("P121.filter.deleteAll"));
		deleteAll.setOnAction(event -> deleteAllFilter());
		contextMenu.getItems().add(deleteAll);
		return contextMenu;
	}

	/**
	 * 明細フィルタを作成
	 * @param fields
	 */
	public void addFilterByFields(List<? extends Field> fields) {
		// get
		List<FilterVBox> vboxs = new ArrayList<>();
		for (Field field : fields) {
			FilterVBox vbox = getFilterVBox(field, null, filterText.getText());
			vboxs.add(vbox);
		}
		addFilter(vboxs);
	}
	
	/**
	 * 明細フィルタを取得
	 */
	private FilterVBox getFilterVBox(Field field, Filter filter, String filterConnect) {
		FilterVBox vbox = new FilterVBox();
		HBox itemHbox = new HBox();
		AnchorPane editorPane = new AnchorPane();
		itemHbox.setPrefHeight(30);
		vbox.setField(field);
		createTitle(vbox, itemHbox, field);
		createChoiceBox(vbox, itemHbox, editorPane, field, filter);
		createButton(vbox, itemHbox);
		createFilterMenu(vbox);
		// 長さを設定する
		itemHbox.setStyle("-fx-background-color: #dbe4f1");
		itemHbox.prefWidthProperty().bind(rightFilterAnchor.widthProperty());
		vbox.prefWidthProperty().bind(rightFilterAnchor.widthProperty().subtract(30));
		filterView.prefWidthProperty().bind(rightFilterAnchor.widthProperty());
		editorPane.prefWidthProperty().bind(rightFilterAnchor.widthProperty());
		filterTitled.prefWidthProperty().bind(rightFilterAnchor.prefWidthProperty());
		vbox.getChildren().add(itemHbox);
		vbox.getChildren().add(editorPane);
		return vbox;
	}

	/**
	 * タイトル作成
	 * @param itemHbox
	 * @param field
	 */
	private void createTitle(FilterVBox vbox, HBox itemHbox, Field field) {
		Label itemName = new Label();
		// SET LABEL IN REFRESH FUNCTION
		vbox.setLabel(itemName);
		itemName.setAlignment(Pos.CENTER_LEFT);
		itemName.setPrefHeight(30);
		itemName.prefWidthProperty().bind(rightFilterAnchor.widthProperty().subtract(150));
		itemHbox.getChildren().add(itemName);
	}

	/**
	 * プルダウンリスト作成
	 * @param editorPane
	 * @param field
	 */
	private void createChoiceBox(FilterVBox vbox, HBox itemHbox, AnchorPane editorPane, Field field, Filter filter) {
		ChoiceBox<OperationType> itemSmybol = new ChoiceBox<OperationType>();
		listener = (ob, old, newValue) -> {
			Platform.runLater(() -> { 
				editor = FilterFactory.getEditor(field, newValue);
				editorPane.getChildren().clear();
				editor.generateEditorPane(editorPane, filter);
				vbox.setGeneralEditor(editor);
				editorPane.autosize();
			});
		};
		itemSmybol.valueProperty().removeListener(listener);
		// プルダウンリスト属性を設定する
		itemSmybol.setItems(FilterData.filter2Op.get(field.getDataType().getFilterType()));
		itemSmybol.setPrefWidth(AdhocUtils.getChoiceBoxWidth(itemSmybol));
		if(filter != null) {
			itemSmybol.setValue(filter.getOp());
			// TOPICでロックされたデータが編集できない
			if (filter.getLock()) {
				itemSmybol.setDisable(true);
				editorPane.setDisable(true);
			}
		} else {
			itemSmybol.setValue(OperationType.equals);
		}
		itemSmybol.setStyle("-fx-background-color: #dbe4f1");
		editor = FilterFactory.getEditor(field, itemSmybol.getValue());
		editorPane.getChildren().clear();
		editor.generateEditorPane(editorPane, filter);
		editorPane.autosize();
		itemSmybol.valueProperty().addListener(listener);
		// HBOXに設定する
		vbox.setChoiceBox(itemSmybol);
		vbox.setGeneralEditor(editor);
		vbox.setResourceId(field.getResourceId());
		itemHbox.getChildren().add(itemSmybol);
	}
	
	/**
	 * ボタン作成
	 * @param itemHbox
	 * @param field
	 */
	private void createButton(FilterVBox vbox, HBox itemHbox) {
		Button button = new Button();
		FontAwesomeIconView listIcon = new FontAwesomeIconView();
		// ボタン属性を設定する
		button.setMaxWidth(33);
		button.setMinWidth(33);
		button.setPrefWidth(33);
		button.setPrefHeight(30);
		button.setAlignment(Pos.CENTER_LEFT);
		button.setOnAction(event -> handleActionClickFilterButton(event, vbox));
		button.setStyle("-fx-background-color: transparent");
		// ボタンのPICを設定
		listIcon.setGlyphName("LIST");
		listIcon.setGlyphSize(12);
		// HBOXに設定する
		button.setGraphic(listIcon);
		itemHbox.getChildren().add(button);
		vbox.setButton(button);
	}
	
	/**
	 * コンテキストメニュー作成
	 * @param vbox
	 */
	private void createFilterMenu(FilterVBox vbox) {
		ContextMenu filterMenu = getFilterMenu(vbox);
		vbox.setFilterMenu(filterMenu);
	}

	/**
	 * フィルタメニュー設定
	 * @param event
	 * @param vbox
	 */
	public void handleActionClickFilterButton(ActionEvent event, FilterVBox vbox) {
		Platform.runLater(() -> {
			ContextMenu filterMenu = vbox.getFilterMenu();
			if (null != filterMenu && filterMenu.isShowing()) {
				filterMenu.hide();
			} else {
				filterMenu.show(vbox.getButton(), Side.BOTTOM, 0, 0);
			}
		});
	}

	/**
	 * フィルタアイテムのメニューを設定
	 * @param vbox
	 * @return
	 */
	public ContextMenu getFilterMenu(FilterVBox vbox) {
		
		ContextMenu contextMenu = new ContextMenu();
		
		MenuItem moveUpFilter = new MenuItem(AdhocUtils.getString("P121.filter.moveUpFilter"));
		MenuItem moveDownFilter = new MenuItem(AdhocUtils.getString("P121.filter.moveDownFilter"));
		MenuItem deleteFilter = new MenuItem(AdhocUtils.getString("P121.filter.deleteFilter"));
		
		vbox.setMoveUpFilter(moveUpFilter);
		vbox.setMoveDownFilter(moveDownFilter);
		vbox.setDeleteFilter(deleteFilter);
		
		moveUpFilter.setOnAction(event -> moveUpFilter(vbox));
		moveDownFilter.setOnAction(event -> moveDownFilter(vbox));
		deleteFilter.setOnAction(event -> deleteFilter(vbox));
		
		contextMenu.getItems().add(deleteFilter);
		contextMenu.getItems().add(moveUpFilter);
		contextMenu.getItems().add(moveDownFilter);
	
		return contextMenu;
	}

	/**
	 * フィルタを上へ移動
	 * @param vbox
	 */
	private void moveUpFilter(FilterVBox vbox) {
		StatementFactory.runLater(
				() -> moveUpFilterImpl(vbox),
				() -> moveDownFilterImpl(vbox)
		);
	}
	
	private void moveUpFilterImpl(FilterVBox vbox) {
		AdhocLogService.moveFilterUp(vbox, filterVBoxs.indexOf(vbox));
		AdhocUtils.moveListElement(filterVBoxs, vbox, -1);
		refresh();
	}

	/**
	 * フィルタを下へ移動
	 * @param vbox
	 */
	private void moveDownFilter(FilterVBox vbox) {
		StatementFactory.runLater(
				() -> moveDownFilterImpl(vbox),
				() -> moveUpFilterImpl(vbox)
		);
	}
	
	private void moveDownFilterImpl(FilterVBox vbox) {
		AdhocLogService.moveFilterDown(vbox, filterVBoxs.indexOf(vbox));
		AdhocUtils.moveListElement(filterVBoxs, vbox, +1);
		refresh();
	}
	
	/**
	 * フィルタを追加
	 * @param vboxs
	 */
	private void addFilter(List<FilterVBox> vboxs) {
		StatementFactory.runLater(
				() -> addFilterImpl(vboxs),
				() -> deleteFilterImpl(vboxs)
		);
	}
	
	private void addFilterImpl(FilterVBox vbox, Integer index) {
		addFilterImpl(Arrays.asList(vbox), index);
	}
	
	private void addFilterImpl(List<FilterVBox> vboxs) {
		addFilterImpl(vboxs, filterVBoxs.size());
	}
	
	private void addFilterImpl(List<FilterVBox> vboxs, Integer index) {
		filterVBoxs.addAll(index, vboxs);
		filterView.setItems(filterVBoxs);
		refresh();
		AdhocLogService.addFilter(vboxs, index);
	}

	/**
	 * 指定されたフィルタを削除
	 * @param vbox
	 */
	private void deleteFilter(FilterVBox vbox) {
		Integer index = filterVBoxs.indexOf(vbox);
		StatementFactory.runLater(
				() -> deleteFilterImpl(vbox),
				() -> addFilterImpl(vbox, index)
		);
	}
	
	private void deleteFilterImpl(FilterVBox vbox) {
		deleteFilterImpl(Arrays.asList(vbox));
	}
	
	private void deleteFilterImpl(List<FilterVBox> vboxs) {
		AdhocLogService.deleteFilter(vboxs);
		filterVBoxs.removeAll(vboxs);
		refresh();
	}
	
	/**
	 * フィルタを全て削除
	 */
	private void deleteAllFilter() {
		List<FilterVBox> vboxs = AdhocUtils.createNewListRemoveNull(filterVBoxs);
		StatementFactory.runLater(
				() -> deleteAllFilterImpl(),
				() -> addFilterImpl(vboxs)
		);
	}
	
	private void deleteAllFilterImpl() {
		AdhocLogService.deleteAllFilter(filterVBoxs);
		filterView.getItems().clear();
		filterVBoxs.clear();
		refresh();
	}

	/**
	 * refresh
	 */
	private void refresh() {
		for (int i = 0; i < filterVBoxs.size(); i ++) {
			// Label
			FilterVBox vbox = filterVBoxs.get(i);
			Label label = vbox.getLabel();
			Field field = vbox.getField();
			label.setText(" "+ getOrderNum(i) + "." + field.getLabel());
			// MenuItem
			Integer index = filterVBoxs.indexOf(vbox);
			vbox.getMoveUpFilter().setVisible(0 != index);
			vbox.getMoveDownFilter().setVisible(filterVBoxs.size() - 1 != index);
			// TODO : set more
		}
	}
	
	/*----------------------------------------------Check----------------------------------------------*/

	/**
	 * フィルタアイテムのチェック
	 */
	public Boolean doFilterItemCheck() {
		Boolean result = true;

		int filterSize = filterVBoxs.size();
		for (int k = 0; k < filterSize; k++) {
			Label labelName = filterVBoxs.get(k).getLabel();
			ChoiceBox<OperationType> cBox = filterVBoxs.get(k).getChoiceBox();
			GeneralEditor values = filterVBoxs.get(k).getGeneralEditor();
			String fieldId = filterVBoxs.get(k).getResourceId();
			Filter filter = new Filter(adhoc.getFieldByResId(fieldId));
			filter.setLabel(labelName.getText());
			filter.setOp((OperationType) cBox.getValue());
			if (!values.checkFilter(filter)) {
				result = false;
				break;
			}
		}
		return result;
	}

	/**
	 * フィルタのチェック
	 * 
	 * @return
	 */
	public boolean doFilterCheck() {
		// 初期化
		filterError.setText("");
		filterText.setStyle("-fx-background-color: #FFFFFF");
		String inputText = filterText.getText();
		// // フィルタが存在を判断
		if (!inputText.isEmpty()) {
			// 余計なブランクを削除
			String inputTextNoBlank = inputText.replaceAll(" +", " ").trim();
			// 字母と括弧ブランク以外の場合、エラーになる
			if (!doformartCheck(inputTextNoBlank)) {
				return false;
			} else {
				// 括弧のチェック
				if (!doBracketsCheck(inputTextNoBlank)) {
					return false;
				} else {
					// 内容チェック
					if (!doTextCheck(inputTextNoBlank)) {
						return false;
					}
				}
			}
		}
		return true;
	}

	/**
	 * 字母と括弧ブランク以外の場合、エラーになる
	 */
	private Boolean doformartCheck(String inputText) {
		String regex = "^[ A-Za-z\\(\\)]+$";
		if (!inputText.matches(regex)) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		return true;
	}

	/**
	 * 括弧不整をチェック
	 * 
	 * @param inputText
	 * @return
	 */
	private Boolean doBracketsCheck(String inputText) {
		if (!partenMatch(inputText)) {
			return false;
		}
		return true;
	}

	/**
	 * 入力テキストチェック
	 * 
	 * @param inputText
	 * @return
	 */
	private Boolean doTextCheck(String inputText) {
		String[] inputItem = inputText.split(" ");
		String inputField = "";
		String paramConnectFlg = "2";
		for (int i = 0; i < inputItem.length; i++) {
			// パラメタ重複チェック
			if (i == 0) {
				// 最初がNOTと参数以外の場合、エラー
				if (!doFirstItemCheck(inputItem[i])) {
					return false;
				}
			}
			//TODO
			// 大文字(AND,OR,NOT以外)のStringが参数
			if (inputItem[i].equals(inputItem[i].toUpperCase()) && !inputItem[i].toUpperCase().equals("AND")
					&& !inputItem[i].toUpperCase().equals("OR") && !inputItem[i].toUpperCase().equals("NOT") && !inputItem[i].toUpperCase().equals("(NOT")) {
				// 参数重複チェック
				if (!"0".equals(paramConnectFlg) || "NOT".equals(inputItem[i].toUpperCase())) {
					paramConnectFlg = "0";
					// チェックエラー
					if (!doParamCheck(inputItem[i])) {
						return false;
					}
				} else {
					setErrorMessage(AdhocUtils.format(AdhocUtils.getString("P121.filter.errorMessage.formulaError"),
							inputItem[i].toString()));
					return false;
				}
			}else {
				// コネクト重複チェック
				if (!"1".equals(paramConnectFlg) || "NOT".equals(inputItem[i].toUpperCase()) || "(NOT".equals(inputItem[i].toUpperCase())) {
					paramConnectFlg = "1";
					// コネクトのテック
					if (!doConnectCheck(inputItem[i])) {
						// チェックエラー
						return false;
					}
				} else {
					setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
					return false;
				}
				if (inputItem[i].toUpperCase().equals("NOT")) {
					if (i > 0) {
						// NOTの前がand or以外の場合、エラー
						if (!"AND".equals(inputItem[i - 1].toUpperCase())
								&& !"OR".equals(inputItem[i - 1].toUpperCase())) {
							setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
							return false;
						}
					}
					// NOTの後ろが参数以外の場合、エラー
					if (i+1 < inputItem.length) {
						if (!(inputItem[i + 1].toUpperCase()).equals(inputItem[i + 1]) || "AND".equals(inputItem[i + 1].toUpperCase())
								|| "OR".equals(inputItem[i + 1].toUpperCase()) || "NOT".equals(inputItem[i + 1].toUpperCase())) {
							setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
							return false;
						}
					} else {
						setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
						return false;
					}
				}
			}
			// 正しカスタム式を構築
			if (i > 0) {
				if ("NOT".equals(inputItem[i - 1].toUpperCase()) && inputItem[i].contains("(")) {
					inputField = inputField + " "+ "(" + inputItem[i] + ")";
				} else {
					inputField = inputField + " " + inputItem[i];
				}
			} else {
				inputField = inputField + " " + inputItem[i];
			}
			if (i == inputItem.length - 1) {
				// 最後が参数以外の場合、エラー
				if (!doLastItemCheck(inputItem[i])) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * 一番目のアイテムをチェック
	 * 
	 * @param firstItem
	 * @return
	 */
	private Boolean doFirstItemCheck(String firstItem) {
		if (firstItem.toUpperCase().equals("AND") || 
				firstItem.toUpperCase().equals("OR")) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		if (!firstItem.toUpperCase().equals("NOT") && 
				!firstItem.toUpperCase().equals("(NOT") && 
				!firstItem.equals(firstItem.toUpperCase())) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		return true;
	}

	/**
	 * 最後のアイテムをチェック
	 * 
	 * @param firstItem
	 * @return
	 */
	private Boolean doLastItemCheck(String lastItem) {
		if (lastItem.toUpperCase().equals("AND") || 
				lastItem.toUpperCase().equals("OR") || 
				lastItem.toUpperCase().equals("NOT") || 
				lastItem.toUpperCase().equals("(NOT")) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		if (!lastItem.equals(lastItem.toUpperCase())) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		return true;
	}
	
	/**
	 * パラメタチェック
	 * @param inputParm
	 * @return
	 */
	private Boolean doParamCheck(String inputParm) {
		// パラメタの場合
		//String inputParm = paramItem;
		// 左で括弧ありかつ右で括弧あり
		if (inputParm.substring(0, 1).equals("(")
				&& inputParm.substring(inputParm.length() - 1, inputParm.length()).equals(")")) {
			inputParm = inputParm.substring(1, inputParm.length() - 1);
		// 左で括弧あり
		} else if (inputParm.substring(0, 1).equals("(")) {
			inputParm = inputParm.substring(1, inputParm.length());
		// 右で括弧あり
		} else if (inputParm.substring(inputParm.length() - 1, inputParm.length()).equals(")")) {
			inputParm = inputParm.substring(0, inputParm.length() - 1);
		// 左で括弧なしかつ右で括弧なし
		} else if (!inputParm.substring(0, 1).equals("(")
				&& !inputParm.substring(inputParm.length() - 1, inputParm.length()).equals(")")) {
		} else {
			setErrorMessage(AdhocUtils.format(AdhocUtils.getString("P121.filter.errorMessage.parameterError"), inputParm));
			return false;
		}
		if (inputParm.isEmpty()) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		if(!doHaveBracketsCheck(inputParm)) {
			return false;
		}
		// 参数を設定する
		int filterNumber = getOrderNum(inputParm);
		// パラメータが存在かどうか
		if ((filterVBoxs.size() - 1) < filterNumber) {
			setErrorMessage(AdhocUtils.format(AdhocUtils.getString("P121.filter.errorMessage.parameterNone"), inputParm));
			return false;
		}
		return true;
	}

	/**
	 * パラメタに括弧が存在チェック
	 * @param inputParm
	 * @return
	 */
	private Boolean doHaveBracketsCheck (String inputParm) {
		if (inputParm.contains(")")) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		} else if (inputParm.contains("(")) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		return true;
	}
	
	/**
	 * コネクトチェック
	 * @param connectItem
	 * @param parameterNoSelected
	 * @return
	 */
	private Boolean doConnectCheck(String connectItem) {
		if (!"AND".equals(connectItem.toUpperCase()) && 
				!"OR".equals(connectItem.toUpperCase()) && 
				!"NOT".equals(connectItem.toUpperCase()) && 
				!"(NOT".equals(connectItem.toUpperCase())) {
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.formulaError"));
			return false;
		}
		return true;
	}
	
	/**
	 * 括弧のチェック
	 * 
	 * @param inputText
	 * @return
	 */
	public static boolean partenMatch(String inputText) {
		int top = 0;
		boolean end = true;
		char[] inputTextChar = inputText.toCharArray();
		for (int i = 0; i < inputText.length(); i++) {
			if (inputTextChar[i] == '(') {
				top++;
			} else if (inputTextChar[i] == ')') {
				if (!(top == 0)) {
					top--;
				} else {
					end = false;
					//右括弧が多くなる
					setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.noLeftBrackets"));
					return end;
				}
			}
		}
		if (top == 0 && end) {
			end = true;
		} else if (top != 0 && end) {
			end = false;
			//左括弧が多くなる
			setErrorMessage(AdhocUtils.getString("P121.filter.errorMessage.noRightBrackets"));
		}
		return end;
	}

	/**
	 * エラーメッセージ共通
	 * @param errorMessage
	 */
	public static void setErrorMessage(String errorMessage) {
		filterError.setText(errorMessage);
		filterError.setTextFill(Color.web("#FF0000"));
		filterText.setStyle("-fx-background-color: #FF0000");
	}
	
	/**
	 * 0 -> A
	 * 1 -> B
	 * 2 -> C
	 * 25 -> Z
	 * 26 -> AA
	 * 27 -> AB
	 * 
	 * @param orderNum
	 * @return
	 */
	public static String getOrderNum(int num) {
		String str = "";
		while (num >= 0) {
			char c = (char) (num % 26 + 'A');
			str = c + str;
			if (num < 26) break;
			num = num / 26 - 1;
		}
		return str;
	}
	
	/**
	 * A -> 0, 
	 * B -> 1, 
	 * C -> 2, 
	 * Z -> 25, 
	 * AA -> 26, 
	 * AB -> 27, 
	 * ...
	 * @param str
	 * @return
	 */
	public static int getOrderNum(String str) {
		int res = 0;
		int t = 1;
		for (int i = str.length() - 1; i >= 0; i --) {
			char c = str.charAt(i);
			res = res + t * (c - 'A' + 1);
			t = t * 26;
		}
		res = res - 1;
		return res;
	}
	
	/*----------------------------------------------Setters----------------------------------------------*/

	public static void setRightFilterAnchor(AnchorPane rightFilterAnchor) {
		FilterPaneFactory.rightFilterAnchor = rightFilterAnchor;
	}

	public static void setFilterView(ListView<FilterVBox> filterView) {
		FilterPaneFactory.filterView = filterView;
	}

	public static void setFilterTitled(TitledPane filterTitled) {
		FilterPaneFactory.filterTitled = filterTitled;
	}

	public static void setFilterError(Label filterError) {
		FilterPaneFactory.filterError = filterError;
	}

	public static void setFilterText(TextArea filterText) {
		FilterPaneFactory.filterText = filterText;
	}

	public static void setFilterReduceButton(Button filterReduceButton) {
		FilterPaneFactory.filterReduceButton = filterReduceButton;
	}

	public static void setFilterCheck(CheckBox filterCheck) {
		FilterPaneFactory.filterCheck = filterCheck;
	}
	
	/*----------------------------------------------Test----------------------------------------------*/
	
	public static void main(String[] args) {
		for (int i = 0; i < 1000; i ++) {
			String t = getOrderNum(i);
			System.out.println(i + " -> " + t + " -> " + getOrderNum(t));
		}
	}
	
}

/*----------------------------------------------FilterData----------------------------------------------*/

class FilterData {
	@SuppressWarnings("serial")
	static final Map<FilterType, ObservableList<OperationType>> filter2Op = Collections.unmodifiableMap(
			new HashMap<FilterType, ObservableList<OperationType>>() {
				{
					put(FilterType.BOOLEAN, 
						FXCollections.observableArrayList(
								OperationType.equals, OperationType.isNotEqualTo, OperationType.isOneOf, OperationType.isNotOneOf));
					put(FilterType.TIME, 
						FXCollections.observableArrayList(
								OperationType.equals, OperationType.isNotEqualTo, OperationType.isAfter, OperationType.isBefore, 
								OperationType.isOnOrAfter, OperationType.isOnOrBefore, OperationType.isBetween, OperationType.isNotBetween));
					put(FilterType.TIMESTAMP, 
						FXCollections.observableArrayList(
								OperationType.equals, OperationType.isNotEqualTo, OperationType.isAfter, OperationType.isBefore, 
								OperationType.isOnOrAfter, OperationType.isOnOrBefore, OperationType.isBetween, OperationType.isNotBetween));
					put(FilterType.STRING, 
						FXCollections.observableArrayList(
								OperationType.isOneOf, OperationType.isNotOneOf, OperationType.equals, OperationType.isNotEqualTo, 
								OperationType.contains, OperationType.doesNotContain, OperationType.startsWith, 
								OperationType.doesNotStartWith, OperationType.endsWith, OperationType.doesNotEndWith));
					put(FilterType.NUMBER, 
						FXCollections.observableArrayList(
								OperationType.isOneOf, OperationType.isNotOneOf, OperationType.equals, OperationType.isNotEqualTo, 
								OperationType.isGreaterThan, OperationType.lessThan, OperationType.isGreaterThanOrEqualTo, 
								OperationType.isLessThanOrEqualTo, OperationType.isBetween, OperationType.isNotBetween));
					put(FilterType.DATE, 
						FXCollections.observableArrayList(
								OperationType.equals, OperationType.isNotEqualTo, OperationType.isAfter, OperationType.isBefore, 
								OperationType.isOnOrAfter, OperationType.isOnOrBefore, OperationType.isBetween, OperationType.isNotBetween));
				}
			}
	);
}
