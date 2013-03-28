package com.schneenet.android.lib.musicclubplayer.media.local;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * @author Matt
 *
 */
public class SongSerializer implements JsonDeserializer<Song>, JsonSerializer<Song> {

	@Override
	public JsonElement serialize(Song src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject sObj = new JsonObject();
		sObj.addProperty(PROP_ID, src.getId());
		sObj.addProperty(PROP_EXTRA, src.extra);
		return sObj;
	}

	@Override
	public Song deserialize(JsonElement json, Type typeOfSrc, JsonDeserializationContext context) throws JsonParseException {

		JsonObject jObj = json.getAsJsonObject();

		long id = jObj.get(PROP_ID).getAsLong();
		Song s = Song.createFromLocalId(id);
		if (jObj.has(PROP_EXTRA)) s.extra = jObj.get(PROP_EXTRA).getAsString();

		return s;
	}

	public static final String PROP_ID = "prop_AmpacheObject_id";
	public static final String PROP_EXTRA = "prop_Song_extra";

}