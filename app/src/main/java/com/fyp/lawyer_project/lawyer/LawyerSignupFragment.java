package com.fyp.lawyer_project.lawyer;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Lawyer;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Objects;

public class LawyerSignupFragment extends RootFragment {
    private View rootView;
    private MainFragmentActivity callBackHandler;
    private final ArrayList<View> allRadioButtons = new ArrayList<>();
    private double startPrice = 0d;
    private double endPrice = 0d;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.activity_lawyer_signup, container, false);
        rootView.findViewById(R.id.submit_details_btn).setOnClickListener(view -> signUp());
        rootView.findViewById(R.id.priceRange).setOnClickListener(view -> openPriceRangeSelector());
        return rootView;
    }

    private void openPriceRangeSelector() {
        Dialog dialog = new Dialog(rootView.getContext());
        dialog.setContentView(R.layout.budget_layout);
        initPriceSelectionListener(dialog);
        dialog.show();
        Button postiveButton = ((Button) dialog.findViewById(R.id.searchLawyerBtn));
        Button negtiveButton = ((Button) dialog.findViewById(R.id.back_btn));
        postiveButton.setText("OK");
        negtiveButton.setOnClickListener(view -> dialog.dismiss());
        postiveButton.setOnClickListener(view -> {
            ((TextView) rootView.findViewById(R.id.priceRange)).setText(startPrice + " - " + endPrice);
            dialog.dismiss();
        });

        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void initPriceSelectionListener(Dialog dialog) {
        ViewGroup viewGroup = dialog.findViewById(R.id.budget_layout_parents);
        for (int i = 0; i < Objects.requireNonNull(viewGroup).getChildCount(); i++) {
            ViewGroup innerContainer = (ViewGroup) viewGroup.getChildAt(i);
            View v1 = innerContainer.getChildAt(0);
            View v2 = innerContainer.getChildAt(1);
            v1.setOnClickListener(new PriceRangeSelector());
            v2.setOnClickListener(new PriceRangeSelector());
            allRadioButtons.add(v1);
            allRadioButtons.add(v2);
        }

    }

    private void signUp() {
        String name = ((EditText) rootView.findViewById(R.id.lawyerusernamefield)).getText().toString().trim();
        String email = ((EditText) rootView.findViewById(R.id.lawyeremailfield)).getText().toString().trim();
        String phoneNumber = ((EditText) rootView.findViewById(R.id.phoneNumberField)).getText().toString().trim();
        String fieldArea = ((Spinner) rootView.findViewById(R.id.practiceField)).getSelectedItem().toString();
        String pass = ((EditText) rootView.findViewById(R.id.passwfield)).getText().toString().trim();
        String confirmPass = ((EditText) rootView.findViewById(R.id.confirmfield)).getText().toString().trim();
        if (!name.isEmpty() && !email.isEmpty() && !phoneNumber.isEmpty() && !fieldArea.isEmpty() && !pass.isEmpty() && startPrice > 0 && endPrice > 0) {
            if (pass.equals(confirmPass)) {
                Lawyer lawyer = new Lawyer(User.TYPE_LAWYER, name, email, pass, phoneNumber, fieldArea, startPrice, endPrice);
                FirebaseHelper.signUpUser(lawyer, new ProgressDialog(rootView.getContext()), new FirebaseHelper.FirebaseActions() {
                    @Override
                    public void onSignupComplete(String status) {
                        Toast.makeText(getContext(), status, Toast.LENGTH_LONG).show();
                        getFragmentManager().popBackStack();
                        FirebaseAuth.getInstance().signOut();
                    }
                });
            } else {
                Toast.makeText(getContext(), "Password does not match", Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getContext(), "Fill in all fields", Toast.LENGTH_LONG).show();
        }
    }

    class PriceRangeSelector implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String[] prices = view.getTag().toString().split("-");
            startPrice = Double.parseDouble(prices[0]);
            endPrice = Double.parseDouble(prices[1]);
            for (View button : allRadioButtons) {
                if (button != view) {
                    ((RadioButton) button).setChecked(false);
                }
            }
        }
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
        this.callBackHandler = callbacks;
    }
}
