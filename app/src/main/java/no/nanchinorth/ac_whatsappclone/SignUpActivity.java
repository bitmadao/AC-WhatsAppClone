package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.hideSoftKeyboard;
import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logAndFancyToastException;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener, View.OnTouchListener {


    private ConstraintLayout constraintLayout;
    private TextInputEditText edtUsername;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtPasswordConfirm;

    private Button btnSignUp;
    private Button btnHaveAccount;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(v.getId() == constraintLayout.getId()){
            hideSoftKeyboard(SignUpActivity.this, v);
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if(ParseUser.getCurrentUser() != null){
            transitionToWhatsAppActivity();
        }

        constraintLayout = findViewById(R.id.constraintLayoutSignUpActivityRoot);

        edtUsername = findViewById(R.id.textInputEditTextSignUpActivityUsername);
        edtEmail = findViewById(R.id.textInputEditTextSignUpActivityEmail);
        edtPassword = findViewById(R.id.textInputEditTextSignUpActivityPassword);
        edtPasswordConfirm = findViewById(R.id.textInputEditTextSignUpActivityPasswordConfirm);

        btnSignUp = findViewById(R.id.btnSignUpSignUp);
        btnHaveAccount = findViewById(R.id.btnSignUpHaveAccount);

        constraintLayout.setOnTouchListener(SignUpActivity.this);

        edtPasswordConfirm.setOnKeyListener(SignUpActivity.this);

        btnSignUp.setOnClickListener(SignUpActivity.this);
        btnHaveAccount.setOnClickListener(SignUpActivity.this);

    }

    @Override
    public void onClick(View v) {

        switch(v.getId()){
            case R.id.btnSignUpSignUp:
                btnSignUpTapped();
                break;

            case R.id.btnSignUpHaveAccount:
                transitionToLoginActivity();
                break;
        }

    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(v.getId() == edtPasswordConfirm.getId()){
            if(keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN){
                btnSignUpTapped();
                hideSoftKeyboard(SignUpActivity.this, v);
            }
        }

        return false;
    }

    private void btnSignUpTapped(){
        final String username;
        boolean objection = false;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Please have a look at the following issues:\n");

        if(edtUsername.getText().toString().isEmpty()){
            objection = true;
            stringBuilder.append("You must supply a password.");
        }

        if(edtEmail.getText().toString().isEmpty()){
            objection = true;
            stringBuilder.append("You must supply an email address.\n");
        }

        if(edtPassword.getText().toString().isEmpty() ||
                edtPassword.getText().toString().trim().length() <= 3){
            objection = true;
            stringBuilder.append("You must choose a password longer than 3 non-whitespace characters and confirm it.\n");
        } else if (!edtPassword.getText().toString().equals(edtPasswordConfirm.getText().toString())){
            objection = true;
            stringBuilder.append("The password and password confirmation fields currently does not match.");
        }

        if(objection){
            FancyToast.makeText(
                    SignUpActivity.this,
                    stringBuilder.toString(),
                    FancyToast.LENGTH_LONG,
                    FancyToast.CONFUSING,
                    true
            ).show();

            return;

        }

        username = edtUsername.getText().toString().toLowerCase();

        ParseUser parseUser = new ParseUser();
        parseUser.setUsername(edtUsername.getText().toString().toLowerCase());
        parseUser.setEmail(edtEmail.getText().toString().toLowerCase());
        parseUser.setPassword(edtPasswordConfirm.getText().toString());


        parseUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    FancyToast.makeText(
                            SignUpActivity.this,
                            String.format(getString(R.string.toast_activity_sign_up_successful),username),
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            true
                    ).show();
                    transitionToWhatsAppActivity();
                } else {
                    logAndFancyToastException(SignUpActivity.this, e);
                }
            }
        });

    }

    private void transitionToWhatsAppActivity(){
        startActivity(new Intent(SignUpActivity.this, WhatsAppActivity.class));
        finish();
    }

    private void transitionToLoginActivity(){
        startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
        finish();
    }
}
