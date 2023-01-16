package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var inMomeryDB: RemindersDatabase
    private lateinit var remindersDao: RemindersDao

    @get:Rule
    var taskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initBefore() {
        inMomeryDB = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersDao = inMomeryDB.reminderDao()
    }

    @After
    fun collectGarbage() {
        inMomeryDB.close()
    }

    @Test
    fun saveReminder() = runBlockingTest {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        remindersDao.saveReminder(rem1)
        var recall = remindersDao.getReminderById(rem1.id)
        assertThat(recall, notNullValue())
        assertThat(recall?.id == rem1.id, `is`(true))
        assertThat(recall?.title == rem1.title, `is`(true))
        assertThat(recall?.description == rem1.description, `is`(true))
        assertThat(recall?.latitude == rem1.latitude, `is`(true))
        assertThat(recall?.longitude == rem1.longitude, `is`(true))
        assertThat(recall?.location == rem1.location, `is`(true))
    }

    @Test
    fun saveThreeAndMatchResult() = runBlockingTest {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        var rem2 =
            ReminderDTO("My Test Title2", "my test descreption2", "test loc2", 19.0, 19.0, "2")
        var rem3 =
            ReminderDTO("My Test Title3", "my test descreption3", "test loc3", 29.0, 29.0, "3")
        remindersDao.saveReminder(rem1)
        remindersDao.saveReminder(rem2)
        remindersDao.saveReminder(rem3)
        val lst = remindersDao.getReminders()
        assertThat(lst, notNullValue())
        assertThat(lst.size == 3, `is`(true))
    }

    @Test
    fun saveThreeAndDeleteThem() = runBlockingTest {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        var rem2 =
            ReminderDTO("My Test Title2", "my test descreption2", "test loc2", 19.0, 19.0, "2")
        var rem3 =
            ReminderDTO("My Test Title3", "my test descreption3", "test loc3", 29.0, 29.0, "3")
        remindersDao.saveReminder(rem1)
        remindersDao.saveReminder(rem2)
        remindersDao.saveReminder(rem3)
        var lst = remindersDao.getReminders()
        assertThat(lst, notNullValue())
        assertThat(lst.size == 3, `is`(true))
        remindersDao.deleteAllReminders()
        lst=remindersDao.getReminders()
        assertThat(lst==null||lst.size<1,`is` (true))
    }
}