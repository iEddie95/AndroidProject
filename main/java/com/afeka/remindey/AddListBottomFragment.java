package com.afeka.remindey;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;

import com.afeka.remindey.logic.Category;
import com.afeka.remindey.model.CategoryViewModel;
import com.afeka.remindey.util.RemindeyApi;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

/**
 * AddListBottomFragment opens a fragment from the bottom of the Main Activity.
 * To add a new category click on the menu icon (three lines- hamburger icon) and click on "Add list".
 * To edit/delete a reminder just click on a reminder, a fragment will open and you can edit or click the "Delete" button for deleting.
 * The categories are saved in the Firestore DB.
 */
public class AddListBottomFragment extends BottomSheetDialogFragment {

    //field for the category name
    private EditText enterList;
    // button for saving the category
    private ImageButton saveButton;

    //Firebase components
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference categoryCollectionReference = db.collection(MainActivity.CATEGORY_TABLE);

    private RemindeyApi remindeyApi = RemindeyApi.getInstance();


    public AddListBottomFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_list_bottom, container, false);

        enterList = view.findViewById(R.id.enter_list_name);
        saveButton = view.findViewById(R.id.add_list_button);

        firebaseAuth = FirebaseAuth.getInstance();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {

                } else {

                }
            }
        };

        return view;
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        /** saveButton - when clicked is saving the new category in the the DB */
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String listName = enterList.getText().toString().trim();
                if (!TextUtils.isEmpty(listName)) {
                    Category category = new Category();
                    category.setName(listName);
                    category.setUserId(remindeyApi.getUserId());
                    CategoryViewModel.insert(category);
                }
                enterList.setText("");
                if (AddListBottomFragment.this.isVisible()) {
                    AddListBottomFragment.this.dismiss();
                }
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();

        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}