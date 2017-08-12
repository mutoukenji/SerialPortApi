package tech.yaog.hardwares.serialport;

import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.Vector;

/**
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class SerialPortFinder {
    private static final String TAG = SerialPortFinder.class.getName();

	private class Driver {
		Driver(String name, String root) {
			mDriverName = name;
			mDeviceRoot = root;
		}
		private String mDriverName;
		private String mDeviceRoot;
		Vector<File> mDevices = null;

        Vector<File> getDevices() {
			if (mDevices == null) {
				mDevices = new Vector<>();
				File dev = new File("/dev");
				File[] files = dev.listFiles();
				int i;
				for (i=0; i<files.length; i++) {
					if (files[i].getAbsolutePath().startsWith(mDeviceRoot)) {
						Log.d(TAG, "Found new device: " + files[i]);
						mDevices.add(files[i]);
					}
				}
			}
			return mDevices;
		}
		String getName() {
			return mDriverName;
		}
	}

	private Vector<Driver> mDrivers = null;

	private Vector<Driver> getDrivers() throws IOException {
		if (mDrivers == null) {
			mDrivers = new Vector<Driver>();
			LineNumberReader r = new LineNumberReader(new FileReader("/proc/tty/drivers"));
			String l;
			while((l = r.readLine()) != null) {
				// Issue 3:
				// Since driver name may contain spaces, we do not extract driver name with split()
				String drivername = l.substring(0, 0x15).trim();
				String[] w = l.split(" +");
				if ((w.length >= 5) && (w[w.length-1].equals("serial"))) {
					Log.d(TAG, "Found new driver " + drivername + " on " + w[w.length-4]);
					mDrivers.add(new Driver(drivername, w[w.length-4]));
				}
			}
			r.close();
		}
		return mDrivers;
	}

    /**
     * Get all defined devices
     * @return device list
     */
    public String[] getAllDevices() {
		Vector<String> devices = new Vector<String>();
		// Parse each driver
		Iterator<Driver> itdriv;
		try {
			itdriv = getDrivers().iterator();
			while(itdriv.hasNext()) {
				Driver driver = itdriv.next();
                for (File file : driver.getDevices()) {
                    String device = file.getName();
                    String value = String.format("%s (%s)", device, driver.getName());
                    devices.add(value);
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
	}

    /**
     * Get all defined device io files
     * @return device path list
     */
	public String[] getAllDevicesPath() {
		Vector<String> devices = new Vector<>();
		// Parse each driver
		Iterator<Driver> itdriv;
		try {
			itdriv = getDrivers().iterator();
			while(itdriv.hasNext()) {
				Driver driver = itdriv.next();
                for (File file : driver.getDevices()) {
                    String device = file.getAbsolutePath();
                    devices.add(device);
                }
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return devices.toArray(new String[devices.size()]);
	}
}
