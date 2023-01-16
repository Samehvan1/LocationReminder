package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.FakeDataSource
import com.udacity.project4.locationreminders.data.local.RemindersDatabase
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import org.koin.test.inject
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : KoinTest {

    private val data: ReminderDataSource by inject()
    private lateinit var context: Application

    @Before
    fun initBefore() {
        stopKoin()
        context = getApplicationContext()
        val modul = module {
            viewModel {
                RemindersListViewModel(context, get() as ReminderDataSource)
            }
            single { FakeDataSource() as ReminderDataSource }
        }
        startKoin {
            modules(listOf(modul))
        }
    }

    @After
    fun collectGarbage()= runBlockingTest {
        data.deleteAllReminders()
        stopKoin()
    }
    @Test
    fun getRemindersAndFillUi()= runBlockingTest {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        var rem2 =
            ReminderDTO("My Test Title2", "my test descreption2", "test loc2", 19.0, 19.0, "2")
        var rem3 =
            ReminderDTO("My Test Title3", "my test descreption3", "test loc3", 29.0, 29.0, "3")
        data.saveReminder(rem1)
        data.saveReminder(rem2)
        data.saveReminder(rem3)
        val scenario= launchFragmentInContainer< ReminderListFragment >(Bundle(),R.style.AppTheme)
        val navController=mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!,navController) }
        onView(withText(rem1.title)).check(matches(isDisplayed()))
        onView(withText(rem2.description)).check(matches(isDisplayed()))
        onView(withText(rem3.title)).check(matches(isDisplayed()))
    }
    @Test
    fun getRemindersEmpty()= runBlockingTest {
        var rem1 = ReminderDTO("My Test Title", "my test descreption", "test loc", 9.0, 9.0, "1")
        val scenario= launchFragmentInContainer< ReminderListFragment >(Bundle(),R.style.AppTheme)
        val navController=mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!,navController) }
        onView(withText(rem1.title)).check(doesNotExist())
        onView(withText(R.string.no_data)).check(matches(isDisplayed()))
    }
    @Test
    fun navigateToReminderFragment()= runBlockingTest {
        val scenario= launchFragmentInContainer< ReminderListFragment >(Bundle(),R.style.AppTheme)
        val navController=mock(NavController::class.java)
        scenario.onFragment { Navigation.setViewNavController(it.view!!,navController) }
        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
}