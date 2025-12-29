# 🍳 MASTER CHEF - English Cooking Adventure

## Game học tiếng Anh qua nấu ăn cho trẻ em

Game Android Native (Java + XML) dạy tiếng Anh qua ngữ cảnh nấu ăn thực tế, với 3 scenes tương tác:

1. **Restaurant Ordering** 🍽️ - Gọi món tại nhà hàng
2. **Supermarket Shopping** 🛒 - Mua nguyên liệu
3. **Kitchen Cooking** 👨‍🍳 - Nấu ăn với nhiều thao tác

---

## 📁 Files quan trọng

- **[MASTERCHEF_DESIGN.md](./MASTERCHEF_DESIGN.md)** - Thiết kế đầy đủ (UI, Architecture, Pseudocode, XML layouts)
- **[MASTERCHEF_IMPLEMENTATION.md](./MASTERCHEF_IMPLEMENTATION.md)** - Tổng kết implementation hiện tại

---

## ✅ Đã triển khai (40%)

### Core System
- ✅ Data models (Food, Ingredient, LevelConfig, CookingStep, PlayerProgress...)
- ✅ Game engine (StateMachine, StepValidator, ScoreCalculator)
- ✅ Repositories (LevelRepository với mock data Level 1, ProgressRepository)

### UI - Map Screen
- ✅ MasterChefMapActivity (level selection map)
- ✅ LevelNodeView (custom view với lock/unlock, stars)
- ✅ LevelIntroDialog (popup trước khi chơi)
- ✅ Integration vào GamesActivity

### Mock Data
- ✅ **Level 1: Spaghetti Bolognese**
  - Scene 1: 3 dialog lines (ordering)
  - Scene 2: 6 ingredients + 3 distractors
  - Scene 3: 18 cooking steps (chop, pour, stir, serve)

---

## 🚧 Chưa triển khai (60%)

- ⏳ Scene1Fragment (Restaurant)
- ⏳ Scene2Fragment (Shopping)
- ⏳ Scene3Fragment (Cooking)
- ⏳ MasterChefGameActivity (container)
- ⏳ TTSManager (Text-to-Speech + Karaoke)
- ⏳ SoundPoolManager (Sound effects)
- ⏳ CookbookActivity (sổ tay món ăn)
- ⏳ Assets (backgrounds, characters, ingredients, tools)

---

## 🎮 Cách chơi (khi hoàn tất)

### Scene 1: Restaurant
- Bé chọn avatar, vào nhà hàng
- NPC chào + hiển thị menu
- Bé nói: "I want to order [Spaghetti]"
- TTS đọc + karaoke highlight từng từ

### Scene 2: Shopping
- Chef bubble: "I need: tomato, onion, garlic..."
- Bé click/drag ingredients vào giỏ
- Đúng → ✓ xanh + sound
- Sai → shake + sound

### Scene 3: Cooking
- 18 steps: drag, tap, hold, stir gestures
- Zones: cutting board, stove, oven, counter, plate
- Hint mode nếu sai nhiều lần
- Kết thúc: món ăn zoom + shine + stars

---

## 🚀 Launch Master Chef

**Từ app:**
1. Home → **Games** → **Master Chef**
2. Chọn level (hiện tại chỉ Level 1 unlocked)
3. Click level → xem intro → **Start** (hiện tại chỉ có toast)

**Từ code:**
```java
Intent intent = new Intent(context, MasterChefMapActivity.class);
startActivity(intent);
```

---

## 📦 Package Structure

```
com.edu.english.masterchef/
├── data/
│   ├── model/          ✅ (8 classes)
│   └── repository/     ✅ (2 classes)
├── ui/
│   ├── map/            ✅ (4 classes)
│   ├── game/           ⏳ (chưa có)
│   └── cookbook/       ⏳ (chưa có)
├── engine/             ✅ (3 classes)
└── util/               ⏳ (chưa có)
```

---

## 🎯 Next Steps

**Priority 1 (Core gameplay):**
1. Scene1Fragment + ViewModel → Dialog flow
2. TTSManager → Text-to-Speech
3. Scene2Fragment → Shopping logic
4. Scene3Fragment (DragDrop only) → Basic cooking
5. MasterChefGameActivity → Container
6. LevelCompleteDialog → Results

**Priority 2 (Polish):**
7. Advanced gestures (Hold, Stir)
8. KaraokeHelper (word highlighting)
9. SoundPoolManager
10. AnimationHelper

**Priority 3 (Content):**
11. CookbookActivity
12. Add more levels (data-driven)
13. Real assets
14. Difficulty modes

---

## 🛠️ Tech Stack

- **Language:** Java (Android Native)
- **UI:** XML Layouts
- **Architecture:** MVVM
- **Build:** Gradle
- **Libraries:**
  - LifecycleViewModel
  - MotionLayout (cho parallax/camera)
  - CardView
  - RecyclerView
  - Gson (cho save/load)
  - (Future: Lottie, SoundPool, TTS)

---

## 📝 License & Credits

Part of **4Kids English Learning App**
Designed for children under 10 years old

---

## 📞 Contact

- Game design: Lead Android Game Developer + English Learning Expert
- Implementation: MVVM + Data-Driven Architecture

---

**Status:** 🟡 In Development (Map screen working, game screens pending)
**Progress:** 40% Complete
**Next Milestone:** Scene1Fragment implementation

---

END README
