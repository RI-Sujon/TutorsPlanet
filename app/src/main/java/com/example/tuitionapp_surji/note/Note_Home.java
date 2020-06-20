package com.example.tuitionapp_surji.note;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.example.tuitionapp_surji.R;
import com.example.tuitionapp_surji.verified_tutor.VerifiedTutorHomePageActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class Note_Home extends AppCompatActivity {


    private DatabaseReference myRefNote ;
    private StorageReference pdfStorage ;
    final static int PICK_PDF_CODE = 2342 ;

    private ArrayList<String> userInfo ;

    MaterialToolbar note_tool_bar;
    Button addNoteButton, editNoteButton;
    private ArrayList<NoteInfo> addNoteInfoArrayList ;
    private ArrayList<String> addNoteKeyArrayList ;

    private TextView noteAttachmentTextView ;

    private TextView noteTitleTV, noteBodyTV ;
    private TextInputEditText noteTitleEditText ;
    private TextInputEditText notePostEditText ;
    private TextInputLayout noteTitleEditTextLayout ;
    private TextInputLayout notePostEditTextLayout ;
    MaterialEditText noteMaterialEditText;

    private ListView noteListView ;

    private String note_title ;
    private String note_post ;
    private Uri filePath ;
    private String uriString ;
    private String pdfName ;
    //private String groupID ,user ,userEmail;
    private Menu toolbarMenu ;



    private Calendar noteCalendar = Calendar.getInstance();
    private SimpleDateFormat simpleDateFormate1 = new SimpleDateFormat("h:mm a") ;
    private SimpleDateFormat simpleDateFormate2 = new SimpleDateFormat("E, dd MMM yyyy") ;
    private String noteDate;
    private String noteTime;
    int flag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_home);

        Intent intent = getIntent() ;
       // user = intent.getStringExtra("user") ;
        userInfo = intent.getStringArrayListExtra("userInfo") ;
       // userEmail = intent.getStringExtra("userEmail") ;


        note_tool_bar = findViewById(R.id.noteTopAppBar);
        addNoteButton = findViewById(R.id.addNoteButton);
        editNoteButton = findViewById(R.id.editNoteButton);

        noteMaterialEditText = findViewById(R.id.noteMaterialEditText);

        noteTitleTV = findViewById(R.id.note_title_heading) ;
        noteTitleEditText = findViewById(R.id.note_title_edit_text) ;
        noteTitleEditTextLayout = findViewById(R.id.note_titleInputLayout) ;

        noteBodyTV = findViewById(R.id.note_body_heading) ;
        notePostEditText = findViewById(R.id.note_post_edit_text) ;
        notePostEditTextLayout = findViewById(R.id.note_postInputLayout) ;

        noteListView = findViewById(R.id.note_list_view) ;

        myRefNote = FirebaseDatabase.getInstance().getReference("Note") ;
        pdfStorage = FirebaseStorage.getInstance().getReference() ;

        addNoteInfoArrayList = new ArrayList<>() ;
        addNoteKeyArrayList = new ArrayList<>() ;

        toolbarMenu = note_tool_bar.getMenu() ;

        toolbarMenu.findItem(R.id.note_addPost).setVisible(false);
        toolbarMenu.findItem(R.id.note_addAttachment).setVisible(false);
        editNoteButton.setVisibility(View.INVISIBLE);




        myRefNote.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dS1: dataSnapshot.getChildren()){
                    NoteInfo noteInfo = dS1.getValue(NoteInfo.class) ;
                        addNoteInfoArrayList.add(noteInfo) ;
                        addNoteKeyArrayList.add(dS1.getKey());

                    goToNoteListView();
                }
                myRefNote.removeEventListener(this);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });


        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NoteInfo selectedPost = addNoteInfoArrayList.get(position);
                String key = addNoteKeyArrayList.get(position);
                editSelectedNote(selectedPost,key);
               // editNoteOperation();

            }
        });



        note_tool_bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToBackPageActivity();
            }
        });

        note_tool_bar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.note_post:
                        prepareForPostOperation();
                        break;


                    case R.id.note_addPost:
                        addPostOperation(null);
                        break;



                }
                return true;
            }
        });

    }


    public void goToNoteListView(){
        CustomAdapterForNote adapter = new CustomAdapterForNote(this,addNoteInfoArrayList) ;
        noteListView.setAdapter(adapter);
    }

    private void editSelectedNote(final NoteInfo selectedNote, final String key)
    {
        toolbarMenu.findItem(R.id.note_post).setVisible(false);
        toolbarMenu.findItem(R.id.note_attachment).setVisible(false);
        toolbarMenu.findItem(R.id.note_addPost).setVisible(true);
        note_tool_bar.setTitle("EDIT NOTE");

        noteMaterialEditText.setText(selectedNote.getNote_post());

        noteTitleTV.setVisibility(View.INVISIBLE);
        noteBodyTV.setVisibility(View.INVISIBLE);
        noteTitleEditTextLayout.setVisibility(View.INVISIBLE);
        notePostEditTextLayout.setVisibility(View.INVISIBLE);
        addNoteButton.setVisibility(View.INVISIBLE);
        noteListView.setVisibility(View.GONE);
        editNoteButton.setVisibility(View.VISIBLE);


        flag =0;

        editNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flag=1;
                FirebaseDatabase  database = FirebaseDatabase.getInstance();
                DatabaseReference mDatabaseRef = database.getReference().child("Note").child(key);
                DatabaseReference databaseReference =FirebaseDatabase.getInstance().getReference();
                //mDatabaseRef.removeValue();


                HashMap<String,String> hashMap = new HashMap<>();
                HashMap<String,String> hashMap2 = new HashMap<>();
                hashMap.put("noteId",selectedNote.getNoteId());
                hashMap2.put("noteId",selectedNote.getNoteId());
                hashMap.put("note_title",selectedNote.getNote_title());
                hashMap2.put("note_title",selectedNote.getNote_title());
                hashMap.put("note_post",selectedNote.getNote_post());
                hashMap2.put("note_post",noteMaterialEditText.getText().toString());
                hashMap.put("noteTime",selectedNote.getNoteTime());
                hashMap2.put("noteTime",selectedNote.getNoteTime());
                hashMap.put("noteDate",selectedNote.getNoteDate());
                hashMap2.put("noteDate",selectedNote.getNoteDate());
                //databaseReference.child("Note").push().setValue(hashMap2);
                mDatabaseRef.setValue(hashMap2);

            }


        });

        System.out.println("RESULTTTTTTTTTTTTTTTTTTTTTTTT");
        System.out.println(2<<5);


    }

    private void editNoteOperation() {

        Intent intent = new Intent(this, Note_Home.class);
        // intent.putExtra("user",user) ;
        intent.putStringArrayListExtra("userInfo",userInfo) ;
        startActivity(intent);
        finish();

    }


    private void prepareForPostOperation(){
        toolbarMenu.findItem(R.id.note_post).setVisible(false);
        toolbarMenu.findItem(R.id.note_attachment).setVisible(false);
        toolbarMenu.findItem(R.id.note_addPost).setVisible(true);
        note_tool_bar.setTitle("ADD A NOTE");

        noteMaterialEditText.setVisibility(View.GONE);

        noteTitleTV.setVisibility(View.VISIBLE);
        noteBodyTV.setVisibility(View.VISIBLE);
        noteTitleEditTextLayout.setVisibility(View.VISIBLE);
        notePostEditTextLayout.setVisibility(View.VISIBLE);
        addNoteButton.setVisibility(View.VISIBLE);
        noteListView.setVisibility(View.GONE);
        editNoteButton.setVisibility(View.INVISIBLE);
    }

    public void addPostOperation(View view){
        DatabaseReference databaseReference= FirebaseDatabase.getInstance().getReference();
        DatabaseReference databaseArtists = FirebaseDatabase.getInstance().getReference("Note");
        note_post = notePostEditText.getText().toString() ;
        note_title = noteTitleEditText.getText().toString() ;

        noteTime = simpleDateFormate1.format(noteCalendar.getTime());
        noteDate = simpleDateFormate2.format(noteCalendar.getTime());

        if(!note_post.equals("") || !note_title.equals("")){
            //NoteInfo noteInfo = new NoteInfo(note_title, note_post) ;
            //  myRefNote.push().setValue(noteInfo);
            //  databaseReference.child("Events").push().setValue(hashMap);

            String noteId = databaseArtists.push().getKey();

            HashMap<String,String> hashMap = new HashMap<>();
            hashMap.put("noteId",noteId);
            hashMap.put("note_title",note_title);
            hashMap.put("note_post",note_post);
            hashMap.put("noteTime",noteTime);
            hashMap.put("noteDate",noteDate);
            databaseReference.child("Note").push().setValue(hashMap);

        }
        Intent intent = new Intent(this, Note_Home.class);
        // intent.putExtra("user",user) ;
        intent.putStringArrayListExtra("userInfo",userInfo) ;
        startActivity(intent);
        finish();
    }






    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_PDF_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData() ;
            noteAttachmentTextView.setText(filePath.toString());
        }
    }




    public void goToBackPageActivity(){
        Intent intent = new Intent(this, VerifiedTutorHomePageActivity.class);
      //  intent.putExtra("groupID", groupID) ;
        intent.putStringArrayListExtra("userInfo",userInfo) ;
       // intent.putExtra("user", user) ;
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed(){
        goToBackPageActivity();
    }


}
