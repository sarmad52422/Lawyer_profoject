package com.fyp.lawyer_project.utils;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class Utilities {
    private static String FCM_URL = "https://fcm.googleapis.com/fcm/send";
    private static final String SERVER_KEY = "AAAA9HOqQm8:APA91bHxl_-0bOXZnDrgXlRRoCOwyGfjQbHqDx4GeG8rcr8AguFRQnS_rb2W2tnOQUAfI7a69CaPPEe0exzZbvNzCIxQJ8tvqKAGGxt0i6b-QZkNIarvjH48mvof-4P-bt8b_xXxlm0K";

    public enum DAYS {

        MONDAY("Monday"),
        TUESDAY("Tuesday"),
        WEDNESDAY("Wednesday"),
        THURSDAY("Thursday"),
        FRIDAY("Friday"),
        SATURDAY("Saturday"),
        SUNDAY("Sunday");
        private final String day;

        private DAYS(String day) {
            this.day = day;
        }
    }

    public static void sendFCMPush(Context context, String receiverToken,String titleMsg,String msgBody) {
        JSONObject obj = null;
        JSONObject objData = null;
        JSONObject dataobjData = null;
        try {
            Log.e("Title gya heheh","Title he = "+titleMsg);
            obj = new JSONObject();
            objData = new JSONObject();
            objData.put("body", msgBody);
            objData.put("title",titleMsg);
            dataobjData = new JSONObject();
            dataobjData.put("text", msgBody);
            dataobjData.put("title", titleMsg);
            obj.put("to", receiverToken);
            obj.put("notification", objData);
            obj.put("data", dataobjData);
        } catch (JSONException ex) {

        }
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, FCM_URL, obj, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.e("Response Sucess", "hehehehehe");
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Responces Failur", "Erorr hohoh" + error.networkResponse);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("Authorization", "key=" + SERVER_KEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        RequestQueue requestQueue = Volley.newRequestQueue(context);
        int socketTimeout = 1000 * 60;

        requestQueue.add(request);
    }

}
