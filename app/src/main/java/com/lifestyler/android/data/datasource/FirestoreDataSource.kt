package com.lifestyler.android.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lifestyler.android.domain.entity.Client
import com.lifestyler.android.domain.entity.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreDataSource @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    
    companion object {
        private const val CLIENTS_COLLECTION = "clients"
        private const val USERS_COLLECTION = "users"
        private const val APPOINTMENTS_COLLECTION = "appointments"
    }
    
    // Client Operations
    suspend fun getAllClients(): List<Client> {
        return try {
            val snapshot = firestore.collection(CLIENTS_COLLECTION)
                .orderBy("registrationDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Client::class.java)?.copy(id = document.id)
            }
        } catch (exception: Exception) {
            throw Exception("Failed to fetch clients: ${exception.message}")
        }
    }
    
    suspend fun getPendingClients(): List<Client> {
        return try {
            val snapshot = firestore.collection(CLIENTS_COLLECTION)
                .whereEqualTo("status", "Pending")
                .orderBy("registrationDate", Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Client::class.java)?.copy(id = document.id)
            }
        } catch (exception: Exception) {
            throw Exception("Failed to fetch pending clients: ${exception.message}")
        }
    }
    
    suspend fun getClientById(clientId: String): Client? {
        return try {
            val document = firestore.collection(CLIENTS_COLLECTION)
                .document(clientId)
                .get()
                .await()
            
            document.toObject(Client::class.java)?.copy(id = document.id)
        } catch (exception: Exception) {
            throw Exception("Failed to fetch client: ${exception.message}")
        }
    }
    
    suspend fun createClient(client: Client): String {
        return try {
            val documentReference = firestore.collection(CLIENTS_COLLECTION)
                .add(client)
                .await()
            documentReference.id
        } catch (exception: Exception) {
            throw Exception("Failed to create client: ${exception.message}")
        }
    }
    
    suspend fun updateClient(clientId: String, client: Client): Boolean {
        return try {
            firestore.collection(CLIENTS_COLLECTION)
                .document(clientId)
                .set(client)
                .await()
            true
        } catch (exception: Exception) {
            throw Exception("Failed to update client: ${exception.message}")
        }
    }
    
    suspend fun deleteClient(clientId: String): Boolean {
        return try {
            firestore.collection(CLIENTS_COLLECTION)
                .document(clientId)
                .delete()
                .await()
            true
        } catch (exception: Exception) {
            throw Exception("Failed to delete client: ${exception.message}")
        }
    }
    
    // User Operations
    suspend fun getUserById(userId: String): User? {
        return try {
            val document = firestore.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()
            
            document.toObject(User::class.java)?.copy(userId = document.id)
        } catch (exception: Exception) {
            throw Exception("Failed to fetch user: ${exception.message}")
        }
    }
    
    suspend fun createUser(user: User): String {
        return try {
            val documentReference = firestore.collection(USERS_COLLECTION)
                .add(user)
                .await()
            documentReference.id
        } catch (exception: Exception) {
            throw Exception("Failed to create user: ${exception.message}")
        }
    }
    
    suspend fun updateUser(userId: String, user: User): Boolean {
        return try {
            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(user)
                .await()
            true
        } catch (exception: Exception) {
            throw Exception("Failed to update user: ${exception.message}")
        }
    }
    
    // Search Operations
    suspend fun searchClientsByName(name: String): List<Client> {
        return try {
            val snapshot = firestore.collection(CLIENTS_COLLECTION)
                .whereGreaterThanOrEqualTo("name", name)
                .whereLessThanOrEqualTo("name", name + '\uf8ff')
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Client::class.java)?.copy(id = document.id)
            }
        } catch (exception: Exception) {
            throw Exception("Failed to search clients: ${exception.message}")
        }
    }
    
    suspend fun searchClientsByPhone(phone: String): List<Client> {
        return try {
            val snapshot = firestore.collection(CLIENTS_COLLECTION)
                .whereEqualTo("phone", phone)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { document ->
                document.toObject(Client::class.java)?.copy(id = document.id)
            }
        } catch (exception: Exception) {
            throw Exception("Failed to search clients by phone: ${exception.message}")
        }
    }
} 