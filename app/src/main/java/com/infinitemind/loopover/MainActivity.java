package com.infinitemind.loopover;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MainActivity extends Activity {

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
	}

	public void clickPlay(View view) {
		startActivity(new Intent(this, StartActivity.class).putExtra("size", currentSize));
	}

	public void clickMinus(View view) {
		currentSize = Math.max(currentSize - 1, MIN_SIZE);
		showCurrentSize();
	}

	public void clickPlus(View view) {
		currentSize = Math.min(currentSize + 1, MAX_SIZE);
		showCurrentSize();
	}

	private void showCurrentSize() {
		((AppCompatTextView) findViewById(R.id.size)).setText(String.valueOf(currentSize));
	}
}
