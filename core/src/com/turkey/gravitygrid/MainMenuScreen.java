

/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;

public class MainMenuScreen implements Screen {

	// A custom point-in-rectangle collision checker
	public static boolean pointInRectangle (Rectangle r, float x, float y) {
		return r.x <= x && r.x + r.width >= x && r.y <= y && r.y + r.height >= y;
	}

	int screenWidth; // set on create so we dont have to keep calling gdx getheight
	int screenHeight;
	
	Rectangle finger; 
	
  	private GravityGrid game;
	
	OrthographicCamera camera;
	
	Texture mainMenuBackground;
	TextureRegion mainMenuBackgroundRegion;
	Texture buttonNewGame;
	TextureRegion buttonNewGameRegion;
	Rectangle buttonNewGameRect;
	Texture buttonContinue;
	TextureRegion buttonContinueRegion;
	Rectangle buttonContinueRect;
	Texture doItNowImage;

	int mainMenuState; 
	
	public MainMenuScreen(GravityGrid game) {
	
		this.game = game;
		
		mainMenuState = 0; 	// 0 - splash screen
							// 1 - level select

		// create the camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, game.screenWidth, game.screenHeight);
		
		mainMenuBackground = game.assets.get("mainmenubg.png", Texture.class);
		mainMenuBackgroundRegion = new TextureRegion(mainMenuBackground);
		buttonNewGame = game.assets.get("button/newgame.png", Texture.class);
		buttonNewGameRegion = new TextureRegion(buttonNewGame);
		buttonContinue = game.assets.get("button/continue.png", Texture.class);
		buttonContinueRegion = new TextureRegion(buttonContinue);
		doItNowImage = game.assets.get("doitnow.png", Texture.class);

		buttonNewGameRect = new Rectangle((Gdx.graphics.getWidth()/4)-200, (Gdx.graphics.getHeight()/3)-200, 400, 400);

		buttonContinueRect = new Rectangle((Gdx.graphics.getWidth()/2)-200, (Gdx.graphics.getHeight()/3)-200, 400, 400);

	}

	@Override
	public void render(float delta) {
		
		int sw = Gdx.graphics.getWidth();
		int sh = Gdx.graphics.getHeight();

		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);

		game.batch.begin();
		
			game.batch.setColor(1f,1f,1f,1f);

			game.batch.setColor(1f,1f,1f,1f);

			game.batch.draw(mainMenuBackgroundRegion, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

			game.batch.draw(buttonContinueRegion, buttonContinueRect.x, buttonContinueRect.y, buttonContinueRect.width/2, buttonContinueRect.height/2, buttonContinueRect.width, buttonContinueRect.height, 1.0f, 1.0f, 0.0f);

			game.batch.draw(doItNowImage, 0, 10, this.screenWidth, 285.0f);

			//game.batch.draw(buttonNewGameRegion, buttonNewGameRect.x, buttonNewGameRect.y, buttonNewGameRect.width/2, buttonNewGameRect.height/2, buttonNewGameRect.width, buttonNewGameRect.height, 1.0f, 1.0f, 0.0f);
				
			game.batch.end();
				

			// Are we touching the screen? (gross. go wash your hands. you don't know where that screen has been.)
			if(Gdx.input.justTouched()){
				// Check which button is pressed
				Vector3 finger = new Vector3();
				camera.unproject(finger.set(Gdx.input.getX(), Gdx.input.getY(), 0));
				if(pointInRectangle(buttonNewGameRect, finger.x, finger.y)) {
					//game.setScreen(new NewGameScreen(game)); // maybe NewGameScreen goes through the intro story?
				} else if(pointInRectangle(buttonContinueRect, finger.x, finger.y)) {
					game.setScreen(new LevelSelectScreen(game)); // Will pickup based on what we read from the player files (the ini)
					// Really, this should be sending us to the LevelSelectScreen
				}

			}
			
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void show() {
		// start the playback of the background music
		// when the screen is shown
		//scannerHum.play();
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
		//mainMenuBackground.dispose();
		
	}

}