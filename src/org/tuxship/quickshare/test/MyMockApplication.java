package org.tuxship.quickshare.test;

import android.content.Context;
import android.test.mock.MockApplication;

public class MyMockApplication extends MockApplication {

	public Context getApplicationContext() {
		return this;
	}
	
	public Context getContext() {
		return this;
	}
}
