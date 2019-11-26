package com.legendapl.lightning.adhoc.factory.export;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFColor;

import com.legendapl.lightning.adhoc.adhocView.model.CrossTableViewData;
import com.legendapl.lightning.adhoc.custom.CTTopLeftColumn;
import com.legendapl.lightning.adhoc.custom.CTTopLeftLeafColumn;
import com.legendapl.lightning.adhoc.custom.CTCustomColumn;

import javafx.geometry.HPos;
import javafx.geometry.VPos;

/**
 * ExportUtils
 * @see tableView.css, crossTable.css
 */
@SuppressWarnings({"rawtypes" })
public class ExportUtils {
	
	public static final class XColor {
		public static final XSSFColor WHITE = new XSSFColor(new Color(255, 255, 255));
		public static final XSSFColor BLUE = new XSSFColor(new Color(217, 228, 241));
		public static final XSSFColor GRAY = new XSSFColor(new Color(244, 244, 244));
		public static final XSSFColor DARK_GRAY = new XSSFColor(new Color(208, 206, 206));
	}
	
	public static final class PColor {
		public static final Color WHITE = new Color(255, 255, 255);
		public static final Color BLUE = new Color(217, 228, 241);
		public static final Color GRAY = new Color(244, 244, 244);
		public static final Color DARK_GRAY = new Color(208, 206, 206);
	}
	
	public static VerticalAlignment getVerticalAlignment(VPos vpos) {
		switch (vpos) {
		case BOTTOM: 
			return VerticalAlignment.BOTTOM;
		case CENTER: 
			return VerticalAlignment.CENTER;
		case TOP: 
		default:
			return VerticalAlignment.TOP;
		}
	}
	
	public static HorizontalAlignment getHorizontalAlignment(HPos hpos) {
		switch (hpos) {
		case RIGHT: 
			return HorizontalAlignment.RIGHT;
		case CENTER: 
			return HorizontalAlignment.CENTER;
		case LEFT: 
		default:
			return HorizontalAlignment.LEFT;
		}
	}
	
	public static class ENode {
		
		CTCustomColumn<CrossTableViewData, ?> column;
		Integer r;
		Integer c1;
		Integer c2;
		List<ENode> children;
		
		public ENode(CTCustomColumn<CrossTableViewData, ?> column, Integer r, Integer c1, Integer c2) {
			this.column = column;
			this.r = r;
			this.c1 = c1;
			this.c2 = c2;
		}
		
		public List<ENode> getChildren() {
			if (null == children) children = new ArrayList<>();
			return children;
		}
		
		public ENode getLastChild() {
			Integer size = getChildren().size();
			return 0 == size ? null : getChildren().get(size - 1);
		}
		
		public Integer getColumnNum() {
			return c2 - c1 + 1;
		}
		
		@Override public String toString() {
			String s = "";
			s += null == column ? "NULL " : column.getText() + " ";
			s += r + " " + c1 + " " + c2;
			return s;
		}
	}
	
	public static boolean inTopLeftColumn(ENode node) {
		return inTopLeftColumn(node.column);
	}
	
	public static boolean inTopLeftColumn(CTCustomColumn column) {
		return column.getClass() == CTTopLeftColumn.class;
	}
	
	public static boolean inTopLeftLeafColumn(ENode node) {
		return inTopLeftLeafColumn(node.column);
	}
	
	public static boolean inTopLeftLeafColumn(CTCustomColumn column) {
		return column.getClass() == CTTopLeftLeafColumn.class;
	}
	
	public static boolean inTopRightColumn(ENode node) {
		return inTopRightColumn(node.column);
	}
	
	public static boolean inTopRightColumn(CTCustomColumn column) {
		return column.getClass() == CTCustomColumn.class;
	}
	
}
