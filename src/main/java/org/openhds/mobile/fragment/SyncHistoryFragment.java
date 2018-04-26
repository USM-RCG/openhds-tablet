package org.openhds.mobile.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYGraphWidget;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

import org.openhds.mobile.R;
import org.openhds.mobile.provider.DatabaseAdapter;

import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SyncHistoryFragment extends Fragment {

    private XYPlot plot;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View graphView = inflater.inflate(R.layout.sync_history_fragment, container, false);

        plot = (XYPlot) graphView.findViewById(R.id.sync_history_plot);

        DatabaseAdapter db = DatabaseAdapter.getInstance(getActivity());

        XYSeries series = new SimpleXYSeries(SimpleXYSeries.ArrayFormat.XY_VALS_INTERLEAVED, "", db.getSyncResults());

        LineAndPointFormatter formatter = new LineAndPointFormatter();
        formatter.setFillPaint(null);
        formatter.getVertexPaint().setARGB(255, 0, 0, 0);
        plot.addSeries(series, formatter);

        final int SECONDS_IN_DAY = 86400;
        final float MAX_DIVISIONS = 15.0f;
        plot.getLegendWidget().setVisible(false);
        plot.getDomainLabelWidget().setVisible(false);
        plot.setPlotPadding(5, 5, 5, 0);
        double dayStep = 1;
        if (series.size() > 1) {
            plot.setDomainLeftMin(series.getX(0).longValue() % SECONDS_IN_DAY);
            int daySpan = (series.getX(series.size() - 1).intValue() - series.getX(0).intValue()) / SECONDS_IN_DAY;
            dayStep = Math.max(1.0, Math.ceil(daySpan / MAX_DIVISIONS));
        }
        plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, SECONDS_IN_DAY * dayStep);
        plot.setDomainValueFormat(new SyncHistoryTimeFormat());

        XYGraphWidget graph = plot.getGraphWidget();
        graph.setDomainLabelOrientation(-75);
        graph.setDomainTickLabelVerticalOffset(5);
        graph.setDomainTickLabelHorizontalOffset(5);

        return graphView;
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
