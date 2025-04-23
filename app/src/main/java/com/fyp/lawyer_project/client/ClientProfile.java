package com.fyp.lawyer_project.client;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.fyp.lawyer_project.R;
import com.fyp.lawyer_project.main.MainFragmentActivity;
import com.fyp.lawyer_project.main.RootFragment;
import com.fyp.lawyer_project.modal_classes.Client;
import com.fyp.lawyer_project.modal_classes.User;
import com.fyp.lawyer_project.utils.FirebaseHelper;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONObject;

import java.io.IOException;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ClientProfile extends RootFragment {
    private View rootView;
    private Client user;
    private Uri selectedImageUri;
    private CircleImageView profileImageView;
    private ProgressDialog progressDialog;
    private boolean isUploadingImage = false;

    private final ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    Glide.with(this)
                            .load(selectedImageUri)
                            .placeholder(R.drawable.lawyer_icon3)
                            .error(R.drawable.lawyer_icon3)
                            .into(profileImageView);
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.client_profile_layout, container, false);
        initClientProfile();
        initActions();
        return rootView;
    }

    private void initActions() {
        com.google.android.material.appbar.MaterialToolbar toolbar = rootView.findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> getFragmentManager().popBackStack());
        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveProfileUpdates();
                return true;
            }
            return false;
        });

        profileImageView = rootView.findViewById(R.id.profile_image);
        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });
    }

    private void saveProfileUpdates() {
        progressDialog = new ProgressDialog(rootView.getContext());
        progressDialog.setMessage("Saving profile...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        clearInputErrors();

        TextInputEditText nameField = rootView.findViewById(R.id.profileNameField);
        TextInputEditText phoneField = rootView.findViewById(R.id.profilePhoneNumberField);
        TextInputEditText passwordField = rootView.findViewById(R.id.profilePasswordField);
        TextInputEditText confirmPasswordField = rootView.findViewById(R.id.profileConfirmPasswordField);

        String fullName = nameField.getText().toString().trim();
        String phoneNumber = phoneField.getText().toString().trim();
        String password = passwordField.getText().toString().trim();
        String confirmPassword = confirmPasswordField.getText().toString().trim();

        boolean hasError = false;
        if (fullName.isEmpty()) {
            setInputError(R.id.name_input_layout, "Full name is required");
            hasError = true;
        }
        if (phoneNumber.isEmpty()) {
            setInputError(R.id.phone_input_layout, "Phone number is required");
            hasError = true;
        }

        if (hasError) {
            progressDialog.dismiss();
            Toast.makeText(rootView.getContext(), "Please fill in all required fields", Toast.LENGTH_LONG).show();
            return;
        }

        user.setFullName(fullName);
        user.setPhoneNumber(phoneNumber);

        if (!password.isEmpty()) {
            if (password.equals(confirmPassword)) {
                user.setPassword(password);
            } else {
                setInputError(R.id.confirm_password_input_layout, "Passwords do not match");
                progressDialog.dismiss();
                Toast.makeText(rootView.getContext(), "Passwords do not match", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (selectedImageUri != null && !isUploadingImage) {
            isUploadingImage = true;
            uploadImageToImgur(selectedImageUri, new ImageUploadCallback() {
                @Override
                public void onSuccess(String imageUrl) {
                    user.setProfileImageUrl(imageUrl);
                    updateUserInFirebase();
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure(String error) {
                    isUploadingImage = false;
                    progressDialog.dismiss();
                    Toast.makeText(rootView.getContext(), "Image upload failed: " + error, Toast.LENGTH_LONG).show();
                }
            });
        } else {
            updateUserInFirebase();
        }
    }

    private void updateUserInFirebase() {
        FirebaseHelper.updateUser(User.TYPE_CLIENT, user, new FirebaseHelper.FirebaseActions() {
            @Override
            public void onUserUpdated(String status) {
                isUploadingImage = false;
                progressDialog.dismiss();
                Toast.makeText(rootView.getContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                if (user.getProfileImageUrl() != null && !user.getProfileImageUrl().isEmpty()) {
                    Glide.with(ClientProfile.this)
                            .load(user.getProfileImageUrl())
                            .placeholder(R.drawable.lawyer_icon3)
                            .error(R.drawable.lawyer_icon3)
                            .into(profileImageView);
                    updateProfileImageAcrossApp(user.getProfileImageUrl());
                }
            }

            @Override
            public void onError(String error) {
                isUploadingImage = false;
                progressDialog.dismiss();
                Toast.makeText(rootView.getContext(), "Failed to update profile: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateProfileImageAcrossApp(String imageUrl) {
        // Update drawer header
        NavigationView navView = getActivity().findViewById(R.id.navView);
        if (navView != null) {
            View header = navView.getHeaderView(0);
            CircleImageView userImage = header.findViewById(R.id.drawer_user_avatar);
            Glide.with(ClientProfile.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.lawyer)
                    .error(R.drawable.lawyer)
                    .into(userImage);
        }

        // Update top bar
        CircleImageView topBarImage = getActivity().findViewById(R.id.userProfileImage);
        if (topBarImage != null) {
            Glide.with(ClientProfile.this)
                    .load(imageUrl)
                    .placeholder(R.drawable.lawyer)
                    .error(R.drawable.lawyer)
                    .into(topBarImage);
        }
    }

    private void initClientProfile() {
        user = (Client) User.getCurrentLoggedInUser();
        if (user != null) {
            ((TextInputEditText) rootView.findViewById(R.id.profileNameField)).setText(user.getFullName());
            ((TextInputEditText) rootView.findViewById(R.id.profileEmailField)).setText(user.getEmailAddress());
            ((TextInputEditText) rootView.findViewById(R.id.profilePhoneNumberField)).setText(user.getPhoneNumber());

            profileImageView = rootView.findViewById(R.id.profile_image);
            String profileImageUrl = user.getProfileImageUrl();
            if (profileImageUrl != null && !profileImageUrl.isEmpty()) {
                Glide.with(this)
                        .load(profileImageUrl)
                        .placeholder(R.drawable.lawyer_icon3)
                        .error(R.drawable.lawyer_icon3)
                        .into(profileImageView);
            }
        }
    }

    private void uploadImageToImgur(Uri imageUri, ImageUploadCallback callback) {
        try {
            byte[] imageBytes = getContext().getContentResolver().openInputStream(imageUri).readAllBytes();
            if (imageBytes == null) {
                callback.onFailure("Failed to read image");
                return;
            }

            OkHttpClient client = new OkHttpClient();
            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", "profile.jpg",
                            RequestBody.create(imageBytes, MediaType.parse("image/jpeg")))
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.imgur.com/3/image")
                    .addHeader("Authorization", "Client-ID 546c25a59c58ad7")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    getActivity().runOnUiThread(() -> callback.onFailure("Network error: " + e.getMessage()));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (response.isSuccessful()) {
                        String responseBody = response.body().string();
                        try {
                            JSONObject json = new JSONObject(responseBody);
                            String imageUrl = json.getJSONObject("data").getString("link");
                            getActivity().runOnUiThread(() -> callback.onSuccess(imageUrl));
                        } catch (Exception e) {
                            getActivity().runOnUiThread(() -> callback.onFailure("Failed to parse response: " + e.getMessage()));
                        }
                    } else {
                        getActivity().runOnUiThread(() -> callback.onFailure("Upload failed: " + response.message()));
                    }
                }
            });
        } catch (Exception e) {
            getActivity().runOnUiThread(() -> callback.onFailure("Error: " + e.getMessage()));
        }
    }

    private void setInputError(int layoutId, String error) {
        TextInputLayout layout = rootView.findViewById(layoutId);
        layout.setError(error);
    }

    private void clearInputErrors() {
        int[] layoutIds = {
                R.id.name_input_layout,
                R.id.email_input_layout,
                R.id.phone_input_layout,
                R.id.password_input_layout,
                R.id.confirm_password_input_layout
        };
        for (int id : layoutIds) {
            TextInputLayout layout = rootView.findViewById(id);
            layout.setError(null);
        }
    }

    @Override
    public void setCallBackAction(MainFragmentActivity callbacks) {
    }

    private interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String error);
    }
}