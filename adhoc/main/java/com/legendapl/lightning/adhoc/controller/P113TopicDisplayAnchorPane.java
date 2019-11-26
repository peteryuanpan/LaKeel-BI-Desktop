package com.legendapl.lightning.adhoc.controller;

import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DisplayRow;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.ShareDataService;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TextFieldTreeTableCell;
import javafx.scene.input.MouseEvent;
import javafx.util.converter.DefaultStringConverter;

/**
 * トピック編集画面のコントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class P113TopicDisplayAnchorPane extends P110TopicBaseAnchorPane {

	@FXML
	private TreeTableView<DisplayRow> dataTable;
	@FXML
	private TreeTableColumn<DisplayRow, String> srLabel;
	@FXML
	private TreeTableColumn<DisplayRow, String> tgLabel;
	@FXML
	private JFXButton moveFirstButton;
	@FXML
	private JFXButton movePreviousButton;
	@FXML
	private JFXButton moveNextButton;
	@FXML
	private JFXButton moveLastButton;

	private TreeItem<DisplayRow> superRoot;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		super.initialize(location, resources);
		
		logger.debug("initialize");

		Topic topic = ShareDataService.loadTopic();
		if (null == topic) {
			logger.warn("Topic is null.");
			return;
		}

		Platform.runLater(() -> {
			// カランをセット
			srLabel.setCellFactory(column -> new SrTreeTableCell());
			srLabel.setContextMenu(null);
			srLabel.setSortable(false);
			tgLabel.setCellFactory(column -> new TgTreeTableCell());
			tgLabel.setContextMenu(null);
			tgLabel.setSortable(false);
			// ツリーをコピー
			superRoot = new TreeItem<DisplayRow>();
			cloneTree(superRoot, topic.getTree().getRoot());
			// テーブルをセット
			dataTable.setRoot(superRoot);
			dataTable.setShowRoot(false);
			dataTable.setEditable(true);
			dataTable.getColumns().setAll(Arrays.asList(srLabel, tgLabel));
			// ボタンをセット
			moveFirstButton.disableProperty().bind(moveUpButtonDisable());
			movePreviousButton.disableProperty().bind(moveUpButtonDisable());
			moveNextButton.disableProperty().bind(moveDownButtonDisable());
			moveLastButton.disableProperty().bind(moveDownButtonDisable());
		});
	}

	private class SrTreeTableCell extends TreeTableCell<DisplayRow, String> {
		@Override public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            DisplayRow row = this.getTreeTableRow().getItem();
            if (null != row) {
            	this.setText(row.getSrLabel().get());
            } else {
            	this.setText(null);
            }
        }
	}
	
	private static TgTreeTableCell editCell = null;

	private class TgTreeTableCell extends TextFieldTreeTableCell<DisplayRow, String> {
		private TextField textField;
		public TgTreeTableCell() {
			super(new DefaultStringConverter());
			this.setEditable(true);
			textField = null;
			this.editingProperty().addListener((record, oldValue, newValue) -> {
				if (!oldValue && newValue) {
					editCell = this;
				}
			});
			this.graphicProperty().addListener((record, oldValue, newValue) -> {
				buildTextField();
			});
		}
		@Override public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            DisplayRow row = this.getTreeTableRow().getItem();
            if (null != row && null != item) {
            	if (leggal(item)) {
            		row.setTgLabel(item);
            		row.getBaseNode().setLabel2(item);
            	}
            }
            if (null != row && !this.isEditing()) {
            	this.setText(row.getTgLabel().get());
            	this.setItem(row.getTgLabel().get());
            } else {
            	this.setText(null);
            	this.setItem(null);
            }
            if (null != this.getTreeTableView()) {
    			this.getTreeTableView().setOnMouseClicked(event -> handleOnMouseClickedTreeTableView(event));
            }
        }
		private void handleOnMouseClickedTreeTableView(MouseEvent event) {
			if (!this.isEditing() && null != editCell && null != textField) {
				this.updateItem(textField.getText(), false);
				editCell = null;
			}
		}
		private void buildTextField() {
			if (null == textField) {
				Node graphic = this.getGraphic();
				if (null != graphic && graphic instanceof TextField) {
					textField = (TextField) graphic;
				}
			}
		}
		private final int MAX_LENGTH = 20;
		private boolean leggal(String item) {
			if (null != item) {
				if (itemAllEmpty(item)) {
            		AlertWindowService.showWarn(AdhocUtils.getString("ERROR_TOPIC_DISPLAY_TGLABEL_EMPTY"));
            		return false;
				}
				if (item.length() > MAX_LENGTH) {
            		AlertWindowService.showWarn(
            				AdhocUtils.format(AdhocUtils.getString("ERROR_TOPIC_DISPLAY_TGLABEL_TOO_LONG"), MAX_LENGTH));
					return false;
				}
			}
			return true;
		}
		private boolean itemAllEmpty(String item) {
			boolean empty = true;
			for (int i = 0; i < item.length(); i ++) {
				if (' ' != item.charAt(i)) {
					empty = false;
				}
			}
			return empty;
		}
	}

	private void cloneTree(TreeItem<DisplayRow> root, BaseNode node) {
		DisplayRow row = new DisplayRow();
		row.setBaseNode(node);
		row.setSrLabel(node.getLabel());
		row.setTgLabel(node.getLabel2());
		root.setValue(row);
		root.setExpanded(true);
		for (int i = 0; i < node.getNodes().size(); i ++) {
			boolean found = false;
			for (BaseNode childNode : node.getNodes()) {
				if (i == childNode.getRank()) {
					TreeItem<DisplayRow> childRoot = new TreeItem<DisplayRow>();
					cloneTree(childRoot, childNode);
					root.getChildren().add(childRoot);
					found = true;
					break;
				}
			}
			if (!found) {
				logger.warn("Clone Tree Process warning : rank " + i + " child not found.");
			}
		}
	}

	private ObservableValue<? extends Boolean> moveUpButtonDisable() {
		return new BooleanBinding() {
			{
				super.bind(dataTable.getSelectionModel().getSelectedItems());
			}
            @Override protected boolean computeValue() {
            	if (!dataTable.getSelectionModel().getSelectedItems().isEmpty()) {
            		TreeItem<DisplayRow> target = dataTable.getSelectionModel().getSelectedItems().get(0);
            		if (null != target) {
            			List<TreeItem<DisplayRow>> brothers = target.getParent().getChildren();
            			int nowIndex = getNodeFromListById(target, brothers);
            			if (-1 != nowIndex) {
            				if (0 != nowIndex) {
            					return false;
            				}
            			}
            		}
            	}
        		return true;
            }
		};
	}

	private ObservableValue<? extends Boolean> moveDownButtonDisable() {
		return new BooleanBinding() {
			{
				super.bind(dataTable.getSelectionModel().getSelectedItems());
			}
            @Override protected boolean computeValue() {
            	if (!dataTable.getSelectionModel().getSelectedItems().isEmpty()) {
            		TreeItem<DisplayRow> target = dataTable.getSelectionModel().getSelectedItems().get(0);
            		if (null != target) {
            			List<TreeItem<DisplayRow>> brothers = target.getParent().getChildren();
            			int nowIndex = getNodeFromListById(target, brothers);
            			if (-1 != nowIndex) {
            				if (1 + nowIndex != brothers.size()) {
            					return false;
            				}
            			}
            		}
            	}
        		return true;
            }
		};
	}

	public void moveFirst(ActionEvent event) {
		TreeItem<DisplayRow> target = dataTable.getSelectionModel().getSelectedItem();
		if (null != target) {
			List<TreeItem<DisplayRow>> brothers = target.getParent().getChildren();
			int nowIndex = getNodeFromListById(target, brothers);
			if (-1 != nowIndex) {
				if (0 != nowIndex) {
					brothers.remove(nowIndex);
					brothers.add(0, target);
					dataTable.getSelectionModel().select(target);
					for (int i = 0; i < brothers.size(); i ++) {
						brothers.get(i).getValue().getBaseNode().setRank(i);
					}
				}
			}
		}
	}

	public void movePrevious(ActionEvent event) {
		TreeItem<DisplayRow> target = dataTable.getSelectionModel().getSelectedItem();
		if (null != target) {
			List<TreeItem<DisplayRow>> brothers = target.getParent().getChildren();
			int nowIndex = getNodeFromListById(target, brothers);
			if (-1 != nowIndex) {
				if (0 != nowIndex) {
					brothers.remove(nowIndex);
					brothers.add(nowIndex - 1, target);
					dataTable.getSelectionModel().select(target);
					for (int i = 0; i < brothers.size(); i ++) {
						brothers.get(i).getValue().getBaseNode().setRank(i);
					}
				}
			}
		}
	}

	public void moveNext(ActionEvent event) {
		TreeItem<DisplayRow> target = dataTable.getSelectionModel().getSelectedItem();
		if (null != target) {
			List<TreeItem<DisplayRow>> brothers = target.getParent().getChildren();
			int nowIndex = getNodeFromListById(target, brothers);
			if (-1 != nowIndex) {
				if (1 + nowIndex != brothers.size()) {
					brothers.remove(nowIndex);
					brothers.add(nowIndex + 1, target);
					dataTable.getSelectionModel().select(target);
					for (int i = 0; i < brothers.size(); i ++) {
						brothers.get(i).getValue().getBaseNode().setRank(i);
					}
				}
			}
		}
	}

	public void moveLast(ActionEvent event) {
		TreeItem<DisplayRow> target = dataTable.getSelectionModel().getSelectedItem();
		if (null != target) {
			List<TreeItem<DisplayRow>> brothers = target.getParent().getChildren();
			int nowIndex = getNodeFromListById(target, brothers);
			if (-1 != nowIndex) {
				if (1 + nowIndex != brothers.size()) {
					brothers.remove(nowIndex);
					brothers.add(brothers.size(), target);
					dataTable.getSelectionModel().select(target);
					for (int i = 0; i < brothers.size(); i ++) {
						brothers.get(i).getValue().getBaseNode().setRank(i);
					}
				}
			}
		}
	}

	private int getNodeFromListById(TreeItem<DisplayRow> keyNode, List<TreeItem<DisplayRow>> children) {
		for (int i = 0; i < children.size(); i ++) {
			if (keyNode.getValue().getBaseNode().getId().equals(
					children.get(i).getValue().getBaseNode().getId())) {
				return i;
			}
		}
		return -1;
	}

}
