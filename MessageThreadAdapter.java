package com.example.chattingdemo;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class MessageThreadAdapter extends RecyclerView.Adapter<MessageThreadAdapter.ViewHolder> {

    //user id
    private String userId;
    private Context context;

    //Tag for tracking self message
    private int SELF = 786;
    private boolean isLoaderVisible = false;
    //ArrayList of messages object containing all the messages in the thread
    private ArrayList<Message> messages;

    //Constructor
    public MessageThreadAdapter(Context context, ArrayList<Message> messages, String userId) {
        this.userId = userId;
        this.messages = messages;
        this.context = context;
    }

    //IN this method we are tracking the self message
    @Override
    public int getItemViewType(int position) {
        //getting message object of current position
        Message message = messages.get(position);

        //If its owner  id is  equals to the logged in user id
        if (message.getUsersId().equals(userId)) {
            return SELF;
        }
        //else returning position
        return position;
    }

    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Creating view
        View itemView;
        //if view type is self
        if (viewType == SELF) {
            //Inflating the layout self
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_thread, parent, false);
        } else {
            //else inflating the layout others
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.chat_thread_other, parent, false);
        }
        //returing the view
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.onBind(position);

        //Adding messages to the views
        /*Message message = messages.get(position);
        TimeAgo ta = new TimeAgo();
        if(message.getType().equals("image")){
            holder.textViewImage.setVisibility(View.VISIBLE);
            holder.textViewMessage.setVisibility(View.GONE);
            Glide.with(context)
                    .load(message.getMessage())
                    .placeholder(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(holder.textViewImage);
        }else if(message.getType().equals("info")){
            try {
                JSONObject res = new JSONObject(message.getMessage());
                String obj_message = res.getString("message");
                String obj_image = res.getString("image");
                String obj_name = res.getString("product_name");
                String obj_price = res.getString("price");
                holder.textViewImage.setVisibility(View.VISIBLE);
                holder.textProductName.setVisibility(View.VISIBLE);
                holder.textProductPrice.setVisibility(View.VISIBLE);
                holder.textViewMessage.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(obj_image)
                        .placeholder(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(holder.textViewImage);
                holder.textViewMessage.setText(obj_message);
                holder.textProductPrice.setText(Html.fromHtml(obj_price));
                holder.textProductName.setText(obj_name);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else {
            holder.textViewImage.setVisibility(View.GONE);
            holder.textProductName.setVisibility(View.GONE);
            holder.textProductPrice.setVisibility(View.GONE);
            holder.textViewMessage.setVisibility(View.VISIBLE);
            holder.textViewMessage.setText(message.getMessage());
        }

        String timeago = ta.covertTimeToText(Utils.getDate(Long.parseLong(message.getSentAt())));
        holder.textViewTime.setText(timeago);*/
        //holder.textViewTime.setText(String.format("%s, %s", message.getName(), timeago));
    }
    /*@Override
    public int getItemCount() {
        return messages.size();
    }*/

    void addItemsTop(List<Message> msg) {
        messages.addAll(0, msg);
        notifyDataSetChanged();
    }

    void addItemsBottom(List<Message> msg) {
        messages.addAll(msg);
        notifyDataSetChanged();
    }

//    public void addLoading() {
//        isLoaderVisible = true;
//        messages.add(new Message());
//        notifyItemInserted(messages.size() - 1);
//    }

    public void removeLoading() {
        isLoaderVisible = false;
        int position = messages.size() - 1;
        Message item = getItem(position);
        if (item != null) {
            messages.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clear() {
        messages.clear();
        notifyDataSetChanged();
    }

    private Message getItem(int position) {
        return messages.get(position);
    }

    @Override
    public int getItemCount() {
        if (messages != null) {
            return messages.size();
        } else {
            return 0;
        }
    }

    //Initializing views
    public class ViewHolder extends BaseViewHolder {
        TextView textViewMessage;
        TextView textViewTime;
        TextView textProductName;
        TextView textProductPrice;
        ImageView textViewImage;

        ViewHolder(View itemView) {
            super(itemView);
            textViewMessage = itemView.findViewById(R.id.textViewMessage);
            textViewTime = itemView.findViewById(R.id.textViewTime);
            textViewImage = itemView.findViewById(R.id.textViewImage);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
        }

        @Override
        protected void clear() {

        }

        @Override
        public void onBind(int position) {
            super.onBind(position);

            textViewMessage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Toast.makeText(context, "" + getAdapterPosition(), Toast.LENGTH_SHORT).show();
                }
            });

            Message message = messages.get(position);
            TimeAgo ta = new TimeAgo();
            if (message.getType().equals("image")) {
                textViewImage.setVisibility(View.VISIBLE);
                textViewMessage.setVisibility(View.GONE);
                Glide.with(context)
                        .load(message.getMessage())
                        .placeholder(R.drawable.ic_launcher_background)
                        .centerCrop()
                        .into(textViewImage);
            } else if (message.getType().equals("info")) {
                try {
                    JSONObject res = new JSONObject(message.getMessage());
                    String obj_message = res.getString("message");
                    String obj_image = res.getString("image");
                    String obj_name = res.getString("product_name");
                    String obj_price = res.getString("price");
                    textViewImage.setVisibility(View.VISIBLE);
                    textProductName.setVisibility(View.VISIBLE);
                    textProductPrice.setVisibility(View.VISIBLE);
                    textViewMessage.setVisibility(View.VISIBLE);
                    Glide.with(context)
                            .load(obj_image)
                            .placeholder(R.drawable.ic_launcher_background)
                            .centerCrop()
                            .into(textViewImage);
                    textViewMessage.setText(obj_message);
                    textProductPrice.setText(Html.fromHtml(obj_price));
                    textProductName.setText(obj_name);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                textViewImage.setVisibility(View.GONE);
                textProductName.setVisibility(View.GONE);
                textProductPrice.setVisibility(View.GONE);
                textViewMessage.setVisibility(View.VISIBLE);
                textViewMessage.setText(message.getMessage());
            }

            String timeago = ta.covertTimeToText(Utils.getDate(Long.parseLong(message.getSentAt())));
            textViewTime.setText(timeago);
        }
    }
}