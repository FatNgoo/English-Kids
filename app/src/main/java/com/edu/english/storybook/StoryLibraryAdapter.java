package com.edu.english.storybook;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.storybook.data.StoryEntity;
import com.edu.english.storybook.model.StoryCategory;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying saved stories in the library.
 */
public class StoryLibraryAdapter extends RecyclerView.Adapter<StoryLibraryAdapter.ViewHolder> {

    public interface OnStoryClickListener {
        void onStoryClick(StoryEntity story);
        void onDeleteClick(StoryEntity story);
    }

    private final Context context;
    private final OnStoryClickListener listener;
    private List<StoryEntity> stories = new ArrayList<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.US);

    public StoryLibraryAdapter(Context context, OnStoryClickListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public void setStories(List<StoryEntity> stories) {
        this.stories = stories;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_library_story, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StoryEntity story = stories.get(position);
        holder.bind(story);
    }

    @Override
    public int getItemCount() {
        return stories.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private final View viewCategoryBadge;
        private final TextView tvCategory;
        private final TextView tvTitle;
        private final TextView tvInfo;
        private final TextView tvDate;
        private final ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            viewCategoryBadge = itemView.findViewById(R.id.view_category_badge);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvTitle = itemView.findViewById(R.id.tv_title);
            tvInfo = itemView.findViewById(R.id.tv_info);
            tvDate = itemView.findViewById(R.id.tv_date);
            btnDelete = itemView.findViewById(R.id.btn_delete);
        }

        void bind(StoryEntity story) {
            tvTitle.setText(story.getTitle());
            tvInfo.setText("Age " + story.getTargetAge() + " • " + story.getLevel() + " Level");
            tvDate.setText("Saved on " + dateFormat.format(new Date(story.getCreatedAt())));

            // Set category
            try {
                StoryCategory category = StoryCategory.valueOf(story.getCategory());
                tvCategory.setText(category.getDisplayName());
                setCategoryColor(category);
            } catch (Exception e) {
                tvCategory.setText("Story");
            }

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStoryClick(story);
                }
            });

            btnDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(story);
                }
            });
        }

        private void setCategoryColor(StoryCategory category) {
            int bgRes;
            switch (category) {
                case FAIRY_TALES:
                    bgRes = R.drawable.bg_category_fairytales;
                    break;
                case SEE_THE_WORLD:
                    bgRes = R.drawable.bg_category_world;
                    break;
                case HISTORY:
                    bgRes = R.drawable.bg_category_history;
                    break;
                default:
                    bgRes = R.drawable.bg_category_novel;
            }
            viewCategoryBadge.setBackgroundResource(bgRes);
        }
    }
}
