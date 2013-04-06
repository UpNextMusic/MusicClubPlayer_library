package com.schneenet.android.lib.musicclubplayer;

import com.schneenet.android.lib.musicclubplayer.media.Playlist;

/**
 * Interface that is used to inform player UIs about state changes
 * @author Matt Schneeberger
 *
 */
public interface PlayerObserver
{
	/**
	 * Called when the meta data changes (usually track changes)
	 * @param title
	 * @param artist
	 * @param album
	 * @param albumArtUrl
	 * @param track
	 * @param trackOf
	 * @param playing
	 * @param repeat
	 * @param shuffle
	 * @param playlist
	 */
	public void onMetaDataUpdate(String title, String artist, String album, String albumArtUrl, String track, String trackOf, boolean playing, boolean repeat, boolean shuffle, Playlist playlist);
	
	/**
	 * Called when the progress changes (buffering, periodically when playing)
	 * @param position
	 * @param buffered
	 * @param duration
	 */
	public void onProgressChanged(float position, float buffered, float duration);
	
	/**
	 * Called when information about the playlist changes or is requested manually
	 * @param playlist
	 * @param currentIndex
	 */
	public void onPlaylistUpdate(Playlist playlist, int currentIndex);
}
