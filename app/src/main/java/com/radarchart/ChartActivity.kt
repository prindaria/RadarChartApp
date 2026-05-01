package com.radarchart

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IRadarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener

class ChartActivity : AppCompatActivity() {

    private val TAG = "ChartActivity"

    private lateinit var radarChart: RadarChart
    private lateinit var winnerInfoLayout: View
    private lateinit var winnerTextView: TextView
    private lateinit var scoreTextView: TextView
    private lateinit var backButton: View
    private lateinit var switchButton: View

    private val dimensions = arrayOf("节能", "级别", "抗腐蚀", "低温性能", "高温性能", "抗压降")

    private var displayMode = 0 // 0 = rainbow, 1 = line style

    private val autoPalette = intArrayOf(
        Color.parseColor("#E53935"),
        Color.parseColor("#1E88E5"),
        Color.parseColor("#43A047"),
        Color.parseColor("#FB8C00"),
        Color.parseColor("#8E24AA"),
        Color.parseColor("#00ACC1"),
        Color.parseColor("#D81B60"),
        Color.parseColor("#7CB342"),
        Color.parseColor("#6D4C41"),
        Color.parseColor("#546E7A")
    )

    private val lineStyles = floatArrayOf(2f, 4f, 6f, 2f, 4f, 2f, 4f, 6f, 2f, 4f)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        try {
            initViews()
            setupChart()
            loadData()
        } catch (e: Exception) {
            Log.e(TAG, "Fatal error in onCreate", e)
            Toast.makeText(this, "初始化失败: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun initViews() {
        radarChart = findViewById(R.id.radarChart)
        winnerInfoLayout = findViewById(R.id.winnerInfoLayout)
        winnerTextView = findViewById(R.id.winnerTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
        backButton = findViewById(R.id.backButton)
        switchButton = findViewById(R.id.switchButton)

        backButton.setOnClickListener { finish() }

        switchButton.setOnClickListener {
            displayMode = if (displayMode == 0) 1 else 0
            // Reload chart with new mode
            loadData()
        }
    }

    private fun setupChart() {
        radarChart.description.isEnabled = false
        radarChart.setBackgroundColor(Color.WHITE)
        radarChart.webLineWidth = 1f
        radarChart.webColor = Color.parseColor("#B0BEC5")
        radarChart.webLineWidthInner = 0.5f
        radarChart.webColorInner = Color.parseColor("#ECEFF1")
        radarChart.webAlpha = 200

        // X axis
        val xAxis = radarChart.xAxis
        xAxis.position = XAxis.XAxisPosition.TOP
        xAxis.setDrawLabels(true)
        xAxis.textSize = 12f
        xAxis.textColor = Color.parseColor("#37474F")
        xAxis.valueFormatter = IndexAxisValueFormatter(dimensions)
        xAxis.setGranularity(1f)

        // Y axis
        val yAxis = radarChart.yAxis
        yAxis.axisMinimum = 0f
        yAxis.axisMaximum = 10f
        yAxis.setDrawLabels(true)
        yAxis.textSize = 10f
        yAxis.textColor = Color.parseColor("#78909C")
        yAxis.setLabelCount(6)

        // Legend
        val legend = radarChart.legend
        legend.isEnabled = true
        legend.orientation = Legend.LegendOrientation.VERTICAL
        legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        legend.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        legend.textSize = 10f
        legend.textColor = Color.parseColor("#37474F")

        // Touch listener
        radarChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {
                // Optional: show tooltip
            }
            override fun onNothingSelected() {}
        })
    }

    @Suppress("DEPRECATION")
    private fun loadData() {
        var products: java.util.ArrayList<Product>? = null
        var highlight = true

        try {
            products = intent.getParcelableArrayListExtra("selected_products")
            highlight = intent.getBooleanExtra("highlight_winner", true)
        } catch (e: Exception) {
            Log.e(TAG, "Error getting intent extras", e)
        }

        if (products.isNullOrEmpty()) {
            Toast.makeText(this, "没有选择产品", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        try {
            val entries = mutableListOf<RadarDataSet>()

            products.forEachIndexed { index, product ->
                val values = listOf(
                    RadarEntry(product.energy.toFloat()),
                    RadarEntry(product.grade.toFloat()),
                    RadarEntry(product.corrosion.toFloat()),
                    RadarEntry(product.lowTemp.toFloat()),
                    RadarEntry(product.highTemp.toFloat()),
                    RadarEntry(product.pressure.toFloat())
                )

                val color = autoPalette[index % autoPalette.size]

                val label = "${product.brand} · ${product.name}"

                val dataSet = RadarDataSet(values, label).apply {
                    this.color = color
                    fillColor = color
                    setDrawFilled(true)
                    fillAlpha = 30
                    lineWidth = if (displayMode == 1) {
                        lineStyles[index % lineStyles.size]
                    } else {
                        2f
                    }
                    setDrawValues(false)
                    isHighlightEnabled = true
                }

                entries.add(dataSet)
            }

            @Suppress("UNCHECKED_CAST")
            val radarData = RadarData(entries as List<IRadarDataSet>)
            radarChart.data = radarData
            radarChart.animateXY(1000, 1000)
            radarChart.invalidate()

            // Calculate winner
            if (highlight && products.size > 1) {
                val winner = products.maxByOrNull { product ->
                    product.energy + product.grade + product.corrosion +
                    product.lowTemp + product.highTemp + product.pressure
                }

                winner?.let {
                    val totalScore = it.energy + it.grade + it.corrosion +
                                   it.lowTemp + it.highTemp + it.pressure

                    winnerInfoLayout.visibility = View.VISIBLE
                    winnerTextView.text = "★ 胜出者: ${it.brand} · ${it.name}"
                    scoreTextView.text = "总分: $totalScore/60"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error building radar chart", e)
            Toast.makeText(this, "图表生成失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
