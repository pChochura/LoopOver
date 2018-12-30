package com.infinitemind.loopover;

import android.animation.*;

public class AnimEndListener implements Animator.AnimatorListener {

	private Runnable callback;

	public AnimEndListener(Runnable callback) {
		this.callback = callback;
	}
	
	@Override
	public void onAnimationStart(Animator p1) {
	}

	@Override
	public void onAnimationEnd(Animator p1) {
		callback.run();
	}

	@Override
	public void onAnimationCancel(Animator p1) {
	}

	@Override
	public void onAnimationRepeat(Animator p1) {
	}

}
