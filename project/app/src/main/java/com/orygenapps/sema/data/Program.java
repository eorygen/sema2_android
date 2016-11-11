package com.orygenapps.sema.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by ashemah on 15/02/2015.
 */

public class Program extends RealmObject {

    public int getDbProgramId() {
        return dbProgramId;
    }

    public void setDbProgramId(int programId) {
        this.dbProgramId = programId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactEmail() {
        return contactEmail;
    }

    public void setContactEmail(String contactEmail) {
        this.contactEmail = contactEmail;
    }

    public int getDbVersionId() {
        return dbVersionId;
    }

    public void setDbVersionId(int dbVersionId) {
        this.dbVersionId = dbVersionId;
    }

    public long getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(long createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public long getUpdatedTimestamp() {
        return updatedTimestamp;
    }

    public void setUpdatedTimestamp(long updatedTimestamp) {
        this.updatedTimestamp = updatedTimestamp;
    }

    public boolean isNeedsSetup() {
        return needsSetup;
    }

    public void setNeedsSetup(boolean needsSetup) {
        this.needsSetup = needsSetup;
    }

    public RealmList<AnswerSet> getAnswerSets() {
        return answerSets;
    }

    public void setAnswerSets(RealmList<AnswerSet> answerSets) {
        this.answerSets = answerSets;
    }

    public RealmList<Survey> getSurveys() {
        return surveys;
    }

    public void setSurveys(RealmList<Survey> surveys) {
        this.surveys = surveys;
    }

    public int getVersionNumber() {
        return versionNumber;
    }

    public void setVersionNumber(int versionNumber) {
        this.versionNumber = versionNumber;
    }

    @Required
    private String displayName;

    @Required
    private String description;

    @Required
    private String contactName;

    @Required
    private String contactNumber;

    @Required
    private String contactEmail;

    private int dbProgramId;
    private int dbVersionId;

    private int versionNumber;

    private long createdTimestamp;
    private long updatedTimestamp;

    private boolean needsSetup;

    private RealmList<AnswerSet> answerSets;

    private RealmList<Survey> surveys;
}
