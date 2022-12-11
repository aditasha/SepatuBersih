package com.aditasha.sepatubersih

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ServerTime @Inject constructor(private val firebaseDatabase: FirebaseDatabase) {
    private val ref = firebaseDatabase.getReference(".info/serverTimeOffset")

    suspend fun getServerTime(): Long {
        return suspendCoroutine {
            ref.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    it.resume(snapshot.value as Long)
                }

                override fun onCancelled(error: DatabaseError) {}

            })
        }
    }

    fun refreshServerTime() {
        firebaseDatabase.goOffline()
        firebaseDatabase.goOnline()
    }
}