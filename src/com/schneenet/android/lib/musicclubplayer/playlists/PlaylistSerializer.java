package com.schneenet.android.lib.musicclubplayer.playlists;

import java.lang.reflect.Type;
import java.util.Iterator;

import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.schneenet.android.lib.musicclubplayer.media.Playable;

public class PlaylistSerializer implements JsonDeserializer<Playlist>, JsonSerializer<Playlist>
{
	
	@Override
	public JsonElement serialize(Playlist src, Type typeOfSrc, JsonSerializationContext context)
	{
		JsonObject plObject = new JsonObject();
		JsonArray slArray = new JsonArray();
		Iterator<Playable> iter = src.getSongList().iterator();
		while (iter.hasNext()) {
			Playable item = iter.next();
			slArray.add(context.serialize(item.getKey()));
		}
		plObject.add(PROP_SONGLIST, slArray);
		plObject.addProperty(PROP_TITLE, src.getName());
		plObject.addProperty(PROP_TYPE, src.getType());
		plObject.addProperty(PROP_UID, src.getUid());
		return plObject;
	}

	@Override
	public Playlist deserialize(JsonElement src, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {
		JsonObject plObject = src.getAsJsonObject();

		Playlist newPlaylist = new Playlist();
		newPlaylist.setName(plObject.getAsJsonPrimitive(PROP_TITLE).getAsString());
		newPlaylist.setType(plObject.getAsJsonPrimitive(PROP_TYPE).getAsString());
		newPlaylist.setUid(plObject.getAsJsonPrimitive(PROP_UID).getAsString());
		
		Iterator<JsonElement> iter = plObject.get(PROP_SONGLIST).getAsJsonArray().iterator();
		while (iter.hasNext()) {
			String key = iter.next().getAsString();
			String[] bits = key.split(":", 2);
			if (bits.length == 2)
			{
				try
				{
					// By using reflection, we retain the type information, because we are literally passing the type as part of the key
					Class<?> clazz = Class.forName(bits[0]);
					Playable p = (Playable) clazz.newInstance();
					p.buildFromKey(bits[1]);
					newPlaylist.addSong(p);
				}
				catch (Exception ex)
				{
					Log.e("PlaylistSerializer", String.format("Failed to load class '%s'.", bits[0]), ex);
				}
			}
			else
			{
				Log.e("PlaylistSerializer", String.format("Failed to parse key for playlist entry: '%s'", key));
			}
		}
		return newPlaylist;
	}

	private static final String PROP_SONGLIST = "prop_Playlist_songList";
	private static final String PROP_TITLE = "prop_Playlist_title";
	private static final String PROP_TYPE = "prop_Playlist_type";
	private static final String PROP_UID = "prop_Playlist_uid";

}