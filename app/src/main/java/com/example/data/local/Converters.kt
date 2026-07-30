package com.example.data.local

import androidx.room.TypeConverter
import com.example.domain.model.PrinterConnectionType
import com.example.domain.model.RepairTicketStatus
import com.example.domain.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.STAFF
    }

    @TypeConverter
    fun fromRepairTicketStatus(status: RepairTicketStatus): String = status.name

    @TypeConverter
    fun toRepairTicketStatus(value: String): RepairTicketStatus = try {
        RepairTicketStatus.valueOf(value)
    } catch (e: Exception) {
        RepairTicketStatus.RECEIVED
    }

    @TypeConverter
    fun fromPrinterConnectionType(type: PrinterConnectionType): String = type.name

    @TypeConverter
    fun toPrinterConnectionType(value: String): PrinterConnectionType = try {
        PrinterConnectionType.valueOf(value)
    } catch (e: Exception) {
        PrinterConnectionType.NONE
    }
}
