package com.edu.english.storybook;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.storybook.model.Story;
import com.edu.english.storybook.model.VocabularyItem;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

/**
 * Bottom sheet displaying vocabulary words from the story.
 */
public class VocabularyBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_STORY_JSON = "story_json";

    private Story story;

    public static VocabularyBottomSheet newInstance(String storyJson) {
        VocabularyBottomSheet fragment = new VocabularyBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_STORY_JSON, storyJson);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            story = Story.fromJson(getArguments().getString(ARG_STORY_JSON));
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_vocabulary, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (story == null || story.getVocabulary() == null) {
            dismiss();
            return;
        }

        List<VocabularyItem> vocabulary = story.getVocabulary();

        TextView tvVocabCount = view.findViewById(R.id.tv_vocab_count);
        tvVocabCount.setText(vocabulary.size() + " words to learn");

        RecyclerView rvVocabulary = view.findViewById(R.id.rv_vocabulary);
        rvVocabulary.setLayoutManager(new LinearLayoutManager(getContext()));
        rvVocabulary.setAdapter(new VocabularyAdapter(vocabulary));
    }

    private static class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.ViewHolder> {
        private final List<VocabularyItem> items;

        VocabularyAdapter(List<VocabularyItem> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_vocabulary, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            private final TextView tvWord;
            private final TextView tvMeaning;
            private final TextView tvExample;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvWord = itemView.findViewById(R.id.tv_word);
                tvMeaning = itemView.findViewById(R.id.tv_meaning);
                tvExample = itemView.findViewById(R.id.tv_example);
            }

            void bind(VocabularyItem item) {
                tvWord.setText(item.getWord());
                tvMeaning.setText(item.getMeaningVi());
                tvExample.setText("\"" + item.getExampleSentence() + "\"");
            }
        }
    }
}
