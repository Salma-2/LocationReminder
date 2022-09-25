package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeAndroidTestRepository
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.succeeded
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.test.get
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var repository: ReminderDataSource
    private lateinit var viewModel: RemindersListViewModel
    private lateinit var appContext: Application

    @Before
    fun init() {
        stopKoin() //stop the original app koin
        appContext = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }
            single {
                FakeAndroidTestRepository() as ReminderDataSource
            }
        }
        startKoin {
            modules(listOf(myModule))
        }

        repository = get()
        viewModel = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }

    }

    //    test the displayed data on the UI.
    @Test
    fun onHomeScreen_RemindersListDisplayed() = mainCoroutineRule.runBlockingTest {

        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            36.877934,
            -94.878487
        ).toReminderDto()

        repository.saveReminder(reminder)

        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        onView(withId(R.id.reminderssRecyclerView))
            .perform(
                RecyclerViewActions
                    .scrollToPosition<RecyclerView.ViewHolder>(0)
            )
            .check(matches(isDisplayed()))
    }

    //    test the navigation of the fragments.
    @Test
    fun clickAddReminderButton_navigateToSaveReminderFragment() {
        // GIVEN - on the home screen
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.requireView(), navController)
        }

        // WHEN - click on "+" Fab
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN -- Navigate to SaveReminderFragment
        verify(navController)
            .navigate(ReminderListFragmentDirections.toSaveReminder())
    }


    //    add testing for the error messages.
    @Test
    fun onHomeScreen_whenNoData_ErrorMessage() {
        launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)

        repository.setReturnError(true)
        viewModel.loadReminders() // Error

//        THEN - a SnackBar with an error message displayed
        assertEquals("Test exception", viewModel.showSnackBar.value)

    }

}