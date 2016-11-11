package com.orygenapps.sema.activity;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import com.orygenapps.sema.R;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ForgotPasswordActivity extends Activity {

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private ResetPasswordTask mResetPasswordTask = null;

    private SEMAApplication mApplication;
    private SEMAService mService;
    private EditText mEmailAddressTextView;
    private Button mSendEmailButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        mApplication = SEMAApplication.getInstance();
        mService = mApplication.getAPI();

        mEmailAddressTextView = (EditText) findViewById(R.id.email_address);

        mSendEmailButton = (Button) findViewById(R.id.send_email_button);
        mSendEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendTapped();
            }
        });
    }

    void sendTapped() {
        if (!mEmailAddressTextView.getText().equals("")) {
            // TODO: Show a progress spinner, and kick off a background task to perform the user login attempt.
            String email = mEmailAddressTextView.getText().toString();
            mResetPasswordTask = new ResetPasswordTask(email);
            mResetPasswordTask.execute((Void) null);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class ResetPasswordTask extends AsyncTask<Void, Void, Integer> {

        private final String mEmail;

        ResetPasswordTask(String email) {
            mEmail = email;
        }

        @Override
        protected Integer doInBackground(Void... params) {

            try {
                // POST
                Response r = mService.resetPassword(mEmail);
            } catch (RetrofitError e) {
                Response response = e.getResponse();
                if (response == null) { // occurs if no network access etc.
                    return -1;
                } else {
                    int statusCode = e.getResponse().getStatus();
                    return statusCode;
                }
            }
            return 200; // completed ok
        }

        @Override
        protected void onPostExecute(final Integer status) {
            mResetPasswordTask = null;
        }

        // TODO: work out if need this still
        @Override
        protected void onCancelled() {
            mResetPasswordTask = null;
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login_trouble, menu);
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
}
