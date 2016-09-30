package edu.buffalo.cse.cse486586.groupmessenger1;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {

    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    int j =0;
   // private final ContentResolver mContentResolver;
    private final Uri mUri = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger1.provider");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));


        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }



        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        final Button SendButton = (Button)findViewById(R.id.button4);
        //final Button button = (Button) findViewById(R.id.button_id);
        SendButton.setOnClickListener(new View.OnClickListener() {


            public void onClick(View v) {
                // Perform action on click


                final EditText editText = (EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString() + "\n";
               // editText.setText(""); // This is one way to reset the input box.
              //  String msg = editText.getText().toString() + "\n";
                //editText.setText(""); // This is one way to reset the input box.
                //TextView localTextView = (TextView) findViewById(R.id.editText1);
                editText.setText(""); // This is one way to display a string.
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.append(msg+"\t\n");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

            }
        });






    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];

            /*
             * TODO: Fill in your server code that receives messages and passes them
             *
             * */
            //PrintWriter out = new PrintWriter(serverSocket., true);
            Log.e(TAG, "Entered Socket function");

            try {
                while (true) {
                    Log.e(TAG, "Entered Socket function while loop");

                    Socket clientsocket = serverSocket.accept();

                    BufferedReader inp = new BufferedReader(new InputStreamReader(clientsocket.getInputStream()));
                    String line = inp.readLine();
                    //System.out.println(in.readLine());
                    //     while(line!= null)
                    publishProgress(line);
                    clientsocket.close();
                    Log.d(TAG, "Entered Socket function after progress function called");

                    //j++;
                }


                // I have taken the permission from Prof Steve Ko to use Code Snippets from the following websites
                //https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                //https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html

                //   }

            } catch (Exception e) {
                Log.e(TAG, "Socket Server Error");
            }
            return null;
        }


        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            Log.e(TAG, "Entered Progress Update function");

            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            //TextView localTextView = (TextView) findViewById(R.id.editText1);
            //localTextView.append("\n");
            ContentValues tobeinserted = new ContentValues();
            tobeinserted.put("key", Integer.toString(j));
            tobeinserted.put("value", strReceived);
            ContentResolver kr = getContentResolver();
            kr.insert(mUri, tobeinserted);
            Log.e(TAG,j+""+strReceived+"success");
            j++;

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            return;
        }
    }

    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {

            Log.e(TAG, "Entered");

            try {
                Log.e(TAG, "try Entered");
                String remotePort;

                for(int i=0;i<=4;i++) {


                    if(i==0) {
                        remotePort = REMOTE_PORT0;
                    }
                    else if(i==1)
                        remotePort = REMOTE_PORT1;
                    else if(i==2)
                        remotePort = REMOTE_PORT2;
                    else if(i==3)
                        remotePort = REMOTE_PORT3;
                    else
                        remotePort = REMOTE_PORT4;

                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));
                    //int count = 0;
                    String msgToSend = msgs[0];
                /*
                 * TODO: Fill in your client code that sends out a message.
                 */

                    //       Socket echoSocket = new Socket(hostName, portNumber);

                    PrintWriter outp = new PrintWriter(socket.getOutputStream(), true);
                    outp.write(msgToSend);
                    outp.flush();
                    Log.e(TAG,"Message Sent");
                    // I have taken the permission from Prof Steve Ko to use Code Snippets from the following websites
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html

                    //   count++;


                    socket.close();
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
               // Log.e(TAG,(String)remotePort);
            }

            return null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
}
