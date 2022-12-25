package org.mbds.nfctag.write;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.mbds.nfctag.R;
import org.mbds.nfctag.model.TagType;

public class NFCWriterActivity extends AppCompatActivity {

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    public static TagType TAG = TagType.TEXT;

    private NfcTagViewModel viewModel;

    EditText editText;
    Button valide;
    RadioGroup radioGroup;
    RadioButton option1;
    RadioButton option2;
    RadioButton option3;
    View nfcImage;
    TextView textView2;
    TextView textView1;
    boolean readCard ;

    // TODO Analyser le code et comprendre ce qui est fait
    // TODO Ajouter un formulaire permettant à un utilisateur d'entrer le texte à mettre dans le tag
    // TODO Le texte peut être 1) une URL 2) un numéro de téléphone 3) un plain texte
    // TODO Utiliser le view binding
    // TODO L'app ne doit pas crasher si les tags sont mal formattés

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.write_tag_layout);

        editText = (EditText)findViewById(R.id.message);
        nfcImage = (View) findViewById(R.id.card_reader_back2);
        textView2 = (TextView) findViewById(R.id.txtView2) ;
        textView1 = (TextView) findViewById(R.id.txtView1) ;
        radioGroup = (RadioGroup) findViewById(R.id.rgChoix);
        valide = (Button) findViewById(R.id.tValider);
        option1 = (RadioButton) findViewById(R.id.option1);
        option2 = (RadioButton) findViewById(R.id.option2);
        option3 = (RadioButton) findViewById(R.id.option3);

        nfcImage.setVisibility(View.INVISIBLE);
        textView2.setVisibility(View.INVISIBLE);

        readCard = false;
        valide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                nfcImage.setVisibility(View.VISIBLE);
                textView2.setVisibility(View.VISIBLE);

                radioGroup.setVisibility(View.INVISIBLE);
                editText.setVisibility(View.INVISIBLE);
                valide.setVisibility(View.INVISIBLE);
                textView1.setVisibility(View.INVISIBLE);

                readCard = true;

            }
        });



        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if (checkedId == R.id.option1) {
                    TAG = TagType.TEXT;

                }
                else if (checkedId == R.id.option2) {
                    TAG = TagType.PHONE;

                }
                else  if (checkedId == R.id.option3) {
                    TAG = TagType.URL;
                }

            }
        });

        // init ViewModel
        viewModel = new ViewModelProvider(this).get(NfcTagViewModel.class);


        //Get default NfcAdapter and PendingIntent instances
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        // check NFC feature:
        if (nfcAdapter == null) {
            // process error device not NFC-capable…

        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        // single top flag avoids activity multiple instances launching
    }



    @Override
    protected void onResume() {
        super.onResume();

        //Enable NFC foreground detection
        if (nfcAdapter != null) {
            if (!nfcAdapter.isEnabled()) {
                // TODO afficher un message d'erreur à l'utilisateur si le NFC n'est pas activé
                // solutution
                Toast.makeText(this, "Activer NFC", Toast.LENGTH_SHORT).show();
                // TODO rediriger l'utilisateur vers les paramètres du téléphone pour activer le NFC
                // solutution
                Intent intent=new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                ComponentName cName = new ComponentName("com.android.phone","com.android.phone.Settings");
                intent.setComponent(cName);
            } else {
                nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
            }
        } else {
            // TODO afficher un message d'erreur à l'utilisateur si le téléphone n'est pas NFC-capable
            // TODO Fermer l'activité ou rediriger l'utilisateur vers une autre activité
            //solution
            AlertDialog.Builder ErrorMsg = new AlertDialog.Builder(this);
            ErrorMsg.setMessage("le téléphone n'a pas de NFC")
                    .setTitle("Erreur");
            ErrorMsg.create();
            ErrorMsg.show();
        }

        viewModel.getTagWritten().observe(this, new Observer<Void>() {
            @Override
            public void onChanged(Void unused) {
                Toast.makeText(NFCWriterActivity.this, "Tag written", Toast.LENGTH_SHORT).show();
            }
        });

        viewModel.getWrittenFailed().observe(this, new Observer<Exception>() {
            @Override
            public void onChanged(Exception e) {
                Toast.makeText(NFCWriterActivity.this, "Tag writing failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        //Disable NFC foreground detection
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }


    /**
     * This method is called when a new intent is detected by the system, for instance when a new NFC tag is detected.
     *
     * @param intent The new intent that was started for the activity.
     */
    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        String action = intent.getAction();
        // check the event was triggered by the tag discovery
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            // get the tag object from the received intent
            if(readCard){
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            viewModel.writeTag(editText.getText().toString(), tag, TAG);}
        } else {
            // TODO Indiquer à l'utilisateur que ce type de tag n'est pas supporté
            Toast.makeText(NFCWriterActivity.this, "Ce Tag n'est pas supporté", Toast.LENGTH_SHORT).show();

        }
    }
}
