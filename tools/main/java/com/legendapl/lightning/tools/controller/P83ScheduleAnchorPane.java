
package com.legendapl.lightning.tools.controller;

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ArrayUtils;

import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.CalendarDaysType;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.CalendarTrigger;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.FtpInfo;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.FtpType;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.IntervalUnitType;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.Job;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobAlert;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobAlertJobState;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobAlertRecipient;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobSource;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobStateType;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.MailNotification;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.MailNotificationSendType;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.OutputFormat;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.RepositoryDestination;
import com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.SimpleTrigger;
import com.jfoenix.controls.JFXTreeTableColumn;
import com.jfoenix.controls.JFXTreeTableView;
import com.legendapl.lightning.common.constants.Constant;
import com.legendapl.lightning.tools.common.Constants;
import com.legendapl.lightning.tools.common.Utils;
import com.legendapl.lightning.tools.model.CsvRow;
import com.legendapl.lightning.tools.model.LocalEnum;
import com.legendapl.lightning.tools.model.ProcessFlag;
import com.legendapl.lightning.tools.model.Schedule;
import com.legendapl.lightning.tools.model.TimeZoneEnum;
import com.legendapl.lightning.tools.service.CsvService;
import com.legendapl.lightning.tools.service.EMailFormatCheckService;
import com.legendapl.lightning.tools.service.ExecuteAPIService;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;

/**
 * スケジュール画面のコントローラクラス
 * 
 * @author 
 * @since 
 *
 */
public class P83ScheduleAnchorPane extends P80BaseToolsAnchorPane {
	
    @FXML
    private JFXTreeTableView<Schedule> scheduleTable;
    @FXML
    private JFXTreeTableColumn<Schedule, String> resource;
    @FXML
    private JFXTreeTableColumn<Schedule, String> jobname;
    @FXML
    private JFXTreeTableColumn<Schedule, String> jobid;
    @FXML
    private JFXTreeTableColumn<Schedule, String> owner;
    @FXML
    private JFXTreeTableColumn<Schedule, String> state;
    @FXML
    private JFXTreeTableColumn<Schedule, String> last_execution;
    @FXML
    private JFXTreeTableColumn<Schedule, String> next_execution;
    @FXML
    private JFXTreeTableColumn<Schedule, String> status;
    
    private Long[] jobId = new Long[1];

    private ObservableList<Schedule> schedules = FXCollections.observableArrayList();
    
    private ObservableList<Schedule> serverSchedules = FXCollections.observableArrayList();
    
    private static Pattern pattern = Pattern.compile(Constants.POSITIVE_INTEGER);
    
    //private static Pattern patternMail = Pattern.compile(Constants.CHECK_MAIL);
    
    private List<Long> delIdList = new ArrayList<Long>();
    
    private List<Long> enableIdList = new ArrayList<Long>();
    
    private List<Long> disableIdList = new ArrayList<Long>();
    
    private List<Long> duplicateIdList = new ArrayList<Long>();
    
    private List<Long> serverList = new ArrayList<Long>();
    
    private Map<String, JobStateType> id_State = new HashMap<String, JobStateType>();
    
    private Map<String, Long> id_Version = new HashMap<String, Long>();
    
    private Map<String, List<String>> outputName_FolderUri = new HashMap<String, List<String>>();
    
    private Map<String, List<String>> outputName_FolderUri_Add = new HashMap<String, List<String>>(); /* @author: 潘 */
    
    private List<Job> addJobList = new ArrayList<Job>();
    
    private List<Job> updateJobList = new ArrayList<Job>();

    private List<String> errorMessages = new ArrayList<String>();
    
    private Job job;
    
    private MailNotification mail;
    
    private JobAlert ja;
    
    private SimpleTrigger st;
    
    private CalendarTrigger ct;
    
    private FtpInfo fi;
    
    private JobSource source;
    
    private RepositoryDestination rd;
    
    private Set<OutputFormat> outputformatsSet = new HashSet<OutputFormat>();
    
    private List<com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobSummary> list = new ArrayList<com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobSummary>();
    
    private final List<JFXTreeTableColumn<Schedule, String>> columns = new ArrayList<JFXTreeTableColumn<Schedule, String>>();
    
    private final List<String> functions = new ArrayList<String>();
    
    private final String[] zeroToOne = new String[] {"0","1"};
    private final String[] oneToTwo = new String[] {"1","2"};
    private final String[] zeroToTwo = new String[] {"0","1","2"};
    private final String[] jobState = new String[] {"0","1","2","3"};
    private final String[] sendType = new String[] {"0","1","2","3","4","5"};
    private final String[] weekDays = new String[] {"1","2","3","4","5","6","7"};
    private final String[] months = new String[] {"1","2","3","4","5","6","7","8","9","10","11","12"};
    
    private Date startDate;
    
    private Date endDate;
    
    private String formatsInfo = "";
    
    @Override
    public void init(URL location, ResourceBundle resources) {
        // preferencesを読み込み
        for(int i = 0;i < OutputFormat.values().length;i++) {
            if(i == 0) {
                formatsInfo += OutputFormat.values()[i].name();
            }else {
                formatsInfo += "," + OutputFormat.values()[i].name();
            }
        }
        
        columns.add(resource);
        columns.add(jobname);
        columns.add(jobid);
        columns.add(owner);
        columns.add(state);
        columns.add(last_execution);
        columns.add(next_execution);
        columns.add(status);
        
        
        functions.add("getResource");
        functions.add("getJobname");
        functions.add("getJobid");
        functions.add("getOwner");
        functions.add("getState");
        functions.add("getLast_execution");
        functions.add("getNext_execution");
        functions.add("getStatus");
        
        loadPreferences();
        getWithoutNotify();
    }
    
    public boolean loadData() {
        logger.debug("loadData started");
        serverSchedules.clear();
        schedules = FXCollections.observableArrayList();
        try {
            list = ExecuteAPIService.getJobSummaryList();
            if(list == null) {
                list = new ArrayList<com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobSummary>();
            }
            for(int i = 0;i < list.size();i++) {
                serverSchedules.add(new Schedule(
                        list.get(i).getReportUnitURI(),
                        list.get(i).getLabel(),
                        list.get(i).getId().toString(),
                        list.get(i).getOwner(),
                        list.get(i).getState().getValue().toString(),
                        Utils.dateToStr(list.get(i).getState().getPreviousFireTime()),
                        Utils.dateToStr(list.get(i).getState().getNextFireTime()),
                        ""
                        ));
                serverList.add(list.get(i).getId());
                id_State.put(list.get(i).getId().toString(), list.get(i).getState().getValue());
                id_Version.put(list.get(i).getId().toString(), list.get(i).getVersion());
            }
        } catch (Exception e) {
            setGetFlag(false);
            logger.error("Get: Failed to get data from server");
            logger.error(e.getMessage(), e);
            showAPIException(Utils.getString(Constants.SEREVER_ERROR_GET), e);
            return false;
        }
        schedules.addAll(serverSchedules);
        Collections.sort(schedules);
        showData(schedules,columns,functions,scheduleTable);
        setGetFlag(true);
        logger.info("Get: Successed to get data from server.");
        logger.debug("loadData ended");
        return true;
    }
    
	/*
	 * 【CSVインポート】　押下
	 */
    protected boolean csvImportWork(List<CsvRow> readSchedules) {
        int flagBlankTimes = 0;
        logger.debug("csvImportWork started");
        if(!loadData() || !flagColumnValidate(readSchedules.get(0))) {
            logger.error("Import: Failed to import file.");
            return false;
        }
        delIdList.clear();
        addJobList.clear();
        updateJobList.clear();
        duplicateIdList.clear();
        errorMessages.clear();
        enableIdList.clear();
        disableIdList.clear();
        outputName_FolderUri.clear();
        outputName_FolderUri_Add.clear(); /* @author: 潘 */
        logger.debug("clear");
        logger.debug("Start checking data");
        for(int i = 0;i < readSchedules.size(); i++) {
            CsvRow csvRow = readSchedules.get(i);
            String id = csvRow.get(Constants.P83_TABLE_COLUMN_JOBID);
            if(ProcessFlag.ADD == ProcessFlag.get(csvRow.get(Constants.P83_TABLE_COLUMN_FLAG))) {
                if(add_UpdateValidate(csvRow,ProcessFlag.ADD)) {
                    setJob(csvRow);
                    logger.debug("Seted job");
                    addJobList.add(job);
                    logger.debug("Added job");
                    schedules.add(new Schedule(
                            csvRow.get(Constants.P83_TABLE_COLUMN_RESOURCE),
                            csvRow.get(Constants.P83_TABLE_COLUMN_LABEL),
                            "",
                            csvRow.get(Constant.ServerInfo.userName),
                            "NORMAL",
                            "",
                            "",
                            Constants.P81_STATUS_ADD,
                            ProcessFlag.ADD
                            ));
                }
            }else if(ProcessFlag.UPDATE == ProcessFlag.get(csvRow.get(Constants.P83_TABLE_COLUMN_FLAG))) {
                if(add_UpdateValidate(csvRow,ProcessFlag.UPDATE)) {
                    String state = "";
                    if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_ENABLE))
                            && id_State.get(id) == JobStateType.NORMAL) {
                        disableIdList.add(Long.parseLong(id));
                        logger.debug("Added the id of job to disable[id : "+ id +"]");
                        state = "PAUSED";
                    }else if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_ENABLE))
                            && id_State.get(id) == JobStateType.PAUSED){
                        enableIdList.add(Long.parseLong(id));
                        logger.debug("Added the id of job to enable[id : "+ id +"]");
                        state = "NORMAL";
                    }
                    
                    setJob(csvRow);
                    logger.debug("Seted job");
                    duplicateIdList.add(Long.parseLong(id));
                    updateJobList.add(job);
                    logger.debug("Added the id of job to update[id : "+ id +"]");
                    if(schedules.size() > 0) {
                        for(int j = 0;j < schedules.size();j++) {
                            if(schedules.get(j).getJobid().getValue().equals(id)) {
                                schedules.get(j).setFlag(ProcessFlag.UPDATE);
                                schedules.get(j).setStatus(Constants.P81_STATUS_UPDATE);
                                schedules.get(j).setResource(csvRow.get(Constants.P83_TABLE_COLUMN_RESOURCE));
                                schedules.get(j).setJobname(csvRow.get(Constants.P83_TABLE_COLUMN_LABEL));
                                schedules.get(j).setNext_execution("");
                                if(!"".equals(state)) {
                                    schedules.get(j).setState(state);
                                }
                            }
                        }
                    }
                }
            }else if(ProcessFlag.DELETE == ProcessFlag.get(csvRow.get(Constants.P83_TABLE_COLUMN_FLAG))) {
                if(idValidate(csvRow)) {
                    duplicateIdList.add(Long.parseLong(id));
                    delIdList.add(Long.parseLong(id));
                    logger.debug("Added the id of job to delete[id : "+ id +"]");
                    if(schedules.size() > 0) {
                        for(int j = 0;j < schedules.size();j++) {
                            if(schedules.get(j).getJobid().getValue().equals(id)) {
                                schedules.get(j).setFlag(ProcessFlag.DELETE);
                                schedules.get(j).setStatus(Constants.P81_STATUS_DELETE);
                            }
                        }
                    }
                }
            }else {
                flagBlankTimes++;
                flagValidate(csvRow);
            }
            
        }
        if(errorMessages.size() > 0  || flagBlankTimes == readSchedules.size()) {
            schedules.clear();
            Collections.sort(schedules);
            schedules.addAll(serverSchedules);
            showData(schedules, columns, functions, scheduleTable);
            logger.error("Import: Failed to import file.");
            showError(Utils.getString(Constants.DATA_ERROR_INCORRECT), errorMessages);
        }else {
            if(delIdList.size() == 0 && updateJobList.size() == 0 && addJobList.size() == 0 && disableIdList.size() == 0 && enableIdList.size() == 0) {
                showError(Utils.getString(Constants.DATA_ERROR_FLAGS_ALL_EMPTY), errorMessages);
            }else {
                setImportFlag(true);
            }
            Collections.sort(schedules);
            showData(schedules,columns,functions,scheduleTable);
            logger.info("Import: Successed to import file.");
            logger.debug("csvImportWork ended");
        }
        return true;
	}
    
    private void setJob(CsvRow csvRow) {
        logger.debug("setJob started");
        job = new Job();
        source = new JobSource();
        rd = new RepositoryDestination();
        fi = new FtpInfo();
        st = new SimpleTrigger();
        ct = new CalendarTrigger();
        mail = new MailNotification();
        ja = new JobAlert();
        
        job.setVersion(id_Version.get(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID)));
        if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID))) {
            job.setId(Long.parseLong(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID)));
        }
        job.setOutputFormats(outputformatsSet);
        source.setReportUnitURI(csvRow.get(Constants.P83_TABLE_COLUMN_RESOURCE));
        job.setLabel(csvRow.get(Constants.P83_TABLE_COLUMN_LABEL));
        job.setDescription(csvRow.get(Constants.P83_TABLE_COLUMN_DESCRIPTION));
        job.setBaseOutputFilename(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME));
        rd.setOutputDescription(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_DESCRIPTION));
        if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL_FOLDER_URI))) {
            rd.setOutputLocalFolder(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL_FOLDER_URI));
        }
        job.setOutputTimeZone(TimeZoneEnum.get(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE))).getName());
        if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY))) {
            rd.setSaveToRepository(true);
            rd.setFolderURI(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI));
        }else if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY))) {
            rd.setSaveToRepository(false);
        }
        if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES))) {
            rd.setOverwriteFiles(true);
        }else if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES))) {
            rd.setOverwriteFiles(false);
        }
        
        if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES))) {
            rd.setSequentialFilenames(true);
            rd.setTimestampPattern(csvRow.get(Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN));
        }else if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES))) {
            rd.setSequentialFilenames(false);
        }
        
        if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SERVER_NAME))) {
            fi.setServerName(csvRow.get(Constants.P83_TABLE_COLUMN_SERVER_NAME));
            if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_FTP_TYPE))) {
                fi.setType(FtpType.ftp);
            }else if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_FTP_TYPE))) {
                fi.setType(FtpType.ftps);
            }
            fi.setPort(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_PORT)));
            fi.setFolderPath(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_PATH));
            fi.setUserName(csvRow.get(Constants.P83_TABLE_COLUMN_USERNAME));
            fi.setPassword(csvRow.get(Constants.P83_TABLE_COLUMN_PASSWORD));
            rd.setOutputFTPInfo(fi);
        }
        job.setOutputLocale(LocalEnum.get(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL))).getName());
        
        if("0".equalsIgnoreCase(csvRow.get(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE))) {
            st.setTimezone(TimeZoneEnum.get(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_TIME_ZONE))).getName());
            st.setStartType(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_START_TYPE)));
            if(st.getStartType() != 1) {
                st.setStartDate(startDate);
            }
            st.setEndDate(endDate);
            st.setCalendarName(csvRow.get(Constants.P83_TABLE_COLUMN_CALENDAR_NAME));
            st.setOccurrenceCount(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT)));
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL))) {
                st.setRecurrenceInterval(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL)));
            }
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT))) {
                for (IntervalUnitType iut : IntervalUnitType.values()) {
                    if(iut.ordinal() == Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT))) {
                        st.setRecurrenceIntervalUnit(iut);
                    }
                }
            }
            job.setTrigger(st);
        }else if("1".equalsIgnoreCase(csvRow.get(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE))) {
            SortedSet<Byte> sortedSetWeekDays = new TreeSet<Byte>();
            SortedSet<Byte> sortedSetMonths = new TreeSet<Byte>();
            String weekDays = csvRow.get(Constants.P83_TABLE_COLUMN_WEEK_DAYS);
            String months = csvRow.get(Constants.P83_TABLE_COLUMN_MONTHS);
            
            ct.setTimezone(TimeZoneEnum.get(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_TIME_ZONE))).getName());
            ct.setStartType(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_START_TYPE)));
            if(ct.getStartType() != 1) {
                ct.setStartDate(startDate);
            }
            ct.setEndDate(endDate);
            
            ct.setCalendarName(csvRow.get(Constants.P83_TABLE_COLUMN_CALENDAR_NAME));
            ct.setMinutes(csvRow.get(Constants.P83_TABLE_COLUMN_MINUTES).replace(CsvService.FieldSplitter, CsvService.Splitter));
            ct.setHours(csvRow.get(Constants.P83_TABLE_COLUMN_HOURS).replace(CsvService.FieldSplitter, CsvService.Splitter));
            String calendarDaysType = csvRow.get(Constants.P83_TABLE_COLUMN_DAYS_TYPE);
            for(int j = 0;j < CalendarDaysType.values().length;j++) {
                if(String.valueOf(CalendarDaysType.values()[j].ordinal()).equals(calendarDaysType)) {
                    ct.setDaysType(CalendarDaysType.values()[j]);
                    break;
                }
            }
            if(ct.getDaysType() == CalendarDaysType.WEEK && !"".equals(weekDays)) {
                for (String str : weekDays.split(CsvService.FieldSplitter)) {
                    sortedSetWeekDays.add(Byte.parseByte(str));
                }
                ct.setWeekDays(sortedSetWeekDays);
            }
            if(ct.getDaysType() == CalendarDaysType.MONTH) {
                ct.setMonthDays(csvRow.get(Constants.P83_TABLE_COLUMN_MONTH_DAYS).replace(";", ","));
            }
            if(!"".equals(months)) {
                for (String str : months.split(CsvService.FieldSplitter)) {
                    sortedSetMonths.add(Byte.parseByte(str));
                }
                ct.setMonths(sortedSetMonths);
            }
            job.setTrigger(ct);
        }
        if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT))) {
            mail.setSubject(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT));
            String toAddressesStr = csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES);
            String ccAddressesStr = csvRow.get(Constants.P83_TABLE_COLUMN_CC_ADDRESSES);
            String bccAddressesStr = csvRow.get(Constants.P83_TABLE_COLUMN_BCC_ADDRESSES);
            
            List<String> toAddresses = new ArrayList<String>();
            List<String> ccAddresses = new ArrayList<String>();
            List<String> bccAddresses = new ArrayList<String>();
            
            if(toAddressesStr.split(CsvService.FieldSplitter).length > 0) {
                for (String str : toAddressesStr.split(CsvService.FieldSplitter)) {
                    toAddresses.add(str);
                }
            }
            if(ccAddressesStr.split(CsvService.FieldSplitter).length > 0) {
                for (String str : ccAddressesStr.split(CsvService.FieldSplitter)) {
                    ccAddresses.add(str);
                }
            }
            if(bccAddressesStr.split(CsvService.FieldSplitter).length > 0) {
                for (String str : bccAddressesStr.split(CsvService.FieldSplitter)) {
                    bccAddresses.add(str);
                }
            }
            mail.setToAddresses(toAddresses);
            mail.setCcAddresses(ccAddresses);
            mail.setBccAddresses(bccAddresses);
            mail.setMessageText(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGES));
            String sendType = csvRow.get(Constants.P83_TABLE_COLUMN_SEND_TYPE);
            for(int j = 0;j < MailNotificationSendType.values().length;j++) {
                if(String.valueOf(MailNotificationSendType.values()[j].ordinal()).equals(sendType)) {
                    mail.setResultSendType(MailNotificationSendType.values()[j]);
                    break;
                }
            }
            if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SKIP_EMPTY))) {
                mail.setSkipEmptyReports(true);
            }else if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SKIP_EMPTY))){
                mail.setSkipEmptyReports(false);
            }
            job.setMailNotification(mail);
        }
        
        if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT_ALERT))) {
            String toAddresses_alertStr = csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT);
            List<String> toAddresses_alert = new ArrayList<String>();
            if(toAddresses_alertStr.split(CsvService.FieldSplitter).length > 0) {
                for (String str : toAddresses_alertStr.split(CsvService.FieldSplitter)) {
                    toAddresses_alert.add(str);
                }
            }
            
            ja.setSubject(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT_ALERT));
            ja.setRecipient(JobAlertRecipient.OWNER);
            ja.setToAddresses(toAddresses_alert);
            String jobState = csvRow.get(Constants.P83_TABLE_COLUMN_JOB_STATE);
            for(int j = 0;j < JobAlertJobState.values().length;j++) {
                if(JobAlertJobState.values()[j].ordinal() == Integer.parseInt(jobState)) {
                    ja.setJobState(JobAlertJobState.values()[j]);
                    break;
                }
            }
            ja.setMessageText(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT));
            ja.setMessageTextWhenJobFails(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT_FAIL));
            if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO))) {
                ja.setIncludingReportJobInfo(true);
            }else if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO))) {
                ja.setIncludingReportJobInfo(false);
            }
            if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE))) {
                ja.setIncludingStackTrace(true);
            }else if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE))) {
                ja.setIncludingStackTrace(false);
            }
            job.setAlert(ja);
        }
        if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_PARAMETERS))) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (String string : csvRow.get(Constants.P83_TABLE_COLUMN_PARAMETERS).split(";")) {
                List<String> list = new ArrayList<String>();
                list.addAll(Arrays.asList(string.substring(string.indexOf("=")+1).split("/")));
                map.put(string.substring(0, string.indexOf("=")),list);
            }
            source.setParameters(map);
        }
        job.setSource(source);
        job.setRepositoryDestination(rd);
        logger.debug("setJob ended");
    }
    
    private boolean flagColumnValidate(CsvRow csvRow) {
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_FLAG) == -1) {
            showError(Utils.getString(Constants.DATA_ERROR_NO_COLUMN,Constants.P83_TABLE_COLUMN_FLAG));
            return false;
        }
        return true;
    }
    
    private void flagValidate(CsvRow csvRow) {
        int rowNo = csvRow.getRowNo();
        if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_FLAG))) {
        }else {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_FLAG,"'',-1,0,1"));
        }
        return;
    }
    
    private boolean add_UpdateValidate(CsvRow csvRow, ProcessFlag processFlag) {
        logger.debug("add_UpdateValidate started");
        boolean flag = true;
        int rowNo = csvRow.getRowNo();
        logger.debug("Start checking the " + rowNo + " row of DATA");
        startDate = null;
        endDate = null;
        boolean htmlFlag = false;
        outputformatsSet = new HashSet<OutputFormat>();
        if(processFlag == ProcessFlag.UPDATE) {
            if(!idValidate(csvRow)) {
                flag = false;
            }
        }else if(processFlag == ProcessFlag.ADD) {
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID))) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CHECK_JOBID,rowNo,Constants.P83_TABLE_COLUMN_FLAG,1,Constants.P83_TABLE_COLUMN_JOBID));
                flag = false;
            }
        }
        
        //ENABLE
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_ENABLE) != -1) {
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_ENABLE))) {
                if(!ArrayUtils.contains(zeroToOne,csvRow.get(Constants.P83_TABLE_COLUMN_ENABLE))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_ENABLE,"0,1"));
                    flag = false;
                }
            }
        }
        
            
        //RESOURCE
        try {
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_RESOURCE) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_RESOURCE));
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_RESOURCE))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_RESOURCE));
                flag = false;
            }else if(csvRow.get(Constants.P83_TABLE_COLUMN_RESOURCE).length() > 250) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_RESOURCE,250));
                flag = false;
            }else if(!ExecuteAPIService.isReportExist(csvRow.get(Constants.P83_TABLE_COLUMN_RESOURCE))) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST,rowNo,Constants.P83_TABLE_COLUMN_RESOURCE));
                flag = false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        
        //LABEL列
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_LABEL) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_LABEL));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_LABEL))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_LABEL));
            flag = false;
        }else if(csvRow.get(Constants.P83_TABLE_COLUMN_LABEL).length() > 100) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_LABEL,100));
            flag = false;
        }
        
        //DESCRIPTION
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_DESCRIPTION) != -1) {
            if(!"".equals(Constants.P83_TABLE_COLUMN_DESCRIPTION)) {
                if(csvRow.get(Constants.P83_TABLE_COLUMN_DESCRIPTION).length() > 2000) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_DESCRIPTION,2000));
                    flag = false;
                }
            }
        }
        
      //TIME_ZONE
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_TIME_ZONE) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_TIME_ZONE));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_TIME_ZONE))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_TIME_ZONE));
            flag = false;
        }else if(TimeZoneEnum.get(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_TIME_ZONE))) == null){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_TIME_ZONE,"0,1,2,3,4,5,6,7"));
            flag = false;
        }
        boolean outputName = false;
        //OUTPUT_NAME
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME));
            flag = false;
        }else if(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME).length() > 100) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME,100));
            flag = false;
        }else {
            outputName = true;
        }
        
        //OUTPUT_DESCRIPTION
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_OUTPUT_DESCRIPTION) != -1) {
            if(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_DESCRIPTION).length() > 250) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_DESCRIPTION,250));
                flag = false;
            }
        }
        
        //OUTPUT_TIME_ZONE
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE));
            flag = false;
        }else if(TimeZoneEnum.get(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE))) == null){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE,"0,1,2,3,4,5,6,7"));
            flag = false;
        }
        
        //IS_SAVE_TO_REPOSITORY
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY));
            flag = false;
        }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY))) 
        {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY,"0,1"));
            flag = false;
        }
        
        //FOLDER_URI
        boolean folderUri = false;
        if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY))) {
            try {
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_FOLDER_URI) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_FOLDER_URI));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_FOLDER_URI));
                    flag = false;
                }else if(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI).length() > 250) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_FOLDER_URI,250));
                    flag = false;
                }else if(!ExecuteAPIService.isFolderExist(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST,rowNo,Constants.P83_TABLE_COLUMN_FOLDER_URI));
                    flag = false;
                }else if(outputName
                        && outputName_FolderUri.containsKey(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME))              // TODO : pan's question: why use contains ? 
                        && outputName_FolderUri.get(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME)).get(1).toLowerCase().contains(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI).toLowerCase())){
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_CHECK_CSV_SAME_OUTPUTNAME,rowNo,outputName_FolderUri.get(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME)).get(0),Constants.P83_TABLE_COLUMN_FOLDER_URI,Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME));
                            flag = false;
                }else if(outputName
                		&& ExecuteAPIService.isOverWriteNotJobExist(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME), csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI), jobId)
                		&& jobId[0] != null && !csvRow.get(Constants.P83_TABLE_COLUMN_JOBID).equals(jobId[0].toString())) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CHECK_SERVER_SAME_OUTPUTNAME,rowNo,jobId[0],Constants.P83_TABLE_COLUMN_FOLDER_URI,Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME));
                    flag = false;
                }else if(processFlag == ProcessFlag.ADD /* @author: 潘 */
                		&& outputName
                		&& outputName_FolderUri_Add.containsKey(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME))
                		&& outputName_FolderUri_Add.get(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME)).get(1).toLowerCase().contains(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI).toLowerCase())) {
                			errorMessages.add(Utils.getString(Constants.P83_OUTPUT_FOLDER_FILE_DUPLICATED,rowNo,outputName_FolderUri_Add.get(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME)).get(0),Constants.P83_TABLE_COLUMN_FOLDER_URI,Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME));		
                			flag = false;
                }else {
                    folderUri = true;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
        }
        
        if(outputName && folderUri && "0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES))) {
            List<String> list = new ArrayList<String>();
            list.add(Utils.allToStr(rowNo));
            list.add(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI));
            outputName_FolderUri.put(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME), list);
        }
        
        /* @author: 潘 */
        if(outputName && folderUri && "1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES))) {
            List<String> list = new ArrayList<String>();
            list.add(Utils.allToStr(rowNo));
            list.add(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_URI));
        	outputName_FolderUri_Add.put(csvRow.get(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME), list);
        }
        
        //OUTPUT_FORMATS
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS));
            flag = false;
        }else {
            try {
                for (String format :  csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS).split(CsvService.FieldSplitter)) {
                    if(outputformatsSet.contains(OutputFormat.valueOf(format))){
                        errorMessages.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE_ELEMENT,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS));
                        flag = false;
                    }else {
                        outputformatsSet.add(OutputFormat.valueOf(format));
                    }
                    
                }
                if(outputformatsSet.contains(OutputFormat.HTML)) {
                    htmlFlag = true;
                }
            } catch (IllegalArgumentException e) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS,formatsInfo));
                flag = false;
            }
        }
        
        //OUTPUT_LOCAL
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL));
            flag = false;
        }else if(LocalEnum.get(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL))) == null){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_TIME_ZONE,"0,1,2,3,4,5,6,7,8,9"));
            flag = false;
        }
        
        //START_TYPE
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_START_TYPE) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_START_TYPE));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_START_TYPE))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_START_TYPE));
            flag = false;
        }else if(!ArrayUtils.contains(oneToTwo, csvRow.get(Constants.P83_TABLE_COLUMN_START_TYPE))) 
        {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_START_TYPE,"1,2"));
            flag = false;
        }

        
        //START_DATE
        if("2".equals(csvRow.get(Constants.P83_TABLE_COLUMN_START_TYPE))) {
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_START_DATE) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_START_TYPE,2,Constants.P83_TABLE_COLUMN_START_DATE));
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_START_DATE))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_START_TYPE,2,Constants.P83_TABLE_COLUMN_START_DATE));
                flag = false;
            }else {
                boolean startDateFlag = true;
                try {
                    startDate = Utils.strToDate(csvRow.get(Constants.P83_TABLE_COLUMN_START_DATE));
                } catch (ParseException e) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_START_DATE));
                    startDateFlag = false;
                    flag = false;
                }
                if(startDateFlag && startDate.before(new Date())) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_START_DATE,rowNo,Constants.P83_TABLE_COLUMN_START_DATE));
                    flag = false;
                }
            }
        }
        
        boolean endDateFlag = false;
        //END_DATE
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_END_DATE) != -1) {
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_END_DATE))) {
                boolean endDataFlagTmp  =true;
                try {
                    endDate = Utils.strToDate(csvRow.get(Constants.P83_TABLE_COLUMN_END_DATE));
                } catch (ParseException e) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_END_DATE));
                    endDataFlagTmp = false;
                    flag = false;
                }
                if(endDataFlagTmp && startDate != null && endDate.before(startDate)) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_END_DATE,rowNo,Constants.P83_TABLE_COLUMN_END_DATE,Constants.P83_TABLE_COLUMN_START_DATE));
                    flag = false;
                }else if(endDataFlagTmp == true){
                    endDateFlag = true;
                }
            }
        }
        
        //CALENDAR_NAME
        try {
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_CALENDAR_NAME) != -1) {
                if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_CALENDAR_NAME))) {
                    if(csvRow.get(Constants.P83_TABLE_COLUMN_CALENDAR_NAME).length() > 50) {
                        errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_CALENDAR_NAME,50));
                        flag = false;
                    }else if(!ExecuteAPIService.isCalendarExist(csvRow.get(Constants.P83_TABLE_COLUMN_CALENDAR_NAME))) {
                        errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST,rowNo,Constants.P83_TABLE_COLUMN_CALENDAR_NAME));
                        flag = false;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
        
        //JOB_TRIGGER
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE));
            flag = false;
        }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE))) 
        {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,"0,1"));
            flag = false;
        }
        
        if("0".equals(csvRow.get(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE))){
            boolean countFlag = true;
            //OCCURRENCE_COUNT
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,0,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT));
                countFlag = false;
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,0,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT));
                countFlag = false;
                flag = false;
            }else if(!pattern.matcher(csvRow.get(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT)).matches())
            {
                if(!"-1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT))) 
                {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT));
                    countFlag = false;
                    flag = false;
                }
            }else if(csvRow.get(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT).length() > 11) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,11));
                flag = false;
            }else if(endDateFlag && !"-1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT))) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_DATE_COUNT_VALIDATE,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,0,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,-1,Constants.P83_TABLE_COLUMN_END_DATE));
                flag = false;
            }
            
            String[] str =new String[] {"0","1","2","3"};
            
            if(countFlag&&!"1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT))){
                //RECURRENCE_INTERVAL
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_INPUT,rowNo,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,0,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,1,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_INPUT,rowNo,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,0,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,1,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL));
                    flag = false;
                }else if(!pattern.matcher(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL)).matches()) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL));
                    flag = false;
                }else if(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL).length() > 11) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL,11));
                    flag = false;
                }
                
                //RECURRENCE_INTERVAL_UNIT
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_INPUT,rowNo,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,0,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,1,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_INPUT,rowNo,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,0,Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT,1,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT));
                    flag = false;
                }else if(!ArrayUtils.contains(str,csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT,"0,1,2,3,"));
                    flag = false;
                }
            }else {
                //RECURRENCE_INTERVAL
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL))){
                        if(!pattern.matcher(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL)).matches()) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL));
                            flag = false;
                        }else if(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL).length() > 11) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL,11));
                            flag = false;
                        }
                    }
                } 
                //RECURRENCE_INTERVAL_UNIT
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT))){
                        if(!ArrayUtils.contains(str,csvRow.get(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT))) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT,"1,2,3,4"));
                            flag = false;
                        }
                    }
                }
            }
            
        }
        
        if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE))){
            //MINUTES
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_MINUTES) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_MINUTES));
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_MINUTES))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_MINUTES));
                flag = false;
            }else if(!minutesValidate(csvRow)) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_MINUTES));
                flag = false;
            }else if(csvRow.get(Constants.P83_TABLE_COLUMN_MINUTES).length() > 200) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_MINUTES,200));
                flag = false;
            }
            //HOURS
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_HOURS) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_HOURS));
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_HOURS))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_HOURS));
                flag = false;
            }else if(!hoursValidate(csvRow)) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_HOURS));
                flag = false;
            }else if(csvRow.get(Constants.P83_TABLE_COLUMN_HOURS).length() > 80) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_HOURS,80));
                flag = false;
            }
            
            //DAYS_TYPE
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_DAYS_TYPE) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_DAYS_TYPE));
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_DAYS_TYPE))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_DAYS_TYPE));
                flag = false;
            }else if(!ArrayUtils.contains(zeroToTwo,csvRow.get(Constants.P83_TABLE_COLUMN_DAYS_TYPE))) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_DAYS_TYPE,"0,1,2"));
                flag = false;
            }
            
            if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_DAYS_TYPE))) {
                //WEEK_DAYS
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_WEEK_DAYS) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_DAYS_TYPE,1,Constants.P83_TABLE_COLUMN_WEEK_DAYS));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_WEEK_DAYS))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_DAYS_TYPE,1,Constants.P83_TABLE_COLUMN_WEEK_DAYS));
                    flag = false;
                }else if(!weekDaysValidate(csvRow)) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_WEEK_DAYS));
                    flag = false;
                }
            }
            if("2".equals(csvRow.get(Constants.P83_TABLE_COLUMN_DAYS_TYPE))) {
                //MONTH_DAYS
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_MONTH_DAYS) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_DAYS_TYPE,2,Constants.P83_TABLE_COLUMN_MONTH_DAYS));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_MONTH_DAYS))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_DAYS_TYPE,2,Constants.P83_TABLE_COLUMN_MONTH_DAYS));
                    flag = false;
                }else if(!monthDaysValidate(csvRow)) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_MONTH_DAYS));
                    flag = false;
                }
            }
            //MONTHS
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_MONTHS) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_MONTHS));
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_MONTHS))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE,1,Constants.P83_TABLE_COLUMN_MONTHS));
                flag = false;
            }else if(!monthsValidate(csvRow)) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_MONTHS));
                flag = false;
            }
        }

        //IS_OVER_WRITE_FILES
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES));
            flag = false;
        }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES))) 
        {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES,"0,1"));
            flag = false;
        }
        
        //IS_SEQUENTIAL_FILENAME
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES) == -1) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES));
            flag = false;
        }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES))){
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES));
            flag = false;
        }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES))) 
        {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES,"0,1"));
            flag = false;
        }
        
        //TIMESTAMP_PATTERN
        if("1".equals(csvRow.get(Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES))) {
            if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN) == -1) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES,1,Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN));
                flag = false;
            }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN))){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK,rowNo,Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES,1,Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN));
                flag = false;
            }else if(!Utils.isTimestampPattern(csvRow.get(Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN))) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN));
                flag = false;
            }
        }
        
        
        //SERVER_NAME
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_SERVER_NAME) != -1) {
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SERVER_NAME))){
                if(csvRow.get(Constants.P83_TABLE_COLUMN_SERVER_NAME).length() > 150) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_SERVER_NAME,150));
                    flag = false;
                }
                //FTP_TYPE
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_FTP_TYPE) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SERVER_NAME,"null",Constants.P83_TABLE_COLUMN_FTP_TYPE));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_FTP_TYPE))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SERVER_NAME,"null",Constants.P83_TABLE_COLUMN_FTP_TYPE));
                    flag = false;
                }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_FTP_TYPE))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_FTP_TYPE,"0,1"));
                    flag = false;
                }
                    
                //PORT
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_PORT) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SERVER_NAME,"null",Constants.P83_TABLE_COLUMN_PORT));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_PORT))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SERVER_NAME,"null",Constants.P83_TABLE_COLUMN_PORT));
                    flag = false;
                }else if(!pattern.matcher(csvRow.get(Constants.P83_TABLE_COLUMN_PORT)).matches()) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_PORT));
                    flag = false;
                }else if(Integer.parseInt(csvRow.get(Constants.P83_TABLE_COLUMN_PORT)) > 65535) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_PORT,"1~65535"));
                    flag = false;
                }
                
                //FOLDER_PATH
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_FOLDER_PATH) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_PATH))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_FOLDER_PATH).length() > 250) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_FOLDER_PATH,250));
                            flag = false;
                        }
                    }
                }
                
                //USERNAME
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_USERNAME) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_USERNAME))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_USERNAME).length() > 50) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_USERNAME,50));
                            flag = false;
                        }
                    }
                }
                
                //PASSWORD
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_PASSWORD) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_PASSWORD))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_PASSWORD).length() > 250) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_PASSWORD,250));
                            flag = false;
                        }
                    }
                }
            }
        }
        
        //SUBJECT
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_SUBJECT) != -1) {
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT))){
                if(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT).length() > 100) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT,100));
                    flag = false;
                }
                //TO
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_TO_ADDRESSES) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT,"null",Constants.P83_TABLE_COLUMN_TO_ADDRESSES));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT,"null",Constants.P83_TABLE_COLUMN_TO_ADDRESSES));
                    flag = false;
                }else if(csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES).length() > 100) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_TO_ADDRESSES,100));
                    flag = false;
                }else if(!mailValidate(csvRow,csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_TO_ADDRESSES));
                    flag = false;
                }
                
                //CC
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_CC_ADDRESSES) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_CC_ADDRESSES))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_CC_ADDRESSES).length() > 100) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_CC_ADDRESSES,100));
                            flag = false;
                        }else if(!mailValidate(csvRow,csvRow.get(Constants.P83_TABLE_COLUMN_CC_ADDRESSES))) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_CC_ADDRESSES));
                            flag = false;
                        }
                    }
                }
                
                //BCC
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_BCC_ADDRESSES) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_BCC_ADDRESSES))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_BCC_ADDRESSES).length() > 100) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_BCC_ADDRESSES,100));
                            flag = false;
                        }else if(!mailValidate(csvRow,csvRow.get(Constants.P83_TABLE_COLUMN_BCC_ADDRESSES))) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_BCC_ADDRESSES));
                            flag = false;
                        }
                    }
                }
                
                //MESSAGES
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_MESSAGES) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGES))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGES).length() > 2000) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_MESSAGES,2000));
                            flag = false;
                        }
                    }
                }
                
                //SEND_TYPE
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_SEND_TYPE) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT,"null",Constants.P83_TABLE_COLUMN_SEND_TYPE));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SEND_TYPE))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT,"null",Constants.P83_TABLE_COLUMN_SEND_TYPE));
                    flag = false;
                }else if(!ArrayUtils.contains(sendType, csvRow.get(Constants.P83_TABLE_COLUMN_SEND_TYPE))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_PORT,"0,1,2,3,4,5"));
                    flag = false;
                }else if(htmlFlag ==false 
                        && ("3".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SEND_TYPE))
                        ||"5".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SEND_TYPE)))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_HTML,rowNo,Constants.P83_TABLE_COLUMN_SEND_TYPE,"3,5",Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS,"HTML"));
                    flag = false;
                }
                
                //SKIP_EMPTY
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_SKIP_EMPTY) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT,"null",Constants.P83_TABLE_COLUMN_SKIP_EMPTY));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SKIP_EMPTY))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT,"null",Constants.P83_TABLE_COLUMN_SKIP_EMPTY));
                    flag = false;
                }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_SKIP_EMPTY))) 
                {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_SKIP_EMPTY,"0,1"));
                    flag = false;
                }
            }
        }
        
        //SUBJECT_ALERT
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_SUBJECT_ALERT) != -1) {
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT_ALERT))){
                if(csvRow.get(Constants.P83_TABLE_COLUMN_SUBJECT_ALERT).length() > 100) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,100));
                    flag = false;
                }
                //TO_ALERT
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT));
                    flag = false;
                }else if(csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT).length() > 100) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT,100));
                    flag = false;
                }else if(!mailValidate(csvRow,csvRow.get(Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT))) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT));
                    flag = false;
                }
                //JOB_STATE
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_JOB_STATE) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_JOB_STATE));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_JOB_STATE))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_JOB_STATE));
                    flag = false;
                }else if(!ArrayUtils.contains(jobState, csvRow.get(Constants.P83_TABLE_COLUMN_JOB_STATE))) 
                {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_JOB_STATE,"0,1,2,3"));
                    flag = false;
                }
                
              //MESSAGES
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT).length() > 2000) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_MESSAGE_TEXT,2000));
                            flag = false;
                        }
                    }
                }
                
              //MESSAGES_FAILS
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT_FAIL) != -1) {
                    if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT_FAIL))){
                        if(csvRow.get(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT_FAIL).length() > 2000) {
                            errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_MESSAGE_TEXT_FAIL,2000));
                            flag = false;
                        }
                    }
                }
                
                //INCLUDING_REPORT_JOB_INFO
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO));
                    flag = false;
                }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO))) 
                {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO,"0,1"));
                    flag = false;
                }
                
              //INCLUDING_STACK_TRACE
                if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE) == -1) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE));
                    flag = false;
                }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE))){
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_CONDITION_CHECK_NOT_BLANK,rowNo,Constants.P83_TABLE_COLUMN_SUBJECT_ALERT,"null",Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE));
                    flag = false;
                }else if(!ArrayUtils.contains(zeroToOne, csvRow.get(Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE))) 
                {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_ILLEGAL_VALUE,rowNo,Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE,"0,1"));
                    flag = false;
                }
                
            }
        }
        //PARAMETERS     
        if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_PARAMETERS) != -1){
            if(!"".equals(csvRow.get(Constants.P83_TABLE_COLUMN_PARAMETERS))) {
                if(!parameterValidate(csvRow)) {
                    errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_PARAMETERS));
                    flag = false;
                }
            }
        }
        logger.debug("add_UpdateValidate ended");
        return flag;
    }
    
    
    private boolean parameterValidate(CsvRow csvRow) {
        for (String string : csvRow.get(Constants.P83_TABLE_COLUMN_PARAMETERS).split(";")) {
            if(!string.contains("=")) {
                return false;
            }else if(string.length()-string.replaceAll("=", "").length() > 1){
                return false;
            }else if("".equals(string.substring(0, string.indexOf("=")))) {
                return false;
            }else if("".equals(string.substring(string.indexOf("=")+1))) {
                return false;
            }else if(string.substring(0, string.indexOf("=")).length() > 100) {
                return false;
            }
        }
        return true;
    }
    
    private boolean mailValidate(CsvRow csvRow,String mail) {
    	EMailFormatCheckService service = new EMailFormatCheckService();
        for (String str : mail.split(CsvService.FieldSplitter)) {
        	service.setEmail(str);
            if(!service.forSchedule()) {
                return false;
            }
        }
        return true;
    }

    private boolean monthsValidate(CsvRow csvRow) {
        List<String> list = new ArrayList<String>();
        for (String str : csvRow.get(Constants.P83_TABLE_COLUMN_MONTHS).split(CsvService.FieldSplitter)) {
            if(list.contains(str)) {
                return false;
            }else {
                list.add(str);
            }
            if(!ArrayUtils.contains(months, str)) {
                return false;
            }
        }
        return true;
    }

    private boolean monthDaysValidate(CsvRow csvRow) {
        for (String str : csvRow.get(Constants.P83_TABLE_COLUMN_MONTH_DAYS).split(CsvService.FieldSplitter)) {
            if(str.contains("-")&&str.length()-str.replaceAll("-", "").length() == 1) {
                for (int i = 0;i < str.split("-").length;i++) {
                    if(!pattern.matcher(str.split("-")[i]).matches()) {
                            return false;
                    }
                    if(Integer.parseInt(str.split("-")[1]) >= 32) {
                        return false;
                    }
                    if(i == 1) {
                        if(Integer.parseInt(str.split("-")[1]) <= Integer.parseInt(str.split("-")[0])) {
                            return false;
                        }
                    }
                }
            }else if(!pattern.matcher(str).matches()) {
                          return false;
                  }else if(Integer.parseInt(str) >=32) {
                            return false;
                  }
        }
        return true;
    }

    private boolean weekDaysValidate(CsvRow csvRow) {
        List<String> list = new ArrayList<String>();
        for (String str : csvRow.get(Constants.P83_TABLE_COLUMN_WEEK_DAYS).split(CsvService.FieldSplitter)) {
            if(list.contains(str)) {
                return false;
            }else {
                list.add(str);
            }
            if(!ArrayUtils.contains(weekDays, str)) {
                return false;
            }
        }
        return true;
    }

    private boolean minutesValidate(CsvRow csvRow) {
        for (String str : csvRow.get(Constants.P83_TABLE_COLUMN_MINUTES).split(CsvService.FieldSplitter)) {
            if(!pattern.matcher(str).matches()) {
                if(!"0".equals(str)){
                    return false;
                }
            }else if(Integer.parseInt(str) >=60) {
                return false;
            }
        }
        return true;
    }
    
    private boolean hoursValidate(CsvRow csvRow) {
        for (String str : csvRow.get(Constants.P83_TABLE_COLUMN_HOURS).split(CsvService.FieldSplitter)) {
            if(str.contains("-")&&str.length()-str.replaceAll("-", "").length() == 1) {
                for (int i = 0;i < str.split("-").length;i++) {
                    if(!pattern.matcher(str.split("-")[i]).matches()) {
                        if(!"0".equals(str.split("-")[i])){
                            return false;
                        }
                    }
                    if(Integer.parseInt(str.split("-")[1]) >= 24) {
                        return false;
                    }
                    if(i == 1) {
                        if(Integer.parseInt(str.split("-")[1]) <= Integer.parseInt(str.split("-")[0])) {
                            return false;
                        }
                    }
                }
            }else if(!pattern.matcher(str).matches()) {
                      if(!"0".equals(str)){
                          return false;
                      }
                  }else if(Integer.parseInt(str) >=24) {
                            return false;
                  }
        }
        return true;
    }
    
    
    

    private boolean idValidate(CsvRow csvRow) {
        logger.debug("idValidate started");
        boolean flag = true;
        int rowNo = csvRow.getRowNo();
        logger.debug("Start checking the " + rowNo + " row of ID");
        
	    if(csvRow.getIndex(Constants.P83_TABLE_COLUMN_JOBID) == -1) {
	        errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_JOBID));
	        flag = false;
	    }else if("".equals(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID))) {
	        errorMessages.add(Utils.getString(Constants.DATA_ERROR_EMPTY,rowNo,Constants.P83_TABLE_COLUMN_JOBID));
	        flag = false;
	    }else if(!pattern.matcher(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID)).matches()) {
            errorMessages.add(Utils.getString(Constants.DATA_ERROR_FORMAT_INCORRECT,rowNo,Constants.P83_TABLE_COLUMN_JOBID));
            flag = false;
	    }else if(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID).length() > 20) {
	        errorMessages.add(Utils.getString(Constants.DATA_ERROR_OVERSIZE,rowNo,Constants.P83_TABLE_COLUMN_JOBID,20));
            flag = false;
	    }
	    
        if(flag == true) {
            long id = Long.parseLong(csvRow.get(Constants.P83_TABLE_COLUMN_JOBID));
            if(duplicateIdList.contains(id)) {
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_DUPLICATE,rowNo,Constants.P83_TABLE_COLUMN_JOBID));
                flag = false;
            }else if(!serverList.contains(id)){
                errorMessages.add(Utils.getString(Constants.DATA_ERROR_NOT_EXIST,rowNo,Constants.P83_TABLE_COLUMN_JOBID));
                flag = false;
            }
        }
        logger.debug("idValidate ended");
        return flag;
    }
    

    /*
	 * 【CSVエクスポート】　押下
	 */
    protected boolean csvExportWork(File saveFile){
        logger.debug("Export: csvExportWork started");
	    if(!loadData()) {
	        return false;
	    }
	    logger.debug("Export: Converting data from software to csv file.");
	    List<List<String>> saveSchedule = getBackUpFile();
	    logger.debug("Export: Saving csv file.");
        if (!saveCsv(saveFile, saveSchedule)) {
            logger.debug("Export: Failed to save csv file.");
            return false;
        }
        showInfo(Utils.getString(Constants.DLG_INFO_EXPORT_SUCC));
        logger.info("Export: Successed to export file.");
        logger.debug("csvExportWork ended");
        return true;
	}
    
    @SuppressWarnings("unchecked")
    private List<List<String>> getBackUpFile(){
        List<List<String>> saveSchedule = new ArrayList<List<String>>();
        List<String> header = new ArrayList<String>();
        header.add(Constants.P83_TABLE_COLUMN_FLAG);
        header.add(Constants.P83_TABLE_COLUMN_ENABLE);
        header.add(Constants.P83_TABLE_COLUMN_RESOURCE);
        header.add(Constants.P83_TABLE_COLUMN_LABEL);
        header.add(Constants.P83_TABLE_COLUMN_JOBID);
        header.add(Constants.P83_TABLE_COLUMN_OWNER);
        header.add(Constants.P83_TABLE_COLUMN_STATE);
        header.add(Constants.P83_TABLE_COLUMN_LAST_EXECUTION);
        header.add(Constants.P83_TABLE_COLUMN_NEXT_EXECUTION);
        header.add(Constants.P83_TABLE_COLUMN_DESCRIPTION);
        header.add(Constants.P83_TABLE_COLUMN_TIME_ZONE);
        header.add(Constants.P83_TABLE_COLUMN_BASE_OUTPUT_NAME);
        header.add(Constants.P83_TABLE_COLUMN_OUTPUT_DESCRIPTION);
        header.add(Constants.P83_TABLE_COLUMN_OUTPUT_TIME_ZONE);
        header.add(Constants.P83_TABLE_COLUMN_IS_SAVE_TO_REPOSITORY);
        header.add(Constants.P83_TABLE_COLUMN_FOLDER_URI);
        header.add(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL_FOLDER_URI);
        header.add(Constants.P83_TABLE_COLUMN_OUTPUT_FORMATS);
        header.add(Constants.P83_TABLE_COLUMN_OUTPUT_LOCAL);
        header.add(Constants.P83_TABLE_COLUMN_START_TYPE);
        header.add(Constants.P83_TABLE_COLUMN_START_DATE);
        header.add(Constants.P83_TABLE_COLUMN_END_DATE);
        header.add(Constants.P83_TABLE_COLUMN_CALENDAR_NAME);
        header.add(Constants.P83_TABLE_COLUMN_JOB_TRIGGER_TYPE);
        header.add(Constants.P83_TABLE_COLUMN_OCCURRENCE_COUNT);
        header.add(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL);
        header.add(Constants.P83_TABLE_COLUMN_RECURRENCE_INTERVAL_UNIT);
        header.add(Constants.P83_TABLE_COLUMN_MINUTES);
        header.add(Constants.P83_TABLE_COLUMN_HOURS);
        header.add(Constants.P83_TABLE_COLUMN_DAYS_TYPE);
        header.add(Constants.P83_TABLE_COLUMN_WEEK_DAYS);
        header.add(Constants.P83_TABLE_COLUMN_MONTH_DAYS);
        header.add(Constants.P83_TABLE_COLUMN_MONTHS);
        header.add(Constants.P83_TABLE_COLUMN_IS_OVERWRITE_FILES);
        header.add(Constants.P83_TABLE_COLUMN_IS_SEQUENTIAL_FILENAMES);
        header.add(Constants.P83_TABLE_COLUMN_TIMESTAMP_PATTERN);
        header.add(Constants.P83_TABLE_COLUMN_SERVER_NAME);
        header.add(Constants.P83_TABLE_COLUMN_FTP_TYPE);
        header.add(Constants.P83_TABLE_COLUMN_PORT);
        header.add(Constants.P83_TABLE_COLUMN_FOLDER_PATH);
        header.add(Constants.P83_TABLE_COLUMN_USERNAME);
        header.add(Constants.P83_TABLE_COLUMN_PASSWORD);
        header.add(Constants.P83_TABLE_COLUMN_SUBJECT);
        header.add(Constants.P83_TABLE_COLUMN_TO_ADDRESSES);
        header.add(Constants.P83_TABLE_COLUMN_CC_ADDRESSES);
        header.add(Constants.P83_TABLE_COLUMN_BCC_ADDRESSES);
        header.add(Constants.P83_TABLE_COLUMN_MESSAGES);
        header.add(Constants.P83_TABLE_COLUMN_SEND_TYPE);
        header.add(Constants.P83_TABLE_COLUMN_SKIP_EMPTY);
        header.add(Constants.P83_TABLE_COLUMN_SUBJECT_ALERT);
        header.add(Constants.P83_TABLE_COLUMN_TO_ADDRESSES_ALERT);
        header.add(Constants.P83_TABLE_COLUMN_JOB_STATE);
        header.add(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT);
        header.add(Constants.P83_TABLE_COLUMN_MESSAGE_TEXT_FAIL);
        header.add(Constants.P83_TABLE_COLUMN_INCLUDING_REPORT_JOB_INFO);
        header.add(Constants.P83_TABLE_COLUMN_INCLUDING_STACK_TRACE);
        header.add(Constants.P83_TABLE_COLUMN_PARAMETERS);
        
        saveSchedule.add(header);
        for(com.jaspersoft.jasperserver.jaxrs.client.dto.jobs.JobSummary js : list) {
            List<String> singleRow = new ArrayList<String>();
            try {
                job = ExecuteAPIService.getJobById(js.getId().longValue());
            } catch (Exception e) {
                logger.error(e.getMessage(),e);
            }
            if(job == null) {
                continue;
            }
            
            rd = job.getRepositoryDestination();
            source = job.getSource();
            fi = rd.getOutputFTPInfo();
            mail = job.getMailNotification();
            ja = job.getAlert();
            if("SimpleTrigger".equalsIgnoreCase(job.getTrigger().getClass().getSimpleName())) {
                st = (SimpleTrigger) job.getTrigger();
                ct = null;
            }else if("CalendarTrigger".equalsIgnoreCase(job.getTrigger().getClass().getSimpleName())) {
                ct = (CalendarTrigger) job.getTrigger();
                st = null;
            }
            Set<OutputFormat> set = job.getOutputFormats();
            String strFormatSet = StringUtils.join(set.toArray(), CsvService.FieldSplitter);
            String toAddresses = "";
            String ccAddresses = "";
            String bccAddresses = "";
            String toAddresses_alert = "";
            String parameters = "";
            if(mail != null) {
                if(mail.getToAddresses().size()>0) {
                    for(int i = 0;i < mail.getToAddresses().size();i++) {
                        if(i == 0) {
                            toAddresses += mail.getToAddresses().get(i);
                        }else {
                            toAddresses += ";" + mail.getToAddresses().get(i);
                        }
                    }
                }
                if(mail.getCcAddresses().size()>0) {
                    for(int i = 0;i < mail.getCcAddresses().size();i++) {
                        if(i == 0) {
                            ccAddresses += mail.getCcAddresses().get(i);
                        }else {
                            ccAddresses += ";" + mail.getCcAddresses().get(i);
                        }
                    }
                }
                if(mail.getBccAddresses().size()>0) {
                    for(int i = 0;i < mail.getBccAddresses().size();i++) {
                        if(i == 0) {
                            bccAddresses += mail.getBccAddresses().get(i);
                        }else {
                            bccAddresses += ";" + mail.getBccAddresses().get(i);
                        }
                    }
                }
            }
            if(ja !=null && ja.getToAddresses().size()>0) {
                for(int i = 0;i < ja.getToAddresses().size();i++) {
                    if(i == 0) {
                        toAddresses_alert += ja.getToAddresses().get(i);
                    }else {
                        toAddresses_alert += ";" + ja.getToAddresses().get(i);
                    }
                }
            }
            
            singleRow.add("");
            if(js.getState().getValue() == JobStateType.PAUSED) {
                singleRow.add("0");
            }else {
                singleRow.add("1");
            }
            singleRow.add(Utils.allToStr(js.getReportUnitURI()));
            singleRow.add(Utils.allToStr(js.getLabel()));
            singleRow.add(Utils.allToStr(job.getId().toString()));
            singleRow.add(Utils.allToStr(js.getOwner()));
            singleRow.add(Utils.allToStr(js.getState().getValue().toString()));
            singleRow.add(Utils.dateToStr(js.getState().getPreviousFireTime()));
            singleRow.add(Utils.dateToStr(js.getState().getNextFireTime()));
            singleRow.add(Utils.allToStr(job.getDescription()));
            if(TimeZoneEnum.getByName(job.getTrigger().getTimezone()) != null) {
                singleRow.add(Utils.allToStr(TimeZoneEnum.getByName(job.getTrigger().getTimezone()).getValue()));
            }else {
                singleRow.add("0");
            }
            singleRow.add(Utils.allToStr(job.getBaseOutputFilename()));
            singleRow.add(Utils.allToStr(job.getRepositoryDestination().getOutputDescription()));
            if(TimeZoneEnum.getByName(job.getOutputTimeZone()) != null){
                singleRow.add(Utils.allToStr(TimeZoneEnum.getByName(job.getOutputTimeZone()).getValue()));
            }else {
                singleRow.add("0");
            }
            if(rd.isSaveToRepository()) {
                singleRow.add("1");
            }else {
                singleRow.add("0");
            }
            if(job.getRepositoryDestination().getFolderURI() != null) {
                singleRow.add(Utils.allToStr(job.getRepositoryDestination().getFolderURI()));
            }else {
                singleRow.add("");
            }
            if(job.getRepositoryDestination().getOutputLocalFolder() != null){
                singleRow.add(Utils.allToStr(job.getRepositoryDestination().getOutputLocalFolder()));
            }else {
                singleRow.add("");
            }
            singleRow.add(strFormatSet);
            if(job.getOutputLocale() == null) {
                singleRow.add(Utils.allToStr(LocalEnum.DEFAULST.getValue()));
            }else {
                singleRow.add(Utils.allToStr(LocalEnum.getByName(job.getOutputLocale()).getValue()));
            }
            singleRow.add(Utils.allToStr(job.getTrigger().getStartType()));
            singleRow.add(Utils.dateToStr(job.getTrigger().getStartDate()));
            singleRow.add(Utils.dateToStr(job.getTrigger().getEndDate()));
            singleRow.add(Utils.allToStr(job.getTrigger().getCalendarName()));
            
            if(st != null) {
                singleRow.add("0");
                singleRow.add(Utils.allToStr(st.getOccurrenceCount()));
                singleRow.add(Utils.allToStr(st.getRecurrenceInterval()));
                if(st.getRecurrenceIntervalUnit() != null) {
                    singleRow.add(Utils.allToStr(st.getRecurrenceIntervalUnit().ordinal()));
                }else {
                    singleRow.add("");
                }
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
            }else if(ct != null) {
                SortedSet<Byte> sortedSet = ct.getMonths();
                String strSortedSet = StringUtils.join(sortedSet.toArray(), CsvService.FieldSplitter);
                singleRow.add("1");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add(Utils.allToStr(ct.getMinutes().replace(",", CsvService.FieldSplitter)));
                singleRow.add(Utils.allToStr(ct.getHours().replace(",", CsvService.FieldSplitter)));
                singleRow.add(Utils.allToStr(ct.getDaysType().ordinal()));
                if(ct.getDaysType() == CalendarDaysType.ALL) {
                    singleRow.add("");
                    singleRow.add("");
                }else if(ct.getDaysType() == CalendarDaysType.WEEK) {
                    String weekDays = "";
                    int j = 0;
                    for (Byte b : ct.getWeekDays()) {
                        if(j == 0) {
                            weekDays += b;
                        }else {
                            weekDays += ";" + b;
                        }
                        j++;
                    }
                    singleRow.add(Utils.allToStr(weekDays));
                    singleRow.add("");
                }else if(ct.getDaysType() == CalendarDaysType.MONTH) {
                    singleRow.add("");
                    singleRow.add(Utils.allToStr(ct.getMonthDays()).replace(",", CsvService.FieldSplitter));
                }
                singleRow.add(Utils.allToStr(strSortedSet));
            }
            
            if(rd.isOverwriteFiles()) {
                singleRow.add("1");
            }else {
                singleRow.add("0");
            }
            
            if(rd.isSequentialFilenames()) {
                singleRow.add("1");
            }else {
                singleRow.add("0");
            }
            singleRow.add(Utils.allToStr(rd.getTimestampPattern()));
            
            if(!"".equals(Utils.allToStr(fi.getServerName()))) {
                singleRow.add(Utils.allToStr(fi.getServerName()));
                if(fi.getType() == FtpType.ftp) {
                    singleRow.add("0");
                }else if(fi.getType() == FtpType.ftps) {
                    singleRow.add("1");
                }
                singleRow.add(Utils.allToStr(fi.getPort()));
                singleRow.add(Utils.allToStr(fi.getFolderPath()));
                singleRow.add(Utils.allToStr(fi.getUserName()));
                singleRow.add(Utils.allToStr(fi.getPassword()));
            }else {
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
            }
            
                
            
            if(mail == null) {
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
            }else {
                singleRow.add(Utils.allToStr(mail.getSubject()));
                singleRow.add(Utils.allToStr(toAddresses));
                singleRow.add(Utils.allToStr(ccAddresses));
                singleRow.add(Utils.allToStr(bccAddresses));
                singleRow.add(Utils.allToStr(mail.getMessageText()));
                singleRow.add(Utils.allToStr(mail.getResultSendType().ordinal()));
                if(mail.isSkipEmptyReports()) {
                    singleRow.add("1");
                }else {
                    singleRow.add("0");
                }
            }
            
            
            if(ja == null) {
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
                singleRow.add("");
            }else {
                singleRow.add(Utils.allToStr(ja.getSubject()));
                singleRow.add(Utils.allToStr(toAddresses_alert));
                singleRow.add(Utils.allToStr(ja.getJobState().ordinal()));
                singleRow.add(Utils.allToStr(ja.getMessageText()));
                singleRow.add(Utils.allToStr(ja.getMessageTextWhenJobFails()));
                if(ja.isIncludingReportJobInfo()) {
                    singleRow.add("1");
                }else {
                    singleRow.add("0");
                }
                if(ja.isIncludingStackTrace()) {
                    singleRow.add("1");
                }else {
                    singleRow.add("0");
                }
            }
            if(source != null && source.getParameters() != null && source.getParameters().size() > 0) {
                for (Entry<String, Object> entry : source.getParameters().entrySet()) {
                    String tmp = "";
                    ArrayList<String> list ;
                    if("ArrayList".equalsIgnoreCase(entry.getValue().getClass().getSimpleName())) {
                        list = (ArrayList<String>) entry.getValue();
                        tmp = String.join("/", (String[]) list.toArray(new String[0]));
                    }
                    parameters += entry.getKey() + "=" + tmp + ";";
                }
            }
            singleRow.add(Utils.allToStr(parameters));
            saveSchedule.add(singleRow);
        }
        return saveSchedule;
    }
    
    

    /*
	 * 【取得】　押下
	 */
    protected boolean getWork() {
        loadData();
        setImportFlag(false);
	    return true;
	}
	
	/*
	 * 【適用】　押下
	 */
	protected boolean applyWork() {
	    logger.debug("Apply: Saving backup file.");
        List<List<String>> saveFile = getBackUpFile();
        if (!saveBackup(saveFile)) {
            return false;
        }
        logger.debug("Apply: Applying data to server.");
        try {
            if(delIdList.size() > 0) {
                ExecuteAPIService.delJobById(delIdList);
            }
            if(updateJobList.size() > 0) {
                ExecuteAPIService.updateJob(updateJobList);
            }
            if(addJobList.size() > 0) {
                ExecuteAPIService.scheduleReport(addJobList);
            }
            if(disableIdList.size() > 0) {
                ExecuteAPIService.disableJobById(disableIdList);
            }
            if(enableIdList.size() > 0) {
                ExecuteAPIService.enableJobById(enableIdList);
            }
            setImportFlag(false);
            if(!loadData()) {
                return false;
            }
            showInfo(Utils.getString(Constants.DLG_INFO_APPLY_SUCC));
            logger.info("Apply: Successed to apply data to server.");
            logger.debug("applyWork ended");
        } catch (Exception e) {
            schedules.clear();
            errorMessages.clear();
            logger.error("Apply: Failed to apply data to server.");
            logger.error(e.getMessage(), e);
            showAPIException(Utils.getString(Constants.SEREVER_ERROR_APPLY), e);
            return false;
        }
        return true;
    }
}
