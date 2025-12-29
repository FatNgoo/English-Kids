# Master Chef Sound Effects Guide

## Required Sound Files

Add these sound effect files to `app/src/main/res/raw/` folder:

### 1. **sound_correct.mp3** / .ogg
- **Description**: Positive feedback when player selects correct item/action
- **Duration**: 0.5-1 second
- **Style**: Cheerful chime or "ding" sound
- **Usage**: Scene2 (correct ingredient), Scene3 (correct cooking action)

### 2. **sound_wrong.mp3** / .ogg
- **Description**: Negative feedback for incorrect selection
- **Duration**: 0.5-1 second
- **Style**: Gentle "buzz" or "error" tone (not harsh)
- **Usage**: Scene2 (wrong ingredient), Scene3 (wrong action)

### 3. **sound_chop.mp3** / .ogg
- **Description**: Chopping/cutting sound
- **Duration**: 0.3-0.5 seconds
- **Style**: Realistic knife on cutting board
- **Usage**: Scene3 (chopping vegetables/meat)

### 4. **sound_pour.mp3** / .ogg
- **Description**: Liquid pouring sound
- **Duration**: 1-2 seconds
- **Style**: Water/oil being poured into pan/bowl
- **Usage**: Scene3 (adding liquids, sauces)

### 5. **sound_sizzle.mp3** / .ogg
- **Description**: Cooking sizzle sound
- **Duration**: 2-3 seconds (loopable)
- **Style**: Food frying in hot pan
- **Usage**: Scene3 (cooking on stove)

### 6. **sound_click.mp3** / .ogg
- **Description**: UI click/tap feedback
- **Duration**: 0.1-0.2 seconds
- **Style**: Soft button click
- **Usage**: All scenes (button presses, selections)

### 7. **sound_success.mp3** / .ogg
- **Description**: Level/scene completion fanfare
- **Duration**: 2-3 seconds
- **Style**: Triumphant jingle with ascending notes
- **Usage**: Scene completion, level victory

### 8. **sound_locked.mp3** / .ogg
- **Description**: Level locked feedback
- **Duration**: 0.5-1 second
- **Style**: "Locked" or "denied" sound
- **Usage**: Map screen (clicking locked levels)

## Implementation Status

### ✅ Code Ready
- `SoundPoolManager.java` is fully implemented
- All sound constants defined (`SOUND_CORRECT`, `SOUND_WRONG`, etc.)
- Sound loading mechanism in place
- Currently using `bg_music_gameplay.mp3` as placeholder

### ⏳ Pending
- Add actual sound effect files to `res/raw/` folder
- Uncomment the proper sound loading lines in `SoundPoolManager.loadSounds()`
- Test each sound effect in-game

## How to Add Sounds

1. **Prepare Audio Files**:
   - Format: MP3 or OGG (OGG recommended for Android)
   - Sample rate: 44.1 kHz
   - Bit rate: 128-192 kbps
   - Keep file sizes small (under 100KB each)

2. **Add to Project**:
   ```
   English-Kids/
   └── app/
       └── src/
           └── main/
               └── res/
                   └── raw/
                       ├── sound_correct.ogg
                       ├── sound_wrong.ogg
                       ├── sound_chop.ogg
                       ├── sound_pour.ogg
                       ├── sound_sizzle.ogg
                       ├── sound_click.ogg
                       ├── sound_success.ogg
                       └── sound_locked.ogg
   ```

3. **Update Code**:
   - Open `SoundPoolManager.java`
   - Uncomment the sound loading lines in `loadSounds()` method
   - Remove placeholder loading code
   - Rebuild the app

## Free Sound Resources

- **Freesound.org**: https://freesound.org/
- **Zapsplat**: https://www.zapsplat.com/
- **Mixkit**: https://mixkit.co/free-sound-effects/
- **Soundbible**: http://soundbible.com/

## Testing Checklist

After adding sounds:
- [ ] Play SOUND_CORRECT when selecting correct ingredient
- [ ] Play SOUND_WRONG when selecting wrong ingredient  
- [ ] Play SOUND_CHOP during chopping steps
- [ ] Play SOUND_POUR during pouring steps
- [ ] Play SOUND_SIZZLE during cooking steps
- [ ] Play SOUND_CLICK on all button presses
- [ ] Play SOUND_SUCCESS on level completion
- [ ] Play SOUND_LOCKED when clicking locked levels
- [ ] Adjust volume levels if needed
- [ ] Test on multiple devices

## Volume Control

Adjust master volume in code:
```java
SoundPoolManager soundManager = SoundPoolManager.getInstance(context);
soundManager.setVolume(0.7f); // 0.0 to 1.0
```

Play with custom volume:
```java
soundManager.play(SoundPoolManager.SOUND_CORRECT, 0.5f);
```
