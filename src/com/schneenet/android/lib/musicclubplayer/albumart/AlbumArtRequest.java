package com.schneenet.android.lib.musicclubplayer.albumart;

public class AlbumArtRequest {

	public AlbumArtRequest(String url, int dstWidth, int dstHeight) {
		mUrl = url;
		mDstWidth = dstWidth;
		mDstHeight = dstHeight;
	}
	
	public String getArtUrl() {
		return mUrl;
	}
	
	public int getDstWidth() {
		return mDstWidth;
	}
	
	public int getDstHeight() {
		return mDstHeight;
	}

	private final String mUrl;
	private final int mDstWidth;
	private final int mDstHeight;
}
