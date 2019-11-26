package com.legendapl.lightning.tools.controller;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.jfoenix.controls.JFXTreeView;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.model.FolderResource;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.common.Utils;
import com.legendapl.lightning.tools.model.CsvRow;
import com.legendapl.lightning.tools.model.DomainRow;
import com.legendapl.lightning.tools.model.ProcessFlag;
import com.legendapl.lightning.tools.service.ExecuteAPIService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIcon;
import de.jensd.fx.glyphs.materialdesignicons.MaterialDesignIconView;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;

/**
 * ドメイン画面のコントローラクラス
 *
 * @author LAC_潘
 * @since 2017.09.18
 *
 */
public class P85DomainAnchorPane extends P80BaseToolsAnchorPane {

	@FXML
	private JFXTreeView<FolderResource> folderTree;
	@FXML
	private JFXTreeView<FolderResource> fileList;
	@FXML
	private JFXTextField dirPath;
	@FXML
	private JFXTreeTableView<DomainRow> dataTable;
	@FXML
	private JFXTreeTableColumn<DomainRow, String> sourceId;
	@FXML
	private JFXTreeTableColumn<DomainRow, String> labelServer;
	@FXML
	private JFXTreeTableColumn<DomainRow, String> labelUpdated;
	@FXML
	protected JFXButton refresh;
	@FXML
	protected JFXButton cutover;
	@FXML
	protected Label fileLabel;

	public List<ClientResourceLookup> directoryList = new ArrayList<ClientResourceLookup>();
	private MaterialDesignIcon folderIcon = MaterialDesignIcon.FOLDER_OUTLINE;
	private FontAwesomeIcon folderExpandedIcon = FontAwesomeIcon.FOLDER_OPEN_ALT;
	private String lastPath = new String("");

	private List<JFXTreeTableColumn<DomainRow, String>> dataTableColumnName = new ArrayList<JFXTreeTableColumn<DomainRow, String>>();
	private List<String> dataTableFunctionName = new ArrayList<String>();

	private String uriRemember = new String("");
	private Document dataFromServer = null;
	private Document dataUpdated = null;
	private ObservableList<DomainRow> domainRowList = FXCollections.observableArrayList();
	private ObservableList<DomainRow> domainRowListBackup = FXCollections.observableArrayList();
	private List<CsvRow> csvRowList = new ArrayList<CsvRow>();
	private List<String> errorMessages = new ArrayList<String>();

	@Override
	public void init(URL location, ResourceBundle resources) {
		dataTableColumnName = new ArrayList<JFXTreeTableColumn<DomainRow, String>>();
		dataTableColumnName.add(sourceId);
		dataTableColumnName.add(labelServer);
		dataTableColumnName.add(labelUpdated);

		dataTableFunctionName = new ArrayList<String>();
		dataTableFunctionName.add(new String("getSourceId"));
		dataTableFunctionName.add(new String("getLabelServer"));
		dataTableFunctionName.add(new String("getLabelUpdated"));

		setPathFlag(false);

		folderTree.setVisible(true);
		fileList.setVisible(false);
		
		//TODO : may set visible for true
		helpButton.setVisible(false);

		logger.debug("Init: Loading list and tree folder.");
		loadListAndTree();
	}
	
	@Override
	protected List<String> getHelpContent() {
		String head = Constants.P85_LABEL_SERVER + getSpaces(6) +
					  Constants.P85_LABEL_UPDATED + getSpaces(6) + 
					  Constants.P85_LABEL_RESULT;
		return Arrays.asList(
				head,
				Constants.P85_LABEL_EMPTY + getSpaces(21) + Constants.P85_LABEL_NOT_EMPTY + getSpaces(12) + Constants.P85_LABEL_RESULT_UPDATE,
				Constants.P85_LABEL_NOT_EMPTY + getSpaces(9) + Constants.P85_LABEL_NOT_EMPTY + getSpaces(12) + Constants.P85_LABEL_RESULT_UPDATE,
				Constants.P85_LABEL_EMPTY + getSpaces(21) + Constants.P85_LABEL_EMPTY + getSpaces(24) + Constants.P85_LABEL_RESULT_CLEAR,
				Constants.P85_LABEL_NOT_EMPTY + getSpaces(9) + Constants.P85_LABEL_EMPTY + getSpaces(24) + Constants.P85_LABEL_RESULT_SKIP
		);
	}
	
	private String getSpaces(int num) {
		String s = new String("");
		for (int i = 0; i < num; i++) {
			s = s.concat(" ");
		}
		return s;
	}

	/*
	 * 【CSVインポート】　押下
	 */
	protected boolean csvImportWork(List<CsvRow> csvRowListTmp) {
		if (csvRowListTmp == null) {
			logger.error("Import: Failed to import file.");
			return false;
		}
		logger.debug("Import: Target file is chosen.");

		if (!getWork()) {
			return false;
		}

		csvRowList = csvRowListTmp;
		if (!csvCheck()) {
			logger.error("Import: Failed to import file.");
			showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
			return false;
		}
		
		logger.debug("Import: domainRowList to domainRowListBackup");
		domainRowListBackup.clear();
		domainRowListBackup.addAll(domainRowList);

		logger.debug("Import: Converting data from csv file to software.");
		NodeList nodelist;
        String sourceId, labelServer, labelUpdated;
		for (CsvRow csvRow : csvRowList) {
			sourceId = csvRow.get(Constants.P85_SOURCE_ID);
			labelServer = csvRow.get(Constants.P85_LABEL_SERVER);
			labelUpdated = csvRow.get(Constants.P85_LABEL_UPDATED);
			if (!labelServer.isEmpty() && labelUpdated.isEmpty()) {
				continue;
			}

			nodelist = dataUpdated.getElementsByTagName(Constants.P85_XML_ITEM_GROUP);
			for (int i = 0; i < nodelist.getLength(); i++) {
				Element element = (Element) nodelist.item(i);
				if (element.getAttribute(Constants.P85_XML_ID).equals(sourceId)) {
					element.setAttribute(Constants.P85_XML_LABEL, labelUpdated);
				}
			}
			nodelist = dataUpdated.getElementsByTagName(Constants.P85_XML_ITEM);
			for (int i = 0; i < nodelist.getLength(); i++) {
				Element element = (Element) nodelist.item(i);
				if (element.getAttribute(Constants.P85_XML_ID).equals(sourceId)) {
					element.setAttribute(Constants.P85_XML_LABEL, labelUpdated);
				}
			}

			DomainRow domainRow = getDomainRowBySourceId(sourceId);
			if (domainRow != null) {
				domainRow.setLabelUpdated(labelUpdated);
				domainRow.setFlag(ProcessFlag.UPDATE);
			}
		}

        Collections.sort(domainRowList);

        logger.debug("Import: Showing data to table.");
        showDataOnPlatform();

        logger.info("Import: Successed to import file.");
		setImportFlag(true);
		return true;
	}

	private boolean csvCheck() {
		errorMessages = new ArrayList<String>();
		List<String> sourceIdList = new ArrayList<String>();
		boolean flag = false;
		String sourceId = new String("");
		String labelServer = new String("");
		String labelUpdated = new String("");

		// リソースID 必須
		sourceId = csvRowList.get(0).get(Constants.P85_SOURCE_ID);
		if (sourceId == null) {
			// CSVのデータにカラム[0]が見つかりませんでした。
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_NO_COLUMN, Constants.P85_SOURCE_ID));
			flag = true;
		}
		
		// 現在の表示名  必須
		labelServer = csvRowList.get(0).get(Constants.P85_LABEL_SERVER);
		if (labelServer == null) {
			// CSVのデータにカラム[0]が見つかりませんでした。
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_NO_COLUMN, Constants.P85_LABEL_SERVER));
			flag = true;
		}

		// 更新するラベル  必須
		labelUpdated = csvRowList.get(0).get(Constants.P85_LABEL_UPDATED);
		if (labelUpdated == null) {
			// CSVのデータにカラム[0]が見つかりませんでした。
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_NO_COLUMN, Constants.P85_LABEL_UPDATED));
			flag = true;
		}

		if (flag) {
			return false;
		}

		// 現在の表示名  更新するラベル  フォーマット
		boolean allEmpty = true;
		for (int i = 0; i < csvRowList.size(); i++) {
			CsvRow csvRow = csvRowList.get(i);
			labelServer = csvRow.get(Constants.P85_LABEL_SERVER);
			labelUpdated = csvRow.get(Constants.P85_LABEL_UPDATED);
			if (!(!labelServer.isEmpty() && labelUpdated.isEmpty())) {
				allEmpty = false;
				break;
			}
		}
		if (allEmpty) {
			// CSVのデータに処理すべきデータがありません。
			errorMessages.add(Utils.getString(Constants.DATA_ERROR_FLAGS_ALL_EMPTY));
		}
		
		// リソースID
		for (int i = 0; i < csvRowList.size(); i++) {
			CsvRow csvRow = csvRowList.get(i);
			flag = false;

			// リソースID 入力必須
			sourceId = csvRow.get(Constants.P85_SOURCE_ID);
			if (sourceId.isEmpty()) {
				// {0}行：{1}は必須入力です。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY, csvRow.getRowNo(), Constants.P85_SOURCE_ID));
				flag = true;
			}

			if (flag) {
				continue;
			}

			// リソースID 存在チェック
			if (getDomainRowBySourceId(sourceId) == null) {
				// {0}行：「{1}」の入力がサーバーに存在していません。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST, csvRow.getRowNo(), Constants.P85_SOURCE_ID));
			}

			// リソースID 重複チェック
			if (inList(sourceId, sourceIdList)) {
				// {0}行：[1]の入力はほかの行に重複しています。
				errorMessages.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE, csvRow.getRowNo(), Constants.P85_SOURCE_ID));
			}
			sourceIdList.add(sourceId);
		}

		return errorMessages.isEmpty() ? true : false;
	}

	private DomainRow getDomainRowBySourceId(String sourceId) {
		for (int i = 0; i < domainRowList.size(); i++) {
			if (domainRowList.get(i).getSourceId().get().equals(sourceId)) {
				return domainRowList.get(i);
			}
		}
		return null;
	}

	private boolean inList(String str, List<String>list) {
		for (String s : list) {
			if (s != null && s.equals(str)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * 【CSVエクスポート】　押下
	 */
	protected boolean csvExportWork(File filename) {
		if (filename == null) {
			logger.debug("Export: Chosen file is null.");
			return false;
		}
		logger.debug("Export: Target file is chosen.");

		if (!getWork()) {
			return false;
		}

		logger.debug("Export: Converting data from software to csv file.");
		List<List<String>> saveFile = getSaveFileByRowList(domainRowList);

		logger.debug("Export: Saving csv file.");
		if (!saveCsv(filename, saveFile)) {
			logger.debug("Export: Failed to save csv file.");
			return false;
		}

		showInfo(Utils.getString(Constants.DLG_INFO_EXPORT_SUCC));
		logger.info("Export: Successed to export file.");
		return true;
	}

	private List<List<String>> getSaveFileByRowList(ObservableList<DomainRow> rowList) {
		List<List<String>> file = new ArrayList<List<String>>();
		List<String> listStr = new ArrayList<String>();

		listStr = new ArrayList<String>();
		listStr.add(Constants.P85_SOURCE_ID);
		listStr.add(Constants.P85_LABEL_SERVER);
		listStr.add(Constants.P85_LABEL_UPDATED);
		file.add(listStr);

		for (int i = 0; i < rowList.size(); i++) {
			listStr = new ArrayList<String>();
			listStr.add(rowList.get(i).getSourceId().get());
			listStr.add(rowList.get(i).getLabelServer().get());
			listStr.add(new String(""));
			file.add(listStr);
		}

		return file;
	}

	/*
	 * 【取得】　押下
	 */
	protected boolean getWork() {
		InputStream inputStream;
		try {
			logger.debug("Get: Getting data from server.");
			inputStream = ExecuteAPIService.getDomainInputStream(uriRemember);
			logger.debug("Get: Converting data from server to software.");
			convertDataFromServerToUpdated(inputStream);
		}
		catch (ParserConfigurationException | SAXException | IOException e) {
			logger.error("Get: Failed to convert data from server to software.");
			logger.error(e.getMessage(), e);
			setGetFlag(false);
			return false;
		}
		catch (Exception e) {
			logger.error("Get: Failed to get data from server.");
			logger.error(e.getMessage(), e);
			setGetFlag(false);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
			return false;
		}

		logger.debug("Get: Showing data to table.");
		showDataOnPlatform();

		setImportFlag(false);
		setGetFlag(true);
		logger.info("Get: Successed to get data from server.");
		return true;
	}

	private void convertDataFromServerToUpdated(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		dataFromServer = documentBuilder.parse(inputStream);
		dataFromServer.getDocumentElement().normalize();

        NodeList nodeList;
        Element element;
        String id, label;

        domainRowList = FXCollections.observableArrayList();
        nodeList = dataFromServer.getElementsByTagName(Constants.P85_XML_ITEM_GROUP);
        for (int i = 0; i < nodeList.getLength(); i++) {
        	element = (Element) nodeList.item(i);
            id = element.getAttribute(Constants.P85_XML_ID);
            label = element.getAttribute(Constants.P85_XML_LABEL);
            if (label.isEmpty()) {
            	label = Constants.P85_SERVER_LABEL_IS_EMPTY;
            }
            domainRowList.add(new DomainRow(id, label, new String("")));
        }
        nodeList = dataFromServer.getElementsByTagName(Constants.P85_XML_ITEM);
        for (int i = 0; i < nodeList.getLength(); i++) {
        	element = (Element) nodeList.item(i);
            id = element.getAttribute(Constants.P85_XML_ID);
            label = element.getAttribute(Constants.P85_XML_LABEL);
            if (label.isEmpty()) {
            	label = Constants.P85_SERVER_LABEL_IS_EMPTY;
            }
            domainRowList.add(new DomainRow(id, label, new String("")));
        }
        Collections.sort(domainRowList);

        dataUpdated = documentBuilder.newDocument();
        org.w3c.dom.Node node = dataUpdated.importNode(dataFromServer.getDocumentElement(), true);
        dataUpdated.appendChild(node);
	}

	private void showDataOnPlatform() {
		showData(domainRowList, dataTableColumnName, dataTableFunctionName, dataTable);
	}

	/*
	 * 【適用】　押下
	 */
	protected boolean applyWork() {
		logger.debug("Apply: Saving backup file.");
		List<List<String>> saveFile = getSaveFileByRowList(domainRowListBackup);
		if (!saveBackup(saveFile)) {
			return false;
		}

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Source xmlSource = new DOMSource(dataUpdated);
        Result outputTarget = new StreamResult(outputStream);
        try {
			TransformerFactory.newInstance().newTransformer().transform(xmlSource, outputTarget);
			logger.debug("Apply: Applying data to server.");
			ExecuteAPIService.updateDomain(uriRemember, outputStream.toByteArray());
		}
        catch (TransformerException | TransformerFactoryConfigurationError e) {
			logger.error("Apply: Failed to transfor xml.");
			logger.error(e.getMessage(), e);
			return false;
		}
        catch (Exception e) {
        	logger.error("Apply: Failed to apply data to server.");
        	logger.error(e.getMessage(), e);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_APPLY), e);
        	return false;
        }

		if (!getWork()) {
			return false;
		}

		showInfo(Utils.getString(Constants.DLG_INFO_APPLY_SUCC));
		logger.info("Apply: Successed to apply data to server.");
		return true;
	}

	/*
	 * 【refresh】、【switch】　押下
	 */
	private boolean turnTree = true;

	public void refreshFired(ActionEvent event) {
		if (!canSync()) {
			return;
		}
		logger.debug("Refresh: Loading list and tree folder.");
		loadListAndTree();
	}

	public void cutoverFired(ActionEvent event) {
		turnTree = !turnTree;
		if (turnTree) { // tree
			folderTree.setVisible(true);
			fileList.setVisible(false);
			fileLabel.setText(Constants.P85_BUTTON_FOLDER);
		}
		else { // list
			folderTree.setVisible(false);
			fileList.setVisible(true);
			fileLabel.setText(Constants.P85_BUTTON_LIST);
		}
	}

	public void cleanBeforeLoading() {
		setGetFlag(false);
		setImportFlag(false);
		setPathFlag(false);
		dirPath.clear();
		dirPath.setText("/");
		dirPath.setEditable(false);
		lastPath = new String("");
		uriRemember = new String("");
		resourceName = new String("");
		domainRowList = FXCollections.observableArrayList();
		showData(domainRowList, dataTableColumnName, dataTableFunctionName, dataTable);
	}

	private void loadListAndTree() {
		cleanBeforeLoading();
		TreeItem<FolderResource> listRoot = new TreeItem<FolderResource>(new FolderResource("root"), new MaterialDesignIconView(folderIcon));
		TreeItem<FolderResource> treeRoot = new TreeItem<FolderResource>(new FolderResource("root"), new MaterialDesignIconView(folderIcon));

	backW.run(
	() -> {
		// get domain list
		List<ClientResourceLookup> domainList;
		try {
			domainList = ExecuteAPIService.getDomainList();
		}
		catch (Exception e) {
			logger.error("loadListAndTree: Failed to get list/tree from server.");
			logger.error(e.getMessage(), e);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
			return;
		}

		// get folder list
		List<ClientResourceLookup> folderList;
		try {
			folderList = ExecuteAPIService.getFolderList();
		}
		catch (Exception e) {
			logger.error("loadListAndTree: Failed to get list/tree from server.");
			logger.error(e.getMessage(), e);
			showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
			return;
		}

		// get map
		Map<String, String> map = new HashMap<String, String>();
		for (ClientResourceLookup folder : folderList) {
			map.put(folder.getUri(), folder.getLabel());
		}
		for (ClientResourceLookup domain : domainList) {
			map.put(domain.getUri(), domain.getLabel());
		}
		for (ClientResourceLookup domain : domainList) {
			String paths[] = domain.getUri().split(Pattern.quote("/"));
			String treePath = new String("");
			for (int i = 1; i < paths.length; i++) {
				treePath = treePath.concat("/"+paths[i]);
				if (map.get(treePath) == null) {
					ClientResourceLookup lookup;
					try {
						lookup = ExecuteAPIService.getResource(treePath);
					}
					catch (Exception e) {
						logger.error("loadListAndTree: Failed to get list/tree from server.");
						logger.error(e.getMessage(), e);
						showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
						return;
					}
					map.put(lookup.getUri(), lookup.getLabel());
				}
			}
		}

		// get list
		for (ClientResourceLookup domain : domainList) {
			TreeItem<FolderResource> child = new TreeItem<FolderResource>(new FolderResource(domain));
			listRoot.getChildren().add(child);
		}

		// get tree
		TreeItem<FolderResource> root = new TreeItem<FolderResource>();
		root.setValue(new FolderResource(new ClientResourceLookup()));
		root.getValue().setUri("/");
		root.getValue().setLabel(getLoginOrgId());
		root.getValue().setResourceType(Constants.P85_FILE_TYPE_FOLDER);
		setUnExpandedIcon(root);
		addListener(root);

		for (ClientResourceLookup domain : domainList) {
			String paths[] = domain.getUri().split(Pattern.quote("/"));
			String treePath = new String("");
			TreeItem<FolderResource> node = root;
			for (int i = 1; i < paths.length; i++) { // begin with 1
				treePath = treePath.concat("/"+paths[i]);
				TreeItem<FolderResource> child = new TreeItem<FolderResource>();
				child.setValue(new FolderResource(new ClientResourceLookup()));
				child.getValue().setUri(treePath);
				TreeItem<FolderResource> childx = findChild(child, node.getChildren());
				if (childx == null) {
					child.getValue().setLabel(map.get(treePath));
					if (i + 1 != paths.length) {
						child.getValue().setResourceType(Constants.P85_FILE_TYPE_FOLDER);
						setUnExpandedIcon(child);
						addListener(child);
					}
					else {
						child.getValue().setResourceType(Constants.P85_FILE_TYPE_DOMAIN);
					}
					node.getChildren().add(child);
					node = child;
				}
				else {
					node = childx;
				}
			}
		}

		TreeItem<FolderResource> publicF = null;
		for (TreeItem<FolderResource> child: root.getChildren()) {
			if ("/public".equals(child.getValue().getUri())) {
				publicF = child;
				break;
			}
		}
		if (publicF != null) {
			root.getChildren().remove(publicF);
			treeRoot.getChildren().add(root);
			treeRoot.getChildren().add(publicF);
		}

		logger.info("List and Folder tree has been synced.");
		
		logger.debug("Show empty data on platform at the last of loading list and tree");
		showData(domainRowList, dataTableColumnName, dataTableFunctionName, dataTable);
	});

		fileList.setRoot(listRoot);
		fileList.setShowRoot(false);
		fileList.setOnMouseClicked((e) -> handleMouseClickedList(e));

		folderTree.setRoot(treeRoot);
		folderTree.setShowRoot(false);
		folderTree.setOnMouseClicked((e) -> handleMouseClickedTree(e));
	}

	private String getLoginOrgId() {
		if (ExecuteAPIService.getSplitNumber(Constant.ServerInfo.userName) != 0) {
			return Constant.ServerInfo.userName.split(Pattern.quote("|"))[1];
		}
		else {
			return new String("root");
		}
	}

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

	private void handleMouseClickedList(MouseEvent event) {
		javafx.scene.Node eventNode = event.getPickResult().getIntersectedNode();
		if (eventNode == null || fileList.getSelectionModel() == null
						 	  || fileList.getSelectionModel().getSelectedItem() == null
						 	  || fileList.getSelectionModel().getSelectedItem().getValue() == null) {
			return;
		}

		FolderResource resource = fileList.getSelectionModel().getSelectedItem().getValue();
		String uri = resource.getUri();

		if (uri == null || uri.isEmpty()) {
			return;
		}

		if (!uri.equals(lastPath)) {
			if (!canSync()) {
				return;
			}
			setPathFlag(true);
			lastPath = uri;
			uriRemember = uri;
			dirPath.clear();
			dirPath.setText(uri);
			resourceName = resource.getLabel();
			getWithoutNotify();
		}
	}

	private void handleMouseClickedTree(MouseEvent event) {
		javafx.scene.Node eventNode = event.getPickResult().getIntersectedNode();
		if (eventNode == null || folderTree.getSelectionModel() == null
							  || folderTree.getSelectionModel().getSelectedItem() == null
							  || folderTree.getSelectionModel().getSelectedItem().getValue() == null) {
			return;
		}

		FolderResource resource = folderTree.getSelectionModel().getSelectedItem().getValue();
		String uri = resource.getUri();

		if (uri == null || uri.isEmpty()) {
			return;
		}

		boolean isDomainFile = Constants.P85_FILE_TYPE_DOMAIN.equals(resource.getResourceType());
		if (isDomainFile) {
			if (!uri.equals(lastPath)) {
				if (!canSync()) {
					return;
				}
				setPathFlag(true);
				lastPath = uri;
				uriRemember = uri;
				dirPath.clear();
				dirPath.setText(uri);
				resourceName = resource.getLabel();
				getWithoutNotify();
			}
		}
	}

	private void setUnExpandedIcon(TreeItem<FolderResource> node) {
	Platform.runLater(() -> {
		MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
		node.setGraphic(graphic);
	});
	}

	private void addListener(TreeItem<FolderResource> node) {
	node.expandedProperty().addListener((e) -> {
		if (node.isExpanded()) {
			node.setGraphic(new FontAwesomeIconView(folderExpandedIcon));
		}
		else {
			MaterialDesignIconView graphic = new MaterialDesignIconView(folderIcon);
			node.setGraphic(graphic);
		}
	});
	}
}
