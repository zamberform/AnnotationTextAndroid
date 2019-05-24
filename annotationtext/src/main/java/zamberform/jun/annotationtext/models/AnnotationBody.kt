package zamberform.jun.annotationtext.models

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import zamberform.jun.annotationtext.R

data class AnnotationBody(
    var text: String? = null,
    var realTxt: String? = null,
    var font: String? = null,
    var size: Int = 0,
    var color: Int = -0x666667,
    var space: Int = 0,
    var rectList: MutableList<Rect> = mutableListOf()
) {
    fun initByDefault(dm: DisplayMetrics, size: Int) {
        // 汉字默认 14sp
        this.size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14 * 0.8f, dm).toInt()
        // spacing
        this.space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f / 2, dm).toInt()
    }

    fun loadTypeArray(array: TypedArray, size: Int) {
        if (array.hasValue(R.styleable.AnnotationTextView_annotationText)) {
            this.text = array.getString(R.styleable.AnnotationTextView_annotationText)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_annotationFont)) {
            this.font = array.getString(R.styleable.AnnotationTextView_annotationFont)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_annotationColor)) {
            this.color = array.getColor(R.styleable.AnnotationTextView_annotationColor, Color.WHITE)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_annotationSpace)) {
            this.color = array.getInteger(R.styleable.AnnotationTextView_annotationSpace, 0)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_annotationSizeRadio)) {
            this.size = (array.getFloat(R.styleable.AnnotationTextView_annotationSizeRadio, 0.8f) * size).toInt()
        }
    }

    fun clear() {
        rectList.removeAll { true }
    }
}