/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;


public class PlayingScreen implements Screen {

    // A custom point-in-rectangle collision checker
    public static boolean pointInRectangle (Rectangle r, float x, float y) {
        return r.x <= x && r.x + r.width >= x && r.y <= y && r.y + r.height >= y;
    }

    public boolean fingerOnScreen; // Are we getting input?
    public boolean readyForInput; // Can we handle input?

    private int screenWidth = Gdx.graphics.getWidth();
    private int screenHeight = Gdx.graphics.getHeight();

    Rectangle inGameMenuResetButtonRect;
    Rectangle inGameMenuLevelSelectButtonRect;
    Rectangle inGameMenuHelpButtonRect;
    Rectangle inGameMenuPrevHelpScreenRect; // turn the page
    Rectangle inGameMenuNextHelpScreenRect; // turn back a page
    private int inGameMenuHelpScreenNum;    // Our current help screen
    private boolean helpScreenMaximized; // If you click on one of the help screens, it fills the screen?
    // Help screens:
    // When state = IN_GAME_MENU, player can



    // On create, these are loaded according to the currentLevel that is managed in the GravityGrid class
    public int thisLevelRedNeeded;
    public int thisLevelBlueNeeded;
    public int thisLevelGreenNeeded;
    public int thisLevelMaxMoves;
    public int thisLevelCurrentRedTotal;
    public int thisLevelCurrentBlueTotal;
    public int thisLevelCurrentGreenTotal;
    public int thisLevelCurrentMoves;
    public int thisLevelCurrentAttempts; // Increased on FAILURE and RESET
    public int thisLevelBackgroundImageNumber; // Computed in the constructor: (int)currentLevel / 10. So lvl 35 would be bg03.png, and lvl 90 would be (you guessed it) bg09.png

    public ParticleEffect levelCompleteFireworks;
    public ParticleEffect backgroundStarfieldParticles;
    public ParticleEffect goodMoveStarburst;
    public ParticleEffectPool goodMoveStarburstPool;
    public ParticleEffect badMoveStarburst;
    public ParticleEffectPool badMoveStarburstPool;

    public boolean inGameMenuActive;
    public Texture inGameMenuButtonImage;
    public Texture inGameMenuBackgroundImage;
    public Texture inGameMenuCancelButtonImage;
    public Texture inGameMenuResetButtonImage;
    public Texture inGameMenuLevelSelectButtonImage;
    public Texture inGameMenuHelpButtonImage;

    // An array of pooled effects to manage all our particle effect systems
    Array<ParticleEffectPool.PooledEffect> particleEffects = new Array();

    public enum gameState {
        READY, 						// Process tile selections
        TILE_SELECTED, 	// Process movements from selections
        // Check new touched tile for empty. If yes, then go find the tile that is marked as
        // 	selected, find out its type, and then copy it to this new one.
        // Update the animations.
        GOOD_MOVE_ATTEMPT,			// Player selected a blank tile to move to, all that's left is to ensure rules are followed
        OUT_OF_MOVES, 				// Player has ran out of moves, so the only thing they can do is reset the level
        OUT_OF_LIVES,				// Player has 0 dark matter
        LEVEL_COMPLETE,             // Displays the "level complete button" that jumps you to the score screen
        IN_GAME_MENU               // Open the menu when you're playing a level. Will give you "BACK TO LEVEL SELECT", "HOW TO PLAY", "ABOUT", and "QUIT"
    }



    // There are three types of tiles. You can be nothing (i.e., blank; you can be a planet; you can be a sun. A sun is a special tile that cannot have any other tiles touching it.
    // BLUEPLANETs are placed side by side or up and down to each other
    // REDPLANETs are placed diagonally
    // GREENPLANETs go anywhere
    // ASTEROIDs cannot have anything side by side or up and down (you can move planets diagonally)
    // SUNs cannot have anything in any square touching them (even diagonally)
    public enum TileType {
        NONE, REDPLANET, BLUEPLANET, GREENPLANET, ASTEROID, SUN, BLOCKED
    }

    public enum TileStatus {
        NONE, // Tile is doing nothing
        SELECTED, // yellow spinner
        MOVETOHERE,
        MOVECOMPLETE, //displaying the spinner animation
        CANNOTMOVE
    }

    public class Tile {
        public TileType type;
        public TileStatus status;
        public Rectangle rect;
        public int tileNum; // This is a tile number we assign incrementally so that we can quickly
        // determine whether a tile can be moved to or not. E.g., if MOVETOHERE is trying to be
        // tileNum=4, we check that canMove==true where tileNum=4 (so it checks surrounding tiles for
        // different rule types
        public int value; // Set when the level is generated

        public int rand; // This is used to hold a random number for the planet#.png files.

        public int overlayFrameNumber;
        public float timeSinceLastFrame;
        public float overlayRotation;

        public Tile(Rectangle rect, int value, TileType type, int tileNum) {
            this.rect = new Rectangle();
            this.type = type;
            this.value = value; //default
            this.rect = rect;
            this.status = TileStatus.NONE;
            this.timeSinceLastFrame = 0.0f;
            this.overlayFrameNumber = 0;
            this.overlayRotation = 0.0f;
            this.tileNum = tileNum;

            this.rand = MathUtils.random(0,3);

        }
    }

    // Run this at the end of every render iteration to update the scores
    public void updateCurrentLevelValueTotals() {

        // Flush the current scores
        thisLevelCurrentBlueTotal = 0;
        thisLevelCurrentRedTotal = 0;
        thisLevelCurrentGreenTotal = 0;

        // Iterate through each tile and update the current color values
        for(Tile tile : this.tile) {
            switch(tile.type) {
                case REDPLANET:
                    thisLevelCurrentRedTotal += tile.value;
                    break;
                case BLUEPLANET:
                    thisLevelCurrentBlueTotal += tile.value;
                    break;
                case GREENPLANET:
                    thisLevelCurrentGreenTotal += tile.value;
                    break;
                default: break;
            }
        }

    }

    // GoodMoveVerified is only used so that I don't have to continually write all these checks for the destination
    // tile type being equal to the selectedType and not being the selectedNum
    public boolean GoodMoveVerified(int tileToCheck, TileType selectedType, int selectedNum) {
        if( this.tile.get(tileToCheck).type == selectedType && this.tile.get(tileToCheck).tileNum != selectedNum ) {
            System.out.println("Good tile "+tileToCheck+" found for selected num "+selectedNum);
            return true;
        } else {
            return false;
        }
    }

    // CanMove function must be ran during gameState.TILE_SELECTED, because it iterates through the tiles and
    // finds the selected one, determines its TileType, and then checks the tiles around the tileNum argument
    // to see if the player can move the selected planet type there.
    // As of August 2015, the rules are that each planet type must move to a tile according to its rules that touches
    // another planet of the same type. Greens can be touching any way, blues only top,bottom,or size, and red only
    // diagonally.
    public boolean canMoveAccordingToRules(int destinationTileNum) {

        // First determine the select tile's tileNum so we can get it's TileType
        TileType selectedType = TileType.NONE; // init'd
        int selectedNum = 0; // init'd

        // This `outcome` is what is returned by this function. If outcome = true, then the function has determined
        // that the tile in question "can move according to rules"
        boolean outcome = false;

        // TODO: First thing this should check is whether destinationTileNum is an illegal space (asteroid, sun, etc). If it is, we can return false right here and skip all the logic.


        // Loop through tiles and find the selected one
        findSelected:
        for (Tile tile : this.tile) {

            // Check for selected tile
            if (tile.status == TileStatus.SELECTED) {
                // Extract the type of the selected tile so we can assign the appropriate rules to the planet type
                selectedType = tile.type;
                selectedNum = tile.tileNum;
                break findSelected;
            }

        }


        // Now we're in the meat and potatoes. The function seeks to answer: can a tile of type selectedType move to
        // the destinationTileNum type?

        // We'll have to switch the selected type so we can apply the appropriate rules.
        // (13-Aug-2015 Jesse) I'm running into a problem where the rules are checking against the selected tile,
        // because technically that is a tile that is next to the next tile. In other words, I can click a blue
        // planet along a straight path away from other planets because the tile I am moving to is next to a blue
        // planet tile -- the one I am on! We need a way to omit the selected tile to ensure that we're only counting OTHER tiles of the same type
        // (13-Aug-2015 Jesse) I discovered that if I added '&& tile.tileNum != selectedNum' to the part where I check whether the tile's type of the adjacent one is the same type of planet as the selected tile, I can ensure that I don't count any adjacent tiles of the same type that are in fact the selected tile (and thus only account for non-selected adjacent tiles)

        switch (selectedType) {

            // Use the following grid of tileNum values for reference:
            //
            // 42 43 44 45 46 47 48
            // 35 36 37 38 39 40 41
            // 28 29 30 31 32 33 34
            // 21 22 23 24 25 26 27
            // 14 15 16 17 18 19 20
            // 07 08 09 10 11 12 13
            // 00 01 02 03 04 05 06

            case REDPLANET:
                // First check the body of the board. If the body doesn't return true, try the edges and corners (the most complicated of the logic because we check them each individually).
                if (this.game.IntArrayContains(this.game.boardMiddle, destinationTileNum)) {
                    // Check the middle section of the board (i.e., all tiles except edges and corners)
                    if (
                            (GoodMoveVerified(destinationTileNum + 8, selectedType, selectedNum))
                            || (GoodMoveVerified(destinationTileNum + 6, selectedType, selectedNum))
                            || (GoodMoveVerified(destinationTileNum - 6, selectedType, selectedNum))
                            || (GoodMoveVerified(destinationTileNum - 8, selectedType, selectedNum))) {
                        outcome = true;
                        //System.out.println("destinationTileNum: " + destinationTileNum + ". SelectedNum: " + selectedNum);
                        return outcome; // Leave this function immediately!
                    }

                } else {
                    // If we're here, then we're on an edge or corner tile. We need to check these manually... Previous attempts at complicated if-else statements have proven unwieldy and confusing.
                    // So why not this switch in a switch?

                    switch (destinationTileNum) {
                        case 42: outcome = GoodMoveVerified(36,selectedType,selectedNum); break;
                        case 43:
                        case 44:
                        case 45:
                        case 46:
                        case 47: outcome = (GoodMoveVerified(destinationTileNum-6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-8,selectedType,selectedNum)); break;
                        case 48: outcome = GoodMoveVerified(40,selectedType,selectedNum); break;
                        case 41:
                        case 34:
                        case 27:
                        case 20:
                        case 13: outcome = (GoodMoveVerified(destinationTileNum+6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-8,selectedType,selectedNum)); break;
                        case  6: outcome = GoodMoveVerified(12,selectedType,selectedNum); break;
                        case  5:
                        case  4:
                        case  3:
                        case  2:
                        case  1: outcome = (GoodMoveVerified(destinationTileNum+6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+8,selectedType,selectedNum)); break;
                        case  0: outcome = GoodMoveVerified(8,selectedType,selectedNum); break;
                        case  7:
                        case 14:
                        case 21:
                        case 28:
                        case 35: outcome = (GoodMoveVerified(destinationTileNum+8,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-6,selectedType,selectedNum)); break;
                        default: break;
                    }

                    return outcome; // Break immediately because there's no point in continuing
                }

                    break;
                case BLUEPLANET:
                    // First check the body of the board. If the body doesn't return true, try the edges and corners (the most complicated of the logic because we check them each individually).
                    if (this.game.IntArrayContains(this.game.boardMiddle, destinationTileNum)) {
                        // Check the middle section of the board (i.e., all tiles except edges and corners)
                        if (
                            (GoodMoveVerified(destinationTileNum + 1, selectedType, selectedNum))
                            || (GoodMoveVerified(destinationTileNum - 1, selectedType, selectedNum))
                            || (GoodMoveVerified(destinationTileNum + 7, selectedType, selectedNum))
                            || (GoodMoveVerified(destinationTileNum - 7, selectedType, selectedNum))) {
                                outcome = true;
                            //System.out.println("destinationTileNum: " + destinationTileNum + ". SelectedNum: " + selectedNum);
                            return outcome; // Leave this function immediately!
                        }

                    } else {
                        // If we're here, then we're on an edge or corner tile. We need to check these manually... Previous attempts at complicated if-else statements have proven unwieldy and confusing.
                        // So why not this switch in a switch?

                        switch (destinationTileNum) {
                            case 42: outcome = ( GoodMoveVerified(35,selectedType,selectedNum) || GoodMoveVerified(43,selectedType,selectedNum) ); break;
                            case 43:
                            case 44:
                            case 45:
                            case 46:
                            case 47: outcome = (GoodMoveVerified(destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-7,selectedType,selectedNum)); break;
                            case 48: outcome = ( GoodMoveVerified(47,selectedType,selectedNum) || GoodMoveVerified(41,selectedType,selectedNum) ); break;
                            case 41:
                            case 34:
                            case 27:
                            case 20:
                            case 13: outcome = (GoodMoveVerified(destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-7,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+7,selectedType,selectedNum)); break;
                            case  6: outcome = ( GoodMoveVerified(5,selectedType,selectedNum) || GoodMoveVerified(13,selectedType,selectedNum) ); break;
                            case  5:
                            case  4:
                            case  3:
                            case  2:
                            case  1: outcome = (GoodMoveVerified(destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+7,selectedType,selectedNum)); break;
                            case  0: outcome = ( GoodMoveVerified(7,selectedType,selectedNum) || GoodMoveVerified(1,selectedType,selectedNum) ); break;
                            case  7:
                            case 14:
                            case 21:
                            case 28:
                            case 35: outcome = (GoodMoveVerified(destinationTileNum-7,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+7,selectedType,selectedNum)); break;
                            default: break;
                        }

                        return outcome; // Break immediately because there's no point in continuing
                    }

                    break;

            case GREENPLANET:
                // First check the body of the board. If the body doesn't return true, try the edges and corners (the most complicated of the logic because we check them each individually).
                if (this.game.IntArrayContains(this.game.boardMiddle, destinationTileNum)) {
                    // Check the middle section of the board (i.e., all tiles except edges and corners)
                    if (    // For green planets, it's left, right, and all three on top and three on bottom
                            GoodMoveVerified(destinationTileNum - 1, selectedType, selectedNum)
                            || GoodMoveVerified(destinationTileNum - 8, selectedType, selectedNum)
                            || GoodMoveVerified(destinationTileNum - 7, selectedType, selectedNum)
                            || GoodMoveVerified(destinationTileNum - 6, selectedType, selectedNum)
                            || GoodMoveVerified(destinationTileNum + 1, selectedType, selectedNum)
                            || GoodMoveVerified(destinationTileNum + 6, selectedType, selectedNum)
                            || GoodMoveVerified(destinationTileNum + 7, selectedType, selectedNum)
                            || GoodMoveVerified(destinationTileNum + 8, selectedType, selectedNum)
                            ) {
                        outcome = true;
                        //System.out.println("destinationTileNum: " + destinationTileNum + ". SelectedNum: " + selectedNum);
                        return outcome; // Leave this function immediately!
                    }

                } else {
                    // If we're here, then we're on an edge or corner tile. We need to check these manually... Previous attempts at complicated if-else statements have proven unwieldy and confusing.
                    // So why not this switch in a switch?

                    switch (destinationTileNum) {
                        case 42: outcome = ( GoodMoveVerified(35,selectedType,selectedNum) || GoodMoveVerified(43,selectedType,selectedNum) || GoodMoveVerified(36,selectedType,selectedNum) ); break;
                        case 43:
                        case 44:
                        case 45:
                        case 46:
                        case 47: outcome = (GoodMoveVerified(destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-7,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-8,selectedType,selectedNum)); break;
                        case 48: outcome = ( GoodMoveVerified(47,selectedType,selectedNum) || GoodMoveVerified(40,selectedType,selectedNum) || GoodMoveVerified(41,selectedType,selectedNum)); break;
                        case 41:
                        case 34:
                        case 27:
                        case 20:
                        case 13: outcome = ( GoodMoveVerified(destinationTileNum+6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+7,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-7,selectedType,selectedNum)); break;
                        case  6: outcome = ( GoodMoveVerified(5,selectedType,selectedNum) || GoodMoveVerified(12,selectedType,selectedNum) || GoodMoveVerified(13,selectedType,selectedNum) ); break;
                        case  5:
                        case  4:
                        case  3:
                        case  2:
                        case  1: outcome = ( GoodMoveVerified(destinationTileNum+6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+7,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+8,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+1,selectedType,selectedNum)); break;
                        case  0: outcome = ( GoodMoveVerified(7,selectedType,selectedNum) || GoodMoveVerified(8,selectedType,selectedNum) || GoodMoveVerified(1,selectedType,selectedNum) ); break;
                        case  7:
                        case 14:
                        case 21:
                        case 28:
                        case 35: outcome = ( GoodMoveVerified(destinationTileNum+6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+7,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-6,selectedType,selectedNum) || GoodMoveVerified(destinationTileNum-7,selectedType,selectedNum)); break;
                        default: break;
                    }

                    return outcome; // Break immediately because there's no point in continuing
                }

                break;
                default:
                    break;
            }
        return outcome;
    }

    private ArrayList<Tile> tile; // Single array of tiles, instead of multidimensional

    private float backgroundStarfieldPosition; // used to keep track of the background spiral that goes over the background images


    private int tileWidth;
    private int tileHeight;
    private int whiteSpace;

    private int headSpace; // This accounts for the header column numbers
    private int leftSpace; // This accounts for the left column numbers



    // All the textures we use
    private Texture tileBlankImage;
    private Texture tileRedPlanetImage;
    private Texture tileBluePlanetImage;
    private Texture tileGreenPlanetImage;

    private TextureRegion tileRedPlanetRegion;
    private TextureRegion tileBluePlanetRegion;
    private TextureRegion tileGreenPlanetRegion;

    Rectangle inGameMenuButtonRect;
    private boolean tryingToReset; // If this is true, user has already pushed the reset button once.
    private int holdToResetCounter;

    private Texture[] backgroundImage;
    Texture tileSunImage;
    Texture tileSunFlareImage;
    TextureRegion tileSunFlareRegion;
    TextureRegion tileSunRegion;
    Texture[] tileAsteroidImage;
    Texture[] tileValueImage;
    Texture[] tileOverlayImage;
    TextureRegion[] tileOverlayRegion;
    Texture blackHoleImage;
    TextureRegion blackHoleRegion;
    Texture buttonFailImage;
    Texture buttonLevelCompleteImage;
    Texture singularityImage;
    Texture backgroundStarfieldImage;
    TextureRegion backgroundStarfieldRegion;
    Texture tileSelectedTopImage;
    Texture tileSelectedBottomImage;
    TextureRegion tileSelectedTopRegion;
    TextureRegion tileSelectedBottomRegion;
    private int tileSelectedTopDirection;
    private int tileSelectedBottomDirection;
    Sound tileSelectSound;
    Sound tileDeselectSound;
    Sound goodMoveAttemptSound;
    Sound cannotMoveSound;
    Sound outOfMovesSound;
    Sound levelCompleteSound;
    Sound restartLevelSound;

    Rectangle finger;
    private GravityGrid game;

    OrthographicCamera camera;

    gameState theGameState;

    public PlayingScreen(GravityGrid game) {

        this.game = game;

        fingerOnScreen = false;
        readyForInput = true;

        holdToResetCounter = 0;

        // Initialize the required color values and the max moves, which are specific elements in the gravityGridLevel array
        thisLevelRedNeeded = game.gravityGridLevel[game.currentLevel][49];
        thisLevelBlueNeeded = game.gravityGridLevel[game.currentLevel][50];
        thisLevelGreenNeeded = game.gravityGridLevel[game.currentLevel][51];
        thisLevelMaxMoves = game.gravityGridLevel[game.currentLevel][52];
        thisLevelCurrentRedTotal = 0;
        thisLevelCurrentBlueTotal = 0;
        thisLevelCurrentGreenTotal = 0;
        thisLevelCurrentAttempts = 0;
        thisLevelCurrentMoves = 0; // Keep track of how many moves we've taken
        thisLevelBackgroundImageNumber = this.game.currentLevel / 10;

        // Init our levelComplete particle effects
        levelCompleteFireworks = new ParticleEffect();
        levelCompleteFireworks.load(Gdx.files.internal("particles/bigstarburst.p"), Gdx.files.internal("particles"));

        goodMoveStarburst = game.assets.get("particles/goodmovestarburst.p", ParticleEffect.class); // Template effect
        goodMoveStarburstPool = new ParticleEffectPool(goodMoveStarburst, 0, 50);                   // Pool for the template

        badMoveStarburst = game.assets.get("particles/badmovestarburst.p", ParticleEffect.class);   // Template effect
        badMoveStarburstPool = new ParticleEffectPool(badMoveStarburst, 0, 50);                     // Pool for the template

        backgroundStarfieldParticles = game.assets.get("particles/starfield.p", ParticleEffect.class);

        theGameState = gameState.READY;

        finger = new Rectangle();
        finger.x = -1;
        finger.y = -1;
        finger.width = 0;
        finger.height = 0;

        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.screenWidth, game.screenHeight);


        // The amount of space we want for the top of the grid where the numbers will be displayed.
        // This comes out to one half of the width (or height) of one tile.
        this.headSpace = 0;//(int)(0.5*(screenWidth/7));

        // The space we want on the left where the numbers will be displayed
        this.leftSpace = this.headSpace;

        // This constructor function will allow us to get screen width to determine our tile width (and subsequently, our tile height)
        this.tileWidth = (screenWidth - leftSpace) / 7; // So it's 7 segments of our screenwidth minus the space we've reserved for the tile labels.
        this.tileHeight = (screenWidth - headSpace) / 7;

        // The whitespace variable sets a modifier for the rect.x values of each tile so that the grid is in the center of the screen.
        this.whiteSpace = (int)(0.5*screenHeight) - (int)(0.5*(screenWidth/7)*7);


        this.tile = new ArrayList<Tile>(); // Initialize our grid

        // Set two counters to count our world rows and columns, which are different from the level ones.
        // These will ensure that we are building the map correctly.
        int worldRow = 0;
        int worldCol = 0;
        int tileNum = 0;

        // Load the Map
        // Loop through the tiles and assign rect values, load values from the values table, and
        // also load the tile type from the levels tables.
        for(int r = 6; r > -1; r--) {
            for(int c = 0; c < 7; c++) {

                // Create a placeholder for the tile type
                TileType rcType;

                // Determine the tile type
                switch(game.gravityGridLevel[game.currentLevel][(r*7)+c]) {
                    case 0:
                        rcType = TileType.NONE;
                        break;
                    case 1:
                        rcType = TileType.REDPLANET;
                        break;
                    case 2:
                        rcType = TileType.BLUEPLANET;
                        break;
                    case 3:
                        rcType = TileType.GREENPLANET;
                        break;
                    case 4:
                        rcType = TileType.ASTEROID;
                        break;
                    case 5:
                        rcType = TileType.SUN;
                        break;
                    case 9:
                        rcType = TileType.BLOCKED;
                        break;
                    default:
                        rcType = TileType.NONE;
                        break;
                }

                // Create a placeholder for the rect values
                Rectangle rect = new Rectangle();

                // Determine the rect values
                rect.x = leftSpace + worldCol*this.tileWidth;
                rect.y = whiteSpace - headSpace + worldRow * this.tileHeight;
                rect.width = this.tileWidth;
                rect.height = this.tileHeight;

                // Here we push the new tile to the array. Since we are assigning rect values per tile, it doesn't
                // matter (theoretically) how we access these tiles or if they're out of order.
                tile.add(new Tile(rect, game.tileValueTable[(r*7)+c], rcType, tileNum));

                worldCol++;
                tileNum++;

                // (4-Nov-2016 Jesse) Add a new particle system on each and every tile so that when the level starts, there's a huge burst of stars
                // (19-Nov-2016 Jesse) Only do this on tiles with planets, otherwise there's lots of lag
                if(rcType == TileType.REDPLANET || rcType == TileType.BLUEPLANET || rcType == TileType.GREENPLANET) {
                    ParticleEffectPool.PooledEffect levelStartEffect = goodMoveStarburstPool.obtain();
                    levelStartEffect.setPosition(rect.x + (rect.width / 2), rect.y + (rect.height / 2));
                    particleEffects.add(levelStartEffect);
                }
            }
            worldCol = 0; // Reset column counter
            worldRow++; // Iterate our row counter
        }

        // TODO: lOAD ALL OF THESE FROM THE ASSET MANAGER
        // Load the sounds before the textures so the assetmanager isn't busy by the time we're clicking things
        //restartLevelSound = game.assets.get("startup.wav"));
        tileSelectSound = game.assets.get("consoleBeep.wav", Sound.class);
        tileDeselectSound = game.assets.get("tileDeselectSound.wav",Sound.class);
        goodMoveAttemptSound = game.assets.get("goodMoveAttempt.ogg",Sound.class);
        restartLevelSound = goodMoveAttemptSound;
        cannotMoveSound = game.assets.get("cannotMoveSound.wav",Sound.class);
        outOfMovesSound = game.assets.get("outOfMovesSound.wav",Sound.class);
        levelCompleteSound = game.assets.get("levelCompleteSound.wav",Sound.class);

       /* 0 = blank
        1 = red
        2 = blue
        3 = green
        4 = asteroid
        5 = sun
        9 = blocked*/

        // Load the textures
        tileBlankImage = game.assets.get("tileBlankImage.png", Texture.class);
        //tilePlanetImage = new Texture[4]; // remember: [4] = [0,1,2,3].
        //tilePlanetRegion = new TextureRegion[4];
        tileRedPlanetImage = new Texture(Gdx.files.internal("planet-red.png"), true);
        tileRedPlanetImage.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
        tileRedPlanetRegion = new TextureRegion(tileRedPlanetImage);
        tileBluePlanetImage = new Texture(Gdx.files.internal("planet-blue.png"), true);
        tileBluePlanetImage.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
        tileBluePlanetRegion = new TextureRegion(tileBluePlanetImage);
        tileGreenPlanetImage = new Texture(Gdx.files.internal("planet-green.png"), true);
        tileGreenPlanetImage.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
        tileGreenPlanetRegion = new TextureRegion(tileGreenPlanetImage);
        tileSunImage = game.assets.get("sun.png", Texture.class);
        tileSunRegion = new TextureRegion(tileSunImage);
        tileSunFlareImage = game.assets.get("sunflare0.png", Texture.class);
        tileSunFlareRegion = new TextureRegion(tileSunFlareImage);
        tileAsteroidImage = new Texture[4];
        tileAsteroidImage[0] = game.assets.get("asteroid0.png", Texture.class);
        tileAsteroidImage[1] = game.assets.get("asteroid1.png", Texture.class);
        tileAsteroidImage[2] = game.assets.get("asteroid2.png", Texture.class);
        tileAsteroidImage[3] = game.assets.get("asteroid3.png", Texture.class);
        backgroundImage = new Texture[4];

        backgroundImage[0] = game.assets.get("bg0.jpg", Texture.class); // Levels 0-
        backgroundImage[1] = game.assets.get("bg1.jpg", Texture.class);
        backgroundImage[2] = game.assets.get("bg2.png", Texture.class);
        backgroundImage[3] = game.assets.get("bg3.png", Texture.class);


        //levelMessageBackgroundImage = game.assets.get("levelMessageBackground.png", Texture.class);

        tileValueImage = new Texture[11];
        tileValueImage[0] = game.assets.get("tile0.png", Texture.class);
        tileValueImage[1] = game.assets.get("tile1.png", Texture.class);
        tileValueImage[2] = game.assets.get("tile2.png", Texture.class);
        tileValueImage[3] = game.assets.get("tile3.png", Texture.class);
        tileValueImage[4] = game.assets.get("tile4.png", Texture.class);
        tileValueImage[5] = game.assets.get("tile5.png", Texture.class);
        tileValueImage[6] = game.assets.get("tile6.png", Texture.class);
        tileValueImage[7] = null; // We don't actually use this value
        tileValueImage[8] = game.assets.get("tile8.png", Texture.class);
        tileValueImage[9] = null; // We don't actually use this value
        tileValueImage[10] = game.assets.get("tile10.png", Texture.class);
        tileOverlayImage = new Texture[7];
        tileOverlayRegion = new TextureRegion[7];
        tileOverlayImage[0] = game.assets.get("tileOverlayAnim0.png", Texture.class);
        tileOverlayRegion[0] = new TextureRegion(tileOverlayImage[0]);
        tileOverlayImage[1] = game.assets.get("tileOverlayAnim1.png", Texture.class);
        tileOverlayRegion[1] = new TextureRegion(tileOverlayImage[1]);
        tileOverlayImage[2] = game.assets.get("tileOverlayAnim2.png", Texture.class);
        tileOverlayRegion[2] = new TextureRegion(tileOverlayImage[2]);
        tileOverlayImage[3] = game.assets.get("tileOverlayAnim3.png", Texture.class);
        tileOverlayRegion[3] = new TextureRegion(tileOverlayImage[3]);
        tileOverlayImage[4] = game.assets.get("tileOverlayAnim4.png", Texture.class);
        tileOverlayRegion[4] = new TextureRegion(tileOverlayImage[4]);
        tileOverlayImage[5] = game.assets.get("tileOverlayAnim5.png", Texture.class);
        tileOverlayRegion[5] = new TextureRegion(tileOverlayImage[5]);
        tileOverlayImage[6] = game.assets.get("tileOverlayAnim6.png", Texture.class);
        tileOverlayRegion[6] = new TextureRegion(tileOverlayImage[6]);
        buttonFailImage = game.assets.get("buttonFail.png", Texture.class);
        buttonLevelCompleteImage = game.assets.get("buttonLevelComplete.png", Texture.class);

        tileSelectedBottomImage =  this.game.assets.get("tileSelected.png", Texture.class);
        tileSelectedTopImage = this.game.assets.get("tileSelected2.png", Texture.class);
        tileSelectedTopRegion = new TextureRegion(tileSelectedTopImage);
        tileSelectedBottomRegion = new TextureRegion(tileSelectedBottomImage);


        tryingToReset = false;
        inGameMenuButtonRect = new Rectangle((screenWidth/7)*6.0f, screenHeight-(screenWidth/7.0f), screenWidth/7.0f, screenWidth/7.0f); // Draw one tile big in upper-right corner

        blackHoleImage = game.assets.get("galaxyOverlay.png", Texture.class);
        blackHoleRegion = new TextureRegion(blackHoleImage);

        inGameMenuActive = false;
        inGameMenuBackgroundImage = game.assets.get("menu/blackBackground.png", Texture.class);
        inGameMenuCancelButtonImage = game.assets.get("menu/cancelButton.png", Texture.class);
        inGameMenuResetButtonImage = game.assets.get("menu/resetButton.png", Texture.class);
        inGameMenuLevelSelectButtonImage = game.assets.get("menu/levelSelectButton.png", Texture.class);
        inGameMenuHelpButtonImage = game.assets.get("menu/helpButton.png", Texture.class);
        inGameMenuButtonImage = game.assets.get("menu/menuButton.png", Texture.class);

        inGameMenuResetButtonRect = new Rectangle(0, this.screenHeight/2.0f, this.screenWidth/3.0f, this.screenWidth/3.0f);
        inGameMenuLevelSelectButtonRect = new Rectangle(this.screenWidth/3.0f, this.screenHeight/2.0f, this.screenWidth/3.0f, this.screenWidth/3.0f);
        inGameMenuHelpButtonRect = new Rectangle((this.screenWidth/3)*2, this.screenHeight/2.0f, this.screenWidth/3.0f, this.screenWidth/3.0f);



        // Call this once before the level starts so that we have some initial values

        updateCurrentLevelValueTotals();

    }

    public void RestartLevel() {

        // Reset all the particleEffects in our array that manages them all
        for (int i = particleEffects.size - 1; i >= 0; i--)
            particleEffects.get(i).free(); //free all the effects back to the pool
        particleEffects.clear(); //clear the current effects array

        // Set our defaults for our levelCompletionInfo
        thisLevelCurrentAttempts += 1; // It's set to zero when we beat the level and RestartLevel is called afterward
        thisLevelCurrentMoves = 0;

        // This also works as a level changer because we can increment the game.currentLevel (via UpdateLevelCompletionInfo) and then call RestartLevel() which reloads everything,
        // essentially loading up a new level. 

        // Let's reset the value totals needed just in case this is a new level. If we don't do this, the old values from the previous level seep through.
        thisLevelRedNeeded = game.gravityGridLevel[game.currentLevel][49];
        thisLevelBlueNeeded = game.gravityGridLevel[game.currentLevel][50];
        thisLevelGreenNeeded = game.gravityGridLevel[game.currentLevel][51];
        thisLevelMaxMoves = game.gravityGridLevel[game.currentLevel][52];

        // Reset the tiles
        int worldRow = 0;
        int worldCol = 0;
        int tileNum = 0;

        // Load the Map
        // Loop through the tiles and assign rect values, load values from the values table, and
        // also load the tile type from the levels tables. 
        for(int r = 6; r > -1; r--) {
            for(int c = 0; c < 7; c++) {

                // Create a placeholder for the tile type
                TileType rcType;

                // Determine the tile type
                switch(game.gravityGridLevel[game.currentLevel][(r*7)+c]) {
                    case 0:
                        rcType = TileType.NONE;
                        break;
                    case 1:
                        rcType = TileType.REDPLANET;
                        break;
                    case 2:
                        rcType = TileType.BLUEPLANET;
                        break;
                    case 3:
                        rcType = TileType.GREENPLANET;
                        break;
                    case 4:
                        rcType = TileType.ASTEROID;
                        break;
                    case 5:
                        rcType = TileType.SUN;
                        break;
                    case 9:
                        rcType = TileType.BLOCKED;
                        break;
                    default:
                        rcType = TileType.NONE;
                        break;
                }

                // Create a placeholder for the rect values
                Rectangle rect = new Rectangle();

                // Determine the rect values
                rect.x = leftSpace + worldCol*this.tileWidth;
                rect.y = whiteSpace - headSpace + worldRow * this.tileHeight;
                rect.width = this.tileWidth;
                rect.height = this.tileHeight;

                // Here we push the new tile to the array. Since we are assigning rect values per tile, it doesn't
                // matter (theoretically) how we access these tiles or if they're out of order. 
                Tile temp = new Tile(rect, game.tileValueTable[(r*7)+c], rcType, tileNum);
                tile.set(tileNum, temp);

                worldCol++;
                tileNum++;

                // (4-Nov-2016 Jesse) Add a new particle system on each and every tile so that when the level starts, there's a huge burst of stars
                ParticleEffectPool.PooledEffect levelStartEffect = goodMoveStarburstPool.obtain();
                levelStartEffect.setPosition(rect.x+(rect.width/2), rect.y+(rect.height/2));
                particleEffects.add(levelStartEffect);

            }
            worldCol = 0; // Reset column counter
            worldRow++; // Iterate our row counter
        }

        // Reset the variables 
        thisLevelCurrentMoves = 0;
        updateCurrentLevelValueTotals();
        readyForInput = true;
        theGameState = gameState.READY;
        tryingToReset = false;

        restartLevelSound.play();
    }

    public void OutOfMoves() {

    }

    public void LevelComplete() {



    }

    public void PlayLevel(float delta) {

        // Check for input
            if (Gdx.input.justTouched()) {

                Vector3 finger = new Vector3();
                camera.unproject(finger.set(Gdx.input.getX(), Gdx.input.getY(), 0));

                if (theGameState == gameState.IN_GAME_MENU) {

                    // Did we tap cancel button?
                    if (pointInRectangle(inGameMenuButtonRect, finger.x, finger.y)) {
                        System.out.println("Exiting in-game menu");
                        theGameState = gameState.READY;
                        readyForInput = false;
                    }

                    // Did we reset?
                    if (pointInRectangle(inGameMenuResetButtonRect, finger.x, finger.y)) {
                        RestartLevel();
                    }
                    // Did we tap level select?
                    if (pointInRectangle(inGameMenuLevelSelectButtonRect, finger.x, finger.y)) {
                        game.setScreen(new LevelSelectScreen(game));
                    }

                    // Did we tap help button?
                    if (pointInRectangle(inGameMenuHelpButtonRect, finger.x, finger.y)) {
                        // TODO: Add a help screen
                    }
                }
            }

        if (Gdx.input.isTouched()) {

            Vector3 finger = new Vector3();
            camera.unproject(finger.set(Gdx.input.getX(), Gdx.input.getY(), 0));

                if (readyForInput) {

                    if(theGameState != gameState.IN_GAME_MENU) {
                        if(pointInRectangle(inGameMenuButtonRect, finger.x, finger.y)) {
                            // Make sure there was a separate touch than the one that caused the menu to appear
                                theGameState = gameState.IN_GAME_MENU;
                        }
                    }

                    // Only process input if the game state is READY
                    if (theGameState == gameState.READY) {

                        //game.regularFont.setColor(1f,1f,1f,1f);
                        //game.regularFont.draw(game.batch, ""+finger.x + ","+finger.y, 240,30);

                        // If we touched the screen during READY state, check to see if we touched a planet.
                        // If we did, mark it as selected.
                        // The `checkTiles` below is how you add a break in Java.
                        checkTiles:
                        for (Tile tile : this.tile) {
                            // We only care about tiles that are planets. Anything else can't be touched (for now).
                            // TODO: Add logic for handling input of MENU and HELP buttons outside of the checkTiles section below.
                            if (tile.type == TileType.REDPLANET || tile.type == TileType.BLUEPLANET || tile.type == TileType.GREENPLANET) {
                                if (pointInRectangle(tile.rect, finger.x, finger.y)) {

                                    // DEBUG:
                                    //game.regularFont.setColor(1f,1f,1f,1f);
                                    //game.regularFont.draw(game.batch, "Tile "+tile.value, 240,60);

                                    // if game state is ready and tile status is none, that means we haven't tried to move yet.
                                    if (tile.status == TileStatus.NONE) {
                                        // Select this tile
                                        tile.status = TileStatus.SELECTED;
                                        // Set the gamestate to handle input after a tile is selected
                                        theGameState = gameState.TILE_SELECTED;

                                        tileSelectSound.play();

                                        readyForInput = false;

                                        // If we've found our tile, there's no sense in looping through the rest of them so we break the loop here.
                                        break checkTiles;
                                    }

                                } // end check for if pointInRectangle
                            } // end check for tile.type
                        } // end for loop


                        // Check to see if our gamestate is still ready. if it is, that means we didn't touch a tile.
                        // We can now go ahead and check to see if the player pressed any UI elements.

                    }
                }


                // To make sure we are not "repeat key"ing, check for ready for input

                if (readyForInput) {
                    if (theGameState == gameState.TILE_SELECTED) {

                        // If we touched the screen during a TILE_SELECTED state, check to see if we touched a type.NONE&&status.NONE
                        // If we did, then find the SELECTED tile's type and move it.

                        // First check to see if where we are going is indeed go-able
                        markDestinationTile:
                        for (Tile tile : this.tile) {
                            // Did we collide with this tile?
                            if (pointInRectangle(tile.rect, finger.x, finger.y)) {

                                // Is this tile blank?
                                if (tile.type == TileType.NONE && tile.status == TileStatus.NONE) {

                                    // This is a potential tile to move to. Check if we're touching it

                                    // Mark this tile as the move destination
                                    tile.status = TileStatus.MOVETOHERE;

                                    // Update game state so we know that both SELECTED and MOVETOHERE have been set.
                                    theGameState = gameState.GOOD_MOVE_ATTEMPT;
                                    tileSelectSound.play();
                                    readyForInput = false;
                                    break markDestinationTile;


                                } else if (tile.status == TileStatus.SELECTED) {

                                    // We clicked a tile that was marked as selected, so deselect it
                                    tile.status = TileStatus.NONE;

                                    // And reset the game state
                                    theGameState = gameState.READY;

                                    tileDeselectSound.play();


                                    readyForInput = false;
                                    break markDestinationTile;

                                    // Don't need the below, since the else {} will cover everyone else
                                    // } else if(tile.type == TileType.REDPLANET || tile.type == //TileType.BLUEPLANET || tile.type == TileType.GREENPLANET) {

                                } else {    // This should cover PLANET, ASTEROID, SUN, and BLOCKED types. (anyone not BLANK)
                                    // (21-Aug-2015 Jesse) Display cannot move animation on any tile
                                    // we try to move to that we can't move to.
                                    tile.status = TileStatus.CANNOTMOVE;

                                    cannotMoveSound.play();

                                    readyForInput = false;
                                    break markDestinationTile;

                                }

                            }
                        } // end for loop					// end check for gamestate.READY during touch

                    }
                }
            } else {
                readyForInput = true;
            }




        // Now that we've processed finger input, let's update the rest of the game states so long as we're not
        // pressing the screen
        if(readyForInput) {



            // If the game state is GOOD_MOVE_ATTEMPT, that means the above loop to find a good tile to move to
            // succeeded. We should now loop through the tiles again and find the SELECTED tile
			/* (12-Aug-2015 Jesse) Previously I had been trying to null out the tile and then recreate one with tile = new Tile(), but
				this was a mistake. Instead, I realized that it's just easier to copy over the tile status and type, which is exactly what
				we want to copy over (since the value of the new position is going to be relevant to the changing of the scores). 
			*/
            if(theGameState == gameState.GOOD_MOVE_ATTEMPT) {

                moveSelectedTile:
                for(Tile from : this.tile) {
                    if(from.status == TileStatus.SELECTED) {

                        for(Tile to : this.tile) {
                            if(to.status == TileStatus.MOVETOHERE) {

                                // If we are here then we have selected a blank tile and not a tile with another planet
                                // or a tile with an asteriod or sun. Now we check to see if we are moving according to
                                // our game rules.
                                if(canMoveAccordingToRules(to.tileNum)) {

                                    to.status = TileStatus.MOVECOMPLETE;
                                    to.type = from.type;
                                    to.rand = from.rand; // This way we get the same planet graphic

                                    from.status = TileStatus.NONE;
                                    from.type = TileType.NONE;

                                    // If we still have moves left, set the gamestate to ready
                                    // Otherwise, set the gamestate to OUT_OF_MOVES
                                    thisLevelCurrentMoves++;

                                    // Finally, after every tile move we check to see if we've beaten the level.
                                    // If we have, force gamestate to LEVEL_COMPLETE. If not, 
                                    // check to make sure we're not out of moves and then if we are not,
                                    // go back to READY.
                                    updateCurrentLevelValueTotals();

                                    goodMoveAttemptSound.play();

                                    // Create a new particle system at this tile. The system is generated independently of the tile itself; we only need to create it here to
                                    // know the to.rect values (i.e., where the system will originate)
                                    ParticleEffectPool.PooledEffect effect = goodMoveStarburstPool.obtain();
                                    effect.setPosition(to.rect.x+(0.5f*to.rect.width), to.rect.y+(0.5f*to.rect.height));
                                    // Add our new particle system to the particleeffects array
                                    particleEffects.add(effect);


                                    if(	thisLevelRedNeeded == thisLevelCurrentRedTotal &&
                                            thisLevelBlueNeeded == thisLevelCurrentBlueTotal &&
                                            thisLevelGreenNeeded == thisLevelCurrentGreenTotal) {
                                        theGameState = gameState.LEVEL_COMPLETE;
                                        levelCompleteSound.play();
                                        //Gdx.input.vibrate(500);

                                    } else {
                                        // We have not beaten the level, so let's
                                        //if(thisLevelCurrentMoves > thisLevelMaxMoves) {
                                            //theGameState = gameState.OUT_OF_MOVES;

                                            //outOfMovesSound.play();

                                        //} else {
                                            // We haven't beaten the level && we haven't maxed out our moves, so
                                            // go back to ready
                                            theGameState = gameState.READY;
                                        //}
                                    }

                                    break moveSelectedTile;
                                } else {
                                    // Oops! We violated our rules. Reset selected tile and set the tile
                                    // we were trying to move to to CANNOTMOVE
                                    to.status = TileStatus.CANNOTMOVE;

                                    from.status = TileStatus.NONE;

                                    // Reset the gamestate, too
                                    theGameState = gameState.READY;

                                    cannotMoveSound.play();
                                    // Create a new particle system at this tile
                                    ParticleEffectPool.PooledEffect effect = badMoveStarburstPool.obtain();
                                    effect.setPosition(to.rect.x+(0.5f*to.rect.width), to.rect.y+(0.5f*to.rect.height));
                                    // Add our new particle system to the particleeffects array
                                    particleEffects.add(effect);

                                    break moveSelectedTile;
                                }
                            }
                        }
                    }

                    // TODO: How would you error-check this?
                }

            }
        }

        // Draw the background based on the "galaxy," which is just the level#/10. Every ten levels is a new background (i.e., a new galaxy)
        game.batch.setColor(1.0f,1.0f,1.0f,1.0f);
        game.batch.draw(backgroundImage[thisLevelBackgroundImageNumber], 0,0,screenWidth, screenHeight);


        // The starfield kept rendering at approximately half width and half height. I edited the particle p file to have
        // spawn width: 2160
        // spawn height: 3840
        // Not sure why this was necessary... and what effect does this have on different rendering displays? Nothing, because I am hard-setting the orthographic projection?
        backgroundStarfieldParticles.setPosition(0,0);
        game.batch.setColor(0.5f,0.5f,0.5f,0.1f);
        backgroundStarfieldParticles.draw(game.batch, delta*.75f);

        int cellNumber = 0;

        // Loop through tiles and draw them
        for (Tile tile : this.tile) {

            // Always draw the tile border
            game.batch.setColor(1f,1f,1f,1f);

            // Draw the value (which are just numbers) image, then the border image (tileBlankImage)

            // (22-Aug-2015 Jesse) Sami had an idea to have the planets sized according to their
            // tile value. I tried this out a few times but the planets changing sizes continuously 
            // just confused me. So I'm going to hard-set this to 1.0f, but keep the variable here
            // just in case we decide to do something crazy later. 
            //float sizeMultiplier = ( ((float)tile.value/10.0f) > 0.5f ? (float)tile.value/10.0f : 0.8f); // create a multiplier for this planet
            float sizeMultiplier = 1.0f;								// size based on tile value. 

            // First draw the type of tile it is and the value of the tile if it's a planet
            switch(tile.type) {
                case NONE:
                    // Flip these if you want the text brighter. right now it's behind the transparent tileir
                    //game.batch.setColor(0.5f,0.5f,0.5f,0.5f);
                    game.batch.setColor(0.5f,0.5f,0.5f,tile.value/10.0f);
                    game.batch.draw(tileBlankImage, tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    game.batch.setColor(1f,1f,1f,.8f);
                    game.batch.draw(tileValueImage[tile.value], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    game.batch.setColor(1f,1f,1f,1f);
                    break;
                case REDPLANET:
                    game.batch.draw(tileRedPlanetRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, sizeMultiplier, sizeMultiplier, 0.0f);
                    game.batch.setColor(1f,1f,1f,1f);
                    game.batch.draw(tileValueImage[tile.value], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    break;
                case BLUEPLANET:
                    game.batch.draw(tileBluePlanetRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, sizeMultiplier, sizeMultiplier, 0.0f);
                    game.batch.setColor(1f,1f,1f,1f);
                    game.batch.draw(tileValueImage[tile.value], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    break;
                case GREENPLANET:
                    game.batch.draw(tileGreenPlanetRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, sizeMultiplier, sizeMultiplier, 0.0f);
                    game.batch.setColor(1f,1f,1f,1f);
                    game.batch.draw(tileValueImage[tile.value], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    break;
                case ASTEROID:
                    game.batch.setColor(1f,1f,1f,1f);
                    game.batch.draw(tileAsteroidImage[tile.rand], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    break;
                case SUN:
                    game.batch.setColor(1f,1f,1f,1f);
                    //game.batch.draw(tileSunImage, tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    game.batch.draw(tileSunRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 3.0f, 3.0f, tile.rand*71f);
                    game.batch.setColor(1f,1f,1f,0.5f);
                    game.batch.draw(tileSunFlareRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 8.0f, 8.0f, tile.rand*71f);
                    break;
                default:
                    break;
            }

            // Next draw the status (if selected or moving or failed to move or whatnot)
            switch(tile.status) {
                case SELECTED:

                    if(tileSelectedBottomDirection <= 0.0f) {
                        tile.overlayRotation = 360.0f;
                    }
                    if(tileSelectedTopDirection >= 360.0f) {
                        tile.overlayRotation = 0.0f;
                    }

                    // Draw the bottom one
                    game.batch.setColor(1.0f,1.0f,1.0f,0.5f);
                    game.batch.draw(tileSelectedBottomRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 1.0f, 1.0f, tileSelectedBottomDirection);
                    game.batch.draw(tileSelectedTopRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 1.0f, 1.0f, tileSelectedTopDirection);

                    // Rotate that bad boy
                    tileSelectedBottomDirection -= 3.0f;
                    tileSelectedTopDirection += 6.0f;

                    //game.regularFont.setColor(1f,1f,1f,0.5f);
                    //game.regularFont.draw(game.batch, "Tile Selected!", 0,30);

                    break;

                case MOVECOMPLETE:
                    // Even though we use a particle system to display some bursting stars, I also want to display the sunflare
                    // If we still have a frame in our animation
                    if(tile.overlayFrameNumber < 12) {
                        game.batch.setColor(1.0f,1.0f,1.0f,1f-(tile.overlayFrameNumber/12.0f));
                        game.batch.draw(tileSunFlareRegion, tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 1.5f*(.5f*tile.overlayFrameNumber), 1.5f*(.5f*tile.overlayFrameNumber), tile.overlayFrameNumber*13.0f);
                        // Add deltatime to our time since last frame
                        if (TimeUtils.nanoTime() - tile.timeSinceLastFrame > 25000000) {
                            tile.overlayFrameNumber++;
                            tile.timeSinceLastFrame = TimeUtils.nanoTime();
                        }
                    } else {
                        // Reset everything if we're done with our animation
                        tile.overlayFrameNumber = 0;
                        tile.timeSinceLastFrame = 0f;
                        tile.status = TileStatus.NONE;
                    }

                    break;

                case CANNOTMOVE:
                        tile.status = TileStatus.NONE;
                    break;

                default: break;
            }

            // Check if the status is 

            cellNumber++;
        }

        // Draw all particle effects from our array that holds all currently running particle effect systems
        for (int i = particleEffects.size - 1; i >= 0; i--) {
            ParticleEffectPool.PooledEffect effect = particleEffects.get(i);
            effect.draw(game.batch, delta*1.5f);
            if (effect.isComplete()) {
                effect.free();
                particleEffects.removeIndex(i);
            }
        }

        // Trick to get accurate lines:
        // Set the Y value of the rendered fonts to (lineNumberFromTop*(screenHeight-this.game.fontSize))

        // Generate some values for our color indicators, with 5-px padding
        // NOTE: when using halign=1 in regularFont.draw, you are setting the X value RELATIVE to the center of the screen. Don't forget, dummy!
        // ALSO: 0-(Gdx.graphics.getWidth()/2); will put the center RIGHT on the edge of the screen, so you'll need to shift (pad) the X value a bit
        // (19-Nov-2016 Jesse) Only display the color score if we have that planet type on this level
        float blueScoreX = 0; // center
        float redScoreX = 0-(Gdx.graphics.getWidth()/4); // use 1/4 distance
        float greenScoreX = (Gdx.graphics.getWidth()/4);

        game.pixelFont.setColor(game.colorOrange);
        game.pixelFont.draw(game.batch, "GRAVITY GRID", 0, screenHeight, Gdx.graphics.getWidth(), 1, false);

        game.regularFont.setColor(1f,1f,1f,1f);
        game.regularFont.draw(game.batch, "Level "+(game.currentLevel+1)+", Par "+thisLevelMaxMoves+". You: "+thisLevelCurrentMoves, 5, screenHeight-(1.5f*this.game.fontSize), this.screenWidth-10, 1, false);

        game.pixelFont.setColor(game.colorRed);
        game.pixelFont.draw(game.batch, ""+thisLevelCurrentRedTotal+"/"+thisLevelRedNeeded+"", redScoreX, screenHeight-(4*this.game.fontSize), this.screenWidth-10, 1, false);
        game.pixelFont.setColor(game.colorBlue);
        game.pixelFont.draw(game.batch, ""+thisLevelCurrentBlueTotal+"/"+thisLevelBlueNeeded+"", blueScoreX, screenHeight-(4*this.game.fontSize), this.screenWidth-10, 1, false);
        game.pixelFont.setColor(game.colorGreen);
        game.pixelFont.draw(game.batch, ""+thisLevelCurrentGreenTotal+"/"+thisLevelGreenNeeded+"", greenScoreX, screenHeight-(4*this.game.fontSize), this.screenWidth-10, 1, false);

        // Draw the menu button
        game.batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
        game.batch.draw(inGameMenuButtonImage, inGameMenuButtonRect.x, inGameMenuButtonRect.y, inGameMenuButtonRect.width, inGameMenuButtonRect.height);

        // Display the level message if we have one, and only if the gameState is ready or tile selected or good move attempt
        if(theGameState == gameState.READY || theGameState == gameState.TILE_SELECTED || theGameState == gameState.GOOD_MOVE_ATTEMPT || theGameState == gameState.IN_GAME_MENU) {

            // The location of the top line should be below the last tile. We can find this easily:
            float tileHeight = Gdx.graphics.getWidth() / 7;
            float middle = Gdx.graphics.getHeight() / 2;
            float startLineY = middle - 3.85f*tileHeight; // So we want to start 3.5*tileHeight from center of screen. That should get us to the bottom.
                                                            // At 3.85f*tileHeight, we give ourselves a little padding between the text and grid

            // Draw a black square behind the text
            //game.batch.setColor(1.0f, 1.0f, 1.0f, 1.0f);
            //game.batch.draw(levelMessageBackgroundImage, 0, 0, this.screenWidth, this.whiteSpace);
            game.regularFont.setColor(1f,0.5f, 1f, 1f);
            game.regularFont.draw(game.batch, ""+game.levelMessage[game.currentLevel], 40, startLineY, this.screenWidth-40, 1, true);
            game.regularFont.setColor(1f,1f,1f,1f);  //reset the regularFont color to white
        }




        // The level has been marked complete, so let's display our awesome level complete confetti loop!
        if(theGameState == gameState.LEVEL_COMPLETE) {

            game.batch.setColor(1f,1f,1f,1f);

            // Render our fireworks
            levelCompleteFireworks.setPosition(0,0);
            levelCompleteFireworks.draw(game.batch, delta);

            // Draw the level complete modal
            game.batch.draw(buttonLevelCompleteImage, 0, 0, this.screenWidth, this.whiteSpace);

            if(Gdx.input.isTouched()) {

                // This will update our currentLevel
                // Status type = 2 means we beat the level
                this.game.UpdateLevelCompletionInfo(this.game.currentLevel, 2, this.thisLevelCurrentAttempts, this.thisLevelCurrentMoves, 100);

                // RestartLevel uses game.currentLevel to determine which level to load, so it's imperative that
                // game.UpdateLevelCompletionInfo is called first!
                RestartLevel();
            }


        }

        if(theGameState == gameState.IN_GAME_MENU) {
            game.batch.setColor(1.0f,1.0f,1.0f,1.0f);

            // Draw the background
            game.batch.setColor(1.0f,1.0f,1.0f,0.5f);
            game.batch.draw(inGameMenuBackgroundImage, 0, 0, screenWidth, screenHeight);
            game.batch.setColor(1.0f,1.0f,1.0f,1.0f);
            // Draw the icons center-line

            game.batch.draw(inGameMenuResetButtonImage, inGameMenuResetButtonRect.x, inGameMenuResetButtonRect.y, inGameMenuResetButtonRect.width, inGameMenuResetButtonRect.height);
            game.batch.draw(inGameMenuLevelSelectButtonImage, inGameMenuLevelSelectButtonRect.x, inGameMenuLevelSelectButtonRect.y, inGameMenuLevelSelectButtonRect.width, inGameMenuLevelSelectButtonRect.height);
            game.batch.draw(inGameMenuHelpButtonImage, inGameMenuHelpButtonRect.x, inGameMenuHelpButtonRect.y, inGameMenuHelpButtonRect.width, inGameMenuHelpButtonRect.height);

            game.batch.draw(inGameMenuCancelButtonImage, inGameMenuButtonRect.x, inGameMenuButtonRect.y, inGameMenuButtonRect.width, inGameMenuButtonRect.height);
        }

        game.batch.end();
    }

    @Override
    public void render(float delta) {
        // Clear to black
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        // DEBUG THE GAMESTATE
		/*switch(theGameState) {
			case READY:
				game.regularFont.draw(game.batch, "READY", 0, 250);
				break;
			case TILE_SELECTED:
				game.regularFont.draw(game.batch, "TILE_SELECTED", 0, 250);
				break;
			case GOOD_MOVE_ATTEMPT:
				game.regularFont.draw(game.batch, "GOOD_MOVE_ATTEMPT", 0, 250);
				break;
			case LEVEL_COMPLETE:
				game.regularFont.draw(game.batch, "LEVEL_COMPLETE", 0, 250);
				break;
			case OUT_OF_MOVES:
				game.regularFont.draw(game.batch, "OUT_OF_MOVES", 0, 250);
				break;
			
			default:
				break;
		}*/

        // Switch the gamestate to ensure that we are not OUT_OF_MOVES or LEVEL_COMPLETE

        PlayLevel(delta);

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {
        // start the playback of the background music
        // when the screen is shown
        //scannerHum.play();
    }

    @Override
    public void hide() {
        // TODO: Switch the gamestate to IN_GAME_MENU
    }

    @Override
    public void pause() {
        // TODO: Switch the gamestate to IN_GAME_MENU
    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {


    }

}