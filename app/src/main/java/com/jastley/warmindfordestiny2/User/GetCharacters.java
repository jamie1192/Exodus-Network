package com.jastley.warmindfordestiny2.User;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jastley.warmindfordestiny2.api.BungieAPI;
import com.jastley.warmindfordestiny2.database.DatabaseHelper;

import java.io.IOException;
import java.util.Iterator;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.jastley.warmindfordestiny2.api.apiKey.apiKey;

/**
 * Created by jamie on 3/4/18.
 */

public class GetCharacters {

    SharedPreferences savedPrefs;
    private DatabaseHelper db;

    public interface GetCharacterResponseInterface {
        void onComplete();
    }

    public GetCharacterResponseInterface delegate = null;

    public GetCharacters(GetCharacterResponseInterface delegate) {
        this.delegate = delegate;
    }

    public void GetCharacterSummaries(final Context context) {

        String baseURL = "https://www.bungie.net";
//        final Context context = contexts[0];

        //Get membershipId and selectedPlatform from SharedPrefs and use for request
        db = new DatabaseHelper(context);
        String membershipType = "2"; //TODO: remove this hard-code later
        String membershipId = "4611686018428911554"; //TODO: and this one
//        SharedPreferences savedPrefs = context.getSharedPreferences("saved_prefs", context.MODE_PRIVATE);

        //Initialise database
//        try{
//            bungieAccount = context.openOrCreateDatabase("bungieAccount.db", Context.MODE_PRIVATE, null);
//            bungieAccount.execSQL("CREATE TABLE IF NOT EXISTS account"
//                    + "('key' VARCHAR PRIMARY KEY, value VARCHAR);");
//
//            if(BuildConfig.DEBUG){
//                File db = context.getDatabasePath("bungieAccount.db");
//                if(db.exists()){
//                    System.out.println("Database created/exists");
//                }
//                else {
//                    System.out.println("Database doesn't exist");
//                }
//            }
//        }
//        catch(Exception e){
//            System.out.println("Error: " + e);
//        }

//                savedPrefs.getString("membershipId"+membershipType, "");

//        String[] characterIds;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {

                Request original = chain.request();

                Request.Builder requestBuilder = original.newBuilder()
                        .header("X-API-Key", apiKey);

                Request request = requestBuilder.build();

                return chain.proceed(request);
            }
        });

        Retrofit.Builder builder = new Retrofit.Builder()
                .baseUrl(baseURL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build());

        Retrofit retrofit = builder.build();

        BungieAPI bungieClient = retrofit.create(BungieAPI.class);
        Call<JsonElement> getProfileCall = bungieClient.getProfile(
                membershipType,
                membershipId
        );

        getProfileCall.enqueue(new Callback<JsonElement>() {

            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                JsonElement json = response.body();
                JsonObject responseObj = (JsonObject) json;

                Integer count = 0;

                savedPrefs = context.getSharedPreferences("saved_prefs", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = savedPrefs.edit();

                for (Iterator iterator = responseObj.getAsJsonObject("Response").getAsJsonObject("characters").getAsJsonObject("data").keySet().iterator(); iterator.hasNext(); ) {
                    String key = (String) iterator.next();
                    JsonObject characterIdObj = (JsonObject) responseObj.getAsJsonObject("Response").getAsJsonObject("characters").getAsJsonObject("data").get(key);

                    //Each character object toString();
                    String characterDB = responseObj.getAsJsonObject("Response").getAsJsonObject("characters").getAsJsonObject("data").get(key).toString();

                    //Append character count to key name and store in sqlite
                    String currentCharacter = "character" + count;
                    try {
                        editor.putString("emblemIcon" + count, characterIdObj.get("emblemPath").getAsString());
                        editor.putString("emblemBackground" + count, characterIdObj.get("emblemBackgroundPath").getAsString());
                        editor.apply();

                        db.insertAccountData("account", currentCharacter, characterDB);
                    } catch (Exception e) {
                        System.out.println("Error: " + e);
                    }

//                    String emblemPath = characterIdObj.get("emblemPath").getAsString();
//                    System.out.println("Emblem path" +count+ ": " +emblemPath);
                    count++;
//                    String something = characterIdObj.getAsString();
                    System.out.println("character string: " + characterDB);

                    delegate.onComplete();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }

        });
    }
}
