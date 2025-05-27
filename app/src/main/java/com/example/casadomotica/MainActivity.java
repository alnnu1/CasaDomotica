package com.example.casadomotica;

import static android.content.ContentValues.TAG;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private String deviceName;
    private String deviceAddress;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    public final static int CONNECTING_STATUS = 1; // used in bluetooth handler to identify message status
    public final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message

    private ToggleButton PuertaGaraje;
    private CheckBox PuertaGarajeSensor;

    private ToggleButton Humedad;
    private CheckBox HumedadSensor;

    private SeekBar FocoSegundoPiso;
    private CheckBox FocoSegundoPisoSensor;

    private SeekBar FocoPrimerPiso;
    private CheckBox FocoPrimerPisoSensor;

    private SeekBar FocoAfuera;
    private CheckBox FocoAfueraSensor;


    private ToggleButton Ventilador;
    private CheckBox VentiladorSensor;

    private ToggleButton Ropa;
    private CheckBox RopaSensor;


    private ToggleButton PuertaSeguridad;
    private CheckBox PuertaSeguridadSensor;

    private Button Elevador;
    private Boolean ElevadorASegundaPlanta = false;
    private Button ForzarParo;

    private final static String CMD_STATUS = "status";

    private final static String CMD_ENABLE_SENSOR = "ES";

    private final static String CMD_DISABLE_SENSOR = "ED";
    private Boolean EstableciendoValores = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        final Button buttonConnect = findViewById(R.id.buttonConnect);
        final Toolbar toolbar = findViewById(R.id.toolbar);
        final ProgressBar progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);

        PuertaGaraje = findViewById((R.id.Garaje));
        PuertaGarajeSensor = findViewById((R.id.GarajeSensor));

        Humedad = findViewById(R.id.Humedad);
        HumedadSensor = findViewById(R.id.HumedadSensor);

        FocoSegundoPiso = findViewById(R.id.FocoSegundoPiso);
        FocoSegundoPisoSensor = findViewById(R.id.FocoSegundoPisoSensor);

        FocoPrimerPiso = findViewById(R.id.FocoPrimerPiso);
        FocoPrimerPisoSensor = findViewById(R.id.FocoPrimerPisoSensor);

        FocoAfuera = findViewById(R.id.FocoAfuera);
        FocoAfueraSensor = findViewById(R.id.FocoAfueraSensor);

        Ventilador = findViewById(R.id.Ventilador);
        VentiladorSensor = findViewById(R.id.VentiladorSensor);

        Ropa = findViewById(R.id.Ropa);
        RopaSensor = findViewById(R.id.RopaSensor);

        PuertaSeguridad = findViewById(R.id.PuertaSeguridad);
        PuertaSeguridadSensor = findViewById(R.id.PuertaSeguridadSensor);

        Elevador = findViewById(R.id.Elevador);
        ForzarParo = findViewById(R.id.ForzarParo);

        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progree and connection status
            Log.d(TAG, "Connecting to " + deviceName);
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);
            buttonConnect.setEnabled(false);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }

        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case CONNECTING_STATUS:
                        switch (msg.arg1) {
                            case 1:
                                toolbar.setSubtitle("Conectado a " + deviceName);
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                // Send command to Arduino board
                                connectedThread.write(CMD_STATUS);
                                break;
                            case -1:
                                toolbar.setSubtitle("Device fails to connect");
                                progressBar.setVisibility(View.GONE);
                                buttonConnect.setEnabled(true);
                                break;
                        }
                        break;

                    case MESSAGE_READ:
                        String arduinoMsg = msg.obj.toString(); // Read message from Arduino
                        Log.e(TAG, arduinoMsg);

                        //Elevador termino de moverse
                        if (arduinoMsg.trim().equals("S")) {
                            Elevador.setEnabled(true);
                            break;
                        }

                        String[] parts = arduinoMsg.split(",");

                        Boolean enableSensor = parts[1].trim().equals("E");

                        Funcion funcion = Funcion.fromValor(Integer.parseInt(parts[0].trim()));
                        EstadoDigital estadoDigital = null;
                        EstadoFuncionAnalogo estadoFuncionAnalogo  = null;
                        EstableciendoValores = true;

                        Log.e(TAG, String.valueOf(funcion.getValor()));

                        switch (funcion) {
                            case LUZ_PRIMERA_PLANTA:
                            case LUZ_SEGUNDA_PLANTA:
                            case LUZ_AFUERA:
                                if (enableSensor) {
                                    estadoFuncionAnalogo = new EstadoFuncionAnalogo(funcion, true);
                                } else {
                                    estadoFuncionAnalogo = new EstadoFuncionAnalogo(funcion, Integer.parseUnsignedInt(parts[1].trim()));
                                    Log.d(TAG, String.valueOf(estadoFuncionAnalogo.valor));
                                }
                                break;
                            default:
                                estadoDigital = EstadoDigital.fromValor(Integer.parseInt(parts[1].trim()));
                                Log.e(TAG, String.valueOf(estadoDigital.getValor()));
                                break;
                        }


                        if (estadoFuncionAnalogo != null) {
                            ControlarFunciones(estadoFuncionAnalogo);
                            break;
                        }

                        //Temporal
                        if (estadoDigital == null) {
                            break;
                        }

                        ControlarFunciones(new EstadoFuncion(funcion, estadoDigital));

                        if (funcion == Funcion.ELEVADOR) {
                            EstableciendoValores = false;
                        }
                        break;
                }
            }
        };

        buttonConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Move to adapter list
                Intent intent = new Intent(MainActivity.this, SelectDeviceActivity.class);
                startActivity(intent);
            }
        });

        PuertaGarajeSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PuertaGaraje.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("1,2");
                    } else {
                        connectedThread.write(PuertaGaraje.isChecked() ? "1,1" : "1,0");
                    }
                }
            }
        });

        PuertaGaraje.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("1,1");
                    } else {
                        connectedThread.write("1,0");
                    }
                }
            }
        });

        HumedadSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Humedad.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("2,2");
                    } else {
                        connectedThread.write(Humedad.isChecked() ? "2,1" : "2,0");
                    }
                }
            }
        });

        Humedad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("2,1");
                    } else {
                        connectedThread.write("2,0");
                    }
                }
            }
        });

        FocoSegundoPiso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    seekBar.setProgress(progress);
                }
            }

            // This method is called when the user starts touching the seek bar.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // This method is called when the user stops touching the seek bar.
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String returnValue = "3," + String.valueOf(seekBar.getProgress());
                Log.e(TAG, returnValue);
                if (!EstableciendoValores) {
                    connectedThread.write(returnValue);
                }
            }
        });

        FocoSegundoPisoSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FocoSegundoPiso.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("3," + CMD_ENABLE_SENSOR);
                    } else {
                        connectedThread.write("3," + CMD_DISABLE_SENSOR);
                    }
                }
            }
        });

        FocoPrimerPiso.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    seekBar.setProgress(progress);
                }
            }

            // This method is called when the user starts touching the seek bar.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // This method is called when the user stops touching the seek bar.
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String returnValue = "4," + String.valueOf(seekBar.getProgress());
                Log.e(TAG, returnValue);
                if (!EstableciendoValores) {
                    connectedThread.write(returnValue);
                }
            }
        });

        FocoPrimerPisoSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FocoPrimerPiso.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("4," + CMD_ENABLE_SENSOR);
                    } else {
                        String returnValue = "4," + String.valueOf(FocoSegundoPiso.getProgress());
                        connectedThread.write("4," + CMD_DISABLE_SENSOR);
                    }
                }
            }
        });

        FocoAfuera.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    seekBar.setProgress(progress);
                }
            }

            // This method is called when the user starts touching the seek bar.
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            // This method is called when the user stops touching the seek bar.
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                String returnValue = "5," + String.valueOf(seekBar.getProgress());
                Log.e(TAG, returnValue);
                if (!EstableciendoValores) {
                    connectedThread.write(returnValue);
                }
            }
        });

        FocoAfueraSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                FocoAfuera.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("5," + CMD_ENABLE_SENSOR);
                    } else {
                        connectedThread.write("5," + CMD_DISABLE_SENSOR);
                    }
                }
            }
        });

        VentiladorSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Ventilador.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("6,2");
                    } else {
                        connectedThread.write(Ventilador.isChecked() ? "6,1" : "6,0");
                    }
                }
            }
        });

        Ventilador.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("6,1");
                    } else {
                        connectedThread.write("6,0");
                    }
                }
            }
        });

        RopaSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Ropa.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("7,2");
                    } else {
                        connectedThread.write(Ropa.isChecked() ? "7,1" : "7,0");
                    }
                }
            }
        });

        Ropa.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("7,1");
                    } else {
                        connectedThread.write("7,0");
                    }
                }
            }
        });

        PuertaSeguridadSensor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                PuertaSeguridad.setEnabled(!isChecked);
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("8,2");
                    } else {
                        connectedThread.write(PuertaSeguridad.isChecked() ? "8,1" : "8,0");
                    }
                }
            }
        });

        PuertaSeguridad.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (!EstableciendoValores) {
                    if (isChecked) {
                        connectedThread.write("8,1");
                    } else {
                        connectedThread.write("8,0");
                    }
                }
            }
        });

        Elevador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Elevador.getText().toString().equals("Segunda planta")) {
                    Elevador.setText("Primera planta");
                    connectedThread.write("9,1");
                    ElevadorASegundaPlanta = true;
                } else if (Elevador.getText().toString().equals("Primera planta")) {
                    Elevador.setText("Segunda planta");
                    connectedThread.write("9,0");
                    ElevadorASegundaPlanta = false;
                }

                Elevador.setEnabled(false);
                ForzarParo.setEnabled(true);
            }
        });

        ForzarParo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ElevadorASegundaPlanta) {
                    connectedThread.write("10,1");
                } else {
                    connectedThread.write("10,0");
                }
                Elevador.setEnabled(true);
                ForzarParo.setEnabled(false);
            }
        });
    }

    public void ControlarFunciones(EstadoFuncion estadoFuncion) {
        switch (estadoFuncion.funcion) {
            case PUERTA_GARAJE:
                if (estadoFuncion.estadoDigital == EstadoDigital.SENSOR_ON) {
                    PuertaGaraje.setEnabled(false);
                    PuertaGarajeSensor.setChecked(true);
                } else {
                    PuertaGaraje.setChecked(estadoFuncion.estadoDigital != EstadoDigital.FORCE_OFF);
                }
                break;
            case AGUA_PLANTA:
                if (estadoFuncion.estadoDigital == EstadoDigital.SENSOR_ON) {
                    Humedad.setEnabled(false);
                    HumedadSensor.setChecked(true);
                } else {
                    Humedad.setChecked(estadoFuncion.estadoDigital != EstadoDigital.FORCE_OFF);
                }
                break;
            case VENTILADOR:
                if (estadoFuncion.estadoDigital == EstadoDigital.SENSOR_ON) {
                    Ventilador.setEnabled(false);
                    VentiladorSensor.setChecked(true);
                } else {
                    VentiladorSensor.setChecked(estadoFuncion.estadoDigital != EstadoDigital.FORCE_OFF);
                }
                break;
            case ROPA:
                if (estadoFuncion.estadoDigital == EstadoDigital.SENSOR_ON) {
                    Ropa.setEnabled(false);
                    RopaSensor.setChecked(true);
                } else {
                    RopaSensor.setChecked(estadoFuncion.estadoDigital != EstadoDigital.FORCE_OFF);
                }
                break;
            case SEGURIDAD_PUERTA:
                if (estadoFuncion.estadoDigital == EstadoDigital.SENSOR_ON) {
                    PuertaSeguridad.setEnabled(false);
                    PuertaSeguridadSensor.setChecked(true);
                } else {
                    PuertaSeguridad.setChecked(estadoFuncion.estadoDigital != EstadoDigital.FORCE_OFF);
                }
                break;
            case ELEVADOR:
                ForzarParo.setEnabled(false);
                if (estadoFuncion.estadoDigital == EstadoDigital.FORCE_ON) {
                    ElevadorASegundaPlanta = true;
                    Elevador.setText("Primera planta");
                } else if (estadoFuncion.estadoDigital == EstadoDigital.FORCE_OFF) {
                    ElevadorASegundaPlanta = false;
                    Elevador.setText("Segunda planta");
                }
        }
    }

    public void ControlarFunciones(EstadoFuncionAnalogo estadoFuncionAnalogo) {
        switch (estadoFuncionAnalogo.funcion) {
            case LUZ_SEGUNDA_PLANTA:
                if (estadoFuncionAnalogo.UsarSensor) {
                    FocoSegundoPisoSensor.setChecked(true);
                    FocoSegundoPiso.setEnabled(false);
                } else {
                    FocoSegundoPiso.setProgress(estadoFuncionAnalogo.valor);
                }
                break;
            case LUZ_PRIMERA_PLANTA:
                if (estadoFuncionAnalogo.UsarSensor) {
                    FocoPrimerPisoSensor.setChecked(true);
                    FocoPrimerPiso.setEnabled(false);
                } else {
                    FocoPrimerPiso.setProgress(estadoFuncionAnalogo.valor);
                }
                break;
            case LUZ_AFUERA:
                if (estadoFuncionAnalogo.UsarSensor) {
                    FocoAfueraSensor.setChecked(true);
                    FocoAfuera.setEnabled(false);
                } else {
                    FocoAfuera.setProgress(estadoFuncionAnalogo.valor);
                }
                break;
        }
    }

    /* ============================ Thread to Create Bluetooth Connection =================================== */
    public static class CreateConnectThread extends Thread {

        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createInsecureRfcommSocketToServiceRecord(uuid);

            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
                Log.e("Status", "Device connected");
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.e("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            connectedThread = new ConnectedThread(mmSocket);
            connectedThread.run();
            connectedThread.write(CMD_STATUS);
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer =========================================== */
    public static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
            }
        }
    }

    /* ============================ Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }
}