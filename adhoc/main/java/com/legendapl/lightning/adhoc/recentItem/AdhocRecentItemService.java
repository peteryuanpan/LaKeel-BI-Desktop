package com.legendapl.lightning.adhoc.recentItem;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.controller.C100AdhocMenuPane;
import com.legendapl.lightning.adhoc.controller.P102LocalDataAnchorPane;
import com.legendapl.lightning.adhoc.model.LoadDataRow;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.BackRunService;
import com.legendapl.lightning.tools.data.AdhocData;

public class AdhocRecentItemService extends C100AdhocMenuPane {
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO
	}
	
	public static LoadDataRow clickedDataRow;
	
	/**
	 * 該当のアドホックファイルが「最近表示アイテム」に追加できます
	 * @param uri
	 * @return
	 */
	public static boolean canAdd(AdhocRecentItem item) {
		if (null == item) return false;
		if (!fileExist(item.getReportURI())) return false;
		return true;
	}
	
	/**
	 * 該当のアドホックファイルを開く<br>
	 * トピックからまたはアドホックから...
	 * @param boxCell
	 */
	public void run(AdhocHBoxCell boxCell) {
		try {
			BackRunService back = new BackRunService();
			back.run(() -> {
				// データをクリアー
				String paneFXML = "/view/P102LocalDataAnchorPane.fxml";
				AdhocData.roots.remove(paneFXML);
				
				// データをセット
				AdhocRecentItem recentItem = boxCell.getAdhocRecentItem();
				clickedDataRow =  new LoadDataRow();
				clickedDataRow.setFileType(P102LocalDataAnchorPane.getBundleStringByType(recentItem.getAdhocFromType()));
				clickedDataRow.setFileName(recentItem.getReportLabel());
				clickedDataRow.setUri(recentItem.getReportURI());
				P102LocalDataAnchorPane.runFunAfterLoading = true;
				
				//　ローカル画面を移動
				//　アドホックビュー編集画面を開く
				super.localField(null);
			});
			
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_READ_TITLE"), e.getMessage());
		}
	}
	
	private static boolean fileExist(String uri) {
		File file = new File(uri);
		return null != file && file.exists();
	}
	
}
