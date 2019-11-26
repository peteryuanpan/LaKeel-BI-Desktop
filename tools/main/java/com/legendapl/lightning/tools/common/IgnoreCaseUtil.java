package com.legendapl.lightning.tools.common;

import java.util.ArrayList;

@SuppressWarnings("serial")
public class IgnoreCaseUtil extends ArrayList<String>{

	 @Override
	    public boolean contains(Object o) {
	        String paramStr = (String)o;
	        for (String s : this) {
	            if (paramStr.equalsIgnoreCase(s)) return true;
	        }
	        return false;
	    }
}
