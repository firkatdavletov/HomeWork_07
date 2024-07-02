package otus.homework.customview

import android.os.Parcelable
import android.view.View.BaseSavedState

class PieChartState(
    savedState: Parcelable?,
    val data: List<Pair<String, Int>>
): BaseSavedState(savedState), Parcelable