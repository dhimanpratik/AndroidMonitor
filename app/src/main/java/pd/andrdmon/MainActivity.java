package pd.andrdmon;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.TextView;

import com.jaredrummler.android.processes.*;
import com.jaredrummler.android.processes.models.AndroidAppProcess;
import com.jaredrummler.android.processes.models.Stat;
import com.jaredrummler.android.processes.models.Statm;
import com.jaredrummler.android.processes.models.Status;


import org.w3c.dom.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;
import java.util.zip.DeflaterOutputStream;

public class MainActivity extends Activity {

    public static int nosCPU = 0;
    List<ProcessInfo> pList = new ArrayList<>();
    TextView cpu_usage, mem_usage;
    ActivityManager m;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.recycleview_activity);

        nosCPU = getNumCores();
        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);

        cpu_usage = (TextView) findViewById(R.id.cpu_percent_usage);
        mem_usage = (TextView) findViewById(R.id.mem_percent_usage);


        LinearLayoutManager llm = new LinearLayoutManager(this);
        rv.setLayoutManager(llm);
        rv.setHasFixedSize(true);

        m = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        callAsynchronousTask();

        PackageManager pm = getApplicationContext().getPackageManager();
        List<AndroidAppProcess> processes = AndroidProcesses.getRunningAppProcesses();

        for (AndroidAppProcess p : processes) {
            int pid = 0;
            String appName = "NONE", startTime = " ";
            long startTimee = 0, totalSizeOfProcess = 0, residentSetSize = 0;
            double received = 0, send = 0, total = 0;
            try {


                Stat stat = p.stat();
                pid = stat.getPid();

                //getCPUUsage(pid);
                int parentProcessId = stat.ppid();

                long bootTime = System.currentTimeMillis() - SystemClock.elapsedRealtime();
                startTimee = bootTime + (10 * stat.starttime());
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy KK:mm:ss a", Locale.getDefault());

                startTime = sdf.format(startTimee);

                int policy = stat.policy();
                char state = stat.state();

                Statm statm = p.statm();
                totalSizeOfProcess = statm.getSize();
                residentSetSize = statm.getResidentSetSize();

                PackageInfo packageInfo = p.getPackageInfo(this.getApplicationContext(), 0);
                appName = packageInfo.applicationInfo.loadLabel(pm).toString();

                Status status = p.status();

                int UID = status.getUid();

                received = (double) TrafficStats.getUidRxBytes(UID)

                        / (1024 * 1024);
                send = (double) TrafficStats.getUidTxBytes(UID)
                        / (1024 * 1024);
                total = received + send;

                addToList(appName, pid, Formatter.formatFileSize(this, totalSizeOfProcess), Formatter.formatFileSize(this, residentSetSize), startTime,
                        String.format("%.2f", total) + " MB", String.format("%.2f", received) + " MB", String.format("%.2f", send) + " MB");

                Log.v("PDATA", "\nPid" + pid +
                        "\n ParentProcess " + parentProcessId + "\nStart Time " + startTime
                        + "\nPolicy " + policy + "\nState" + state
                        + "\n Total Size " + Long.toString(totalSizeOfProcess)
                        + "\n Resident Size " + Long.toString(residentSetSize)
                        + "\n Name " + appName
                        + "\n UID " + UID);

            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                appName = e.toString().split(":")[1].toString().trim();
                addToList(appName, pid, Formatter.formatFileSize(this, totalSizeOfProcess), Formatter.formatFileSize(this, residentSetSize), startTime,
                        String.format("%.2f", total) + " MB", String.format("%.2f", received) + " MB", String.format("%.2f", send) + " MB");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Log.v("PDATA", "Done Done");

        RVAdapter adapter = new RVAdapter(pList);
        rv.setAdapter(adapter);

        // Log.v("CPU",Integer.toString(getNumCores()));
    }

    private void addToList(String appName, int pid, String totalSizeOfProcess, String residentSetSize, String startTime, String data_tot, String data_rec, String data_send) {
        ProcessInfo temp = new ProcessInfo(appName, pid,
                totalSizeOfProcess,
                residentSetSize,
                startTime, data_tot, data_rec, data_send);
        // temp.printAll();
        pList.add(temp);
    }


    private int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            return 1;
        }
    }

    public void callAsynchronousTask() {
        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            new CalculateUsage(cpu_usage, mem_usage).execute();

                            ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
                            m.getMemoryInfo(mi);
                            long availableMegs = mi.availMem / 1048576L;

//Percentage can be calculated for API 16+
                            double percentAvail = 100.0 * mi.availMem / mi.totalMem;

                            mem_usage.setText(" " + Integer.toString((int) percentAvail) + "%");


                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 5000); //execute in every 50000 ms
    }
}

class CalculateUsage extends AsyncTask<Void, Void, Void> {
    TextView tcpu, tmem;
    double totalcpu = 0.0;

    CalculateUsage(TextView tcpu, TextView tmem) {
        this.tcpu = tcpu;
        this.tmem = tmem;
    }

    @Override
    protected Void doInBackground(Void... params) {

        float[] coreValues = new float[10];
        //get how many cores there are from function
        int numCores = MainActivity.nosCPU;
        for (byte i = 0; i < numCores; i++) {
            coreValues[i] = readCore(i);
            totalcpu += coreValues[i];
        }


        return null;
    }

    // for multi core value
    private float readCore(int i) {
    /*
     * how to calculate multicore this function reads the bytes from a
     * logging file in the android system (/proc/stat for cpu values) then
     * puts the line into a string then spilts up each individual part into
     * an array then(since he know which part represents what) we are able
     * to determine each cpu total and work then combine it together to get
     * a single float for overall cpu usage
     */
        try {
            RandomAccessFile reader = new RandomAccessFile("/proc/stat", "r");
            // skip to the line we need
            for (int ii = 0; ii < i + 1; ++ii) {

                String line = reader.readLine();
            }
            String load = reader.readLine();
            // cores will eventually go offline, and if it does, then it is at
            // 0% because it is not being
            // used. so we need to do check if the line we got contains cpu, if
            // not, then this core = 0
            if (load.contains("cpu")) {
                String[] toks = load.split(" ");

                // we are recording the work being used by the user and
                // system(work) and the total info
                // of cpu stuff (total)
                // http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                long work1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                        + Long.parseLong(toks[3]);
                long total1 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                        + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                        + Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                        + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

                try {
                    // short sleep time = less accurate. But android devices
                    // typically don't have more than
                    // 4 cores, and I'n my app, I run this all in a second. So,
                    // I need it a bit shorter
                    Thread.sleep(300);
                } catch (Exception e) {
                }

                reader.seek(0);
                // skip to the line we need
                for (int ii = 0; ii < i + 1; ++ii) {
                    reader.readLine();
                }
                load = reader.readLine();

                // cores will eventually go offline, and if it does, then it is
                // at 0% because it is not being
                // used. so we need to do check if the line we got contains cpu,
                // if not, then this core = 0%
                if (load.contains("cpu")) {
                    reader.close();
                    toks = load.split(" ");

                    long work2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                            + Long.parseLong(toks[3]);
                    long total2 = Long.parseLong(toks[1]) + Long.parseLong(toks[2])
                            + Long.parseLong(toks[3]) + Long.parseLong(toks[4])
                            + Long.parseLong(toks[5]) + Long.parseLong(toks[6])
                            + Long.parseLong(toks[7]) + Long.parseLong(toks[8]);

                    // here we find the change in user work and total info, and
                    // divide by one another to get our total
                    // seems to be accurate need to test on quad core
                    // http://stackoverflow.com/questions/3017162/how-to-get-total-cpu-usage-in-linux-c/3017438#3017438

                    if ((total2 - total1) == 0)
                        return 0;
                    else
                        return (float) (work2 - work1) / ((total2 - total1));

                } else {
                    reader.close();
                    return 0;
                }

            } else {
                reader.close();
                return 0;
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return 0;
    }


    @Override
    protected void onPostExecute(Void res) {
        tcpu.setText(" " + Integer.toString((int) (totalcpu * 100.0)) + "%");
    }

}