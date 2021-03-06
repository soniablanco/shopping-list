package com.piscos.soni.shoppinglist.lists;

import android.support.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingList;
import com.piscos.soni.shoppinglist.shoppinglist.ShoppingListItem;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FBShoppingListsRepository {
    private DatabaseReference shoppingListsDBRef;
    private DatabaseReference shoppingListItemsDBRef;
    private DatabaseReference myShoppingListsDBRef;
    private static final String SHOPPING_LISTS_NODE = "shoppinglists";
    private static final String SHOPPING_LISTS_ITEMS = "shoppinglistproducts";
    private static final String MY_SHOPPING_LISTS = "myshoppinglists";

    public FBShoppingListsRepository(){
        shoppingListsDBRef = FirebaseDatabase.getInstance().getReference();
        shoppingListsDBRef = shoppingListsDBRef.child(SHOPPING_LISTS_NODE);

        shoppingListItemsDBRef = FirebaseDatabase.getInstance().getReference();
        shoppingListItemsDBRef = shoppingListItemsDBRef.child(SHOPPING_LISTS_ITEMS);

        myShoppingListsDBRef = FirebaseDatabase.getInstance().getReference();
        myShoppingListsDBRef = myShoppingListsDBRef.child(MY_SHOPPING_LISTS);
    }

    public void getShoppingListLastUpdateTimeStamp(final SyncShoppingList sl, final SyncShoppingListLastUpdateTSListener listener){
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

        itemProperty = shoppingListItem.child("wasCollected");
        itemProperty.setValue(product.mWasCollected);

    }


    public void getShoppingListsUpdated(final Long lastSyncTS, final SyncFetchUpdatedShoppingListsListener listener){
        final List<SyncShoppingList> ts = new ArrayList<>();
        Query query = shoppingListsDBRef.orderByChild("lastUpdateTimestamp").startAt(lastSyncTS==null?0:lastSyncTS);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    FBShoppingList list = ds.getValue(FBShoppingList.class);
                    list.code = UUID.fromString(ds.getKey());
                    ts.add(new SyncShoppingList(list.code,list.name,null,list.lastUpdateTimestamp));
                }
                listener.onReady(ts);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getUpdatedShoppingListProducts(UUID shoppingListId, final SyncFetchUpdatedShoppingListProductsListener listener){
        final List<SyncShoppingListProduct> ts = new ArrayList<>();
        Query query = shoppingListItemsDBRef.child(shoppingListId.toString());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    FBShoppingListItem item = ds.getValue(FBShoppingListItem.class);
                    ts.add(new SyncShoppingListProduct(item.name,item.code,item.quantity,item.wasCollected));
                }
                listener.onReady(ts);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void getMyShoppingListsLastUpdateTimeStamp(final SyncGetMyShoppingListsLastTSListener listener){
        final Long[] ts = new Long[1];
        //DatabaseReference dr = myShoppingListsDBRef.child("lastUpdateTimestamp");
        myShoppingListsDBRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ts[0] = dataSnapshot.child("lastUpdateTimestamp").getValue(Long.class);
               listener.onReady(ts[0]==null?0:ts[0]);//(Long.getLong("123456"));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void updateMyShoppingListsLastUpdateTimestamp(Long newLastUpdateTimestamp){//has to be a FBfunction
        DatabaseReference lastTS = myShoppingListsDBRef.child("lastUpdateTimestamp");
        lastTS.setValue(newLastUpdateTimestamp);
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
}
