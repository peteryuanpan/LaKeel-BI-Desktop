/**
 * 
 */
package com.legendapl.lightning.model;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;

/**
 * [ver1.0] JasperReportsのAPIを用いて取得したリソース情報を格納するクラス
 * 親クラスの値(Label,description,uri,resourceType,version,permissionMask,creationDate,updateDate,thumnailData)に加えて
 * 表示用の情報を追加する
 * 
 * [ver2.0] リソースのフルパスもフィールドとして保持するよう拡張
 * 
 * @author Legend Applications, LaKeel BI development team.
 *
 */
public class FolderResource extends ClientResourceLookup {

	private static final long serialVersionUID = 1L;
	private String path;

	public FolderResource() {
		super();
	}

	public FolderResource(String label) {
		this.setLabel(label);
	}

	public FolderResource(String label, String path) {
		this.setLabel(label);
		this.setPath(path);
	}

	public FolderResource(ClientResourceLookup resource) {
		this.setLabel(resource.getLabel());
		this.setDescription(resource.getDescription());
		this.setUri(resource.getUri());
		this.setResourceType(resource.getResourceType());
		this.setVersion(resource.getVersion());
		this.setPermissionMask(resource.getPermissionMask());
		this.setCreationDate(resource.getCreationDate());
		this.setUpdateDate(resource.getUpdateDate());
		this.setThumbnailData(resource.getThumbnailData());
	}

	/*
	 * TreeViewに表示するためToStringメソッドをラベルのみに変更
	 */
	@Override
	public String toString() {
		return getLabel();
	}

	public String superToString() {
		return super.toString();
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
