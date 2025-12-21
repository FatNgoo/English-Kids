package com.edu.english.shapes.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.edu.english.shapes.fragments.Scene1TraceFragment;
import com.edu.english.shapes.fragments.Scene2CharacterFragment;
import com.edu.english.shapes.fragments.Scene3VocabularyFragment;
import com.edu.english.shapes.fragments.Scene4SummaryFragment;

/**
 * Adapter for the 4 scenes within each shape learning flow
 */
public class ScenesPagerAdapter extends FragmentStateAdapter {

    private static final int SCENE_COUNT = 4;
    private final int shapeIndex;

    public ScenesPagerAdapter(@NonNull Fragment fragment, int shapeIndex) {
        super(fragment);
        this.shapeIndex = shapeIndex;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return Scene1TraceFragment.newInstance(shapeIndex);
            case 1:
                return Scene2CharacterFragment.newInstance(shapeIndex);
            case 2:
                return Scene3VocabularyFragment.newInstance(shapeIndex);
            case 3:
                return Scene4SummaryFragment.newInstance(shapeIndex);
            default:
                return Scene1TraceFragment.newInstance(shapeIndex);
        }
    }

    @Override
    public int getItemCount() {
        return SCENE_COUNT;
    }
}
