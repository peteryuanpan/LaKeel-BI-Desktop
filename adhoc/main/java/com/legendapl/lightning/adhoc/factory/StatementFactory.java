package com.legendapl.lightning.adhoc.factory;

import java.util.Stack;

import com.legendapl.lightning.adhoc.adhocView.model.VoidRunFun;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.service.AdhocLogService;

import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.Button;

public class StatementFactory extends AdhocBaseFactory {
	
	public static Button lastStepButton;
	public static Button nextStepButton;
	public static Button firstStepButton;
	
	public static Boolean onlyPush = false;
	private static ObservableStack<AdhocEvent> stack;
	private static SimpleObjectProperty<Integer> index;
	private static EventType eventType = EventType.TODO;
	
	public static void setLastStepButton(Button lastStepButton) {
		StatementFactory.lastStepButton = lastStepButton;
	}

	public static void setNextStepButton(Button nextStepButton) {
		StatementFactory.nextStepButton = nextStepButton;
	}

	public static void setFirstStepButton(Button firstStepButton) {
		StatementFactory.firstStepButton = firstStepButton;
	}
	
	public static String getEventTypeName() {
		return eventType.toString();
	}
	
	public static void init() {
		stack = new ObservableStack<>();
		index = new SimpleObjectProperty<>(-1);
		lastStepButton.disableProperty().bind(lastStepButtonDisable());
		nextStepButton.disableProperty().bind(nextStepButtonDisable());
		firstStepButton.disableProperty().bind(firstStepButtonDisable());
		lastStepButton.setOnAction(event -> handleActionOnLastStepButton());
		nextStepButton.setOnAction(event -> handleActionOnNextStepButton());
		firstStepButton.setOnAction(event -> handleActionOnFirstStepButton());
	}
	
	private static ObservableValue<? extends Boolean> lastStepButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(stack, index);
            }
            @Override protected boolean computeValue() {
            	return index.getValue() < 0;
			}
		};
	}
	
	private static ObservableValue<? extends Boolean> nextStepButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(stack, index);
            }
            @Override protected boolean computeValue() {
            	return index.getValue() + 1 >= stack.size();
			}
		};
	}
	
	private static ObservableValue<? extends Boolean> firstStepButtonDisable() {
		return new BooleanBinding() {
            {
            	super.bind(stack, index);
            }
            @Override protected boolean computeValue() {
            	return index.getValue() < 0;
			}
		};
	}
	
	/**
	 * undo an event
	 */
	private static void handleActionOnLastStepButton() {
		AdhocEvent event = stack.get(index.getValue());
		runLater(event, EventType.UNDO, false);
		index.setValue(index.getValue() - 1);
	}
	
	/**
	 * redo an event
	 */
	private static void handleActionOnNextStepButton() {
		AdhocEvent event = stack.get(index.getValue() + 1);
		runLater(event, EventType.REDO, false);
		index.setValue(index.getValue() + 1);
	}
	
	/**
	 * undo all events
	 */
	private static void handleActionOnFirstStepButton() {
		Platform.runLater(() -> {
			eventType = EventType.UNDO;
			AdhocLogService.info(AdhocUtils.getString("AOL_handleActionOnFirstStepButton"));
		});
		while (index.getValue() >= 0) {
			AdhocEvent event = stack.get(index.getValue());
			runLater(event, EventType.UNDO, true);
			index.setValue(index.getValue() - 1);
		}
	}
	
	/**
	 * clear stack
	 */
	public static void clear() {
		AdhocLogService.sleep = false;
		AdhocLogService.info(AdhocUtils.getString("AOL_handleActionClearStatementFactory"));
		stack.clear();
		index.setValue(-1);
	}
	
	/**
	 * push an event
	 * @param event
	 * @param onlyPush
	 */
	private static void push(AdhocEvent event, Boolean onlyPush) {
		if (onlyPush) {
			stack.push(event);
		} else {
			push(event);
		}
	}
	
	/**
	 * push an event
	 * @param event
	 */
	private static void push(AdhocEvent event) {
		while (index.getValue() + 1 < stack.size()) {
			stack.pop();
		}
		stack.push(event);
		index.setValue(stack.size() - 1);
	}
	
	/**
	 * push an event and runLater
	 * @param todo = redo
	 * @param undo
	 */
	public static void runLater(VoidRunFun todo, VoidRunFun undo) {
		runLater(todo, todo, undo);
	}
	
	/**
	 * push an event and runLater
	 * @param todo
	 * @param redo
	 * @param undo
	 */
	public static void runLater(VoidRunFun todo, VoidRunFun redo, VoidRunFun undo) {
		AdhocEvent event = new AdhocEvent() {
			@Override public void todo() {
				if (null != todo) todo.call();
			}
			@Override public void redo() {
				if (null != redo) redo.call();
			}
			@Override public void undo() {
				if (null != undo) undo.call();
			}
		};
		if (null != todo) runLater(event);
	}
	
	/**
	 * push an event and runLater
	 * @param event
	 */
	private static void runLater(AdhocEvent event) {
		if (null != event) {
			push(event, StatementFactory.onlyPush);
			runLater(event, EventType.TODO, false);
		}
	}
	
	/**
	 * run an event with eventType IN JavaFX-Thread
	 * @param event
	 * @param eventType
	 * @param sleep - Log sleep
	 */
	private static void runLater(AdhocEvent event, EventType eventType, Boolean sleep) {
		Platform.runLater(() -> {
			run(event, eventType, sleep);
		});
	}
	
	/**
	 * run an event with eventType NOT IN JavaFX-Thread
	 * @param event
	 * @param eventType
	 * @param sleep - Log sleep
	 */
	synchronized private static void run(AdhocEvent event, EventType eventType, Boolean sleep) {
		AdhocLogService.sleep = sleep;
		StatementFactory.eventType = eventType;
		switch (eventType) {
		case TODO:
			event.todo();
			break;
		case REDO:
			event.redo();
			break;
		case UNDO:
			event.undo();
			break;
		}
		AdhocLogService.sleep = false;
	}
	
	/**
	 * merge 2 events and print todoLog
	 * @param n
	 * @param todoLog = redoLog
	 * @param undoLog
	 */
	public static void handle(Boolean valid, VoidRunFun todoLog, VoidRunFun undoLog) {
		handle(valid, todoLog, todoLog, undoLog);
	}
	
	/**
	 * merge 2 events and print todoLog
	 * @param n
	 * @param todoLog
	 * @param redoLog
	 * @param undoLog
	 */
	public static void handle(Boolean valid, VoidRunFun todoLog, VoidRunFun redoLog, VoidRunFun undoLog) {
		AdhocEvent event1 = stack.pop();
		AdhocEvent event2 = stack.pop();
		AdhocEvent event = getNewAdhocEvent(event1, event2, todoLog, redoLog, undoLog);
		if (valid) push(event);
		else runLater(event, EventType.UNDO, true);
		Platform.runLater(() -> {
			StatementFactory.onlyPush = false;
			if (valid && null != todoLog) todoLog.call();
		});
	}
	
	/**
	 * return merged new event
	 * @param event1
	 * @param event2
	 * @param todoLog
	 * @param redoLog
	 * @param undoLog
	 * @return
	 */
	private static AdhocEvent getNewAdhocEvent(AdhocEvent event1, AdhocEvent event2, VoidRunFun todoLog, VoidRunFun redoLog, VoidRunFun undoLog) {
		return new AdhocEvent() {
			@Override public void todo() {
				if (null != todoLog) todoLog.call();
				run(event2, EventType.TODO, true);
				run(event1, EventType.TODO, true);
			}
			@Override public void redo() {
				if (null != redoLog) redoLog.call();
				run(event2, EventType.REDO, true);
				run(event1, EventType.REDO, true);
			}
			@Override public void undo() {
				if (null != undoLog) undoLog.call();
				run(event1, EventType.UNDO, true);
				run(event2, EventType.UNDO, true);
			}
		};
	}
}

class ObservableStack<T> extends SimpleListProperty<T> {
	
    private final Stack<T> stack;
    
    public ObservableStack() {
        this.stack = new Stack<>();
        this.set(FXCollections.observableList(this.stack));
    }
    
    public T pop() {
    	return stack.pop();
    }
    
    public T push(T e) {
    	return stack.push(e);
    }
}

enum EventType {
	
	TODO("TODO"), REDO("REDO"), UNDO("UNDO");
	
	String name;
	
	EventType(String name) {
		this.name = name;
	}
	
	@Override public String toString() {
		return name;
	}
}

interface AdhocEvent {
	public void todo();
	public void redo();
	public void undo();
}
