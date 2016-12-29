package net.wustudio.breakout;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btnStart).setOnClickListener(onStartClick);

    }

    View.OnClickListener onStartClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, InGame.class);
            startActivity(intent);
        }
    };

    View.OnClickListener onQuitClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            finish();
        }
    };

}
