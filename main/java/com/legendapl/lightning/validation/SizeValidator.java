package com.legendapl.lightning.validation;

import com.jfoenix.validation.base.ValidatorBase;

import javafx.scene.control.TextInputControl;

public class SizeValidator extends ValidatorBase{
	private int size;

	public SizeValidator(int size) {
		this.size = size;
	}

	@Override
	protected void eval() {
		TextInputControl textField = (TextInputControl) srcControl.get();
		if(!textField.getText().isEmpty()) {
			hasErrors.set(textField.getText().length() > size);
		} else {
			hasErrors.set(false);
		}
	}

}
