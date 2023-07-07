package com.black.code.temp

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.*
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.doOnDetach
import androidx.core.view.isVisible
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseBindingAdapter
import androidx.databinding.InverseBindingListener
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.airbnb.lottie.LottieAnimationView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.button.MaterialButton
import com.netmarble.nmapp.R

/**
 * BottomNavigationView
 * Created by jinhyuk.lee on 2022/05/31
 **/
object BottomNavigationViewBindingAdapter {
    @BindingAdapter("itemIconTintList")
    @JvmStatic
    fun setItemIconTintList(view: BottomNavigationView, @ColorRes colorResId: Int?) {
        val colorStateList = colorResId?.takeIf { it != 0 }?.let {
            ContextCompat.getColorStateList(view.context, it)
        }
        view.itemIconTintList = colorStateList
    }
}

/**
 * View
 */
object ViewBindingAdapter {
    private const val DURATION = 300L

    @BindingAdapter("fadeVisible")
    @JvmStatic
    fun setFadeVisible(view: View, isFadeVisible: Boolean) {
        with (view) {
            clearAnimation()
            animate().cancel()
            if (isFadeVisible && !isVisible) {
                alpha = 0f
                visibility = View.VISIBLE
                animate()
                    .alpha(1f)
                    .apply { duration = DURATION }
                    .setListener(null)
            } else if (!isFadeVisible && isVisible) {
                animate()
                    .alpha(0f)
                    .apply { duration = DURATION }
                    .setListener(object : AnimatorListenerAdapter() {
                        override fun onAnimationEnd(animation: Animator) {
                            this@with.visibility = View.GONE
                        }
                    })
            }
            null
        }
    }
}

/**
 * ImageView
 */
object ImageViewBindingAdapter {
    @BindingAdapter("glideUrl", "glideCircle", "glideError")
    @JvmStatic
    fun setGlideImage(view: ImageView, url: String?, isCircle: Boolean?, @DrawableRes errorDrawableResId: Int?) {
        if (url == null) {
            return
        }

        Glide.with(view)
            .load(url)
            .transition(DrawableTransitionOptions.withCrossFade())
            .run { if (isCircle == true) circleCrop() else this }
            .run { if (errorDrawableResId != null) error(errorDrawableResId) else this }
            .into(view)
    }

    @BindingAdapter("glideUrl")
    @JvmStatic
    fun setGlideImage(view: ImageView, url: String?) {
        setGlideImage(view, url, false, null)
    }

    @BindingAdapter("android:src")
    @JvmStatic
    fun setImageResource(view: ImageView, resId: Int?) {
        view.setImageResource(resId ?: return)
    }

    @BindingAdapter("drawable")
    @JvmStatic
    fun setImageDrawable(view: ImageView, drawable: Drawable?) {
        if (drawable == null) {
            return
        }
        view.setImageDrawable(drawable)
    }

    @BindingAdapter("clipToOutline")
    @JvmStatic
    fun setClipToOutline(view: ImageView, isEnable: Boolean) {
        view.clipToOutline = isEnable
    }

    @BindingAdapter("cornerRadiusDp")
    @JvmStatic
    fun setCornerRadius(view: ImageView, radiusDp: Float) {
        if (radiusDp <= 0) {
            view.background = null
            view.clipToOutline = false
            return
        }

        view.background = GradientDrawable().apply {
            cornerRadius = ViewUtil.dpToPx(view.context, radiusDp).toFloat()
            colors = intArrayOf(0xFFFFFF, 0xFFFFFF)
        }
        view.clipToOutline = true
    }
}

/**
 * ViewPager
 */
object ViewPagerBindingAdapter {
    interface OnPagerScrollStateChangedListener {
        fun onPagerScrollStateChanged(state: Int)
    }

    @BindingAdapter("currentItemSmooth")
    @JvmStatic fun setCurrentItemSmooth(view: ViewPager2, newValue: Int) {
        if (view.currentItem != newValue) {
            view.setCurrentItem(newValue, true)
        }
    }

    @InverseBindingAdapter(attribute = "currentItemSmooth")
    @JvmStatic fun getCurrentItemSmooth(view: ViewPager2) : Int {
        return view.currentItem
    }

    @BindingAdapter("currentItemSmoothAttrChanged")
    @JvmStatic fun setCurrentItemSmoothListener(
        view: ViewPager2,
        attrChanged : InverseBindingListener
    ) {
        view.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                attrChanged.onChange()
            }
        })
    }

    @BindingAdapter("currentItem")
    @JvmStatic fun setCurrentItem(view: ViewPager2, newValue: Int) {
        if (view.currentItem != newValue) {
            view.setCurrentItem(newValue, false)
        }
    }

    @InverseBindingAdapter(attribute = "currentItem")
    @JvmStatic fun getCurrentItem(view: ViewPager2) : Int {
        return view.currentItem
    }

    @BindingAdapter("currentItemAttrChanged")
    @JvmStatic fun setCurrentItemListener(
        view: ViewPager2,
        attrChanged : InverseBindingListener
    ) {
        view.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                attrChanged.onChange()
            }
        })
    }

    @BindingAdapter("onPageScrollStateChanged")
    @JvmStatic fun setOnPagerScrollStateChangedListener(view: ViewPager2, listener: OnPagerScrollStateChangedListener?) {
        if (listener == null) {
            Log.w("onPageScrollStateChanged is null")
            return
        }

        view.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {
            override fun onPageScrollStateChanged(state: Int) {
                listener.onPagerScrollStateChanged(state)
            }
        })
    }

    @BindingAdapter("adapter")
    @JvmStatic
    fun setAdapter(view: ViewPager2, adapter: RecyclerView.Adapter<*>) {
        view.adapter = adapter
    }

    @BindingAdapter("offscreenPageLimit")
    @JvmStatic
    fun setOffscreenPageLimit(view: ViewPager2, limit: Int?) {
        view.offscreenPageLimit = limit ?: 1
    }

    @BindingAdapter("pageMarginDp")
    @JvmStatic
    fun setPageMargin(view: ViewPager2, @Dimension marginDp: Float?) {
        view.setPageTransformer(MarginPageTransformer(ViewUtil.dpToPx(view.context, marginDp ?: 0f)))
    }

    @BindingAdapter("overScrollMode")
    @JvmStatic
    fun setOverScrollMode(view: ViewPager2, overScrollMode: Int) {
        view.getChildAt(0).overScrollMode = overScrollMode
    }

    @BindingAdapter("userInputEnabled")
    @JvmStatic
    fun setUserInputEnabled(view: ViewPager2, enabled: Boolean) {
        view.isUserInputEnabled = enabled
    }
}

/**
 * EditText
 */
object EditTextBindingAdapter {
    @BindingAdapter("onFocusChange")
    @JvmStatic
    fun setOnFocusChangeListener(view: EditText, onFocusChangeListener: View.OnFocusChangeListener?) {
        view.onFocusChangeListener = onFocusChangeListener
    }

    @BindingAdapter("errorRes")
    @JvmStatic
    fun setErrorResId(view: EditText, @StringRes errorResId: Int?) {
        view.error = errorResId?.takeIf { it != 0 }?.let { view.context.getString(it) }
    }
}

/**
 * TextView
 */
object TextViewBindingAdapter {
    @BindingAdapter("textRes")
    @JvmStatic
    fun setTextRes(view: TextView, @StringRes resId: Int) {
        view.setText(resId.takeIf { it != 0 } ?: return)
    }
}

/**
 * LottieAnimationView
 */
object LottieAnimationViewBindingAdapter {
    @BindingAdapter("lottieRawRes")
    @JvmStatic
    fun setLottieRawResId(view: LottieAnimationView, @RawRes resId: Int) {
        view.setAnimation(resId.takeIf { it != 0 } ?: return)
    }
}

/**
 * ToolBar
 */
object ToolBarBindingAdapter {
    /**
     * NavController를 설정하여 navigation.xml에 설정되어 있는 Title(label) 노출 및 뒤로 돌아갈 수 있는 경우 백버튼 노출
     */
    @BindingAdapter("navController")
    @JvmStatic
    fun setNavController(view: Toolbar, navController: NavController?) {
        navController ?: return

        val onDestinationChanged = NavController.OnDestinationChangedListener { _, destination, _ ->
            view.title = destination.label
        }

        navController.addOnDestinationChangedListener(onDestinationChanged)

        view.doOnDetach {
            navController.removeOnDestinationChangedListener(onDestinationChanged)
        }

        if (navController.graph.findStartDestination().id != navController.currentDestination?.id) {
            view.setNavigationIcon(R.drawable.ic_tool_bar_back)
            view.setNavigationOnClickListener { navController.navigateUp() }
        }
    }
}

/**
 * MaterialButton
 */
object MaterialButtonBindingAdapter {
    @BindingAdapter(value = ["progress", "progressWithDisabled"], requireAll = false)
    @JvmStatic
    fun setProgress(view: MaterialButton, progress: Boolean, progressWithDisabled: Boolean?) {
        if (progress) {
            view.icon = CircularProgressDrawable(view.context).apply {
                setStyle(CircularProgressDrawable.DEFAULT)
                setColorSchemeColors(view.iconTint.defaultColor)
                callback = object : Drawable.Callback {
                    override fun invalidateDrawable(who: Drawable) {
                        view.invalidate()
                    }
                    override fun scheduleDrawable(who: Drawable, what: Runnable, `when`: Long) {}
                    override fun unscheduleDrawable(who: Drawable, what: Runnable) {}
                }
                start()
            }
        } else {
            view.icon = null
        }

        if (progressWithDisabled != false) {
            view.isEnabled = !progress
        }
    }
}

object RecyclerViewBindingAdapter {
    @BindingAdapter(value = ["adapter", "itemList"], requireAll = false)
    @JvmStatic
    fun <T> setItemList(view: RecyclerView, adapter: ListAdapter<T, *>?, list: List<T>?) {
        view.adapter = adapter ?: return
        adapter.submitList(list ?: return)
    }

    /**
     * [DividerItemDecoration] 기반으로 재구성
     */
    @BindingAdapter(value = ["dividerHeight", "dividerPaddingHorizontal", "dividerPaddingVertical", "dividerColor"], requireAll = false)
    @JvmStatic
    fun setDivider(view: RecyclerView, dividerHeight: Float?, dividerPaddingHorizontal: Float?, dividerPaddingVertical: Float?, @ColorInt dividerColor: Int?) {
        val decoration = object: ItemDecoration() {
            private val paint = Paint()
            init {
                paint.color = dividerColor ?: Color.parseColor("#FFFFFF")
            }

            override fun onDraw(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
                parent.layoutManager ?: return

                canvas.save()
                val left: Float
                val right: Float
                if (parent.clipToPadding) {
                    left = parent.paddingLeft + (dividerPaddingHorizontal ?: 0f)
                    right = parent.width - parent.paddingRight - (dividerPaddingHorizontal ?: 0f)
                    canvas.clipRect(
                        left.toInt(),
                        parent.paddingTop,
                        right.toInt(),
                        parent.height - parent.paddingBottom
                    )
                } else {
                    left = 0f
                    right = parent.width.toFloat()
                }

                // childCount - 1 : 마지막 Divider는 그리지 않음
                for (i in 0 until parent.childCount - 1) {
                    val child = parent.getChildAt(i)
                    val mBounds = Rect()

                    // 해당 아이템의 AlignBottom에 Divider를 그림
                    parent.getDecoratedBoundsWithMargins(child, mBounds)
                    val bottom = mBounds.bottom + child.translationY
                    val top: Float = bottom - (dividerHeight ?: 0f)
                    canvas.drawRect(left, top, right, bottom, paint)
                }
                canvas.restore()
            }

            /**
             * 아이템의 여백 설정
             */
            override fun getItemOffsets(
                outRect: Rect,
                view: View,
                parent: RecyclerView,
                state: RecyclerView.State
            ) {
                val position = parent.getChildAdapterPosition(view)

                // 첫번째 아이템 상단엔 여백을 추가하지 않음
                val top = if (position == 0) {
                    0
                } else {
                    dividerPaddingVertical?.toInt() ?: 0
                }

                // 마지막 아이템 하단엔 여백을 추가하지 않음
                val bottom = if (position == state.itemCount - 1) {
                    0
                } else {
                    // Divider가 AlignBottom에 그려지므로 Divider가 그려질 영역만큼 여백을 더 추가해줌
                    (dividerPaddingVertical?.toInt() ?: 0) + (dividerHeight?.toInt() ?: 0)
                }
                outRect.set(0, top, 0, bottom)
            }
        }
        view.addItemDecoration(decoration)
    }
}