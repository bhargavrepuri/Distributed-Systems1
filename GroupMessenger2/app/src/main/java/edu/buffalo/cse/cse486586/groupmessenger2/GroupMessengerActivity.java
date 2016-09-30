package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.PriorityQueue;
import java.net.SocketTimeoutException;
import java.util.concurrent.PriorityBlockingQueue;


/**
 * GroupMessengerActivity is tkinhe main Activity for the assignment.
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
    PriorityBlockingQueue<mesg1> queue = new PriorityBlockingQueue<mesg1>(10, new queuer());
    ArrayList<String> port_Addr = new ArrayList<String>();


    static final int SERVER_PORT = 10000;
    static String myPort;
    int s =0,counter=0,j1=0,crash_count=0,k=1;
    // private final ContentResolver mContentResolver;
    private final Uri mUri = Uri.parse("content://edu.buffalo.cse.cse486586.groupmessenger2.provider");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //  android.os.Debug.waitForDebugger();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);

        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        k = Integer.parseInt(String.valueOf(k) + myPort);

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
        port_Addr.add(REMOTE_PORT0);
        port_Addr.add(REMOTE_PORT1);
        port_Addr.add(REMOTE_PORT2);
        port_Addr.add(REMOTE_PORT3);
        port_Addr.add(REMOTE_PORT4);
        final Button SendButton = (Button)findViewById(R.id.button4);
        //final Button button = (Button) findViewById(R.id.button_id);
        SendButton.setOnClickListener(new View.OnClickListener() {


            public void onClick(View v) {
                // Perform action on click


                final EditText editText = (EditText) findViewById(R.id.editText1);
                String msg = editText.getText().toString() + "\n";
                // editText.setText(""); // This is one way to reset the input box.
                //   String msg = editText.getText().toString() + "\n";
                editText.setText(""); // This is one way to reset the input box.
                TextView localTextView = (TextView) findViewById(R.id.editText1);
                editText.setText(""); // This is one way to display a string.
                TextView remoteTextView = (TextView) findViewById(R.id.textView1);
                remoteTextView.append(msg + "\t\n");
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
         //   Log.e(TAG, "Entered Socket function");

                int exception_flag3 = 0;
            mesg1 newm = new mesg1();

                while (true) {
                    try {
                //    Log.e(TAG, "Entered Socket function while loop");

                    Socket clientsocket = serverSocket.accept();
                    clientsocket.setSoTimeout(5000);
                    ObjectInputStream in = new ObjectInputStream(clientsocket.getInputStream());

                    newm = (mesg1)in.readObject();


                    if(newm.mesg_no == 1) {

                        int flag1 = 0;
                    //    Log.e(TAG,"Entered 1st Condition");
                        String h = String.valueOf(k);
                        s = Integer.parseInt(h.substring(0, h.length() - 5));
                        s = s + 1;
                        k = Integer.parseInt(Integer.toString(s) + myPort);
                        newm.Seq_num = s;
                        newm.Proposed_Seq_num = k;
                        newm.mesg_no = 2;
                        newm.Status =  "undeliverable";
                        newm.proposal_port = myPort;
                        newm.final_proposer = myPort;
                        //Add the mesg object to the priority queue
                        Iterator<mesg1> iter1 = queue.iterator();
                        while(iter1.hasNext())
                        {
                            mesg1 kl = iter1.next();
                            if(kl.mesgid.equals(newm.mesgid))
                                flag1 = 1;
                        }
                        if(flag1==0) {
                            queue.add(newm);
                            flag1 = 1;
                        }
                        // Send the proposed sequence number to the Sender
                     //   Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                      //          Integer.parseInt(newm.port));
                        Log.e(TAG,"Sending Proposal for mesg"+newm.mesg+"with  proposal number "+ newm.Proposed_Seq_num );
                        newm.port = myPort;
                        newm.port_list.add(myPort);
                        try {
                            ObjectOutputStream oos = new ObjectOutputStream(clientsocket.getOutputStream());

                            oos.writeObject(newm);
                         //   oos.flush();
                         //   clientsocket.close();
                        }
                        catch (EOFException e){
                            exception_flag3 = 1;
                            Log.e(TAG,"EOF Exception- flag3");
                        }
                        catch (SocketTimeoutException e){
                            exception_flag3 = 1;
                            Log.e(TAG,"Socket Timeout Exception- flag3");
                        }
                        catch (StreamCorruptedException e){
                            exception_flag3 = 1;
                            Log.e(TAG,"Stream Corrupted Exception- flag3");
                        }
                        catch (IOException e){
                            exception_flag3 = 1;
                            Log.e(TAG,"IO Exception- flag3");
                        }
                        if(exception_flag3==1)
                        {
                            Log.e(TAG,"Entered Exception flag exceptions_flag3 removing port"+ newm.port);
                            //remove mesgs from queue and don't send mesgs to this port;
                            for(int r1 = 0;r1<port_Addr.size();r1++)
                            {
                                if(port_Addr.get(r1).equals(newm.senderport))
                                {
                                    synchronized (this) {
                                        Log.e(TAG,"Removed port"+newm.senderport);
                                        port_Addr.set(r1,"Failed");
                                        crash_count++;
                                    }
                                    break;
                                }
                            }
                            Iterator<mesg1> iter5 = queue.iterator();
                            while(iter5.hasNext())
                            {
                                mesg1 kl2 = iter5.next();
                                if(kl2.senderport.equals(newm.senderport))
                                {
                                    Log.e(TAG,"Message removed from queue"+ kl2.mesg+"with senderport"+kl2.senderport);
                                    iter5.remove();

                                }
                            }
                            exception_flag3 = 0;
                        }

                    }

                    else if(newm.mesg_no==3)
                    {
                       // Log.e(TAG,"Entered 3rd Condition");
                        //update queue - deliverable - Every Time queue is updated check the head of queue if it is marked as deliverable then deque and publish it
                        Iterator<mesg1> iter3 = queue.iterator();
                        while(iter3.hasNext())
                        {
                            mesg1 kl = iter3.next();
                            if(kl.mesgid.equals(newm.mesgid))
                            {
                                Log.e(TAG, "Received message for Delivery :" + newm.mesg + " with proposal_number" + newm.Proposed_Seq_num );
                                iter3.remove();
                               // queue.remove(kl);

                                queue.add(newm);

                               String u =  String.valueOf(newm.Proposed_Seq_num);
                                int u1 = Integer.valueOf(u.substring(0,u.length()-5));

                                k = Math.max(newm.Proposed_Seq_num, k);
                              //  Log.e(TAG,"mesg peek status" + queue.peek().Status + "with Sequence No" + newm.Proposed_Seq_num);
                                break;

                            }
                        }
                        while(!queue.isEmpty()) {
                            Log.e(TAG, "Message on peek is" + queue.peek().mesg + "with status"+queue.peek().Status + "with sequence number" + queue.peek().Proposed_Seq_num);
                            if(queue.peek().Status.equals("Deliverable")) {

                                Log.e(TAG, "Message DElivered is" + queue.peek().mesg + "with status"+queue.peek().Status +"with sequence number" + queue.peek().Proposed_Seq_num);
                                if(port_Addr.contains(queue.peek().senderport)) {
                                    publishProgress(queue.peek().mesg);
                                }
                               queue.poll();
                            }
                            else
                            {
                                if(!port_Addr.contains(queue.peek().senderport))
                                {
                                    queue.poll();
                                }
                                else {
                                    break;
                                }
                            }
                        }

                    }

                  //  Log.d(TAG, "Entered Socket function after progress function called");
                }catch (ClassNotFoundException e)
                {

                }
                catch (IOException e){

                }
            }

        }


        protected void onProgressUpdate(String... strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

         //   Log.e(TAG, "Entered Progress Update function");

            String strReceived = strings[0].trim();
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            TextView localTextView = (TextView) findViewById(R.id.editText1);
            localTextView.append("\n");
            ContentValues tobeinserted = new ContentValues();
            tobeinserted.put("key", Integer.toString(j1));
            tobeinserted.put("value", strReceived);
            ContentResolver kr = getContentResolver();
            kr.insert(mUri, tobeinserted);
            Log.e(TAG,j1+" "+strReceived+"success");
            j1++;

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

          //  Log.e(TAG, "Entered");
            counter = counter + 1;

            //    Log.e(TAG, "try Entered");

                String remotePort;
                int exception_flag1 = 0,exception_flag2 = 0;
                for(int i=0;i<port_Addr.size();i++) {

                    if(port_Addr.get(i).equals("Failed"))
                    {
                        continue;
                    }
                    remotePort = port_Addr.get(i);
                    try {
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort));

                    //Increment Counter

                    mesg1 msgToSend = new mesg1();

                    msgToSend.mesg = msgs[0];
                    msgToSend.mesgid = (Integer.toString(counter) + myPort) ;
                    msgToSend.mesg_no = 1;
                    msgToSend.count = 0;
                  //  msgToSend.port = myPort;
                        msgToSend.senderport = myPort;


                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                        Log.e(TAG, "Sent Message :" + msgs[0] + " From emulator:" + myPort + "to " + remotePort);
                    oos.writeObject(msgToSend);
                    oos.flush();
                   //     socket.getOutputStream().close();
                   //     oos.close();
                    ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                    mesg1 newm = new mesg1();

                        socket.setSoTimeout(2500);
                        newm = (mesg1)in.readObject();
                   //     socket.getInputStream().close();
                    //    in.close();

                        if(newm.mesg_no == 2)
                        {

                       //     Log.e(TAG,"Entered 2nd Condition");
                            // Check all the proposed seq numbers and find the maximum - Two cases - Tie and No Tie

                            int flag = 0;
                            Iterator<mesg1> iter = queue.iterator();
                            while (iter.hasNext()) {
                                mesg1 jk = iter.next();

                                if(newm.mesgid.equals(jk.mesgid)) {

                            //        Log.e(TAG, "Entered 2nd Condition - Found mesg in queue");
                                    Log.e(TAG, "Received Proposal for Message :"+msgs[0]+" From emulator:" + newm.port + "to " + myPort + "with proposal number"+newm.Proposed_Seq_num);
                                    flag = 1;
                                 //   for (int y1 = 0;y1<=port_Addr.size();y1++){

                                   //     if (port_Addr.get(y1).equals(newm.port)) {
                                            newm.count = jk.count + 1;
                                     //       break;
                                      //  }
                                   // }

                                    newm.Proposed_Seq_num = Math.max(newm.Proposed_Seq_num,jk.Proposed_Seq_num);
                                    newm.mesg_no = 3;
                                    jk.port_list.add(newm.port);
                                    newm.port_list.clear();
                                    for(int jo=0;jo<jk.port_list.size();jo++)
                                    {
                                           newm.port_list.add(jk.port_list.get(jo));
                                    }
                                    iter.remove();
                                    queue.add(newm);
                                    // check all ports in message are valid;
                                    boolean valid_port = true;
                                    for(int v1=0;v1<newm.port_list.size();v1++)
                                    {
                                        valid_port = valid_port && port_Addr.contains(newm.port_list.get(v1));
                                    }
                                    Log.e(TAG,"Message is" + newm.mesg+"with valid port"+String.valueOf(valid_port)+"count is"+newm.count + "size of port_list"+String.valueOf(newm.port_list.size()));
                                    if(((newm.count>=port_Addr.size()-crash_count) && valid_port)|| newm.count>=5)
                                    {
                                //        Log.e(TA,"Entered 2nd Condition - counter >= 5");
                                        //Multicast the accepted Sequence number to all the processes
                                        newm.Status = "Deliverable";
                                        String remotePort1;

                                        for(int i1=0;i1<port_Addr.size();i1++) {

                                            if(port_Addr.get(i1).equals("Failed"))
                                            {
                                                continue;
                                            }

                                            remotePort1 = port_Addr.get(i1);
                                            Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                    Integer.parseInt(remotePort1));
                                            socket1.setSoTimeout(5000);
                                            ObjectOutputStream oos1 = new ObjectOutputStream(socket1.getOutputStream());
                                            try {
                                                newm.port = myPort;
                                                Log.e(TAG, "For Delivery Sent Message :"+newm.mesg+ "and sequence number" + newm.Proposed_Seq_num);
                                                oos1.writeObject(newm);
                                                oos1.flush();
                                                oos1.close();
                                                socket1.close();
                                            }
                                            catch (EOFException e){
                                                exception_flag2 = 1;
                                                Log.e(TAG,"EOF exception - flag2");
                                            }
                                            catch (SocketTimeoutException e){
                                                exception_flag2 = 1;
                                                Log.e(TAG,"Socket Timeout Exception - flag2");
                                            }
                                            catch (StreamCorruptedException e){
                                                exception_flag2 = 1;
                                                Log.e(TAG,"Socket Corrupted Exception- flag2");
                                            }
                                            catch (IOException e){
                                                exception_flag2 = 1;
                                                Log.e(TAG,"IO Exception- flag2");
                                            }

                                            if(exception_flag2==1)
                                            {
                                                Log.e(TAG,"Entered Exception flag exceptions_flag2 removing port" + remotePort1);
                                                //remove mesgs from queue and don't send mesgs to this port;
                                                for(int r1 = 0;r1<port_Addr.size();r1++)
                                                {
                                                    if(port_Addr.get(r1).equals(remotePort1))
                                                    {
                                                        synchronized (this) {
                                                            Log.e(TAG,"Removed port"+remotePort1);
                                                            port_Addr.set(r1,"Failed");
                                                            crash_count++;
                                                        }

                                                        break;
                                                    }
                                                }
                                                Iterator<mesg1> iter5 = queue.iterator();
                                                while(iter5.hasNext())
                                                {
                                                    mesg1 kl2 = iter5.next();
                                                    if(kl2.senderport.equals(remotePort1))
                                                    {
                                                        Log.e(TAG,"Message removed from queue"+ kl2.mesg+"with senderport"+kl2.senderport);
                                                        iter5.remove();
                                                        continue;

                                                    }
                                                    //deliver undelivered messages
                                                    if(kl2.count==4 && !kl2.port_list.contains(remotePort1) )
                                                    {
                                                        //Multicast Deliver message
                                                        for(int ju=0;ju<kl2.port_list.size();ju++)
                                                        {
                                                            String rem_port = kl2.port_list.get(ju);
                                                            Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                                    Integer.parseInt(rem_port));
                                                            kl2.Status = "Deliverable";
                                                            kl2.port = myPort;
                                                            //socket1.setSoTimeout(5000);
                                                            ObjectOutputStream oos2 = new ObjectOutputStream(socket2.getOutputStream());
                                                            Log.e(TAG, "For Delivery Sent Message :"+kl2.mesg+ "and sequence number" + newm.Proposed_Seq_num);
                                                            oos2.writeObject(kl2);
                                                            oos2.flush();
                                                            oos2.close();
                                                            socket2.close();
                                                        }

                                                    }
                                                }
                                                exception_flag2 = 0;
                                            }
                                        }
                                    }

                                    break;
                                }
                                // do something with current
                            }
                            if(flag==0)
                            {
                                Log.e(TAG, "Received Proposal for Message with flag :"+msgs[0]+" From emulator:" + newm.port + "to " + myPort+ "with seq num" + newm.Proposed_Seq_num);

                                    newm.count++;
                                    queue.add(newm);

                                flag = 1;

                            }


                        }


                  //  Log.e(TAG, "Message Sent");

                   //  socket.close();
                    }

                    catch (SocketTimeoutException e){
                        exception_flag1 = 1;
                        Log.e(TAG,"Socket Timeout Exception- flag1");
                    }
                    catch (StreamCorruptedException e){
                        exception_flag1 = 1;
                        Log.e(TAG,"Socket Corrupted Exception- flag1");
                    }
                    catch(EOFException e){
                        exception_flag1 = 1;
                        Log.e(TAG,"EOF Exception- flag1");
                    }
                    catch (IOException e) {
                        exception_flag1 = 1;
                        Log.e(TAG,"IO Exception- flag1");
                    }
                    catch (ClassNotFoundException e){
                        exception_flag1 = 1;
                        Log.e(TAG,"Class Not Found Exception- flag1");
                    }

                    if(exception_flag1==1)
                    {
                        Log.e(TAG,"Entered Exception flag exceptions_flag1 removing port" + remotePort);
                        //clear queue and don't send mesgs to that port
                        for(int r2 = 0;r2<port_Addr.size();r2++)
                        {
                            if(port_Addr.get(r2).equals(remotePort))
                            {
                                synchronized (this) {
                                    Log.e(TAG,"Removed port"+remotePort);
                                    port_Addr.set(r2,"Failed");
                                    crash_count++;
                                }

                                break;
                            }
                        }
                        Iterator<mesg1> iter4 = queue.iterator();
                        while(iter4.hasNext())
                        {
                            mesg1 kl1 = iter4.next();
                            if(kl1.senderport.equals(remotePort))
                            {
                                Log.e(TAG,"Message removed from queue"+ kl1.mesg+"with senderport"+kl1.senderport);
                                iter4.remove();
                                continue;

                            }
                            //deliver undelivered messages
                            Log.e(TAG,"For mesg" + kl1.mesg+"Delivering in Exception flag1 with port_list size" + String.valueOf(kl1.port_list.size()) +"count" + kl1.count+ "value of !kl1.port_list.contains(remotePort)" + String.valueOf(!kl1.port_list.contains(remotePort)) );
                            if(kl1.count==4 && !kl1.port_list.contains(remotePort) )
                            {
                                //Multicast Deliver message
                                for(int ju1=0;ju1<kl1.port_list.size();ju1++)
                                {

                                    String rem_port1 = kl1.port_list.get(ju1);


                                    try {
                                        Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                                Integer.parseInt(rem_port1));
                                        kl1.Status = "Deliverable";
                                        kl1.port = myPort;
                                        //socket1.setSoTimeout(5000);
                                        ObjectOutputStream oos3 = new ObjectOutputStream(socket3.getOutputStream());
                                        Log.e(TAG, "For Delivery Sent Message :"+kl1.mesg+ "and sequence number" + kl1.Proposed_Seq_num);
                                        oos3.writeObject(kl1);
                                        oos3.flush();
                                        oos3.close();
                                        socket3.close();
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                }

                            }
                        }

                        exception_flag1 = 0;
                    }

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
