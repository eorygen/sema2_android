package com.orygenapps.sema.activity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.cengalabs.flatui.views.FlatButton;
import com.cengalabs.flatui.views.FlatTextView;
import com.orygenapps.sema.R;
import com.orygenapps.sema.data.Program;
import com.orygenapps.sema.data.Survey;

import io.realm.Realm;
import io.realm.RealmList;

public class ProgramActivity extends ActionBarActivity {

    private int mDbProgramId;
    private Program mProgram;
    private TextView mTitle;
    private TextView mDescription;
    private TextView mContactName;
    private TextView mContactNumber;
    private TextView mContactEmail;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_program);
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }

        Bundle bundle = getIntent().getExtras();
        try {
            mDbProgramId = bundle.getInt("programId");
        }
        catch (Exception e) {
            // TODO
        }

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            mProgram = realm.where(Program.class).equalTo("dbProgramId", mDbProgramId).findFirst();

            mTitle = (TextView) findViewById(R.id.program_title);
            mTitle.setText(mProgram.getDisplayName());

            mDescription = (TextView) findViewById(R.id.program_description);
            mDescription.setText(mProgram.getDescription());

            mContactName = (TextView) findViewById(R.id.program_contact_name);
            mContactName.setText(mProgram.getContactName());

            mContactNumber = (TextView) findViewById(R.id.program_contact_phone);
            mContactNumber.setText(mProgram.getContactNumber());

            mContactEmail = (TextView) findViewById(R.id.program_contact_email);
            mContactEmail.setText(mProgram.getContactEmail());

            // Email Button
            FlatButton emailButton = (FlatButton) findViewById(R.id.program_email_button);
            if (mContactEmail.getText().equals("")) {
                emailButton.setEnabled(false);
            }
            emailButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    email();
                }
            });

            // Phone Button
            FlatButton phoneButton = (FlatButton) findViewById(R.id.program_phone_button);
            if (mContactNumber.getText().equals("")) {
                phoneButton.setEnabled(false);
            }
            phoneButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    call();
                }
            });

            // Demo Survey Button
            FlatButton demoSurveyButton = (FlatButton) findViewById(R.id.demo_survey);
            RealmList<Survey> surveys = mProgram.getSurveys();
            if (surveys.size() == 0) {
                demoSurveyButton.setEnabled(false);
            }

            demoSurveyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    launchDemoSurvey();
                }
            });
        }

        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    void email() {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, mContactEmail.getText());
            intent.putExtra(Intent.EXTRA_SUBJECT, "SEMA - " + mProgram.getDisplayName());
            intent.setData(Uri.parse("mailto:" + mContactEmail.getText().toString()));
            startActivity(Intent.createChooser(intent, "Email program contact"));
        } catch (ActivityNotFoundException e) {
            Log.e("Email", "Email intent failed", e);
        }
    }

    void call() {
        try {

            String number = mContactNumber.getText().toString();

            if (number.length() > 0) {

                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + number));
                startActivity(Intent.createChooser(intent, "Call program contact"));
            }
        } catch (ActivityNotFoundException e) {
            Log.e("Phone", "Call intent failed", e);
        }
    }

    void launchDemoSurvey() {

        // TODO: display a list of surveys (if the user is a participant in multiple surveys for this program)

        Realm realm = null;

        try {
            realm = Realm.getDefaultInstance();

            RealmList<Survey> surveys = mProgram.getSurveys();
            if (surveys.size() > 0) {
                Survey survey = surveys.get(0); // TODO: for now I am just getting the first in the list

                Intent intent = new Intent(ProgramActivity.this, DemoSurveyActivity.class);
                intent.putExtra("programId", mDbProgramId);
                intent.putExtra("surveyId", survey.getDbSurveyId());
                startActivity(intent);
            } else {
                // do nothing
            }

        }
        finally {
            if (realm != null) {
                realm.close();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_program, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_program, container, false);
            return rootView;
        }
    }
}
