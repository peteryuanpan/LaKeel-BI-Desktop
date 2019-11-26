package com.legendapl.lightning.adhoc.model;

import com.legendapl.lightning.tools.model.BaseModel;
import com.legendapl.lightning.tools.model.StringBase;

/**
 * データを読み込む画面の中に　ツリーテーブルカラムのデータベース
 * 
 * @author Legend Applications China, LaKeel BI development team.
 * @author panyuan
 * @since 2018.02.24
 *
 */
public class LoadDataRow extends BaseModel<LoadDataRow> implements Comparable<LoadDataRow> {

	private StringBase fileName = new StringBase("");
	private StringBase description = new StringBase("");
	private StringBase fileType = new StringBase("");
	private StringBase createTime = new StringBase("");
	private StringBase updateTime = new StringBase("");
	private String uri = new String();
	
	public LoadDataRow() {
		super();
	}
	
	public LoadDataRow(String fileName, String description, String fileType, String createTime, String updateTime, String uri) {
		setFileName(fileName);
		setDescription(description);
		setFileType(fileType);
		setCreateTime(createTime);
		setUpdateTime(updateTime);
		setUri(uri);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LoadDataRow) {
			return fileName.get().equals( ((LoadDataRow)obj).getFileName().get() );
		} else {
			return false;
		}
	}
	
	@Override
	public int compareTo(LoadDataRow column) {
		return fileName.get().compareTo( column.getFileName().get() );
	}
	
	public LoadDataRow setFileName(String str) {
		fileName.set(str);
		return this;
	}
	
	public StringBase getFileName() {
		return fileName;
	}
	
	public LoadDataRow setDescription(String str) {
		description.set(str);
		return this;
	}
	
	public StringBase getDescription() {
		return description;
	}
	
	public LoadDataRow setFileType(String str) {
		fileType.set(str);
		return this;
	}
	
	public StringBase getFileType() {
		return fileType;
	}
	
	public LoadDataRow setCreateTime(String str) {
		createTime.set(str);
		return this;
	}
	
	public StringBase getCreateTime() {
		return createTime;
	}
	
	public LoadDataRow setUpdateTime(String str) {
		updateTime.set(str);
		return this;
	}
	
	public StringBase getUpdateTime() {
		return updateTime;
	}
	
	public LoadDataRow setUri(String uri) {
		this.uri = uri;
		return this;
	}
	
	public String getUri() {
		return uri;
	}
}
