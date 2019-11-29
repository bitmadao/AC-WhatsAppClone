package no.nanchinorth.ac_whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener{

    private TextInputEditText edtUsername;
    private TextInputEditText edtEmail;
    private TextInputEditText edtPassword;
    private TextInputEditText edtPasswordConfirm;

    private Button btnSignUp;
    private Button btnHaveAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        if(ParseUser.getCurrentUser() != null){
            transitionToWhatsAppActivity();
        }

        edtUsername = findViewById(R.id.textInputEditTextSignUpActivityUsername);
        edtEmail = findViewById(R.id.textInputEditTextSignUpActivityEmail);
        edtPassword = findViewById(R.id.textInputEditTextSignUpActivityPassword);
        edtPasswordConfirm = findViewById(R.id.textInputEditTextSignUpActivityPasswordConfirm);

        btnSignUp = findViewById(R.id.btnSignUpSignUp);
        btnHaveAccount = findViewById(R.id.btnSignUpHaveAccount);

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
                            String.format("%s signed up successfully!",username),
                            FancyToast.LENGTH_LONG,
                            FancyToast.SUCCESS,
                            true
                    ).show();
                    transitionToWhatsAppActivity();
                } else {
                    Log.i("APPTAG", e.getMessage());
                    FancyToast.makeText(
                            SignUpActivity.this,
                            getString(R.string.toast_generic_error),
                            FancyToast.LENGTH_LONG,
                            FancyToast.ERROR,
                            true
                    ).show();
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
