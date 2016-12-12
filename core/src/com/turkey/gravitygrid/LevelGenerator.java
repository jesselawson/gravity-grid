/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

/**
 * Created by lawsonje on 12/8/2016.
 */

import java.util.ArrayList;
import com.badlogic.gdx.math.MathUtils;

public class LevelGenerator {

    public boolean debug = true;

    /* Board components. Helps differentiate between edges/corners and middle of board. */
    private static final int[] boardMiddle = new int[] { 36,37,38,39,40,29,30,31,32,33,22,23,24,25,26,15,16,17,18,19,8,9,10,11,12 };
    private static final int[] boardEdge = new int[] { 42,43,44,45,46,47,48,35,28,21,14,7,0,1,2,3,4,5,6,13,20,27,34,41 };

    // IntArrayContains is used specifically for the above boardMiddle and boardEdge arrays and in the canMoveAccordingToLogic function
    private boolean IntArrayContains(int[] list, int number) {

        for(int i=0; i<list.length; i++) {
            if(list[i] == number) {
                return true;
            }
        }
        return false;
    }

    // This array holds the values of each tile
    private static final int[] tileValueTable = new int[] { 0,1,3,5,3,1,0,1,2,4,6,4,2,1,3,4,6,8,6,4,3,5,6,8,10,8,6,5,3,4,6,8,6,4,3,1,2,4,6,4,2,1,0,1,3,5,3,1,0 };

    private enum TileType {
        NONE, REDPLANET, BLUEPLANET, GREENPLANET, ASTEROID, SUN, BLOCKED
    }

    private class Tile {
        public TileType type;
        boolean isSelected;

        public int tileNum;
        public int value;

        public Tile(int value, int tileNum) {
            this.type = TileType.NONE; // default
            this.value = value;
            this.tileNum = tileNum;
            isSelected = false;
        }
    }

    public boolean GoodMoveVerified(ArrayList<Tile> tile, int tileToCheck, TileType selectedType, int selectedNum) {
        if( tile.get(tileToCheck).type == selectedType && tile.get(tileToCheck).tileNum != selectedNum ) {
            if(debug){
                System.out.println("(3/4): Good tile "+tileToCheck+" found for selected num "+selectedNum);
            }
            return true;
        } else {
            return false;
        }
    }

    public boolean canMoveAccordingToRules(ArrayList<Tile> tile, int destinationTileNum) {

        // First determine the select tile's tileNum so we can get it's TileType
        TileType selectedType = TileType.NONE; // init'd
        int selectedNum = 0; // init'd

        // This `outcome` is what is returned by this function. If outcome = true, then the function has determined
        // that the tile in question "can move according to rules"
        boolean outcome = false;

        // Loop through tiles and find the selected one
        findSelected:
        for (Tile theTile : tile) {
            // Check for selected tile
            if (theTile.isSelected) {
                // Extract the type of the selected tile so we can assign the appropriate rules to the planet type
                selectedType = theTile.type;
                selectedNum = theTile.tileNum;
                if(debug){
                    System.out.println("(2/4): canMoveAccordingToRules: Tile #"+selectedNum+" ("+selectedType+") is selected. Trying to move to "+destinationTileNum+"...");
                }
                break findSelected;
            }
        }
        // Now we're in the meat and potatoes. The function seeks to answer: can a tile of type selectedType move to
        // the destinationTileNum type?

        switch (selectedType) {

            case REDPLANET:

                // First check the body of the board. If the body doesn't return true, try the edges and corners (the most complicated of the logic because we check them each individually).
                if (IntArrayContains(boardMiddle, destinationTileNum)) {
                    // Check the middle section of the board (i.e., all tiles except edges and corners)
                    if (
                            (GoodMoveVerified(tile,destinationTileNum + 8, selectedType, selectedNum))
                                    || (GoodMoveVerified(tile,destinationTileNum + 6, selectedType, selectedNum))
                                    || (GoodMoveVerified(tile,destinationTileNum - 6, selectedType, selectedNum))
                                    || (GoodMoveVerified(tile,destinationTileNum - 8, selectedType, selectedNum))) {
                        outcome = true;
                        if(debug){
                            System.out.println("destinationTileNum: " + destinationTileNum + ". SelectedNum: " + selectedNum);
                        }
                        return outcome; // Leave this function immediately!
                    }

                } else {
                    // If we're here, then we're on an edge or corner tile. We need to check these manually... Previous attempts at complicated if-else statements have proven unwieldy and confusing.
                    // So why not this switch in a switch?

                    switch (destinationTileNum) {
                        case 42: outcome = GoodMoveVerified(tile,36,selectedType,selectedNum); break;
                        case 43:
                        case 44:
                        case 45:
                        case 46:
                        case 47: outcome = (GoodMoveVerified(tile,destinationTileNum-6,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum-8,selectedType,selectedNum)); break;
                        case 48: outcome = GoodMoveVerified(tile,40,selectedType,selectedNum); break;
                        case 41:
                        case 34:
                        case 27:
                        case 20:
                        case 13: outcome = (GoodMoveVerified(tile,destinationTileNum+6,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum-8,selectedType,selectedNum)); break;
                        case  6: outcome = GoodMoveVerified(tile,12,selectedType,selectedNum); break;
                        case  5:
                        case  4:
                        case  3:
                        case  2:
                        case  1: outcome = (GoodMoveVerified(tile,destinationTileNum+6,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum+8,selectedType,selectedNum)); break;
                        case  0: outcome = GoodMoveVerified(tile,8,selectedType,selectedNum); break;
                        case  7:
                        case 14:
                        case 21:
                        case 28:
                        case 35: outcome = (GoodMoveVerified(tile,destinationTileNum+8,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum-6,selectedType,selectedNum)); break;
                        default: break;
                    }

                    return outcome; // Break immediately because there's no point in continuing
                }

                break;
            case BLUEPLANET:
                // First check the body of the board. If the body doesn't return true, try the edges and corners (the most complicated of the logic because we check them each individually).
                if (IntArrayContains(boardMiddle, destinationTileNum)) {
                    // Check the middle section of the board (i.e., all tiles except edges and corners)
                    if (
                            (GoodMoveVerified(tile,destinationTileNum + 1, selectedType, selectedNum))
                                    || (GoodMoveVerified(tile,destinationTileNum - 1, selectedType, selectedNum))
                                    || (GoodMoveVerified(tile,destinationTileNum + 7, selectedType, selectedNum))
                                    || (GoodMoveVerified(tile,destinationTileNum - 7, selectedType, selectedNum))) {
                        outcome = true;
                        if(debug){
                            System.out.println("destinationTileNum: " + destinationTileNum + ". SelectedNum: " + selectedNum);
                        }
                        return outcome; // Leave this function immediately!
                    }

                } else {
                    // If we're here, then we're on an edge or corner tile. We need to check these manually... Previous attempts at complicated if-else statements have proven unwieldy and confusing.
                    // So why not this switch in a switch?

                    switch (destinationTileNum) {
                        case 42: outcome = ( GoodMoveVerified(tile,35,selectedType,selectedNum) || GoodMoveVerified(tile,43,selectedType,selectedNum) ); break;
                        case 43:
                        case 44:
                        case 45:
                        case 46:
                        case 47: outcome = (GoodMoveVerified(tile,destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum-7,selectedType,selectedNum)); break;
                        case 48: outcome = ( GoodMoveVerified(tile,47,selectedType,selectedNum) || GoodMoveVerified(tile,41,selectedType,selectedNum) ); break;
                        case 41:
                        case 34:
                        case 27:
                        case 20:
                        case 13: outcome = (GoodMoveVerified(tile,destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum-7,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum+7,selectedType,selectedNum)); break;
                        case  6: outcome = ( GoodMoveVerified(tile,5,selectedType,selectedNum) || GoodMoveVerified(tile,13,selectedType,selectedNum) ); break;
                        case  5:
                        case  4:
                        case  3:
                        case  2:
                        case  1: outcome = (GoodMoveVerified(tile,destinationTileNum-1,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum+7,selectedType,selectedNum)); break;
                        case  0: outcome = ( GoodMoveVerified(tile,7,selectedType,selectedNum) || GoodMoveVerified(tile,1,selectedType,selectedNum) ); break;
                        case  7:
                        case 14:
                        case 21:
                        case 28:
                        case 35: outcome = (GoodMoveVerified(tile,destinationTileNum-7,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum+1,selectedType,selectedNum) || GoodMoveVerified(tile,destinationTileNum+7,selectedType,selectedNum)); break;
                        default: break;
                    }

                    return outcome; // Break immediately because there's no point in continuing
                }

                break;

            case GREENPLANET:
                // First check the body of the board. If the body doesn't return true, try the edges and corners (the most complicated of the logic because we check them each individually).
                if (IntArrayContains(boardMiddle, destinationTileNum)) {
                    // Check the middle section of the board (i.e., all tiles except edges and corners)
                    if (    // For green planets, it's left, right, and all three on top and three on bottom
                            !GoodMoveVerified(tile,destinationTileNum - 1, selectedType, selectedNum)
                                    && !GoodMoveVerified(tile,destinationTileNum - 8, selectedType, selectedNum)
                                    && !GoodMoveVerified(tile,destinationTileNum - 7, selectedType, selectedNum)
                                    && !GoodMoveVerified(tile,destinationTileNum - 6, selectedType, selectedNum)
                                    && !GoodMoveVerified(tile,destinationTileNum + 1, selectedType, selectedNum)
                                    && !GoodMoveVerified(tile,destinationTileNum + 6, selectedType, selectedNum)
                                    && !GoodMoveVerified(tile,destinationTileNum + 7, selectedType, selectedNum)
                                    && !GoodMoveVerified(tile,destinationTileNum + 8, selectedType, selectedNum)
                            ) {
                        outcome = true;
                        if(debug){
                            System.out.println("destinationTileNum: " + destinationTileNum + ". SelectedNum: " + selectedNum);
                        }
                        return outcome; // Leave this function immediately!
                    }

                } else {
                    // If we're here, then we're on an edge or corner tile. We need to check these manually... Previous attempts at complicated if-else statements have proven unwieldy and confusing.
                    // So why not this switch in a switch?

                    switch (destinationTileNum) {
                        case 42: outcome = ( !GoodMoveVerified(tile,35,selectedType,selectedNum) && !GoodMoveVerified(tile,43,selectedType,selectedNum) && !GoodMoveVerified(tile,36,selectedType,selectedNum) ); break;
                        case 43:
                        case 44:
                        case 45:
                        case 46:
                        case 47: outcome = (!GoodMoveVerified(tile,destinationTileNum-1,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum+1,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-6,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-7,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-8,selectedType,selectedNum)); break;
                        case 48: outcome = ( !GoodMoveVerified(tile,47,selectedType,selectedNum) && !GoodMoveVerified(tile,40,selectedType,selectedNum) && !GoodMoveVerified(tile,41,selectedType,selectedNum)); break;
                        case 41:
                        case 34:
                        case 27:
                        case 20:
                        case 13: outcome = ( !GoodMoveVerified(tile,destinationTileNum+6,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum+7,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-1,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-6,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-7,selectedType,selectedNum)); break;
                        case  6: outcome = ( !GoodMoveVerified(tile,5,selectedType,selectedNum) && !GoodMoveVerified(tile,12,selectedType,selectedNum) && !GoodMoveVerified(tile,13,selectedType,selectedNum) ); break;
                        case  5:
                        case  4:
                        case  3:
                        case  2:
                        case  1: outcome = ( !GoodMoveVerified(tile,destinationTileNum+6,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum+7,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum+8,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-1,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum+1,selectedType,selectedNum)); break;
                        case  0: outcome = ( !GoodMoveVerified(tile,7,selectedType,selectedNum) && !GoodMoveVerified(tile,8,selectedType,selectedNum) && !GoodMoveVerified(tile,1,selectedType,selectedNum) ); break;
                        case  7:
                        case 14:
                        case 21:
                        case 28:
                        case 35: outcome = ( !GoodMoveVerified(tile,destinationTileNum+6,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum+7,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum+1,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-6,selectedType,selectedNum) && !GoodMoveVerified(tile,destinationTileNum-7,selectedType,selectedNum)); break;
                        default: break;
                    }

                    return outcome; // Break immediately because there's no point in continuing
                }

                break;
            default:
                if(debug){
                    System.out.println("canMoveAccordingToRules: No Planet Type understood!");
                }
                break;
        }
        return outcome;
    }


    public int[] calculateScores(ArrayList<Tile> tile) {
        int thisLevelCurrentBlueTotal = 0;
        int thisLevelCurrentRedTotal = 0;
        int thisLevelCurrentGreenTotal = 0;

        // Iterate through each tile and update the current color values
        for(Tile theTile : tile) {
            switch(theTile.type) {
                case REDPLANET:
                    thisLevelCurrentRedTotal += theTile.value;
                    break;
                case BLUEPLANET:
                    thisLevelCurrentBlueTotal += theTile.value;
                    break;
                case GREENPLANET:
                    thisLevelCurrentGreenTotal += theTile.value;
                    break;
                default: break;
            }
        }

        int result[] = new int[] {thisLevelCurrentRedTotal, thisLevelCurrentBlueTotal, thisLevelCurrentGreenTotal};
        return result;
    }

    public boolean HistoricValue(ArrayList<Integer> list, int value) {
        boolean result = false;

        if(list.contains(value)) {
            result = true;
        }

        return result;
    }


    public int[] GenerateLevel(int complexity, int redWeight, int blueWeight, int greenWeight, boolean genAsteroids, boolean genSuns) {



        ArrayList<Tile> tile = new ArrayList<Tile>();

        // Create arrays to store previous values of scores needed so that no move will put us back to where we were at any point
        // (i.e., all moves will create a unique score combination)
        ArrayList<Integer> historicRedValues = new ArrayList<Integer>();
        ArrayList<Integer> historicBlueValues = new ArrayList<Integer>();
        ArrayList<Integer> historicGreenValues = new ArrayList<Integer>();

        // These will be the scores needed to beat the level
        int numMovesNeeded = 0;
        int numRedNeeded = 0;
        int numBlueNeeded = 0;
        int numGreenNeeded = 0;

        int extraMoves = 0; // Used only if we need to at the end of the complexity for-loop

        // Running totals to make sure we aren't accidentally creating a level with no difference in value
        int thisLevelCurrentRedTotal = 0;
        int thisLevelCurrentBlueTotal = 0;
        int thisLevelCurrentGreenTotal = 0;

        // This will be the final level we deliver to the player.
        int[] levelFinal = {
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0
        };

        // This will hold the solution to levelFinal
        int[] levelSolution = {
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0
        };

        // Keeps track of our movements during level generation so we don't choose a tile we've already been to.
        // Anytime a planet has been on a tile or is on a tile or moves to a tile, it's flagged. At the end, if there are any remaining
        // tiles, we can stuff them full of asteroids
        int[] levelFlagged = {
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0,
                0, 0, 0, 0, 0, 0, 0
        };

        // First step is to randomly place planets and non-movables according to 1) whether we say to use them, and 2) how complex we want to make the level.
        // Complexity is defined as the minimum number of moves required to beat the level.

        int tileNum = 0;

        if(debug){
            System.out.println("Creating new level.");
        }

        // Instantiate a dummy map to use
        for(int r = 6; r > -1; r--) {
            for(int c = 0; c < 7; c++) {
                tile.add(new Tile(tileValueTable[(r*7)+c], tileNum));
                tileNum++;
            }
        }

        if(debug){
            System.out.println("Map array instantiated.");
        }

        // Now we have an ArrayList full of tiles. Let's stick some tiles in there.

        int numRedTiles = redWeight;//MathUtils.random(2,redWeight); // use 2 here to always have at least one planet to move to another planet
        int numBlueTiles = blueWeight;//(blueWeight > 0 ? MathUtils.random(0,blueWeight) : 0);
        int numGreenTiles = greenWeight;//(greenWeight > 0 ? MathUtils.random(0,greenWeight) : 0);

        if(debug){
            System.out.println("Creating new level with "+numRedTiles+" red, "+numBlueTiles+" blue, and "+numGreenTiles+" green.");
        }

        // Now, randomly go through the level map and place a random tile
        // Start with red
        for(int a=0; a<(numRedTiles); a++) {

            int randomTile = MathUtils.random(0,48);//MathUtils.random(0,48);

            if(levelFlagged[randomTile] == 0) {
                tile.get(randomTile).type = TileType.REDPLANET;
                levelFlagged[randomTile] = 1; // Flag it so we don't select it next time
            } else {
                a--; // If we selected a flagged tile then we don't want this iteration to count
            }
        }

        if(debug){
            System.out.println("Red tiles handled.");
        }

        // Next up is blue
        for(int a=0; a<(numBlueTiles); a++) {

            int randomTile = MathUtils.random(0,48);

            if(levelFlagged[randomTile] == 0) {
                tile.get(randomTile).type = TileType.BLUEPLANET;
                levelFlagged[randomTile] = 1; // Flag it so we don't select it next time
            } else {
                a--; // If we selected a flagged tile then we don't want this iteration to count
            }
        }

        if(debug){
            System.out.println("Blue tiles handled.");
        }

        // Finally, we do green
        for(int a=0; a<(numGreenTiles); a++) {

            int randomTile = MathUtils.random(0,48);

            if(levelFlagged[randomTile] == 0) {
                tile.get(randomTile).type = TileType.GREENPLANET;
                levelFlagged[randomTile] = 1; // Flag it so we don't select it next time
            } else {
                a--; // If we selected a flagged tile then we don't want this iteration to count
            }
        }

        if(debug){
            System.out.println("Green tiles handled.");
        }

        // At this point, we have a map with random planets everywhere, and the levelFlagged has been updated.
        // Let's calculate the planet values so that we don't repeat them in future moves
        int scores[] = calculateScores(tile);

        historicRedValues.add(scores[0]);
        historicBlueValues.add(scores[1]);
        historicGreenValues.add(scores[2]);

        // Also, we should update our value totals that we'll use below during our complexity movements
        // Flush the current scores

        if(debug){
            System.out.println("Level values set (red: "+numRedNeeded+", blue: "+numBlueNeeded+", green:"+numGreenNeeded+")");
        }

        // Now, for every complexity point, let's move a random tile to a non-levelFlagged square according to that tile's rules.
        // Sometimes we might move the same tile a few times, other times we might just move other tiles. Who knows.

        // At this point we have the final level EXCEPT our total scores needed. We'll calc that after we do the movements to create the solution string (levelSolution).

        // Let's compile the map portion of the levelFinal string. Later, we'll compile the moves+number of scores needed for each planet type.
        // This will only give us the planets, not the asteroids or suns. We'll do that after we've moved stuff around.
        for(int r = 6; r > -1; r--) {
            for(int c = 0; c < 7; c++) {

                int position = (r*7)+c; // Where we are in the tile ArrayList
                int typeToWrite = 0;    // What number to write to the level file
                // Convert the TileType to a number for the level string
                // Determine the tile type
                switch( tile.get(position).type ) {
                    case NONE:
                        typeToWrite = 0;
                        break;
                    case REDPLANET:
                        typeToWrite = 1;
                        break;
                    case BLUEPLANET:
                        typeToWrite = 2;
                        break;
                    case GREENPLANET:
                        typeToWrite = 3;
                        break;

                    default:
                        typeToWrite = 0;
                        break;
                }

                levelFinal[position] = typeToWrite;

                tileNum++;
            }
        }


        // Now let's move stuff around according to rules to figure out what our required level values will be
        int i=0;
        boolean goodLevel = false;
        while(goodLevel == false) {


            int selectedValue = 0;  // the value of the selected tile
            int selectedNum = 0;    // the tileNum of the selected tile

            TileType selectedType = TileType.NONE;

            // Select a random tile that is a planet
            boolean selected = false;
            if(debug){
                System.out.println("(Move #"+i+") Looking for a tile for move #"+i);
            }
            while( !selected ) {

                int rand = MathUtils.random(0,48);

                // Check if this is a planet tile
                if( tile.get(rand).type != TileType.NONE ) {

                    tile.get(rand).isSelected = true;
                    selectedValue = tile.get(rand).value;
                    selectedNum = tile.get(rand).tileNum;
                    selectedType = tile.get(rand).type;
                    // We don't have to worry about flagging it in levelFlagged because it already has been flagged
                    selected = true;
                    if(debug){
                        System.out.println("(Move #"+i+") > Selecting tile #"+tile.get(rand).tileNum+" ("+tile.get(rand).type+")");
                    }
                } else {
                    if(debug){
                        System.out.println("(Move #"+i+") > Skipping tile #"+tile.get(rand).tileNum);
                    }
                }

            }

            // At this point, we have selected a random a tile that is not of type NONE.
            // Next, let's select a random destination for that tile. We must make sure of two things:
            // 1) we should not move this tile to another tile of the same value, and 2) we should not
            // move this tile against the rules. We will check for both of these before committing to
            // a move.
            boolean goodDestinationFound = false;

            int tries = 0;

            while( !goodDestinationFound && tries < 10) {

                tries++;

                int rand = MathUtils.random(0, 48);

                if(debug){
                    System.out.println("(Move #"+i+") [Attempt #"+tries+"] Checking tile "+tile.get(rand).tileNum+" for being a good destination for selection "+selectedNum+"...");
                }

                // Great! We found a blank tile. Not so fast, though. Let's check to see if this
                // new tile has a value different from the old one.

                // Let's ensure the location hasn't been flagged yet
                if (levelFlagged[tile.get(rand).tileNum] == 0) {

                    if (debug) {
                        System.out.println("(1/4): Tile #" + tile.get(rand).tileNum + " has not already been flagged...");
                    }

                    // temporary copy of the tile array for later
                    ArrayList<Tile> temp = tile;

                    // Finally, let's make sure we can move according to rules
                    if (canMoveAccordingToRules(tile, tile.get(rand).tileNum)) {

                        if (debug) {
                            System.out.println("(4/4): Tile #" + tile.get(rand).tileNum + " satisfies movement rules!");
                        }

                        // Destination
                        tile.get(rand).type = selectedType;         // Set destination to the selectedType

                        // Previous Location
                        tile.get(selectedNum).type = TileType.NONE; // Set the old one to none
                        tile.get(selectedNum).isSelected = false; // Set the destination to none

                        // Reset the current values
                        int newValues[] = calculateScores(tile);

                        thisLevelCurrentBlueTotal = newValues[0];
                        thisLevelCurrentRedTotal = newValues[1];
                        thisLevelCurrentGreenTotal = newValues[2];

                        // Check if moving to this tile would put us back to a historic value
                        if(
                                (tile.get(rand).type == TileType.REDPLANET && !HistoricValue(historicRedValues, thisLevelCurrentRedTotal)) ||
                                        (tile.get(rand).type == TileType.BLUEPLANET && !HistoricValue(historicBlueValues, thisLevelCurrentBlueTotal)) ||
                                        (tile.get(rand).type == TileType.GREENPLANET && !HistoricValue(historicGreenValues, thisLevelCurrentGreenTotal))
                                ) {

                            levelFlagged[tile.get(rand).tileNum] = 1;   // Flag the location as dirty

                            historicRedValues.add(newValues[0]);
                            historicBlueValues.add(newValues[1]);
                            historicGreenValues.add(newValues[2]);

                            goodDestinationFound = true;
                        } else {
                            // No good destination found AND lets reverse our decisions
                            tile = temp;
                            i = i-1;
                            if(debug){
                                System.out.println("Aborting due to historic value.");
                            }

                        }

                        //if (thisLevelCurrentBlueTotal == numBlueNeeded || thisLevelCurrentRedTotal == numRedNeeded || thisLevelCurrentGreenTotal == numGreenNeeded) {
                        //goodDestinationFound = false; // Reset this if now that we've moved we have the same damn value as we began with.
                        //}

                    } else {
                        if(debug){
                            System.out.println("Nope! Can't move according to rules.");
                        }
                        continue;
                    }

                } else {
                    if(debug) {
                        System.out.println("Nope! This tile was flagged.");
                    }
                    continue;
                }
            }

            if(tries < 10) {
                numMovesNeeded++; // Increment our par moves counter... I guess this is just equal to the complexity, isn't it, Jesse?
            } else {
                if(debug) {
                    System.out.println("Couldn't find a destination after 10 tries, so skipping this one.");

                    i = i-1; // give us another complexity

                }
            }

            // Finally, check to make sure ALL of the values are different from the start. If not, add another complexity
            boolean redsAreGood = false;
            boolean bluesAreGood = false;
            boolean greensAreGood = false;

            if(numRedNeeded > 0) {
                if(thisLevelCurrentRedTotal != numRedNeeded) {
                    redsAreGood = true;
                }
            } else { redsAreGood = true; }

            if(numBlueNeeded > 0) {
                if(thisLevelCurrentBlueTotal != numBlueNeeded) {
                    bluesAreGood = true;
                }
            } else { bluesAreGood = true; }

            if(numGreenNeeded > 0) {
                if(thisLevelCurrentGreenTotal != numGreenNeeded) {
                    greensAreGood = true;
                }
            } else { greensAreGood = true; }

            // if we're above our complexity and the values are different, break the loop
            if(numMovesNeeded >= complexity && redsAreGood && bluesAreGood && greensAreGood){
                goodLevel = true;
            }

            i++;
        }

        // At this point, we have all the things we need for our level. We should go through and look for places to stick an asteroid and
        // a sun, or both, or combinations.

        if(genAsteroids) {
            // Suns are not generated if complexity < 2
            // Asteroids will show up anytime

            // First figure out how many possible total asteroids we can get
            ArrayList<Integer> possibleAsteroids = new ArrayList<Integer>();

            for(int a=0; a<49; a++) {
                if(levelFlagged[a] == 0) {
                    possibleAsteroids.add(a);
                }
            }

            // Now, every possibleAsteroids element is a tile that can be set to an asteroid if we want

            if(possibleAsteroids.size() > 0) {
                int numAsteroids = MathUtils.random(1, possibleAsteroids.size()-1); //(int) Math.log((double) MathUtils.random(1, complexity + 1) * (numRedNeeded + numBlueNeeded + numGreenNeeded)); // gen number of asteroids

                for (int a = 0; a < numAsteroids; a++) {

                    int rand = possibleAsteroids.get(MathUtils.random(0, possibleAsteroids.size()-1));
                    if(debug){
                        System.out.println("Trying to put an asteroid at tile " + rand + "...");
                    }

                    boolean placed = false;
                    while (!placed) {
                        if (levelFlagged[rand] == 0) {
                            // This tile has never been used. Stick an asteroid there!
                            tile.get(rand).type = TileType.ASTEROID;
                            placed = true;
                        }
                    }
                }
            } else {
                if(debug){
                    System.out.println("I was told to place asteroids, but there's no spot for asteroids!");
                }
            }
        }

        if(genSuns) {

            // First figure out how many possible total suns we can get
            ArrayList<Integer> possibleSuns = new ArrayList<Integer>();

            for(int a=0; a<49; a++) {

                if(IntArrayContains(boardEdge, a)) {
                    // Check for squares specially
                    switch (a) {
                        case 42: if(levelFlagged[35] == 0 && levelFlagged[43] == 0 && levelFlagged[36] == 0) { possibleSuns.add(a); } break;
                        case 43:
                        case 44:
                        case 45:
                        case 46:
                        case 47: if(levelFlagged[a-1] == 0 && levelFlagged[a+1] == 0 && levelFlagged[a-6] == 0 && levelFlagged[a-7] == 0 && levelFlagged[a-8] == 0) { possibleSuns.add(a); } break;
                        case 48: if(levelFlagged[47] == 0 && levelFlagged[40] == 0 && levelFlagged[41] == 0) { possibleSuns.add(a); } break;
                        case 41:
                        case 34:
                        case 27:
                        case 20:
                        case 13: if(levelFlagged[a+6] == 0 && levelFlagged[a+1] == 0 && levelFlagged[a+7] == 0 && levelFlagged[a-1] == 0 && levelFlagged[a-6] == 0 && levelFlagged[a-7] == 0) { possibleSuns.add(a); } break;
                        case  6: if(levelFlagged[5] == 0 && levelFlagged[12] == 0 && levelFlagged[13] == 0) { possibleSuns.add(a); } break;
                        case  5:
                        case  4:
                        case  3:
                        case  2:
                        case  1: if(levelFlagged[a+6] == 0 && levelFlagged[a+7] == 0 && levelFlagged[a+8] == 0 && levelFlagged[a-1] == 0 && levelFlagged[a+1] == 0) { possibleSuns.add(a); } break;
                        case  0: if(levelFlagged[7] == 0 && levelFlagged[8] == 0 && levelFlagged[1] == 0) { possibleSuns.add(a); } break;
                        case  7:
                        case 14:
                        case 21:
                        case 28:
                        case 35: if(levelFlagged[a+6] == 0 && levelFlagged[a+7] == 0 && levelFlagged[a+1] == 0 && levelFlagged[a-6] == 0 && levelFlagged[a-7] == 0) { possibleSuns.add(a); } break;
                        default: break;
                    }
                } else if (IntArrayContains(boardMiddle, a)) {
                    // Check the middle section of the board (i.e., all tiles except edges and corners)
                    if (    // For green planets, it's left, right, and all three on top and three on bottom
                            levelFlagged[a-1] == 0 &&
                                    levelFlagged[a+1] == 0 &&
                                    levelFlagged[a-6] == 0 &&
                                    levelFlagged[a-7] == 0 &&
                                    levelFlagged[a-8] == 0 &&
                                    levelFlagged[a+6] == 0 &&
                                    levelFlagged[a+7] == 0 &&
                                    levelFlagged[a+8] == 0
                            ) {
                        possibleSuns.add(a);
                    }
                } else {
                    // do nothing
                }
            }

            // Now, every possibleSuns element is a tile that can be set to an asteroid if we want



            if(possibleSuns.size() > 0) {
                int numSuns = 1;// (possibleSuns.size() == 1 ? 1 : MathUtils.random(1, possibleSuns.size())); //(int) Math.log((double) MathUtils.random(1, complexity + 1) * (numRedNeeded + numBlueNeeded + numGreenNeeded)); // gen number of asteroids

                for (int a = 0; a < numSuns; a++) {



                    int rand = possibleSuns.get(MathUtils.random(0, possibleSuns.size()));
                    if(debug){
                        System.out.println("Trying to put a sun at tile " + rand + "...");
                    }

                    boolean placed = false;
                    int tries = 0;
                    while (!placed && tries < 10) {
                        tries++;
                        if(IntArrayContains(boardEdge, a)) {
                            // Check for squares specially
                            switch (rand) {
                                case 42: if(levelFlagged[35] == 0 && levelFlagged[43] == 0 && levelFlagged[36] == 0) { tile.get(rand).type = TileType.SUN;tile.get(35).type = TileType.BLOCKED;tile.get(43).type = TileType.BLOCKED;tile.get(36).type = TileType.BLOCKED;placed = true; } break;
                                case 43:
                                case 44:
                                case 45:
                                case 46:
                                case 47: if(levelFlagged[rand-1] == 0 && levelFlagged[rand+1] == 0 && levelFlagged[rand-6] == 0 && levelFlagged[rand-7] == 0 && levelFlagged[rand-8] == 0) {
                                    tile.get(rand).type = TileType.SUN;
                                    tile.get(rand-1).type = TileType.BLOCKED;tile.get(rand+1).type = TileType.BLOCKED;tile.get(rand-6).type = TileType.BLOCKED;tile.get(rand-7).type = TileType.BLOCKED;tile.get(rand-8).type = TileType.BLOCKED; placed = true;; } break;
                                case 48: if(levelFlagged[47] == 0 && levelFlagged[40] == 0 && levelFlagged[41] == 0) {
                                    tile.get(rand).type = TileType.SUN;tile.get(47).type = TileType.BLOCKED;tile.get(40).type = TileType.BLOCKED;tile.get(41).type = TileType.BLOCKED;placed = true; } break;
                                case 41:
                                case 34:
                                case 27:
                                case 20:
                                case 13: if(levelFlagged[rand+6] == 0 && levelFlagged[rand+1] == 0 && levelFlagged[rand+7] == 0 && levelFlagged[rand-1] == 0 && levelFlagged[rand-6] == 0 && levelFlagged[rand-7] == 0) {
                                    tile.get(rand).type = TileType.SUN;tile.get(rand+6).type = TileType.BLOCKED;tile.get(rand+1).type = TileType.BLOCKED;tile.get(rand+7).type = TileType.BLOCKED;tile.get(rand-1).type = TileType.BLOCKED;tile.get(rand-6).type = TileType.BLOCKED;tile.get(rand-7).type = TileType.BLOCKED;placed = true; } break;
                                case  6: if(levelFlagged[5] == 0 && levelFlagged[12] == 0 && levelFlagged[13] == 0) {
                                    tile.get(rand).type = TileType.SUN;tile.get(5).type = TileType.BLOCKED;tile.get(12).type = TileType.BLOCKED;tile.get(13).type = TileType.BLOCKED;placed = true; } break;
                                case  5:
                                case  4:
                                case  3:
                                case  2:
                                case  1: if(levelFlagged[rand+6] == 0 && levelFlagged[rand+7] == 0 && levelFlagged[rand+8] == 0 && levelFlagged[rand-1] == 0 && levelFlagged[rand+1] == 0) {
                                    tile.get(rand).type = TileType.SUN;tile.get(rand+6).type = TileType.BLOCKED;tile.get(rand+7).type = TileType.BLOCKED;tile.get(rand+8).type = TileType.BLOCKED;tile.get(rand-1).type = TileType.BLOCKED;tile.get(rand+1).type = TileType.BLOCKED;placed = true; } break;
                                case  0: if(levelFlagged[7] == 0 && levelFlagged[8] == 0 && levelFlagged[1] == 0) {
                                    tile.get(rand).type = TileType.SUN;tile.get(7).type = TileType.BLOCKED;tile.get(8).type = TileType.BLOCKED;tile.get(1).type = TileType.BLOCKED;placed = true; } break;
                                case  7:
                                case 14:
                                case 21:
                                case 28:
                                case 35: if(levelFlagged[rand+6] == 0 && levelFlagged[rand+7] == 0 && levelFlagged[rand+1] == 0 && levelFlagged[rand-6] == 0 && levelFlagged[rand-7] == 0) {
                                    tile.get(rand).type = TileType.SUN;tile.get(rand+6).type = TileType.BLOCKED;tile.get(rand+7).type = TileType.BLOCKED;tile.get(rand+1).type = TileType.BLOCKED;tile.get(rand-6).type = TileType.BLOCKED;tile.get(rand-7).type = TileType.BLOCKED;placed = true; } break;
                                default: break;
                            }
                        } else {
                            // Check the middle section of the board (i.e., all tiles except edges and corners)
                            if (    // For green planets, it's left, right, and all three on top and three on bottom
                                    levelFlagged[rand-1] == 0 &&
                                            levelFlagged[rand+1] == 0 &&
                                            levelFlagged[rand-6] == 0 &&
                                            levelFlagged[rand-7] == 0 &&
                                            levelFlagged[rand-8] == 0 &&
                                            levelFlagged[rand+6] == 0 &&
                                            levelFlagged[rand+7] == 0 &&
                                            levelFlagged[rand+8] == 0
                                    ) {
                                tile.get(rand).type = TileType.SUN;
                                tile.get(rand+6).type = TileType.BLOCKED;
                                tile.get(rand+7).type = TileType.BLOCKED;
                                tile.get(rand+8).type = TileType.BLOCKED;
                                tile.get(rand-1).type = TileType.BLOCKED;
                                tile.get(rand+1).type = TileType.BLOCKED;
                                tile.get(rand-6).type = TileType.BLOCKED;
                                tile.get(rand-7).type = TileType.BLOCKED;
                                tile.get(rand-8).type = TileType.BLOCKED;

                                placed = true;
                            }
                        }
                    }

                    if(tries >= 10) {
                        if(debug){
                            System.out.println("Tried ten times to place the sun. Skipping.");
                        }
                    }
                }
            } else {
                if(debug){
                    System.out.println("I was told to place suns, but there's no spot for suns!");
                }
            }
        }

        // Now only update the asteroids and suns and blocked tiles in the levelFinal
        for(int r = 6; r > -1; r--) {
            for(int c = 0; c < 7; c++) {

                int position = (r*7)+c; // Where we are in the tile ArrayList
                int typeToWrite = 0;    // What number to write to the level file
                // Convert the TileType to a number for the level string
                // Determine the tile type
                switch( tile.get(position).type ) {

                    case ASTEROID:
                        typeToWrite = 4;
                        levelFinal[position] = typeToWrite;
                        break;
                    case SUN:
                        typeToWrite = 5;
                        levelFinal[position] = typeToWrite;
                        break;
                    case BLOCKED:
                        typeToWrite = 9;
                        levelFinal[position] = typeToWrite;
                        break;
                    default:

                        break;
                }



                tileNum++;
            }
        }

        // Now update the asteroids and suns in the levelFinal

        // We can get the values needed for each planet color for the level now.
        // Flush the current scores
        thisLevelCurrentBlueTotal = 0;
        thisLevelCurrentRedTotal = 0;
        thisLevelCurrentGreenTotal = 0;

        // Iterate through each tile and update the current color values
        for(Tile theTile : tile) {
            switch(theTile.type) {
                case REDPLANET:
                    thisLevelCurrentRedTotal += theTile.value;
                    break;
                case BLUEPLANET:
                    thisLevelCurrentBlueTotal += theTile.value;
                    break;
                case GREENPLANET:
                    thisLevelCurrentGreenTotal += theTile.value;
                    break;
                default: break;
            }
        }

        numRedNeeded = thisLevelCurrentRedTotal;
        numBlueNeeded = thisLevelCurrentBlueTotal;
        numGreenNeeded = thisLevelCurrentGreenTotal;

        // Add the final elements of the level string
        levelFinal[49] = numRedNeeded;
        levelFinal[50] = numBlueNeeded;
        levelFinal[51] = numGreenNeeded;
        levelFinal[52] = numMovesNeeded;

        if(debug){
            System.out.println("Level generated!");
        }
        if(debug){
            System.out.println("Creating new level with "+numRedTiles+" red, "+numBlueTiles+" blue, and "+numGreenTiles+" green.");
        }

        return levelFinal;
    }


}