package ru.coolone.calculator;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

	private static final int maxIntegerLen = 6;
	private static final int maxFractionLen = 3;

	// Calculate nums
	public String firstStr = "";
	public String secondStr = "";
	private static final double resultDefault = 0;
	public double result = resultDefault;
	public String resultStr = "";

	// - Ids -

	// Button
	final static int[] buttonIds =
			{
					R.id.button0,
					R.id.button1,
					R.id.button2,
					R.id.button3,
					R.id.button4,
					R.id.button5,
					R.id.button6,
					R.id.button7,
					R.id.button8,
					R.id.button9,
					R.id.buttonComma,
					R.id.buttonDivide,
					R.id.buttonMultiply,
					R.id.buttonClear,
					R.id.buttonPlus,
					R.id.buttonMinus
			};

	// Text view
	final static int[] textViewIds =
			{
					R.id.textViewEqual,
					R.id.textViewOperation,
					R.id.textViewResult,
					R.id.textViewFirst,
					R.id.textViewSecond
			};

	// - Views -

	// Buttons
	private SparseArray<Button> buttons = new SparseArray<>();

	// Text views
	private SparseArray<TextView> textViews = new SparseArray<>();

	// Operation
	private enum Operation
	{
		DIVIDE,
		MULTIPLY,
		PLUS,
		MINUS,
		NULL
	}

	/**
	 * @param _operation Operation id
	 * @return Operation char
	 */
	private char operationToChar(Operation _operation)
	{
		char operationChar = ' ';

		// Parse operation
		switch (_operation)
		{
		case DIVIDE:
			operationChar = '/';
			break;
		case MULTIPLY:
			operationChar = '*';
			break;
		case PLUS:
			operationChar = '+';
			break;
		case MINUS:
			operationChar = '-';
			break;
		}

		return operationChar;
	}

	public Operation operation = Operation.NULL;

	/**
	 * @param first      First num
	 * @param _operation Operation between nums
	 * @param second     Second num
	 * @return Result of operation
	 */
	// Operation execute
	private double operationExec(double first, Operation _operation, double second)
	{
		double _result = first;

		// Exec operation
		switch (_operation)
		{
		case DIVIDE:
			_result /= second;
			break;
		case MULTIPLY:
			_result *= second;
			break;
		case PLUS:
			_result += second;
			break;
		case MINUS:
			_result -= second;
			break;
		}

		return _result;
	}

	/**
	 * @param numStr Num, trat will be checked
	 * @return Num validity (for convert to double)
	 */
	private boolean validNumStr(String numStr)
	{
		return !numStr.isEmpty() &&
				!numStr.equals("-") &&
				!numStr.equals(".");
	}

	/**
	 * @param viewIds Array of view ids
	 * @return Views array
	 */
	@SuppressWarnings("unchecked")
	private <T extends View> SparseArray<T> findViewsByIds(int... viewIds)
	{
		SparseArray<T> arrRet = new SparseArray<>();

		// Find array of viewIds
		for (int mViewId : viewIds)
		{
			arrRet.put(mViewId, (T) findViewById(mViewId));
		}

		return arrRet;
	}

	static final String[] clearArray =
			{
					"Infinity",
					"\u221e"
			};

	/**
	 * Dots mode for formatting (add dots or not)
	 */
	private enum DotsMode
	{
		ON,
		OFF
	}

	/**
	 * Nulls mode for formatting (delete nulls on end or not)
	 */
	private enum NullsMode
	{
		DELETE,
		NOT_DELETE
	}

	/**
	 * @param numStr number str, that will formatted
	 * @return Formatted double (string)
	 */
	private String formatNumStr(String numStr, NullsMode nullsMode, DotsMode dotsMode)
	{
		String ret = "";

		int fractionIndex = numStr.indexOf('.');

		// Add number part
		for (int mRetId = 0;
		     mRetId < (fractionIndex == -1 ?
				     numStr.length() :
				     fractionIndex);
		     mRetId++)
		{
			char mRetChar = numStr.charAt(mRetId);

			// Add , every 3 numbers
			if (mRetId != 0 &&
					mRetId % 3 == 0)
			{
				ret += ',';
			}

			ret += mRetChar;
		}

		// Add "..." (dots)
		if(dotsMode == DotsMode.ON &&
				fractionIndex > maxIntegerLen)
		{
			ret = ret.substring(0, maxIntegerLen + 1) + "...";
		}
		else
		{
			// Add fraction part
			if (nullsMode == NullsMode.DELETE)
			{
				// Add without end nulls
				if (fractionIndex != -1)
				{
					String fractionStr = numStr.substring(fractionIndex + 1);

					// Limit length
					if (fractionStr.length() > maxFractionLen)
						fractionStr = fractionStr.substring(0, maxFractionLen - 1);

					// Delete end nulls
					for (int mFractionId = fractionStr.length() - 1; mFractionId >= 0; mFractionId--)
					{
						char mFractionChar = fractionStr.charAt(mFractionId);

						if (mFractionChar == '0')
							fractionStr = fractionStr.substring(0, fractionStr.length() - 1);
					}

					if (!fractionStr.isEmpty())
						ret += "." + fractionStr;
				}
			} else
			{
				// Add all
				if (fractionIndex != -1)
				{
					ret += numStr.substring(fractionIndex);
				}
			}
		}

		return ret;
	}

	/**
	 * @param text Text, that will be in showed toast
	 */
	private void showToast(String text)
	{
		// Create toast
		Toast toastMaxLen = Toast.makeText(getApplicationContext(),
				text,
				Toast.LENGTH_SHORT);

		// Show toast
		toastMaxLen.show();
	}

	/**
	 * @param text Text, that will be copied to clipboard
	 */
	private void copyToClipboard(String text)
	{
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText("number", text);
		clipboard.setPrimaryClip(clip);
	}

	/**
	 * Refresh's buttons and textViews
	 */
	private void refreshUi()
	{
		// --- Refresh ---

		// -- Text views --

		// First num
		TextView textViewFirst = textViews.get(R.id.textViewFirst);
		if (validNumStr(firstStr))
		{
			textViewFirst.setText(formatNumStr(firstStr,
					NullsMode.DELETE,
					DotsMode.ON));
		} else
			textViewFirst.setText("");

		// Second num
		TextView textViewSecond = textViews.get(R.id.textViewSecond);
		if (validNumStr(secondStr))
		{
			String secondStrFmt = formatNumStr(secondStr,
					NullsMode.NOT_DELETE,
					DotsMode.OFF);

			if (secondStrFmt.charAt(0) == '-')
				secondStrFmt = "(" + secondStrFmt + ")";

			textViewSecond.setText(secondStrFmt);
		} else if (secondStr.equals("-"))
			textViewSecond.setText("(- )");
		else
			textViewSecond.setText("");

		// Operation
		TextView textViewOperation = textViews.get(R.id.textViewOperation);
		textViewOperation.setText(String.valueOf(operationToChar(operation)));
		textViewOperation.setVisibility(firstStr.isEmpty() ? View.INVISIBLE : View.VISIBLE);

		// Result
		TextView textViewResult = textViews.get(R.id.textViewResult);
		if (validNumStr(firstStr) &&
				validNumStr(secondStr))
		{
			// Enum
			result = operationExec(
					Double.valueOf(firstStr),
					operation,
					Double.valueOf(secondStr));
			resultStr = String.valueOf(result);

			// Show
			textViewResult.setText(formatNumStr(resultStr,
					NullsMode.DELETE,
					DotsMode.ON));
		} else
			textViewResult.setText("");

		// Equal
		textViews.get(R.id.textViewEqual).setVisibility(
				(validNumStr(firstStr) && validNumStr(secondStr)) ?
						View.VISIBLE :
						View.INVISIBLE);

		// -- Buttons --

		// Comma
		buttons.get(R.id.buttonComma).setTextColor(getResources().getColor(
				secondStr.indexOf('.') == -1 &&
						(!firstStr.isEmpty() || !secondStr.isEmpty()) ?
						R.color.colorPrimaryActive :
						R.color.colorPrimaryInactive));

		// Clear
		buttons.get(R.id.buttonClear).setTextColor(getResources().getColor(
				(!secondStr.isEmpty() ||
						!firstStr.isEmpty()) ?
						R.color.colorPrimaryActive :
						R.color.colorPrimaryInactive));

		// - Operations -

		boolean buttonComparison = (
				!firstStr.isEmpty() ||
						!secondStr.isEmpty()
		);

		// Divide
		buttons.get(R.id.buttonDivide).setTextColor(getResources().getColor(
				operation != Operation.DIVIDE &&
						buttonComparison ?
						R.color.colorPrimaryActive :
						R.color.colorPrimaryInactive));

		// Multiply
		buttons.get(R.id.buttonMultiply).setTextColor(getResources().getColor(
				operation != Operation.MULTIPLY &&
						buttonComparison ?
						R.color.colorPrimaryActive :
						R.color.colorPrimaryInactive));

		// Plus
		buttons.get(R.id.buttonPlus).setTextColor(getResources().getColor(
				operation != Operation.PLUS &&
						buttonComparison ?
						R.color.colorPrimaryActive :
						R.color.colorPrimaryInactive));

		// Minus
		buttons.get(R.id.buttonMinus).setTextColor(getResources().getColor(
				operation != Operation.MINUS &&
						buttonComparison ?
						R.color.colorPrimaryActive :
						R.color.colorPrimaryInactive));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		// Open main activity
		setContentView(R.layout.activity_main);

		// Get buttons array
		buttons = findViewsByIds(buttonIds);

		// Get textViews array
		textViews = findViewsByIds(textViewIds);

		// Create buttons listener
		View.OnClickListener onClickButtonsListener = view ->
		{

			// Get view id
			int viewId = view.getId();

			// --- OnClick events ---

			switch (viewId)
			{
			// -- Numbers
			case R.id.button0:
			case R.id.button1:
			case R.id.button2:
			case R.id.button3:
			case R.id.button4:
			case R.id.button5:
			case R.id.button6:
			case R.id.button7:
			case R.id.button8:
			case R.id.button9:

				boolean lenGood = false;

				int dotIndex = secondStr.indexOf('.');
				if (dotIndex == -1)
				{
					// Check integer part length
					if (secondStr.length() < maxIntegerLen)
						lenGood = true;
					else
						// Show error
						showToast(String.valueOf(maxIntegerLen) + " numbers is max length!");
				} else
				{
					// Check decimal length
					if (secondStr.substring(dotIndex + 1).length() < maxFractionLen)
						lenGood = true;
					else
						// Show error
						showToast(String.valueOf(maxFractionLen) + " fraction numbers is max length!");
				}

				if (lenGood)
				{
					// Add number
					secondStr += buttons.get(viewId).getText();

					// Auto dot
					if (viewId == R.id.button0 &&
							secondStr.equals("0"))
					{
						secondStr += ".";
					}
				}
				break;

			// -- Operations --
			case R.id.buttonDivide:
			case R.id.buttonMultiply:
			case R.id.buttonPlus:
			case R.id.buttonMinus:

				if (result != 0)
				{
					if (result == Math.round(result))
						firstStr = String.valueOf((long) result);
					else
						firstStr = String.valueOf(result);
				} else
				{
					if (validNumStr(secondStr))
					{
						firstStr = secondStr;
					}
				}
				secondStr = "";

				// Set operation
				switch (viewId)
				{
				case R.id.buttonDivide:
					operation = Operation.DIVIDE;
					break;
				case R.id.buttonMultiply:
					operation = Operation.MULTIPLY;
					break;
				case R.id.buttonPlus:
					operation = Operation.PLUS;
					break;
				case R.id.buttonMinus:
					switch (operation)
					{
					case MULTIPLY:
					case DIVIDE:
						// Add minus to second num
						if (secondStr.isEmpty())
						{
							secondStr = "-";
						}
						break;
					default:
						// Set operation
						operation = Operation.MINUS;
					}
					break;
				}
				break;

			// -- Comma --
			case R.id.buttonComma:
				if (!secondStr.isEmpty() &&
						secondStr.indexOf('.') == -1)
				{
					secondStr += ".";
				}
				break;

			// -- Clear --
			case R.id.buttonClear:

				if (!firstStr.isEmpty() || !secondStr.isEmpty())
				{
					// Get clear string
					String clearString = (!secondStr.isEmpty() ? secondStr : firstStr);

					// Get clear length
					int clearLen = 1;
					for (String mClearStr : clearArray)
					{
						if (clearString.length() >= mClearStr.length() &&
								clearString.substring(clearString.length() - mClearStr.length())
										.equalsIgnoreCase(mClearStr))
						{
							clearLen = mClearStr.length();
						}
					}

					// Local delete
					clearString = clearString.substring(0, clearString.length() - clearLen);

					// Global delete
					if (!secondStr.isEmpty())
						secondStr = clearString;
					else
						firstStr = clearString;

					// Move first to second num
					if (secondStr.isEmpty() &&
							!firstStr.isEmpty())
					{
						secondStr = firstStr;
						firstStr = "";
						operation = Operation.NULL;
					}
				}
				break;
			}

			// Refresh ui
			refreshUi();
		};

		// Activate listener to buttons
		for (int mButtonId = 0; mButtonId < buttons.size(); mButtonId++)
		{
			int mButtonKey = buttons.keyAt(mButtonId);

			buttons.get(mButtonKey).setOnClickListener(onClickButtonsListener);
		}

		// Create textViews listener
		View.OnClickListener onClickTextViewsListener = view ->
		{

			TextView textView = (TextView) view;

			int textViewId = view.getId();

			if (!textView.getText().toString().isEmpty())
			{
				String numStr = "";
				switch (textViewId)
				{
				case R.id.textViewFirst:
					numStr = firstStr;
					break;
				case R.id.textViewSecond:
					numStr = secondStr;
					break;
				case R.id.textViewResult:
					numStr = resultStr;
					break;
				default:
					numStr = textView.getText().toString();
				}

				final String numStrFinal = numStr;

				// Create num dialog
				AlertDialog.Builder numDialogBuilder = new AlertDialog.Builder(MainActivity.this);
				numDialogBuilder.setTitle(getResources().getString(R.string.num_dialog_title))
						.setMessage(numStrFinal)
						.setNeutralButton(getResources().getString(R.string.num_dialog_button_copy),
								(dialog, which) ->
								{
									// Copy result to clipboard
									copyToClipboard(numStrFinal);

									// Show toast
									showToast("\"" + numStrFinal + "\" " +
											getResources().getString(R.string.num_dialog_toast_copy));
								});
				AlertDialog numDialog = numDialogBuilder.create();

				// Open num dialog
				numDialog.show();
			}
		};

		// Activate listener to textViews
		for (int mTextViewId = 0; mTextViewId < textViews.size(); mTextViewId++)
		{
			int mTextViewKey = textViews.keyAt(mTextViewId);

			textViews.get(mTextViewKey).setOnClickListener(onClickTextViewsListener);
		}

		// Refresh ui
		refreshUi();
	}

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState)
	{
		// - Save vals -

		// Strings
		savedInstanceState.putString      ("firstStr",  firstStr);
		savedInstanceState.putString      ("secondStr", secondStr);
		savedInstanceState.putSerializable("operation", operation);

		super.onSaveInstanceState(savedInstanceState);
	}

	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState)
	{
		super.onRestoreInstanceState(savedInstanceState);

		// - Restore vals -

		// Strings
		firstStr  = savedInstanceState.getString("firstStr");
		secondStr = savedInstanceState.getString("secondStr");
		operation = (Operation) savedInstanceState.getSerializable("operation");

		// Refresh ui
		refreshUi();
	}
}
