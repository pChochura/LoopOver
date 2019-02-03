package com.infinitemind.loopover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;

public class MainActivity extends Activity {

	public static final String MIN_MOVES = "MIN_MOVES";
	public static final int MIN_SIZE = 3, MAX_SIZE = 10;
	private int currentSize = 4;

	@Override
	protected void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		findViewById(R.id.startButton).setOnLongClickListener(v -> {
			startActivity(new Intent(this, StartActivity.class).putExtra("size", currentSize).putExtra("instantRandom", true));
			return true;
		});

		showCurrentSize();
		showMinMoves();
	}

	public void clickPlay(View view) {
		startActivity(new Intent(this, StartActivity.class).putExtra("size", currentSize));
	}

	public void clickMinus(View view) {
		currentSize = Math.max(currentSize - 1, MIN_SIZE);
		showCurrentSize();
		showMinMoves();
	}

	public void clickPlus(View view) {
		currentSize = Math.min(currentSize + 1, MAX_SIZE);
		showCurrentSize();
		showMinMoves();
	}

	private void showCurrentSize() {
		((AppCompatTextView) findViewById(R.id.size)).setText(String.valueOf(currentSize));
	}

	private void showMinMoves() {
		int minMoves = getSharedPreferences(MIN_MOVES, MODE_PRIVATE).getInt(MIN_MOVES + "_" + currentSize, -1);
		AppCompatTextView minMovesText = findViewById(R.id.minMovesText);
		minMovesText.setVisibility(minMoves == -1 ? View.GONE : View.VISIBLE);
		minMovesText.setText(getResources().getString(R.string.min_moves, minMoves));
	}

	@Override
	protected void onResume() {
		super.onResume();
		showMinMoves();
	}
}
