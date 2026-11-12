package com.example.liste_de_contacts_v2

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ContactViewModel : ViewModel() {
    private val _contacts = MutableLiveData<MutableList<Contact>>()
    val contacts: LiveData<MutableList<Contact>> = _contacts

    init {
        // Initial list corresponding to the original V2 mockup
        _contacts.value = mutableListOf(
            Contact(1, "Dupont", "Claire", "06 12 34 56 78", "claire.dupont@example.com"),
            Contact(2, "Martin", "Thomas", "07 23 45 67 89", "thomas.martin@example.com"),
            Contact(3, "Bernard", "Sophie", "06 98 76 54 32", "sophie.bernard@example.com"),
            Contact(4, "Petit", "Lucas", "07 11 22 33 44", "lucas.petit@example.com"),
            Contact(5, "Moreau", "Julien", "06 55 44 33 22", "julien.moreau@example.com")
        )
    }

    fun addContact(contact: Contact) {
        val currentList = _contacts.value ?: mutableListOf()
        currentList.add(contact)
        _contacts.value = currentList
    }

    fun updateContact(contact: Contact) {
        val currentList = _contacts.value ?: return
        val index = currentList.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            currentList[index] = contact
            _contacts.value = currentList
        }
    }

    fun deleteContact(id: Int) {
        val currentList = _contacts.value ?: return
        currentList.removeAll { it.id == id }
        _contacts.value = currentList
    }
    
    fun getContactById(id: Int): Contact? {
        return _contacts.value?.find { it.id == id }
    }
    
    fun getNextId(): Int {
        val currentList = _contacts.value ?: emptyList()
        return (currentList.maxOfOrNull { it.id } ?: 0) + 1
    }
}
