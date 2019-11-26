package test;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkin;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.text.Font;

@SuppressWarnings("restriction")
public class CustomerSkin<T> extends TableViewSkin<T> {

	static final String DEFER_TO_PARENT_PREF_WIDTH = "deferToParentPrefWidth";
	final TableView<T> tableView;

	public CustomerSkin(TableView<T> tableView) {
		super(tableView);
		this.tableView = tableView;
	}

	@Override
	protected TableHeaderRow createTableHeaderRow() {
		return CustomerTableHeaderRow.getInstance(this);
	}

	@SuppressWarnings("deprecation")
	public void customResize(TableColumn<T, ?> tc, int maxRows) {
		double initWidth = tc.getWidth();
		double textwidth = AdhocUtils.computeTextWidth(new Font("Meiryo Bold", 13), tc.getText(), -1);
		double toCompareWidth = Math.max(initWidth, textwidth);
		resizeColumnToFitContent(tc, maxRows);
		tc.impl_setWidth(Math.max(toCompareWidth, tc.getWidth()));
	}

}
