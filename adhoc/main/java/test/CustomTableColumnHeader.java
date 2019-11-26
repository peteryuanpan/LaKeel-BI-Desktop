package test;

import com.sun.javafx.scene.control.skin.TableColumnHeader;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumnBase;

public class CustomTableColumnHeader extends TableColumnHeader{

	private static final double DEFAULT_COLUMN_WIDTH = 80.0F;
	private boolean autoSizeComplete;
	private TableViewSkinBase skin;

	public CustomTableColumnHeader(TableViewSkinBase skin, TableColumnBase tc) {
		super(skin, tc);
		this.skin = skin;
	}

	protected void handlePropertyChanged(String p) {
        if ("SCENE".equals(p) && getTableColumn() != null  && getTableColumn().getWidth() != DEFAULT_COLUMN_WIDTH && getTableColumn().getColumns().isEmpty()) {
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
