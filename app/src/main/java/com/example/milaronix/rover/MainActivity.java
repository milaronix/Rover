package com.example.milaronix.rover;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBarActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.concurrent.ExecutionException;


public class MainActivity extends ActionBarActivity {
    String respuesta = null;
    String nombre = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final ImageButton ar_izq = (ImageButton) findViewById(R.id.b_ar_izq);
        final ImageButton ar_der = (ImageButton) findViewById(R.id.ar_der);
        final ImageButton ab_izq = (ImageButton) findViewById(R.id.ab_izq);
        final ImageButton ab_der = (ImageButton) findViewById(R.id.ab_der);
        final Switch auto = (Switch) findViewById(R.id.automatico);

        View.OnTouchListener manejador = new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                String pin = null;
                switch (v.getId()){
                    case R.id.b_ar_izq:
                        pin = "D6";
                        nombre = "ar_izq";
                        break;
                    case R.id.ar_der:
                        pin = "D7";
                        nombre = "ar_der";
                        break;
                    case R.id.ab_izq:
                        pin = "D8";
                        nombre = "ab_izq";
                        break;
                    case R.id.ab_der:
                        pin = "D9";
                        nombre = "ab_der";
                        break;
                }

                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    Log.d("down del boton", "down");
                    Toast toast = Toast.makeText(getApplicationContext(), "Down "+nombre, Toast.LENGTH_SHORT);
                    toast.show();

                    try {
                        respuesta = new conexion_http().execute("high", pin).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }else if (event.getAction() == MotionEvent.ACTION_UP){
                    Log.d("up del boton", "up");
                    Toast toast = Toast.makeText(getApplicationContext(), "Up "+nombre, Toast.LENGTH_SHORT);
                    toast.show();

                    try {
                        respuesta = new conexion_http().execute("low", pin).get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }
                return false;
            }
        };

        ar_izq.setOnTouchListener(manejador);
        ar_der.setOnTouchListener(manejador);
        ab_izq.setOnTouchListener(manejador);
        ab_der.setOnTouchListener(manejador);

        auto.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if(isChecked){

                    Toast toast = Toast.makeText(getApplicationContext(), "Automatico Encendido", Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        respuesta = new conexion_http().execute("high", "D5").get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }else{

                    Toast toast = Toast.makeText(getApplicationContext(), "Automatico Apagado", Toast.LENGTH_SHORT);
                    toast.show();
                    try {
                        respuesta = new conexion_http().execute("low", "D5").get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    private class conexion_http extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... accion) {
            String resp = null;
            //Crea conector http y autorizacion
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost("https://devicecloud.digi.com/ws/sci");
            String basicAuth = "Basic " + Base64.encodeToString("milaronix:Gatocagado1.".getBytes(), Base64.NO_WRAP);
            httppost.setHeader("Authorization", basicAuth);

            Log.d("el pin a trabajar ", accion[0]);
            Log.d("estado del pin ", accion[1]);

            try {
                StringEntity se = new StringEntity( "<sci_request version=\"1.0\"> \n" +
                        "  <send_message cache=\"false\"> \n" +
                        "    <targets>\n" +
                        "      <device id=\"00000000-00000000-00409DFF-FF5E0CE5\"/>\n" +
                        "    </targets> \n" +
                        "    <rci_request version=\"1.1\"> \n" +
                        "      <set_setting>\n" +
                        "        <InputOutput>\n" +
                        "          <"+accion[0]+">"+accion[1]+"</"+accion[0]+">\n" +
                        "        </InputOutput>\n" +
                        "      </set_setting>\n" +
                        "    </rci_request>\n" +
                        "  </send_message>\n" +
                        "</sci_request>", HTTP.UTF_8);
                se.setContentType("text/xml");

                // realiza el POST http
                httppost.setEntity(se);
                HttpResponse httpresponse = httpclient.execute(httppost);
                HttpEntity resEntity = httpresponse.getEntity();

                // Pone repuesta en pantalla para verificar
                resp = EntityUtils.toString(resEntity);
                /*EditText respuesta2 = (EditText) rootView.findViewById(R.id.respuesta2);
                respuesta2.setText(resp);*/

            } catch (ClientProtocolException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return resp;
        }
    }
}
