package de.davepe.futorial.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AnimationUtils;

import de.davepe.futorial.MainActivity;
import de.davepe.futorial.R;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        findViewById(R.id.speaker1).setAnimation(AnimationUtils.loadAnimation(this, R.anim.speaker_1));
        findViewById(R.id.speaker2).setAnimation(AnimationUtils.loadAnimation(this, R.anim.speaker_2));

        findViewById(R.id.next).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, WelcomeActivity2.class));
            }
        });
    }
}
