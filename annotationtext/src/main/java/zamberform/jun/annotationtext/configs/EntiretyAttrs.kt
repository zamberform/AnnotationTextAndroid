package zamberform.jun.annotationtext.configs

import android.content.res.TypedArray
import android.util.DisplayMetrics
import android.util.TypedValue
import zamberform.jun.annotationtext.R

data class EntiretyAttrs(
    var isShowAnnotation: Boolean = true,
    var space: Int = 0,
    var lineSpace: Int = 0,
    var splitStr: String = "#"
) {
    fun initByDefault(dm: DisplayMetrics) {
        // spacing
        this.space = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5f, dm).toInt()
        this.lineSpace = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, dm).toInt()
    }

    fun loadTypeArray(array: TypedArray) {
        if (array.hasValue(R.styleable.AnnotationTextView_isShowAnnotation)) {
            this.isShowAnnotation = array.getBoolean( R.styleable.AnnotationTextView_split, false)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_lineSpace)) {
            this.lineSpace = array.getDimensionPixelSize( R.styleable.AnnotationTextView_lineSpace, 0)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_annotationSpace)) {
            this.space = array.getDimensionPixelSize( R.styleable.AnnotationTextView_annotationSpace, 0)
        }
        if (array.hasValue(R.styleable.AnnotationTextView_split)) {
            this.splitStr = array.getString( R.styleable.AnnotationTextView_split)
        }

    }
}