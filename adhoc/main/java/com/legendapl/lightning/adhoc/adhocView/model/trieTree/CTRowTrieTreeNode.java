package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.List;
import java.util.Map;

import com.legendapl.lightning.adhoc.adhocView.model.CalculatedNumber;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;

/**
 * クロス集計の行のデータを格納する辞書ツリーノード
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */

@SuppressWarnings("rawtypes")
public class CTRowTrieTreeNode extends TrieTreeNode {
	
	private CTViewTrieTree tree = new CTViewTrieTree();

	public CTRowTrieTreeNode(Comparable name) {
		super(name);
	}

	public CTRowTrieTreeNode() {
		super();
	}

	@Override
	protected TrieTreeNode getInstance(Comparable name) {
		return new CTRowTrieTreeNode(name);
	}

	@Override
	public void generateTreeTotal() {
		tree.generateTreeTotal();
	}

	@Override
	public void merge(List<Comparable> columns, Map<CrossTableField, CalculatedNumber> measureMap) {
		tree.insert(columns, null, measureMap);
	}

	@Override
	public void combineData(TrieTreeNode root) {
		this.tree = ((CTRowTrieTreeNode) root).tree;
	}

	public CTViewTrieTree getTree() {
		return tree;
	}

	public void setTree(CTViewTrieTree tree) {
		this.tree = tree;
	}

}
