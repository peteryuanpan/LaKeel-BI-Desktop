package com.legendapl.lightning.model;

import javafx.beans.value.ObservableValue;

/**
 * 画面用拡張データソース情報I/F
 * 
 * @author taka
 *
 */
public interface ObservableDataSource extends DataSource {

    // 画面に表示されるが、XMLにない項目
	public ObservableValue<Boolean>	selectedProperty();
    public ObservableValue<String> dataSourcePathProperty();

    // XMLに在って画面にも表示されるデータ項目
    public ObservableValue<String> nameProperty();
    public ObservableValue<String> typeProperty();
    public ObservableValue<String> serverAddressProperty();
    public ObservableValue<String> portProperty();
    public ObservableValue<String> schemaProperty();
    public ObservableValue<String> usernameProperty();
    //    public ObservableValue<String> passwordProperty();	// 標準なTableView編集機能では、ENTERキーが必要のため、Propertyから外した。
    public ObservableValue<Status> statusProperty();
}
