package com.radarchart

import android.os.Parcel
import android.os.Parcelable
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import com.google.android.material.textfield.TextInputEditText

class MainActivity : AppCompatActivity() {

    private lateinit var searchEditText: TextInputEditText
    private lateinit var productListContainer: LinearLayout
    private lateinit var selectAllButton: AppCompatButton
    private lateinit var clearAllButton: AppCompatButton
    private lateinit var highlightCheckBox: CheckBox
    private lateinit var confirmButton: AppCompatButton

    private val productData = mutableListOf<Product>()
    private val checkedProducts = mutableSetOf<Product>()
    private val productCheckboxes = mutableMapOf<Product, CheckBox>()

    data class Product(
        val brand: String,
        val series: String,
        val name: String,
        val energy: Int,
        val grade: Int,
        val corrosion: Int,
        val lowTemp: Int,
        val highTemp: Int,
        val pressure: Int
    ) : Parcelable {
        constructor(parcel: Parcel) : this(
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readString() ?: "",
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readInt()
        )

        override fun writeToParcel(parcel: Parcel, flags: Int) {
            parcel.writeString(brand)
            parcel.writeString(series)
            parcel.writeString(name)
            parcel.writeInt(energy)
            parcel.writeInt(grade)
            parcel.writeInt(corrosion)
            parcel.writeInt(lowTemp)
            parcel.writeInt(highTemp)
            parcel.writeInt(pressure)
        }

        override fun describeContents(): Int = 0
    }

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
            reader.readLine() // Skip header

            reader.forEachLine { line ->
                val parts = line.split(",")
                if (parts.size >= 9) {
                    val product = Product(
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
                    productData.add(product)
                }
            }
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupListeners() {
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateProductList(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        selectAllButton.setOnClickListener {
            productData.forEach { product ->
                if (!checkedProducts.contains(product)) {
                    checkedProducts.add(product)
                    productCheckboxes[product]?.isChecked = true
                }
            }
        }

        clearAllButton.setOnClickListener {
            checkedProducts.clear()
            productCheckboxes.values.forEach { it.isChecked = false }
        }

        confirmButton.setOnClickListener {
            if (checkedProducts.isEmpty()) {
                return@setOnClickListener
            }

            val highlight = highlightCheckBox.isChecked
            val selectedProducts = checkedProducts.toList()

            val intent = Intent(this, ChartActivity::class.java).apply {
                putParcelableArrayListExtra("selected_products", ArrayList(selectedProducts))
                putExtra("highlight_winner", highlight)
            }
            startActivity(intent)
        }
    }

    private fun updateProductList(query: String) {
        productListContainer.removeAllViews()
        productCheckboxes.clear()

        val filteredProducts = if (query.isEmpty()) {
            productData
        } else {
            productData.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.brand.contains(query, ignoreCase = true) ||
                it.series.contains(query, ignoreCase = true)
            }
        }

        // Group by brand
        val groupedProducts = filteredProducts.groupBy { it.brand }

        groupedProducts.forEach { (brand, products) ->
            // Brand header
            val brandText = TextView(this).apply {
                text = "  $brand"
                textSize = 14f
                setTextColor(resources.getColor(R.color.primary, theme))
                setPadding(0, 16, 0, 8)
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
                    // Preserve checked state if searching
                    isChecked = checkedProducts.contains(product)
                }
                productCheckboxes[product] = checkbox
                productListContainer.addView(checkbox)
            }
        }

        if (filteredProducts.isEmpty()) {
            val noDataText = TextView(this).apply {
                text = getString(R.string.no_data)
                textSize = 14f
                setTextColor(resources.getColor(R.color.text_secondary, theme))
                gravity = android.view.Gravity.CENTER
                setPadding(0, 32, 0, 32)
            }
            productListContainer.addView(noDataText)
        }
    }
}