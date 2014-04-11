package alisovets.example.hellotranslate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

import alisovets.example.hellotranslate.dialog.AboutDialog;
import alisovets.example.hellotranslate.dialog.NoNetworkDialog;
import alisovets.example.hellotranslate.dialog.TranslateFailDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

public class MainFragment extends Fragment implements OnClickListener {

	private static final String FAIL_TRANSLATE_DLG_TAG = "fail_translate_dialog";
	private static final String NO_NETWORK_DLG_TAG = "no_network_dialog";
	private static final String ABOUT_DLG_TAG = "about_dialog";

	private static final String TRANSLATE_TEXT_KEY = "translate";
	private static final String FROM_SPINNER_SELECTED_KEY = "from_spinner";
	private static final String TO_SPINNER_SELECTED_KEY = "to_spinner";

	private EditText mSourceText;
	private EditText mTranslateText;
	private Spinner mFromLangSpinner;
	private Spinner mToLangSpinner;
	private ImageButton mPlaySrcButton;
	private ImageButton mPlayTranslateButton;
	private ProgressBar mProgressBar;
	private Button mTranslateButton;
	private ArrayAdapter<LocaleWrapper> mArrayAdapter;
	private int mSpinnerAutoChange = 0;
	private Handler mHandler;
	private Speaker mSpeaker;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		View viewHierarchy = inflater.inflate(R.layout.fragment_main, container, false);

		mSourceText = (EditText) viewHierarchy.findViewById(R.id.srcText);
		mTranslateButton = (Button) viewHierarchy.findViewById(R.id.translate_button);
		mTranslateButton.setOnClickListener(this);
		mTranslateText = (EditText) viewHierarchy.findViewById(R.id.translateText);
		mPlaySrcButton = (ImageButton) viewHierarchy.findViewById(R.id.play_src_btn);
		mPlaySrcButton.getBackground().setAlpha(150);
		mPlaySrcButton.setOnClickListener(this);
		mPlayTranslateButton = (ImageButton) viewHierarchy.findViewById(R.id.play_translate_btn);
		mPlayTranslateButton.getBackground().setAlpha(150);
		mPlayTranslateButton.setOnClickListener(this);
		ImageButton swapButton = (ImageButton) viewHierarchy.findViewById(R.id.swapButton);
		swapButton.setOnClickListener(this);
		ImageButton clearTextButton = (ImageButton) viewHierarchy.findViewById(R.id.clear_btn);
		clearTextButton.getBackground().setAlpha(150);
		clearTextButton.setOnClickListener(this);
		mProgressBar = (ProgressBar) viewHierarchy.findViewById(R.id.progressBar);
		mTranslateText.setKeyListener(null);
		mTranslateText.setOnFocusChangeListener(onFocusChangeListener);
		mFromLangSpinner = (Spinner) viewHierarchy.findViewById(R.id.from_lang_spinner);
		mFromLangSpinner.setPrompt("Translate from");
		mToLangSpinner = (Spinner) viewHierarchy.findViewById(R.id.to_lang_spinner);
		mToLangSpinner.setPrompt("translate to");
		createSpinnerAdapter();
		mHandler = new Handler();
		restoreTranslate(savedInstanceState);
		setHasOptionsMenu(true);
		mSpeaker = new Speaker(getActivity());
		return viewHierarchy;
	}

	@Override
	public void onResume() {
		super.onResume();
		mSpinnerAutoChange += 2;
		SharedPreferences sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		mFromLangSpinner.setSelection(sPref.getInt(FROM_SPINNER_SELECTED_KEY, 0));
		mToLangSpinner.setSelection(sPref.getInt(TO_SPINNER_SELECTED_KEY, 0));
		if (checkNetworkAvailable()) {
			if (mPlaySrcButton.getVisibility() == View.INVISIBLE) {
				checkAvailableSpeakerAndSetVisibility(mPlaySrcButton, ((LocaleWrapper) mFromLangSpinner.getSelectedItem()).locale.getLanguage());
			}
			if (mPlayTranslateButton.getVisibility() == View.INVISIBLE) {
				checkAvailableSpeakerAndSetVisibility(mPlayTranslateButton, ((LocaleWrapper) mToLangSpinner.getSelectedItem()).locale.getLanguage());
			}
		}

	}

	@Override
	public void onPause() {
		super.onPause();
		SharedPreferences sPref = getActivity().getPreferences(Context.MODE_PRIVATE);
		Editor ed = sPref.edit();
		ed.putInt(FROM_SPINNER_SELECTED_KEY, mFromLangSpinner.getSelectedItemPosition());
		ed.putInt(TO_SPINNER_SELECTED_KEY, mToLangSpinner.getSelectedItemPosition());
		ed.commit();

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onOptionsMenuClosed(Menu menu) {
		if (mTranslateText.isFocused()) {
			hideTheKeyboard(getActivity().getApplicationContext(), mTranslateText);
		}
		super.onOptionsMenuClosed(menu);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.swapButton:
			swapLanguages();
			break;
		case R.id.translate_button:
			translate();
			break;
		case R.id.play_src_btn:
			saySource();
			break;
		case R.id.play_translate_btn:
			sayTranslate();
			break;
		case R.id.clear_btn:
			clearText();
			break;
		}
	}

	/*
	 * Speaks the string Speaks the string that is in the source textView.
	 */
	private void saySource() {
		String text = mSourceText.getText().toString();
		Locale locale = ((LocaleWrapper) mFromLangSpinner.getSelectedItem()).locale;
		sayText(text, locale);

	}

	/*
	 * Speaks the string that is on the translate textView.
	 */
	private void sayTranslate() {
		String text = mTranslateText.getText().toString();
		Locale locale = ((LocaleWrapper) mToLangSpinner.getSelectedItem()).locale;
		sayText(text, locale);
	}

	/*
	 * swaps word and translation languages
	 */
	private void swapLanguages() {
		int position = mFromLangSpinner.getSelectedItemPosition();
		mFromLangSpinner.setSelection(mToLangSpinner.getSelectedItemPosition());
		mToLangSpinner.setSelection(position);
	}

	/*
	 * Clears text in the source textView.
	 */
	private void clearText() {
		mSourceText.setText("");
		mTranslateText.setText("");
	}

	/*
	 * checks if language is supported to speak and sets a button visibility
	 * param the button that needs set the visibility param language
	 */
	private void checkAvailableSpeakerAndSetVisibility(final ImageButton button, final String language) {
		button.setVisibility(View.INVISIBLE);
		if (!ConnectChecker.isNetworkAvailable(getActivity().getApplicationContext())) {
			return;
		}
		new Thread(new Runnable() {

			@Override
			public void run() {

				if (TranslateRequestor.isAvailableSpeakerLanguage(language)) {
					button.post(new Runnable() {

						@Override
						public void run() {
							button.setVisibility(View.VISIBLE);
						}
					});
				}
			}
		}).start();

	}

	/*
	 * creates and inits a spinner adapter for languages
	 */
	private void createSpinnerAdapter() {
		mArrayAdapter = new ArrayAdapter<LocaleWrapper>(getActivity(), android.R.layout.simple_spinner_item, getLanguageList());
		mArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mFromLangSpinner.setAdapter(mArrayAdapter);
		mToLangSpinner.setAdapter(mArrayAdapter);

		mSpinnerAutoChange += 2;
		OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> spinner, View view, int position, long id) {
				if (spinner == mFromLangSpinner) {
					checkAvailableSpeakerAndSetVisibility(mPlaySrcButton, ((LocaleWrapper) spinner.getSelectedItem()).locale.getLanguage());
					if (mSpinnerAutoChange > 0) {
						mSpinnerAutoChange--;
					} else {
						mTranslateText.setText("");
					}
				} else {
					checkAvailableSpeakerAndSetVisibility(mPlayTranslateButton, ((LocaleWrapper) spinner.getSelectedItem()).locale.getLanguage());
					if (mSpinnerAutoChange > 0) {
						mSpinnerAutoChange--;
					} else {
						mTranslateText.setText("");
					}
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		};
		mFromLangSpinner.setOnItemSelectedListener(onItemSelectedListener);
		mToLangSpinner.setOnItemSelectedListener(onItemSelectedListener);
	}

	/*
	 * restore the saved translate text from Bundle
	 */
	private void restoreTranslate(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			String savedString = savedInstanceState.getString(TRANSLATE_TEXT_KEY);
			if (savedString == null) {
				savedString = "";
			}
			mTranslateText.setText(savedString);
		}
	}

	/*
	 * creates and returns list of available locales
	 */
	private List<LocaleWrapper> getLanguageList() {
		Locale[] locales = Locale.getAvailableLocales();
		TreeSet<LocaleWrapper> languages = new TreeSet<LocaleWrapper>();
		for (Locale locale : locales) {
			languages.add(new LocaleWrapper(locale.getLanguage()));
		}
		return new ArrayList<LocaleWrapper>(languages);
	}

	/**
	 * checks if network is available and shows message when network is not
	 * available
	 * 
	 * @return true if network is available
	 */
	private boolean checkNetworkAvailable() {

		if (!ConnectChecker.isNetworkAvailable(getActivity().getApplicationContext())) {
			showDialog(NO_NETWORK_DLG_TAG);
			return false;
		}
		return true;
	}

	/*
	 * requests translate from the Google translate service, processes the
	 * response and insert the translate in the translate TextView
	 */
	private void translate() {

		final String text = mSourceText.getText().toString();
		mTranslateText.setText("");

		if (TextUtils.isEmpty(text)) {
			return;
		}

		if (!checkNetworkAvailable()) {
			return;
		}

		mTranslateButton.setEnabled(false);
		mProgressBar.setVisibility(View.VISIBLE);

		new Thread() {
			public void run() {

				Locale localeFrom = ((LocaleWrapper) mFromLangSpinner.getSelectedItem()).locale;
				Locale localeTo = ((LocaleWrapper) mToLangSpinner.getSelectedItem()).locale;
				String translate = null;
				try {
					translate = TranslateRequestor.translate(text, localeFrom, localeTo);
				} catch (IOException e) {
					e.printStackTrace();
					translate = "";
					showDialog(FAIL_TRANSLATE_DLG_TAG);
					return;
				} finally {

					final String translateString = translate;
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mTranslateButton.setEnabled(true);
							mTranslateText.setText(translateString);
							mProgressBar.setVisibility(View.INVISIBLE);
						}
					});

				}

			}
		}.start();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			showDialog(ABOUT_DLG_TAG);
			return true;
		default:
			return super.onOptionsItemSelected(item);

		}

	}

	/*
	 * shows dialog a dialog that matches the tag avoiding duplicates
	 */
	private void showDialog(String dialogTag) {
		Fragment prev = getFragmentManager().findFragmentByTag(dialogTag);
		if (prev != null) {
			FragmentTransaction ft = getFragmentManager().beginTransaction();
			ft.remove(prev);
			ft.commit();
		}

		if (dialogTag == FAIL_TRANSLATE_DLG_TAG) {
			TranslateFailDialog failTranslateDialog = new TranslateFailDialog();
			failTranslateDialog.show(getFragmentManager(), FAIL_TRANSLATE_DLG_TAG);
		} else if (dialogTag == NO_NETWORK_DLG_TAG) {
			NoNetworkDialog messageDialog = new NoNetworkDialog();
			messageDialog.show(getFragmentManager(), NO_NETWORK_DLG_TAG);
		}
		if (dialogTag == ABOUT_DLG_TAG) {
			AboutDialog aboutDialog = new AboutDialog();
			aboutDialog.show(getFragmentManager(), ABOUT_DLG_TAG);
		}
	}

	/**
	 * Speaks the string using the specified a locale parameter.
	 * 
	 * @param text
	 *            string which is spoken
	 * @param locale
	 *            locale
	 */
	public void sayText(final String text, final Locale locale) {
		if (TextUtils.isEmpty(text)) {
			return;
		}

		if (!checkNetworkAvailable()) {
			return;
		}

		mProgressBar.setVisibility(View.VISIBLE);
		mPlaySrcButton.setEnabled(false);
		mPlayTranslateButton.setEnabled(false);
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					mSpeaker.speak(text, locale);
				} catch (Exception e) {

					mHandler.post(new Runnable() {
						@Override
						public void run() {
							Toast toast = Toast.makeText(getActivity(), getString(R.string.speaker_fail_message), Toast.LENGTH_LONG);
							toast.setGravity(Gravity.CENTER, 0, 0);
							toast.show();
						}
					});

				} finally {
					mHandler.post(new Runnable() {
						@Override
						public void run() {
							mProgressBar.setVisibility(View.INVISIBLE);
							mPlaySrcButton.setEnabled(true);
							mPlayTranslateButton.setEnabled(true);
						}
					});
				}
			}
		}).start();

	}

	OnFocusChangeListener onFocusChangeListener = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				hideTheKeyboard(getActivity(), mTranslateText);
			}

		}
	};

	/**
	 * hides the Keyboard
	 * 
	 * @param context
	 *            The context of the activity
	 * @param editText
	 *            The edit text for which we want to hide the keyboard
	 */
	public void hideTheKeyboard(Context context, EditText editText) {
		InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), InputMethodManager.RESULT_UNCHANGED_SHOWN);
	}

	/**
	 * the comparable wrapper for a Locale object
	 */
	static class LocaleWrapper implements Comparable<LocaleWrapper> {
		Locale locale;

		/**
		 * Constructs a new Locale using the specified language.
		 * 
		 * @param language
		 */
		LocaleWrapper(String language) {
			locale = new Locale(language);
		}

		@Override
		public String toString() {
			return locale.getDisplayLanguage();
		}

		@Override
		public int compareTo(LocaleWrapper another) {
			if (another == null) {
				return 1;
			}
			return locale.getDisplayLanguage().compareTo(another.locale.getDisplayLanguage());
		}

		@Override
		public int hashCode() {
			return locale.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof LocaleWrapper)) {
				return false;
			}
			return locale.getLanguage().equals(((LocaleWrapper) obj).locale.getLanguage());
		}

	}

}
