package com.legendapl.lightning.adhoc.factory.export;

import java.io.File;
import java.util.Arrays;

import com.legendapl.lightning.adhoc.common.AdhocUtils;
import com.legendapl.lightning.adhoc.controller.C100AdhocBaseAnchorPane;
import com.legendapl.lightning.adhoc.controller.P121AdhocAnchorPane;
import com.legendapl.lightning.adhoc.factory.AdhocSaveFactory;
import com.legendapl.lightning.adhoc.service.AlertWindowService;
import com.legendapl.lightning.adhoc.service.SaveFileChooserService;
import com.lowagie.text.Document;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import javafx.event.ActionEvent;

public class ExportPdfFactory extends ExportFactory {
	
	static final String FONT_NAME_HEISEI = "HeiseiKakuGo-W5"; // font name
	static final String FONT_CODE_UNIJIS = "UniJIS-UCS2-H"; // font code
	static final int PAGE_SIZE_WIDTH = 612;
	static final int PAGE_SIZE_HEIGHT = 792;
	static final int MARGIN_SIZE = 20;
	static final float DEFAULT_COLUMN_WIDTH_RATIO = (float) 0.9;
	static final Float DEFAULT_HEIGHT_IN_POINTS = (float) 25;
	
	static AdhocExportPdfService service = new AdhocExportPdfService();
	
	/**
	 * convert JFX width to PDF width
	 * @param width
	 * @return
	 */
	static final Integer getColumnWidth(double width) {
		return (int) (width * DEFAULT_COLUMN_WIDTH_RATIO);
	}
	
	/**
	 * Adhoc Export Service 
	 */
	static class AdhocExportPdfService extends SaveFileChooserService {
		public AdhocExportPdfService() {
			super();
			this.getProperty().setTitle(AdhocUtils.getString("P121.button.export"));
			this.getProperty().setFilterTypes(Arrays.asList(ExtensionFilterType.PDF));
		}
		@Override protected void doCustomerCheck(File outputFile) throws Exception {
			String fileName = SaveFileChooserService.getRealFileName(outputFile.getName(), ExtensionFilterType.PDF);
			if (fileName.isEmpty()) { // 名前が空場合
				String detailErrors = AdhocUtils.getString("ERROR_SAVE_FILE_NAME_EMPTY");
				throw new RuntimeException(detailErrors);
			}
			if (fileName.contains(".")) { // 名前が「.」を含む場合
				String detailErrors = AdhocUtils.getString("ERROR_SAVE_FILE_NAME_ILLEGAL");
				throw new RuntimeException(detailErrors);
			}
		}
		@Override protected void doSave(File outputFile) throws Exception {
			lastParentPath = outputFile.getParent();
			export(outputFile);
		}
		@Override protected void doFailed(File outputFile, Exception e) {
			logger.error(e.getMessage(), e);
			AlertWindowService.showError(AdhocUtils.getString("ERROR_ADHOC_EXPORT"), e.getMessage());
		}
		@Override protected void doSuccess(File outputFile) {
			logger.info("Exported Adhoc.");
			AlertWindowService.showInfoNotInBack(AdhocUtils.getString("SUCCESS_ADHOC_EXPORT"));
		}
	}
	
	/**
	 * handle action on exporting
	 * @param event
	 */
	public static void handleActionExportPdf(ActionEvent event) {
		service.getProperty().setInitialFileName(AdhocSaveFactory.fileName);
		service.getProperty().setInitialDirectoryPath(getInitialDirectoryPath());
		service.save(C100AdhocBaseAnchorPane.adhocStage);
	}
	
	static Document document;
	static PdfPTable datatable;
	static Integer columnNum;
	static Integer rowNum;
	static BaseFont baseFont;
	
	/**
	 * export outputFile
	 * @param outputFile
	 * @throws Exception
	 */
	static void export(File outputFile) throws Exception {
		switch (P121AdhocAnchorPane.lastViewModelType) {
		case TABLE:
			TableExportPdfFactory.exportImpl(outputFile);
			break;
		case CROSSTABLE:
			CrossTableExportPdfFactory.exportImpl(outputFile);
			break;
		default:
			break;
		}
	}
	
	static Document setParameters(Document document,String title,String subject,String keywords,String author,
            String creator){
        document.addTitle(title);
        document.addSubject(subject);
        document.addKeywords(keywords);
        document.addAuthor(author);
        document.addCreator(creator);
        document.addProducer();
        document.addCreationDate();
        
        return document;
    }
	
	static PdfPCell getCommonPdfPCell() {
		PdfPCell cell = new PdfPCell();
        cell.setPaddingLeft(5);
		cell.setPaddingTop(5);
        cell.setFixedHeight(DEFAULT_HEIGHT_IN_POINTS);
        cell.setBorderWidth(1);
        return cell;
	}

}
