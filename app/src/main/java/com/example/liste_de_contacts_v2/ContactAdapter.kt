package com.example.liste_de_contacts_v2

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(
    private var contacts: List<Contact>,
    private val onContactClick: (Contact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvContactName)
        val tvPhone: TextView = view.findViewById(R.id.tvContactPhone)
        val ivPhoto: ImageView = view.findViewById(R.id.ivContactPhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contacts[position]
        holder.tvName.text = "${contact.nom} ${contact.prenom}"
        holder.tvPhone.text = contact.telephone

        if (!contact.imageUri.isNullOrEmpty()) {
            try {
                holder.ivPhoto.setImageURI(Uri.parse(contact.imageUri))
            } catch (e: Exception) {
                holder.ivPhoto.setImageResource(R.drawable.ic_avatar)
            }
        } else {
            holder.ivPhoto.setImageResource(R.drawable.ic_avatar)
        }

        holder.itemView.setOnClickListener {
            onContactClick(contact)
        }
    }

    override fun getItemCount(): Int = contacts.size

    fun updateData(newContacts: List<Contact>) {
        contacts = newContacts
        notifyDataSetChanged()
    }
}
