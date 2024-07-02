package otus.homework.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.os.Parcelable
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.min
import kotlin.math.sqrt


class PieChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
): View(context, attrs), PieChartViewInterface {
    private var data: List<Pair<String, Int>> = listOf()
    private var sectorAngles: List<Pair<Float, Float>> = listOf()
    private var sum = 0

    private var onClickListener: OnClickListener? = null
    private var selectedCategory: String? = null

    private val rectF = RectF()
    private val bounds = Rect()
    private val arcPaint = Paint().apply {
        style = Paint.Style.STROKE
    }
    private val textPaint = Paint().apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private var backgroundColor = DEFAULT_BACKGROUND_COLOR
    private var strokeWidth = DEFAULT_STROKE_WIDTH
    private var sectorPadding = DEFAULT_SECTOR_PADDING
    private var circlePadding = DEFAULT_CIRCLE_PADDING
    private var startAngle = DEFAULT_START_ANGLE
    private var textSize = DEFAULT_TEXT_SIZE

    private var radius: Float? = null

    init {
        if (attrs != null) {
            val typeArray = context.obtainStyledAttributes(attrs, R.styleable.PieChartView)
            backgroundColor = typeArray.getColor(R.styleable.PieChartView_backgroundColor, DEFAULT_BACKGROUND_COLOR)

            typeArray.recycle()
        }
    }

    override fun getSelectedCategory(): String? {
        return selectedCategory
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val wMode = MeasureSpec.getMode(widthMeasureSpec)
        val wSize = MeasureSpec.getSize(widthMeasureSpec)
        val hMode = MeasureSpec.getMode(heightMeasureSpec)
        val hSize = MeasureSpec.getSize(heightMeasureSpec)

        val measuredWidth = when (wMode) {
            MeasureSpec.EXACTLY -> {
                wSize
            }
            MeasureSpec.AT_MOST -> {
                wSize
            }
            MeasureSpec.UNSPECIFIED -> {
                wSize
            }
            else -> wSize
        }

        val measuredHeight = when (hMode) {
            MeasureSpec.EXACTLY -> {
                hSize
            }
            MeasureSpec.AT_MOST -> {
                hSize
            }
            MeasureSpec.UNSPECIFIED -> {
                hSize
            }
            else -> {
                hSize
            }
        }

        setMeasuredDimension(measuredWidth, measuredHeight)
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isInEditMode) {
            setDataChart(sampleData)
        }

        val sectorCount = data.size

        val midWidth = width/2
        val midHeight = height/2

        val radius = min(width, height)/2.0f - circlePadding
        this.radius = radius

        arcPaint.strokeWidth = strokeWidth.toFloat()

        val left = midWidth - radius + strokeWidth/2
        val top = midHeight - radius + strokeWidth/2
        val right = midWidth + radius - strokeWidth/2
        val bottom = midHeight + radius - strokeWidth/2

        rectF.set(left, top, right, bottom)

        var index = 0

        repeat(sectorCount) {
            val colorIndex = if (index < 9) index else index % 10

            arcPaint.setColor(colors[colorIndex])

            canvas.drawArc(
                rectF,
                sectorAngles[index].first,
                sectorAngles[index].second,
                false,
                arcPaint
            )
            index++
        }

        rectF.setEmpty()

        val mText = "$sum ₽"

        textPaint.textSize = textSize.toFloat()
        textPaint.getTextBounds(mText, 0, mText.length, bounds)

        val textWidth = textPaint.measureText(mText)
        val textY = midHeight.toFloat() + bounds.height()/2
        val textX = midWidth - textWidth/2

        canvas.drawText(mText, textX, textY, textPaint)
    }

    private val gestureDetector = GestureDetector(context, object : SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent): Boolean {
            val xA = e.x
            val yA = e.y

            val xB = width/2
            val yB = height/2

            val dx = xA - xB
            val dy = yA - yB

            val dAB = sqrt(dx*dx + dy*dy)

            if (radius == null) throw Exception("radius is null")

            if (dAB < radius!! && dAB > (radius!! - strokeWidth)) {
                var angle = (atan2(dy, dx) * 180) / PI
                angle = if (angle < 0) {
                    360 + angle
                } else {
                    angle
                }
                Log.d("PieChartView", "Angle: $angle")

                val index = sectorAngles.indexOfFirst {
                    val startAngle = it.first
                    val endAngle = startAngle + it.second
                    angle in startAngle..endAngle

                }
                if (index != -1) {
                    val category = data[index].first
                    selectedCategory = category
                }
            }
            return true
        }
    })

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            gestureDetector.onTouchEvent(event)
            performClick()
        }
        return true
    }

    override fun performClick(): Boolean {
        onClickListener?.onClick(this)
        return super.performClick()
    }

    override fun setOnClickListener(l: OnClickListener?) {
        onClickListener = l
    }


    override fun setDataChart(data: List<Pair<String, Int>>) {
        this.data = data
        calculateAngles()
    }

    private fun calculateAngles() {
        sum = if (data.isNotEmpty()) { data.sumOf { it.second } } else 0

        var startAngle = this.startAngle.toFloat()
        var sweepAngle = 0f

        sectorAngles = data.map { item ->
            startAngle += sweepAngle
            sweepAngle = item.second / (sum / 100.0f) * 3.6f
            startAngle to sweepAngle - sectorPadding
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        val pieChartState = state as PieChartState
        super.onRestoreInstanceState(state)
        data = pieChartState.data
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState =  super.onSaveInstanceState()
        return PieChartState(superState, data)
    }

    companion object {
        const val DEFAULT_STROKE_WIDTH = 150
        const val DEFAULT_SECTOR_PADDING = 1
        const val DEFAULT_CIRCLE_PADDING = 15
        const val DEFAULT_START_ANGLE = 0
        const val DEFAULT_TEXT_SIZE = 96
        const val DEFAULT_BACKGROUND_COLOR = 0x277DA1

        val colors = arrayOf(
            Color.parseColor("#F94144"),
            Color.parseColor("#F3722C"),
            Color.parseColor("#F8961E"),
            Color.parseColor("#F9844A"),
            Color.parseColor("#F9C74F"),
            Color.parseColor("#90BE6D"),
            Color.parseColor("#43AA8B"),
            Color.parseColor("#4D908E"),
            Color.parseColor("#577590"),
            Color.parseColor("#277DA1")
        )

        val sampleData = listOf(
            "Apple" to 1580,
            "Orange" to 499,
            "Pineapple" to 290,
            "Carrot" to 541,
            "Potato" to 600,
            "Cucumber" to 141,
            "Bread" to 369
        )
    }
}