package com.example.foodplanner.presentation.videoplayer.presenter;

import com.example.foodplanner.presentation.videoplayer.view.VideoPlayerView;

/**
 * Contract interface for Video Player Presenter (MVP pattern).
 */
public interface VideoPlayerPresenterContract {
    void attachView(VideoPlayerView view);

    void detachView();

    void loadVideo(String videoUrl);
}
