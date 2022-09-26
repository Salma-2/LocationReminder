package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Error
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.data.dto.succeeded
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.toReminderDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.junit.*
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()


    private lateinit var database: RemindersDatabase
    private lateinit var appContext: Application
    private lateinit var localDataSource: ReminderDataSource

    @Before
    fun setup() {
        appContext = ApplicationProvider.getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            appContext,
            RemindersDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        localDataSource = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminder_retrieveReminder() = runBlocking {
        // GIVEN - A new reminder saved in the database.
        val reminder = ReminderDataItem(
            "title",
            "description",
            "location",
            36.877934,
            -94.878487
        ).toReminderDto()
        database.reminderDao().saveReminder(reminder)

        // WHEN  - reminder retrieved by ID.
        val result = localDataSource.getReminder(reminder.id)

        // THEN - Same reminder is returned.
        assertThat(result.succeeded, `is`(true))
        result as Success
        assertThat(result.data.id, `is`(reminder.id))
        assertThat(result.data.title, `is`(reminder.title))
        assertThat(result.data.description, `is`(reminder.description))
        assertThat(result.data.location, `is`(reminder.location))


    }

    @Test
    fun getReminderWithInvalidId_reminderNotFound() = runBlocking {
        val loaded = localDataSource.getReminder("invalidId")
        assertThat(loaded.succeeded, `is`(false))
        loaded as Error
        assertThat(loaded.exception, `is`("Reminder not found!"))
    }

}