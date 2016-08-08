/*
 * Copyright (C) 2011 Google Inc.
 * Licensed to The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package meet.you.data;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.Time;

/**
 * Loader for a set of photo IDs.
 */
public class MeetCursorLoader extends CursorLoader {

	public static String[] PROJECTION = new String[] { MeetData.KEY_ID,
			MeetData.KEY_TOPIC, MeetData.KEY_WHEN, MeetData.KEY_WHERE };

	private final Uri mUri = MeetData.URI;
	private final String[] mDataProjection;

	private final long mDateMillis;

	public MeetCursorLoader(Context context, long date, String[] projection) {

		super(context);
		mDateMillis = date;
		mDataProjection = projection;
	}

	@Override
	public Cursor loadInBackground() {

		Time focusTime = new Time();
		focusTime.set(mDateMillis);

		GregorianCalendar focusDate = new GregorianCalendar(focusTime.year,
				focusTime.month, focusTime.monthDay);

		long todayStart = focusDate.getTimeInMillis();

		focusDate.roll(Calendar.DAY_OF_MONTH, true);

		long todayEnd = focusDate.getTimeInMillis();

		setUri(mUri);
		setProjection(mDataProjection);
		setSelection(MeetData.KEY_WHEN + ">=? " + " AND " + MeetData.KEY_WHEN
				+ "< ?");

		setSelectionArgs(new String[] { String.valueOf(todayStart),
				String.valueOf(todayEnd) });

		return super.loadInBackground();
	}
}
