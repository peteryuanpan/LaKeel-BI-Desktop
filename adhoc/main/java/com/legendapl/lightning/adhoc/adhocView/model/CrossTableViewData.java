package com.legendapl.lightning.adhoc.adhocView.model;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTViewTrieTree;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTRowTrieTree;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTRowTrieTreeNode;

/**
 * クロス集計の行のデータ
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.03.22
 */
public class CrossTableViewData {

	/**
	 * CTRowLabelColumn　の値を格納する
	 */
	private List<Object> fields = new ArrayList<>();
	/**
	 * CTRowDataTrieTree　のトラック
	 */
	private List<Object> searchList = new ArrayList<>();
	/**
	 * CrossTableCustomColumn　の値を格納する
	 */
	private CTViewTrieTree tree;
	/**
	 *　メジャーは行にある場所
	 */
	private CrossTableField measure;

	private List<CTRowTrieTreeNode> nodes = new ArrayList<>();

	private boolean isTotal;
	private int measureLayer = -1;

	public boolean isTotal() {
		return isTotal;
	}
	public void setTotal(boolean isTotal) {
		this.isTotal = isTotal;
	}
	public List<Object> getFields() {
		return fields;
	}
	public void setFields(List<Object> fields) {
		this.fields = fields;
	}

	public CTViewTrieTree getTree() {
		return tree;
	}
	public void setTree(CTViewTrieTree tree) {
		this.tree = tree;
	}

	@Override
	public String toString() {
		return fields.toString() + "\n";
	}
	public CrossTableField getMeasure() {
		return measure;
	}
	public void setMeasure(CrossTableField measure) {
		this.measure = measure;
	}
	public String getColumnCellValue(ColumnSearchTrack columnSearchTrack) {
		if(measure != null)
			return tree.getColumnCellValue(columnSearchTrack, measure);
		else {
			return tree.getColumnCellValue(columnSearchTrack);
		}
	}

	public Object getRowCellValue(int rowNum) {

		return fields.get(rowNum);
	}

	/**
	 * CTRowDataTrieTree　のトラックを生成する
	 */
	public List<Object> getSearchList(int size) {
		if(searchList.size() < size) {
			for(int i=searchList.size();i<size;i++) {
				if(i == measureLayer - 1) {
					searchList.add(measure);
				} else {
					searchList.add(fields.get(i));
				}
			}
			return new ArrayList<>(searchList);
		} else {
			return new ArrayList<>(searchList.subList(0, size));
		}
	}

	public int getMeasureLayer() {
		return measureLayer;
	}
	public void setMeasureLayer(int measureLayer) {
		this.measureLayer = measureLayer;
	}

	public CTRowTrieTreeNode getNode(int index, CTRowTrieTree tree) {
		if(nodes.size() < index + 1) {
			for(int i=nodes.size();i<index + 1;i++) {
				if(tree.search(getSearchList(i + 1)) == null) {
					System.out.println(getSearchList(i + 1));
				}
				nodes.add(tree.search(getSearchList(i + 1)));
			}
		}
		return nodes.get(index);
	}

}
