package alisovets.example.hellotranslate;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Locale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import android.content.Context;
import android.util.Log;

/**
 * The utility class to request translates and speech mp3 files from Google Translate service
 * 
 * @author Lisovets Alexander
 * 
 */
public class TranslateRequestor {
	private static final String TAG = "TranslateRequestor log";
	private static final String TRANSLATE_URL_REQUEST = "http://translate.google.com/translate_a/t?client=p&ie=UTF-8&oe=UTF-8&hl=en&sl=%s&tl=%s&text=%s";
	private static final String CHECK_LANG_URL_REQUEST = "http://translate.google.com/translate_tts?ie=UTF-8&q=a&tl=";
	private static final String GET_MP3_URL_REQUEST = "http://translate.google.com/translate_tts?ie=UTF-8&tl=%s&q=%s";
	private static final String SENTENCES_KEY = "sentences";
	private static final String TRANS_KEY = "trans";

	/**
	 * requests a translate of the text from the Google translate web service
	 * 
	 * @param text
	 *            - text to translate
	 * @param localeFrom
	 *            - the locale for the source text
	 * @param localeTo
	 *            - the locale for the thranslate
	 * @return translate
	 * @throws IOException
	 */
	public static String translate(String text, Locale localeFrom, Locale localeTo) throws IOException {

		URL url = new URL(String.format(TRANSLATE_URL_REQUEST, localeFrom.getLanguage(), localeTo.getLanguage(), URLEncoder.encode(text, "UTF-8")));
		URLConnection urlConnection = url.openConnection();
		Log.d(TAG, "urlConnection= " + urlConnection);
		// urlConnection.setRequestProperty("User-Agent", "Mozilla/5.0");
		urlConnection.setConnectTimeout(1500);
		urlConnection.setReadTimeout(1500);
		urlConnection.setDoOutput(true);
		InputStream inputStream = urlConnection.getInputStream();

		BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
		String result = "";
		try {
			result = br.readLine();
		} finally {
			br.close();
		}
		return parseTranslateResultJson(result);
	}

	/*
	 * parses the Json string that is returned the Google translate web service
	 * returns the translate string
	 */
	private static String parseTranslateResultJson(String jsonString) {

		JSONObject object;
		String translate = "";
		try {
			object = (JSONObject) new JSONTokener(jsonString).nextValue();
			JSONArray sentences = object.getJSONArray(SENTENCES_KEY);
			for (int i = 0; i < sentences.length(); i++) {
				JSONObject sentence = sentences.getJSONObject(i);
				if (i == 0) {

					translate += sentence.getString(TRANS_KEY);
				} else {
					translate += " " + sentence.getString(TRANS_KEY);
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
			Log.e(TAG, "failed parsing: " + jsonString);
		}

		return translate;

	}

	

	/**
	 * check if the translate web service supports the language to speak
	 * 
	 * @param language
	 * @return true if language is supported
	 */
	public static boolean isAvailableSpeakerLanguage(String language) {
		try {
			URL url = new URL(CHECK_LANG_URL_REQUEST + language);
			URLConnection urlConnection = url.openConnection();
			urlConnection.setConnectTimeout(1000);
			urlConnection.getInputStream().close();
		} catch (IOException e) {
			return false;
		}
		return true;

	}

	/**
	 * clears the tmp directory
	 * 
	 * @param context
	 */
	public static void clearTmpDirrectory(Context context) {
		File tmpDir = context.getFilesDir();
		File[] files = tmpDir.listFiles();
		for (File file : files) {
			if (file.isFile()) {
				file.delete();
			}
		}
	}

	public static void requestMp3AndCreateFile(File mp3File,  String text, Locale locale) throws IOException {
		URL url = new URL(String.format(GET_MP3_URL_REQUEST, locale.getLanguage(), URLEncoder.encode(text, "UTF-8")));
		URLConnection urlConnection = url.openConnection();
		urlConnection.setConnectTimeout(1500);
		urlConnection.setReadTimeout(1500);
		urlConnection.setDoOutput(true);
		InputStream inputStream = urlConnection.getInputStream();
		byte[] resultBuffer;
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				baos.write(buffer, 0, bytesRead);
			}
			resultBuffer = baos.toByteArray();
		} finally {
			inputStream.close();
		}

		FileOutputStream fos = new FileOutputStream(mp3File);
		try {
			fos.write(resultBuffer);
		} finally {
			fos.close();
		}
		mp3File.deleteOnExit();
	}


}
