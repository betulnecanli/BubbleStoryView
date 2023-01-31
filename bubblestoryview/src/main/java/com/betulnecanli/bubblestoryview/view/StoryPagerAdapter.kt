
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
//This is a custom PagerAdapter class in Kotlin named StoryPagerAdapter
// which is used to create a collection of pages to be shown in a ViewPager.
// It is used to display a collection of Fragments.
//
//The class has a constructor that takes two arguments: fragmentManager and storyList.
// The fragmentManager argument is passed to the superclass constructor,
// which is the FragmentStatePagerAdapter, to manage the lifecycle of the fragments in the adapter.
// The storyList argument is a private property of the class, representing a list of StoryUser objects.
//
//The getItem method returns a Fragment object by creating a new instance of the StoryDisplayFragment
// class and passing the current position and the StoryUser object at that position to it.
// The getCount method returns the size of the storyList property.
//
//The findFragmentByPosition method is used to find a fragment by its position in the ViewPager.
// It creates a fragment by calling the instantiateItem method and returns the fragment cast as a Fragment type.
// The finishUpdate method is called to finish updating the ViewPager.
class StoryPagerAdapter constructor(fragmentManager: FragmentManager, private val storyList: ArrayList<StoryUser>)
    : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = StoryDisplayFragment.newInstance(position, storyList[position])

    override fun getCount(): Int {
        return storyList.size
    }

    fun findFragmentByPosition(viewPager: ViewPager, position: Int): Fragment? {
        try {
            val f = instantiateItem(viewPager, position)
            return f as? Fragment
        } finally {
            finishUpdate(viewPager)
        }
    }
}