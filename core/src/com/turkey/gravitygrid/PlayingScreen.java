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
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;


public class PlayingScreen implements Screen {

    // A custom point-in-rectangle collision checker
    public static boolean pointInRectangle (Rectangle r, float x, float y) {
        return r.x <= x && r.x + r.width >= x && r.y <= y && r.y + r.height >= y;
    }

    public boolean fingerOnScreen; // Are we getting input?
    public boolean readyForInput; // Can we handle input?

    // On create, these are loaded according to the currentLevel that is managed in the GravityGrid class
    public int thisLevelRedNeeded;
    public int thisLevelBlueNeeded;
    public int thisLevelGreenNeeded;
    public int thisLevelMaxMoves;
    public int thisLevelCurrentRedTotal;
    public int thisLevelCurrentBlueTotal;
    public int thisLevelCurrentGreenTotal;
    public int thisLevelCurrentMoves;

    public int bgRand; // Random number for each level's background

    public enum gameState {
        READY, 						// Process tile selections
        TILE_SELECTED, 	// Process movements from selections
        // Check new touched tile for empty. If yes, then go find the tile that is marked as
        // 	selected, find out its type, and then copy it to this new one.
        // Update the animations.
        GOOD_MOVE_ATTEMPT,			// Player selected a blank tile to move to, all that's left is to ensure rules are followed
        OUT_OF_MOVES, 				// Player has ran out of moves, so the only thing they can do is reset the level
        OUT_OF_LIVES,				// Player has 0 dark matter
        LEVEL_COMPLETE
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
        for(Tile tile : this.tile) {

            // Check for selected tile
            if(tile.status == TileStatus.SELECTED) {
                // Extract the type of the selected tile so we can assign the appropriate rules to the planet type
                selectedType = tile.type;
                selectedNum = tile.tileNum;
                break findSelected;
            }

        }

        // Read this as "for each 'tile' that exists in the arraylist 'this.tile'. This is how Java does foreach loops.
        for(Tile tile : this.tile) {

            // Now we're in the meat and potatoes. The function seeks to answer: can a tile of type selectedType move to
            // the destinationTileNum type?

            // We'll have to switch the selected type so we can apply the appropriate rules.
            // (13-Aug-2015 Jesse) I'm running into a problem where the rules are checking against the selected tile,
            // because technically that is a tile that is next to the next tile. In other words, I can click a blue
            // planet along a straight path away from other planets because the tile I am moving to is next to a blue
            // planet tile -- the one I am on! We need a way to omit the selected tile to ensure that we're only counting OTHER tiles of the same type
            // (13-Aug-2015 Jesse) I discovered that if I added '&& tile.tileNum != selectedNum' to the part where I check whether the tile's type of the adjacent one is the same type of planet as the selected tile, I can ensure that I don't count any adjacent tiles of the same type that are in fact the selected tile (and thus only account for non-selected adjacent tiles)

            switch(selectedType) {

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

                    // 18-Aug: Discovered an awkward error that occurs when computing the values for cells that are on the
                    // edges. Fixed this by segregating the checks by sides (top, left, bottom, right) and then by body

                    // The logic below seeks to answer the question: If a red planet is selected, can they move to tileNum?

                    // Check the corners
                    if(
                           (destinationTileNum == 0  && tile.tileNum == 8)
                        || (destinationTileNum == 42 && tile.tileNum == 36)
                        || (destinationTileNum == 48 && tile.tileNum == 40)
                        || (destinationTileNum == 6  && tile.tileNum == 12) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if( // check left side
                        (tile.tileNum == 35 && (destinationTileNum == 43 || destinationTileNum == 29))
                        || (tile.tileNum == 28 && (destinationTileNum == 36 || destinationTileNum == 22))
                        || (tile.tileNum == 21 && (destinationTileNum == 29 || destinationTileNum == 22))
                        || (tile.tileNum == 14 && (destinationTileNum == 22 || destinationTileNum == 8))
                        || (tile.tileNum == 7 && (destinationTileNum == 15 || destinationTileNum == 1)) ) {
                            // If the tile that we found that is around us is one of the selectedType planets (i.e., if we selected a red planet, is the surrounding tile that we found also a red planet?), then yes, we can move here.
                            // Additionally, let's make sure that even though this has an adjacent tiletype, that adjacent
                            // tile type is not our selected tile.
                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if ( // check top side
                           ( tile.tileNum == 43 && (destinationTileNum == 35 || destinationTileNum == 37) )
                        || ( tile.tileNum == 44 && (destinationTileNum == 36 || destinationTileNum == 38) )
                        || ( tile.tileNum == 45 && (destinationTileNum == 37 || destinationTileNum == 39) )
                        || ( tile.tileNum == 46 && (destinationTileNum == 38 || destinationTileNum == 40) )
                        || ( tile.tileNum == 47 && (destinationTileNum == 39 || destinationTileNum == 41) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if ( // check right side
                           ( tile.tileNum == 41 && (destinationTileNum == 47 || destinationTileNum == 33) )
                        || ( tile.tileNum == 34 && (destinationTileNum == 40 || destinationTileNum == 26) )
                        || ( tile.tileNum == 27 && (destinationTileNum == 33 || destinationTileNum == 19) )
                        || ( tile.tileNum == 20 && (destinationTileNum == 26 || destinationTileNum == 12) )
                        || ( tile.tileNum == 13 && (destinationTileNum == 19 || destinationTileNum == 5 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if( // check bottom side
                           ( tile.tileNum == 1 && (destinationTileNum == 7  || destinationTileNum == 9 ) )
                        || ( tile.tileNum == 2 && (destinationTileNum == 8  || destinationTileNum == 10) )
                        || ( tile.tileNum == 3 && (destinationTileNum == 9  || destinationTileNum == 11) )
                        || ( tile.tileNum == 4 && (destinationTileNum == 10 || destinationTileNum == 12) )
                        || ( tile.tileNum == 5 && (destinationTileNum == 11 || destinationTileNum == 13) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else {
                        if( // check the rest of the grid
                               tile.tileNum == destinationTileNum-6
                            || tile.tileNum == destinationTileNum+6
                            || tile.tileNum == destinationTileNum-8
                            || tile.tileNum == destinationTileNum+8 )  {

                            if(tile.type == selectedType && tile.tileNum != selectedNum) {
                                outcome = true;
                            }
                        }
                    }

                break;

                case BLUEPLANET:

                    if( // Check the corners
                            (destinationTileNum == 42 && ( tile.tileNum == 35 || tile.tileNum == 43 ) )
                         || (destinationTileNum == 48 && ( tile.tileNum == 47 || tile.tileNum == 41 ) )
                         || (destinationTileNum == 0  && ( tile.tileNum == 7  || tile.tileNum == 1  ) )
                         || (destinationTileNum == 6  && ( tile.tileNum == 5  || tile.tileNum == 13 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if( // check left side
                           ( tile.tileNum == 35 && (destinationTileNum == 42 || destinationTileNum == 36 || destinationTileNum == 28 ) )
                        || ( tile.tileNum == 28 && (destinationTileNum == 35 || destinationTileNum == 29 || destinationTileNum == 21 ) )
                        || ( tile.tileNum == 21 && (destinationTileNum == 28 || destinationTileNum == 22 || destinationTileNum == 14 ) )
                        || ( tile.tileNum == 14 && (destinationTileNum == 21 || destinationTileNum == 15 || destinationTileNum ==  7 ) )
                        || ( tile.tileNum ==  7 && (destinationTileNum == 14 || destinationTileNum ==  8 || destinationTileNum ==  0 ) ) ) {

                        // If the tile that we found that is around us is one of the selectedType planets (i.e., if we selected a red planet, is the surrounding tile that we found also a red planet?), then yes, we can move here.
                        // Additionally, let's make sure that even though this has an adjacent tiletype, that adjacent
                        // tile type is not our selected tile.
                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if ( // check top side
                           ( tile.tileNum == 43 && (destinationTileNum == 42 || destinationTileNum == 36 || destinationTileNum == 44 ) )
                        || ( tile.tileNum == 44 && (destinationTileNum == 43 || destinationTileNum == 37 || destinationTileNum == 45 ) )
                        || ( tile.tileNum == 45 && (destinationTileNum == 44 || destinationTileNum == 38 || destinationTileNum == 46 ) )
                        || ( tile.tileNum == 46 && (destinationTileNum == 45 || destinationTileNum == 39 || destinationTileNum == 47 ) )
                        || ( tile.tileNum == 47 && (destinationTileNum == 46 || destinationTileNum == 40 || destinationTileNum == 48 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if ( // check right side
                           ( tile.tileNum == 41 && (destinationTileNum == 48 || destinationTileNum == 40 || destinationTileNum == 34 ) )
                        || ( tile.tileNum == 34 && (destinationTileNum == 41 || destinationTileNum == 33 || destinationTileNum == 27 ) )
                        || ( tile.tileNum == 27 && (destinationTileNum == 34 || destinationTileNum == 26 || destinationTileNum == 20 ) )
                        || ( tile.tileNum == 20 && (destinationTileNum == 27 || destinationTileNum == 19 || destinationTileNum == 13 ) )
                        || ( tile.tileNum == 13 && (destinationTileNum == 20 || destinationTileNum == 12 || destinationTileNum ==  6 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if( // check bottom side
                           ( tile.tileNum == 1 && (destinationTileNum ==  0 || destinationTileNum ==  8 || destinationTileNum ==  2 ) )
                        || ( tile.tileNum == 2 && (destinationTileNum ==  1 || destinationTileNum ==  9 || destinationTileNum ==  3 ) )
                        || ( tile.tileNum == 3 && (destinationTileNum ==  2 || destinationTileNum == 10 || destinationTileNum ==  4 ) )
                        || ( tile.tileNum == 4 && (destinationTileNum ==  3 || destinationTileNum == 11 || destinationTileNum ==  5 ) )
                        || ( tile.tileNum == 5 && (destinationTileNum ==  4 || destinationTileNum == 12 || destinationTileNum ==  6 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else {
                        if( // check the rest of the grid
                           tile.tileNum == destinationTileNum-6
                        || tile.tileNum == destinationTileNum+6
                        || tile.tileNum == destinationTileNum-8
                        || tile.tileNum == destinationTileNum+8 )  {

                            if(tile.type == selectedType && tile.tileNum != selectedNum) {
                                outcome = true;
                            }
                        }
                    }

                break;

                case GREENPLANET:
                    if( // Check the corners
                           ( destinationTileNum == 42 && ( tile.tileNum == 43 || tile.tileNum == 36 || tile.tileNum == 35 ) )
                        || ( destinationTileNum == 48 && ( tile.tileNum == 47 || tile.tileNum == 40 || tile.tileNum == 41 ) )
                        || ( destinationTileNum == 0  && ( tile.tileNum == 7  || tile.tileNum ==  8 || tile.tileNum ==  1 ) )
                        || ( destinationTileNum == 6  && ( tile.tileNum == 5  || tile.tileNum == 12 || tile.tileNum == 13 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if( // check left side
                           ( tile.tileNum == 35 && (destinationTileNum == 42 || destinationTileNum == 43 || destinationTileNum == 36 || destinationTileNum == 29 || destinationTileNum == 28 ) )
                        || ( tile.tileNum == 28 && (destinationTileNum == 35 || destinationTileNum == 36 || destinationTileNum == 29 || destinationTileNum == 22 || destinationTileNum == 21 ) )
                        || ( tile.tileNum == 21 && (destinationTileNum == 28 || destinationTileNum == 29 || destinationTileNum == 22 || destinationTileNum == 15 || destinationTileNum == 14 ) )
                        || ( tile.tileNum == 14 && (destinationTileNum == 21 || destinationTileNum == 22 || destinationTileNum == 15 || destinationTileNum ==  8 || destinationTileNum ==  7 ) )
                        || ( tile.tileNum ==  7 && (destinationTileNum == 14 || destinationTileNum == 15 || destinationTileNum ==  8 || destinationTileNum ==  1 || destinationTileNum ==  0 ) ) ) {

                        // If the tile that we found that is around us is one of the selectedType planets (i.e., if we selected a red planet, is the surrounding tile that we found also a red planet?), then yes, we can move here.
                        // Additionally, let's make sure that even though this has an adjacent tiletype, that adjacent
                        // tile type is not our selected tile.
                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if( // check top side
                           ( tile.tileNum == 43 && (destinationTileNum == 42 || destinationTileNum == 35 || destinationTileNum == 36 || destinationTileNum == 37 || destinationTileNum == 44 ) )
                        || ( tile.tileNum == 44 && (destinationTileNum == 43 || destinationTileNum == 36 || destinationTileNum == 37 || destinationTileNum == 38 || destinationTileNum == 45 ) )
                        || ( tile.tileNum == 45 && (destinationTileNum == 44 || destinationTileNum == 37 || destinationTileNum == 38 || destinationTileNum == 39 || destinationTileNum == 46 ) )
                        || ( tile.tileNum == 46 && (destinationTileNum == 45 || destinationTileNum == 38 || destinationTileNum == 37 || destinationTileNum == 40 || destinationTileNum == 47 ) )
                        || ( tile.tileNum == 47 && (destinationTileNum == 46 || destinationTileNum == 39 || destinationTileNum == 40 || destinationTileNum == 41 || destinationTileNum == 48 ) ) ) {

                        // If the tile that we found that is around us is one of the selectedType planets (i.e., if we selected a red planet, is the surrounding tile that we found also a red planet?), then yes, we can move here.
                        // Additionally, let's make sure that even though this has an adjacent tiletype, that adjacent
                        // tile type is not our selected tile.
                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if ( // check right side
                           ( tile.tileNum == 41 && (destinationTileNum == 48 || destinationTileNum == 47 || destinationTileNum == 40 || destinationTileNum == 33 || destinationTileNum == 34 ) )
                        || ( tile.tileNum == 34 && (destinationTileNum == 41 || destinationTileNum == 40 || destinationTileNum == 33 || destinationTileNum == 26 || destinationTileNum == 27 ) )
                        || ( tile.tileNum == 27 && (destinationTileNum == 34 || destinationTileNum == 33 || destinationTileNum == 26 || destinationTileNum == 19 || destinationTileNum == 20 ) )
                        || ( tile.tileNum == 20 && (destinationTileNum == 27 || destinationTileNum == 26 || destinationTileNum == 19 || destinationTileNum == 12 || destinationTileNum == 13 ) )
                        || ( tile.tileNum == 13 && (destinationTileNum == 20 || destinationTileNum == 19 || destinationTileNum == 12 || destinationTileNum ==  5 || destinationTileNum ==  6 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else if( // check bottom side
                           ( tile.tileNum == 1 && (destinationTileNum ==  0 || destinationTileNum ==  7 || destinationTileNum ==  8 || destinationTileNum ==  9 || destinationTileNum ==  2 ) )
                        || ( tile.tileNum == 2 && (destinationTileNum ==  1 || destinationTileNum ==  8 || destinationTileNum ==  9 || destinationTileNum == 10 || destinationTileNum ==  3 ) )
                        || ( tile.tileNum == 3 && (destinationTileNum ==  2 || destinationTileNum ==  9 || destinationTileNum == 10 || destinationTileNum == 11 || destinationTileNum ==  4 ) )
                        || ( tile.tileNum == 4 && (destinationTileNum ==  3 || destinationTileNum == 10 || destinationTileNum == 11 || destinationTileNum == 12 || destinationTileNum ==  5 ) )
                        || ( tile.tileNum == 5 && (destinationTileNum ==  4 || destinationTileNum == 11 || destinationTileNum == 12 || destinationTileNum == 13 || destinationTileNum ==  6 ) ) ) {

                        if(tile.type == selectedType && tile.tileNum != selectedNum) {
                            outcome = true;
                        }
                    } else { // check rest of board
                        if (
                           tile.tileNum == destinationTileNum - 6
                        || tile.tileNum == destinationTileNum - 7
                        || tile.tileNum == destinationTileNum - 8
                        || tile.tileNum == destinationTileNum - 1
                        || tile.tileNum == destinationTileNum + 1
                        || tile.tileNum == destinationTileNum + 6
                        || tile.tileNum == destinationTileNum + 7
                        || tile.tileNum == destinationTileNum + 8) {
                            if (tile.type == selectedType && tile.tileNum != selectedNum) {
                                outcome = true;
                            }
                        }
                    }

                    break;

                default: break;
            }
        }



        return outcome;
    }

    private ArrayList<Tile> tile; // Single array of tiles, instead of multidimensional



    private int tileWidth;

    private int tileHeight;

    private int whiteSpace;

    private int headSpace; // This accounts for the header column numbers
    private int leftSpace; // This accounts for the left column numbers

    private int screenWidth = 480;//Gdx.graphics.getWidth();
    private int screenHeight = 800;//Gdx.graphics.getHeight();

    // All the textures we use
    private Texture tileBlankImage;
    private Texture[] tilePlanetImage;
    private TextureRegion[] tilePlanetRegion;
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

    public float blackHoleRotation;

    public PlayingScreen(GravityGrid game) {

        this.game = game;

        fingerOnScreen = false;
        readyForInput = true;

        bgRand = MathUtils.random(0,3);

        blackHoleRotation = 0.0f;

        // Initialize the required color values and the max moves, which are specific elements in the gravityGridLevel array
        thisLevelRedNeeded = game.gravityGridLevel[game.currentLevel][49];
        thisLevelBlueNeeded = game.gravityGridLevel[game.currentLevel][50];
        thisLevelGreenNeeded = game.gravityGridLevel[game.currentLevel][51];
        thisLevelMaxMoves = game.gravityGridLevel[game.currentLevel][52];
        thisLevelCurrentRedTotal = 0;
        thisLevelCurrentBlueTotal = 0;
        thisLevelCurrentGreenTotal = 0;

        thisLevelCurrentMoves = 0; // Keep track of how many moves we've taken

        theGameState = gameState.READY;

        finger = new Rectangle();
        finger.x = -1;
        finger.y = -1;
        finger.width = 0;
        finger.height = 0;

        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 480, 800);

        // The amount of space we want for the top of the grid where the numbers will be displayed.
        // This comes out to one half of the width (or height) of one tile.
        this.headSpace = (int)(0.5*(screenWidth/7));

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
            }
            worldCol = 0; // Reset column counter
            worldRow++; // Iterate our row counter
        }

        // Load the sounds before the textures so the assetmanager isn't busy by the time we're clicking things
        //restartLevelSound = Gdx.audio.newSound(Gdx.files.internal("startup.wav"));
        tileSelectSound = Gdx.audio.newSound(Gdx.files.internal("consoleBeep.wav"));
        tileDeselectSound = Gdx.audio.newSound(Gdx.files.internal("tileDeselectSound.wav"));
        goodMoveAttemptSound = Gdx.audio.newSound(Gdx.files.internal("goodMoveAttempt.ogg"));
        restartLevelSound = goodMoveAttemptSound;
        cannotMoveSound = Gdx.audio.newSound(Gdx.files.internal("cannotMoveSound.wav"));
        outOfMovesSound = Gdx.audio.newSound(Gdx.files.internal("outOfMovesSound.wav"));
        levelCompleteSound = Gdx.audio.newSound(Gdx.files.internal("levelCompleteSound.wav"));



        // Load the textures
        tileBlankImage = new Texture(Gdx.files.internal("tileBlankImage.png"));
        tilePlanetImage = new Texture[4]; // remember: [4] = [0,1,2,3].
        tilePlanetRegion = new TextureRegion[4];
        tilePlanetImage[0] = new Texture(Gdx.files.internal("planet0.png"));
        tilePlanetRegion[0] = new TextureRegion(tilePlanetImage[0]);
        tilePlanetImage[1] = new Texture(Gdx.files.internal("planet1.png"));
        tilePlanetRegion[1] = new TextureRegion(tilePlanetImage[1]);
        tilePlanetImage[2] = new Texture(Gdx.files.internal("planet2.png"));
        tilePlanetRegion[2] = new TextureRegion(tilePlanetImage[2]);
        tilePlanetImage[3] = new Texture(Gdx.files.internal("planet3.png"));
        tilePlanetRegion[3] = new TextureRegion(tilePlanetImage[3]);
        tileSunImage = new Texture(Gdx.files.internal("sun.png"));
        tileSunRegion = new TextureRegion(tileSunImage);
        tileSunFlareImage = new Texture(Gdx.files.internal("sunflare0.png"));
        tileSunFlareRegion = new TextureRegion(tileSunFlareImage);
        tileAsteroidImage = new Texture[4];
        tileAsteroidImage[0] = new Texture(Gdx.files.internal("asteroid0.png"));
        tileAsteroidImage[1] = new Texture(Gdx.files.internal("asteroid1.png"));
        tileAsteroidImage[2] = new Texture(Gdx.files.internal("asteroid2.png"));
        tileAsteroidImage[3] = new Texture(Gdx.files.internal("asteroid3.png"));
        backgroundImage = new Texture[4];
        backgroundImage[0] = new Texture(Gdx.files.internal("bg0.png"));
        backgroundImage[1] = new Texture(Gdx.files.internal("bg1.png"));
        backgroundImage[2] = new Texture(Gdx.files.internal("bg2.png"));
        backgroundImage[3] = new Texture(Gdx.files.internal("bg3.png"));

        singularityImage = new Texture(Gdx.files.internal("singularity0.png"));

        tileValueImage = new Texture[11];
        tileValueImage[0] = new Texture(Gdx.files.internal("tile0.png"));
        tileValueImage[1] = new Texture(Gdx.files.internal("tile1.png"));
        tileValueImage[2] = new Texture(Gdx.files.internal("tile2.png"));
        tileValueImage[3] = new Texture(Gdx.files.internal("tile3.png"));
        tileValueImage[4] = new Texture(Gdx.files.internal("tile4.png"));
        tileValueImage[5] = new Texture(Gdx.files.internal("tile5.png"));
        tileValueImage[6] = new Texture(Gdx.files.internal("tile6.png"));
        tileValueImage[7] = null; // We don't actually use this value
        tileValueImage[8] = new Texture(Gdx.files.internal("tile8.png"));
        tileValueImage[9] = null; // We don't actually use this value
        tileValueImage[10] = new Texture(Gdx.files.internal("tile10.png"));
        tileOverlayImage = new Texture[7];
        tileOverlayRegion = new TextureRegion[7];
        tileOverlayImage[0] = new Texture(Gdx.files.internal("tileOverlayAnim0.png"));
        tileOverlayRegion[0] = new TextureRegion(tileOverlayImage[0]);
        tileOverlayImage[1] = new Texture(Gdx.files.internal("tileOverlayAnim1.png"));
        tileOverlayRegion[1] = new TextureRegion(tileOverlayImage[1]);
        tileOverlayImage[2] = new Texture(Gdx.files.internal("tileOverlayAnim2.png"));
        tileOverlayRegion[2] = new TextureRegion(tileOverlayImage[2]);
        tileOverlayImage[3] = new Texture(Gdx.files.internal("tileOverlayAnim3.png"));
        tileOverlayRegion[3] = new TextureRegion(tileOverlayImage[3]);
        tileOverlayImage[4] = new Texture(Gdx.files.internal("tileOverlayAnim4.png"));
        tileOverlayRegion[4] = new TextureRegion(tileOverlayImage[4]);
        tileOverlayImage[5] = new Texture(Gdx.files.internal("tileOverlayAnim5.png"));
        tileOverlayRegion[5] = new TextureRegion(tileOverlayImage[5]);
        tileOverlayImage[6] = new Texture(Gdx.files.internal("tileOverlayAnim6.png"));
        tileOverlayRegion[6] = new TextureRegion(tileOverlayImage[6]);
        buttonFailImage = new Texture(Gdx.files.internal("buttonFail.png"));
        buttonLevelCompleteImage = new Texture(Gdx.files.internal("buttonLevelComplete.png"));

        blackHoleImage = new Texture(Gdx.files.internal("galaxyOverlay.png"));
        blackHoleRegion = new TextureRegion(blackHoleImage);

        // Call this once before the level starts so that we have some initial values
        updateCurrentLevelValueTotals();

    }

    public void RestartLevel() {

        // This also works as a level changer because we can increment the game.currentLevel and then call RestartLevel() which reloads everything,
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
            }
            worldCol = 0; // Reset column counter
            worldRow++; // Iterate our row counter
        }

        // Reset the variables 
        thisLevelCurrentMoves = 0;
        updateCurrentLevelValueTotals();
        theGameState = gameState.READY;

        restartLevelSound.play();
    }

    public void OutOfMoves() {

    }

    public void LevelComplete() {

    }

    public void PlayLevel() {

        // If we are out of dark matter, then ensure that we update our game state appropriately
        // Forcing the gamestate here should ensure that the rest of the screen renders but does not
        // process input. 
        if(game.darkMatterCount <= 0) {
            theGameState = gameState.OUT_OF_LIVES;
        }

        if(Gdx.input.isTouched() ) {

            Vector3 finger = new Vector3();
            camera.unproject(finger.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            if(readyForInput) {
                // Only process input if the game state is READY
                if(theGameState == gameState.READY) {

                    //game.font.setColor(1f,1f,1f,1f); 
                    //game.font.draw(game.batch, ""+finger.x + ","+finger.y, 240,30); 

                    // If we touched the screen during READY state, check to see if we touched a planet.
                    // If we did, mark it as selected.
                    // The `checkTiles` below is how you add a break in Java.
                    checkTiles:
                    for (Tile tile : this.tile) {
                        // We only care about tiles that are planets. Anything else can't be touched (for now).
                        // TODO: Add logic for handling input of MENU and HELP buttons outside of the checkTiles section below.
                        if(tile.type == TileType.REDPLANET || tile.type == TileType.BLUEPLANET || tile.type == TileType.GREENPLANET) {
                            if(pointInRectangle(tile.rect, finger.x, finger.y)) {

                                // DEBUG:
                                //game.font.setColor(1f,1f,1f,1f); 
                                //game.font.draw(game.batch, "Tile "+tile.value, 240,60); 

                                // if game state is ready and tile status is none, that means we haven't tried to move yet. 
                                if(tile.status == TileStatus.NONE) {
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
                    // TODO: See above. Add logic here to check for UI element presses
                }
            }


            // To make sure we are not "repeat key"ing, check for ready for input

            if(readyForInput == true) {
                if(theGameState == gameState.TILE_SELECTED) {

                    // If we touched the screen during a TILE_SELECTED state, check to see if we touched a type.NONE&&status.NONE
                    // If we did, then find the SELECTED tile's type and move it.

                    // First check to see if where we are going is indeed go-able
                    markDestinationTile:
                    for (Tile tile : this.tile) {
                        // Did we collide with this tile? 
                        if(pointInRectangle(tile.rect, finger.x, finger.y)) {

                            // Is this tile blank?
                            if(tile.type == TileType.NONE && tile.status == TileStatus.NONE) {

                                // This is a potential tile to move to. Check if we're touching it

                                // Mark this tile as the move destination
                                tile.status = TileStatus.MOVETOHERE;

                                // Update game state so we know that both SELECTED and MOVETOHERE have been set.
                                theGameState = gameState.GOOD_MOVE_ATTEMPT;
                                tileSelectSound.play();
                                readyForInput = false;
                                break markDestinationTile;



                            } else if(tile.status == TileStatus.SELECTED) {

                                // We clicked a tile that was marked as selected, so deselect it
                                tile.status = TileStatus.NONE;

                                // And reset the game state
                                theGameState = gameState.READY;

                                tileDeselectSound.play();


                                readyForInput = false;
                                break markDestinationTile;

                            // Don't need the below, since the else {} will cover everyone else
                            // } else if(tile.type == TileType.REDPLANET || tile.type == //TileType.BLUEPLANET || tile.type == TileType.GREENPLANET) {

                            } else {	// This should cover PLANET, ASTEROID, SUN, and BLOCKED types. (anyone not BLANK)
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


                                    if(	thisLevelRedNeeded == thisLevelCurrentRedTotal &&
                                            thisLevelBlueNeeded == thisLevelCurrentBlueTotal &&
                                            thisLevelGreenNeeded == thisLevelCurrentGreenTotal) {
                                        theGameState = gameState.LEVEL_COMPLETE;
                                        levelCompleteSound.play();

                                    } else {
                                        // We have not beaten this level, so let's make sure we're not out of moves
                                        if(thisLevelCurrentMoves == thisLevelMaxMoves) {
                                            theGameState = gameState.OUT_OF_MOVES;

                                            outOfMovesSound.play();

                                        } else {
                                            // We haven't beaten the level && we haven't maxed out our moves, so
                                            // go back to ready
                                            theGameState = gameState.READY;
                                        }
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

                                    break moveSelectedTile;
                                }
                            }
                        }
                    }

                    // TODO: How would you error-check this?
                }

            }
        }

        // First draw the background image
        game.batch.setColor(1f,1f,1f,.25f);
        game.batch.draw(backgroundImage[bgRand], 0,0,screenWidth, screenHeight);

        int cellNumber = 0;

        // Loop through tiles and draw them
        for (Tile tile : this.tile) {

            // Always draw the tile border
            game.batch.setColor(1f,1f,1f,1f);
            game.batch.draw(tileBlankImage, tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);

            // (22-Aug-2015 Jesse) Sami had an idea to have the planets sized according to their
            // tile value. I tried this out a few times but the planets changing sizes continuously 
            // just confused me. So I'm going to hard-set this to 1.0f, but keep the variable here
            // just in case we decide to do something crazy later. 
            //float sizeMultiplier = ( ((float)tile.value/10.0f) > 0.5f ? (float)tile.value/10.0f : 0.8f); // create a multiplier for this planet
            float sizeMultiplier = 1.0f;								// size based on tile value. 

            // First draw the type of tile it is and the value of the tile if it's a planet
            switch(tile.type) {
                case NONE:
                    //game.font.setColor(1f,1f,1f,0.5f);
                    //game.font.draw(game.batch, ""+tile.value, tile.rect.x+18, tile.rect.y+38);
                    break;
                case REDPLANET:
                    game.batch.setColor(game.colorRed);
                    game.batch.draw(tilePlanetRegion[tile.rand], tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, sizeMultiplier, sizeMultiplier, 0.0f);

                    //game.batch.draw(tilePlanetImage[tile.rand], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    game.batch.setColor(1f,1f,1f,1f);
                    game.batch.draw(tileValueImage[tile.value], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    break;
                case BLUEPLANET:
                    game.batch.setColor(game.colorBlue);
                    game.batch.draw(tilePlanetRegion[tile.rand], tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, sizeMultiplier, sizeMultiplier, 0.0f);
                    game.batch.setColor(1f,1f,1f,1f);
                    game.batch.draw(tileValueImage[tile.value], tile.rect.x, tile.rect.y, tile.rect.width, tile.rect.height);
                    break;
                case GREENPLANET:
                    game.batch.setColor(game.colorGreen);
                    game.batch.draw(tilePlanetRegion[tile.rand], tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, sizeMultiplier, sizeMultiplier, 0.0f);
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

                    // When a square is selected we'll superimpose the first frame of the overlay and spin it.
                    // Update the tuner rotation and draw the tuner
                    if(tile.overlayRotation >= 359f) {
                        tile.overlayRotation = 0.0f;
                    }

                    game.batch.setColor(game.colorYellow);
                    game.batch.draw(tileOverlayRegion[0], tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 1.0f, 1.0f, tile.overlayRotation);

                    // Rotate that bad boy
                    tile.overlayRotation += 3.0f;

                    //game.font.setColor(1f,1f,1f,0.5f);
                    //game.font.draw(game.batch, "Tile Selected!", 0,30);

                    break;

                case MOVECOMPLETE:

                    // Move complete means that we should increment our frame counter and set the texture
                    // of the appropriate frame
                    // If our frame counter gets high enough (i.e., we're done with the animation, we'll set
                    // the tile status to be NONE

                    // If we still have a frame in our animation
                    if(tile.overlayFrameNumber < 6) {



                        game.batch.setColor(game.colorYellow);

                        game.batch.draw(tileOverlayRegion[tile.overlayFrameNumber], tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 1.5f*(.5f*tile.overlayFrameNumber), 1.5f*(.5f*tile.overlayFrameNumber), 0.0f);

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

                    // CANNOTMOVE means that we tried to move to a tile that was outside the bounds of our movement rules.
                    // See function canMoveAccordingToRules() for more details on what causes this to trigger.

                    // If we still have a frame in our animation
                    if(tile.overlayFrameNumber < 6) {

                        //game.batch.setColor(game.colorRed);
                        //game.font.draw(game.batch, "TileNum="+tile.tileNum, 50,600); 
                        game.batch.draw(tileOverlayRegion[tile.overlayFrameNumber], tile.rect.x, tile.rect.y, tile.rect.width/2, tile.rect.height/2, tile.rect.width, tile.rect.height, 1.0f, 1.0f, 0.0f);

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

                default: break;
            }

            // Check if the status is 

            cellNumber++;
        }


        // Last but not least, draw any UI elements (or ads if you're an asshat [hint: don't be an asshat, Jesse])

        // Draw the row and col headers
        game.font.setColor(1f,1f,1f,1.0f);
        int s = (int)(0.5*leftSpace);
        int s2 = (int)(0.5*tileWidth) + s;
        game.font.draw(game.batch, "0", s, whiteSpace+(tileHeight*6));
        game.font.draw(game.batch, "1", s, whiteSpace+(tileHeight*5));
        game.font.draw(game.batch, "3", s, whiteSpace+(tileHeight*4));
        game.font.draw(game.batch, "5", s, whiteSpace+(tileHeight*3));
        game.font.draw(game.batch, "3", s, whiteSpace+(tileHeight*2));
        game.font.draw(game.batch, "1", s, whiteSpace+(tileHeight*1));
        game.font.draw(game.batch, "0", s, whiteSpace+(tileHeight*0));
        game.font.draw(game.batch, "0", leftSpace + s + (tileWidth*6), screenHeight - whiteSpace - s2);
        game.font.draw(game.batch, "1", leftSpace + s + (tileWidth*5), screenHeight - whiteSpace - s2);
        game.font.draw(game.batch, "3", leftSpace + s + (tileWidth*4), screenHeight - whiteSpace - s2);
        game.font.draw(game.batch, "5", leftSpace + s + (tileWidth*3), screenHeight - whiteSpace - s2);
        game.font.draw(game.batch, "3", leftSpace + s + (tileWidth*2), screenHeight - whiteSpace - s2);
        game.font.draw(game.batch, "1", leftSpace + s + (tileWidth*1), screenHeight - whiteSpace - s2);
        game.font.draw(game.batch, "0", leftSpace + s + (tileWidth*0), screenHeight - whiteSpace - s2);
		
		/*
		Level %num%: %name%
		#/# | #/# | #/#
		*/

        game.font.setColor(1f,1f,1f,1f);
        game.font.draw(game.batch, "Level "+(game.currentLevel+1)+": "+game.levelName[game.currentLevel], 5, 771);
        game.font.setColor(game.colorRed);
        game.font.draw(game.batch, "<"+thisLevelCurrentRedTotal+"/"+thisLevelRedNeeded+">", 5, 747);
        game.font.setColor(game.colorBlue);
        game.font.draw(game.batch, "<"+thisLevelCurrentBlueTotal+"/"+thisLevelBlueNeeded+">", 101, 747);
        game.font.setColor(game.colorGreen);
        game.font.draw(game.batch, "<"+thisLevelCurrentGreenTotal+"/"+thisLevelGreenNeeded+">", 197, 747);
        game.font.setColor(1f,1f,1f,1f);
        game.font.draw(game.batch, "Moves Left: "+(thisLevelMaxMoves - thisLevelCurrentMoves), 5, 727);
        game.font.setColor(1f,0f,1f,1f);
        game.font.draw(game.batch, "ALPHA RELEASE - THANKS FOR HELPING!", 5, 707);

        // Display the dark matter (lives) at the top of the screen
        if(game.darkMatterCount > 0) {
            game.batch.setColor(1f,1f,1f,1f);
        } else { game.batch.setColor(1f,1f,1f,0.5f); }
        game.batch.draw(singularityImage, (screenWidth/2) -16 -2 -32 -2 -32,767,32,32);

        if(game.darkMatterCount > 1) {
            game.batch.setColor(1f,1f,1f,1f);
        } else { game.batch.setColor(1f,1f,1f,0.5f); }
        game.batch.draw(singularityImage, (screenWidth/2) - 16 -2 -32,767,32,32);

        if(game.darkMatterCount > 2) {
            game.batch.setColor(1f,1f,1f,1f);
        } else { game.batch.setColor(1f,1f,1f,0.5f); }
        game.batch.draw(singularityImage, (screenWidth/2) -16,767,32,32);

        if(game.darkMatterCount > 3) {
            game.batch.setColor(1f,1f,1f,1f);
        } else { game.batch.setColor(1f,1f,1f,0.5f); }
        game.batch.draw(singularityImage, (screenWidth/2) +16 +2,767,32,32);

        if(game.darkMatterCount > 4) {
            game.batch.setColor(1f,1f,1f,1f);
        } else { game.batch.setColor(1f,1f,1f,0.5f); }
        game.batch.draw(singularityImage, (screenWidth/2) +16 +2 +32 +2,767,32,32);

        // Display the level message, if we have one, and only if the gameState is ready or tile selected or good move attempt
        if(theGameState == gameState.READY || theGameState == gameState.TILE_SELECTED || theGameState == gameState.GOOD_MOVE_ATTEMPT) {
            game.font.setColor(1f,0f,1f,1f);
            game.font.draw(game.batch, ""+game.levelMessage[game.currentLevel], 0, 100, screenWidth, 1, true);
            game.font.setColor(1f,1f,1f,1f);  //reset the font color to white
        }



        // If we have less than 5 lives, display our timer
        if(game.darkMatterCount < 5) {
            long diff = game.darkMatterCooldown - game.timerElapsedTime;
            game.font.draw(game.batch, ""+game.toPrettyDate(diff)+" > ", 5, 800);
        }

        // Another UI element is the button that goes to another level. This is only displayed if 
        // the gamestate is LEVEL_COMPLETE. Alternatively, if we're OUT_OF_MOVES, we'll display a "Restart Level" button.
        if(theGameState == gameState.OUT_OF_MOVES) {
            game.batch.setColor(1f,1f,1f,1f);
            game.batch.draw(buttonFailImage, 0, 0, 480, this.whiteSpace);

            if(Gdx.input.isTouched()) {
                RestartLevel();
                // Also decrement a life
                game.darkMatterCount--;

            }
        }

        if(theGameState == gameState.LEVEL_COMPLETE) {
            game.batch.setColor(1f,1f,1f,1f);
            game.batch.draw(buttonLevelCompleteImage, 0, 0, 480, this.whiteSpace);

            if(Gdx.input.isTouched()) {
                // This doesn't work because the restartlevel uses the same Tile()s as the last level. We need to completely rebuild this screen. 
                game.markLevelAsComplete();
                RestartLevel();
                bgRand = MathUtils.random(0,3); // Force new background image
            }
        }

        // Also check to see if we're out of lives 
        if(theGameState == gameState.OUT_OF_LIVES) {

            game.batch.setColor(1f,0f,0f,0.8f);
            game.batch.draw(backgroundImage[0], 0, 0, screenWidth, screenHeight);
            game.batch.setColor(1f,1f,1f,1f);


            // Display a rotating blackhole over our screen 
            game.batch.setColor(1f,1f,1f,1f);
            //game.batch.draw(blackHoleRegion, 0, (screenHeight/2)-200, 200, 200, screenWidth,(screenHeight/2)+200, 3.0f, 3.0f, blackHoleRotation);
            game.batch.draw(blackHoleRegion, 0, 0, 200, 200, 400,400, 3.0f, 3.0f, blackHoleRotation);



            // Update the blackHoleRotation
            if(blackHoleRotation <= 354.0f){
                blackHoleRotation += 6.0f;
            }

            if(blackHoleRotation >=360.0f) {
                blackHoleRotation = 0.0f;
            }

            // For libgdx, the [1, true] part at the end means "Center the text" (1) and "wrap to screenWidth" (true)
			/*game.font.draw(game.batch, "You're Out of Dark Matter!\nSince this is the FREE version of Gravity Grid, each time you fail a level you spend one Dark Matter point. When you've used up all of your five total Dark Matter points, you have to wait for new Dark Matter to recharge.\nNew dark matter in:\n"+(game.toPrettyDate(game.darkMatterCooldown - game.timerElapsedTime))+"\n\nIf you just want to play the puzzles without all this Dark Matter stuff, the $1.99 version of this game has zero ads, zero in-app purchases, and zero Dark Matter.", 0, (screenHeight/2)+140, screenWidth, 1, true);
			*/
            game.font.draw(game.batch, "You're Out of Dark Matter!\nIn the free version of Gravity Grid, each time you fail a level you spend one Dark Matter point. When you've used up all of your five total Dark Matter points, you have to wait for a new Dark Matter to recharge (every twenty minutes).\nNew dark matter in:\n"+(game.toPrettyDate(game.darkMatterCooldown - game.timerElapsedTime))+"\n\nSince I don't like in-app purchases and intrusive ads, this was how I gently encourage people who like the game to purchase the full version. If this is a bad idea, please let me know!\n\nAnd thank you for participating in the alpha release!", 0, (screenHeight/2)+180, screenWidth, 1, true);


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
				game.font.draw(game.batch, "READY", 0, 250); 
				break;
			case TILE_SELECTED:
				game.font.draw(game.batch, "TILE_SELECTED", 0, 250); 
				break;
			case GOOD_MOVE_ATTEMPT:
				game.font.draw(game.batch, "GOOD_MOVE_ATTEMPT", 0, 250); 
				break;
			case LEVEL_COMPLETE:
				game.font.draw(game.batch, "LEVEL_COMPLETE", 0, 250); 
				break;
			case OUT_OF_MOVES:
				game.font.draw(game.batch, "OUT_OF_MOVES", 0, 250); 
				break;
			
			default:
				break;
		}*/

        // Switch the gamestate to ensure that we are not OUT_OF_MOVES or LEVEL_COMPLETE

        PlayLevel();

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
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        tileBlankImage.dispose();
        tilePlanetImage[0].dispose();
        tilePlanetImage[1].dispose();
        tilePlanetImage[2].dispose();
        tilePlanetImage[3].dispose();

        tileSunImage.dispose();
        tileSunFlareImage.dispose();

        tileAsteroidImage[0].dispose();
        tileAsteroidImage[1].dispose();
        tileAsteroidImage[2].dispose();
        tileAsteroidImage[3].dispose();

        backgroundImage[0].dispose();
        backgroundImage[1].dispose();
        backgroundImage[2].dispose();
        backgroundImage[3].dispose();

        tileValueImage[0].dispose();
        tileValueImage[1].dispose();
        tileValueImage[2].dispose();
        tileValueImage[3].dispose();
        tileValueImage[4].dispose();
        tileValueImage[5].dispose();
        tileValueImage[6].dispose();
        tileValueImage[8].dispose();
        tileValueImage[10].dispose();

        tileOverlayImage[0].dispose();
        tileOverlayImage[1].dispose();
        tileOverlayImage[2].dispose();
        tileOverlayImage[3].dispose();
        tileOverlayImage[4].dispose();
        tileOverlayImage[5].dispose();
        tileOverlayImage[6].dispose();

        buttonLevelCompleteImage.dispose();
        buttonFailImage.dispose();

        singularityImage.dispose();
        blackHoleImage.dispose();

        tileSelectSound.dispose();
        tileDeselectSound.dispose();
        goodMoveAttemptSound.dispose();
        cannotMoveSound.dispose();
        outOfMovesSound.dispose();
        levelCompleteSound.dispose();
        restartLevelSound.dispose();

    }

}