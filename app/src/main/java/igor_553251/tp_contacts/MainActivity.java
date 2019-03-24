package igor_553251.tp_contacts;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.core.models.User;
import com.twitter.sdk.android.core.services.AccountService;

import java.util.Arrays;

import retrofit2.Call;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    private static final int RC_SIGN_IN = 9001 ;
    String TAG="SignInActivity";
    Button btngoogle;
    EditText editText9;
    EditText phoneNum;
    EditText emailText;
    EditText companyText;
    TextView textView;
    Pessoa p;
    CallbackManager callbackManager;
    LoginButton loginButton;
    TwitterLoginButton loginButtont;
    GoogleSignInClient mGoogleSignInClient;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //////////////////////////////////// TWITTER LOGIN //////////////////////////
        Twitter.initialize(this);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig(getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET), getString(R.string.com_twitter_sdk_android_CONSUMER_SECRET)))
                .debug(true)
                .build();
        Twitter.initialize(config);
        setContentView(R.layout.activity_main);
        loginButtont = (TwitterLoginButton) findViewById(R.id.twitterbutton);
        loginButtont.setCallback(new Callback<TwitterSession>() {




            @Override
            public void success(Result<TwitterSession> result) {
                // Do something with result, which provides a TwitterSession for making API calls
                Log.e("result", "result " + result);
                final TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
                AccountService accountService = twitterApiClient.getAccountService();
                Call<User> call = accountService.verifyCredentials(true, true, true);
                call.enqueue(new Callback<com.twitter.sdk.android.core.models.User>() {
                    @Override
                    public void success(Result<com.twitter.sdk.android.core.models.User> result) {
                        loginButtont.setText("Logged In");
                        loginButtont.setEnabled(false);
                        Toast.makeText(getApplicationContext(), "Logged in succesfully as " + result.data.name , Toast.LENGTH_LONG).show();
                    }
                    @Override
                    public void failure(TwitterException exception) {
                        exception.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Something went wrong " , Toast.LENGTH_LONG).show();

                    }
                });
            }
            @Override
            public void failure(TwitterException exception) {
                // Do something on failure
                exception.printStackTrace();
            }
        });

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();


        /////////////////// GOOGLE //////////////

        // Configure sign-in to request the user's ID, email address, and basic
// profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);

    }


    @SuppressLint("WrongConstant")
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.sign_out_button:
                mGoogleSignInClient.signOut();
                View sign_out_button = findViewById(R.id.sign_out_button);
                sign_out_button.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Logged out!", Toast.LENGTH_LONG).show();
                SignInButton signInButton = findViewById(R.id.sign_in_button);
                signInButton.setVisibility(View.VISIBLE);
                break;
        }
    }



    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        // ...
                    }
                });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {


        if (requestCode == TwitterAuthConfig.DEFAULT_AUTH_REQUEST_CODE) {
            loginButtont.onActivityResult(requestCode, resultCode, data);

        }else if(requestCode == CallbackManagerImpl.RequestCodeOffset.Login.toRequestCode()){
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }else if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
            updateUI(null);
        }
    }

    @SuppressLint("WrongConstant")
    private void updateUI(GoogleSignInAccount account) {
            if(account!=null){
                SignInButton signInButton = findViewById(R.id.sign_in_button);
                signInButton.setVisibility(View.GONE);
                Toast.makeText(getApplicationContext(), "Logged in as " + account.getDisplayName(), Toast.LENGTH_LONG).show();
                Button signOutbutton = findViewById(R.id.sign_out_button);
                signOutbutton.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(getApplicationContext(), "Something went wrong!", Toast.LENGTH_LONG).show();
            }
    }

    public void FBLogin(View v){

        callbackManager = CallbackManager.Factory.create();
        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");



        loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions("email");
        // If using in a fragment
        //loginButton.setFragment(this);

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Toast.makeText(getApplicationContext(), "Logged in succesfully ", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), " Canceled! ", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(FacebookException exception) {
                Toast.makeText(getApplicationContext(), " Something went wrong! ", Toast.LENGTH_LONG).show();
            }
        });


        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Toast.makeText(getApplicationContext(), "Logged in succesfully ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onCancel() {
                        Toast.makeText(getApplicationContext(), " Canceled! ", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(FacebookException exception) {
                        Toast.makeText(getApplicationContext(), " Something went wrong! ", Toast.LENGTH_LONG).show();
                    }
                });




        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile"));

    }





    public void saveContacts(View v){
        Intent intent = new Intent(ContactsContract.Intents.Insert.ACTION);
        intent.setType(ContactsContract.RawContacts.CONTENT_TYPE);

        editText9 = (EditText) findViewById(R.id.editText9);
        String nome = editText9.getText().toString();

        phoneNum = (EditText) findViewById(R.id.phoneNum);
        String phone = phoneNum.getText().toString();

        emailText = (EditText) findViewById(R.id.emailText);
        String email = emailText.getText().toString();

        companyText = (EditText) findViewById(R.id.companyText);
        String company = companyText.getText().toString();

        p  = new Pessoa(nome,email,company,phone);



            intent

                    .putExtra(ContactsContract.Intents.Insert.EMAIL, p.email)
                    .putExtra(ContactsContract.Intents.Insert.EMAIL_TYPE, ContactsContract.CommonDataKinds.Email.TYPE_HOME)
                    .putExtra(ContactsContract.Intents.Insert.COMPANY, p.company)
                    .putExtra(ContactsContract.Intents.Insert.PHONE, p.phone_number)
                    .putExtra(ContactsContract.Intents.Insert.PHONE_TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_HOME)
                    .putExtra(ContactsContract.Intents.Insert.NAME, p.name)
            ;


            startActivity(intent);




    }

    public void botaoEmail(View v){
        if(p == null ) Toast.makeText(getApplicationContext(), "O email deve ser preenchido!", Toast.LENGTH_LONG).show();
        else Email_Message(p);
    }


    public void botaoWpp(View v){
        if(p == null ) Toast.makeText(getApplicationContext(), "O número de telefone deve ser preenchido!", Toast.LENGTH_LONG).show();
        else WPP_Message(p);
    }

    public void WPP_Message(Pessoa p){


        try {
            String text = "Contato foi cadastrado com sucesso!";

            String toNumber = "5531"+p.phone_number;


            Intent wppintent = new Intent(Intent.ACTION_VIEW);
            wppintent.setData(Uri.parse("http://api.whatsapp.com/send?phone="+toNumber +"&text="+text));

            startActivity(wppintent);
        }
        catch (Exception e){
            e.printStackTrace();
        }

    }

    public void Email_Message(Pessoa p){
        String subject = " Cadastro ";
        String body = "Seu cadastro foi completado com sucesso!";
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" + p.email));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        startActivity(Intent.createChooser(emailIntent, "Chooser Title"));
    }


}
