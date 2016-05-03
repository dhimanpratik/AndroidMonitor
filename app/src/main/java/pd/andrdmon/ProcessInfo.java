package pd.andrdmon;

import android.util.Log;

/**
 * Created by pratikdhiman on 5/3/16.
 */
public class ProcessInfo {
    String pname, totalmem, residentmem, starttime, data_tot, data_rec, data_send;
    int pid;

    ProcessInfo(String pname, int pid, String totalmem, String residentmem, String starttime,
                String data_tot, String data_rec, String data_send) {
        this.pname = pname;
        this.pid = pid;
        this.totalmem = totalmem;
        this.residentmem = residentmem;
        this.starttime = starttime;
        this.data_tot = data_tot;
        this.data_rec = data_rec;
        this.data_send = data_send;
    }

    void printAll() {
        Log.v("PRINTING", pname + " : " + pid + " : " + totalmem + " : " + residentmem + " : " + starttime);
    }
}

