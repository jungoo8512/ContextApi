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

package uk.co.md87.android.sensorlogger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.util.Log;
import android.widget.TextView;

import com.flurry.android.FlurryAgent;

import java.util.TimerTask;

import uk.co.md87.android.sensorlogger.R;

/**
 *
 * @author chris
 */
public class RecordingActivity extends BoundActivity {

    private final Handler handler = new Handler();
    int state = 3;
    int phase = 3;

    private final TimerTask task = new TimerTask() {
            @Override
            public void run() {
                checkSamples();
            }
    };

    /** {@inheritDoc} */
    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.recording);

        handler.removeCallbacks(task);
        handler.postDelayed(task, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        handler.removeCallbacks(task);
    }

    public void checkSamples() {
        try {
            phase = (phase + 1) % 4;

            String text = "?";
            switch (phase) {
                case 0: text = ".  "; break;
                case 1: text = " . "; break;
                case 2: text = "  ."; break;
                case 3: text = " . "; break;
            }

            ((TextView) findViewById(R.id.recordingcount)).setText(text);

            final int serviceState = service.getState();

            if (serviceState > state) {
                state = serviceState;

                if (state == 4) {
                    setTitle("Sensor Logger > Analysing");
                    ((TextView) findViewById(R.id.recordingheader)).setTag(R.string.analysingheader);
                }
            }

            if (serviceState > 4) {
                FlurryAgent.onEvent("countdown_to_results");
                startActivity(new Intent(this, ResultsActivity.class));
                finish();
            } else {
                handler.postDelayed(task, 500);
            }
        } catch (RemoteException ex) {
            Log.e(getClass().getName(), "Error getting countdown", ex);
        }
    }

    /** {@inheritDoc} */
    @Override
    protected void onStart() {
        super.onStart();

        FlurryAgent.onStartSession(this, "TFBJJPQUQX3S1Q6IUHA6");
    }

    /** {@inheritDoc} */
    @Override
    protected void onStop() {
        super.onStop();

        FlurryAgent.onEndSession(this);
    }

}
