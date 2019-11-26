package com.legendapl.lightning.adhoc.custom;

import com.legendapl.lightning.adhoc.adhocView.model.TableField;

import javafx.scene.control.TableColumn;

/**
 *　テーブルのTableColumn
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class TableCustomColumn<S, T> extends TableColumn<S, T> {

	private TableField field;

	public TableCustomColumn() {
		super();
		setSortable(false);
	}

	public TableCustomColumn(String text) {
		super(text);
		setSortable(false);
	}
	
	public TableField getField() {
		return field;
	}

	public void setField(TableField field) {
		this.field = field;
	}

}
