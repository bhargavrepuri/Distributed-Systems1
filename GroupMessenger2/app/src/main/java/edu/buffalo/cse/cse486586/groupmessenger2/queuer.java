package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

/**
 * Created by bhargav on 3/20/16.
 */
class queuer implements Comparator<mesg1> {
    public int compare(mesg1 one, mesg1 two) {

        return one.Proposed_Seq_num -two.Proposed_Seq_num;
    }
}
