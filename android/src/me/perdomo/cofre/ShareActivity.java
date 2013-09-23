/*
 * Copyright (C) 2013 Iván Perdomo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.perdomo.cofre;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.widget.Toast;

public class ShareActivity extends Activity {

	private static final String BOUNDARY = "cofre";
	private static final String EOL = "\r\n";
	private static final String HOST = "http://192.168.1.49:3000";
	private static final int TIMEOUT = 5000;

	@Override
	protected void onCreate(Bundle paramBundle) {
		super.onCreate(paramBundle);

		final Intent intent = getIntent();
		final String action = intent.getAction();
		final String type = intent.getType();

		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if (!cm.getActiveNetworkInfo().isConnected()) {
			return;
		}

		if (Intent.ACTION_SEND.equals(action) && type != null
				&& type.startsWith("image/")) {
			handleSendImage(intent);
		}

	}

	private void handleSendImage(Intent intent) {
		Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
		if (imageUri != null) {
			new UploadImageTask().execute(imageUri);
		}
	}

	private class UploadImageTask extends AsyncTask<Uri, Integer, String> {

		@Override
		protected String doInBackground(Uri... uri) {
			final String path = getRealPathFromURI(uri[0]);
			try {
				final ExifInterface exif = new ExifInterface(path);
				int w = 800;
				int h = 600;
				if (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION,
						ExifInterface.ORIENTATION_UNDEFINED) != ExifInterface.ORIENTATION_UNDEFINED) {
					w = 600;
					h = 800;
				}
				return compressAndUpload(ImageResizer
						.decodeSampledBitmapFromFile(path, w, h));
			} catch (IOException e) {
				e.printStackTrace(); // TODO: Better exception handling
			}
			return null;
		}

		@Override
		protected void onPostExecute(String paramString) {
			super.onPostExecute(paramString);
			if (paramString == null) {
				return;
			}
			ClipboardManager cm = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
			cm.setText(paramString);
			Toast t = Toast.makeText(getApplicationContext(),
					"Dirección lista en el clipboard", Toast.LENGTH_LONG);
			t.show();
		}

		private String compressAndUpload(Bitmap image) {
			try {
				final URL url = new URL(HOST);
				final HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();

				conn.setDoInput(true);
				conn.setDoOutput(true);
				conn.setConnectTimeout(TIMEOUT);
				conn.setReadTimeout(TIMEOUT);

				conn.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=" + BOUNDARY);

				final OutputStream os = conn.getOutputStream();
				final PrintWriter writer = new PrintWriter(
						new OutputStreamWriter(os, "UTF-8"));

				final StringBuffer header = new StringBuffer();
				header.append("--")
						.append(BOUNDARY)
						.append(EOL)
						.append("Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"")
						.append(EOL).append("Content-Type: image/jpeg")
						.append(EOL)
						.append("Content-Transfer-Encoding: binary")
						.append(EOL).append(EOL);

				final StringBuffer footer = new StringBuffer();
				footer.append(EOL).append("--").append(BOUNDARY).append("--")
						.append(EOL);

				writer.write(header.toString());
				writer.flush();

				image.compress(Bitmap.CompressFormat.JPEG, 95, os);
				os.flush();

				writer.write(footer.toString());
				writer.close();

				final BufferedReader reader = new BufferedReader(
						new InputStreamReader(conn.getInputStream()));
				final String response = reader.readLine();

				reader.close();
				conn.disconnect();

				return HOST + "/" + response;
			} catch (Exception e) {
				e.printStackTrace(); // TODO: Better exception handling
			}

			return null;
		}

		/**
		 * Extracted from http://stackoverflow.com/a/13209514
		 * 
		 * @author Chirag Raval
		 */

		private String getRealPathFromURI(Uri uri) {
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = managedQuery(uri, proj, null, null, null);
			int column_index = cursor
					.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}
	}
}
