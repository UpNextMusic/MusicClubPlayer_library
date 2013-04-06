package com.schneenet.android.lib.musicclubplayer.media;

import java.util.List;

public interface Playlist
{
	public boolean isSamePlaylist(Playlist other);
	public int size();
	
	public int getTrackPosition(Playable track, boolean shuffle);
	public int getTrackNumber(Playable track);
	
	public Playable getTrack(int position, boolean shuffle);
	public List<Playable> getTrackList();
	
	public void addTrack(Playable track);
	public void append(Playlist plist);
	public void addTracks(List<Playable> tracks);
	public void clear();
	public Playable deleteTrackAt(int position);
	public boolean deleteTrack(Playable track);

}
