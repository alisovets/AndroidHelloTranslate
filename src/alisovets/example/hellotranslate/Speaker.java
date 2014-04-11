package alisovets.example.hellotranslate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import android.content.Context;
import android.media.MediaPlayer;
import android.text.TextUtils;

/**
 * To speak texts. 
 * @author Alexander Lisovets, 2014
 *
 */
public class Speaker {
	
	private static final String FORBIDDEN_SYMBOL_PATERN = "[\\\\/:\\*<>\\|\"!~;,^`]+";
	private static final int MAX_LENGTH_FILENAME = 100;
	
	private MediaPlayer mMediaPlayer;
	private Context mContext;
	
	/**
	 * Constructor 
	 * @param context 
	 */
	Speaker(Context context){
		mContext = context;
		mMediaPlayer = new MediaPlayer();
	}

	/**
	 * speaks the text 
	 * @param text
	 * @param locale
	 * @throws IOException if a network or file problems occur
	 */
	public void speak(String text, Locale locale) throws IOException {
		
		File mp3File = getTranslateMp3(text, locale);
		if (mp3File == null) {
			return;
		}

		try {
			final FileInputStream fis = new FileInputStream(mp3File);
			playFromFileInputStream(fis);
			mp3File.delete();
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * plays audio from the file that specified by the FileInputStream
	 * @param fis 
	 * @throws IOException when case of problems when trying to play
	 */
	public synchronized void playFromFileInputStream(FileInputStream fis) throws IOException  {
		
		mMediaPlayer.reset();
		mMediaPlayer.setDataSource(fis.getFD());
		mMediaPlayer.prepare();
		mMediaPlayer.start();
	}
	
	
	/*
	 * creates an returns the name for mp3 file for text to speak param text -
	 * to speak param locale for the text
	 */
	public static String createMp3FilenameForText(String text, Locale locale) {

		String filename = text.toLowerCase(locale).replaceAll("\\?", ".").replaceAll(FORBIDDEN_SYMBOL_PATERN, " ").trim().replaceAll("\\s+", "_");
		if (filename.length() > MAX_LENGTH_FILENAME) {
			filename = filename.substring(0, MAX_LENGTH_FILENAME).trim();
		}
		if (TextUtils.isEmpty(filename)) {
			return null;
		}
		return locale.getLanguage() + "." + filename;

	}
	
	/**
	 * creates mp3 file with voiced text
	 * 
	 * @param context
	 * @param text the text to be to voice
	 * @param locale for the text
	 * @return file with voiced text
	 * @throws IOException if a network or file problems occur
	 */
	public File getTranslateMp3(String text, Locale locale) throws IOException {
		String filename = createMp3FilenameForText(text, locale);
		if (filename == null) {
			return null;
		}
		File mp3File = new File(mContext.getFilesDir(), filename);
		if (!mp3File.exists()) {
			TranslateRequestor.requestMp3AndCreateFile(mp3File, text,locale);
		}
		return mp3File;
	}

}
