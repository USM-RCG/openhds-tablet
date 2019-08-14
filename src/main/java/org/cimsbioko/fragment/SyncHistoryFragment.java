package org.cimsbioko.fragment;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.StepMode;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;

import org.cimsbioko.R;
import org.cimsbioko.provider.DatabaseAdapter;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SyncHistoryFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View graphView = inflater.inflate(R.layout.sync_history_fragment, container, false);

        XYPlot plot = graphView.findViewById(R.id.sync_history_plot);

        DatabaseAdapter db = DatabaseAdapter.getInstance(getActivity());

        XYSeries series = new SimpleXYSeries(SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "", db.getSyncResults());

        LineAndPointFormatter formatter = new LineAndPointFormatter();
        formatter.setFillPaint(null);
        formatter.getVertexPaint().setARGB(255, 0, 0, 0);
        plot.addSeries(series, formatter);

        final int SECONDS_IN_DAY = 86400;
        final float MAX_DIVISIONS = 15.0f;
        plot.getLegend().setVisible(false);
        plot.getDomainTitle().setVisible(false);
        plot.setPlotPadding(5, 5, 5, 0);

        double daysToStep = 1;
        if (series.size() > 1) {
            final int duration = series.getX(series.size() - 1).intValue() - series.getX(0).intValue();
            final int daysSpanned = duration / SECONDS_IN_DAY;
            daysToStep = Math.max(1.0, Math.ceil(daysSpanned / MAX_DIVISIONS));
        }
        final double secondsToStep = daysToStep * SECONDS_IN_DAY;
        plot.setDomainStep(StepMode.INCREMENT_BY_VAL, secondsToStep);

        if (series.size() > 0) {
            plot.setDomainLowerBoundary(getMidnightOfDay(series.getX(0).longValue()), BoundaryMode.FIXED);
            plot.setDomainUpperBoundary(getMidnightOfDay(series.getX(series.size()-1).longValue()) + (long)secondsToStep, BoundaryMode.FIXED);
        }

        XYGraphWidget graph = plot.getGraph();

        XYGraphWidget.LineLabelStyle bottomStyle = graph.getLineLabelStyle(XYGraphWidget.Edge.BOTTOM);
        bottomStyle.setFormat(new SyncHistoryTimeFormat());
        bottomStyle.setRotation(-75);

        return graphView;
    }

    private long getMidnightOfDay(long epochTime) {
        final int MILLIS_IN_SEC = 1000;
        Calendar startCal = Calendar.getInstance();
        startCal.setTimeInMillis(epochTime * MILLIS_IN_SEC);
        startCal.set(Calendar.HOUR_OF_DAY, 0);
        startCal.set(Calendar.MINUTE, 0);
        startCal.set(Calendar.SECOND, 0);
        startCal.set(Calendar.MILLISECOND, 0);
        return startCal.getTimeInMillis() / MILLIS_IN_SEC;
    }

    private static class SyncHistoryTimeFormat extends Format {

        private static final int MILLIS_IN_SEC = 1000;

        private SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd");

        @Override
        public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition field) {
            long timestamp = ((Number) obj).longValue() * MILLIS_IN_SEC;
            Date date = new Date(timestamp);
            return dateFormat.format(date, buffer, field);
        }

        @Override
        public Object parseObject(String string, ParsePosition position) {
            return null;
        }
    }
}
