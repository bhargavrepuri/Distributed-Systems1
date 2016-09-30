package edu.buffalo.cse.cse486586.groupmessenger2;

import java.util.Comparator;

/**
 * Created by bhargav on 3/16/16.
 */
class queuecomp implements Comparator<mesg1> {

    public int compare(mesg1 one, mesg1 two) {

        if(one.Seq_num==two.Seq_num)
        {
            if(one.Status.equals("undeliverable")&&two.Status.equals("Deliverable"))
            {
                return -1;
            }
            else if(one.Status.equals("Deliverable")&&two.Status.equals("undeliverable"))
            {
                return 1;
            }
            return  one.final_proposer.compareTo(two.final_proposer);
        }
        return one.Seq_num -two.Seq_num;
    }

}


