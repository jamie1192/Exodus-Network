package com.jastley.exodusnetwork.Inventory.fragments;

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
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;


import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.jastley.exodusnetwork.Definitions;
import com.jastley.exodusnetwork.Inventory.adapters.CharacterItemsRecyclerAdapter;
import com.jastley.exodusnetwork.Inventory.holders.TransferItemViewHolder;
import com.jastley.exodusnetwork.Inventory.interfaces.SuccessListener;
import com.jastley.exodusnetwork.Inventory.interfaces.TransferSelectListener;
import com.jastley.exodusnetwork.Inventory.models.CharacterDatabaseModel;
import com.jastley.exodusnetwork.Inventory.models.InventoryItemModel;
import com.jastley.exodusnetwork.Dialogs.LoadingDialogFragment;
import com.jastley.exodusnetwork.MainActivity;
import com.jastley.exodusnetwork.Milestones.models.InventoryDataModel;
import com.jastley.exodusnetwork.R;
import com.jastley.exodusnetwork.Utils.UnsignedHashConverter;
import com.jastley.exodusnetwork.Vendors.HeaderItemDecoration;
import com.jastley.exodusnetwork.api.BungieAPI;
import com.jastley.exodusnetwork.api.RetrofitHelper;
import com.jastley.exodusnetwork.database.AppManifestDatabase;
import com.jastley.exodusnetwork.database.dao.InventoryItemDefinitionDAO;
import com.jastley.exodusnetwork.database.models.DestinyInventoryItemDefinition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import retrofit2.HttpException;

import static com.jastley.exodusnetwork.api.BungieAPI.baseURL;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CharacterInventoryFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link CharacterInventoryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CharacterInventoryFragment extends Fragment implements TransferSelectListener, SuccessListener, SearchView.OnQueryTextListener {

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
    HeaderItemDecoration headerItemDecoration;


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

        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_character_inventory, container, false);

        ButterKnife.bind(this, rootView);
        setHasOptionsMenu(true);

        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        mBungieAPI = RetrofitHelper.getAuthBungieAPI(getContext(), baseURL);

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
        TabLayout mTabLayout = getActivity().findViewById(R.id.sliding_tabs);

        mTabLayout.setVisibility(View.GONE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.inventory_toolbar, menu);

        final SearchView searchView = (SearchView) menu.findItem(R.id.inventory_search).getActionView();
        searchView.setOnQueryTextListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.inventory_refresh:
                refreshInventory();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onTransferSelect(View view, int position, TransferItemViewHolder holder) {
        Toast.makeText(getContext(), " selected!!!?", Toast.LENGTH_SHORT).show();
        System.out.println("onTransferSelect triggered");
    }

    @Override
    public void inProgress(String title) {
        showLoadingDialog(title, "Please wait");
    }

    @Override
    public void onAuthFail() {
        Snackbar.make(getView(), "Re-Authorization required.", Snackbar.LENGTH_INDEFINITE)
                .setAction("Re-Authorize", v -> ((MainActivity)Objects.requireNonNull(getActivity())).refreshAccessToken())
                .show();
    }

    @Override
    public void onSuccess(int position, boolean wasSuccessful, String message, boolean isTransfer) {

        dismissLoadingFragment();

        if(wasSuccessful) {

            if(isTransfer){

                itemList.remove(position);
                resetItemDecoration(itemList);
                mItemsRecyclerAdapter.updateList(itemList);
                Snackbar.make(getView(), "Transferred to " + message, Snackbar.LENGTH_SHORT)
                        .show();
            }
            else {
                itemList.remove(position);
                resetItemDecoration(itemList);
                mItemsRecyclerAdapter.updateList(itemList);
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

    @Override
    public void onEquipSameCharacter(int position, boolean wasSuccessful, String message, boolean isTransfer) {

        dismissLoadingFragment();

        if(wasSuccessful){
            refreshInventory();
            Snackbar.make(getView(), "Equipped to " + message, Snackbar.LENGTH_SHORT)
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

    @Override
    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String query) {

        if(itemList != null){
            final List<InventoryItemModel> filteredItems = filter(itemList, query);

            resetItemDecoration(filteredItems);
            mItemsRecyclerAdapter.updateList(filteredItems);
            mItemsRecyclerView.scrollToPosition(0);
        }
        return true;
    }

    private static List<InventoryItemModel> filter(List<InventoryItemModel> itemList, String query) {
        final String lowerCaseQuery = query.toLowerCase();

        final List<InventoryItemModel> filteredList = new ArrayList<>();
        for (InventoryItemModel item : itemList) {
            final String text = item.getItemName().toLowerCase();
            if(text.contains(lowerCaseQuery)) {
                filteredList.add(item);
            }
        }
        return filteredList;
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

                        /* non-equipped items (getInventory()) */
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
                            itemModel.setSlot(Definitions.sortBuckets(response.getResponse().getInventory().getData().getItems().get(i).getBucketHash()));
                            //Required to manipulate UI on transfer/equip modal
                            itemModel.setClassType(mCharacter.getClassType());
                            itemModel.setTabIndex(mTabNumber);
                            itemList.add(itemModel);
                        }

                        /* character-equipped items (getEquipment()) */
                        for(int i = 0; i < response.getResponse().getEquipment().getEquipmentData().getEquipmentsItems().size(); i++){

                            String primaryStatValue;
                            String itemInstanceId = null;

                            //add/calculate hashes
                            String itemHash = String.valueOf(response.getResponse().getEquipment().getEquipmentData().getEquipmentsItems().get(i).getItemHash());
                            String primaryKey = UnsignedHashConverter.getPrimaryKey(itemHash);

                            itemHashList.add(primaryKey);

                            InventoryItemModel itemModel = new InventoryItemModel();
                            itemModel.setItemHash(itemHash);
                            itemModel.setPrimaryKey(primaryKey);

                            //Get instanceId to look up it's instanceData from the response
                            if(response.getResponse().getEquipment().getEquipmentData().getEquipmentsItems().get(i).getItemInstanceId() != null) {
                                itemInstanceId = response.getResponse().getEquipment().getEquipmentData().getEquipmentsItems().get(i).getItemInstanceId();
                                itemModel.setItemInstanceId(itemInstanceId);

                                itemModel.setIsEquipped(response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getIsEquipped());
                                itemModel.setCanEquip(response.getResponse().getItemComponents().getInstances().getInstanceData().get(itemInstanceId).getCanEquip());

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
                            itemModel.setBucketHash(response.getResponse().getEquipment().getEquipmentData().getEquipmentsItems().get(i).getBucketHash());
                            itemModel.setSlot(Definitions.sortBuckets(response.getResponse().getEquipment().getEquipmentData().getEquipmentsItems().get(i).getBucketHash()));
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
                    if(throwable instanceof HttpException){
                        Snackbar.make(getParentFragment().getView(), "Re-Authorization required.", Snackbar.LENGTH_INDEFINITE)
                                .setAction("Re-Authorize", v -> ((MainActivity)Objects.requireNonNull(getActivity())).refreshAccessToken())
                                .show();
                    }
                    else {
                        Log.d("GET_CHARACTER_INVENTORY", throwable.getMessage());
                        Snackbar.make(getParentFragment().getView(), throwable.getLocalizedMessage(), Snackbar.LENGTH_INDEFINITE)
                                .setAction("Retry", v -> refreshInventory())
                                .show();
                    }

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
                            itemModel.setSlot(Definitions.sortBuckets(response.getResponse().getProfileInventory().getData().getItems().get(i).getBucketHash()));
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

        InventoryItemDefinitionDAO mInventoryItemDAO = AppManifestDatabase.getManifestDatabase(getContext()).getInventoryItemDAO();

        Disposable disposable = mInventoryItemDAO.getItemsListByKey(hashes)
                .subscribeOn(Schedulers.computation())
                .observeOn(Schedulers.io())
                .subscribe(items -> {

                    for (int i = 0; i < items.size(); i++) {

                        Gson gson = new GsonBuilder().create();
                        InventoryDataModel itemData = gson.fromJson(items.get(i).getValue(), InventoryDataModel.class);

                        for (int j = 0; j < itemList.size(); j++) {
                            if(itemData.getHash().equals(itemList.get(j).getItemHash())) {

                                itemList.get(j).setItemName(itemData.getDisplayProperties().getName());
                                itemList.get(j).setItemTypeDisplayName(itemData.getItemTypeDisplayName());
                                try{
                                    Log.d("InventoryAPIListHash: ", itemList.get(j).getItemHash());
                                    Log.d("ManifestItemHash: ", itemData.getHash());
                                    itemList.get(j).setItemIcon(itemData.getDisplayProperties().getIcon());
                                }
                                catch(Exception e){
                                    Log.d("getManifestData: ", e.getLocalizedMessage());
                                }
                            }

                        }

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


        resetItemDecoration(itemList);

        mItemsRecyclerAdapter = new CharacterItemsRecyclerAdapter(getContext(), itemList, (view, position, holder) -> {
            Log.d("ITEM_LIST_CLICK", holder.getItemName().getText().toString());

            //get properties of clicked item
            InventoryItemModel clickedItem = itemList.get(position);

            clickedItem.setCurrentPosition(position);


            //for transferring TO vault
            clickedItem.setTabIndex(holder.getTabIndex());
            clickedItem.setVaultCharacterId(mCharacter.getCharacterId());

            ItemTransferDialogFragment transferDialogFragment = ItemTransferDialogFragment.newInstance(clickedItem, mTabNumber, mCharacterList, this);
            Bundle args = new Bundle();
            args.putParcelable("selectedItem", clickedItem);
            args.putParcelableArrayList("characterList", (ArrayList<? extends Parcelable>) mCharacterList);
            args.putInt("tabIndex", mTabNumber);
            transferDialogFragment.setArguments(args);

            transferDialogFragment.show(getChildFragmentManager(), "transferModalDialog");

        });
        mSwipeRefreshLayout.setRefreshing(false);

        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getContext());
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(getContext(), R.anim.layout_animation_slide_right);

        mItemsRecyclerView.setLayoutAnimation(controller);

        mItemsRecyclerView.setLayoutManager(mLinearLayoutManager);
        mItemsRecyclerView.setAdapter(mItemsRecyclerAdapter);

        loadingProgress.setVisibility(View.GONE);
    }

    public void refreshInventory() {

        mSwipeRefreshLayout.setRefreshing(true);

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
    }

    public void resetItemDecoration(List<InventoryItemModel> newList) {

        mItemsRecyclerView.removeItemDecoration(headerItemDecoration);

        headerItemDecoration = new HeaderItemDecoration(120, true, new HeaderItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                return position == 0 || newList.get(position).getSlot() != newList.get(position -1).getSlot();
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return Definitions.getBucketName(newList.get(position).getSlot());
            }

            @Override
            public CharSequence getItemCount(int position) {

                int count = 0;
                int section = newList.get(position).getSlot();

                for(InventoryItemModel item : newList) {
                    if(item.getSlot() == section) {
                        count++;
                    }
                }
                String itemCount = String.valueOf(count);

                if(section < 10) { //item/armor/equippable item (can only hold 10)
                    itemCount = itemCount+"/10";
                }

                return itemCount;
            }
        });
        mItemsRecyclerView.addItemDecoration(headerItemDecoration);
    }
}
