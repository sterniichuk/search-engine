package service;

import domain.Statistic;
import lombok.RequiredArgsConstructor;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class ChartService {

    public void makeChart(List<Statistic> statistics, String title, String filePath) throws IOException {
        XYSeries series = new XYSeries(title);
        statistics.forEach(stat-> series.add(stat.threads(), stat.time()));
        int fastestIndex = findFastest(statistics);
        var fastest = statistics.get(fastestIndex);
        XYSeries fastestSeries = new XYSeries("Fastest Point");
        fastestSeries.add(fastest.threads(), fastest.time());
        XYSeriesCollection dataset = new XYSeriesCollection(series);
        dataset.addSeries(fastestSeries);
        JFreeChart chart = createChart(title, dataset);
        XYPlot plot = chart.getXYPlot();
        setBackground(plot);
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        colorSeries(renderer);
        String text = annotatePoints(statistics, plot, fastest);
        setRange(statistics, fastest, plot, text);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.REVERSE);
        plot.setRenderer(renderer);
        ChartUtilities.saveChartAsPNG(new File(filePath), chart, 800, 350);
    }

    private static String annotatePoints(List<Statistic> statistics, XYPlot plot, Statistic fastest) {
        plot.addAnnotation(fastestPointLabel(fastest));
        String text = STR. "(\{ statistics.getFirst().threads() },\{ statistics.getFirst().time() })" ;
        plot.addAnnotation(firstPointLabel(statistics.getFirst(), text));
        return text;
    }

    private static void setRange(List<Statistic> statistics, Statistic fastest, XYPlot plot, String text) {
        double min = fastest.time();
        double max = statistics.stream().mapToInt(Statistic::time).max().orElse(-1);
        plot.getRangeAxis().setRange(min - min * 0.75, max + max * 0.1);
        int maxThreads = statistics.stream().mapToInt(Statistic::threads).max().orElse(-1);
        plot.getDomainAxis().setRange(-text.length(), maxThreads + (maxThreads * 0.1));
    }

    private static void setBackground(XYPlot plot) {
        plot.setBackgroundPaint(Color.white);
        plot.setDomainGridlinePaint(Color.gray);
        plot.setRangeGridlinePaint(Color.gray);
    }

    private static void colorSeries(XYLineAndShapeRenderer renderer) {
        renderer.setSeriesPaint(0, Color.blue);
        renderer.setSeriesPaint(1, Color.GREEN);
        renderer.setSeriesStroke(0, new BasicStroke(2.0f));
        renderer.setSeriesItemLabelsVisible(1, true);
    }

    private static JFreeChart createChart(String title, XYSeriesCollection dataset) {
        return ChartFactory.createXYLineChart(
                title,
                "Threads",
                "Time",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
    }

    private static XYTextAnnotation fastestPointLabel(Statistic fastest) {
        XYTextAnnotation pLabel = new XYTextAnnotation(STR. "(\{ fastest.threads() },\{ fastest.time() })" , fastest.threads(), fastest.time());
        pLabel.setTextAnchor(TextAnchor.TOP_CENTER);
        pLabel.setPaint(Color.GREEN);
        return pLabel;
    }

    private static XYTextAnnotation firstPointLabel(Statistic first, String text) {
        XYTextAnnotation pLabel = new XYTextAnnotation(text, first.threads(), first.time());
        pLabel.setTextAnchor(TextAnchor.BOTTOM_CENTER);
        pLabel.setPaint(Color.RED);
        return pLabel;
    }

    private int findFastest(List<Statistic> statistics) {
        int min = statistics.getFirst().time();
        int minIndex = 0;
        for (int i = 1; i < statistics.size(); i++) {
            int time = statistics.get(i).time();
            if (time < min) {
                minIndex = i;
                min = time;
            }
        }
        return minIndex;
    }
}