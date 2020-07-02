package com.example.tuitionapp_surji.calendar;

import android.content.Intent;
import android.os.AsyncTask;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.api.services.calendar.model.EventReminder;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;


public class CalendarEventAsyncTask extends AsyncTask<Void, Void, String>
{

    String title;
    String location;
    String description;
    String date;
    String startTime;
    String endTime;
    String attendee;
    String weekDay;
    ArrayList<String> allAttendees = new ArrayList<>(50);
    String[] strings;

    com.google.api.services.calendar.Calendar service;

    String s1,s2;

    CalendarSampleActivity parent;
    private ArrayList<String> userInfo ;



    public CalendarEventAsyncTask(CalendarSampleActivity parent, Calendar service, String title, String location, String description, String date, String startTime,
                                  String endTime, String attendee, String weekDay, ArrayList<String> userInfo) {

        this.parent = parent;
        this.service = service;
        this.title = title;
        this.location = location;
        this.description = description;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.attendee = attendee;
        this.weekDay = weekDay;
        this.userInfo = userInfo;

    }


    @Override
    protected String doInBackground(Void... voids)
    {

        strings = attendee.split(",");

        for(String s:strings){
            allAttendees.add(s);
        }

        Event event = new Event()
                .setSummary(title)
                .setLocation(location)
                .setDescription(description);


        DateTime startDateTime = new DateTime( date +"T"+startTime+"+06:00" );//"2020-05-05T11:00:00+06:00");
        EventDateTime start = new EventDateTime()
                .setDateTime(startDateTime)
                .setTimeZone("Asia/Dhaka");
        event.setStart(start);

        DateTime endDateTime = new DateTime(date +"T"+endTime+"+06:00");//"2020-05-05T12:00:00+06:00");
        EventDateTime end = new EventDateTime()
                .setDateTime(endDateTime)
                .setTimeZone("Asia/Dhaka");
        event.setEnd(end);

        String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=1"};
        event.setRecurrence(Arrays.asList(recurrence));

      /*  s1 = "rahimsumon29@gmail.com";
        s2 = "nadimahmed1028@gmail.com";

        EventAttendee[] attendees = new EventAttendee[] {
                new EventAttendee().setEmail(s1),
                new EventAttendee().setEmail(s2),
        };*/

        EventAttendee attendees[];

        attendees = new EventAttendee[allAttendees.size()];

        for(int i=0; i<allAttendees.size(); i++){
            System.out.println(allAttendees.get(i));
            attendees[i] = new EventAttendee().setEmail(allAttendees.get(i));
        }
        event.setAttendees(Arrays.asList(attendees));



        EventReminder[] reminderOverrides = new EventReminder[] {
                new EventReminder().setMethod("email").setMinutes(24 * 60),
                new EventReminder().setMethod("popup").setMinutes(10),
        };


        Event.Reminders reminders = new Event.Reminders()
                .setUseDefault(false)
                .setOverrides(Arrays.asList(reminderOverrides));
        event.setReminders(reminders);

        String calendarId = "primary";

        try {
            event = service.events().insert(calendarId, event).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String meetingId = event.getHangoutLink();
        //System.out.println(meetingId);

        //System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");

        parent.setMeetingId(meetingId);


        String eventId = event.getId();
        //System.out.println("EVENTIDDDDDDDDDDDDDDDDDDDDDDDDDD = "+eventId);


        System.out.printf("Event created: %s\n", event.getHtmlLink());

        updateDataOnFireBase(meetingId,eventId,title,location,description,date,startTime,endTime,attendee,weekDay);

        return calendarId;

    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);

        Intent intent = new Intent(parent, CalendarHomeActivity.class);
        intent.putStringArrayListExtra("userInfo", userInfo) ;
        parent.startActivity(intent);
        parent.finish();
    }

    public void updateDataOnFireBase(String meetingId, String eventId, String eventTitle, String location, String description,
                                     String date, String startTime, String endTime, String attendee, String weekDay){
        FirebaseUser  firebaseUser= FirebaseAuth.getInstance().getCurrentUser();
        String userId=firebaseUser.getUid();
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();

        String newDate = dateParser(date,weekDay);
        String newStarTime = timeParser(startTime);
        String newEndTime = timeParser(endTime);

        HashMap<String,String> hashMap = new HashMap<>();
        hashMap.put("eventCreatorId",userId);
        hashMap.put("eventId",eventId);
        hashMap.put("meetingId",meetingId);
        hashMap.put("eventTitle",eventTitle);
        hashMap.put("location",location);
        hashMap.put("description",description);
        hashMap.put("date",newDate);
        hashMap.put("startTime",newStarTime);
        hashMap.put("endTime",newEndTime);
        hashMap.put("attendee",attendee);
        hashMap.put("weekDay",weekDay);

        databaseReference.child("Events").push().setValue(hashMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                System.out.println("SUJON er Matha");
            }
        });


    }

    private String timeParser(String eventTime) {
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

        return hr+":"+time[1]+ " "+time_period;
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
        String dateFormat = weekDay +", "+week_month.get(0)+" "+ stringArrayList.get(2);


        return dateFormat;
    }

}

















/*try {
                        service = new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                                .setApplicationName("Event Calendar")
                                .build();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Event event = new Event()
                            .setSummary("Google I/O 2015")
                            .setLocation("800 Howard St., San Francisco, CA 94103")
                            .setDescription("A chance to hear more about Google's developer products.");

                    DateTime startDateTime = new DateTime("2020-04-28T09:00:00-07:00");
                    EventDateTime start = new EventDateTime()
                            .setDateTime(startDateTime)
                            .setTimeZone("America/Los_Angeles");
                    event.setStart(start);

                    DateTime endDateTime = new DateTime("2020-04-28T17:00:00-07:00");
                    EventDateTime end = new EventDateTime()
                            .setDateTime(endDateTime)
                            .setTimeZone("America/Los_Angeles");
                    event.setEnd(end);

                    String[] recurrence = new String[] {"RRULE:FREQ=DAILY;COUNT=2"};
                    event.setRecurrence(Arrays.asList(recurrence));

                    EventAttendee[] attendees = new EventAttendee[] {
                            new EventAttendee().setEmail("bsse1028@iit.du.ac.bd"),
                            new EventAttendee().setEmail("nadimahmed1028@gmail.com"),
                    };
                    event.setAttendees(Arrays.asList(attendees));

                    EventReminder[] reminderOverrides = new EventReminder[] {
                            new EventReminder().setMethod("email").setMinutes(24 * 60),
                            new EventReminder().setMethod("popup").setMinutes(10),
                    };
                    Event.Reminders reminders = new Event.Reminders()
                            .setUseDefault(false)
                            .setOverrides(Arrays.asList(reminderOverrides));
                    event.setReminders(reminders);

                    String calendarId = "primary";

                    try {
                        event = service.events().insert(calendarId, event).execute();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.printf("Event created: %s\n", event.getHtmlLink());
*/