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

	public static int currentLevel;	// The current level
	public static int currentGalaxy; // The current galaxy (currentLevel / 25)

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

	// Global resources (yes, includes our fonts and our littleastronaut)
	SpriteBatch batch;
	BitmapFont regularFont; // The Roboto Slab font called "turkey.ttf"
	BitmapFont pixelFont; // The big GravityGridder font used for planet gravity values and special things.
	Texture littleAstronautImage; // Used for our loading screen
	TextureRegion littleAstronautRegion;

	public static int fontSize = 60;

	// This array holds the values of each tile
	public static final int[] tileValueTable = new int[] { 0,1,3,5,3,1,0,1,2,4,6,4,2,1,3,4,6,8,6,4,3,5,6,8,10,8,6,5,3,4,6,8,6,4,3,1,2,4,6,4,2,1,0,1,3,5,3,1,0 };



	// Each set of 25 levels is part of a new galaxy
	public static final String[] galaxyName = new String[] {
			"Andromeda",
			"Black Eye",
			"Bode's",
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
	public static final int[] boardMiddle = new int[] { 36,37,38,39,40,29,30,31,32,33,22,23,24,25,26,15,16,17,18,19,8,9,10,11,12 };
	public static final int[] boardEdge = new int[] { 42,43,44,45,46,47,48,35,28,21,14,7,0,1,2,3,4,5,6,13,20,27,34,41 };

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
	public static int[][] levelCompletionInfo = new int[][] { // This will ALWAYS instantiate to zero values
			{1,0,0,0,0}, // 1
			{0,0,0,0,0}, // 2
			{0,0,0,0,0}, // 3
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0}, // 10
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0}, // 15
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0}, // 20
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0},
			{0,0,0,0,0}
	};

	/* Level messages. Displayed at the bottom of each grid for its associated level */
	public static String[] levelMessage = new String[100];

	// Most levels will be randomly generated.
	public int[][] gravityGridLevel = new int[100][52];

	// Some levels are specifically constructed by me, like tutorial levels.
	public static final int[][] myCustomLevel = new int[][] {
			{	// Level 1
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,1,0,0,
					0,0,0,1,0,0,0,
					0,0,1,0,0,0,0,
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

			{	// 4
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,0,0,
					0,0,0,0,0,2,0,
					0,0,0,0,0,2,2,
					0,4,0,1
			},
			{	// 5
					0,2,0,0,0,2,0,
					0,0,0,0,0,0,0,
					2,0,0,0,0,0,2,
					0,0,2,0,2,0,0,
					0,0,0,0,0,0,0,
					0,0,0,2,0,0,0,
					0,0,0,0,0,0,0,
					0,13,0,3
			},
			{	// 6
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
	public static boolean pointInRectangle (Rectangle r, float x, float y) {
		return r.x <= x && r.x + r.width >= x && r.y <= y && r.y + r.height >= y;
	}

	// toPrettyDate helps us create a MM:SS string for displaying the next time our dark matter
	// will regenerate
	public static String toPrettyDate(long nanotime) {
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
		if(status == 1) {
			levelCompletionInfo[level][3] = moves; // Number moves to beat the game
			levelCompletionInfo[level][4] = points; // Add the number of points we earned by beating this level
			// Also mark the next level as ready to play ("1")
			if(level+1 < levelCompletionInfo.length-1) {
				levelCompletionInfo[level+1][0] = 1;
			}
		} else {
			levelCompletionInfo[level][3] = 0;
			levelCompletionInfo[level][4] = 0;
		}

		// Finally, set the NEXT level as the playable level
		levelCompletionInfo[level+1][0] = 1;

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

	/*{
		"Move the red planets so that their total score equals 16. Tap a planet to select it, then tap a blank tile to move it there.", // 1
				"Planets in some systems start out misaligned. That's okay! But if you MOVE a planet, you must follow that planet's rules.", //2
				"Red planets are always diagonal.", //3
				"Blue planets can only be moved next to other blue planets, on any side except diagonally.", //4
				"Planets that start out in violation of their rules can finish that way, too, as long as you don't move them.",//5
				"",//6
				"Anytime you move a planet you must follow the movement rules for that planet color.",//7
				"",//8
				"Sometimes you need to move planets of one color out of the way to make room for planets of a different color.",//9
				"Asteroids mean you can't use that tile. You'll have to work around it.",//10
				"",//11
				"If the planets are aligned, we can save the universe!",//12
				"Do you see the astronaut on this level? No? That's because there isn't one.",//13
				"",//14
				"Suns are just like asteroids, but take up a lot more space. You cannot move a planet into any tile that is under any part of a sun.",//15
				"Saturn's moon Titan has plenty of evidence of organic (life) chemicals in its atmosphere.",//16
				"A day in Mercury lasts approximately as long as 59 days on earth.",//17
				"The left pillar of the Pillars of Creation is 40 trillion kilometers long!",//18
				"If you think our sun is big, compare it to VY Canis Majoris.",//19
				"Well that's all, folks!",//20
				"",//21
				"",//22
				"",//23
				""//24
	};*/

	public void create() {

		gravityGridLevel[0] = myCustomLevel[0];
		levelMessage[0] = "Tap a planet to select it, then tap a tile diagonal to another planet. Keep moving planets like this until the total numbers in red equal 16.";
		gravityGridLevel[1] = myCustomLevel[1];
		levelMessage[1] = "Great! Notice the red numbers above the grid (35/32). Your current red score is 35; you must move the planets until your red score equals 32.";
		gravityGridLevel[2] = myCustomLevel[3];
		gravityGridLevel[3] = new int[] {0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,1};
		gravityGridLevel[4] = new int[] {0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,9,0,0,1};
		gravityGridLevel[5] = new int[] {0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,19,0,0,1};
		gravityGridLevel[6] = new int[] {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,28,0,0,1};



		/*
		gravityGridLevel[4] = levelGenerator.GenerateLevel(2,5,0,0);

		gravityGridLevel[5] = levelGenerator.GenerateLevel(2,5,0,0);
		gravityGridLevel[6] = myCustomLevel[3];
		//gravityGridLevel[4] = myCustomLevel[3]; an example of how I can inject my custom levels here and there
		gravityGridLevel[7] = myCustomLevel[4];
		gravityGridLevel[8] = levelGenerator.GenerateLevel(2,3,4,0);
		gravityGridLevel[9] = levelGenerator.GenerateLevel(2,3,4,0);
		gravityGridLevel[10] = levelGenerator.GenerateLevel(3,5,5,0);
		//gravityGridLevel[11] = levelGenerator.GenerateLevel(2,4,0,0);
		*/

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
