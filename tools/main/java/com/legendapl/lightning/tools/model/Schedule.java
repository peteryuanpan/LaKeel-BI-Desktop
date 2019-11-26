package com.legendapl.lightning.tools.model;

import javax.xml.bind.annotation.XmlElement;

import javafx.beans.property.SimpleStringProperty;

public class Schedule extends BaseModel<Schedule> implements Comparable<Schedule>{
    
    private final StringBase resource = new StringBase("");
    private final StringBase jobname = new StringBase("");
    private final StringBase jobid = new StringBase("");
    private final StringBase owner = new StringBase("");
    private final StringBase state = new StringBase("");
    private final StringBase last_execution = new StringBase("");
    private final StringBase next_execution = new StringBase("");
    private final StringBase status = new StringBase("");

    
    public Schedule() {
        super();
    }
    
    public Schedule(String resource, String jobname, String jobid, String owner, String state, String last_execution, String next_execution, String status) {
        setResource(resource);
        setJobname(jobname);
        setJobid(jobid);
        setOwner(owner);
        setState(state);
        setLast_execution(last_execution);
        setNext_execution(next_execution);
        setStatus(status);
    }
    
    public Schedule(String resource, String jobname, String jobid, String owner, String state, String last_execution, String next_execution, String status, ProcessFlag flag) {
        setResource(resource);
        setJobname(jobname);
        setJobid(jobid);
        setOwner(owner);
        setState(state);
        setLast_execution(last_execution);
        setNext_execution(next_execution);
        setStatus(status);
        this.flag = flag;
    }
    
    @Override
    public boolean equals(Object scheduleCmp) {
        Schedule schedule = (Schedule) scheduleCmp;
        if(!this.jobid.get().isEmpty() && this.jobid.get().equals(schedule.getJobid().get()) && this.jobname.get().equals(schedule.getJobname().get()))
            return true;
        else if(this.jobid.get().isEmpty()) {
            return this.jobname.get().equals(schedule.getJobname().get());
        }
            return false;
    }
    
    @Override
    public int compareTo(Schedule schedule) {
        int res = jobname.get().compareTo( schedule.getJobname().get() );
        if (res == 0) {
            return jobid.get().compareTo( schedule.getJobid().get() );
        }
        else {
            return res;
        }
    }

    @XmlElement(name = "resource")
    public SimpleStringProperty getResource() {
        return resource;
    }
    
    
    public void setResource(String resourceStr) {
        resource.set(resourceStr);
    }

    @XmlElement(name = "jobname")
    public SimpleStringProperty getJobname() {
        return jobname;
    }

    
    public void setJobname(String jobnameStr) {
        jobname.set(jobnameStr);
    }
    
    @XmlElement(name = "jobid")
    public SimpleStringProperty getJobid() {
        return jobid;
    }
    
    
    public void setJobid(String jobidStr) {
        jobid.set(jobidStr);
    }

    @XmlElement(name = "owner")
    public SimpleStringProperty getOwner() {
        return owner;
    }
    
    
    public void setOwner(String ownerStr) {
        owner.set(ownerStr);
    }

    @XmlElement(name = "state")
    public SimpleStringProperty getState() {
        return state;
    }
    
    
    public void setState(String stateStr) {
        state.set(stateStr);
    }

    @XmlElement(name = "last_execution")
    public SimpleStringProperty getLast_execution() {
        return last_execution;
    }
    
    
    public void setLast_execution(String last_executionStr) {
        last_execution.set(last_executionStr);
    }

    @XmlElement(name = "next_execution")
    public SimpleStringProperty getNext_execution() {
        return next_execution;
    }
    
    
    public void setNext_execution(String next_executionStr) {
        next_execution.set(next_executionStr);
    }
    
    @XmlElement(name = "status")
    public SimpleStringProperty getStatus() {
        return status;
    }
    
    
    public void setStatus(String statusStr) {
        status.set(statusStr);
    }


    
}
