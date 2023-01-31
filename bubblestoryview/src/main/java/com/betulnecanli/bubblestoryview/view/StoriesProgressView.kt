import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import com.betulnecanli.bubblestoryview.R
import java.util.*

class StoriesProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : LinearLayout(context, attrs, defStyle) {

    // A mutable list of `PausableProgressBar`s
    private val progressBars: MutableList<PausableProgressBar> = ArrayList()

    // A listener for stories events.
    private var storiesListener: StoriesListener? = null

    // The number of stories to display in the view.
    private var storiesCount = -1

    // The current story being displayed.
    private var current = -1

    // Flag to keep track of if the user has skipped the story.
    private var isSkipStart = false

    // Flag to keep track of if the user has reversed the story.
    private var isReverseStart = false

    // Debugging variable for the position of the story.
    private var position = -1

    // Flag to keep track of if all the stories have been displayed.
    private var isComplete = false


    // Initialize the view.
    init {
        // Set the orientation of the `LinearLayout` to be horizontal.
        orientation = HORIZONTAL

        // Get the attributes from the XML file.
        val typedArray = context.obtainStyledAttributes(attrs,
            R.styleable.StoriesProgressView
        )

        // Get the number of stories to display from the XML file.
        storiesCount = typedArray.getInt(R.styleable.StoriesProgressView_progressCount, 0)

        // Recycle the typed array.
        typedArray.recycle()

        // Bind the views.
        bindViews()
    }
    // Method to bind the views in the view.
    private fun bindViews() {
        // Clear the list of `PausableProgressBar`s.
        progressBars.clear()

        // Remove all views from the `LinearLayout`.
        removeAllViews()

        // For each story to display:
        for (i in 0 until storiesCount) {
            // Create a new `PausableProgressBar` and set its tag for debugging.
            val p = createProgressBar()
            p.tag = "p($position) c($i)" // debug

            // Add the `PausableProgressBar` to the list of progress bars.
            progressBars.add(p)

            // Add the `PausableProgressBar` to the `LinearLayout`.
            addView(p)
            if (i + 1 < storiesCount) {
                addView(createSpace())
            }
        }
    }

    private fun createProgressBar(): PausableProgressBar {
        return PausableProgressBar(context).apply {
            layoutParams =
                PROGRESS_BAR_LAYOUT_PARAM
        }
    }

    private fun createSpace(): View {
        return View(context).apply {
            layoutParams =
                SPACE_LAYOUT_PARAM
        }
    }

    private fun callback(index: Int): PausableProgressBar.Callback {
        return object : PausableProgressBar.Callback {
            override fun onStartProgress() {
                current = index
            }

            override fun onFinishProgress() {
                if (isReverseStart) {
                    if (storiesListener != null) storiesListener!!.onPrev()
                    if (0 <= current - 1) {
                        val p = progressBars[current - 1]
                        p.setMinWithoutCallback()
                        progressBars[--current].startProgress()
                    } else {
                        progressBars[current].startProgress()
                    }
                    isReverseStart = false
                    return
                }
                val next = current + 1
                if (next <= progressBars.size - 1) {
                    if (storiesListener != null) storiesListener!!.onNext()
                    progressBars[next].startProgress()
                    ++current
                } else {
                    isComplete = true
                    if (storiesListener != null) storiesListener!!.onComplete()
                }
                isSkipStart = false
            }
        }
    }

    fun setStoriesCountDebug(storiesCount: Int, position: Int) {
        this.storiesCount = storiesCount
        this.position = position
        bindViews()
    }

    fun setStoriesListener(storiesListener: StoriesListener?) {
        this.storiesListener = storiesListener
    }

    fun skip() {
        if (isSkipStart || isReverseStart) return
        if (isComplete) return
        if (current < 0) return
        val p = progressBars[current]
        isSkipStart = true
        p.setMax()
    }

    fun reverse() {
        if (isSkipStart || isReverseStart) return
        if (isComplete) return
        if (current < 0) return
        val p = progressBars[current]
        isReverseStart = true
        p.setMin()
    }

    fun setAllStoryDuration(duration: Long) {
        for (i in progressBars.indices) {
            progressBars[i].setDuration(duration)
            progressBars[i].setCallback(callback(i))
        }
    }

    fun startStories() {
        if (progressBars.size > 0) {
            progressBars[0].startProgress()
        }
    }

    fun startStories(from: Int) {
        for (i in progressBars.indices) {
            progressBars[i].clear()
        }
        for (i in 0 until from) {
            if (progressBars.size > i) {
                progressBars[i].setMaxWithoutCallback()
            }
        }
        if (progressBars.size > from) {
            progressBars[from].startProgress()
        }
    }

    fun destroy() {
        for (p in progressBars) {
            p.clear()
        }
    }

    fun abandon() {
        if (progressBars.size > current && current >= 0) {
            progressBars[current].setMinWithoutCallback()
        }
    }

    fun pause() {
        if (current < 0) return
        progressBars[current].pauseProgress()
    }

    fun resume() {
        if (current < 0 && progressBars.size > 0) {
            progressBars[0].startProgress()
            return
        }
        progressBars[current].resumeProgress()
    }

    fun getProgressWithIndex(index: Int): PausableProgressBar {
        return progressBars[index]
    }

    companion object {
        private val PROGRESS_BAR_LAYOUT_PARAM = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1F)
        private val SPACE_LAYOUT_PARAM = LayoutParams(5, LayoutParams.WRAP_CONTENT)
    }

    interface StoriesListener {
        fun onNext()
        fun onPrev()
        fun onComplete()
    }
}