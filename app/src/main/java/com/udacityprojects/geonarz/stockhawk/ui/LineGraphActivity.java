package com.udacityprojects.geonarz.stockhawk.ui;

import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.widget.Toast;

import com.db.chart.model.LineSet;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.udacityprojects.geonarz.stockhawk.R;
import com.udacityprojects.geonarz.stockhawk.rest.Utils;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class LineGraphActivity extends AppCompatActivity {

    int maxPrice, minPrice;
    private ArrayList<String> labels;
    private ArrayList<Float> values;
    private LineChartView lineChartView;
    private LineSet lineSet;
    private String company;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_line_graph);

        initializeChart();
        String symbol = getIntent().getStringExtra(getResources().getString(R.string.string_symbol));
        downloadStockDetails(symbol);
    }

    public void initializeChart() {
        lineSet = new LineSet();
        lineChartView = (LineChartView)findViewById(R.id.linechart);

        Paint paint = new Paint();
        paint.setColor(getResources().getColor(R.color.line_color));
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);
        paint.setStrokeWidth(dpToPx(1));

        lineChartView.setBorderSpacing(1)
                .setLabelsColor(getResources().getColor(R.color.labels))
                .setBorderSpacing(dpToPx(5))
                .setGrid(ChartView.GridType.HORIZONTAL, paint);

        lineSet.setColor(getResources().getColor(R.color.line_set_color))
                .setDotsStrokeColor(getResources().getColor(R.color.line_color))
                .setDotsColor(Color.WHITE)
                .setDotsRadius(10)
                .setDotsStrokeThickness(dpToPx(2));
    }

    public void refreshChart() {
        findMinMax();
        addPointsToLineSet();
        lineChartView.addData(lineSet);
        lineChartView.show();
    }

    public void addPointsToLineSet() {
        for (int i = 0; i < values.size(); i++) {
            lineSet.addPoint(labels.get(i), values.get(i));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(company != null && values != null && labels != null) {
            // convert float array list into regular array to store in the bundle
            float[] valuesArray = new float[values.size()];
            for (int i = 0; i < valuesArray.length; i++) {
                valuesArray[i] = values.get(i);
            }
            // store remaining state values in the bundle
            outState.putFloatArray("values", valuesArray);
            outState.putString("company", company);
            outState.putStringArrayList("labels", labels);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.containsKey("company")) {
            values = new ArrayList<>();
            float[] valuesArray = savedInstanceState.getFloatArray("values");
            for (int i = 0; i < valuesArray.length; i++)
                values.add(valuesArray[i]);
            company = savedInstanceState.getString("company");
            labels = savedInstanceState.getStringArrayList("labels");
            onDownloadCompleted();
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    // Get the json data from yahoo api and display it on the graph
    public void downloadStockDetails(String symbol) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("http://chartapi.finance.yahoo.com/instrument/1.0/" + symbol + "/chartdata;type=quote;range=5y/json")
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(Response response) throws IOException {
                if (response.code() == 200) {
                    try {
                        // Response string starts and ends with part that is not JSON so we remove it
                        String result = response.body().string();
                        if (result.startsWith("finance_charts_json_callback( ")) {
                            result = result.substring(29, result.length() - 2);
                        }
                        labels = new ArrayList<String>();
                        values = new ArrayList<Float>();
                        company = Utils.parseJsonPrevious(new JSONObject(result), getApplicationContext(), labels, values);
                        onDownloadCompleted();
                    } catch (Exception e) {
                        onDownloadFailed();
                        e.printStackTrace();
                    }
                } else {
                    onDownloadFailed();
                }
            }
            @Override
            public void onFailure(Request request, IOException e) {
                onDownloadFailed();
            }
        });
    }

    public void onDownloadCompleted() {
        refreshChart();
        LineGraphActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                getSupportActionBar().setTitle(company);
            }
        });
    }

    public void onDownloadFailed() {
        LineGraphActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), R.string.request_failed,
                        Toast.LENGTH_LONG).show();
            }
        });
    }

    public void findMinMax() {
        maxPrice = Math.round(Collections.max(values));
        minPrice = Math.round(Collections.min(values));

        lineChartView.setAxisBorderValues(minPrice - 50, maxPrice + 50);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

}
