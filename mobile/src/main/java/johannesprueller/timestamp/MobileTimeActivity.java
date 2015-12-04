package johannesprueller.timestamp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

public class MobileTimeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.context = this;

        setContentView(R.layout.activity_mobile_time);

        boolean granted;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            granted = checkNfcPermission();
        } else {
            granted = true;
        }

        if (granted) {
            NfcManager manager = (NfcManager) this.getSystemService(Context.NFC_SERVICE);
            if (manager != null) {
                NfcAdapter adapter = manager.getDefaultAdapter();
                adapter.enableReaderMode(this, new NfcCallcback(), NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_NFC_B | NfcAdapter.FLAG_READER_NFC_F | NfcAdapter.FLAG_READER_NFC_V, null);
            }
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message message) {
                String msg = message.obj.toString();
                Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
            }
        };

    }


    private boolean checkNfcPermission() {
        String permission = "android.permission.NFC";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private Handler handler;
    private Context context;

    private class NfcCallcback implements NfcAdapter.ReaderCallback {


        @Override
        public void onTagDiscovered(Tag tag) {
            Message message = handler.obtainMessage(0, "Leck, es funktioniert");
            message.sendToTarget();
        }
    }
}
