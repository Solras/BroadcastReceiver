package com.example.pica.zcall;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.util.Xml;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "LLAMADAS: ";
    private int[][] llamadas = new int[7][2];
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        webView = (WebView) findViewById(R.id.webView);

        Llamada reclla = new Llamada();
        registerReceiver(reclla, new IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED));

        leer();
        webView.loadUrl("file:///android_asset/javascript.html");
        WebSettings ws = webView.getSettings();
        ws.setJavaScriptEnabled(true);
        webView.addJavascriptInterface(this, "interfaz");
    }

    @JavascriptInterface
    public int dia(int i, int j){
        return llamadas[i][j];
    }

    public class Llamada extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);

                if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    GregorianCalendar g = new GregorianCalendar();
                    Toast.makeText(context, "Phone Is Ringing " + g.get(Calendar.DAY_OF_WEEK), Toast.LENGTH_LONG).show();
                    int id = g.get(Calendar.DAY_OF_WEEK);
                    llamadas[id - 1][0]++;
                    escribir();
                }

                if (state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    GregorianCalendar g = new GregorianCalendar();
                    int id = g.get(Calendar.DAY_OF_WEEK);
                    llamadas[id - 1][1]++;
                    Toast.makeText(context, "Call Ougoing" + llamadas[id - 1][1], Toast.LENGTH_LONG).show();
                    escribir();
                }

                if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    Toast.makeText(context, "Phone Is Idle", Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }


    public class ReceptorLlamada extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final Context c = context;
            TelephonyManager tm = (TelephonyManager)
                    context.getSystemService(Context.TELEPHONY_SERVICE);
            tm.listen(new PhoneStateListener() {
                public void onCallStateChanged(int state, String incomingNumber) {
                    Log.v("MILOG", "estado: " + state + " " + incomingNumber);
                    if (state == Call.STATE_NEW) {
                        //Registrar el teléfono en la memoria externa en un documento
                        try {
                            File ruta_sd = Environment.getExternalStorageDirectory();

                            File f = new File(ruta_sd.getAbsolutePath(), "llamadas.txt");

                            OutputStreamWriter fout = new OutputStreamWriter(
                                    new FileOutputStream(f, true));

                            Date date = new Date();
                            fout.write("Numero" + incomingNumber + "Fecha" + date.toString() + "\n");
                            fout.close();
                        } catch (Exception ex) {
                            Log.e("Ficheros", "Error al escribir fichero a tarjeta SD");
                        }
                    }
                }

            }, PhoneStateListener.LISTEN_CALL_STATE);
        }
    }


    private String diaSemana(int i) {
        switch (i) {
            case 1:
                return "domingo";
            case 2:
                return "lunes";
            case 3:
                return "martes";
            case 4:
                return "miercoles";
            case 5:
                return "jueves";
            case 6:
                return "viernes";
            case 7:
                return "sabado";
            default:
                return "";
        }
    }

    public void escribir() {
        FileOutputStream fosxml = null;
        try {
            fosxml = new FileOutputStream(new File(getExternalFilesDir(null), "llamadas.xml"));
            XmlSerializer docxml = Xml.newSerializer();
            docxml.setOutput(fosxml, "UTF-8");
            docxml.startDocument(null, true);
            docxml.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);

            docxml.startTag(null, "llamadas");

            leer();
            for (int i = 1; i <= 7; i++) {
                docxml.startTag(null, diaSemana(i));
                docxml.attribute(null, "id", Integer.toString(i));
                docxml.attribute(null, "entrantes", Integer.toString(llamadas[i - 1][0]));
                docxml.attribute(null, "salientes", Integer.toString(llamadas[i - 1][1]));
                docxml.endTag(null, diaSemana(i));
            }

            docxml.endTag(null, "llamadas");
            docxml.endDocument();
            docxml.flush();
            fosxml.close();
        } catch (FileNotFoundException e) {
            Log.v("LLAMADAS", "FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            Log.v("LLAMADAS", "IOException: " + e.getMessage());
        }
    }

    public void leer() {
        XmlPullParser lectorxml = Xml.newPullParser();
        int evento;
        try {
            lectorxml.setInput(new FileInputStream(new File(getExternalFilesDir(null), "llamadas.xml")), "utf-8");
            evento = lectorxml.getEventType();

            while (evento != XmlPullParser.END_DOCUMENT) {
                if (evento == XmlPullParser.START_TAG) {
                    String etiqueta = lectorxml.getName();
                    if (etiqueta.compareTo("llamadas") != 0) {
                        int id = Integer.parseInt(lectorxml.getAttributeValue(0));
                        llamadas[id - 1][0] = Integer.parseInt(lectorxml.getAttributeValue(1));
                        llamadas[id - 1][1] = Integer.parseInt(lectorxml.getAttributeValue(2));
                        Log.v(TAG, etiqueta + " " + id + " " + llamadas[id - 1][0] + " " + llamadas[id - 1][1]);
                    }
                }
                evento = lectorxml.next();
            }

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
