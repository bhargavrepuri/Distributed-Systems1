package edu.buffalo.cse.cse486586.groupmessenger2;

import java.io.Serializable;
import java.util.ArrayList;


/**
 * Created by bhargav on 3/15/16.
 */
public class mesg1 implements Serializable {
    String mesg;
    String mesgid;
    int mesg_no;
    int Proposed_Seq_num;
    int Seq_num;
    int count;
    String Status;
    String proposal_port;
    String senderport;
    String final_proposer;
    String port;
    ArrayList<String> port_list = new ArrayList<String>();

}
