package com.schneenet.android.lib.musicclubplayer.media;


public abstract class PlaylistManager {
	
	public abstract String serializePlaylist(Playlist playlist);
	
	public abstract Playlist deserializePlaylist(String jsonString);
	
	public abstract Playlist newInstance();
	
}
