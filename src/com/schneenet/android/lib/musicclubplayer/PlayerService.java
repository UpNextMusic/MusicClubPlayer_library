package com.schneenet.android.lib.musicclubplayer;

import android.app.Application;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;

import com.schneenet.android.lib.musicclubplayer.common.LocalBroadcastManager;
import com.schneenet.android.lib.musicclubplayer.media.Playable;
import com.schneenet.android.lib.musicclubplayer.media.Playlist;
import com.schneenet.android.lib.musicclubplayer.media.PlaylistManager;

public class PlayerService extends Service {

	// Meta Update Action
	public static final String ACTION_META_UPDATE = "com.schneenet.android.lib.musicclubplayer.ACTION_META_UPDATE";
	public static final String EXTRA_META_TITLE = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_TITLE";
	public static final String EXTRA_META_ARTIST = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_ARTIST";
	public static final String EXTRA_META_ALBUM = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_ALBUM";
	public static final String EXTRA_META_TRACK = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_TRACK";
	public static final String EXTRA_META_TRACKOF = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_TRACKOF";
	public static final String EXTRA_META_ALBUMART = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_ALBUMART";
	public static final String EXTRA_META_ALBUMART_URL = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_ALBUMART_URL";
	public static final String EXTRA_META_SHUFFLE = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_SHUFFLE";
	public static final String EXTRA_META_REPEAT = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_REPEAT";
	public static final String EXTRA_META_RUNNING = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_RUNNING";
	public static final String EXTRA_META_PLAYING = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_PLAYING";
	public static final String EXTRA_META_PLAYLIST = "com.schneenet.android.lib.musicclubplayer.EXTRA_META_PLAYLIST";

	// Progress Update Action
	public static final String ACTION_PROGRESS_UPDATE = "com.schneenet.android.lib.musicclubplayer.ACTION_PROGRESS_UPDATE";
	public static final String EXTRA_PROGRESS_VALUE = "com.schneenet.android.lib.musicclubplayer.EXTRA_PROGRESS_VALUE";
	public static final String EXTRA_PROGRESS_BUFFERED = "com.schneenet.android.lib.musicclubplayer.EXTRA_PROGRESS_BUFFERED";
	public static final String EXTRA_PROGRESS_DURATION = "com.schneenet.android.lib.musicclubplayer.EXTRA_PROGRESS_DURATION";

	// Player Control Actions
	public static final String ACTION_CONTROL_PLAYPAUSE = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_PLAYPAUSE";
	public static final String ACTION_CONTROL_PLAY = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_PLAY";
	public static final String ACTION_CONTROL_PREV = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_PREV";
	public static final String ACTION_CONTROL_NEXT = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_NEXT";
	public static final String ACTION_CONTROL_TOGGLESHUFFLE = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_TOGGLESHUFFLE";
	public static final String ACTION_CONTROL_TOGGLEREPEAT = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_TOGGLEREPEAT";
	public static final String ACTION_CONTROL_REQUESTSEEK = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_REQUESTSEEK";
	public static final String EXTRA_CONTROL_SEEKVALUE = "com.schneenet.android.lib.musicclubplayer.EXTRA_CONTROL_SEEKVALUE";
	public static final String ACTION_CONTROL_FORCEUPDATE = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_FORCEUPDATE";
	public static final String ACTION_CONTROL_ENQUEUE = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_ENQUEUE";
	public static final String ACTION_CONTROL_SETPLAYLIST = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_SETPLAYLIST";
	public static final String ACTION_CONTROL_PLAY_TRACK = "com.schneenet.android.lib.musicclubplayer.EXTRA_CONTROL_PLAY_TRACK";
	public static final String EXTRA_CONTROL_ENQUEUE_SONGS = "com.schneenet.android.lib.musicclubplayer.EXTRA_CONTROL_ENQUEUE_SONGS";
	public static final String EXTRA_CONTROL_STARTINDEX = "com.schneenet.android.lib.musicclubplayer.EXTRA_CONTROL_STARTINDEX";
	public static final String EXTRA_CONTROL_ENQUEUE_TYPE = "com.schneenet.android.lib.musicclubplayer.EXTRA_CONTROL_ENQUEUE_TYPE";
	public static final String EXTRA_CONTROL_ENQUEUE_UID = "com.schneenet.android.lib.musicclubplayer.EXTRA_CONTROL_ENQUEUE_UID";
	public static final String ACTION_CONTROL_SHUTDOWN = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_SHUTDOWN";
	public static final String ACTION_CONTROL_PLAYLIST_ITEM_DELETE = "com.schneenet.android.lib.musicclubplayer.ACTION_CONTROL_PLAYLIST_ITEM_DELETE";
	public static final String EXTRA_CONTROL_ITEM_POSITION = "com.schneenet.android.lib.musicclubplayer.EXTRA_CONTROL_ITEM_POSITION";

	// Settings relevant to the Player Service
	public static final String PREFS_LAST_PLAYLIST = "com.schneenet.android.lib.musicclubplayer.PlayerService.PREFS_LAST_PLAYLIST";
	public static final String PREFS_REPEAT_STATE = "com.schneenet.android.lib.musicclubplayer.PlayerService.PREFS_REPEAT_STATE";
	public static final String PREFS_SHUFFLE_STATE = "com.schneenet.android.lib.musicclubplayer.PlayerService.PREFS_SHUFFLE_STATE";
	public static final String PREFS_PLAYER_MEDIABUTTONS = "com.schneenet.android.lib.musicclubplayer.PlayerService.PREFS_PLAYER_MEDIABUTTONS";

	private boolean running;
	private boolean phonePaused = false;
	private boolean autostart = true;

	private MediaPlayer mp = new MediaPlayer();
	private Playlist mPlaylist;
	private int playlistIndex; // ONLY FOR USE WITH MOVING BETWEEN SONGS, DO NOT
								// DISPLAY!
	private Playable nowPlaying; // Use for displaying metadata, use this to find
								// the actual playlist index

	private String songName;
	private String songArtist;
	private String songAlbum;
	private String albumArt;
	private Bitmap albumArtBitmap = null;

	private float position;
	private float duration;
	private float buffered;

	private static final String TAG = "PlayerService";

	private LocalBroadcastManager mLocalBroadcastManager;
	private MusicClubPlayerApplication mApplicationObj;
	private TelephonyManager phoneManager;
	private AudioManager mAudioManager;
	private ComponentName mMediaButtonReceiver;
	private Handler mProgressHandler;
	private ProgressListener pListener;
	private PlayerController controller;
	private PowerManager.WakeLock wakeLock;
	private PlaylistManager mPlaylistManager;

	private boolean repeatAll = false;
	private boolean shufflePL = false;

	private boolean useMediaButtons = false;
	
	@Override
	public IBinder onBind(Intent intent) {
		// This service is controlled by Broadcasts and a LocalBroadcastManager
		return null;
	}

	public void onCreate() {
		super.onCreate();

		Log.d(TAG, "Creating PlayerService...");

		// Get a reference to the application object, it SHOULD inherit the MusicClubPlayerApplication abstract class
		Application appObj = getApplication();
		if (appObj instanceof MusicClubPlayerApplication)
		{
			mApplicationObj = (MusicClubPlayerApplication) appObj; 
		}
		else
		{
			throw new ClassCastException("Application class must inherit from MusicClubPlayerApplication to use this service.");
		}
		
		// Get a reference to the LocalBroadcastManager
		mLocalBroadcastManager = mApplicationObj.getLocalBroadcastManager();

		// Get a PlaylistManager
		mPlaylistManager = mApplicationObj.getPlaylistManager();

		// Read old state data from prefs
		repeatAll = mApplicationObj.getPreferenceBoolean(PlayerService.PREFS_REPEAT_STATE, false);
		shufflePL = mApplicationObj.getPreferenceBoolean(PlayerService.PREFS_SHUFFLE_STATE, false);

		// Initially we are not running
		running = false;

		// Set up the headset plug listener
		IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
		registerReceiver(new HeadsetMonitor(), filter);

		// Set up the phone state listener
		phoneManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
		phoneManager.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

		// Set up the audio manager and media button event listener
		mAudioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		mMediaButtonReceiver = new ComponentName(getPackageName(), PlayerController.class.getName());
		mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiver);

		// Set up the player service controller
		controller = new PlayerController();
		mApplicationObj.registerOnSharedPreferenceChangeListener(controller);

		// Begin pinging the progress of playback
		mProgressHandler = new Handler();
		pListener = new ProgressListener();

		// Prepare wake lock
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG);

		// Set up primary route for controller (for in-app control)
		IntentFilter controlFilter = new IntentFilter();
		controlFilter.addAction(PlayerService.ACTION_CONTROL_PLAYPAUSE);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_PLAY);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_NEXT);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_PREV);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_REQUESTSEEK);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_TOGGLEREPEAT);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_TOGGLESHUFFLE);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_ENQUEUE);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_SETPLAYLIST);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_PLAY_TRACK);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_FORCEUPDATE);
		controlFilter.addAction(PlayerService.ACTION_CONTROL_SHUTDOWN);
		controlFilter.addAction(Intent.ACTION_MEDIA_BUTTON);
		mLocalBroadcastManager.registerReceiver(controller, controlFilter);

		// Set up secondary route for controller (for widgets, etc.)
		IntentFilter secondaryFilter = new IntentFilter();
		secondaryFilter.addAction(PlayerService.ACTION_CONTROL_PLAYPAUSE);
		secondaryFilter.addAction(PlayerService.ACTION_CONTROL_NEXT);
		secondaryFilter.addAction(PlayerService.ACTION_CONTROL_PREV);
		secondaryFilter.addAction(PlayerService.ACTION_CONTROL_FORCEUPDATE);
		secondaryFilter.addAction(PlayerService.ACTION_CONTROL_SHUTDOWN);
		registerReceiver(controller, secondaryFilter);

		// Finish Preparation
		loadLastPlaylist();
		updateMetadata();
	}

	public void onDestroy() {
		pListener.stop();
		wakeLock.release();
		mp.stop();
		mp.release();
		shutdown();
		mLocalBroadcastManager.unregisterReceiver(controller);
		unregisterReceiver(controller);
		super.onDestroy();
	}

	private void preparePlayer(Playable song) {
		try {
			mp.reset();
			mp.setDataSource(song.getUrl());
			mp.prepareAsync();
		} catch (Exception ex) {
			Log.e(TAG, "Error while preparing player: ", ex);
		}
	}

	private void playSong(final Playable s) {
		try {
			wakeLock.acquire();
			nowPlaying = s;
			albumArtBitmap = null;
			updateMetadata();
			mp.setOnErrorListener(new OnErrorListener() {

				public boolean onError(MediaPlayer mp, int what, int extra) {
					Log.e(TAG, "Media Player Error: (" + what + ", " + extra + ")");
					running = false;
					preparePlayer(s);
					return true;
				}
			});
			mp.setOnCompletionListener(new OnCompletionListener() {

				public void onCompletion(MediaPlayer m) {
					wakeLock.release();
					nextSong();
				}
			});
			mp.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {

				public void onBufferingUpdate(MediaPlayer mp, int percent) {
					buffered = ((float) percent / 100.0f) * duration;
					sendProgress();
				}
			});
			mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

				public void onPrepared(MediaPlayer mp) {
					startPlayer(autostart);
				}
			});
			buffered = 0;
			position = 0;
			duration = nowPlaying.getLength();
			sendProgress();
			preparePlayer(s);
		} catch (Exception e) {
			Log.e(TAG, "Exception while trying to play file", e);
		}
	}

	private void pausePlayer() {
		if (running) {
			mp.pause();

			// Inform the notification that we are now paused
			updateMetadata();
			stopForeground(false);
			wakeLock.release();
		}
	}

	private void playPlayer() {
		autostart = true;
		startPlayer(autostart);
	}

	private void startPlayer(boolean play) {
		if (play) {
			running = true;
			mp.start();
			pListener.start();
		}
		updateMetadata();
		if (play) {
			wakeLock.acquire();
		}

		// TODO Test Bluetooth controls
		
		// Player is now playing music
		registerRemoteControl();
	}
	
	private void stopPlayer() {
		// Reset
		mp.reset();
		running = false;
		
		updateMetadata();
		if (wakeLock.isHeld()) {
			wakeLock.release();
		}
	}

	private void sendProgress() {
		Intent progressUpdate = new Intent();
		progressUpdate.setAction(PlayerService.ACTION_PROGRESS_UPDATE);
		progressUpdate.putExtra(PlayerService.EXTRA_PROGRESS_VALUE, position);
		progressUpdate.putExtra(PlayerService.EXTRA_PROGRESS_BUFFERED, buffered);
		progressUpdate.putExtra(PlayerService.EXTRA_PROGRESS_DURATION, duration);
		mLocalBroadcastManager.sendBroadcast(progressUpdate);
	}

	private void updateMetadata() {
		if (mPlaylist.size() > 0 && playlistIndex < mPlaylist.size() && nowPlaying != null) {

			songName = nowPlaying.getName();
			songArtist = nowPlaying.getArtist();
			songAlbum = nowPlaying.getAlbum();
			albumArt = mApplicationObj.getArtUrl(nowPlaying);
			mApplicationObj.fetchArtImage(albumArt, new MusicClubPlayerApplication.ArtLoaderCallback() {

				@Override
				public void onArtLoaded(Bitmap bitmap) {
					albumArtBitmap = bitmap;
					sendMetadataBroadcast();
				}
			});

		} else {
			songName = "No Media";
			songArtist = "";
			songAlbum = "";
		}
		sendMetadataBroadcast();
	}

	private void sendMetadataBroadcast() {
		Intent metaUpdate = new Intent();
		metaUpdate.setAction(PlayerService.ACTION_META_UPDATE);
		metaUpdate.putExtra(PlayerService.EXTRA_META_TITLE, this.songName == null ? "" : this.songName);
		metaUpdate.putExtra(PlayerService.EXTRA_META_ARTIST, this.songArtist == null ? "" : this.songArtist);
		metaUpdate.putExtra(PlayerService.EXTRA_META_ALBUM, this.songAlbum == null ? "" : this.songAlbum);
		metaUpdate.putExtra(PlayerService.EXTRA_META_ALBUMART_URL, albumArt);
		metaUpdate.putExtra(PlayerService.EXTRA_META_ALBUMART, albumArtBitmap);
		metaUpdate.putExtra(PlayerService.EXTRA_META_TRACK, mPlaylist.getTrackNumber(nowPlaying));
		metaUpdate.putExtra(PlayerService.EXTRA_META_TRACKOF, mPlaylist.size());
		metaUpdate.putExtra(PlayerService.EXTRA_META_REPEAT, repeatAll);
		metaUpdate.putExtra(PlayerService.EXTRA_META_SHUFFLE, shufflePL);
		metaUpdate.putExtra(PlayerService.EXTRA_META_RUNNING, running);
		metaUpdate.putExtra(PlayerService.EXTRA_META_PLAYING, running && mp.isPlaying());
		// TODO metaUpdate.putExtra(PlayerService.EXTRA_META_PLAYLIST_TYPE, mPlaylist.getType());
		// TODO metaUpdate.putExtra(PlayerService.EXTRA_META_PLAYLIST_UID, mPlaylist.getUid());
		metaUpdate.putExtra(PlayerService.EXTRA_META_PLAYLIST, mPlaylistManager.serializePlaylist(mPlaylist));
		mLocalBroadcastManager.sendBroadcast(metaUpdate);
	}

	private void nextSong() {
		if (mPlaylist.size() > 0) {
			playlistIndex++;
			if (playlistIndex > mPlaylist.size() - 1) {
				playlistIndex = 0;
				if (!repeatAll) {
					return;
				}
			}
			playSong(mPlaylist.getTrack(playlistIndex, shufflePL));
		}
	}

	private void prevSong() {
		if (mPlaylist.size() > 0) {
			playlistIndex--;
			if (playlistIndex < 0) {
				if (repeatAll)
					playlistIndex = mPlaylist.size() - 1;
				else
					playlistIndex = 0;
			}
			playSong(mPlaylist.getTrack(playlistIndex, shufflePL));
		}
	}

	// Called by the PlayerController when the user queues in a new playlist.
	// NOTE: Setting a NEW playlist* will stop the player if it is currently
	// playing and restart it at the beginning of the track at startIndex in the newPlaylist
	// Use addToPlaylist() to avoid this.
	// * Also, if the start index is not the current index, it will
	// automatically restart at the new index.
	private void setPlaylist(Playlist newPlaylist, boolean as, int startIndex) {
		if ((!mPlaylist.isSamePlaylist(newPlaylist) || startIndex != playlistIndex) && newPlaylist.size() > 0) {
			// Playlist is different, or we are starting with a different index
			
			// Stop currently playing
			stopPlayer();
			
			// Set playlist to newPlaylist
			mPlaylist = newPlaylist;
			saveLastPlaylist();

			// If either of the above actions happen, do this:
			playTrackAt(as, startIndex);

		} else {
			Log.i(TAG, "Same playlists or newPlaylist was empty!");
		}
	}

	// Just add the songs, it is up to the activities to start playback.
	private void addToPlaylist(Playlist moreSongs) {
		mPlaylist.append(moreSongs);
		sendMetadataBroadcast();
	}

	// Jump to the song at the index and play it
	private void playTrackAt(boolean as, int startIndex) {
		nowPlaying = mPlaylist.getTrack(startIndex, false);
		if (nowPlaying != null) {
			playlistIndex = mPlaylist.getTrackPosition(nowPlaying, shufflePL);
			updateMetadata();
			autostart = as;
			playSong(nowPlaying);
		}
	}

	private boolean toggleRepeat() {
		repeatAll = !repeatAll;
		updateMetadata();
		mApplicationObj.setPreferenceBoolean(PlayerService.PREFS_REPEAT_STATE, repeatAll);
		return repeatAll;
	}

	private boolean toggleShuffle() {
		shufflePL = !shufflePL;
		playlistIndex = mPlaylist.getTrackPosition(nowPlaying, shufflePL);
		updateMetadata();
		mApplicationObj.setPreferenceBoolean(PlayerService.PREFS_SHUFFLE_STATE, shufflePL);
		return shufflePL;
	}

	// Toggles between playing and paused
	private void playPause() {
		if (mp.isPlaying()) {
			pausePlayer();
		} else {
			playPlayer();
		}
	}

	// Save the NowPlaying to a JSON file
	private void saveLastPlaylist() {
		mApplicationObj.setPreferenceString(PREFS_LAST_PLAYLIST, mPlaylistManager.serializePlaylist(mPlaylist));
	}

	// Load the NowPlaing from a JSON file
	private void loadLastPlaylist() {
		Playlist pl = mPlaylistManager.deserializePlaylist(mApplicationObj.getPreferenceString(PREFS_LAST_PLAYLIST, ""));
		if (pl != null) {
			setPlaylist(pl, false, 0);
		}
	}

	// Shut the service down
	private void shutdown() {
		try {
			mp.stop();
		} catch (Exception ex) {
			
		}
		wakeLock.release();
		unregisterRemoteControl();
		stopForeground(true);
		running = false;
		buffered = 0;
		position = 0;
		duration = nowPlaying.getLength();
		updateMetadata();
		sendProgress();
		
	}

	private void registerRemoteControl() {
		mAudioManager.registerMediaButtonEventReceiver(mMediaButtonReceiver);
	}

	private void unregisterRemoteControl() {
		mAudioManager.unregisterMediaButtonEventReceiver(mMediaButtonReceiver);
	}

	public class HeadsetMonitor extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			Bundle extras = intent.getExtras();
			if (action.equals(Intent.ACTION_HEADSET_PLUG)) {
				int state = extras.getInt("state");
				String inOrOut = (state == 1) ? "plugged in" : "unplugged";
				Log.i(TAG, "A headset was " + inOrOut + " while the PlayerService was running.");
				try {
					if (state == 0 && running && mp.isPlaying()) {
						pausePlayer();
					}
				} catch (Exception ex) {
					Log.e(TAG, "Generic exception caught in HeadsetMonitor.onReceive()", ex);
				}
			}
		}
	}

	private PhoneStateListener phoneListener = new PhoneStateListener() {

		public void onCallStateChanged(int phoneState, String incomingNumber) {
			switch (phoneState) {
				case TelephonyManager.CALL_STATE_RINGING:
				case TelephonyManager.CALL_STATE_OFFHOOK:
					if (running && mp.isPlaying()) {
						pausePlayer();
						phonePaused = true;
					}
					break;
				case TelephonyManager.CALL_STATE_IDLE:
					if (running && phonePaused) {
						playPlayer();
						phonePaused = false;
					}
					break;
				default:
					break;
			}
		}
	};

	public class ProgressListener implements Runnable {

		public void stop() {
			mProgressHandler.removeCallbacks(this);
		}

		public void start() {
			mProgressHandler.postDelayed(this, 100);
		}

		public void run() {
			if (running && mp.isPlaying()) {
				position = mp.getCurrentPosition() / 1000f;
				sendProgress();
				start();
			}
		}
	}

	/**
	 * class PlayerController
	 * Receives messages from the LocalBroadcastManager and controls the player
	 * 
	 * @author Matt Schneeberger
	 * 
	 */
	public class PlayerController extends BroadcastReceiver implements SharedPreferences.OnSharedPreferenceChangeListener {

		@Override
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
			Log.d(TAG, "Service received action: " + action);
			if (PlayerService.ACTION_CONTROL_PLAYPAUSE.equals(action)) {
				playPause();
			} else if (action.equals(PlayerService.ACTION_CONTROL_PLAY)) {
				playPlayer();
			} else if (action.equals(PlayerService.ACTION_CONTROL_NEXT)) {
				nextSong();
			} else if (action.equals(PlayerService.ACTION_CONTROL_PREV)) {
				prevSong();
			} else if (action.equals(PlayerService.ACTION_CONTROL_FORCEUPDATE)) {
				Log.d(TAG, "Manual update requested...");
				updateMetadata();
			} else if (action.equals(PlayerService.ACTION_CONTROL_ENQUEUE)) {
				final Playlist playlist = mPlaylistManager.deserializePlaylist(intent.getStringExtra(PlayerService.EXTRA_CONTROL_ENQUEUE_SONGS)); 
				addToPlaylist(playlist);
			} else if (action.equals(PlayerService.ACTION_CONTROL_SETPLAYLIST)) {
				final Playlist playlist = mPlaylistManager.deserializePlaylist(intent.getStringExtra(PlayerService.EXTRA_CONTROL_ENQUEUE_SONGS));
				final int startIndex = intent.getIntExtra(PlayerService.EXTRA_CONTROL_STARTINDEX, 0);
				setPlaylist(playlist, true, startIndex);
			} else if (action.equals(PlayerService.ACTION_CONTROL_PLAY_TRACK)) {
				final int startIndex = intent.getIntExtra(PlayerService.EXTRA_CONTROL_STARTINDEX, 0);
				stopPlayer();
				playTrackAt(true, startIndex);
			} else if (action.equals(PlayerService.ACTION_CONTROL_REQUESTSEEK)) {
				final int pos = intent.getIntExtra(PlayerService.EXTRA_CONTROL_SEEKVALUE, 0);
				Log.d(TAG, "Seek requested. Going to: " + pos);
				if (running)
					mp.seekTo(pos * 1000);
			} else if (action.equals(PlayerService.ACTION_CONTROL_TOGGLEREPEAT)) {
				toggleRepeat();
			} else if (action.equals(PlayerService.ACTION_CONTROL_TOGGLESHUFFLE)) {
				toggleShuffle();
			} else if (action.equals(PlayerService.ACTION_CONTROL_SHUTDOWN)) {
				shutdown();
			} else if (Intent.ACTION_MEDIA_BUTTON.equals(action)) {
				final KeyEvent e = intent.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				final int code = e.getKeyCode();
				switch (code) {
					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
						playPause();
						break;
					case KeyEvent.KEYCODE_MEDIA_NEXT:
						nextSong();
						break;
					case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
						prevSong();
						break;
				}
			}
		}

		public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
			if (PlayerService.PREFS_PLAYER_MEDIABUTTONS.equals(key)) {
				// Code disabled until the problem is fixed
				useMediaButtons = sharedPreferences.getBoolean(PlayerService.PREFS_PLAYER_MEDIABUTTONS, false);
				if (useMediaButtons)
					registerRemoteControl();
				else
					unregisterRemoteControl();
			}
		}
	}

	/*
	 * Static Intent Methods
	 */

	public static Intent getPlayPause() {
		Intent intent = new Intent();
		intent.setAction(PlayerService.ACTION_CONTROL_PLAYPAUSE);
		return intent;
	}

	public static Intent getPrev() {
		Intent intent = new Intent();
		intent.setAction(PlayerService.ACTION_CONTROL_PREV);
		return intent;
	}

	public static Intent getNext() {
		Intent intent = new Intent();
		intent.setAction(PlayerService.ACTION_CONTROL_NEXT);
		return intent;
	}

	public static Intent getToggleRepeat() {
		Intent intent = new Intent();
		intent.setAction(PlayerService.ACTION_CONTROL_TOGGLEREPEAT);
		return intent;
	}

	public static Intent getToggleShuffle() {
		Intent intent = new Intent();
		intent.setAction(PlayerService.ACTION_CONTROL_TOGGLESHUFFLE);
		return intent;
	}

	public static Intent getShutdown() {
		Intent intent = new Intent();
		intent.setAction(PlayerService.ACTION_CONTROL_SHUTDOWN);
		return intent;
	}

}
