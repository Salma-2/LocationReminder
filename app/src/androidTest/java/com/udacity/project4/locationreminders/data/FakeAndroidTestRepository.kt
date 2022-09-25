package com.udacity.project4.locationreminders.data

import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.data.dto.Result.Error
import com.udacity.project4.locationreminders.data.dto.Result


class FakeAndroidTestRepository : ReminderDataSource {
    private var reminders: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    @VisibleForTesting
    override fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }


    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Error("Test exception")
        }
        return Success(reminders.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (shouldReturnError) {
            return Error("Test exception")
        }
        reminders[id]?.let {
            return Success(it)
        }
        return Error("Could not find task")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}