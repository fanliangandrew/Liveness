package com.example.zainfo.liveness;


/**
 * Created by zasx-fanliang on 2017/9/11.
 */


import android.content.Context;
import android.graphics.Bitmap;
import android.provider.Settings;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by zasx-fanliang on 2017/7/31.
 */

public class RequestController {

    private static final String Tag = "VolleyTag";

    private RequestQueue requestQueue ;

    private static RequestController mInstance;

    private ImageLoader mImageLoader;

    private Context mContext;

    private RequestController(Context context){
        mContext = context;

    }

    public static RequestController getInstance(Context context){
        if(mInstance == null){
            synchronized (RequestController.class){
                if(mInstance == null){
                    mInstance = new RequestController(context);
                }
            }
        }
        return  mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null){
            synchronized(RequestController.class)
            {
                if (requestQueue == null){
                    requestQueue = Volley.newRequestQueue(mContext);
                }
            }
        }
        return requestQueue;
    }


    public <T> void addToRequestQueue(Request<T> req,String tag){
        req.setTag(TextUtils.isEmpty(tag)?Tag:tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req){
        req.setTag(Tag);
        getRequestQueue().add(req);
    }

    public void cancelPendingRequest(Object tag){
        if(requestQueue != null){
            requestQueue.cancelAll(tag);
        }
    }



    public static void  main(String args[]){
    }
}
