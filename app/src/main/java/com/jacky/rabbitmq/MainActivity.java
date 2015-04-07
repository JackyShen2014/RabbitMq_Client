package com.jacky.rabbitmq;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.jacky.rabbitmq.MessageConsumer.OnReceiveMessageHandler;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity {
    private MessageConsumer mConsumer;

    private EditText field_hostIp;
    private EditText field_port;
    private EditText field_usrName;
    private EditText field_pasWord;
    private EditText field_sendPkg;
    private EditText field_recvPkg;
    private EditText field_exchName;
    private EditText field_exchType;
    private EditText field_queName;
    private Button button_sendMsm;

    private Handler handler;
    private Runnable refresher;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();

        refresher = new Runnable(){
            public void run(){
                MainActivity.this.freshCurrentInfo();
            }
        };
        handler = new Handler();
        handler.postDelayed(refresher, 1000);

        SharedPreferences account = this.getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        field_hostIp.setText(account.getString(getString(R.string.SERVER_IP),""));
        field_port.setText(account.getString(getString(R.string.PORT),""));
        field_usrName.setText(account.getString(getString(R.string.Usr_Name),""));
        field_pasWord.setText(account.getString(getString(R.string.Pas_Word),""));
        field_exchName.setText(account.getString(getString(R.string.Ex_Name),""));
        field_exchType.setText(account.getString(getString(R.string.Ex_Type),""));
        field_queName.setText(account.getString(getString(R.string.Que_Name),""));

        //Set button listener and process connection to server
        setListeners();

        //Receive MSG from server
        recvMsgFromServer();

    }

    private void findViews(){
        field_hostIp = (EditText) findViewById(R.id.hostIp);
        field_port = (EditText) findViewById(R.id.port);
        field_usrName = (EditText) findViewById(R.id.usrName);
        field_pasWord = (EditText) findViewById(R.id.passWord);
        field_sendPkg = (EditText) findViewById(R.id.sendPkg);
        field_recvPkg = (EditText) findViewById(R.id.recvPkg);
        field_exchName = (EditText) findViewById(R.id.exchangeName);
        field_exchType = (EditText) findViewById(R.id.exchangeType);
        field_queName = (EditText) findViewById(R.id.queueName);
        button_sendMsm = (Button) findViewById(R.id.sendButton);
    }

    private void setListeners(){
        button_sendMsm.setOnClickListener(sendMsm);
    }

    private Button.OnClickListener sendMsm = new Button.OnClickListener(){
        @Override
        public void onClick(View arg0) {
            MainActivity.this.start();
        }

    };

    protected void freshCurrentInfo(){
        SharedPreferences account = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String serverIp = account.getString(getString(R.string.SERVER_IP), "");
        String port = account.getString(getString(R.string.PORT), "");
        String usrName = account.getString(getString(R.string.Usr_Name), "");
        String passWord = account.getString(getString(R.string.Pas_Word), "");
        String exchName = account.getString(getString(R.string.Ex_Name), "");
        String exchType = account.getString(getString(R.string.Ex_Type), "");
        String queName = account.getString(getString(R.string.Que_Name), "");

        field_hostIp.setText(serverIp);
        field_hostIp.postInvalidate();

        field_port.setText(port);
        field_port.postInvalidate();

        field_usrName.setText(usrName);
        field_usrName.postInvalidate();

        field_pasWord.setText(passWord);
        field_pasWord.postInvalidate();

        field_pasWord.setText(exchName);
        field_pasWord.postInvalidate();

        field_pasWord.setText(exchType);
        field_pasWord.postInvalidate();

        field_pasWord.setText(queName);
        field_pasWord.postInvalidate();


        try{
            this.findViewById(R.id.activity_main).postInvalidate();
        }catch(Exception e){}

    }

    protected void start(){
        if(field_hostIp.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入服务器ip", Toast.LENGTH_SHORT).show();
            field_hostIp.requestFocus();
            return;
        }
        if(field_port.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入服务器端口", Toast.LENGTH_SHORT).show();
            field_port.requestFocus();
            return;
        }
        if(field_usrName.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入用户名", Toast.LENGTH_SHORT).show();
            field_usrName.requestFocus();
            return;
        }
        if(field_pasWord.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入密码", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        }
        if(field_exchName.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入Exchange Name", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        }
        if(field_exchType.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入Exchange Type", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        }
        if(field_queName.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入Queue Name", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        }
        if(field_sendPkg.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入需要发送的消息", Toast.LENGTH_SHORT).show();
            field_sendPkg.requestFocus();
            return;
        }


        int intServerPort = 0;
        try{
            intServerPort = Integer.parseInt(field_port.getText().toString());
        }catch(Exception e){
            Toast.makeText(this.getApplicationContext(), "端口格式错误", Toast.LENGTH_SHORT).show();
            field_port.requestFocus();
            return;
        }

        saveAccountInfo();

        //Todo: Connect Server
        new send().execute(field_sendPkg.getText().toString());

        freshCurrentInfo();


    }

    private void saveAccountInfo (){
        SharedPreferences account = this.getSharedPreferences(getString(R.string.preference_file_key),Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = account.edit();
        editor.putString(getString(R.string.SERVER_IP), field_hostIp.getText().toString());
        editor.putString(getString(R.string.PORT), field_port.getText().toString());
        editor.putString(getString(R.string.Usr_Name), field_usrName.getText().toString());
        editor.putString(getString(R.string.Pas_Word), field_pasWord.getText().toString());
        editor.putString(getString(R.string.Ex_Name), field_exchName.getText().toString());
        editor.putString(getString(R.string.Ex_Type), field_exchType.getText().toString());
        editor.putString(getString(R.string.Que_Name), field_queName.getText().toString());

        editor.commit();
    }

    private void recvMsgFromServer(){
        if(field_hostIp.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入服务器ip", Toast.LENGTH_SHORT).show();
            field_hostIp.requestFocus();
            return;
        }
        if(field_exchName.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入Exchange Name", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        }
        if(field_exchType.getText().toString().length() == 0){
            Toast.makeText(this.getApplicationContext(), "请输入Exchange Type", Toast.LENGTH_SHORT).show();
            field_pasWord.requestFocus();
            return;
        }

        String hostIp, exchangeName, exchangeType;
        hostIp = field_hostIp.getText().toString();
        exchangeName = field_exchName.getText().toString();
        exchangeType = field_exchType.getText().toString();

        // Create the consumer
        mConsumer = new MessageConsumer(hostIp, exchangeName, exchangeType);
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

                field_recvPkg.append("\n" + text);
            }
        });

    }

    private class send extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String...Message) {
            try {

                ConnectionFactory factory = new ConnectionFactory();
                //Set server IP address and port
                factory.setHost(field_hostIp.getText().toString());
                factory.setPort(Integer.parseInt(field_port.getText().toString()));

                //Set user name and password
                factory.setUsername(field_usrName.getText().toString());
                factory.setPassword(field_pasWord.getText().toString());


                System.out.println(""+factory.getHost()+factory.getPort()+factory.getRequestedHeartbeat()+factory.getUsername());

                Connection connection = factory.newConnection();

                Channel channel = connection.createChannel();

                String exchangeName, exchangeType, queName;
                exchangeName = field_exchName.getText().toString();
                exchangeType = field_exchType.getText().toString();
                queName = field_queName.getText().toString();

                channel.exchangeDeclare(exchangeName, exchangeType, true);
                channel.queueDeclare(queName, false, false, false, null);
                String tempstr = "";
                for (int i = 0; i < Message.length; i++)
                    tempstr += Message[i];

                channel.basicPublish(exchangeName, queName, null,
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