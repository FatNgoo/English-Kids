# Magic Storybook - Setup Guide

## Overview
Magic Storybook is an AI-powered feature that generates personalized English learning stories for kids aged 4-12. It uses Google's Gemini AI to create engaging stories with vocabulary and quiz components.

## Features
- **4 Story Categories**: Novel, Fairy Tales, See the World, History
- **Customizable Stories**: Age, English level (A1/A2), length, theme, character name
- **"Surprise Me" Mode**: AI generates random theme and character
- **3-Step Generation**: Outline → Chapters → Vocabulary/Quiz
- **Text-to-Speech**: Listen to stories with adjustable speed
- **Offline Storage**: Save stories to library using Room database
- **Interactive Quiz**: Test comprehension after reading

## Setup Instructions

### 1. API Key Configuration (Required for AI Stories)
Add your Gemini API key to `local.properties`:
```properties
GEMINI_API_KEY=your_api_key_here
```

Get a free API key from: https://makersuite.google.com/app/apikey

### 2. Demo Mode
If no API key is configured, the app uses demo mode with pre-written stories for each category. This allows testing without API access.

### 3. Dependencies (Already Added)
```kotlin
// Room for offline storage
implementation("androidx.room:room-runtime:2.6.1")
annotationProcessor("androidx.room:room-compiler:2.6.1")

// ViewModel & LiveData
implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")

// OkHttp for API calls
implementation("com.squareup.okhttp3:okhttp:4.12.0")

// Gson for JSON parsing
implementation("com.google.code.gson:gson:2.10.1")
```

## File Structure
```
storybook/
├── model/
│   ├── Story.java              - Main story model
│   ├── Chapter.java            - Chapter with heading/content
│   ├── VocabularyItem.java     - Word + Vietnamese meaning
│   ├── QuizQuestion.java       - Multiple choice question
│   ├── StoryOutline.java       - AI-generated outline
│   ├── StorySetup.java         - User configuration
│   └── StoryCategory.java      - Category enum with themes
├── data/
│   ├── StoryEntity.java        - Room entity
│   ├── StoryDao.java           - Database access
│   ├── StoryDatabase.java      - Room database
│   └── StoryRepository.java    - Repository pattern
├── api/
│   ├── GeminiRestClient.java   - REST API client
│   ├── StoryPromptBuilder.java - AI prompt templates
│   ├── StoryGeneratorService.java - 3-step generation
│   ├── MockStoryGenerator.java - Demo fallback
│   └── JsonSanitizer.java      - JSON response cleaner
├── MagicStorybookCategoryActivity.java
├── StorySetupBottomSheet.java
├── StoryReaderActivity.java
├── StoryLibraryActivity.java
├── StoryLibraryAdapter.java
├── VocabularyBottomSheet.java
└── QuizBottomSheet.java
```

## Usage Flow
1. Home Screen → Tap "Magic Storybook" card
2. Category Screen → Select one of 4 categories
3. Setup Sheet → Configure story options or tap "Surprise Me"
4. Generation → Watch progress bar during 3-step AI generation
5. Story Reader → Read chapters, use TTS, save to library
6. Library → Access saved stories anytime offline

## AI Generation Process
1. **Step 1 - Outline**: AI creates title, summary, chapter titles (~2-5 chapters)
2. **Step 2 - Chapters**: AI writes each chapter sequentially
3. **Step 3 - Vocabulary/Quiz**: AI extracts 6 vocabulary words + 5 comprehension questions

## Safety Features
- Age-appropriate content filtering in AI prompts
- Kid-safe vocabulary and themes
- No scary/violent content enforcement
- Happy endings encouraged for young readers

## Customization
- Modify category themes in `StoryCategory.java`
- Adjust prompt templates in `StoryPromptBuilder.java`
- Change chapter lengths in `getChaptersForLength()`
- Edit demo stories in `MockStoryGenerator.java`

## Troubleshooting
- **"Demo mode" stories appearing**: Check API key in local.properties
- **JSON parsing errors**: AI response may be malformed, retry generation
- **TTS not working**: Device may not support English TTS
- **Database issues**: Clear app data and restart
