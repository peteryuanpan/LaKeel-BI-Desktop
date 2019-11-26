package com.legendapl.lightning.adhoc.controller;

import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXB;

import org.apache.commons.io.IOUtils;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jfoenix.controls.JFXTextField;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.model.DatabaseInfo;
import com.legendapl.lightning.adhoc.model.Domain;
import com.legendapl.lightning.adhoc.model.LoadDataRow;
import com.legendapl.lightning.adhoc.model.Topic;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.DatabaseService;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.model.DataSourceImpl;
import com.legendapl.lightning.tools.data.AdhocData;
import com.legendapl.lightning.adhoc.model.FolderResource;
import com.legendapl.lightning.adhoc.service.ExecuteAPIService;
import com.legendapl.lightning.adhoc.service.ShareDataService;

import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;

/**
 * サーバーからデータを読み込む画面のコントローラクラス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class P101ServerDataAnchorPane extends P100LoadDataBaseAnchorPane {

	@FXML
	private Button doTopic;
	@FXML
	private JFXTextField dirPath;
	@FXML
	private TreeView<FolderResource> folderTree;
	@FXML
	private TreeView<FolderResource> fileList;
	@FXML
	private TreeTableView<LoadDataRow> treeTableView;
	@FXML
	private TreeTableColumn<LoadDataRow, String> fileName;
	@FXML
	private TreeTableColumn<LoadDataRow, String> description;
	@FXML
	private TreeTableColumn<LoadDataRow, String> createTime;
	@FXML
	private TreeTableColumn<LoadDataRow, String> updateTime;
	
	private ObservableList<LoadDataRow> dataRowList = FXCollections.observableArrayList();
	private List<TreeTableColumn<LoadDataRow, String>> columnObjectList;
	private List<String> functionNameList;
	private Map<String, List<FolderResource>> mapUriToShowDataList;
	private LoadDataRow clickedDataRow;
	private String lastPath = null;

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		logger.debug("initialize");
		
		columnObjectList = new ArrayList<TreeTableColumn<LoadDataRow, String>>();
		columnObjectList.add(fileName);
		columnObjectList.add(description);
		columnObjectList.add(createTime);
		columnObjectList.add(updateTime);
		
		functionNameList = new ArrayList<String>();
		functionNameList.add("getFileName");
		functionNameList.add("getDescription");
		functionNameList.add("getCreateTime");
		functionNameList.add("getUpdateTime");
		
		folderTree.setVisible(true);
		fileList.setVisible(false);
		dirPath.setText(null);
		lastPath = null;
		
		doTopic.disableProperty().bind(doTopicButtonDisable());
		
		treeTableView.setOnMouseClicked((event) -> handleActionOnMouseClickedTreeTableView(event));
		
		logger.debug("Loading list and tree folder.");
		loadListAndTree();
	}
	
	/**
	 * 切り替えボタンのイベント
	 * 
	 * @param event
	 */
	public void cutoverFired(ActionEvent event) {
		super.cutoverFired(event);
		Platform.runLater(() -> {
			// initialize
			final TreeItem<FolderResource> resource = turnTree ? 
					folderTree.getSelectionModel().getSelectedItem() :
					fileList.getSelectionModel().getSelectedItem();
			final String uri;
			if (null == resource) uri = null;
			else uri = resource.getValue().getUri();
			// dirPath
			dirPath.clear();
			dirPath.setText(getUriForShow(uri));
		});
	}
	
	/**
	 * 
	 * @return
	 */
	private ObservableValue<? extends Boolean> doTopicButtonDisable() {
		return new BooleanBinding() {
	        {
	        	super.bind(fileList.getSelectionModel().getSelectedItems(),
	        			   treeTableView.getSelectionModel().getSelectedItems());
	        }
	        @Override protected boolean computeValue() {
	        	TreeItem<FolderResource> resource = fileList.getSelectionModel().getSelectedItem();
	        	if (null != resource && !turnTree) {
	        		return false;
	        	}
	        	TreeItem<LoadDataRow> row = treeTableView.getSelectionModel().getSelectedItem();
	        	if (null != row) {
	        		return false;
	        	}
	        	return true;
			}
		};
	}

	/**
	 * 
	 * @param event
	 */
	public void doTopic(ActionEvent event) {
		
		if (null == clickedDataRow) {
			logger.warn("clickedDataRow is null.");
			return;
		}
		
		backW.run(
		() -> {
			// まず、サーバーからドメインデータを取得
			InputStream inputStream = null;
			try {
				inputStream = ExecuteAPIService.getDomainInputStream(clickedDataRow.getUri());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showExceptionMessage(AdhocUtils.getString("ERROR_READ_TITLE"), e);
				throw new Exception(e);
			}
			
			//　データベースを取得
			GetDatabaseFuncImpl func = new GetDatabaseFuncImpl();
			DatabaseInfo database = getDatabase(func);
			
			// ドメインデータからトピックデータに変換
			Topic topic = null;
			try {
				String XMLString = IOUtils.toString(inputStream, "UTF-8");
				Pattern pattern = Pattern.compile("([\\S\\s]*)xmlns=\"([\\S]*)\"([\\S\\s]*)");
				Matcher matcher = pattern.matcher(XMLString);
				matcher.matches();
				XMLString = XMLString.replaceAll(matcher.group(2), "");
				logger.debug(XMLString);
				StringReader srd = new StringReader(XMLString);
				Domain domain = JAXB.unmarshal(srd, Domain.class);
				domain.setDatabase(database);
				domain.setOthers();
				topic = new Topic(domain);
				topic.setOthers();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"), 
						AdhocUtils.getString("ERROR_READ_DOMAIN_DATA"));
				throw new Exception(e);
			}

			// データを保存
			setObject(topic);
			ShareDataService.share(topic);
		},
		() -> {
			// その後、トピック編集画面を開く
			try {
				C110TopicMenuPane.filePath = "";
				C110TopicMenuPane.fileName = clickedDataRow.getFileName().get();
				String paneFXML = "/view/P111TopicSelectAnchorPane.fxml";
				topicStage = showPane(event, paneFXML, 
									  getTopicTitle("P111.window.title", C110TopicMenuPane.fileName), 
									  Modality.APPLICATION_MODAL, null, AdhocUtils.bundleMessage);
				AdhocData.roots.put(paneFXML, topicStage.getScene().getRoot());
				topicStage.setMinWidth(AdhocConstants.Graphic.TOPIC_STAGE_MIN_WIDTH);
				topicStage.setMinHeight(AdhocConstants.Graphic.TOPIC_STAGE_MIN_HEIGHT);
				topicStage.show();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"), 
						AdhocUtils.getString("ERROR_SHOW_TOIC_PANE"));
				return;
			}
		});
	}
	
	/**
	 * @throws Exception 
	 */
	private class GetDatabaseFuncImpl implements GetDatabaseFunc {
		@Override
		public DatabaseInfo get() throws Exception {
			DatabaseInfo database = DatabaseService.getDatabase(clickedDataRow.getUri());
			DataSourceImpl datasource = database.getDataSourceFromLocal();
			String password = datasource.getPassword();
			if (null == password || password.isEmpty()) {
				throw new PasswordNullEmptyException();
			}
			return database;
		}
	}
	
	/**
	 * テーブルのアイテムを押すイベント
	 * 
	 * @param event
	 */
	private void handleActionOnMouseClickedTreeTableView(MouseEvent event) {
		
		// check
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode) {
			return;
		}
		
		// clear
		folderTree.getSelectionModel().clearSelection();
		fileList.getSelectionModel().clearSelection();
		
		// initialize
		final TreeItem<LoadDataRow> row = treeTableView.getSelectionModel().getSelectedItem();
		final LoadDataRow value;
		final String uri;
		
		if (null != row) {
			value = row.getValue();
			uri = value.getUri();
		} else {
			value = null;
			uri = null;
		}
		
		// clickedDataRow
		clickedDataRow = value;
		
		// dirPath
		dirPath.clear();
		dirPath.setText(uri);
		
		// double Clicked
		if (MouseButton.PRIMARY.equals(event.getButton()) && 2 == event.getClickCount()) {
			doTopic(null);
		}
	}
	
	/**
	 * ファイルリストのアイテムを押すイベント
	 * 
	 * @param event
	 */
	private void handleActionOnMouseClickedFileList(MouseEvent event) {
		
		// check
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode || 
				null == fileList.getSelectionModel() ||
				null == fileList.getSelectionModel().getSelectedItem() ||
				null == fileList.getSelectionModel().getSelectedItem().getValue()) {
			return;
		}
		
		// clear
		treeTableView.getSelectionModel().clearSelection();
		
		// initialize
		final FolderResource value = fileList.getSelectionModel().getSelectedItem().getValue();
		final String uri = value.getUri();
		
		// dirPath
		dirPath.clear();
		dirPath.setText(uri);
		
		// show
		lastPath = uri;
		showDataOnTable(uri);
		
		// clickedDataRow
		clickedDataRow = new LoadDataRow();
		clickedDataRow.setFileType(value.getFileType());
		clickedDataRow.setFileName(value.getLabel());
		clickedDataRow.setUri(value.getUri());
		
		// double Clicked
		if (MouseButton.PRIMARY.equals(event.getButton()) && 2 == event.getClickCount()) {
			doTopic(null);
		}
	}
	
	/**
	 * ツリーフォルダのアイテムを押すイベント
	 * 
	 * @param event
	 */
	private void handleActionOnMouseClickedFolderTree(MouseEvent event) {
		
		// check
		javafx.scene.Node clickedNode = event.getPickResult().getIntersectedNode();
		if (null == clickedNode || 
				null == folderTree.getSelectionModel() ||
				null == folderTree.getSelectionModel().getSelectedItem() ||
				null == folderTree.getSelectionModel().getSelectedItem().getValue()) {
			return;
		}
		
		// clear
		treeTableView.getSelectionModel().clearSelection();
		
		// initialize
		final TreeItem<FolderResource> resource = folderTree.getSelectionModel().getSelectedItem();
		final FolderResource value = folderTree.getSelectionModel().getSelectedItem().getValue();
		final String uri = value.getUri();
		
		// dirPath
		dirPath.clear();
		dirPath.setText(uri);
		
		// check
		if (null != lastPath && lastPath.equals(uri) &&
				!(clickedNode instanceof MaterialDesignIconView)) {
			return;
		}
		
		// setExpanded
		if (clickedNode instanceof MaterialDesignIconView) {
			resource.setExpanded(true);
		}
		
		// show
		lastPath = uri;
		showDataOnTable(uri);
	}

	/**
	 * 
	 * @param uri
	 */
	private void showDataOnTable(String uri) {
		
		dataRowList = FXCollections.observableArrayList();
		
		List<FolderResource> files = mapUriToShowDataList.get(uri);
		if (files != null) {
			for (FolderResource file : files) {
				LoadDataRow dataRow = new LoadDataRow(
						file.getLabel(),
						file.getDescription(),
						null, //fileType
						file.getCreationDate(),
						file.getUpdateDate(),
						file.getUri()
				);
				dataRowList.add(dataRow);
			}
		}
		
		showData(dataRowList, columnObjectList, functionNameList, treeTableView);
	}

	/**
	 * 
	 */
	private void cleanBeforeLoading() {
		dirPath.clear();
		dirPath.setText(null);
		lastPath = null;
		dataRowList = FXCollections.observableArrayList();
		showData(dataRowList, columnObjectList, functionNameList, treeTableView);
	}

	/**
	 * 
	 */
	@Override protected void loadListAndTree() {
		
		cleanBeforeLoading();
		TreeItem<FolderResource> listRoot = new TreeItem<FolderResource>(new FolderResource("root"), new MaterialDesignIconView(folderIcon));
		TreeItem<FolderResource> treeRoot = new TreeItem<FolderResource>(new FolderResource("root"), new MaterialDesignIconView(folderIcon));
		
		backW.run(
		() -> {
			// get domain list
			List<ClientResourceLookup> domainList;
			try {
				domainList = ExecuteAPIService.getDomainList();
			} catch (Exception e) {
				logger.error("Failed to get list/tree from server.");
				logger.error(e.getMessage(), e);
				AlertWindowService.showExceptionMessage(AdhocUtils.getString("SERVER_ERROR_GET"), e);
				return;
			}
			
			// get folder list
			List<ClientResourceLookup> folderList;
			try {
				folderList = ExecuteAPIService.getFolderList();
			} catch (Exception e) {
				logger.error("Failed to get list/tree from server.");
				logger.error(e.getMessage(), e);
				AlertWindowService.showExceptionMessage(AdhocUtils.getString("SERVER_ERROR_GET"), e);
				return;
			}
			
			// insert map
			Map<String, FolderResource> map = new HashMap<>();
			for (ClientResourceLookup folder : folderList) {
				FolderResource resource = new FolderResource(folder);
				map.put(folder.getUri(), resource);
			}
			for (ClientResourceLookup domain : domainList) {
				FolderResource resource = new FolderResource(domain);
				map.put(domain.getUri(), resource);
			}
			for (ClientResourceLookup domain : domainList) {
				String paths[] = domain.getUri().split(Pattern.quote("/"));
				String treePath = new String("");
				for (int i = 1; i < paths.length; i++) {
					treePath = treePath.concat("/"+paths[i]);
					if (null == map.get(treePath)) {
						ClientResourceLookup lookup;
						try {
							lookup = ExecuteAPIService.getResource(treePath);
						} catch (Exception e) {
							logger.error("Failed to get list/tree from server.");
							logger.error(e.getMessage(), e);
							AlertWindowService.showExceptionMessage(AdhocUtils.getString("SERVER_ERROR_GET"), e);
							return;
						}
						map.put(lookup.getUri(), new FolderResource(lookup));
					}
				}
			}
	
			Platform.runLater(() -> {
				try {
					loadListAndTreeImpl(domainList, map, listRoot, treeRoot);
				} catch (Exception e) {
					logger.error("Failed to get list/tree from local.");
					logger.error(e.getMessage(), e);
					AlertWindowService.showExceptionMessage(AdhocUtils.getString("SERVER_ERROR_GET"), e);
				} finally {
					// show empty data at the last
					showData(dataRowList, columnObjectList, functionNameList, treeTableView);
				}
			});
		});
		
		fileList.setRoot(listRoot);
		fileList.setShowRoot(false);
		fileList.setOnMouseClicked((e) -> handleActionOnMouseClickedFileList(e));
	
		folderTree.setRoot(treeRoot);
		folderTree.setShowRoot(false);
		folderTree.setOnMouseClicked((e) -> handleActionOnMouseClickedFolderTree(e));
	}
	
	private void loadListAndTreeImpl(
			List<ClientResourceLookup> domainList, Map<String, FolderResource> map,
			TreeItem<FolderResource> listRoot, TreeItem<FolderResource> treeRoot) {
		
		// insert list
		for (ClientResourceLookup domain : domainList) {
			TreeItem<FolderResource> child = new TreeItem<FolderResource>(new FolderResource(domain));
			listRoot.getChildren().add(child);
		}

		// insert tree
		List<String> uris = new ArrayList<>();
		List<FolderResource> resources = new ArrayList<>();
		TreeItem<FolderResource> root = new TreeItem<FolderResource>();
		root.setValue(new FolderResource(new ClientResourceLookup()));
		root.getValue().setUri("/");
		String orgId = getLoginOrgId();
		root.getValue().setLabel(null == orgId ? "root" : orgId);
		setUnExpandedIcon(root);
		addListener(root);
		treeRoot.getChildren().add(root);
		for (ClientResourceLookup domain : domainList) {
			String paths[] = domain.getUri().split(Pattern.quote("/"));
			String treePath = new String("");
			TreeItem<FolderResource> node = root;
			if (null != orgId && paths.length > 1 && "public".equals(paths[1])) {
				node = treeRoot;
			}
			for (int i = 1; i < paths.length; i++) { // begin with 1
				treePath = treePath.concat("/"+paths[i]);
				FolderResource resource = new FolderResource(map.get(treePath));
				TreeItem<FolderResource> child = new TreeItem<FolderResource>(resource);
				TreeItem<FolderResource> childx = findChild(child, node.getChildren());
				if (null == childx) {
					if (i + 1 != paths.length) { // folder node
						setUnExpandedIcon(child);
						addListener(child);
						node.getChildren().add(child);
						node = child;
					} else { // leaf node
						uris.add(child.getValue().getUri());
						resources.add(resource);
						uris.add(node.getValue().getUri());
						resources.add(resource);
					}
				} else {
					node = childx;
				}
			}
		}
		
		// insert map (mapUriToShowDataList)
		mapUriToShowDataList = new HashMap<>();
		for (int i = 0; i < uris.size(); i ++) {
			if (null == mapUriToShowDataList.get(uris.get(i))) {
				List<FolderResource> showDataList = new ArrayList<>();
				for (int j = 0; j < uris.size(); j ++) {
					if (uris.get(i).equals(uris.get(j))) {
						showDataList.add(resources.get(j));
					}
				}
				sortListByLabel(showDataList);
				mapUriToShowDataList.put(uris.get(i), showDataList);
			}
		}
		
		// split public folder from root if needed
		TreeItem<FolderResource> publicF = null;
		for (TreeItem<FolderResource> child: root.getChildren()) {
			if ("/public".equals(child.getValue().getUri())) {
				publicF = child;
				break;
			}
		}
		if (publicF != null) {
			root.getChildren().remove(publicF);
			treeRoot.getChildren().add(publicF);
		}
		
		// sort children by label
		sortChildrenByLabel(root);
		sortChildrenByLabel(publicF);
		sortChildrenByLabel(listRoot);
		
		logger.info("List and Folder tree has been synced.");
	}

	/**
	 * 
	 * @return
	 */
	private String getLoginOrgId() {
		if (ExecuteAPIService.getSplitNumber(Constant.ServerInfo.userName) != 0) {
			return Constant.ServerInfo.userName.split(Pattern.quote("|"))[1];
		}
		else {
			return null;
		}
	}

	/**
	 * 「child」が「childs」にあるかどうかを判断する
	 * 
	 * @param child
	 * @param childs
	 * @return
	 */
	private TreeItem<FolderResource> findChild(TreeItem<FolderResource> child, List<TreeItem<FolderResource>> childs) {
		if (child != null && childs != null) {
			for (int i = 0; i < childs.size(); i++) {
				if (childs.get(i).getValue().getUri().equals(child.getValue().getUri())) {
					return childs.get(i);
				}
			}
		}
		return null;
	}
	
}
