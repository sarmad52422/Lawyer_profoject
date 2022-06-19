package com.fyp.common;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.modal_classes.Comment;

import java.util.ArrayList;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {
    private ArrayList<Comment> comments;
    private Context iContext;
    public CommentAdapter(Context iContext,ArrayList<Comment> comments){
        this.iContext = iContext;
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(iContext).inflate(R.layout.comment_item,parent,false);
        return new CommentHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.commnetView.setText(comment.getComment());
        holder.dateView.setText(comment.getDate());
        holder.numberView.setText(String.valueOf(position+1));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentHolder extends RecyclerView.ViewHolder{
        TextView commnetView;
        TextView dateView;
        TextView numberView;
        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            numberView = itemView.findViewById(R.id.numbring);
            commnetView = (TextView)itemView.findViewById(R.id.commentBox);
            dateView = itemView.findViewById(R.id.dateBox);
        }
    }
}
