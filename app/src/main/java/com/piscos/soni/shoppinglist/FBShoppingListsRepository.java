package com.piscos.soni.shoppinglist;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

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

    public void updloadList(ShoppingList sl){
        DatabaseReference shoppingList = shoppingListsDBRef.child(sl.getId().toString());

        DatabaseReference shoppingListName = shoppingList.child("name");
        shoppingListName.setValue(sl.getName());

        uploadListItems(sl.getId(),sl.Items);
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

        itemProperty = shoppingListItem.child("timestamp");
        itemProperty.setValue(sli.getTimestamp());
    }

    public void uploadListItems (UUID slUUID,List<ShoppingListItem> slItems){
        for(ShoppingListItem i: slItems){
            uploadListItem(slUUID,i);
        }
    }
}
