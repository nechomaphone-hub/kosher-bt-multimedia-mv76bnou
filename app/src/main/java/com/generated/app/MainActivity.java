package com.generated.app;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity {

    private LinearLayout rootLayout;
    private LinearLayout contentContainer;
    private int currentTab = 0; // 0: Dialer, 1: Contacts, 2: History, 3: BT, 4: Settings

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<String> deviceList = new ArrayList<>();
    private ArrayAdapter<String> deviceAdapter;
    
    private ArrayList<String> contactsList = new ArrayList<>();
    private ArrayList<String> historyList = new ArrayList<>();
    
    private EditText numberInput;
    private TextView btStatusText;
    
    private BroadcastReceiver btReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {
                    String name = device.getName() != null ? device.getName() : "התקן לא ידוע";
                    String info = name + "\n" + device.getAddress();
                    if (!deviceList.contains(info)) {
                        deviceList.add(info);
                        if (deviceAdapter != null) {
                            deviceAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        
        rootLayout = new LinearLayout(this);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        rootLayout.setBackgroundColor(0xFFF5F5F5);

        setupTopBar();
        setupContentContainer();
        setupBottomNav();

        loadTab(0);
        checkAndRequestPermissions();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(btReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(btReceiver);
        } catch (Exception e) {
            // ignored
        }
    }

    private void checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ArrayList<String> perms = new ArrayList<>();
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    perms.add(Manifest.permission.BLUETOOTH_CONNECT);
                }
                if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    perms.add(Manifest.permission.BLUETOOTH_SCAN);
                }
            }
            if (checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.READ_CONTACTS);
            }
            if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.READ_CALL_LOG);
            }
            if (checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                perms.add(Manifest.permission.CALL_PHONE);
            }
            if (!perms.isEmpty()) {
                requestPermissions(perms.toArray(new String[0]), 101);
            }
        }
    }

    private void setupTopBar() {
        TextView titleBar = new TextView(this);
        titleBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 120));
        titleBar.setText(R.string.app_name);
        titleBar.setTextColor(0xFFFFFFFF);
        titleBar.setTextSize(18);
        titleBar.setGravity(Gravity.CENTER);
        titleBar.setBackgroundColor(0xFF1976D2);
        rootLayout.addView(titleBar);
    }

    private void setupContentContainer() {
        contentContainer = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f);
        contentContainer.setLayoutParams(params);
        contentContainer.setOrientation(LinearLayout.VERTICAL);
        contentContainer.setPadding(32, 32, 32, 32);
        rootLayout.addView(contentContainer);
    }

    private void setupBottomNav() {
        LinearLayout navBar = new LinearLayout(this);
        navBar.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 140));
        navBar.setOrientation(LinearLayout.HORIZONTAL);
        navBar.setBackgroundColor(0xFFFFFFFF);

        String[] tabs = {
                getString(R.string.tab_dialer),
                getString(R.string.tab_contacts),
                getString(R.string.tab_calls),
                getString(R.string.tab_bt),
                getString(R.string.tab_settings)
        };

        for (int i = 0; i < tabs.length; i++) {
            final int index = i;
            Button btn = new Button(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
            btn.setLayoutParams(lp);
            btn.setText(tabs[i]);
            btn.setTextSize(12);
            btn.setBackgroundColor(0x00000000);
            btn.setTextColor(0xFF1976D2);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    loadTab(index);
                }
            });
            navBar.addView(btn);
        }

        rootLayout.addView(navBar);
        setContentView(rootLayout);
    }

    private void loadTab(int tabIndex) {
        currentTab = tabIndex;
        contentContainer.removeAllViews();

        switch (tabIndex) {
            case 0:
                initDialerTab();
                break;
            case 1:
                initContactsTab();
                break;
            case 2:
                initHistoryTab();
                break;
            case 3:
                initBluetoothTab();
                break;
            case 4:
                initSettingsTab();
                break;
        }
    }

    private void initDialerTab() {
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);

        numberInput = new EditText(this);
        numberInput.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 120));
        numberInput.setHint("הכנס מספר טלפון...");
        numberInput.setTextSize(22);
        numberInput.setGravity(Gravity.CENTER);
        layout.addView(numberInput);

        // Keypad grid simulation
        String[][] keys = {
                {"1", "2", "3"},
                {"4", "5", "6"},
                {"7", "8", "9"},
                {"*", "0", "#"}
        };

        for (String[] rowKeys : keys) {
            LinearLayout row = new LinearLayout(this);
            row.setLayoutParams(new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
            row.setOrientation(LinearLayout.HORIZONTAL);

            for (final String key : rowKeys) {
                Button btn = new Button(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        0, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                lp.setMargins(8, 8, 8, 8);
                btn.setLayoutParams(lp);
                btn.setText(key);
                btn.setTextSize(24);
                btn.setBackgroundResource(R.drawable.bg_card);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        numberInput.append(key);
                    }
                });
                row.addView(btn);
            }
            layout.addView(row);
        }

        Button callBtn = new Button(this);
        LinearLayout.LayoutParams callLp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 140);
        callLp.setMargins(0, 16, 0, 0);
        callBtn.setLayoutParams(callLp);
        callBtn.setText(R.string.call_button);
        callBtn.setTextColor(0xFFFFFFFF);
        callBtn.setTextSize(18);
        callBtn.setBackgroundResource(R.drawable.bg_button_green);
        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String num = numberInput.getText().toString().trim();
                if (!num.isEmpty()) {
                    performCall(num);
                } else {
                    Toast.makeText(MainActivity.this, "נא להכניס מספר חיוג", Toast.LENGTH_SHORT).show();
                }
            }
        });
        layout.addView(callBtn);

        contentContainer.addView(layout);
    }

    private void initContactsTab() {
        contactsList.clear();
        if (checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, null, null,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                    String phone = cursor.getString(cursor.getColumnIndexOrThrow(
                            ContactsContract.CommonDataKinds.Phone.NUMBER));
                    contactsList.add(name + "\n" + phone);
                }
                cursor.close();
            }
        }

        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, contactsList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = contactsList.get(position);
                String[] parts = item.split("\n");
                if (parts.length > 1) {
                    performCall(parts[1]);
                }
            }
        });

        contentContainer.addView(listView);
    }

    private void initHistoryTab() {
        historyList.clear();
        if (checkSelfPermission(Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED) {
            Cursor cursor = getContentResolver().query(
                    CallLog.Calls.CONTENT_URI,
                    null, null, null,
                    CallLog.Calls.DATE + " DESC LIMIT 50");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER));
                    String date = cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE));
                    int type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE));
                    String typeStr = "שיחה";
                    if (type == CallLog.Calls.INCOMING_TYPE) typeStr = "נכנסת";
                    else if (type == CallLog.Calls.OUTGOING_TYPE) typeStr = "יוצאת";
                    else if (type == CallLog.Calls.MISSED_TYPE) typeStr = "לא נענתה";

                    historyList.add(typeStr + ": " + number);
                }
                cursor.close();
            }
        }

        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = historyList.get(position);
                String[] parts = item.split(": ");
                if (parts.length > 1) {
                    performCall(parts[1]);
                }
            }
        });

        contentContainer.addView(listView);
    }

    private void initBluetoothTab() {
        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        btStatusText = new TextView(this);
        btStatusText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        btStatusText.setTextSize(16);
        btStatusText.setPadding(0, 0, 0, 24);
        
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            btStatusText.setText(R.string.bt_status_disconnected);
            btStatusText.setTextColor(0xFFD32F2F);
        } else {
            btStatusText.setText("Bluetooth כבוי במכשיר");
            btStatusText.setTextColor(0xFFD32F2F);
        }
        layout.addView(btStatusText);

        Button scanBtn = new Button(this);
        scanBtn.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 120));
        scanBtn.setText("סרוק התקנים קרובים (טלפון כשר)");
        scanBtn.setTextColor(0xFFFFFFFF);
        scanBtn.setBackgroundResource(R.drawable.bg_button);
        scanBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bluetoothAdapter != null) {
                    if (!bluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        try {
                            startActivityForResult(enableBtIntent, 1);
                        } catch (SecurityException e) {
                            Toast.makeText(MainActivity.this, "חסרות הרשעות Bluetooth", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        deviceList.clear();
                        Set<Runnable> pairedDevices = (Set<Runnable>) (Set) bluetoothAdapter.getBondedDevices();
                        for (Object d : pairedDevices) {
                            BluetoothDevice dev = (BluetoothDevice) d;
                            deviceList.add(dev.getName() + "\n" + dev.getAddress() + " (מוצמד)");
                        }
                        if (deviceAdapter != null) {
                            deviceAdapter.notifyDataSetChanged();
                        }
                        try {
                            bluetoothAdapter.startDiscovery();
                            Toast.makeText(MainActivity.this, "סורק התקנים...", Toast.LENGTH_SHORT).show();
                        } catch (SecurityException e) {
                            // ignored
                        }
                    }
                }
            }
        });
        layout.addView(scanBtn);

        ListView listView = new ListView(this);
        listView.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 0, 1.0f));
        
        deviceAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
        listView.setAdapter(deviceAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                btStatusText.setText(R.string.bt_status_connected);
                btStatusText.setTextColor(0xFF388E3C);
                Toast.makeText(MainActivity.this, "התחבר בהצלחה כדיבורית", Toast.LENGTH_SHORT).show();
                
                // Route audio through bluetooth sco
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (am != null) {
                    am.setBluetoothScoOn(true);
                    am.startBluetoothSco();
                }
            }
        });
        layout.addView(listView);

        contentContainer.addView(layout);
    }

    private void initSettingsTab() {
        ScrollView scroll = new ScrollView(this);
        scroll.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        LinearLayout layout = new LinearLayout(this);
        layout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView title = new TextView(this);
        title.setText(R.string.instructions_title);
        title.setTextSize(18);
        title.setTextColor(0xFF1976D2);
        title.setPadding(0, 0, 0, 16);
        layout.addView(title);

        TextView content = new TextView(this);
        content.setText(R.string.instructions_content);
        content.setTextSize(14);
        content.setTextColor(0xFF333333);
        content.setLineSpacing(1.2f, 1.2f);
        layout.addView(content);

        Button permBtn = new Button(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, 120);
        lp.setMargins(0, 32, 0, 0);
        permBtn.setLayoutParams(lp);
        permBtn.setText(R.string.grant_permissions);
        permBtn.setTextColor(0xFFFFFFFF);
        permBtn.setBackgroundResource(R.drawable.bg_button);
        permBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndRequestPermissions();
            }
        });
        layout.addView(permBtn);

        scroll.addView(layout);
        contentContainer.addView(scroll);
    }

    private void performCall(String number) {
        if (checkSelfPermission(Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(Intent.ACTION_CALL);
            intent.setData(Uri.parse("tel:" + number));
            try {
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "שגיאה בביצוע חיוג", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "חסרת הרשעת ביצוע שיחות", Toast.LENGTH_SHORT).show();
            checkAndRequestPermissions();
        }
    }
}
