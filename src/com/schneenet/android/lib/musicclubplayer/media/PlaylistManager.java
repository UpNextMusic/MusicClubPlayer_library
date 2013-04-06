package com.schneenet.android.lib.musicclubplayer.media;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class PlaylistManager {

	protected Gson mGsonInstance;
	
	public PlaylistManager()
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		mGsonInstance = gsonBuilder.create();
	}
	
	public abstract String serializePlaylist(Playlist playlist);
	
	public abstract Playlist deserializePlaylist(String jsonString);
	
	public abstract Playlist newInstance();
	
}
