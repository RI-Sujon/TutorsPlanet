package com.example.tuitionapp_surji.calendar;


import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;

import android.os.Build;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.tuitionapp_surji.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;


public final class CalendarEventCreateActivity extends Activity  {

    /**
     * Logging level for HTTP requests/responses.
     *
     * <p>
     * To turn on, set to {@link Level#CONFIG} or {@link Level#ALL} and run this from command line:
     * </p>
     *
     * <pre>
     adb shell setprop log.tag.HttpTransport DEBUG
     * </pre>
     */
    private static final Level LOGGING_LEVEL = Level.OFF;

    private static final String PREF_ACCOUNT_NAME = "accountName";

    static final String TAG = "CalendarSampleActivity";

    private static final int CONTEXT_EDIT = 0;

    private static final int CONTEXT_DELETE = 1;

    private static final int CONTEXT_BATCH_ADD = 2;

    static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;

    static final int REQUEST_AUTHORIZATION = 1;

    static final int REQUEST_ACCOUNT_PICKER = 2;

    private final static int ADD_OR_EDIT_CALENDAR_REQUEST = 3;

    private static final int ADD_EVENT= 4;

    final HttpTransport transport = AndroidHttp.newCompatibleTransport();

    final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

    private GoogleAccountCredential credential;

    CalendarModel model = new CalendarModel();

    private ArrayAdapter<CalendarInfo> adapter;

    com.google.api.services.calendar.Calendar client;

    int numAsyncTasks;

    private ListView listView;

    private static final List<String> SCOPES = Collections.singletonList(CalendarScopes.CALENDAR_READONLY);
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    final HttpTransport HTTP_TRANSPORT = AndroidHttp.newCompatibleTransport();

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private com.google.api.services.calendar.Calendar service;

    private String meetingId;

    private ArrayList<String> eventInfoList = new ArrayList<>();

    private ArrayList<String> userInfo ;
    private FirebaseUser firebaseUser;



    private EditText eventTitle, location, description, date, startTime, endTime, attendees;
    private Button submitButton;

    private String eventTitle_txt, location_txt, description_txt, date_txt, startTime_txt, endTime_txt, attendees_txt;
    private DatePicker date_picker;
    private TextView save_btn,cancel_btn,set_starting_time,set_ending_time,time_save_btn, time_cancel_btn;
    private TextView set_date;
    private TimePicker startTimePicker;
    private TimePicker endTimePicker;
    private Dialog mDialog,mTimeDialog;
    private String dateString=null, startTimeString=null,endTimeString=null;



    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        // enable logging
        Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

        setContentView(R.layout.activity_calendar_event_create);
        //listView = findViewById(R.id.list);
        //registerForContextMenu(listView);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mDialog= new Dialog(this);
        mTimeDialog= new Dialog(this);
        Intent intent = getIntent() ;
        userInfo = intent.getStringArrayListExtra("userInfo") ;


        credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
        //credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
        credential.setSelectedAccountName(firebaseUser.getEmail());

        client = new com.google.api.services.calendar.Calendar.Builder(
                transport, jsonFactory, credential).setApplicationName("Google-CalendarAndroidSample")
                .build();


        eventTitle = findViewById(R.id.eventTitle);
        location = findViewById(R.id.location);
        description = findViewById(R.id.description);
        //datePicker = findViewById(R.id.datePicker);
        //startTimePicker = findViewById(R.id.startTimePicker);
        //endTimePicker = findViewById(R.id.endTimePicker);
        attendees = findViewById(R.id.attendees);
        set_date = findViewById(R.id.set_date);
        set_starting_time = findViewById(R.id.set_starting_time);
        set_ending_time = findViewById(R.id.set_ending_time);

    }



  /*  void refreshView()
    {
        adapter = new ArrayAdapter<CalendarInfo>(
                this, android.R.layout.simple_list_item_1, model.toSortedArray()) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                // by default it uses toString; override to use summary instead
                TextView view = (TextView) super.getView(position, convertView, parent);
                CalendarInfo calendarInfo = getItem(position);
                view.setText(calendarInfo.summary);
                return view;
            }
        };
        //listView.setAdapter(adapter);
    }
*/
    @Override
    protected void onResume()
    {
        super.onResume();
        if (checkGooglePlayServicesAvailable())
        {
            haveGooglePlayServices();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_GOOGLE_PLAY_SERVICES:
                if (resultCode == Activity.RESULT_OK) {
                    haveGooglePlayServices();
                } else {
                    checkGooglePlayServicesAvailable();
                }
                break;
            case REQUEST_AUTHORIZATION:
                if (resultCode == Activity.RESULT_OK) {
                    AsyncLoadCalendars.run(this);
                } else {
                    chooseAccount();
                }
                break;
            case REQUEST_ACCOUNT_PICKER:
                if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        credential.setSelectedAccountName(accountName);
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        AsyncLoadCalendars.run(this);
                    }
                }
                break;
            case ADD_OR_EDIT_CALENDAR_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    Calendar calendar = new Calendar();
                    calendar.setSummary(data.getStringExtra("summary"));
                    String id = data.getStringExtra("id");
                    if (id == null) {
                        new AsyncInsertCalendar(this, calendar).execute();
                    } else {
                        calendar.setId(id);
                        new AsyncUpdateCalendar(this, id, calendar).execute();
                    }
                }
                break;

            case ADD_EVENT:
                if (resultCode == Activity.RESULT_OK) {
                    System.out.println("BBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB");
                    //new CalendarEventAsyncTask(client).execute();
                }
                break;

        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
           /* case R.id.menu_refresh:
                AsyncLoadCalendars.run(this);
                break;*/
            case R.id.menu_accounts:
                chooseAccount();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, CONTEXT_EDIT, 0, R.string.edit);
        menu.add(0, CONTEXT_DELETE, 0, R.string.delete);
        menu.add(0, CONTEXT_BATCH_ADD, 0, R.string.batchadd);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
        int calendarIndex = (int) info.id;
        if (calendarIndex < adapter.getCount()) {
            final CalendarInfo calendarInfo = adapter.getItem(calendarIndex);
            switch (item.getItemId()) {
                case CONTEXT_EDIT:
                    startAddOrEditCalendarActivity(calendarInfo);
                    return true;
                case CONTEXT_DELETE:
                    new AlertDialog.Builder(this).setTitle(R.string.delete_title)
                            .setMessage(calendarInfo.summary)
                            .setCancelable(false)
                            .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int which) {
                                    new AsyncDeleteCalendar(CalendarEventCreateActivity.this, calendarInfo).execute();
                                }
                            })
                            .setNegativeButton(R.string.no, null)
                            .create()
                            .show();
                    return true;
                case CONTEXT_BATCH_ADD:
                    List<Calendar> calendars = new ArrayList<Calendar>();
                    for (int i = 0; i < 3; i++) {
                        Calendar cal = new Calendar();
                        cal.setSummary(calendarInfo.summary + " [" + (i + 1) + "]");
                        calendars.add(cal);
                    }
                    new AsyncBatchInsertCalendars(this, calendars).execute();
                    return true;
            }
        }
        return super.onContextItemSelected(item);
    }


    /** Check that Google Play services APK is installed and up to date. */
    private boolean checkGooglePlayServicesAvailable() {
        GoogleApiAvailability g1 = GoogleApiAvailability.getInstance();
        final int connectionStatusCode = g1.isGooglePlayServicesAvailable(this);
        if (GoogleApiAvailability.getInstance().isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
            return false;
        }
        return true;
    }

    private void haveGooglePlayServices() {
        // check if there is already an account selected
        if (credential.getSelectedAccountName() == null) {
            // ask user to choose account
            chooseAccount();
        } else {
            // load calendars
            AsyncLoadCalendars.run(this);
        }
    }

    private void chooseAccount() {
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }


    private void startAddOrEditCalendarActivity(CalendarInfo calendarInfo) {
        Intent intent = new Intent(this, AddOrEditCalendarActivity.class);
        if (calendarInfo != null) {
            intent.putExtra("id", calendarInfo.id);
            intent.putExtra("summary", calendarInfo.summary);
        }
        startActivityForResult(intent, ADD_OR_EDIT_CALENDAR_REQUEST);
    }




    void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode)
    {
        runOnUiThread(new Runnable() {
            public void run() {
                GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
                Dialog dialog = googleApiAvailability.getErrorDialog(CalendarEventCreateActivity.this,
                        connectionStatusCode,  REQUEST_GOOGLE_PLAY_SERVICES);
                dialog.show();
            }

            //apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST).show();
        });
    }





    @SuppressLint("NewApi")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void createEvent(View view) throws InterruptedException {

        mDialog.setContentView(R.layout.custom_pop_up_date_picker);
        date_picker = mDialog.findViewById(R.id.date_picker);
        mTimeDialog.setContentView(R.layout.custom_pop_up_time_picker);



        eventTitle_txt =eventTitle.getText().toString() ;
        location_txt = location.getText().toString();
        description_txt = description.getText().toString();
        date_txt = dateString;
        startTime_txt = startTimeString;
        endTime_txt = endTimeString;
        attendees_txt = attendees.getText().toString();

        if(eventTitle_txt.length()==0){
            Toast.makeText(this, "Add event title.", Toast.LENGTH_SHORT).show();
        }

        else if(location_txt.length()==0){
            Toast.makeText(this, "Add event location.", Toast.LENGTH_SHORT).show();
        }

        else if(description_txt.length()==0){
            Toast.makeText(this, "Add event description.", Toast.LENGTH_SHORT).show();
        }

        else if(date_txt==null){
            Toast.makeText(this, "Set event date.", Toast.LENGTH_SHORT).show();
        }

        else if(startTime_txt==null){
            Toast.makeText(this, "Set event starting time.", Toast.LENGTH_SHORT).show();
        }

        else if(endTime_txt==null){
            Toast.makeText(this, "Set event ending time.", Toast.LENGTH_SHORT).show();
        }

       /* else if(attendees_txt.length()==0){
            Toast.makeText(this, "Add at least one guest email.", Toast.LENGTH_SHORT).show();
        }*/

        else{
            CalendarEventCreateAsyncTask calendarEventCreateAsyncTask =   new CalendarEventCreateAsyncTask(this,client,eventTitle_txt, location_txt, description_txt, date_txt, startTime_txt,
                    endTime_txt, attendees_txt,getWeek(date_picker),userInfo);
            calendarEventCreateAsyncTask.execute();
        }

    }


    public  void setMeetingId(String meetingId){
        this.meetingId = meetingId;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showPopUpDate(View v){
        mDialog.setContentView(R.layout.custom_pop_up_date_picker);
        date_picker = mDialog.findViewById(R.id.date_picker);
        save_btn = mDialog.findViewById(R.id.save_btn);
        cancel_btn = mDialog.findViewById(R.id.cancel_btn);

        dateString = getDate(date_picker);
        mDialog.show();

        save_btn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                dateString = getDate(date_picker);
                mDialog.dismiss();
                set_date.setText(dateParser(dateString,getWeek(date_picker)));

            }
        });

        cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showPopUpTime01(View v){
        mTimeDialog.setContentView(R.layout.custom_pop_up_time_picker);
        startTimePicker = mTimeDialog.findViewById(R.id.time_picker);
        time_save_btn= mTimeDialog.findViewById(R.id.time_save_btn);
        time_cancel_btn = mTimeDialog.findViewById(R.id.time_cancel_btn);

        startTimeString = getTime(startTimePicker);
        mTimeDialog.show();

        time_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTimeString = getTime(startTimePicker);
                mTimeDialog.dismiss();
                set_starting_time.setText(timeParser(startTimeString));
            }
        });

        time_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeDialog.dismiss();

            }
        });

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showPopUpTime02(View v){
        mTimeDialog.setContentView(R.layout.custom_pop_up_time_picker);
        endTimePicker = mTimeDialog.findViewById(R.id.time_picker);
        time_save_btn= mTimeDialog.findViewById(R.id.time_save_btn);
        time_cancel_btn = mTimeDialog.findViewById(R.id.time_cancel_btn);


        endTimeString = getTime(endTimePicker);
        mTimeDialog.show();

        time_save_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTimeString = getTime(endTimePicker);
                mTimeDialog.dismiss();
                set_ending_time.setText(timeParser(endTimeString));
            }
        });

        time_cancel_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimeDialog.dismiss();

            }
        });


    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getDate(DatePicker datePicker)
    {


        StringBuilder stringBuilder  = new  StringBuilder();

        stringBuilder.append(datePicker.getYear()+"-");

        if(datePicker.getMonth() < 10){
            int a =datePicker.getMonth()+1;
                    stringBuilder.append("0"+a+"-");
        }
        else
            stringBuilder.append(datePicker.getMonth()+1+"-");


        if(datePicker.getDayOfMonth() <10){

            stringBuilder.append("0"+datePicker.getDayOfMonth());
        }

        else
            stringBuilder.append(datePicker.getDayOfMonth());

        return stringBuilder.toString();
    }


    private String timeParser(String eventTime)
    {
        String[] time = eventTime.split(":");
        int hr,min;
        String time_period="AM";

        hr= Integer.parseInt(time[0]);
        min= Integer.parseInt(time[1]);
        if(hr>12){
            hr=hr-12;
            time_period = "PM";
        }
        else if(hr==12)
            time_period="PM";

        else if(hr==0){
            hr=12;
        }

        System.out.println(hr+":"+time[1]+ " "+time_period);

        String parsingTime, stringHour;
        stringHour = String.valueOf(hr);
        if(stringHour.length()==1){
            parsingTime = "0"+hr+":"+time[1]+ " "+time_period;
        }

        else{
            parsingTime = hr+":"+time[1]+ " "+time_period;
        }

        return parsingTime ;
    }


    private String dateParser(String date, String weekDay) {

        String[] dates = date.split("-");
        ArrayList<String> stringArrayList = new ArrayList<>();
        ArrayList<String> week_month = new ArrayList<>(2);
        stringArrayList.add(dates[0]);
        stringArrayList.add(dates[1]);
        stringArrayList.add(dates[2]);

        for (int i=0; i<stringArrayList.size();i++){
            System.out.println(stringArrayList.get(i));
        }

        String[] months = {"January" ,"February","March","April","May","June","July","August",
                "September","October","November","December"
        };

        week_month.add(months[Integer.parseInt(stringArrayList.get(1))-1]);
        System.out.println(week_month.get(0));
        String dateFormat = weekDay +", "+week_month.get(0)+" "+ stringArrayList.get(2)+", "+stringArrayList.get(0);

        return dateFormat;
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String getWeek(DatePicker datePicker)
    {
        DateTimeFormatter dayOfWeekFormatter = DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH);
        LocalDate date = LocalDate.of(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth()+3);
       // System.out.println("Week day =========================== "+date.format(dayOfWeekFormatter));
        String weekDay = date.format(dayOfWeekFormatter);
        return weekDay;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    public String getTime(TimePicker timePicker)
    {
        StringBuilder stringBuilder  = new  StringBuilder();

        if(timePicker.getHour()<10){
            stringBuilder.append("0"+timePicker.getHour()+":");
        }

        else
            stringBuilder.append(timePicker.getHour()+":");


        if(timePicker.getMinute()<10){
            stringBuilder.append("0"+timePicker.getMinute());
        }

        else
            stringBuilder.append(timePicker.getMinute());

        stringBuilder.append(":00");

        return stringBuilder.toString();
    }


    public void goToHomePageActivity(View view) {

        Intent intent = new Intent(this, CalendarHomeActivity.class);
        intent.putStringArrayListExtra("userInfo", userInfo) ;
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, CalendarHomeActivity.class);
        intent.putStringArrayListExtra("userInfo", userInfo) ;
        startActivity(intent);
        finish();
    }

    public void ViewTheEvents(View view) {
        Intent intent = new Intent(this, CalendarEventViewActivity.class);
        intent.putStringArrayListExtra("userInfo", userInfo) ;
        startActivity(intent);
        finish();
    }
}

























