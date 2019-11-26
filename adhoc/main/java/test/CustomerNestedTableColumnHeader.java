package test;

import com.sun.javafx.scene.control.skin.NestedTableColumnHeader;
import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;

public class CustomerNestedTableColumnHeader extends NestedTableColumnHeader{

	private static final double DEFAULT_COLUMN_WIDTH = 80.0F;
	private boolean autoSizeComplete;
	private TableViewSkinBase skin;

	public CustomerNestedTableColumnHeader(TableViewSkinBase skin, TableColumnBase tc) {
		super(skin, tc);
		this.skin = skin;
	}

    // protected to allow subclasses to customise the column header types
    protected TableColumnHeader createTableColumnHeader(TableColumnBase col) {
        return col.getColumns().isEmpty() ?
                new CustomTableColumnHeader(getTableViewSkin(), col) :
                new CustomerNestedTableColumnHeader(getTableViewSkin(), col);
    }

    @Override
    protected void handlePropertyChanged(String p) {
        if ("SCENE".equals(p) && getTableColumn() != null && getTableColumn().getWidth() != DEFAULT_COLUMN_WIDTH && getTableColumn().getColumns().isEmpty()) {
            updateScene();
        } else {
        	super.handlePropertyChanged(p);
        }
    }

	private void updateScene() {
		final int n = 30;
        if (! autoSizeComplete) {
            ((CustomerSkin)skin).customResize((TableColumn<?, ?>) getTableColumn(), n);
            autoSizeComplete = true;
        }
	}

}
