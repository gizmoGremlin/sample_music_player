package com.pickle.musicapp

import android.app.Activity
import android.content.Intent
import android.database.Cursor

import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.PowerManager
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {


    companion object {
        const val RC_GET_AUDIO_FILE = 0
    }

    lateinit var stopBtn: Button

    lateinit var textViewSelectedSong: TextView
    lateinit var playBtn: Button
    lateinit var songPickerBtn: Button
    lateinit var progressBar: ProgressBar
    lateinit var audioUri: Uri
    lateinit var mediaPlayer: MediaPlayer
    lateinit var txtTitle: TextView
    lateinit var txtAlbum: TextView
    lateinit var txtArtist: TextView
    lateinit var txtDuration: TextView
    lateinit var txtBitrate: TextView
    lateinit var txtMimeType: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        stopBtn = findViewById(R.id.btn_stop)

        textViewSelectedSong = findViewById(R.id.text_view_selected_song)
        playBtn = findViewById(R.id.btn_play)
        songPickerBtn = findViewById(R.id.btn_select_song) as Button
        progressBar = findViewById(R.id.progress_bar)
        txtTitle = findViewById(R.id.txt_title)
        txtAlbum = findViewById(R.id.txt_album)
        txtArtist = findViewById(R.id.txt_artist)
        txtDuration = findViewById(R.id.txt_duration)
        txtBitrate =  findViewById(R.id.txt_bitrate)
        txtMimeType = findViewById(R.id.txt_mimetype)


        songPickerBtn.setOnClickListener {
            getAudioFile(it)
        }

        playBtn.setOnClickListener {

            if (textViewSelectedSong.text != "select a song...") {
                mediaPlayer = MediaPlayer().apply {
                    setWakeMode(applicationContext, PowerManager.PARTIAL_WAKE_LOCK)
                    setDataSource(applicationContext, audioUri)
                    prepare()
                    start()

                }
            }
            stopBtn.setOnClickListener {
                if (mediaPlayer.isPlaying) {
                    mediaPlayer.stop()
                }
            }

        }


    }

    fun getAudioFile(v: View) {


        // Used to tell the framework that we inted to get content
        // And that content will be audio
        val audioIntent: Intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = ("audio/*")
        }


        //Return the Activity component that should be used to handle this intent
        //Class for retrieving various kinds of information related to the application packages that are currently installed on the device.
        if (audioIntent.resolveActivity(packageManager) == null) {
            return
        }

        startActivityForResult(audioIntent, RC_GET_AUDIO_FILE)


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_GET_AUDIO_FILE && resultCode == Activity.RESULT_OK) {
            data.let {
                audioUri = it!!.data

                getAndDisplayMetadata(audioUri)
                val fileName = getFileName(audioUri)
                textViewSelectedSong.setText(fileName)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun getAndDisplayMetadata(uri: Uri?) {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(this, uri)
            val title = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "UnKnown Title"
            val duration =
                (((Integer.parseInt( retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)).toLong())/1000)/60) ?: 1
            val album = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM) ?: "UnKnown Album"
            val artist = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST) ?: "UnKnown Artist"
            val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE) ?: "UnKnown Bitrate"
            val mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE) ?: "UnKnown Mime type"

            txtTitle.text = "Title: $title"
            txtAlbum.text ="Album: $album"
            txtArtist.text = "Artist: $artist"
            txtDuration.text = "Duration: $duration mins"
            txtBitrate.text = "Bitrate: $bitrate"
            txtMimeType.text = "Mime Type: $mimeType"

            Log.d(
                "MetaData",
                "title $title duration ${duration} album $album artist $artist bitrate $bitrate mime $mimeType"
            )

            retriever.release()
        } catch (e: Error) {
            e.printStackTrace()
        }
    }


    private fun getFileName(uri: Uri?): String? {

        var result: String? = null

        if (uri?.scheme.equals("content")) {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null)

            cursor.use {
                if (cursor != null) {
                    if (cursor.moveToFirst())
                        result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }

        }

        if (result == null) {
            result = uri?.path
            val cut = result?.lastIndexOf("/")
            if (cut != -1) {
                result = cut?.plus(1)?.let { result?.substring(it) }
            }
        }
        return result

    }

}
