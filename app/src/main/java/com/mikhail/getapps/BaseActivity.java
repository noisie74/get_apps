package com.mikhail.getapps;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BaseActivity extends ListActivity {

    private PackageManager packageManager;
    private List<ApplicationInfo> applist;
    private ApplicationAdapter listadaptor;
    Button runningApps;
    Button getApps;
    ApplicationInfo applicationInfo;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

//        activityManager = (ActivityManager) context
//                .getSystemService(Activity.ACTIVITY_SERVICE);

        getApps = (Button) findViewById(R.id.get_apps);
        runningApps = (Button) findViewById(R.id.get_running_apps);
        packageManager = getPackageManager();
        setClickListener();

    }

    public static String getAppNameByPID(Context context, int pid){
        ActivityManager manager
                = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        for(ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()){
            if(processInfo.pid == pid){
                return processInfo.processName;
            }
        }
        return "";
    }

    private void getRunningApps(){

//        ActivityManager activityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
//
//        long currentMillis = Calendar.getInstance().getTimeInMillis();
//        Calendar cal = Calendar.getInstance();
//
//        for (ActivityManager.RunningServiceInfo info : services) {
//            cal.setTimeInMillis(currentMillis-info.activeSince);
//
//            Log.i("BaseActivity", String.format("Process %s with component %s has been running since %s (%d milliseconds)",
//                    info.process, info.service.getClassName(), cal.getTime().toString(), info.activeSince));
//        }

        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningTaskInfo> recentTasks = activityManager.getRunningTasks(Integer.MAX_VALUE);

        for (int i = 0; i < recentTasks.size(); i++)
        {

            String taskName = recentTasks.get(i).baseActivity.toShortString();
            int lastIndex = taskName.indexOf("/");
            if(-1 != lastIndex)
            {
                taskName = taskName.substring(1,lastIndex);
            }
            PackageManager packageManager = getPackageManager();
            try
            {
                applicationInfo = packageManager.getApplicationInfo(taskName, 0);
            }
            catch (final PackageManager.NameNotFoundException e) {

            }
            final String title = (String)((applicationInfo != null) ? packageManager.getApplicationLabel(applicationInfo) : "???");


            Log.d("Executed apps", "Application executed : " + recentTasks.get(i).baseActivity.toShortString() + "\t\t ID: " + recentTasks.get(i).id + "" + title);
        }
    }



    private void setClickListener() {
        getApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LoadApplications().execute();

            }
        });

        runningApps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getRunningApps();
            }
        });
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        ApplicationInfo app = applist.get(position);
        try {
            Intent intent = packageManager
                    .getLaunchIntentForPackage(app.packageName);

            if (null != intent) {
                startActivity(intent);
            }
        } catch (ActivityNotFoundException e) {
            Toast.makeText(BaseActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(BaseActivity.this, e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
    }

    private List<ApplicationInfo> checkForLaunchIntent(List<ApplicationInfo> list) {
        ArrayList<ApplicationInfo> applist = new ArrayList<>();
        for (ApplicationInfo info : list) {
            try {
                if (null != packageManager.getLaunchIntentForPackage(info.packageName)) {
                    applist.add(info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return applist;
    }

    private class LoadApplications extends AsyncTask<Void, Void, Void> {
        private ProgressDialog progress = null;

        @Override
        protected Void doInBackground(Void... params) {
            applist = checkForLaunchIntent(packageManager.getInstalledApplications(PackageManager.GET_META_DATA));
            listadaptor = new ApplicationAdapter(BaseActivity.this,
                    R.layout.snippet_list_row, applist);

            return null;
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
        }

        @Override
        protected void onPostExecute(Void result) {
            setListAdapter(listadaptor);
            progress.dismiss();
            super.onPostExecute(result);
        }

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(BaseActivity.this, null,
                    "Loading application info...");
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }
    }
}

