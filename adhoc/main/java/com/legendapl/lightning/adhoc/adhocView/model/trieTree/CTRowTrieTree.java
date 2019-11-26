package com.legendapl.lightning.adhoc.adhocView.model.trieTree;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableViewData;
import com.legendapl.lightning.adhoc.service.ThreadPoolService;

/**
 * クロス集計の行のデータを格納する辞書ツリー
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018.04.12
 */

@SuppressWarnings("rawtypes")
public class CTRowTrieTree extends TrieTree<CTRowTrieTreeNode> {

    private String sqlStatement = "";

    public CTRowTrieTree() {
        super();
    }

    /**
     * 辞書ツリーをたどって、CrossTableViewDataを生成する
     */
	public List<CrossTableViewData> transferToModels() {
		if(root.getChilds().isEmpty()) {
			List<CrossTableViewData> models = new ArrayList<>();
			models.addAll(getModels(root, new ArrayList<Object>(), null));
			return models;
		} else {
			return transferToModels(-1, false);
		}
	}

	/**
     * 辞書ツリーのレイヤーを拡大または縮小してから、辞書ツリーをたどって、CrossTableViewDataを生成する
     */
	public List<CrossTableViewData> transferToModels(int expandLayer, boolean expand) {
		List<CrossTableViewData> models = new ArrayList<>();
		ColumnFilledSignal<List<CrossTableViewData>> signal = new ColumnFilledSignal<>(root.childs.size());
		for(int i=0;i<root.childs.size();i++) {
			final int tempI = i;
			ThreadPoolService.getInstance().execute(new Processor<List<CrossTableViewData>>(
					() -> getModelsAfterNotify((CTRowTrieTreeNode) root.childs.get(tempI), new ArrayList<Object>(), null, expandLayer, expand)
					, signal, tempI));
		}
		List<List<CrossTableViewData>> splitModels = signal.check();
		splitModels.forEach(list -> models.addAll(list));
		return models;
	}

	/**
     * 指定された辞書ツリーノードをたどって、CrossTableViewDataを生成する
     */
	public List<CrossTableViewData> getModels(CTRowTrieTreeNode root, List<Object> fields, CrossTableField measure) {
		List<Object> currentFields = new ArrayList<>();
		currentFields.addAll(fields);
		currentFields.add(root.getFieldValue());
		if(root.getLayer() == measureLayer) {
			measure = root.getMeasure();
		}

		List<CrossTableViewData> models = new ArrayList<CrossTableViewData>();
		if(root.isLeaf()) {
			CrossTableViewData model = new CrossTableViewData();
			model.setTotal(root.isTotal());
			model.setFields(currentFields);
			model.setTree(root.getTree());
			model.setMeasure(measure);
			model.setMeasureLayer(measureLayer);
			models.add(model);
		} else if(root.isExpand(measure) || root.getTotalNode() == null){
			for(int i=0;i<root.getChilds().size();i++) {
				CTRowTrieTreeNode node = (CTRowTrieTreeNode)root.getChilds().get(i);
				models.addAll(getModels(node, currentFields, measure));
			}
		} else {
			models.addAll(getModels((CTRowTrieTreeNode)root.getTotalNode(), currentFields, measure));
		}
		return models;
	}

	public String getSqlStatement() {
		return sqlStatement;
	}

	public void setSqlStatement(String sqlStatement) {
		this.sqlStatement = sqlStatement;
	}

	@Override
	protected CTRowTrieTreeNode getInstance() {
		return new CTRowTrieTreeNode();
	}

	@Override
	protected CTRowTrieTreeNode getInstance(Comparable name) {
		return new CTRowTrieTreeNode(name);
	}

	public List<CrossTableViewData> getModelsAfterNotify(CTRowTrieTreeNode root, List<Object> fields, CrossTableField measure, int expandLayer, boolean expand) {
		if(expandLayer != -1) {
			notify(root, null, expandLayer, expand);
		}
		return getModels(root, fields, measure);
	}

}
