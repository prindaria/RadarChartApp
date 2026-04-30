package com.radarchart

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private lateinit var searchEditText: TextInputEditText
    private lateinit var productListContainer: LinearLayout
    private lateinit var selectAllButton: MaterialButton
    private lateinit var clearAllButton: MaterialButton
    private lateinit var highlightCheckBox: MaterialCheckBox
    private lateinit var confirmButton: MaterialButton

    private val productData = mutableListOf<Product>()
    private val checkedProducts = mutableSetOf<Product>()
    private val productCheckboxes = mutableMapOf<Product, CheckBox>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        loadData()
        setupListeners()
        updateProductList("")
    }

    private fun initViews() {
        searchEditText = findViewById(R.id.searchEditText)
        productListContainer = findViewById(R.id.productListContainer)
        selectAllButton = findViewById(R.id.selectAllButton)
        clearAllButton = findViewById(R.id.clearAllButton)
        highlightCheckBox = findViewById(R.id.highlightCheckBox)
        confirmButton = findViewById(R.id.confirmButton)
    }

    private fun loadData() {
        try {
            val inputStream = assets.open("products.csv")
            val reader = inputStream.bufferedReader()

            // Skip potential BOM (UTF-8 BOM = EF BB BF)
            var firstLine = reader.readLine() ?: ""
            if (firstLine.startsWith("\uFEFF")) {
                firstLine = firstLine.substring(1)
            }
            if (firstLine.isNotEmpty() && !firstLine.contains("品牌")) {
                // First non-BOM line is actually data, parse it
                parseCsvLine(firstLine)?.let { productData.add(it) }
            }

            reader.forEachLine { line ->
                if (line.isNotBlank()) {
                    parseCsvLine(line)?.let { productData.add(it) }
                }
            }
            reader.close()

            Log.d(TAG, "Loaded ${productData.size} products")
            if (productData.isEmpty()) {
                Toast.makeText(this, "数据加载失败，请检查CSV文件", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load data", e)
            Toast.makeText(this, "数据加载失败: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun parseCsvLine(line: String): Product? {
        try {
            // Handle potential BOM
            val cleanLine = line.replaceFirst("\uFEFF", "")
            val parts = cleanLine.split(",").map { it.trim() }
            if (parts.size >= 9) {
                return Product(
                    brand = parts[0],
                    series = parts[1],
                    name = parts[2],
                    energy = parts[3].toIntOrNull() ?: 0,
                    grade = parts[4].toIntOrNull() ?: 0,
                    corrosion = parts[5].toIntOrNull() ?: 0,
                    lowTemp = parts[6].toIntOrNull() ?: 0,
                    highTemp = parts[7].toIntOrNull() ?: 0,
                    pressure = parts[8].toIntOrNull() ?: 0
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to parse line: $line", e)
        }
        return null
    }

    private fun setupListeners() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateProductList(s?.toString() ?: "")
            }
        })

        selectAllButton.setOnClickListener {
            val query = searchEditText.text?.toString() ?: ""
            val visibleProducts = if (query.isEmpty()) {
                productData.toList()
            } else {
                productData.filter {
                    it.name.contains(query, ignoreCase = true) ||
                    it.brand.contains(query, ignoreCase = true) ||
                    it.series.contains(query, ignoreCase = true)
                }
            }
            visibleProducts.forEach { product ->
                checkedProducts.add(product)
                productCheckboxes[product]?.isChecked = true
            }
        }

        clearAllButton.setOnClickListener {
            checkedProducts.clear()
            productCheckboxes.values.forEach { it.isChecked = false }
        }

        confirmButton.setOnClickListener {
            if (checkedProducts.isEmpty()) {
                Toast.makeText(this, R.string.please_select, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            try {
                val highlight = highlightCheckBox.isChecked
                val selectedProducts = ArrayList(checkedProducts.toList())

                val intent = Intent(this, ChartActivity::class.java).apply {
                    putParcelableArrayListExtra("selected_products", selectedProducts)
                    putExtra("highlight_winner", highlight)
                }
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to start ChartActivity", e)
                Toast.makeText(this, "启动图表失败: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateProductList(query: String) {
        productListContainer.removeAllViews()
        productCheckboxes.clear()

        if (productData.isEmpty()) {
            val noDataText = TextView(this).apply {
                text = getString(R.string.no_data)
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
                gravity = Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            productListContainer.addView(noDataText)
            return
        }

        val filteredProducts = if (query.isEmpty()) {
            productData
        } else {
            productData.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true) ||
                it.series.contains(query, ignoreCase = true)
            }
        }

        if (filteredProducts.isEmpty()) {
            val noDataText = TextView(this).apply {
                text = getString(R.string.no_data)
                textSize = 14f
                setTextColor(getColor(R.color.text_secondary))
                gravity = Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            productListContainer.addView(noDataText)
            return
        }

        // Group by brand
        val groupedProducts = filteredProducts.groupBy { it.brand }

        groupedProducts.forEach { (brand, products) ->
            // Brand header
            val brandText = TextView(this).apply {
                text = "  $brand"
                textSize = 14f
                setTextColor(getColor(R.color.primary))
                setPadding(0, 16, 0, 8)
                setTypeface(typeface, android.graphics.Typeface.BOLD)
            }
            productListContainer.addView(brandText)

            products.forEach { product ->
                val checkbox = CheckBox(this).apply {
                    text = product.name
                    setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            checkedProducts.add(product)
                        } else {
                            checkedProducts.remove(product)
                        }
                    }
                    isChecked = checkedProducts.contains(product)
                }
                productCheckboxes[product] = checkbox
                productListContainer.addView(checkbox)
            }
        }
    }
}
