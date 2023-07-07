package com.netmarble.nmapp.base.component

/**
 * https://jinjoochoi.github.io/post/2018/04/15/Kotlin-singletons-with-argument.html
 * argument를 받을 수 있는 Singleton
 * 다음과 같이 사용
 * companion object : SingletonHolder<{Class}, {Argument}>(::{Class})
 * Created by jinhyuk.lee on 2023/06/28
 **/
open class SingletonHolder<out T, in A>(creator: (A) -> T) {
    private var creator: ((A) -> T)? = creator
    @Volatile private var instance: T? = null

    fun getInstance(arg: A): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(arg)
                instance = created
                creator = null
                created
            }
        }
    }
}