package com.adnan.photoblog;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.remote.WatchChange;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private static final int RESULT_OK = 1;
    private int mRequestCode = 100;
    //Variable declaration
    private RecyclerView blog_post_recyclerView;
    private List<BlogPost> blog_list;
    private List<User> user_list;
    private FirebaseFirestore firebaseFirestore;
    private BlogPostAdapter blogPostAdapter;
    private FirebaseAuth firebaseAuth;
    private static final String TAG = "HomeFragment";
    private DocumentSnapshot lastVisible;
    private Boolean isFirstPageFirstLoad = true;
    private TextView textViewCommentsCount1;
    String blogPostId1;
    String currentUserId;
    // private int lastVisible;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        blog_post_recyclerView = view.findViewById(R.id.home_fragment_rv);
        blog_list = new ArrayList<>();
        user_list = new ArrayList<>();
        //      String user_id=FirebaseAuth.getInstance().getCurrentUser().getUid();
        blogPostAdapter = new BlogPostAdapter(blog_list, user_list);
        blog_post_recyclerView.setHasFixedSize(true);
        blog_post_recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        blog_post_recyclerView.setAdapter(blogPostAdapter);
        textViewCommentsCount1=container.findViewById(R.id.blog_list_item_tv_comment_count);
        firebaseAuth=FirebaseAuth.getInstance();
       currentUserId=firebaseAuth.getCurrentUser().getUid();
        //Set Description Text
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                String blogPostId = blog_list.get(viewHolder.getAdapterPosition()).BlogPostId;

                String blog_user_id = blog_list.get(viewHolder.getAdapterPosition()).getUser_id();
                if (!blog_user_id.equals(currentUserId)) {
                    blogPostAdapter.notifyDataSetChanged();

                    return;
                }

//                  deleteSubCollection(blogPostId, "comments");
//                deleteSubCollection(blogPostId, "Likes");
//                firebaseFirestore.collection("posts" ).document(blogPostId).collection("comments")
//                        .whereEqualTo("blogPostId", blogPostId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        WriteBatch batch=FirebaseFirestore.getInstance().batch();
//                        List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
//
//
//                        for(DocumentSnapshot snapshot:documentSnapshots){
//
//                            batch.delete(snapshot.getReference());
//                        }
//                        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Toast.makeText(getContext(), "All Comment Deleted", Toast.LENGTH_SHORT).show();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                String errorMessgae=e.getMessage();
//                                Log.i(TAG, "onFailure: "+errorMessgae);
//                            }
//                        });;
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        String errorMessage=e.getMessage();
//                        Log.i(TAG, "onFailure: "+errorMessage);
//                    }
//                });
//                firebaseFirestore.collection("posts" ).document(blogPostId).collection("Likes")
//                        .whereEqualTo("blogPostId", blogPostId).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
//                    @Override
//                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
//                        WriteBatch batch=FirebaseFirestore.getInstance().batch();
//                        List<DocumentSnapshot> documentSnapshots=queryDocumentSnapshots.getDocuments();
//
//
//                        for(DocumentSnapshot snapshot:documentSnapshots){
//
//                            batch.delete(snapshot.getReference());
//                        }
//                        batch.commit().addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Toast.makeText(getContext(), "All Comment Deleted", Toast.LENGTH_SHORT).show();
//                            }
//                        }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                String errorMessgae=e.getMessage();
//                                Log.i(TAG, "onFailure: "+errorMessgae);
//                            }
//                        });;
//                    }
//                }).addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        String errorMessage=e.getMessage();
//                        Log.i(TAG, "onFailure: "+errorMessage);
//                    }
//                });


//
//                firebaseFirestore.collection("posts")
//                        .document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        if (blog_list != null && user_list != null) {
//                         //  blogPostId = blog_list.get(viewHolder.getAdapterPosition()).BlogPostId;
//                            String currentUserId = firebaseAuth.getCurrentUser().getUid();
//                            String blog_user_id = blog_list.get(viewHolder.getAdapterPosition()).getUser_id();
//                            Log.i(TAG, "onSuccess:deleteswip" + currentUserId + " " + blog_user_id);
//                           if (blog_user_id.equals(currentUserId)) {
//                                blog_list.remove(viewHolder.getAdapterPosition());
//                                user_list.remove(viewHolder.getAdapterPosition());
//                                blogPostAdapter.setBlog_list(blog_list);
//                                blogPostAdapter.setUser_list(user_list);
//                                blogPostAdapter.notifyDataSetChanged();
//                            }
//                        }
//                    }
//                });
              if (blog_user_id.equals(currentUserId)) {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    //Do your Yes progress
                                    deleteSubCollection(blogPostId, "comments");
                                    deleteSubCollection(blogPostId, "Likes");
                                    firebaseFirestore.collection("posts")
                                            .document(blogPostId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            if (blog_list != null && user_list != null) {
                                                //  blogPostId = blog_list.get(viewHolder.getAdapterPosition()).BlogPostId;
                                                String currentUserId = firebaseAuth.getCurrentUser().getUid();
                                                String blog_user_id = blog_list.get(viewHolder.getAdapterPosition()).getUser_id();
                                                Log.i(TAG, "onSuccess:deleteswip" + currentUserId + " " + blog_user_id);
                                                if (blog_user_id.equals(currentUserId)) {
                                                    blog_list.remove(viewHolder.getAdapterPosition());
                                                    user_list.remove(viewHolder.getAdapterPosition());
                                                    blogPostAdapter.setBlog_list(blog_list);
                                                    blogPostAdapter.setUser_list(user_list);
                                                    blogPostAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                                    break;

                                case DialogInterface.BUTTON_NEGATIVE:
                                    Toast.makeText(getContext(), "cancled", Toast.LENGTH_SHORT).show();
                                    blogPostAdapter.notifyDataSetChanged();
                                    break;
                            }
                        }
                    };
                    AlertDialog.Builder ab = new AlertDialog.Builder(getContext());
                    ab.setMessage("Are you sure to delete?").setPositiveButton("Yes", dialogClickListener)
                            .setNegativeButton("No", dialogClickListener).show();

                }else{
                  blogPostAdapter.notifyDataSetChanged();
              }
              }

        }).attachToRecyclerView(blog_post_recyclerView);


        firebaseAuth = FirebaseAuth.getInstance();
        if (firebaseAuth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();
            blog_post_recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    boolean reachedBottom = !recyclerView.canScrollVertically(1);
                    if (reachedBottom) {

                        Toast.makeText(container.getContext(), "reached To Bottom", Toast.LENGTH_SHORT).show();
                        loadMorePost();
                    }

                }
            });

//query tho display the first 3 posts
            Query firstQuey = firebaseFirestore.collection("posts")
                    .orderBy("timestamp", Query.Direction.DESCENDING).limit(3);
            firstQuey.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                 if(value !=null){


                     if (isFirstPageFirstLoad) {
                         if (value.size() > 0) {
                             lastVisible = value.getDocuments().get(value.size() - 1);
                             blog_list.clear();
                             user_list.clear();
                         }
                     }
                     for (DocumentChange doc : value.getDocumentChanges()) {
                         if (doc.getType() == DocumentChange.Type.ADDED) {
                             String blogPostId = doc.getDocument().getId();
                             BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                             String blogUserId = doc.getDocument().getString("user_id");
                             Log.i(TAG, "onEvent: " + doc.getDocument().getData().get("2lD5QEWCSOxWEarjpUlp"));
                             firebaseFirestore.collection("usersPhotoBlog").document(blogUserId).get()
                                     .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                         @Override
                                         public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                             if (task.isSuccessful()) {
                                                 User user = task.getResult().toObject(User.class);

                                                 if (isFirstPageFirstLoad) {
                                                     user_list.add(user);
                                                     blog_list.add(blogPost);
                                                 } else {
                                                     user_list.add(0, user);
                                                     blog_list.add(0, blogPost);
                                                     blogPostAdapter.notifyDataSetChanged();
                                                 }

                                             } else {

                                                 String errorMessage = task.getException().getMessage();
                                                 Log.i(TAG, "onComplete: FireBase Error" + errorMessage);
                                             }
                                         }
                                     });
                         }
                     }
                     isFirstPageFirstLoad = false;




                 }

                }
            });

        }

        return view;

    }

    //this method to display the next 3 posts
    public void loadMorePost() {
        Query nextQuey = firebaseFirestore.collection("posts").orderBy("timestamp", Query.Direction.DESCENDING)
                .startAfter(lastVisible).limit(3);
        nextQuey.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (!value.isEmpty()) {
                    lastVisible = value.getDocuments().get(value.size() - 1);
                    for (DocumentChange doc : value.getDocumentChanges()) {

                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blogPostId = doc.getDocument().getId();
                            BlogPost blogPost = doc.getDocument().toObject(BlogPost.class).withId(blogPostId);
                            String blogUserId = doc.getDocument().getString("user_id");
                            firebaseFirestore.collection("usersPhotoBlog").document(blogUserId).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                User user = task.getResult().toObject(User.class);
                                                user_list.add(user);
                                                blog_list.add(blogPost);
                                                blogPostAdapter.notifyDataSetChanged();

                                            } else {

                                                String errorMessage = task.getException().getMessage();
                                                Log.i(TAG, "onComplete: FireBase Error" + errorMessage);
                                            }
                                        }
                                    });

                        }
                    }
                }
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
                    Toast.makeText(getContext(), "All recorded Deleted", Toast.LENGTH_SHORT).show();
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



    @Override
    public void onResume() {
        super.onResume();
//        SharedPreferences sp =getActivity().getSharedPreferences("LoginInfos", 0);
//       blogPostId1 = sp.getString("blogPostId", "NOTHING");
//        Log.i(TAG, "onResume: " +blogPostId1);
      //  updateCommentCount( blogPostId1);
        blogPostAdapter.notifyDataSetChanged();
    }



}