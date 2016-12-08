package com.turkey.gravitygrid;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by lawsonje on 12/8/2016.
 */

public class Tile {

    // There are three types of tiles. You can be nothing (i.e., blank; you can be a planet; you can be a sun. A sun is a special tile that cannot have any other tiles touching it.
    // BLUEPLANETs are placed side by side or up and down to each other
    // REDPLANETs are placed diagonally
    // GREENPLANETs go anywhere
    // ASTEROIDs cannot have anything side by side or up and down (you can move planets diagonally)
    // SUNs cannot have anything in any square touching them (even diagonally)
    enum TileType {
        NONE, REDPLANET, BLUEPLANET, GREENPLANET, ASTEROID, SUN, BLOCKED
    }

    enum TileStatus {
        NONE, // Tile is doing nothing
        SELECTED, // yellow spinner
        MOVETOHERE,
        MOVECOMPLETE, //displaying the spinner animation
        CANNOTMOVE
    }

    public Tile.TileType type;
    public Tile.TileStatus status;
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

    public Tile(Rectangle rect, int value, Tile.TileType type, int tileNum) {
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
