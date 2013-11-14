package in.techdinosaurs.sound;

import android.app.Activity;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.media.audiofx.NoiseSuppressor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;
import edu.emory.mathcs.jtransforms.fft.DoubleFFT_1D;

public class MainActivity extends Activity {
	private static int[] mSampleRates = new int[] { 8000, 11025, 22050, 44100 };
	private AudioRecord recorder;
	private TextView mtexto;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		recorder = findAudioRecord();
		mtexto = (TextView) findViewById(R.id.tv_wat);
		new readFrequencies().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public AudioRecord findAudioRecord() {
	    for (int rate : mSampleRates) {
	        for (short audioFormat : new short[] { AudioFormat.ENCODING_PCM_8BIT, AudioFormat.ENCODING_PCM_16BIT }) {
	            for (short channelConfig : new short[] { AudioFormat.CHANNEL_IN_MONO, AudioFormat.CHANNEL_IN_STEREO }) {
	                try {
	                    Log.d("ABCG", "Attempting rate " + rate + "Hz, bits: " + audioFormat + ", channel: "
	                            + channelConfig);
	                    int bufferSize = AudioRecord.getMinBufferSize(rate, channelConfig, audioFormat);

	                    if (bufferSize != AudioRecord.ERROR_BAD_VALUE) {
	                        // check if we can instantiate and have a success
	                        AudioRecord recorder = new AudioRecord(AudioSource.DEFAULT, rate, channelConfig, audioFormat, bufferSize);

	                        if (recorder.getState() == AudioRecord.STATE_INITIALIZED)
	                            return recorder;
	                    }
	                } catch (Exception e) {
	                    Log.e("ABC", rate + "Exception, keep trying.",e);
	                }
	            }
	        }
	    }
	    return null;
	}
	private class readFrequencies extends AsyncTask<Void,Integer,Integer> {
		
		@Override
		protected Integer doInBackground(Void... arg0) {
			int frequency = 8000;
			int channelConfiguration = AudioFormat.CHANNEL_CONFIGURATION_STEREO;
			int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
			int bufferSize = AudioRecord.getMinBufferSize(frequency,
					channelConfiguration, audioEncoding);
			boolean recording = true;

			int rate = 44100;
			short audioFormat = AudioFormat.ENCODING_PCM_16BIT;
			short channelConfig = AudioFormat.CHANNEL_IN_MONO;
			

			try {
				bufferSize = AudioRecord.getMinBufferSize(rate,channelConfig, audioFormat);
				if (recorder.getState() == AudioRecord.STATE_INITIALIZED) {
					/*
					 *  Android 4.1.2
					 */ 
                   // int recorder_id = recorder.getAudioSessionId();
                    //if (NoiseSuppressor.isAvailable()) NoiseSuppressor.create(recorder_id);
					 
				}
				else {
					Toast.makeText(getApplicationContext(), 
							"Error en la inicializaci—n", Toast.LENGTH_SHORT).show();
				}
			} catch (Exception e) {}

			short[] audioData = new short[bufferSize];

			if (recorder != null) {
				while (recording) {
					if (recorder.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
						recorder.startRecording();
					}
					else {
						int numshorts = recorder.read(audioData,0,audioData.length);
						if ((numshorts != AudioRecord.ERROR_INVALID_OPERATION) && 
								(numshorts != AudioRecord.ERROR_BAD_VALUE)) {

							//  Hann
							double[] preRealData = new double[bufferSize];
							double PI = 3.14159265359;
							for (int i = 0; i < bufferSize; i++) {
								double multiplier = 0.5 * (1 - Math.cos(2*PI*i/(bufferSize-1)));
								preRealData[i] = multiplier * audioData[i];
							}

							DoubleFFT_1D fft = new DoubleFFT_1D(bufferSize);
							double[] realData = new double[bufferSize * 2];

							for (int i=0;i<bufferSize;i++) {
								realData[2*i] = preRealData[i];
								realData[2*i+1] = 0;    
							}
							fft.complexForward(realData);

							double magnitude[] = new double[bufferSize / 2];

							for (int i = 0; i < magnitude.length; i++) {
								double R = realData[2 * i];
								double I = realData[2 * i + 1];

								magnitude[i] = Math.sqrt(I*I + R*R);
							}

							int maxIndex = 0;
							double max = magnitude[0];
							for(int i = 1; i < magnitude.length; i++) {
								if (magnitude[i] > max) {
									max = magnitude[i];
									maxIndex = i;
								}
							}

							int frequency2 = rate * maxIndex / (bufferSize *2);
							if(frequency2 > 5000) {
								Log.w("FREQ", Integer.toString(frequency2));
							}
							publishProgress(frequency2);
							
						}
						else {
							if (numshorts == AudioRecord.ERROR_BAD_VALUE) {
								Toast.makeText(getApplicationContext(), 
										"ERROR_BAD_VALUE", Toast.LENGTH_SHORT).show();
							}
							else {
								Toast.makeText(getApplicationContext(), 
										"ERROR_INVALID_OPERATION", Toast.LENGTH_SHORT).show();
							}

							return -1;
						}
					}
				}

				if (recorder.getState() == AudioRecord.RECORDSTATE_RECORDING) 
					recorder.stop(); //stop the recorder before ending the thread
				recorder.release();
				recorder=null;
			}
			return 0;
		}

		protected void onProgressUpdate(Integer... f) {
			//TextView texto = (TextView) findViewById(R.id.texto);
			if(f[0] >= 15000) {
				Log.w("ABC", "Progress update" + String.valueOf(f[0]));
				mtexto.setText(String.valueOf(f[0]));
			}
			
		}

		protected void onPostExecute(Integer f) {

			int frequencies = f.intValue();
//			mtexto.setText(String.valueOf(frequencies));
			Log.w("ABC", "Post Execute" + Integer.toString(frequencies));
		}
	}
}
