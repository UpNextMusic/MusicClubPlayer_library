package com.schneenet.android.lib.musicclubplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.RejectedExecutionException;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.schneenet.android.lib.musicclubplayer.albumart.AlbumArtLoader;
import com.schneenet.android.lib.musicclubplayer.albumart.AlbumArtRequest;
import com.schneenet.android.lib.musicclubplayer.albumart.AlbumArtResponse;
import com.schneenet.android.lib.musicclubplayer.common.LocalBroadcastManager;
import com.schneenet.android.lib.musicclubplayer.media.Playable;
import com.schneenet.android.lib.musicclubplayer.media.Playlist;
import com.schneenet.android.lib.musicclubplayer.media.PlaylistManager;

/**
 * 
 * MusicClubPlayerApplication class
 * 
 * <b>Important!</b> You must extend this class and use it's functionality if you want the PlayerService to function properly. This class contains required functionality to integrate the PlayerService with the Android application life cycle and your Application!
 * 
 * @author Matt Schneeberger
 *
 */
public abstract class MusicClubPlayerApplication extends Application {
	
	private LocalBroadcastManager mLocalBroadcastManager;
	private SharedPreferences mPrefs;
	private ArrayList<PlayerObserver> mObservers;
	private PlaylistManager mPlaylistManager;
	
	private BroadcastReceiver mObserverReceiver = new BroadcastReceiver()
	{
			
		@Override
		public void onReceive(Context context, Intent intent)
		{
			final String action = intent.getAction();
			if (action.equals(PlayerService.ACTION_META_UPDATE))
			{
				final String title = intent.getStringExtra(PlayerService.EXTRA_META_TITLE);
				final String artist = intent.getStringExtra(PlayerService.EXTRA_META_ARTIST);
				final String album = intent.getStringExtra(PlayerService.EXTRA_META_ALBUM);
				final String albumArtUrl = intent.getStringExtra(PlayerService.EXTRA_META_ALBUMART_URL);
				final String track = String.valueOf(intent.getIntExtra(PlayerService.EXTRA_META_TRACK, 0));
				final String trackOf = String.valueOf(intent.getIntExtra(PlayerService.EXTRA_META_TRACKOF, 0));
				final boolean repeat = intent.getBooleanExtra(PlayerService.EXTRA_META_REPEAT, false);
				final boolean shuffle = intent.getBooleanExtra(PlayerService.EXTRA_META_SHUFFLE, false);
				final boolean playing = intent.getBooleanExtra(PlayerService.EXTRA_META_PLAYING, false);
				final Playlist playlist = mPlaylistManager.deserializePlaylist(intent.getStringExtra(PlayerService.EXTRA_META_PLAYLIST));
				for ( PlayerObserver obs : mObservers )
				{
					obs.onMetaDataUpdate(title, artist, album, albumArtUrl, track, trackOf, playing, repeat, shuffle, playlist);
				}
			}
			else if (action.equals(PlayerService.ACTION_PROGRESS_UPDATE))
			{
				final float position = intent.getFloatExtra(PlayerService.EXTRA_PROGRESS_VALUE, 0);
				final float buffered = intent.getFloatExtra(PlayerService.EXTRA_PROGRESS_BUFFERED, 0);
				final float duration = intent.getFloatExtra(PlayerService.EXTRA_PROGRESS_DURATION, 0);
				for ( PlayerObserver obs : mObservers )
				{
					obs.onProgressChanged(position, buffered, duration);
				}
			}
		}
	};
	
	/**
	 * Method called on application start.
	 * 
	 * <b>IMPORTANT</b>: You must override this method, you must set mPlaylistManager to your own implementation, and then you must make a call to super.onCreate()!
	 * <pre>
	 * @Override
	 * public void onCreate() {
	 *     mPlaylistManager = new MyPlaylistManager();
	 *     super.onCreate();
	 *     // ... continue application set up
	 * }
	 * </pre>
	 */
	public void onCreate() {
		super.onCreate();
		
		if (mPlaylistManager == null)
		{
			throw new RuntimeException("Application did not define a PlaylistManager implementation.");
		}
		
		mLocalBroadcastManager = LocalBroadcastManager.getInstance(this);
		mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
		mObservers = new ArrayList<PlayerObserver>();
		
		// Register to receive broadcast events from the PlayerService to send to PlayerObservers
		IntentFilter filter = new IntentFilter();
		filter.addAction(PlayerService.ACTION_META_UPDATE);
		filter.addAction(PlayerService.ACTION_PROGRESS_UPDATE);
		registerLocalBroadcastReceiver(mObserverReceiver, filter);
		
		// Go ahead and start the PlayerService here
		ensureServiceStarted();
	}
	
	/**
	 * Request a metadata update from the PlayerService
	 */
	public final void requestMetadataUpdate()
	{
		Intent updateIntent = new Intent();
		updateIntent.setAction(PlayerService.ACTION_CONTROL_FORCEUPDATE);
		mLocalBroadcastManager.sendBroadcast(updateIntent);
	}
	
	/**
	 * Send a command to the PlayerService
	 * @param command Command to send
	 */
	public final void sendCommand(String command)
	{
		sendCommand(command, null);
	}
	
	/**
	 * Send a command to the PlayerService
	 * @param command Command to send
	 * @param arguments Extras to send with the command
	 */
	public final void sendCommand(String command, Bundle arguments)
	{
		Intent commandIntent = new Intent();
		commandIntent.setAction(command);
		if (arguments != null)
			commandIntent.putExtras(arguments);
		mLocalBroadcastManager.sendBroadcast(commandIntent);
	}

	/**
	 * Add a list of tracks to the currently playing playlist
	 * @param tracks List of tracks to add
	 */
	public final void enqueueTracks(Playlist tracks) {

		// ENSURE the service is started
		ensureServiceStarted();
		
		// SEND Proper Intent to PlayerService with list of tracks
		Intent enqueueIntent = new Intent();
		enqueueIntent.setAction(PlayerService.ACTION_CONTROL_ENQUEUE);
		enqueueIntent.putExtra(PlayerService.EXTRA_CONTROL_ENQUEUE_SONGS, mPlaylistManager.serializePlaylist(tracks));
		mLocalBroadcastManager.sendBroadcast(enqueueIntent);

	}
	
	/**
	 * Remove a track from the current playlist in the PlaylistService
	 * @param position Remove the track at this position
	 */
	public final void removeFromPlaylist(int position)
	{
		// ENSURE Service is running
		ensureServiceStarted();
		
		// SEND Proper Intent to PlayerServce
		Intent deleteIntent = new Intent();
		deleteIntent.setAction(PlayerService.ACTION_CONTROL_PLAYLIST_ITEM_DELETE);
		deleteIntent.putExtra(PlayerService.EXTRA_CONTROL_ITEM_POSITION, position);
		mLocalBroadcastManager.sendBroadcast(deleteIntent);
	}

	/**
	 * Play a list of tracks in the player
	 * @param tracks List of tracks to replace the current playlist with
	 * @param startingIndex Index of first song to start playing
	 * @param launchPlayer Should we launch the Player UI?
	 */
	public final void playTracks(Playlist tracks, int startingIndex, boolean launchPlayer) {
		// ENSURE Service is running
		ensureServiceStarted();

		// SEND Proper Intent to PlayerService with list of tracks
		Intent enqueueIntent = new Intent();
		enqueueIntent.setAction(PlayerService.ACTION_CONTROL_SETPLAYLIST);
		enqueueIntent.putExtra(PlayerService.EXTRA_CONTROL_STARTINDEX, startingIndex);
		enqueueIntent.putExtra(PlayerService.EXTRA_CONTROL_ENQUEUE_SONGS, mPlaylistManager.serializePlaylist(tracks));
		mLocalBroadcastManager.sendBroadcast(enqueueIntent);

		if (launchPlayer) {
			// LAUNCH PlayerActivity
			launchPlayer();
		}
	}

	/**
	 * Play the song at the startingIndex in the currently playing playlist
	 * @param startingIndex The index of the song to play
	 * @param launchPlayer Should we launch the player UI?
	 */
	public final void playTrackAt(int startingIndex, boolean launchPlayer) {

		// ENSURE Service is running
		ensureServiceStarted();

		// SEND Proper Intent to PlayerService with list of tracks
		Intent enqueueIntent = new Intent();
		enqueueIntent.setAction(PlayerService.ACTION_CONTROL_PLAY_TRACK);
		enqueueIntent.putExtra(PlayerService.EXTRA_CONTROL_STARTINDEX, startingIndex);
		mLocalBroadcastManager.sendBroadcast(enqueueIntent);

		if (launchPlayer) {
			// LAUNCH PlayerActivity
			launchPlayer();
		}
	}

	/**
	 * Ensure the PlayerService is started and running
	 */
	public final void ensureServiceStarted() {
		// Ensure service is running
		Log.i("MusicClubApplication", "Ensuring player service is started...");
		Intent playerServiceIntent = new Intent(this, PlayerService.class);
		startService(playerServiceIntent);
	}
	
	/**
	 * Stop the PlayerService
	 */
	public final void stopService() {
		Intent playerServiceIntent = new Intent(this, PlayerService.class);
		stopService(playerServiceIntent);
	}

	/**
	 * Create a song list containing a single track
	 * @param track The track
	 * @return ArrayList of Song
	 */
	public final static ArrayList<Playable> singleTrackSonglist(Playable track) {
		ArrayList<Playable> list = new ArrayList<Playable>();
		list.add(track);
		return list;
	}
	
	/**
	 * Create a Playlist containing a single track
	 * @param track The track
	 * @return Playlist object
	 */
	/*
	public final static Playlist singleTrackPlaylist(Playable track)
	{
		Playlist plist = new Playlist();
		plist.addSong(track);
		return plist;
	}
	*/

	/**
	 * Get singleton instance of the LocalBroadcastManager implemented by this library
	 * @return LocalBroadcastManager instance
	 */
	public final LocalBroadcastManager getLocalBroadcastManager() {
		return mLocalBroadcastManager;
	}
	
	/**
	 * Get singleton instance of the PlaylistManager implemented by the application
	 * @return PlaylistManager implementation instance
	 */
	public final PlaylistManager getPlaylistManager() {
		return mPlaylistManager;
	}
	
	/**
	 * Set singleton instance of the PlaylistManager implemented by the application
	 * <b>IMPORTANT</b>: Must be called in the overridden onCreate() method prior to the call to super.onCreate().
	 * @param pm Application implemented instance of the PlaylistManager interface
	 */
	protected final void setPlaylistManager(PlaylistManager pm)
	{
		mPlaylistManager = pm;
	}
	
	/**
	 * Save a String preference to application SharedPreferences
	 * @param key Key of preference
	 * @param value Value to save
	 */
	public final void setPreferenceString(String key, String value)
	{
		mPrefs.edit().putString(key, value).commit();
	}
	
	/**
	 * Get String preference from application SharedPreferences
	 * @param key Key of preference
	 * @return Saved preference value or an empty string if the key does not exist 
	 */
	public final String getPreferenceString(String key)
	{
		return getPreferenceString(key, "");
	}
	
	/**
	 * Get String preference from application SharedPreferences
	 * @param key Key of preference
	 * @param defaultValue Default value if the key is not found
	 * @return Saved preference value or defaultValue if the key is not found
	 */
	public final String getPreferenceString(String key, String defaultValue)
	{
		return mPrefs.getString(key, defaultValue);
	}
	
	/**
	 * Save a Boolean preference value to application SharedPreferences
	 * @param key Key of preference
	 * @param value Value to save
	 */
	public final void setPreferenceBoolean(String key, boolean value)
	{
		mPrefs.edit().putBoolean(key, value).commit();
	}
	
	/**
	 * Get Boolean preference from application SharedPreferences
	 * @param key Key of preference
	 * @param defaultValue Default value if the key is not found
	 * @return Saved preference value or defaultValue if the key is not found
	 */
	public final boolean getPreferenceBoolean(String key, boolean defaultValue)
	{
		return mPrefs.getBoolean(key, defaultValue);
	}
	
	/**
	 * Convenience method for calling SharedPreferences.registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)
	 * @param listener
	 */
	public final void registerOnSharedPreferenceChangeListener(OnSharedPreferenceChangeListener listener)
	{
		mPrefs.registerOnSharedPreferenceChangeListener(listener);
	}
	
	/**
	 * Register to receive simple callbacks from the PlayerService
	 * @param cb PlayerObserver callbacks class
	 */
	public final void registerPlayerObserver(PlayerObserver cb)
	{
		mObservers.add(cb);
	}
	
	/**
	 * Unregister a previously registered PlayerObserver
	 * @param cb PlayerObserver callbacks class
	 */
	public final void unregisterPlayerObserver(PlayerObserver cb)
	{
		mObservers.remove(cb);
	}
	
	/**
	 * Convenience method for calling LocalBroadcastManager.registerReceiver(BroadcastReceiver br, IntentFilter filter)
	 * @param br BroadcastReceiver to use
	 * @param filter IntentFilter to use
	 */
	public final void registerLocalBroadcastReceiver(BroadcastReceiver br, IntentFilter filter)
	{
		mLocalBroadcastManager.registerReceiver(br, filter);
	}
	
	/**
	 * Convenience method for calling LocalBroadcastManager.unregisterReceiver(BroadcastReceiver br)
	 * @param br BroadcastReceiver to use
	 */
	public final void unregisterLocalBroadcastReceiver(BroadcastReceiver br)
	{
		mLocalBroadcastManager.unregisterReceiver(br);
	}

	private HashMap<String, Bitmap> artCache = new HashMap<String, Bitmap>();
	private Queue<AlbumArtJob> artJobQueue = new LinkedList<AlbumArtJob>();
	
	/**
	 * Get an album art image asyncrhonously
	 * @param artUrl Load this url
	 * @param cb Callback to receive the loaded image
	 */
	public final void fetchArtImage(final String artUrl, final ArtLoaderCallback cb) {
		if (artCache.containsKey(artUrl)) {
			cb.onArtLoaded(artCache.get(artUrl));
		} else {
			try {
				final AlbumArtLoader loader = new AlbumArtLoader(new AlbumArtLoader.AlbumArtLoaderListener() {

					@Override
					public void onAlbumArtLoaded(AlbumArtResponse response) {
						artCache.put(artUrl, response.getAlbumArtImage());
						cb.onArtLoaded(response.getAlbumArtImage());
						if (!artJobQueue.isEmpty()) {
							AlbumArtJob job = artJobQueue.poll();
							fetchArtImage(job.artUrl, job.callback);
						}
					}
				});
				final AlbumArtRequest request = new AlbumArtRequest(artUrl, 0, 0);
				loader.execute(request);
			} catch (RejectedExecutionException ex) {
				Log.e("MusicClubApplication::fetchArtImage", "System rejected AsyncTask. Saving for later...");
				artJobQueue.offer(AlbumArtJob.create(artUrl, cb));
			}
		}
	}

	/**
	 * Represents a queued album art lookup
	 * 
	 * @author Matt Schneeberger
	 *
	 */
	public static class AlbumArtJob {

		public static AlbumArtJob create(String _artUrl, ArtLoaderCallback _callback) {
			AlbumArtJob _job = new AlbumArtJob();
			_job.artUrl = _artUrl;
			_job.callback = _callback;
			return _job;
		}

		public String artUrl;
		public ArtLoaderCallback callback;
	}

	/**
	 * Used to receive Album Art image after it has been loaded
	 * 
	 * @author Matt Schneeberger
	 *
	 */
	public interface ArtLoaderCallback {

		public void onArtLoaded(Bitmap bitmap);
	}

	/**
	 * Extend this method to launch your player UI when requested by common UI elements
	 */
	public abstract void launchPlayer();
	
	/**
	 * Override this method to process the tracks album art URL before being loaded
	 * 
	 * This is useful for Ampache clients that need a time based authentication token when accessed.
	 * @param track Playable track
	 * @return Processed URL
	 */
	public String getArtUrl(Playable track)
	{
		return track.getArtUrl();
	}
	
	/**
	 * Override this method to process the track's URL before being played
	 * 
	 * This is useful for Ampache clients that need a time based authentication token when accessed.
	 * @param track Playable track
	 * @return Processed URL
	 */
	public String getUrl(Playable track)
	{
		return track.getUrl();
	}
	
	
}
