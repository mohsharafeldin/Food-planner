package com.example.foodplanner.presentation.videoplayer.view;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import com.example.foodplanner.utils.SnackbarUtils;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.media3.common.MediaItem;
import androidx.media3.common.PlaybackException;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.foodplanner.R;
import com.example.foodplanner.presentation.videoplayer.presenter.VideoPlayerPresenterContract;
import com.example.foodplanner.presentation.videoplayer.presenter.VideoPlayerPresenterImpl;

public class VideoActivity extends AppCompatActivity implements VideoPlayerView {

    private PlayerView playerView;
    private ExoPlayer exoPlayer;
    private ProgressBar progressBar;
    private VideoPlayerPresenterContract presenter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        playerView = findViewById(R.id.player_view);
        progressBar = findViewById(R.id.progress_bar);

        setupPresenter();

        String videoUrl = null;
        if (getIntent() != null) {
            videoUrl = getIntent().getStringExtra("video_url");
        }

        presenter.loadVideo(videoUrl);
    }

    private void setupPresenter() {
        presenter = new VideoPlayerPresenterImpl();
        presenter.attachView(this);
    }

    // ============ VideoPlayerView Implementation ============

    @Override
    public void initializePlayer(String videoUrl) {
        exoPlayer = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(exoPlayer);

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        exoPlayer.setMediaItem(mediaItem);

        exoPlayer.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_BUFFERING) {
                    showBuffering();
                } else {
                    hideBuffering();
                }
            }

            @Override
            public void onPlayerError(PlaybackException error) {
                showVideoError(error.getMessage());
            }
        });

        exoPlayer.prepare();
        exoPlayer.play();
    }

    @Override
    public void showBuffering() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideBuffering() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showVideoError(String message) {
        if (findViewById(android.R.id.content) != null) {
            SnackbarUtils.showError(findViewById(android.R.id.content), "Error playing video: " + message);
        }
    }

    @Override
    public void closePlayer() {
        finish();
    }

    @Override
    public void showLoading() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideLoading() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showError(String message) {
        if (findViewById(android.R.id.content) != null) {
            SnackbarUtils.showError(findViewById(android.R.id.content), message);
        }
    }

    @Override
    public void showNetworkError() {
        if (findViewById(android.R.id.content) != null) {
            SnackbarUtils.showError(findViewById(android.R.id.content), "Network error. Please check your connection.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter != null) {
            presenter.detachView();
        }
        if (exoPlayer != null) {
            exoPlayer.release();
            exoPlayer = null;
        }
    }
}
