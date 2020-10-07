package ga.piscos.shoppinglist

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProviders
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



fun <T> LifecycleOwner.observe(data: LiveData<T>, onChange:(T)->Unit){
    data.observe(this, Observer{onChange(it!!)})
}