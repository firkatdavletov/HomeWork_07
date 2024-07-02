package otus.homework.customview

interface PieChartViewInterface {
    fun setDataChart(data: List<Pair<String, Int>>)
    fun getSelectedCategory(): String?
}