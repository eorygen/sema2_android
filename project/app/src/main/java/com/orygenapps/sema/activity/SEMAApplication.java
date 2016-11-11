package com.orygenapps.sema.activity;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

import com.cengalabs.flatui.FlatUI;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.orygenapps.sema.Migration;
import com.orygenapps.sema.SyncFinishedEvent;
import com.orygenapps.sema.SyncStartedEvent;
import com.orygenapps.sema.data.AnswerSet;
import com.orygenapps.sema.data.Constants;
import com.path.android.jobqueue.JobManager;
import com.orygenapps.sema.SurveyAlarmManager;
import com.orygenapps.sema.data.SurveyDataManager;
import com.orygenapps.sema.data.User;
import com.squareup.okhttp.OkHttpClient;

import io.fabric.sdk.android.Fabric;
import java.io.File;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.exceptions.RealmMigrationNeededException;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.OkClient;

/**
 * Created by ashemah on 15/02/2015.
 */

public class SEMAApplication extends Application {

    private static SEMAApplication instance;
    private SurveyDataManager mDataManager;
    private SurveyAlarmManager mSurveyAlarmManager;
    private SEMAService mSemaService;
    private JobManager mJobManager;
    private GoogleCloudMessaging mGCM;
    private String mRegId;
    public boolean mIsSynchronising;


    public SEMAApplication() {
        instance = this;
    }

    public static SEMAApplication getInstance() {
        return instance;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        RealmConfiguration config0 = new RealmConfiguration.Builder(this)
                .schemaVersion(4)
                .migration(new Migration())
                .build();

        try {
            Realm realm1 = Realm.getInstance(config0);
        }
        catch (RealmMigrationNeededException e) {
            Realm.migrateRealm(config0, new Migration());
        }

        Realm.setDefaultConfiguration(config0);

        FlatUI.initDefaultValues(this);
        FlatUI.setDefaultTheme(FlatUI.CANDY);

        mDataManager = new SurveyDataManager();

        mSurveyAlarmManager = new SurveyAlarmManager();

        // Define the interceptor, add authentication headers
        RequestInterceptor requestInterceptor = new RequestInterceptor() {
            @Override
            public void intercept(RequestInterceptor.RequestFacade request) {

                // Get any existing token from the database if available
                Realm realm = null;

                try {
                    realm = Realm.getDefaultInstance();

                    User user = getCurrentUser(realm);

                    if (user != null) { // user exists so get the token
                        String token = user.getAuthToken();
                        request.addHeader("Authorization", "JWT " + token);
                    }
                }
                finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
            }
        };

        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("https://sema-surveys.com/")
//                .setEndpoint("http://exo:8000")
                .setClient(new OkClient(new OkHttpClient()))
                .setRequestInterceptor(requestInterceptor)
                .build();

        mSemaService = restAdapter.create(SEMAService.class);

        mJobManager = new JobManager(getApplicationContext());

        getRegId();
    }

    public SurveyDataManager getDataManager() {
        return mDataManager;
    }

    public SurveyAlarmManager getSurveyAlarmManager() {
        return mSurveyAlarmManager;
    }

    public JobManager getJobManager() {
        return mJobManager;
    }

    public String getCurrentUsername() {
        SharedPreferences sharedPref = getSharedPreferences("SEMA", Context.MODE_PRIVATE);
        String lastUsername = sharedPref.getString("current_username", "");
        return lastUsername;
    }

    public User getCurrentUser(Realm realm) {

        String currentUsername = getCurrentUsername();
        if (currentUsername != null) {
            User user = realm.where(User.class).equalTo("username", currentUsername).findFirst();
            return user;
        } else {
            return null;
        }
    }

    public void setCurrentUsername(String username) {
        SharedPreferences sharedPref = getSharedPreferences("SEMA", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("current_username", username);
        editor.commit();
    }

    public void getRegId() {

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {

                String msg = "";

                try {
                    if (mGCM == null) {
                        mGCM = GoogleCloudMessaging.getInstance(getApplicationContext());
                    }

                    mRegId = mGCM.register(Constants.PROJECT_NUMBER);
                    msg = "Device registered, registration ID = " + mRegId;

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();

                }
                return msg;
            }

            @Override
            protected void onPostExecute(String msg) {

                Realm realm = null;

                try {
                    realm = Realm.getDefaultInstance();
                    User userProfile = realm.where(User.class).findFirst();

                    realm.beginTransaction();

                    if (userProfile == null) {
                        userProfile = realm.createObject(User.class);
                    }

                    if (mRegId != null) {
                        userProfile.setPushToken(mRegId);
                    } else {
                        userProfile.setPushToken("<invalid>");
                    }

                    realm.commitTransaction();
                }

                finally {
                    if (realm != null) {
                        realm.close();
                    }
                }
            }

        }.execute(null, null, null);
    }

    public SEMAService getAPI() {
        return mSemaService;
    }

    /*
    public boolean isSynchronising() {

        if (mJobManager.count() > 0) {
            return true;
        }
        else {
            return false;
        }
    }
    */
}