package cn.zjamss.pp.net;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

/**
 * @Program: PlainPicture
 * @Description:
 * @Author: ZJamss
 * @Create: 2022-03-07 16:05
 **/
public class PictureService {

    public static Service service = new Retrofit.Builder()
            .baseUrl("http://192.168.15.100:9003")
            .addConverterFactory(GsonConverterFactory.create())
            .build().create(Service.class);

    public interface Service {
        @GET("/getPic")
        Call<ResponseBody> getPic();

        @Multipart
        @POST("/uploadPic")
        Call<ResponseBody> uploadPic(@Part MultipartBody.Part part);
    }
}
