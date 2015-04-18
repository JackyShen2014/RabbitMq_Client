package com.jacky.rabbitmq;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class LoginActivity extends Activity {


    private Button loginBt;
    private EditText usernameEd;
    private EditText passwordEd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        loginBt = (Button)findViewById(R.id.login);
        usernameEd = (EditText)findViewById(R.id.username);
        passwordEd = (EditText)findViewById(R.id.password);
        loginBt.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString("username",usernameEd.getText().toString());
                bundle.putString("password",passwordEd.getText().toString());
                bundle.putString("server","192.168.100.106");
                intent.setClass(LoginActivity.this,MainActivity.class);
                intent.putExtras(bundle);
                startActivity(intent);
                LoginActivity.this.finish();
            }

        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
