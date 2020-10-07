package ga.piscos.shoppinglist

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import kotlinx.android.synthetic.main.product_product_layout.*

object SnapshotObservable {

    fun create(reference: DatabaseReference) : Observable<DataSnapshot>{
        return Observable.create<DataSnapshot> {


            val listener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    it.onNext(dataSnapshot)
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    it.onError(Error(databaseError.details))
                }
            }
            reference.addValueEventListener(listener)
            it.setDisposable(object: Disposable {
                var wasDisposed:Boolean=false
                override fun isDisposed(): Boolean {
                    return  wasDisposed;
                }

                override fun dispose() {
                    reference.removeEventListener(listener)
                    wasDisposed = true
                }

            })

        }
    }
}