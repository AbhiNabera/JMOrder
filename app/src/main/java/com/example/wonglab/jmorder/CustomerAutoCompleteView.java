package com.example.wonglab.jmorder;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;

public class CustomerAutoCompleteView extends android.support.v7.widget.AppCompatAutoCompleteTextView {

    public CustomerAutoCompleteView(Context context) {
        super(context);
    }

    public CustomerAutoCompleteView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomerAutoCompleteView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    // this is how to disable AutoCompleteTextView filter
    @Override
    protected void performFiltering(final CharSequence text, final int keyCode) {
        String filterText = "";
        super.performFiltering(text, keyCode);
    }

    /*
     * after a selection we have to capture the new value and append to the existing text
     */

    @Override
    protected void replaceText(final CharSequence text) {
        super.replaceText(text);
    }

}
