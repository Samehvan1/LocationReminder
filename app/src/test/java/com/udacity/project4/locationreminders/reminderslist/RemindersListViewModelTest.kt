package com.udacity.project4.locationreminders.reminderslist

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import javax.sql.DataSource

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
class RemindersListViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var reminderVModle: RemindersListViewModel

    @get:Rule
    var taskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    @Before
    fun initBefore() = mainCoroutineRule.runBlockingTest{
        stopKoin()
        fakeDataSource = FakeDataSource()
        reminderVModle =
            RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeDataSource)
    }

    @After
    fun collectGarbage() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.deleteAllReminders()
        stopKoin()
    }

    @Test
    fun getRemindersShowLoading()= mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        reminderVModle.loadReminders()
        assertThat(reminderVModle.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(reminderVModle.showLoading.getOrAwaitValue(), `is`(false))
    }
    @Test
    fun getRemindersListNotEmpty()=mainCoroutineRule.runBlockingTest {
        var rem1=ReminderDTO("My Test Title","my test descreption","test loc",9.0,9.0,"1")
        fakeDataSource.saveReminder(rem1)
        reminderVModle.loadReminders()
        assertThat(reminderVModle.remindersList.getOrAwaitValue().isNotEmpty(),`is`(true))
    }
    @Test
    fun clearAndDeleteReminders()=mainCoroutineRule.runBlockingTest {
        var rem1=ReminderDTO("My Test Title","my test descreption","test loc",9.0,9.0,"1")
        fakeDataSource.saveReminder(rem1)
        fakeDataSource.deleteAllReminders()
        assertThat(fakeDataSource.reminders.size<1,`is`(true))
    }
    @Test
    fun getRemindersShowError()=mainCoroutineRule.runBlockingTest {
        fakeDataSource.setReturnError(true)
        reminderVModle.loadReminders()
        assertThat(reminderVModle.showSnackBar.getOrAwaitValue(),`is`("Error Loading Reminders"))
    }
}