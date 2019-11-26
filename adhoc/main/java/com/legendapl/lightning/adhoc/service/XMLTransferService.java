package com.legendapl.lightning.adhoc.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

import com.legendapl.lightning.adhoc.adhocView.model.Adhoc;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.common.ModelType;
import com.legendapl.lightning.adhoc.model.BaseModel;
import com.legendapl.lightning.adhoc.model.Topic;

/**
 * XMLでデータを永続化させる
 *
 * @author Legend Applications China, LaKeel BI development team.
 * @since 2018/1/31
 */
public class XMLTransferService {

	protected static Logger logger = Logger.getLogger(XMLTransferService.class);
	
	/**
	 * Saves the current adhoc/topic data to the specified file.
	 *
	 * @param file
	 * @throws Exception 
	 */
	public static void saveDataToXML(BaseModel object, File file) throws Exception {
	    try {
	    	ModelType modelType = object.getModelType();
	    	Class<? extends BaseModel> classType = AdhocUtils.getClassType(modelType);
	    	JAXBContext context = JAXBContext.newInstance(classType);
	    	Marshaller m = context.createMarshaller();
	    	m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
	        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	        // Marshalling and saving XML to the file.
	        m.marshal(classType.cast(object), file);
	    } catch (Exception e) { // catches ANY exception
	    	throw e;
	    }
	}

	/**
	 * Loads adhoc data from the specified file.
	 *
	 * @param file
	 * @throws Exception 
	 */
	public static Adhoc loadAdhocFromFile(File file) throws Exception {
	    try {
	    	JAXBContext context = JAXBContext.newInstance(Adhoc.class);
	    	Unmarshaller um = context.createUnmarshaller();
	    	Adhoc adhoc = (Adhoc) um.unmarshal(file);
	    	return adhoc;
	    } catch (Exception e) { // catches ANY exception
	    	throw e;
	    }
	}

	/**
	 * Loads topic data from the specified file.
	 *
	 * @param file
	 * @throws Exception 
	 */
	public static Topic loadTopicFromFile(File file) throws Exception {
		try {
	    	JAXBContext context = JAXBContext.newInstance(Topic.class);
	    	Unmarshaller um = context.createUnmarshaller();
	    	Topic topic = (Topic) um.unmarshal(file);
	    	return topic;
	    } catch (Exception e) { // catches ANY exception
	    	throw e;
	    }
	}
	
	/**
	 * Loads XML data from the specified file.
	 * 
	 * @param file
	 * @throws Exception
	 */
	@SuppressWarnings("resource")
	public static String loadXMLFromFile(File file) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(file));
		StringBuilder sb = new StringBuilder();
		String line;
		while ((line = rd.readLine()) != null) {
			sb.append(line).append(System.lineSeparator());
		}
		if (sb.length() > 0) {
			int l = sb.lastIndexOf(System.lineSeparator());
			sb.delete(l, sb.length());
		}
		String XMLString = sb.toString().trim();
		return XMLString;
	}
	
}
