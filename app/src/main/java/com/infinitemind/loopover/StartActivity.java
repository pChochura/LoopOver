package com.infinitemind.loopover;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.AppCompatTextView;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;

import java.util.ArrayList;
import java.util.Random;

@SuppressLint("ClickableViewAccessibility")
public class StartActivity extends Activity implements View.OnTouchListener {

	public enum Direction {vertical, horizontal}

	private int duration = 200;
	private boolean instantRandom;
	private int MAP_SIZE = 5, TILE_SIZE, moves = 0;
	private AppCompatTextView winMessage;
	private AppCompatTextView[][] tiles;
	private int[][] prevValues;
	private Point startPos = new Point(), prevPos = new Point();
	private AnimatorSet set = new AnimatorSet();
	private ViewGroup mapLayout;
	private Direction prevDir, dir;
	private ArrayList<MoveHistory> movesHistory;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);

		MAP_SIZE = getIntent().getIntExtra("size", MainActivity.MIN_SIZE) + 1;
		instantRandom = getIntent().getBooleanExtra("instantRandom", false);

		tiles = new AppCompatTextView[MAP_SIZE][MAP_SIZE];
		prevValues = new int[MAP_SIZE][MAP_SIZE];

		mapLayout = findViewById(R.id.map);
		mapLayout.setOnTouchListener(this);

		Point size = new Point();
		getWindowManager().getDefaultDisplay().getSize(size);
		TILE_SIZE = size.x / (MAP_SIZE - 1);

		findViewById(R.id.undoButton).setAlpha(0.3f);
		findViewById(R.id.resetButton).setAlpha(0.3f);

		movesHistory = new ArrayList<>();

		generateMap();
		randomMap();
		showMovesCounter();
	}

	private void randomMap() {
		Random random = new Random();
		if(instantRandom) {
			int numberOfMoves = random.nextInt(50) + 25;
			for(int i = 0, prevDir = -1; i < numberOfMoves; i++) {
				int dir = random.nextInt(4);
				if(dir == 0 && prevDir == 1 || dir == 1 && prevDir == 0 || dir == 2 && prevDir == 3 || dir == 3 && prevDir == 2)
					dir = prevDir;
				switch(dir) {
					case 0:
						shiftLeftHorizontal(random.nextInt(MAP_SIZE), 0, null);
						break;
					case 1:
						shiftRightHorizontal(random.nextInt(MAP_SIZE), null);
						break;
					case 2:
						shiftLeftVertical(random.nextInt(MAP_SIZE), 0, null);
						break;
					case 3:
						shiftRightVertical(random.nextInt(MAP_SIZE), null);
						break;
				}
				prevDir = dir;
			}
			instantRandom = false;
			findViewById(R.id.resetButton).setAlpha(1);
		} else makeMove(0, random.nextInt(MAP_SIZE * 4) + MAP_SIZE * 10, random.nextInt(4), -1, random.nextInt(MAP_SIZE - 1),
				() -> findViewById(R.id.resetButton).animate().alpha(1f).setDuration(duration).start());
	}

	private void makeMove(int index, int maxIndex, int dir, int prevDir, int offset, Runnable callback) {
		ArrayList<Animator> items = new ArrayList<>();
		if(dir == 0 && prevDir == 1 || dir == 1 && prevDir == 0 || dir == 2 && prevDir == 3 || dir == 3 && prevDir == 2)
			dir = prevDir;
		switch(dir) {
			case 0:
				setTilesX(offset, 0, 0);
				tiles[MAP_SIZE - 1][offset].setText(tiles[0][offset].getText());
				for(int x = 0; x < MAP_SIZE; x++)
					items.add(ObjectAnimator.ofFloat(tiles[x][offset], "x", tiles[x][offset].getX(), tiles[x][offset].getX() - TILE_SIZE));
				break;
			case 1:
				setTilesX(offset, 0, 0);
				tiles[MAP_SIZE - 1][offset].setText(tiles[MAP_SIZE - 2][offset].getText());
				tiles[MAP_SIZE - 1][offset].setX(-TILE_SIZE);
				for(int x = 0; x < MAP_SIZE; x++)
					items.add(ObjectAnimator.ofFloat(tiles[x][offset], "x", tiles[x][offset].getX(), tiles[x][offset].getX() + TILE_SIZE));
				break;
			case 2:
				setTilesY(offset, 0, 0);
				tiles[offset][MAP_SIZE - 1].setText(tiles[offset][0].getText());
				for(int y = 0; y < MAP_SIZE; y++)
					items.add(ObjectAnimator.ofFloat(tiles[offset][y], "y", tiles[offset][y].getY(), tiles[offset][y].getY() - TILE_SIZE));
				break;
			case 3:
				setTilesY(offset, 0, 0);
				tiles[offset][MAP_SIZE - 1].setText(tiles[offset][MAP_SIZE - 2].getText());
				tiles[offset][MAP_SIZE - 1].setY(-TILE_SIZE);
				for(int y = 0; y < MAP_SIZE; y++)
					items.add(ObjectAnimator.ofFloat(tiles[offset][y], "y", tiles[offset][y].getY(), tiles[offset][y].getY() + TILE_SIZE));
				break;
		}
		AnimatorSet set = new AnimatorSet();
		set.playTogether(items);
		set.setDuration(duration / MAP_SIZE * MainActivity.MIN_SIZE);
		set.start();
		int finalDir = dir;
		set.addListener(new AnimEndListener(() -> {
			switch(finalDir) {
				case 0:
					shiftLeftHorizontal(offset, TILE_SIZE, null);
					setTilesX(offset, 0, 0);
					break;
				case 1:
					shiftRightHorizontal(offset, null);
					setTilesX(offset, 0, 0);
					break;
				case 2:
					shiftLeftVertical(offset, TILE_SIZE, null);
					setTilesY(offset, 0, 0);
					break;
				case 3:
					shiftRightVertical(offset, null);
					setTilesY(offset, 0, 0);
					break;
			}
			if(callback == null) {
				if(index < maxIndex - 1)
					makeMove(index + 1, maxIndex, finalDir, -1, offset, null);
			} else if(index < maxIndex - 1)
				makeMove(index + 1, maxIndex, new Random().nextInt(4), finalDir, new Random().nextInt(MAP_SIZE - 1), callback);
			else callback.run();
		}));
	}

	private void generateMap() {
		mapLayout.getLayoutParams().width = (MAP_SIZE - 1) * TILE_SIZE;
		mapLayout.getLayoutParams().height = (MAP_SIZE - 1) * TILE_SIZE;
		for(int i = 0; i < MAP_SIZE; i++) for(int j = 0; j < MAP_SIZE; j++) {
			AppCompatTextView tile = new AppCompatTextView(getApplicationContext());
			tile.setLayoutParams(new ViewGroup.LayoutParams(TILE_SIZE, TILE_SIZE));
			tile.setTextSize(TypedValue.COMPLEX_UNIT_PX, TILE_SIZE / 5);
			tile.setText(String.valueOf(i * (MAP_SIZE - 1) + j + 1));
			tile.setTextColor(getResources().getColor(R.color.colorPrimary));
			tile.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.arcon));
			tile.setGravity(Gravity.CENTER);
			mapLayout.addView(tiles[j][i] = tile);

			tile.setX(j * TILE_SIZE);
			tile.setY(i * TILE_SIZE);
		}
		winMessage = new AppCompatTextView(getApplicationContext());
		winMessage.setLayoutParams(new ViewGroup.LayoutParams((MAP_SIZE - 1) * TILE_SIZE, (MAP_SIZE - 1) * TILE_SIZE));
		winMessage.setGravity(Gravity.CENTER);
		winMessage.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
		winMessage.setTextColor(getResources().getColor(R.color.colorPrimary));
		winMessage.setTypeface(ResourcesCompat.getFont(getApplicationContext(), R.font.arcon));
		winMessage.setY(-(MAP_SIZE - 1) * TILE_SIZE);
		winMessage.setRotation(new Random().nextFloat() * 180 - 90);
		winMessage.setText(getResources().getString(R.string.win_message));
		winMessage.setAlpha(0);
		mapLayout.addView(winMessage);
	}

	private void showMovesCounter() {
		((AppCompatTextView) findViewById(R.id.moves)).setText(getResources().getString(R.string.moves, moves));
	}

	private void winAnimation() {
		findViewById(R.id.undoButton).animate().alpha(0.3f).setDuration(duration).start();

		ArrayList<Animator> items = new ArrayList<>();
		for(int i = 0; i < MAP_SIZE - 1; i++) for(int j = 0; j < MAP_SIZE - 1; j++) {
			ObjectAnimator a1 = ObjectAnimator.ofFloat(tiles[j][i], "rotation", new Random().nextFloat() * 180 - 90);
			ObjectAnimator a2 = ObjectAnimator.ofFloat(tiles[j][i], "y", tiles[MAP_SIZE - 1][MAP_SIZE - 1].getY());
			ObjectAnimator a3 = ObjectAnimator.ofFloat(tiles[j][i], "x", tiles[j][i].getX() + (new Random().nextFloat() - 0.5f) * TILE_SIZE * 0.5f);
			a1.setStartDelay(new Random().nextInt(duration * 3));
			a2.setStartDelay(new Random().nextInt(duration * 3));
			a3.setStartDelay(new Random().nextInt(duration * 3));
			items.add(a1);
			items.add(a2);
			items.add(a3);
		}
		ObjectAnimator a1 = ObjectAnimator.ofFloat(winMessage, "y", -(MAP_SIZE - 1) * TILE_SIZE, 0);
		ObjectAnimator a2 = ObjectAnimator.ofFloat(winMessage, "rotation", new Random().nextInt(180) - 90, 0);
		ObjectAnimator a3 = ObjectAnimator.ofFloat(winMessage, "alpha", 1);
		a1.setStartDelay(duration * 5);
		a2.setStartDelay(duration * 5);
		items.add(a1);
		items.add(a2);
		items.add(a3);
		AnimatorSet set = new AnimatorSet();
		set.playTogether(items);
		set.setDuration(duration * 10);
		set.setInterpolator(new AccelerateInterpolator(6f));
		set.start();
	}

	public void clickReset(View view) {
		if(findViewById(R.id.resetButton).getAlpha() == 1) {
			for(int i = 0; i < MAP_SIZE; i++) {
				setTilesX(i, 0, 0);
				setTilesY(i, 0, 0);
				for(int j = 0; j < MAP_SIZE; j++) tiles[j][i].setRotation(0);
			}
			winMessage.animate().alpha(0).setDuration(duration).start();
			findViewById(R.id.undoButton).animate().alpha(0.3f).setDuration(duration).start();
			findViewById(R.id.resetButton).animate().rotationBy(180).alpha(0.3f).setDuration(duration).start();
			moves = 0;
			showMovesCounter();
			randomMap();
			movesHistory = new ArrayList<>();
		}
	}

	public void clickUndo(View view) {
		if(moves > 0 && findViewById(R.id.undoButton).getAlpha() == 1 && !movesHistory.isEmpty()) {
			moves--;
			showMovesCounter();
			if(movesHistory.size() == 1)
				findViewById(R.id.undoButton).animate().alpha(0.3f).setDuration(duration).start();

            MoveHistory m = movesHistory.get(movesHistory.size() - 1);
            int pos = m.getPos(), offset = m.getOffset();
            if(m.getDirection() == Direction.horizontal) {
				boolean b = offset > (MAP_SIZE - 1) / 2;
				makeMove(0, b ? MAP_SIZE - 1 - offset : offset, b ? 1 : 0, -1, pos, null);
			} else {
				boolean b = offset > (MAP_SIZE - 1) / 2;
				makeMove(0, b ? MAP_SIZE - 1 - offset : offset, b ? 3 : 2, -1, pos, null);
			}
			movesHistory.remove(m);
		}
	}

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		Point pos = new Point((int) event.getX(), (int) event.getY());
		int x, y;
		if(findViewById(R.id.resetButton).getAlpha() == 1 && winMessage.getAlpha() != 1) {
			switch(event.getAction()) {
				case MotionEvent.ACTION_DOWN:
					for(int i = 0; i < MAP_SIZE; i++)
						for(int j = 0; j < MAP_SIZE; j++)
							prevValues[j][i] = Integer.parseInt(tiles[j][i].getText().toString());
					if(!set.isRunning()) {
						startPos.set(pos.x, pos.y);
						prevPos.set(pos.x, pos.y);
					}
					break;
				case MotionEvent.ACTION_MOVE:
					if(!set.isRunning()) {
						int xOff = Math.abs(pos.x - startPos.x);
						int yOff = Math.abs(pos.y - startPos.y);
						x = startPos.x / TILE_SIZE;
						y = startPos.y / TILE_SIZE;

						if(dir == Direction.vertical || (dir == null && yOff > xOff + 20)) {
							dir = Direction.vertical;

							if(pos.y < prevPos.y)
								shiftLeftVertical(x, 1, () -> startPos.y -= TILE_SIZE);
							else if(pos.y > prevPos.y && tiles[x][MAP_SIZE - 1].getY() > TILE_SIZE * (MAP_SIZE - 1))
								shiftRightVertical(x, () -> startPos.y += TILE_SIZE);

							setTilesY(x, 0, pos.y - startPos.y);
						} else if(dir == Direction.horizontal || (dir == null && xOff > yOff + 20)) {
							dir = Direction.horizontal;

							if(pos.x < prevPos.x)
								shiftLeftHorizontal(y, 1, () -> startPos.x -= TILE_SIZE);
							else if(pos.x > prevPos.x && tiles[MAP_SIZE - 1][y].getX() > TILE_SIZE * (MAP_SIZE - 1))
								shiftRightHorizontal(y, () -> startPos.x += TILE_SIZE);

							setTilesX(y, 0, pos.x - startPos.x);
						}
						prevPos.set(pos.x, pos.y);
					}
					break;
				case MotionEvent.ACTION_UP:
					if(!set.isRunning()) {
						ArrayList<Animator> items = new ArrayList<>();
						boolean before = false;
						x = startPos.x / TILE_SIZE;
						y = startPos.y / TILE_SIZE;
						if(dir == Direction.vertical) {
							float offset = (startPos.y - pos.y) / (float) TILE_SIZE;
							if(before = (tiles[x][0].getY() < -TILE_SIZE / 2f)) {
								offset -= 1;

								shiftLeftVertical(x, 2f, null);
								setTilesY(x, offset, 0);
							}
							for(int i = 0; i < MAP_SIZE; i++)
								items.add(ObjectAnimator.ofFloat(tiles[x][i], "y", tiles[x][i].getY(), tiles[x][i].getY() + offset * TILE_SIZE));
						} else if(dir == Direction.horizontal) {
							float offset = (startPos.x - pos.x) / (float) TILE_SIZE;
							if(before = (tiles[0][y].getX() < -TILE_SIZE / 2f)) {
								offset -= 1;

								shiftLeftHorizontal(y, 2f, null);
								setTilesX(y, offset, 0);
							}
							for(int i = 0; i < MAP_SIZE; i++)
								items.add(ObjectAnimator.ofFloat(tiles[i][y], "x", tiles[i][y].getX(), tiles[i][y].getX() + offset * TILE_SIZE));
						}
						final boolean fBefore = before;
						set.playTogether(items);
						set.setDuration(duration);
						set.start();
						set.addListener(new AnimEndListener(() -> {
							if(!fBefore)
								for(int i = 0; i < MAP_SIZE; i++)
									for(int j = 0; j < MAP_SIZE; j++) {
										tiles[j][i].setX(j * TILE_SIZE);
										tiles[j][i].setY(i * TILE_SIZE);
									}
							set = new AnimatorSet();
							boolean moved = false, won = true;
							for(int i = 0; i < MAP_SIZE - 1; i++) for(int j = 0; j < MAP_SIZE - 1; j++) {
								int value = Integer.parseInt(tiles[j][i].getText().toString());
								if(prevValues[j][i] != value) moved = true;
								if(value != i * (MAP_SIZE - 1) + j + 1) won = false;
							}
							if(won) {
								winAnimation();
							} else if(moved) {
							    int offset = 0, position;
							    if(prevDir == Direction.horizontal) {
                                    position = y;
                                    for(int i = 0; i < MAP_SIZE - 1; i++) if(prevValues[0][y] == Integer.parseInt(tiles[i][y].getText().toString())) offset = i;
                                } else {
                                    position = x;
                                    for(int i = 0; i < MAP_SIZE - 1; i++) if(prevValues[x][0] == Integer.parseInt(tiles[x][i].getText().toString())) offset = i;
                                }

							    movesHistory.add(new MoveHistory(position, offset, prevDir));

								moves++;
								showMovesCounter();
								findViewById(R.id.undoButton).animate().alpha(1).setDuration(duration).start();
							}
						}));
						prevDir = dir;
						dir = null;
					}
					break;
			}
		}
		return true;
	}

	private void setTilesX(int y, float offset, int headStart) {
		for(int i = 0; i < MAP_SIZE; i++)
			tiles[i][y].setX(headStart + (i - offset) * TILE_SIZE);
	}

	private void setTilesY(int x, float offset, int headStart) {
		for(int i = 0; i < MAP_SIZE; i++)
			tiles[x][i].setY(headStart + (i - offset) * TILE_SIZE);
	}

	private void shiftLeftHorizontal(int y, float bias, @Nullable Runnable callback) {
		if(tiles[0][y].getX() <= -TILE_SIZE / bias) {
			for(int i = 0; i < MAP_SIZE - 1; i++)
				tiles[i][y].setText(tiles[i + 1][y].getText());
			if(callback != null) callback.run();
		}
		tiles[MAP_SIZE - 1][y].setText(tiles[0][y].getText());
	}

	private void shiftLeftVertical(int x, float bias, @Nullable Runnable callback) {
		if(tiles[x][0].getY() <= -TILE_SIZE / bias) {
			for(int i = 0; i < MAP_SIZE - 1; i++)
				tiles[x][i].setText(tiles[x][i + 1].getText());
			if(callback != null) callback.run();
		}
		tiles[x][MAP_SIZE - 1].setText(tiles[x][0].getText());
	}

	private void shiftRightHorizontal(int y, @Nullable Runnable callback) {
		for(int i = MAP_SIZE - 1; i > 0; i--)
			tiles[i][y].setText(tiles[i - 1][y].getText());
		tiles[0][y].setText(tiles[MAP_SIZE - 1][y].getText());
		if(callback != null) callback.run();
	}

	private void shiftRightVertical(int x, @Nullable Runnable callback) {
		for(int i = MAP_SIZE - 1; i > 0; i--)
			tiles[x][i].setText(tiles[x][i - 1].getText());
		tiles[x][0].setText(tiles[x][MAP_SIZE - 1].getText());
		if(callback != null) callback.run();
	}
}
