package testJAXB;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAccessType;

@XmlAccessorType(XmlAccessType.NONE)
@XmlRootElement(name = "A")
public class A {

	String a;
	
	public A() {
		a = new String();
	}

	@XmlAttribute(name = "a")
	public String getA() {
		return a;
	}

	public void setA(String a) {
		this.a = a;
	}
	
}
