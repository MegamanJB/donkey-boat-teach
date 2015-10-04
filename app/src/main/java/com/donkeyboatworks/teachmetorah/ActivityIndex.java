package com.donkeyboatworks.teachmetorah;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;

/**
 * Created by Joshua on 10/4/2015.
 */
public class ActivityIndex extends ActionBarActivity implements View.OnClickListener {

    Button mainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent viewTextIntent = new Intent(this, ActivityViewText.class);
        startActivity(viewTextIntent);
    }
}
