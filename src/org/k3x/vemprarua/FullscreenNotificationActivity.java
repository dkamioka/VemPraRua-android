package org.k3x.vemprarua;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TextView;

public class FullscreenNotificationActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_fullscreen_notification);

		String title = getIntent().getStringExtra("title");
		String message = getIntent().getStringExtra("message");

		TextView mTitle = (TextView)findViewById(R.id.fullscreen_notification_title);
		TextView mMessage = (TextView)findViewById(R.id.fullscreen_notification_message);

		mTitle.setText(title);
		mMessage.setText(message);
		
		findViewById(R.id.fullscreen_notification_button).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.fullscreen_notification, menu);
		return false;
	}

}
