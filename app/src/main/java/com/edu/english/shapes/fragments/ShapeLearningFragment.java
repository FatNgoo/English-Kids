package com.edu.english.shapes.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.edu.english.R;
import com.edu.english.shapes.adapters.ScenesPagerAdapter;
import com.edu.english.shapes.data.ShapeDataProvider;
import com.edu.english.shapes.models.Shape;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

/**
 * Main fragment for learning a single shape
 * Contains ViewPager2 for 4 scenes: Trace, Character, Vocabulary, Summary
 */
public class ShapeLearningFragment extends Fragment {

    private static final String ARG_SHAPE_INDEX = "shape_index";

    private int shapeIndex;
    private Shape shape;
    private ViewPager2 scenesViewPager;
    private TabLayout tabLayout;

    public static ShapeLearningFragment newInstance(int shapeIndex) {
        ShapeLearningFragment fragment = new ShapeLearningFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SHAPE_INDEX, shapeIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            shapeIndex = getArguments().getInt(ARG_SHAPE_INDEX, 0);
        }
        shape = ShapeDataProvider.getInstance().getShapeByIndex(shapeIndex);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_shape_learning, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        scenesViewPager = view.findViewById(R.id.scenesViewPager);
        tabLayout = view.findViewById(R.id.tabLayout);

        setupScenesPager();
    }

    private void setupScenesPager() {
        ScenesPagerAdapter adapter = new ScenesPagerAdapter(this, shapeIndex);
        scenesViewPager.setAdapter(adapter);
        scenesViewPager.setUserInputEnabled(false); // Disable swipe, use buttons

        // Add page change callback for scene transitions
        scenesViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update tab indicators
                updateTabSelection(position);
            }
        });

        // Setup tab indicators
        new TabLayoutMediator(tabLayout, scenesViewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("✏️");
                    tab.setContentDescription("Trace");
                    break;
                case 1:
                    tab.setText("👋");
                    tab.setContentDescription("Meet");
                    break;
                case 2:
                    tab.setText("📚");
                    tab.setContentDescription("Learn");
                    break;
                case 3:
                    tab.setText("⭐");
                    tab.setContentDescription("Review");
                    break;
            }
        }).attach();
    }

    private void updateTabSelection(int position) {
        // Animate current tab
        if (tabLayout != null && tabLayout.getTabAt(position) != null) {
            View tabView = tabLayout.getTabAt(position).view;
            if (tabView != null) {
                tabView.animate()
                        .scaleX(1.2f)
                        .scaleY(1.2f)
                        .setDuration(200)
                        .withEndAction(() -> {
                            tabView.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(200)
                                    .start();
                        })
                        .start();
            }
        }
    }

    public void goToNextScene() {
        if (scenesViewPager != null) {
            int currentItem = scenesViewPager.getCurrentItem();
            if (currentItem < 3) {
                scenesViewPager.setCurrentItem(currentItem + 1, true);
            }
        }
    }

    public Shape getShape() {
        return shape;
    }

    public int getShapeIndex() {
        return shapeIndex;
    }
}
