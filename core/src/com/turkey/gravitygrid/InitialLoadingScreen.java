/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.math.Rectangle;

/**
 * Created by jesse on 10/30/16.
 */

public class InitialLoadingScreen implements Screen {



    private GravityGrid game;

    OrthographicCamera camera;

    private int screenWidth = Gdx.graphics.getWidth();
    private int screenHeight = Gdx.graphics.getHeight();

    private float astronautDir; // keeps track of the astronaut direction

    // Screen constructor
    public InitialLoadingScreen(GravityGrid game) {

        this.game = game;

        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        astronautDir = 0.0f;

        // Load the loading screen astronaut and the font first
        this.game.assets.load("littleAstronaut.png", Texture.class);

        // Setup asset manager for freetype fonts
        this.game.assets.setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()));
        this.game.assets.setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(new InternalFileHandleResolver()));

        // Generate our regularFont
        FreetypeFontLoader.FreeTypeFontLoaderParameter regularFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        regularFontParams.fontFileName = "turkey.ttf";
        regularFontParams.fontParameters.size = this.game.fontSize;
        this.game.assets.load("turkey.ttf", BitmapFont.class, regularFontParams);

        // Generate our pixelFont (Our big fancy one)
        FreetypeFontLoader.FreeTypeFontLoaderParameter pixelFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        pixelFontParams.fontFileName = "OrialBold.ttf";
        pixelFontParams.fontParameters.size = this.game.fontSize+8;
        this.game.assets.load("OrialBold.ttf", BitmapFont.class, pixelFontParams);

        this.game.assets.finishLoading(); // Wait for all the assets to load, then go ahead and get our initial assets for the loading screen

        this.game.littleAstronautImage = this.game.assets.get("littleAstronaut.png", Texture.class);
        this.game.littleAstronautRegion = new TextureRegion(this.game.littleAstronautImage);
        this.game.regularFont = this.game.assets.get("turkey.ttf", BitmapFont.class);
        this.game.pixelFont = this.game.assets.get("OrialBold.ttf", BitmapFont.class);

        // Load all assetmanager assets
        this.game.assets.load("consoleBeep.wav", Sound.class);
        this.game.assets.load("tileDeselectSound.wav", Sound.class);
        this.game.assets.load("goodMoveAttempt.ogg", Sound.class);
        this.game.assets.load("cannotMoveSound.wav", Sound.class);
        this.game.assets.load("outOfMovesSound.wav", Sound.class);
        this.game.assets.load("levelCompleteSound.wav", Sound.class);

        // Main menu assets
        this.game.assets.load("mainmenubg.png", Texture.class);

        // Load the textures
        this.game.assets.load("tileBlankImage.png", Texture.class);
        this.game.assets.load("planet-red.png", Texture.class);
        this.game.assets.load("planet-blue.png", Texture.class);
        this.game.assets.load("planet-green.png", Texture.class);
        this.game.assets.load("sun.png", Texture.class);
        this.game.assets.load("sunflare0.png", Texture.class);
        this.game.assets.load("asteroid0.png", Texture.class);
        this.game.assets.load("asteroid1.png", Texture.class);
        this.game.assets.load("asteroid2.png", Texture.class);
        this.game.assets.load("asteroid3.png", Texture.class);
        this.game.assets.load("bg0.png", Texture.class);
        this.game.assets.load("bg1.png", Texture.class);
        this.game.assets.load("bg2.png", Texture.class);
        this.game.assets.load("bg3.png", Texture.class);
        this.game.assets.load("singularity0.png", Texture.class);
        this.game.assets.load("starfield.png", Texture.class);
        this.game.assets.load("tile0.png", Texture.class);
        this.game.assets.load("tile1.png", Texture.class);
        this.game.assets.load("tile2.png", Texture.class);
        this.game.assets.load("tile3.png", Texture.class);
        this.game.assets.load("tile4.png", Texture.class);
        this.game.assets.load("tile5.png", Texture.class);
        this.game.assets.load("tile6.png", Texture.class);
        this.game.assets.load("tile8.png", Texture.class);
        this.game.assets.load("tile10.png", Texture.class);
        this.game.assets.load("tileOverlayAnim0.png", Texture.class);
        this.game.assets.load("tileOverlayAnim1.png", Texture.class);
        this.game.assets.load("tileOverlayAnim2.png", Texture.class);
        this.game.assets.load("tileOverlayAnim3.png", Texture.class);
        this.game.assets.load("tileOverlayAnim4.png", Texture.class);
        this.game.assets.load("tileOverlayAnim5.png", Texture.class);
        this.game.assets.load("tileOverlayAnim6.png", Texture.class);
        this.game.assets.load("buttonFail.png", Texture.class);
        this.game.assets.load("buttonLevelComplete.png", Texture.class);
        this.game.assets.load("galaxyOverlay.png", Texture.class);

        // Load our confetti!
        this.game.assets.load("confetti/confettiFrame01.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame02.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame03.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame04.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame05.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame06.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame07.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame08.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame09.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame10.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame11.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame12.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame13.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame14.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame15.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame16.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame17.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame18.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame19.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame20.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame21.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame22.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame23.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame24.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame25.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame26.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame27.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame28.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame29.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame30.png", Texture.class);
        this.game.assets.load("confetti/confettiFrame31.png", Texture.class);

        // Buttons
        this.game.assets.load("button/continue.png", Texture.class);
        this.game.assets.load("button/newgame.png", Texture.class);

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

        if (game.assets.update()) {
            game.setScreen(new MainMenuScreen(game));
        } else {
            game.pixelFont.draw(game.batch, "Loading...", 0, Gdx.graphics.getHeight()/3, Gdx.graphics.getWidth(), 1, true);
            game.pixelFont.draw(game.batch, ""+Math.round(game.assets.getProgress()*100)+"%", 0, Gdx.graphics.getHeight()/4, Gdx.graphics.getWidth(), 1, true);
        }

        game.batch.setColor(1f,1f,1f,1f);

        game.batch.draw(game.littleAstronautRegion, (Gdx.graphics.getWidth()/2)-40, (Gdx.graphics.getHeight()/2)-40, 40, 40, 80, 80, 1.0f, 1.0f, astronautDir);

        game.pixelFont.setColor(1.0f,1.0f,1.0f,1.0f);

        game.batch.end();

        // Update astronautDir so that our astronaut spins
        astronautDir += 1.0f;
        if(astronautDir >= 359.0f) {
            astronautDir = 0.0f;
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
