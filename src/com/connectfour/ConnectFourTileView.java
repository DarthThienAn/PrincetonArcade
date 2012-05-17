/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* Adaptation of a generic class for a TileSet interface */

package com.connectfour;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

/**
 * TileView: a View-variant designed to set up a grid
 * 
 */
public class ConnectFourTileView extends View {

	/**
	 * Parameters controlling the size of the tiles and their range within view.
	 * Width/Height are in pixels, and Drawables will be scaled to fit to these
	 * TileNum. X/Y Tile Counts are the number of tiles that will be drawn.
	 */

	protected static int mTileSize = 0;

	// 6 x 7 grid
	protected final static int xTileNum = 7;
	protected final static int yTileNum = 6;
	protected static int xDimensions;
	protected static int yDimensions;

	protected static int xOffset;
	protected static int yOffset;
	protected static int barOffset;
	protected static int headOffset;


	/**
	 * A hash that maps integer handles specified by the subclasser to the
	 * drawable that will be used for that reference
	 */
	private Bitmap[] mTileArray;

	/**
	 * A two-dimensional array of integers in which the number represents the
	 * index of the tile that should be drawn at that locations
	 */
	private int[][] mTileGrid;

	private final Paint mPaint = new Paint();

	public ConnectFourTileView (Context context)
	{
		super(context);
	}

	public ConnectFourTileView (Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}	

	public ConnectFourTileView (Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		DisplayMetrics display = getResources().getDisplayMetrics();
		int w = display.widthPixels;
		int h = display.heightPixels;

		mTileSize = (int) (Math.floor(w / xTileNum) * .9);
		xDimensions = w;
		yDimensions = h;
		headOffset = 200;

		switch (display.densityDpi) {
		case DisplayMetrics.DENSITY_HIGH:
			barOffset = 48;
			break;
		case DisplayMetrics.DENSITY_MEDIUM:
			barOffset = 36;
			break;
		case DisplayMetrics.DENSITY_LOW:
			barOffset = 24;
			break;
		default:
			barOffset = 0;	
		}
	}

	/**
	 * Rests the internal array of Bitmaps used for drawing tiles, and sets the
	 * maximum index of tiles to be inserted
	 * 
	 * @param tilecount
	 */

	public void resetTiles(int tilecount) {
		mTileArray = new Bitmap[tilecount];
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		xOffset = ((w - (mTileSize * xTileNum)) / 2);
		yOffset = ((h - (mTileSize * yTileNum)) / 2);

		mTileGrid = new int[xTileNum][yTileNum];
		clearTiles();
	}

	/**
	 * Function to set the specified Drawable as the tile for a particular
	 * integer key.
	 * 
	 * @param key
	 * @param tile
	 */
	public void loadTile(int key, Drawable tile) {
		Bitmap bitmap = Bitmap.createBitmap(mTileSize, mTileSize,
				Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		tile.setBounds(0, 0, mTileSize, mTileSize);
		tile.draw(canvas);

		mTileArray[key] = bitmap;
	}

	/**
	 * Resets all tiles to 0 (empty)
	 * 
	 */
	public void clearTiles() {
		for (int x = 0; x < xTileNum; x++) {
			for (int y = 0; y < yTileNum; y++) {
				setTile(0, x, y);
			}
		}
	}

	/**
	 * Used to indicate that a particular tile (set with loadTile and referenced
	 * by an integer) should be drawn at the given x/y coordinates during the
	 * next invalidate/draw cycle.
	 * 
	 * @param tileindex
	 * @param x
	 * @param y
	 */
	public void setTile(int tileindex, int x, int y) {
		if (mTileGrid == null)
			mTileGrid = new int[xTileNum][yTileNum];

		mTileGrid[x][y] = tileindex;
	}

	@Override
	public void onDraw(Canvas canvas) {
		for (int x = 0; x < xTileNum; x += 1) {
			for (int y = 0; y < yTileNum; y += 1) {
				if (mTileGrid[x][y] > 0) {
					canvas.drawBitmap(mTileArray[mTileGrid[x][y]], xOffset + x
							* mTileSize, yOffset + y * mTileSize, mPaint);
				}
			}
		}
	}
}