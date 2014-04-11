package alisovets.example.hellotranslate.dialog;

import alisovets.example.hellotranslate.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Displays a dialog informing the translate receiving is fail. 
 */
public class TranslateFailDialog extends DialogFragment {
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		return new AlertDialog.Builder(getActivity()).setTitle(R.string.net_problem_caption).setMessage(R.string.translate_fail_message)
		.setPositiveButton(R.string.ok_button_caption, null).setOnCancelListener(null).create();	
	}
}
