package com.schneenet.android.lib.musicclubplayer.albumart;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;

public class AlbumArtLoader extends AsyncTask<AlbumArtRequest, Void, AlbumArtResponse> {

	public AlbumArtLoader(AlbumArtLoaderListener l) {
		mListener = l;
	}

	protected AlbumArtResponse doInBackground(AlbumArtRequest... args) {
		if (args.length == 1) {
			AlbumArtRequest req = args[0];
			AlbumArtResponse resp = new AlbumArtResponse(req);
			try {
				HttpClient client = new DefaultHttpClient();
				HttpGet request = new HttpGet();
				String url = req.getArtUrl();
				/* Check server url for scheme */
				if (!url.startsWith("http://"))
				{
					url = "http://" + url;
				}
				request.setURI(new URI(url));
				HttpResponse response = client.execute(request);
				InputStream ips = response.getEntity().getContent();
				Bitmap bm = BitmapFactory.decodeStream(ips);
				if (req.getDstHeight() > 0 && req.getDstWidth() > 0)
				{
					resp.setAlbumArtImage(Bitmap.createScaledBitmap(bm, req.getDstWidth(), req.getDstHeight(), false));
				}
				else
				{
					resp.setAlbumArtImage(bm);
				}
				resp.setStatus(0, null);
			} catch (URISyntaxException e) {
				resp.setStatus(-1, e.getMessage());
			} catch (ClientProtocolException e) {
				resp.setStatus(-1, e.getMessage());
			} catch (IOException e) {
				resp.setStatus(-1, e.getMessage());
			}
			return resp;
		} else {
			return null;
		}
	}
	
	protected void onPostExecute(AlbumArtResponse result) {
		mListener.onAlbumArtLoaded(result);
	}

	private AlbumArtLoaderListener mListener;
	
	public interface AlbumArtLoaderListener {
		public void onAlbumArtLoaded(AlbumArtResponse response);
	}

}

