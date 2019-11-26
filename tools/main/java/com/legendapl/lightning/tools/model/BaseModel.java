package com.legendapl.lightning.tools.model;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;


/**
 * 共通モデル
 *
 * @author LAC_徐
 * @since 2017/9/15
 */
public abstract class BaseModel<T> extends RecursiveTreeObject<T> {

	protected ProcessFlag flag = ProcessFlag.NONE;

	/**
	 * フラグ取得
	 * @return フラグ
	 */
	public ProcessFlag getFlag() {
		return flag;
	}

	/**
	 * フラグ設定
	 * @param 新フラグ
	 */
	public void setFlag(ProcessFlag flag) {
		this.flag = flag;
	}
}
