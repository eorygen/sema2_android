package com.orygenapps.sema.data;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by ashemah on 15/02/2015.
 */
public class Survey extends RealmObject {

    public Program getProgram() {
        return program;
    }
    public void setProgram(Program program) {
        this.program = program;
    }

    public int getDbSurveyId() {
        return dbSurveyId;
    }
    public void setDbSurveyId(int dbSurveyId) {
        this.dbSurveyId = dbSurveyId;
    }

    public boolean getRandomiseQuestionSetDisplayOrder() {
        return randomiseQuestionSetDisplayOrder;
    }
    public void setRandomiseQuestionSetDisplayOrder(boolean randomiseQuestionSetDisplayOrder) {
        this.randomiseQuestionSetDisplayOrder = randomiseQuestionSetDisplayOrder;
    }

    public RealmList<QuestionSet> getQuestionSets() {
        return questionSets;
    }
    public void setQuestionSets(RealmList<QuestionSet> questionSets) {
        this.questionSets = questionSets;
    }

    public int getMaxIterations() {
        return maxIterations;
    }
    public void setMaxIterations(int maxIterations) {
        this.maxIterations = maxIterations;
    }

    public int getCurrentIteration() {
        return currentIteration;
    }
    public void setCurrentIteration(int currentIteration) {
        this.currentIteration = currentIteration;
    }

    public int getDailyIteration() {
        return dailyIteration;
    }
    public void setDailyIteration(int dailyIteration) {
        this.dailyIteration = dailyIteration;
    }


    public int getAnswerTriggerMode() {
        return answerTriggerMode;
    }
    public void setAnswerTriggerMode(int answerTriggerMode) {
        this.answerTriggerMode = answerTriggerMode;
    }

    public boolean isScheduleIsActive() {
        return scheduleIsActive;
    }

    public void setScheduleIsActive(boolean scheduleIsActive) {
        this.scheduleIsActive = scheduleIsActive;
    }

    public int getScheduleStartSendingAtHour() {
        return scheduleStartSendingAtHour;
    }

    public void setScheduleStartSendingAtHour(int scheduleStartSendingAtHour) {
        this.scheduleStartSendingAtHour = scheduleStartSendingAtHour;
    }

    public int getScheduleStartSendingAtMinute() {
        return scheduleStartSendingAtMinute;
    }

    public void setScheduleStartSendingAtMinute(int scheduleStartSendingAtMinute) {
        this.scheduleStartSendingAtMinute = scheduleStartSendingAtMinute;
    }

    public int getScheduleStopSendingAtHour() {
        return scheduleStopSendingAtHour;
    }

    public void setScheduleStopSendingAtHour(int scheduleStopSendingAtHour) {
        this.scheduleStopSendingAtHour = scheduleStopSendingAtHour;
    }

    public int getScheduleStopSendingAtMinute() {
        return scheduleStopSendingAtMinute;
    }

    public void setScheduleStopSendingAtMinute(int scheduleStopSendingAtMinute) {
        this.scheduleStopSendingAtMinute = scheduleStopSendingAtMinute;
    }

    public int getScheduleDeliveryIntervalMinutes() {
        return scheduleDeliveryIntervalMinutes;
    }

    public void setScheduleDeliveryIntervalMinutes(int scheduleDeliveryIntervalMinutes) {
        this.scheduleDeliveryIntervalMinutes = scheduleDeliveryIntervalMinutes;
    }

    public int getScheduleDeliveryVariationMinutes() {
        return scheduleDeliveryVariationMinutes;
    }

    public void setScheduleDeliveryVariationMinutes(int scheduleDeliveryVariationMinutes) {
        this.scheduleDeliveryVariationMinutes = scheduleDeliveryVariationMinutes;
    }

    public int getScheduleExpiryTimeMinutes() {
        return scheduleExpiryTimeMinutes;
    }

    public void setScheduleExpiryTimeMinutes(int scheduleExpiryTimeMinutes) {
        this.scheduleExpiryTimeMinutes = scheduleExpiryTimeMinutes;
    }

    public boolean isScheduleAllowMonday() {
        return scheduleAllowMonday;
    }

    public void setScheduleAllowMonday(boolean scheduleAllowMonday) {
        this.scheduleAllowMonday = scheduleAllowMonday;
    }

    public boolean isScheduleAllowTuesday() {
        return scheduleAllowTuesday;
    }

    public void setScheduleAllowTuesday(boolean scheduleAllowTuesday) {
        this.scheduleAllowTuesday = scheduleAllowTuesday;
    }

    public boolean isScheduleAllowWednesday() {
        return scheduleAllowWednesday;
    }

    public void setScheduleAllowWednesday(boolean scheduleAllowWednesday) {
        this.scheduleAllowWednesday = scheduleAllowWednesday;
    }

    public boolean isScheduleAllowThursday() {
        return scheduleAllowThursday;
    }

    public void setScheduleAllowThursday(boolean scheduleAllowThursday) {
        this.scheduleAllowThursday = scheduleAllowThursday;
    }

    public boolean isScheduleAllowFriday() {
        return scheduleAllowFriday;
    }

    public void setScheduleAllowFriday(boolean scheduleAllowFriday) {
        this.scheduleAllowFriday = scheduleAllowFriday;
    }

    public boolean isScheduleAllowSaturday() {
        return scheduleAllowSaturday;
    }

    public void setScheduleAllowSaturday(boolean scheduleAllowSaturday) {
        this.scheduleAllowSaturday = scheduleAllowSaturday;
    }

    public boolean isScheduleAllowSunday() {
        return scheduleAllowSunday;
    }

    public void setScheduleAllowSunday(boolean scheduleAllowSunday) {
        this.scheduleAllowSunday = scheduleAllowSunday;
    }

    public AnswerSet getAnswerSet() {
        return answerSet;
    }
    public void setAnswerSet(AnswerSet answerSet) {
        this.answerSet = answerSet;
    }

    private Program program;

    private int dbSurveyId;
    private boolean randomiseQuestionSetDisplayOrder;
    private int answerTriggerMode;

    private int currentIteration;
    private int maxIterations;
    private int dailyIteration;

    private boolean scheduleIsActive;

    private int scheduleStartSendingAtHour;
    private int scheduleStartSendingAtMinute;

    private int scheduleStopSendingAtHour;
    private int scheduleStopSendingAtMinute;

    private int scheduleDeliveryIntervalMinutes;
    private int scheduleDeliveryVariationMinutes;

    private int scheduleExpiryTimeMinutes;

    private boolean scheduleAllowMonday;
    private boolean scheduleAllowTuesday;
    private boolean scheduleAllowWednesday;
    private boolean scheduleAllowThursday;
    private boolean scheduleAllowFriday;
    private boolean scheduleAllowSaturday;
    private boolean scheduleAllowSunday;

    private RealmList<QuestionSet> questionSets;
    private AnswerSet answerSet;
}
