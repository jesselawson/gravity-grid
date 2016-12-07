/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
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
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GravityGrid extends Game {

	private Preferences ini;

	private boolean playSound;

	private int highestLevelCompleted;

	// Use this to grab whether the play wants to play sound or not.
	public boolean playerWantsSound() {
		return playSound;
	}

	public void toggleSound() {
		if(this.playSound) {
			this.playSound = false;
		} else {
			this.playSound = true;
		}
	}

	GameOptions gameOptions;

	public GameOptions getOptions() {
		return gameOptions;
	}

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
	AssetLoader assets;

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
			{1,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 05
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 10
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 15
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 20
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 25
			{0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, {0,0,0,0,0}, // 30
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

	// Most levels will be randomly generated. This string holds them all!
	// Future releases of the game simply need to increase the total number of levels
	public final int[][] gravityGridLevel = new int[][] {
			// GALAXY 1
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,16,0,0,1}, 	// Level 1
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,1,0,0,0,0,1,0,0,1,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,32,0,0,2}, 	// Level 2
			{0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,9,0,0,1}, 	// Level 3
			{0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,25,0,0,2}, 	// Level 4
			{1,0,0,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,1,0,0,0,0,0,0,1,0,0,0,0,0,22,0,0,2}, 	// Level 5
			{0,0,0,0,0,0,0,1,0,1,0,0,0,0,0,1,0,0,0,0,0,0,1,0,0,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1,0,0,29,0,0,2}, 	// Level 6
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,0,0,2,2,0,4,0,1}, 	// Level 7
			{0,0,0,0,1,0,0,1,0,0,0,0,0,0,0,2,1,1,2,2,0,2,2,0,0,0,2,0,0,0,0,0,0,0,0,0,2,0,0,1,0,0,0,0,2,0,0,0,0,15,25,0,3}, 	// Level 8
			{0,0,0,2,0,0,0,0,0,2,0,2,2,0,0,1,0,0,1,0,0,2,2,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,0,0,0,0,0,1,0,1,0,24,27,0,4}, 	// Level 9
			{0,1,0,0,0,0,0,0,0,1,0,0,0,0,0,1,0,2,2,0,0,0,0,2,0,0,1,1,0,2,0,2,1,0,0,0,0,0,0,1,0,0,0,0,0,0,2,0,0,34,37,0,3}, 	// Level 10
			{0,0,1,0,0,0,1,0,2,1,0,1,0,0,0,0,1,1,0,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,2,0,0,1,2,0,2,2,1,0,0,2,0,0,0,32,20,0,4}, 	// Level 11
			{0,0,0,0,0,2,0,0,0,1,0,1,0,2,0,0,0,2,0,0,0,0,0,1,0,1,0,0,0,0,2,0,0,0,2,0,0,0,0,2,2,1,1,0,0,1,0,2,0,34,28,0,3}, 	// Level 12
			{0,0,0,1,0,0,0,0,2,0,1,0,0,0,2,1,2,0,0,1,0,0,0,0,2,0,0,1,0,2,0,0,0,0,0,0,0,1,0,1,0,1,1,0,0,0,0,0,0,40,28,0,4}, 	// Level 13
			{1,1,0,0,0,0,0,0,0,2,0,0,1,2,1,0,1,0,0,2,0,0,0,0,0,0,0,0,0,0,1,0,1,0,1,2,2,2,0,0,0,0,1,0,0,0,1,1,0,32,18,0,4}, 	// Level 14
			{0,2,0,1,0,0,0,0,0,0,2,0,0,0,0,0,0,0,0,0,1,0,0,0,0,0,0,2,0,0,0,2,2,0,0,0,0,0,0,1,0,2,0,2,0,0,0,0,0,14,29,0,4}, 	// Level 15
			{0,0,0,0,0,0,0,0,2,0,0,0,2,0,0,0,2,2,0,0,0,0,0,0,0,0,0,0,0,0,0,2,2,0,0,0,0,0,0,0,0,2,0,0,2,2,2,2,0,0,49,0,4}, 	// Level 16
			{1,0,0,0,0,1,0,1,0,1,0,0,0,0,0,2,0,0,0,1,0,1,0,0,0,0,2,0,0,0,0,2,0,0,0,2,0,0,0,1,0,0,0,0,1,0,1,0,0,28,23,0,3}, 	// Level 17
			{0,0,0,1,2,0,0,0,0,0,0,1,0,1,0,1,0,2,0,2,0,0,2,0,1,0,2,0,0,0,0,0,1,0,0,0,2,0,0,0,1,0,1,2,2,2,0,1,0,33,44,0,4}, 	// Level 18
			{1,0,2,1,2,1,2,0,0,0,0,0,0,0,2,0,0,0,0,2,0,0,0,2,0,0,2,0,0,0,1,1,0,0,0,0,1,0,1,0,2,0,0,0,1,0,0,0,0,31,23,0,4}, 	// Level 19
			{0,0,0,0,0,1,0,1,0,1,0,0,0,0,0,0,0,0,0,0,0,2,1,0,0,2,1,0,1,0,2,0,0,0,0,0,2,0,0,0,0,2,0,1,0,2,2,0,0,27,35,0,4}, 	// Level 20
			{0,1,1,0,1,0,0,0,0,1,0,0,0,0,0,0,0,0,1,1,0,1,0,1,0,2,1,2,1,0,0,2,0,1,0,0,0,1,0,0,0,0,0,0,1,0,0,0,0,61,21,0,4}, 	// Level 21
			{0,0,0,2,0,0,0,0,0,0,0,1,0,0,2,2,2,0,2,2,0,0,0,2,2,1,1,0,0,2,0,2,0,0,0,0,0,0,2,0,2,0,0,0,0,0,2,0,0,18,69,0,3}, 	// Level 22
			{1,1,0,0,0,0,0,0,2,2,1,0,0,2,0,1,0,0,2,2,0,2,0,0,0,2,0,2,2,0,2,0,0,0,1,0,0,1,0,0,2,0,1,0,0,2,2,2,0,18,49,0,4}, 	// Level 23
			{0,0,0,0,2,2,1,2,1,0,0,0,0,2,0,0,0,0,0,0,1,0,0,0,0,1,0,0,1,0,2,1,1,2,2,0,0,0,1,0,0,0,0,2,0,1,0,2,0,40,20,0,4}, 	// Level 24
			{0,0,0,0,0,0,0,1,0,0,1,0,2,0,0,0,0,0,0,1,0,2,0,2,0,0,0,0,0,2,0,0,2,0,2,0,1,0,0,0,0,0,1,0,0,0,0,1,0,12,34,0,5}, 	// Level 25

			// GALAXY 2 Red, Blue, Asteroids
			{0,2,0,4,4,4,0,4,0,0,0,4,0,0,4,0,0,4,0,0,4,0,4,4,0,0,1,1,0,0,0,0,0,0,0,4,0,2,0,0,0,0,0,4,0,0,0,0,0,12,1,0,3}, 	// Level 26
			{4,0,4,0,0,2,0,4,4,0,0,1,0,0,0,0,4,2,0,2,1,0,4,0,0,0,4,0,4,1,0,0,0,4,0,0,0,0,0,4,4,0,0,0,4,4,0,4,0,9,7,0,3}, 	// Level 27
			{0,0,0,0,1,0,4,0,4,0,0,0,0,0,0,4,0,0,0,4,1,2,0,0,0,2,2,0,0,0,1,2,0,0,0,4,0,4,0,4,0,0,0,4,0,4,1,0,0,15,21,0,3}, 	// Level 28
			{4,0,2,0,4,1,1,1,0,4,0,0,2,0,0,0,1,4,0,4,4,0,4,0,4,0,4,4,0,4,1,4,4,0,4,0,0,0,2,4,4,2,2,4,0,0,4,4,4,9,15,0,4}, 	// Level 29
			{0,4,1,2,0,2,0,0,0,0,0,0,0,1,0,0,4,4,1,0,4,2,4,0,0,0,4,0,0,1,2,0,4,0,0,4,0,0,4,2,1,0,4,4,2,4,1,4,4,17,24,0,4}, 	// Level 30
			{0,0,0,2,0,0,0,1,1,1,0,4,0,0,1,2,0,0,0,0,0,0,0,0,2,1,1,0,0,4,2,0,0,0,1,0,0,4,4,4,0,4,2,0,0,0,0,2,4,32,32,0,4}, 	// Level 31
			{1,2,1,4,2,0,4,4,4,4,4,2,4,0,0,1,0,2,0,0,0,0,0,1,0,0,0,0,1,0,1,0,0,0,4,4,4,0,0,0,0,2,2,1,0,0,0,1,0,30,23,0,5}, 	// Level 32
			{0,1,2,0,0,0,1,0,0,0,0,0,2,0,2,0,0,4,0,1,0,2,2,1,1,1,1,0,0,0,0,2,1,0,0,0,0,0,0,1,0,0,0,2,0,0,0,0,0,48,28,0,4}, 	// Level 33
			{0,0,0,0,0,2,1,1,0,2,0,0,2,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,2,1,4,0,2,0,2,0,2,0,0,1,1,2,0,1,1,1,0,1,0,31,22,0,4}, 	// Level 34
			{0,2,0,0,2,0,2,0,1,0,4,4,0,0,2,2,0,2,1,1,1,2,0,2,0,0,0,1,0,0,1,0,1,1,0,2,0,1,1,4,4,0,2,2,0,0,0,1,0,42,39,0,5}, 	// Level 35
			{0,0,1,1,0,0,1,4,1,1,0,4,0,4,4,0,0,0,4,0,4,0,4,0,0,0,0,0,1,0,0,0,0,1,0,0,1,1,0,0,0,1,1,0,0,4,0,0,0,34,0,0,5}, 	// Level 36
			{0,2,0,0,4,2,0,0,2,2,4,0,4,0,0,4,0,4,0,0,0,2,2,0,2,2,0,0,0,0,0,0,0,0,0,0,2,4,0,0,0,2,2,0,0,0,0,0,0,0,43,0,4}, 	// Level 37
			{4,0,2,2,2,1,2,0,0,4,0,1,2,0,0,0,4,4,2,4,2,1,4,2,4,4,4,2,0,0,4,0,2,1,4,4,1,0,0,0,4,0,1,4,4,2,1,0,0,16,46,0,4}, 	// Level 38
			{1,1,4,0,1,2,2,0,0,2,4,0,1,0,0,0,2,0,4,1,1,4,1,1,2,0,4,0,1,0,0,2,0,1,4,0,2,0,0,0,0,0,4,1,0,0,0,0,0,46,30,0,5}, 	// Level 39
			{2,4,0,0,0,1,2,0,0,1,2,2,0,4,0,4,0,0,1,4,4,2,4,1,1,0,4,0,1,0,1,0,0,0,0,2,1,1,0,0,0,0,4,0,4,4,4,2,4,38,17,0,5}, 	// Level 40
			{1,0,4,4,0,0,1,1,2,0,0,4,2,0,2,4,4,4,4,2,0,4,0,2,2,4,1,4,4,0,0,4,4,4,4,1,1,4,4,1,4,2,2,0,4,1,2,1,4,22,29,0,5}, 	// Level 41
			{1,0,4,2,0,4,4,1,4,2,4,0,2,4,0,1,0,0,0,1,1,4,4,0,0,0,1,1,4,0,2,2,0,4,4,2,0,0,1,2,4,0,2,1,2,4,4,0,0,34,42,0,5}, 	// Level 42
			{0,0,1,0,1,0,2,0,0,2,0,0,2,1,4,0,0,1,0,1,0,2,2,4,0,1,0,0,0,0,1,1,0,1,2,0,0,2,2,0,4,0,0,2,0,0,0,0,0,45,31,0,5}, 	// Level 43
			{4,4,2,4,0,2,0,0,4,0,1,4,4,4,1,4,0,0,1,1,2,1,2,0,4,4,0,0,1,2,0,4,0,0,4,0,2,1,2,4,0,1,2,0,0,0,1,2,4,33,32,0,5}, 	// Level 44
			{4,4,4,4,1,4,4,1,1,4,4,2,4,4,4,1,0,2,2,0,2,4,0,0,0,0,4,1,4,0,1,0,4,4,0,0,0,1,1,0,4,0,1,0,1,0,1,0,4,55,23,0,5}, 	// Level 45
			{2,2,0,0,1,0,0,0,4,0,0,4,0,0,4,4,0,2,1,0,0,1,1,0,4,4,0,2,0,0,1,0,0,0,0,0,0,0,1,1,0,1,1,4,1,1,0,4,0,48,17,0,5}, 	// Level 46
			{1,2,0,0,4,2,0,1,0,2,0,4,4,2,0,2,0,0,4,2,0,4,1,0,0,0,0,4,2,2,0,1,4,2,4,4,0,4,0,0,2,4,4,4,0,0,2,0,4,22,38,0,5}, 	// Level 47
			{4,4,4,4,2,0,0,2,2,2,0,2,4,0,2,0,4,4,4,2,0,1,0,1,0,4,0,4,2,1,4,0,0,4,1,2,2,0,0,4,4,0,4,0,0,4,4,2,0,20,36,0,5}, 	// Level 48
			{1,0,4,1,0,1,2,0,0,1,0,4,0,0,4,0,4,0,0,4,1,2,2,2,4,0,0,0,1,2,1,4,0,1,0,4,0,0,0,4,2,0,0,0,2,0,0,2,0,27,28,0,5}, 	// Level 49
			{4,2,0,0,1,2,1,0,0,0,0,0,1,0,1,0,0,4,4,2,4,0,4,1,4,1,0,0,1,0,2,4,0,2,4,2,4,0,0,0,2,0,0,4,1,2,4,0,0,33,18,0,5}, 	// Level 50

			// GALAXY 3 Green planets, then mixed
			{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,3,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,0,0,13,1}, // Level 51 W/ Tutorial Overlay
			{3,0,0,0,0,0,0,4,0,3,0,0,0,3,0,0,0,3,0,0,0,3,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,4,0,0,0,0,0,0,0,0,0,0,0,0,0,17,1}, // Level 52
			{0,0,3,4,0,0,0,4,0,3,0,4,0,3,0,3,0,3,0,0,0,0,0,4,0,0,3,3,0,0,0,0,0,0,0,0,0,0,3,0,0,0,0,0,0,0,0,3,0,0,0,36,1}, // Level 53
			{4,0,0,3,1,4,4,0,4,1,0,0,4,0,0,4,4,4,3,0,0,3,4,3,0,4,4,4,1,4,1,4,0,3,4,1,1,0,4,0,3,4,0,4,4,0,0,4,0,26,0,23,2}, // Level 54
			{1,4,4,0,4,0,0,4,0,0,0,0,0,4,0,4,3,4,4,0,4,1,3,3,1,0,3,3,0,1,0,4,4,1,4,0,0,1,4,3,4,0,4,0,0,4,0,0,0,27,0,28,2}, // Level 55
			{4,0,0,0,0,0,0,4,4,0,0,0,0,0,0,0,0,0,1,0,0,0,0,3,4,0,3,0,4,0,1,1,0,1,0,0,0,1,3,1,3,0,3,4,3,0,0,0,0,29,0,25,2}, // Level 56
			{3,0,0,2,1,0,0,3,1,4,0,1,4,0,1,0,0,0,0,4,4,2,4,3,0,0,1,2,0,0,3,4,4,4,4,0,0,0,3,3,2,0,4,0,0,0,1,1,4,21,17,25,3}, // Level 57
			{0,1,2,0,0,3,3,4,0,1,0,0,1,0,0,0,0,3,0,0,0,0,2,4,0,3,0,0,2,0,0,0,4,0,3,0,0,0,0,2,0,1,2,0,2,2,0,3,4,8,22,21,3}, // Level 58
			{4,4,4,3,4,0,4,4,3,4,3,0,0,3,4,0,0,4,4,4,0,0,0,4,0,1,1,0,4,0,2,0,0,4,3,0,4,2,1,4,4,4,2,3,3,2,4,1,4,21,15,27,3}, // Level 59
			{4,3,3,0,2,0,0,4,4,1,0,4,4,0,4,2,0,0,1,0,2,4,0,3,3,0,1,3,1,0,0,4,4,4,0,0,4,4,3,4,1,4,3,2,4,0,4,0,4,25,17,33,4}, // Level 60
			{0,4,0,3,0,3,0,0,1,0,0,4,1,0,0,0,0,3,4,0,0,0,4,1,3,1,0,0,0,0,4,4,1,0,0,0,4,1,0,0,3,3,0,3,0,1,4,4,4,33,0,30,4}, // Level 61
			{1,0,4,1,0,1,1,0,3,0,0,1,0,3,3,3,0,4,3,4,0,0,0,3,0,0,0,4,1,3,0,0,4,0,0,0,0,0,0,0,0,0,0,4,1,4,0,0,0,15,0,27,4}, // Level 62
			{2,0,0,4,2,0,4,4,4,1,4,3,3,0,0,3,2,3,0,1,3,4,3,0,0,0,4,1,3,0,2,0,1,4,4,0,1,2,3,2,3,0,0,4,1,4,0,0,0,24,20,37,4}, // Level 63
			{0,0,0,3,2,0,4,0,3,0,0,1,0,0,0,4,2,0,2,0,0,3,1,0,3,4,4,4,0,2,1,3,1,0,3,0,0,2,3,1,0,0,3,0,4,4,4,1,0,20,23,33,4}, // Level 64
			{0,0,0,0,4,0,4,2,4,4,0,0,3,0,0,4,1,0,3,0,0,0,2,1,0,0,0,4,0,0,0,4,2,3,0,0,1,0,1,3,0,0,0,0,4,2,0,0,0,19,21,17,5}, // Level 65
			{4,0,0,0,0,1,2,0,0,0,0,0,4,0,3,0,1,0,3,1,2,0,1,3,0,0,0,2,1,3,4,0,2,0,0,4,0,0,4,0,4,2,0,0,0,0,0,3,4,19,28,22,5}, // Level 66
			{1,4,4,0,1,0,4,1,0,0,0,0,0,0,1,3,2,0,1,0,2,4,0,0,2,4,0,3,3,0,3,0,2,1,4,0,3,0,2,0,2,0,0,0,0,0,4,3,0,16,34,22,5}, // Level 67
			{4,3,2,4,0,1,1,4,0,1,1,4,4,1,1,3,0,1,2,0,4,0,0,4,0,4,4,4,4,4,0,2,0,3,3,4,1,0,0,1,0,2,1,4,3,2,4,4,0,31,24,21,5}, // Level 68
			{4,4,0,0,2,0,0,0,4,0,1,0,0,0,0,0,0,3,3,1,3,4,2,0,0,0,1,0,3,4,2,2,3,2,2,4,1,2,0,0,0,2,4,0,2,1,2,4,0,23,47,26,5}, // Level 69
			{0,2,2,1,3,0,0,2,3,0,4,3,1,0,0,1,2,4,0,0,1,3,0,4,1,1,4,0,0,3,4,0,2,0,2,4,1,0,4,0,1,0,0,0,0,2,0,1,4,36,20,18,6}, // Level 70
			{0,3,3,0,0,0,4,1,1,0,3,4,0,0,3,2,0,3,0,1,4,0,1,0,0,2,4,0,1,4,4,3,4,4,0,2,0,2,4,3,3,4,1,1,0,0,3,2,0,24,24,36,6}, // Level 71
			{3,0,3,3,3,2,1,3,0,0,4,0,2,2,0,4,3,4,0,2,1,0,0,3,0,4,4,0,0,0,0,2,3,3,0,0,0,0,0,2,1,4,0,1,2,0,1,0,4,17,26,28,6}, // Level 72
			{2,3,0,4,0,0,4,3,0,2,4,1,3,1,2,2,0,3,4,2,2,0,0,0,0,1,0,3,1,0,1,0,2,0,0,4,4,4,0,2,2,0,4,4,1,4,1,0,4,31,24,17,6}, // Level 73
			{0,3,4,0,0,4,3,0,0,3,0,3,0,3,0,0,0,0,0,0,0,0,0,3,0,0,3,0,0,3,0,0,0,0,0,0,0,4,0,0,0,0,0,3,3,0,0,0,4,0,0,36,6}, // Level 74
			{0,4,3,0,0,3,3,0,0,3,0,0,4,0,0,4,4,0,4,0,0,0,4,3,0,4,3,0,4,0,4,0,0,4,0,0,3,4,0,3,0,0,0,3,3,4,0,4,4,0,0,36,6}, // Level 75

			// GALAXY 4 Everything and mixed
			{0,0,0,0,0,0,9,9,0,0,0,0,9,5,9,0,1,0,3,9,9,2,2,0,2,0,0,1,0,0,0,0,0,0,0,0,0,0,0,1,0,0,0,3,0,0,3,0,0,15,21,12,3}, // Level 76
			{0,1,0,3,0,0,0,2,0,0,0,0,0,0,0,0,1,2,3,1,0,0,2,3,0,0,0,0,1,0,0,0,0,0,0,9,9,0,0,2,0,3,5,9,0,0,0,0,0,16,17,23,3}, // Level 77
			{0,3,0,0,3,0,9,9,0,0,0,0,9,5,9,0,0,0,0,9,9,0,0,0,0,0,0,1,1,0,0,0,0,0,0,2,3,0,0,2,1,0,0,0,0,0,0,2,0,14,5,6,3}, // Level 78
			{4,3,0,1,4,1,2,2,4,0,0,0,2,0,0,4,0,1,4,0,4,0,0,0,0,0,1,4,1,0,2,0,0,3,4,1,3,0,9,9,9,4,0,0,3,9,5,9,0,27,10,10,3}, // Level 79
			{2,4,0,3,0,0,4,0,2,4,2,4,1,0,4,4,4,4,2,0,1,1,3,2,4,4,0,4,0,0,4,4,3,4,0,4,0,4,4,2,9,9,4,3,1,0,4,9,5,14,26,16,3}, // Level 80

			{0,9,5,9,0,0,0,3,9,9,9,0,0,2,0,2,3,0,0,1,0,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,2,0,0,0,0,3,0,0,0,0,1,0,15,8,3,4}, // Level 81
			{0,0,2,9,5,9,0,1,3,0,9,9,9,0,0,0,3,0,0,0,0,0,0,0,0,0,3,2,0,0,2,0,0,0,0,0,2,1,0,0,0,0,0,0,3,1,0,0,0,10,10,17,4}, // Level 82
			{0,3,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,9,9,0,2,0,0,9,5,9,0,1,1,0,9,9,0,3,0,0,2,0,0,0,2,0,0,3,0,0,20,14,11,4}, // Level 83
			{4,0,4,4,4,3,1,0,1,2,2,0,4,0,0,3,0,0,1,4,4,2,1,4,0,4,4,0,4,0,0,4,0,1,4,2,4,9,9,9,4,1,0,4,9,5,9,3,0,23,13,11,4}, // Level 84
			{1,4,0,0,0,3,0,4,0,0,1,4,2,2,4,4,2,0,2,4,0,9,9,2,1,4,4,3,5,9,3,3,0,4,9,9,0,2,4,4,0,4,4,0,0,4,4,4,0,24,27,18,4}, // Level 85

			{0,2,0,4,0,9,5,0,0,4,1,4,9,9,0,3,0,0,0,2,0,1,4,0,0,3,0,4,0,0,0,4,1,0,0,1,0,4,4,0,3,3,0,2,0,0,0,2,0,15,6,15,4}, // Level 86
			{0,4,0,0,0,4,1,4,3,0,3,3,0,0,9,9,2,4,4,1,0,5,9,2,0,0,0,9,9,4,0,0,2,0,4,4,1,0,3,0,2,0,0,4,0,1,4,4,4,7,18,18,5}, // Level 87
			{4,0,4,4,2,9,5,4,3,4,4,3,9,9,1,4,2,0,4,0,0,0,0,4,2,1,0,0,0,3,0,1,4,1,2,4,4,4,4,0,4,0,0,3,4,0,4,4,4,18,22,12,4}, // Level 88
			{0,4,4,2,4,1,9,9,4,4,4,4,9,5,9,0,0,4,2,9,9,2,4,4,0,0,2,0,1,1,4,3,0,0,0,3,0,3,4,4,4,4,1,3,4,4,4,4,4,9,20,10,5}, // Level 89
			{0,0,2,3,0,0,3,0,2,0,2,0,3,1,0,4,2,4,0,0,0,0,4,4,0,1,3,4,0,0,0,0,0,0,4,0,1,9,9,9,4,1,4,4,9,5,9,0,0,11,15,16,4}, // Level 90

			{0,3,0,4,3,0,0,0,0,4,0,0,4,0,0,0,0,0,3,0,4,9,9,0,0,0,0,0,5,9,4,4,0,3,9,9,0,4,4,0,0,4,0,0,0,4,0,0,0,0,0,23,4}, // Level 91
			{0,0,0,0,4,4,0,0,0,4,0,3,0,0,4,3,0,0,0,0,9,9,0,0,4,4,9,5,9,0,4,3,0,9,9,0,0,0,0,0,0,0,3,0,0,0,0,4,0,0,0,11,5}, // Level 92
			{1,0,4,4,0,4,3,4,0,4,0,0,1,0,4,0,0,0,1,0,0,0,4,3,3,4,1,0,0,0,0,0,0,0,4,9,9,4,4,0,0,0,5,9,3,0,4,4,0,14,0,14,4}, // Level 93
			{0,2,0,0,0,0,9,9,4,0,0,0,9,5,9,3,0,0,0,9,9,0,0,0,0,3,0,0,0,2,0,0,3,0,0,2,0,0,0,2,0,0,0,3,0,0,0,0,0,0,6,22,5}, // Level 94
			{2,0,0,4,0,1,0,0,4,0,0,0,0,0,4,1,4,4,2,4,4,4,0,4,0,2,0,0,0,4,4,4,1,2,0,0,4,4,9,9,9,0,4,1,4,9,5,9,4,12,24,0,4}, // Level 95
			{4,4,1,0,0,1,0,0,0,0,0,0,4,0,0,0,1,0,0,1,1,9,9,0,0,4,4,4,5,9,1,0,0,0,9,9,0,0,0,4,0,0,4,0,0,0,4,0,4,32,0,0,5}, // Level 96
			{4,0,0,4,0,0,0,0,2,0,4,2,0,0,0,0,2,0,0,4,4,0,0,0,0,0,4,0,9,9,0,0,0,4,4,5,9,0,4,4,0,9,9,0,2,4,2,2,0,0,23,0,5}, // Level 97
			{4,9,5,9,4,0,3,4,9,9,9,0,0,4,0,0,4,3,3,4,0,4,4,0,0,4,4,4,0,4,3,0,4,0,0,4,4,0,0,0,4,3,3,0,0,0,0,0,0,0,0,18,5}, // Level 98
			{4,0,0,0,0,0,0,3,0,2,0,0,0,0,3,0,0,0,4,2,0,3,3,0,0,0,0,0,4,2,0,0,3,0,0,0,0,4,9,9,9,2,4,2,0,9,5,9,4,0,16,20,5}, // Level 99
			{4,1,4,0,3,9,5,4,4,0,0,4,9,9,3,4,4,4,1,3,4,3,3,2,0,2,0,0,2,4,4,2,4,0,4,0,0,0,1,4,2,1,0,0,1,0,0,0,0,15,34,19,5} // Level 100

	};

	/* Level messages. Displayed at the bottom of each grid for its associated level */

	// TODO: Rewrite all strings to fit on top of the banner ad.
	// This fits: Sometimes it seems like you need to make room for different color planets, but there might also be
	public List<String> levelMessage;

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
	/*public int[] GetLevel(int levelNum) {
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
			highestLevelCompleted = level;
			levelCompletionInfo[level][3] = moves; // Number moves used to beat the level during the playthrough where it was beaten
			levelCompletionInfo[level][4] = points; // Add the number of points we earned by beating this level

			// Check if there's a level past this one. If there is, set its status as "1" (ready to play)
			if(level+1 < levelCompletionInfo.length) {
				if (levelCompletionInfo[level + 1][0] == 0) {

					// Also mark the next level as ready to play ("1")
					levelCompletionInfo[level + 1][0] = 1;

					// Increment our currentLevel counter so we know the next one to load
					this.currentLevel++; // should also be equal to level+1

				}
			} else {
				// If we can't assign a current level to be level+1, then just keep the currentLevel at the maximum
				// level that the game offers at this time. The currentLevel will also stay as the last level as well.
				levelCompletionInfo[level][0] = 1;

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


		// Finally, save the new values to our preferences file
		this.storeSaveState();
	}

	public void storeSaveState() {

		// Create a hashtable to store our level (and level completion) data.
		Hashtable<String, String> hashTable = new Hashtable<String, String>();

		Json json = new Json();

		// Here we are serializing our array
		hashTable.put("levelcompletion", json.toJson(levelCompletionInfo));
		hashTable.put("gameoptions", json.toJson(gameOptions));
		hashTable.put("highestlevelcompleted", json.toJson(highestLevelCompleted));

		// Store it in the preferences file
		ini.put(hashTable);
		ini.flush();
	}

	public void loadSaveState() {

		Json json = new Json();

		// Level Completion Information
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

		// Game Options
		String gameOptionsValues = ini.getString("gameoptions");

		if(!gameOptionsValues.isEmpty()) {
			// If we found data, let's load it up
			gameOptions = json.fromJson(GameOptions.class, gameOptionsValues);

			// Do something with all our options
			if(getOptions().playSounds()) {
				if(this.playSound == false) {
					toggleSound();
				}
			}
		} // If there is no values, then we simply keep the defaults provided by the class constructor

		String highestLevelCompletedValue = ini.getString("highestLevelCompleted");

		if(!highestLevelCompletedValue.isEmpty()) {
			highestLevelCompleted = json.fromJson(int.class, highestLevelCompletedValue);
		} else {
			// Default to current level, whatever that may be
			highestLevelCompleted = currentLevel;
		}
	}

	public AssetLoader getAssetLoader() {
		return this.assets;
	}

	public boolean playerHasBeatenHighestLevel() {
		if(highestLevelCompleted == levelCompletionInfo.length-1) {
			return true;
		} else {return false;}
	}

	public void create() {

		gameOptions = new GameOptions();

		levelMessage = new ArrayList<String>();
		levelMessage.add("Remember: Red planets can only be moved to a tile that is diagonal to another red planet."); 
		levelMessage.add("");
		levelMessage.add("Fantastic! Now it's time to learn why you're here: In 500 years, the universe will collapse.");
		levelMessage.add("An ultrastar will explode, causing huge gravity tidal waves. Thankfully, there's a way to prevent it."); 
		levelMessage.add("Scientists from the future travelled back in time to bring us the Gravity Grid device.");
		levelMessage.add("The Gravity Grid device seems like a simple smartphone app, but it's actually a wormhole manipulator.");
		levelMessage.add("Your mission is to realign all the planets in each system's Gravity Grid. Do this, and you will save the universe!");
		levelMessage.add("Remember: Blue planets can only be moved to the top, bottom, left, or right of other blue planets."); 
		levelMessage.add("Great! Just like red planets, blue planets might start in violation of their rules. Only when moving must the rules be followed."); 
		levelMessage.add("Who knows how a planetary system will present itself. It's up to you to come up with the smartest solution."); 
		levelMessage.add(""); 
		levelMessage.add("Sometimes you need to move planets of one color out of the way to make room for planets of a different color.");
		levelMessage.add("Sometimes it seems like you need to make room for different color planets, but there might also be a different solution.");
		levelMessage.add("Here, try to get the red AND blue scores aligned in only three moves. Can you do it?");
		levelMessage.add("Remember that everything in our universe is connected. Could there be multiple ways to solve this problem?");
		levelMessage.add("Each galaxy consists of 25 planetary systems (or levels). When you beat the 25th level of a galaxy, you unlock the next galaxy!");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add(""); // Level 20
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		// Galaxy 2
		levelMessage.add("Woohoo! You've unlocked the second galaxy, which has ASTEROIDS. Asteroids mean you can't use that tile.");
		levelMessage.add("Asteroids cannot be moved, and no planet can be moved onto a tile that has an asteroid.");
		levelMessage.add(""); //
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add(""); // Level 50
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add(""); // Level 75
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("Uranus' blue glow is due to the methane in its atmosphere, which filters out all the red light.");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");
		levelMessage.add("");

		highestLevelCompleted = 0; // init

		// Instantiate the tutorial overlays
		for(int i=0; i<100; i++) {
			levelsWithTutorialOverlays[i] = 0;
		}

		// Manually set the levels with tutorial overlays
		levelsWithTutorialOverlays[0] = 1;	// level1TutorialOverlay.png on level 1
		levelsWithTutorialOverlays[1] = 1;	// level2TutorialOverlay.png on level 2
		levelsWithTutorialOverlays[6] = 1;  // level7TutorialOverlay.png on level 7



		fingerOnScreen = false;

		// Prepopulate our screen geometry
		this.screenWidth = Gdx.graphics.getWidth();
		this.screenHeight = Gdx.graphics.getHeight();

		// Load our internal saved files from previous game plays
		ini = Gdx.app.getPreferences("license"); // Haha we named our preferences file "license" 

		// Load the saveState OR populate default values if this is a new install
		this.loadSaveState();

		batch = new SpriteBatch(); // Initialize our spritebatch used to draw everything in the world

		assets = new AssetLoader(this, new AssetManager());

		// Head to our InitialLoading screen, which loads all our assets
		this.setScreen(new InitialLoadingScreen(this));
	}

	public void render() {
		super.render(); // important!
	}

	public void resume() {

	}

	public void pause() {
		// In the app lifecycle, this function is called both when the app loses focus AND right before the application closes, so it makes sense to store our save state here. 

		this.storeSaveState();

	}

	public void dispose() {
		batch.dispose();
		//regularFont.dispose();
		this.assets.dispose();
		Gdx.app.exit();
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
