package org.openhds.mobile.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.openhds.mobile.R;

public class SearchActivity extends Activity implements View.OnClickListener {

    public static final String LABEL_KEY = "label";
    public static final String VALUE_KEY = "value";
    public static final String VALUE_HINT_KEY = "value_hint";
    public static final String CANCEL_LABEL_KEY = "cancel_label";
    public static final String OK_LABEL_KEY = "ok_label";

    TextView labelView;
    EditText valueView;
    Button okButton;
    Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        labelView = (TextView) findViewById(R.id.search_label);
        valueView = (EditText) findViewById(R.id.search_value);
        okButton = (Button) findViewById(R.id.ok_button);
        cancelButton = (Button) findViewById(R.id.cancel_button);

        Bundle extras = getIntent().getExtras();
        if (extras.containsKey(LABEL_KEY)) {
            labelView.setText(extras.getString(LABEL_KEY));
        }
        if (extras.containsKey(VALUE_KEY)) {
            valueView.setText(extras.getString(VALUE_KEY));
        }
        if (extras.containsKey(VALUE_HINT_KEY)) {
            valueView.setHint(extras.getString(VALUE_HINT_KEY));
        }
        if (extras.containsKey(CANCEL_LABEL_KEY)) {
            cancelButton.setText(extras.getString(CANCEL_LABEL_KEY));
        }
        if (extras.containsKey(OK_LABEL_KEY)) {
            okButton.setText(extras.getString(OK_LABEL_KEY));
        }

        cancelButton.setOnClickListener(this);
        okButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == okButton) {
            Intent result = new Intent();
            result.putExtras(getIntent().getExtras());
            result.putExtra(VALUE_KEY, valueView.getText());
            setResult(RESULT_OK, result);
        }
        finish();
    }
}
