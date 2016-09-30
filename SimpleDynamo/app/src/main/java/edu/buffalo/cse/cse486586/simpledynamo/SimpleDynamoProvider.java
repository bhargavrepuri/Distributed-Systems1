package edu.buffalo.cse.cse486586.simpledynamo;

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

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Formatter;


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
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

public class SimpleDynamoProvider extends ContentProvider {

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


			String msg_hash = null;
			String avd_hash = null;
			String avd_succ_hash = null;
			String avd_last_hash = null;


			//Calculate hash values


			for(int p1=0;p1<arr1.length-1;p1++)
			{
				String avd1_hash = String.valueOf(Integer.valueOf(arr1[p1]) / 2);
				String avd2_hash = String.valueOf(Integer.valueOf(arr1[p1+1]) / 2);
				String avd5_hash = String.valueOf(Integer.valueOf(arr1[arr1.length-1]) / 2);
				try {
					msg_hash = genHash(selection);
					avd_hash = genHash(avd1_hash);
					avd_last_hash = genHash(avd5_hash);
					avd_succ_hash = genHash(avd2_hash);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				//get the right place and insert
				//First node
				//	lock1.lock();
				if(p1==0&&((msg_hash.compareTo(avd_hash) <= 0)|| (msg_hash.compareTo(avd_last_hash)>0)))
				{

					Log.e(TAG, "key = " + selection + "deleted in " + avd1_hash);
					if(portStr.equals(avd1_hash))
					{
						File file = new File(dir, selection);
						file.delete();
					}
					else {
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete", selection, avd1_hash);
					}
					if(portStr.equals( String.valueOf(Integer.valueOf(arr1[(p1 + 1)]) / 2)))
					{
						File file = new File(dir, selection);
						file.delete();
					}
					else
					{
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete",  selection, String.valueOf(Integer.valueOf(arr1[(p1 + 1)]) / 2));
					}
					if(portStr.equals(String.valueOf(Integer.valueOf(arr1[(p1 + 2)]) / 2)))
					{
						File file = new File(dir, selection);
						file.delete();
					}
					else {
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete",  selection, String.valueOf(Integer.valueOf(arr1[(p1 + 2)]) / 2));
					}

					break;
				}
				//General node
				if (msg_hash.compareTo(avd_hash) > 0 && msg_hash.compareTo(avd_succ_hash) <= 0) {

					Log.e(TAG, "2 key = " + selection + "deleted in " + avd2_hash + "mesg hashval " + msg_hash + "avd hash " + avd_succ_hash);
					if(portStr.equals(avd2_hash))
					{
						File file = new File(dir, selection);
						file.delete();
					}
					else {
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete",  selection, avd2_hash);
					}
					if(portStr.equals(String.valueOf(Integer.valueOf(arr1[(p1 + 2) % arr1.length]) / 2)))
					{
						File file = new File(dir, selection);
						file.delete();
					}
					else
					{
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete",  selection, String.valueOf(Integer.valueOf(arr1[(p1 + 2) % arr1.length]) / 2));
					}
					if(portStr.equals(String.valueOf(Integer.valueOf(arr1[(p1 + 3) % arr1.length]) / 2)))
					{
						File file = new File(dir, selection);
						file.delete();
					}
					else {
						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "delete",  selection, String.valueOf(Integer.valueOf(arr1[(p1 + 3) % arr1.length]) / 2));
					}

					break;
				}
				//	lock1.unlock();
			}




		}
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}
	Lock lock1 = new ReentrantLock();
	String[] arr1 = {REMOTE_PORT4,REMOTE_PORT1,REMOTE_PORT0,REMOTE_PORT2,REMOTE_PORT3};
	@Override
	public  Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		lock1.lock();
		String FILENAME = values.getAsString("key");
		String string1 = values.getAsString("value");



		String msg_hash = null;
		String avd_hash = null;
		String avd_succ_hash = null;
		String avd_last_hash = null;


		//Calculate hash values


		for(int p1=0;p1<arr1.length-1;p1++)
		{
			String avd1_hash = String.valueOf(Integer.valueOf(arr1[p1]) / 2);
			String avd2_hash = String.valueOf(Integer.valueOf(arr1[p1+1]) / 2);
			String avd5_hash = String.valueOf(Integer.valueOf(arr1[arr1.length-1]) / 2);
			try {
				msg_hash = genHash(FILENAME);
				avd_hash = genHash(avd1_hash);
				avd_last_hash = genHash(avd5_hash);
				avd_succ_hash = genHash(avd2_hash);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			//get the right place and insert
			//First node
			//	lock1.lock();
			if(p1==0&&((msg_hash.compareTo(avd_hash) <= 0)|| (msg_hash.compareTo(avd_last_hash)>0)))
			{
				force_insert = 1;
				Log.e(TAG, "key = " + FILENAME + "inserted in " + avd1_hash);
				if(portStr.equals(avd1_hash))
				{
					custom_insert(uri,values);
				}
				else {
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert", string1, FILENAME, avd1_hash);
				}
				if(portStr.equals( String.valueOf(Integer.valueOf(arr1[(p1 + 1)]) / 2)))
				{
					custom_insert(uri,values);
				}
				else
				{
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert", string1, FILENAME, String.valueOf(Integer.valueOf(arr1[(p1 + 1)]) / 2));
				}
				if(portStr.equals(String.valueOf(Integer.valueOf(arr1[(p1 + 2)]) / 2)))
				{
					custom_insert(uri,values);
				}
				else {
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert", string1, FILENAME, String.valueOf(Integer.valueOf(arr1[(p1 + 2)]) / 2));
				}
				force_insert = 0;
				break;
			}
			//General node
			if (msg_hash.compareTo(avd_hash) > 0 && msg_hash.compareTo(avd_succ_hash) <= 0) {
				force_insert = 1;
				Log.e(TAG, "2 key = " + FILENAME + "inserted in " + avd2_hash + "mesg hashval " + msg_hash + "avd hash " + avd_succ_hash);
				if(portStr.equals(avd2_hash))
				{
					custom_insert(uri,values);
				}
				else {
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert", string1, FILENAME, avd2_hash);
				}
				if(portStr.equals(String.valueOf(Integer.valueOf(arr1[(p1 + 2) % arr1.length]) / 2)))
				{
					custom_insert(uri,values);
				}
				else
				{
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert", string1, FILENAME, String.valueOf(Integer.valueOf(arr1[(p1 + 2) % arr1.length]) / 2));
				}
				if(portStr.equals(String.valueOf(Integer.valueOf(arr1[(p1 + 3) % arr1.length]) / 2)))
				{
					custom_insert(uri,values);
				}
				else {
					new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert", string1, FILENAME, String.valueOf(Integer.valueOf(arr1[(p1 + 3) % arr1.length]) / 2));
				}
				force_insert = 0;
				break;
			}
			//	lock1.unlock();
		}
		//Two cases - First node and general node
		/*if(avd_hash.compareTo(pred_hash)<0)
		{
			//First node
			if(msg_hash.compareTo(avd_hash)<0||msg_hash.compareTo(avd_hash)==0)
			{
				Log.v(TAG, "1 Inserting in general node for message " + string1 + " in emulator" + portStr);
				custom_insert(uri, values);
				insert_flag = 1;
				//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"forceinsert", string1, FILENAME, succesor);

			}
			else
			{
				// hash value greater than all the node values then insert in the first node
				if(msg_hash.compareTo(pred_hash)>0) {
					Log.v(TAG,"2 Inserting in general node for message "+string1+" in emulator"+portStr);
					custom_insert(uri, values);
					insert_flag = 1;
				//	new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"forceinsert", string1, FILENAME, succesor);
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
			custom_insert(uri, values);
			insert_flag = 1;
			//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"forceinsert", string1, FILENAME, succesor);
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
					custom_insert(uri, values);
					insert_flag = 1;
					//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"forceinsert", string1, FILENAME, succesor);
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
				custom_insert(uri, values);
				insert_flag = 1;
				//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"forceinsert", string1, FILENAME, succesor);
			}
			else if(msg_hash.compareTo(avd_hash)>0)
			{
				//hash value of message greater than the current node so send to successor
				Log.v(TAG,"8 Forwarding for message "+string1+" in emulator "+portStr+" to emulator "+succesor);
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, string1,FILENAME, succesor);
			}
		}*/
		lock1.unlock();
		return uri;
	}

	public Uri custom_insert(Uri uri, ContentValues values)
	{
		String FILENAME = values.getAsString("key");
		String string1 = values.getAsString("value");
		lock.lock();
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
		lock.unlock();
		Log.v("insert", values.toString());
		return uri;
	}

	static final String TAG = SimpleDynamoProvider.class.getSimpleName();
	static final String REMOTE_PORT0 = "11108";
	static final String REMOTE_PORT1 = "11112";
	static final String REMOTE_PORT2 = "11116";
	static final String REMOTE_PORT3 = "11120";
	static final String REMOTE_PORT4 = "11124";
	static final int SERVER_PORT = 10000;
	private final Uri mUri = Uri.parse("content://edu.buffalo.cse.cse486586.simpledynamo.provider");
	String succesor = null;
	String predecessor = null;
	String prev_predecessor = null;
	String next_succesor = null;
	//    mesg msg_com1 = new mesg();
	ArrayList k = new ArrayList();
	ArrayList v = new ArrayList();
	int j =0,flag_query=0,query_prev = 0,insert_flag=0,force_insert = 0,flag_query1=0,flag_query3=0,flag_pred=0,flag_succ=0;
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

		if(myPort.equals(REMOTE_PORT4))
		{
			succesor = REMOTE_PORT1;
			predecessor = REMOTE_PORT3;
			prev_predecessor = REMOTE_PORT2;
			next_succesor = REMOTE_PORT0;
			//String msg = "request";
			//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
		}
		else if(myPort.equals(REMOTE_PORT1))
		{
			succesor = REMOTE_PORT0;
			predecessor = REMOTE_PORT4;
			prev_predecessor = REMOTE_PORT3;
			next_succesor = REMOTE_PORT2;
		}
		else if(myPort.equals(REMOTE_PORT0))
		{
			succesor = REMOTE_PORT2;
			predecessor = REMOTE_PORT1;
			prev_predecessor = REMOTE_PORT4;
			next_succesor = REMOTE_PORT3;
		}
		else if(myPort.equals(REMOTE_PORT2))
		{
			succesor = REMOTE_PORT3;
			predecessor = REMOTE_PORT0;
			prev_predecessor = REMOTE_PORT1;
			next_succesor = REMOTE_PORT4;
		}
		else if(myPort.equals(REMOTE_PORT3))
		{
			succesor = REMOTE_PORT4;
			predecessor = REMOTE_PORT2;
			prev_predecessor = REMOTE_PORT0;
			next_succesor = REMOTE_PORT1;
		}
		Lock lock3 = new ReentrantLock();
		succesor = String.valueOf(Integer.parseInt(succesor)/2);
		predecessor = String.valueOf(Integer.parseInt(predecessor)/2);
		prev_predecessor = String.valueOf(Integer.parseInt(prev_predecessor)/2);
		//lock3.lock();
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "recovery", "predecessor", predecessor);
		new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"recovery" ,"succesor", succesor);

		//	flag_succ = 1;
	//	while(flag_succ==1)
	//	{

	//	}
	//	lock3.unlock();
		Log.e(TAG,"For avd "+portStr+"Final Predecessor is "+predecessor+"Final succcessor is "+succesor+"with hash value");
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
					 if(newm.mesgid ==2) {
						FileInputStream bha = null;
						//temp.newRow().add("key", selection);

						File dir = getContext().getFilesDir();
						File[] file_list = dir.listFiles();
						//Recovery messages
						Log.e(TAG, "Inserting messages for Recovery node ");
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
							String msg_hash = genHash(tmpf.getName());
							String avd_hash = genHash(portStr);
							String pred_avd_hash = genHash(predecessor);
							String prev_pred_avd_hash = genHash(prev_predecessor);
							String orig_avd_hash = genHash(newm.port_no);
							if(newm.mesg.equals("predecessor"))
							{
								// Need to retrieve messages inserted from predecessor and previous predecessor
								//Special case for first avd
								Log.e(TAG, "Predecessor Entered Recovery  for avd" + newm.port_no + "in avd" + portStr + "with key " + tmpf.getName());
								if(portStr.compareTo("5562")==0||predecessor.compareTo("5562")==0)
								{

									if(portStr.compareTo("5562")==0)
									{
										Log.e(TAG,"Special Case -prev node is 5562 -Predecessor Entered Recovery  for avd"+newm.port_no+"in avd"+portStr+"with key "+tmpf.getName());
										//get keys from predecessor
										if(msg_hash.compareTo(pred_avd_hash)>0||msg_hash.compareTo(avd_hash)<=0)
										{
											newm.mesgid = 22;
											newm.key1.add(tmpf.getName());
											newm.value1.add(lm.toString());

										}
										//get keys from prev predecessor
										if(msg_hash.compareTo(pred_avd_hash)<=0&&msg_hash.compareTo(prev_pred_avd_hash)>0)
										{
											newm.mesgid = 22;
											newm.key1.add(tmpf.getName());
											newm.value1.add(lm.toString());

										}
									}
									else if(predecessor.compareTo("5562")==0)
									{
										Log.e(TAG,"Special Case -prev - prev node is 5562 -Predecessor Entered Recovery  for avd"+newm.port_no+"in avd"+portStr+"with key "+tmpf.getName());
										//Get keys from prev predecessor
										if(msg_hash.compareTo(prev_pred_avd_hash)>0||msg_hash.compareTo(pred_avd_hash)<=0)
										{
											newm.mesgid = 22;
											newm.key1.add(tmpf.getName());
											newm.value1.add(lm.toString());

										}
										//get keys from predecessor
										if(msg_hash.compareTo(avd_hash)<=0&&msg_hash.compareTo(pred_avd_hash)>0)
										{
											newm.mesgid = 22;
											newm.key1.add(tmpf.getName());
											newm.value1.add(lm.toString());

										}

									}
								}
								else if(msg_hash.compareTo(avd_hash)==0||msg_hash.compareTo(pred_avd_hash)==0||(msg_hash.compareTo(avd_hash)<=0&&msg_hash.compareTo(pred_avd_hash)>0)||(msg_hash.compareTo(pred_avd_hash)<=0&&msg_hash.compareTo(prev_pred_avd_hash)>0))
								{
									Log.e(TAG,"General Case-Predecessor Entered Recovery  for avd"+newm.port_no+"in avd"+portStr+"with key "+tmpf.getName());
									newm.mesgid = 22;
									newm.key1.add(tmpf.getName());
									newm.value1.add(lm.toString());

								}
							}
							else
							{
								//Special case for last avd
								Log.e(TAG,"Successor Entered Recovery  for avd"+newm.port_no+"in avd"+portStr+"with key "+tmpf.getName());
								if(newm.port_no.compareTo("5562")==0)
								{

									if(msg_hash.compareTo(orig_avd_hash)<=0||(msg_hash.compareTo(prev_pred_avd_hash)>0))
									{
										Log.e(TAG,"Special Case - Successor Entered Recovery  for avd"+newm.port_no+"in avd"+portStr+"with key "+tmpf.getName());
										newm.mesgid = 23;
										newm.key1.add(tmpf.getName());
										newm.value1.add(lm.toString());
									}
								}
								else
								{
									Log.e(TAG,"Successor Entered Recovery  for avd"+newm.port_no+"in avd"+portStr+"with key "+tmpf.getName());
									if(msg_hash.compareTo(orig_avd_hash)<=0&&msg_hash.compareTo(prev_pred_avd_hash)>0) {
										newm.mesgid = 23;
										newm.key1.add(tmpf.getName());
										newm.value1.add(lm.toString());

									}
								}

							}

							// Generate hash values compare and then inser

						}
						 String s_port = String.valueOf((Integer.parseInt(newm.port_no) * 2));
						 socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								 Integer.parseInt(s_port));
						 ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
						 oos.writeObject(newm);
						 oos.flush();
						 socket.close();
					}
					else if(newm.mesgid==21)
					{
						Log.v(TAG,"Replicating in successor key "+newm.key);
						// Replicate in Successor and pass it to its successor
						force_insert = 1;
						publishProgress(newm.mesg, newm.key);
					/*	newm.mesgid = 22;
						Log.v(TAG,"Replicate in successor - successor is"+succesor);
						String s_port = String.valueOf((Integer.parseInt(succesor) * 2));
						socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
								Integer.parseInt(s_port));

						ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
						oos.writeObject(newm);
						oos.flush();
						socket.close();*/
					}
					else if(newm.mesgid == 22)
					{
						Log.e(TAG,"Predecessor Entered Recovery insert for avd"+portStr+"with key "+newm.key);
						for(int i1=0;i1<newm.key1.size();i1++)
						{
							publishProgress(newm.value1.get(i1).toString(),newm.key1.get(i1).toString());
						}
						flag_pred = 0;

					}
					 else if(newm.mesgid == 23)
					 {
						 Log.e(TAG,"Successor Entered Recovery insert for avd"+portStr+"with key "+newm.key);
						 for(int i1=0;i1<newm.key1.size();i1++)
						 {
							 publishProgress(newm.value1.get(i1).toString(),newm.key1.get(i1).toString());
						 }
						flag_succ = 0;

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
								flag_query3 = 0;

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
								Log.e(TAG, "For * query forwarding from port " + portStr + " to port " + newm.port_no + "with origin"+newm.port_no);
								String s_port = String.valueOf((Integer.parseInt(newm.port_no) * 2));
								socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
										Integer.parseInt(s_port));
								socket.setSoTimeout(2500);
								ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
								oos.writeObject(newm);
								oos.flush();
								socket.close();

							}

						}
						else {
							Log.v(TAG,"Query forwarded from "+newm.port_no+"for key"+newm.key);
							int yu = 0;
						//	for (File tmpf : file_list) {
						//		if (tmpf.getName().equals(newm.key)) {

									try {
										bha = getContext().openFileInput(newm.key);
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
										yu = 1;

									} catch (FileNotFoundException e) {
										e.printStackTrace();
									}

						//			break;
						//		}
						//	}
							if (yu == 0) {
								//Not found so forward to successor
								String s_port = String.valueOf((Integer.parseInt(succesor) * 2));
								socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
										Integer.parseInt(s_port));
								socket.setSoTimeout(2500);
								ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
								oos.writeObject(newm);
								oos.flush();
								socket.close();


							} else {
								// Found so send to original port
								Log.e(TAG,newm.key+"Key found in avd "+portStr+"sending to original port"+newm.port_no);
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

					 else if(newm.mesgid==50)
					 {
						 //delete
						 File dir = getContext().getFilesDir();
						 File file = new File(dir, newm.key);
						 file.delete();

					 }
					else if(newm.mesgid==11)
					{
						//result for query

						lock.lock();
						query_key = newm.key;
						query_value  = newm.value;
						Log.e(TAG,"flag_query is"+flag_query+"flag_query1 is"+flag_query1+"for query"+newm.key);
						if(newm.mesg.equals("1")) {
							flag_query1 = 0;
						}
						else if(newm.mesg.equals("2"))
						{
							flag_query = 0;
						}
						lock.unlock();
						Log.e(TAG,"Received result for query "+newm.key+"value is "+newm.value);

					}


					clientsocket.close();
					Log.e(TAG, "For avd " + portStr + "Predecessor is " + predecessor + "succcessor is " + succesor);

					//j++;
				}


			} catch (EOFException e){
			//	exception_flag1 = 1;
				Log.e(TAG,"EOF exception - flag1");
			}
			catch (SocketTimeoutException e){
			//	exception_flag1 = 1;
				Log.e(TAG,"Socket Timeout Exception - flag1");
			}
			catch (StreamCorruptedException e){
			//	exception_flag1 = 1;
				Log.e(TAG,"Socket Corrupted Exception- flag1");
			}
			catch (IOException e) {
				Log.e(TAG, "ClientTask socket IOException");
				// Log.e(TAG,(String)remotePort);
			//	exception_flag1 = 1;
			}
			catch (Exception e) {
				Log.e(TAG, "Server Socket Exception");
				// Log.e(TAG,(String)remotePort);
			//	exception_flag1 = 1;
			}
			/*if(exception_flag1==1)
			{
				//For query send to the successor
				Log.e(TAG, "Entered Failure for avd " + portStr + " when called to" + newm.port_no);
				if(newm.mesgid==22) {
					Log.e(TAG, "Entered Failure for avd " + portStr + " when called from" + newm.port_no + "for recovery of key" + newm.key);
				}
					if(newm.mesgid==10&&!newm.key.equals("*")) {
						String se_port = String.valueOf((Integer.parseInt(next_succesor)));
						Socket socket = null;
						//	mesg msg_com = new mesg();
						//	msg_com.mesgid = 10;

						try {
							socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
									Integer.parseInt(se_port));
							ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
							oos.writeObject(newm);
							oos.flush();
							Log.e(TAG, "Message Sent");
							// I have taken the permission from Prof Steve Ko to use Code Snippets from the following websites
							//https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
							//https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html

							socket.close();

						} catch (IOException e) {
							e.printStackTrace();
						}
					}

				exception_flag1 = 0;

			}*/
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
			custom_insert(mUri,tobeinserted);
			//kr.insert(mUri, tobeinserted);
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

int exception_flag2 = 0,exception_flag1=0;

	private class ClientTask extends AsyncTask<String, Void, Void> {

		@Override
		protected Void doInBackground(String... msgs) {

			Log.e(TAG, "Entered");
			String msgToSend = msgs[0];

			try {
				Log.e(TAG, "try Entered");
				String remotePort;

				if(msgToSend.equals("forceinsert"))
				{
					mesg msg_com = new mesg();
					msg_com.mesgid = 21;
					msg_com.mesg = msgs[1];
					msg_com.key = msgs[2];
					msg_com.port_no = portStr;
					//msg_com.succ = succesor;
					//msg_com.pred = predecessor;
					Log.e(TAG, "Entered Force Insert");
					String se_port = String.valueOf((Integer.parseInt(msgs[3]) * 2));
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(se_port));
					socket.setSoTimeout(2500);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(msg_com);
					oos.flush();
					Log.e(TAG, "Key sent for insert"+msg_com.key+" to avd "+msgs[3]+"with value "+msgs[1]);
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
					msg_com.mesg = msgs[3];
					msg_com.port_no = portStr;
					msg_com.key = msgs[1];
					//msg_com.pred = predecessor;
					String se_port = String.valueOf((Integer.parseInt(msgs[2]) * 2));
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(se_port));
					socket.setSoTimeout(2500);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(msg_com);
					oos.flush();
					Log.e(TAG, "Message Sent");
					// I have taken the permission from Prof Steve Ko to use Code Snippets from the following websites
					//https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
					//https://docs.oracle.com/javase/tutorial/networking/sockets/readingWriting.html

					socket.close();
				}
				else if(msgToSend.equals("recovery"))
				{
					//query
					Log.v(TAG," Recovery"+msgToSend);
					mesg msg_com = new mesg();
					msg_com.mesgid = 2;
					msg_com.mesg = msgs[1];
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
				else if(msgToSend.equals("delete"))
				{
					Log.v(TAG,"Delete key "+msgs[1]);
					mesg msg_com = new mesg();
					msg_com.mesgid = 50;
					msg_com.port_no = portStr;
					msg_com.key = msgs[1];
					//msg_com.pred = predecessor;
					String se_port = String.valueOf((Integer.parseInt(msgs[2]) * 2));
					Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
							Integer.parseInt(se_port));
					socket.setSoTimeout(2500);
					ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
					oos.writeObject(msg_com);
					oos.flush();
					Log.e(TAG, "Delete Message Sent");
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

			} catch (EOFException e){
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
			catch (IOException e) {
				Log.e(TAG, "ClientTask socket IOException");
				// Log.e(TAG,(String)remotePort);
				exception_flag2 = 1;
			}
			if(exception_flag2==1)
			{
				//For query send to the successor
				if(msgToSend.equals("recovery")) {
					Log.e(TAG, "Entered Failure for avd in client task " + portStr + " when called to avd" + msgs[2] + "for recovery of keys");
				}
				if(msgToSend.equals("forceinsert"))
				{
					Log.e(TAG, "Entered Failure for avd in client task " + portStr + " when called to avd" + msgs[3] + "for insert of key "+msgs[2]);
				}
					if(msgToSend.equals("query")&&!msgs[3].equals("0")) {
						Log.e(TAG, "Entered Failure for avd in client task " + portStr + " when called to avd" + msgs[2] + "for query of keys");
						String succ_fail = null;
						if(msgs[2].equals("5562"))
						{
							succ_fail = REMOTE_PORT1;
						}
						else if(msgs[2].equals("5556"))
						{
							succ_fail = REMOTE_PORT0;
						}
						else if(msgs[2].equals("5554"))
						{
							succ_fail =REMOTE_PORT2;
						}
						else if(msgs[2].equals("5558"))
						{
							succ_fail =REMOTE_PORT3;
						}
						else if(msgs[2].equals("5560"))
						{
							succ_fail =REMOTE_PORT4;
						}
						String se_port = String.valueOf((Integer.parseInt(succ_fail)));
						Socket socket = null;
						mesg msg_com = new mesg();
						msg_com.mesgid = 10;
						msg_com.mesg = msgs[3];
						msg_com.port_no = portStr;
						msg_com.key = msgs[1];
					//	msg_com.pred = predecessor;
						try {
							socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
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
						catch(IOException e){
							e.printStackTrace();
						}
					}
				else if(msgToSend.equals("query")&&msgs[3].equals("0"))
					{
						flag_query3 = 0;
					}
				exception_flag2 = 0;

			}
			return null;
		}
	}

	Lock lock = new ReentrantLock();
	Lock lock2 = new ReentrantLock();
int flag_query4 = 0,flag_query5 = 0,flag_query6=0;

	@Override

	public synchronized Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
						String sortOrder) {
		// TODO Auto-generated method stub

		MatrixCursor temp = new MatrixCursor(new String[]{"key","value"});
		//temp.newRow().add("key", selection);
		int yu1 =0,yu2 =0;
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
				flag_query3 = 1;

				String succ1 = null;
				String succ2 = null;
				String succ3 = null;
				Log.e(TAG,"For * query forwarding from port "+portStr+" to port "+succesor+" with origin port"+portStr);

				for(int ji=0;ji<arr1.length;ji++)
				{
					if(arr1[ji].equals(String.valueOf(Integer.valueOf(succesor)*2)))
					{
						 succ1 = String.valueOf(Integer.valueOf(arr1[(ji+1)%arr1.length]) / 2);
						 succ2 = String.valueOf(Integer.valueOf(arr1[(ji+2)%arr1.length]) / 2);
						 succ3 = String.valueOf(Integer.valueOf(arr1[(ji+3)%arr1.length]) / 2);

					}
				}

				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"query" ,selection, succesor,"0");
				while(flag_query3==1)
				{

				}

				// Iterate through hashmap and add rows to temp;
				for(int i1=0;i1<k.size();i1++)
				{
					Log.e(TAG,"First - Key is "+k.get(i1).toString()+" value is"+ v.get(i1).toString());
					temp.addRow(new String[]{k.get(i1).toString(), v.get(i1).toString()});
				}
				k.clear();
				v.clear();
				flag_query3 = 1;
				Log.e(TAG,"For * query forwarding from port "+portStr+" to port "+succ1+" with origin port"+portStr);
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"query" ,selection, succ1,"0");
				while(flag_query3==1)
				{

				}
				for(int i1=0;i1<k.size();i1++)
				{
					Log.e(TAG,"Second - Key is "+k.get(i1).toString()+" value is"+ v.get(i1).toString());
					temp.addRow(new String[]{k.get(i1).toString(), v.get(i1).toString()});
				}
				k.clear();
				v.clear();
				flag_query3 = 1;
				Log.e(TAG,"For * query forwarding from port "+portStr+" to port "+succ2+" with origin port"+portStr);
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"query" ,selection, succ2,"0");
				while(flag_query3==1)
				{

				}
				for(int i1=0;i1<k.size();i1++)
				{
					Log.e(TAG,"Third - Key is "+k.get(i1).toString()+" value is"+ v.get(i1).toString());
					temp.addRow(new String[]{k.get(i1).toString(), v.get(i1).toString()});
				}
				k.clear();
				v.clear();
				flag_query3 = 1;
				Log.e(TAG,"For * query forwarding from port "+portStr+" to port "+succ3+" with origin port"+portStr);
				new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,"query" ,selection, succ3,"0");
				while(flag_query3==1)
				{

				}
				for(int i1=0;i1<k.size();i1++)
				{
					Log.e(TAG,"Fourth - Key is "+k.get(i1).toString()+" value is"+ v.get(i1).toString());
					temp.addRow(new String[]{k.get(i1).toString(), v.get(i1).toString()});
				}
				k.clear();
				v.clear();
			}
		}

		else {

		//	lock.lock();
			Log.v("query", selection);
			String msg_hash = null;
			String avd_hash = null;
			String avd_succ_hash = null;
			String avd_last_hash = null;
			for(int p1=0;p1<arr1.length-1;p1++)
			{
				String avd1_hash = String.valueOf(Integer.valueOf(arr1[p1]) / 2);
				String avd2_hash = String.valueOf(Integer.valueOf(arr1[(p1+1)%arr1.length]) / 2);
				String avd3_hash = String.valueOf(Integer.valueOf(arr1[(p1+2)%arr1.length]) / 2);
				String avd4_hash = String.valueOf(Integer.valueOf(arr1[(p1+3)%arr1.length]) / 2);
				String avd5_hash = String.valueOf(Integer.valueOf(arr1[arr1.length-1]) / 2);
				try {
					msg_hash = genHash(selection);
					avd_hash = genHash(avd1_hash);
					avd_last_hash = genHash(avd5_hash);
					avd_succ_hash = genHash(avd2_hash);
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				//get the right place and insert
				//First node
				//	lock.lock();
				if(p1==0&&((msg_hash.compareTo(avd_hash) <= 0)|| (msg_hash.compareTo(avd_last_hash)>0))) {

					;
					Log.e(TAG, "key = " + selection + "inserted in " + avd1_hash);
					//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert",selection, avd1_hash);
					if (avd1_hash.equals(portStr)||avd2_hash.equals(portStr)||avd3_hash.equals(portStr))
					{
					//	File dir = getContext().getFilesDir();
					//	File[] file_list = dir.listFiles();

					//	for (File tmpf : file_list) {
					//		if (tmpf.getName().equals(selection)) {
					//			yu2 = 1;
						try {
							bha = getContext().openFileInput(selection);
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

							temp.addRow(new String[]{selection, lm.toString()});
							yu2 = 1;
							Log.e(TAG,"Break");
							break;

						} catch (FileNotFoundException e) {

							e.printStackTrace();
						}
					//		}
					//	 }
					//	if(yu2==1)
					//	{
					//		break;
					//	}
				   }
					if(yu2==0)
					{
						Log.e(TAG,"Entered flag_query1 for key"+selection);
						flag_query1 = 1;

						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query", selection, avd1_hash, "1");

						while(flag_query1==1)
						{
							//Log.e(TAG,"looping for key"+selection);
						}

						temp.addRow(new String[]{query_key, query_value});
						break;
					}

				}
				//General node
				if (msg_hash.compareTo(avd_hash) > 0 && msg_hash.compareTo(avd_succ_hash) <= 0) {



					Log.e(TAG, "2 key = " + selection + "inserted in " + avd2_hash + "mesg hashval " + msg_hash + "avd hash " + avd_succ_hash);
					//new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "forceinsert", selection, avd2_hash);

					if (avd2_hash.equals(portStr)||avd3_hash.equals(portStr)||avd4_hash.equals(portStr))
					{
					//	File dir = getContext().getFilesDir();
					//	File[] file_list = dir.listFiles();
						Log.e(TAG,"Entered query in original port condition for key"+selection);
					//	for (File tmpf : file_list) {
					//		if (tmpf.getName().equals(selection)) {
					//			yu1 = 1;
								Log.e(TAG,"Entered query in original port condition for key"+selection+"and key found");
								try {
									bha = getContext().openFileInput(selection);
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

									temp.addRow(new String[]{selection, lm.toString()});
									yu1 = 1;
									Log.e(TAG,"Break");
									break;

								} catch (FileNotFoundException e) {

									e.printStackTrace();
								}

					//		}
					//	}

					}
					if(yu1==0) {
						Log.e(TAG,"Entered flag_query for key"+selection);
						flag_query = 1;

						new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, "query", selection, avd2_hash, "2");

						while (flag_query == 1) {
							//Log.e(TAG,"looping for key"+selection);
						}


						temp.addRow(new String[]{query_key, query_value});
						break;
					}
				}
				//	lock.unlock();
			}
		//	lock.unlock();
		}

		Log.e(TAG, "Return key is" + query_key + "Value is" + query_value);
		yu1 = 0;
		yu2 = 0;
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
