package com.schneenet.android.lib.musicclubplayer.media.local;

import com.schneenet.android.lib.musicclubplayer.media.ContentObject;
import com.schneenet.android.lib.musicclubplayer.media.Playable;

//TODO This class is pending research on android.content.MediaStore

public class Song extends ContentObject implements Playable {

	protected Song(long id)
	{
		super(id);
	}
	
	/**
	 * Create a new instance from an ID
	 * 
	 * Looks up the song in the database
	 * @param id Song ID
	 * @return Instance of Song
	 */
	public static Song createFromLocalId(long id)
	{
		Song s = new Song(id);
		// TODO Look up the song
		return s;
	}
	
	// TODO Subject to change based on contents of android.content.MediaStore
	public long artistId;
	public long albumId;
	public long time;
	public String name;
	public String artist;
	public String album;
	public String genre;
	public String art;
	public String url;
	public String track;
	public String extra;
	
	public long getId()
	{
		return mId;
	}

	@Override
	public String getUrl() {
		return url;
	}

	@Override
	public float getLength() {
		return time;
	}

	@Override
	public String getKey() {
		return this.getClass().getName() + ":" + String.valueOf(mId);
	}

	@Override
	public Playable buildFromKey(String key) {
		return Song.createFromLocalId(Long.parseLong(key));
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getArtist() {
		return artist;
	}

	@Override
	public String getAlbum() {
		return album;
	}

	@Override
	public String getArtUrl() {
		// TODO Album Art URL for Local Media
		return "";
	}
}
