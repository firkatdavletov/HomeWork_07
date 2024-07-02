package otus.homework.customview

import android.os.Parcelable
import android.view.View.BaseSavedState

class LineCharState(
    savedState: Parcelable?,
    val data: List<Pair<Int, Float>>
): BaseSavedState(savedState), Parcelable