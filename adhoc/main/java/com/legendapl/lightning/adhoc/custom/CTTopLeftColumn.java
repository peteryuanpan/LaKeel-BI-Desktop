package com.legendapl.lightning.adhoc.custom;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.BackRunService.VoidRunFun;

/**
 * CrossTable Top Left Column
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class CTTopLeftColumn <S, T> extends CTCustomColumn<S, T> implements Expandable {

	private VoidRunFun expandFun;
	private VoidRunFun collapseFun;

	public CTTopLeftColumn(String text) {
		super(text);
		getStyleClass().setAll("label-column");
	}

	public CTTopLeftColumn() {
		super();
		getStyleClass().setAll("label-column");
	}

	@Override
	public void collapse() {
		try {
			collapseFun.call();
		} catch (Exception e) {
			AdhocUtils.logger.error(e.getMessage(), e);
			AlertWindowService.showError("collapse failed.", e.getMessage());
		}
	}

	@Override
	public void expand() {
		try {
			expandFun.call();
		} catch (Exception e) {
			AdhocUtils.logger.error(e.getMessage(), e);
			AlertWindowService.showError("expand failed.", e.getMessage());
		}
	}

	@Override
	public void register(VoidRunFun expandFun, VoidRunFun collapseFun) {
		this.expandFun = expandFun;
		this.collapseFun = collapseFun;
	}

	@Override
	public void disExpand() {
		setGraphic(null);
	}

	@Override
	public void initExpand() {
		setGraphic(collapsedView);
	}

	@Override
	public void changeExpandStatus(boolean expand) {
		if(getGraphic() == null) return;
		if(expand) {
			setGraphic(expandedView);
		} else {
			setGraphic(collapsedView);
		}
	}
}
