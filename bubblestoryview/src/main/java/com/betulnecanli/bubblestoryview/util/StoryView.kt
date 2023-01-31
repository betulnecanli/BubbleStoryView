import android.content.Context
import android.content.Intent

class StoryView private constructor(
    // Context for starting activity
    private val context: Context,
    // List of users with their stories
    private val storyUserList: ArrayList<StoryUser>,
    // Current page of the story
    private val currentPage: Int
) {

    // Method to show stories using an intent
    fun showStories() {
        val payIntent = Intent(context, StoryViewActivity::class.java)
        // Pass the story user list and current page to the activity
        payIntent.putExtra("storyUserList", storyUserList)
        payIntent.putExtra("currentPage", currentPage)
        // Start the activity
        context.startActivity(payIntent)
    }
    // Inner class for building StoryView objects
    class Builder {
        private var context: Context? = null
        private var storyUserList: ArrayList<StoryUser>? = null
        private var currentPage: Int = 0
        // Specifies the context for building StoryView
        fun with(context: Context): Builder {
            this.context = context
            storyUserList = ArrayList()
            return this
        }

        // Sets the story user list for StoryView
        fun setStoryUserList(storyUserList: ArrayList<StoryUser>): Builder {
            // Ensure that the list is not empty
            check(storyUserList.isNotEmpty()) { "storyUserList should not be empty" }
            this.storyUserList = storyUserList
            return this
        }
        // Sets the current page of the story for StoryView
        fun setCurrentPage(currentPage: Int): Builder {
            this.currentPage = currentPage
            return this
        }

        // Builds and returns the StoryView object
        fun build(): StoryView {
            // Check that context and story user list are not null
            checkNotNull(context) { "Activity must be specified using with() call before build()" }
            checkNotNull(storyUserList) { "storyUserList must be specified using setStoryUserList() call before build()" }
            return StoryView(context!!, storyUserList!!, currentPage)
        }
    }
}