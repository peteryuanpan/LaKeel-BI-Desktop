package com.legendapl.lightning.service;

import java.io.IOException;
import java.util.List;

import com.legendapl.lightning.model.ObservableDataSource;

/**
 * データソースのロードと保存
 * 
 * @author taka
 *
 */
public interface DataSourceService {
	public	List<ObservableDataSource> getDataSources() throws IOException;
	public void saveDataSources(List<ObservableDataSource> dataSources);
}
