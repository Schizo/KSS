/*
package com.google.android.exoplayer.demo.player;

*/
/**
 * Created by chavez on 7/18/15.
 *//*


import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.MediaFormatHolder;
import com.google.android.exoplayer.SampleHolder;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.SampleSource.SampleSourceReader;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.hls.Subtitle;
import com.google.android.exoplayer.text.SubtitleParser;
import com.google.android.exoplayer.text.SubtitleParserHelper;
import com.google.android.exoplayer.text.TextRenderer;
import com.google.android.exoplayer.util.Assertions;

import android.annotation.TargetApi;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

*/
/**
 * A {@link TrackRenderer} for textual subtitles. The actual rendering of each line of text to a
 * suitable output (e.g. the display) is delegated to a {@link TextRenderer}.
 *//*

@TargetApi(16)
public class MiscTrackRenderer extends TrackRenderer implements Callback {*/
/*

    private static final int MSG_UPDATE_OVERLAY = 0;

    private final Handler textRendererHandler;
    private final TextRenderer textRenderer;
    private final SampleSourceReader source;
    private final MediaFormatHolder formatHolder;
    private final SubtitleParser[] subtitleParsers;

    private int parserIndex;
    private int trackIndex;

    private boolean inputStreamEnded;

    private Subtitle subtitle;
    private Subtitle nextSubtitle;
    private SubtitleParserHelper parserHelper;
    private HandlerThread parserThread;
    private int nextSubtitleEventIndex;

    *//*
*/
/**
     * @param source A source from which samples containing subtitle data can be read.
     * @param textRenderer The text renderer.
     * @param textRendererLooper The looper associated with the thread on which textRenderer should be
     *     invoked. If the renderer makes use of standard Android UI components, then this should
     *     normally be the looper associated with the applications' main thread, which can be
     *     obtained using {@link android.app.Activity#getMainLooper()}. Null may be passed if the
     *     renderer should be invoked directly on the player's internal rendering thread.
     * @param subtitleParsers An array of available subtitle parsers. Where multiple parsers are able
     *     to render a subtitle, the one with the lowest index will be preferred.
     *//*
*/
/*
    public MiscTrackRenderer(SampleSource source, TextRenderer textRenderer,
                             Looper textRendererLooper, SubtitleParser... subtitleParsers) {
        this.source = source.register();
        this.textRenderer = Assertions.checkNotNull(textRenderer);
        this.textRendererHandler = textRendererLooper == null ? null
                : new Handler(textRendererLooper, this);
        this.subtitleParsers = Assertions.checkNotNull(subtitleParsers);
        formatHolder = new MediaFormatHolder();
    }

    @Override
    protected int doPrepare(long positionUs) throws ExoPlaybackException {
        try {
            boolean sourcePrepared = source.prepare(positionUs);
            if (!sourcePrepared) {
                return TrackRenderer.STATE_UNPREPARED;
            }
        } catch (IOException e) {
            throw new ExoPlaybackException(e);
        }
        for (int i = 0; i < subtitleParsers.length; i++) {
            for (int j = 0; j < source.getTrackCount(); j++) {
                if (subtitleParsers[i].canParse(source.getTrackInfo(j).mimeType)) {
                    parserIndex = i;
                    trackIndex = j;
                    return TrackRenderer.STATE_PREPARED;
                }
            }
        }
        return TrackRenderer.STATE_IGNORE;
    }

    @Override
    protected void onEnabled(long positionUs, boolean joining) {
        source.enable(trackIndex, positionUs);
        parserThread = new HandlerThread("textParser");
        parserThread.start();
        parserHelper = new SubtitleParserHelper(parserThread.getLooper(), subtitleParsers[parserIndex]);
        seekToInternal();
    }

    @Override
    protected void seekTo(long positionUs) {
        source.seekToUs(positionUs);
        seekToInternal();
    }

    private void seekToInternal() {
        inputStreamEnded = false;
        subtitle = null;
        nextSubtitle = null;
        parserHelper.flush();
        clearTextRenderer();
    }

    @Override
    protected void doSomeWork(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
        try {
            source.continueBuffering(trackIndex, positionUs);
        } catch (IOException e) {
            throw new ExoPlaybackException(e);
        }

        if (nextSubtitle == null) {
            try {
                nextSubtitle = parserHelper.getAndClearResult();
            } catch (IOException e) {
                throw new ExoPlaybackException(e);
            }
        }

        boolean textRendererNeedsUpdate = false;
        long subtitleNextEventTimeUs = Long.MAX_VALUE;
        if (subtitle != null) {
            // We're iterating through the events in a subtitle. Set textRendererNeedsUpdate if we
            // advance to the next event.
            subtitleNextEventTimeUs = getNextEventTime();
            while (subtitleNextEventTimeUs <= positionUs) {
                nextSubtitleEventIndex++;
                subtitleNextEventTimeUs = getNextEventTime();
                textRendererNeedsUpdate = true;
            }
        }

        if (subtitleNextEventTimeUs == Long.MAX_VALUE && nextSubtitle != null
                && nextSubtitle.getStartTime() <= positionUs) {
            // Advance to the next subtitle. Sync the next event index and trigger an update.
            subtitle = nextSubtitle;
            nextSubtitle = null;
            nextSubtitleEventIndex = subtitle.getNextEventTimeIndex(positionUs);
            textRendererNeedsUpdate = true;
        }

        if (textRendererNeedsUpdate && getState() == TrackRenderer.STATE_STARTED) {
            // textRendererNeedsUpdate is set and we're playing. Update the renderer.
            updateTextRenderer(subtitle.getCues(positionUs));
        }

        if (!inputStreamEnded && nextSubtitle == null && !parserHelper.isParsing()) {
            // Try and read the next subtitle from the source.
            try {
                SampleHolder sampleHolder = parserHelper.getSampleHolder();
                sampleHolder.clearData();
                int result = source.readData(trackIndex, positionUs, formatHolder, sampleHolder, false);
                if (result == SampleSource.SAMPLE_READ) {
                    parserHelper.startParseOperation();
                } else if (result == SampleSource.END_OF_STREAM) {
                    inputStreamEnded = true;
                }
            } catch (IOException e) {
                throw new ExoPlaybackException(e);
            }
        }
    }

    @Override
    protected void onDisabled() {
        subtitle = null;
        nextSubtitle = null;
        parserThread.quit();
        parserThread = null;
        parserHelper = null;
        clearTextRenderer();
        source.disable(trackIndex);
    }

    @Override
    protected void onReleased() {
        source.release();
    }

    @Override
    protected long getDurationUs() {
        return source.getTrackInfo(trackIndex).durationUs;
    }

    @Override
    protected long getBufferedPositionUs() {
        // Don't block playback whilst subtitles are loading.
        return END_OF_TRACK_US;
    }

    @Override
    protected boolean isEnded() {
        return inputStreamEnded && (subtitle == null || getNextEventTime() == Long.MAX_VALUE);
    }

    @Override
    protected boolean isReady() {
        // Don't block playback whilst subtitles are loading.
        // Note: To change this behavior, it will be necessary to consider [Internal: b/12949941].
        return true;
    }

    private long getNextEventTime() {
        return ((nextSubtitleEventIndex == -1)
                || (nextSubtitleEventIndex >= subtitle.getEventTimeCount())) ? Long.MAX_VALUE
                : (subtitle.getEventTime(nextSubtitleEventIndex));
    }

    private void updateTextRenderer(List<Cue> cues) {
        if (textRendererHandler != null) {
            textRendererHandler.obtainMessage(MSG_UPDATE_OVERLAY, cues).sendToTarget();
        } else {
            invokeRendererInternalCues(cues);
        }
    }

    private void clearTextRenderer() {
        updateTextRenderer(Collections.<Cue>emptyList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_OVERLAY:
                invokeRendererInternalCues((List<Cue>) msg.obj);
                return true;
        }
        return false;
    }

    private void invokeRendererInternalCues(List<Cue> cues) {
        textRenderer.onCues(cues);
    }*//*


}*/
