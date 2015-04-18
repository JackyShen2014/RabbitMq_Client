package com.jacky.rabbitmq;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.jacky.rabbitmq.MessageConsumer.OnReceiveMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class MainActivity extends Activity {
    private MessageConsumer mConsumer;

    private EditText hostIpEditor;
    private EditText reciever;
    private EditText message;

    private Button sendMsmBt;

    private String username;
    private String password;

    private ListView conentList;
    private ArrayList<String> receiveContent;



    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();


        SharedPreferences account = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        hostIpEditor.setText(account.getString(getString(R.string.SERVER_IP),""));

        Bundle bundle = getIntent().getExtras();
        username = bundle.getString("username");
        password = bundle.getString("password");
        hostIpEditor.setText(bundle.getString("server"));

        //Set button listener and process connection to server
        setListeners();

        //Receive MSG from server
        recvMsgFromServer();

    }

    private void findViews(){
        hostIpEditor = (EditText) findViewById(R.id.hostip);
        reciever = (EditText)findViewById(R.id.routkey);
        message = (EditText)findViewById(R.id.message);
        sendMsmBt = (Button)findViewById(R.id.send);
        conentList = (ListView)findViewById(R.id.content);
        receiveContent = new ArrayList<>();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.list_item,receiveContent);
        conentList.setAdapter(adapter);
    }

    private void setListeners(){
        sendMsmBt.setOnClickListener(sendMsmListener);
    }

    private Button.OnClickListener sendMsmListener = new Button.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            MainActivity.this.start();
        }

    };

    protected void start(){
        if(hostIpEditor.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入服务器ip", Toast.LENGTH_SHORT).show();
            hostIpEditor.requestFocus();
            return;
        }

        saveAccountInfo();

        //Todo: Connect Server
        new send().execute(message.getText().toString());

    }

    private void saveAccountInfo (){
        SharedPreferences account = this.getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = account.edit();
        editor.putString(getString(R.string.SERVER_IP), hostIpEditor.getText().toString());

        editor.apply();
    }

    private void recvMsgFromServer(){

        String hostIp;
        hostIp = hostIpEditor.getText().toString();

        // Create the consumer
        mConsumer = new MessageConsumer(hostIp, username, password);
        new consumerconnect().execute();
        // register for messages
        mConsumer.setOnReceiveMessageHandler(new OnReceiveMessageHandler() {

            public void onReceiveMessage(byte[] message) {
                String text = "";
                try {
                    text = new String(message, "UTF8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                receiveContent.add(text);
                conentList.invalidateViews();
            }
        });

    }

    private class send extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String...Message) {
            try {

                ConnectionFactory factory = new ConnectionFactory();
                //Set server IP address and port
                factory.setHost(hostIpEditor.getText().toString());
                factory.setPort(5672);

                //Set user name and password
                factory.setUsername(username);
                factory.setPassword(password);


                Log.d("Rabbitmq",""+factory.getHost()+factory.getPort()+factory.getRequestedHeartbeat()+factory.getUsername());

                Connection connection = factory.newConnection();

                Channel channel = connection.createChannel();

                String tempstr = "";

                for (String msg: Message) {
                    tempstr += msg;
                }

                channel.basicPublish("carlocation.fanout", reciever.getText().toString(), null,
                        tempstr.getBytes());

                channel.close();

                connection.close();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }


    private class consumerconnect extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... Message) {
            try {
                // Connect to broker
                mConsumer.connectToRabbitMQ();

            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
            // TODO Auto-generated method stub
            return null;
        }

    }

    @Override
    protected void onResume() {
        super.onPause();
        new consumerconnect().execute();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mConsumer != null){
            mConsumer.dispose();
        }

    }
}