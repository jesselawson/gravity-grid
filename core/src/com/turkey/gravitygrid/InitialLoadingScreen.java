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
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;

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
    public InitialLoadingScreen(GravityGrid game) {

        this.game = game;

        // create the camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, screenWidth, screenHeight);

        spaceTurkeyGamesLogoDirection = 359.0f;

        // Load the loading screen astronaut and the font first
        this.game.assets.load("spaceturkeylogosquare.png", Texture.class);

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
        pixelFontParams.fontFileName = "agencyfb.ttf";
        pixelFontParams.fontParameters.size = this.game.fontSize+16;
        this.game.assets.load("agencyfb.ttf", BitmapFont.class, pixelFontParams);

        this.game.assets.finishLoading(); // Wait for all the assets to load, then go ahead and get our initial assets for the loading screen

        this.game.gravityGridSphereLogoImage = this.game.assets.get("spaceturkeylogosquare.png", Texture.class);
        this.game.spaceTurkeyLogoRegion = new TextureRegion(this.game.gravityGridSphereLogoImage);
        this.game.pixelFont = this.game.assets.get("agencyfb.ttf", BitmapFont.class);
        this.game.regularFont = this.game.assets.get("turkey.ttf", BitmapFont.class);

        // Load all assetmanager assets
        this.game.assets.load("consoleBeep.wav", Sound.class);
        this.game.assets.load("tileDeselectSound.wav", Sound.class);
        this.game.assets.load("goodMoveAttempt.ogg", Sound.class);
        this.game.assets.load("cannotMoveSound.wav", Sound.class);
        this.game.assets.load("outOfMovesSound.wav", Sound.class);
        this.game.assets.load("levelCompleteSound.wav", Sound.class);

        // Sound assets

        this.game.assets.load("sounds/inGameMenuOpenSound.wav", Sound.class);
        this.game.assets.load("sounds/inGameMenuButtonSound.wav", Sound.class);
        this.game.assets.load("sounds/inGameMenuResetButtonSound.wav", Sound.class);
        this.game.assets.load("sounds/levelSelectMenuPlayLevelButtonSound.wav", Sound.class);
        this.game.assets.load("sounds/nextGalaxyButtonSound.wav.wav", Sound.class);
        this.game.assets.load("sounds/previousGalaxyButtonSound.wav.wav", Sound.class);


        // Tutorial assets
        this.game.assets.load("tap-here.png", Texture.class);

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
        this.game.assets.load("bg0.jpg", Texture.class);
        this.game.assets.load("bg1.jpg", Texture.class);
        this.game.assets.load("bg2.png", Texture.class);
        this.game.assets.load("bg3.png", Texture.class);
        this.game.assets.load("singularity0.png", Texture.class);
        this.game.assets.load("tile0.png", Texture.class);
        this.game.assets.load("tile1.png", Texture.class);
        this.game.assets.load("tile2.png", Texture.class);
        this.game.assets.load("tile3.png", Texture.class);
        this.game.assets.load("tile4.png", Texture.class);
        this.game.assets.load("tile5.png", Texture.class);
        this.game.assets.load("tile6.png", Texture.class);
        this.game.assets.load("tile8.png", Texture.class);
        this.game.assets.load("tile10.png", Texture.class);
        this.game.assets.load("tileSelected.png", Texture.class);
        this.game.assets.load("tileSelected2.png", Texture.class);
        this.game.assets.load("tileOverlayAnim0.png", Texture.class);
        this.game.assets.load("tileOverlayAnim1.png", Texture.class);
        this.game.assets.load("tileOverlayAnim2.png", Texture.class);
        this.game.assets.load("tileOverlayAnim3.png", Texture.class);
        this.game.assets.load("tileOverlayAnim4.png", Texture.class);
        this.game.assets.load("tileOverlayAnim5.png", Texture.class);
        this.game.assets.load("tileOverlayAnim6.png", Texture.class);
        this.game.assets.load("buttonFail.png", Texture.class);
        this.game.assets.load("buttonLevelComplete.png", Texture.class);
        this.game.assets.load("levelCompleteTrophy.png", Texture.class);
        this.game.assets.load("galaxyOverlay.png", Texture.class);

        this.game.assets.load("levelMessageBackground.png", Texture.class);

        // Tutorial Overlays
        this.game.assets.load("tutorials/level1TutorialOverlay.png", Texture.class);
        this.game.assets.load("tutorials/level2TutorialOverlay.png", Texture.class);
        this.game.assets.load("tutorials/level3TutorialOverlay.png", Texture.class);
        this.game.assets.load("tutorials/level7TutorialOverlay.png", Texture.class);


        // Buttons
        this.game.assets.load("button/continue.png", Texture.class);
        this.game.assets.load("button/newgame.png", Texture.class);

        // Particle effects
        this.game.assets.load("particles/starfield.p", ParticleEffect.class);
        this.game.assets.load("particles/goodmovestarburst.p", ParticleEffect.class);
        this.game.assets.load("particles/badmovestarburst.p", ParticleEffect.class);

        // Assets for LevelSelectScreen
        this.game.assets.load("levelicons/background.png", Texture.class);
        this.game.assets.load("levelicons/done.png", Texture.class);
        this.game.assets.load("levelicons/locked.png", Texture.class);
        this.game.assets.load("levelicons/play.png", Texture.class);
        this.game.assets.load("levelicons/previousGalaxyButton.png", Texture.class);
        this.game.assets.load("levelicons/nextGalaxyButton.png", Texture.class);

        // Assets for the in-game menu
        game.assets.load("menu/blackBackground.png", Texture.class);
        game.assets.load("menu/cancelButton.png", Texture.class);
        game.assets.load("menu/levelSelectButton.png", Texture.class);
        game.assets.load("menu/helpButton.png", Texture.class);
        game.assets.load("menu/resetButton.png", Texture.class);
        game.assets.load("menu/menuButton.png", Texture.class);
        game.assets.load("doitnow.png", Texture.class);

        // Assets for help screen
        //game.assets.load("menu/helpScreen0.png", Texture.class);
        //game.assets.load("menu/helpScreen1.png", Texture.class);

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
