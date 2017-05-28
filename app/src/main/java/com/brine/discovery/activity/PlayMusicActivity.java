package com.brine.discovery.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.brine.discovery.R;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PlayMusicActivity extends Activity {
    public final static String URL = "stream_url";
    public final static String SONG_NAME = "song_name";

    private Button mBtnForward, mBtnPause, mBtnBack, mBtnRewind;
    private ImageView mImgSoundCloud;
    private MediaPlayer mMediaPlayer;

    private double mStartTime = 0;
    private double mFinalTime = 0;

    private Handler mMyHandler = new Handler();;
    private int mForwardTime = 5000;
    private int mBackwardTime = 5000;
    private SeekBar mSeekbar;
    private TextView mTvTimeCount, mTvTimesSong, mTvSongName;

    public static int mOneTimeOnly = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_music);

        mBtnForward = (Button) findViewById(R.id.button_forward);
        mBtnPause = (Button) findViewById(R.id.button_pause);
        mBtnBack = (Button)findViewById(R.id.button_back);
        mBtnRewind = (Button)findViewById(R.id.button_rewind);
        mImgSoundCloud = (ImageView)findViewById(R.id.imageView);

        mTvTimeCount = (TextView)findViewById(R.id.tv_time_count);
        mTvTimesSong = (TextView)findViewById(R.id.tv_times_song);
        mTvSongName = (TextView)findViewById(R.id.tv_song_name);

        String musicUrl = getIntent().getStringExtra(URL);
        String songName = getIntent().getStringExtra(SONG_NAME);
        if(songName.length() > 15){
            songName = songName.substring(0, 15) + " ...";
        }
        mTvSongName.setText(songName);

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        if(isNetworkAvailable()) {
            new SCPlayer().execute(musicUrl);
        }else{
            Toast.makeText(getApplicationContext(), "Network not connection.", Toast.LENGTH_LONG).show();
            return;
        }

        mSeekbar = (SeekBar)findViewById(R.id.seekBar);
        mSeekbar.setClickable(false);
        mBtnPause.setEnabled(false);

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Playing sound",Toast.LENGTH_SHORT).show();
                        mMediaPlayer.start();

                mFinalTime = mMediaPlayer.getDuration();
                mStartTime = mMediaPlayer.getCurrentPosition();

                if (mOneTimeOnly == 0) {
                    mSeekbar.setMax((int) mFinalTime);
                    mOneTimeOnly = 1;
                }

                mTvTimesSong.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) mFinalTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) mFinalTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        mFinalTime)))
                );

                mTvTimeCount.setText(String.format("%d min, %d sec",
                        TimeUnit.MILLISECONDS.toMinutes((long) mStartTime),
                        TimeUnit.MILLISECONDS.toSeconds((long) mStartTime) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                        mStartTime)))
                );

                mSeekbar.setProgress((int) mStartTime);
                mMyHandler.postDelayed(UpdateSongTime,100);
                mBtnPause.setEnabled(true);
                mBtnBack.setEnabled(false);
            }
        });

        mBtnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Pausing sound",Toast.LENGTH_SHORT).show();
                        mMediaPlayer.pause();
                mBtnPause.setEnabled(false);
                mBtnBack.setEnabled(true);
            }
        });

        mBtnForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int) mStartTime;

                if((temp+ mForwardTime)<= mFinalTime){
                    mStartTime = mStartTime + mForwardTime;
                    mMediaPlayer.seekTo((int) mStartTime);
                    Toast.makeText(getApplicationContext(),"You have Jumped forward 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Cannot jump forward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });

        mBtnRewind.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int temp = (int) mStartTime;

                if((temp- mBackwardTime)>0){
                    mStartTime = mStartTime - mBackwardTime;
                    mMediaPlayer.seekTo((int) mStartTime);
                    Toast.makeText(getApplicationContext(),"You have Jumped backward 5 seconds",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),"Cannot jump backward 5 seconds",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private Runnable UpdateSongTime = new Runnable() {
        public void run() {
            mStartTime = mMediaPlayer.getCurrentPosition();
            mTvTimeCount.setText(String.format("%d min, %d sec",
                    TimeUnit.MILLISECONDS.toMinutes((long) mStartTime),
                    TimeUnit.MILLISECONDS.toSeconds((long) mStartTime) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.
                                    toMinutes((long) mStartTime)))
            );
            mSeekbar.setProgress((int) mStartTime);
            mMyHandler.postDelayed(this, 100);
        }
    };

    private class SCPlayer extends AsyncTask<String, Void, Boolean> {
        private ProgressDialog progress;

        SCPlayer() {
            progress = new ProgressDialog(PlayMusicActivity.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            super.onPreExecute();
            this.progress.setMessage("Buffering...");
            this.progress.show();
        }

        @Override
        protected Boolean doInBackground(String... params) {
            Boolean prepared = false;
            try {
                mMediaPlayer.setDataSource(params[0]);
                mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mediaPlayer) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                    }
                });
                mMediaPlayer.prepare();
                prepared = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return prepared;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            progress.dismiss();
            Log.d("Prepared", "//" + result);
            playingSound();
        }
    }

    private void playingSound(){
        Toast.makeText(getApplicationContext(), "Playing sound",Toast.LENGTH_SHORT).show();
        mMediaPlayer.start();

        mFinalTime = mMediaPlayer.getDuration();
        mStartTime = mMediaPlayer.getCurrentPosition();

        if (mOneTimeOnly == 0) {
            mSeekbar.setMax((int) mFinalTime);
            mOneTimeOnly = 1;
        }

        mTvTimesSong.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) mFinalTime),
                TimeUnit.MILLISECONDS.toSeconds((long) mFinalTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                mFinalTime)))
        );

        mTvTimeCount.setText(String.format("%d min, %d sec",
                TimeUnit.MILLISECONDS.toMinutes((long) mStartTime),
                TimeUnit.MILLISECONDS.toSeconds((long) mStartTime) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes((long)
                                mStartTime)))
        );

        mSeekbar.setProgress((int) mStartTime);
        mMyHandler.postDelayed(UpdateSongTime,100);
        mBtnPause.setEnabled(true);
        mBtnBack.setEnabled(false);
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
}
