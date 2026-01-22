package fr.innog.memoryflow.ui.utils

import android.R
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

class AndroidUtils {

    companion object {
        fun displayFragment(containerId: Int, fragment: Fragment, fragmentManager: FragmentManager, addBackToStack : Boolean = true) {
            val transaction = fragmentManager.beginTransaction()
            transaction.add(containerId, fragment)
            if(addBackToStack) transaction.addToBackStack(null)
            transaction.commit()
        }
    }

}

