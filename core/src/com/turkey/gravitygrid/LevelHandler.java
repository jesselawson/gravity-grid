/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

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

    static class LevelCompletionInfo {
        LevelCompletionInfo() {
            levelData = new ArrayList<int[]>();
        }
        ArrayList<int[]> levelData;
    }

    Preferences levels;

    LevelHandler() {

        // Load our levels via preferences file
        levels = Gdx.app.getPreferences("levels");

        // If levels is empty, then we should probably cry
        this.levelCompletionInfo = new LevelCompletionInfo();

        if(!LoadLevelCompletionInfo() || this.levelCompletionInfo.levelData.size() < 25) {
            // We need at least 25 elements for the first galaxy

            // If we're here, then the levelcompletioninfo string in the "levels" file cannot be found. This would only be the case if this is our first time playing this game
            // So we need to build the level completion info, store it, then get on with our lives.

            int[] temp = new int[]{1,0,0,0,0}; // First one on a new install should be "1" status
            this.levelCompletionInfo.levelData.add(temp); // First level

            // After charging the levelData with the first level as status "1", make the rest of the galaxy available
            for(int a=1; a<25; a++) {
                int[] tmp = new int[]{0,0,0,0,0};
                this.levelCompletionInfo.levelData.add(tmp); // First level
            }

            // since this is our first data, let's store it
            SaveLevelCompletionInfo();

            // Also might have to put a check to recompute the current chunk of 25 elements in the levels... if the arraylist.get(#) tries to get a future level, the game will crash
        }

    }


    public int[] GenerateLevel(int a, int b, int c, int d, boolean e, boolean f) {

        int[] level = null;

        levelGenerator = new LevelGenerator();

        boolean goodResult = false;

        // TODO: If you can't build a good level in like 10 tries, then return a default level that looks like a smiley face
        while(!goodResult) {
            int tmp[][] = levelGenerator._GenerateLevel(a, b, c, d, e, f);

            if(tmp[0][0] == tmp[0][1] && tmp[0][0] > 0){

            } else if(tmp[0][2] == tmp[0][3] && tmp[0][2] > 0) {

            } else if(tmp[0][4] == tmp[0][5] && tmp[0][4] > 0) {

            } else {
                goodResult = true;
                //level = new int[52];
                level = tmp[1].clone();
                //System.out.println("Generated OK level "+a+": ("+tmp[0]+"/"+tmp[1]+"), ("+tmp[2]+"/"+tmp[3]+"), "+tmp[4]+"/"+tmp[5]+")");
            }
            //System.out.println("("+tmp[0]+"/"+tmp[1]+") ("+tmp[2]+"/"+tmp[3]+") ("+tmp[4]+"/"+tmp[5]+")");
        }

        return level;
    }



    int[] getLevel(int levelNum) {

        int[] level;

        // If this is a tutorial level, return the tutorial level.
        // Add any and all tutorial levels right here!
        switch(levelNum) {
            case 0: // Level 1
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,16,0,0,1}; // Level 1
                break;
            case 1: // Level 2
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,32,0,0,2}; // Level 2
                break;
            case 6: // Level 7
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,2,2,0,4,0,1}; // Level 7
                break;
            case 50: // Level 51
                level = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,8,1}; // Level 51
                break;
            default:
                level = CreateOrRetrieveLevel(levelNum);
                break;
        }

        return level;
    }

    int[] CreateOrRetrieveLevel(int levelNum) {

        // Make sure we create an entry for levelCompletionInfo as well
        if(levelNum+1 == getLevelCompletionInfo().levelData.size()) {
            // So if we were asking for level 5, and levelData.size() was 6, we could return levelData[5] but
            // we would still need to generate the next levelcompletion info. So let's build it here.
            int tmp[] = new int[]{0,0,0,0,0};
            getLevelCompletionInfo().levelData.add(tmp);
        }

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

            int a = 0;
            int b = 0;
            int c = 0;
            int d = 0; // init variables for use in our GenerateLevel() func below
            boolean e = false;
            boolean f = false;

            // Figure out our boundaries for random numbers given our current level
            switch(levelNum) { // Switch here to control the first galaxy
                case 2: a=1; b=3; break;
                case 3: a=1; b=4; break;
                case 4: a=1; b=5; break;
                case 5: a=1; b=6; break;

                case 7:  a=1; b=0; c=3; break;
                case 8:  a=1; b=0; c=4; break;
                case 9:  a=1; b=0; c=5; break;
                case 10: a=2; b=0; c=6; break;
                case 11:
                case 12: a=2; b=4; c=4; break;
                case 13:
                case 14:
                case 15: a=3; b=5; c=5; break;
                case 16:
                case 17:
                case 18: a=3; b=6; c=6; break;
                case 19:
                case 20: a=3; b=11; c=4; break;
                case 21: a=3; b=4; c=11; break;
                case 22:
                case 23:
                case 24: a=3; b=7; c=7; break;
                case 25:  // Game level 26
                case 26:
                case 27:
                case 28:
                case 29:
                case 30: a=3; b=MathUtils.random(2, 9); c=MathUtils.random(2,9); e = true;

                case 51:
                case 52: a=3; b=0;c=0;d=3; e=true;
                case 53: a=3; b=0;c=0;d=4; e=true;
                case 54: a=3; b=0;c=0;d=6; e=true;

                default:
                    // This only triggers after all the above levels have been accounted for:
                    if(levelNum > 30 && levelNum <= 50) {
                        a=MathUtils.random(2,4);
                        b=MathUtils.random(0, 9);
                        c=MathUtils.random(0,9);
                        e = true;
                    } else if(levelNum > 50 && levelNum <= 75) {
                        a = MathUtils.random(3, 6);
                        b = MathUtils.random(0, 11);
                        c = MathUtils.random(0, 11);
                        d = MathUtils.random(0, 11);
                        e = true;
                    } else {
                        // Anything here has levelNum > 75, so do everything
                        a = MathUtils.random(3, 8);
                        b = MathUtils.random(0, 11);
                        c = MathUtils.random(0, 11);
                        e = true;
                        f = true;
                }

            }

            level = GenerateLevel(a, b, c, d, e, f);

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

        // TODO: Add check here to see if we're on the last level of a galaxy. if we are, let's go ahead
        // and prepopulate the next 25 levels so that our level select screen doesn't crash.

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
            this.levelCompletionInfo.levelData = new ArrayList<int[]>();
            this.levelCompletionInfo = json.fromJson(LevelCompletionInfo.class, serializedData);
            return true;
        } else {
            return false;
        }
    }


}
