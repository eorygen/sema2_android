package com.orygenapps.sema.data;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orygenapps.sema.SyncFinishedEvent;
import com.orygenapps.sema.SyncStartedEvent;
import com.path.android.jobqueue.Job;
import com.path.android.jobqueue.Params;
import com.orygenapps.sema.CheckingVersionsEvent;
import com.orygenapps.sema.NoChangeEvent;
import com.orygenapps.sema.ProgramsUpdatedEvent;
import com.orygenapps.sema.R;
import com.orygenapps.sema.SurveyAlarmManager;
import com.orygenapps.sema.UpdatingDatabaseEvent;
import com.orygenapps.sema.activity.SEMAApplication;
import com.orygenapps.sema.activity.SEMAService;
import com.orygenapps.sema.SendingAnswersEvent;
import com.orygenapps.sema.SyncJobFailedEvent;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.greenrobot.event.EventBus;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
/**
 * Created by starehe on 26/03/15.
 */
public class SyncJob extends Job {

    public static final String kSyncDataStartTimestamp = "kSyncDataStartTimestamp";
    public static final String kSyncDataEndTimestamp = "kSyncDataEndTimestamp";
    public static final String kSyncDataOffline = "kSyncDataSyncOffline";
    public static final String kSyncDataServerTestFailed = "kSyncDataServerTestFailed";
    public static final String kSyncDataCount = "kSyncDataCount"; // answer sets to send
    public static final String kSyncDataSendCount = "kSyncDataSendCount"; // answer sets sent

    SEMAApplication mApplication;
    SEMAService mService;
    SurveyDataManager mDataManager;
    SurveyAlarmManager mSurveyAlarmManager;
    public static final int PRIORITY = 1;

    EventBus bus;

    public SyncJob() {
        //super(new Params(PRIORITY).requireNetwork().persist()); // only run if network available
        super(new Params(PRIORITY).persist()); // run even if network unavailable
    }

    @Override
    public void onAdded() {
        // Job has been saved to disk.
    }
    @Override
    public void onRun() throws Throwable {

        Realm realm = null;

        try {
            mApplication = SEMAApplication.getInstance();
            mService = mApplication.getAPI();
            mDataManager = SEMAApplication.getInstance().getDataManager();
            mSurveyAlarmManager = SEMAApplication.getInstance().getSurveyAlarmManager();

            realm = Realm.getDefaultInstance();

            SharedPreferences sharedPref = mApplication.getSharedPreferences(mApplication.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();

            long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
            editor.putLong("last_sync_timestamp", currentTimestamp);
            editor.commit();

            mApplication.mIsSynchronising = true;

            // TODO: already setting a last_sync timestamp so can eventually combine them?
            resetLastSyncData();
            setLastSyncStartTimestamp();

            bus = EventBus.getDefault();
            SyncStartedEvent syncStartedEvent = new SyncStartedEvent();
            // update event properties here if required
            bus.post(syncStartedEvent);

            int sendCount = 0;

            try {
                // 1) SEND DATA TO SERVER

                // get any answersets that have not yet been uploaded
                RealmResults<AnswerSet> answerSets = mDataManager.getAnswerSetsToUpload(SEMAApplication.getInstance());

                editor.putLong(kSyncDataCount, answerSets.size());
                editor.commit();

                // add each answerset to the list of answer set proxies
                ArrayList<AnswerSetProxy> mAnswerSetProxies = new ArrayList<AnswerSetProxy>();
                for (int i = answerSets.size() - 1; i >= 0; i--) { // necessary to do this in reverse due to realm modifying the actual data (and query), which affects the size of the list
                    AnswerSet answerSet = answerSets.get(i);

                    if (answerSet.getCompletedTimestamp() != -1 || (answerSet.getExpiryTimestamp() != -1 && answerSet.getExpiryTimestamp() < currentTimestamp)) {

                        if (answerSet.getCompletedTimestamp() == -1) {
                            Log.d("sync", "** SyncJob - uploading expired answer set with UUID " + answerSet.getUuid() + " (Program ID " + answerSet.getDbProgramId() + ")");
                        } else {
                            Log.d("sync", "** SyncJob - uploading completed answer set with UUID " + answerSet.getUuid() + " (Program ID " + answerSet.getDbProgramId() + ")");
                        }

                        Log.d("sync", "** Uploading: " + answerSet.getUuid() + " Iteration: " + answerSet.getIteration());

                        // Only upload answerSets that are either scheduled or adhoc and completed
                        Survey survey = answerSet.getSurvey();

                        AnswerSetProxy answerSetProxy = AnswerSetProxy.initWithAnswerSet(SEMAApplication.getInstance(), answerSet);

                        // send an event to the event bus notifying that the app is trying to update
                        bus = EventBus.getDefault();
                        SendingAnswersEvent event = new SendingAnswersEvent();
                        // update event properties here if required
                        bus.post(event);

                        // POST the answerset to the server
                        Response r = mService.postAnswerSet(answerSetProxy);

                        realm.beginTransaction();
                        // set uploaded time in db if response was successful
                        answerSet.setUploadedTimestamp(currentTimestamp);
                        realm.commitTransaction();

                        sendCount++;
                        editor.putLong(kSyncDataSendCount, sendCount);
                        editor.commit();


                        Log.d("sync", "** Uploaded: " + answerSet.getUuid());
                    } else {
                        Log.d("sema", "Skipped: " + i);
                    }
                }

                // 2) POST PROGRAM VERSION INFO AND GET UPDATED DATA FROM SERVER

                ArrayList<ProgramVersionProxy> programVersionProxies = new ArrayList<ProgramVersionProxy>();

                // get all of the programs
                RealmResults<Program> programs = mDataManager.getPrograms(mApplication);

                for (Program program : programs) {
                    ProgramVersionProxy programVersionProxy = new ProgramVersionProxy(program.getDbProgramId(), program.getVersionNumber());
                    programVersionProxies.add(programVersionProxy);
                }

                // send an event to the event bus notifying that the app is trying to check its program versions
                bus = EventBus.getDefault();
                CheckingVersionsEvent checkingVersionsEvent = new CheckingVersionsEvent();
                // update event properties here if required
                bus.post(checkingVersionsEvent);

                String versionName = mApplication.getPackageManager().getPackageInfo(mApplication.getPackageName(), 0).versionName;
                int versionCode = mApplication.getPackageManager().getPackageInfo(mApplication.getPackageName(), 0).versionCode;
                User userProfile = realm.where(User.class).findFirst();

                if (userProfile == null) {
                    userProfile = realm.createObject(User.class);
                }

                // POST
                SyncDataProxy syncProxy = new SyncDataProxy(versionCode, versionName, userProfile.getPushToken(), programVersionProxies);

                Response r = mService.sync(syncProxy);
                // TODO: check if 200 series of status codes

                // send an event to the event bus notifying that the app is trying to update
                bus = EventBus.getDefault();
                UpdatingDatabaseEvent event = new UpdatingDatabaseEvent();
                // update event properties here if required
                bus.post(event);

                // remove all notifications (as their associated answer sets will no longer exist)
                NotificationManager notificationManager = (NotificationManager) mApplication.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.cancelAll();

                TypedInput body = r.getBody();

                String jsonResponseString = "";
                try {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(body.in()));
                    StringBuilder out = new StringBuilder();
                    String newLine = System.getProperty("line.separator");
                    String line;
                    while ((line = reader.readLine()) != null) {
                        out.append(line);
                        out.append(newLine);
                    }
                    jsonResponseString = out.toString();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // update cache.json with the response data. TODO: improve update process
                String filename = "cache.json"; // TODO: path
                FileOutputStream outputStream;

                try {
                    outputStream = SEMAApplication.getInstance().openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(jsonResponseString.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                // 3) UPDATE DATABASE
                updateDatabaseFromCache(mApplication);

                // send an event to the event bus notifying that the app is now up to date
                bus = EventBus.getDefault();
                ProgramsUpdatedEvent programsUpdatedEvent = new ProgramsUpdatedEvent();
                // update event properties here if required
                bus.post(programsUpdatedEvent);

            } catch (RetrofitError e) {

                Response response = e.getResponse();
                if (response == null) { // occurs if no network access etc.

                    setLastSyncOffline(); // TODO: confirm

                    bus = EventBus.getDefault();
                    SyncJobFailedEvent syncJobFailedEvent = new SyncJobFailedEvent();
                    // update event properties here if required
                    bus.post(syncJobFailedEvent);
                } else {
                    int statusCode = e.getResponse().getStatus();

                    if (statusCode == 304) { // not modified i.e. local data is already up to date
                        // send an event to the event bus notifying that there is no change to the program data required
                        bus = EventBus.getDefault();
                        NoChangeEvent noChangeEvent = new NoChangeEvent();
                        // update event properties here if required
                        bus.post(noChangeEvent);
                    } else { // e.g. 500

                        setLastSyncServerTestFailed();

                        bus = EventBus.getDefault();
                        SyncJobFailedEvent event = new SyncJobFailedEvent();
                        // update event properties here if required
                        bus.post(event);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {

            // get all adhoc surveys
            RealmResults<Survey> surveys = mDataManager.getAdHocSurveys(mApplication.getApplicationContext());

            for (int s = 0; s < surveys.size(); s++) {
                // look for an existing answerset for the current adhoc survey that either does not have an expiry, or expires in the future
                Survey survey = surveys.get(s);
                long currentTimestamp = TimeConverter.currentTimeToMsTimestamp();
                AnswerSet existingAnswerSet = realm.where(AnswerSet.class)
//                        .equalTo("answerTriggerMode", AnswerSetTriggerMode.ADHOC)
                        .equalTo("dbSurveyId", survey.getDbSurveyId())
                        .equalTo("completedTimestamp", -1)                      // does not have a completion time
                        .beginGroup()
                            .equalTo("expiryTimestamp", -1)                     // does not have an expiry time (i.e. hasn't been started yet)
                            .or()
                            .greaterThan("expiryTimestamp", currentTimestamp)   // AND has not yet expired
                        .endGroup()
                        .findFirst();

                if (existingAnswerSet == null) {
                    // setup a single adhoc scheduled answer sets for this survey
                    realm.beginTransaction(); // TODO: make sure this won't cause conflicts
                    mDataManager.setUpAnswerSetsForSurvey(mApplication, realm, survey.getProgram(), survey);
                    realm.commitTransaction();
                }

            }

            if (realm != null) {
                realm.close();
            }

            try {
                // clear and reschedule up to 64 alarms across all surveys
                mSurveyAlarmManager.setupAlarms(mApplication.getApplicationContext());
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                mApplication.mIsSynchronising = false;

                // notify that sync has finished
                setLastSyncEndTimestamp();

                bus = EventBus.getDefault();
                SyncFinishedEvent syncFinishedEvent = new SyncFinishedEvent();
                bus.post(syncFinishedEvent);
            }
        }

    }

    public void resetLastSyncData() {

        SharedPreferences sharedPref = mApplication.getSharedPreferences(mApplication.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(kSyncDataStartTimestamp);
        editor.remove(kSyncDataEndTimestamp);
        editor.remove(kSyncDataOffline);
        editor.remove(kSyncDataServerTestFailed);
        editor.remove(kSyncDataCount);
        editor.remove(kSyncDataSendCount);
        editor.commit();
    }

    public void setLastSyncStartTimestamp() {

        long timestamp = TimeConverter.currentTimeToMsTimestamp();

        // Update sync data
        SharedPreferences sharedPref = mApplication.getSharedPreferences(mApplication.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(kSyncDataStartTimestamp, timestamp);
        editor.commit();
    }

    public void setLastSyncEndTimestamp() {

        long timestamp = TimeConverter.currentTimeToMsTimestamp();

        // Update sync data
        SharedPreferences sharedPref = mApplication.getSharedPreferences(mApplication.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putLong(kSyncDataEndTimestamp, timestamp);
        editor.commit();
    }

    public void setLastSyncOffline() {

        // Update sync data
        SharedPreferences sharedPref = mApplication.getSharedPreferences(mApplication.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(kSyncDataOffline, true);
        editor.commit();
    }

    public void setLastSyncServerTestFailed() {

        // Update sync data
        SharedPreferences sharedPref = mApplication.getSharedPreferences(mApplication.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(kSyncDataServerTestFailed, true);
        editor.commit();
    }

    public void updateDatabaseFromCache(Context context) throws IOException {

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            // Load the data from the local disk cache
            String dataStr = jsonFileToString("cache.json");
            ObjectMapper mMapper = new ObjectMapper();
            Map<String, Object> mData = mMapper.readValue(dataStr, Map.class);

            List<Map<String, Object>> programs = (List<Map<String, Object>>) mData.get("programs");

            // Clear the existing alarms
            RealmResults<AnswerSet> answerSets = realm.allObjects(AnswerSet.class);

            long timestamp = TimeConverter.currentTimeToMsTimestamp();

            realm.beginTransaction();
            for (int i = answerSets.size() - 1; i >= 0; i--) { // necessary to do this in reverse due to realm modifying the actual data (and query), which affects the size of the list

                AnswerSet aset = answerSets.get(i);
                mSurveyAlarmManager.removeAlarmsFromAnswerSet(mApplication, aset);

                if (aset.getAnswerTriggerMode() == AnswerSetTriggerMode.ADHOC ||
                        (aset.getAnswerTriggerMode() == AnswerSetTriggerMode.SCHEDULE && aset.getDeliveryTimestamp() > timestamp)) {

                    aset.removeFromRealm();
                }
            }
            realm.commitTransaction();

            RealmResults<Program> existing = realm.allObjects(Program.class);

            for (int i = existing.size() - 1; i >= 0; i--) {
                Program program = existing.get(i);
                realm.beginTransaction();
                clearExistingQuestionData(mApplication, realm, program);
                program.removeFromRealm();
                realm.commitTransaction();
            }

            // Add new programs
            for (Map<String, Object> programData : programs) {
                updateProgramInDatabase(context, realm, programData);
            }
        }

        finally {
            if (realm != null) {
                realm.close();
            }
        }

    }

    private String jsonFileToString(String filename) throws IOException {

        FileInputStream finStream = new FileInputStream(SEMAApplication.getInstance().getFilesDir() + "/" + filename);
        BufferedReader reader = new BufferedReader(new InputStreamReader(finStream));

        String jsonString = "";
        try {

            StringBuilder out = new StringBuilder();
            String newLine = System.getProperty("line.separator");
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
                out.append(newLine);
            }
            jsonString = out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            reader.close();
        }

        return jsonString;
    }

    private void updateProgramInDatabase(Context context, Realm realm, Map<String, Object> programData) {

        int dbProgramId = (Integer)programData.get("id");
        int dbProgramVersionId = (Integer)programData.get("version_id");
        int programVersionNumber = (Integer)programData.get("version_number");

        realm.beginTransaction();

        Program program = realm.createObject(Program.class);
        program.setDbProgramId(dbProgramId);
        program.setDbVersionId(dbProgramVersionId);
        program.setVersionNumber(programVersionNumber);
        program.setCreatedTimestamp(TimeConverter.currentTimeToMsTimestamp());
        program.setNeedsSetup(true);

        program.setUpdatedTimestamp(TimeConverter.currentTimeToMsTimestamp());

        // Revision tracking
        int newVersionNumber = (Integer) programData.get("version_number");
        int currentVersionNumber = program.getVersionNumber();

        if (newVersionNumber > currentVersionNumber) {
            program.setNeedsSetup(true);
        }

        // Hardcoded
        program.setVersionNumber(newVersionNumber);

        program.setDbVersionId((Integer) programData.get("version_id"));
        program.setDisplayName((String)programData.get("display_name"));
        program.setDescription((String) programData.get("description"));

        program.setContactName((String)programData.get("contact_name"));
        program.setContactNumber((String) programData.get("contact_number"));
        program.setContactEmail((String) programData.get("contact_email"));

        realm.commitTransaction();

        realm.beginTransaction();
        updateQuestionsInDatabase(mApplication, realm, program, programData);
        realm.commitTransaction();
    }

    private void clearExistingQuestionData(Context context, Realm realm, Program program) {

        RealmList<Survey> surveys = program.getSurveys();
        for (int i = surveys.size()-1; i >= 0; i--) { // necessary to do this in reverse due to realm modifying the actual data (and query), which affects the size of the list

            Survey survey = surveys.get(i);

            for (QuestionSet set : survey.getQuestionSets()) {
                for (Question question : set.getQuestions()) {
                    for (QuestionChoice choice : question.getChoices()) {
                        choice.removeFromRealm();
                    }
                    question.removeFromRealm();
                }
                set.removeFromRealm();
            }
            survey.removeFromRealm();
        }
    }

    private void updateQuestionsInDatabase(Context context, Realm realm, Program program, Map<String, Object> data) {

        // Iterate the Surveys
        List<Map<String, Object>> surveys = (List<Map<String, Object>>) data.get("surveys");

        for (Map<String, Object> surveyData : surveys) {
            Survey survey = realm.createObject(Survey.class);

            survey.setProgram(program);
            program.getSurveys().add(survey);

            survey.setDbSurveyId((Integer) surveyData.get("id"));
            survey.setMaxIterations((Integer) surveyData.get("max_iterations"));
            survey.setAnswerTriggerMode((Integer)surveyData.get("trigger_mode"));

            survey.setCurrentIteration((Integer) surveyData.get("current_iteration"));

            // Schedule
            survey.setScheduleIsActive((Boolean) surveyData.get("schedule_is_active"));

            survey.setScheduleStartSendingAtHour((Integer) surveyData.get("schedule_start_sending_at_hour"));
            survey.setScheduleStartSendingAtMinute((Integer) surveyData.get("schedule_start_sending_at_minute"));

            survey.setScheduleStopSendingAtHour((Integer) surveyData.get("schedule_stop_sending_at_hour"));
            survey.setScheduleStopSendingAtMinute((Integer) surveyData.get("schedule_stop_sending_at_minute"));

            survey.setScheduleDeliveryIntervalMinutes((Integer) surveyData.get("schedule_delivery_interval_minutes"));
            survey.setScheduleDeliveryVariationMinutes((Integer) surveyData.get("schedule_delivery_variation_minutes"));

            survey.setScheduleExpiryTimeMinutes((Integer) surveyData.get("schedule_survey_expiry_minutes"));

            survey.setScheduleAllowMonday((Boolean)surveyData.get("schedule_allow_monday"));
            survey.setScheduleAllowTuesday((Boolean)surveyData.get("schedule_allow_tuesday"));
            survey.setScheduleAllowWednesday((Boolean)surveyData.get("schedule_allow_wednesday"));
            survey.setScheduleAllowThursday((Boolean)surveyData.get("schedule_allow_thursday"));
            survey.setScheduleAllowFriday((Boolean)surveyData.get("schedule_allow_friday"));
            survey.setScheduleAllowSaturday((Boolean)surveyData.get("schedule_allow_saturday"));
            survey.setScheduleAllowSunday((Boolean)surveyData.get("schedule_allow_sunday"));

            survey.setRandomiseQuestionSetDisplayOrder((Boolean) surveyData.get("randomise_set_order"));

            List<Map<String, Object>> sets = (List<Map<String, Object>>)surveyData.get("question_sets");

            for (Map<String, Object> setData : sets) {

                QuestionSet set = realm.createObject(QuestionSet.class);

                set.setSurvey(survey);
                survey.getQuestionSets().add(set);

                set.setDbQuestionSetId((Integer)setData.get("id"));
                set.setRandomiseQuestionDisplayOrder((Boolean)setData.get("randomise_question_order"));

                List<Map<String, Object>> questions = (List<Map<String, Object>>)setData.get("questions");

                for (Map<String, Object> qData : questions) {

                    Question question = realm.createObject(Question.class);

                    question.setSet(set);
                    set.getQuestions().add(question);

                    question.setDbQuestionId((Integer) qData.get("id"));
                    question.setRandomiseChoiceDisplayOrder((Boolean) qData.get("randomise_option_order"));
                    question.setQuestionType((Integer) qData.get("question_type"));
                    question.setQuestionText((String) qData.get("question_text"));
                    question.setMaximumValue((Integer) qData.get("maximum_value"));
                    question.setMinimumValue((Integer) qData.get("minimum_value"));
                    question.setMaximumLabel((String) qData.get("maximum_label"));
                    question.setMinimumLabel((String) qData.get("minimum_label"));

                    List<Map<String, Object>> choices = (List<Map<String, Object>>)qData.get("options");

                    for (Map<String, Object> cData : choices) {

                        QuestionChoice choice = realm.createObject(QuestionChoice.class);

                        choice.setQuestion(question);
                        question.getChoices().add(choice);

                        choice.setDbChoiceId((Integer) cData.get("id"));
                        choice.setChoiceText((String) cData.get("label"));
                        // TODO: add default value?
                    }
                }
            }

            int triggerMode = survey.getAnswerTriggerMode();
            if (triggerMode == AnswerSetTriggerMode.SCHEDULE) {

                // setup multiple scheduled answer sets for this survey
                mDataManager.setUpAnswerSetsForSurvey(context, realm, program, survey);

            }
        }

//        // clear and reschedule up to 64 alarms across all surveys
//        mSurveyAlarmManager.setupAlarms(context);
    }

    @Override
    protected boolean shouldReRunOnThrowable(Throwable throwable) {
        // An error occurred in onRun.
        // Return value determines whether this job should retry running (true) or abort (false).
        return false; // TODO: consider adding a retry process
    }
    @Override
    protected void onCancel() {
        // Job has exceeded retry attempts or shouldReRunOnThrowable() has returned false.
    }
}
