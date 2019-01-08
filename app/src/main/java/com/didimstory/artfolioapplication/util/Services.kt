package com.didimstory.artfolioapplication.util

import com.didimstory.artfolioapplication.data.ImageData
import okhttp3.RequestBody
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Multipart
import retrofit2.http.Part


interface Services{

    @Multipart
    @POST("ajaxFileUpload")
    fun UploadImg(@Part file : MultipartBody.Part,
                 @Part("categoryIdx") categoryIdx: RequestBody): Call<ImageData>
}