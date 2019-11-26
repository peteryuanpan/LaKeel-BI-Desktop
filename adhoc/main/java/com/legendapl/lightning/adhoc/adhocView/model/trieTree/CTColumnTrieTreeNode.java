package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.custom.CTCustomColumn;

/**
 * クロス集計の列のデータを格納する辞書ツリーノード
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */

@SuppressWarnings("rawtypes")
public class CTColumnTrieTreeNode extends TrieTreeNode{

	public CTColumnTrieTreeNode(Comparable name) {
		super(name);
		defaultExpand = false;
		expand = defaultExpand;
	}

	public CTColumnTrieTreeNode() {
		super();
		defaultExpand = false;
		expand = defaultExpand;
	}

	@Override
	protected TrieTreeNode getInstance(Comparable name) {
		return new CTColumnTrieTreeNode(name);
	}

	@Override
	protected TrieTreeNode addChild(Comparable name) {
		TrieTreeNode child = getInstance(name);
		child.layer = layer + 1;
		child.isTotal = isTotal;
		childs.add(child);
		return child;
	}

	@SuppressWarnings("unchecked")
	public void generateChildColumns(CTCustomColumn<?, ?> tableCustomColumn) {
		List list = new ArrayList<>();
		for(int i=0;i<childs.size()-1;i++) {
			TrieTreeNode child = childs.get(i);
			list.add(((CTColumnTrieTree)tree).transferToTableView(child, tableCustomColumn.getTrack().getFieldNames(),
					tableCustomColumn.getTrack().getMeasure(), tableCustomColumn.getRoot().isTotal()));
		}
		list.add(tableCustomColumn.getTotalChild());
		tableCustomColumn.setChilds(list);
	}

}
