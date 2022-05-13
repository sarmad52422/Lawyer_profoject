package com.fyp.lawyer_project.lawyer;

import android.app.Dialog;
import android.content.Context;
import android.widget.EditText;

import androidx.annotation.NonNull;

import com.fyp.lawyer_project.R;

public  class CustomDialogs {
    static class ZoomLoginDialog extends Dialog{
        LoginWindowCallBack callBack;
        public ZoomLoginDialog(@NonNull Context context,LoginWindowCallBack callBack) {
            super(context);
            this.callBack = callBack;
            setContentView(R.layout.login_window);
            String userName = ((EditText)findViewById(R.id.usernamefield)).getText().toString();
            String password = ((EditText)findViewById(R.id.passwfield)).getText().toString();
            findViewById(R.id.loginBtn).setOnClickListener(view->callBack.onLoginButtonClicked(userName,password));
            findViewById(R.id.cancelBtn).setOnClickListener(view->dismiss());

        }
        interface  LoginWindowCallBack{
            void onLoginButtonClicked(String userName,String password);
        }
    }
}
