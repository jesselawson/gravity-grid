/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import java.util.concurrent.TimeUnit;

public class GravityGrid extends Game {
	
	Preferences ini;
	
	// Timer variables. We implement the timer in this main Game class because we want it to be 
	// independent of the level. Also, we're using TimeUtils instead of the other timer because
	// the timer should be independent of the actual game (e.g., we need to have 20 real world minutes 
	// pass before another Dark Matter regenerates
	public long timerStartTime; // Timestamp of when we started this game session
	public long timerElapsedTime; // Difference between current timestamp and timerStartTime
								 // We save this value
	
	public int darkMatterCount; // This is the "lives" that we have. The max is 5. 
	
	public int currentLevel;
	
	// This is 20 minutes
	// TRANSITION PLAN BEFORE WE get rid of dark matter entirely: set timer to 1 second
	public static final long darkMatterCooldown = 1000000L * 1000; //* 60 * 20;
	
	// Explicitly set our colors for consistency
	Color colorDarkBlue = new Color(.38f,.57f,.80f,0.75f);
	Color colorLightBlue = new Color(.46f,.77f,.98f, .35f);
	Color colorYellow = new Color(.99f,.90f,.29f,1f);
	Color colorRed = new Color(.79f,.01f,.25f,1f);
	Color colorGreen = new Color(.60f,.77f,.23f,1f);
	Color colorBlue = new Color(.37f,.78f,.93f,1f);
	
	SpriteBatch batch;
	BitmapFont font;
	
	
	// This array holds the values of each tile
	public static final int[] tileValueTable = new int[] { 0,1,3,5,3,1,0,1,2,4,6,4,2,1,3,4,6,8,6,4,3,5,6,8,10,8,6,5,3,4,6,8,6,4,3,1,2,4,6,4,2,1,0,1,3,5,3,1,0 };
	
	// levelNames to make our levels prettier
	// For more: https://en.wikipedia.org/wiki/List_of_most_massive_black_holes
	
	public static final String[] levelName = new String[] {
		"Saggitarius A",
		"Messier 32",
		"Messier 61", 
		"Markarian 335", 
		"M60-UCD1", 
		"Messier 82", 
		"NGC 4151",
		"Centaurus A",
		"Messier 81",
		"RX J12x-11x",
		"Andromeda Galaxy",
		"Messier 59",
		"NGC 1275",
		"NGC 4261",
		"3C 273",
		"Markarian 501",
		"Sombrero Galaxy",
		"Cygnus A",
		"NGC 6166",
		"Q0906+6930",
		"ULAS J1120",
		"Hercules A",
		"NGC 1277",
		"Messier 87",
		"NGC 3842",
		"Holmberg 15A",
		"Phoenix cluster",
		"S5 0014",//27
		"Messier A3"

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
		RED SCORE NEEDED, BLUE SCORE NEEDED, GREEN SCORE NEEDED, MAX MOVES
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
			
	/* Level messages. Displayed at the bottom of each grid for its associated level */ 
	public static final String[] levelMessage = new String[] {
		
		"Red planets can only be moved diagonally to other red planets.", // 1
		"Planets can start out not touching other planets...", //2
		"... but if you do move a planet, you must follow the placement rules for that planet color.", //3
		"Blue planets can only be moved next to other blue planets, on any side except diagonally.", //4
		"Planets that start out in violation of their rules can finish that way, too, as long as you don't move them.",//5
		"",//6
		"Anytime you move a planet you must follow the movement rules for that planet color.",//7
		"",//8
		"Sometimes you need to move planets of one color out of the way to make room for planets of a different color.",//9
		"Asteroids mean you can't use that tile. You'll have to work around it.",//10
		"",//11
		"",//12
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
		"",//24
		};
	
	public static final int[][] gravityGridLevel = new int[][] {
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
		
	};
	
	// GetLevel(#) is used to preload the PlayingScreen main playing array. 
	public static int[] GetLevel(int levelNum) {
		return gravityGridLevel[levelNum];
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
	
	// 
	public void markLevelAsComplete() {		
		// Increment our currentLevel counter so we know the next one to load
		this.currentLevel++;
		this.storeSaveState(); 
	} 
	
	public void storeSaveState() {
		
		ini.putInteger("a", this.currentLevel); 
		ini.putInteger("b", this.darkMatterCount);
		ini.putLong("c", this.timerStartTime);
		
		ini.flush(); 
		
	}
	
	public void loadSaveState() {
		this.currentLevel = ini.getInteger("a", 0); 
		this.darkMatterCount = ini.getInteger("b", 5); // default: 5 lives
		this.timerStartTime = ini.getLong("c", 0); // default: 0 seconds elapsed since last 
		long rightNow = TimeUtils.nanoTime(); 
		this.timerElapsedTime = rightNow - timerStartTime;  // Update our elapsed timer using the timestamp that was saved
		
		// IMPORTANT: When the game starts up and this function is called, we need to make sure
		// that we check elapsed time from our system timestamp (that we also loaded from a file).
		// From here, we determine how many increments of 20 minutes have passed, and then add that
		// many dark matter back until we reach the full amount
		
			// We saved on a less-than-full lives amount, so let's see how much time has passed
			// since we closed the game (and subsequently the storeSaveState was called) 
			
			long makeupTime = this.timerElapsedTime;
			while(makeupTime > 0) {
				// Can we subtract 20 minutes? 
				if(makeupTime - this.darkMatterCooldown > 0){
					// Do we have less than 5 lives? 
					if(this.darkMatterCount < 5) {
						// Subtract 20 minutes
						makeupTime -= this.darkMatterCooldown;
						// Increment our lives
						this.darkMatterCount++;
					} else {
						// If we have the max lives, zero out everything else
						this.timerElapsedTime = 0L;
						makeupTime = 0L;
						break;
					}
				} else {
					// Now we have less than 20 minutes, so let's make that our new elapsed time
					this.timerElapsedTime = makeupTime;
					break; // We're done looping
					
					
				}
			}

	}
	
	public void create() {
		
		// Initialize the timers and the dark matter counter
		
		ini = Gdx.app.getPreferences("license"); // Haha we named our preferences file "license" 
		
		// Load the saveState OR populate default values
		this.loadSaveState(); 
		
		batch = new SpriteBatch();
		font = new BitmapFont();
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("turkey.ttf"));
		FreeTypeFontParameter parameter = new FreeTypeFontParameter();
		parameter.size = 22; 
		font = generator.generateFont(parameter);
		generator.dispose(); 
		
		
		this.setScreen(new MainMenuScreen(this));
	}

	public void render() {
		// Update the global timer. Since we have loaded the previous starttime and elapsedtime,
		// we need to remember to reset both of them when we have reached 5 lives (dark matter). 
		
		// Check how many lives we have
		if(this.darkMatterCount < 5) {
			
			//Has it been 20 minutes? 
			if(this.timerElapsedTime > this.darkMatterCooldown) {
				// If so, reset both timer variables
				this.timerElapsedTime = 0L; 
				this.timerStartTime = TimeUtils.nanoTime(); // Get a new timestamp
				this.darkMatterCount++; // Increment our dark matter
			} else {
				// It hasn't been 20 minutes, so update our elapsed timer
							
				// Get a timestamp
				long rightNow = TimeUtils.nanoTime(); 
				this.timerElapsedTime = rightNow - this.timerStartTime;
			}
		} else {
			// Our dark matter is full, so we don't need to do anything except for update our 
			// start time, which will always ensure we are starting our 20 minute counter from 
			// the time we lost a life. 
			this.timerStartTime = TimeUtils.nanoTime(); 
		}
		
		super.render(); // important!
	}
	
	public void pause() {
		// In the app lifecycle, this function is called both when the app loses focus AND right before the application closes, so it makes sense to store our save state here. 
		
		this.storeSaveState(); 
	}

	public void dispose() {
		batch.dispose();
		font.dispose();
		
	}
}
/* BETA RELEASE

	* More levels!

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
