package com.legendapl.lightning.adhoc.model;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jaspersoft.jasperserver.dto.resources.ClientResourceLookup;
import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.service.XMLTransferService;

public class FolderResource extends com.legendapl.lightning.model.FolderResource {
	
	private static final long serialVersionUID = 1L;
	
	private String fileType;
	
	public FolderResource() {
		super();
	}

	public FolderResource(String label) {
		super(label);
	}
	
	public FolderResource(String label, String uri) {
		this.setLabel(label);
		this.setUri(uri);
	}
	
	public FolderResource(ClientResourceLookup resource) {
		super(resource);
		this.setCreationDate(transferData(this.getCreationDate()));
		this.setUpdateDate(transferData(this.getUpdateDate()));
	}
	
	public FolderResource(FolderResource resource) {
		super(resource);
		this.setFileType(resource.getFileType());
	}
	
	public FolderResource(File file) throws IOException {
		
		this.setLabel(transferName(file.getName()));
		this.setUri(file.getPath());
		BasicFileAttributes attr = Files.readAttributes(file.toPath(), BasicFileAttributes.class);
		this.setCreationDate(transferData(attr.creationTime().toString()));
		this.setUpdateDate(transferData(attr.lastModifiedTime().toString()));
		
		// set fileType
		String XMLString = XMLTransferService.loadXMLFromFile(file);
		Pattern pattern = Pattern.compile("<\\?xml([\\S\\s]*)\\?>([\\S\\s]*)");
		Matcher matcher = pattern.matcher(XMLString);
		this.setFileType(AdhocUtils.getString("P100.fileType.UNKNOW"));
		if (matcher.matches()) {
			String group2 = matcher.group(2).trim();
			if (group2.startsWith("<Topic")) {
				this.setFileType(AdhocUtils.getString("P100.fileType.TOPIC"));
			} else if (group2.startsWith("<Adhoc")) {
				this.setFileType(AdhocUtils.getString("P100.fileType.ADHOC"));
			}
		}
	}
	
	public String getFileType() {
		return fileType;
	}

	public void setFileType(String fileType) {
		this.fileType = fileType;
	}
	
	private String transferData(String date) {
		if (null != date) {
			Pattern pattern = Pattern.compile("([\\S]+)-([\\S]+)-([\\S]+)T([\\S]*)");
			Matcher matcher = pattern.matcher(date);
			if (matcher.matches()) {
				return matcher.group(1)+"/"+matcher.group(2)+"/"+matcher.group(3);
			}
		}
		return date;
	}
	
	public static String transferName(String fileName) {
		if (null != fileName) {
			if (fileName.endsWith(".xml")) {
				return fileName.substring(0, fileName.length() - 4);
			}
		}
		return fileName;
	}

	@Override
	public String toString() {
		return getLabel();
	}
}
