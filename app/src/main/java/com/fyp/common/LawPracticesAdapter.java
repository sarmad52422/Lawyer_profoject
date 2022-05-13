package com.fyp.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.utils.Constants;

import java.util.zip.Inflater;

public class LawPracticesAdapter extends RecyclerView.Adapter<LawPracticesAdapter.CategoryHolder> {
    private Context iContext;
    private OnItemClickListener onItemClickListener;
    public LawPracticesAdapter(Context iContext){
        this.iContext = iContext;
    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(iContext).inflate(R.layout.practice_list_item,parent,false);
        return new CategoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryHolder holder, int position) {
        holder.practiceTitle.setText(Constants.lawPracticesTitles[position]);
        holder.lawPracticeImage.setImageResource(Constants.lawPracticesImages[position]);
        holder.lawPracticeImage.setOnClickListener(v->onItemClickListener.onItemClicked(position));

    }
    public void setOnItemClickListener(OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }
    @Override
    public int getItemCount() {
        return Constants.lawPracticesImages.length;
    }

    class CategoryHolder extends RecyclerView.ViewHolder{
        ImageFilterView lawPracticeImage;
        TextView practiceTitle;
        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            lawPracticeImage = itemView.findViewById(R.id.practice_Image);
            practiceTitle = itemView.findViewById(R.id.practice_Text);
        }
    }
    public interface OnItemClickListener {
        void onItemClicked(int position);
    }
}
