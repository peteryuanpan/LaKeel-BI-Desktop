package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.legendapl.lightning.adhoc.adhocView.model.CalculatedNumber;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.dao.util.SQLUtils;

import javafx.concurrent.Task;

/**
 * 共通の辞書ツリー
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class TrieTree<T extends TrieTreeNode> {

	protected T root;

	protected int depth;

	protected int measureLayer = -1;

	public TrieTree() {
		root = getInstance();
		root.setLayer(0);
	}

	protected abstract T getInstance();
	protected abstract T getInstance(Comparable name);

	/**
	 * 辞書ツリー
	 * @param words　　　行のデータ
	 * @param columns　列のデータ
	 * @param map　　　　　メジャーのデータ
	 */
	public final void insert(List<Comparable> words, List<Comparable> columns, Map<CrossTableField, CalculatedNumber> map) {
		depth = words.size();
		root.setLeaf(words.size() == 0);
		insert(this.root, words, columns, map);
	}

	/**
	 * 辞書ツリー
	 * @param root　　　ツリーのノード
	 * @param words　　　行のデータ
	 * @param columns　列のデータ
	 * @param map　　　　　メジャーのデータ
	 */
	private void insert(TrieTreeNode root, List<Comparable> words, List<Comparable> columns, Map<CrossTableField, CalculatedNumber> map) {
		root.merge(columns, map);
		for (int i = 0; i < words.size(); i++) {
			Comparable word = words.get(i);
			TrieTreeNode child = root.getChild(word);
			if (child == null) {
				child = root.addChild(word);
				child.setTree(this);
				child.setLeaf(child.getLayer() == depth);
			}
			child.merge(columns, map);
			root = child;
		}
	}

	public final T search(List<Object> words) {
		return search(root, words);
	}

	private T search(T root, List<Object> words) {
		for (Object word: words) {
    		root = (T) root.search(word);
    		if(root == null) {
    			return null;
    		}
    	}
    	return root;
	}

	public final void generateTreeTotal() {
		generateTreeTotal(root);
	}

	private void generateTreeTotal(TrieTreeNode root) {
		root.generateTreeTotal();
		if (root.isLeaf()) return;
		if (!(root instanceof CTColumnTrieTreeNode))
			root.sort();
		for (TrieTreeNode child : root.getChilds()) {
			generateTreeTotal(child);
		}
		generateTotalTrieTreeNode(root, true);
	}

	private void generateTotalTrieTreeNode(TrieTreeNode root, boolean icon) {
		if (root.getLayer() != depth) {
			TrieTreeNode totalTrieTreeNode = root.addTotalChild(icon);
			totalTrieTreeNode.setTree(this);
			totalTrieTreeNode.setLeaf(totalTrieTreeNode.getLayer() == depth);
			totalTrieTreeNode.combineData(root);
			generateTotalTrieTreeNode(totalTrieTreeNode, false);
		} else {
			root.setLeaf(true);
		}
	}

	/**
	 * get all nodes of which depth is equal [layer] from super root of trie tree
	 * @param layer
	 * @return
	 */
	public final List<TrieTreeNode> getLayerTrieTreeNodes(int layer) {
		if (layer > depth) return null;
		List<TrieTreeNode> TrieTreeNodes = new LinkedList<TrieTreeNode>();
		TrieTreeNodes.addAll(getLayerTrieTreeNodes(root, layer));
		return TrieTreeNodes;
	}

	/**
	 * get all nodes of which depth is equal [layer] from [root] of trie tree
	 * @param root
	 * @param layer
	 * @return
	 */
	private List<TrieTreeNode> getLayerTrieTreeNodes(TrieTreeNode root, int layer) {
		List<TrieTreeNode> TrieTreeNodes = new LinkedList<TrieTreeNode>();
		if (root.getLayer() == layer) {
			TrieTreeNodes.add(root);
		} else {
			for (TrieTreeNode child : root.getChilds()) {
				TrieTreeNodes.addAll(getLayerTrieTreeNodes(child, layer));
			}
		}
		return TrieTreeNodes;
	}

	/**
	 * add each measure to its father
	 * @param layer
	 * @param values
	 */
	public final void addMeasure(int layer, List<CrossTableField> values) {
		depth++;
		measureLayer = layer;
		List<TrieTreeNode> trieTreeNodes = getLayerTrieTreeNodes(layer - 1);
		for (TrieTreeNode trieTreeNode : trieTreeNodes) {
			List<TrieTreeNode> measureList = new LinkedList<TrieTreeNode>();
			for (CrossTableField measureField : values) {
				TrieTreeNode measureTrieTreeNode = getInstance(measureField.getLabel());
				measureTrieTreeNode.setChilds(trieTreeNode.getChilds());
				measureTrieTreeNode.setTotalNode(trieTreeNode.getTotalNode());
				measureTrieTreeNode.setTotal(trieTreeNode.isTotal);
				measureTrieTreeNode.combineData(trieTreeNode);
				measureTrieTreeNode.setLeaf(trieTreeNode.isLeaf());
				measureTrieTreeNode.setLayer(trieTreeNode.getLayer() + 1);
				measureTrieTreeNode.setMeasure(SQLUtils.uniqueMeasure(measureField));
				measureTrieTreeNode.setIcon(trieTreeNode.isIcon());
				measureTrieTreeNode.setTree(this);
				measureList.add(measureTrieTreeNode);
			}
			trieTreeNode.setTotalNode(null);
			trieTreeNode.setChilds(measureList);
			adapte(trieTreeNode);
		}
	}

	private void adapte(TrieTreeNode root) {
		if (root.getLayer() == depth) {
			root.setLeaf(true);
		} else {
			root.setLeaf(false);
			for (int i = 0; i < root.getChilds().size(); i++) {
				TrieTreeNode child = root.getChilds().get(i);
				child.setLayer(root.getLayer() + 1);
				adapte(child);
			}
		}
	}

	public final int getDepth() {
		return depth;
	}

	public final void setDepth(int depth) {
		this.depth = depth;
	}

	public final int getMeasureLayer() {
		return measureLayer;
	}

	public final void setMeasureLayer(int measureLayer) {
		this.measureLayer = measureLayer;
	}

	protected final void notify(TrieTreeNode root, CrossTableField measure, int expandLayer, boolean expand) {
		if(root.getLayer() == measureLayer) {
			measure = root.getMeasure();
		}
		if(expand && root.getLayer() <= expandLayer) {
			root.setExpand(measure, expand);
		} else if(!expand && root.getLayer() >= expandLayer) {
			root.setExpand(measure, expand);
		} else if(expand) {
			return;
		}
		for(TrieTreeNode node: root.getChilds()) {
			notify(node, measure, expandLayer, expand);
		}
	}

}

/**
 * 並行プログラミングによるシーケンスの問題を解決するため
 */
interface VoidRunFun<T> {
	T call();
}

class Processor<T> extends Task<Void> {
	private VoidRunFun<T> task;
	private ColumnFilledSignal<T> signal;
	private int index;

	public Processor(VoidRunFun<T> task, ColumnFilledSignal<T> signal, int index) {
		this.task = task;
		this.signal = signal;
		this.index = index;
	}

	@Override
	protected Void call() throws Exception {
		signal.add(task.call(), index);
		return null;
	}
}

class ColumnFilledSignal<T> {
	private int size;
	private int currentSize = 0;
	private List<T> columns;

	ColumnFilledSignal(int size) {
		this.size = size;
		columns = new ArrayList<>();
		IntStream.range(0, size).boxed().collect(Collectors.toList()).forEach(p -> columns.add(null));
	}

	synchronized List<T> check() {
		if (currentSize < size)
			try {
				wait();
			} catch (InterruptedException e) {
				System.out.println("InterruptedException caught");
			}
		return columns;
	}

	synchronized void add(T tableColumn, int index) {
		columns.set(index, tableColumn);
		if(++currentSize == size) {
			notify();
		}
	}
}
