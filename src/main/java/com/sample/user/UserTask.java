package com.sample.user;

import java.time.LocalDateTime;

public class UserTask {
    private String taskId;
    private String country;
    private String taskDescription;
    private LocalDateTime startTime;
    private String processName;

    public UserTask(String taskId, String country, String taskDescription, LocalDateTime startTime, String processName) {
        this.taskId = taskId;
        this.country = country;
        this.taskDescription = taskDescription;
        this.startTime = startTime;
        this.processName = processName;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getCountry() {
        return country;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getProcessName() {
        return processName;
    }
}
