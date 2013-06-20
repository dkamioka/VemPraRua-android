package org.k3x.vemprarua.model;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import org.k3x.vemprarua.util.Configs;

import android.content.Context;
import android.content.ContextWrapper;
import android.util.Log;

public class User implements Serializable{

	private static final long serialVersionUID = 1L;

	private static String FILENAME = "user.ser";
	
	private static User user = null;

	public String id;
	public String name;
	public String status;
	public double latitude;
	public double longitude;
	
	public void save(ContextWrapper contextWrapper) {
		FileOutputStream fos = null;
		try
		{
			fos = contextWrapper.openFileOutput(FILENAME, Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(this);
			out.close();
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static User getUser(ContextWrapper contextWrapper) {
		if(user != null) {
			return user;
		}

		try
		{
			FileInputStream fis = contextWrapper.openFileInput(FILENAME);
			ObjectInputStream in = new ObjectInputStream(fis);
			user = (User) in.readObject();
			in.close();
			fis.close();
		} catch (FileNotFoundException e) {
			user = new User();
		} catch (IOException e) {
			user = new User();
		} catch(ClassNotFoundException c)
		{
			Log.e(Configs.LOG_TAG, "User class not found");
			throw new RuntimeException(c);
		}

		Log.d(Configs.LOG_TAG, "User loaded...");
		Log.d(Configs.LOG_TAG, "code:" + user.id);
		Log.d(Configs.LOG_TAG, "code:" + user.latitude);
		Log.d(Configs.LOG_TAG, "code:" + user.longitude);

		return user;
	}
	
}
