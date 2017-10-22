package ru.coolone.calculator;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;

public class MainActivity extends AppCompatActivity {

    private static final int maxLen = 6;
    private static final int maxFractionLen = 3;

    // Calculate nums
    public String firstStr = "";
    public String secondStr = "";

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

    // Button
    private SparseArray<Button> buttons = new SparseArray<>();

    // Text view
    private SparseArray<TextView> textViews = new SparseArray<>();

    // Operation
    private enum Operation {
        DIVIDE,
        MULTIPLY,
        PLUS,
        MINUS,
        NULL
    }
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
    private <T extends View> SparseArray<T> findViewsByIds(int ... viewIds)
    {
        SparseArray<T> arrRet = new SparseArray<T>();

        // Find array of viewIds
        for(int mViewId : viewIds)
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
     * @param num Count of repeat
     * @param str String, that will be repeated
     * @return Repeated string
     */
    private String repeat(int num, String str)
    {
        String ret = "";

        // Repeat
        for (int mNum = 0; mNum < num; mNum++)
        {
            ret += str;
        }

        return ret;
    }

    /**
     * @param numStr number str, that will formatted
     * @return Formatted double (string)
     */
    private String formatNumStr(String numStr)
    {
        String ret;

        double num = Double.valueOf(numStr);

        // Generate format string
        String fmtStr = "#,###";
        if(num != Math.round(num))
            fmtStr += "." + repeat(maxFractionLen, "#");

        // Create formatter
        DecimalFormat fmt = new DecimalFormat(fmtStr);

        // Format
        ret = fmt.format(num);

        // Add dot
        if(numStr.substring(numStr.length() - 1).equals("."))
            ret += ".";

        return ret;
    }

    private void refreshUi()
    {
        // --- Update ui ---

        // -- Text views --

        // First num
        TextView textViewFirst = textViews.get(R.id.textViewFirst);
        if(validNumStr(firstStr))
        {
            textViewFirst.setText(formatNumStr(firstStr));
        }
        else
            textViewFirst.setText("");

        // Second num
        TextView textViewSecond = textViews.get(R.id.textViewSecond);
        if(validNumStr(secondStr))
        {
            String secondStrFmt = formatNumStr(secondStr);

            if(secondStrFmt.charAt(0) == '-')
                secondStrFmt = "(" + secondStrFmt + ")";

            textViewSecond.setText(secondStrFmt);
        }
        else if(secondStr.equals("-"))
            textViewSecond.setText("(- )");
        else
            textViewSecond.setText("");

        // Operation
        TextView textViewOperation = textViews.get(R.id.textViewOperation);
        textViewOperation.setText(String.valueOf(operationToChar(operation)));
        textViewOperation.setVisibility( firstStr.isEmpty() ? View.INVISIBLE : View.VISIBLE);

        // Result
        TextView textViewResult = textViews.get(R.id.textViewResult);
        if(validNumStr(firstStr) &&
                validNumStr(secondStr))
        {
            // Enum
            double result = operationExec(
                    Double.valueOf(firstStr),
                    operation,
                    Double.valueOf(secondStr));
            String resultStr = (result == Math.round(result)) ?
                    String.valueOf((long) result) :
                    String.valueOf(result);

            // Show
            textViewResult.setText(formatNumStr(resultStr));
        }
        else
            textViewResult.setText("");

        // Equal
        textViews.get(R.id.textViewEqual).setVisibility( (validNumStr(firstStr) && validNumStr(secondStr)) ? View.VISIBLE : View.INVISIBLE );

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
                !firstStr .isEmpty() ||
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Open main activity
        setContentView(R.layout.activity_main);

        // Get buttons array
        buttons = findViewsByIds(buttonIds);

        // Get textViews array
        textViews = findViewsByIds(textViewIds);

        // Create listener
        View.OnClickListener onClickListener = view -> {

            // Get view id
            int viewId = view.getId();

            // Old result
            double oldResult = 0;
            if(validNumStr(firstStr) && validNumStr(secondStr))
            {
                oldResult = operationExec(Double.valueOf(firstStr), operation, Double.valueOf(secondStr));
            }

            // --- OnClick events ---

            switch(viewId)
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
                    // Check lengths
                    int dotIndex = secondStr.indexOf('.');
                    boolean lenGood = (dotIndex == -1 && secondStr.length() < maxLen) ||
                            (dotIndex >= 0 && secondStr.substring(0, dotIndex).length() < maxLen);
                    boolean lenFractionGood = (dotIndex == -1) ||
                            (secondStr.substring(dotIndex).length() <= maxFractionLen);
                    if( !lenGood )
                    {
                        // Too much numbers
                        Toast toastMaxLen = Toast.makeText(getApplicationContext(),
                                String.valueOf(maxLen) + " numbers is max length!",
                                Toast.LENGTH_SHORT);
                        toastMaxLen.show();
                    }
                    else if(!lenFractionGood)
                    {
                        // Too much numbers
                        Toast toastMaxLen = Toast.makeText(getApplicationContext(),
                                String.valueOf(maxFractionLen) + " fraction numbers is max length!",
                                Toast.LENGTH_SHORT);
                        toastMaxLen.show();
                    }
                    else
                    {
                        // Add number
                        secondStr += buttons.get(viewId).getText();

                        // Auto dot
                        if(viewId == R.id.button0 &&
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

                    if(oldResult != 0)
                    {
                        if(oldResult == Math.round(oldResult))
                            firstStr = String.valueOf((long) oldResult);
                        else
                            firstStr = String.valueOf(oldResult);
                    }
                    else
                    {
                        if(validNumStr(secondStr))
                        {
                            firstStr = secondStr;
                        }
                    }
                    secondStr = "";

                    // Set operation
                    switch (viewId) {
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
                                    if(secondStr.isEmpty())
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
                    if(!secondStr.isEmpty() &&
                            secondStr.indexOf('.') == -1)
                    {
                        secondStr += ".";
                    }
                    break;

                // -- Clear --
                case R.id.buttonClear:

                    if(!firstStr.isEmpty() || !secondStr.isEmpty())
                    {
                        // Get clear string
                        String clearString = (!secondStr.isEmpty() ? secondStr : firstStr);

                        // Get clear length
                        int clearLen = 1;
                        for (String mClearStr : clearArray) {
                            if (clearString.length() >= mClearStr.length() &&
                                    clearString.substring(clearString.length() - mClearStr.length())
                                            .equalsIgnoreCase(mClearStr)) {
                                clearLen = mClearStr.length();
                            }
                        }

                        // Local delete
                        clearString = clearString.substring(0, clearString.length() - clearLen);

                        // Global delete
                        if(!secondStr.isEmpty())
                            secondStr = clearString;
                        else
                            firstStr = clearString;

                        // Move first to second num
                        if (secondStr.isEmpty() &&
                                !firstStr.isEmpty()) {
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
        for(int mButtonId = 0; mButtonId < buttons.size(); mButtonId++)
        {
            int mButtonKey = buttons.keyAt(mButtonId);

            buttons.get(mButtonKey).setOnClickListener(onClickListener);
        }

        // Refresh ui
        refreshUi();
    }

}
