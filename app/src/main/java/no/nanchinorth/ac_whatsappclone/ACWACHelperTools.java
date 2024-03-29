package no.nanchinorth.ac_whatsappclone;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import static android.content.Context.INPUT_METHOD_SERVICE;

class ACWACHelperTools {

    static void hideSoftKeyboard(Context context, View activityLayout) {

        try {
            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(activityLayout.getWindowToken(), 0);
        } catch(Exception e) {
            logAndFancyToastException(context, e);
        }

    }

    static void logoutParseUser(final Context context, final AppCompatActivity activity){
        ParseUser.logOutInBackground(new LogOutCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {

                    Intent intent = new Intent(context, LoginActivity.class);
                    activity.startActivity(intent);
                    activity.finish();
                }

            }
        });
    }

    static void logAndFancyToastException(Context context, Exception e){
        Log.i("APPTAG", e.getMessage());
        FancyToast.makeText(
                context,
                context.getString(R.string.toast_generic_error),
                FancyToast.LENGTH_LONG,
                FancyToast.ERROR,
                true)
            .show();
    }
}
