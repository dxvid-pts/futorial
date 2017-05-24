package de.davepe.futorial;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class NotificationOpenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        String mUrl = getIntent().getStringExtra("url");
        if (mUrl == null || mUrl.equals("")) {
            finish();
            return;
        }

        MainActivity.open(mUrl);
        finish();
    }
}
