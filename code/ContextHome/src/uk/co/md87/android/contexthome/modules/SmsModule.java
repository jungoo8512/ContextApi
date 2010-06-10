/*
 * Copyright (c) 2009-2010 Chris Smith
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package uk.co.md87.android.contexthome.modules;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import uk.co.md87.android.contexthome.Module;
import uk.co.md87.android.contexthome.R;

/**
 * A module which displays SMS messages.
 *
 * @author chris
 */
public class SmsModule implements Module {

    private static final Uri INBOX_URI = Uri.parse("content://sms/inbox");

    /** {@inheritDoc} */
    @Override
    public View getView(final Context context, final int weight) {
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final Cursor cursor = context.getContentResolver().query(INBOX_URI,
                new String[] { "thread_id", "date", "body", "address" }, null, null, "date DESC");
        final int idIndex = cursor.getColumnIndex("thread_id");
        final int bodyIndex = cursor.getColumnIndex("body");
        final int addressIndex = cursor.getColumnIndex("address");

        final LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        boolean success = cursor.moveToFirst();
        for (int i = 0; i < weight && success; i++) {
            final String body = cursor.getString(bodyIndex);
            final String address = cursor.getString(addressIndex);

            layout.addView(getView(context, layout, body, address, cursor.getLong(idIndex)),
                    params);

            success = cursor.moveToNext();
        }
        cursor.close();
        

        return layout;
    }

    private View getView(final Context context, ViewGroup layout, String text,
            String address, final long threadId) {
        final View view = View.inflate(context, R.layout.titledimage, null);
        view.setClickable(true);
        view.setFocusable(true);
        view.setOnClickListener(new View.OnClickListener() {

            public void onClick(View arg0) {
                final Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("content://mms-sms/conversations/" + threadId));
                context.startActivity(intent);
            }
        });

        final Uri contactUri = Uri.withAppendedPath(Contacts.Phones.CONTENT_FILTER_URL,
                Uri.encode(address));

        final Cursor cursor = context.getContentResolver().query(contactUri,
                new String[] { Contacts.Phones.PERSON_ID,
                Contacts.Phones.DISPLAY_NAME }, null, null, null);

        final TextView title = (TextView) view.findViewById(R.id.title);
        final ImageView image = (ImageView) view.findViewById(R.id.image);

        if (cursor.moveToFirst()) {
            title.setText(Html.fromHtml("<b>" + cursor.getString(cursor
                    .getColumnIndex(Contacts.Phones.DISPLAY_NAME)) + "</b>"));
            Uri uri = ContentUris.withAppendedId(People.CONTENT_URI,
                    cursor.getLong(cursor.getColumnIndex(Contacts.Phones.PERSON_ID)));
            image.setImageBitmap(Contacts.People.loadContactPhoto(context,
                    uri, R.drawable.blank, null));
        } else {
            title.setText(Html.fromHtml("<b>" + address + "</b>"));
        }

        cursor.close();

        final TextView body = (TextView) view.findViewById(R.id.body);
        body.setText(text);

        return view;
    }

}