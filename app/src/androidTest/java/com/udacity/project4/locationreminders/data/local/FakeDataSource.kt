package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

class FakeDataSource(var reminders: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {

    private var returnError = false
    fun setReturnError(newRet: Boolean) {
        returnError = newRet
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (returnError) {
            return Result.Error("Error Loading Reminders")
        } else return Result.Success(ArrayList(reminders))
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        reminders?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        if (returnError) {
            return Result.Error("Error Loading Reminder with id " + id)
        } else {
            var reminder = reminders?.find { it -> it.id == id }
            return if (reminder == null) Result.Error("unable to get reminder id " + id) else Result.Success(
                reminder
            )
        }
    }

    override suspend fun deleteAllReminders() {
        reminders?.clear()
    }


}