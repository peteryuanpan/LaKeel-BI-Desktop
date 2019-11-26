package com.legendapl.lightning.validation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jfoenix.validation.base.ValidatorBase;

import javafx.scene.control.TextInputControl;

public class RegValidator extends ValidatorBase {


	private String regEx = "";

	public RegValidator(String regEx) {
		this.regEx = regEx;
	}

	@Override
	protected void eval() {
		TextInputControl textField = (TextInputControl) srcControl.get();
		if(!textField.getText().isEmpty()) {
			Pattern pattern = Pattern.compile(regEx);
			Matcher matcher = pattern.matcher(textField.getText());
			boolean regRight = matcher.matches();
			hasErrors.set(!regRight);
		} else {
			hasErrors.set(false);
		}

	}

}
