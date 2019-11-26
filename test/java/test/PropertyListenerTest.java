package test;

import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty ;
import javafx.beans.property.IntegerProperty ;
import javafx.beans.property.SimpleBooleanProperty ;
import javafx.beans.property.SimpleIntegerProperty ;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PropertyListenerTest {
	
    public static void main(String args[]) {
    	OggettoObservable oggettoObservable = new OggettoObservable();
    	oggettoObservable.computeValue().stringProperty().addListener((a, b, c) -> {
    		System.out.println(c);
    	});
    	for (int i = 0 ; i < 5; i ++) {
    		oggettoObservable.computeValue().stringProperty().set("test" + i);
    	}
    }
}

class OggettoObservable extends ObjectBinding<Object> {

    private final Oggetto value;

    public OggettoObservable() {
        this.value = new Oggetto(0, true, "");
        bind(this.value.valueProperty(),
        		this.value.validProperty(),
        		this.value.stringProperty());
    }

    @Override public Oggetto computeValue() {
        return value ;
    }
}

class Oggetto {
	
	private final StringProperty string = new SimpleStringProperty();
	
	public final StringProperty stringProperty() {
		return string;
	}
	
    public final StringProperty getString() {
		return string;
	}

    public final void setString(String string) {
        this.string.set(string);
    }

	private final IntegerProperty value = new SimpleIntegerProperty() ;

    public final IntegerProperty valueProperty() {
        return value ;
    }

    public final int getValue() {
        return value.get();
    }

    public final void setValue(int value) {
        this.value.set(value);
    }

    private final BooleanProperty valid = new SimpleBooleanProperty();

    public final BooleanProperty validProperty() {
        return valid ;
    }

    public final boolean isValid() {
        return valid.get();
    }

    public final void setValid(boolean valid) {
        this.valid.set(valid);
    }

    public Oggetto(int value, boolean valid, String string) {
        setValue(value);
        setValid(valid);
        setString(string);
    }
}
