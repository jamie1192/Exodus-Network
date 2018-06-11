package com.jastley.warmindfordestiny2.Inventory.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jastley.warmindfordestiny2.Inventory.adapters.CharacterItemsRecyclerAdapter;
import com.jastley.warmindfordestiny2.Inventory.holders.TransferItemViewHolder;
import com.jastley.warmindfordestiny2.Inventory.interfaces.SuccessListener;
import com.jastley.warmindfordestiny2.Inventory.interfaces.TransferSelectListener;
import com.jastley.warmindfordestiny2.Inventory.models.CharacterDatabaseModel;
import com.jastley.warmindfordestiny2.Inventory.models.InventoryItemModel;
import com.jastley.warmindfordestiny2.Dialogs.LoadingDialogFragment;
import com.jastley.warmindfordestiny2.Milestones.models.InventoryDataModel;
import com.jastley.warmindfordestiny2.R;
import com.jastley.warmindfordestiny2.Utils.InventoryBucketDefinition;
import com.jastley.warmindfordestiny2.Utils.UnsignedHashConverter;
import com.jastley.warmindfordestiny2.Vendors.HeaderItemDecoration;
import com.jastley.warmindfordestiny2.api.BungieAPI;
import com.jastley.warmindfordestiny2.api.RetrofitHelper;
import com.jastley.warmindfordestiny2.database.AppDatabase;
import com.jastley.warmindfordestiny2.database.InventoryItemDAO;
import com.jastley.warmindfordestiny2.database.models.DestinyInventoryItemDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.jastley.warmindfordestiny2.api.BungieAPI.baseURL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CharacterInventoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CharacterInventoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterInventoryFragment extends Fragment implements TransferSelectListener, SuccessListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    private static final int ARG_TAB_NUMBER;

    // TODO: Rename and change types of parameters
    private int mTabNumber;
    private CharacterDatabaseModel mCharacter;

    @BindView(R.id.inventory_items_recyclerview) RecyclerView mItemsRecyclerView;
    @BindView(R.id.inventory_items_progress) ProgressBar loadingProgress;
    @BindView(R.id.inventory_swipe_refresh) SwipeRefreshLayout mSwipeRefreshLayout;
    private CharacterItemsRecyclerAdapter mItemsRecyclerAdapter;

    private OnFragmentInteractionListener mListener;
    private BungieAPI mBungieAPI;
    List<InventoryItemModel> itemList = new ArrayList<>();
    private List<CharacterDatabaseModel> mCharacterList = new ArrayList<>();

    CompositeDisposable compositeDisposable = new CompositeDisposable();

//    boolean mIsRestoredFromBackstack;

    public CharacterInventoryFragment() {
        // Required empty public constructor
    }


    public static CharacterInventoryFragment newInstance(int tabNumber,
                                                         CharacterDatabaseModel character,
                                                         ArrayList<CharacterDatabaseModel> characterList,
                                                         ArrayList<DestinyInventoryItemDefinition> destinyInventoryItemDefinitionManifest) {
        CharacterInventoryFragment fragment = new CharacterInventoryFragment();
        Bundle args = new Bundle();
        System.out.println("Fragment created, tabIndex: " + tabNumber);
        args.putInt("ARG_TAB_NUMBER", tabNumber);
        args.putParcelable("ARG_CHARACTER_DATA", character);
        args.putParcelableArrayList("ARG_CHARACTER_LIST", characterList);
        args.putParcelableArrayList("ARG_COLLECTABLES_MANIFEST", destinyInventoryItemDefinitionManifest);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTabNumber = getArguments().getInt("ARG_TAB_NUMBER");
            mCharacter = getArguments().getParcelable("ARG_CHARACTER_DATA");
            mCharacterList = getArguments().getParcelableArrayList("ARG_CHARACTER_LIST");
            System.out.println("onCreate tab "+mTabNumber+", "+mCharacter.getClassType());
//            mCollectablesList = getArguments().getParcelableArrayList("ARG_COLLECTABLES_MANIFEST");
        }

//        mIsRestoredFromBackstack = false;

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_character_inventory, container, false);

        ButterKnife.bind(this, rootView);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);

        mBungieAPI = RetrofitHelper.getAuthBungieAPI(getContext(), baseURL);

//        if (savedInstanceState != null) {
//            itemList = savedInstanceState.getParcelableArrayList("characterItems");
//            setRecyclerView(itemList);
//        }
//        else {

        String classType = mCharacter.getClassType();

        if(classType.equals("vault")){
            //get Vault items only
            getVaultInventory(
                    mCharacter.getMembershipType(),
                    mCharacter.getMembershipId());
        }
        else {
            getCharacterInventory(
                mCharacter.getMembershipType(),
                mCharacter.getMembershipId(),
                mCharacter.getCharacterId());
        }

        mSwipeRefreshLayout.setOnRefreshListener(() -> {

            mSwipeRefreshLayout.setRefreshing(true);

            if(classType.equals("vault")){
                //get Vault items only
                getVaultInventory(
                        mCharacter.getMembershipType(),
                        mCharacter.getMembershipId());
            }
            else {
                getCharacterInventory(
                        mCharacter.getMembershipType(),
                        mCharacter.getMembershipId(),
                        mCharacter.getCharacterId());
            }
        });

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
            System.out.println("FragmentInteraction triggered");
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnDetailsFragmentInteraction");
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
        compositeDisposable.dispose();

        //Hide character tab bar
        TabLayout mTabLayout = getActivity().findViewById(R.id.inventory_sliding_tabs);

        mTabLayout.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

//        outState.putParcelableArrayList("characterItems", (ArrayList<? extends Parcelable>) itemList);
    }

    @Override
    public void onTransferSelect(View view, int position, TransferItemViewHolder holder) {
        Toast.makeText(getContext(), " selected!!!?", Toast.LENGTH_SHORT).show();
        System.out.println("onTransferSelect triggered");
    }

    @Override
    public void inProgress() {
        showLoadingDialog("Transferring...", "Please wait");
    }

    @Override
    public void onSuccess(int position, boolean wasSuccessful, String message, boolean isTransfer) {

        dismissLoadingFragment();

        if(wasSuccessful) {

            if(isTransfer){
                itemList.remove(position);
                mItemsRecyclerAdapter.notifyItemRemoved(position);
                Snackbar.make(getView(), "Transferred to " + message, Snackbar.LENGTH_SHORT)
                        .show();
            }
            else {
                itemList.remove(position);
                mItemsRecyclerAdapter.notifyItemRemoved(position);
                Snackbar.make(getView(), "Equipped to " + message, Snackbar.LENGTH_SHORT)
                        .show();
            }
        }
        else {
            //Error handling
            Snackbar.make(getView(), message, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void showLoadingDialog(String title, String message) {
        LoadingDialogFragment fragment = LoadingDialogFragment.newInstance(title, message);
        fragment.setCancelable(false);
        fragment.show(getActivity().getFragmentManager(), "loadingDialog");
    }

    private void dismissLoadingFragment() {
        LoadingDialogFragment loadingFragment = (LoadingDialogFragment)getActivity().getFragmentManager().findFragmentByTag("loadingDialog");
        if (loadingFragment != null){
            loadingFragment.dismiss();
        }
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String uri);
    }

    public void getCharacterInventory(String membershipType, String membershipId, String characterId) {

        Disposable disposable = mBungieAPI.getCharacterInventory(membershipType, membershipId, characterId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(response -> {

                    //Error code checking
                    if(!response.getErrorCode().equals("1")){
                        Snackbar.make(getParentFragment().getView(), response.getMessage(), Snackbar.LENGTH_INDEFINITE)
                                .setAction("Retry", v -> refreshInventory())
                                .show();
                    }
                    else {

                        ArrayList<String> itemHashList = new ArrayList<>();

                        itemList.clear();
                        for(int i = 0; i < response.getResponse().getInventory().getData().getItems().size(); i++){

                            String primaryStatValue;
                            String itemInstanceId = null;

                            //add/calculate hashes
                            String itemHash = String.valueOf(response.getResponse().getInventory().getData().getItems().get(i).getItemHash());
                            String primaryKey = UnsignedHashConverter.getPrimaryKey(itemHash);

                            itemHashList.add(primaryKey);

                            InventoryItemModel itemModel = new InventoryItemModel();
                            itemModel.setItemHash(itemHash);
                            itemModel.setPrimaryKey(primaryKey);

                            //Get instanceId to look up it's instanceData from the response
                            if(response.getResponse().getInventory().getData().getItems().get(i).getItemInstanceId() != null) {
                                itemInstanceId = response.getResponse().getInventory().getData().getItems().get(i).getItemInstanceId();
                                itemModel.setItemInstanceId(itemInstanceId);

                                //get damageType (if != null)
                                if(response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getDamageType() != null){
                                    itemModel.setDamageType(response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getDamageType());
                                }

                            }

                            try{
                                primaryStatValue = response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getPrimaryStat().getValue();
                                itemModel.setPrimaryStatValue(primaryStatValue);
                            }
                            catch(Exception e){
                                System.out.println("Not an instance-specific item");
                            }
//
                            itemModel.setBucketHash(response.getResponse().getInventory().getData().getItems().get(i).getBucketHash());
                            itemModel.setSlot(InventoryBucketDefinition.sortBuckets(response.getResponse().getInventory().getData().getItems().get(i).getBucketHash()));
                            //Required to manipulate UI on transfer/equip modal
                            itemModel.setClassType(mCharacter.getClassType());
                            itemModel.setTabIndex(mTabNumber);
                            itemList.add(itemModel);
                        }

                        //Sort lists by primary key value, must cast to Long as some values too large for int
                        Collections.sort(itemHashList, (s, t1) -> Long.valueOf(s).compareTo(Long.valueOf(t1)));
                        Collections.sort(itemList, (t1, t2) -> Long.valueOf(t1.getPrimaryKey()).compareTo(Long.valueOf((t2.getPrimaryKey()))));

                        getManifestData(itemHashList);
                    }

                }, throwable -> {
                    Log.d("GET_CHARACTER_INVENTORY", throwable.getMessage());
                    Snackbar.make(getParentFragment().getView(), throwable.getLocalizedMessage(), Snackbar.LENGTH_INDEFINITE)
                            .setAction("Retry", v -> refreshInventory())
                            .show();
                    });
        compositeDisposable.add(disposable);
    }

    public void getVaultInventory(String membershipType, String membershipId) {

        Disposable disposable = mBungieAPI.getVaultInventory(membershipType, membershipId)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(response -> {

                    //Error code checking
                    if(!response.getErrorCode().equals("1")){
                        Snackbar.make(getParentFragment().getView(), response.getMessage(), Snackbar.LENGTH_INDEFINITE)
                                .setAction("Retry", v -> refreshInventory())
                                .show();
                    }
                    else {

                        ArrayList<String> itemHashList = new ArrayList<>();

                        itemList.clear();
                        /** FOR VAULT INVENTORY, USE getProfileInventory(), NOT getInventory() **/
                        for(int i = 0; i < response.getResponse().getProfileInventory().getData().getItems().size(); i++){

                            String primaryStatValue;
                            String itemInstanceId = null;

                            //add/calculate hashes
                            String itemHash = String.valueOf(response.getResponse().getProfileInventory().getData().getItems().get(i).getItemHash());
                            String primaryKey = UnsignedHashConverter.getPrimaryKey(itemHash);

                            itemHashList.add(primaryKey);

                            InventoryItemModel itemModel = new InventoryItemModel();

                            itemModel.setItemHash(itemHash);
                            itemModel.setPrimaryKey(primaryKey);

                            //Get instanceId to look up it's instanceData from the response
                            if(response.getResponse().getProfileInventory().getData().getItems().get(i).getItemInstanceId() != null) {
                                itemInstanceId = response.getResponse().getProfileInventory().getData().getItems().get(i).getItemInstanceId();
                                itemModel.setItemInstanceId(itemInstanceId);

                                //get damageType (if != null)
                                if(response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getDamageType() != null){
                                    itemModel.setDamageType(response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getDamageType());
                                }

                            }

                            try{
                                primaryStatValue = response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getPrimaryStat().getValue();
                                itemModel.setPrimaryStatValue(primaryStatValue);

                            }
                            catch(Exception e){
                                System.out.println("Not an instance-specific item");
                            }

                            itemModel.setBucketHash(response.getResponse().getProfileInventory().getData().getItems().get(i).getBucketHash());
                            itemModel.setSlot(InventoryBucketDefinition.sortBuckets(response.getResponse().getProfileInventory().getData().getItems().get(i).getBucketHash()));
                            //Required to manipulate UI on transfer/equip modal
                            itemModel.setClassType(mCharacter.getClassType());
                            itemModel.setTabIndex(mTabNumber);
                            itemList.add(itemModel);
                        }

                        //Sort lists by primary key value, must cast to Long as some values too large for int
                        Collections.sort(itemHashList, (s, t1) -> Long.valueOf(s).compareTo(Long.valueOf(t1)));
                        Collections.sort(itemList, (t1, t2) -> Long.valueOf(t1.getPrimaryKey()).compareTo(Long.valueOf((t2.getPrimaryKey()))));

                        getManifestData(itemHashList);
                    }

                }, throwable -> {
                    Snackbar.make(getParentFragment().getView(), throwable.getLocalizedMessage(), Snackbar.LENGTH_INDEFINITE)
                            .setAction("Retry", v -> refreshInventory())
                            .show();
                });

        compositeDisposable.add(disposable);
    }


    public void getManifestData(List<String> hashes){

        InventoryItemDAO mInventoryItemDAO = AppDatabase.getAppDatabase(getContext()).getInventoryItemDAO();

        Disposable disposable = mInventoryItemDAO.getItemsListByKey(hashes)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(items -> {

                    for (int i = 0; i < items.size(); i++) {

                        Gson gson = new GsonBuilder().create();
                        InventoryDataModel itemData = gson.fromJson(items.get(i).getValue(), InventoryDataModel.class);

                        if(itemData.getHash().equals(itemList.get(i).getItemHash())) {

                                itemList.get(i).setItemName(itemData.getDisplayProperties().getName());
                                try{
                                    Log.d("InventoryAPIListHash: ", itemList.get(i).getItemHash());
                                    Log.d("ManifestItemHash: ", itemData.getHash());
                                    itemList.get(i).setItemIcon(itemData.getDisplayProperties().getIcon());
                                }
                                catch(Exception e){
                                    Log.d("getManifestData: ", e.getLocalizedMessage());
                                }
                            }
//                        }
                    }

                    //sort by slot order for RVItemDecoration
                    Collections.sort(itemList, (inventoryItemModel, t1) -> inventoryItemModel.getSlot() - t1.getSlot());

                    Handler mainHandler = new Handler(Looper.getMainLooper());
                    Runnable mRunnable = () -> {
                        setRecyclerView(itemList);
                    };
                    mainHandler.post(mRunnable);

                }, throwable -> {
                    Log.d("GET_MANIFEST_DATA", throwable.getLocalizedMessage());
                    itemList.clear();
                    mItemsRecyclerAdapter.notifyDataSetChanged();
                    Snackbar.make(getParentFragment().getView(), throwable.getLocalizedMessage(), Snackbar.LENGTH_INDEFINITE)
                            .setAction("Retry", v -> refreshInventory())
                            .show();
                });
        compositeDisposable.add(disposable);
    }

    public void setRecyclerView(List<InventoryItemModel> itemList){

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());

        HeaderItemDecoration headerItemDecoration = new HeaderItemDecoration(20, true, new HeaderItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                return false;
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return InventoryBucketDefinition.getBucketName(itemList.get(position).getSlot());
            }
        });

        mItemsRecyclerAdapter = new CharacterItemsRecyclerAdapter(getContext(), itemList, (view, position, holder) -> {
            Log.d("ITEM_LIST_CLICK", holder.getItemName().getText().toString());

            //get properties of clicked item
            InventoryItemModel clickedItem = new InventoryItemModel();
            clickedItem.setBucketHash(holder.getBucketHash());
            clickedItem.setItemInstanceId(holder.getItemInstanceId());
            clickedItem.setIsEquipped(holder.getIsEquipped());
            clickedItem.setCanEquip(holder.getCanEquip());
            clickedItem.setItemHash(holder.getItemHash());
            clickedItem.setItemName(holder.getItemName().getText().toString());
            clickedItem.setItemIcon(holder.getImageUrl());
            clickedItem.setPrimaryStatValue(holder.getPrimaryStatValue());
            clickedItem.setItemTypeDisplayName(holder.getItemTypeDisplayName());
            clickedItem.setDamageType(holder.getDamageType());
            clickedItem.setCurrentPosition(position);

            //for transferring TO vault
            clickedItem.setTabIndex(holder.getTabIndex());
            clickedItem.setVaultCharacterId(mCharacter.getCharacterId());

//            transferModalDialog = new ItemTransferDialogFragment();
            ItemTransferDialogFragment transferDialogFragment = ItemTransferDialogFragment.newInstance(clickedItem, mTabNumber, mCharacterList, this);
            Bundle args = new Bundle();
            args.putParcelable("selectedItem", clickedItem);
            args.putParcelableArrayList("characterList", (ArrayList<? extends Parcelable>) mCharacterList); //TODO HERERERERE
            args.putInt("tabIndex", mTabNumber);
            transferDialogFragment.setArguments(args);

            transferDialogFragment.show(getChildFragmentManager(), "transferModalDialog");

        });
        mSwipeRefreshLayout.setRefreshing(false);

        mItemsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mItemsRecyclerView.setAdapter(mItemsRecyclerAdapter);
        mItemsRecyclerView.addItemDecoration(headerItemDecoration);
        mItemsRecyclerAdapter.notifyDataSetChanged();
        loadingProgress.setVisibility(View.GONE);
    }

    public void refreshInventory() {
        String classType = mCharacter.getClassType();

        if(classType.equals("vault")){
            //get Vault items only
            getVaultInventory(
                    mCharacter.getMembershipType(),
                    mCharacter.getMembershipId());
        }
        else {
//            mItemsRecyclerAdapter.notifyDataSetChanged();
            getCharacterInventory(
                    mCharacter.getMembershipType(),
                    mCharacter.getMembershipId(),
                    mCharacter.getCharacterId());
        }
    }

}
