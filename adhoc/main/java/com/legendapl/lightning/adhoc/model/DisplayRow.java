package com.legendapl.lightning.adhoc.model;

import com.legendapl.lightning.tools.model.BaseModel;
import com.legendapl.lightning.tools.model.StringBase;

/**
 * トピック表示画面のツリーテーブルカラムのデータを格納します。
 * 
 * @author Legend Applications China, LaKeel BI development team.
 * @author panyuan
 * @since 2018.03.21
 *
 */
public class DisplayRow extends BaseModel<DisplayRow>  {
	
	private BaseNode baseNode;
	private StringBase srLabel = new StringBase("");
	private StringBase tgLabel = new StringBase("");
	
	public DisplayRow() {
		baseNode = null;
		srLabel = new StringBase("");
		tgLabel = new StringBase("");
	}
	
	public DisplayRow(BaseNode baseNode, StringBase srLabel, StringBase tgLabel) {
		super();
		this.baseNode = baseNode;
		this.srLabel = srLabel;
		this.tgLabel = tgLabel;
	}
	
	public BaseNode getBaseNode() {
		return baseNode;
	}

	public void setBaseNode(BaseNode baseNode) {
		this.baseNode = baseNode;
	}

	public StringBase getSrLabel() {
		return srLabel;
	}
	
	public void setSrLabel(String srLabel) {
		this.srLabel.set(srLabel);
	}

	public StringBase getTgLabel() {
		return tgLabel;
	}
	
	public void setTgLabel(String tgLabel) {
		this.tgLabel.set(tgLabel);
	}

}
