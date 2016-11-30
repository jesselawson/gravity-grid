/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

/**
 * Created by jesse on 10/30/16.
 */

public class LevelSelectScreen implements Screen {

    private GravityGrid game;

    private int tileWidth;
    private int tileHeight;
    private int whiteSpace;

    private int headSpace; // This accounts for the header column numbers
    private int leftSpace; // This accounts for the left column numbers

    private int screenWidth = Gdx.graphics.getWidth();
    private int screenHeight = Gdx.graphics.getHeight();

    Texture screenBackground;
    Texture levelTileBackground;
    Texture[] levelIcon; // levelIcon[0] corresponds to game.levelCompletionInfo[level].[0] value. see `type` below
    ArrayList<LevelIcon> levelIcons; // The tiles for our level selector

    Texture previousGalaxyButtonImage;
    Texture nextGalaxyButtonImage;
    Rectangle previousGalaxyButtonRect;
    Rectangle nextGalaxyButtonRect;

    OrthographicCamera camera;

    public String message;
    public float messageAlpha;

    // Simple instance of a level icon on the level select screen
    public class LevelIcon {
        public int type; // corresponds to game.levelCompletionInfo; 0 = locked, 1 = playable, 2 =beat
        public Rectangle rect;
        public int levelNum; // corresponds to the level num to make currentLevel

        public LevelIcon(Rectangle rect, int num, int type) {
            this.type = type;
            this.levelNum = num;
            this.rect = new Rectangle();
            this.rect = rect;
        }
    }

    // White icon for levels, with an overlay image on top based on LevelIcon.type. 0=lock, 1=play button, 2=done

    // Screen constructor
    public LevelSelectScreen(GravityGrid game) {
        this.game = game;

        // Setup the camera
        camera = new OrthographicCamera();
        //camera.setToOrtho(false, 1080, 1920); // Hard-set these to control for multiple display sizes
        camera.setToOrtho(false, game.screenWidth, game.screenHeight);

        // Get our textures
        screenBackground = game.assets.getAssetManager().get("menu/blackBackground.png", Texture.class);
        this.levelTileBackground = game.assets.getAssetManager().get("levelicons/background.png", Texture.class);

        this.tileWidth = screenWidth / 5; // So it's 7 segments of our screenwidth minus the space we've reserved for the tile labels.
        this.tileHeight = screenWidth / 5;

        this.levelIcon = new Texture[3];
        this.levelIcon[0] = game.assets.getAssetManager().get("levelicons/locked.png", Texture.class);
        this.levelIcon[1] = game.assets.getAssetManager().get("levelicons/play.png", Texture.class);
        this.levelIcon[2] = game.assets.getAssetManager().get("levelicons/done.png", Texture.class);

        previousGalaxyButtonImage = game.assets.getAssetManager().get("levelicons/previousGalaxyButton.png", Texture.class);
        nextGalaxyButtonImage = game.assets.getAssetManager().get("levelicons/nextGalaxyButton.png", Texture.class);

        // The whitespace variable sets a modifier for the rect.x values of each tile so that the grid is in the center of the screen.
        this.whiteSpace = (int)(0.5*screenHeight) + (int)(0.33*(screenWidth/5)*5);

        // Generate rect's for buttons

        previousGalaxyButtonRect = new Rectangle(this.tileWidth, 5, this.tileWidth, this.tileHeight);
        nextGalaxyButtonRect = new Rectangle(3*this.tileWidth, 5, this.tileWidth, this.tileHeight);


        this.levelIcons = new ArrayList<LevelIcon>(); // Initialize our grid

        // Set two counters to count our world rows and columns, which are different from the level ones.
        // These will ensure that we are building the map correctly.
        int worldRow = 0;
        int worldCol = 0;
        int levelNum = 0;

        // Figure out our current galaxy
        if(game.currentLevel <= 25) {
            game.currentGalaxy = 0;
        } else {
            game.currentGalaxy = game.currentLevel / 25;
        }

        // Load the Map, based on our current galaxy
        // Loop through the tiles and assign rect values, load values from the values table, and
        // also load the tile type from the levels tables.
        for(int r = 4; r >= 0; r--) {
            for(int c = 0; c < 5; c++) {

                // Figure out the type of this level icon
                int thisLevelIconType = game.levelCompletionInfo[(game.currentGalaxy*25)+levelNum][0];
                //System.out.println("Max: "+game.levelCompletionInfo.length+":: At levelNum "+levelNum+" I found "+thisLevelIconType);

                // Create a placeholder for the rect values
                Rectangle rect = new Rectangle();

                // Determine the rect values
                rect.x = worldCol*this.tileWidth;
                rect.y = whiteSpace - worldRow * this.tileHeight;
                rect.width = this.tileWidth;
                rect.height = this.tileHeight;

                // Here we push the new tile to the array. Since we are assigning rect values per tile, it doesn't
                // matter (theoretically) how we access these tiles or if they're out of order.
                levelIcons.add(new LevelSelectScreen.LevelIcon(rect, levelNum, thisLevelIconType));

                worldCol++;
                if(levelNum <= game.levelCompletionInfo.length-1) {
                    levelNum++; // make sure we aren't going to throw an index out of bounds exception
                }
            }

            worldCol = 0; // Reset column counter
            worldRow++; // Iterate our row counter

        }


    }

    // Call this to change the currentGalaxy so that we map the game.levelCompletionInfo to the tiles appropriately
    public void ChangeToGalaxy(int direction) {

        game.currentGalaxy += direction;

        int levelNum = 0;

        for(int r = 4; r >= 0; r--) {
            for(int c = 0; c < 5; c++) {

                // Get the icon we need
                int thisLevelIconType = game.levelCompletionInfo[(game.currentGalaxy*25)+levelNum][0];

                // set the level icon appropriately
                levelIcons.get(levelNum).type = thisLevelIconType;

                if(levelNum <= game.levelCompletionInfo.length-1) {
                    levelNum++; // make sure we aren't going to throw an index out of bounds exception
                }
            }
        }
    }

    public void PlayLevel(int levelNum) {
        //game.currentLevel = levelNum;
        //this.playingScreen.RestartLevel();
        //game.setScreen(this.playingScreen);
        game.setScreen(new PlayingScreen(this.game, this));
    }

    public void PlayNextLevel() {
        // This function is called in PlayingScreen and assumes that UpdateLevelCompletionInfo has already been called, which should have updated game.currentLevel
        //this.playingScreen.RestartLevel();

        // Update the levelSelectScreen, too
        ChangeToGalaxy(0); // This will just force us to redraw all the thisLevelIconType's in the levelIcons so that they reflect our progress. If you get rid of this, the levelIcons wont update unless you go Prev then Next galaxy.

        game.setScreen(new PlayingScreen(this.game, this));
    }

    @Override
    public void render(float delta) {

        // Draw starfield
        // Draw 5x5 grid for levels (25 levels each screen)
        // Draw modifiers and recs

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        // Check for input
        processInput:
        if(Gdx.input.justTouched() ) {

            Vector3 finger = new Vector3();
            camera.unproject(finger.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            // First check if the player is touching one of the prev/next galaxy buttons
            if(game.currentGalaxy < 5) { // so: 0,1,2,3,4
                // Don't bother with previous button if this is our current galaxy
                if(game.pointInRectangle(nextGalaxyButtonRect, finger.x, finger.y)) {
                    // Has the player beaten level 25 yet?
                    if(game.levelCompletionInfo[(game.currentGalaxy *25)+24][0] == 2) {
                        // Yep, so let's increment our galaxy
                        ChangeToGalaxy(1);
                        message = "Now entering the "+game.galaxyName[game.currentGalaxy]+" Galaxy";
                        messageAlpha = 1.0f;
                    }
                    break processInput;
                }
            }

            // What about the prev galaxy button?
            if(game.currentGalaxy > 0) { // so: 0,1,2,3,4
                // Don't bother with previous button if this is our current galaxy
                if(game.pointInRectangle(previousGalaxyButtonRect, finger.x, finger.y)) {
                    // We can assume that if the player can go back that they beat the previous galaxies
                        ChangeToGalaxy(-1);
                        message = "Now entering the "+game.galaxyName[game.currentGalaxy]+" Galaxy";
                        messageAlpha = 1.0f;
                    break processInput;
                }
            }

            // loop through the levels and find the touched one
            for(LevelSelectScreen.LevelIcon level : this.levelIcons) {
                if (game.pointInRectangle(level.rect, finger.x, finger.y)) {

                    // If it's a playable level, then set that as the current level and load that bad boy
                    if(level.type == 1 || level.type == 2) {
                        game.currentLevel = level.levelNum+(game.currentGalaxy*25);
                        System.out.println("Loading level "+game.currentLevel+"...");
                        PlayLevel(game.currentLevel);
                    } else {
                        message = "You can't play that one yet!";
                        messageAlpha = 1.0f;

                    }
                    break processInput;
                }
            }

        }

        // Make sure we are displaying the currentLevel as able to play
        //if(levelCompletionInfo[game.currentLevel][0] == ) {

        //}

        game.batch.begin();

        game.batch.setColor(1.0f,1.0f,1.0f,1.0f);

        game.batch.draw(screenBackground, 0, 0, screenWidth, screenHeight);

        // Loop through tiles and draw them
        for (LevelSelectScreen.LevelIcon level : this.levelIcons) {


            // Always draw the tile border
            //game.batch.setColor(1f, 1f, 1f, 1f);

            switch(level.type) {
                case 0: game.batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); break;   //Locked
                case 1: game.batch.setColor(0.0f, 0.5f, 0.0f, 0.9f); break; //Current Level
                case 2: game.batch.setColor(0.0f, 0.5f, 0.0f, 0.9f); break; //Completed
                default: game.batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); break;
            }

            // Draw the white icon underneath, then switch the type to figure out what to draw on top
            game.batch.draw(levelTileBackground, level.rect.x, level.rect.y, level.rect.width, level.rect.height);

            // Draw the tile type on top
            game.batch.setColor(1.0f,1.0f,1.0f,0.75f);
            float cornerX = level.rect.x+(level.rect.width-(0.5f*level.rect.width));
            float cornerY = level.rect.y+(level.rect.height-(0.5f*level.rect.height));
            float cornerWH = 0.5f*level.rect.width;
            game.batch.draw(levelIcon[level.type], cornerX, cornerY, cornerWH, cornerWH);
            game.batch.setColor(1.0f,1.0f,1.0f,1.0f);

            // Draw the level number in the middle
            game.regularFont.setColor(1f,1.0f, 1f, 1f);
            float y = level.rect.y+(0.5f*level.rect.height)+(0.5f*(game.fontSize));
            game.regularFont.draw(game.batch, ""+(level.levelNum+1), level.rect.x, y, level.rect.width, 1, true);


        }


        // Display any messages AND the "next galaxy" button
        game.batch.setColor(1.0f,1.0f,1.0f,1.0f);

        // Prev and Next galaxy buttons
        // Check to see if our currentGalaxy*25 level is completed. If it is, we can display the "Next galaxy" button
        if(game.currentGalaxy > 0) { // dont draw "previous galaxy" on our first galaxy
            game.batch.draw(previousGalaxyButtonImage, previousGalaxyButtonRect.x, previousGalaxyButtonRect.y, previousGalaxyButtonRect.width, previousGalaxyButtonRect.height);
        }
        if(game.currentGalaxy != 4) { // Don't draw "next galaxy" on our 4th galaxy
            // Now let's lock the "next galaxy" button if we haven't completed the 25th level in oru current galaxy
            if(game.levelCompletionInfo[(game.currentGalaxy*25)+24][0] != 2) {
                // display a lock icon over the button
                game.batch.draw(levelIcon[0], nextGalaxyButtonRect.x, nextGalaxyButtonRect.y, nextGalaxyButtonRect.width, nextGalaxyButtonRect.height);
                game.batch.setColor(0.5f,0.5f,0.5f,0.5f);
            }
            game.batch.draw(nextGalaxyButtonImage, nextGalaxyButtonRect.x, nextGalaxyButtonRect.y, nextGalaxyButtonRect.width, nextGalaxyButtonRect.height);

        }



        // The location of the top line should be below the last tile. We can find this easily:
        float tileHeight = Gdx.graphics.getWidth() / 7;
        float middle = Gdx.graphics.getHeight() / 2;
        float startLineY = middle - 3.85f*tileHeight; // So we want to start 3.5*tileHeight from center of screen. That should get us to the bottom.
        // At 3.85f*tileHeight, we give ourselves a little padding between the text and grid

        if(messageAlpha > 0.0f) {
            game.regularFont.setColor(1f, 0.5f, 1f, messageAlpha);
            game.regularFont.draw(game.batch, "" + message, 0, startLineY, this.screenWidth, 1, true);
            game.regularFont.setColor(1f, 1f, 1f, 1f);  //reset the regularFont color to white
            messageAlpha -= 0.005f;
        }


        game.pixelFont.setColor(0.87f,0.84f,0.22f,1f);
        game.pixelFont.draw(game.batch, "GRAVITY GRID", 0, screenHeight, Gdx.graphics.getWidth(), 1, false);
        game.regularFont.draw(game.batch, game.galaxyName[game.currentGalaxy]+" Galaxy", 5, screenHeight-(1.5f* game.fontSize), this.screenWidth-10, 1, false);
        game.regularFont.draw(game.batch, "Select a Planetary System:", 5, screenHeight-(2.5f* game.fontSize), this.screenWidth-10, 1, false);

        game.batch.end();
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void show() {

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
    }

}
