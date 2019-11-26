package com.legendapl.lightning.validation;

import java.time.LocalTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javafx.util.StringConverter;

public class JFXTimePickerConverter extends StringConverter<LocalTime>{

	@Override
	public String toString(LocalTime time) {
		if(time != null)
			return time.toString();
		else
			return null;
	}

	@Override
	public LocalTime fromString(String string) {
		if(string == null || string.isEmpty())
			return null;
		LocalTime result = null;
		Pattern pattern = Pattern.compile("([0-9]{1,2})[:：]([0-9]{1,2})");
		Matcher matcher = pattern.matcher(string);
		if(matcher.find() && matcher.groupCount() == 2) {
			int hour = Integer.parseInt(matcher.group(1));
			int min = Integer.parseInt(matcher.group(2));
			if(0 <= hour && hour < 24 & min < 60 && min >= 0) {
				result = LocalTime.of(hour, min);
			}
		}
		return result;
	}

}
