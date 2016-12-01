package com.turkey.gravitygrid;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGeneratorLoader;
import com.badlogic.gdx.graphics.g2d.freetype.FreetypeFontLoader;
import com.badlogic.gdx.utils.Json;

/**
 * Created by lawsonje on 11/30/2016.
 */

public final class AssetLoader {

    AssetManager assets = null;

    private int fontSize = 60;
    
    AssetLoader(GravityGrid game, AssetManager theAssetManager) {
        assets = theAssetManager;

        // Pass over any variables from game we need
        fontSize = game.fontSize;

        // Load initial assets before heading to initial load screen
        //LoadInitialAssets();

        //LoadAllAssets();
    }

    public AssetManager getAssetManager() {
        return this.assets;
    }

    // Initial assets are those that are required to show a loading screen
    // It always ends with finishLoading();
    public void LoadInitialAssets() {

        // Load the loading screen astronaut and the font first
        getAssetManager().load("spaceturkeylogosquare.png", Texture.class);

        // Setup asset manager for freetype fonts
        getAssetManager().setLoader(FreeTypeFontGenerator.class, new FreeTypeFontGeneratorLoader(new InternalFileHandleResolver()));
        getAssetManager().setLoader(BitmapFont.class, ".ttf", new FreetypeFontLoader(new InternalFileHandleResolver()));

        // Generate our regularFont
        FreetypeFontLoader.FreeTypeFontLoaderParameter regularFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        regularFontParams.fontFileName = "turkey.ttf";
        regularFontParams.fontParameters.size = fontSize;
        getAssetManager().load("turkey.ttf", BitmapFont.class, regularFontParams);

        // Generate our pixelFont (Our big fancy one)
        FreetypeFontLoader.FreeTypeFontLoaderParameter pixelFontParams = new FreetypeFontLoader.FreeTypeFontLoaderParameter();
        pixelFontParams.fontFileName = "agencyfb.ttf";
        pixelFontParams.fontParameters.size = fontSize+16;
        getAssetManager().load("agencyfb.ttf", BitmapFont.class, pixelFontParams);

        getAssetManager().finishLoading(); // Wait for all the assets to load, then go ahead and get our initial assets for the loading screen
    }

    public void LoadAllAssets() {

        // Load all assetmanager assets
        //getAssetManager().load("consoleBeep.wav", Sound.class);
        getAssetManager().load("sounds/tileDeselectSound.wav", Sound.class);
        getAssetManager().load("sounds/goodMoveSound.wav", Sound.class);
        getAssetManager().load("sounds/cannotMoveSound.wav", Sound.class);
        //getAssetManager().load("outOfMovesSound.wav", Sound.class);
        getAssetManager().load("sounds/levelCompleteSound.ogg", Sound.class);

        // Sound assets

        getAssetManager().load("sounds/inGameMenuOpenButtonSound.wav", Sound.class);
        getAssetManager().load("sounds/inGameMenuButtonSound.wav", Sound.class);
        getAssetManager().load("sounds/inGameMenuResetLevelButtonSound.wav", Sound.class);
        getAssetManager().load("sounds/levelSelectMenuPlayLevelButtonSound.wav", Sound.class);
        getAssetManager().load("sounds/nextGalaxyButtonSound.wav", Sound.class);
        getAssetManager().load("sounds/previousGalaxyButtonSound.wav", Sound.class);
        getAssetManager().load("sounds/tileSelectSound.wav", Sound.class);

        // Tutorial assets
        getAssetManager().load("tap-here.png", Texture.class);

        // Main menu assets
        getAssetManager().load("mainmenubg.png", Texture.class);

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
        getAssetManager().load("bg2.png", Texture.class);
        getAssetManager().load("bg3.png", Texture.class);
        getAssetManager().load("singularity0.png", Texture.class);
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
        getAssetManager().load("tileOverlayAnim0.png", Texture.class);
        getAssetManager().load("tileOverlayAnim1.png", Texture.class);
        getAssetManager().load("tileOverlayAnim2.png", Texture.class);
        getAssetManager().load("tileOverlayAnim3.png", Texture.class);
        getAssetManager().load("tileOverlayAnim4.png", Texture.class);
        getAssetManager().load("tileOverlayAnim5.png", Texture.class);
        getAssetManager().load("tileOverlayAnim6.png", Texture.class);
        getAssetManager().load("buttonFail.png", Texture.class);
        getAssetManager().load("buttonLevelComplete.png", Texture.class);
        getAssetManager().load("levelCompleteTrophy.png", Texture.class);
        getAssetManager().load("galaxyOverlay.png", Texture.class);

        getAssetManager().load("levelMessageBackground.png", Texture.class);

        // Tutorial Overlays
        getAssetManager().load("tutorials/level1TutorialOverlay.png", Texture.class);
        getAssetManager().load("tutorials/level2TutorialOverlay.png", Texture.class);
        getAssetManager().load("tutorials/level3TutorialOverlay.png", Texture.class);
        getAssetManager().load("tutorials/level7TutorialOverlay.png", Texture.class);

        // Buttons
        getAssetManager().load("button/continue.png", Texture.class);
        getAssetManager().load("button/newgame.png", Texture.class);

        // Particle effects
        getAssetManager().load("particles/starfield.p", ParticleEffect.class);
        getAssetManager().load("particles/goodmovestarburst.p", ParticleEffect.class);
        getAssetManager().load("particles/badmovestarburst.p", ParticleEffect.class);

        // Assets for LevelSelectScreen
        getAssetManager().load("levelicons/background.png", Texture.class);
        getAssetManager().load("levelicons/done.png", Texture.class);
        getAssetManager().load("levelicons/locked.png", Texture.class);
        getAssetManager().load("levelicons/play.png", Texture.class);
        getAssetManager().load("levelicons/previousGalaxyButton.png", Texture.class);
        getAssetManager().load("levelicons/nextGalaxyButton.png", Texture.class);

        // Assets for the in-game menu
        getAssetManager().load("menu/blackBackground.png", Texture.class);
        getAssetManager().load("menu/cancelButton.png", Texture.class);
        getAssetManager().load("menu/levelSelectButton.png", Texture.class);
        getAssetManager().load("menu/helpButton.png", Texture.class);
        getAssetManager().load("menu/resetButton.png", Texture.class);
        getAssetManager().load("menu/menuButton.png", Texture.class);
        getAssetManager().load("doitnow.png", Texture.class);

        // Assets for help screen
        //getAssetManager().load("menu/helpScreen0.png", Texture.class);
        //getAssetManager().load("menu/helpScreen1.png", Texture.class);
    }

    // This is called onResume to reload everything
    public void ReloadAllAssets() {

    }

    public void dispose() {
        getAssetManager().clear(); // Clear out all assets that have been loaded.
        getAssetManager().dispose(); // Dispose of all our assets
        this.assets = null;
    }


}
