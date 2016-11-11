package com.orygenapps.sema.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by ashemah on 15/02/2015.
 */

public class AnswerSet extends RealmObject {

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getDbProgramId() {
        return dbProgramId;
    }

    public void setDbProgramId(int dbProgramId) {
        this.dbProgramId = dbProgramId;
    }

    public int getDbProgramVersionId() {
        return dbProgramVersionId;
    }

    public void setDbProgramVersionId(int dbProgramVersionId) {
        this.dbProgramVersionId = dbProgramVersionId;
    }

    public int getIteration() {
        return iteration;
    }

    public void setIteration(int iteration) {
        this.iteration = iteration;
    }

    public int getAnswerTriggerMode() {
        return answerTriggerMode;
    }

    public void setAnswerTriggerMode(int answerTriggerMode) {
        this.answerTriggerMode = answerTriggerMode;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getDeliveryTimestamp() {
        return deliveryTimestamp;
    }

    public void setDeliveryTimestamp(long deliveryTimestamp) {
        this.deliveryTimestamp = deliveryTimestamp;
    }

    public long getCompletedTimestamp() {
        return completedTimestamp;
    }

    public void setCompletedTimestamp(long completedTimestamp) {
        this.completedTimestamp = completedTimestamp;
    }

    public long getExpiryTimestamp() {
        return expiryTimestamp;
    }

    public void setExpiryTimestamp(long expiryTimestamp) {
        this.expiryTimestamp = expiryTimestamp;
    }

    public long getUploadedTimestamp() {
        return uploadedTimestamp;
    }

    public void setUploadedTimestamp(long uploadedTimestamp) {
        this.uploadedTimestamp = uploadedTimestamp;
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public int getDbSurveyId() {
        return dbSurveyId;
    }

    public void setDbSurveyId(int dbSurveyId) {
        this.dbSurveyId = dbSurveyId;
    }

    public int getStartAlarmRequestCode() {
        return startAlarmRequestCode;
    }

    public void setStartAlarmRequestCode(int startAlarmRequestCode) {
        this.startAlarmRequestCode = startAlarmRequestCode;
    }

    public int getExpiryAlarmRequestCode() {
        return expiryAlarmRequestCode;
    }

    public void setExpiryAlarmRequestCode(int expiryAlarmRequestCode) {
        this.expiryAlarmRequestCode = expiryAlarmRequestCode;
    }

    public long getFirstReminderTimestamp() {
        return firstReminderTimestamp;
    }

    public void setFirstReminderTimestamp(long firstReminderTimestamp) {
        this.firstReminderTimestamp = firstReminderTimestamp;
    }

    public long getSecondReminderTimestamp() {
        return secondReminderTimestamp;
    }

    public void setSecondReminderTimestamp(long secondReminderTimestamp) {
        this.secondReminderTimestamp = secondReminderTimestamp;
    }

    public RealmList<Question> getOrderedQuestionList() {
        return orderedQuestionList;
    }

    public void setOrderedQuestionList(RealmList<Question> orderedQuestionList) {
        this.orderedQuestionList = orderedQuestionList;
    }

    public int getFirstReminderRequestCode() {
        return firstReminderRequestCode;
    }

    public void setFirstReminderRequestCode(int firstReminderRequestCode) {
        this.firstReminderRequestCode = firstReminderRequestCode;
    }

    public int getSecondReminderRequestCode() {
        return secondReminderRequestCode;
    }

    public void setSecondReminderRequestCode(int secondReminderRequestCode) {
        this.secondReminderRequestCode = secondReminderRequestCode;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    private Survey survey;
    private int dbSurveyId;

    @Required
    private String uuid;

    private int dbProgramId;
    private int dbProgramVersionId;
    private int iteration;

    private int answerTriggerMode;

    private long uploadedTimestamp;
    private long createdTimestamp;
    private long deliveryTimestamp;
    private long expiryTimestamp;
    private long firstReminderTimestamp;
    private long secondReminderTimestamp;
    private long completedTimestamp;

    private int startAlarmRequestCode;
    private int expiryAlarmRequestCode;

    private int firstReminderRequestCode;
    private int secondReminderRequestCode;

    @Required
    private String timezone;

    private RealmList<Question> orderedQuestionList;
}

