package com.adnan.photoblog;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class BlogPostAdapter extends RecyclerView.Adapter<BlogPostAdapter.BlogPostViewHolder> {

    private List<BlogPost> blog_list;
    private List<User> user_list;
    private Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;


    private static final String TAG = "BlogPostAdapter";
    public BlogPostAdapter(List<BlogPost> blog_list,List<User> user_list){
        this.blog_list=blog_list;
        this.user_list=user_list;

     }

    public void setBlog_list(List<BlogPost> blog_list) {
        this.blog_list = blog_list;
    }

    public void setUser_list(List<User> user_list) {
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public BlogPostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.blog_list_item, parent, false);
       context=parent.getContext();
       firebaseFirestore=FirebaseFirestore.getInstance();
       firebaseAuth=FirebaseAuth.getInstance();

        return new BlogPostViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull BlogPostViewHolder holder, int position) {
            holder.setIsRecyclable(false);

      String blogPostId=blog_list.get(position).BlogPostId;
holder.updateCommentCount(blogPostId);


        String currentUserId=firebaseAuth.getCurrentUser().getUid();
        //Set Description Text
     String desc=blog_list.get(position).getDesc();
     holder.setDescText(desc);
     //Set Image
        String image_uri=blog_list.get(position).getImageOriginalUri();
        String imageThumbs_uri=blog_list.get(position).getImageThumbUri();
        holder.setBlogImage(image_uri,imageThumbs_uri);
        //set user data username and image
        String blog_user_id=blog_list.get(position).getUser_id();
        holder.blogDeleteButton.setEnabled(false);
        if(blog_user_id.equals(currentUserId)){
            holder.blogDeleteButton.setEnabled(true);
            holder.blogDeleteButton.setVisibility(View.VISIBLE);

        }
        String userName=user_list.get(position).getName();
        String userImage=user_list.get(position).getImage();
        holder.setUserData(userName,userImage);
        //set Date
        try {
            long millisecond = blog_list.get(position).getTimestamp().getTime();
            SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
            String dateString = formatter.format(new Date(millisecond));
            holder.SetDate(dateString);
        }catch(Exception e){
            Toast.makeText(context, "Exception" +e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        //get Likes Counts
        firebaseFirestore.collection("posts/"+ blogPostId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(!value.isEmpty()){
                    int count=value.size();
                    holder.updatCount(count);
                }else{
                    holder.updatCount(0);
                }
            }
        });

        //get likes
        firebaseFirestore.collection("posts/"+ blogPostId + "/Likes")
                .document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){

                    holder.likesImageViewBtn.setImageDrawable(context.getDrawable(R.drawable.blog_list_item_accent_like_icon));
                }else{
                    holder.likesImageViewBtn.setImageDrawable(context.getDrawable(R.drawable.blog_list_item_grey_like_icon));
                }
            }
        });

    //this for handle likes
        holder.likesImageViewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("posts/"+ blogPostId + "/Likes")
                        .document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()) {

                    if (!task.getResult().exists()) {
                        Map<String, Object> likesMap = new HashMap<>();
                        likesMap.put("timestamp", FieldValue.serverTimestamp());
                        likesMap.put("blogPostId", blogPostId);
                        firebaseFirestore.collection("posts/" + blogPostId + "/Likes")
                                .document(currentUserId).set(likesMap);
                        String userName=user_list.get(position).getName();
                        String userImage=user_list.get(position).getImage();
                        try {
                            holder.displayNotification(userName,userImage);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        firebaseFirestore.collection("posts/" + blogPostId + "/Likes")
                                .document(currentUserId).delete();
                    }
                }
                    }
                });
            }
        });
        //handel comments
holder.imageViewCommentsBtn.setOnClickListener(new View.OnClickListener() {
    @Override
    public void onClick(View v) {
        Intent commentIntent=new Intent(context,CommentActivity.class);
        commentIntent.putExtra("blogPostId", blogPostId);

        Log.i(TAG, "onClick:blogPostId "+blogPostId);
        context.startActivity(commentIntent);
    }
});

//delete button
        holder.blogDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which){
                            case DialogInterface.BUTTON_POSITIVE:
                                //Do your Yes progress
                                holder.deleteSubCollection(blogPostId, "comments");
                                holder.deleteSubCollection(blogPostId, "Likes");
                                firebaseFirestore.collection("posts")
                                        .document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        blog_list.remove(position);
                                        user_list.remove(position);
                                        setBlog_list(blog_list);
                                        setUser_list(user_list);
                                        notifyDataSetChanged();

                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {

                                    }
                                });
                                break;

                            case DialogInterface.BUTTON_NEGATIVE:
                                //Do your No progress
                                break;
                        }
                    }
                };
                AlertDialog.Builder ab = new AlertDialog.Builder(context);
                ab.setMessage("Are you sure to delete?").setPositiveButton("Yes", dialogClickListener)
                        .setNegativeButton("No", dialogClickListener).show();


            }
        });

    }


    @Override
    public int getItemCount() {
        return blog_list.size();
    }



    public class BlogPostViewHolder extends RecyclerView.ViewHolder{
     private View mView;
     private TextView textViewDesc;
        private TextView textViewUserName;
        private TextView blogDate;
    private ImageView imageViewPostImage;
     private CircleImageView blogImageView;
private Context context;
     //likes and text view
        private ImageView likesImageViewBtn;
        private TextView textViewLikesCount;
        //comments
        private ImageView imageViewCommentsBtn;
        private  TextView textViewComment;
        private TextView textViewCommentsCount;
        //delete button
        private ImageView blogDeleteButton;
        public  static final String CHANNEL_ID="blog1_channelId";
       public BlogPostViewHolder(@NonNull View itemView) {
           super(itemView);
           context=itemView.getContext();
        mView=itemView;
           likesImageViewBtn=mView.findViewById(R.id.blog_list_item_iv_blog_like);
         //  textViewLikesCount =mView.findViewById(R.id.blog_list_item_tv_blog_like_count);
           imageViewCommentsBtn=mView.findViewById(R.id.blog_list_item_iv_comment);
           textViewComment=mView.findViewById(R.id.blog_list_item_tv_comment_count);
           blogDeleteButton=mView.findViewById(R.id.blog_list_item_iv_delete_btn1);
       }
       public void setDescText(String desc){
           textViewDesc=mView.findViewById(R.id.blog_list_item_tv_post_text);
           textViewDesc.setText(desc);
       }
        public void setBlogImage(String downloadUri,String imageThumbs_uri){
            imageViewPostImage=mView.findViewById(R.id.blog_list_item_iv_postImage);
            Glide.with(context).load(downloadUri).thumbnail( Glide.with(context).load(imageThumbs_uri)).into(imageViewPostImage);
        }

        public void setUserData(String userName,String image){
            textViewUserName=mView.findViewById(R.id.blog_list_item_tv_username);
            textViewUserName.setText(userName);
            blogImageView=mView.findViewById(R.id.blog_list_item_circleImageView);
            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.default_image);
           Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(image).into(blogImageView);
        }
        public void SetDate(String date){
            blogDate=mView.findViewById(R.id.blog_list_item_tv_post_date);
            blogDate.setText(date);
        }

        public void updatCount(int count){
            textViewLikesCount=mView.findViewById(R.id.blog_list_item_tv_blog_like_count);
            textViewLikesCount.setText(count +"likes");
        }

        public void updateCommentCount(String blogPostId){
            textViewCommentsCount=mView.findViewById(R.id.blog_list_item_tv_comment_count);
            firebaseFirestore.collection("posts" ).document(blogPostId).collection("comments")
                    .whereEqualTo("blogPostId", blogPostId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                  //  WriteBatch batch=FirebaseFirestore.getInstance().batch();
                    List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
                   int count=documentSnapshots.size();
                    textViewCommentsCount.setText(count +"comments");

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMessgae=e.getMessage();
                    Log.i(TAG, "onFailure: "+errorMessgae);
                }
            });
        }


        public void deleteSubCollection(String documentPath,String sunCollection){


            firebaseFirestore.collection("posts" ).document(documentPath).collection(sunCollection)
                    .whereEqualTo("blogPostId", documentPath).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                @Override
                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                    WriteBatch batch=FirebaseFirestore.getInstance().batch();
                    List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();


                    for(DocumentSnapshot snapshot:documentSnapshots){

                        batch.delete(snapshot.getReference());
                    }
                    batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(context, "All recorded Deleted", Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            String errorMessgae=e.getMessage();
                            Log.i(TAG, "onFailure: "+errorMessgae);
                        }
                    });;
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    String errorMessage=e.getMessage();
                    Log.i(TAG, "onFailure: "+errorMessage);
                }
            });

        }

//get user data to sisplay in notification
//        public void getUserName(){
//            firebaseFirestore.collection("usersPhotoBlog").document(user_id).get()
//                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                        @Override
//                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                            if(task.isSuccessful()){
//                                String userName=task.getResult().getString("name");
//                                String userNameimage=task.getResult().getString("image");
//                                holder.setUserName(userName);
//                                holder.SetUserNameCommentImage(userNameimage);
//                            }else{
//                                String errorMessage=task.getException().getMessage();
//                                Log.i(TAG, "onComplete: Error" +errorMessage);
//                            }
//                        }
//                    });
//
//        }

//display notification
public void displayNotification(String userName,String image) throws IOException {

if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){

    NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID, "channel Display Name", NotificationManager.IMPORTANCE_DEFAULT);
    notificationChannel.setDescription("This is blog channel");
    NotificationManager notificationManager= context.getSystemService(NotificationManager.class);
    notificationManager.createNotificationChannel(notificationChannel);
}
//    NotificationCompat.Builder notificationBuilder=
//            new NotificationCompat.Builder(this,NOTIFICATION_CHANNEL_ID);
//NotificationFragment.
     Uri uri= Uri.parse(image);
   Log.i(TAG, "displayNotification: " +uri);
 // Bitmap bitmap = MediaStore.Images.Media.getBitmap( context.getContentResolver(), uri);
//    Log.i(TAG, "displayNotification: " +bitmap);
  Bitmap bitmap=decodeUriToBitmap(context,uri);
   Log.i(TAG, "displayNotification: " +bitmap);
    Intent intent=new Intent(context,MainActivity.class);
    PendingIntent pendingIntent=PendingIntent.getActivity(context, 0, intent, 0);

    NotificationCompat.Builder builder=new  NotificationCompat.Builder(context,CHANNEL_ID);
                    builder.setSmallIcon(R.drawable.blog_list_item_notification)
                    .setContentTitle(userName)
                        .setLargeIcon(bitmap)
                    .setContentText(userName +" likes your post")

                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(pendingIntent)
                    .setStyle(new NotificationCompat.BigTextStyle().bigText("mydata"))
                    .addAction(R.drawable.blog_list_item_notification,"Replay",pendingIntent);
                    NotificationManagerCompat notificationManager=NotificationManagerCompat.from(context);

    notificationManager.notify(10, builder.build());

}

    }

    public static Bitmap decodeUriToBitmap(Context mContext, Uri sendUri) {
        Bitmap getBitmap = null;
        try {
            InputStream image_stream;
            try {
                image_stream = mContext.getContentResolver().openInputStream(sendUri);
                getBitmap = BitmapFactory.decodeStream(image_stream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return getBitmap;
    }
    
   
}
