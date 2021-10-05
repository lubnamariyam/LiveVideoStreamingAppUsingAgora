package com.lubnamariyam.livestreamagora

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.os.Bundle
import io.agora.rtc.Constants;
import io.agora.rtc.IRtcEngineEventHandler;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.video.VideoCanvas;
import androidx.core.app.ActivityCompat
import java.lang.Exception
import java.lang.RuntimeException


class MainActivity : AppCompatActivity() {

    // Fill the App ID of your project generated on Agora Console.
    private val appId = ""

    // Fill the channel name.
    private val channelName = ""

    // Fill the temp token generated on Agora Console.
    private val token = "0067a4c205295ca4e5899a70e94218a0ee9IADvdyXa6lmiiC30g02+of9jHWza/5KWZT4gt8VnPABsdip7Mm4AAAAAEABekxT0JCxUYQEAAQAkLFRh"

    private var mRtcEngine: RtcEngine? = null

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        // Listen for the remote host joining the channel to get the uid of the host.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { // Call setupRemoteVideo to set the remote video view after getting uid from the onUserJoined callback.
                setupRemoteVideo(uid)
            }
        }
    }

    private val PERMISSION_REQ_ID = 22

    private val REQUESTED_PERMISSIONS = arrayOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.CAMERA
    )

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        if (ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, REQUESTED_PERMISSIONS, requestCode)
            return false
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(REQUESTED_PERMISSIONS[0], PERMISSION_REQ_ID) &&
            checkSelfPermission(REQUESTED_PERMISSIONS[1], PERMISSION_REQ_ID)) {
            initializeAndJoinChannel();
        }
    }

    private fun initializeAndJoinChannel() {
        mRtcEngine = try {
            RtcEngine.create(baseContext, appId, mRtcEventHandler)
        } catch (e: Exception) {
            throw RuntimeException("Check the error.")
        }

        // For a live streaming scenario, set the channel profile as BROADCASTING.
        mRtcEngine?.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        // Set the client role as BORADCASTER or AUDIENCE according to the scenario.
        mRtcEngine?.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)

        // By default, video is disabled, and you need to call enableVideo to start a video stream.
        mRtcEngine?.enableVideo()
        val container = findViewById<FrameLayout>(R.id.local_video_view_container)
        // Call CreateRendererView to create a SurfaceView object and add it as a child to the FrameLayout.
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)
        // Pass the SurfaceView object to Agora so that it renders the local video.
        mRtcEngine?.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))

        // Join the channel with a token.
        mRtcEngine?.joinChannel(token, channelName, "", 0)
    }

    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<FrameLayout>(R.id.remote_video_view_container)
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }
    override fun onDestroy() {
        super.onDestroy()
        mRtcEngine!!.leaveChannel()
        RtcEngine.destroy()
    }
}