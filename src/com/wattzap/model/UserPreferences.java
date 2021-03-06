/* This file is part of Wattzap Community Edition.
 *
 * Wattzap Community Edtion is free software: you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Wattzap Community Edition is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Wattzap.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.wattzap.model;

import java.awt.Rectangle;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.UUID;

import uk.co.caprica.vlcj.logger.Logger;

import com.wattzap.controller.MessageBus;
import com.wattzap.controller.Messages;
import com.wattzap.model.dto.WorkoutData;
import com.wattzap.model.power.Power;
import com.wattzap.model.power.PowerProfiles;
import com.wattzap.utils.TcxWriter;
import com.wattzap.view.Workouts;

/**
 * Singleton helper to read/write user preferences to a backing store
 * 
 * @author David George / 15 September 2013
 * (C) Copyright 2013-2015
 */
public enum UserPreferences {
	INSTANCE;
	private Power powerProfile;
	String user;
	private final DataStore ds;
	private static int evalTime = 240;
	private static String workingDirectory = null;
	private static String userDataDirectory = null;
	private static final String cryptKey = "afghanistanbananastan";
	private static final double LBSTOKG = 0.45359237;
	private ResourceBundle messages;
	private boolean antEnabled = true;

	private UserPreferences() {
		user = System.getProperty("user.name");
		String wd = getWD();
		ds = new DataStore(wd, cryptKey);

		Locale currentLocale = getLocale();
		messages = ResourceBundle.getBundle("MessageBundle", currentLocale);

		// check User data directories exist
		String udDir = getUserDataDirectory();
		File f = new File(udDir + TcxWriter.WORKOUTDIR);
		if (!f.exists()) {
			f.mkdirs();
			Logger.info("created " + udDir);
		}

		f = new File(udDir + Workouts.IMPORTDIR);
		if (!f.exists()) {
			f.mkdirs();
			Logger.info("created " + udDir);
		}
	}

	public void setAntEnabled(boolean v) {
		antEnabled = v;
	}

	public boolean isAntEnabled() {
		return antEnabled;
	}

	public void addWorkout(WorkoutData data) {
		ds.saveWorkOut(user, data);
	}

	public void updateWorkout(WorkoutData data) {
		ds.updateWorkout(user, data);
	}

	public WorkoutData getWorkout(String name) {
		return ds.getWorkout(user, name);
	}

	public void deleteWorkout(String name) {
		ds.deleteWorkout(user, name);
	}

	public List<WorkoutData> listWorkouts() {
		return ds.listWorkouts(user);
	}

	public String getDBVersion() {
		return get("", "dbVersion", "1.2");
	}

	public void setDBVersion(String v) {
		set("", "dbVersion", v);
	}

	public Rectangle getMainBounds() {
		int width = getInt("", "mainWidth", 1200);
		int height = getInt("", "mainHeight", 650);
		int x = getInt("", "mainX", 0);
		int y = getInt("", "mainY", 0);

		Rectangle r = new Rectangle(x, y, width, height);
		return r;
	}

	public void setMainBounds(Rectangle r) {
		setInt("", "mainHeight", r.height);
		setInt("", "mainWidth", r.width);
		setInt("", "mainX", r.x);
		setInt("", "mainY", r.y);

	}

	public Rectangle getVideoBounds() {
		int width = getInt("", "videoWidth", 800);
		int height = getInt("", "videoHeight", 600);
		int x = getInt("", "videoX", 0);
		int y = getInt("", "videoY", 650);

		Rectangle r = new Rectangle(x, y, width, height);
		return r;
	}

	public void setVideoBounds(Rectangle r) {
		setInt("", "videoHeight", r.height);
		setInt("", "videoWidth", r.width);
		setInt("", "videoX", r.x);
		setInt("", "videoY", r.y);
	}

	public double getWeight() {
		if (this.isMetric()) {
			return getDouble("weight", 80.0);
		} else {
			// convert to lbs
			return getDouble("weight", 80.0) / LBSTOKG;
		}
	}

	public double getWeightKG() {
		return getDouble("weight", 80.0);
	}

	// always store as kg
	public void setWeight(double weight) {
		if (this.isMetric()) {
			setDouble("weight", weight);
		} else {
			// convert from lbs
			setDouble("weight", weight * LBSTOKG);
		}
	}

	public double getBikeWeight() {
		if (this.isMetric()) {
			return getDouble("bikeweight", 10.0);
		} else {
			// convert from lbs
			return getDouble("bikeweight", 10.0) / LBSTOKG;
		}
	}

	public double getTotalWeight() {
		return getDouble("weight", 80.0) + getDouble("bikeweight", 10.0);
	}

	// always store as kg
	public void setBikeWeight(double weight) {
		if (this.isMetric()) {
			setDouble("bikeweight", weight);
		} else {
			// convert to lbs
			setDouble("bikeweight", weight * LBSTOKG);
		}
	}

	// 2133 is 700Cx23
	public int getWheelsize() {
		return getInt(user, "wheelsize", 2133);
	}

	public double getWheelSizeCM() {
		return getInt(user, "wheelsize", 2133) / 10.0;
	}

	public void setWheelsize(int wheelsize) {
		setInt(user, "wheelsize", wheelsize);
	}

	public int getMaxHR() {
		return getInt(user, "maxhr", 0);
	}

	public void setMaxHR(int maxhr) {
		setInt(user, "maxhr", maxhr);
	}

	public int getMaxPower() {
		return getInt(user, "maxpower", 0);
	}

	public void setMaxPower(int maxPower) {
		setInt(user, "maxpower", maxPower);
	}

	public boolean isMetric() {
		return getBoolean("units", true);
	}

	public void setUnits(boolean value) {
		setBoolean("units", value);
	}
	
	// For automatic screen shots
	public boolean isScreenshot() {
		return getBoolean("screenshot", false);
	}

	public void setScreenshot(boolean value) {
		setBoolean("screenshot", value);
	}

	// Social
	public String getSLUser() {
		return get(user, "sl_user", "");
	}

	public void setSLUser(String slUser) {
		set(user, "sl_user", slUser);
	}

	public String getSLPass() {
		return get(user, "sl_pass", "");
	}

	public void setSLPass(String slPass) {
		set(user, "sl_pass", slPass);
	}

	// ANT
	public boolean isANTUSB() {
		return getBoolean("antusbm", false);
	}

	public void setAntUSBM(boolean value) {
		setBoolean("antusbm", value);
	}

	public boolean isDebug() {
		return getBoolean("debug", false);
	}

	public void setDebug(boolean value) {
		setBoolean("debug", value);
	}

	public boolean isVirtualPower() {
		return getBoolean("virtualPower", false);
	}

	public void setVirtualPower(boolean value) {
		setBoolean("virtualPower", value);
	}

	public void setLocale(String value) {
		set(user, "locale", value);
		messages = ResourceBundle.getBundle("MessageBundle", getLocale());
		MessageBus.INSTANCE.send(Messages.LOCALE, null);
	}

	public Locale getLocale() {
		String iso3 = get(user, "locale", Locale.getDefault().getISO3Language());
		Locale locale = new Locale(iso3);
		return locale;
	}

	public Power getPowerProfile() {
		String profile = get(user, "profile", "Tacx Satori / Blue Motion");

		if (powerProfile == null) {
			PowerProfiles pp = PowerProfiles.INSTANCE;
			powerProfile = pp.getProfile(profile);
		}
		return powerProfile;
	}

	public void setPowerProfile(String profile) {
		String p = get(user, "profile", null);
		if (!profile.equals(p)) {
			set(user, "profile", profile);
			PowerProfiles pp = PowerProfiles.INSTANCE;
			powerProfile = pp.getProfile(profile);
		}
	}

	public int getResistance() {
		int i = getInt(user, "resistance", 1);
		return i;
	}

	public void setResistance(int r) {
		setInt(user, "resistance", r);
	}

	public int getSCId() {
		int i = getInt(user, "sandcId", 0);
		return i;
	}

	public void setSCId(int i) {
		setInt(user, "sandcId", i);
	}

	public int getSpeedId() {
		return getInt(user, "speedId", 0);
	}

	public void setSpeedId(int i) {
		setInt(user, "speedId", i);
	}

	public int getCadenceId() {
		return getInt(user, "cadenceId", 0);
	}

	public void setCadenceId(int i) {
		setInt(user, "cadenceId", i);
	}

	public int getPowerId() {
		return getInt(user, "powerId", 0);
	}

	public void setPowerId(int i) {
		setInt(user, "powerId", i);
	}
	
	public int getPowerSmoothing() {
		return getInt(user, "powerSmooth", 0);
	}

	public void setPowerSmooth(int i) {
		setInt(user, "powerSmooth", i);
	}

	public int getHRMId() {
		return getInt(user, "hrmid", 0);
	}

	public void setHRMId(int i) {
		setInt(user, "hrmid", i);
	}

	/** Registration Stuff **/
	public String getSerial() {
		String id = get("", "ssn", null);

		if (id == null) {
			// not yet initialized
			id = UUID.randomUUID().toString();
			set("", "ssn", id);

		}
		return id;
	}

	public boolean isRegistered() {
		if (get("", "rsnn", null) == null) {
			return false;
		}
		return true;
	}

	public String getRegistrationKey() {
		return get("", "rsnn", null);
	}

	public void setRegistrationKey(String key) {
		set("", "rsnn", key);
	}

	public int getEvalTime() {
		int i = getIntCrypt("", "evalTime", evalTime);
		if (i < 0) {
			return 0;
		}
		return i;
	}

	public void setEvalTime(int t) {
		setIntCrypt("", "evalTime", t);
	}

	public String getRouteDir() {
		return get("", "videoLocation", this.getWD() + "/Routes");
	}

	public void setRouteDir(String s) {
		set("", "videoLocation", s);
	}

	public String getTrainingDir() {
		return get("", "trainingLocation", this.getWD() + "/Trainings");
	}

	public void setTrainingDir(String s) {
		set("", "trainingLocation", s);
	}

	// Data Access Functions

	private double getDouble(String key, double d) {
		String v = ds.getProp(user, key);
		if (v != null) {
			try {
				d = Double.parseDouble(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return d;
	}

	private void setDouble(String key, double d) {
		ds.insertProp(user, key, Double.toString(d));
	}

	private int getInt(String user, String key, int i) {
		String v = ds.getProp(user, key);
		if (v != null) {
			try {
				i = Integer.parseInt(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return i;
	}

	private int getIntCrypt(String user, String key, int i) {
		String v = ds.getPropCrypt(user, key);
		if (v != null) {
			try {
				i = Integer.parseInt(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return i;
	}

	private void setInt(String user, String key, int i) {
		ds.insertProp(user, key, Integer.toString(i));
	}

	private void setIntCrypt(String user, String key, int i) {
		ds.insertPropCrypt(user, key, Integer.toString(i));
	}

	private boolean getBoolean(String key, boolean b) {
		String v = ds.getProp(user, key);
		if (v != null) {
			try {
				b = Boolean.parseBoolean(v);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		return b;
	}

	private void setBoolean(String key, boolean b) {
		ds.insertProp(user, key, Boolean.toString(b));
	}

	private String get(String user, String key, String s) {
		String v = ds.getProp(user, key);
		if (v != null) {
			s = v;
		}

		return s;
	}

	private void set(String user, String key, String s) {
		ds.insertProp(user, key, s);
	}

	public void shutDown() {
		ds.close();
	}

	/*
	 * Stores common data files: database logfile Videos Trainings
	 * 
	 * These directories are created by the Windows/Unix installer
	 * 
	 * On Windows 7: C:\ProgramData\Wattzap On Windows XP: C:\Documents and
	 * Settings\All Users\Application Data\Wattzap On Unix: ??? $home/.wattzap
	 */
	public String getWD() {
		if (workingDirectory == null) {
			// here, we assign the name of the OS, according to Java, to a
			// variable...
			String OS = (System.getProperty("os.name")).toUpperCase();
			// to determine what the workingDirectory is.
			// if it is some version of Windows

			if (OS.contains("WIN")) {
				// it is simply the location of the "AppData" folder
				workingDirectory = System.getenv("ALLUSERSPROFILE");

				if (OS.contains("WINDOWS XP")) {
					workingDirectory += "/Application Data/Wattzap";
				} else {
					workingDirectory += "/Wattzap";

				}
			} else {
				// in either case, we would start in the user's home directory
				workingDirectory = System.getProperty("user.home")
						+ "/.wattzap";
			}
		}
		return workingDirectory;
	}

	/*
	 * Stores User dependent data Workouts
	 * 
	 * On Windows 7: C:\Users\$user\AppData\Roaming\Wattzap On Windows XP:
	 * C:\Documents & Settings\$user\AppData\Wattzap On Unix: ??? $home/.wattzap
	 */
	public String getUserDataDirectory() {

		if (userDataDirectory == null) {
			// here, we assign the name of the OS, according to Java, to a
			// variable...
			String OS = (System.getProperty("os.name")).toUpperCase();

			// to determine what the workingDirectory is.
			// if it is some version of Windows
			if (OS.contains("WIN")) {
				// it is simply the location of the "AppData" folder
				userDataDirectory = System.getenv("APPDATA") + "/Wattzap";
			} else {
				// in either case, we would start in the user's home directory
				userDataDirectory = System.getProperty("user.home")
						+ "/wattzap";
			}
		}
		return userDataDirectory;
	}
	
	// Hack For UTF resources
	public String getString(String key) {
		String val = messages.getString(key); 
		try {
			return new String(val.getBytes("ISO-8859-1"), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/*
	 * Reset certain properties to their defaults.
	 */
	public void factoryReset() {
		Rectangle r = new Rectangle(0, 0, 800, 600);
		setVideoBounds(r);
		
		r = new Rectangle(10, 10, 800, 400);
		this.setMainBounds(r);
	}
}
