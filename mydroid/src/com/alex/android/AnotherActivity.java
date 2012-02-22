package com.alex.android;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public class AnotherActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.another_activity);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(KeyEvent.KEYCODE_BACK == keyCode && 0 == event.getRepeatCount()) {
			finish();
			overridePendingTransition(R.drawable.slide_up_in, R.drawable.slide_down_out);
			return false;
		}
		return false;
	}

}
