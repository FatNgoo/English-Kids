package com.edu.english.storybook.api;

import com.edu.english.storybook.model.Chapter;
import com.edu.english.storybook.model.QuizQuestion;
import com.edu.english.storybook.model.Story;
import com.edu.english.storybook.model.StoryCategory;
import com.edu.english.storybook.model.VocabularyItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * Mock story generator for demo mode when API is not available
 * Provides pre-written sample stories for each category
 */
public class MockStoryGenerator {
    
    private static final Random random = new Random();
    
    /**
     * Generate a mock story based on category
     */
    public static Story generateMockStory(String category, int age, String readingLevel) {
        Story story = new Story();
        story.setId(UUID.randomUUID().toString());
        story.setCategory(category);
        story.setAge(age);
        story.setReadingLevel(readingLevel);
        
        switch (category.toLowerCase()) {
            case "novel":
                populateNovelStory(story);
                break;
            case "fairy_tales":
            case "fairy tales":
                populateFairyTaleStory(story);
                break;
            case "see_the_world":
            case "see the world":
                populateWorldStory(story);
                break;
            case "history":
                populateHistoryStory(story);
                break;
            default:
                populateNovelStory(story);
        }
        
        return story;
    }
    
    private static void populateNovelStory(Story story) {
        story.setTitle("The Brave Little Mouse");
        
        List<Chapter> chapters = new ArrayList<>();
        
        chapters.add(new Chapter(
            "Chapter 1: A Tiny Home",
            "In a cozy hole under an old oak tree, there lived a tiny mouse named Pip. Pip had soft gray fur and bright curious eyes. " +
            "His home was warm and comfortable. He had a small bed made of soft leaves and a tiny kitchen with acorn cups. " +
            "Every morning, Pip would wake up to the sound of birds singing. He loved his home very much. " +
            "But Pip had a dream. He wanted to see the world beyond the oak tree. He wanted to go on an adventure!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 2: The Big Decision",
            "One sunny morning, Pip made a big decision. He would go on an adventure! He packed a small bag with some cheese and berries. " +
            "His mother gave him a tiny map. 'Be careful, my little one,' she said with love. 'And always be kind to everyone you meet.' " +
            "Pip hugged his mother and set off on his journey. His heart was beating fast with excitement. " +
            "The world outside looked so big and beautiful. Pip took a deep breath and started walking."
        ));
        
        chapters.add(new Chapter(
            "Chapter 3: A New Friend",
            "As Pip walked through the meadow, he heard a small voice. 'Help! Please help!' It was a ladybug stuck in a spider web. " +
            "Pip quickly helped her escape. 'Thank you so much!' said the ladybug. 'My name is Dotty. You are very brave!' " +
            "Pip smiled. 'My name is Pip. Would you like to come with me on my adventure?' " +
            "Dotty was so happy. Now Pip had a friend to travel with. Together, they continued down the path."
        ));
        
        chapters.add(new Chapter(
            "Chapter 4: The Dark Forest",
            "Soon, Pip and Dotty reached a forest. The trees were tall and the path was dark. Pip felt a little scared. " +
            "But Dotty said, 'Don't worry, Pip. We are together. We can do this!' " +
            "They walked slowly and carefully. They heard strange sounds, but they kept going. " +
            "Then they saw a light! It was a friendly firefly named Flash. 'Follow me,' said Flash. 'I know the way!'"
        ));
        
        chapters.add(new Chapter(
            "Chapter 5: The Beautiful Lake",
            "Flash led them to a beautiful lake. The water sparkled in the sunlight. Pip had never seen anything so pretty. " +
            "'This is the most beautiful place I have ever seen!' said Pip with wonder. " +
            "They sat by the lake and ate their snacks. They watched fish swimming and frogs jumping. " +
            "Pip was so happy. His adventure was wonderful! He had new friends and saw amazing things."
        ));
        
        chapters.add(new Chapter(
            "Chapter 6: Going Home",
            "After many days, Pip decided it was time to go home. He missed his mother and his cozy home. " +
            "Dotty and Flash came with him part of the way. They promised to visit each other again. " +
            "'Thank you for being my friends,' said Pip. 'This was the best adventure ever!' " +
            "When Pip got home, his mother hugged him tight. Pip told her all about his journey. " +
            "That night, Pip fell asleep with a big smile. He was a brave little mouse who followed his dreams."
        ));
        
        story.setChapters(chapters);
        story.setVocabulary(createNovelVocabulary());
        story.setQuestions(createNovelQuestions());
    }
    
    private static void populateFairyTaleStory(Story story) {
        story.setTitle("The Princess and the Friendly Dragon");
        
        List<Chapter> chapters = new ArrayList<>();
        
        chapters.add(new Chapter(
            "Chapter 1: The Kind Princess",
            "Once upon a time, there was a kind princess named Lily. She lived in a beautiful castle on a green hill. " +
            "Princess Lily had long golden hair and sparkling blue eyes. She loved all animals and flowers. " +
            "Every day, she would walk in the garden and talk to the birds. The birds loved her very much. " +
            "Princess Lily was different from other princesses. She didn't want jewels or fancy dresses. " +
            "She only wanted everyone in her kingdom to be happy and safe."
        ));
        
        chapters.add(new Chapter(
            "Chapter 2: The Lonely Dragon",
            "Far away from the castle, there lived a dragon named Sparky. He was green with purple wings. " +
            "But Sparky was not a scary dragon. He was kind and gentle. He loved to blow small puffs of smoke rings. " +
            "The problem was, everyone was afraid of Sparky. When people saw him, they would run away. " +
            "This made Sparky very sad. He had no friends. He spent his days alone in his cave. " +
            "'I wish I had a friend,' Sparky would say to the stars at night."
        ));
        
        chapters.add(new Chapter(
            "Chapter 3: The Meeting",
            "One day, Princess Lily was picking flowers in the forest. She walked farther than usual. " +
            "Suddenly, she saw the dragon! But she did not run away. She saw that he looked sad. " +
            "'Hello,' said Princess Lily with a smile. 'What is your name?' " +
            "Sparky was very surprised. No one had ever talked to him before! 'My name is Sparky,' he said softly. " +
            "'Why do you look so sad?' asked the princess. Sparky told her about being lonely."
        ));
        
        chapters.add(new Chapter(
            "Chapter 4: A Special Friendship",
            "Princess Lily felt sorry for Sparky. 'I will be your friend!' she said. Sparky was so happy that he blew colorful smoke hearts. " +
            "Every day, Princess Lily would visit Sparky. They would play games and tell stories. " +
            "Sparky would give Lily rides on his back, flying over the beautiful kingdom. " +
            "Lily taught Sparky how to make flower crowns. Sparky taught Lily how to roast marshmallows. " +
            "They became the best of friends."
        ));
        
        chapters.add(new Chapter(
            "Chapter 5: Helping the Kingdom",
            "One day, a big storm came to the kingdom. Many houses were damaged. People needed help. " +
            "Princess Lily had an idea. 'Sparky can help!' She brought Sparky to the village. " +
            "At first, people were scared. But then Sparky used his fire to keep everyone warm. " +
            "He used his big wings to cover houses from the rain. He carried supplies to help rebuild. " +
            "The people saw that Sparky was kind. They were not afraid anymore!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 6: Happy Ever After",
            "From that day on, Sparky was welcomed in the kingdom. Everyone loved him. " +
            "He became the kingdom's special helper and protector. He had many friends now! " +
            "Princess Lily and Sparky showed everyone that being different is wonderful. " +
            "The kingdom became the happiest place in all the land. " +
            "And Princess Lily and Sparky remained best friends forever and ever. The End."
        ));
        
        story.setChapters(chapters);
        story.setVocabulary(createFairyTaleVocabulary());
        story.setQuestions(createFairyTaleQuestions());
    }
    
    private static void populateWorldStory(Story story) {
        story.setTitle("Journey to the Rainforest");
        
        List<Chapter> chapters = new ArrayList<>();
        
        chapters.add(new Chapter(
            "Chapter 1: The Amazing Rainforest",
            "Today we are going to visit the rainforest! A rainforest is a special forest with lots of rain and tall trees. " +
            "Rainforests are found in warm places near the equator. They are full of amazing animals and plants. " +
            "Did you know? Rainforests are called the 'lungs of the Earth' because they make so much fresh air! " +
            "The trees are so tall that they make a green roof called a canopy. Let's explore together!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 2: Colorful Birds",
            "Look up! Can you see the beautiful birds? Rainforests have the most colorful birds in the world. " +
            "There's a toucan with its big orange beak! Toucans use their beaks to pick fruit from trees. " +
            "And look at the macaws! They are parrots with red, blue, and yellow feathers. " +
            "Macaws are very smart. They can learn to say words! Some macaws live for 80 years!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 3: Funny Monkeys",
            "Swing, swing, swing! The monkeys are playing in the trees. They use their long tails to hold onto branches. " +
            "Spider monkeys are great climbers. They can swing from tree to tree very fast. " +
            "Baby monkeys ride on their mother's back. They hold on tight! It's like a fun ride. " +
            "Monkeys eat fruits, leaves, and sometimes insects. They live in big family groups."
        ));
        
        chapters.add(new Chapter(
            "Chapter 4: The Forest Floor",
            "Now let's look at the ground. The forest floor is dark because the tall trees block the sun. " +
            "But there's so much to see! Look at the big leaf-cutter ants carrying leaves. They work together as a team. " +
            "Here's a dart frog! It's tiny but very colorful - bright blue and red. The bright colors warn others to stay away. " +
            "And there's a jaguar resting in the shade. Jaguars are excellent swimmers and climbers."
        ));
        
        chapters.add(new Chapter(
            "Chapter 5: Amazing Plants",
            "Rainforest plants are incredible! Some trees grow as tall as 50 meters - that's taller than most buildings! " +
            "Orchids grow on other trees. They come in many beautiful colors. " +
            "The kapok tree is one of the tallest. Its seeds fly through the air like tiny helicopters! " +
            "Some plants catch rainwater in their leaves. Frogs lay their eggs in these tiny pools!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 6: Protecting Rainforests",
            "Our rainforest adventure is ending, but there's one more important thing to learn. " +
            "Rainforests are very special, but they need our help. Some rainforests are being cut down. " +
            "We can help by learning about them and telling others how amazing they are. " +
            "We can also plant trees and be kind to nature. Every little bit helps! " +
            "Remember: the rainforest is home to millions of animals. Let's protect it together!"
        ));
        
        story.setChapters(chapters);
        story.setVocabulary(createWorldVocabulary());
        story.setQuestions(createWorldQuestions());
    }
    
    private static void populateHistoryStory(Story story) {
        story.setTitle("The Young Baker of Long Ago");
        
        List<Chapter> chapters = new ArrayList<>();
        
        chapters.add(new Chapter(
            "Chapter 1: Meet Thomas",
            "Long, long ago, before there were cars or phones, there lived a boy named Thomas. " +
            "Thomas was 8 years old. He lived in a small village with his family. " +
            "His father was a baker. Every day, his father made delicious bread for the whole village. " +
            "Thomas loved watching his father work. The bakery always smelled wonderful!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 2: Learning to Bake",
            "One day, Thomas's father said, 'Would you like to learn how to bake?' Thomas was so excited! " +
            "First, his father showed him how to mix flour and water. It was messy but fun! " +
            "Then they added yeast to make the bread rise. 'Yeast is like magic,' said Father. " +
            "Thomas kneaded the dough with his small hands. Push, fold, turn. Push, fold, turn."
        ));
        
        chapters.add(new Chapter(
            "Chapter 3: The Big Oven",
            "The oven in those days was very different from today. It was made of stone and heated with wood fire. " +
            "Father showed Thomas how to check if the oven was ready. He would throw a bit of flour inside. " +
            "If it turned brown slowly, the temperature was just right! No thermometers back then! " +
            "Thomas watched the bread bake. It puffed up and turned golden brown. Magic!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 4: Village Life",
            "Life in Thomas's village was simple but happy. There were no supermarkets. " +
            "People bought bread from the bakery, vegetables from the farmer, and meat from the butcher. " +
            "Children helped their parents with work. They didn't have many toys, but they played games outside. " +
            "At night, they used candles for light. There was no electricity! Can you imagine that?"
        ));
        
        chapters.add(new Chapter(
            "Chapter 5: A Special Day",
            "One day, Thomas baked his first loaf all by himself! His father watched proudly. " +
            "The bread came out perfect - golden and crusty on the outside, soft inside. " +
            "Thomas's mother was so proud. She hugged him tight. 'My little baker!' she said. " +
            "That evening, the whole family ate Thomas's bread for dinner. It was the best bread ever!"
        ));
        
        chapters.add(new Chapter(
            "Chapter 6: Lessons from the Past",
            "Thomas grew up to be a great baker, just like his father. He taught his own children to bake. " +
            "Even though life long ago was different, some things stay the same. " +
            "Families still eat together. People still help each other. Bread is still delicious! " +
            "When you eat bread today, think of Thomas and the bakers of long ago. " +
            "They worked hard to share something wonderful with others. And that's what really matters."
        ));
        
        story.setChapters(chapters);
        story.setVocabulary(createHistoryVocabulary());
        story.setQuestions(createHistoryQuestions());
    }
    
    // Vocabulary lists
    private static List<VocabularyItem> createNovelVocabulary() {
        return Arrays.asList(
            new VocabularyItem("adventure", "cuộc phiêu lưu", "Pip went on a big adventure."),
            new VocabularyItem("brave", "dũng cảm", "The brave mouse helped his friend."),
            new VocabularyItem("cozy", "ấm cúng", "His home was warm and cozy."),
            new VocabularyItem("curious", "tò mò", "Pip had curious eyes."),
            new VocabularyItem("journey", "chuyến đi", "He set off on his journey."),
            new VocabularyItem("meadow", "đồng cỏ", "Pip walked through the meadow."),
            new VocabularyItem("sparkle", "lấp lánh", "The water sparkled in the sun."),
            new VocabularyItem("wonderful", "tuyệt vời", "It was a wonderful adventure."),
            new VocabularyItem("escape", "trốn thoát", "Pip helped Dotty escape."),
            new VocabularyItem("excitement", "sự hào hứng", "His heart beat with excitement.")
        );
    }
    
    private static List<VocabularyItem> createFairyTaleVocabulary() {
        return Arrays.asList(
            new VocabularyItem("castle", "lâu đài", "The princess lived in a castle."),
            new VocabularyItem("kingdom", "vương quốc", "Everyone in the kingdom was happy."),
            new VocabularyItem("gentle", "hiền lành", "Sparky was kind and gentle."),
            new VocabularyItem("lonely", "cô đơn", "The dragon felt lonely."),
            new VocabularyItem("protector", "người bảo vệ", "He became the kingdom's protector."),
            new VocabularyItem("sparkle", "lấp lánh", "Her eyes were sparkling."),
            new VocabularyItem("rebuild", "xây dựng lại", "People helped rebuild houses."),
            new VocabularyItem("welcomed", "được chào đón", "Sparky was welcomed by everyone."),
            new VocabularyItem("different", "khác biệt", "Being different is wonderful."),
            new VocabularyItem("forever", "mãi mãi", "They were friends forever.")
        );
    }
    
    private static List<VocabularyItem> createWorldVocabulary() {
        return Arrays.asList(
            new VocabularyItem("rainforest", "rừng nhiệt đới", "The rainforest has many animals."),
            new VocabularyItem("canopy", "tán cây", "Birds live in the canopy."),
            new VocabularyItem("colorful", "nhiều màu sắc", "Macaws are colorful birds."),
            new VocabularyItem("equator", "xích đạo", "Rainforests are near the equator."),
            new VocabularyItem("protect", "bảo vệ", "We must protect the rainforest."),
            new VocabularyItem("species", "loài", "Many species live there."),
            new VocabularyItem("tropical", "nhiệt đới", "It's a tropical forest."),
            new VocabularyItem("climb", "leo trèo", "Monkeys climb trees."),
            new VocabularyItem("amazing", "tuyệt vời", "The plants are amazing."),
            new VocabularyItem("environment", "môi trường", "We care for the environment.")
        );
    }
    
    private static List<VocabularyItem> createHistoryVocabulary() {
        return Arrays.asList(
            new VocabularyItem("baker", "thợ làm bánh", "His father was a baker."),
            new VocabularyItem("village", "làng", "He lived in a small village."),
            new VocabularyItem("flour", "bột mì", "They mixed flour and water."),
            new VocabularyItem("yeast", "men", "Yeast makes bread rise."),
            new VocabularyItem("knead", "nhào bột", "Thomas kneaded the dough."),
            new VocabularyItem("oven", "lò nướng", "The bread baked in the oven."),
            new VocabularyItem("electricity", "điện", "There was no electricity."),
            new VocabularyItem("candle", "nến", "They used candles for light."),
            new VocabularyItem("proud", "tự hào", "His father was proud."),
            new VocabularyItem("simple", "đơn giản", "Life was simple but happy.")
        );
    }
    
    // Quiz questions
    private static List<QuizQuestion> createNovelQuestions() {
        return Arrays.asList(
            new QuizQuestion("What is the mouse's name?", 
                Arrays.asList("Tom", "Pip", "Max", "Ben"), 0),
            new QuizQuestion("Where did Pip live?", 
                Arrays.asList("In a castle", "In a tree hole", "In a house", "By a lake"), 1),
            new QuizQuestion("Who did Pip help?", 
                Arrays.asList("A butterfly", "A ladybug", "A bee", "A bird"), 1),
            new QuizQuestion("What was the firefly's name?", 
                Arrays.asList("Spark", "Flash", "Light", "Glow"), 1),
            new QuizQuestion("How did Pip feel about his adventure?", 
                Arrays.asList("Sad", "Scared", "Happy", "Angry"), 2)
        );
    }
    
    private static List<QuizQuestion> createFairyTaleQuestions() {
        return Arrays.asList(
            new QuizQuestion("What was the princess's name?", 
                Arrays.asList("Rose", "Lily", "Daisy", "Violet"), 1),
            new QuizQuestion("What color was the dragon?", 
                Arrays.asList("Red", "Blue", "Green", "Gold"), 2),
            new QuizQuestion("Why was Sparky sad?", 
                Arrays.asList("He was hungry", "He was tired", "He had no friends", "He was sick"), 2),
            new QuizQuestion("How did Sparky help the village?", 
                Arrays.asList("He found gold", "He kept people warm", "He sang songs", "He built houses"), 1),
            new QuizQuestion("What lesson did the story teach?", 
                Arrays.asList("Don't talk to strangers", "Being different is wonderful", "Dragons are scary", "Stay at home"), 1)
        );
    }
    
    private static List<QuizQuestion> createWorldQuestions() {
        return Arrays.asList(
            new QuizQuestion("What do we call the green roof of trees?", 
                Arrays.asList("Ceiling", "Canopy", "Cover", "Crown"), 1),
            new QuizQuestion("What bird has a big orange beak?", 
                Arrays.asList("Macaw", "Eagle", "Toucan", "Parrot"), 2),
            new QuizQuestion("How do spider monkeys move through trees?", 
                Arrays.asList("They fly", "They swing", "They hop", "They crawl"), 1),
            new QuizQuestion("What helps us by making fresh air?", 
                Arrays.asList("Cars", "Houses", "Rainforests", "Cities"), 2),
            new QuizQuestion("Why should we protect rainforests?", 
                Arrays.asList("They are home to many animals", "They make us rich", "They are boring", "We don't need them"), 0)
        );
    }
    
    private static List<QuizQuestion> createHistoryQuestions() {
        return Arrays.asList(
            new QuizQuestion("What was Thomas's father's job?", 
                Arrays.asList("Farmer", "Baker", "Teacher", "Doctor"), 1),
            new QuizQuestion("What makes bread rise?", 
                Arrays.asList("Sugar", "Salt", "Yeast", "Butter"), 2),
            new QuizQuestion("How were ovens heated long ago?", 
                Arrays.asList("Electricity", "Gas", "Wood fire", "Magic"), 2),
            new QuizQuestion("What did people use for light at night?", 
                Arrays.asList("Light bulbs", "Candles", "Phones", "Computers"), 1),
            new QuizQuestion("What did Thomas bake by himself?", 
                Arrays.asList("A cake", "A loaf of bread", "Cookies", "A pie"), 1)
        );
    }
}
