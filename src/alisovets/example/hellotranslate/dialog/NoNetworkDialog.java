package alisovets.example.hellotranslate.dialog;

import alisovets.example.hellotranslate.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * 
 * Creates dialog to report that the network is not available. 
 * The dialog has two buttons: 
 * "Setting" to open network setting; 
 * "Cancel" to close the dialog with no actions.
 * 
 */
public class NoNetworkDialog extends DialogFragment {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		builder.setTitle(R.string.no_connection_caption).setMessage(R.string.cannot_connection_message)
				.setPositiveButton(R.string.settings_btn_caption, new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent intent = new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						getActivity().startActivity(new Intent(intent));

					}
				}).setNegativeButton(R.string.cancel_button_caption, null);

		return builder.create();

	}
}
