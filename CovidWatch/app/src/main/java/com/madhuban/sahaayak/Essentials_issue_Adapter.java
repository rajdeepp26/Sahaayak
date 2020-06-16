package com.madhuban.sahaayak;


import android.content.Context;
import android.graphics.Bitmap;
import android.text.Html;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Essentials_issue_Adapter extends RecyclerView.Adapter<Essentials_issue_Adapter.ViewHolder> {
    List<Essen_issuePOJO> list = Collections.emptyList();
    Bitmap getimage=null;
    Context context;
    int i;
    String st="00",isAdmin="00";

    public Essentials_issue_Adapter(ArrayList<Essen_issuePOJO> issuePOJOS, Context context) {

        this.list = issuePOJOS;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.essen_item_layout, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //String img=list.get(position).getImage();

        String mail=list.get(position).getUserEmailId();
        holder.id.setText(list.get(position).getUserEmailId());
        String msg=list.get(position).getUserMessage();
        holder.userMessage.setText("Message :"+"\n\n"+msg);
        String data=list.get(position).getUserName();
        holder.postedBy.setText(" "+data);
        holder.post_time.setText(list.get(position).getTime());
        holder.issueId.setText("#"+list.get(position).getIssueId()+" ");
        String add;
        add="<font color='#F44336'>Posted at : </font>"+list.get(position).getLocation();
        holder.address.setText(Html.fromHtml(add), TextView.BufferType.SPANNABLE);


        st=list.get(position).getIssueStatus();
        try {

            // Log.e("Issue_status",st);
            if (st.contentEquals("1")){
                Log.e("Issue_status",st);
                // Cons.issueSolved=true;
               // Toast.makeText(context, "Issue marked as Solved", Toast.LENGTH_SHORT).show();

                holder.issueStatus.setText("Solved");
                holder.img.setVisibility(View.VISIBLE);
            }else {

                // Toast.makeText(context, "Admin_set"+st, Toast.LENGTH_LONG).show();
                holder.issueStatus.setText("Yet to solve");
            }


        }catch (NullPointerException e){
            e.printStackTrace();
        }


        isAdmin=list.get(position).getIsAdmin();

        try {
            if (isAdmin.contentEquals("1")){
                // Cons.isAdmin=true;
                Log.e("isAdmin",isAdmin);
                Cons.ADMIN_TOKEN=list.get(position).getAdminToken();
                Log.d("Admin ",list.get(position).getAdminToken() );

              //  Toast.makeText(context, "Admin_set", Toast.LENGTH_LONG).show();
            }
        }catch (NullPointerException e){
            e.printStackTrace();
        }

        try {

            if (mail.contentEquals(Cons.UserEmailId)){

                Log.d("abcd",mail+" "+Cons.UserEmailId);

                // Toast.makeText(context, "Solve right"+mail+" "+Cons.UserEmailId, Toast.LENGTH_SHORT).show();

                holder.userMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        // Toast.makeText(context, "Counter "+position, Toast.LENGTH_LONG).show();
                        Cons.item_position=position;

                        i++;
                        int a=5-i;
                        if (holder.img.getVisibility()==View.VISIBLE){

                            Toast.makeText(context, "Your issue is already marked as Solved", Toast.LENGTH_SHORT).show();

                        }else if (a>0){

                            Toast.makeText(context, "You're "+a+" click away from marking your issue as Solved", Toast.LENGTH_SHORT).show();

                        }

                        Log.d("click", String.valueOf(i));
                        if (i==5){

                            Cons.issueSolved=true;
                            Cons.timeStamp=list.get(position).getTime();
                            Log.d("time",list.get(position).getTime());

                            Toast.makeText(context, "Reload the page to see the changes", Toast.LENGTH_SHORT).show();

//                  holder.issueStatus.setText("Solved");
//                  holder.img.setVisibility(View.VISIBLE);

                        }else if (i==12){

                            Cons.isAdmin=true;
                            Cons.adminTimeStamp=list.get(position).getTime();
                            Toast.makeText(context, "You're a admin of the area", Toast.LENGTH_SHORT).show();


                        }


                    }
                });

            }else {

                //Toast.makeText(context, "You're not allowed to mark others issue as Solved", Toast.LENGTH_SHORT).show();
                Log.d("click", String.valueOf(i));


            }

        }catch (NullPointerException e){


        }

        try {

            holder.contactTv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    

                }
            });
        }catch (NullPointerException e){

        }catch (Exception e){

        }






    }



    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView id,userMessage,postedBy,post_time,issueId,issueStatus,address,contactTv;
        CardView cardView;
        ImageView img;
        ViewHolder(View itemView) {
            super(itemView);
            // name = itemView.findViewById(R.id.edit_store_name);
            id=itemView.findViewById(R.id.id);
            userMessage= itemView.findViewById(R.id.detail_content);
            userMessage.setMovementMethod(new ScrollingMovementMethod());
            postedBy=itemView.findViewById(R.id.posted_by);
            post_time=itemView.findViewById(R.id.post_time);
           // storeName=itemView.findViewById(R.id.edit_store_name);
            issueId=itemView.findViewById(R.id.issueId);
            issueStatus=itemView.findViewById(R.id.status);
            img=itemView.findViewById(R.id.st);
            cardView=itemView.findViewById(R.id.cardview);
            address=itemView.findViewById(R.id.address);
            contactTv=itemView.findViewById(R.id.contact);
           // address.setSelected(true);
            //cardView.setOnClickListener(this);



        }


//        @Override
//        public void onClick(View v) {
//            i++;
//            Toast.makeText(context, "TextView Clicked!", Toast.LENGTH_LONG).show();
//
//            Log.d("click", String.valueOf(i));
//            if (i==5){
//
//                Cons.issueSolved=true;
////                holder.issueStatus.setText("Solved");
////                holder.img.setVisibility(View.VISIBLE);
//
//            }else if (i==6){
//
//                Cons.isAdmin=true;
//
//                Log.d("click", String.valueOf(i));
//
//            }
//
//        }
    }




}

