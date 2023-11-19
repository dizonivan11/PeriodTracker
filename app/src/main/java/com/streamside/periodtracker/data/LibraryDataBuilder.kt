package com.streamside.periodtracker.data

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streamside.periodtracker.R
import com.streamside.periodtracker.ui.library.DictionaryFragment
import com.streamside.periodtracker.ui.library.LIBRARY_CALLBACK
import com.streamside.periodtracker.ui.library.WebFragment

class LibraryDataBuilder {
    companion object {
        fun getLibraryData(fa: FragmentActivity): Array<Library> {
            val navView: BottomNavigationView = fa.findViewById(R.id.nav_view)
            val fm = fa.supportFragmentManager
            return arrayOf(
                Library("Search for word or term on dictionary", R.drawable.pexels_pixabay_159581) {
                    generateLibraryCallback(navView) {
                        replaceFragment(fm, DictionaryFragment())
                    }
                },
                Library("Meaning behind your discharge color", R.drawable.pexels_sora_shimazaki_5938447) {
                    generateLibraryCallback(navView) {
                        openWeb(fm, "https://www.sutterhealth.org/health/teens/female/vaginal-discharge")
                    }
                },
                Library("Fixing your sleep problems", R.drawable.pexels_karolina_grabowska_6660783) {
                    generateLibraryCallback(navView) {
                        openWeb(fm, "https://sleepopolis.com/education/the-ultimate-guide-to-the-menstrual-cycle-and-sleep/#:~:text=You%20may%20also%20need%20more,sleep%2C%20and%20disrupted%20circadian%20rhythms.")
                    }
                },
                Library("Handling discomforts during your period", R.drawable.pexels_cottonbro_studio_6542718) {
                    generateLibraryCallback(navView) {
                        openWeb(fm, "https://medlineplus.gov/periodpain.html#:~:text=Many%20women%20have%20painful%20periods,as%20premenstrual%20syndrome%20(PMS).")
                    }
                },
                Library("How exercise may change your period", R.drawable.pexels_tirachard_kumtanom_601177) {
                    generateLibraryCallback(navView) {
                        openWeb(fm, "https://www.verywellhealth.com/exercise-effects-on-menstruation-4104136#:~:text=Intense%20exercise%20can%20cause%20changes,sometimes%2C%20no%20period%20at%20all.")
                    }
                },
                Library("What are irregular periods and its effect?", R.drawable.pexels_nadezhda_moryak_7467101) {
                    generateLibraryCallback(navView) {
                        openWeb(fm, "https://my.clevelandclinic.org/health/diseases/14633-abnormal-menstruation-periods")
                    }
                },
            )
        }

        // Handles the bottom navigation bar item change then invokes the callback inside the Library item
        private fun generateLibraryCallback(navView: BottomNavigationView, callback: () -> Unit) {
            LIBRARY_CALLBACK = {
                callback.invoke()
                LIBRARY_CALLBACK = null
            }

            if (navView.selectedItemId == R.id.navigation_library)
                LIBRARY_CALLBACK?.invoke()
            else
                navView.selectedItemId = R.id.navigation_library
        }

        // Simplify Fragment transaction
        private fun replaceFragment(fm: FragmentManager, newFragment: Fragment) {
            fm.beginTransaction().replace(R.id.frameLibrary, newFragment).commit()
        }

        // Simplify WebFragment transaction
        private fun openWeb(fm: FragmentManager, url: String) {
            fm.beginTransaction().replace(R.id.frameLibrary, WebFragment(url)).commit()
        }
    }
}