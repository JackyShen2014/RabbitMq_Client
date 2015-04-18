package com.jacky.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;

/**
 * Base class for objects that connect to a RabbitMQ Broker
 */
public abstract class IConnectToRabbitMQ {
    public String mServer;
    public String mUserName;
    public String mPassword;

    protected Channel mModel = null;
    protected Connection  mConnection;

    protected boolean Running ;

    protected  String MyExchangeType ;

    /**
     *
     * @param server The server address
     * @param username The username
     * @param password The password
     */
    public IConnectToRabbitMQ(String server, String username, String password)
    {
        mServer = server;
        mUserName = username;
        mPassword = password;
    }

    public void Dispose()
    {
        Running = false;

			try {
				if (mConnection!=null)
					mConnection.close();
				if (mModel != null)
					mModel.abort();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

    }

    /**
     * Connect to the broker and create the exchange
     * @return success
     */
    public boolean connectToRabbitMQ()
    {
  	  if(mModel!= null && mModel.isOpen() )//already declared
  		  return true;
        try
        {
      	    ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(mServer);
            connectionFactory.setUsername(mUserName);
            connectionFactory.setPassword(mPassword);
            mConnection = connectionFactory.newConnection();
            mModel = mConnection.createChannel();

            return true;
        }
        catch (Exception e)
        {
      	  e.printStackTrace();
            return false;
        }
    }
}