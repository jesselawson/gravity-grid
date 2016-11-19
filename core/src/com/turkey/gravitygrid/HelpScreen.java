/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by jesse on 10/30/16.
 */

public class HelpScreen implements Screen {

    // A custom point-in-rectangle collision checker
    public static boolean pointInRectangle (Rectangle r, float x, float y) {
        return r.x <= x && r.x + r.width >= x && r.y <= y && r.y + r.height >= y;
    }

    private GravityGrid game;

    private OrthographicCamera camera;

    private Texture helpScreen[];

    private Rectangle nextScreen;

    private int currentScreen;
    private int totalScreens = 3; // 0, 1, 2

    // Screen constructor
    public HelpScreen(GravityGrid game) {
        this.game = game;

        helpScreen = new Texture[2];

        helpScreen[0] = game.assets.get("menu/helpScreen0.png", Texture.class);
        helpScreen[1] = game.assets.get("menu/helpScreen1.png", Texture.class);

        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, game.screenWidth, game.screenHeight);

        // Init current screen to 0
        currentScreen = 0;

    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        // tell the camera to update its matrices.
        camera.update();

        // tell the SpriteBatch to render in the
        // coordinate system specified by the camera.
        game.batch.setProjectionMatrix(camera.combined);

        game.batch.begin();
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
