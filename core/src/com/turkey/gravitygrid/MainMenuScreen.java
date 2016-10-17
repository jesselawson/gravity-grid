

/*
 * Copyright (c) 2016 Jesse Lawson. All Rights Reserved. No part of this code may be redistributed, reused, or otherwise used in any way, shape, or form without written permission from the author.
 */

package com.turkey.gravitygrid;

import java.util.Iterator;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.Logger;

public class MainMenuScreen implements Screen {

	int screenWidth; // set on create so we dont have to keep calling gdx getheight
	int screenHeight;
	
	Rectangle finger; 
	
  	private GravityGrid game;
	
	OrthographicCamera camera;
	
	Texture mainMenuBackground;
	TextureRegion elements;
	Texture mainMenuPresents;
	Texture mainMenuGravityGrid;
	
	int mainMenuState; 
	
	public MainMenuScreen(GravityGrid game) {
	
		this.game = game;
		
		mainMenuState = 0; 	// 0 - splash screen
							// 1 - level select
		
		// create the camera
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 480, 800);
		
		mainMenuBackground = new Texture(Gdx.files.internal("mainmenubg.png"));
		//mainMenuPresents = new Texture(Gdx.files.internal("mainmenupart0.png"));
		//mainMenuGravityGrid = new Texture(Gdx.files.internal("mainmenupart1.png"));
		//elements = new TextureRegion;
		elements = new TextureRegion(mainMenuBackground);
		//elements[1] = new TextureRegion(mainMenuPresents);
		//elements[2] = new TextureRegion(mainMenuGravityGrid);
		
		
		
	}

	@Override
	public void render(float delta) {
		
		int sw = Gdx.graphics.getWidth();
		int sh = Gdx.graphics.getHeight(); 
		
		// Clear to black
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// tell the camera to update its matrices.
		camera.update();

		// tell the SpriteBatch to render in the
		// coordinate system specified by the camera.
		game.batch.setProjectionMatrix(camera.combined);
		 
		game.batch.begin();
		
			game.batch.setColor(1f,1f,1f,1f);
			game.batch.draw(elements, 0,0,sw/2,sh/2,480,800,1.0f,1.0f,0.0f);
			//game.batch.draw(elements[1], 0,0,sw/2,sh/2,480,800,1.0f,1.0f,0.0f);
			//game.batch.draw(elements[2], 0,0,sw/2,sh/2,480,800,1.0f,1.0f,0.0f);
			game.font.setColor(game.colorBlue);
				
			game.font.draw(game.batch, "Touch Anywhere to Start", 50, 50);
				
			game.batch.end();
				
			// Basically touch anywhere to begin
				
			if(Gdx.input.isTouched()){
				game.setScreen(new PlayingScreen(game));
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
		mainMenuBackground.dispose();
		mainMenuPresents.dispose();
		mainMenuGravityGrid.dispose(); 
		
	}

}