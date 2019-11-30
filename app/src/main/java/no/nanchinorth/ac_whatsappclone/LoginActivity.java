package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    private TextInputEditText edtUsername;
    private TextInputEditText edtPassword;

    private Button btnLogin;
    private Button btnNeedAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if(ParseUser.getCurrentUser() != null){
            transitionToWhatsAppActivity();
        }

        edtUsername = findViewById(R.id.textInputEditTextLoginActivityUsername);
        edtPassword = findViewById(R.id.textInputEditTextLoginActivityPassword);

        btnLogin = findViewById(R.id.btnLoginActivityLogin);
        btnNeedAccount = findViewById(R.id.btnLoginActivityNeedAccount);


        edtPassword.setOnKeyListener(LoginActivity.this);
        btnLogin.setOnClickListener(LoginActivity.this);
        btnNeedAccount.setOnClickListener(LoginActivity.this);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        if(v.getId() == edtPassword.getId()) {
            if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                btnLoginTapped();
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnLoginActivityLogin:
                btnLoginTapped();
                break;

            case R.id.btnLoginActivityNeedAccount:
                transitionToSignUpActivity();
                break;
        }

    }

    private void btnLoginTapped() {
        boolean objection = false;
        String username;
        String password;
        StringBuilder stringBuilder = new StringBuilder();

        if (edtUsername.getText().toString().isEmpty()) {
            objection = true;
            stringBuilder.append("No username?\n");
        }

        if (edtPassword.getText().toString().isEmpty()) {
            objection = true;
            stringBuilder.append("No password?");
        }

        if (objection) {
            Toast.makeText(this, stringBuilder.toString(), Toast.LENGTH_LONG).show();
            return;
        }

        username = edtUsername.getText().toString().toLowerCase();
        password = edtPassword.getText().toString();

        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                if(e == null) {
                    transitionToWhatsAppActivity();
                } else if (e.getMessage().equals("Invalid username/password.")){
                    Toast.makeText(LoginActivity.this, "Can't find that user/password-combo", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("APPTAG", e.getMessage());
                }
            }
        });

    }

    private void transitionToSignUpActivity(){
        startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
        finish();
    }

    private void transitionToWhatsAppActivity(){
        startActivity(new Intent(LoginActivity.this, WhatsAppActivity.class));
        finish();
    }
}
