package johannesprueller.timestamp;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

public class NfcHelper {


    private Handler handler;
    private Activity activity;

    public NfcHelper( Activity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }

    public void InitializeNfc() {
        boolean granted;
        granted = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checkNfcPermission();

        if (granted) {
            NfcManager manager = (NfcManager) activity.getSystemService(Context.NFC_SERVICE);
            if (manager != null) {
                NfcAdapter adapter = manager.getDefaultAdapter();
                adapter.enableReaderMode(activity, new NfcCallcback(), NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V, null);
            }
        }
    }


    private boolean checkNfcPermission() {
        String permission = "android.permission.NFC";
        int res = activity.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private class NfcCallcback implements NfcAdapter.ReaderCallback {


        @Override
        public void onTagDiscovered(Tag tag) {
            Message message = handler.obtainMessage(0, "Leck, es funktioniert");
            message.sendToTarget();
        }
    }
}

