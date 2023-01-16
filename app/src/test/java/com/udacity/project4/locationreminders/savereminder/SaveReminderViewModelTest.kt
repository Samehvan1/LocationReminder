package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.R
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {
    private lateinit var data: FakeDataSource
    private lateinit var saveVM: SaveReminderViewModel

    @get:Rule
    var taskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initBefore() {
        stopKoin()
        data = FakeDataSource()
        saveVM = SaveReminderViewModel(ApplicationProvider.getApplicationContext(), data)
    }

    @After
    fun collectGarbage() = runBlockingTest {
        data.deleteAllReminders()
    }

    @Test
    fun saveReminders() = mainCoroutineRule.runBlockingTest {
        var rem1 =
            ReminderDataItem("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        var rem2 =
            ReminderDataItem("My Test Title2", "my test descreption2", "test loc2", 19.0, 19.0, "2")
        var rem3 =
            ReminderDataItem("My Test Title3", "my test descreption3", "test loc3", 29.0, 29.0, "3")
        saveVM.saveReminder(rem1)
        saveVM.saveReminder(rem2)
        saveVM.saveReminder(rem3)
        val res = data.getReminders()
        MatcherAssert.assertThat(res, CoreMatchers.notNullValue())
        MatcherAssert.assertThat(res is Result.Success, CoreMatchers.`is`(true))
        var lst = (res as Result.Success).data
        MatcherAssert.assertThat(lst.size == 3, CoreMatchers.`is`(true))
    }

    @Test
    fun clearRemindersLiveData() = mainCoroutineRule.runBlockingTest {
        var rem1 =
            ReminderDataItem("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        saveVM.latitude.value = rem1.latitude
        saveVM.longitude.value = rem1.latitude
        saveVM.reminderSelectedLocationStr.value = rem1.location
        saveVM.reminderTitle.value = rem1.title
        saveVM.reminderDescription.value = rem1.description
        saveVM.onClear()
        MatcherAssert.assertThat(saveVM.latitude.getOrAwaitValue(), CoreMatchers.nullValue())
        MatcherAssert.assertThat(saveVM.longitude.getOrAwaitValue(), CoreMatchers.nullValue())
        MatcherAssert.assertThat(
            saveVM.reminderSelectedLocationStr.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )
        MatcherAssert.assertThat(saveVM.reminderTitle.getOrAwaitValue(), CoreMatchers.nullValue())
        MatcherAssert.assertThat(
            saveVM.reminderDescription.getOrAwaitValue(),
            CoreMatchers.nullValue()
        )
    }
    @Test
    fun saveMissingTitle()=mainCoroutineRule.runBlockingTest {
        var rem1 =
            ReminderDataItem("", "my test descreption", "test loc", 9.0, 9.0, "1")
var check=saveVM.validateEnteredData(rem1)
        MatcherAssert.assertThat(check,`is` (false))
        MatcherAssert.assertThat(saveVM.showSnackBarInt.getOrAwaitValue(),`is` (R.string.err_enter_title))
    }
}