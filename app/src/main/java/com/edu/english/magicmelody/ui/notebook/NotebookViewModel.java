package com.edu.english.magicmelody.ui.notebook;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.edu.english.magicmelody.data.model.CollectionEntity;
import com.edu.english.magicmelody.data.repo.CollectionRepository;

import java.util.List;

/**
 * ViewModel for NotebookActivity
 */
public class NotebookViewModel extends AndroidViewModel {
    
    private final CollectionRepository repository;
    
    private final MediatorLiveData<List<CollectionEntity>> collectionsLiveData = new MediatorLiveData<>();
    private final MutableLiveData<CollectionEntity> selectedItemLiveData = new MutableLiveData<>();
    private LiveData<Integer> collectionCountLiveData;
    
    private String currentFilter = null;
    private LiveData<List<CollectionEntity>> currentSource;
    
    public NotebookViewModel(@NonNull Application application) {
        super(application);
        repository = new CollectionRepository(application);
        collectionCountLiveData = repository.getCollectionCount();
        
        // Load all by default
        setFilter(null);
    }
    
    public void setFilter(String theme) {
        currentFilter = theme;
        
        // Remove previous source
        if (currentSource != null) {
            collectionsLiveData.removeSource(currentSource);
        }
        
        // Add new source
        if (theme == null) {
            currentSource = repository.getAll();
        } else {
            currentSource = repository.getByTheme(theme);
        }
        
        collectionsLiveData.addSource(currentSource, collectionsLiveData::setValue);
    }
    
    public LiveData<List<CollectionEntity>> getCollections() {
        return collectionsLiveData;
    }
    
    public LiveData<Integer> getCollectionCount() {
        return collectionCountLiveData;
    }
    
    public void selectItem(CollectionEntity item) {
        selectedItemLiveData.setValue(item);
    }
    
    public void clearSelection() {
        selectedItemLiveData.setValue(null);
    }
    
    public LiveData<CollectionEntity> getSelectedItem() {
        return selectedItemLiveData;
    }
    
    public String getCurrentFilter() {
        return currentFilter;
    }
}
