/*
 * Copyright 2018 Dmytro Ponomarenko
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.playback.soundrec;

/**
 * AppConstants that may be used in multiple classes.
 */
public class AppConstants {

	private AppConstants() {}




	public static final String FORMAT_PCM = "pcm";
	public static final String FORMAT_AAC = "aac";


	public static final String NAME_FORMAT_RECORD = "record";
	public static final String NAME_FORMAT_TIMESTAMP = "timestamp";
	public static final String NAME_FORMAT_DATE = "date";
	public static final String NAME_FORMAT_DATE_US = "date_us";
	public static final String NAME_FORMAT_DATE_ISO8601 = "date_iso8601";

	//END-------------- Waveform visualisation constants ----------------------------------------

	public static final int TIME_FORMAT_24H = 11;
	public static final int TIME_FORMAT_12H = 12;

	// recording and playback
	public static final int PLAYBACK_SAMPLE_RATE = 44100;
	public static final int RECORD_SAMPLE_RATE_44100 = 44100;
	public static final int RECORD_SAMPLE_RATE_8000 = 8000;
	public static final int RECORD_SAMPLE_RATE_16000 = 16000;
	public static final int RECORD_SAMPLE_RATE_22050 = 22050;
	public static final int RECORD_SAMPLE_RATE_32000 = 32000;
	public static final int RECORD_SAMPLE_RATE_48000 = 48000;

	public static final int RECORD_ENCODING_BITRATE_12000 = 12000; //Bitrate for 3gp format
	public static final int RECORD_ENCODING_BITRATE_24000 = 24000;
	public static final int RECORD_ENCODING_BITRATE_48000 = 48000;
	public static final int RECORD_ENCODING_BITRATE_96000 = 96000;
	public static final int RECORD_ENCODING_BITRATE_128000 = 128000;
	public static final int RECORD_ENCODING_BITRATE_192000 = 192000;
	public static final int RECORD_ENCODING_BITRATE_256000 = 256000;

	public static final int SORT_DATE = 1;
	public static final int SORT_NAME = 2;
	public static final int SORT_DURATION = 3;
	public static final int SORT_DATE_DESC = 4;
	public static final int SORT_NAME_DESC = 5;
	public static final int SORT_DURATION_DESC = 6;

//	public final static int RECORD_AUDIO_CHANNELS_COUNT = 2;
	public final static int RECORD_AUDIO_MONO = 1;
	public final static int RECORD_AUDIO_STEREO = 2;

	public static final String DEFAULT_RECORDING_FORMAT = FORMAT_PCM;
	public static final String DEFAULT_NAME_FORMAT = NAME_FORMAT_RECORD;
	public static final int DEFAULT_RECORD_SAMPLE_RATE = RECORD_SAMPLE_RATE_44100;
	public static final int DEFAULT_RECORD_ENCODING_BITRATE = RECORD_ENCODING_BITRATE_128000;

	/** Time interval for Recording progress visualisation. */
	public final static int RECORDING_VISUALIZATION_INTERVAL = 13; //mills
	public final static int PLAYBACK_VISUALIZATION_INTERVAL = (int)(2.1* RECORDING_VISUALIZATION_INTERVAL); //mills

	public final static int RECORD_BYTES_PER_SECOND = RECORD_ENCODING_BITRATE_48000 /8; //bits per sec converted to bytes per sec.
	public final static int MIGRATE_PUBLIC_STORAGE_WARNING_COOLDOWN_MILLS = 12*60*60*1000; //12 hours

}
