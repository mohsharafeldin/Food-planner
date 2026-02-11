package com.example.foodplanner.presentation.videoplayer.view;

import com.example.foodplanner.presentation.base.BaseView;

/**
 * View interface for Video Player screen (MVP pattern).
 */
public interface VideoPlayerView extends BaseView {
    void initializePlayer(String videoUrl);

    void showBuffering();

    void hideBuffering();

    void showVideoError(String message);

    void closePlayer();
}
