package com.legendapl.lightning.adhoc.custom;

import com.legendapl.lightning.adhoc.service.BackRunService.VoidRunFun;

public interface Expandable {
	public void register(VoidRunFun expandFun, VoidRunFun collapseFun);

	public void disExpand();

	public void initExpand();

	public void changeExpandStatus(boolean expand);
}
