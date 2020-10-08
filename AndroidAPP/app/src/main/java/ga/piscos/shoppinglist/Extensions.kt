package ga.piscos.shoppinglist
import android.net.Uri
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.google.android.gms.tasks.Task
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.Exception

operator fun CompositeDisposable.plus(disposable: Disposable) : CompositeDisposable {
    add(disposable)
    return this
}

fun  DatabaseReference.observable(): Observable<DataSnapshot> {
    return SnapshotObservable.create(this)
}

fun <T> Task<T>.observable():Observable<T>{
    return Observable.create<T>(){
        val failureListener =  {error:Exception->
            it.onError(error)
        }
        val succesListener =  { result:T ->
            it.onNext(result)
        }
        this.addOnSuccessListener(succesListener)
        this.addOnFailureListener(failureListener)
    }
}

fun StorageReference.uploadObservable(file: Uri):Observable<UploadTask.TaskSnapshot>{



    return Observable.create<UploadTask.TaskSnapshot> {


        val failureListener =  {error:Exception->
            it.onError(error)
        }
        val succesListener =  {taskSnapshot:UploadTask.TaskSnapshot->
            it.onNext(taskSnapshot)
        }
        val uploadTask = this.putFile(file)
        uploadTask.addOnFailureListener(failureListener)
        uploadTask.addOnSuccessListener(succesListener)
        it.setDisposable(object: Disposable {
            var wasDisposed:Boolean=false
            override fun isDisposed(): Boolean {
                return  wasDisposed;
            }

            override fun dispose() {
                uploadTask.removeOnFailureListener(failureListener)
                uploadTask.removeOnSuccessListener(succesListener)
                wasDisposed = true
            }

        })

    }






}

fun <T> LifecycleOwner.observe(data: LiveData<T>, onChange:(T)->Unit){
    data.observe(this, Observer{onChange(it!!)})
}