package com.legendapl.lightning.adhoc.custom;

import com.legendapl.lightning.adhoc.common.AdhocModelType;
import com.legendapl.lightning.adhoc.model.Field;

import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

public class LayoutLabel extends Label {
	
	private AdhocModelType modelType;
	private Field field;
	private Note note;
	
	private MenuItem menuItemDelete;
	private MenuItem menuItemMoveLeft;
	private MenuItem menuItemMoveRight;
	private Menu menuDataFormat;
	
	public LayoutLabel () {
		super();
		this.getStylesheets().setAll("/view/adhocView.css");
	}

	public AdhocModelType getModelType() {
		return modelType;
	}

	public void setModelType(AdhocModelType modelType) {
		this.modelType = modelType;
	}

	public Field getField() {
		return field;
	}

	public void setField(Field field) {
		this.field = field;
	}
	
	public MenuItem getMenuItemDelete() {
		return menuItemDelete;
	}

	public void setMenuItemDelete(MenuItem menuItemDelete) {
		this.menuItemDelete = menuItemDelete;
	}

	public MenuItem getMenuItemMoveLeft() {
		return menuItemMoveLeft;
	}

	public void setMenuItemMoveLeft(MenuItem menuItemMoveLeft) {
		this.menuItemMoveLeft = menuItemMoveLeft;
	}

	public MenuItem getMenuItemMoveRight() {
		return menuItemMoveRight;
	}

	public void setMenuItemMoveRight(MenuItem menuItemMoveRight) {
		this.menuItemMoveRight = menuItemMoveRight;
	}

	public Menu getMenuDataFormat() {
		return menuDataFormat;
	}

	public void setMenuDataFormat(Menu menuDataFormat) {
		this.menuDataFormat = menuDataFormat;
	}

	/**
	 * Notification
	 */
	public class Note {
		Boolean visible;
		String calculateType;
		// TODO : other
		public Note() {
			visible = true;
		}
		public Note(Note note) {
			this.visible = note.visible;
			this.calculateType = note.calculateType;
		}
		public String getCalculateType() {
			return calculateType;
		}
		public void setCalculateType(String calculateType) {
			clearNoteText();
			this.calculateType = calculateType;
			setNoteText();
		}
		public Boolean isVisible() {
			return visible;
		}
		public void setVisible(Boolean visible) {
			this.visible = visible;
			handleVisible();
		}
		void handleVisible() {
			if (visible) {
				clearNoteText();
				setNoteText();
			} else {
				clearNoteText();
			}
		}
	}
	
	private String getNoteText(Note note) {
		String noteText = new String();
		if (null != note.calculateType) {
			if (!noteText.isEmpty()) noteText = noteText + ", ";
			noteText += note.calculateType;
		}
		if (!noteText.isEmpty()) {
			noteText = "(" + noteText + ")";
		}
		return noteText;
	}
	
	private void clearNoteText() {
		String noteText = getNoteText();
		if (!noteText.isEmpty() && this.getText().endsWith(noteText)) {
			String text = this.getText();
			this.setText(text.substring(0, text.length() - noteText.length()));
		}
	}
	
	private void setNoteText() {
		String noteText = getNoteText();
		this.setText(this.getText() + noteText);
	}
	
	public String getNoteText() {
		return getNoteText(getNote());
	}
	
	public Note getNote() {
		if (null == note) note = new Note();
		return note;
	}

	public void updateNote(Note note) {
		clearNoteText();
		this.note = new Note(note);
		setNoteText();
	}
	
	public Note cloneNote() {
		return cloneNote(getNote());
	}
	
	public Note cloneNote(Note note) {
		return new Note(note);
	}
	
}
