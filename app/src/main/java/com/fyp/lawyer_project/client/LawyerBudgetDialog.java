package com.fyp.lawyer_project.client;

import android.content.Context;

import androidx.annotation.NonNull;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class LawyerBudgetDialog extends BottomSheetDialog {
    public LawyerBudgetDialog(@NonNull Context context,String caseType) {
        super(context);

    }
}
