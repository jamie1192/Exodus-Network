package com.jastley.warmindfordestiny2.api;

import com.google.gson.JsonElement;

import com.jastley.warmindfordestiny2.api.models.*;
import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

/**
 * Created by jamie1192 on 15/03/2018.
 */

public interface BungieAPI {

    String baseURL = "https://www.bungie.net";
    String plumbingURL = "https://destiny.plumbing/en/";

    //OAuth
    @Headers("Accept: application/json")
    @POST("/Platform/App/OAuth/Token/")
    @FormUrlEncoded
    Observable<AccessToken> getAccessToken(
        @Field("client_id") String clientId,
        @Field("client_secret") String clientSecret,
        @Field("grant_type") String grantType,
        @Field("code") String code
    );

    //Renew access_token
    @Headers("Accept: application/json")
    @POST("/Platform/App/OAuth/Token/")
    @FormUrlEncoded
    Observable<AccessToken> renewAccessToken(
        @Field("client_id") String clientId,
        @Field("client_secret") String clientSecret,
        @Field("grant_type") String grantType,
        @Field("refresh_token") String refreshToken
    );

    /** CHARACTER/ACCOUNT ENDPOINTS **/

    //Get membership data across all platforms for member
    @GET("/Platform/User/GetMembershipsForCurrentUser/")
    Observable<Response_GetCurrentUser> getMembershipsCurrentUser();

    //Get all characters
    @GET("/Platform/Destiny2/{membershipType}/Profile/{membershipId}/?components=200")
    Observable<JsonElement> getAllCharacters(@Path("membershipType") String membershipType,
                                 @Path("membershipId") String membershipId);

    //Get character inventory(?components=201), with item instance data(?components=300)
    @GET("/Platform/Destiny2/{membershipType}/Profile/{membershipId}/Character/{characterId}/?components=201&components=300")
    Observable<Response_GetCharacterInventory> getCharacterInventory(@Path("membershipType") String membershipType,
                                                                     @Path("membershipId") String membershipId,
                                                                     @Path("characterId") String characterId);

    //Get vault inventory, with instance data(?components=300)
    @GET("/Platform/Destiny2/{membershipType}/Profile/{membershipId}/?components=102&components=300")
    Observable<Response_GetCharacterInventory> getVaultInventory(@Path("membershipType") String membershipType,
                                                                 @Path("membershipId") String membershipId);

    //HistoricalStatsForAccount
    @GET("/Platform/Destiny2/{membershipType}/Account/{membershipId}/Stats/")
    Observable<Response_GetHistoricalStatsAccount> getHistoricalStatsAccount(@Path("membershipType") String membershipType,
                                                                             @Path("membershipId") String membershipId);
    //Get Clan Data
    @GET("/Platform/GroupV2/User/{membershipType}/{membershipId}/0/1/")
    Observable<Response_GetGroupsForMember> getClanData(@Path("membershipType") String membershipType, @Path("membershipId") String membershipId);

    //Transfer item to vault/character
    @POST("/Platform/Destiny2/Actions/Items/TransferItem/")
    Observable<Response_TransferEquipItem> transferItem(@Body TransferItemRequestBody transferBody);

    @POST("/Platform/Destiny2/Actions/Items/EquipItem/")
    Observable<Response_TransferEquipItem> equipItem(@Body EquipItemRequestBody equipBody);

    //Get Faction progression for character(?components=202)
    @GET("/Platform/Destiny2/{membershipType}/Profile/{membershipId}/Character/{characterId}/?components=202")
    Observable<Response_FactionProgression> getFactionProgress(@Path("membershipType") String membershipType,
                                                               @Path("membershipId") String membershipId,
                                                               @Path("characterId") String characterId);


    /** MANIFEST DATA **/

    //Get destiny.plumbing homepage to check manifest version/date (full URL overrides baseURL)
    @GET("https://destiny.plumbing/")
    Observable<Response_DestinyPlumbing> getDestinyPlumbing();

    //Get FactionDefinition data
    @GET("https://destiny.plumbing/en/raw/DestinyFactionDefinition.json")
    Observable<JsonElement> getFactionDefinitions();

    //Collectable Items/Weapons/Armor
    //@GET("reducedCollectableInventoryItems.json")
    @GET("raw/DestinyInventoryItemDefinition.json")
    Observable<JsonElement> getCollectablesDatabase();

    //Official Bungie Manifests
    @GET("/Platform/Destiny2/Manifest/")
    Observable<Response_GetBungieManifest> getBungieManifests();

    //Download .content manifest file
    @Streaming
    @GET
    Observable<ResponseBody> downloadUrlContent(@Url String fileUrl);


    /***** XUR ******/


    //Get Weekly Xur stock
//    @GET("/api/?request=history&for=xur")
    @GET("https://braytech.org/api/")
    Observable<Response_GetXurWeekly> getXurWeeklyInventory(@Query("key") String key,
                                                            @Query("request") String request,
                                                            @Query("for") String forVendor);

    @GET("/Platform/Destiny2/{membershipType}/Profile/{membershipId}/Character/{characterId}/Vendors/{vendorHash}/?components=400,402")
    Observable<Response_GetVendor> getVendorData(@Path("membershipType") String membershipType,
                                                 @Path("membershipId") String membershipId,
                                                 @Path("characterId") String characterId,
                                                 @Path("vendorHash") String vendorHash);

    /**** MILESTONES ***/

    @GET("/Platform/Destiny2/Milestones/")
    Observable<Response_GetMilestones> getMilestones();
}
