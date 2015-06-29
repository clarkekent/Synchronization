package com.example.dzm.internet;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.util.ArrayMap;
import android.os.Bundle;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.Html;
import android.util.LruCache;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class MainActivity extends Activity implements SwipeRefreshLayout.OnRefreshListener{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        final View mainDialogView = getLayoutInflater().inflate(R.layout.dialog_layout, null);
        final Dialog cDialog = new AlertDialog.Builder(this).setView(mainDialogView).create();
        final DialogFragment cDialogFrag = new DialogFragment(){
            @Override
            public Dialog onCreateDialog(Bundle savedInstanceState) {
                return cDialog;
            }
        };

        final FragmentManager fManager = getFragmentManager();
        FragmentTransaction fTran = fManager.beginTransaction();
        String[] from = {CourseInfo.Courses.CoursesEleFields.SHORTNAME,
                CourseInfo.UNIVERSITIES + CourseInfo.Universities.UniversityEleFields.SHORTNAME,
                            CourseInfo.Courses.CoursesEleFields.WORKLOAD};
        int[] to = {R.id.course_shortname, R.id.university_shortname, R.id.workload};

        final SimpleAdapter adapter = new SimpleAdapter(this, courseList, R.layout.item_layout, from, to){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View rowView = super.getView(position, convertView, parent);
                final ArrayMap<String, String> aMap = (ArrayMap<String, String>)getItem(position);
                ImageView iView = (ImageView)rowView.findViewById(R.id.course_icon);
                handleSIconView(iView, position, aMap.get(CourseInfo.Courses.CoursesEleFields.SMALLICON));

                rowView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        handleBasicInfoForDiaView();
                        handleUniNameForDiaView();
                        handleCourseIconForDiaView();
                        cDialogFrag.show(fManager, "dialogtag");
                    }

                    private void handleBasicInfoForDiaView(){
                        ((TextView)mainDialogView.findViewById(R.id.class_workload)).setText(aMap.get(CourseInfo.Courses.CoursesEleFields.WORKLOAD));
                        ((TextView)mainDialogView.findViewById(R.id.course_name)).setText(aMap.get(CourseInfo.Courses.CoursesEleFields.NAME));
                        ((TextView)mainDialogView.findViewById(R.id.language)).setText(aMap.get(CourseInfo.Courses.CoursesEleFields.LANGUAGE));
//                        ((TextView)mainDialogView.findViewById(R.id.description)).setText(Html.fromHtml(aMap.get(CourseInfo.Courses.CoursesEleFields.BRIEF)));
                        ((WebView)mainDialogView.findViewById(R.id.description)).loadData(aMap.get(CourseInfo.Courses.CoursesEleFields.BRIEF), "text/html; charset=UTF-8", null);

                    }

                    private void handleUniNameForDiaView(){
                        TextView univer = (TextView)mainDialogView.findViewById(R.id.university_name);
                        univer.setText("");
                        UniversityNameTask currentUniNameTask = uniNameMemo.getUniversityNameTask();
                        if(currentUniNameTask!=null){
                            currentUniNameTask.cancel(true);
                        }
                        UniversityNameTask uNameTask = new UniversityNameTask(thisContext, univer);
                        uNameTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, aMap.get(new StringBuilder(CourseInfo.UNIVERSITIES).append(CourseInfo.Universities.UniversityEleFields.ID).toString()));
                        uniNameMemo.setUniversityNameTask(uNameTask);
                    }

                    private void handleCourseIconForDiaView(){
                        ImageView largeImage = (ImageView)mainDialogView.findViewById(R.id.large_icon);
                        clearImageAndTask(largeImage);
                        ImageTask largeImageTask = new ImageTask(thisContext, largeImage);
                        largeImageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, aMap.get(CourseInfo.Courses.CoursesEleFields.LARGEICON));
                        largeImage.setImageDrawable(new MyBitmapDrawable(thisContext, null, largeImageTask));
                    }
                });

                return rowView;
            }

            private void handleSIconView(ImageView iView, int position, String sIconURL){
                clearImageAndTask(iView);
                Bitmap smallIcon = smallIconCache.get(position);
                if(smallIcon==null){
//                    String sIconURL = aMap.get(CourseInfo.Courses.CoursesEleFields.SMALLICON);
                    ImageTask iTask = new LruImageTask(thisContext, iView, smallIconCache, position);
                    iTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, sIconURL);
                    iView.setImageDrawable(new MyBitmapDrawable(thisContext, null, iTask));
                }else {
                    iView.setImageDrawable(new MyBitmapDrawable(thisContext, smallIcon, null));
                }
            }
        };

        lFrag.setListAdapter(adapter);
        fTran.add(R.id.swipeId, lFrag);
        fTran.commit();

        aReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getBooleanExtra(getResources().getString(R.string.got_data), true)){
                    adapter.notifyDataSetChanged();
                }else {
                    lFrag.setEmptyText("Sorry...Couldn't get data, please refresh");
                }
                refreshLayout.setRefreshing(false);
            }
        };

        aNetInfo = aConnMan.getActiveNetworkInfo();
        if(aNetInfo!=null&&aNetInfo.isConnected()){
            loadData();
        }
    }

    @Override
    protected void onResume(){
        super.onResume();

        lbm.registerReceiver(aReceiver, aFilter);
    }

    @Override
    protected void onPause(){
        super.onPause();
        lbm.unregisterReceiver(aReceiver);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState){}

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh(){
        update();
    }

    private void update(){
        aNetInfo = aConnMan.getActiveNetworkInfo();
        if(aNetInfo!=null&&aNetInfo.isConnected()){
            lFrag.setEmptyText(loadCue);
            loadData();
        }else{
            lFrag.setEmptyText(noConnectionCue);
        }
    }

    private void loadData(){
        try {
            if(mainTask!=null){
                mainTask.cancel(true);
            }
            mainTask = new HttpTask(this, courseList);
            mainTask.execute(getCourseraCoursesURLLinkedWithUniversity());
        } catch (MalformedURLException e) {
            Toast.makeText(this, "change the url", Toast.LENGTH_LONG).show();
//            lFrag.setEmptyText(failureCue);
        }
    }

    private void init(){
        initSmallIconCache();
        resourcea = getResources();
        loadCue = resourcea.getString(R.string.load_data);
        noConnectionCue = resourcea.getString(R.string.no_connection_info);
        failureCue = resourcea.getString(R.string.fail_to_get_data);
        lFrag = new ListFragment(){
            @Override
            public void onViewCreated(View view, Bundle savedInstanceState){
                super.onViewCreated(view, savedInstanceState);
                String emptyText;
                if(aNetInfo!=null&&aNetInfo.isConnected()){
                    emptyText = "Loading...";
                }else {
                    emptyText = "not connect to internet, please check network settings";
                }
                setEmptyText(emptyText);
            }
        };
        refreshLayout = new SwipeRefreshLayout(this){
            @Override
            public boolean canChildScrollUp(){
//                View v = findViewById(android.R.id.list);
                View v = lFrag.getListView();
                if(v!=null){

                    return ViewCompat.canScrollVertically(v, -1);
                }else{
                    return false;
                }
//                return true;
            }
        };
        refreshLayout.setId(R.id.swipeId);
        refreshLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        ((FrameLayout)findViewById(R.id.course_frame)).addView(refreshLayout);
        refreshLayout.setOnRefreshListener(this);
        uniNameMemo = new UniversityNameMemo();
        thisContext = this;
        aFilter = new IntentFilter(Filter.COURSEFILTER);
        lbm = LocalBroadcastManager.getInstance(this);

        aConnMan = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
        courseList = new ArrayList<ArrayMap<String, String>>();
    }

    /**
     * assert no more than one task working on the given ImageView
     * @param iView bound with MyBitmapDrawable
     */
    private void clearImageAndTask(ImageView iView){
        if(iView == null){
            return;
        }else {
            Drawable drawable = iView.getDrawable();
            if(drawable == null){
                return;
            }else {
                ImageTask iTask = ((MyBitmapDrawable) drawable).getTask();
                if (iTask != null){
                    iTask.cancel(true);
                }
                iView.setImageDrawable(null);
                return;
            }
        }
    }

    private URL getCourseraCoursesURLLinkedWithUniversity() throws MalformedURLException{
        String fieldsSet = "fields=estimatedClassWorkload,shortName,smallIcon,name,largeIcon,language,aboutTheCourse";
        String includesSet = "includes=universities";
        StringBuilder sb = new StringBuilder();
        sb.append(CourseInfo.Courses.baseURL);
        sb.append("?");
        sb.append(fieldsSet);
        sb.append('&');
        sb.append(includesSet);
        return new URL(sb.toString());
    }

    private void initSmallIconCache(){
        int smallIconCacheSize = calSmallIconCacheSize();
        smallIconCache = new LruCache<Integer, Bitmap>(smallIconCacheSize){
            @Override
            protected int sizeOf(Integer key, Bitmap value) {
                return value.getByteCount()/1024;
            }
        };
    }

    private int calSmallIconCacheSize(){
        int maxMemory = (int)(Runtime.getRuntime().maxMemory()/1024);
        return maxMemory/smallIconCacheMemoryRatio;
    }

    private LocalBroadcastManager lbm;
    private IntentFilter aFilter;
    private BroadcastReceiver aReceiver;
    private Context thisContext;
    private UniversityNameMemo uniNameMemo;
    private LruCache<Integer, Bitmap> smallIconCache;
    private int smallIconCacheMemoryRatio = 8;
    private ListFragment lFrag;
    private NetworkInfo aNetInfo;
    private ConnectivityManager aConnMan;
    private SwipeRefreshLayout refreshLayout;
    private Resources resourcea;
    private String loadCue;
    private String noConnectionCue;
    private String failureCue;
    private HttpTask mainTask;
    private ArrayList<ArrayMap<String, String>> courseList;
}
