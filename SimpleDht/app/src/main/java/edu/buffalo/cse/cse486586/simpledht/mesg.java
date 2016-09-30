package edu.buffalo.cse.cse486586.simpledht;

import android.database.MatrixCursor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by bhargav on 4/3/16.
 */
public class mesg implements Serializable
{
    String mesg;
    int mesgid;
    String port_no;
    String succ;
    String pred;
    String key;
    String value;
    ArrayList key1 = new ArrayList();
    ArrayList value1 = new ArrayList();

}
