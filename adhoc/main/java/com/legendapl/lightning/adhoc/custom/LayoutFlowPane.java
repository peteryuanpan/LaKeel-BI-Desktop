package com.legendapl.lightning.adhoc.custom;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.Geometry;
import com.legendapl.lightning.adhoc.common.AdhocConstants;
import com.legendapl.lightning.adhoc.service.AlertWindowService;

import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;

/**
 * レイアウトバンドのフローペイン
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @author pan
 * @since 2018/04/13
 */
public class LayoutFlowPane extends FlowPane {
	
	protected Logger logger = Logger.getLogger(getClass());
	
	private List<Child> children = new ArrayList<>();
	
	public LayoutFlowPane() {
		super();
	}
	
	public LayoutFlowPane(FlowPane _flowPane) {
		super();
		this.setId(_flowPane.getId());
		this.getStylesheets().setAll(_flowPane.getStylesheets());
		this.getStyleClass().setAll(_flowPane.getStyleClass());
	}
	
    @Override public void layoutChildren() {
    	
    	try {
    		super.layoutChildren();
    		storeChildren();
    		adjustChildren();
    		specialAdjust();
    		debugLayoutChildren();
        } catch (Exception e) {
    		logger.error(e.getMessage(), e);
    		AlertWindowService.showError(e.getMessage());
    		return;
        } finally {
        	children.forEach(child -> {
        		layoutInArea(
    				child.node, child.x, child.y, 
                	child.w, child.h, child.blft, getMargin(child.node),
                    getColumnHalignmentInternal(), getRowValignmentInternal()
        		);
        	});
        }
    }
    
    @Override protected double computePrefHeight(double forWidth) {
    	
	    if (getChildren().isEmpty()) {
	    	return super.computePrefHeight(forWidth);
	    } else {
	    	try {
	    		super.layoutChildren();
	    		storeChildren();
	    		adjustChildren();
	    		debugLayoutChildren();
	        } catch (Exception e) {
	    		logger.error(e.getMessage(), e);
	    		AlertWindowService.showError(e.getMessage());
	        }
	        return getChildrenMaxuy(children) + getProperty().bottom;
	    }
    }
    
    private void storeChildren() {
	    children = new ArrayList<>();
	    for (int i = 0; i < getChildren().size(); i ++) {
	    	Node child = getChildren().get(i);
	    	double x = child.getLayoutX();
	    	double y = child.getLayoutY();
	    	double w = 0;
	    	double h = 0;
	    	if (child instanceof Region) {
	    		Region region = (Region) child;
	    		w = region.getWidth();
	    		h = region.getHeight();
	    	} else {
	    		throw new UnknowTypeException("Node is NOT instance of Region.");
	    	}
	    	double blft = child.getBaselineOffset();
	    	double midy = (y + y + h) / 2;
	    	Child newChild = new Child(child, x, y, w, h, blft, midy);
	    	children.add(newChild);
	    }
    }
    
    private void adjustChildren() {
	    Property pro = getProperty();
	    for (int i = 1; i < children.size(); i ++) { // begin with 1
	    	Child pc = children.get(i-1);
	    	Child ch = children.get(i);
	    	boolean bothGridPane = (pc.node instanceof GridPane) && (ch.node instanceof GridPane);
	    	double x = pc.x + pc.w + (bothGridPane ? 0 : pro.hgap);
	    	double midy;
	    	if (Geometry.Double.lessEqual(x + ch.w, pro.maxw)) { // ThisLine
	    		midy = pc.midy;
	    		ch.x = x;
	    		ch.y = getNewy(ch.y, ch.midy, midy);
	    		ch.midy = midy;
	    	} else { // NextLine
	    		midy = getNextMidy(pc.midy, i);
	    		ch.x = children.get(0).x;
	    		ch.y = getNewy(ch.y, ch.midy, midy);
	    		ch.midy = midy;
	    	}
	    }
    }
    
    private void specialAdjust() {
    	if (children.size() >= 2) {
	    	Child pc = children.get(children.size()-2);
	    	Child ch = children.get(children.size()-1);
	    	Property pro = getProperty();
	    	if (ch.y + ch.h > pro.height) {
	    		boolean bothGridPane = (pc.node instanceof GridPane) && (ch.node instanceof GridPane);
	    		ch.x = pc.x + pc.w + (bothGridPane ? 0 : pro.hgap);
	    		ch.y = getNewy(ch.y, ch.midy, pc.midy);
	    		ch.midy = pc.midy;
	    	}
    	}
    }
    
    private double getNextMidy(double midy, int kth) {
	    Property pro = getProperty();
	    for (int i = kth; i < children.size(); i ++) {
	    	if (midy < children.get(i).midy && 
	    			Geometry.Double.lessEqual(pro.vgap, Math.abs(midy - children.get(i).midy))) {
	    		return children.get(i).midy;
	    	}
	    }
    	double maxuy = getChildrenMaxuy(children.subList(0, kth));
    	double maxh = getChildrenMaxh(children.subList(kth, children.size()));
    	double newMidy = maxuy + pro.vgap + maxh / 2;
    	return newMidy;
    }
    
    private double getNewy(double oldy, double oldMidy, double newMidy) {
    	return oldy + (newMidy - oldMidy);
    }
    
    private double getChildrenMaxuy(List<Child> childs) {
	    double maxuy = 0;
	    for (Child child : childs) {
	    	maxuy = Math.max(maxuy, child.y + child.h);
	    }
	    return maxuy;
    }
    
    private double getChildrenMaxh(List<Child> childs) {
	    double maxh = 0;
	    for (Child child : childs) {
	    	maxh = Math.max(maxh, child.h);
	    }
	    return maxh;
    }
    
    private HPos getColumnHalignmentInternal() {
        HPos localPos = getColumnHalignment();
        return null == localPos ? HPos.LEFT : localPos;
    }
    
    private VPos getRowValignmentInternal() {
        VPos localPos =  getRowValignment();
        return null == localPos ? VPos.CENTER : localPos;
    }
    
    private class Property {
	    double width, height;
	    double top, bottom;
	    double left, right;
	    double maxw, maxh;
	    double hgap, vgap;
        public Property() {
            width = getWidth();
            height = getHeight();
            top = getInsets().getTop();
            bottom = getInsets().getBottom();
            left = getInsets().getLeft();
            right = getInsets().getRight();
            maxw = width - left - right;
            maxh = height - top - bottom;
            hgap = snapSpace(getHgap());
            vgap = snapSpace(getVgap());
        }
        @Override public String toString() {
        	return "width = " + width + " height = " + height +
        			" top = " + top + " bottom = " + bottom +
	        		" left = " + left + " right = " + right + 
	        		" maxw = " + maxw + " maxh = " + maxh +
	        		" hgap = " + hgap + " vgap = " + vgap;
        }
    }
    
    private Property getProperty() {
    	return new Property();
    }
    
	private class Child {
		Node node;
		double x, y, w, h;
		double blft, midy;
		public Child(Node _node,
				double _x, double _y, double _w, double _h,
				double _blft, double _midy) {
			super();
			node = _node;
			x = _x;
			y = _y;
			w = _w;
			h = _h;
			blft = _blft;
			midy = _midy;
		}
		@Override public String toString() {
			return node.getId() + 
				" x = " + x + " y = " + y + " w = " + w + " h = " + h + 
				" blft = " + blft + " midy = " + midy;
		}
	}
	
    private class UnknowTypeException extends RuntimeException {
		private static final long serialVersionUID = 1L;
		public UnknowTypeException(String message) {
    		super(message);
		}
    }
    
    private void debugLayoutChildren() {
    	if (AdhocConstants.Debug.LayoutFlowPane) {
            Property pro = new Property();
            System.out.println(pro.toString());
            children.forEach(child -> {
            	System.out.println(child.toString());
            });
    	}
    }
    
}
