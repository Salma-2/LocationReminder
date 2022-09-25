package com.udacity.project4.locationreminders.data.local

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.toReminderDto

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Test

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var database: RemindersDatabase
    private lateinit var appContext: Application

    @Before
    fun initDb() {
        appContext = getApplicationContext()
        database = Room.inMemoryDatabaseBuilder(
            appContext,
            RemindersDatabase::class.java
        ).build()
    }

    @After
    fun closeDb() = database.close()

    @Test
    fun insertReminderAndGetById() = mainCoroutineRule.runBlockingTest {
        // GIVEN - Insert Reminder
        val reminder = ReminderDataItem(
            "title",
            "desc",
            "location",
            36.877934,
            -94.878487
        ).toReminderDto()
        database.reminderDao().saveReminder(reminder)

        // WHEN - Get the reminder by id
        val loaded = database.reminderDao().getReminderById(reminder.id)

        // THEN - the loaded data contains the expected values
        assertThat<ReminderDTO>(loaded as ReminderDTO, notNullValue())
        assertThat(loaded.id, `is`(reminder.id))
        assertThat(loaded.title, `is`(reminder.title))
        assertThat(loaded.description, `is`(reminder.description))
        assertThat(loaded.location, `is`(reminder.location))


    }

    @Test
    fun deleteRemindersAndCheckTheDbIsClear() = mainCoroutineRule.runBlockingTest {
        // GIVEN - insert some reminders
        database.reminderDao().saveReminder(ReminderDataItem(
            "title1",
            "desc1",
            "location1",
            36.877934,
            -94.878487
        ).toReminderDto())
        database.reminderDao().saveReminder(ReminderDataItem(
            "title2",
            "desc2",
            "location2",
            36.877934,
            -94.878487
        ).toReminderDto())

        // WHEN - delete all reminders
        database.reminderDao().deleteAllReminders()

        // THEN - getReminders returns empty list
        val loaded: List<ReminderDTO> = database.reminderDao().getReminders()
        assertThat(loaded.size, `is`(0))
    }

    @Test
    fun insertRemindersAndGetAll() = mainCoroutineRule.runBlockingTest {
        // GIVEN
        val reminder1 = ReminderDataItem(
            "title1",
            "desc1",
            "location1",
            36.877934,
            -94.878487
        ).toReminderDto()
        val reminder2 = ReminderDataItem(
            "title2",
            "desc2",
            "location2",
            36.877934,
            -94.878487
        ).toReminderDto()

        database.reminderDao().saveReminder(reminder1)
        database.reminderDao().saveReminder(reminder2)

        // WHEN
        val loaded = database.reminderDao().getReminders()
        val loadedReminder1 = loaded[0]
        val loadedReminder2 = loaded[1]

        // THEN
        assertThat(loadedReminder1.id, `is`(reminder1.id))
        assertThat(loadedReminder2.id, `is`(reminder2.id))

    }

    @Test
    fun getReminderWithInvalidId() = mainCoroutineRule.runBlockingTest {
        val loaded = database.reminderDao().getReminderById("invalid-id")
        assertThat<ReminderDTO>(loaded, `is`(nullValue()))
    }


}