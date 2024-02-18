package com.streamside.periodtracker.notification

interface INotificationScheduler {
    fun schedule(item: NotificationItem)
    fun cancel(item: NotificationItem)
}