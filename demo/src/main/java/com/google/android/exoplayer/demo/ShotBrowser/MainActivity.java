package com.google.android.exoplayer.demo.ShotBrowser;

import java.io.FileNotFoundException;
import java.util.ArrayList;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.google.android.exoplayer.demo.PlayerActivity;
import com.google.android.exoplayer.demo.R;


public class MainActivity extends Activity {

	private SitesAdapter mAdapter;
	private ListView sitesList;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i("StackSites", "OnCreate()");
		setContentView(R.layout.activity_main);

		//Get reference to our ListView
		sitesList = (ListView)findViewById(R.id.sitesList);

		//Set the click listener to launch the browser when a row is clicked.
		sitesList.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View v, int pos,long id) {
				String url = mAdapter.getItem(pos).getLink();
				String video_id = mAdapter.getItem(pos).getId();
				String video_name = mAdapter.getItem(pos).getName();
				Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
				String link = Uri.parse(url).toString();
				intent.putExtra("video_id", video_id);
				intent.putExtra("video_name", video_name);
				intent.putExtra("link", link);
				startActivity(intent);

			}

		});

		/*
		 * If network is available download the xml from the Internet.
		 * If not then try to use the local file from last time.
		 */
		if(isNetworkAvailable()){
			Log.i("StackSites", "starting download Task");
			SitesDownloadTask download = new SitesDownloadTask();
			download.execute();
		}else{
			mAdapter = new SitesAdapter(getApplicationContext(), -1, SitesXmlPullParser.getStackSitesFromFile(MainActivity.this));
			sitesList.setAdapter(mAdapter);
		}

	}

	//Helper method to determine if Internet connection is available.
	private boolean isNetworkAvailable() {
	    ConnectivityManager connectivityManager
	          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
	    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	/*
	 * AsyncTask that will download the xml file for us and store it locally.
	 * After the download is done we'll parse the local file.
	 */
	private class SitesDownloadTask extends AsyncTask<Void, Void, Void>{

		@Override
		protected Void doInBackground(Void... arg0) {
			//Download the file
			try {
				String serverUrl = getResources().getString(R.string.server)  + "db.php?action=shots";
				Log.d("serverUrl", serverUrl);

				Downloader.DownloadFromUrl(serverUrl , openFileOutput("StackSites.xml", Context.MODE_PRIVATE));
				//Downloader.DownloadFromUrl("http://schiho.com/kss/csvTest_v001.xml", openFileOutput("StackSites.xml", Context.MODE_PRIVATE));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void result){
			//setup our Adapter and set it to the ListView.
			mAdapter = new SitesAdapter(MainActivity.this, -1, SitesXmlPullParser.getStackSitesFromFile(MainActivity.this));
			sitesList.setAdapter(mAdapter);
			Log.i("StackSites", "adapter size = "+ mAdapter.getCount());
		}
	}

}
