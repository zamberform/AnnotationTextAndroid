package zamberform.jun.annotationtext.models

import android.content.res.TypedArray
import android.graphics.Color
import android.graphics.Rect
import android.util.DisplayMetrics
import android.util.TypedValue
import zamberform.jun.annotationtext.R

data class TextBody(
    var text: String? = null,
    var realTxt: String? = null,
    var font: String? = null,
    var size: Int = 0,
    var color: Int = -0xcccccd,
    var space: Int = 0,
    var rectList: MutableList<Rect> = mutableListOf()
) {
    fun initByDefault(dm: DisplayMetrics) {
        // 汉字默认 14sp
        this.size = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, dm).toInt()
        // spacing
        this.space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, dm).toInt()
    }

    fun loadTypeArray(array: TypedArray) {
        if (array.hasValue(R.styleable.AnnotationTextView_text)) {
            this.text = array.getString(R.styleable.AnnotationTextView_text)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_textFont)) {
            this.font = array.getString(R.styleable.AnnotationTextView_textFont)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_textFontSize)) {
            this.size = array.getDimensionPixelSize(R.styleable.AnnotationTextView_textFontSize, 14)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_textColor)) {
            this.color = array.getColor(R.styleable.AnnotationTextView_textColor, Color.WHITE)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_textSpace)) {
            this.space = array.getInteger(R.styleable.AnnotationTextView_textSpace, 0)
        }
    }

    fun clear() {
        rectList.removeAll { true }
    }
}