package com.example.liste_de_contacts_v2

import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import java.io.File
import java.io.FileOutputStream

class DetailContactFragment : Fragment() {

    private val viewModel: ContactViewModel by activityViewModels()

    private var contact: Contact? = null
    private var isNewContact: Boolean = false
    private var currentImageUri: String? = null

    private lateinit var ivPhoto: ImageView
    private lateinit var etNom: EditText
    private lateinit var etPrenom: EditText
    private lateinit var etTelephone: EditText
    private lateinit var etEmail: EditText
    private lateinit var etLatitude: EditText
    private lateinit var etLongitude: EditText

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                requireContext().contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {
            }
            currentImageUri = uri.toString()
            ivPhoto.setImageURI(uri)
        } else {
            Toast.makeText(requireContext(), "Sélection d'image annulée", Toast.LENGTH_SHORT).show()
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedUri = saveBitmapToCache(bitmap)
            if (savedUri != null) {
                currentImageUri = savedUri.toString()
                ivPhoto.setImageBitmap(bitmap)
            } else {
                Toast.makeText(requireContext(), "Erreur lors de la sauvegarde de l'image", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(requireContext(), "Prise de photo annulée", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_detail_contact, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val contactId = arguments?.getInt("contact_id", -1) ?: -1
        isNewContact = contactId == -1

        if (isNewContact) {
            val newId = viewModel.getNextId()
            contact = Contact(newId, "", "", "", "")
        } else {
            contact = viewModel.getContactById(contactId)?.copy()
        }

        if (contact == null) {
            Toast.makeText(requireContext(), "Erreur : Contact non trouvé", Toast.LENGTH_SHORT).show()
            parentFragmentManager.popBackStack()
            return
        }

        currentImageUri = contact?.imageUri

        ivPhoto = view.findViewById(R.id.ivDetailPhoto)
        etNom = view.findViewById(R.id.etNom)
        etPrenom = view.findViewById(R.id.etPrenom)
        etTelephone = view.findViewById(R.id.etTelephone)
        etEmail = view.findViewById(R.id.etEmail)
        etLatitude = view.findViewById(R.id.etLatitude)
        etLongitude = view.findViewById(R.id.etLongitude)
        
        val tvDetailTitle = view.findViewById<TextView>(R.id.tvDetailTitle)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)
        val btnDelete = view.findViewById<ImageButton>(R.id.btnDeleteContact)
        val ivCameraBadge = view.findViewById<ImageView>(R.id.ivCameraBadge)
        val btnGallery = view.findViewById<Button>(R.id.btnGallery)
        val btnCamera = view.findViewById<Button>(R.id.btnCamera)
        val btnCall = view.findViewById<ImageButton>(R.id.btnCall)
        val btnSendEmail = view.findViewById<ImageButton>(R.id.btnSendEmail)
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnMap = view.findViewById<Button>(R.id.btnMap)

        tvDetailTitle.text = if (isNewContact) "Nouveau contact" else "Détail du contact"

        btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        
        btnCancel.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        btnDelete.setOnClickListener {
            if (!isNewContact) {
                viewModel.deleteContact(contact!!.id)
                parentFragmentManager.popBackStack()
            } else {
                parentFragmentManager.popBackStack()
            }
        }

        ivCameraBadge.setOnClickListener {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Impossible d'ouvrir l'appareil photo", Toast.LENGTH_SHORT).show()
            }
        }

        etNom.setText(contact?.nom)
        etPrenom.setText(contact?.prenom)
        etTelephone.setText(contact?.telephone)
        etEmail.setText(contact?.email)
        etLatitude.setText(contact?.latitude?.toString() ?: "")
        etLongitude.setText(contact?.longitude?.toString() ?: "")

        if (!contact?.imageUri.isNullOrEmpty()) {
            try {
                ivPhoto.setImageURI(Uri.parse(contact?.imageUri))
            } catch (_: Exception) {
                ivPhoto.setImageResource(R.drawable.ic_avatar)
            }
        } else {
            ivPhoto.setImageResource(R.drawable.ic_avatar)
        }

        btnGallery.setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        btnCamera.setOnClickListener {
            try {
                cameraLauncher.launch(null)
            } catch (_: Exception) {
                Toast.makeText(requireContext(), "Impossible d'ouvrir l'appareil photo", Toast.LENGTH_SHORT).show()
            }
        }

        btnCall.setOnClickListener {
            val phone = etTelephone.text?.toString()?.trim() ?: ""
            if (phone.isNotEmpty()) {
                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                try {
                    startActivity(callIntent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "Aucune application de téléphone disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                etTelephone.error = "Veuillez entrer un numéro de téléphone"
            }
        }

        btnSendEmail.setOnClickListener {
            val email = etEmail.text?.toString()?.trim() ?: ""
            if (email.isNotEmpty()) {
                val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email"))
                try {
                    startActivity(emailIntent)
                } catch (_: ActivityNotFoundException) {
                    Toast.makeText(requireContext(), "Aucune application de messagerie disponible", Toast.LENGTH_SHORT).show()
                }
            } else {
                etEmail.error = "Veuillez entrer une adresse e-mail"
            }
        }
        
        btnMap.setOnClickListener {
            val latText = etLatitude.text.toString()
            val lngText = etLongitude.text.toString()
            val lat = latText.toDoubleOrNull()
            val lng = lngText.toDoubleOrNull()
            
            if (lat == null || lng == null) {
                Toast.makeText(requireContext(), "Coordonnées invalides ou absentes", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (lat < -90.0 || lat > 90.0) {
                Toast.makeText(requireContext(), "Latitude doit être entre -90 et 90", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (lng < -180.0 || lng > 180.0) {
                Toast.makeText(requireContext(), "Longitude doit être entre -180 et 180", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Always update current working object before passing to next fragment to not lose them
            contact?.latitude = lat
            contact?.longitude = lng
            
            val fragment = CarteContactFragment().apply {
                arguments = Bundle().apply {
                    putInt("contact_id", contact!!.id)
                    putDouble("latitude", lat)
                    putDouble("longitude", lng)
                    putString("nom", contact!!.nom)
                }
            }
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

        btnSave.setOnClickListener {
            val nom = etNom.text?.toString()?.trim() ?: ""
            val prenom = etPrenom.text?.toString()?.trim() ?: ""
            val phone = etTelephone.text?.toString()?.trim() ?: ""
            val email = etEmail.text?.toString()?.trim() ?: ""
            
            val latText = etLatitude.text?.toString()?.trim() ?: ""
            val lngText = etLongitude.text?.toString()?.trim() ?: ""
            
            var lat: Double? = null
            var lng: Double? = null

            if (nom.isEmpty()) {
                etNom.error = "Le nom est obligatoire"
                return@setOnClickListener
            }
            if (prenom.isEmpty()) {
                etPrenom.error = "Le prénom est obligatoire"
                return@setOnClickListener
            }
            if (phone.isEmpty()) {
                etTelephone.error = "Le numéro de téléphone est obligatoire"
                return@setOnClickListener
            }
            
            if (latText.isNotEmpty()) {
                lat = latText.toDoubleOrNull()
                if (lat == null) {
                    etLatitude.error = "Valeur non numérique"
                    return@setOnClickListener
                }
                if (lat < -90.0 || lat > 90.0) {
                    etLatitude.error = "Doit être entre -90 et 90"
                    return@setOnClickListener
                }
            }
            
            if (lngText.isNotEmpty()) {
                lng = lngText.toDoubleOrNull()
                if (lng == null) {
                    etLongitude.error = "Valeur non numérique"
                    return@setOnClickListener
                }
                if (lng < -180.0 || lng > 180.0) {
                    etLongitude.error = "Doit être entre -180 et 180"
                    return@setOnClickListener
                }
            }
            
            if ((lat != null && lng == null) || (lat == null && lng != null)) {
                Toast.makeText(requireContext(), "Veuillez renseigner les deux coordonnées", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            contact?.nom = nom
            contact?.prenom = prenom
            contact?.telephone = phone
            contact?.email = email
            contact?.latitude = lat
            contact?.longitude = lng
            contact?.imageUri = currentImageUri

            if (isNewContact) {
                viewModel.addContact(contact!!)
            } else {
                viewModel.updateContact(contact!!)
            }
            
            parentFragmentManager.popBackStack()
        }
    }

    private fun saveBitmapToCache(bitmap: Bitmap): Uri? {
        return try {
            val file = File(requireContext().cacheDir, "photo_${System.currentTimeMillis()}.jpg")
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
