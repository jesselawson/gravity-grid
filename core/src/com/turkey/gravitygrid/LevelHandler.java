package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Json;

import java.util.ArrayList;
import java.util.Hashtable;

/**
 * Created by lawsonje on 12/8/2016.
 *
 * Offers the game the ability to load pregenerated levels and, in the future, generate levels at random.
 * This will also handle our levelCompletionInfo
 */

final class LevelHandler {

    private LevelGenerator levelGenerator;

    private LevelCompletionInfo levelCompletionInfo;

    public LevelCompletionInfo getLevelCompletionInfo() { return this.levelCompletionInfo; }

    class LevelCompletionInfo {
        ArrayList<int[]> levelData;
    }

    Preferences levels;

    LevelHandler() {

        // Load our levels via preferences file
        levels = Gdx.app.getPreferences("levels");

        // If levels is empty, then we should probably cry


        if(!LoadLevelCompletionInfo()) {
            // If we're here, then the levelcompletioninfo string in the "levels" file cannot be found. This would only be the case if this is our first time playing this game
            // So we need to build the level completion info, store it, then get on with our lives.
            this.levelCompletionInfo = new LevelCompletionInfo();
            this.levelCompletionInfo.levelData.add(new int[]{1,0,0,0,0}); // First level

            // since this is our first data, let's store it
            SaveLevelCompletionInfo();
        }
    }

    int[] getLevel(int levelNum) {

        int[] level;

        // If this is a tutorial level, return the tutorial level.
        // Add any and all tutorial levels right here!
        switch(levelNum) {
            case 1:
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,16,0,0,1}; // Level 1
                break;
            case 2:
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,32,0,0,2}; // Level 2
                break;
            case 7:
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,2,2,0,4,0,1}; // Level 7
                break;
            case 51:
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,8,1}; // Level 51
                break;
            default:
                level = CreateOrRetrieveLevel(levelNum);
                break;
        }

        return level;
    }

    int[] CreateOrRetrieveLevel(int levelNum) {

        int[] level;

        // First check if that level file already exists
        Json json = new Json();

        String serializedLevel = levels.getString("level"+levelNum+"data"); // level#data is old gravityGridLevel, level#completioninfo is old levelCompletionInfo

        // Does this level file exist?
        if(!serializedLevel.isEmpty()) {
            // If it's not empty, let's pull in the values for the level
            level = json.fromJson(int[].class, serializedLevel);

            return level;
        } else {
            // There was no serialized level or the file doesn't exist (same thing), so let's build a new level, store it, and then return it

            // The default is triggered if we're not displaying a tutorial level. This generates a level for us
            levelGenerator = new LevelGenerator();

            int a = MathUtils.random(3, 8);
            int b = MathUtils.random(0, 11);
            int c = MathUtils.random(0, 11);
            int d = MathUtils.random(0, 11);
            boolean asteroids = (levelNum > 25 ? true : false);
            boolean suns = (levelNum > 75 ? true : false);
            level = levelGenerator.GenerateLevel(a, b, c, d, asteroids, suns);

            // Create a hashtable to store our level data.
            Hashtable<String, String> hashTable = new Hashtable<String, String>();

            // Here we are serializing our array
            hashTable.put("level"+levelNum+"data", json.toJson(level));

            // Store it in the preferences file
            levels.put(hashTable);
            levels.flush();

            // Return the level for use
            return level;
        }
    }

    public void SaveLevelCompletionInfo() {

        Hashtable<String, String> hashTable = new Hashtable<String, String>();

        Json json = new Json();

        // Save our levelcompletioninfo
        hashTable.put("levelcompletioninfo", json.toJson(this.levelCompletionInfo));

        // Store it in the preferences file
        levels.put(hashTable);
        levels.flush();
    }

    public boolean LoadLevelCompletionInfo() {
        // Load a serialized ArrayList
        Json json = new Json();

        String serializedData = levels.getString("levelcompletioninfo"); // level#data is old gravityGridLevel, level#completioninfo is old levelCompletionInfo

        // If the levelcompletion info string exists, then lets populate our level completion info.
        if(!serializedData.isEmpty()) {
            this.levelCompletionInfo = json.fromJson(LevelCompletionInfo.class, serializedData);
            return true;
        } else {
            return false;
        }
    }


}
