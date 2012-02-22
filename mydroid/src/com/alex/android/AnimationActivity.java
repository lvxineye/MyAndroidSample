package com.alex.android;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class AnimationActivity extends Activity implements OnClickListener {

	private Spinner mAnimSpinner;
	private Button mOpenOtherBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.animation_activity);

		mAnimSpinner = (Spinner) findViewById(R.id.animation_sp);
		mOpenOtherBtn = (Button) findViewById(R.id.other_button);
		mOpenOtherBtn.setOnClickListener(this);

		// Get the content of the Spinner by the Resource file
		String[] ls = getResources().getStringArray(R.array.anim_type);
		List<String> list = new ArrayList<String>();
		// write the content of the Array to the List
		for (int i = 0; i < ls.length; i++) {
			list.add(ls[i]);
		}
		ArrayAdapter<String> animType = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_dropdown_item, list);
		animType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		mAnimSpinner.setAdapter(animType);
		mAnimSpinner.setSelection(0);

	}

	public void onClick(View v) {
		if (mOpenOtherBtn == v) {
			Intent intent = new Intent();
			intent.setClass(AnimationActivity.this, AnotherActivity.class);
			startActivity(intent);

			getSpinnerAction();
		}
	}

	private void getSpinnerAction() {
		switch (mAnimSpinner.getSelectedItemPosition()) {
		case 0:
			/*
			 * This method must be token after startActivity() and finish().
			 * first param: the animation when leave the first Activity, second
			 * param:the animation when enter the second Activity.
			 */
			overridePendingTransition(R.drawable.fade, R.drawable.hold);
			break;
		case 1:
			overridePendingTransition(R.drawable.my_scale_action, R.drawable.my_alpha_action);
			break;
		case 2:
			overridePendingTransition(R.drawable.scale_rotate, R.drawable.my_alpha_action);
			break;
		case 3:
			overridePendingTransition(R.drawable.scale_translate_rotate, R.drawable.my_alpha_action);
			break;
		case 4:
			overridePendingTransition(R.drawable.scale_translate, R.drawable.my_alpha_action);
			break;
		case 5:
			overridePendingTransition(R.drawable.hyperspace_in, R.drawable.hyperspace_out);
			break;
		case 6:
			overridePendingTransition(R.drawable.push_left_in, R.drawable.push_left_out);
			break;
		case 7:
			overridePendingTransition(R.drawable.push_up_in, R.drawable.push_up_out);
			break;
		case 8:
			overridePendingTransition(R.drawable.slide_left, R.drawable.slide_right);
			break;
		case 9:
			overridePendingTransition(R.drawable.wave_scale, R.drawable.my_alpha_action);
			break;
		case 10:
			overridePendingTransition(R.drawable.zoom_enter, R.drawable.zoom_exit);
			break;
		case 11:
			overridePendingTransition(R.drawable.slide_up_in, R.drawable.slide_down_out);
			break;
		default:
			break;
		}
	}

}
