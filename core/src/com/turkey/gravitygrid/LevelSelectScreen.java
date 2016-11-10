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
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

import static com.turkey.gravitygrid.GravityGrid.levelCompletionInfo;

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
    Texture[] levelIcon; // levelIcon[0] corresponds to levelcompletioninfo[level].[0] value. see `type` below
    ArrayList<LevelIcon> levelIcons;

    OrthographicCamera camera;

    public String message;
    public float messageAlpha;

    // Simple instance of a level icon on the level select screen
    public class LevelIcon {
        public int type; // corresponds to levelCompletionInfo; 0 = locked, 1 = playable, 2 =beat
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
        camera.setToOrtho(false, 1080, 1920); // Hard-set these to control for multiple display sizes

        // Get our textures
        screenBackground = game.assets.get("menu/blackBackground.png", Texture.class);
        this.levelTileBackground = game.assets.get("levelicons/background.png", Texture.class);
        this.levelIcon = new Texture[3];
        this.levelIcon[0] = game.assets.get("levelicons/locked.png", Texture.class);
        this.levelIcon[1] = game.assets.get("levelicons/play.png", Texture.class);
        this.levelIcon[2] = game.assets.get("levelicons/done.png", Texture.class);

        this.tileWidth = screenWidth / 5; // So it's 7 segments of our screenwidth minus the space we've reserved for the tile labels.
        this.tileHeight = screenWidth / 5;

        // The whitespace variable sets a modifier for the rect.x values of each tile so that the grid is in the center of the screen.
        this.whiteSpace = (int)(0.5*screenHeight) + (int)(0.5*(screenWidth/5)*5);

        this.levelIcons = new ArrayList<LevelIcon>(); // Initialize our grid

        // Set two counters to count our world rows and columns, which are different from the level ones.
        // These will ensure that we are building the map correctly.
        int worldRow = 0;
        int worldCol = 0;
        int levelNum = 0;

        // Load the Map
        // Loop through the tiles and assign rect values, load values from the values table, and
        // also load the tile type from the levels tables.
        for(int r = 4; r >= 0; r--) {
            for(int c = 0; c < 5; c++) {

                // Figure out the type of this level icon
                int thisLevelIconType = levelCompletionInfo[levelNum][0];
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
                if(levelNum <= levelCompletionInfo.length-1) {
                    levelNum++; // make sure we aren't going to throw an index out of bounds exception
                }
            }

            worldCol = 0; // Reset column counter
            worldRow++; // Iterate our row counter

        }

        // Now we need to iterate through the level icons again to find the one that the player is currently on.
        // We might be able to just use currentLevel
        //this.levelIcons.get(this.game.currentLevel).type = 1;
        // No this needs to be computed on the fly right here because we're not writing currentLevel to the filesystem
    }

    // This will be used when a player has beaten level 25, 50, 75, or 100
    public void CreateNewGalaxy() {

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
        if(Gdx.input.justTouched() ) {

            Vector3 finger = new Vector3();
            camera.unproject(finger.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            // loop through the levels and find the touched one
            for(LevelSelectScreen.LevelIcon level : this.levelIcons) {
                if (game.pointInRectangle(level.rect, finger.x, finger.y)) {

                    // If it's a playable level, then set that as the current level and load that bad boy
                    if(level.type == 1 || level.type == 2) {
                        this.game.currentLevel = level.levelNum;
                        game.setScreen(new PlayingScreen(game)); // Will pickup based on what we read from the player files (the ini)
                    } else {
                        message = "You can't play that one yet!";
                        messageAlpha = 1.0f;

                    }
                }
            }

        }

        // Make sure we are displaying the currentLevel as able to play
        //if(levelCompletionInfo[game.currentLevel][0] == ) {

        //}

        game.batch.begin();

        game.batch.draw(screenBackground, 0, 0, screenWidth, screenHeight);

        // Loop through tiles and draw them
        for (LevelSelectScreen.LevelIcon level : this.levelIcons) {


            // Always draw the tile border
            //game.batch.setColor(1f, 1f, 1f, 1f);

            switch(level.type) {
                case 0: game.batch.setColor(1.0f, 1.0f, 1.0f, 0.75f); break;
                case 1: game.batch.setColor(0.0f, 0.5f, 0.0f, 1.0f); break;
                case 2: game.batch.setColor(0.0f, 0.5f, 0.0f, 0.5f); break;
                default: game.batch.setColor(1.0f, 1.0f, 1.0f, 1.0f); break;
            }

            // Draw the white icon underneath, then switch the type to figure out what to draw on top
            game.batch.draw(levelTileBackground, level.rect.x, level.rect.y, level.rect.width, level.rect.height);

            // Draw the tile type on top
            //game.batch.draw(levelIcon[level.type], level.rect.x, level.rect.y, level.rect.width, level.rect.height);


            // Draw the level number in the middle
            game.pixelFont.setColor(0.0f, 0.0f, 0.0f, 1.0f);
            float y = level.rect.y+(0.5f*level.rect.height)+(0.5f*game.fontSize);
            game.pixelFont.draw(game.batch, ""+(level.levelNum+1), level.rect.x, y, level.rect.width, 1, true);


        }

        // Display any messages AND the "next galaxy" button
        // The location of the top line should be below the last tile. We can find this easily:
        float tileHeight = Gdx.graphics.getWidth() / 7;
        float middle = Gdx.graphics.getHeight() / 2;
        float startLineY = middle - 3.85f*tileHeight; // So we want to start 3.5*tileHeight from center of screen. That should get us to the bottom.
        // At 3.85f*tileHeight, we give ourselves a little padding between the text and grid

        if(messageAlpha > 0.0f) {
            game.regularFont.setColor(1f, 0.5f, 1f, messageAlpha);
            game.regularFont.draw(game.batch, "" + message, 0, startLineY, this.screenWidth, 1, true);
            game.regularFont.setColor(1f, 1f, 1f, 1f);  //reset the regularFont color to white
            messageAlpha -= 0.015f;
        }


        game.pixelFont.setColor(0.87f,0.84f,0.22f,1f);
        game.pixelFont.draw(game.batch, "GRAVITY GRID", 0, screenHeight, Gdx.graphics.getWidth(), 1, false);
        game.regularFont.draw(game.batch, "GALAXY "+game.galaxyName[game.currentGalaxy], 5, screenHeight-(1.5f*this.game.fontSize), this.screenWidth-10, 1, false);
        game.regularFont.draw(game.batch, "Select a Planetary System:", 5, screenHeight-(2.5f*this.game.fontSize), this.screenWidth-10, 1, false);

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
