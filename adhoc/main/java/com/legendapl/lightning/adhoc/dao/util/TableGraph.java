package com.legendapl.lightning.adhoc.dao.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.legendapl.lightning.adhoc.adhocView.model.JoinDTO;
import com.legendapl.lightning.adhoc.adhocView.model.SortData;
import com.legendapl.lightning.adhoc.common.JoinType;
import com.legendapl.lightning.adhoc.filter.Filter;
import com.legendapl.lightning.adhoc.model.Join;

/**
 *　データベースのテーブルの結合を格納して、From句を生成する
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class TableGraph {
	private List<TableNode> nodes = new ArrayList<TableNode>();
	private static final String eqRegex = "[^\\(\\s]*\\s==\\s[^\\)\\s]*";
	private static final Pattern eqPattern = Pattern.compile(eqRegex);
	private Set<String> filterTableIds = new HashSet<String>();
	private Set<String> sortTableIds = new HashSet<String>();
	private Map<String, TableNode> idMap = new HashMap<String, TableNode>();
	private int size;
	private TableEdge[][] graph;
	private List<String> tableIds = new ArrayList<String>();
	private List<Integer> minConnectedNodes;
	public TableGraph(int size) {
		this.size = size;
		graph = new TableEdge[size][];
		for(int i=0;i<size;i++) {
			graph[i] = new TableEdge[size];
		}
	}

	public void addEdge(String domainJoin) {
		domainJoin = domainJoin.toLowerCase();
		JoinType joinType = JoinType.Unknow;
		if(domainJoin.startsWith("left outer join")) {
			joinType = JoinType.LeftOuter;
		} else if(domainJoin.startsWith("right outer join")) {
			joinType = JoinType.RightOuter;
		} else if(domainJoin.startsWith("join")) {
			joinType = JoinType.Inner;
		} else if(domainJoin.startsWith("full out")) {
			joinType = JoinType.Full;
		}

		Matcher matcher = eqPattern.matcher(domainJoin);

		while(matcher.find()) {
			String joinSQL = matcher.group();
			String[] fields = joinSQL.split(" == ");
			String sTable = fields[0].split("\\.")[0];
			String tTable = fields[1].split("\\.")[0];
			TableNode sNode = getTableNode(sTable);
			TableNode tNode = getTableNode(tTable);
			TableEdge edge = new TableEdge(joinSQL, joinType);
			edge.sNode = sNode;
			edge.tNode = tNode;

			if(graph[sNode.index][tNode.index] == null) {
				graph[sNode.index][tNode.index] = edge;
				graph[tNode.index][sNode.index] = edge;
			} else {
				graph[sNode.index][tNode.index].updateEdge(edge);
			}
		}
	}

	public void addEdges(List<Join> joinList) {
		joinList.forEach(join -> {
			addEdge(join.getSql());
		});
	}

	private TableNode getTableNode(String tableId) {
		if(idMap.get(tableId) != null) {
			return idMap.get(tableId);
		}
		TableNode tableNode = new TableNode(tableId);
		idMap.put(tableId, tableNode);
		tableNode.index = nodes.size();
		nodes.add(tableNode);
		tableIds.add(tableId);
		return tableNode;
	}

	public void addTableIds(List<String> tableIdSet, List<Filter> filters, List<SortData> sorts) {
		// 元セットをクリア
		filterTableIds.clear();
		sortTableIds.clear();
		for (Filter filter : filters) {
			filterTableIds.add(filter.getFieldId().substring(0, filter.getFieldId().indexOf(".")));
		}
		for (SortData sort : sorts) {
			sortTableIds.add(sort.getTableId());
		}
		minConnectedNodes = new ArrayList<Integer>();
		String firstTableId = tableIdSet.get(0);
		Set<String> selectedTableIds = new HashSet<String>();
		selectedTableIds.addAll(tableIdSet);
		selectedTableIds.addAll(filterTableIds);
		selectedTableIds.addAll(sortTableIds);
		List<String> leftTable = new ArrayList<String>();
		leftTable.addAll(tableIds);
		leftTable.removeAll(selectedTableIds);
		GenerateLeftList<String> tool = new GenerateLeftList<>(leftTable);
		while(true) {
			List<String> joinTableIds = new ArrayList<String>();
			joinTableIds.addAll(selectedTableIds);
			joinTableIds.addAll(tool.get());
			List<Integer> joinTableNums = new ArrayList<Integer>();
			for(String id: joinTableIds) {
				joinTableNums.add(idMap.get(id.toLowerCase()).index);
			}
			TableSubGraph subGraph = new TableSubGraph(size);
			for(int i=0;i<size;i++) {
				if(!joinTableNums.contains(i))
					continue;
				for(int j=0;j<size;j++) {
					if(!joinTableNums.contains(j))
						continue;
					if(graph[i][j] != null) {
						subGraph.addEdge(i, j);
					}
				}
			}
			DepthFirstSearch dfs = new DepthFirstSearch(subGraph, idMap.get(firstTableId.toLowerCase()).index);
			if(dfs.count == joinTableNums.size()) {
				minConnectedNodes = joinTableNums;
				Collections.sort(minConnectedNodes);
				break;
			}
		}
	}

	public void setFilterTableIds(Set<String> filterTableIds) {
		this.filterTableIds = filterTableIds;
	}

	public String getMainTableId() {
		return nodes.get(minConnectedNodes.get(0)).tableId;
	}

	public List<JoinDTO> getSubTables() {
		List<JoinDTO> joins = new ArrayList<JoinDTO>();
		Queue<Integer> queue = new LinkedList<Integer>();
		Set<Integer> indexSet = new HashSet<Integer>();
		indexSet.addAll(minConnectedNodes);
		queue.add(minConnectedNodes.get(0));
		while(!indexSet.isEmpty()) {
			int index = queue.poll();
			indexSet.remove(index);
			for(int i=0;i<size;i++) {
				if(graph[index][i] != null && indexSet.contains(i)) {
					queue.add(i);
					JoinDTO join = new JoinDTO();
					join.setCondition(graph[index][i].joinSQL);
					join.setJoinType(graph[index][i].joinType);
					join.setSubTableId(nodes.get(i).tableId);
					joins.add(join);
				}
			}
		}
		return joins;
	}

	private static class TableNode implements Comparable<TableNode> {
		private int index;
		private String tableId;

		@Override
		public boolean equals(Object obj) {
			TableNode otherNode = (TableNode) obj;
			if(otherNode.tableId.equals(tableId)) {
				return true;
			} else {
				return false;
			}
		}

		TableNode(String tableId) {
			this.tableId = tableId;
		}

		@Override
		public int compareTo(TableNode otherNode) {
			if(index < otherNode.index)
				return -1;
			else if(index == otherNode.index)
				return 0;
			else
				return 1;
		}

		@Override
		public String toString() {
			return "TableNode [index=" + index + ", tableId=" + tableId + "]";
		}
	}

	private static class TableEdge {
		private TableNode sNode;
		private TableNode tNode;
		private String joinSQL;
		private JoinType joinType;
		public TableEdge(String joinSQL, JoinType joinType) {
			this.joinSQL = joinSQL;
			this.joinType = joinType;
		}

		@Override
		public boolean equals(Object obj) {
			TableEdge otherEdge = (TableEdge) obj;
			if((sNode.equals(otherEdge.sNode) && tNode.equals(otherEdge.tNode)) || (tNode.equals(otherEdge.sNode) && sNode.equals(otherEdge.tNode))) {
				return true;
			}
			return false;
		}

		public void updateEdge(TableEdge edge) {
			joinSQL += " and " + edge.joinSQL;
		}
	}

	private static class DepthFirstSearch {
	    private boolean[] marked;
	    private int count;

	    public DepthFirstSearch(TableSubGraph g, int s) {
	        marked = new boolean[g.getVertices()];
	        dfs(g, s);
	    }

	    private void dfs(TableSubGraph g, int v) {
	        marked[v] = true;
	        count++;
	        for(int w : g.adj(v))
	            if(!marked[w]) {
	                dfs(g, w);
	            }
	    }
	}

	public void addFilterTables(List<Filter> filters) {
		filters.forEach(filter -> {
			addEdge(filter.getResourceId());
		});
	}

}
