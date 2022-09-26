package com.udacity.project4.locationreminders.data

import androidx.annotation.VisibleForTesting
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result.Success
import com.udacity.project4.locationreminders.data.dto.Result


//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource : ReminderDataSource {
    private var reminders: LinkedHashMap<String, ReminderDTO> = LinkedHashMap()
    private var shouldReturnError = false

    @VisibleForTesting
    override fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

//   Create a fake data source to act as a double to the real data source

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error("Test exception")
        }
        return Success(reminders.values.toList())
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders[reminder.id] = reminder }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (!shouldReturnError) {
            reminders[id]?.let {
                return Success(it)
            }
        }
        return Result.Error("Test Exception")
    }

    override suspend fun deleteAllReminders() {
        reminders.clear()
    }


}