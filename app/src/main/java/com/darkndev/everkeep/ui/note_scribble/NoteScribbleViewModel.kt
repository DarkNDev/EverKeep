package com.darkndev.everkeep.ui.note_scribble

import android.Manifest
import android.app.Application
import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.darkndev.everkeep.R
import com.darkndev.everkeep.database.NoteDao
import com.darkndev.everkeep.models.Note
import com.darkndev.everkeep.utils.checkPermission
import com.darkndev.everkeep.utils.sdkVersion29AndAbove
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class NoteScribbleViewModel @Inject constructor(
    private val noteDao: NoteDao,
    state: SavedStateHandle,
    private val application: Application,
) : ViewModel() {

    //permission
    var writePermissionGranted = false

    //save states
    private val note = state.get<Note>("NOTE")!!
    var imageArray = state.get<ByteArray>("IMAGE_ARRAY") ?: note.imageArray

    //user requests
    fun updateScribble(bitmap: Bitmap) = viewModelScope.launch(Dispatchers.IO) {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        imageArray = outputStream.toByteArray()
        noteDao.updateScribble(note.id, outputStream.toByteArray())
    }

    fun deleteScribble() = viewModelScope.launch(Dispatchers.IO) {
        noteDao.updateScribble(note.id, byteArrayOf())
        scribbleEventChannel.send(ScribbleEvent.NavigateWithMessage("Note Scribble Deleted"))
    }

    fun checkWritePermissionAndSaveScribble(bitmap: Bitmap) = viewModelScope.launch(Dispatchers.IO) {
        writePermissionGranted = sdkVersion29AndAbove {
            true
        } ?: checkPermission(
            application,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        if (writePermissionGranted) {
            saveScribbleToStorage(bitmap)
        } else {
            scribbleEventChannel.send(ScribbleEvent.CheckWritePermission)
        }
    }

    private fun saveScribbleToStorage(bitmap: Bitmap) = viewModelScope.launch(Dispatchers.IO) {
        val imageCollection = sdkVersion29AndAbove {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } ?: MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val contentValues = ContentValues().apply {
            put(
                MediaStore.Images.Media.DISPLAY_NAME,
                "${application.getString(R.string.app_name)}_${System.currentTimeMillis()}_${note.id}"
            )
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, bitmap.width)
            put(MediaStore.Images.Media.HEIGHT, bitmap.height)
            sdkVersion29AndAbove {
                put(
                    MediaStore.Images.Media.RELATIVE_PATH,
                    Environment.DIRECTORY_PICTURES + File.separator + application.getString(R.string.app_name)
                )
            }
        }
        application.apply {
            contentResolver.insert(imageCollection, contentValues)?.also { uri ->
                contentResolver.openOutputStream(uri).use { outputStream ->
                    if (outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) } == true) {
                        scribbleEventChannel.send(ScribbleEvent.ShowMessage("Scribble saved successfully"))
                    } else {
                        throw IOException("Couldn't save bitmap")
                    }
                }
            } ?: throw IOException("Couldn't create MediaStore entry")
        }
    }

    fun shareScribble(bitmap: Bitmap) = viewModelScope.launch(Dispatchers.IO) {
        val imagePath = File(application.cacheDir, "scribbles")
        imagePath.mkdirs()
        val image = File(imagePath, "${note.id}.jpg")
        val uri = FileProvider.getUriForFile(
            application,
            "com.darkndev.everkeep.provider",
            image
        )
        application.contentResolver.openOutputStream(uri).use { outputStream ->
            if (outputStream?.let { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) } == true) {
                scribbleEventChannel.send(ScribbleEvent.ShareScribble(uri))
            } else {
                throw IOException("Couldn't save bitmap")
            }
        }
    }

    //Events
    private val scribbleEventChannel = Channel<ScribbleEvent>()
    val scribbleEvent = scribbleEventChannel.receiveAsFlow()

    sealed class ScribbleEvent {
        data class NavigateWithMessage(val message: String) : ScribbleEvent()
        object CheckWritePermission : ScribbleEvent()
        data class ShowMessage(val message: String) : ScribbleEvent()
        data class ShareScribble(val scribbleUri: Uri) : ScribbleEvent()
    }
}