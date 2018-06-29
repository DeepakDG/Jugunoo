package com.hirecraft.jugunoo.passenger.common;

import java.util.regex.Pattern;

import android.widget.EditText;

public class Validation
{

	public static final String EMAIL_REGEX = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	private static final String PHONE_REGEX = "^[[2-9][0-9]{7}$";
	private static final String MOBILE_REGEX = "^5[0-9]{9}$";

	// Error Messages
	private static final String REQUIRED_MSG = "Required";
	private static final String EMAIL_MSG = "Invalid Email";
	private static final String PHONE_MSG = "Invalid phone No";
	private static final String MOBILE_MSG = "Invalid mobile No";

	// call this method when you need to check email validation
	public static boolean isEmailAddress(EditText editText, boolean required)
	{
		return isValid(editText, EMAIL_REGEX, EMAIL_MSG, required);
	}

	public static boolean isEmailAddressE(EditText editText, boolean required)
	{
		return isValidE(editText, EMAIL_REGEX, EMAIL_MSG);
	}

	// call this method when you need to check phone number validation
	public static boolean isPhoneNumber(EditText editText, boolean required)
	{
		return isValid(editText, PHONE_REGEX, PHONE_MSG, required);
	}

	// call this method when you need to check mobile number validation
	public static boolean isMobileNumber(EditText editText, boolean required)
	{
		return isValid(editText, MOBILE_REGEX, MOBILE_MSG, required);
	}

	// return true if the input field is valid, based on the parameter passed
	public static boolean isValid(EditText editText, String regex,
			String errMsg, boolean required)
	{

		String text = editText.getText().toString().trim();

		editText.setError(null);

		if (required && !Pattern.matches(regex, text))
		{
			// editText.setError(errMsg);
			return false;
		}
		;

		return true;
	}

	// return true if the input field is valid, based on the parameter passed
	public static boolean isValidE(EditText editText, String regex,
			String errMsg)
	{

		String text = editText.getText().toString().trim();

		editText.setError(null);

		if (!Pattern.matches(regex, text))
		{
			if (errMsg != null)
				// editText.setError(errMsg);
				return false;
		}
		;

		return true;
	}

	public static boolean hasText(EditText editText)
	{

		String text = editText.getText().toString().trim();
		// editText.setError(null);

		if (text.length() == 0)
		{
			// editText.setError(REQUIRED_MSG);
			return false;
		}

		return true;
	}

	public static boolean isNotNull(String txt)
	{
		return txt != null && txt.trim().length() > 0 ? true : false;
	}
}
