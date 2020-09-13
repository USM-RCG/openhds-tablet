package org.cimsbioko.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.androidplot.xy.*
import org.cimsbioko.databinding.SyncHistoryFragmentBinding
import org.cimsbioko.provider.DatabaseAdapter
import java.text.FieldPosition
import java.text.Format
import java.text.ParsePosition
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

private const val MILLIS_IN_SEC = 1000
private const val SECONDS_IN_DAY = 86400
private const val MAX_DIVISIONS = 15.0f

class SyncHistoryFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return SyncHistoryFragmentBinding.inflate(inflater, container, false).also {
            it.syncHistoryPlot.apply {
                SimpleXYSeries(SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "", *DatabaseAdapter.syncResults).also { series ->
                    addSeries(series, LineAndPointFormatter().apply {
                        fillPaint = null
                        vertexPaint.setARGB(255, 0, 0, 0)
                    })
                    legend.isVisible = false
                    domainTitle.isVisible = false
                    setPlotPadding(5f, 5f, 5f, 0f)
                    val secondsToStep = (if (series.size() > 1) {
                        val duration: Int = series.getX(series.size() - 1).toInt() - series.getX(0).toInt()
                        val daysSpanned: Int = duration / SECONDS_IN_DAY
                        max(1.0, ceil(daysSpanned / MAX_DIVISIONS.toDouble()))
                    } else 1.0) * SECONDS_IN_DAY
                    setDomainStep(StepMode.INCREMENT_BY_VAL, secondsToStep)
                    if (series.size() > 0) {
                        setDomainLowerBoundary(getMidnightOfDay(series.getX(0).toLong()), BoundaryMode.FIXED)
                        setDomainUpperBoundary(getMidnightOfDay(series.getX(series.size() - 1).toLong()) + secondsToStep.toLong(), BoundaryMode.FIXED)
                    }
                    graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM).apply {
                        format = SyncHistoryTimeFormat()
                        rotation = -75f
                    }
                }
            }
        }.root
    }

    private fun getMidnightOfDay(epochTime: Long): Long =
            Calendar.getInstance()
                    .also { c ->
                        c.timeInMillis = epochTime * MILLIS_IN_SEC
                        c[Calendar.HOUR_OF_DAY] = 0
                        c[Calendar.MINUTE] = 0
                        c[Calendar.SECOND] = 0
                        c[Calendar.MILLISECOND] = 0
                    }.timeInMillis / MILLIS_IN_SEC
}


private class SyncHistoryTimeFormat : Format() {

    private val dateFormat = SimpleDateFormat("MM/dd")

    override fun format(obj: Any, buffer: StringBuffer, field: FieldPosition): StringBuffer {
        val timestamp: Long = (obj as Number).toLong() * MILLIS_IN_SEC
        val date = Date(timestamp)
        return dateFormat.format(date, buffer, field)
    }

    override fun parseObject(string: String, position: ParsePosition): Any? = null
}