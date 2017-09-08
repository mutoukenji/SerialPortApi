/*
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

package tech.yaog.hardwares.serialport.sample;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Map;


import android.content.SharedPreferences;
import android.util.Log;

import tech.yaog.hardwares.serialport.SerialPort;
import tech.yaog.hardwares.serialport.SerialPortFinder;

public class Application extends android.app.Application {

	public SerialPortFinder mSerialPortFinder = new SerialPortFinder();
	private SerialPort mSerialPort = null;

	public SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
		if (mSerialPort == null) {
			/* Read serial port parameters */
			SharedPreferences sp = getSharedPreferences("tech.yaog.serialportapi_preferences", MODE_PRIVATE);
			String path = sp.getString("DEVICE", "");
			int baudrate = Integer.parseInt(sp.getString("BAUDRATE", "9600"));
			int csize = Integer.parseInt(sp.getString("CSIZE", SerialPort.CSIZE_8+""));
			int parity = Integer.parseInt(sp.getString("PARITY", SerialPort.PARITY_NONE+""));
			int stopbits = Integer.parseInt(sp.getString("STOPBITS", SerialPort.STOP_BIT_1+""));

			/* Check parameters */
			if ( (path.length() == 0) || (baudrate == -1)) {
				throw new InvalidParameterException();
			}

			/* Open the serial port */
			mSerialPort = new SerialPort(new File(path), baudrate, csize, parity, stopbits, 0);
		}
		return mSerialPort;
	}

	public void closeSerialPort() {
		if (mSerialPort != null) {
			mSerialPort.close();
			mSerialPort = null;
		}
	}
}
