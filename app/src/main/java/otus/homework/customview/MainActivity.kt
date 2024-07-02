package otus.homework.customview

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import kotlinx.serialization.json.Json
import java.util.Calendar

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listOfPayments = getPayments()

        initLineChart(listOfPayments, R.id.splineChartView)
    }

    private fun getPayments(): List<Payment> {
        val fileContent = resources.openRawResource(R.raw.payload).readBytes()
        val str = String(fileContent)
        return Json.decodeFromString(str)
    }

    private fun initPieChar(payments: List<Payment>, viewId: Int) {
        val pieChart = findViewById<PieChartView>(viewId)
        val categories = mutableMapOf<String, Int>()

        payments.forEach { payment ->
            categories[payment.category] = (categories[payment.category] ?: 0) + payment.amount
        }

        pieChart.setDataChart(
            categories.toList()
        )

        pieChart.setOnClickListener {
            val category = pieChart.getSelectedCategory()
            Log.d("MainActivity", "Category: $category")
        }
    }

    private fun initLineChart(payments: List<Payment>, viewId: Int) {
        val mapOfPayments = mutableMapOf<Int, Int>()
        payments.forEach { payment ->
            if (payment.category == "Здоровье") {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = payment.time.toLong() * 1000L
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                val month = calendar.get(Calendar.MONTH)
                Log.d("MainActivity", "Day: $day Year: $month")
                mapOfPayments[day] = (mapOfPayments[day] ?: 0) + payment.amount
            }
        }

        val lineChartView: LineChartView = findViewById(viewId)

        val dataPoints = listOf(
            Pair(0, 0f),
            Pair(1, 500f),
            Pair(2, 100f),
            Pair(3, 700.8f),
            Pair(4, 300f),
            Pair(5, 0f),
            Pair(6, 0f),
            Pair(7, 100f),
            Pair(8, 700f),
            Pair(9, 300f),
            Pair(10, 500f),
            Pair(11, 0f)
        )

        lineChartView.setData(dataPoints)
    }
 }