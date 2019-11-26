package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;

@SuppressWarnings("rawtypes")
public class SortableTrieTree {

	protected SortableTrieTreeNode root = new SortableTrieTreeNode(null);

	private int depth;

	private List<TableViewData> tempData;

	public SortableTrieTree(List<TableViewData> tempData) {
		root.setLayer(0);
		this.tempData = tempData;
	}

	//Classes in the list need to override equals function
	public final void insert(List<Comparable> words, TableViewData data) {
		depth = words.size();
		root.setLeaf(depth == 0);
		insert(this.root, words, data);
	}

	private void insert(SortableTrieTreeNode root, List<Comparable> words, TableViewData data) {
		for (int i = 0; i < words.size(); i++) {
			Comparable word = words.get(i);
			SortableTrieTreeNode child = root.getChild(word);
			if (child == null) {
				child = root.addChild(word);
				child.setLeaf(child.getLayer() == depth);
			}
			root = child;
		}
		root.addData(data);
	}

	public final void sortTree() {
		sortNode(this.root);
	}

	private void sortNode(SortableTrieTreeNode root) {
		root.sort();
		if(!root.getChilds().isEmpty()) {
			for(SortableTrieTreeNode child: root.getChilds()) {
				sortNode(child);
			}
		}
	}

	public void getDataList() {
		tempData.addAll(getDataList(this.root, new ArrayList<Object>()));
	}

	private List<TableViewData> getDataList(SortableTrieTreeNode root, List<Object> formatedGroups) {
		List<TableViewData> datas = new ArrayList<>();
		if(root.isLeaf()) {
			if(depth != 0) {
				datas.add(new TableViewData(formatedGroups));
			}
			boolean oddStyle = true;
			if(root.getDatas() != null) {
				for(TableViewData data: root.getDatas()) {
					data.setOddStyle(oddStyle);
					oddStyle = !oddStyle;
				}
				datas.addAll(root.getDatas());
			}

		} else {
			for(SortableTrieTreeNode child: root.getChilds()) {
				List<Object> formatedGroupsNew = new ArrayList<>(formatedGroups);
				formatedGroupsNew.add(child.getValue());
				datas.addAll(getDataList(child, formatedGroupsNew));
			}
		}
		return datas;
	}

}
