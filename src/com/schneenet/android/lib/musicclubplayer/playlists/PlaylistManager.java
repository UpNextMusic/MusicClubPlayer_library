package com.schneenet.android.lib.musicclubplayer.playlists;

import java.io.Reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.schneenet.android.lib.musicclubplayer.media.local.Song;
import com.schneenet.android.lib.musicclubplayer.media.local.SongSerializer;

public class PlaylistManager {

	private Gson mGsonInstance;
	
	public PlaylistManager()
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Song.class, new SongSerializer());
		gsonBuilder.registerTypeAdapter(Playlist.class, new PlaylistSerializer());
		mGsonInstance = gsonBuilder.create();
	}
	
	public String serializePlaylist(Playlist playlist)
	{
		return mGsonInstance.toJson(playlist, Playlist.class);
	}
	
	public void serializePlaylistToStream(Playlist playlist, Appendable writer)
	{
		mGsonInstance.toJson(playlist, Playlist.class, writer);
	}
	
	public Playlist deserializePlaylist(String jsonString)
	{
		return mGsonInstance.fromJson(jsonString, Playlist.class);
	}
	
	public Playlist deserializePlaylist(Reader r)
	{
		return mGsonInstance.fromJson(r, Playlist.class);
	}
	
}
