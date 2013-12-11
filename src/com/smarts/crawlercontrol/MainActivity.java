package com.smarts.crawlercontrol;

import java.io.IOException;

import com.smarts.myaccelerometer.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       
    }

    public void sendParams(View view) throws IOException
    {
    	Intent intent_main_activity = new Intent(this, MySocketHandler.class);
     
    	EditText editText = (EditText) findViewById(R.id.MyText); //get data from textbox
    	String MyIpAddress = editText.getText().toString();
     
    	intent_main_activity.putExtra("IpAddress", MyIpAddress);
    	startActivity(intent_main_activity);
    }
    
    
    @Override
    public void onBackPressed() {
        // do something on back.
    	System.exit(0);
        
    }
   
	
}
