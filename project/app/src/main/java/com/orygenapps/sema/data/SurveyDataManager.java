package com.orygenapps.sema.data;

import android.content.Context;
import android.text.format.Time;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Calendar;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by ashemah on 14/02/2015.
 */

public class SurveyDataManager {

    ObjectMapper mMapper = new ObjectMapper();
    Map<String, Object> mData = null;
    RefreshCallback mCallback;

    public interface RefreshCallback {
        public void refreshDidComplete();
    }

    public void setUpAnswerSetsForSurvey(Context context, Realm realm, Program program, Survey survey) {

        if (survey != null) {

            int totalSurveysScheduled = 0;

            int triggerMode = survey.getAnswerTriggerMode();
            int currentIteration = survey.getCurrentIteration();
            int maxIterations = survey.getMaxIterations();

            long fromTimestamp = TimeConverter.currentTimeToMsTimestamp();

            if (triggerMode == AnswerSetTriggerMode.SCHEDULE) {
                while ((maxIterations == -1 || currentIteration < maxIterations) && totalSurveysScheduled < 200) {

                    if (hasAllowedDays(survey)) {

                        AnswerSet answerSet = createAnswerSetForSurvey(realm, program, survey);
                        incrementSurveyCurrentIteration(survey); // TODO: need to move this somewhere else otherwise won't increment
                        calcTimestampsForSet(survey, answerSet, fromTimestamp);
                        fromTimestamp = answerSet.getDeliveryTimestamp();

                        currentIteration++;
                        totalSurveysScheduled++;
                    }

                }
            }
            else if (triggerMode == AnswerSetTriggerMode.ADHOC) {
                AnswerSet set = createAnswerSetForSurvey(realm, program, survey);
                incrementSurveyCurrentIteration(survey);
//                set.setDeliveryTimestamp(TimeConverter.currentTimeToMsTimestamp());
            }
        }
    }

    private boolean hasAllowedDays(Survey survey) {
        return (survey.isScheduleAllowMonday() || survey.isScheduleAllowTuesday() || survey.isScheduleAllowWednesday() || survey.isScheduleAllowThursday() || survey.isScheduleAllowFriday() || survey.isScheduleAllowSaturday() || survey.isScheduleAllowSunday());
    }

    private AnswerSet createAnswerSetForSurvey(Realm realm, Program program, Survey survey) {

        AnswerSet answerSet = realm.createObject(AnswerSet.class);
        answerSet.setDbProgramId(program.getDbProgramId());
        answerSet.setDbProgramVersionId(program.getDbVersionId());
        answerSet.setIteration(survey.getCurrentIteration());

        Log.d("sema", "ITERATION: " + answerSet.getIteration());

        answerSet.setSurvey(survey);
        answerSet.setUuid(UUID.randomUUID().toString());
        answerSet.setDbSurveyId(survey.getDbSurveyId());
        survey.setAnswerSet(answerSet);

        // Get the answer set's ordered question list (empty)
        RealmList<Question> orderedQuestionList = answerSet.getOrderedQuestionList();

        // Get the list of question sets within the survey
        RealmList<QuestionSet> questionSets = survey.getQuestionSets();

        // Shuffle question sets if randomisation is enabled
        if (survey.getRandomiseQuestionSetDisplayOrder()) {
            long seed = System.nanoTime();
            Collections.shuffle(questionSets, new Random(seed));
        }

        // Iterate through the ordered question sets
        for (QuestionSet questionSet : questionSets) {

            // Get the ist of questions within the question set
            RealmList<Question> questions = questionSet.getQuestions();

            // Shuffle the questions if randomisation is enabled
            if (questionSet.getRandomiseQuestionDisplayOrder()) {
                long seed = System.nanoTime();
                Collections.shuffle(questions, new Random(seed));
            }

            // Iterate through the ordered questions
            for (Question question : questions) {

                // Add the question to the answer set's ordered questions list
                orderedQuestionList.add(question);
            }
        }

        Random r = new Random();
        answerSet.setStartAlarmRequestCode(r.nextInt());
        answerSet.setFirstReminderRequestCode(r.nextInt());
        answerSet.setSecondReminderRequestCode(r.nextInt());
        answerSet.setExpiryAlarmRequestCode(r.nextInt());

        long currentUnixTimestamp = TimeConverter.currentTimeToMsTimestamp();

        answerSet.setAnswerTriggerMode(survey.getAnswerTriggerMode());
        answerSet.setCreatedTimestamp(currentUnixTimestamp);

        answerSet.setDeliveryTimestamp(-1); // would normally be some time in the future when the alert goes off
        answerSet.setExpiryTimestamp(-1);

        answerSet.setCompletedTimestamp(-1);
        answerSet.setUploadedTimestamp(-1);

        return answerSet;
    }

    public void incrementSurveyCurrentIteration(Survey survey) {
        int iteration = survey.getCurrentIteration() + 1;
        survey.setCurrentIteration(iteration);
    }

    public void calcTimestampsForSet(Survey survey, AnswerSet answerSet, long fromTimestamp) {

//        Log.d("alarms", "** SurveyDataManager - Creating timestamps for AnswerSet UUID " + answerSet.getUuid() + " (Program ID " + answerSet.getDbProgramId() + ")");

        // generate a valid delivery timestamp
        int minutesOffset = 0;
        long proposedDeliveryTimestamp = 0;
        long validDeliveryTimestamp = 0;

        // calculate random offset
        if (survey.getScheduleDeliveryVariationMinutes() > 0) {
            Random rand = new Random();
            minutesOffset = rand.nextInt(2 * survey.getScheduleDeliveryVariationMinutes()) - survey.getScheduleDeliveryVariationMinutes();
        }

        // calculate next delivery time
        proposedDeliveryTimestamp = TimeConverter.addMinutesToTimestamp(fromTimestamp, survey.getScheduleDeliveryIntervalMinutes() + minutesOffset);

        // check that the proposed delivery time falls on an 'allowed day'
        int weekDay = TimeConverter.getDayFromTimestamp(proposedDeliveryTimestamp);
        boolean dayIsValid = isAllowedDayForSurvey(weekDay, survey);

//        Log.d("alarms", "** SurveyDataManager - weekDay " + weekDay + " (dayIsValid = " + dayIsValid + ")");

        if (dayIsValid) { // the day is an 'allowed day'

            // check that the proposed delivery time is valid
            Time deliveryTime = new Time(Time.getCurrentTimezone());
            deliveryTime.set(proposedDeliveryTimestamp);
            long deliveryMillis = deliveryTime.toMillis(false);

            Time programStartTime = new Time(Time.getCurrentTimezone());
            programStartTime.set(proposedDeliveryTimestamp);
            programStartTime.hour = survey.getScheduleStartSendingAtHour();
            programStartTime.minute = survey.getScheduleStartSendingAtMinute();
            long programStartMillis = programStartTime.toMillis(false);

            Time programEndTime = new Time(Time.getCurrentTimezone());
            programEndTime.set(proposedDeliveryTimestamp);
            programEndTime.hour = survey.getScheduleStopSendingAtHour();
            programEndTime.minute = survey.getScheduleStopSendingAtMinute();
            long programEndMillis = programEndTime.toMillis(false);

            if (deliveryMillis < programStartMillis) { // proposed delivery time is before the program's start time

//                Log.d("alarms", "** SurveyDataManager - proposed delivery time (" + deliveryMillis + ") is prior to program's start time (" + programStartMillis + ")");

                // set the delivery time to the specified day's start time TODO: include positive randomisation
                Time adjustedDeliveryTime = new Time(Time.getCurrentTimezone());
                adjustedDeliveryTime.set(programStartMillis);
                validDeliveryTimestamp = adjustedDeliveryTime.toMillis(false);

            }
            else if (deliveryMillis > programEndMillis) { // proposed delivery time is after the program's end time

//                Log.d("alarms", "** SurveyDataManager - proposed delivery time (" + deliveryMillis + ") is after program's end time (" + programEndMillis + ")");

                int daysToIncrement = getDaysUntilNextAllowedDay(weekDay, survey);
                if (daysToIncrement != -1) {

//                    Log.d("alarms", "** SurveyDataManager - attempting to reschedule " + daysToIncrement + " days later");

                    // set the delivery time to tomorrow's start time (plus positive randomisation only)
                    Time adjustedDeliveryTime = new Time(Time.getCurrentTimezone());
                    adjustedDeliveryTime.set(programStartMillis);
                    adjustedDeliveryTime.monthDay = adjustedDeliveryTime.monthDay + daysToIncrement;    // TODO: confirm this actually works right in edge cases
                    validDeliveryTimestamp = adjustedDeliveryTime.toMillis(true);    // TODO: confirm ok
                }
                else {
                    validDeliveryTimestamp = -1;
                }

            }
            else { // the proposed time is within the valid start and end times

//                Log.d("alarms", "** SurveyDataManager - proposed delivery time (" + deliveryMillis + ") is within program's start time (" + programStartMillis + ") and end time (" + programEndMillis + ")");

                validDeliveryTimestamp = proposedDeliveryTimestamp;

            }

        } else { // the day is not an 'allowed day'

            Time programStartTime = new Time(Time.getCurrentTimezone());
            programStartTime.set(proposedDeliveryTimestamp);
            programStartTime.hour = survey.getScheduleStartSendingAtHour();
            programStartTime.minute = survey.getScheduleStartSendingAtMinute();
            long programStartMillis = programStartTime.toMillis(false);

            int daysToIncrement = getDaysUntilNextAllowedDay(weekDay, survey);
            if (daysToIncrement != -1) {

//                Log.d("alarms", "** SurveyDataManager - attempting to reschedule " + daysToIncrement + " days later");

                // set the delivery time to tomorrow's start time (plus positive randomisation only)
                Time adjustedDeliveryTime = new Time(Time.getCurrentTimezone());
                adjustedDeliveryTime.set(programStartMillis);
                adjustedDeliveryTime.monthDay = adjustedDeliveryTime.monthDay + daysToIncrement;    // TODO: confirm this actually works right in edge cases
                validDeliveryTimestamp = adjustedDeliveryTime.toMillis(true);    // TODO: confirm ok
            }
            else {
                validDeliveryTimestamp = -1;
            }

        }

//        Log.d("alarms", "** SurveyDataManager - final valid timestamp is " + validDeliveryTimestamp + " (" + TimeConverter.convertTimestampToDateString(validDeliveryTimestamp) + ")");

        Log.d("alarms", "** SurveyDataManager - Scheduled delivery " + TimeConverter.convertTimestampToDateString(validDeliveryTimestamp));

        long expiryTimestamp = TimeConverter.addMinutesToTimestamp(validDeliveryTimestamp, survey.getScheduleExpiryTimeMinutes());

        answerSet.setDeliveryTimestamp(validDeliveryTimestamp);
        answerSet.setExpiryTimestamp(expiryTimestamp);

        // reminders
        long firstReminderTimestamp = validDeliveryTimestamp + Math.round((expiryTimestamp - validDeliveryTimestamp) * 0.33f);
        answerSet.setFirstReminderTimestamp(firstReminderTimestamp);
        long secondReminderTimestamp = validDeliveryTimestamp + Math.round((expiryTimestamp - validDeliveryTimestamp) * 0.66f);
        answerSet.setSecondReminderTimestamp(secondReminderTimestamp);
    }

    boolean isAllowedDayForSurvey(int weekDay, Survey survey) {

        if (weekDay == Calendar.SATURDAY) {
            return survey.isScheduleAllowSaturday();
        } else if (weekDay == Calendar.SUNDAY) {
            return survey.isScheduleAllowSunday();
        } else if (weekDay == Calendar.MONDAY) {
            return survey.isScheduleAllowMonday();
        } else if (weekDay == Calendar.TUESDAY) {
            return survey.isScheduleAllowTuesday();
        } else if (weekDay == Calendar.WEDNESDAY) {
            return survey.isScheduleAllowWednesday();
        } else if (weekDay == Calendar.THURSDAY) {
            return survey.isScheduleAllowThursday();
        } else if (weekDay == Calendar.FRIDAY) {
            return survey.isScheduleAllowFriday();
        } else {
            return false;
        }
    }

    int getDaysUntilNextAllowedDay(int previousWeekDay, Survey survey) {

        int proposedWeekDay;
        for (int n = 1; n <= 7; n++) {

            proposedWeekDay = ((previousWeekDay - 1 + n) % 7) + 1;
            if (isAllowedDayForSurvey(proposedWeekDay, survey)) {

                return n;
            }
        }

        return -1;
    }

    public static boolean programIsActive(Program program, long timestamp) { // refactor this out - ash

        return true;

//        Time programStartTime = new Time(Time.getCurrentTimezone());
//        programStartTime.setToNow();
//        programStartTime.hour = program.getScheduleStartSendingAtHour();
//        programStartTime.minute = program.getScheduleStartSendingAtMinute();
//        long programStartMillis = programStartTime.toMillis(false);
//
//        Time programEndTime = new Time(Time.getCurrentTimezone());
//        programEndTime.setToNow();
//        programEndTime.hour = program.getScheduleStopSendingAtHour();
//        programEndTime.minute = program.getScheduleStopSendingAtMinute();
//        long programEndMillis = programEndTime.toMillis(false);
//
//        return timestamp >= programStartMillis && timestamp <= programEndMillis;
    }

    public RealmResults<Program> getPrograms(Context context) {
        Realm realm = null;
        RealmQuery<Program> query = null;

        try {
            realm = Realm.getDefaultInstance();
            query = realm.where(Program.class);
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }

        return query.findAllSorted("displayName");
    }

    public RealmResults<AnswerSet> getActiveAnswerSets(Realm realm, Context context) {
        long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();

        RealmResults<AnswerSet> answerSets = null;

        answerSets = realm.where(AnswerSet.class)
                .beginGroup()
                .equalTo("answerTriggerMode", AnswerSetTriggerMode.SCHEDULE)   // the answer set type is ADHOC
                .lessThan("deliveryTimestamp", currentTimestamp)    // The answer set was delivered in the past
                .equalTo("completedTimestamp", -1)                  // AND it has not been completed
                .equalTo("uploadedTimestamp", -1)                   // AND it has not been uploaded
                .beginGroup()                                       // AND
                .equalTo("expiryTimestamp", -1)                     // It is not set to expire ever
                .or()                                               // OR
                .greaterThan("expiryTimestamp", currentTimestamp)   // It will expire in the future
                .endGroup()
                .endGroup()
                .or()
                .beginGroup()
                .equalTo("answerTriggerMode", AnswerSetTriggerMode.ADHOC)   // the schedule type is ADHOC
                .equalTo("completedTimestamp", -1)                  // the ADHOC survey has not been completed yet
                .beginGroup()
                .equalTo("expiryTimestamp", -1)                 // The ADHOC answer set has not been opened
                .or()
                .greaterThan("expiryTimestamp", currentTimestamp)   // The ADHOC answer set has expired
                .endGroup()
                .endGroup()
                .findAll();

        Log.d("answersets", "** SurveyDataManager - found " + answerSets.size() + " active answer sets");

        return answerSets;
    }

    public RealmResults<AnswerSet> getAnswerSetsToUpload(Context context) {
        long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();

        Realm realm = null;
        RealmResults<AnswerSet> answerSets = null;

        try {
            realm = Realm.getDefaultInstance();

            answerSets = realm.where(AnswerSet.class)
                    .notEqualTo("deliveryTimestamp", -1)                // It has a delivery timestamp
                    .lessThan("deliveryTimestamp", currentTimestamp)    // AND The answer set was delivered in the past
                    .equalTo("uploadedTimestamp", -1)                   // AND it has not been uploaded
                    .beginGroup()                                       // AND
                    .beginGroup()
                    .notEqualTo("completedTimestamp", -1)               // It has a completed timestamp
                    .lessThan("completedTimestamp", currentTimestamp)   // AND It has been completed in the past
                    .endGroup()
                    .or()                                                   // OR
                    .beginGroup()
                    .notEqualTo("expiryTimestamp", -1)                  // It has an expiry timestamp
                    .lessThan("expiryTimestamp", currentTimestamp)      // AND It has expired in the past
                    .endGroup()
                    .endGroup().findAll();
            Log.d("answersets", "** SurveyDataManager - found " + answerSets.size() + " answer sets to upload");
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }

        return answerSets;
    }

    public RealmResults<Survey> getAdHocSurveys(Context context) {
        long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();

        Realm realm = null;
        RealmResults<Survey> surveys = null;

        try {
            realm = Realm.getDefaultInstance();
            surveys = realm.where(Survey.class).equalTo("answerTriggerMode", AnswerSetTriggerMode.ADHOC).findAll();
        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }

        return surveys;
    }

}
