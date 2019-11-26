package test;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;

import javafx.scene.control.Label;

public class CustomerTableHeaderRow extends TableHeaderRow {
	private static TableViewSkinBase skinView;
	public CustomerTableHeaderRow(TableViewSkinBase skin) {
		super(skin);
	}

	public static CustomerTableHeaderRow getInstance(TableViewSkinBase skin) {
		skinView = skin;
		return new CustomerTableHeaderRow(skin);
	}

	@Override
	protected NestedTableColumnHeader createRootHeader() {
		return new CustomerNestedTableColumnHeader(skinView, null);
	}


}
