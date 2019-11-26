package com.legendapl.lightning.tools.model;

import java.io.Serializable;

import javafx.beans.property.SimpleStringProperty;


/**
 * フラグを含めのSimpleStringProperty
 *
 * @author LAC_徐
 * @since 2017/9/11
 *
 */
@SuppressWarnings("serial")
public class StringBase extends SimpleStringProperty implements Serializable {
	private ProcessFlag flag = ProcessFlag.NONE;

	public StringBase(String str) {
		super(str);
	}

	public ProcessFlag getFlag() {
		return flag;
	}

	public void setFlag(ProcessFlag flag) {
		this.flag = flag;
	}
}
