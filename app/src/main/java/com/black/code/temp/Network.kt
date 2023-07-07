package com.black.code.temp

import android.annotation.SuppressLint
import com.google.gson.Gson
import com.netmarble.nmapp.BuildConfig
import com.netmarble.nmapp.model.data.NMResult
import com.netmarble.nmapp.model.data.NetworkResult
import com.netmarble.nmapp.util.Log
import kotlinx.coroutines.*
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okio.Timeout
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.net.SocketTimeoutException
import java.security.SecureRandom
import java.security.cert.X509Certificate
import java.util.concurrent.CancellationException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

/**
 * Retrofit 래핑 클래스
 * Created by jinhyuk.lee on 2023/06/13
 */
class Network<T> private constructor(private val retrofit: Retrofit.Builder,
                                     private var client: OkHttpClient.Builder,
                                     private val serviceCls: Class<T>) {
    companion object {
        private const val CONNECT_TIMEOUT_SEC = 30L
        private const val READ_TIMEOUT_SEC = 30L
        private const val WRITE_TIMEOUT_SEC = 30L

        internal const val STATUS_CODE_INVALID_URL = 9999

        // OkHttpClient 기본 설정
        private val defaultClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(WRITE_TIMEOUT_SEC, TimeUnit.SECONDS)
            .cache(null) // 캐싱하지 않음
            .retryOnConnectionFailure(false)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .also {
                if (BuildConfig.DEBUG) {
                    // 피들러 테스트를 위한 인증서 허용 코드
                    try {
                        @SuppressLint("CustomX509TrustManager")
                        val trustAllCerts:  Array<TrustManager> = arrayOf(object : X509TrustManager {
                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?){}
                            @SuppressLint("TrustAllX509TrustManager")
                            override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
                            override fun getAcceptedIssuers(): Array<X509Certificate>  = arrayOf()
                        })

                        val  sslContext = SSLContext.getInstance("SSL")
                        sslContext.init(null, trustAllCerts, SecureRandom())

                        val sslSocketFactory = sslContext.socketFactory
                        if (trustAllCerts.isNotEmpty() &&  trustAllCerts.first() is X509TrustManager) {
                            it.sslSocketFactory(sslSocketFactory, trustAllCerts.first() as X509TrustManager)
                            it.hostnameVerifier { _, _ -> true }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

        // Gson 설정
        private val gson = Gson().newBuilder()
            .setPrettyPrinting()
            .create()

        /**
         * 네트워크 호출을 위한 Service 객체 반환
         */
        fun <T> service(baseUrl: String, serviceCls: Class<T>) : T {
            return builder(baseUrl, serviceCls).build()
        }

        /**
         * Retrofit, OkHttpClient 등 커스텀할 수 있는 Network 객체 반환
         */
        fun <T> builder(baseUrl: String, serviceCls: Class<T>) : Network<T> {
            lateinit var client: OkHttpClient.Builder
            val retrofit = Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create(gson))
                // Response를 NetworkResult로 컨버팅해주는 CallAdapter 설정
                .addCallAdapterFactory(NetmarbleCallAdapter.Factory)

            try {
                // baseUrl 마지막에 "/"가 누락되는 경우 Exception이 발생하므로 추가해줌
                retrofit.baseUrl("$baseUrl/")
                client = defaultClient
            }
            // Url이 비정상적인 경우 Exception 발생
            // 이 경우 Intercepter를 사용하여 API 호출 시 무조건 에러 response를 반환하도록 설정
            catch(e: Exception) {
                e.printStackTrace()

                // exception이 발생하지 않도록 임의의 url 설정
                retrofit.baseUrl("https://netmarble.net/")
                client = defaultClient
                    .build()
                    .newBuilder()
                    .addInterceptor(object: Interceptor {
                        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                            return chain.proceed(chain.request())
                                .newBuilder()
                                .code(STATUS_CODE_INVALID_URL)
                                .message(e.message ?: "Exception")
                                .build()
                        }
                    })
            }
            return Network(retrofit, client, serviceCls)
        }
    }

    /**
     * OkHttpClient 커스텀
     */
    fun client(override: OkHttpClient.Builder.() -> Unit) : Network<T> = also {
        client = client.build().newBuilder()
        override(client)
    }

    /**
     * Retrofit 커스텀
     */
    fun retrofit(override: Retrofit.Builder.() -> Unit) : Network<T> = also {
        override(retrofit)
    }

    /**
     * Service 객체 반환
     */
    fun build() : T {
        return retrofit.client(client.build())
            .build()
            .create(serviceCls)
    }
}

/**
 * https://proandroiddev.com/modeling-retrofit-responses-with-sealed-classes-and-coroutines-9d6302077dfe
 * https://medium.com/shdev/retrofit%EC%97%90-calladapter%EB%A5%BC-%EC%A0%81%EC%9A%A9%ED%95%98%EB%8A%94-%EB%B2%95-853652179b5b
 */
class NetmarbleCall<T>(private val proxy: Call<T>, private val type: Type) : Call<NetworkResult<T>> {
    /**
     * 참고 : Service에 suspend fun으로 구현된 함수는 기본적으로 enqueue로 동작함
     */
    override fun enqueue(callback: Callback<NetworkResult<T>>) {
        proxy.enqueue(object: Callback<T> {
            override fun onResponse(call: Call<T>, response: Response<T>) {
                callback.onResponse(this@NetmarbleCall, Response.success(onResponse(response, type)))
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                callback.onResponse(this@NetmarbleCall, Response.success(onFailed(t)))
            }
        })
    }

    override fun execute(): Response<NetworkResult<T>> {
        val networkResult = try {
            onResponse(proxy.execute(), type)
        } catch (t: Throwable) {
            onFailed(t)
        }
        return Response.success(networkResult)
    }

    /**
     * 서버에서 Response를 받은 경우 처리(statusCode와 상관없음)
     */
    private fun onResponse(response: Response<T>, type: Type) : NetworkResult<T> {
        // statusCode 200 ~ 300을 벗어난 경우 body가 null이고, errorBody에 데이터가 있음
        val data : T? = response.body() ?: parse(response.errorBody()?.string(), type)
        return if (data != null) {
            NetworkResult(NMResult.CODE_SUCCESS, NMResult.MESSAGE_SUCCESS, data, response.code())
        } else if (response.code() == Network.STATUS_CODE_INVALID_URL) {
            // -1xxx007
            NetworkResult(NMResult.CODE_INVALID_URL, response.message(), null, response.code())
        } else {
            // -1xxx009
            NetworkResult(NMResult.CODE_INVALID_RESPONSE, "Response is null", null, response.code())
        }
    }

    /**
     * 네트워크 요청 실패 시 처리
     */
    private fun onFailed(t: Throwable) : NetworkResult<T> {
        return when(t) {
            // 타임아웃
            is SocketTimeoutException -> {
                t.printStackTrace()
                val errorMessage = t.message ?: "Timeout"
                // -1xxx003
                NetworkResult(NMResult.CODE_TIMEOUT, errorMessage, null)
            }

            // 네트워크 에러
            is HttpException -> {
                t.printStackTrace()
                val errorMessage = t.message ?: "Network error"
                // -1xxx004
                NetworkResult(NMResult.CODE_NETWORK_ERROR, errorMessage, null)
            }

            // job.cancel
            is CancellationException -> {
                t.printStackTrace()
                // -1xxx002
                NetworkResult(NMResult.CODE_CANCELED, NMResult.MESSAGE_CANCELED, null)
            }

            // 그 외 exception
            else -> {
                t.printStackTrace()
                val errorMessage = t.message ?: "Unknown network error"
                // -1xxx004
                NetworkResult(NMResult.CODE_NETWORK_ERROR, errorMessage, null)
            }
        }
    }

    /**
     * Gson을 사용하여 String -> T로 변환
     */
    private fun parse(response: String?, type: Type) : T? {
        if (response == null) {
            Log.w("response is null")
            return null
        }
        // https://stackoverflow.com/questions/32444863/google-gson-linkedtreemap-class-cast-to-myclass
        // 제네릭으로 처리 시 클래스 정보가 손실되므로 type을 직접 전달하여 파싱
        // 참고 : Gson에서 exception 발생 시 try catch로 잡히지 않고 코루틴 invokeOnCompletion로 잡힘
        //       혹시 모를 상황을 대비하여 try catch 문 작성
        return try {
            Gson().fromJson(response, type)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    override fun clone(): Call<NetworkResult<T>> = NetmarbleCall(proxy, type)
    override fun isExecuted(): Boolean = proxy.isExecuted
    override fun cancel() = proxy.cancel()
    override fun isCanceled(): Boolean = proxy.isCanceled
    override fun request(): Request = proxy.request()
    override fun timeout(): Timeout = proxy.timeout()
}

class NetmarbleCallAdapter(private val resultType: Type):
    CallAdapter<Type, Call<NetworkResult<Type>>> {
    override fun responseType(): Type {
        return resultType
    }

    override fun adapt(call: Call<Type>): Call<NetworkResult<Type>> {
        return NetmarbleCall(call, resultType)
    }

    object Factory : CallAdapter.Factory() {
        override fun get(
            returnType: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit
        ): CallAdapter<*, *>? {
            // Service의 Return 클래스가 Call인지 확인
            // 참고 : suspend fun인 경우 내부에서 Call로 감싸짐
            if (getRawType(returnType) != Call::class.java) {
                return null
            }

            // Call<#> -> #이 NetworkResult인지 확인
            val callType = getParameterUpperBound(0, returnType as ParameterizedType)
            if (getRawType(callType) != NetworkResult::class.java) {
                return null
            }

            // NetworkResult<#> -> # 클래스 타입 획득하여 NetmarbleCallAdapter 생성
            val resultType = getParameterUpperBound(0, callType as ParameterizedType)
            return NetmarbleCallAdapter(resultType)
        }
    }
}