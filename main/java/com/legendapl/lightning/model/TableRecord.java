package com.legendapl.lightning.model;

import javax.xml.bind.annotation.XmlElement;

import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject;

import javafx.beans.property.SimpleStringProperty;

/**
 * テーブルビューの1行分のデータとなるエンティティクラス
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class TableRecord extends RecursiveTreeObject<TableRecord> {

	private final SimpleStringProperty name = new SimpleStringProperty("");
	private final SimpleStringProperty type = new SimpleStringProperty("");
	private final SimpleStringProperty description = new SimpleStringProperty("");
	private final SimpleStringProperty createDate = new SimpleStringProperty("");
	private final SimpleStringProperty updateDate = new SimpleStringProperty("");
	private final SimpleStringProperty uri = new SimpleStringProperty("");

	public TableRecord() {
		super();
	}

	public TableRecord(String name, String type, String description, String createDate, String updateDate, String uri) {
		setName(name);
		setType(type);
		setDescription(description);

		setCreateDate(dateFormat(createDate));
		setUpdateDate(dateFormat(updateDate));

		setUri(uri);
	}

	/**
	 * 日付のフォーマットを置換するメソッド
	 * 
	 * @param date
	 */
	public String dateFormat(String date) {
		String str1 = date.replace("T", " ");
		String str2 = str1.replace("-", "/");
		return str2;

	}

	@XmlElement(name = "name")
	public SimpleStringProperty getName() {
		return this.name;
	}

	public void setName(String nameStr) {
		name.set(nameStr);
	}

	@XmlElement(name = "type")
	public SimpleStringProperty getType() {
		return this.type;
	}

	public void setType(String typeStr) {
		type.set(typeStr);
	}

	@XmlElement(name = "createDate")
	public SimpleStringProperty getCreateDate() {
		return this.createDate;
	}

	public void setCreateDate(String createDateStr) {
		createDate.set(createDateStr);
	}

	@XmlElement(name = "updateDate")
	public SimpleStringProperty getUpdateDate() {
		return this.updateDate;
	}

	public void setUpdateDate(String updateDateStr) {
		updateDate.set(updateDateStr);
	}

	@XmlElement(name = "uri")
	public SimpleStringProperty getUri() {
		return this.uri;
	}

	public void setUri(String uriStr) {
		uri.set(uriStr);
	}

	@XmlElement(name = "description")
	public SimpleStringProperty getDescription() {
		return description;
	}

	public void setDescription(String descriptionStr) {
		description.set(descriptionStr);
	}

}
