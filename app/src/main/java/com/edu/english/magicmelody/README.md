# 🎮 Magic Melody – The Legend of Sound Kingdom

## 📌 Architecture Overview

### Vision
A rhythm-based educational game for children (ages 5–10) featuring:
- Environmental evolution visuals (gray → color transformation)
- Personalized learning levels
- 3D world map
- Boss battles using voice (mic + camera)
- Magic Notebook collection system
- UX voice prompts & animations

---

## 🏗️ Module Structure

```
magicmelody/
├── core/                    # Core game logic & utilities
│   ├── audio/               # Audio engine (SoundPool, ExoPlayer)
│   ├── rhythm/              # Rhythm detection & note timing
│   └── evolution/           # Environmental evolution system
│
├── data/                    # Data layer
│   ├── entity/              # Room entities
│   ├── dao/                 # Data Access Objects
│   ├── repository/          # Repositories
│   └── database/            # Room database
│
├── ui/                      # UI layer (MVVM)
│   ├── splash/              # Game splash screen
│   ├── worldmap/            # 3D World map screen
│   ├── gameplay/            # Main rhythm gameplay
│   ├── notebook/            # Magic Notebook collection
│   └── boss/                # Boss battle (AR concert mode)
│
├── model/                   # Domain models
│   ├── UserProfile.java
│   ├── LessonConfig.java
│   ├── WorldMap.java
│   ├── Note.java
│   └── Boss.java
│
└── util/                    # Utilities
    ├── AudioUtils.java
    ├── AnimationUtils.java
    └── Constants.java
```

---

## 🎨 Architecture Pattern: MVVM

```
┌─────────────────────────────────────────────────────────┐
│                      VIEW (Activity/Fragment)            │
│  - Observes ViewModel                                   │
│  - Handles UI events                                    │
│  - Renders animations & 3D scenes                       │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                      VIEWMODEL                          │
│  - Holds UI state (LiveData)                           │
│  - Handles business logic                              │
│  - Communicates with Repository                        │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                      REPOSITORY                         │
│  - Single source of truth                              │
│  - Coordinates data from Room & JSON assets            │
└───────────────────────┬─────────────────────────────────┘
                        │
┌───────────────────────▼─────────────────────────────────┐
│                      DATA SOURCES                       │
│  - Room Database (UserProfile, Progress)               │
│  - JSON Assets (Lessons, Themes, Notes)                │
│  - SharedPreferences (Settings)                        │
└─────────────────────────────────────────────────────────┘
```

---

## 🎵 Core Game Systems

### 1. Rhythm Engine
- **SoundPool**: Low-latency note playback
- **ExoPlayer**: Background music tracks
- **BeatDetector**: Timing accuracy detection

### 2. Environmental Evolution
- Grayscale → Color transformation based on progress
- Particle effects for visual feedback
- 3D scene updates via SceneView

### 3. Boss Battle (AR Mode)
- CameraX for camera preview
- Microphone RMS detection for voice input
- AR overlay synchronized with gameplay

### 4. Magic Notebook
- Collection of learned notes/words
- 3D pop-up character interactions
- Replay learning content

---

## 📦 Dependencies

| Library | Purpose |
|---------|---------|
| Room | Local database persistence |
| SceneView | 3D world map rendering |
| CameraX | AR boss battle camera |
| ExoPlayer | Background music playback |
| SoundPool | Low-latency note sounds |
| Lottie | UI animations |
| LiveData | Reactive UI updates |
| ViewModel | UI state management |

---

## 🎯 Screen Flow

```
┌─────────────┐     ┌─────────────┐     ┌─────────────┐
│   Splash    │────▶│  World Map  │────▶│  Gameplay   │
│   Screen    │     │   (3D)      │     │  (Rhythm)   │
└─────────────┘     └──────┬──────┘     └──────┬──────┘
                           │                   │
                           ▼                   ▼
                    ┌─────────────┐     ┌─────────────┐
                    │   Magic     │     │    Boss     │
                    │  Notebook   │     │   Battle    │
                    └─────────────┘     └─────────────┘
```

---

## 🔊 Audio System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    AudioManager                         │
├─────────────────────────────────────────────────────────┤
│  ┌─────────────────┐    ┌─────────────────┐            │
│  │   SoundPool     │    │   ExoPlayer     │            │
│  │   (Notes)       │    │   (BGM)         │            │
│  │   - Do, Re, Mi  │    │   - Theme music │            │
│  │   - Effects     │    │   - Ambient     │            │
│  └─────────────────┘    └─────────────────┘            │
│                                                         │
│  ┌─────────────────┐    ┌─────────────────┐            │
│  │ VoiceDetector   │    │  TTS Engine     │            │
│  │ (Mic input)     │    │  (UX prompts)   │            │
│  │ - RMS analysis  │    │  - Guide voice  │            │
│  │ - Boss battles  │    │  - Feedback     │            │
│  └─────────────────┘    └─────────────────┘            │
└─────────────────────────────────────────────────────────┘
```

---

## 📝 JSON Schema References

### Lesson Configuration
```json
{
  "lessonId": "lesson_001",
  "ageGroup": "5-6",
  "theme": "ocean",
  "notes": ["do", "re", "mi"],
  "difficulty": 1,
  "bpm": 80
}
```

### Theme Assets
```json
{
  "themeId": "ocean",
  "name": "Ocean Kingdom",
  "bgMusic": "ocean_theme.mp3",
  "colorPalette": ["#0077BE", "#00A3E0", "#87CEEB"],
  "characters": ["dolphin", "starfish", "seahorse"]
}
```

---

## 📊 Data Layer (Task 2)

### Room Entities

| Entity | Table | Purpose |
|--------|-------|---------|
| `UserProfile` | user_profiles | User settings, age group, total progress |
| `LessonProgress` | lesson_progress | Per-lesson scores, stars, completion |
| `WorldProgress` | world_progress | World unlock, evolution stages |
| `CollectedNote` | collected_notes | Magic Notebook collection |
| `BossProgress` | boss_progress | Boss battle history & achievements |

### Domain Models (Non-Room)

| Model | Purpose |
|-------|---------|
| `LessonConfig` | Lesson data loaded from JSON |
| `NoteEvent` | Single note in rhythm sequence |
| `ThemeConfig` | World theme configuration |
| `GameState` | Current gameplay state |
| `GameResult` | Result of completed game session |
| `HitResult` | Note hit timing & accuracy |

### DAOs

| DAO | Methods |
|-----|---------|
| `UserProfileDao` | CRUD + stars, XP, streak updates |
| `LessonProgressDao` | Progress tracking, best scores |
| `WorldProgressDao` | World unlock, evolution stages |
| `CollectedNoteDao` | Note collection, favorites |
| `BossProgressDao` | Boss battles, AR stats |

### Repositories

| Repository | Purpose |
|------------|---------|
| `UserProfileRepository` | User data management |
| `LessonProgressRepository` | Lesson progress + GameResult saving |
| `WorldProgressRepository` | World unlocks + evolution |
| `CollectedNoteRepository` | Magic Notebook management |
| `BossProgressRepository` | Boss battle tracking |
| `AssetDataRepository` | Load lessons/themes from JSON |

### Database

```java
@Database(
    entities = {
        UserProfile.class,
        LessonProgress.class,
        WorldProgress.class,
        CollectedNote.class,
        BossProgress.class
    },
    version = 1
)
public abstract class MagicMelodyDatabase extends RoomDatabase {
    public abstract UserProfileDao userProfileDao();
    public abstract LessonProgressDao lessonProgressDao();
    public abstract WorldProgressDao worldProgressDao();
    public abstract CollectedNoteDao collectedNoteDao();
    public abstract BossProgressDao bossProgressDao();
}
```

---

## ✅ Task Completion Status

| Task | Status | Description |
|------|--------|-------------|
| Task 1: Architecture | ✅ Done | Base structure, layouts, drawables, JSON |
| Task 2: Models & Entities | ✅ Done | Room entities, DAOs, Database, Repositories |
| Task 3: UI Screens | ⏳ Next | ViewModels, detailed UI components |
| Task 4: Gameplay Logic | ⏳ Pending | Rhythm engine, note detection |
| Task 5: Special Features | ⏳ Pending | Boss Battle AR, Magic Notebook |
| Task 6: Audio Engine | ⏳ Pending | SoundPool, ExoPlayer integration |
| Task 7: Integration | ⏳ Pending | Final polish, testing |
