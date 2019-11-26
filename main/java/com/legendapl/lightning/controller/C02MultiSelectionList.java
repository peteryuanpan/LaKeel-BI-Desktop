package com.legendapl.lightning.controller;

import java.io.IOException;
import java.util.ResourceBundle;

import org.apache.log4j.Logger;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import com.legendapl.lightning.common.constants.Constant;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.SelectionMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

/**
 * 入力コントロール画面用カスタマイズコントロール部品クラス FXML：C02MultiSelectionList.fxml
 * 
 * ・フィルタ入力 ・複数選択可能のリスト ・全選択・全解除ボタンを含む
 * 
 * @author taka
 *
 */
public class C02MultiSelectionList<T> extends VBox {
	// 多言語対応バンドルファイル
	protected ResourceBundle myResource = ResourceBundle.getBundle(Constant.Application.MY_BUNDLE);
	protected ResourceBundle messageRes = ResourceBundle.getBundle("messages");
	private Logger logger = Logger.getLogger(getClass());

	ObservableList<T> items;

	@FXML
	private JFXTextField filter;

	@FXML
	JFXListView<T> list;

	@FXML
	JFXButton selectAll;

	@FXML
	JFXButton unselectAll;

	public C02MultiSelectionList() {
		this(FXCollections.observableArrayList());
	}

	public C02MultiSelectionList(ObservableList<T> items) {
		this.items = items;
		FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/C02MultiSelectionList.fxml"),
				ResourceBundle.getBundle(Constant.Application.MY_BUNDLE));
		loader.setController(this);
		loader.setRoot(this);
		try {
			loader.load();
			list.setItems(items);

			// Ctrlキーを押さなくても複数選択できるように変更
			MultipleSelectionModel<T> selectionModel = list.getSelectionModel();
			selectionModel.setSelectionMode(SelectionMode.MULTIPLE);

			list.setCellFactory(lv -> {
				JFXListCell<T> cell = new JFXListCell<>();
				cell.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
					list.requestFocus();
					if (!cell.isEmpty()) {
						int index = cell.getIndex();
						if (selectionModel.getSelectedIndices().contains(index)) {
							selectionModel.clearSelection(index);
						} else {
							selectionModel.select(index);
						}
						event.consume();
					}
				});
				return cell;
			});
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}

	}

	public void filterChanged(KeyEvent event) {
		logger.debug("filter=" + filter.getText());

		if (filter.getText().trim().isEmpty()) {
			list.setItems(this.items);
			return;
		}

		ObservableList<T> l = FXCollections.observableArrayList();
		for (T s : items) {
			if (s.toString().contains(filter.getText())) {
				l.add(s);
			}
		}
		list.setItems(l);
	}

	public void selectAll(ActionEvent event) {
		list.getSelectionModel().selectAll();
	}

	public void unselectAll(ActionEvent event) {
		list.getSelectionModel().clearSelection();
	}

	public ObservableList<T> getValue() {
		return list.getSelectionModel().getSelectedItems();
	}

	public void setEditable(boolean flag) {
		if (!flag) {
			filter.setEditable(false);
			filter.setDisable(true);
			list.setEditable(false);
			list.setDisable(true);
			selectAll.setDisable(true);
			unselectAll.setDisable(true);
		} else {
			filter.setEditable(true);
			filter.setDisable(false);
			list.setEditable(true);
			list.setDisable(false);
			selectAll.setDisable(false);
			unselectAll.setDisable(false);

		}

	}
}
