import android.os.Handler
import android.util.Log
import androidx.viewpager.widget.ViewPager.*

abstract class PageChangeListener : OnPageChangeListener {

    // Save the page index before user starts dragging the ViewPager
    private var pageBeforeDragging = 0

    // Save the current page index
    private var currentPage = 0

    // Save the last time user stopped dragging the ViewPager
    private var lastTime = DEBOUNCE_TIMES + 1L

    // Callback method when the scroll state of the ViewPager changes
    override fun onPageScrollStateChanged(state: Int) {
        // Depending on the state, perform different actions
        when (state) {
            // If the ViewPager is not scrolling
            SCROLL_STATE_IDLE -> {
                Log.d("onPageScrollState"," SCROLL_STATE_IDLE")

                // Check if the current time minus the last time user stopped dragging the ViewPager
                // is less than the debounce time. If it is, return
                val now = System.currentTimeMillis()
                if (now - lastTime < DEBOUNCE_TIMES) {
                    return
                }

                // Update the last time user stopped dragging the ViewPager
                lastTime = now

                // Use a Handler to post a delayed runnable to check if the pageBeforeDragging
                // equals to currentPage. If so, call onPageScrollCanceled()
                Handler().postDelayed({
                    if (pageBeforeDragging == currentPage) {
                        onPageScrollCanceled()
                    }
                }, 300L)
            }
            // If the ViewPager is being dragged
            SCROLL_STATE_DRAGGING -> {
                Log.d("onPageScrollState"," SCROLL_STATE_DRAGGING")
                // Update the pageBeforeDragging to the currentPage
                pageBeforeDragging = currentPage
            }
            // If the ViewPager is settling to a final position
            SCROLL_STATE_SETTLING -> {
                Log.d("onPageScrollState"," SCROLL_STATE_SETTLING")
            }
        }
    }

    // Callback method when the ViewPager is scrolled
    override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
    }

    // Callback method when a new page is selected
    override fun onPageSelected(position: Int) {
        Log.d("onPageScrollState","onPageSelected(): position($position)")
        // Update the currentPage to the selected position
        currentPage = position
    }

    // Abstract method to be overridden by concrete implementation
    abstract fun onPageScrollCanceled()

    // Constant for debouncing
    companion object {
        private const val DEBOUNCE_TIMES = 500L
    }
}