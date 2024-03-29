package com.black.code.ui.example.architecture.mvc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.black.code.R
import com.black.code.ui.example.architecture.CounterModel
import kotlinx.android.synthetic.main.activity_mvc.*

/**
 * MVC에서는 Activity가 View와 Controller의 역할을 모두 담당
 * https://blog.crazzero.com/m/152
 */
class MVCActivity : AppCompatActivity() {
    // Model 초기화
    private val model by lazy { CounterModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Layout xml 적용
        setContentView(R.layout.activity_mvc)

        // 버튼을 터치했을 때 동작 설정
        countButton.setOnClickListener {
            // Model을 통해 값을 증가시키고, 증가시킨 값을 텍스트뷰에 설정
            countTextView.text = model.addCount().toString()
        }
    }
}