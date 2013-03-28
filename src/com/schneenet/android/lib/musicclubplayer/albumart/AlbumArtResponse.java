package com.schneenet.android.lib.musicclubplayer.albumart;

import android.graphics.Bitmap;

public class AlbumArtResponse {

	public AlbumArtResponse(AlbumArtRequest request) {
		mRequest = request;
	}
	
	public AlbumArtRequest getRequest() {
		return mRequest;
	}
	
	public void setAlbumArtImage(Bitmap _image)
	{
		mAlbumArtImage = _image;
	}
	
	public Bitmap getAlbumArtImage() {
		return mAlbumArtImage;
	}
	
	public int getStatusCode()
	{
		return mStatusCode;
	}
	
	public String getStatusMessage()
	{
		return mStatusMessage;
	}
	
	public void setStatus(int _code, String _message)
	{
		mStatusCode = _code;
		mStatusMessage = _message;
	}
	
	private int mStatusCode;
	private String mStatusMessage;
	private AlbumArtRequest mRequest;
	private Bitmap mAlbumArtImage;
	
}
