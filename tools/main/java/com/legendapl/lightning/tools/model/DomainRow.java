package com.legendapl.lightning.tools.model;

/**
 * 
 * @author LAC_潘
 * @since 2017/9/21
 *
 */
public class DomainRow extends BaseModel<DomainRow> implements Comparable<DomainRow> {
	private StringBase sourceId = new StringBase("");
	private StringBase labelServer = new StringBase("");
	private StringBase labelUpdated = new StringBase("");

	public DomainRow() {
		super();
	}
	
	public DomainRow(String sourceId, String labelServer, String labelUpdated) {
		setSourceId(sourceId);
		setLabelServer(labelServer);
		setLabelUpdated(labelUpdated);
	}
	
	@Override
	public boolean equals(Object domain_) {
		DomainRow domain = (DomainRow) domain_;
		return sourceId.get().equals( domain.getSourceId().get() );
	}
	
	@Override
	public int compareTo(DomainRow domain) {
		return sourceId.get().compareTo( domain.getSourceId().get() );
	}
	
	public StringBase getSourceId() {
		return this.sourceId;
	}
	public DomainRow setSourceId(String sourceIdStr) {
		sourceId.set(sourceIdStr);
		return this;
	}
	
	
	public StringBase getLabelServer() {
		return this.labelServer;
	}
	public DomainRow setLabelServer(String labelServerStr) {
		labelServer.set(labelServerStr);
		return this;
	}
	
	
	public StringBase getLabelUpdated() {
		return this.labelUpdated;
	}
	public DomainRow setLabelUpdated(String labelUpdatedStr) {
		labelUpdated.set(labelUpdatedStr);
		return this;
	}
}
