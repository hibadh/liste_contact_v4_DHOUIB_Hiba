package com.example.liste_de_contacts_v2

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ListeContactsFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()
    private lateinit var adapter: ContactAdapter
    private var allContacts: List<Contact> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_liste_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewContacts)
        val etSearch = view.findViewById<EditText>(R.id.etSearch)
        val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)
        val btnAddHeader = view.findViewById<ImageButton>(R.id.btnAddContactHeader)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = ContactAdapter(emptyList()) { contact ->
            openDetail(contact.id)
        }
        recyclerView.adapter = adapter

        viewModel.contacts.observe(viewLifecycleOwner) { contacts ->
            allContacts = contacts
            filterContacts(etSearch.text.toString(), tvEmptyState)
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterContacts(s.toString(), tvEmptyState)
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        btnAddHeader.setOnClickListener {
            openDetail(null)
        }
    }

    private fun filterContacts(query: String, tvEmptyState: TextView) {
        val filteredList = if (query.isEmpty()) {
            allContacts
        } else {
            allContacts.filter {
                it.nom.contains(query, ignoreCase = true) ||
                it.prenom.contains(query, ignoreCase = true) ||
                it.telephone.contains(query, ignoreCase = true)
            }
        }

        adapter.updateData(filteredList)

        if (filteredList.isEmpty()) {
            tvEmptyState.visibility = View.VISIBLE
        } else {
            tvEmptyState.visibility = View.GONE
        }
    }

    private fun openDetail(contactId: Int?) {
        val fragment = DetailContactFragment().apply {
            arguments = Bundle().apply {
                if (contactId != null) {
                    putInt("contact_id", contactId)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
