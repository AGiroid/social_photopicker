package com.example.social_photopicker

import android.R.attr.bitmap
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.get
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.example.social_photopicker.databinding.ActivityHomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.cast.framework.media.ImagePicker
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class HomeActivity : AppCompatActivity() {
    private val homeBinding: ActivityHomeBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_home)
    }
    private var mGoogleSignInClient: GoogleSignInClient? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)
        val account = GoogleSignIn.getLastSignedInAccount(this)

        if (account != null) {
            homeBinding.txtName.text = account.displayName
            homeBinding.txtEmail.text = account.email
        }
        homeBinding.btnSignout.setOnClickListener {
            signOut()
        }
        homeBinding.btnGallery.setOnClickListener {
            val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
            startActivityForResult(intent, 1)
        }
        homeBinding.btnCamera.setOnClickListener {
            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePictureIntent, 2)


        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            if (requestCode == 1) {
                val selectedImageUri: Uri = data?.data!!
                if (selectedImageUri != null) {
                    Glide.with(this)
                        .load(selectedImageUri)
                        .into(homeBinding.imgView) //
                }
            }
            if (requestCode == 2) {
                val photo: Bitmap? = data!!.extras!!["data"] as Bitmap?
                val imageFile = createImageFile()
                saveBitmapToFile(photo, imageFile)

                // Now you can use 'imageFile.path' as the image path
                val imagePath = imageFile.absolutePath
                Log.d("path",imagePath.toString())
                if (photo != null) {
                    val exif = ExifInterface(imagePath)
                    val orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                    val bitmap = BitmapFactory.decodeFile(imagePath)
                    val rotatedBitmap = rotateBitmap(bitmap, orientation)

                    Glide.with(this)
                        .load(rotatedBitmap)
                        .into(homeBinding.imgView)

                }
            }
        }
    }
    private fun rotateBitmap(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
    private fun saveBitmapToFile(bitmap: Bitmap?, file: File) {
        try {
            FileOutputStream(file).use { out ->
                bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, out)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
    }
    fun signOut() {
        mGoogleSignInClient?.signOut()?.addOnCompleteListener(
            this
        ) { Toast.makeText(this@HomeActivity, "Signed Out", Toast.LENGTH_LONG).show() }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}