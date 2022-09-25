package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.content.Context
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.udacity.project4.MyApp
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.AutoCloseKoinTest
import org.robolectric.annotation.Config


@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest : AutoCloseKoinTest() {


    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var appContext: Application


    @Before
    fun setUp() {
        fakeDataSource = FakeDataSource()
        appContext = getApplicationContext()
        viewModel =
            SaveReminderViewModel(appContext, fakeDataSource)
    }

    // provide testing to the SaveReminderView and its live data objects

    @Test()
    fun validateEnteredData_nullTitle() {
        // GIVEN - a new reminder with null title
        val reminder = ReminderDataItem(
            null,
            "desc",
            "location",
            null,
            null
        )
        // WHEN - validating the input
        val result = viewModel.validateEnteredData(reminder)

        // THEN - validation will fail.
        assertThat(result, `is`(false))
    }

    @Test
    fun validateEnteredData_nullLocation() {
        // GIVEN - a new reminder with null location
        val reminder = ReminderDataItem(
            null,
            "desc",
            "location",
            null,
            null
        )
        // WHEN - validating the input
        val result = viewModel.validateEnteredData(reminder)

        // THEN - validation will fail.
        assertThat(result, `is`(false))
    }

    @Test
    fun validateEnteredData_nullData() {
        // GIVEN - a new reminder with null entries
        val reminder = ReminderDataItem(
            null,
            null,
            null,
            null,
            null
        )
        // WHEN - validating the input
        val result = viewModel.validateEnteredData(reminder)

        // THEN - validation will fail.
        assertThat(result, `is`(false))
    }

    @Test
    fun validateEnteredData_validInput() {
        // GIVEN - a new valid reminder
        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            null,
            null
        )
        // WHEN - validating the input
        val result = viewModel.validateEnteredData(reminder)

        // THEN - validation will success.
        assertThat(result, `is`(true))
    }

    @Test
    fun onClear_ResetAll() {
        // GIVEN - all live data have values
        viewModel.reminderTitle.value = "title"
        viewModel.reminderDescription.value = "description"
        viewModel.reminderSelectedLocationStr.value = "location"

        // WHEN - onClear
        viewModel.onClear()

        // Then - all live data become null
        assertThat(viewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))

    }

    @Test
    fun saveReminder_checkLoading() {
        // GIVEN - new reminder
        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            null,
            null
        )

        // WHEN - saving the reminder, THEN - a loading will happen
        mainCoroutineRule.pauseDispatcher()
        viewModel.saveReminder(reminder)
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun saveReminder_checkToastMsg() {
        // GIVEN - new reminder
        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            null,
            null
        )

        // WHEN - saving the reminder success, THEN - a toast msg appears
        viewModel.saveReminder(reminder)
        assertThat(viewModel.showToast.getOrAwaitValue(),
            `is`(appContext.getString(R.string.reminder_saved)))
    }

    @Test
    fun saveReminder_checkNavigation() {
        // GIVEN - new reminder
        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            null,
            null
        )

        // WHEN - saving the reminder success, THEN - navigate back
        viewModel.saveReminder(reminder)
        assertEquals(NavigationCommand.Back, viewModel.navigationCommand.getOrAwaitValue())
    }

    @Test
    fun saveReminder_createReminder() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            null,
            null
        )
        viewModel.saveReminder(reminder)

        val result: Result<ReminderDTO> = fakeDataSource.getReminder(reminder.id)
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))
        assertThat(result.data.longitude, `is`(reminder.longitude))
        assertThat(result.data.latitude, `is`(reminder.latitude))

    }

    @Test
    fun setLocation() {
        val lat = 51.507351
        val lng = -0.127758
        val latLng = LatLng(lat, lng)
        val location = "London"
        viewModel.setLatLng(latLng, location)
        assertThat(viewModel.latitude.getOrAwaitValue(), `is`(lat))
        assertThat(viewModel.longitude.getOrAwaitValue(), `is`(lng))
        assertThat(viewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(location))
    }

    @Test
    fun validateAndSave_nullData_notSaving() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDataItem(
            null,
            null,
            null,
            null,
            null
        )

        viewModel.validateAndSaveReminder(reminder)

        val result = fakeDataSource.getReminder(reminder.id)
        assertThat(result.succeeded, `is`(false))
    }

    @Test
    fun validateAndSave_completeData_save() = mainCoroutineRule.runBlockingTest {
        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            null,
            null
        )

        viewModel.validateAndSaveReminder(reminder)

        val result = fakeDataSource.getReminder(reminder.id)
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.title, `is`(reminder.title))
    }

}