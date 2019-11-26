package com.legendapl.lightning.adhoc.service;

import java.io.File;
import java.nio.file.Files;

public class LocalFileDeleteService {
	
	public static void delete(File file) throws Exception {
		if (null != file) {
			if (file.isDirectory()) deleteFolder(file);
			else deleteFile(file);
		}
	}
	
	public static void deleteFile(File file) throws Exception {
		if (null != file) {
			Files.delete(file.toPath());
		}
	}
	
	public static void deleteFolder(File folder) throws Exception {
		if (null != folder) {
		    File[] files = folder.listFiles();
		    if (null != files) {
		        for (File file: files) {
		        	delete(file);
		        }
		    }
		    deleteFile(folder);
		}
	}
	
}
