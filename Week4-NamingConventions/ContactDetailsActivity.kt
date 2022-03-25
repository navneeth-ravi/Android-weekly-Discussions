package com.example.nav_contacts.presentation.activity

// All comments are indicates that bad practice which is previously used.

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.nav_contacts.*
import com.example.nav_contacts.data.db.local_db.DatabaseFunctionalities
import com.example.nav_contacts.data.db.local_db.MyContentProvider
import com.example.nav_contacts.domain.entity.ContactDataClass
import com.example.nav_contacts.presentation.adapter.PhoneNumberAdapter
import com.example.nav_contacts.presentation.custom_view.IconWithNameLayout
import com.example.nav_contacts.presentation.handler.IconWithNameHandler
import kotlinx.android.synthetic.main.activity_contact_details.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

class ContactDetailsActivity : AppCompatActivity(){
    companion object {
//        const val CONTACT_KEY = "contactDetail"
        const val CONTACT_ID = "contactId"
//        const val POSITION_IN_ADAPTER = "position"
        const val ADAPTER_POSITION = "position"

    }
    private lateinit var contact: ContactDataClass
//    private lateinit var recyler: RecyclerView
    private lateinit var recycler: RecyclerView
    private lateinit var phoneNumber:String
//    private var positionOfContactInAdapter:Int = -1
    private var contactPositionInAdapter:Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        contactPositionInAdapter=intent.getIntExtra(ADAPTER_POSITION,-1)
//        initialized as a function
//        recycler = findViewById(R.id.recycler_contacts)
//        recycler.layoutManager = LinearLayoutManager(this)
        initializeRecycler()
        setToolbarAsActionBar()
        onClickListeners()
    }
    private fun initializeRecycler(){
        recycler = findViewById(R.id.recycler_contacts)
        recycler.layoutManager = LinearLayoutManager(this)
    }

    private fun onClickListeners(){
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            // activity closed
            setResult(RESULT_OK,intent)
            finish()
        }
        findViewById<IconWithNameLayout>(R.id.call_layout).setOnClickListener {
            initiatePhoneCall()
        }
        findViewById<View>(R.id.edit_icon).setOnClickListener {
            editContact()
        }
        findViewById<ImageView>(R.id.mail_icon).setOnClickListener {
//            function name change
//            mailAndMessageNotAvailableToast()
            mailAndMessageNotAvailable()
        }
        findViewById<TextView>(R.id.mail_id).setOnClickListener {
            mailAndMessageNotAvailable()
        }
        findViewById<IconWithNameLayout>(R.id.message_layout).setOnClickListener {
            mailAndMessageNotAvailable()
        }
        star.setOnClickListener {
//            addOrRemoveFavorites()
            updateFavorites()
        }

    }

    private fun updateScreen(){
        val callLayout:IconWithNameHandler = findViewById(R.id.call_layout)
        callLayout.setImageAndText(R.drawable.ic_baseline_call_24,getString(R.string.call))
        val messageLayout:IconWithNameHandler = findViewById(R.id.message_layout)
        messageLayout.setImageAndText(R.drawable.ic_baseline_message_24, getString(R.string.message))
        if (contact.favorite) {
            val starIcon:ImageView = findViewById(R.id.star)
            starIcon.setImageResource(R.drawable.fav_star)
        }
        recycler.adapter = PhoneNumberAdapter(contact.number, context = this)
//        fillContactDetailsInLayout()
        fillContactDetails()
        setProfilePictureIfAvailable()
    }

    override fun onBackPressed() {
        setResult(RESULT_OK,intent)
        finish()
    }

    private fun updateFavorites(){
        if ( contact.favorite ) {
            star.setImageResource(R.drawable.star_border)
            contact.favorite = false
        } else {
            star.setImageResource(R.drawable.fav_star)
            contact.favorite = true
        }
        val values=ContentValues()
        values.put(MyContentProvider.FAVORITE,contact.favorite)
        DatabaseFunctionalities.update(values, contact.dbID)
        intent.putExtra("fav",contact.favorite)
    }

    private fun initiatePhoneCall(){
        if (contact.number.isNotEmpty()) {
            phoneNumber = contact.number[0]
            if (PermissionUtils.hasPermission(this, Manifest.permission.CALL_PHONE)) {
                makePhoneCall()
            }
            else {
                requestCallPermission()
            }
        } else {
            Toast.makeText(this, resources.getString(R.string.number_not_available), Toast.LENGTH_SHORT).show()
        }
    }

    private fun editContact(){
        val intent=Intent(this, CreateAndEditContactActivity::class.java)
        intent.putExtra(CreateAndEditContactActivity.OPTION, CreateAndEditContactActivity.EDIT)
        intent.putExtra(CreateAndEditContactActivity.EDIT_CONTACT_KEY,contact.dbID)
        intent.putExtra(ADAPTER_POSITION,contactPositionInAdapter)
        startActivity(intent)
    }

    private fun mailAndMessageNotAvailable(){
        Toast.makeText(this, resources.getString(R.string.on_progress_feature), Toast.LENGTH_SHORT).show()
    }

    private fun setProfilePictureIfAvailable(){
        val imageView = findViewById<ImageView>(R.id.user_ic)
        val cardView = findViewById<CardView>(R.id.custom_profile)
        try {
            val directory = applicationContext.filesDir
            val imageDirectory = File(directory, resources.getString(R.string.image_directory_name))
            val imgFile = File(imageDirectory, "${contact.firstName + contact.lastName}${resources.getString(
                R.string.image_format
            )}")
            if (imgFile.exists()) {
                imageView.setImageDrawable(Drawable.createFromPath(imgFile.toString()))
                cardView.visibility = View.VISIBLE
            }
        } catch (e: IOException){
            cardView.visibility = View.GONE
        }
    }

    private fun setToolbarAsActionBar() {
        val toolbar=findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    private fun fillContactDetails() {
        findViewById<Button>(R.id.contact_icon).text=contact.firstName[0].toString()
        val contactName=contact.firstName+" "+contact.lastName
        findViewById<TextView>(R.id.contact_name).text=contactName
//        val mailField=findViewById<TextView>(R.id.mail_id)
        val mailId=findViewById<TextView>(R.id.mail_id)
        if (contact.email!="") {
            mailId.visibility=View.VISIBLE
            mailId.text = contact.email
        } else {
            mailId.visibility=View.GONE
        }
    }
    fun setPhoneNumber(phoneNumber:String?=null){
        if (phoneNumber!=null){
            this.phoneNumber = phoneNumber
        }
    }
    fun requestCallPermission() {
        if (PermissionUtils.shouldShowRational(this, Manifest.permission.CALL_PHONE)) {
            AlertDialog.Builder(this).setTitle(resources.getString(R.string.call_permission)).setMessage(resources.getString(
                R.string.allow_permission
            ))
                .setPositiveButton(resources.getString(R.string.ok)) { _, _ ->
                    val intent=Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri= Uri.fromParts(resources.getString(R.string.uri_package),packageName,null)
                    intent.data=uri
                    startActivity(intent)
            }.setNegativeButton(resources.getString(R.string.cancel)){ dialogInterface, _ ->
                    dialogInterface.dismiss()
                }.create().show()
        }
        else {
            PermissionUtils.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                PermissionUtils.CALL_PERMISSION_CODE
            )
        }
    }

    override fun onResume() {
        super.onResume()
        getContact()

//  move to function getContact()
//           val dbId=intent.getIntExtra(CONTACT_KEY,-1).toString()
//        GlobalScope.launch(Dispatchers.IO) {
//            if(dbId!="-1") {
//                val cursor= DatabaseFunctionalities.getContact(dbId)
//                withContext(Dispatchers.Main) {
//                    cursor?.moveToFirst()
//                    val data= ContactDataClass.getContact(cursor)
//                    if(data!=null) {
//                        contact=data
////                        updateEntireScreenValues()
//                        updateScreen()
//                    }
//                }
//            }
//        }
    }

    private fun getContact(){
//        val dbId=intent.getIntExtra(CONTACT_KEY,-1).toString()
        val contactDbId=intent.getIntExtra(CONTACT_ID,-1).toString()
        GlobalScope.launch(Dispatchers.IO) {
            if(contactDbId!="-1") {
                val cursor= DatabaseFunctionalities.getContact(contactDbId)
                withContext(Dispatchers.Main) {
                    cursor?.moveToFirst()
                    val data= ContactDataClass.getContact(cursor)
                    if(data!=null) {
                        contact=data
//                        updateEntireScreenValues()
                        updateScreen()
                    }
                }
            }
        }
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if ( requestCode== PermissionUtils.CALL_PERMISSION_CODE && grantResults.isNotEmpty()
            && (grantResults[0]==PackageManager.PERMISSION_GRANTED) ) {
            makePhoneCall()
        }
    }
     private fun makePhoneCall() {
        val phone= "tel:$phoneNumber"
        val intent= Intent(Intent.ACTION_CALL)
        intent.data=Uri.parse(phone)
        startActivity(intent)
    }

//    private val doEditContact =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
//        if (it.resultCode == RESULT_OK) {
//
//        }
//    }
    private fun deleteContact() {
        Toast.makeText(this, resources.getString(R.string.deleted)+"  "+contact.firstName, Toast.LENGTH_SHORT).show()
        DatabaseFunctionalities.delete(contact.dbID.toString())
        setResult(RESULT_OK,intent)
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.contact_details_menu,menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId== R.id.delete) {
            buildConfirmationDeleteContactDialog()
        }
        return super.onOptionsItemSelected(item)
    }
    private fun buildConfirmationDeleteContactDialog(){
        AlertDialog.Builder(this).setTitle(resources.getString(R.string.sure_delete))
            .setMessage(
                resources.getString(
                    R.string.permanent_delete
                )
            )
            .setPositiveButton(resources.getString(R.string.delete)) { _, _ ->
                deleteContact()
                finish()
            }.setNegativeButton(resources.getString(R.string.cancel)) { dialogInterface, _ ->
                dialogInterface.dismiss()
            }.create().show()
    }
}