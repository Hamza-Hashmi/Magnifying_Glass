package com.example.magnifyingglass.magnifier.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.example.magnifyingglass.magnifier.ui.fragments.FragmentAdFull
import com.example.magnifyingglass.magnifier.ui.fragments.Fragment_Four
import com.example.magnifyingglass.magnifier.ui.fragments.Fragment_One
import com.example.magnifyingglass.magnifier.ui.fragments.Fragment_Three
import com.example.magnifyingglass.magnifier.ui.fragments.Fragment_Two

class ViewPagerAdapter(fm: FragmentManager, canShow: Boolean) : FragmentPagerAdapter(fm) {
    private val fragmentList = if (canShow) {
        listOf(
            Fragment_One(),
            Fragment_Two(),
            FragmentAdFull(),
            Fragment_Three(),
            Fragment_Four()
        )
    } else {
        listOf(
            Fragment_One(),
            Fragment_Two(),
            Fragment_Three(),
            Fragment_Four()
        )
    }

    override fun getItem(position: Int): Fragment {
        return fragmentList[position]
    }

    override fun getCount(): Int {
        return fragmentList.size
    }
}
