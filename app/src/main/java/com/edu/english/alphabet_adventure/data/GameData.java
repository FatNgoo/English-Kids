package com.edu.english.alphabet_adventure.data;

import com.edu.english.R;
import com.edu.english.alphabet_adventure.models.Mascot;
import com.edu.english.alphabet_adventure.models.WordItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Mock data for the Alphabet Adventure game.
 * Contains 20 easy words for children (A1 level) and 4 mascots.
 */
public class GameData {

    private static List<WordItem> wordList;
    private static List<Mascot> mascotList;

    /**
     * Get all available words for the game.
     * @return List of 20 WordItem objects
     */
    public static List<WordItem> getWords() {
        if (wordList == null) {
            wordList = new ArrayList<>();
            
            // 20 easy words for children (A1 level)
            wordList.add(new WordItem("apple", "🍎", "A red fruit"));
            wordList.add(new WordItem("ball", "⚽", "You can kick it"));
            wordList.add(new WordItem("cat", "🐱", "A cute pet that says meow"));
            wordList.add(new WordItem("dog", "🐕", "A pet that says woof"));
            wordList.add(new WordItem("egg", "🥚", "Comes from a chicken"));
            wordList.add(new WordItem("fish", "🐟", "Lives in water"));
            wordList.add(new WordItem("goat", "🐐", "An animal with horns"));
            wordList.add(new WordItem("hat", "🎩", "You wear it on your head"));
            wordList.add(new WordItem("ice", "🧊", "Very cold water"));
            wordList.add(new WordItem("juice", "🧃", "A sweet drink"));
            wordList.add(new WordItem("kite", "🪁", "Flies in the sky"));
            wordList.add(new WordItem("lion", "🦁", "King of the jungle"));
            wordList.add(new WordItem("milk", "🥛", "A white drink from cows"));
            wordList.add(new WordItem("nest", "🪺", "Where birds live"));
            wordList.add(new WordItem("orange", "🍊", "An orange fruit"));
            wordList.add(new WordItem("panda", "🐼", "A black and white bear"));
            wordList.add(new WordItem("queen", "👸", "A royal lady"));
            wordList.add(new WordItem("robot", "🤖", "A machine that can move"));
            wordList.add(new WordItem("sun", "☀️", "Gives us light and warmth"));
            wordList.add(new WordItem("tree", "🌳", "Has leaves and branches"));
        }
        return new ArrayList<>(wordList);
    }

    /**
     * Get shuffled words for a new game session.
     * @return Shuffled list of words
     */
    public static List<WordItem> getShuffledWords() {
        List<WordItem> shuffled = getWords();
        Collections.shuffle(shuffled);
        return shuffled;
    }

    /**
     * Get all available mascots.
     * @return List of 4 Mascot objects
     */
    public static List<Mascot> getMascots() {
        if (mascotList == null) {
            mascotList = new ArrayList<>();
            
            mascotList.add(new Mascot(1, "Bunny", "🐰", R.color.card_pink));
            mascotList.add(new Mascot(2, "Bear", "🐻", R.color.card_orange));
            mascotList.add(new Mascot(3, "Dino", "🦕", R.color.card_green));
            mascotList.add(new Mascot(4, "Unicorn", "🦄", R.color.card_purple));
        }
        return new ArrayList<>(mascotList);
    }

    /**
     * Get a mascot by ID.
     * @param id Mascot ID
     * @return Mascot object or first mascot if not found
     */
    public static Mascot getMascotById(int id) {
        for (Mascot mascot : getMascots()) {
            if (mascot.getId() == id) {
                return mascot;
            }
        }
        return getMascots().get(0);
    }
}
