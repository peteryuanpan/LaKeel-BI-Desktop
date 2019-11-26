package com.legendapl.lightning.adhoc.custom;

import java.util.ArrayList;
import java.util.List;

import com.legendapl.lightning.adhoc.adhocView.model.ColumnSearchTrack;
import com.legendapl.lightning.adhoc.adhocView.model.CrossTableField;
import com.legendapl.lightning.adhoc.adhocView.model.trieTree.CTColumnTrieTreeNode;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.common.AdhocUtils;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.application.Platform;
import javafx.scene.control.TableColumn;

/**
 *　クロス集計の列を表示するTableColumn
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CTCustomColumn<S, T> extends TableColumn<S, T> implements Cloneable{
	
	private boolean total;

	private ColumnSearchTrack track = new ColumnSearchTrack();

	private TableColumn<S, ?> totalChild;

	private List<TableColumn<S, ?>> childs = new ArrayList<>();

	private boolean expanded = true;

	protected final FontAwesomeIconView expandedView = new FontAwesomeIconView(AdhocConstants.CrossTable.EXPANDEX_ICON);

	protected final FontAwesomeIconView collapsedView = new FontAwesomeIconView(AdhocConstants.CrossTable.COLLAPSED_ICON);

	private CTColumnTrieTreeNode root;

	private CrossTableField field = null;

	public void addWidth(double gap) {
		if (!getColumns().isEmpty()) {
			double childWidth = gap / getColumns().size();
			getColumns().forEach(column -> {
				((CTCustomColumn<S, T>) column).addWidth(childWidth);
			});
			setIntWidth(getWidth() + gap);
		} else {
			setIntWidth(getWidth() + gap);
		}
	}

	public CTCustomColumn() {
		super();
		setSortable(false);
		setGraphic(null);
		impl_setReorderable(false);
		expandedView.setOnMouseClicked(event -> {
			collapse();
		});
		collapsedView.setOnMouseClicked(event -> {
			expand();
		});
	}

	public CTCustomColumn(Comparable text) {
		super(text == null ? "" : text.toString());
		setSortable(false);
		setGraphic(null);
		impl_setReorderable(false);
		expandedView.setOnMouseClicked(event -> {
			Platform.runLater(() -> {
				collapse();
			});
		});
		collapsedView.setOnMouseClicked(event -> {
			Platform.runLater(() -> {
				expand();
			});
		});
	}

	public boolean isTotal() {
		return total;
	}

	public void setTotal(boolean total) {
		this.total = total;
	}

	public void collapse() {
		if(!expanded) {
			return;
		}
		Platform.runLater(() -> {
			root.setExpand(track.getMeasure(), false);
			expanded = false;
			getColumns().setAll(totalChild);
			setGraphic(collapsedView);
			AdhocUtils.autoWidth(getTableView());
			getTableView().refresh();
		});
	}

	public void expand() {
		if(expanded) {
			return;
		}
		Platform.runLater(() -> {
			root.setExpand(track.getMeasure(), true);
			expanded = true;
			if(childs.isEmpty()) {
				root.generateChildColumns(this);
			}
			getColumns().setAll(childs);
			setGraphic(expandedView);
			AdhocUtils.autoWidth(getTableView());
			getTableView().refresh();
		});
	}

	public CrossTableField getField() {
		return field;
	}

	public void setField(CrossTableField field) {
		this.field = field;
	}

	public ColumnSearchTrack getTrack() {
		return track;
	}

	public void setTrack(ColumnSearchTrack track) {
		this.track = track;
	}

	@Override
	public String toString() {
		return track.getFieldNames().toString();
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void setExpanded(boolean expanded) {
		this.expanded = expanded;
	}

	public CTColumnTrieTreeNode getRoot() {
		return root;
	}

	public void setRoot(CTColumnTrieTreeNode root) {
		this.root = root;
	}

	public void generateColumnIcon() {
		if (root.hasIcon()) {
			if (expanded) {
				setGraphic(expandedView);
			} else {
				setGraphic(collapsedView);
			}
		}
	}

	public List<TableColumn<S, ?>> getChilds() {
		return childs;
	}

	public void setChilds(List<TableColumn<S, ?>> childs) {
		this.childs = childs;
	}

	public TableColumn<S, ?> getTotalChild() {
		return totalChild;
	}

	public void setTotalChild(TableColumn<S, ?> totalChild) {
		this.totalChild = totalChild;
	}

	public void setIntWidth(double widht) {
		try {
			setPrefWidth((int)Math.ceil(widht));
		} catch(RuntimeException e) {
			System.out.println(track);
		}
	}

}
