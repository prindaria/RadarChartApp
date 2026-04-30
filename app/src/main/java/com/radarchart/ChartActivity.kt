package com.radarchart

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    private lateinit var radarChart: RadarChart
    private lateinit var winnerInfoLayout: View
    private lateinit var winnerTextView: TextView
    private lateinit var scoreTextView: TextView

    private val dimensions = arrayOf("节能", "级别", "抗腐蚀", "低温性能", "高温性能", "抗压降")

    private val brandColors = mapOf(
        "Ebara" to Color.parseColor("#1565C0")
    )

    private val autoPalette = arrayOf(
        "#E53935", "#1E88E5", "#43A047", "#FB8C00",
        "#8E24AA", "#00ACC1", "#D81B60", "#7CB342"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chart)

        initViews()
        setupChart()
        loadData()
    }

    private fun initViews() {
        radarChart = findViewById(R.id.radarChart)
        winnerInfoLayout = findViewById(R.id.winnerInfoLayout)
        winnerTextView = findViewById(R.id.winnerTextView)
        scoreTextView = findViewById(R.id.scoreTextView)
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
            override fun onValueSelected(e: com.github.mikephil.charting.data.Entry?, h: Highlight?) {}
            override fun onNothingSelected() {}
        })
    }

    private fun loadData() {
        @Suppress("DEPRECATION")
        val products = intent.getParcelableArrayListExtra<Product>("selected_products")
        val highlight = intent.getBooleanExtra("highlight_winner", true)

        if (products.isNullOrEmpty()) {
            finish()
            return
        }

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

            val color = brandColors[product.brand] ?: Color.parseColor(autoPalette[index % autoPalette.size])

            val dataSet = RadarDataSet(values, "${product.brand} · ${product.name}").apply {
                this.color = color
                fillColor = color
                setDrawFilled(true)
                fillAlpha = 30
                lineWidth = 2f
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
    }
}