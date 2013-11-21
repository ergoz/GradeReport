package com.shinymetal.gradereport;

import android.content.Context;
import android.provider.Settings.Secure;
import android.util.Log;

import com.google.android.vending.licensing.AESObfuscator;
import com.google.android.vending.licensing.LicenseChecker;
import com.google.android.vending.licensing.LicenseCheckerCallback;
import com.google.android.vending.licensing.Policy;
import com.google.android.vending.licensing.ServerManagedPolicy;
import com.shinymetal.gradereport.objects.TS;
import com.shinymetal.gradereport.utils.GshisLoader;

public class LicenseValidatorHelper implements LicenseCheckerCallback {

	private static final String BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmGmVMb5v06NsnxMAt4iOaJGXuU9Tj3XR9/QMmE1lE4VMjUzMrYT96FX6obkOrukZrN3cgw+oduv4mgLQjmaavd5U8EdFXKjdGD753k01DN/YYaG96WNFUd1ES4sZlq0R/rRR8B+l+uRaEaVIAQdEvGMd1nH1s6lRkkzQHf34plpH0O4DxAJn+OWhyDxWVsyC8hY3uPTrpKpr6g0iTQJOS+77+LhdIHmrd0oNm3R7galW4qVC6V+6BTqUz0YgzdF383H+7dP7GE2RRld7AeFlYjo4JFU5LQJzmPhrz/w788hO/dGKe5U5CYkw2HV1iJlmdboz+lKzYDnYzJyXT3s9cwIDAQAB";
	private static final byte[] SALT = new byte[] { 34, 87, 35, -23, -34, -12,
			43, -87, 34, 18, -12, -65, 76, -98, -121, -32, -12, 76, -43, 43 };
	
	private LicenseChecker mChecker;
    private String mDeviceId;
    
	private int mLicState = Policy.RETRY;
	private boolean mInProgress = false;
	
	private static volatile LicenseValidatorHelper instance;

	private LicenseValidatorHelper(Context context) {

		// Try to use more data here. ANDROID_ID is a single point of attack.
	    mDeviceId = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
	    
        mChecker = new LicenseChecker(
                context, new ServerManagedPolicy(context,
                    new AESObfuscator(SALT, context.getPackageName(), mDeviceId)),
                BASE64_PUBLIC_KEY);
	    
        mChecker.checkAccess(this);
        mInProgress = true;
	}
	
	public static LicenseValidatorHelper getInstance(Context context) {

		LicenseValidatorHelper localInstance = instance;

		if (localInstance == null) {

			synchronized (GshisLoader.class) {

				localInstance = instance;
				if (localInstance == null) {
					instance = localInstance = new LicenseValidatorHelper(context);
				}
			}
		}

		return localInstance;
	}
	
	public void retry () {
		
		mChecker.checkAccess(this);
		mInProgress = true;
	}
	
	public void onDestroy() {
		
		mInProgress = false;
		
		if (mChecker != null) {

			mChecker.onDestroy();
			mChecker = null;
		}
	}
	
	public int getLicState() {
		
		return mLicState;
	}

	public boolean isInProgress() {
		
		return mInProgress;
	}

	@Override
	public void allow(int reason) {
		
		mInProgress = false;
		mLicState = Policy.LICENSED;		
	}

	@Override
	public void dontAllow(int reason) {

		mInProgress = false;
		mLicState = reason;
	}

	@Override
	public void applicationError(int errorCode) {

		mInProgress = false;
		
		if (errorCode == LicenseCheckerCallback.ERROR_NOT_MARKET_MANAGED) {
			
			if (BuildConfig.DEBUG)
				Log.d(this.toString(), TS.get() + "applicationError (): not published yet");

			mLicState = Policy.LICENSED;
		}

		if (BuildConfig.DEBUG)
			Log.d(this.toString(), TS.get() + "applicationError (): "
					+ errorCode); 
	}
}