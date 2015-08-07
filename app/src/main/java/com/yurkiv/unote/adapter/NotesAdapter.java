package com.yurkiv.unote.adapter;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.yurkiv.unote.R;
import com.yurkiv.unote.model.Note;
import com.yurkiv.unote.util.Utility;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by yurkiv on 03.08.2015.
 */
public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private static String TAG=NotesAdapter.class.getSimpleName();

    private static SimpleDateFormat dateFormat=new SimpleDateFormat("dd MMM");
    private List<Note> noteList;
    private Context context;
    private static OnItemClickListener onItemClickListener;

    public NotesAdapter(Context context) {
        this.context=context;
    }

    public void setData(List<Note> noteList){
        this.noteList = noteList;
    }

    public static void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        NotesAdapter.onItemClickListener = onItemClickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.notes_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Note note=noteList.get(position);

        holder.titleRow.setText(Utility.styleText(note.getTitle()));
        holder.contentRow.setText(Utility.styleText(note.getContent()));
        holder.tvDate.setText(dateFormat.format(note.getUpdatedAt()));
        holder.tvIcon.setText(note.getTitle().substring(0,1).toUpperCase());
        GradientDrawable bgShape = (GradientDrawable)holder.llIcon.getBackground();
        bgShape.setColor(note.getColor());
    }

    @Override
    public int getItemCount() {
        return noteList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView titleRow;
        private TextView contentRow;
        private TextView tvDate;
        private TextView tvIcon;
        private LinearLayout llIcon;

        public ViewHolder(View itemView) {
            super(itemView);
            titleRow = (TextView) itemView.findViewById(R.id.tvTitle);
            contentRow = (TextView) itemView.findViewById(R.id.tvContent);
            tvIcon = (TextView) itemView.findViewById(R.id.tvNoteIcon);
            tvDate = (TextView) itemView.findViewById(R.id.tvDateItem);
            llIcon = (LinearLayout) itemView.findViewById(R.id.llNoteIcon);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            onItemClickListener.onItemClick(getPosition(), view);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(int position, View v);
    }
}
