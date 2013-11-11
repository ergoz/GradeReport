package com.shinymetal.gradereport;

import android.app.Activity;
import android.provider.Settings.Secure;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.ServerManagedPolicy;

public class LicenseValidatorHelper {

	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmGmVMb5v06NsnxMAt4iOaJGXuU9Tj3XR9/QMmE1lE4VMjUzMrYT96FX6obkOrukZrN3cgw+oduv4mgLQjmaavd5U8EdFXKjdGD753k01DN/YYaG96WNFUd1ES4sZlq0R/rRR8B+l+uRaEaVIAQdEvGMd1nH1s6lRkkzQHf34plpH0O4DxAJn+OWhyDxWVsyC8hY3uPTrpKpr6g0iTQJOS+77+LhdIHmrd0oNm3R7galW4qVC6V+6BTqUz0YgzdF383H+7dP7GE2RRld7AeFlYjo4JFU5LQJzmPhrz/w788hO/dGKe5U5CYkw2HV1iJlmdboz+lKzYDnYzJyXT3s9cwIDAQAB";
	private static final byte[] SALT = new byte[] { 34, 87, 35, -23, -34, -12,
			43, -87, 34, 18, -12, -65, 76, -98, -121, -32, -12, 76, -43, 43 };
	
	private LicenseCheckerCallback mLicenseCheckerCallback;
    private LicenseChecker mChecker;
    private String mDeviceId;

	public LicenseValidatorHelper(Activity activity, LicenseCheckerCallback licenseCheckerCallback) {

		// Try to use more data here. ANDROID_ID is a single point of attack.
	    mDeviceId = Secure.getString(activity.getContentResolver(), Secure.ANDROID_ID);
	    
	    // Library calls this when it's done.
	    mLicenseCheckerCallback = licenseCheckerCallback;

        mChecker = new LicenseChecker(
                activity, new ServerManagedPolicy(activity,
                    new AESObfuscator(SALT, activity.getPackageName(), mDeviceId)),
                BASE64_PUBLIC_KEY);
	    
        mChecker.checkAccess(mLicenseCheckerCallback);
	}
	
	public void retry () {
		
		mChecker.checkAccess(mLicenseCheckerCallback);
	}
	
	protected void onDestroy() {
		
		mChecker.onDestroy();
	}
}