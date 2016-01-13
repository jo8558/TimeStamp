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

    public NfcHelper(Activity activity, Handler handler) {
        this.activity = activity;
        this.handler = handler;
    }

    public void InitializeNfc() {
        NfcManager manager = (NfcManager) activity.getSystemService(Context.NFC_SERVICE);
        if (manager != null) {
            NfcAdapter adapter = manager.getDefaultAdapter();
            adapter.enableReaderMode(activity, new NfcCallcback(), NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V, null);
        }

    }

    private class NfcCallcback implements NfcAdapter.ReaderCallback {


        @Override
        public void onTagDiscovered(Tag tag) {


            Message message = handler.obtainMessage(0, bytesToHex(tag.getId()));
            message.sendToTarget();
        }

        private String bytesToHex(byte[] bytes)
        {
            char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
            char[] hexChars = new char[bytes.length * 2];
            int v;
            for ( int j = 0; j < bytes.length; j++ ) {
                v = bytes[j] & 0xFF;
                hexChars[j * 2] = hexArray[v >>> 4];
                hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            }
            return new String(hexChars);
        }
    }


}

