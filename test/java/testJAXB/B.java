package testJAXB;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "B")
@XmlType(propOrder = {"b"})
public class B extends A {

	String b;
	
	public B() {
		super();
		b = new String();
	}

	@XmlElement(name = "b")
	public String getB() {
		return b;
	}

	public void setB(String b) {
		this.b = b;
	}
	
	@XmlAttribute(name = "cc")
	//@XmlTransient
	@Override public String getA() {
		return a;
	}

	@Override public void setA(String a) {
		this.a = a;
	}
	
}
