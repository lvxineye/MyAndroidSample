package com.alex.android;

import java.util.Arrays;
import java.util.LinkedList;

import com.alex.android.Drag2RefreshView.OnRefreshListener;

import android.app.ListActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class DragToRefreshActivity extends ListActivity {

	private LinkedList<String> mListItems;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// Set a listener to be invoked when the list should be refreshed.
		((Drag2RefreshView) getListView()).setOnRefreshListener(new OnRefreshListener() {

			public void onRefresh() {
				new GetDataTask().execute();
			}
		});
		mListItems = new LinkedList<String>();
		mListItems.addAll(Arrays.asList(mStrings));
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mListItems);
		setListAdapter(adapter);
	}

	private class GetDataTask extends AsyncTask<Void, Void, String[]> {

		@Override
		protected String[] doInBackground(Void... params) {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			return mStrings;
		}

		@Override
		protected void onPostExecute(String[] result) {
			mListItems.addFirst("Added after refresh...");
			
			// Call onRefreshComplete when the list has been refreshed.
			((Drag2RefreshView)getListView()).onRefreshComplete();
			super.onPostExecute(result);
		}

	}

	private String[] mStrings = { "First String", "Second String" };

}