package com.legendapl.lightning.adhoc.service;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.common.ViewType;
import com.legendapl.lightning.adhoc.model.BaseNode;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * ツリーをスプリットするサービスを提供します。
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/3/1
 */
public class SplitTreeService {
	
	public static void splitTree(
			TreeItem<BaseNode> alSuperRoot, ViewType FLAG_TO_SET,
			TreeItem<BaseNode> srSuperRoot, ViewType SR_FLAG_OPPOSITE, 
			TreeItem<BaseNode> tgSuperRoot, ViewType TG_FLAG_OPPOSITE) {
		
		SplitTreeService.setTreeViewType(alSuperRoot, FLAG_TO_SET);
		SplitTreeService.insertTargetTree(alSuperRoot, tgSuperRoot, TG_FLAG_OPPOSITE);
		SplitTreeService.insertTargetTree(alSuperRoot, srSuperRoot, SR_FLAG_OPPOSITE);
	}
	
	public static void splitTree(
			List<TreeItem<BaseNode>> selectedItems, 
			TreeItem<BaseNode> alSuperRoot, ViewType FLAG_TO_SET,
			TreeItem<BaseNode> srSuperRoot, ViewType SR_FLAG_OPPOSITE, 
			TreeItem<BaseNode> tgSuperRoot, ViewType TG_FLAG_OPPOSITE) {
		
		SplitTreeService.setTreeViewType(selectedItems, alSuperRoot, FLAG_TO_SET);
		SplitTreeService.insertTargetTree(alSuperRoot, tgSuperRoot, TG_FLAG_OPPOSITE);
		SplitTreeService.insertTargetTree(alSuperRoot, srSuperRoot, SR_FLAG_OPPOSITE);
	}

	public static void setTreeViewType(List<TreeItem<BaseNode>> selectedItems, TreeItem<BaseNode> superRoot, ViewType FLAG_TO_SET) {
		for (TreeItem<BaseNode> selectedItem : selectedItems) {
			TreeItem<BaseNode> root = getNodeFromTreeById(superRoot, selectedItem.getValue().getId());
			setTreeViewType(root, FLAG_TO_SET);
		}
		setTreeViewTypeBySubTree(superRoot);
	}
	
	public static void setTreeViewType(TreeItem<BaseNode> root, ViewType FLAG_TO_SET) {
		root.getValue().setViewType(FLAG_TO_SET);
		for (TreeItem<BaseNode> child : root.getChildren()) {
			setTreeViewType(child, FLAG_TO_SET);
		}
	}
	
	public static void setTreeViewType(BaseNode node, TreeItem<BaseNode> root, ViewType FLAG_TO_SET) {
		TreeItem<BaseNode> keyRoot = getNodeFromTreeById(root, node.getId());
		if (null != keyRoot) {
			keyRoot.getValue().setViewType(FLAG_TO_SET);
		}
		for (BaseNode child : node.getNodes()) {
			setTreeViewType(child, root, FLAG_TO_SET);
		}
	}
	
	public static void setTreeViewTypeBySubTree(TreeItem<BaseNode> root) {
		int viewType = 0;
		for (TreeItem<BaseNode> child : root.getChildren()) {
			setTreeViewTypeBySubTree(child);
			viewType |= getViewType(child.getValue().getViewType());
		}
		if (viewType != 0) {
			root.getValue().setViewType(getViewType(viewType));
		}
	}
	
	static int getViewType(ViewType viewType) {
		switch(viewType) {
		case TOP:
			return 1;
		case BOTTOM:
			return 2;
		case LEFT:
			return 4;
		case RIGHT:
			return 8;
		case BOTH:
		default:
			return (1 | 2 | 4 | 8);
		}
	}
	
	static ViewType getViewType(int viewType) {
		switch(viewType) {
		case 1:
			return ViewType.TOP;
		case 2:
			return ViewType.BOTTOM;
		case 4:
			return ViewType.LEFT;
		case 8:
			return ViewType.RIGHT;
		case (1 | 2 | 4 | 8):
		default:
			return ViewType.BOTH;
		}
	}
	
	public static void insertTargetTree(TreeItem<BaseNode> alRoot, TreeItem<BaseNode> tgRoot, ViewType FLAG_OPPOSITE) {
		List<TreeItem<BaseNode>> newChildren = new ArrayList<>();
		for (TreeItem<BaseNode> alChild : alRoot.getChildren()) {
			if (!FLAG_OPPOSITE.equals(alChild.getValue().getViewType())) {
				TreeItem<BaseNode> newChild = new TreeItem<BaseNode>();
				newChild.setValue(alChild.getValue());
				TreeItem<BaseNode> tgChild = getNodeFromListById(tgRoot.getChildren(), alChild.getValue().getId());
				if (null == tgChild) {
					insertTargetTree(alChild, newChild, FLAG_OPPOSITE);
				} else {
					newChild.setExpanded(tgChild.isExpanded());
					insertTargetTree(alChild, tgChild, FLAG_OPPOSITE);
					newChild.getChildren().setAll(tgChild.getChildren());
				}
				newChildren.add(newChild);
			}
		}
		tgRoot.getChildren().setAll(newChildren);
	}
	
	public static void setTreeExpanded(List<TreeItem<BaseNode>> selectedItems, TreeItem<BaseNode> superRoot) {
		for (TreeItem<BaseNode> selectedItem : selectedItems) {
			TreeItem<BaseNode> root = getNodeFromTreeById(superRoot, selectedItem.getValue().getId());
			while (null != root.getParent()) {
				root.getParent().setExpanded(true);
				root = root.getParent();
			}
		}
	}
	
	public static void setTreeSelection(List<TreeItem<BaseNode>> selectedItems, TreeView<BaseNode> tree) {
		for (TreeItem<BaseNode> selectedItem : selectedItems) {
			TreeItem<BaseNode> root = getNodeFromTreeById(tree.getRoot(), selectedItem.getValue().getId());
			tree.getSelectionModel().select(root);
		}
	}
    
	public static TreeItem<BaseNode> getNodeFromTreeById(TreeItem<BaseNode> root, String keyId) {
		if (keyId.equals(root.getValue().getId())) return root;
		for (TreeItem<BaseNode> child : root.getChildren()) {
			TreeItem<BaseNode> keyChild = getNodeFromTreeById(child, keyId);
			if (null != keyChild) return keyChild;
		}
		return null;
	}
	
	public static TreeItem<BaseNode> getNodeFromListById(List<TreeItem<BaseNode>> children, String keyId) {
		for (TreeItem<BaseNode> child : children) {
			if (keyId.equals(child.getValue().getId())) return child;
		}
		return null;
	}
	
	public static TreeItem<BaseNode> getNodeFromTreeByResId(TreeItem<BaseNode> root, String keyResId) {
		if (keyResId.equals(root.getValue().getResourceId())) return root;
		for (TreeItem<BaseNode> child : root.getChildren()) {
			TreeItem<BaseNode> keyChild = getNodeFromTreeByResId(child, keyResId);
			if (null != keyChild) return keyChild;
		}
		return null;
	}
	
	public static TreeItem<BaseNode> getNodeFromListByResId(List<TreeItem<BaseNode>> children, String keyResId) {
		for (TreeItem<BaseNode> child : children) {
			if (keyResId.equals(child.getValue().getResourceId())) return child;
		}
		return null;
	}

}
