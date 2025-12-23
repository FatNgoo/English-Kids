package com.edu.english.masterchef;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.edu.english.R;
import com.edu.english.masterchef.adapters.RecipeAdapter;
import com.edu.english.masterchef.data.MasterChefRepository;
import com.edu.english.masterchef.data.Recipe;

import java.util.List;

/**
 * Recipe selection screen where users choose which recipe to cook.
 */
public class RecipeSelectActivity extends AppCompatActivity implements RecipeAdapter.OnRecipeClickListener {

    public static final String EXTRA_RECIPE_ID = "recipe_id";
    
    private RecyclerView recyclerView;
    private Button btnBack;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_select);
        
        initViews();
        loadRecipes();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recipe_list);
        btnBack = findViewById(R.id.btn_back_to_games);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        btnBack.setOnClickListener(v -> finish());
    }
    
    private void loadRecipes() {
        MasterChefRepository repository = MasterChefRepository.getInstance();
        List<Recipe> recipes = repository.getAllRecipes();
        
        RecipeAdapter adapter = new RecipeAdapter(this, recipes, this);
        recyclerView.setAdapter(adapter);
    }
    
    @Override
    public void onRecipeClick(Recipe recipe) {
        // Go through customer order selection flow first
        Intent intent = new Intent(this, OrderSelectActivity.class);
        intent.putExtra(OrderSelectActivity.EXTRA_RECIPE_ID, recipe.getId());
        startActivity(intent);
    }
}
