package com.piscos.soni.shoppinglist;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FBShoppingListsRepository {
    private DatabaseReference shoppingListsDBRef;
    private DatabaseReference shoppingListItemsDBRef;
    private static final String SHOPPING_LISTS_NODE = "shoppinglists";
    private static final String SHOPPING_LISTS_ITEMS = "shoppinglistproducts";

    public FBShoppingListsRepository(){
        shoppingListsDBRef = FirebaseDatabase.getInstance().getReference();
        shoppingListsDBRef = shoppingListsDBRef.child(SHOPPING_LISTS_NODE);

        shoppingListItemsDBRef = FirebaseDatabase.getInstance().getReference();
        shoppingListItemsDBRef = shoppingListItemsDBRef.child(SHOPPING_LISTS_ITEMS);
    }

    public void getShoppingListLastUpdateTimeStamp(final SyncShoppingList sl,final SyncShoppingListLastUpdateTSListener listener){
        final Long[] ts = new Long[1];
        DatabaseReference dr = shoppingListsDBRef.child(sl.mId.toString());
        //dr = dr.child("lastUpdateTimestamp");
        dr.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ts[0] = dataSnapshot.child("lastUpdateTimestamp").getValue(Long.class);
                listener.onReady(ts[0]);//(Long.getLong("123456"));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void pushShoppingList(SyncShoppingList sl){
        DatabaseReference shoppingList = shoppingListsDBRef.child(sl.mId.toString());

        DatabaseReference shoppingListName = shoppingList.child("name");
        shoppingListName.setValue(sl.mName);

        DatabaseReference shoppingListTS = shoppingList.child("lastUpdateTimestamp");
        shoppingListTS.setValue(sl.mLastUpdateTS);

        pushShoppingListProductsItems (sl.mId,sl.Items);
    }

    public void pushShoppingListProductsItems (UUID shoppingListId,List<SyncShoppingListProduct> products){
        DatabaseReference shoppingListItem = shoppingListItemsDBRef.child(shoppingListId.toString());
        shoppingListItem.removeValue();
        for(SyncShoppingListProduct i: products){
            pushShoppingListProduct(shoppingListId,i);
        }
    }

    public void pushShoppingListProduct(UUID shoppingListId,SyncShoppingListProduct product){
        DatabaseReference shoppingListItem = shoppingListItemsDBRef.child(shoppingListId.toString());
        shoppingListItem = shoppingListItem.child(product.mCode);

        DatabaseReference itemProperty =  shoppingListItem.child("code");
        itemProperty.setValue(product.mCode);

        itemProperty = shoppingListItem.child("name");
        itemProperty.setValue(product.mName);

        itemProperty = shoppingListItem.child("quantity");
        itemProperty.setValue(product.mQuantity);

    }





    public void uploadList(ShoppingList sl){
        DatabaseReference shoppingList = shoppingListsDBRef.child(sl.getId().toString());

        DatabaseReference shoppingListName = shoppingList.child("name");
        shoppingListName.setValue(sl.getName());

        DatabaseReference shoppingListTS = shoppingList.child("lastUpdateTimestamp");
        shoppingListTS.setValue(sl.getLastUpdateTS());

        //uploadListItems(sl.getId(),sl.Items);
    }

    public void uploadListItem(UUID slUUID, ShoppingListItem sli){
        DatabaseReference shoppingListItem = shoppingListItemsDBRef.child(slUUID.toString());
        shoppingListItem = shoppingListItem.child(sli.getCode());

        DatabaseReference itemProperty =  shoppingListItem.child("code");
        itemProperty.setValue(sli.getCode());

        itemProperty = shoppingListItem.child("name");
        itemProperty.setValue(sli.getName());

        itemProperty = shoppingListItem.child("quantity");
        itemProperty.setValue(sli.getQuantity());

        /*itemProperty = shoppingListItem.child("timestamp");
        itemProperty.setValue(sli.getTimestamp());*/
    }

    public void uploadListItems (UUID slUUID,List<ShoppingListItem> slItems){
        DatabaseReference shoppingListItem = shoppingListItemsDBRef.child(slUUID.toString());
        shoppingListItem.removeValue();
        for(ShoppingListItem i: slItems){
            uploadListItem(slUUID,i);
        }
    }

   public  void  fetchListItems(final ShoppingListItemsDownloadedListener listener){
        final List<ShoppingListItem> productList = new ArrayList<>();
        shoppingListItemsDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    FBShoppingListItem product = ds.getValue(FBShoppingListItem.class);
                    productList.add(new ShoppingListItem(product.name, product.code, "",product.quantity,product.timestamp,true));
                }
                listener.onReady(productList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
