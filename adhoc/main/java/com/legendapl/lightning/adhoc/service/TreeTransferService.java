package com.legendapl.lightning.adhoc.service;

import java.util.List;

import com.legendapl.lightning.adhoc.model.BaseNode;
import com.legendapl.lightning.adhoc.model.DBNode;
import com.legendapl.lightning.adhoc.model.DBTree;
import com.legendapl.lightning.adhoc.model.Item;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * ツリーデータとJavaFXコントローラを変換するサービス
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/3/1
 */
public class TreeTransferService {

	/**
	 * 「DBTree」を「TreeView<BaseNode>」に変換します。
	 *
	 * @param treeView
	 * @param dbTree
	 */
	public static void transferTree(TreeView<BaseNode> treeView, DBTree dbTree) {
		if (treeView != null && dbTree != null) {
			TreeItem<BaseNode> root = new TreeItem<>();
			for (Item item : dbTree.getRoot().getItems()) {
				TreeItem<BaseNode> child = new TreeItem<>(item);
				transferTreeNode(child);
				root.getChildren().add(child);
			}
			for (DBNode dbNode : dbTree.getRoot().getChildren()) {
				TreeItem<BaseNode> child = new TreeItem<>(dbNode);
				transferTreeNode(child);
				root.getChildren().add(child);
			}
			treeView.setRoot(root);
			treeView.setShowRoot(false);
		}
	}

	/**
	 * 「List<DBTree>」を「TreeItem<BaseNode>」に変換します。
	 *
	 * @param superRoot
	 * @param dbTrees
	 */
	public static void transferTree(TreeItem<BaseNode> superRoot, List<DBTree> dbTrees) {
		if (superRoot != null && dbTrees != null) {
			superRoot.getChildren().clear();
			for (DBTree dbTree : dbTrees) {
				TreeItem<BaseNode> root = new TreeItem<>(dbTree.getRoot());
				transferTreeNode(root);
				superRoot.getChildren().addAll(root.getChildren());
			}
		}
	}

	private static void transferTreeNode(TreeItem<BaseNode> root) {
		List<BaseNode> nodes = root.getValue().getNodes();
		for (BaseNode node: nodes) {
			TreeItem<BaseNode> treeItem = new TreeItem<>(node);
			root.getChildren().add(treeItem);
			transferTreeNode(treeItem);
		}
	}
	
	/**
	 * copy a tree
	 * @param root
	 */
	public static TreeItem<BaseNode> copyTree(TreeItem<BaseNode> root) {
		
		TreeItem<BaseNode> newRoot = new TreeItem<BaseNode>();
		newRoot.setExpanded(root.isExpanded());
		newRoot.setGraphic(root.getGraphic());
		
		BaseNode baseNode = root.getValue();
		if (baseNode instanceof Item) {
			Item item = new Item((Item)baseNode);
			newRoot.setValue(item);
		} else if (baseNode instanceof DBNode) {
			DBNode DBNode = new DBNode((DBNode)baseNode);
			newRoot.setValue(DBNode);
		}
		
		for (TreeItem<BaseNode> child : root.getChildren()) {
			TreeItem<BaseNode> newChild = copyTree(child);
			newRoot.getChildren().add(newChild);
		}
		return newRoot;
	}

}
