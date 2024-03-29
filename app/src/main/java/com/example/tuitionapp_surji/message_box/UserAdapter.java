package com.example.tuitionapp_surji.message_box;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tuitionapp_surji.R;
import com.example.tuitionapp_surji.candidate_tutor.CandidateTutorInfo;
import com.example.tuitionapp_surji.guardian.GuardianInfo;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private Context mContext;
    private List<MessageBoxInfo> mUsers;
    private  String checkUser;
    private CandidateTutorInfo tutorInfo;
    private String LastMessage;
    private boolean isChat;
    private ArrayList<String> userInfo ;
    //private MessageBoxInfo user = new MessageBoxInfo();
    private DatabaseReference candidateTutorReference, guardianReference;

    public UserAdapter(Context mContext, List<MessageBoxInfo> mUsers, String checkUser, boolean isChat, ArrayList<String> userInfo) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.checkUser = checkUser;
        this.isChat = isChat;
        this.userInfo = userInfo;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(mContext).inflate(R.layout.messagebox_user_item,parent,false);
        return new UserAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        final MessageBoxInfo user=mUsers.get(position);

         candidateTutorReference = FirebaseDatabase.getInstance().getReference("CandidateTutor");
         guardianReference = FirebaseDatabase.getInstance().getReference("Guardian");

         if(checkUser.equals("guardian"))
         {
            candidateTutorReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                {
                    for(DataSnapshot snapshot:dataSnapshot.getChildren())
                    {
                        CandidateTutorInfo candidateTutorInfo = snapshot.getValue(CandidateTutorInfo.class);
                        if(candidateTutorInfo.getEmailPK().equals(user.getTutorEmail()))
                        {

                            CandidateTutorInfo c =candidateTutorInfo;

                            System.out.println("WHY MAN WHY +++++++ === "+c.getUserName());
                            holder.username.setText(c.getUserName());


                                if(candidateTutorInfo.getProfilePictureUri()!= null)
                                    Picasso.get().load(candidateTutorInfo.getProfilePictureUri()).into(holder.profile_image);
                                else
                                    holder.profile_image.setImageResource(R.drawable.user_profile_view);


                            if(isChat){
                                if(candidateTutorInfo.getStatus().equals("online")){
                                    holder.img_on.setVisibility(View.VISIBLE);
                                    holder.img_off.setVisibility(View.GONE);
                                }
                                else
                                {
                                    holder.img_on.setVisibility(View.GONE);
                                    holder.img_off.setVisibility(View.VISIBLE);
                                }
                            }
                            else {
                                holder.img_on.setVisibility(View.GONE);
                                holder.img_off.setVisibility(View.GONE);
                            }

                            break;
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            lastMessageSetter(user.getTutorUid(), holder.last_msg);

        }

        else if(checkUser.equals("tutor")){

            guardianReference.addValueEventListener(new ValueEventListener()
            {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                        GuardianInfo guardianInfo = snapshot.getValue(GuardianInfo.class);

                        assert user !=null;

                        if(guardianInfo.getPhoneNumber()!=null){
                            if(guardianInfo.getPhoneNumber().equals(user.getGuardianMobileNumber()))
                            {
                                holder.username.setText(guardianInfo.getName());

                                if(guardianInfo.getProfilePicUri()!=null){
                                    Picasso.get().load(guardianInfo.getProfilePicUri()).into(holder.profile_image);
                                }

                                else{
                                    holder.profile_image.setImageResource(R.drawable.user_profile_view);
                                }

                                if(isChat){
                                    if(guardianInfo.getStatus().equals("online")){
                                        holder.img_on.setVisibility(View.VISIBLE);
                                        holder.img_off.setVisibility(View.GONE);
                                    }

                                    else
                                    {
                                        holder.img_on.setVisibility(View.GONE);
                                        holder.img_off.setVisibility(View.VISIBLE);
                                    }
                                }

                                else {
                                    holder.img_on.setVisibility(View.GONE);
                                    holder.img_off.setVisibility(View.GONE);
                                }
                                break;
                            }
                        }


                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            lastMessageSetter(user.getGuardianUid(), holder.last_msg);

        }

        /*
        if(user.getGuardianMobileNumber() != ""){
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        }

        else{
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }*/

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);

                if(checkUser.equals("guardian")){
                    intent.putExtra("userId", user.getTutorUid());
                    intent.putExtra("tutorEmail",user.getTutorEmail());
                    intent.putExtra("user", checkUser);
                }

                else if(checkUser.equals("tutor")){
                    intent.putExtra("userId", user.getGuardianUid());
                    intent.putExtra("mobileNumber",user.getGuardianMobileNumber());
                    intent.putStringArrayListExtra("userInfo", userInfo) ;
                    intent.putExtra("user", checkUser);

                }
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        public TextView username;
        public ImageView profile_image;
        public ImageView img_on;
        public ImageView img_off;
        private  TextView last_msg;

        public ViewHolder(View itemView){
            super(itemView);

            username=itemView.findViewById(R.id.username);
            profile_image=itemView.findViewById(R.id.profile_image);
            last_msg=itemView.findViewById(R.id.last_msg);
            img_on=itemView.findViewById(R.id.img_on);
            img_off=itemView.findViewById(R.id.img_off);
        }
    }


    public  void lastMessageSetter(final String userId, final TextView last_msg){
        LastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Chats");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot:dataSnapshot.getChildren()){
                    Chat chat = snapshot.getValue(Chat.class);
                    if( (chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId))
                            || chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())){

                        LastMessage = chat.getMessage() + "  " + chat.getMessage_time();

                        lastMessageCondition(LastMessage,chat.getMessage_time(),last_msg,chat.getMessage_type(),userId,chat);
                    }

                }

                LastMessage = "default";
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void lastMessageCondition(String LastMessage, String message_time, TextView last_msg,  String message_type,String userId, Chat chat) {

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        if(LastMessage!=null){

            if(firebaseUser.getUid().equals(chat.getReceiver())){
                if(chat.getIsSeen().equals("no")){
                    last_msg.setTextColor(Color.parseColor("#000000"));
                    last_msg.setTypeface(null,Typeface.BOLD);
                }
            }

            if(LastMessage.equals("default")){
                last_msg.setText("No message");
            }

            else
            {
                if(LastMessage.length()>35){

                    if(message_type.equals("text")){
                        String shortMessage = LastMessage.substring(0,20);
                        last_msg.setText(shortMessage+"... "+ message_time);
                    }

                    else
                    {
                        if(chat.getSender().equals(userId)){
                            last_msg.setText("You have received a photo.  "+chat.getMessage_time());
                        }

                        else{
                            last_msg.setText("You have sent a photo.  "+chat.getMessage_time());
                        }
                    }
                }
                else
                    last_msg.setText(LastMessage);
            }
        }

        else
            last_msg.setText("No Message");
    }
}
