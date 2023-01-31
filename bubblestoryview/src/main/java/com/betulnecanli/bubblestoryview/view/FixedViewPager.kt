import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

class FixedViewPager @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    ViewPager(context, attrs) {
    // prevent NPE if fake dragging and touching ViewPager
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        return if (isFakeDragging) {
            false
        } else try {
            super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            false
        }
    }
}

/* // Overrides the onInterceptTouchEvent method from the parent class (ViewPager)
    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        // Check if the ViewPager is in fake dragging mode
        return if (isFakeDragging) {
            // If the ViewPager is in fake dragging mode, return false to indicate that
            // the touch event should not be intercepted
            false
        } else try {
            // If the ViewPager is not in fake dragging mode, call the onInterceptTouchEvent method
            // from the parent class (ViewPager)
            super.onInterceptTouchEvent(ev)
        } catch (e: IllegalArgumentException) {
            // If an IllegalArgumentException is thrown, return false to indicate that the touch event
            // should not be intercepted
            false
        }
    }
}*/