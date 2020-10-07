package ga.piscos.shoppinglist

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable

operator fun CompositeDisposable.plus(disposable: Disposable) : CompositeDisposable {
    add(disposable)
    return this
}

fun  DatabaseReference.observable(): Observable<DataSnapshot> {
    return SnapshotObservable.create(this)
}