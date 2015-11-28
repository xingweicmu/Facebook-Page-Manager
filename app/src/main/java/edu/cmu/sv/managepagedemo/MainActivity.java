package edu.cmu.sv.managepagedemo;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.ProfilePictureView;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.internal.ShareFeedContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;

//import org.apache.http.HttpResponse;
//import org.apache.http.NameValuePair;
//import org.apache.http.client.ClientProtocolException;
//import org.apache.http.client.HttpClient;
//import org.apache.http.client.entity.UrlEncodedFormEntity;
//import org.apache.http.client.methods.HttpPost;
//import org.apache.http.impl.client.DefaultHttpClient;
//import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends FragmentActivity {

    private static final String PERMISSION = "publish_actions";
    private static final Location SEATTLE_LOCATION = new Location("") {
        {
            setLatitude(47.6097);
            setLongitude(-122.3331);
        }
    };

    private final String PENDING_ACTION_BUNDLE_KEY =
            "com.example.hellofacebook:PendingAction";

    private ArrayList<PostDataProvider> posts = new ArrayList<>();
    public static String pageAccessTotken;
    private Button postStatusUpdateButton;
    private Button postPhotoButton;
    private Button pagePostButton;
    private Button allPagePostButton;
    private ProfilePictureView profilePictureView;
    private TextView greeting;
    private PendingAction pendingAction = PendingAction.NONE;
    private boolean canPresentShareDialog;
    private boolean canPresentShareDialogWithPhotos;
    private CallbackManager callbackManager;
    private ProfileTracker profileTracker;
    private ShareDialog shareDialog;
    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d("HelloFacebook", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
            String title = getString(R.string.error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("HelloFacebook", "Success!");
            if (result.getPostId() != null) {
                String title = getString(R.string.success);
                String id = result.getPostId();
                String alertMessage = getString(R.string.successfully_posted_post, id);
                showResult(title, alertMessage);
            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    };

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE,
        PAGE_POST,
        ALL_PAGE_POST
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(this.getApplicationContext());

        callbackManager = CallbackManager.Factory.create();
//        LoginManager.getInstance().per

        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        handlePendingAction();
                        updateUI();
                    }

                    @Override
                    public void onCancel() {
                        if (pendingAction != PendingAction.NONE) {
                            showAlert();
                            pendingAction = PendingAction.NONE;
                        }
                        updateUI();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        if (pendingAction != PendingAction.NONE
                                && exception instanceof FacebookAuthorizationException) {
                            showAlert();
                            pendingAction = PendingAction.NONE;
                        }
                        updateUI();
                    }

                    private void showAlert() {
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle(R.string.cancelled)
                                .setMessage(R.string.permission_not_granted)
                                .setPositiveButton(R.string.ok, null)
                                .show();
                    }
                });

        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        setContentView(R.layout.main);

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                updateUI();
                // It's possible that we were waiting for Profile to be populated in order to
                // post a status update.
                handlePendingAction();
            }
        };

        profilePictureView = (ProfilePictureView) findViewById(R.id.profilePicture);
        greeting = (TextView) findViewById(R.id.greeting);

        Button publishPostButton = (Button) findViewById(R.id.publishPostButton);
        publishPostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                publishPost();
            }
        });

        allPagePostButton = (Button) findViewById(R.id.allPostButton);
        allPagePostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
//                retrieveAllPosts();
                onClickAllPagePost();
            }
        });

        Button postViewButton = (Button) findViewById(R.id.postViewButton);
        postViewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onePostView();
            }
        });

        pagePostButton = (Button) findViewById(R.id.postPageButton);
        pagePostButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickPagePost();
            }
        });

        postStatusUpdateButton = (Button) findViewById(R.id.postStatusUpdateButton);
        postStatusUpdateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickPostStatusUpdate();
            }
        });

        postPhotoButton = (Button) findViewById(R.id.postPhotoButton);
        postPhotoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                onClickPostPhoto();
            }
        });

        // Can we present the share dialog for regular links?
        canPresentShareDialog = ShareDialog.canShow(
                ShareLinkContent.class);

        // Can we present the share dialog for photos?
        canPresentShareDialogWithPhotos = ShareDialog.canShow(
                SharePhotoContent.class);
    }

    protected void showInputDialog() {

        // get input_dialog.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        final Switch publishSwitch = (Switch) promptView.findViewById(R.id.publishSwitchDialog);
        // setup a dialog window
        alertDialogBuilder.setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if(publishSwitch.isChecked()) {
                            createPost(editText.getText().toString(), true);
                        }
                        else{
                            Toast.makeText(getApplicationContext(), "Publish switch is set OFF.", Toast.LENGTH_LONG).show();
                            createPost(editText.getText().toString(), false);
                        }
                    }
                })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        AlertDialog alert = alertDialogBuilder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onResume methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.activateApp(this);

        updateUI();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(posts!=null) posts.clear();

        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
        // reporting.  Do so in the onPause methods of the primary Activities that an app may be
        // launched into.
        AppEventsLogger.deactivateApp(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.stopTracking();
    }

    private void updateUI() {
        boolean enableButtons = AccessToken.getCurrentAccessToken() != null;

        postStatusUpdateButton.setEnabled(enableButtons || canPresentShareDialog);
        postPhotoButton.setEnabled(enableButtons || canPresentShareDialogWithPhotos);

        Profile profile = Profile.getCurrentProfile();
        if (enableButtons && profile != null) {
            profilePictureView.setProfileId(profile.getId());
            greeting.setText(getString(R.string.hello_user, profile.getFirstName()));
        } else {
            profilePictureView.setProfileId(null);
            greeting.setText(null);
        }
    }

    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case NONE:
                break;
            case POST_PHOTO:
                postPhoto();
                break;
            case POST_STATUS_UPDATE:
                postStatusUpdate();
                break;
            case PAGE_POST:
                pagePost();
                break;
            case ALL_PAGE_POST:
                allPagePost();
        }
    }

    public void publishPost() {
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("manage_pages"));
        LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_pages"));
        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me/accounts",
                null,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        /* handle the result */
                        Log.d("facebook##", "response "+ response.toString());
                        // Parse the response to get page access_token
                        JSONObject jsonObject = response.getJSONObject();
                        JSONArray array = null;
                        try {
                            if(jsonObject != null) {
                                array = jsonObject.getJSONArray("data");
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject item = array.getJSONObject(i);
                                    pageAccessTotken = item.getString("access_token");
                                    Log.d("facebook##", "response " + item.getString("access_token"));
                                }
//                                createRegularPost("Test");
                            }
                            else{
                                Toast.makeText(getApplicationContext(), "OAuthException! Please try to login again.", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();

    }

    private void createPost(String postContent, boolean published) {
        new PostTask(this).execute(postContent, published+"");
    }
    class PostTask extends AsyncTask<String, Void, String> {

        private Exception exception;
        private Context context;

        public PostTask(Context context) {
            this.context = context;
        }

        protected String doInBackground(String... urls) {
            String response = "";
            try {
                URL url = new URL("https://graph.facebook.com/901893839866098/feed");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
//                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.setDoOutput(true);

                // FOR POST
                Uri.Builder builder = new Uri.Builder()
                        .appendQueryParameter("message", urls[0])
//                        .appendQueryParameter("published", "false")
                        .appendQueryParameter("access_token", "CAAOOwRcZBOD4BACwLkcMSXQIdESZCfxn9eMIaalLtDCidomUifxm8YP0ZAZCQUwz5FixBeHV5uWZCLRzUZBa3u4eTtWb0owEcHt9LuZASwk1ppc8HP6dU2YAhCus2dQklG4d8vM0VctRv3lSOzDCZBxT5IeNPIxaYltZAlTZAcqbplvlRTf0m7oRSltW0pJb0umtcZD");
                if(urls[1].equals("false")){
                    builder.appendQueryParameter("published", "false");
                    Log.d("facebook##", urls[1]+" published->false");
                }
                String query = builder.build().getEncodedQuery();

                OutputStream os = conn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(
                        new OutputStreamWriter(os, "UTF-8"));
                writer.write(query);
                writer.flush();
                writer.close();
                os.close();
                int responseCode=conn.getResponseCode();
                Log.d("facebook##", responseCode + "");
                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    String line;
                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        response+=line;
                    }
                    Log.d("facebook##", response);
                }
                else {
                    response=null;
                }

            }catch(Exception e) {
                e.printStackTrace();
            }
            return response;
        }



        protected void onPostExecute(String response) {
            // TODO: check this.exception
            // TODO: do something with the feed
            if(response != null) {
                Toast.makeText(getApplicationContext(), "Post success!", Toast.LENGTH_LONG).show();
                startActivity(new Intent(this.context, MainActivity.class));
            }
        }
    }

    private void retrieveAllPosts() {
        new GetTask().execute("https://graph.facebook.com/901893839866098/promotable_posts?fields=is_published,created_time,id,message&access_token=CAAOOwRcZBOD4BACwLkcMSXQIdESZCfxn9eMIaalLtDCidomUifxm8YP0ZAZCQUwz5FixBeHV5uWZCLRzUZBa3u4eTtWb0owEcHt9LuZASwk1ppc8HP6dU2YAhCus2dQklG4d8vM0VctRv3lSOzDCZBxT5IeNPIxaYltZAlTZAcqbplvlRTf0m7oRSltW0pJb0umtcZD");
    }

    class GetTask extends AsyncTask<String, Void, String> {

        private Exception exception;

        protected String doInBackground(String... urls) {
            String result = "";
            try {
                String url = urls[0];

                URL obj = new URL(url);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();

                // optional default is GET
                con.setRequestMethod("GET");

                int responseCode = con.getResponseCode();
                Log.d("facebook##", "Response Code : " + responseCode);

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer response = new StringBuffer();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                //print result
                result = response.toString();
                Log.d("facebook##", response.toString());
            }catch(Exception e) {
                e.printStackTrace();
            }
            return result;
        }



        protected void onPostExecute(String response) {
            // Parse the response
            try {
                JSONObject jsonObject = new JSONObject(response);
//                JSONArray array = null;

                if(jsonObject != null) {
                    JSONArray array = jsonObject.getJSONArray("data");
                    for (int i = 0; i < array.length(); i++) {
                        JSONObject item = array.getJSONObject(i);
                        String message = item.getString("message");
                        String createTime = "";
                        try {
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ssZ");
                            Date result = df.parse(item.getString("created_time"));
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            Calendar calendar = new GregorianCalendar();
                            TimeZone timeZone = calendar.getTimeZone();
                            sdf.setTimeZone(timeZone);
                            createTime = sdf.format(result);
                        }catch (Exception e){}
                        String id = item.getString("id");
                        String isPublished = item.getString("is_published");
                        Log.d("facebook##", "message: " + message);
                        posts.add(new PostDataProvider(message, createTime, id, isPublished, 0));
                    }
                }
//                Log.d("facebook##", "main:"+posts.size());
                Intent intent = new Intent(MainActivity.this, PostListActivity.class);
//                intent.removeExtra("posts");
                intent.putParcelableArrayListExtra("posts", posts);
                startActivity(intent);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public void onePostView() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("read_insights"));
        new GetTask().execute("https://graph.facebook.com/901893839866098_906594766062672/insights/post_impressions_unique/lifetime?access_token=CAAOOwRcZBOD4BACwLkcMSXQIdESZCfxn9eMIaalLtDCidomUifxm8YP0ZAZCQUwz5FixBeHV5uWZCLRzUZBa3u4eTtWb0owEcHt9LuZASwk1ppc8HP6dU2YAhCus2dQklG4d8vM0VctRv3lSOzDCZBxT5IeNPIxaYltZAlTZAcqbplvlRTf0m7oRSltW0pJb0umtcZD");
    }

    private void onClickAllPagePost() {
        performPublish(PendingAction.ALL_PAGE_POST, true);
    }

    private void allPagePost() {
        Profile profile = Profile.getCurrentProfile();
        Log.d("facebook##", "profile  " + profile);
        if (profile != null && hasAllPagePermission()){
            retrieveAllPosts();
        }
        else {
            pendingAction = PendingAction.PAGE_POST;
            // We need to get new permissions, then complete the action when we get called back.
            if(!AccessToken.getCurrentAccessToken().getPermissions().contains("manage_pages"))
                LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("manage_pages"));
            if(!AccessToken.getCurrentAccessToken().getPermissions().contains("publish_pages"))
                LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_pages"));
            if(!AccessToken.getCurrentAccessToken().getPermissions().contains("read_insights"))
                LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("read_insights"));
        }
    }

    private void onClickPagePost(){
//        showInputDialog();
        performPublish(PendingAction.PAGE_POST, true);
    }

    private void pagePost() {
        Profile profile = Profile.getCurrentProfile();
        Log.d("facebook##", "profile  " + profile);
        if (profile != null && hasPagePermission()){
            showInputDialog();
        }
        else {
            pendingAction = PendingAction.PAGE_POST;
            // We need to get new permissions, then complete the action when we get called back.
            if(!AccessToken.getCurrentAccessToken().getPermissions().contains("manage_pages"))
                LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("manage_pages"));
            if(!AccessToken.getCurrentAccessToken().getPermissions().contains("publish_pages"))
                LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList("publish_pages"));
        }
    }

    private void onClickPostStatusUpdate() {
        performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
    }

    private void postStatusUpdate() {
        Profile profile = Profile.getCurrentProfile();
        ShareLinkContent linkContent = new ShareLinkContent.Builder()
                .setContentTitle("Hello Facebook")
                .setContentDescription(
                        "The 'Hello Facebook' sample  showcases simple Facebook integration")
                .setContentUrl(Uri.parse("http://developers.facebook.com/docs/android"))
                .build();
        if (canPresentShareDialog) {
            shareDialog.show(linkContent);
        } else if (profile != null && hasPublishPermission()) {
            ShareApi.share(linkContent, shareCallback);
        } else {
            pendingAction = PendingAction.POST_STATUS_UPDATE;
        }
    }

    private void onClickPostPhoto() {
        performPublish(PendingAction.POST_PHOTO, canPresentShareDialogWithPhotos);
    }

    private void postPhoto() {
        Bitmap image = BitmapFactory.decodeResource(this.getResources(), R.drawable.com_facebook_button_icon);
        SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(image).build();
        ArrayList<SharePhoto> photos = new ArrayList<>();
        photos.add(sharePhoto);

        SharePhotoContent sharePhotoContent =
                new SharePhotoContent.Builder().setPhotos(photos).build();
        if (canPresentShareDialogWithPhotos) {
            shareDialog.show(sharePhotoContent);
        } else if (hasPublishPermission()) {
            ShareApi.share(sharePhotoContent, shareCallback);
        } else {
            pendingAction = PendingAction.POST_PHOTO;
            // We need to get new permissions, then complete the action when we get called back.
            LoginManager.getInstance().logInWithPublishPermissions(
                    this,
                    Arrays.asList(PERMISSION));
        }
    }

    private boolean hasPublishPermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Log.d("facebook##", "publish_actions " + accessToken.getPermissions().contains("publish_actions"));
        return accessToken != null && accessToken.getPermissions().contains("publish_actions");
    }

    private boolean hasPagePermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Log.d("facebook##", "publish_pages "+accessToken.getPermissions().contains("publish_pages"));
        Log.d("facebook##", "manage  "+accessToken.getPermissions().contains("manage_pages"));
        Log.d("facebook##", "publish permission "+hasPublishPermission());
        return accessToken != null && accessToken.getPermissions().contains("publish_pages")
                && accessToken.getPermissions().contains("manage_pages");
    }

    private boolean hasAllPagePermission() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();

        return accessToken != null && accessToken.getPermissions().contains("publish_pages")
                && accessToken.getPermissions().contains("manage_pages")
                && accessToken.getPermissions().contains("read_insights");
    }

    private void performPublish(PendingAction action, boolean allowNoToken) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null || allowNoToken) {
            pendingAction = action;
            handlePendingAction();
        }
    }
}


