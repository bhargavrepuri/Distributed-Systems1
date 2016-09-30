package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.content.ContentResolver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.TextView;

public class SimpleDhtProvider extends ContentProvider {

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        //For all the files
        File dir = getContext().getFilesDir();
        if(selection.compareTo("*")==0||selection.compareTo("@")==0)
        {
            //delete all files
            File[] file_list = dir.listFiles();

            for(File tmpf : file_list)
            {
                tmpf.delete();
            }

        }
        else {
            //For single file
            File file = new File(dir, selection);
            file.delete();
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO Auto-generated method stub
        String FILENAME = values.getAsString("key");
        String string1 = values.getAsString("value");

        String msg_hash = null;
        String avd_hash = null;
        String pred_hash = null;
        String succ_hash = null;

        //Calculate hash values
        try {
            msg_hash = genHash(FILENAME);
            avd_hash = genHash(portStr);
            pred_hash = genHash(predecessor);
            succ_hash = genHash(succesor);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }


        //Two cases - First node and general node
        if(avd_hash.compareTo(pred_hash)<0)
        {
            //First node
            if(msg_hash.compareTo(avd_hash)<0||msg_hash.compareTo(avd_hash)==0)
            {
                Log.v(TAG,"1 Inserting in general node for message "+string1+" in emulator"+portStr);
                custom_insert(uri, values);
            }
            else
            {
                // hash value greater than all the node values then insert in the first node
                if(msg_hash.compareTo(pred_hash)>0) {
                    Log.v(TAG,"2 Inserting in general node for message "+string1+" in emulator"+portStr);
                    custom_insert(uri, values);
                }
                else
                {
                    //hash value of message greater than first node but not the largest so send to the successor
                    Log.v(TAG,"3 Forwarding for message "+string1+" in emulator "+portStr+" to emulator "+succesor);
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, string1,FILENAME, succesor);

                }
            }

        }
        else if(avd_hash.compareTo(pred_hash)==0)
        {
            //only one node
            Log.v(TAG,"4 Inserting in single node for message "+string1);
            custom_insert(uri,values);
        }
        else if(avd_hash.compareTo(pred_hash)>0)
        {
            //general node
            if(msg_hash.compareTo(avd_hash)<0)
            {
                //if message hash value is less than me and greater than my predecessor then insert in my node
                if(msg_hash.compareTo(pred_hash)>0)
                {
                    Log.v(TAG,"5 Inserting in general node for message "+string1+" in emulator"+portStr);
                    custom_insert(uri,values);
                }
                else
                {
                    Log.v(TAG,"6 Forwarding for message "+string1+" in emulator "+portStr+" to emulator "+succesor);
                    new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, string1,FILENAME, succesor);
                }

            }
            else if(msg_hash.compareTo(avd_hash)==0)
            {
                //hash value of message equal to my value so insert
                Log.v(TAG,"7 Inserting in general node for message "+string1+" in emulator"+portStr);
                custom_insert(uri,values);
            }
            else if(msg_hash.compareTo(avd_hash)>0)
            {
                //hash value of message greater than the current node so send to successor
                Log.v(TAG,"8 Forwarding for message "+string1+" in emulator "+portStr+" to emulator "+succesor);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, string1,FILENAME, succesor);
            }
        }

        return uri;
    }

public Uri custom_insert(Uri uri, ContentValues values)
{
    String FILENAME = values.getAsString("key");
    String string1 = values.getAsString("value");

    FileOutputStream fos = null;
    try {
        fos = getContext().openFileOutput(FILENAME, Context.MODE_PRIVATE);

    } catch (FileNotFoundException e) {
        e.printStackTrace();
    }

    try {
        fos.write(string1.getBytes());
        fos.close();
    } catch (IOException e) {
        e.printStackTrace();
    }

    Log.v("insert", values.toString());
    return uri;
}

    static final String TAG = SimpleDhtActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    private final Uri mUri = Uri.parse("content://edu.buffalo.cse.cse486586.simpledht.provider");
    String succesor = null;
    String predecessor = null;
//    mesg msg_com1 = new mesg();
   ArrayList k = new ArrayList();
    ArrayList v = new ArrayList();
    int j =0,flag_query=0,query_prev = 0;
    String query_key = null,query_value=null;

    String portStr;
    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub

        TelephonyManager tel = (TelephonyManager) this.getContext().getSystemService(Context.TELEPHONY_SERVICE);
        portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        predecessor = portStr;
        succesor = portStr;
        try {
            Log.e(TAG,"For avd "+portStr+"Initial Predecessor is "+predecessor+"Initial succcessor is "+succesor+"with hash value"+genHash(portStr));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        try {

            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {

            Log.e(TAG, "Can't create a ServerSocket");

        }

        if(!myPort.equals(REMOTE_PORT0))
        {
            String msg = "request";
            new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
        }
        return true;
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
            mesg newm = new mesg();
            try {
                while (true) {
                    Log.e(TAG, "Entered Socket function while loop");

                    Socket clientsocket = serverSocket.accept();
                    Socket socket = null;
                    ObjectInputStream in = new ObjectInputStream(clientsocket.getInputStream());

                    newm = (mesg)in.readObject();
                    //System.out.println(in.readLine());
                    //     while(line!= null)
                    if(newm.mesgid==1)
                    {
                        //calculate hash value and insert in the ring appropriately
                        String cur_avd = genHash(portStr);
                        String n_avd = genHash(newm.port_no);
                        if(cur_avd.compareTo(n_avd)<0)
                        {
                            if(genHash(succesor).compareTo(genHash(portStr))> 0 && genHash(succesor).compareTo(n_avd)<0)
                            {

                                String s_port = String.valueOf((Integer.parseInt(succesor) * 2));
                                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(s_port));
                            }
                            else {
                                newm.succ = succesor;
                                succesor = newm.port_no;
                                newm.pred = portStr;
                                Log.e(TAG,"For avd "+portStr+"Modified Successor is "+succesor);
                                //Send successor and predecessor values 3 = succesor request , 4 = pred request
                                newm.mesgid = 3;
                                String s_port = String.valueOf((Integer.parseInt(newm.port_no) * 2));
                                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(s_port));
                            }


                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject(newm);
                            oos.flush();
                            socket.close();

                        }
                        else
                        {
                            if(genHash(predecessor).compareTo(genHash(portStr))<0 && genHash(predecessor).compareTo(n_avd)>0)
                            {

                                String s_port = String.valueOf((Integer.parseInt(predecessor) * 2));
                                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(s_port));
                            }
                            else {
                                newm.pred = predecessor;
                                newm.succ = portStr;
                                predecessor = newm.port_no;
                                Log.e(TAG, "For avd " + portStr + "Modified Predecessor is " + predecessor);
                                newm.mesgid = 4;
                                //Send successor and predecessor values
                                String s_port = String.valueOf((Integer.parseInt(newm.port_no) * 2));
                                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(s_port));
                            }
                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject(newm);
                            oos.flush();
                            socket.close();
                        }

                    }
                    else if(newm.mesgid ==2)
                    {
                        publishProgress(newm.mesg,newm.key);
                    }

                    else if(newm.mesgid==10)
                    {
                        //query

                        FileInputStream bha = null;
                        //temp.newRow().add("key", selection);

                        File dir = getContext().getFilesDir();
                        File[] file_list = dir.listFiles();
                      //  MatrixCursor temp = new MatrixCursor(new String[]{"key","value"});
                        if(newm.key.equals("*"))
                        {
                            if(newm.port_no.equals(portStr))
                            {
                                // return from all nodes for *
                                //Add all the retrieved nodes
                                Log.e(TAG, " Stop * query in port " + portStr+ "with sender port"+newm.port_no);
                                for(int i1=0;i1<newm.key1.size();i1++)
                                {
                                    k.add(newm.key1.get(i1));
                                    v.add(newm.value1.get(i1));
                                }
                                flag_query = 0;

                            }
                            else {
                                for (File tmpf : file_list) {

                                    try {
                                        bha = getContext().openFileInput(tmpf.getName());

                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    InputStreamReader bha1 = new InputStreamReader(bha);
                                    BufferedReader bufferedReader = new BufferedReader(bha1);
                                    StringBuilder lm = new StringBuilder();
                                    String line;
                                    try {
                                        while ((line = bufferedReader.readLine()) != null) {
                                            lm.append(line);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                    newm.key1.add(tmpf.getName());
                                    newm.value1.add(lm.toString());
                                    // temp.addRow(new String[]{tmpf.getName(), lm.toString()});
                                }
                                //Forward the list to successor until successor is not equal to original port
                                Log.e(TAG, "For * query forwarding from port " + portStr + " to port " + succesor + "with origin"+newm.port_no);
                                String s_port = String.valueOf((Integer.parseInt(succesor) * 2));
                                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(s_port));
                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                oos.writeObject(newm);
                                oos.flush();
                                socket.close();

                            }

                        }
                        else {
                            int yu = 0;
                            for (File tmpf : file_list) {
                                if (tmpf.getName().equals(newm.key)) {
                                    yu = 1;
                                    try {
                                        bha = getContext().openFileInput(newm.key);

                                    } catch (FileNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    InputStreamReader bha1 = new InputStreamReader(bha);
                                    BufferedReader bufferedReader = new BufferedReader(bha1);
                                    StringBuilder lm = new StringBuilder();
                                    String line;
                                    try {
                                        while ((line = bufferedReader.readLine()) != null) {
                                            lm.append(line);
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }

                                    newm.value = lm.toString();
                                    break;
                                }
                            }
                            if (yu == 0) {
                                //Not found so forward to successor
                                String s_port = String.valueOf((Integer.parseInt(succesor) * 2));
                                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(s_port));
                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                oos.writeObject(newm);
                                oos.flush();
                                socket.close();


                            } else {
                                // Found so send to original port
                                String s_port = String.valueOf((Integer.parseInt(newm.port_no) * 2));
                                socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                        Integer.parseInt(s_port));
                                newm.mesgid = 11;
                                ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                                oos.writeObject(newm);
                                oos.flush();
                                socket.close();

                            }
                        }

                    }
                    else if(newm.mesgid==11)
                    {
                        //result for query
                        query_key = newm.key;
                        query_value  = newm.value;
                        flag_query = 0;


                    }
                    else if(newm.mesgid==3 || newm.mesgid==4)
                    {
                        succesor = newm.succ;
                        predecessor = newm.pred;
                        Log.e(TAG,"For avd "+portStr+"Final assigned Predecessor is "+predecessor+"Final assigned succcessor is "+ succesor);
                        if(newm.mesgid==3)
                        {
                            //newm.succ should have its predecessor as portstr
                            newm.mesgid = 5;
                            newm.pred = portStr;
                            String s_port = String.valueOf((Integer.parseInt(newm.succ) * 2));
                            socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(s_port));

                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject(newm);
                            oos.flush();
                            socket.close();

                        }
                        else
                        {
                            //newm.pred should have its succesor as portstr
                            newm.mesgid = 6;
                            newm.succ = portStr;
                            String s_port = String.valueOf((Integer.parseInt(newm.pred) * 2));
                            socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(s_port));

                            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                            oos.writeObject(newm);
                            oos.flush();
                            socket.close();
                        }
                    }
                    else if(newm.mesgid==5)
                    {
                        predecessor = newm.pred;
                        Log.e(TAG,"For avd "+portStr+" Modified Predecessor is "+predecessor+"Not modified succcessor is "+ succesor);
                    }
                    else if(newm.mesgid==6)
                    {
                        succesor = newm.succ;
                        Log.e(TAG,"For avd "+portStr+"Not modified Predecessor is "+predecessor+"Modified succcessor is "+succesor);
                    }

                    clientsocket.close();
                    Log.e(TAG, "For avd " + portStr + "Predecessor is " + predecessor + "succcessor is " + succesor);

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
            String str1Received  = strings[1].trim();
        //    TextView remoteTextView = (TextView) findViewById(R.id.textView1);
         //   remoteTextView.append(strReceived + "\t\n");
            //TextView localTextView = (TextView) findViewById(R.id.editText1);
            //localTextView.append("\n");
            ContentValues tobeinserted = new ContentValues();
            tobeinserted.put("key", str1Received);
            tobeinserted.put("value", strReceived);
            ContentResolver kr = getContext().getContentResolver();
            kr.insert(mUri, tobeinserted);
        //    Log.e(TAG,j+""+strReceived+"success");
         //   j++;

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
            String msgToSend = msgs[0];

            try {
                Log.e(TAG, "try Entered");
                String remotePort;

                if(msgToSend.equals("request"))
                {
                    mesg msg_com = new mesg();
                    msg_com.mesgid = 1;
                    msg_com.mesg = msgToSend;
                    msg_com.port_no = portStr;
                    msg_com.succ = succesor;
                    msg_com.pred = predecessor;
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(REMOTE_PORT0));

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(msg_com);
                    oos.flush();
                    Log.e(TAG, "Message Sent");
                    // I have taken the permission from Prof Steve Ko to use Code Snippets from the following websites
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html

                    socket.close();
                }
                else if(msgToSend.equals("query"))
                {
                    //query
                    Log.v(TAG,"Forwarding message "+msgToSend);
                    mesg msg_com = new mesg();
                    msg_com.mesgid = 10;
                    msg_com.mesg = msgToSend;
                    msg_com.port_no = portStr;
                    msg_com.key = msgs[1];

                    String se_port = String.valueOf((Integer.parseInt(msgs[2]) * 2));
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(se_port));

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(msg_com);
                    oos.flush();
                    Log.e(TAG, "Message Sent");
                    // I have taken the permission from Prof Steve Ko to use Code Snippets from the following websites
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html

                    socket.close();
                }
                else
                {
                    //Check the hash value of both the message and the avd ;
                    Log.v(TAG,"Forwarding message "+msgToSend);
                    mesg msg_com = new mesg();
                    msg_com.mesgid = 2;
                    msg_com.mesg = msgToSend;
                    msg_com.port_no = portStr;
                    msg_com.key = msgs[1];
                    String se_port = String.valueOf((Integer.parseInt(msgs[2]) * 2));
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(se_port));

                    ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
                    oos.writeObject(msg_com);
                    oos.flush();
                    Log.e(TAG, "Message Sent");
                    // I have taken the permission from Prof Steve Ko to use Code Snippets from the following websites
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
                    //https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html

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
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
            String sortOrder) {
        // TODO Auto-generated method stub
        MatrixCursor temp = new MatrixCursor(new String[]{"key","value"});
        //temp.newRow().add("key", selection);

        FileInputStream bha = null;
        if(selection.compareTo("*")==0||selection.compareTo("@")==0)
        {
            File dir = getContext().getFilesDir();
            File[] file_list = dir.listFiles();
            for(File tmpf : file_list)
            {
                try {
                    bha = getContext().openFileInput(tmpf.getName());

                } catch (FileNotFoundException e) {
                    e.printStackTrace();

                }
                InputStreamReader bha1 = new InputStreamReader(bha);
                BufferedReader bufferedReader = new BufferedReader(bha1);
                StringBuilder lm = new StringBuilder();
                String line;
                try {
                    while ((line = bufferedReader.readLine()) != null) {
                        lm.append(line);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //  temp.newRow().add("value",sb.toString())
                //         .add("key", selection);
                temp.addRow(new String[]{tmpf.getName(), lm.toString()});
            }
            if(selection.compareTo("*")==0 && !portStr.equals(succesor))
            {
                // wait and call successors
                flag_query = 1;
                Log.e(TAG,"For * query forwarding from port "+portStr+" to port "+succesor+" with origin port"+portStr);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"query" ,selection, succesor);
                while(flag_query==1)
                {

                }
                // Iterate through hashmap and add rows to temp;
                for(int i1=0;i1<k.size();i1++)
                {
                    temp.addRow(new String[]{k.get(i1).toString(), v.get(i1).toString()});
                }
            }
        }
        else {
            File dir = getContext().getFilesDir();
            File[] file_list = dir.listFiles();
            int yu = 0;
            for(File tmpf : file_list)
            {
                if(tmpf.getName().equals(selection))
                {
                    yu = 1;
                    try {
                        bha = getContext().openFileInput(selection);

                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    InputStreamReader bha1 = new InputStreamReader(bha);
                    BufferedReader bufferedReader = new BufferedReader(bha1);
                    StringBuilder lm = new StringBuilder();
                    String line;
                    try {
                        while ((line = bufferedReader.readLine()) != null) {
                            lm.append(line);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    //  temp.newRow().add("value",sb.toString())
                    //         .add("key", selection);
                    temp.addRow(new String[]{selection, lm.toString()});
                    break;
                }
            }
            if(yu==0)
            {
                flag_query=1;
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"query" ,selection, succesor);
                while(flag_query==1)
                {

                }
                temp.addRow(new String[]{query_key,query_value});

            }

            Log.v("query", selection);

        }
        return temp;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }
}
