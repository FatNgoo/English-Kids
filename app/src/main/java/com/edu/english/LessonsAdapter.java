package com.edu.english;

import android.animation.ObjectAnimator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LessonsAdapter extends RecyclerView.Adapter<LessonsAdapter.LessonViewHolder> {

    private List<LessonItem> lessons;
    private OnLessonClickListener listener;

    public interface OnLessonClickListener {
        void onLessonClick(LessonItem lesson);
    }

    public LessonsAdapter(List<LessonItem> lessons, OnLessonClickListener listener) {
        this.lessons = lessons;
        this.listener = listener;
    }

    @NonNull
    @Override
    public LessonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lesson, parent, false);
        return new LessonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LessonViewHolder holder, int position) {
        LessonItem lesson = lessons.get(position);
        holder.bind(lesson);
    }

    @Override
    public int getItemCount() {
        return lessons.size();
    }

    class LessonViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout container;
        private TextView titleText;

        public LessonViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.lesson_container);
            titleText = itemView.findViewById(R.id.lesson_title);
        }

        public void bind(LessonItem lesson) {
            titleText.setText(lesson.getTitle());
            
            // Use background images for each lesson
            switch (lesson.getTitle()) {
                case "Alphabet":
                    container.setBackgroundResource(R.drawable.bg_alphabet);
                    break;
                case "Numbers":
                    container.setBackgroundResource(R.drawable.bg_number);
                    break;
                case "Colors":
                    container.setBackgroundResource(R.drawable.bg_color);
                    break;
                case "Shapes":
                    container.setBackgroundResource(R.drawable.bg_shape);
                    break;
                case "Animals":
                    container.setBackgroundResource(R.drawable.bg_animal);
                    break;
                default:
                    container.setBackgroundColor(lesson.getBackgroundColor());
                    break;
            }

            // Set click listener with animation
            itemView.setOnClickListener(v -> {
                animateClick(v);
                if (listener != null) {
                    listener.onLessonClick(lesson);
                }
            });
        }

        private void animateClick(View view) {
            ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(view, "scaleX", 1f, 0.95f);
            ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(view, "scaleY", 1f, 0.95f);
            scaleDownX.setDuration(100);
            scaleDownY.setDuration(100);

            ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(view, "scaleX", 0.95f, 1f);
            ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(view, "scaleY", 0.95f, 1f);
            scaleUpX.setDuration(100);
            scaleUpY.setDuration(100);
            scaleUpX.setStartDelay(100);
            scaleUpY.setStartDelay(100);

            scaleDownX.start();
            scaleDownY.start();
            scaleUpX.start();
            scaleUpY.start();
        }
    }
}
