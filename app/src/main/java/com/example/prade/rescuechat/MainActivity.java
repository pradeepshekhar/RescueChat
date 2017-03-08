package com.example.prade.rescuechat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import static java.lang.Math.min;

public class MainActivity extends AppCompatActivity {

    private ListView chatWindow;
    private EditText chatText;
    private Button sendButton;
    private Button scanButton;
    private Button sendListButton;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String DEVICE_ADDRESS = "device_address";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    public static int connectFlag;
    public static String addressFinal;
    private Devices devices;
    private Devices devices1;

    private String names = "";
    public String readAddress;

    private String connectedDeviceName = null;
    private String connectedDeviceAddress = null;
    private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;

    private DeviceDBHandler dbHandler;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            setStatus(
                                    connectedDeviceName);
//                            chatArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    String writeMessage = new String(writeBuf);
                    if(writeMessage.startsWith("TID")) {
                        writeMessage = writeMessage.substring(3);
                        chatArrayAdapter.add("Me:\n" + writeMessage);
                    }
                    else if(writeMessage.startsWith("NFM")){
                        writeMessage = writeMessage.substring(20);
                        chatArrayAdapter.add("Me:\n" + writeMessage);
                    }
                    else if(writeMessage.startsWith("NF2"));
//                        Don't display anything to the user
                    else
                        chatArrayAdapter.add("Me:\n" + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if(readMessage.startsWith("TID") && readMessage.length()>3)
                    {
                        readMessage = readMessage.substring(3);
                        addToTable(readMessage);
                    }
                    else if(readMessage.startsWith("NFM") && readMessage.length()>3)
                    {
                        readAddress = readMessage.substring(3,20);
                        readMessage = readMessage.substring(20);
                        String fromName = connectedDeviceName;
                        String fromAddress = connectedDeviceAddress;
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
//                            e.printStackTrace();
                        }
                        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(readAddress);
                        chatService.connect(device, false);
                        try {
                            Thread.sleep(1800);
                        } catch (InterruptedException e) {
//                            e.printStackTrace();
                        }
                        sendMessage("NF2"+fromName+"\n"+fromAddress+readMessage);
                        chatService.stop();
                        chatService.start();

//                        chatArrayAdapter.add(connectedDeviceName+":\n"+readAddress+readMessage);
                    }
                    else if(readMessage.startsWith("NF2") && readMessage.length()>3)
                    {
                        String [] data = readMessage.split("\n");
                        String name = data[0].substring(3);
                        readMessage = data[1].substring(17);
                        chatArrayAdapter.add(name+":\n"+readMessage);

                    }
                    else
                        chatArrayAdapter.add(connectedDeviceName + ":\n" + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:

                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    connectedDeviceAddress = msg.getData().getString(DEVICE_ADDRESS);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + connectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });

    private void addToTable(String names)
    {
        //This function stores the second hop range devices data into "SecondHopDevices" table
        String [] splitNames = names.split("\r?\n");
        int size = splitNames.length;
        for(int i=0;i<size;i=i+3) {
            String a = splitNames[i];
            String b = splitNames[i + 1];
            String c = splitNames[i + 2];
            devices = new Devices(a, b, c);
            //check if device already exists in table
            int RSSI12 = dbHandler.isExits2(b);


            if(RSSI12!= 1000){  //This condition is satisfied when the device already exists in database
                String addressInter = dbHandler.addressInter(b);
                int RSSI11 = dbHandler.rssi1(addressInter); //rssi between inter for path1 and us
                int RSSI21 = dbHandler.rssi1(connectedDeviceAddress);
                int RSSI22 = Integer.parseInt(c);
                if(min(RSSI11,RSSI12) < min(RSSI21,RSSI22)) { //update the db only if min(path1) < min(path2)
                    dbHandler.updateRow2(devices, b, connectedDeviceName, connectedDeviceAddress);
                }
            }
            else if (RSSI12 == 1000){
                //Add the new device into the table
                dbHandler.addDevice2(devices, connectedDeviceName, connectedDeviceAddress);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        chatWindow = (ListView) findViewById(R.id.chatWindow);
        chatText = (EditText) findViewById(R.id.chatText);
        scanButton = (Button) findViewById(R.id.scanButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        sendListButton = (Button) findViewById(R.id.sendListButton);

        scanButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startScan(v);
                    }
                }
        );

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if(connectFlag == 1) {
                    String message = chatText.getText().toString();
                    sendMessage(message);
                }
                if(connectFlag == 2) {
                    String message = chatText.getText().toString();
                    sendMessage("NFM"+addressFinal+message);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
////                            e.printStackTrace();
//                    }
                    chatService.stop();
                    chatService.start();
                }
            }
        });
        ensureDiscoverable();

        dbHandler = new DeviceDBHandler(this);

        sendListButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int count1 = dbHandler.getCount1();
                        String recNames ="";
                        String [] dbNames ;
                        for(int i=0;i<count1;i++){
                            dbNames = dbHandler.deviceAt1(i+1);
                            recNames+= dbNames[0]+"\n"+dbNames[1]+"\n"+dbNames[2]+"\n";
                        }
                        recNames="TID"+recNames;
                        sendMessage(recNames);
//                        showToast();
                    }
                }
        );
    }

    private final void setStatus(int resId) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }
    private void startScan(View view){
        int count = dbHandler.getCount2();
        String recNames ="";
        String [] dbNames;
        for(int i=0;i<count;i++){
            dbNames = dbHandler.deviceAt2(i+1);
            recNames+= dbNames[0]+"\n"+dbNames[1]+"\n"+dbNames[2]+"\n"+dbNames[3]+"\n"+dbNames[4]+"\n";
        }
//        Toast.makeText(this,dbNames[1],Toast.LENGTH_LONG).show();
        Intent serverIntent = new Intent(this, ScanDevices.class);
        serverIntent.putExtra("serverIntent",recNames);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(
                ScanDevices.DEVICE_ADDRESS);
        addressFinal = data.getExtras().getString(
                ScanDevices.DEVICE_2_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        chatService.connect(device, secure);
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            ensureDiscoverable();
        }
        else {
            if (chatService == null)
                setupChat();
        }
    }

    private void sendMessage(String message) {
        if (chatService.getState() != ChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);
            outStringBuffer.setLength(0);
            chatText.setText(outStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private void setupChat() {
        chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        chatWindow.setAdapter(chatArrayAdapter);

        chatService = new ChatService(this, handler);

        outStringBuffer = new StringBuffer("");
    }



    @Override
    public synchronized void onResume() {
        super.onResume();

        if (chatService != null) {
            if (chatService.getState() == ChatService.STATE_NONE) {
                chatService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        dbHandler.close();
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }

}