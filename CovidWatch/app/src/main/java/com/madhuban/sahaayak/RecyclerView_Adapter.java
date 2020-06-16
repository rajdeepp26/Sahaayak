package com.madhuban.sahaayak;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.madhuban.sahaayak.ViewHolder.decodeFromFirebaseBase64;


public class RecyclerView_Adapter extends RecyclerView.Adapter<ViewHolder> {
    List<StorePOJO> list = Collections.emptyList();
    Bitmap getimage=null,img;
    Context context;
    Bitmap Scaleimage;

    public RecyclerView_Adapter(ArrayList<StorePOJO> storePOJOS, Context context) {

        this.list = storePOJOS;
        this.context = context;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        ViewHolder holder = new ViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
       // holder.name.setText(list.get(position).getTitle());
       // holder.store_image.setImageBitmap(list.get(position).getImage());
        String img=list.get(position).getImage();
        try {
             getimage=decodeFromFirebaseBase64(img);

        } catch (IOException e) {
            e.printStackTrace();
        }catch (NullPointerException e){}
       // Scaleimage = Bitmap.createScaledBitmap(getimage, 1800, 1300, true);

       // holder.store_image.setImageBitmap(getimage);

        holder.store_location.setText(list.get(position).getLocation());
        String data="Uploaded by "+list.get(position).getUploader()+" at "+list.get(position).getTime();
        holder.uploader.setText(data);
        holder.storeName.setText(list.get(position).getStoreName());


        Glide.with(context).load(getimage)
                .centerCrop()
                .useAnimationPool(true)
                .into(holder.store_image);


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
}

class ViewHolder extends RecyclerView.ViewHolder {
   // EditText name;
    ImageView store_image;
    TextView store_location,storeName;
    TextView uploader;
   // TextView time;
    ViewHolder(View itemView) {
        super(itemView);
       // name = itemView.findViewById(R.id.edit_store_name);
        store_location=itemView.findViewById(R.id.store_location);
        store_image= itemView.findViewById(R.id.cover_image);
        uploader=itemView.findViewById(R.id.uploaded_by);
        storeName=itemView.findViewById(R.id.edit_store_name);

        //store_image.setAdjustViewBounds(true);

    }
    public static Bitmap decodeFromFirebaseBase64(String image) throws IOException {
        byte[] decodedByteArray = android.util.Base64.decode(image, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedByteArray, 0, decodedByteArray.length);

    }


}