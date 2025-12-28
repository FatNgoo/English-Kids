package com.edu.english.storybook.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.edu.english.BuildConfig;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Gemini API client using REST API with OkHttp
 * This is the fallback client when Firebase AI is not configured
 */
public class GeminiRestClient {
    
    private static final String TAG = "GeminiRestClient";
    private static final String BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    
    private final OkHttpClient client;
    private final String apiKey;
    private final Handler mainHandler;
    
    public GeminiRestClient() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        
        // Get API key from BuildConfig (injected from local.properties)
        this.apiKey = BuildConfig.GEMINI_API_KEY;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    public GeminiRestClient(String apiKey) {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .build();
        
        this.apiKey = (apiKey != null && !apiKey.isEmpty()) ? apiKey : BuildConfig.GEMINI_API_KEY;
        this.mainHandler = new Handler(Looper.getMainLooper());
    }
    
    /**
     * Check if API key is available
     */
    public boolean isApiKeyAvailable() {
        return apiKey != null && !apiKey.isEmpty();
    }
    
    /**
     * Generate content using Gemini API
     */
    public void generateContent(String prompt, GeminiCallback callback) {
        if (!isApiKeyAvailable()) {
            mainHandler.post(() -> callback.onError("API key not configured. Using demo mode."));
            return;
        }
        
        try {
            String url = BASE_URL + "?key=" + apiKey;
            
            // Build request body
            JSONObject requestBody = new JSONObject();
            JSONArray contents = new JSONArray();
            JSONObject content = new JSONObject();
            JSONArray parts = new JSONArray();
            JSONObject part = new JSONObject();
            
            part.put("text", prompt);
            parts.put(part);
            content.put("parts", parts);
            contents.put(content);
            requestBody.put("contents", contents);
            
            // Add safety settings
            JSONArray safetySettings = new JSONArray();
            String[] categories = {
                "HARM_CATEGORY_HARASSMENT",
                "HARM_CATEGORY_HATE_SPEECH",
                "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                "HARM_CATEGORY_DANGEROUS_CONTENT"
            };
            for (String category : categories) {
                JSONObject setting = new JSONObject();
                setting.put("category", category);
                setting.put("threshold", "BLOCK_MEDIUM_AND_ABOVE");
                safetySettings.put(setting);
            }
            requestBody.put("safetySettings", safetySettings);
            
            // Generation config
            JSONObject generationConfig = new JSONObject();
            generationConfig.put("temperature", 0.7);
            generationConfig.put("topK", 40);
            generationConfig.put("topP", 0.95);
            generationConfig.put("maxOutputTokens", 8192);
            requestBody.put("generationConfig", generationConfig);
            
            RequestBody body = RequestBody.create(requestBody.toString(), JSON);
            
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    Log.e(TAG, "API call failed", e);
                    mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
                }
                
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    try {
                        String responseBody = response.body() != null ? response.body().string() : "";
                        
                        if (!response.isSuccessful()) {
                            Log.e(TAG, "API error: " + response.code() + " - " + responseBody);
                            mainHandler.post(() -> callback.onError("API error: " + response.code()));
                            return;
                        }
                        
                        // Parse response
                        JSONObject json = new JSONObject(responseBody);
                        JSONArray candidates = json.optJSONArray("candidates");
                        
                        if (candidates != null && candidates.length() > 0) {
                            JSONObject candidate = candidates.getJSONObject(0);
                            JSONObject contentObj = candidate.optJSONObject("content");
                            
                            if (contentObj != null) {
                                JSONArray partsArray = contentObj.optJSONArray("parts");
                                if (partsArray != null && partsArray.length() > 0) {
                                    String text = partsArray.getJSONObject(0).optString("text", "");
                                    String sanitized = JsonSanitizer.sanitize(text);
                                    mainHandler.post(() -> callback.onSuccess(sanitized));
                                    return;
                                }
                            }
                        }
                        
                        mainHandler.post(() -> callback.onError("Invalid response format"));
                        
                    } catch (Exception e) {
                        Log.e(TAG, "Parse error", e);
                        mainHandler.post(() -> callback.onError("Parse error: " + e.getMessage()));
                    }
                }
            });
            
        } catch (Exception e) {
            Log.e(TAG, "Request build error", e);
            mainHandler.post(() -> callback.onError("Error: " + e.getMessage()));
        }
    }
    
    /**
     * Callback interface for API responses
     */
    public interface GeminiCallback {
        void onSuccess(String response);
        void onError(String error);
    }
}
