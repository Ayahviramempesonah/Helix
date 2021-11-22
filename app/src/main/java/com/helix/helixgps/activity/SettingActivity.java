package com.helix.helixgps.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.helix.helixgps.R;
import com.helix.helixgps.helper.SessionManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingActivity extends AppCompatActivity {
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.config_view);
        SessionManager sesi = new SessionManager(getApplicationContext());

        SwitchCompat network_mode = findViewById(R.id.network_mode);
        SwitchCompat sw_coor = findViewById(R.id.sw_coor);
        SwitchCompat sw_muter = findViewById(R.id.sw_muter);
        SwitchCompat sw_acc = findViewById(R.id.sw_acc);
         boolean acc = sesi.getAcc();
        Boolean muter = sesi.getMuter();
        Boolean coor =  sesi.getRandomLat();
        Boolean net = sesi.getNetworkMode();

            sw_acc.setChecked(acc);
            sw_acc.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                  sesi.setAcc(true);
                    return;
                }
                sesi.setAcc(false);

             });
            if (sw_acc.isChecked()) {
                sesi.setAcc(true);
            }


        sw_coor.setChecked(coor);
        sw_coor.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                sesi.setRandomLat(true);
                return;
            }
            sesi.setRandomLat(false);

        });
        if (sw_coor.isChecked()) {
            sesi.setRandomLat(true);
        }
        sw_muter.setChecked(muter);
        sw_muter.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b) {
                sesi.setMuter(true);
                return;
            }
            sesi.setMuter(false);

        });
        if (sw_muter.isChecked()) {
            sesi.setMuter(true);
        }
            network_mode.setChecked(net);
            network_mode.setOnCheckedChangeListener((compoundButton, b) -> {
                if (b) {
                    sesi.setNetworkMode(true);
                    return;
                }
                sesi.setNetworkMode(false);
            });
            if (network_mode.isChecked()) {
                sesi.setNetworkMode(true);
            }

    }
}