package com.streamside.periodtracker.data

import com.streamside.periodtracker.FA
import com.streamside.periodtracker.MainActivity.Companion.replaceFragment
import com.streamside.periodtracker.NAVVIEW
import com.streamside.periodtracker.R
import com.streamside.periodtracker.setup.SymptomsFragment
import com.streamside.periodtracker.ui.library.DictionaryFragment
import com.streamside.periodtracker.ui.library.LIBRARY_CALLBACK
import com.streamside.periodtracker.ui.library.WebFragment

class AppDataBuilder {
    companion object {
        @Volatile
        private var SYMPTOMS_INSTANCE: SymptomList? = null
        @Volatile
        private var LIBRARY_INSTANCE: List<Library>? = null

        fun newSymptomsData(): SymptomList {
            val temp = SYMPTOMS_INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = SymptomList(listOf(
                    Category(R.string.cat_vaginal_discharge_color, listOf(
                        Symptom(R.string.vdc_white, R.drawable.baseline_water_drop_24),
                        Symptom(R.string.vdc_clear, R.drawable.baseline_water_drop_24),
                        Symptom(R.string.vdc_yellow_green, R.drawable.baseline_water_drop_24),
                        Symptom(R.string.vdc_brown, R.drawable.baseline_water_drop_24),
                        Symptom(R.string.vdc_red, R.drawable.baseline_water_drop_24),
                        Symptom(R.string.vdc_pink, R.drawable.baseline_water_drop_24),
                        Symptom(R.string.vdc_gray, R.drawable.baseline_water_drop_24),
                    ), true),
                    Category(R.string.cat_vaginal_discharge, listOf(
                        Symptom(R.string.vd_no_discharge, R.drawable.baseline_block_24),
                        Symptom(R.string.vd_creamy, R.drawable.waves),
                        Symptom(R.string.vd_watery, R.drawable.baseline_water_24),
                        Symptom(R.string.vd_sticky, R.drawable.egg_alt),
                        Symptom(R.string.vd_egg_white, R.drawable.egg),
                        Symptom(R.string.vd_spotting, R.drawable.baseline_search_24),
                        Symptom(R.string.vd_unusual, R.drawable.baseline_question_mark_24),
                    ), true),
                    Category(R.string.cat_mood, listOf(
                        Symptom(R.string.mood_calm, R.drawable.sentiment_calm_24px),
                        Symptom(R.string.mood_energetic, R.drawable.battery_charging_full),
                        Symptom(R.string.mood_swings, R.drawable.sentiment_worried_24px),
                        Symptom(R.string.mood_irritated, R.drawable.sentiment_neutral),
                        Symptom(R.string.mood_happy, R.drawable.sentiment_excited),
                        Symptom(R.string.mood_sad, R.drawable.sentiment_dissatisfied_24px),
                        Symptom(R.string.mood_depressed, R.drawable.sentiment_frustrated_24px),
                        Symptom(R.string.mood_stressed, R.drawable.sentiment_stressed_4px),
                        Symptom(R.string.mood_low_energy, R.drawable.baseline_battery_1_bar_24),
                    )),
                    Category(R.string.cat_sex, listOf(
                        Symptom(R.string.sex_protected_sex, R.drawable.shield_with_heart),
                        Symptom(R.string.sex_unprotected_sex, R.drawable.shield),
                        Symptom(R.string.sex_masturbation, R.drawable.volunteer_activism),
                        Symptom(R.string.sex_high_sex_drive, R.drawable.ecg_heart),
                        Symptom(R.string.sex_low_sex_drive, R.drawable.favorite),
                        Symptom(R.string.sex_painful_sex, R.drawable.bolt),
                        Symptom(R.string.sex_difficulty_with_orgasm, R.drawable.sentiment_sad),
                    )),
                    Category(R.string.cat_symptoms, listOf(
                        Symptom(R.string.sym_menstrual_cramps, R.drawable.gynecology),
                        Symptom(R.string.sym_tender_breasts, R.drawable.breastfeeding),
                        Symptom(R.string.sym_backache, R.drawable.orthopedics),
                        Symptom(R.string.sym_abdominal_pain, R.drawable.gastroenterology),
                        Symptom(R.string.sym_vaginal_itching, R.drawable.hand_gesture),
                        Symptom(R.string.sym_vaginal_dryness, R.drawable.cool_to_dry),
                    )),
                    Category(R.string.cat_digestion_stool, listOf(
                        Symptom(R.string.ds_nausea, R.drawable.oncology),
                        Symptom(R.string.ds_bloating, R.drawable.zoom_out_map),
                        Symptom(R.string.ds_constipation, R.drawable.sentiment_very_dissatisfied),
                        Symptom(R.string.ds_diarrhea, R.drawable.gastroenterology),
                    )),
                    Category(R.string.cat_fitness_goal, listOf(
                        Symptom(R.string.fg_lose_weight, R.drawable.monitor_weight_loss),
                        Symptom(R.string.fg_gain_weight, R.drawable.monitor_weight_gain),
                        Symptom(R.string.fg_maintain_healthy_weight, R.drawable.monitor_weight),
                        Symptom(R.string.fg_get_more_energy, R.drawable.fitness_center),
                    )),
                    Category(R.string.cat_sleep, listOf(
                        Symptom(R.string.sl_insomnia, R.drawable.bedtime_off),
                        Symptom(R.string.sl_waking_up_tired, R.drawable.bed),
                        Symptom(R.string.sl_waking_up_during_night, R.drawable.bedtime),
                        Symptom(R.string.sl_lack_of_sleep_schedule, R.drawable.sleep_score),
                    )),
                    Category(R.string.cat_skin, listOf(
                        Symptom(R.string.sk_dryness, R.drawable.dry),
                        Symptom(R.string.sk_acne_and_blemishes, R.drawable.face_5),
                        Symptom(R.string.sk_dark_spots_pores, R.drawable.mystery),
                        Symptom(R.string.sk_fine_lines_wrinkles, R.drawable.ssid_chart),
                        Symptom(R.string.sk_dullness_texture, R.drawable.dermatology),
                    )),
                ))
                SYMPTOMS_INSTANCE = instance
                return instance
            }
        }

        fun getLibraryData(): List<Library> {
            val temp = LIBRARY_INSTANCE
            if (temp != null) return temp
            synchronized(this) {
                val instance = listOf(
                    Library("Search for word or term on dictionary", R.drawable.pexels_pixabay_159581) {
                        generateLibraryCallback {
                            replaceFragment(DictionaryFragment(), R.id.frameLibrary)
                        }
                    },
                    Library("Log your symptoms", R.drawable.rebecca_manning_q7dgla0rvuy_unsplash, false, listOf(
                        R.string.cat_vaginal_discharge_color,
                        R.string.cat_vaginal_discharge,
                        R.string.cat_mood,
                        R.string.cat_sex,
                        R.string.cat_symptoms,
                        R.string.cat_digestion_stool,
                        R.string.cat_fitness_goal,
                        R.string.cat_sleep,
                        R.string.cat_skin,
                    ), R.layout.insight_symptoms_item) {
                        generateLibraryCallback {
                            replaceFragment(SymptomsFragment(), R.id.frameLibrary)
                        }
                    },
                    Library("Meaning behind your discharge color", R.drawable.pexels_sora_shimazaki_5938447, true, listOf(
                        R.string.cat_vaginal_discharge_color
                    )) {
                        generateLibraryCallback {
                            openWeb("https://www.sutterhealth.org/health/teens/female/vaginal-discharge")
                        }
                    },
                    Library("Fixing your sleep problems", R.drawable.pexels_karolina_grabowska_6660783, true,  listOf(
                        R.string.cat_sleep
                    )) {
                        generateLibraryCallback {
                            openWeb("https://sleepopolis.com/education/the-ultimate-guide-to-the-menstrual-cycle-and-sleep/")
                        }
                    },
                    Library("Handling discomforts during your period", R.drawable.pexels_cottonbro_studio_6542718, true,  listOf(
                        R.string.cat_symptoms
                    )) {
                        generateLibraryCallback {
                            openWeb("https://medlineplus.gov/periodpain.html")
                        }
                    },
                    Library("How exercise may change your period", R.drawable.pexels_tirachard_kumtanom_601177, true,  listOf(
                        R.string.cat_fitness_goal
                    )) {
                        generateLibraryCallback {
                            openWeb("https://www.verywellhealth.com/exercise-effects-on-menstruation-4104136")
                        }
                    },
                    Library("What are irregular periods and its effect?", R.drawable.pexels_nadezhda_moryak_7467101) {
                        generateLibraryCallback {
                            openWeb("https://my.clevelandclinic.org/health/diseases/14633-abnormal-menstruation-periods")
                        }
                    },
                )
                LIBRARY_INSTANCE = instance
                return instance
            }
        }

        // Handles the bottom navigation bar item change then invokes the callback inside the Library item
        private fun generateLibraryCallback(callback: () -> Unit) {
            LIBRARY_CALLBACK = {
                callback.invoke()
                LIBRARY_CALLBACK = null
            }

            if (NAVVIEW.selectedItemId == R.id.navigation_library)
                LIBRARY_CALLBACK?.invoke()
            else
                NAVVIEW.selectedItemId = R.id.navigation_library
        }

        // Simplify WebFragment transaction
        private fun openWeb(url: String) {
            FA.supportFragmentManager.beginTransaction().replace(R.id.frameLibrary, WebFragment(url)).commit()
        }
    }
}