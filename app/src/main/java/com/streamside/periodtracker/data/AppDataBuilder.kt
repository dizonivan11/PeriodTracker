package com.streamside.periodtracker.data

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.streamside.periodtracker.R
import com.streamside.periodtracker.ui.library.DictionaryFragment
import com.streamside.periodtracker.ui.library.LIBRARY_CALLBACK
import com.streamside.periodtracker.ui.library.WebFragment

class AppDataBuilder {
    companion object {
        @Volatile
        private var SYMPTOMS_INSTANCE: MutableList<Category>? = null
        @Volatile
        private var LIBRARY_INSTANCE: MutableList<Library>? = null

        fun getSymptomsData(): MutableList<Category> {
            val temp = SYMPTOMS_INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = mutableListOf(
                    Category(R.string.cat_vaginal_discharge_color, listOf(
                        Symptom(R.string.vdc_white),
                        Symptom(R.string.vdc_clear),
                        Symptom(R.string.vdc_yellow_green),
                        Symptom(R.string.vdc_brown),
                        Symptom(R.string.vdc_red),
                        Symptom(R.string.vdc_pink),
                        Symptom(R.string.vdc_gray),
                    )),
                    Category(R.string.cat_vaginal_discharge, listOf(
                        Symptom(R.string.vd_no_discharge),
                        Symptom(R.string.vd_creamy),
                        Symptom(R.string.vd_watery),
                        Symptom(R.string.vd_sticky),
                        Symptom(R.string.vd_egg_white),
                        Symptom(R.string.vd_spotting),
                        Symptom(R.string.vd_unusual),
                    )),
                    Category(R.string.cat_mood, listOf(
                        Symptom(R.string.mood_calm),
                        Symptom(R.string.mood_energetic),
                        Symptom(R.string.mood_swings),
                        Symptom(R.string.mood_irritated),
                        Symptom(R.string.mood_happy),
                        Symptom(R.string.mood_sad),
                        Symptom(R.string.mood_anxious),
                        Symptom(R.string.mood_depressed),
                        Symptom(R.string.mood_stressed),
                        Symptom(R.string.mood_low_energy),
                    )),
                    Category(R.string.cat_sex, listOf(
                        Symptom(R.string.sex_protected_sex),
                        Symptom(R.string.sex_unprotected_sex),
                        Symptom(R.string.sex_masturbation),
                        Symptom(R.string.sex_high_sex_drive),
                        Symptom(R.string.sex_low_sex_drive),
                        Symptom(R.string.sex_painful_sex),
                        Symptom(R.string.sex_difficulty_with_orgasm),
                    )),
                    Category(R.string.cat_symptoms, listOf(
                        Symptom(R.string.sym_menstrual_cramps),
                        Symptom(R.string.sym_tender_breasts),
                        Symptom(R.string.sym_backache),
                        Symptom(R.string.sym_abdominal_pain),
                        Symptom(R.string.sym_vaginal_itching),
                        Symptom(R.string.sym_vaginal_dryness),
                    )),
                    Category(R.string.cat_digestion_stool, listOf(
                        Symptom(R.string.ds_nausea),
                        Symptom(R.string.ds_bloating),
                        Symptom(R.string.ds_constipation),
                        Symptom(R.string.ds_diarrhea),
                    )),
                    Category(R.string.cat_fitness_goal, listOf(
                        Symptom(R.string.fg_lose_weight),
                        Symptom(R.string.fg_gain_weight),
                        Symptom(R.string.fg_maintain_healthy_weight),
                        Symptom(R.string.fg_get_more_energy),
                    )),
                    Category(R.string.cat_sleep, listOf(
                        Symptom(R.string.sl_insomnia),
                        Symptom(R.string.sl_waking_up_tired),
                        Symptom(R.string.sl_waking_up_during_night),
                        Symptom(R.string.sl_lack_of_sleep_schedule),
                    )),
                    Category(R.string.cat_skin, listOf(
                        Symptom(R.string.sk_dryness),
                        Symptom(R.string.sk_acne_and_blemishes),
                        Symptom(R.string.sk_dark_spots_pores),
                        Symptom(R.string.sk_fine_lines_wrinkles),
                        Symptom(R.string.sk_dullness_texture),
                    )),
                )
                SYMPTOMS_INSTANCE = instance
                return instance
            }
        }

        fun getLibraryData(fa: FragmentActivity): MutableList<Library> {
            val temp = LIBRARY_INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val navView: BottomNavigationView = fa.findViewById(R.id.nav_view)
                val fm = fa.supportFragmentManager
                val symptoms = getSymptomsData()
                val instance = mutableListOf(
                    Library("Search for word or term on dictionary", R.drawable.pexels_pixabay_159581) {
                        generateLibraryCallback(navView) {
                            replaceFragment(fm, DictionaryFragment())
                        }
                    },
                    Library("Meaning behind your discharge color", R.drawable.pexels_sora_shimazaki_5938447, listOf(
                        R.string.cat_vaginal_discharge_color
                    )) {
                        generateLibraryCallback(navView) {
                            openWeb(fm, "https://www.sutterhealth.org/health/teens/female/vaginal-discharge")
                        }
                    },
                    Library("Fixing your sleep problems", R.drawable.pexels_karolina_grabowska_6660783, listOf(
                        R.string.cat_sleep
                    )) {
                        generateLibraryCallback(navView) {
                            openWeb(fm, "https://sleepopolis.com/education/the-ultimate-guide-to-the-menstrual-cycle-and-sleep/#:~:text=You%20may%20also%20need%20more,sleep%2C%20and%20disrupted%20circadian%20rhythms.")
                        }
                    },
                    Library("Handling discomforts during your period", R.drawable.pexels_cottonbro_studio_6542718, listOf(
                        R.string.ic_discomforts
                    )) {
                        generateLibraryCallback(navView) {
                            openWeb(fm, "https://medlineplus.gov/periodpain.html#:~:text=Many%20women%20have%20painful%20periods,as%20premenstrual%20syndrome%20(PMS).")
                        }
                    },
                    Library("How exercise may change your period", R.drawable.pexels_tirachard_kumtanom_601177, listOf(
                        R.string.cat_fitness_goal
                    )) {
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
                LIBRARY_INSTANCE = instance
                return instance
            }
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