/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import static com.badlogic.gdx.utils.JsonWriter.OutputType.json;

public class GravityGrid extends Game {

	private Preferences ini;

	AssetManager assets = new AssetManager(); // Asset manager

	public int currentLevel;	// The current level
	public int currentGalaxy; // The current galaxy (currentLevel / 25)

	public int screenWidth;
	public int screenHeight;

	public boolean fingerOnScreen;


	// Explicitly set our colors for consistency
	Color colorDarkBlue = new Color(.38f,.57f,.80f,0.75f);
	Color colorLightBlue = new Color(.46f,.77f,.98f, .35f);
	Color colorYellow = new Color(.99f,.90f,.29f,1f);
	Color colorRed = new Color(.79f,.01f,.25f,1f);
	Color colorGreen = new Color(.60f,.77f,.23f,1f);
	Color colorBlue = new Color(.37f,.78f,.93f,1f);
	Color colorOrange = new Color(0.91f, 0.56f, 0.02f, 1.0f);

	// Global resources (yes, includes our fonts and our littleastronaut)
	SpriteBatch batch;
	BitmapFont regularFont; // The Roboto Slab font called "turkey.ttf"
	BitmapFont pixelFont; // The big GravityGridder font used for planet gravity values and special things.
	Texture gravityGridSphereLogoImage; // Used for our loading screen
	TextureRegion spaceTurkeyLogoRegion;

	public int fontSize = 60;

	// This array holds the values of each tile
	public final int[] tileValueTable = new int[] { 0,1,3,5,3,1,0,1,2,4,6,4,2,1,3,4,6,8,6,4,3,5,6,8,10,8,6,5,3,4,6,8,6,4,3,1,2,4,6,4,2,1,0,1,3,5,3,1,0 };

	// Each set of 25 levels is part of a new galaxy
	public final String[] galaxyName = new String[] {
			"Andromeda",
			"Bode's",
			"Black Eye",
			"Cartwheel",
			"Cigar",
			"Pinwheel",
			"Sunflower",
			"Tadpole"
	};

	/*
		LEVEL LEGEND:
		0 = blank
		1 = red
		2 = blue
		3 = green
		4 = asteroid
		5 = sun
		9 = blocked


		Last line syntax is as follows:
		RED SCORE NEEDED, BLUE SCORE NEEDED, GREEN SCORE NEEDED, PAR MOVES
	*/

	/*
	template
			0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,
			0,0,0,0,0,0,0,
			*/

	/* Board components. Helps differentiate between edges/corners and middle of board. */
	public final int[] boardMiddle = new int[] { 36,37,38,39,40,29,30,31,32,33,22,23,24,25,26,15,16,17,18,19,8,9,10,11,12 };
	public final int[] boardEdge = new int[] { 42,43,44,45,46,47,48,35,28,21,14,7,0,1,2,3,4,5,6,13,20,27,34,41 };

	// IntArrayContains is used specifically for the above boardMiddle and boardEdge arrays and in the canMoveAccordingToLogic function
	public boolean IntArrayContains(int[] list, int number) {

		for(int i=0; i<list.length; i++) {
			if(list[i] == number) {
				return true;
			}
		}
		return false;
	}

	/* Default values for level progress. Stores the <status,total_attempts,total_moves_attempted,moves_to_win,points_earned> of each level */
	public int[][] levelCompletionInfo = new int[][] { // This will ALWAYS instantiate to zero values
			{2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, // 05	// TODO: TURN ALL THESE BACK TO ZERO BEFORE PUSHING TO PUBLIC {1,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0},
			{2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, // 10
			{2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, // 15
			{2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, // 20
			{2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, {2,0,0,0,0}, // 25
			{2,0,0,0,0}, {1,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 30
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 35
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 40
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 45
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 50
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 55
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 60
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 65
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 70
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 75
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 80
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 85
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 90
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 95
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, //100
	};

	/* Level messages. Displayed at the bottom of each grid for its associated level */
	public String[] levelMessage = new String[100];

	// Most levels will be randomly generated. This string holds them all!
	// Future releases of the game simply need to increase the total number of levels
	public int[][] gravityGridLevel = new int[100][52];

	// This will correspond to the level#TutorialOverlay.png in our tutorials/* folder.
	// If our current level has an associated tutorial overlay (i.e., it = 1), then we will
	// Display at the start of that level the level#TutorialOverlay.png picture.
	// 0 = no tutorial
	// 1 = has an associated tutorial
	public int[] levelsWithTutorialOverlays = new int[100];

	// Some levels are specifically constructed by me, like tutorial levels.
	public final int[][] myCustomLevel = new int[][] {
			{	// Level 1
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,1,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,1,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					16,0,0,1
			},

			{	// Level 2
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,1,0,0,0,0,
					0,0,0,1,0,0,0,
					0,1,0,0,1,0,0,
					0,0,0,1,0,0,0,
					0,0,1,0,0,0,0,
					32,0,0,2
			},

			{ // 3
					0,0,1,0,0,0,0,
					0,1,0,0,0,1,0,
					1,0,1,0,1,0,0,
					0,0,0,1,0,0,0,
					1,0,1,0,1,0,0,
					0,1,0,0,0,1,0,
					0,0,1,0,0,0,0,
					42,0,0,4
			},

			{	// 4 Blue introduction
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,2,0,
					0,0,0,0,0,2,2,
					0,4,0,1
			},
			{	// 5 Blue Intro 2
					0,2,0,0,0,2,0,
					0,0,0,0,0,0,0,
					2,0,0,0,0,0,2,
					0,0,2,0,2,0,0,
					0,0,0,0,0,0,0,
					0,0,0,2,0,0,0,
					0,0,0,0,0,0,0,
					0,13,0,3
			},
			{	// 6 Blue Intro 3
					0,0,0,0,0,0,0,
					0,0,0,0,2,0,0,
					0,2,2,0,2,0,0,
					0,0,0,0,0,0,0,
					0,0,2,0,2,2,0,
					0,0,2,0,0,0,0,
					0,0,0,0,0,0,0,
					0,56,0,4
			},
			{	// 7
					0,2,0,0,0,0,0,
					0,2,1,0,0,0,0,
					0,0,0,1,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					5,6,0,2
			},
			{ // 8
					0,0,0,0,0,0,0,
					0,0,2,0,2,0,0,
					0,0,0,0,0,0,0,
					0,0,2,0,2,0,0,
					0,0,1,0,1,0,0,
					0,1,0,0,0,1,0,
					0,0,0,1,0,0,0,
					15,28,0,4
			},
			{	// 9
					0,0,0,0,0,0,1,
					0,0,0,0,0,0,0,
					0,0,2,2,2,0,0,
					0,0,2,1,2,0,0,
					0,0,2,2,2,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					16,50,0,5
			},
			{	// 10
					0,0,0,0,0,0,0,
					0,0,2,0,0,0,0,
					0,2,0,4,0,0,0,
					4,2,4,2,0,0,0,
					0,2,0,4,0,0,0,
					0,0,2,0,0,0,0,
					0,0,0,0,0,0,0,
					0,37,0,3
			},
			{	// 11
					0,0,0,0,0,0,0,
					4,0,0,1,0,0,4,
					1,0,1,4,1,0,1,
					0,1,0,0,0,1,0,
					1,0,1,4,1,0,1,
					4,0,0,1,0,0,4,
					0,0,0,1,0,0,0,
					42,0,0,8
			},
			{	// 12
					0,1,0,4,0,1,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,1,0,1,0,0,
					0,1,0,0,0,1,0,
					4,0,2,0,2,0,4,
					2,2,0,0,0,2,2,
					12,15,0,5
			},
			{	// 13
					0,0,2,0,2,0,0,
					0,0,0,2,0,2,0,
					0,4,2,4,2,4,0,
					4,0,0,0,0,0,4,
					0,0,4,0,4,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,40,0,3
			},
			{	// 14
					0,0,0,0,0,0,1,
					1,0,0,2,0,4,0,
					0,0,4,0,2,2,0,
					0,0,1,0,0,0,2,
					0,0,0,0,2,0,0,
					2,2,2,0,0,0,2,
					1,2,0,0,2,2,1,
					24,27,0,5
			},
			{	// 15
					0,1,9,9,9,1,0,
					4,1,9,5,9,1,4,
					0,1,9,9,9,1,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					32,0,0,4
			},
			{	// 16
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					2,0,1,0,2,9,9,
					4,2,4,2,4,9,5,
					1,0,2,0,1,9,9,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					8,32,0,5
			},
			{	// 17
					0,9,9,9,0,0,0,
					0,9,5,9,4,0,1,
					0,9,9,9,0,1,0,
					0,1,4,0,4,4,0,
					0,0,0,0,1,0,0,
					0,0,0,0,0,1,0,
					0,0,0,0,0,0,1,
					23,0,0,2
			},
			{	// 18
					2,0,4,0,4,0,0,
					2,0,0,4,0,0,0,
					2,0,0,0,0,2,2,
					2,2,2,0,0,2,0,
					2,0,0,0,0,2,2,
					2,0,4,0,4,0,0,
					2,0,0,4,0,0,0,
					23,0,0,2
			},
			{	// 19
					0,0,0,0,2,0,1,
					4,0,4,2,0,0,0,
					0,4,2,2,0,0,0,
					0,2,1,4,4,0,0,
					2,0,4,1,0,0,0,
					0,0,0,0,4,0,0,
					0,0,0,0,4,0,0,
					8,38,0,4
			},
			{	// 20
					1,0,0,3,0,0,1,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,3,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					1,0,0,0,0,0,1,
					18,5,0,4
			},


	};

	// GetLevel(#) is used to preload the PlayingScreen main playing array.
	/*public static int[] GetLevel(int levelNum) {
		return gravityGridLevel[levelNum];
	}*/

	// A custom point-in-rectangle collision checker
	public boolean pointInRectangle (Rectangle r, float x, float y) {
		return r.x <= x && r.x + r.width >= x && r.y <= y && r.y + r.height >= y;
	}

	// toPrettyDate helps us create a MM:SS string for displaying the next time our dark matter
	// will regenerate
	public String toPrettyDate(long nanotime) {
		long days = TimeUnit.NANOSECONDS.toDays(nanotime);
		nanotime -= TimeUnit.DAYS.toNanos(days);
		long hours = TimeUnit.NANOSECONDS.toHours(nanotime);
		nanotime -= TimeUnit.HOURS.toNanos(hours);
		long minutes = TimeUnit.NANOSECONDS.toMinutes(nanotime);
		nanotime -= TimeUnit.MINUTES.toNanos(minutes);
		long seconds = TimeUnit.NANOSECONDS.toSeconds(nanotime);

		StringBuilder sb = new StringBuilder(64);

		// Format MM :SS
		if(days>0) {
			sb.append(days); sb.append("d ");
		}
		if(hours>0) {
			sb.append(hours); sb.append("h ");
		}
		if(minutes > 0) {
			sb.append(minutes); sb.append("m");
		}
		if(seconds > 0) {
			sb.append(seconds);
			sb.append("s");
		}

		return(sb.toString());
	}

	// This is only called from our LEVEL_COMPLETE modal when we beat a level, or when we need to update values for a level
	// (like when we fail a level or close the program)
	// <status,total_attempts,total_moves_attempted,moves_to_win,points_earned>
	public void UpdateLevelCompletionInfo(int level, int status, int attempts, int moves, int points) {

		levelCompletionInfo[level][0] = status;
		levelCompletionInfo[level][1] += 1; // Add one attempt to this level
		levelCompletionInfo[level][2] += moves; // Total accrued moves this level for all attempts

		// Check if we are marking this level as 1 ("complete"). If we are, then we'll record the moves given as the number of moves
		// it took to beat this level. If not, we'll keep that at zero, since the level has not been marked complete yet.
		if(status == 2) {
			levelCompletionInfo[level][3] = moves; // Number moves used to beat the level during the playthrough where it was beaten
			levelCompletionInfo[level][4] = points; // Add the number of points we earned by beating this level
			if(levelCompletionInfo[level+1][0] == 0) {
				levelCompletionInfo[level+1][0] = 1;
				// Also mark the next level as ready to play ("1")
			}

		} else {
			levelCompletionInfo[level][3] = 0;
			levelCompletionInfo[level][4] = 0;
		}

		// We need to now loop through the levels backwards to find the most recent playable level. This will also
		// allow us to set the galaxy to the correct galaxy as well.
		findCurrentLevel:
		for(int i=gravityGridLevel.length; i<0; i--) {
			// Looping through each level, find [level]=0 and [level-1]=2 (beaten)
			if(levelCompletionInfo[i][0] == 0 && levelCompletionInfo[i-1][0] == 2) {
				levelCompletionInfo[i][0] = 1;
				currentLevel = i;
				break findCurrentLevel;
			}
		}

		if(currentLevel <= 25) {
			currentGalaxy = 0;
		} else {
			currentGalaxy = currentLevel / 25;
		}


		// Increment our currentLevel counter so we know the next one to load
		this.currentLevel++;

		// Finally, save the new values to our preferences file
		this.storeSaveState();
	}

	public void storeSaveState() {

		// Create a hashtable to store our level (and level completion) data.
		Hashtable<String, String> hashTable = new Hashtable<String, String>();

		Json json = new Json();

		// Here we are serializing our array
		hashTable.put("levelcompletion", json.toJson(levelCompletionInfo));

		// Store it in the preferences file
		ini.put(hashTable);
		ini.flush();
	}

	public void loadSaveState() {

		Json json = new Json();

		String serializedValues = ini.getString("levelcompletion");

		if(!serializedValues.isEmpty()) {
			levelCompletionInfo = json.fromJson(int[][].class, serializedValues);
			System.out.println("Loaded the following:");
			for(int a=0; a<levelCompletionInfo.length-1; a++) {
				System.out.println("Level "+a+": "+levelCompletionInfo[a][0]+","+levelCompletionInfo[a][1]+","+levelCompletionInfo[a][2]+","+levelCompletionInfo[a][3]+".");
			}
			currentLevel = 0;

			boolean setLevel = false;

			// Compute currentlevel
			computeCurrentLevel:
			for(int i=0; i<levelCompletionInfo.length; i++) {
				// iterate through and find the earliest level where list[0] (status) is 1 (playable), then set that to our currentLevel.
				if(levelCompletionInfo[i][0] == 1){
					currentLevel = i;
					setLevel = true;
					break computeCurrentLevel;
				}
			}

			if(setLevel == false) {
				// we iterated through every single one and still didnt find the currentLevel, then we need to set it as the earliest possible locked level
				// Loop through tiles?
				// For now:
				currentLevel = 0;
				levelCompletionInfo[0][0] = 1; // hard set the first one. This would only be called if this is a new game
			}
		}

		// Finally, compute current galaxy
		if(currentLevel <= 25) {
			currentGalaxy = 0;
		} else {
			currentGalaxy = currentLevel / 25;
		}
	}

	public void create() {

		// Instantiate the tutorial overlays
		for(int i=0; i<100; i++) {
			levelsWithTutorialOverlays[i] = 0;
		}

		// Manually set the levels with tutorial overlays
		levelsWithTutorialOverlays[0] = 1;	// level1TutorialOverlay.png on level 1
		levelsWithTutorialOverlays[1] = 1;	// level2TutorialOverlay.png on level 2
		levelsWithTutorialOverlays[2] = 1;  // level3TutorialOverlay.png on level 3
		levelsWithTutorialOverlays[3] = 0;
		levelsWithTutorialOverlays[4] = 0;
		levelsWithTutorialOverlays[5] = 0;
		levelsWithTutorialOverlays[6] = 1;  // level7TutorialOverlay.png on level 7



		gravityGridLevel[0] = myCustomLevel[0];
		levelMessage[0] = "";
		gravityGridLevel[1] = myCustomLevel[1];
		levelMessage[1] = "";
		gravityGridLevel[2] = new int[] {0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,9,0,0,1};
		levelMessage[2] = "Remember: Red planets can only be moved to a tile that is diagonal to another red planet.";
		gravityGridLevel[3] = new int[] {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,16,0,0,1};
		levelMessage[3] = "Fantastic! Now I suppose it's time to tell you why you're here: In 500 years, the universe will collapse.";
		gravityGridLevel[4] = new int[] {0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,19,0,0,1};
		levelMessage[4] = "An ultrastar will explode, causing huge gravity tidal waves. Thankfully, there's a way to prevent it.";
		gravityGridLevel[5] = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,24,0,0,1};
		levelMessage[5] = "Time travellers from the future have given you the Gravity Grid device to realign the gravity patterns in planetary systems across the universe.";
		gravityGridLevel[6] = myCustomLevel[3];
		levelMessage[6] = "Your mission is to realign all the planets in each system's Gravity Grid. Do this, and you will save the universe!";
		gravityGridLevel[7] = myCustomLevel[4]; // First blue
		levelMessage[7] = "Remember: Blue planets can only be moved to the top, bottom, left, or right of other blue planets.";
		gravityGridLevel[8] = myCustomLevel[5];
		levelMessage[8] = "Great! Just like red planets, blue planets might start in violation of their rules. Only when moving must the rules be followed.";
		gravityGridLevel[9] = myCustomLevel[6];
		levelMessage[9] = "Who knows how a planetary system will present itself. It's up to you to come up with the smartest solution.";
		gravityGridLevel[10] = new int[] {0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,8,0,1};
		levelMessage[10] = "";
		gravityGridLevel[11] = new int[] {0,0,0,2,2,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,13,0,1};
		levelMessage[11] = "";
		gravityGridLevel[12] = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,2,0,0,0,0,0,0,18,0,1};
		levelMessage[12] = "";
		gravityGridLevel[13] = myCustomLevel[7];
		levelMessage[13] = "Sometimes you need to move planets of one color out of the way to make room for planets of a different color.";
		gravityGridLevel[14] = myCustomLevel[8];
		levelMessage[13] = "Sometimes it seems like you need to make room for different color planets, but there might also be a different solution.";
		gravityGridLevel[14] = new int[] {0,0,0,0,1,2,0,0,2,0,0,1,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,2,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,11,16,0,3};
		levelMessage[14] = "Here, try to get the red AND blue scores aligned in only three moves. Can you do it?";
		gravityGridLevel[15] = new int[] {2,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,1,0,12,18,0,2};
		levelMessage[15] = "Remember that everything in our universe is connected. Could there be multiple ways to solve this problem?";
		gravityGridLevel[16] = new int[] {0,0,0,0,0,0,2,0,0,0,1,0,2,0,0,0,1,0,0,0,0,1,0,2,2,0,0,0,0,0,0,2,0,0,0,2,0,0,0,0,0,0,1,0,2,1,0,0,0,17,25,0,3};
		levelMessage[16] = "Each galaxy consists of 25 planetary systems (or levels). When you beat the 25th level of a galaxy, you unlock the next galaxy!";
		gravityGridLevel[17] = new int[] {0,0,0,1,0,0,0,0,0,0,2,0,0,0,0,0,2,0,0,0,0,0,2,0,0,0,0,0,0,1,0,0,0,0,0,2,0,1,0,0,0,0,0,0,0,1,0,0,0,21,30,0,2};
		levelMessage[17] = "";
		gravityGridLevel[18] = new int[] {0,0,0,0,0,2,0,1,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,2,0,0,0,1,0,0,2,2,0,1,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,17,14,0,2};
		levelMessage[18] = "Just seven more levels in this galaxy, including this one!";
		gravityGridLevel[19] = new int[] {0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,2,2,0,1,0,0,0,0,0,0,0,2,0,2,0,0,0,1,2,0,0,0,32,19,0,3};
		levelMessage[19] = "Six more levels--you're almost there!";
		gravityGridLevel[20] = new int[] {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,1,0,0,0,2,2,0,2,1,0,2,0,0,0,1,1,0,2,0,0,0,7,24,0,3};
		levelMessage[20] = "Five levels left!";
		gravityGridLevel[21] = new int[] {0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,1,2,2,2,0,0,1,2,0,0,1,0,0,2,0,0,0,2,0,2,2,0,0,0,0,0,0,0,0,19,38,0,3};
		levelMessage[21] = "Four more levels in this galaxy. Keep going!";
		gravityGridLevel[22] = new int[] {0,2,0,1,0,0,0,0,0,2,0,1,0,0,0,2,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,0,0,2,0,1,1,0,0,0,1,0,1,0,1,0,0,27,10,0,3};
		levelMessage[22] = "Three levels left!";
		gravityGridLevel[23] = new int[] {0,0,0,0,0,2,2,2,1,0,0,0,0,2,0,0,2,0,1,0,0,1,0,0,0,1,0,1,0,1,0,0,2,1,0,0,0,0,0,2,2,2,1,0,0,1,0,0,0,34,29,0,3};
		levelMessage[23] = "Two more levels until you unlock the next galaxy!";
		gravityGridLevel[24] = new int[] {0,0,0,1,0,0,2,0,1,0,0,0,0,2,1,2,0,0,1,0,2,0,0,0,0,0,0,0,1,1,0,2,1,0,0,0,1,0,1,2,0,0,2,0,2,0,2,0,0,41,24,0,4};
		levelMessage[24] = "This is the last level in this galaxy! After you beat this level, you can unlock the next galaxy!";

		gravityGridLevel[25] = myCustomLevel[10];
		levelMessage[25] = "Woohoo! You've unlocked the second galaxy, which has ASTEROIDS. Asteroids mean you can't use that tile.";
		gravityGridLevel[26] = new int[] {0,0,2,0,2,0,0,4,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,1,0,0,2,0,0,0,4,1,2,2,0,0,0,1,1,2,4,0,0,0,0,0,0,26,15,0,3};
		levelMessage[26] = "Asteroids cannot be moved, and no planet can be moved onto a tile that has an asteriod.";
		gravityGridLevel[27] = new int[] {1,0,4,4,0,2,1,0,4,2,0,2,1,0,2,0,0,0,2,2,0,0,0,0,0,0,1,0,0,4,0,0,1,2,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,17,27,0,3};
		levelMessage[27] = "";
		gravityGridLevel[28] = new int[] {2,4,0,0,0,0,0,2,0,0,0,0,0,0,2,2,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,4,0,1,1,2,0,0,0,0,2,0,2,0,0,0,4,1,0,14,18,0,3};
		levelMessage[28] = "";
		gravityGridLevel[29] =  new int[] {0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,1,2,0,0,0,0,0,0,4,2,4,0,1,1,2,2,0,0,0,0,0,0,0,0,2,0,1,0,2,0,0,28,17,0,3};
		    levelMessage[29] = "";
		gravityGridLevel[30] = new int[] {0,0,0,0,0,0,0,0,0,0,0,1,0,4,0,4,0,1,0,0,4,0,0,0,0,0,0,2,0,0,0,0,4,2,2,0,0,0,0,0,0,0,0,1,0,0,0,0,0,15,15,0,4};
		    levelMessage[30] = "";
		gravityGridLevel[31] = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,1,0,0,0,0,1,0,2,2,0,0,1,0,0,0,0,0,0,2,0,0,0,0,0,0,0,4,4,0,0,15,11,0,4};
		levelMessage[31] = "";
		gravityGridLevel[32] = new int[] {0,0,0,0,2,0,1,1,0,2,0,1,1,0,0,0,0,2,2,0,4,0,4,0,0,0,0,2,0,0,1,0,0,2,0,0,1,0,0,0,4,0,1,0,2,0,0,0,0,22,31,0,4};
		levelMessage[32] = "";
		gravityGridLevel[33] = new int[] {1,0,0,0,0,0,0,4,1,0,1,0,1,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,1,1,0,4,0,0,1,0,0,4,0,0,1,0,0,0,1,0,0,0,40,0,0,4};
		levelMessage[33] = "";
		gravityGridLevel[34] = new int[] {0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,4,0,1,0,0,4,0,0,1,0,1,1,1,0,0,0,0,1,1,1,0,0,0,0,0,1,0,38,0,0,4};
		levelMessage[34] = "";
		gravityGridLevel[35] = new int[] {4,0,0,0,0,0,4,0,0,0,1,0,0,0,0,0,0,0,1,1,0,1,4,1,0,0,0,0,0,0,0,0,1,0,0,1,0,0,1,0,0,0,0,0,0,4,1,0,0,37,0,0,4};
		levelMessage[35] = "";
		gravityGridLevel[36] = new int[] {0,0,0,1,2,0,1,2,2,1,0,0,0,0,0,2,0,0,0,0,0,2,0,1,4,1,4,0,2,0,0,0,0,2,1,0,0,0,0,0,4,0,0,0,1,0,0,0,0,34,20,0,4};
		levelMessage[36] = "";
		gravityGridLevel[37] = new int[] {0,0,0,0,0,1,0,4,0,4,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,2,0,0,1,0,4,4,0,0,2,0,1,4,0,7,12,0,4};
		levelMessage[37] = "";
		gravityGridLevel[38] = new int[] {2,2,0,0,0,0,4,2,2,0,2,0,1,4,4,4,4,2,0,1,2,0,2,0,4,0,2,1,2,0,1,1,1,2,0,0,0,4,4,1,4,0,0,1,2,1,2,4,0,38,45,0,4};
		levelMessage[38] = "";
		gravityGridLevel[39] = new int[] {4,1,1,0,4,4,0,2,0,4,2,4,2,4,0,1,1,0,4,2,1,1,1,0,0,4,0,0,0,2,0,0,0,4,1,1,2,2,2,0,0,2,2,4,0,2,2,2,4,32,39,0,4};
		levelMessage[39] = "";
		gravityGridLevel[40] = new int[] {0,0,1,0,2,2,0,2,0,4,4,0,0,2,0,1,0,4,1,0,0,2,0,0,1,0,1,0,0,0,0,1,0,4,4,0,0,1,0,1,2,2,1,0,0,1,1,4,0,46,11,0,4};
		levelMessage[40] = "";
		gravityGridLevel[41] = new int[] {1,0,2,1,2,1,4,0,1,4,4,0,4,4,4,2,0,4,0,1,4,4,2,0,4,0,4,4,4,0,0,0,0,4,4,2,1,0,1,4,2,1,0,4,1,1,1,2,0,41,21,0,4};
		levelMessage[41] = "";
		gravityGridLevel[42] = new int[] {2,0,4,4,1,0,1,0,0,0,0,0,4,1,2,4,0,4,4,4,0,2,2,2,2,0,2,2,0,0,2,0,0,0,2,2,0,4,4,4,2,0,0,2,2,2,4,4,4,4,52,0,4};
		levelMessage[42] = "";
		gravityGridLevel[43] = new int[] {0,2,2,0,0,0,0,0,0,0,0,4,0,0,2,0,0,0,0,1,0,0,4,0,2,2,0,2,0,1,0,2,0,0,2,2,0,2,2,2,0,2,1,0,2,0,0,2,0,8,70,0,4};
		levelMessage[43] = "";
		gravityGridLevel[44] = new int[] {0,0,1,2,0,4,1,1,1,4,0,1,0,0,4,0,1,1,0,4,1,4,4,4,4,0,0,1,4,0,0,1,0,4,0,4,0,2,2,1,0,1,1,4,2,1,0,0,4,45,12,0,4};
		levelMessage[44] = "";
		gravityGridLevel[45] = new int[] {0,0,4,2,0,4,0,0,4,1,4,4,4,4,0,4,2,2,4,2,2,2,0,2,4,2,1,2,0,4,0,2,0,1,1,0,2,4,4,4,2,4,2,0,0,0,0,2,0,14,70,0,4};
		levelMessage[45] = "";
		gravityGridLevel[46] = new int[] {1,0,4,1,4,0,1,1,1,1,4,0,1,1,4,1,0,1,1,0,1,4,4,2,0,1,4,4,0,0,0,4,4,0,2,0,1,4,4,0,0,4,0,2,0,4,2,4,0,59,15,0,4};
		levelMessage[46] = "";
		gravityGridLevel[47] = new int[] {2,1,1,4,2,1,2,1,4,2,0,2,1,0,4,2,4,2,0,2,1,4,0,2,2,2,1,0,1,1,4,0,4,2,0,1,1,2,0,1,2,0,4,4,1,0,1,4,4,41,61,0,4};
		levelMessage[47] = "";
		gravityGridLevel[48] = new int[] {0,2,4,2,0,0,2,1,2,4,2,4,0,0,4,4,2,2,4,4,2,1,0,0,1,0,0,2,0,0,0,1,1,1,2,0,4,0,1,1,0,4,0,4,4,1,1,0,4,40,37,0,5};
		levelMessage[48] = "";
		gravityGridLevel[49] = new int[] {1,4,2,0,0,2,2,4,2,2,1,0,1,2,2,0,0,0,4,0,0,4,0,0,0,1,0,2,0,0,0,1,0,0,0,1,4,0,0,0,4,2,1,1,0,1,1,2,0,31,26,0,5};
		levelMessage[49] = "";
		gravityGridLevel[50] = new int[] {2,4,1,1,0,0,0,0,2,0,2,2,0,0,0,4,0,2,0,4,4,0,4,0,4,4,4,1,4,2,2,2,1,1,2,0,4,1,1,0,1,4,0,1,4,2,1,4,4,35,43,0,5};
		levelMessage[50] = "";
		gravityGridLevel[51] = new int[] {0,2,0,0,0,0,2,0,1,4,0,0,0,1,0,4,0,0,0,0,1,0,4,0,4,4,2,2,4,0,0,1,1,0,4,2,2,4,1,1,4,0,2,0,0,0,0,4,0,22,15,0,5};
		levelMessage[51] = "";
		gravityGridLevel[52] = new int[] {2,0,1,1,0,0,1,4,1,1,0,4,4,0,4,2,0,0,0,4,0,4,2,4,4,2,0,2,0,1,0,0,0,0,2,0,0,4,2,0,4,0,1,0,0,4,4,0,4,19,32,0,5};
		levelMessage[52] = "";

		fingerOnScreen = false;

		// Prepopulate our screen geometry
		this.screenWidth = Gdx.graphics.getWidth();
		this.screenHeight = Gdx.graphics.getHeight();

		// Load our internal saved files from previous game plays
		ini = Gdx.app.getPreferences("license"); // Haha we named our preferences file "license" 

		// Load the saveState OR populate default values if this is a new install
		this.loadSaveState();

		batch = new SpriteBatch(); // Initialize our spritebatch used to draw everything in the world

		// Head to our InitialLoading screen, which loads all our assets
		this.setScreen(new InitialLoadingScreen(this));

	}

	public void render() {

		super.render(); // important!
	}

	public void pause() {
		// In the app lifecycle, this function is called both when the app loses focus AND right before the application closes, so it makes sense to store our save state here. 

		this.storeSaveState();
	}

	public void dispose() {

		batch.dispose();
		//regularFont.dispose();

		assets.clear(); // Clear out all assets that have been loaded.
		assets.dispose(); // Dispose of all our assets
	}
}
/*

CODE CLEANUP
* Get the particle effects pool out of the screens and into its own class or inside the game engine itself




BETA RELEASE

	* More levels!
	* [DONE] UNDO button as a reset button you hold on each level

ALPHA RELEASE

	[DONE] Beta-release one-page website
		- Simple template with what the game is and what it's doing 

	[DEFERRED] * currentMoves must be 0 when the level is complete. You MUST complete the level in the number of moves that is specified! (adds to the challenge)
		This also forces people to solve the puzzle in at least the number of moves that I took to create the level. 
	
	[DONE] Double check asteroid rules to make sure they only block the square they're in (I think they do)

	[DONE] Better loading screen that is specifically ALPHA version
	
	[DONE] Better screenshots on the alpha webpage
	
	[DONE] Test new gradle settings (keystore, proguard)
		- Interestingly, with the keystore and proguard additions my build time 
		went from a mere 13 seconds to 1m7s. Yikes!
	
ALL POST-ALPHA VERSIONS:
	
	
	* Generate more levels for paid version. 
	
	* "How to Play" button that goes to a screen of graphical tutorials. 
		- If a planet starts in violation of its rule, you do not need to fix it. 
		- However, whenever you move a planet you must place that planet next to a planet of the same color. 
		
	
	* Size of planets is no longer depenent on their value (maybe a little bit)
	
	* FREE VERSION: When the player is out of lives, You don't have to buy anything -- you just have to wait. 
		[Done] Player has lives that are represented by Dark Matter (which are the lives). 
		[Done] Each play is one Dark Matter. If you beat the level, you don't spend any Dark Matter.
		[Done] If you lose a level, you lose one Dark Matter. 
		[Done] If the player has less than the max number of dark matter (5), then a new one will appear
			in 20 minutes. 
		* A custom splashscreen for the OUT OF LIVES state should tell the player about the full version 
			of the game. 
	
	
	* PAID VERSION: Remove lives and life refill requirements.
	
	* Google Merchant Account to sell app for $0.99. 
	* NO ADS. (Blegh.)

	27-Aug:
	* [FUNCTIONAL] game.levelMessages[] are strings of text that, if exist, give us the ability to display a simple, single, text-wrapped screen of words at the start of a level. 
		Just display the message at the bottom of the level grid. Duh. 
	
	
	20-Aug: 
	* To avoid having all these silly problems with the Sun and Asteroid, I'm hard-coding the tiles that cannot be accessed
		with a 9. So asteroid tiles that cannot be accessed will look like this:
		
		0,9,0,
		9,4,9,
		0,9,0,
		
		And sun tiles will look like this
		
		9,9,9,
		9,5,9,
		9,9,9,

	18-Aug: 
	* preferences don't work on android unless you add ini.flush() after writing. Level saving works now. 
	* Was going to say add a level screen, but that's not necessary for this game. Just need more levels. 
	* ADD FEATURE: Help screen? 
	
	15-Aug:
	* (done) Column and row headers
	* (done, 20-Aug) random background image each level, 
	* (done) sound effects
	* KNOWN BUG: If you try to load a level that doesn't exist, the game crashes
	* Fixed the colors and add graphics for asteroids 
	* Made a better tile border (maybe embossed button-like?) and draw it always. 

	14-Aug:
	* Added You win/you lose checker
	* Added level restart function
	
	(13-Aug-2015 Jesse) 
		* Added level names
		* Finalized rules for movement
	

*/
