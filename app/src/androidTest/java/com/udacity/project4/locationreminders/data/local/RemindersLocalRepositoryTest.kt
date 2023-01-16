package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {
    private lateinit var inMomeryDB: RemindersDatabase
    private lateinit var remindersRepo: RemindersLocalRepository

    @get:Rule
    var taskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initBefore() {

        inMomeryDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersRepo = RemindersLocalRepository(inMomeryDB.reminderDao(), Dispatchers.Main)
    }

    @After
    fun collectGarbage() {
        inMomeryDB.close()
    }

    @Test
    fun saveReminder() = runBlocking {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        remindersRepo.saveReminder(rem1)
        var recall = remindersRepo.getReminder(rem1.id) as Result.Success
        assertThat(recall, CoreMatchers.notNullValue())
        assertThat(recall is Result.Success, `is`(true))
        var data = recall.data
        assertThat(data.id == rem1.id, `is`(true))
        assertThat(data.title == rem1.title, `is`(true))
        assertThat(data.description == rem1.description, `is`(true))
        assertThat(data.latitude == rem1.latitude, `is`(true))
        assertThat(data.longitude == rem1.longitude, `is`(true))
        assertThat(data.location == rem1.location, `is`(true))
    }

    @Test
    fun saveThreeAndMatchResult() = runBlocking {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        var rem2 =
            ReminderDTO("My Test Title2", "my test descreption2", "test loc2", 19.0, 19.0, "2")
        var rem3 =
            ReminderDTO("My Test Title3", "my test descreption3", "test loc3", 29.0, 29.0, "3")
        remindersRepo.saveReminder(rem1)
        remindersRepo.saveReminder(rem2)
        remindersRepo.saveReminder(rem3)
        val res = remindersRepo.getReminders()
        assertThat(res, CoreMatchers.notNullValue())
        //assertThat(res is Result.Success, `is`(true))
        //var lst = (res as Result.Success).data
       // assertThat(lst.size == 3, `is`(true))
    }

    @Test
    fun saveThreeAndDeleteThem() = runBlocking {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        var rem2 =
            ReminderDTO("My Test Title2", "my test descreption2", "test loc2", 19.0, 19.0, "2")
        var rem3 =
            ReminderDTO("My Test Title3", "my test descreption3", "test loc3", 29.0, 29.0, "3")
        remindersRepo.saveReminder(rem1)
        remindersRepo.saveReminder(rem2)
        remindersRepo.saveReminder(rem3)
        var res = remindersRepo.getReminders()
        assertThat(res, CoreMatchers.notNullValue())
        assertThat(res is Result.Success, `is`(true))
        var lst = (res as Result.Success).data
        assertThat(lst.size == 3, `is`(true))
        remindersRepo.deleteAllReminders()
        res = remindersRepo.getReminders()
        assertThat(res is Result.Success, `is`(true))
        lst = (res as Result.Success).data
        assertThat(lst == null || lst.size < 1, `is`(true))
    }

    @Test
    fun getReminderByIdAndReturnError() = runBlocking {
        var res = remindersRepo.getReminder("2")
        assertThat(res is Result.Error, CoreMatchers.notNullValue())
        var error = res as Result.Error
        assertThat(error.message, `is`("Reminder not found!"))
    }

}