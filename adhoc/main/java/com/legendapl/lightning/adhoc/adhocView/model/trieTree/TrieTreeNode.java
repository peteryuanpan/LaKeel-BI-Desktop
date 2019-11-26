package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.CalculatedNumber;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;

/**
 * 共通の辞書ツリーノード
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class TrieTreeNode implements Comparable<TrieTreeNode> {

	protected List<TrieTreeNode> childs = new ArrayList<TrieTreeNode>();

	protected boolean isLeaf;

	protected Comparable fieldValue;

	protected int layer;

	protected CrossTableField measure;

	protected boolean isTotal;

	protected TrieTreeNode totalNode;

	protected boolean defaultExpand = true;

	protected boolean expand = defaultExpand;

	protected boolean icon = true;

	protected Set<CrossTableField> measureExpand = new HashSet<>();

	protected Logger logger = Logger.getLogger(getClass());

	protected TrieTree<? extends TrieTreeNode> tree;

	protected Comparator<TrieTreeNode> comparator = new Comparator<TrieTreeNode>() {
		@Override
		public int compare(TrieTreeNode o1, TrieTreeNode o2) {
			if(o1.fieldValue == null && o2.fieldValue == null) {
				return 0;
			} else if(o1.fieldValue == null && o2.fieldValue != null) {
				return -1;
			} else if(o1.fieldValue != null && o2.fieldValue == null) {
				return 1;
			} else {
				return o1.fieldValue.compareTo(o2.fieldValue);
			}
		}
	};

	public TrieTreeNode(Comparable word) {
		fieldValue = word;
	}

	public TrieTreeNode() {

	}

	public final TrieTreeNode getChild(Comparable childValue) {
		for (TrieTreeNode child : childs) {
			if(child.fieldValue == null || childValue == null) {
				if(child.fieldValue == null && childValue == null) {
					return child;
				} else {
					continue;
				}
			}
			if (child.fieldValue.equals(childValue)) {
				return child;
			}
		}
		return null;
	}

	protected TrieTreeNode addChild(Comparable name) {
		TrieTreeNode child = getInstance(name);
		child.layer = layer + 1;
		childs.add(child);
		return child;
	}

	protected abstract TrieTreeNode getInstance(Comparable name);

	public final List<TrieTreeNode> getChilds() {
		return childs;
	}

	public final void setChilds(List<TrieTreeNode> childs) {
		this.childs = childs;
	}

	public final boolean isLeaf() {
		return isLeaf;
	}

	public final void setLeaf(boolean isLeaf) {
		this.isLeaf = isLeaf;
	}

	public final boolean isTotal() {
		return isTotal;
	}

	public final void setTotal(boolean isTotal) {
		this.isTotal = isTotal;
	}

	public final TrieTreeNode getTotalNode() {
		return totalNode;
	}

	public final void setTotalNode(TrieTreeNode totalNode) {
		this.totalNode = totalNode;
	}

	public final Comparable getFieldValue() {
		return fieldValue;
	}

	public final void setFieldValue(Comparable fieldValue) {
		this.fieldValue = fieldValue;
	}

	public final int getLayer() {
		return layer;
	}

	public final void setLayer(int layer) {
		this.layer = layer;
	}

	public final CrossTableField getMeasure() {
		return measure;
	}

	public final void setMeasure(CrossTableField measure) {
		this.measure = measure;
	}

	public TrieTreeNode search(Object word) {
		for (TrieTreeNode child : childs) {
			// null
			if (word == null) {
				if(child.fieldValue == null) {
					return child;
				} else {
					continue;
				}
			}
			// CrossTableField
			if (word instanceof CrossTableField && child.measure == word) {
				return child;
			}
			// TotalNode
			if (word instanceof TotalNode && child.fieldValue instanceof TotalNode) {
				return child;
			}
			// Others
			if (word.equals(child.fieldValue)) {
				return child;
			}
		}
		return null;
	}

	public final boolean hasIcon() {
		return totalNode != null && icon;
	}

	public final boolean isExpand(CrossTableField measure) {
		return measure == null ? expand : measureExpand.contains(measure) ? !defaultExpand : defaultExpand;
	}

	public final void setExpand(CrossTableField measure, boolean expand) {
		if (measure == null)
			this.expand = expand;
		else if (expand == defaultExpand) {
			measureExpand.remove(measure);
		} else {
			measureExpand.add(measure);
		}
	}

	public final TrieTreeNode addTotalChild(boolean icon) {
		TrieTreeNode child = getInstance(new TotalNode());
		child.isTotal = true;
		child.layer = layer + 1;
		childs.add(child);
		totalNode = child;
		this.icon = icon;
		return child;
	}

	//フック
	public void generateTreeTotal() {

	}
	//フック
	public void merge(List<Comparable> columns, Map<CrossTableField, CalculatedNumber> map) {

	}
	//フック
	public void combineData(TrieTreeNode root) {

	}

	public final void setIcon(boolean icon) {
		this.icon = icon;
	}

	public final boolean isIcon() {
		return icon;
	}

	public final void setTree(TrieTree<? extends TrieTreeNode> tree) {
		this.tree = tree;
	}

	public void sort() {
		Collections.sort(childs, comparator);
	}

	@Override
	public int compareTo(TrieTreeNode o) {
		return fieldValue.compareTo(o.fieldValue);
	}

	public Comparator<TrieTreeNode> getComparator() {
		return comparator;
	}

	public void setComparator(Comparator<TrieTreeNode> comparator) {
		this.comparator = comparator;
	}

}
