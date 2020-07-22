package me.connect.sdk.java.sample;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import me.connect.sdk.java.AgencyConfig;
import me.connect.sdk.java.ConnectMeVcx;
import me.connect.sdk.java.sample.databinding.MainActivityBinding;
import me.connect.sdk.java.sample.ui.main.MainPagerAdapter;

public class MainActivity extends AppCompatActivity {
    private final String TAG = "MainActivity";
    private ConnectMeVcx sdk;


    private MainActivityBinding viewBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewBinding = MainActivityBinding.inflate(getLayoutInflater());
        setContentView(viewBinding.getRoot());

        MainPagerAdapter adapter = new MainPagerAdapter(getSupportFragmentManager());
        viewBinding.pager.setAdapter(adapter);
        viewBinding.tabLayout.setupWithViewPager(viewBinding.pager);

        initSdk();
    }

    private void initSdk() {
        Toast.makeText(this, "Started SDK initialization", Toast.LENGTH_SHORT).show();
        sdk = ConnectMeVcx.builder()
                .withAgency(AgencyConfig.DEFAULT)
                .withGenesisPool(Constants.POOL_TXN_GENESIS)
                .withWalletName(Constants.WALLET_NAME)
                .withContext(this)
                .build();
        // Todo progress bar could be added
        sdk.init().handleAsync((res, err) -> {
            String message;
            if (err == null) {
                message = "SDK initialized successfully.";
            } else {
                message = "SDK was not initialized!";
                Log.e(TAG, "Sdk not initialized: ", err);
            }
            runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_SHORT).show());
            return null;
        });
    }
}