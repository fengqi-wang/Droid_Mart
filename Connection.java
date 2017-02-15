package com.test.commander;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.jmdns.ServiceInfo;

/**
 * Created by pocket-social on 2017-02-14.
 */

public class Connection {

    class Node {
        String name;
    }

    class Command {
        String mCommand;
    }

    interface CommandListener {
        void onCommand(String c);
    }

    interface serverListener {
        void onServerFound(Node n);
    }

    private ServerSocket serverSocket;
    private Socket mSocket;
    private RadioStation mRadio =  new RadioStation(null);
    private HandlerThread senderThread;
    private Handler mSender;

    void startServer(final CommandListener receiver){
        reset();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    serverSocket = new ServerSocket(0);
                    int port = serverSocket.getLocalPort();

                    mRadio.registerServer(port);

                    mSocket = serverSocket.accept();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            BufferedReader input;
                            try {
                                input = new BufferedReader(new InputStreamReader(
                                        mSocket.getInputStream()));
                                while (!Thread.currentThread().isInterrupted()) {
                                    String messageStr = input.readLine();
                                    if (messageStr != null) {
                                        receiver.onCommand(messageStr);
                                    } else {
                                        break;
                                    }
                                }
                                input.close();
                            } catch (IOException e) {

                            }
                        }
                    }).start();
                } catch (IOException e) {
                }
            }
        }).start();
    }

    void scanServers() {

        mRadio.findServers(new RadioStation.OnFoundListener() {
            @Override
            public void onFound(ServiceInfo info) {
                  String name = info.getServer();
                  int port = info.getPort();
            }
        });
    }

    void bindTo(Node node) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mSocket =  new Socket();
            }
        }).start();

        senderThread = new HandlerThread("moreants_worker");
        senderThread.start();

        mSender =  new Handler(senderThread.getLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String cmd = (String)msg.obj;

                try {
                    PrintWriter out = new PrintWriter(
                            new BufferedWriter(
                                    new OutputStreamWriter(mSocket.getOutputStream())
                            ),
                            true
                    );
                    out.println(cmd);
                    out.flush();
                } catch (Exception e) {
                }

                return false;
            }
        });
    }

    public synchronized void command(final Command c) {
        Message msg = mSender.obtainMessage();
        msg.obj = c.mCommand;
        mSender.sendMessage(msg);
    }

    void reset() {
        if(senderThread != null)
            senderThread.quit();
        if(serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(mRadio != null)
            mRadio.reset();
    }
}
