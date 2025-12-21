package com.edu.english.shapes.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.edu.english.shapes.fragments.ShapeLearningFragment;
import com.edu.english.shapes.models.Shape;

import java.util.List;

/**
 * Adapter for ViewPager2 to handle shape learning fragments
 */
public class ShapesPagerAdapter extends FragmentStateAdapter {

    private final List<Shape> shapes;

    public ShapesPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Shape> shapes) {
        super(fragmentActivity);
        this.shapes = shapes;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return ShapeLearningFragment.newInstance(position);
    }

    @Override
    public int getItemCount() {
        return shapes.size();
    }
}
