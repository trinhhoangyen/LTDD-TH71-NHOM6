package com.example.eaten;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.eaten.DTO.Account;
import com.example.eaten.DTO.Card;
import com.example.eaten.myadapter.CardAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class AccInfoActivity extends AppCompatActivity {
    ListView lv;
    List<Card> cardList;
    ImageView acc_info;
    TextView displayname_info, email_info, gender_info, year_info, quantity;
    int temp;
    Dialog myDialog;

    private static final String JSON_URL = "https://eatenapi.azurewebsites.net/api/Posts/get-all-post-info";
    private static final String JSON_URLACC = "https://eatenapi.azurewebsites.net/api/Accounts/get-all";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_info);
        myDialog = new Dialog(this);

        //Hide ActionBar
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        //Khai báo BottomNavigation
        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.nav_view);
        //Khi chuyển Activity, select ngay nút vừa chọn
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem1 = menu.getItem(4 );
        menuItem1.setChecked(true);

        // Xử lý sự kiện click chọn
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        Intent in1 = new Intent(AccInfoActivity.this, HomeActivity.class);
                        startActivity(in1);
                        Animatoo.animateSlideRight(AccInfoActivity.this);
                        break;
                    case R.id.navigation_videos:
                        Intent in2 = new Intent(AccInfoActivity.this, VideosActivity.class);
                        startActivity(in2);
                        Animatoo.animateSlideRight(AccInfoActivity.this);
                        break;
                    case R.id.navigation_post:
                        Intent in3 = new Intent(AccInfoActivity.this, PostActivity.class);
                        startActivity(in3);
                        Animatoo.animateSlideUp(AccInfoActivity.this);
                        break;
                    case R.id.navigation_notifications:
                        Intent in4 = new Intent(AccInfoActivity.this, NotificationsActivity.class);
                        startActivity(in4);
                        Animatoo.animateSlideRight(AccInfoActivity.this);
                        break;
                    case R.id.navigation_profile:
                        break;
                }
                return true;
            }

        });

        SharedPreferences sp = getSharedPreferences("Save_ID_Acc", MODE_PRIVATE);
        //Đọc dữ liệu
        temp = sp.getInt("accID", -1); //X là kiểu dữ liệu

        mapping();
        loadacc();
        loadlv();
        loadPostQuantity();

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Card card_sub = (Card) cardList.get(position);
                Intent sub = new Intent(view.getContext(), HomeSubActivity.class);
                //sub.putExtra("accID", temp); //chuyển accountId sang HomeSubActivity
                sub.putExtra("card", cardList.get(position).getPostId());
                startActivity(sub);
            }
        });
    }
    private void loadacc(){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, JSON_URLACC,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray cardArray = jsonObject.getJSONArray("data");
                            //Log.e("abc","e e e e e");
                            for(int i = 0 ; i < cardArray.length(); i++) {
                                JSONObject obj = cardArray.getJSONObject(i);
                                if (temp != -1 && i+1 == temp) {
                                    Account account = new Account(
                                            obj.getInt("accountId"),
                                            obj.getInt("yearOfBirth"),
                                            obj.getString("email"),
                                            obj.getString("password"),
                                            obj.getString("displayName"),
                                            obj.getString("avatarURL"),
                                            obj.getString("gender")
                                    );
                                    Picasso.with(AccInfoActivity.this)
                                            .load(account.getAvatar())
                                            //.resize(Home_sub.this.getWindow().getDecorView().getWidth(), 300)
                                            //.centerInside()
                                            .fit()
                                            .centerCrop()
                                            .into(acc_info);
                                    displayname_info.setText(account.getDisplayName());
                                    email_info.setText(account.getEmail());
                                    gender_info.append(account.getGender());
                                    year_info.append(String.valueOf(account.getYearOfBirth()));
                                }
                            }
                        }catch (JSONException e){
                            e.printStackTrace();
                            //Log.e("abc","e e e e e");
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e("abc","e e e e e");
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        //progressBar.setVisibility(View.INVISIBLE);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void loadlv(){
        cardList = new ArrayList<>();
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar_info);
        progressBar.setVisibility(View.VISIBLE);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(View.INVISIBLE);

                        try{
                            JSONObject jsonObject = new JSONObject(response);
                            JSONArray cardArray = jsonObject.getJSONArray("data");
                            //Log.e("abc","e e e e e");
                            for(int i = 0 ; i < cardArray.length(); i++){
                                JSONObject obj = cardArray.getJSONObject(i);
                                if (temp != -1 && obj.getInt("accountId") == temp) {
                                    Card card = new Card(
                                            obj.getInt("postId"),
                                            obj.getInt("accountId"),
                                            obj.getString("postName"),
                                            obj.getString("content"),
                                            obj.getString("address"),
                                            obj.getString("displayName"),
                                            obj.getString("pictureURL"),
                                            //null,
                                            obj.getInt("reactQuantity")
                                    );
                                    //Log.e("abc","e e e e e");
                                    cardList.add(card);
                                }
                            }
                            CardAdapter adapter = new CardAdapter(cardList, getApplicationContext());
                            lv.setAdapter(adapter);
                        }catch (JSONException e){
                            e.printStackTrace();
                            //Log.e("abc","e e e e e");
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e("abc","e e e e e");
                        Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);
    }
    private void mapping(){
        acc_info = (ImageView) findViewById(R.id.id_acc_info);
        displayname_info = (TextView) findViewById(R.id.id_displayname_info);
        email_info = (TextView) findViewById(R.id.id_email_info);
        gender_info = (TextView) findViewById(R.id.id_gender_info);
        year_info = (TextView) findViewById(R.id.id_year_info);
        lv = (ListView) findViewById(R.id.id_list_status_info);
        quantity = (TextView) findViewById(R.id.quantity_post);
    }

    private void mapping_popup(Dialog myDialog){
        displayname_info = myDialog.findViewById(R.id.id_displayname_info);
        acc_info = myDialog.findViewById(R.id.id_acc_info);
        email_info = myDialog.findViewById(R.id.id_email_info);
        gender_info = myDialog.findViewById(R.id.id_gender_info);
        year_info = myDialog.findViewById(R.id.id_year_info);
    }

    public void showPopup(View v){
        TextView txtclose;
        Button btnFl;
        myDialog.setContentView(R.layout.profiledetail_popup);
        mapping_popup(myDialog);
        loadacc();
        Button btnLogout = myDialog.findViewById(R.id.btnlogout);
        btnLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AccInfoActivity.this, MainActivity.class);
                finish();
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        txtclose = (TextView) myDialog.findViewById(R.id.txtclose);
//        btnFl = (Button) myDialog.findViewById(R.id.btnfollow);
        txtclose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myDialog.dismiss();
            }
        });
        myDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        myDialog.show();
    }


    private void loadPostQuantity(){
        String data = String.format("{\n" +
                "  \"id\": %d,\n" +
                "  \"keyword\": \"string\"\n" +
                "}", temp);
        Submit(data);
    }

    private void Submit(String data) {
        final String savedata = data;
        String URL = "https://eatenapi.azurewebsites.net/api/Accounts/get-post-quantity";

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue = Volley.newRequestQueue(getApplicationContext());
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                quantity.setText(response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return savedata == null ? null : savedata.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    //Log.v("Unsupported Encoding while trying to get the bytes", data);
                    return null;
                }
            }
        };
        requestQueue.add(stringRequest);
    }
}
