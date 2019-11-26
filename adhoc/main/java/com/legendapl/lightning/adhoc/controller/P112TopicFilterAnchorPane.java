package com.legendapl.lightning.adhoc.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeView;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.OperationType;
import com.legendapl.lightning.adhoc.factory.EditPaneFactory;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.filter.editor.GeneralEditor;
import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBTree;
import com.legendapl.lightning.adhoc.model.Field;
import com.legendapl.lightning.adhoc.model.Item;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.service.ShareDataService;
import com.legendapl.lightning.adhoc.service.TreeTransferService;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * トピック編集画面のコントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class P112TopicFilterAnchorPane extends P110TopicBaseAnchorPane {

	@FXML
	private JFXTreeView<BaseNode> treeView;
	@FXML
	private BorderPane filterEditPane;
	@FXML
	private VBox filterEditVBox;
	@FXML
	private TableView<Filter> tableView;
	@FXML
	private AnchorPane editorPane;
	@FXML
	private CheckBox checkBox;
	@FXML
	private Label label;
	@FXML
	private ChoiceBox<OperationType> choiceBox;
	@FXML
	private JFXButton OKButton;
	@FXML
	private JFXButton cancelButton;
	@FXML
	private HBox left;
	@FXML
	private HBox bottom;
	@FXML
	private VBox leftVBox;
	@FXML
	private Label leftLabel;
	@FXML
	private Label rightLabel;
	@FXML
	private AnchorPane resizePane;
	@FXML
	private AnchorPane rightEntirePane;

	private DBTree dbTree;

	private List<Filter> topicFilterList;
	private Topic topic;

	double testDouble = 0;

	private Filter filter = null;

	private EditPaneFactory editPaneFactory;

	@Override
	public void initialize(URL location, ResourceBundle resources) {

		logger.debug("initialize");

		topic = ShareDataService.loadTopic();
		GeneralEditor.setDatabaseInfo(topic.getDatabase());
		dbTree = topic.getTree();

		if(topic.getFilters() == null) {
			topicFilterList = new ArrayList<Filter>();
			topic.setFilters(topicFilterList);
		} else {
			topicFilterList = topic.getFilters();
			List<Filter> removedFilters = new ArrayList<Filter>();
			for(Filter filter: topicFilterList) {
				if(dbTree.getFieldByResId(filter.getResourceId()) == null) {
					removedFilters.add(filter);
				}
			}
			topicFilterList.removeAll(removedFilters);
			removedFilters = null;
		}
		tableView.getItems().addAll(topicFilterList);

		TreeTransferService.transferTree(treeView, dbTree);
		filterEditVBox.getStylesheets().add("/view/topicFilter.css");
		filterEditVBox.setStyle("-fx-background-color: #FFFFFF;");
		rightLabel.setStyle("-fx-background-color: #f4f4f4;");
		treeView.setOnMouseClicked(event -> {
	        if(event.getClickCount() == 2) {
	            TreeItem<BaseNode> item = treeView.getSelectionModel().getSelectedItem();
	            if (null != item) {
		            if(item.getValue() instanceof Item)
		            	doubleClickField(item.getValue());
	            }
	        }
		});

		initColumns();
		componentsInit();
	}

	private void initColumns() {
		TableColumn<Filter, String> col1 = new TableColumn<>();
		TableColumn<Filter, String> col2 = new TableColumn<>();
		TableColumn<Filter, Void> col3 = new TableColumn<>();
		TableColumn<Filter, Void> col4 = new TableColumn<>();
		tableView.getColumns().addAll(Arrays.asList(col1, col2, col3, col4));
		col1.setCellFactory(tc -> {
			return new TableCell<Filter, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					int i = getIndex();
					if(i >= 0 && i < getTableView().getItems().size()) {
						Filter filter = getTableView().getItems().get(i);
						Text text = new Text(filter.getExpress());
						text.wrappingWidthProperty().bind(col1.widthProperty());;
						setGraphic(text);
						if(i % 2 == 1) {
							setStyle("-fx-background-color: #f4f4f4");
						} else {
							setTextFill(Color.BLACK);
							setStyle("-fx-background-color: white");
						}
					} else {
						setGraphic(null);
						setStyle("-fx-background-color: white");
					}
				}
			};
		});
		col2.setCellFactory(tc -> {
			return new TableCell<Filter, String>() {

				@Override
				protected void updateItem(String item, boolean empty) {
					int i = getIndex();
					if(i >= 0 && i < getTableView().getItems().size()) {
						Filter filter = getTableView().getItems().get(i);
						setText(filter.getLocked());
						if(i % 2 == 1) {
							setTextFill(Color.BLACK);
							setStyle("-fx-background-color: #f4f4f4");
						} else {
							setTextFill(Color.BLACK);
							setStyle("-fx-background-color: white");
						}
					} else {
						setText("");
						setStyle("-fx-background-color: white");
					}
				}
			};
		});

		col3.setCellFactory(tc -> {
			return new TableCell<Filter, Void>() {
				private Hyperlink link;

				@Override
				protected void updateItem(Void item, boolean empty) {
					super.updateItem(item, empty);
					link = new Hyperlink(AdhocUtils.getString("P112.filter.change"));
			        link.setOnAction(evt -> {
			        	cancelEditPane();
			            filter = getTableView().getItems().get(getTableRow().getIndex());
			            topicFilterList.remove(filter);
			            getTableView().getItems().remove(getTableRow().getIndex());
			            editPaneFactory.generatePane(topic.getFieldByResId(filter.getResourceId()), filter);
			            filterEditPane.setVisible(true);
			            if(getTableView().getItems().size() == 0) {
			            	tableView.setVisible(false);
			            }
			        });

			        setGraphic(empty ? null : link);
			        if(empty) {
			        	setStyle("-fx-background-color: white");
			        } else {
			        	if(getIndex() % 2 == 1) {
							setStyle("-fx-background-color: #f4f4f4");
						} else {
							setStyle("-fx-background-color: white");
						}
			        }
				}
			};
		});

		col4.setCellFactory(tc -> {
			return new TableCell<Filter, Void>() {
				private Hyperlink link;

				@Override
				protected void updateItem(Void item, boolean empty) {
					super.updateItem(item, empty);
					link = new Hyperlink(AdhocUtils.getString("P112.filter.remove"));
			        link.setOnAction(evt -> {
			            topicFilterList.remove(getTableView().getItems().get(getTableRow().getIndex()));
			            getTableView().getItems().remove(getTableRow().getIndex());
			            if(getTableView().getItems().size() == 0) {
			            	tableView.setVisible(false);
			            	tableView.prefHeightProperty().setValue(35);
			            } else {
							tableView.prefHeightProperty().setValue(topicFilterList.size() * 35);
			            }
			        });
			        setGraphic(empty ? null : link);
			        if(empty) {
			        	setStyle("-fx-background-color: white");
			        } else {
			        	if(getIndex() % 2 == 1) {
							setStyle("-fx-background-color: #f4f4f4");
						} else {
							setStyle("-fx-background-color: white");
						}
			        }
				}
			};
		});
		tableView.setPlaceholder(new Label());
		tableView.prefWidthProperty().bind(filterEditPane.widthProperty());
		tableView.prefHeightProperty().setValue(topicFilterList.size() * 35);
		col2.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));
		col3.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
		col4.prefWidthProperty().bind(tableView.widthProperty().multiply(0.1));
		col1.prefWidthProperty().bind(tableView.widthProperty().multiply(0.65));
		col2.setResizable(false);
		col3.setResizable(false);
		col4.setResizable(false);
		if(tableView.getItems().size() == 0) {
			tableView.setVisible(false);
		}
	}

	public void doubleClickField(BaseNode node) {
		cancelEditPane();
		Field field = topic.getFieldByResId(node.getResourceId());
		field.setLabel(node.getLabel());
		editPaneFactory.generatePane(field, null);
		filterEditPane.setVisible(true);
	}

	private void componentsInit() {
		rightLabel.minWidthProperty().bind(rightEntirePane.widthProperty().subtract(10));
		treeView.prefHeightProperty().bind(leftVBox.heightProperty().subtract(leftLabel.heightProperty()));
		resizePane.minHeightProperty().bind(rightEntirePane.heightProperty().subtract(10));
		resizePane.minWidthProperty().bind(rightEntirePane.widthProperty().subtract(10));
		editPaneFactory = new EditPaneFactory(label, choiceBox, editorPane, checkBox, filterEditPane);
		OKButton.setOnAction(event -> {
			Filter filter = editPaneFactory.generateFilter();
			if(filter != null) {
				tableView.getItems().add(filter);
				topicFilterList.add(filter);
				filterEditPane.setVisible(false);;
				tableView.setVisible(true);
				this.filter = null;
				editorPane.getChildren().clear();
			}
			editorPane.autosize();
			filterEditPane.autosize();
		});

		cancelButton.setOnAction(event -> {
			cancelEditPane();
		});
		left.prefHeightProperty().bind(editorPane.heightProperty());
		filterEditPane.prefHeightProperty().bind(editorPane.heightProperty().add(bottom.heightProperty()));
		filterEditPane.visibleProperty().addListener((ob, old, newValue) -> {
			if(newValue) {
				filterEditVBox.getChildren().add(1, filterEditPane);
				tableView.prefHeightProperty().setValue((topicFilterList.size() + 1) * 35);
				tableView.refresh();
				//tableView.prefHeightProperty().bind(filterEditVBox.heightProperty().subtract(filterEditPane.heightProperty()));
			} else {
				filterEditVBox.getChildren().remove(1);
				tableView.prefHeightProperty().setValue(topicFilterList.size() * 35);
				tableView.refresh();
				//tableView.prefHeightProperty().bind(filterEditVBox.heightProperty());
			}
		});

		filterEditPane.setVisible(false);
	}

	private void cancelEditPane() {
		if(filter != null) {
			tableView.getItems().add(filter);
			topicFilterList.add(filter);
			filter = null;
			tableView.setVisible(true);
		}
		filterEditPane.setVisible(false);
		editorPane.getChildren().clear();
		editorPane.autosize();
		filterEditPane.autosize();
	}

}
