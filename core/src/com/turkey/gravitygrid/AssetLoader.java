/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.Json;

/**
 * Created by lawsonje on 11/30/2016.
 */

public final class AssetLoader {

    private AssetManager assets;
    private TextureAtlas atlas;

    private int fontSize = 60;
    
    AssetLoader(GravityGrid game, AssetManager theAssetManager) {
        assets = theAssetManager;

        // Pass over any variables from game we need
        fontSize = game.fontSize;

        // Load initial assets before heading to initial load screen
        // Load initial assets before heading to initial load screen
        //LoadInitialAssets();

        //LoadAllAssets();
    }

    public AssetManager getAssetManager() {
        return this.assets;
    }
    public TextureAtlas getAtlas() { return this.atlas; }

    // Initial assets are those that are required to show a loading screen
    // It always ends with finishLoading();
    public void LoadInitialAssets() {

        // Load the loading screen astronaut and the font first
        getAssetManager().load("spaceturkeylogosquare.png", Texture.class);

        // Setup asset manager for freetype fonts
        //getAssetManager().setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()));
        //getAssetManager().setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(new InternalFileHandleResolver()));

        // Generate our regularFont
        //FreetypeFontLoader.FreeTypeFontLoaderParameter regularFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        getAssetManager().load("turkey.fnt", BitmapFont.class);

        // Generate our pixelFont (Our big fancy one)
        //FreetypeFontLoader.FreeTypeFontLoaderParameter pixelFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        //pixelFontParams.fontFileName = "agencyfb.ttf";
        //pixelFontParams.fontParameters.size = fontSize+16;
        getAssetManager().load("agencyfb.fnt", BitmapFont.class);

        getAssetManager().finishLoading(); // Wait for all the assets to load, then go ahead and get our initial assets for the loading screen
    }

    public void LoadFirstBatchOfAssets() {
        // Load all assetmanager assets
        getAssetManager().load("sounds/tileDeselectSound.ogg",Sound.class);
        getAssetManager().load("sounds/goodMoveSound.ogg",Sound.class);
        getAssetManager().load("sounds/cannotMoveSound.ogg",Sound.class);
        getAssetManager().load("sounds/mainMenuButton.ogg", Sound.class);
        getAssetManager().load("sounds/levelCompleteSound.ogg", Sound.class);
        getAssetManager().load("sounds/nope.ogg",Sound.class);

        // Sound assets
        getAssetManager().load("sounds/inGameMenuOpenButtonSound.ogg",Sound.class);
        //getAssetManager().load("sounds/inGameMenuButtonSound.ogg",Sound.class);
        getAssetManager().load("sounds/inGameMenuResetLevelButtonSound.ogg",Sound.class);
        getAssetManager().load("sounds/inGameMenuLevelSelectButtonSound.ogg",Sound.class);
        getAssetManager().load("sounds/levelSelectMenuPlayLevelButtonSound.ogg",Sound.class);
        getAssetManager().load("sounds/nextGalaxyButtonSound.ogg",Sound.class);
        getAssetManager().load("sounds/previousGalaxyButtonSound.ogg",Sound.class);
        getAssetManager().load("sounds/tileSelectSound.ogg",Sound.class);

        // Music assets
        getAssetManager().load("sounds/galaxy1music.mp3", Music.class);

    }

    public void LoadAllAssets() {

        getAssetManager().load("GravityGridAtlas.atlas", TextureAtlas.class);

        //getAssetManager().load("sounds/galaxy1music.mp3", Music.class);
       // getAssetManager().load("sounds/galaxy2music.mp3", Music.class);
       // getAssetManager().load("sounds/galaxy3music.mp3", Music.class);

        // Particle effects
        getAssetManager().load("particles/starfield.p", ParticleEffect.class);
        getAssetManager().load("particles/goodmovestarburst.p", ParticleEffect.class);
        getAssetManager().load("particles/badmovestarburst.p", ParticleEffect.class);

        /*

        getAssetManager().load("flare.png", Texture.class);

        // Main menu assets
        getAssetManager().load("mainmenubg.jpg", Texture.class);

        // Load the textures
        getAssetManager().load("tileBlankImage.png", Texture.class);
        getAssetManager().load("planetRed.png", Texture.class);
        getAssetManager().load("planetBlue.png", Texture.class);
        getAssetManager().load("planetGreen.png", Texture.class);
        getAssetManager().load("sun.png", Texture.class);
        getAssetManager().load("sunflare0.png", Texture.class);
        getAssetManager().load("asteroid0.png", Texture.class);
        getAssetManager().load("asteroid1.png", Texture.class);
        getAssetManager().load("asteroid2.png", Texture.class);
        getAssetManager().load("asteroid3.png", Texture.class);
        getAssetManager().load("bg0.jpg", Texture.class);
        getAssetManager().load("bg1.jpg", Texture.class);
        getAssetManager().load("bg2.jpg", Texture.class);
        getAssetManager().load("bg3.jpg", Texture.class);
        getAssetManager().load("tile0.png", Texture.class);
        getAssetManager().load("tile1.png", Texture.class);
        getAssetManager().load("tile2.png", Texture.class);
        getAssetManager().load("tile3.png", Texture.class);
        getAssetManager().load("tile4.png", Texture.class);
        getAssetManager().load("tile5.png", Texture.class);
        getAssetManager().load("tile6.png", Texture.class);
        getAssetManager().load("tile8.png", Texture.class);
        getAssetManager().load("tile10.png", Texture.class);
        getAssetManager().load("tileSelected.png", Texture.class);
        getAssetManager().load("tileSelected2.png", Texture.class);
        getAssetManager().load("buttonLevelComplete.png", Texture.class);
        getAssetManager().load("levelCompleteTrophy.png", Texture.class);

        // Tutorial Overlays
        getAssetManager().load("tutorials/level1TutorialOverlay.png", Texture.class);
        getAssetManager().load("tutorials/level2TutorialOverlay.png", Texture.class);
        getAssetManager().load("tutorials/level7TutorialOverlay.png", Texture.class);

        // Buttons
        getAssetManager().load("button/continue.png", Texture.class);
        //getAssetManager().load("button/newgame.png", Texture.class);



        // Assets for LevelSelectScreen
        getAssetManager().load("levelicons/background.png", Texture.class);
        getAssetManager().load("levelicons/done.png", Texture.class);
        getAssetManager().load("levelicons/locked.png", Texture.class);
        getAssetManager().load("levelicons/play.png", Texture.class);
        getAssetManager().load("levelicons/previousGalaxyButton.png", Texture.class);
        getAssetManager().load("levelicons/nextGalaxyButton.png", Texture.class);

        // Assets for the in-game menu
        getAssetManager().load("menu/blackBackground.jpg", Texture.class);
        getAssetManager().load("menu/cancelButton.png", Texture.class);
        getAssetManager().load("menu/levelSelectButton.png", Texture.class);
        //getAssetManager().load("menu/helpButton.png", Texture.class);
        getAssetManager().load("menu/resetButton.png", Texture.class);
        getAssetManager().load("menu/menuButton.png", Texture.class);
        getAssetManager().load("menu/soundOnButton.png", Texture.class);
        getAssetManager().load("menu/soundOffButton.png", Texture.class);


        // Assets for help screen
        //getAssetManager().load("menu/helpScreen0.png", Texture.class);
        //getAssetManager().load("menu/helpScreen1.png", Texture.class);

        */
    }

    // Called AFTER everything is finished in InitialLoadingScreen
    public void FinalizeAssets() {
        this.atlas = getAssetManager().get("GravityGridAtlas.atlas", TextureAtlas.class);
    }

    public void dispose() {
        getAssetManager().clear(); // Clear out all assets that have been loaded.
        getAssetManager().dispose(); // Dispose of all our assets
        this.assets = null;
    }


}
