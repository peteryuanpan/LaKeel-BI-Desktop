package testJAXB;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

public class MainTest {

	public static void main(String[] args) {
		
		B b = new B();
		//print(b);
		A a = new B();
		print(a);
	}
	
	public static void print(Object obj) {
		try {
	    	JAXBContext context = JAXBContext.newInstance(obj.getClass());
	    	Marshaller m = context.createMarshaller();
	    	m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        m.marshal(obj, System.out);
	    	
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
