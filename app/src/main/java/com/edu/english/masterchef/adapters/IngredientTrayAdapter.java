package com.edu.english.masterchef.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.edu.english.R;
import com.edu.english.masterchef.data.Ingredient;
import com.edu.english.masterchef.data.Tool;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for the ingredient/tool tray at the bottom of the kitchen screen.
 * Supports both Ingredients and Tools.
 */
public class IngredientTrayAdapter extends BaseAdapter {

    private final Context context;
    private final List<TrayItem> items = new ArrayList<>();
    private OnTrayItemClickListener listener;
    
    public interface OnTrayItemClickListener {
        void onIngredientClick(Ingredient ingredient);
        void onToolClick(Tool tool);
        void onItemLongClick(TrayItem item, View view);
    }
    
    public IngredientTrayAdapter(Context context) {
        this.context = context;
    }
    
    public void setItems(List<Ingredient> ingredients, List<Tool> tools) {
        items.clear();
        
        // Add ingredients first
        for (Ingredient ingredient : ingredients) {
            items.add(new TrayItem(ingredient));
        }
        
        // Add tools
        for (Tool tool : tools) {
            items.add(new TrayItem(tool));
        }
        
        notifyDataSetChanged();
    }
    
    public void setListener(OnTrayItemClickListener listener) {
        this.listener = listener;
    }
    
    @Override
    public int getCount() {
        return items.size();
    }
    
    @Override
    public TrayItem getItem(int position) {
        return items.get(position);
    }
    
    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_ingredient, parent, false);
            holder = new ViewHolder();
            holder.imgIngredient = convertView.findViewById(R.id.img_ingredient);
            holder.textName = convertView.findViewById(R.id.text_ingredient_name);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        
        TrayItem item = items.get(position);
        
        // Set name
        holder.textName.setText(item.getNameEn());
        
        // Set drawable
        try {
            int drawableId = context.getResources().getIdentifier(
                item.getDrawableName(),
                "drawable",
                context.getPackageName()
            );
            if (drawableId != 0) {
                holder.imgIngredient.setImageDrawable(ContextCompat.getDrawable(context, drawableId));
            }
        } catch (Exception e) {
            // Use placeholder
        }
        
        // Set click listeners
        convertView.setOnClickListener(v -> {
            if (listener != null) {
                if (item.isIngredient()) {
                    listener.onIngredientClick(item.ingredient);
                } else {
                    listener.onToolClick(item.tool);
                }
            }
        });
        
        final View finalView = convertView;
        convertView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onItemLongClick(item, finalView);
            }
            return true;
        });
        
        return convertView;
    }
    
    static class ViewHolder {
        ImageView imgIngredient;
        TextView textName;
    }
    
    /**
     * Wrapper class that can hold either Ingredient or Tool
     */
    public static class TrayItem {
        public final Ingredient ingredient;
        public final Tool tool;
        
        public TrayItem(Ingredient ingredient) {
            this.ingredient = ingredient;
            this.tool = null;
        }
        
        public TrayItem(Tool tool) {
            this.ingredient = null;
            this.tool = tool;
        }
        
        public boolean isIngredient() {
            return ingredient != null;
        }
        
        public boolean isTool() {
            return tool != null;
        }
        
        public String getId() {
            if (ingredient != null) return ingredient.getId();
            if (tool != null) return tool.getId();
            return "";
        }
        
        public String getNameEn() {
            if (ingredient != null) return ingredient.getNameEn();
            if (tool != null) return tool.getNameEn();
            return "";
        }
        
        public String getDrawableName() {
            if (ingredient != null) return ingredient.getDrawableName();
            if (tool != null) return tool.getDrawableName();
            return "";
        }
    }
}
