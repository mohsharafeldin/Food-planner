package com.example.foodplanner.presentation.videoplayer.presenter;

import com.example.foodplanner.base.BasePresenter;
import com.example.foodplanner.presentation.videoplayer.view.VideoPlayerView;

/**
 * Presenter implementation for Video Player screen (MVP pattern).
 */
public class VideoPlayerPresenterImpl extends BasePresenter<VideoPlayerView>
        implements VideoPlayerPresenterContract {

    public VideoPlayerPresenterImpl() {
        // No repository needed for video player - it just handles video playback
    }

    @Override
    public void loadVideo(String videoUrl) {
        if (!isViewAttached())
            return;

        if (videoUrl == null || videoUrl.isEmpty()) {
            view.showError("No video URL provided");
            view.closePlayer();
            return;
        }

        view.initializePlayer(videoUrl);
    }
}
