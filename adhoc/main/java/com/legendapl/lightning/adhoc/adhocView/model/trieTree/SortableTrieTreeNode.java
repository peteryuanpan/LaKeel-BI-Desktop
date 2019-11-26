package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.TableViewData;

@SuppressWarnings({"rawtypes", "unchecked"})
public class SortableTrieTreeNode implements Comparable<SortableTrieTreeNode> {

	private List<SortableTrieTreeNode> childs = new ArrayList<>();

	private Comparable value;

	private List<TableViewData> datas;

	private int layer;

	private boolean leaf;

	private Comparator<SortableTrieTreeNode> comparator = new Comparator<SortableTrieTreeNode>() {

		@Override
		public int compare(SortableTrieTreeNode o1, SortableTrieTreeNode o2) {
			if(o1.value == null && o2.value == null) {
				return 0;
			} else if(o1.value == null && o2.value != null) {
				return -1;
			} else if(o1.value != null && o2.value == null) {
				return 1;
			} else {
				return o1.value.compareTo(o2.value);
			}
		}
	};

	public SortableTrieTreeNode(Comparable value) {
		this.value = value;
	}

	public SortableTrieTreeNode getChild(Comparable word) {
		for (SortableTrieTreeNode child : childs) {
			if(child.value == null || word == null) {
				if(child.value == null && word == null) {
					return child;
				} else {
					continue;
				}
			}
			if (child.value.equals(word)) {
				return child;
			}
		}
		return null;
	}

	public SortableTrieTreeNode addChild(Comparable word) {
		SortableTrieTreeNode child = new SortableTrieTreeNode(word);
		childs.add(child);
		child.setLayer(layer + 1);
		return child;
	}

	public List<SortableTrieTreeNode> getChilds() {
		return childs;
	}

	public Comparable getValue() {
		return value;
	}

	public void sort() {
		Collections.sort(childs, comparator);
	}

	@Override
	public int compareTo(SortableTrieTreeNode o) {
		return value.compareTo(o.value);
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public boolean isLeaf() {
		return leaf;
	}

	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}

	public void addData(TableViewData data) {
		if(data == null)
			return;
		if(datas == null) {
			datas = new ArrayList<>();
		}
		datas.add(data);
	}

	public List<TableViewData> getDatas() {
		return datas;
	}

	public Comparator<SortableTrieTreeNode> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<SortableTrieTreeNode> comparator) {
		this.comparator = comparator;
	}

}
