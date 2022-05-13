package com.fyp.lawyer_project.client;

import android.app.ProgressDialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.common.LawPracticesAdapter;
import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.Constants;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.fyp.lawyer_project.utils.GridEqualSpacing;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.Objects;

public class LawyerPickerDialog extends BottomSheetDialog implements LawPracticesAdapter.OnItemClickListener {
    private ListLoader listLoader;
    private double startPrice = 0;
    private double endPrice = 0;
    private final ArrayList<View> allRadioButtons = new ArrayList<>();

    public LawyerPickerDialog(@NonNull Context context) {
        super(context);
        setContentView(R.layout.lawyer_selection_dialog);
        initGrid(context);
    }

    private void initPriceSelectionClickListeners() {
        ViewGroup viewGroup = findViewById(R.id.budget_layout_parents);
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

    private void initGrid(Context context) {
        RecyclerView recyclerView = findViewById(R.id.caseCategoriesList);
        recyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        recyclerView.addItemDecoration(new GridEqualSpacing(16));
        LawPracticesAdapter adapter = new LawPracticesAdapter(context);
        adapter.setOnItemClickListener(this);
        recyclerView.setAdapter(adapter);

    }

    public void setActionCallBack(ListLoader listLoader) {
        this.listLoader = listLoader;
    }

    @Override
    public void onItemClicked(int position) {
        Toast.makeText(getContext(), Constants.lawPracticesTitles[position], Toast.LENGTH_LONG).show();
        String castType = Constants.lawPracticesTitles[position];
        setContentView(R.layout.budget_layout);
        initPriceSelectionClickListeners();

        findViewById(R.id.searchLawyerBtn).setOnClickListener(view ->{
            ProgressDialog dialog = new ProgressDialog(getContext());
            dialog.show();
            FirebaseHelper.findLawyers(castType, startPrice, endPrice, new FirebaseHelper.FirebaseActions() {
                @Override
                public void onUserListLoaded(ArrayList<User> users) {
                    dialog.dismiss();
                    if (users.isEmpty()) {
                        Toast.makeText(getContext(), "No record found ", Toast.LENGTH_LONG).show();
                    } else {
                        listLoader.onLawyersListLoaded(users);
                        dismiss();
                    }
                }
            });
        });
        findViewById(R.id.back_btn).setOnClickListener(view -> {
            setContentView(R.layout.lawyer_selection_dialog);
            initGrid(getContext());
        });
    }

    interface ListLoader {
        void onLawyersListLoaded(ArrayList<User> lawyers);
    }

    class PriceRangeSelector implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            String[] prices = view.getTag().toString().split("-");
            startPrice = Double.parseDouble(prices[0]);
            endPrice = Double.parseDouble(prices[1]);
            Log.e("Priceses", " Start = " + startPrice + " and End = " + endPrice);
            for (View button : allRadioButtons) {
                if (button != view) {
                    ((RadioButton) button).setChecked(false);
                }
            }
        }
    }
}
