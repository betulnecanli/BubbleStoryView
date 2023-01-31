import android.content.Context
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.betulnecanli.bubblestoryview.R
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.util.Util
import kotlinx.android.synthetic.main.fragment_story_display.*

import java.util.*

class StoryDisplayFragment : Fragment(),
    StoriesProgressView.StoriesListener {
    // Define position and retrieve the position passed as an argument from the calling activity
    private val position: Int by
    lazy { arguments?.getInt(EXTRA_POSITION) ?: 0 }
    // Define storyUser and retrieve the storyUser object passed as an argument from the calling activity
    private val storyUser: StoryUser by
    lazy {
        (arguments?.getParcelable<StoryUser>(
            EXTRA_STORY_USER
        ) as StoryUser)
    }

    private val stories: ArrayList<Story> by
    lazy { storyUser.stories }

    private var simpleExoPlayer: SimpleExoPlayer? = null
    private lateinit var mediaDataSourceFactory: DataSource.Factory
    private var pageViewOperator: PageViewOperator? = null
    private var counter = 0
    private var pressTime = 0L
    private var limit = 500L
    private var onResumeCalled = false
    private var onVideoPrepared = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_story_display, container, false)
    }
    // Set up UI elements and update the story when the view is created
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        storyDisplayVideo.useController = false
        updateStory()
        setUpUi()
    }
    // Set pageViewOperator as the calling context when the fragment is attached

    override fun onAttach(context: Context) {
        super.onAttach(context)
        this.pageViewOperator = context as PageViewOperator
    }
    // Restore the position of the story when the fragment is started

    override fun onStart() {
        super.onStart()
        counter = restorePosition()
    }
    // Play the story and start the animation when the fragment is resumed

    override fun onResume() {
        super.onResume()
        onResumeCalled = true
        if (stories[counter].isVideo() && !onVideoPrepared) {
            simpleExoPlayer?.playWhenReady = false
            return
        }

        simpleExoPlayer?.seekTo(5)
        simpleExoPlayer?.playWhenReady = true
        if (counter == 0) {
            storiesProgressView?.startStories()
        } else {
            // restart animation
            counter = StoryViewActivity.progressState.get(arguments?.getInt(EXTRA_POSITION) ?: 0)
            storiesProgressView?.startStories(counter)
        }
    }
// Pause the story and abandon the animation when the fragment is paused

    override fun onPause() {
        super.onPause()
        simpleExoPlayer?.playWhenReady = false
        storiesProgressView?.abandon()
    }

    // This function is called when the `ExoPlayer` completes playing the media
    override fun onComplete() {
        // Release the `ExoPlayer` instance to free up resources
        simpleExoPlayer?.release()
        // Trigger the next page view
        pageViewOperator?.nextPageView()
    }
    // This function is called when the previous button is clicked
    override fun onPrev() {
        // Check if the current counter is less than 0, return if true
        if (counter - 1 < 0) return
        // Decrement the counter
        --counter
        // Save the current position of the counter
        savePosition(counter)
        // Update the current story
        updateStory()
    }
    // This function is called when the next button is clicked
    override fun onNext() {
        // Check if the current story is the last one in the list, return if true
        if (stories.size <= counter + 1) {
            return
        }
        // Increment the counter
        ++counter
        // Save the current position of the counter
        savePosition(counter)
        // Update the current story
        updateStory()
    }
    // This function is called when the fragment's view is destroyed
    override fun onDestroyView() {
        // Call the super class's implementation of the method
        super.onDestroyView()
        // Release the `ExoPlayer` instance to free up resources
        simpleExoPlayer?.release()
    }
    // This function updates the story display
    private fun updateStory() {
        // Stop the `ExoPlayer` if it's currently playing
        simpleExoPlayer?.stop()
        // Check if the current story is a video
        if (stories[counter].isVideo()) {
            // Show the video display
            storyDisplayVideo.show()
            // Hide the image display
            storyDisplayImage.hide()
            // Show the video progress
            storyDisplayVideoProgress.show()
            // Initialize the `ExoPlayer` instance
            initializePlayer()
        } else {
            // Hide the video display
            storyDisplayVideo.hide()
            // Hide the video progress
            storyDisplayVideoProgress.hide()
            // Show the image display
            storyDisplayImage.show()
            // Load the image into the image display using Glide
            Glide.with(this).load(stories[counter].url).into(storyDisplayImage)
        }
        // Create a Calendar instance with the current story's date
        val cal: Calendar = Calendar.getInstance(Locale.ENGLISH).apply {
            timeInMillis = stories[counter].storyDate
        }
        // Update the text of the story display time with the formatted date string

        storyDisplayTime.text = DateFormat.format("MM-dd-yyyy HH:mm:ss", cal).toString()
    }
    // This function initializes the `ExoPlayer` instance
    private fun initializePlayer() {
        if (simpleExoPlayer == null) {
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
        } else {
            simpleExoPlayer?.release()
            simpleExoPlayer = null
            simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
        }

        mediaDataSourceFactory = CacheDataSourceFactory(
            StoryApp.simpleCache,
            DefaultHttpDataSourceFactory(
                Util.getUserAgent(
                    context!!,
                    Util.getUserAgent(requireContext(), getString(R.string.app_name))
                )
            )
        )
        val mediaSource = ProgressiveMediaSource.Factory(mediaDataSourceFactory).createMediaSource(
            Uri.parse(stories[counter].url)
        )
        simpleExoPlayer?.prepare(mediaSource, false, false)
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
        }

        storyDisplayVideo.setShutterBackgroundColor(Color.BLACK)
        storyDisplayVideo.player = simpleExoPlayer

        simpleExoPlayer?.addListener(object : Player.EventListener {
            override fun onPlayerError(error: ExoPlaybackException?) {
                super.onPlayerError(error)
                storyDisplayVideoProgress.hide()
                if (counter == stories.size.minus(1)) {
                    pageViewOperator?.nextPageView()
                } else {
                    storiesProgressView?.skip()
                }
            }

            override fun onLoadingChanged(isLoading: Boolean) {
                super.onLoadingChanged(isLoading)
                if (isLoading) {
                    storyDisplayVideoProgress.show()
                    pressTime = System.currentTimeMillis()
                    pauseCurrentStory()
                } else {
                    storyDisplayVideoProgress.hide()
                    storiesProgressView?.getProgressWithIndex(counter)
                        ?.setDuration(simpleExoPlayer?.duration ?: 8000L)
                    onVideoPrepared = true
                    resumeCurrentStory()
                }
            }
        })
    }

    private fun setUpUi() {
        val touchListener = object : OnSwipeTouchListener(activity!!) {
            override fun onSwipeTop() {
                Toast.makeText(activity, "onSwipeTop", Toast.LENGTH_LONG).show()
            }

            override fun onSwipeBottom() {
                Toast.makeText(activity, "onSwipeBottom", Toast.LENGTH_LONG).show()
            }

            override fun onClick(view: View) {
                when (view) {
                    next -> {
                        if (counter == stories.size - 1) {
                            pageViewOperator?.nextPageView()
                        } else {
                            storiesProgressView?.skip()
                        }
                    }
                    previous -> {
                        if (counter == 0) {
                            pageViewOperator?.backPageView()
                        } else {
                            storiesProgressView?.reverse()
                        }
                    }
                }
            }

            override fun onLongClick() {
                hideStoryOverlay()
            }

            override fun onTouchView(view: View, event: MotionEvent): Boolean {
                super.onTouchView(view, event)
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        pressTime = System.currentTimeMillis()
                        pauseCurrentStory()
                        return false
                    }
                    MotionEvent.ACTION_UP -> {
                        showStoryOverlay()
                        resumeCurrentStory()
                        return limit < System.currentTimeMillis() - pressTime
                    }
                }
                return false
            }
        }
        previous.setOnTouchListener(touchListener)
        next.setOnTouchListener(touchListener)

        storiesProgressView?.setStoriesCountDebug(
            stories.size, position = arguments?.getInt(EXTRA_POSITION) ?: -1
        )
        storiesProgressView?.setAllStoryDuration(4000L)
        storiesProgressView?.setStoriesListener(this)

        Glide.with(this).load(storyUser.profilePicUrl).circleCrop().into(storyDisplayProfilePicture)
        storyDisplayNick.text = storyUser.username
    }

    private fun showStoryOverlay() {
        if (storyOverlay == null || storyOverlay.alpha != 0F) return

        storyOverlay.animate()
            .setDuration(100)
            .alpha(1F)
            .start()
    }

    private fun hideStoryOverlay() {
        if (storyOverlay == null || storyOverlay.alpha != 1F) return

        storyOverlay.animate()
            .setDuration(200)
            .alpha(0F)
            .start()
    }

    private fun savePosition(pos: Int) {
        StoryViewActivity.progressState.put(position, pos)
    }

    private fun restorePosition(): Int {
        return StoryViewActivity.progressState.get(position)
    }

    fun pauseCurrentStory() {
        simpleExoPlayer?.playWhenReady = false
        storiesProgressView?.pause()
    }

    fun resumeCurrentStory() {
        if (onResumeCalled) {
            simpleExoPlayer?.playWhenReady = true
            showStoryOverlay()
            storiesProgressView?.resume()
        }
    }

    companion object {
        private const val EXTRA_POSITION = "EXTRA_POSITION"
        private const val EXTRA_STORY_USER = "EXTRA_STORY_USER"
        fun newInstance(position: Int, story: StoryUser): StoryDisplayFragment {
            return StoryDisplayFragment().apply {
                arguments = Bundle().apply {
                    putInt(EXTRA_POSITION, position)
                    putParcelable(EXTRA_STORY_USER, story)
                }
            }
        }
    }
}