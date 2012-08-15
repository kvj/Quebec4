package org.kvj.quebec4.service;

import org.kvj.bravo7.ApplicationContext;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

abstract public class LocationController implements LocationListener {

	protected static final String TAG = "LocationController";
	LocationManager manager = null;
	private String[] locationProviders = { LocationManager.GPS_PROVIDER,
			LocationManager.NETWORK_PROVIDER };
	private LocationTimeout timeout = new LocationTimeout();
	private Handler handler = new Handler();
	private boolean enabled = false;

	class LocationTimeout implements Runnable {

		public void run() {
			if (enabled) {
				if (locationFound(null)) {
					disableLocation();
				}
			}
		}

	}

	public LocationController(ApplicationContext context) {
		manager = (LocationManager) context
				.getSystemService(Context.LOCATION_SERVICE);
	}

	public void onLocationChanged(Location location) {
		Log.i(TAG,
				"Location[" + location.getProvider() + "]: "
						+ location.getAccuracy() + ", " + location.getSpeed());
		if (locationFound(location)) {
			disableLocation();
		}
		// Found
		// loc.put("lon", location.getLongitude());
		// loc.put("lat", location.getLatitude());
		// loc.put("speed", location.getSpeed());
		// loc.put("alt", location.getAltitude());
		// loc.put("acc", location.getAccuracy());
		// loc.put("at", new Date().getTime());
	}

	public void disableLocation() {
		if (enabled) {
			Log.i(TAG, "Disabling location");
			manager.removeUpdates(this);
			handler.removeCallbacks(timeout);
			enabled = false;
			locationFinished();
		}
	}

	public void onProviderDisabled(String provider) {
		Log.i(TAG, "Provider " + provider + " is disabled");
	}

	public void onProviderEnabled(String provider) {
		Log.i(TAG, "Provider " + provider + " is enabled");
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i(TAG, "Provider " + provider + " status changed: " + status);
	}

	public boolean enableLocation(int timeoutMins) {
		if (!enabled) {
			handler.removeCallbacks(timeout);
			if (timeoutMins > 0) {
				handler.postDelayed(timeout, timeoutMins * 60 * 1000);
			}
			for (String prov : locationProviders) {
				if (manager.isProviderEnabled(prov)) { // Prov enabled
					manager.requestLocationUpdates(prov, 0, 0, this);
				}
			}
			enabled = true;
			Log.i(TAG, "All location providers started");
			locationStarted();
			return true;
		}
		return false;
	}

	abstract public void locationStarted();

	abstract public void locationFinished();

	abstract public boolean locationFound(Location location);
}