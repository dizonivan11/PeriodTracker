package com.streamside.periodtracker.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.streamside.periodtracker.MainActivity.Companion.getCheckUpResultViewModel
import com.streamside.periodtracker.MainActivity.Companion.getDataViewModel
import com.streamside.periodtracker.R
import com.streamside.periodtracker.data.DataViewModel
import com.streamside.periodtracker.data.checkup.CheckUp
import com.streamside.periodtracker.data.checkup.CheckUpList
import com.streamside.periodtracker.data.checkup.CheckUpResult
import com.streamside.periodtracker.data.checkup.CheckUpResultViewModel
import java.util.Date

class CheckUpFragment : Fragment() {
    private lateinit var fa: FragmentActivity
    private lateinit var today: Date
    private lateinit var dataViewModel: DataViewModel
    private lateinit var checkUpResultViewModel: CheckUpResultViewModel
    private lateinit var checkUpMap: CheckUpList
    private val messages: MutableList<ChatMessage> = mutableListOf()
    private var mode: ChatMode = ChatMode.Text
    private var currentQuestionIndex: Int = 0
    private lateinit var currentQuestion: CheckUp
    private lateinit var rvChatBox: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var editChat: EditText
    private lateinit var spinChat: Spinner
    private lateinit var btnChat: Button

    private var isInitialized = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_check_up, container, false)
        fa = requireActivity()
        today = Date()
        dataViewModel = getDataViewModel(fa)
        checkUpResultViewModel = getCheckUpResultViewModel(fa)
        rvChatBox = root.findViewById(R.id.rvChatBox)
        editChat = root.findViewById(R.id.editChat)
        spinChat = root.findViewById(R.id.spinChat)
        btnChat = root.findViewById(R.id.btnChat)

        chatAdapter = ChatAdapter(messages)
        rvChatBox.layoutManager = LinearLayoutManager(fa, LinearLayoutManager.VERTICAL, false).apply { stackFromEnd = true }
        rvChatBox.adapter = chatAdapter

        val o = checkUpResultViewModel.get(today)
        o.observe(viewLifecycleOwner) { todayCheckUp ->
            if (!o.hasActiveObservers() || !isInitialized) {
                if (todayCheckUp != null) {
                    messages.add(ChatMessage(ChatRole.System, "Daily check up was already been recorded for today"))
                    messages.add(ChatMessage(ChatRole.System, "Would you like to retry the check up?"))
                    refreshChat(chatAdapter)
                    mode = ChatMode.DropDown
                    changeMode(mode)
                    changeChoices(mutableListOf("No", "Yes"))
                    btnChat.setOnClickListener {
                        if (spinChat.selectedItem.toString() == "Yes") {
                            checkUpResultViewModel.delete(todayCheckUp)
                            initialize()
                        } else {
                            editChat.isEnabled = false
                            spinChat.isEnabled = false
                            btnChat.isEnabled = false
                            messages.add(ChatMessage(ChatRole.System, "We've recommended some tips for you at the Home page's Recommended Tips section"))
                            refreshChat(chatAdapter)
                        }
                    }
                } else initialize()
                isInitialized = true
                o.removeObservers(viewLifecycleOwner)
            }
        }

        return root
    }

    private fun initialize() {
        messages.add(ChatMessage(ChatRole.System, "Good day, we will be asking general wellness question, please answer using the dropdown/textbox at the bottom"))
        refreshChat(chatAdapter)
        val o = dataViewModel.getCheckUpData()
        o.observe(viewLifecycleOwner) { newCheckUp ->
            o.removeObservers(viewLifecycleOwner)

            // Remap first to simplify iteration
            checkUpMap = remapToSingleArray(newCheckUp)

            if (newCheckUp.list.isNotEmpty()) {
                setToCurrent()
                initializeCheckUp(currentQuestion, chatAdapter)

                btnChat.setOnClickListener {
                    btnChat.isEnabled = false
                    var proceed = true
                    if (mode == ChatMode.Text) {
                        if (editChat.text.trim().isNotEmpty()) {
                            currentQuestion.answer = editChat.text.toString()
                            messages.add(ChatMessage(ChatRole.User, editChat.text.toString()))
                            refreshChat(chatAdapter)
                            editChat.text.clear()
                        } else {
                            messages.add(ChatMessage(ChatRole.System, "Invalid input, please type a valid answer"))
                            refreshChat(chatAdapter)
                            editChat.text.clear()
                            proceed = false
                            btnChat.isEnabled = true
                        }
                    } else if (mode == ChatMode.DropDown) {
                        if (spinChat.selectedItem.toString().isNotEmpty()) {
                            currentQuestion.answer = spinChat.selectedItem.toString()
                            messages.add(ChatMessage(ChatRole.User, spinChat.selectedItem.toString()))
                            refreshChat(chatAdapter)
                        }
                    }

                    if (proceed) {
                        if (++currentQuestionIndex < checkUpMap.list.size) {
                            setToCurrent()
                            if (currentQuestion.parentIndex > -1 && checkUpMap.list[currentQuestion.parentIndex].answer.isNotEmpty()) {
                                var conditionCheck = false
                                while (!conditionCheck) {
                                    val conditions = currentQuestion.parentCondition.split(",")
                                    if (currentQuestion.parentIndex == -1 || conditions.contains(checkUpMap.list[currentQuestion.parentIndex].answer)) {
                                        conditionCheck = true
                                        initializeCheckUp(currentQuestion, chatAdapter)
                                    } else {
                                        currentQuestionIndex++
                                        setToCurrent()
                                    }
                                }
                            } else {
                                initializeCheckUp(currentQuestion, chatAdapter)
                            }
                        } else {
                            // Save Check-up with answers
                            checkUpResultViewModel.add(CheckUpResult(0, today, newCheckUp)).observe(viewLifecycleOwner) {
                                messages.add(ChatMessage(ChatRole.System, "Your answers are recorded, we've recommended some tips for you at the Home page's Recommended Tips section"))
                                refreshChat(chatAdapter)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun remapToSingleArray(checkUpList: CheckUpList): CheckUpList {
        val list = CheckUpList()

        for (i in checkUpList.list) {
            list.list.add(i)
            addTo(i, list)
        }

        return list
    }

    private fun addTo(checkUp: CheckUp, remapList: CheckUpList) {
        for (c in checkUp.children.list) {
            remapList.list.add(c)
            addTo(c, remapList)
        }
    }

    private fun initializeCheckUp(checkUp: CheckUp, chatAdapter: ChatAdapter) {
        mode = getMode(checkUp)
        changeMode(mode)
        if (mode == ChatMode.DropDown) changeChoices(checkUp.choices.list)
        messages.add(ChatMessage(ChatRole.Assistant, checkUp.question))
        refreshChat(chatAdapter)
        btnChat.isEnabled = true
    }

    private fun setToCurrent() { currentQuestion = checkUpMap.list[currentQuestionIndex] }

    private fun refreshChat(chatAdapter: ChatAdapter) {
        val i = chatAdapter.itemCount - 1
        chatAdapter.notifyItemChanged(i)
        rvChatBox.scrollToPosition(i)
    }

    private fun getMode(checkUp: CheckUp): ChatMode {
        return if (checkUp.choices.list.isEmpty())
            ChatMode.Text
        else
            ChatMode.DropDown
    }

    private fun changeMode(mode: ChatMode) {
        if (mode == ChatMode.Text) {
            spinChat.visibility = View.GONE
            editChat.visibility = View.VISIBLE
        } else if (mode == ChatMode.DropDown) {
            editChat.visibility = View.GONE
            spinChat.visibility = View.VISIBLE
        }
    }

    private fun changeChoices(choices: List<String>) {
        spinChat.adapter = ArrayAdapter(fa, androidx.appcompat.R.layout.select_dialog_item_material, choices)
    }
}