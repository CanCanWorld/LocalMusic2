package com.et.localmusic2.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.et.localmusic2.R;
import com.et.localmusic2.bean.Song;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.InnerHolder> {

    private static final String TAG = "MusicAdapter";
    private final Context mContext;
    private final List<Song> list;
    private OnClickListener onClickListener;

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void setOnClickListener(int position);
    }

    public MusicAdapter(Context context, List<Song> list) {
        this.mContext = context;
        this.list = list;
    }

    @NonNull
    @Override
    public InnerHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_music, parent, false);
        return new InnerHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InnerHolder holder, int position) {
        holder.setData(position);
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class InnerHolder extends RecyclerView.ViewHolder {

        private final TextView mTvId;
        private final TextView mTvSongName;
        private final TextView mTvSinger;
        private final TextView mTvDuration;

        public InnerHolder(@NonNull View itemView) {
            super(itemView);
            mTvId = itemView.findViewById(R.id.tv_position);
            mTvSongName = itemView.findViewById(R.id.tv_song_name);
            mTvSinger = itemView.findViewById(R.id.tv_singer);
            mTvDuration = itemView.findViewById(R.id.tv_duration);
        }

        public void setData(int position) {
            Song song = list.get(position);
            Log.d(TAG, "setData: " + song);
            mTvId.setText(String.valueOf(position + 1));
            mTvSongName.setText(song.getSong());
            mTvSinger.setText(song.getSinger());
            mTvDuration.setText(formatDuration(song.getDuration()));
            itemView.setOnClickListener(v -> {
                onClickListener.setOnClickListener(position);
            });
            if (song.isCheck()) {
                mTvId.setTextColor(ContextCompat.getColor(mContext, R.color.gold_color));
                mTvSongName.setTextColor(ContextCompat.getColor(mContext, R.color.gold_color));
                mTvSinger.setTextColor(ContextCompat.getColor(mContext, R.color.gold_color));
                mTvDuration.setTextColor(ContextCompat.getColor(mContext, R.color.gold_color));
            } else {
                mTvId.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                mTvSongName.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                mTvSinger.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                mTvDuration.setTextColor(ContextCompat.getColor(mContext, R.color.white));
            }
        }

        private String formatDuration(int duration) {
            int m = duration / (60 * 1000);
            int s = (duration - 60 * 1000 * m) / 1000;
            return m + ":" + (String.valueOf(s).length() == 1 ? "0" + s : s);
        }
    }
}
