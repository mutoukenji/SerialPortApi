package tech.yaog.hardwares.serialport;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
public class SerialPort {

	private static final String TAG = SerialPort.class.getName();

	public static final int CSIZE_5 = 5;
	public static final int CSIZE_6 = 6;
	public static final int CSIZE_7 = 7;
	public static final int CSIZE_8 = 8;
	public static final int PARITY_NONE = -1;
	public static final int PARITY_ODD = 1;
	public static final int PARITY_EVEN = 0;
	public static final int PARITY_SPACE = 2;
	public static final int STOP_BIT_1 = 1;
	public static final int STOP_BIT_2 = 2;

	/*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;

	public SerialPort(File device, int baudrate, int csize, int parity, int stopbits, int flags) throws SecurityException, IOException {
		this(device, baudrate, csize, parity, stopbits, false, false, flags);
	}

	public SerialPort(File device, int baudrate, int csize, int parity, int stopbits, boolean rtscts, boolean xonoff, int flags) throws SecurityException, IOException {

		/* Check access permission */
		if (!device.canRead() || !device.canWrite()) {
			try {
				/* Missing read/write permission, trying to chmod the file */
				Process su;
				su = Runtime.getRuntime().exec("/system/bin/su");
				String cmd = "chmod 666 " + device.getAbsolutePath() + "\n"
						+ "exit\n";
				su.getOutputStream().write(cmd.getBytes());
				if ((su.waitFor() != 0) || !device.canRead()
						|| !device.canWrite()) {
					throw new SecurityException();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new SecurityException();
			}
		}

		mFd = open(device.getAbsolutePath(), baudrate, csize, parity, stopbits, rtscts, xonoff, flags);
		if (mFd == null) {
			Log.e(TAG, "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}

	// Getters and setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}

	// JNI
	private native static FileDescriptor open(String path, int baudrate, int csize, int parity, int stopbits, boolean rtscts, boolean xonxoff, int flags);
	public native void close();

	public native void setRtx(boolean mark);
	public native boolean getCtx();

	static {
		System.loadLibrary("serial_port");
	}
}
