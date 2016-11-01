package com.turkey.gravitygrid;

/**
 * Created by lawsonje on 11/1/2016.
 */



public class Level {

    private int[] levelTiles;
    private int redScoreNeeded;
    private int blueScoreNeeded;
    private int greenScoreNeeded;
    private int parMoves;
    private int darkMatterRequiredToAccess; // How much dark matter does the player need to have accrued before this level becomes available to play?

    private boolean complete; // 0, not complete; 1, completed by player
    private int numPointsEarned;
    private int numMovesUsed;
    private double timeToComplete;

    private String levelMessage; // A string to display at the bottom of the level

    Level(int[] map, int red, int blue, int green, int par, int darkMatterRequiredToAccess) {
        this.levelTiles = map;
        this.redScoreNeeded = red;
        this.blueScoreNeeded = blue;
        this.greenScoreNeeded = green;
        this.parMoves = par;
        this.complete = false;
        this.numPointsEarned = 0;
        this.numMovesUsed = 0;
        this.darkMatterRequiredToAccess = darkMatterRequiredToAccess;
    }

    public boolean IsComplete() {
        return this.complete;
    }

    public int[] getLevelTiles() {
        return this.levelTiles;
    }

    public void MarkAsComplete(int points, int numMoves, double timeToComplete) {
        this.complete = true;
        this.numMovesUsed = numMoves;
        this.numPointsEarned = numPointsEarned;
    }

}