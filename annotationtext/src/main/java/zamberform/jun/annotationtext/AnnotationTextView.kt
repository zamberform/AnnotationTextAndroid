package zamberform.jun.annotationtext

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Pair
import android.view.View
import zamberform.jun.annotationtext.configs.EntiretyAttrs
import zamberform.jun.annotationtext.models.AnnotationBody
import zamberform.jun.annotationtext.models.TextBody
import java.util.ArrayList

class AnnotationTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private var textBody = TextBody()
    private var annotationBody = AnnotationBody()
    private var entiretyAttrs = EntiretyAttrs()

    private val paint = TextPaint(Paint.ANTI_ALIAS_FLAG or Paint.LINEAR_TEXT_FLAG)
    private var textBodyHeight: Int = 0
    private var annotationHeight: Int = 0
    private val bounds = Rect()
    private val annotationInfos = ArrayList<Pair<String, String>>()
    private var staticLayout: StaticLayout? = null

    init {
        initDefault()

        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AnnotationTextView)

            entiretyAttrs.loadTypeArray(a)

            textBody.loadTypeArray(a)
            annotationBody.loadTypeArray(a, textBody.size)

            a.recycle()
        }

        invalidate()
    }

    private fun initDefault() {
        val r = context.resources
        val dm = r.displayMetrics

        entiretyAttrs.initByDefault(dm)

        textBody.initByDefault(dm)
        annotationBody.initByDefault(dm, textBody.size)

        paint.style = Paint.Style.FILL
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        clearAll()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        collectionAnnotationInfos(entiretyAttrs.splitStr)

        calTextHeight()
        requestLayout()

        if (entiretyAttrs.isShowAnnotation && !annotationInfos.isEmpty()) {
            measureAnnotationText(widthMeasureSpec, heightMeasureSpec, false)
        } else if (!entiretyAttrs.isShowAnnotation && !TextUtils.isEmpty(textBody.realTxt)) {
            measurePlainText(widthMeasureSpec, heightMeasureSpec, false)
        } else {
            measureDefault(widthMeasureSpec, heightMeasureSpec)
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (this.isInEditMode) {
            return
        }
        if (annotationInfos.size <= 0) {
            collectionAnnotationInfos(entiretyAttrs.splitStr)
            calTextHeight()
        }
        requestLayout()

        if (entiretyAttrs.isShowAnnotation) {
            drawAnnotationAndText(canvas)
        } else {
            drawPlainText(canvas)
        }
    }

    private fun collectionAnnotationInfos(splitStr: String) {
        textBody.realTxt = textBody.text?.replace(entiretyAttrs.splitStr, "")
        annotationBody.realTxt = annotationBody.text?.replace(entiretyAttrs.splitStr, "")

        if (!entiretyAttrs.isShowAnnotation) {
            return
        }
        clearAll()

        var textInfos = textBody.text!!.split(("\\" + splitStr).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var annoTxtInfos = annotationBody.text!!.split(("\\" + splitStr).toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        textInfos.forEachIndexed { index, text ->
            if (index >= annoTxtInfos.size) {
                annotationInfos.add(Pair.create(text, ""))
            }
            else {
                annotationInfos.add(Pair.create(text, annoTxtInfos[index]))
            }

        }
    }

    private fun calTextHeight() {
        if (!TextUtils.isEmpty(textBody.realTxt)) {
            paint.textSize = textBody.size.toFloat()
            paint.getTextBounds(textBody.realTxt, 0, textBody.realTxt!!.length, bounds)
            textBodyHeight = bounds.height()
        } else {
            textBodyHeight = 0
        }

        if (!TextUtils.isEmpty(annotationBody.realTxt)) {
            paint.textSize = annotationBody.size.toFloat()
            paint.getTextBounds(annotationBody.realTxt, 0, annotationBody.realTxt!!.length, bounds)
            annotationHeight = bounds.height()
        } else {
            annotationHeight = 0
        }
    }

    private fun drawAnnotationAndText(canvas: Canvas) {
        val paddingLeft = this.paddingLeft
        val paddingTop = this.paddingTop

        annotationInfos.forEachIndexed { index, pair ->
            val text = pair.first
            val annotationTxt = pair.second
            val textRect = textBody.rectList[index]
            val annotationRect = annotationBody.rectList[index]

            paint.color = textBody.color
            paint.textSize = textBody.size.toFloat()
            textRect.offset(paddingLeft, paddingTop)
            textBody.font?.let { paint.typeface = Typeface.createFromAsset(context.assets, textBody.font) }

            val textHalfWidth = textRect.width() - getTextWidth(text, textBody.size) shr 1
            canvas.drawText(
                text,
                (textRect.left + textHalfWidth).toFloat(),
                textRect.bottom.toFloat(),
                paint
            )

            paint.color = annotationBody.color
            paint.textSize = annotationBody.size.toFloat()
            annotationRect.offset(paddingLeft, paddingTop)
            annotationBody.font?.let { paint.typeface = Typeface.createFromAsset(context.assets, annotationBody.font) }

            val annotationTextHalfWidth = annotationRect.width() - getTextWidth(annotationTxt, annotationBody.size) shr 1
            canvas.drawText(
                annotationTxt,
                (annotationRect.left + annotationTextHalfWidth).toFloat(),
                annotationRect.bottom.toFloat(),
                paint
            )
        }
    }

    private fun drawPlainText(canvas: Canvas) {
        if (staticLayout != null) {
            val paddingLeft = this.paddingLeft
            val paddingTop = this.paddingTop
            canvas.translate(paddingLeft.toFloat(), paddingTop.toFloat())

            paint.color = textBody.color
            paint.textSize = textBody.size.toFloat()
            textBody.font?.let { paint.typeface = Typeface.createFromAsset(context.assets, textBody.font) }
            staticLayout!!.draw(canvas)
        }
    }

    fun setText(text: String, annotationTxt: String) {
        this.textBody.text = text
        this.annotationBody.text = annotationTxt

        invalidate()
    }

    fun setTextFont(txtFont: String, annoFont: String) {
        this.textBody.font = txtFont
        this.annotationBody.font = annoFont

        invalidate()
    }

    fun setTextSize(px: Int, annoPxRadio: Float?) {
        if (px < 2) {
            throw IllegalArgumentException("Text size must larger than 2px")
        }
        textBody.size = px

        if (annoPxRadio == null) {
            annotationBody.size = (px * 0.8f).toInt()
        }
        else {
            annotationBody.size = (px * annoPxRadio).toInt()
        }

        if (annotationBody.size <= 0) {
            throw IllegalArgumentException("Annotation text size must larger than 1px")
        }

        invalidate()
    }

    fun setTextColor(color: Int, annoColor: Int) {
        this.textBody.color = color
        this.annotationBody.color = annoColor
        invalidate()
    }

    fun setLineSpacing(px: Int) {
        entiretyAttrs.lineSpace = px
        invalidate()
    }

    fun setAnnotationSpacing(px: Int) {
        entiretyAttrs.space = px
        invalidate()
    }

    fun setTxtSpacing(px: Int, annoPx: Int) {
        this.textBody.space = px
        this.annotationBody.space = annoPx
        invalidate()

    }

    private fun clearAll() {
        annotationInfos.clear() // clear
        this.textBody.clear()
        this.annotationBody.clear()
    }

    private fun measureAnnotationText(widthMeasureSpec: Int, heightMeasureSpec: Int, isVertical: Boolean) {
        val paddingLeft = this.paddingLeft
        val paddingRight = this.paddingRight
        val paddingTop = this.paddingTop
        val paddingBottom = this.paddingBottom

        var sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        if (isVertical) {
            sizeWidth = textBody.size + annotationBody.size + entiretyAttrs.space
        }
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        var measuredWidth = if (modeWidth == MeasureSpec.EXACTLY) sizeWidth else 0
        var measuredHeight = if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else 0

        var line = 0
        var col = 0
        var lineLength = 0
        var baseLine = 0
        var nextLine = false

        for (pair in annotationInfos) {
            val textWidth = getTextWidth(pair.first, textBody.size)
            val annoTxtWidth = getTextWidth(pair.second, annotationBody.size)

            val maxWidth = Math.max(textWidth, annoTxtWidth)

            if (nextLine) {
                line++
                col = 0
                nextLine = false
            }

            if ((lineLength + maxWidth + (if (col == 0) 0 else entiretyAttrs.space)) > sizeWidth) { // new row
                lineLength = maxWidth

                if (isVertical) {
                    baseLine += textBodyHeight
                }
                else {
                    baseLine += textBodyHeight + annotationHeight + entiretyAttrs.lineSpace
                }


                if (modeWidth != MeasureSpec.EXACTLY) {
                    measuredWidth = sizeWidth
                }

                nextLine = true
            } else {
                if (col != 0 || line != 0) { // not the first item of first row
                    lineLength += textBody.space
                }
                lineLength += maxWidth

                if (modeWidth != MeasureSpec.EXACTLY && measuredWidth < lineLength) {
                    measuredWidth = lineLength
                    if (measuredWidth > sizeWidth) {
                        measuredWidth = sizeWidth
                    }
                }
                col++
            }

            val annotationRect = Rect()
            val textRect = Rect()
            if (isVertical) {
                textRect.left = lineLength - maxWidth
                textRect.right = textRect.left + maxWidth
                textRect.top = baseLine
                textRect.bottom = textRect.top + textBodyHeight

                annotationRect.left = textRect.right
                annotationRect.right = annotationRect.left + maxWidth
                annotationRect.top = textRect.bottom - textBodyHeight
                annotationRect.bottom = annotationRect.top + annotationHeight
            }
            else {
                annotationRect.left = lineLength - maxWidth
                annotationRect.right = annotationRect.left + maxWidth
                annotationRect.top = baseLine
                annotationRect.bottom = annotationRect.top + annotationHeight

                textRect.left = lineLength - maxWidth
                textRect.right = textRect.left + maxWidth
                textRect.top = annotationRect.bottom + entiretyAttrs.space
                textRect.bottom = textRect.top + textBodyHeight
            }

            annotationBody.rectList.add(annotationRect)
            textBody.rectList.add(textRect)

        }

        if (modeHeight != MeasureSpec.EXACTLY) {
            measuredHeight = baseLine + annotationHeight + entiretyAttrs.space + textBodyHeight * 5 / 4 + entiretyAttrs.lineSpace
            if (measuredHeight > sizeHeight) {
                measuredHeight = sizeHeight
            }
        }
        setMeasuredDimension(
            measuredWidth + paddingLeft + paddingRight + 14,
            measuredHeight + paddingTop + paddingBottom + 12
        )
    }

    private fun measurePlainText(widthMeasureSpec: Int, heightMeasureSpec: Int, isVertical: Boolean) {
        val paddingLeft = this.paddingLeft
        val paddingRight = this.paddingRight
        val paddingTop = this.paddingTop
        val paddingBottom = this.paddingBottom

        // max allowed width or height
        var sizeWidth = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        if (isVertical) {
            sizeWidth = textBody.size
        }
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        // mode
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        paint.textSize = textBody.size.toFloat()
        staticLayout = StaticLayout(textBody.realTxt, paint, sizeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0f, false)

        val measuredWidth = if (modeWidth == MeasureSpec.EXACTLY)
            sizeWidth
        else
            Math.min(
                sizeWidth,
                Math.ceil(Layout.getDesiredWidth(textBody.realTxt, paint).toDouble()).toInt()
            )
        val measuredHeight = if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else staticLayout!!.height

        setMeasuredDimension(
            measuredWidth + paddingLeft + paddingRight + 14,
            measuredHeight + paddingTop + paddingBottom + 6
        )
    }

    private fun measureDefault(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // max allowed width or height
        val sizeWidth = MeasureSpec.getSize(widthMeasureSpec)
        val sizeHeight = MeasureSpec.getSize(heightMeasureSpec)

        // mode
        val modeWidth = MeasureSpec.getMode(widthMeasureSpec)
        val modeHeight = MeasureSpec.getMode(heightMeasureSpec)

        // measured width and height
        val measuredWidth = if (modeWidth == MeasureSpec.EXACTLY) sizeWidth else paddingLeft + paddingRight
        val measuredHeight = if (modeHeight == MeasureSpec.EXACTLY) sizeHeight else paddingTop + paddingBottom

        setMeasuredDimension(measuredWidth, measuredHeight)
    }


    private fun getTextWidth(text: String?, textSize: Int): Int {
        paint.textSize = textSize.toFloat()
        return Math.ceil(Layout.getDesiredWidth(text, paint).toDouble()).toInt()
    }
}