package com.adnan.photoblog;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
public List<Comment> commentList;
    //private List<BlogPost> blog_list;
private Context context;
private FirebaseFirestore firebaseFirestore;
    private static final String TAG = "CommentAdapter";

public CommentAdapter(List<Comment> commentList){

    this.commentList=commentList;
   // this.blog_list=blog_list;

}


    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.comment_list_item, parent, false);
context=parent.getContext();
firebaseFirestore=FirebaseFirestore.getInstance();

        return new CommentAdapter.CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
holder.setIsRecyclable(false);
//        String blogPostId=blog_list.get(position).BlogPostId;
String commentMessage=commentList.get(position).getMessage();
String user_id=commentList.get(position).getUser_id();
//display message
holder.setCommentMessage(commentMessage);


        //get the data from firebase to display userName snd User Image
        firebaseFirestore.collection("usersPhotoBlog").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            String userName=task.getResult().getString("name");
                            String userNameimage=task.getResult().getString("image");
                            holder.setUserName(userName);
                            holder.SetUserNameCommentImage(userNameimage);
                        }else{
                            String errorMessage=task.getException().getMessage();
                            Log.i(TAG, "onComplete: Error" +errorMessage);
                        }
                    }
                });

//        //get Comments Counts
//        firebaseFirestore.collection("post/"+ blogPostId + "/comments").addSnapshotListener(new EventListener<QuerySnapshot>() {
//            @Override
//            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                if(!value.isEmpty()){
//                    int count=value.size();
//                    holder.updatCommentsCount(count);
//
//                }else{
//
//                    holder.updatCommentsCount(0);
//
//                }
//            }
//        });
    }

    @Override
    public int getItemCount() {
       if(commentList!=null){
           return commentList.size();
       }
       else {
           return 0;
       }
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder {
    private View mView;
    private TextView commentMessageTextView;
        private TextView usernameTextView;
        private TextView commentCountTextView;
        private CircleImageView commentImageView;
        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            mView=itemView;

        }
        public void setCommentMessage(String commentMessage){
            commentMessageTextView=mView.findViewById(R.id.comment_list_item_tv_comment_message);
            commentMessageTextView.setText(commentMessage);
        }
        public void setUserName(String userName){
            usernameTextView=mView.findViewById(R.id.comment_list_item_tv_user_name);
            usernameTextView.setText(userName);
        }
        public void SetUserNameCommentImage(String image){
            commentImageView=mView.findViewById(R.id.comment_list_item_comment_user_image);
            RequestOptions placeholderRequest = new RequestOptions();
            placeholderRequest.placeholder(R.drawable.default_image);
            Glide.with(context).setDefaultRequestOptions(placeholderRequest).load(image).into(commentImageView);
        }
        public void updatCommentsCount(int count){
            commentCountTextView=mView.findViewById(R.id.blog_list_item_tv_blog_like_count);
            commentCountTextView.setText(count +"comments");
        }
    }
}
