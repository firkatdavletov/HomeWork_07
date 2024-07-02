package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View

class LineChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs), LineChartViewInterface {
    private var xMax = X_MAX_VALUE
    private var xGridStep = X_STEP
    private var yMax = Y_MAX_VALUE
    private var yGridStep = Y_STEP

    init {
        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.LineChartView)
            xMax = typeArray.getInt(R.styleable.LineChartView_xMax, X_MAX_VALUE)
            xGridStep = typeArray.getInt(R.styleable.LineChartView_xGridStep, X_STEP)
            yMax = typeArray.getInt(R.styleable.LineChartView_yMax, Y_MAX_VALUE)
            yGridStep = typeArray.getInt(R.styleable.LineChartView_yGridStep, Y_STEP)
            typeArray.recycle()
        }
    }

    private val paint = Paint().apply {
        color = Color.BLUE
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val yAxisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val xAxisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 8f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val gridLinePaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 1f
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val dataPoints = mutableListOf<Pair<Int, Float>>()
    private val controlPoints = mutableListOf<Pair<PointF, PointF>>()
    private val path = Path()
    private var pathValid = false

    override fun setData(points: List<Pair<Int, Float>>) {
        dataPoints.clear()
        dataPoints.addAll(points)
        calculateControlPoints()
        pathValid = false
        invalidate()
    }

    private fun calculateControlPoints() {
        controlPoints.clear()
        if (dataPoints.size < 2) return

        for (i in 1 until dataPoints.size) {
            val point = dataPoints[i]
            val prevPoint = dataPoints[i - 1]

            val controlPoint1 = PointF(
                (prevPoint.first + point.first) / 2f,
                prevPoint.second
            )
            val controlPoint2 = PointF(
                (prevPoint.first + point.first) / 2f,
                point.second
            )

            controlPoints.add(controlPoint1 to controlPoint2)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val desiredWidth = 300
        val desiredHeight = 300

        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(widthSize)
            MeasureSpec.UNSPECIFIED -> desiredWidth
            else -> desiredWidth
        }

        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> desiredHeight
        }

        setMeasuredDimension(width, height)
    }

    private fun updatePath(height: Float, width: Float) {
        if (dataPoints.size < 2) return

        val xStep = width/ xMax
        val yStep = height/ yMax

        path.reset()
        path.moveTo(dataPoints[0].first * xStep, height - dataPoints[0].first * yStep)

        for (i in 1 until dataPoints.size) {
            val point = dataPoints[i]
            val controlPointPair = controlPoints[i - 1]

            path.cubicTo(
                controlPointPair.first.x * xStep, height - controlPointPair.first.y * yStep,
                controlPointPair.second.x * xStep, height - controlPointPair.second.y * yStep,
                point.first * xStep, height - point.second * yStep
            )
        }

        pathValid = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        //Рисуем оси
        path.moveTo(0f, height.toFloat() - 4)
        path.lineTo(width.toFloat(), height.toFloat() - 4)
        canvas.drawPath(path, xAxisPaint)

        path.moveTo(4f, height + 4f)
        path.lineTo(4f,0f)
        canvas.drawPath(path, yAxisPaint)

        //Рисуем сетку
        val xMax = xMax / xGridStep
        val yMax = yMax / yGridStep
        val xGridStep = width / xMax
        val yGridStep = height / yMax

        for (i in 1..xMax) {
            path.moveTo(i * xGridStep.toFloat(), 0f)
            path.lineTo(i * xGridStep.toFloat(), height.toFloat())
            canvas.drawPath(path, gridLinePaint)
        }

        for (i in 1..yMax) {
            path.moveTo(0f, i * yGridStep.toFloat())
            path.lineTo(width.toFloat(), i * yGridStep.toFloat())
            canvas.drawPath(path, gridLinePaint)
        }


        if (dataPoints.size < 2) return

        if (!pathValid) {
            updatePath(height.toFloat(), width.toFloat())
        }

        canvas.drawPath(path, paint)
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val lineCharState = state as LineCharState
        super.onRestoreInstanceState(state)
        dataPoints.clear()
        dataPoints.addAll(lineCharState.data)
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return LineCharState(superState, dataPoints)
    }

    companion object {
        const val X_MAX_VALUE = 15
        const val Y_MAX_VALUE = 1000
        const val X_STEP = 1
        const val Y_STEP = 100
    }

}