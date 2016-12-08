/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by jesse on 10/30/16.
 */

public class InitialLoadingScreen implements Screen {

    private GravityGrid game;

    OrthographicCamera camera;

    private int screenWidth = Gdx.graphics.getWidth();
    private int screenHeight = Gdx.graphics.getHeight();

    private float spaceTurkeyGamesLogoDirection; // keeps track of the astronaut direction

    // Screen constructor
    InitialLoadingScreen(GravityGrid game) {

        this.game = game;

        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        spaceTurkeyGamesLogoDirection = 359.0f;

        game.getAssetLoader().LoadInitialAssets();

        // We can use these now that we have called LoadInitialAssets()
        //this.game.gravityGridSphereLogoImage = game.assets.getAssetManager().get("spaceturkeylogosquare.png", Texture.class);
        this.game.spaceTurkeyLogoImage = game.assets.getAssetManager().get("spaceturkeylogosquare.png", Texture.class);
        this.game.spaceTurkeyLogoRegion = new TextureRegion(this.game.spaceTurkeyLogoImage);
        this.game.pixelFont = game.assets.getAssetManager().get("agencyfb.ttf", BitmapFont.class);
        this.game.regularFont = game.assets.getAssetManager().get("turkey.ttf", BitmapFont.class);

        game.getAssetLoader().LoadAllAssets();
    }

    @Override
    public void render(float delta) {

        // Check if the asset manager is done loading all the assets. If so, go ahead and move to the Main Menu screen
        // If it's not done, continue to display the loading screen

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();

        if (game.getAssetLoader().getAssetManager().update()) {
            game.getAssetLoader().FinalizeAssets();
            game.setScreen(new MainMenuScreen(game));
        } else {
            game.pixelFont.draw(game.batch, "Loading...", 0, Gdx.graphics.getHeight()/3, Gdx.graphics.getWidth(), 1, true);
            game.pixelFont.draw(game.batch, ""+Math.round(game.assets.getAssetManager().getProgress()*100)+"%", 0, Gdx.graphics.getHeight()/4, Gdx.graphics.getWidth(), 1, true);
        }

        game.batch.setColor(1f,1f,1f,1f);

        game.batch.draw(game.spaceTurkeyLogoRegion, (Gdx.graphics.getWidth()/2)-250, (Gdx.graphics.getHeight()/2)-250, 250, 250, 500, 500, 1.0f, 1.0f, spaceTurkeyGamesLogoDirection);

        game.pixelFont.setColor(1.0f,1.0f,1.0f,1.0f);

        game.batch.end();

        // Update spaceTurkeyGamesLogoDirection so that our astronaut spins
        spaceTurkeyGamesLogoDirection -= 0.5f;
        if(spaceTurkeyGamesLogoDirection <= 0.0f) {
            spaceTurkeyGamesLogoDirection = 359.0f;
        }
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
