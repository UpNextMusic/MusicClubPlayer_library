package com.schneenet.android.lib.musicclubplayer.playlists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import com.schneenet.android.lib.musicclubplayer.media.Playable;

public class Playlist {

	ArrayList<Playable> mSongList;
	private ArrayList<Playable> mSongListRand;
	String mPlaylistType = "playlist";
	String mPlaylistUid = "";
	String mPlaylistName;

	public Playlist() {
		mPlaylistName = "";
		mSongList = new ArrayList<Playable>();
		mSongListRand = new ArrayList<Playable>();
	}
	
	public Playlist(String type, String uid) 
	{
		this();
		mPlaylistType = type;
		mPlaylistUid = uid;
	}
	
	public static Playlist createFromList(ArrayList<Playable> list)
	{
		Playlist p = new Playlist();
		p.addSongs(list);
		return p;
	}
	
	public ArrayList<Playable> getSongList()
	{
		return mSongList;
	}
	
	public String getType()
	{
		return mPlaylistType;
	}
	
	public String getUid()
	{
		return mPlaylistUid;
	}
	
	public void setType(String newValue)
	{
		mPlaylistType = newValue;
	}
	
	public void setUid(String newValue)
	{
		mPlaylistUid = newValue;
	}
	
	public void addSong(Playable song) {
		mSongList.add(song);
		randomize();
	}

	public void addSongs(Collection<Playable> songs) {
		mSongList.addAll(songs);
		randomize();
	}

	public void clear() {
		mSongList.clear();
		mSongListRand.clear();
	}

	public Playable getSong(int index, boolean shuffle) {
		return shuffle ? mSongListRand.get(index) : mSongList.get(index);
	}

	public int size() {
		return mSongList.size();
	}

	/**
	 * Get the track number of the song in the playlist
	 * 
	 * @param s
	 *            Playable to search for
	 * @return Track number 1 -> size() or -1 if the song is not in the list
	 */
	public int getTrackNumber(Playable s) {
		final int x = mSongList.indexOf(s);
		return x < 0 ? x : x + 1;
	}
	
	public int getSongPosition(Playable s, boolean shuffle)
	{
		if (shuffle) {
			return mSongListRand.indexOf(s);
		} else {
			return mSongList.indexOf(s);
		}
	}

	public String getName() {
		return mPlaylistName;
	}

	public void setName(String newValue) {
		mPlaylistName = newValue;
	}

	public int syncPlaylistPosition(boolean shuffle, Playable nowPlaying) {
		return shuffle ? mSongListRand.indexOf(nowPlaying) : mSongList.indexOf(nowPlaying);
	}

	public boolean isSamePlaylist(Playlist other) {
		if (size() != other.size())
			return false; // Playlists are different because of size
		final int n = size();
		for (int i = 0; i < n; i++) {
			// Playlists are difference because the item's key at "i"
			if (!getSong(i, false).getKey().equals(other.getSong(i, false).getKey()))
				return false;
			// Playlists are different because the item's length at "i"
			if (getSong(i, false).getLength() != other.getSong(i, false).getLength())
				return false;
			// Playlists are difference because the item's URL at "i"
			if (!getSong(i, false).getUrl().equals(other.getSong(i, false).getUrl()))
				return false;
		}
		// You made it through the entire playlist and everything matched,
		// therefore...
		return true; // Playlists are identical
	}

	void randomize() {
		ArrayList<Playable> tracks = new ArrayList<Playable>(mSongList);
		mSongListRand.clear();
		Random rand = new Random(System.currentTimeMillis());
		final int n = mSongList.size();
		for (int i = 0; i < n; i++) {
			mSongListRand.add(tracks.remove(rand.nextInt(tracks.size())));
		}
	}

}
