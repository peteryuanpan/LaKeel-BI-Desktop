package com.legendapl.lightning.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javax.xml.bind.JAXB;

import org.apache.log4j.Logger;

import com.legendapl.lightning.common.crypt.CryptUtil;
import com.legendapl.lightning.common.logger.LoggerMessageKey.Error;
import com.legendapl.lightning.model.DataSourceImpl;
import com.legendapl.lightning.model.ObservableDataSource;

/**
 * データソースのロードと保存
 * 
 * @author taka
 *
 */
public class DataSourceServiceImpl implements DataSourceService {

	protected String xmlPassword;

	protected Logger logger = Logger.getLogger(getClass());

	protected File serverRoot;
	
	protected DataSourceServiceImpl() {
	}

	public DataSourceServiceImpl(String dataSourceServerRootPath, ResourceBundle res, String xmlPassword)
			throws Exception {
		this.xmlPassword = xmlPassword;
		serverRoot = new File(dataSourceServerRootPath);
		if (!serverRoot.isDirectory() || !serverRoot.canWrite()) {
			throw new Exception(MessageFormat.format(res.getString(Error.ERROR_W03_01), dataSourceServerRootPath));
		}
		logger.debug("Data Source Root : " + serverRoot.getAbsolutePath());
	}

	@Override
	public List<ObservableDataSource> getDataSources() throws IOException {

		List<ObservableDataSource> dss = new ArrayList<ObservableDataSource>();

		FileVisitor<Path> visitor = new FileVisitor<Path>() {

			@Override
			public FileVisitResult postVisitDirectory(Path arg0, IOException arg1) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult preVisitDirectory(Path arg0, BasicFileAttributes arg1) throws IOException {
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult visitFile(Path arg0, BasicFileAttributes arg1) throws IOException {

				try (BufferedReader rd = new BufferedReader(new FileReader(arg0.toFile()))) {
					StringBuilder sb = new StringBuilder();
					String line;
					while ((line = rd.readLine()) != null) {
						sb.append(line).append(System.lineSeparator());
					}
					if (sb.length() > 0) { // 最後の改行を削除する
						int l = sb.lastIndexOf(System.lineSeparator());
						sb.delete(l, sb.length());
					}
					// 復号化
					String decodedXMLString;

					if (sb.toString().trim().startsWith("<?xml")) { // 設定ファイルは平文？
						decodedXMLString = sb.toString();
					} else {
						decodedXMLString = getCrypter().deryptByAES(sb.toString());
					}

					try (StringReader srd = new StringReader(decodedXMLString)) {
						DataSourceImpl hoge = JAXB.unmarshal(srd, DataSourceImpl.class);
						hoge.setLocalFileUrl(arg0.toString());
						dss.add(hoge);
						return FileVisitResult.CONTINUE;
					}
				}
			}

			@Override
			public FileVisitResult visitFileFailed(Path arg0, IOException arg1) throws IOException {
				// TODO Auto-generated method stub
				return FileVisitResult.TERMINATE;
			}

		};

		Files.walkFileTree(Paths.get(serverRoot.getAbsolutePath()), visitor);

		return dss;
	}

	protected CryptUtil crypter = null;

	protected CryptUtil getCrypter() {
		if (crypter == null) {
			crypter = CryptUtil.getInstance(xmlPassword);
		}
		return crypter;
	}

	
	/**
	 * @author panyuan edit on 2018.01.08
	 */
	@Override
	public void saveDataSources(List<ObservableDataSource> dataSources) {
		
		dataSources.forEach((d) -> {

			if (!d.isDirty()) {
				return;
			}
			
			if (d.getPassword()!=null && d.getPassword().isEmpty()) {
				d.setPassword(null);
			}
			
			Writer wr = null;
			
			try {
				StringWriter sw = new StringWriter();
				JAXB.marshal(d, sw);
				logger.debug("------------------\n" + sw.toString() + "\n----------------------\n");

				String XMLString;
				if (d.getPassword()!=null && !d.getPassword().isEmpty()) {
					XMLString = getCrypter().encryptByAES(sw.toString());
				} else {
					XMLString = sw.toString();
				}
				
				String filePath = serverRoot.getAbsolutePath() + (!d.getDataSourcePath().startsWith("/") ? "/" : "")
								  + d.getDataSourcePath() + "/" + d.getName() + ".xml";
				File outFile = new File(filePath);
                if (!outFile.exists()) {
                	new File(outFile.getParent()).mkdirs();
                }
				
                wr = new FileWriter(filePath);
				wr.write(XMLString);
				wr.close();
				d.setDirty(false);
				
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (wr != null) {
					try {
						wr.close();
					} catch (IOException e) {
						logger.error(e.getLocalizedMessage(), e);
					}
				}
			}
		});
	}
}
