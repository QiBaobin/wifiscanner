package com.example.wifilocator;

import java.util.List;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity
    extends ListActivity
{
    private boolean mScanning;

    private BroadcastReceiver mReceiver;

    @Override
    public void onCreate( final Bundle savedInstanceState )
    {
        super.onCreate(savedInstanceState);
        bindRecentScanResult();
        scanWifi();
    }

    @Override
    protected void onDestroy()
    {
        if (mReceiver != null)
        {
            unregisterReceiver(mReceiver);
            mReceiver = null;
        }
        super.onDestroy();
    }

    private void scanWifi()
    {
        if (mScanning)
        {
            return;
        }

        mScanning = true;
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        if (!wm.isWifiEnabled())
        {
            wm.setWifiEnabled(true);
        }
        if (mReceiver == null)
        {
            mReceiver = new BroadcastReceiver()
                {

                    @Override
                    public void onReceive( final Context context, final Intent intent )
                    {
                        bindRecentScanResult();
                    }

                };
            registerReceiver(mReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        wm.startScan();
    }

    void bindRecentScanResult()
    {
        WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
        List<ScanResult> result = wm.getScanResults();
        if (result == null || result.isEmpty())
        {
            setListAdapter(null);
        }
        else
        {
            setListAdapter(new Adapter(result));
        }
        Toast.makeText(getApplicationContext(), "Scan finished", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu( final Menu menu )
    {
        menu.add(0, 0, 0, "Refresh");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected( final int featureId, final MenuItem item )
    {
        scanWifi();
        return true;
    }

    static class Adapter
        extends BaseAdapter
    {
        private final List<ScanResult> mSource;

        public Adapter( final List<ScanResult> results )
        {
            mSource = results;
        }

        @Override
        public int getCount()
        {
            return mSource.size();
        }

        @Override
        public ScanResult getItem( final int position )
        {
            return mSource.get(position);
        }

        @Override
        public long getItemId( final int position )
        {
            return position;
        }

        @Override
        public View getView( final int position, final View convertView, final ViewGroup parent )
        {
            View item = convertView;
            if (convertView == null)
            {
                item = LayoutInflater.from(parent.getContext()).inflate(R.layout.item, parent, false);
            }

            ScanResult result = getItem(position);
            ((TextView) item.findViewById(R.id.ssid)).setText(result.SSID);
            ((TextView) item.findViewById(R.id.bssid)).setText(result.BSSID);
            ((TextView) item.findViewById(R.id.distance)).setText(String.format("%f meters", calculateDistance(result)));
            return item;
        }

        private double calculateDistance( final ScanResult result )
        {
            return MainActivity.calculateDistance(result.level, result.frequency);
        }

    }

    public static double calculateDistance( final double signalLevelInDb, final double freqInMHz )
    {
        double exp = (27.55 - (20 * Math.log10(freqInMHz)) - signalLevelInDb) / 20.0;
        return Math.pow(10.0, exp);
    }
}
