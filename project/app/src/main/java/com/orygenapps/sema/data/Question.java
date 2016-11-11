package com.orygenapps.sema.data;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.Required;

/**
 * Created by ashemah on 15/02/2015.
 */
public class Question extends RealmObject {
    public QuestionSet getSet() {
        return set;
    }

    public void setSet(QuestionSet set) {
        this.set = set;
    }

    public int getDbQuestionId() {
        return dbQuestionId;
    }

    public void setDbQuestionId(int dbQuestionId) {
        this.dbQuestionId = dbQuestionId;
    }

    public int getQuestionType() {
        return questionType;
    }

    public void setQuestionType(int questionType) {
        this.questionType = questionType;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public int getMinimumValue() {
        return minimumValue;
    }

    public void setMinimumValue(int minimumValue) {
        this.minimumValue = minimumValue;
    }

    public int getMaximumValue() {
        return maximumValue;
    }

    public void setMaximumValue(int maximumValue) {
        this.maximumValue = maximumValue;
    }

    public String getMinimumLabel() {
        return minimumLabel;
    }

    public void setMinimumLabel(String minimumLabel) {
        this.minimumLabel = minimumLabel;
    }

    public String getMaximumLabel() {
        return maximumLabel;
    }

    public void setMaximumLabel(String maximumLabel) {
        this.maximumLabel = maximumLabel;
    }

    public boolean isRandomiseChoiceDisplayOrder() {
        return randomiseChoiceDisplayOrder;
    }

    public void setRandomiseChoiceDisplayOrder(boolean randomiseChoiceDisplayOrder) {
        this.randomiseChoiceDisplayOrder = randomiseChoiceDisplayOrder;
    }

    public RealmList<QuestionChoice> getChoices() {
        return choices;
    }

    public void setChoices(RealmList<QuestionChoice> choices) {
        this.choices = choices;
    }

    private QuestionSet set;

    private int dbQuestionId;
    private int questionType;

    @Required
    private String questionText;

    private int minimumValue;
    private int maximumValue;

    @Required
    private String minimumLabel;

    @Required
    private String maximumLabel;

    private boolean randomiseChoiceDisplayOrder;

    private RealmList<QuestionChoice> choices;
}
