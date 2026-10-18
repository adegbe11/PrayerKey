package com.prayerkey.manna.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import com.prayerkey.manna.MainActivity
import com.prayerkey.manna.R
import com.prayerkey.manna.data.Today
import com.prayerkey.manna.data.Walk
import com.prayerkey.manna.model.todaysPrayer
import com.prayerkey.manna.platform.today

/**
 * Three widgets for the home screen: the verse of the day, the walk, and
 * today's prayer. Each is drawn from what the app has already saved - the walk
 * and the day's verse - so a widget never has to open the Bible to draw, and
 * each opens the app straight at the thing it shows.
 */
abstract class PrayerKeyWidget : AppWidgetProvider() {
    abstract fun draw(context: Context): RemoteViews

    override fun onUpdate(context: Context, manager: AppWidgetManager, ids: IntArray) {
        val views = runCatching { draw(context) }.getOrNull() ?: return
        ids.forEach { manager.updateAppWidget(it, views) }
    }

    protected fun open(context: Context, route: String, code: Int): PendingIntent =
        PendingIntent.getActivity(
            context, code,
            Intent(context, MainActivity::class.java)
                .putExtra("prayerkey_destination", route)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    companion object {
        /** Redraws every PrayerKey widget on the home screen. Cheap when there are none. */
        fun refresh(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            listOf(VerseWidget::class.java, WalkWidget::class.java, PrayerWidget::class.java).forEach { type ->
                val ids = manager.getAppWidgetIds(ComponentName(context, type))
                if (ids.isNotEmpty()) {
                    context.sendBroadcast(
                        Intent(context, type).setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE)
                            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids),
                    )
                }
            }
        }

        internal fun walk(context: Context): Pair<Int, Boolean> {
            val day = today().toEpochDays().toLong()
            val state = Walk.load(context)
            return state.current(day) to state.walkedOn(day)
        }
    }
}

class VerseWidget : PrayerKeyWidget() {
    override fun draw(context: Context): RemoteViews {
        val date = today()
        val card = Today.read(context, date.toString(), date.toEpochDays().toLong())
        val (days, _) = walk(context)
        return RemoteViews(context.packageName, R.layout.widget_verse).apply {
            setTextViewText(R.id.verse, card.verse)
            setTextViewText(R.id.ref, "${card.ref} · KJV")
            val walking = if (days > 0) View.VISIBLE else View.GONE
            setViewVisibility(R.id.walk_flame, walking)
            setViewVisibility(R.id.walk_days, walking)
            setTextViewText(R.id.walk_days, days.toString())
            setOnClickPendingIntent(R.id.widget_root, open(context, "bible", 9_101))
        }
    }
}

class WalkWidget : PrayerKeyWidget() {
    override fun draw(context: Context): RemoteViews {
        val (days, walkedToday) = walk(context)
        return RemoteViews(context.packageName, R.layout.widget_walk).apply {
            setTextViewText(R.id.walk_days, days.toString())
            setTextViewText(
                R.id.walk_label,
                when { days == 0 -> "Begin your walk"; days == 1 -> "day with God"; else -> "days with God" },
            )
            if (walkedToday) {
                setTextViewText(R.id.walk_action, "Walked today")
                setInt(R.id.walk_action, "setBackgroundResource", R.drawable.widget_pill_done)
                setTextColor(R.id.walk_action, 0xFFFFFFFF.toInt())
            } else {
                setTextViewText(R.id.walk_action, if (days > 0) "Pray today" else "Pray now")
                setInt(R.id.walk_action, "setBackgroundResource", R.drawable.widget_pill)
                setTextColor(R.id.walk_action, 0xFF2E0078.toInt())
            }
            setOnClickPendingIntent(R.id.widget_root, open(context, "prayer", 9_102))
        }
    }
}

class PrayerWidget : PrayerKeyWidget() {
    override fun draw(context: Context): RemoteViews {
        val prayer = todaysPrayer()
        val (_, walkedToday) = walk(context)
        return RemoteViews(context.packageName, R.layout.widget_prayer).apply {
            setTextViewText(R.id.title, prayer.title)
            setTextViewText(R.id.body, prayer.prayer)
            setTextViewText(R.id.action, if (walkedToday) "Pray again today" else "Pray now")
            setOnClickPendingIntent(R.id.widget_root, open(context, "prayer", 9_103))
        }
    }
}
