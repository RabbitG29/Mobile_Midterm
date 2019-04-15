package inha.inti.mobile_midterm;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kakao.kakaolink.v2.KakaoLinkResponse;
import com.kakao.kakaolink.v2.KakaoLinkService;
import com.kakao.message.template.ContentObject;
import com.kakao.message.template.LinkObject;
import com.kakao.message.template.LocationTemplate;
import com.kakao.network.ErrorResult;
import com.kakao.network.callback.ResponseCallback;
import com.kakao.util.helper.log.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DetailActivity extends FragmentActivity implements OnMapReadyCallback {
    final String url = "http://45.119.147.154:3001/delete";
    GoogleMap mMap;
    Post post;
    LatLng latlng;
    String title, content, imagePath;
    int id;
    ImageView imageView;
    TextView titleText, contentText;
    Button shareButton, deleteButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Intent intent = getIntent(); // 넘어온 Intent
        post = (Post) intent.getSerializableExtra("object"); // 넘어온 post값 가져옴
        double lat = intent.getDoubleExtra("lat", 132);
        double lon = intent.getDoubleExtra("lon", 37);
        id = post.getId();
        latlng = new LatLng(lat, lon);
        //Log.e("lat1", ""+post.getLatlng());
        title = post.getTitle();
        content = post.getSnippet();
        imagePath = post.getImagePath();
        Log.e("lat", ""+latlng);
        // 액티비티 요소들 정의
        imageView = (ImageView) findViewById(R.id.detailImage);
        File uploadImage = new File(imagePath);
        Bitmap myBitmap = BitmapFactory.decodeFile(uploadImage.getAbsolutePath());
        imageView.setImageBitmap(myBitmap);
        titleText = (TextView) findViewById(R.id.titleTextd);
        contentText = (TextView) findViewById(R.id.contentTextd);
        shareButton = (Button) findViewById(R.id.shareButton);
        deleteButton = (Button) findViewById(R.id.deleteButton);
        titleText.setText(title);
        contentText.setText(content);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map3);
        mapFragment.getMapAsync(this);

        deleteButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this); // 삭제 여부를 묻는 다이얼로그를 띄운다.
                builder.setTitle("게시글 삭제");
                builder.setMessage("게시글을 삭제하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { // 예를 누른다면
                                DeleteTask deleteTask = new DeleteTask(id);
                                deleteTask.execute();
                                finish();
                            }
                        });
                builder.setNegativeButton("아니오", // 아니오를 누르면 종료
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                            }
                        });
                builder.show();
            }
        });

        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DetailActivity.this); // 공유 여부를 묻는 다이얼로그를 띄운다.
                builder.setTitle("카카오링크 공유하기");
                builder.setMessage("위치를 공유하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { // 예를 누른다면
                                String location = getAddress(getApplicationContext(), latlng.latitude, latlng.longitude); // 위치 보기를 위해 위경도를 지도로 변환
                                LocationTemplate params = LocationTemplate.newBuilder(location,
                                        ContentObject.newBuilder(title,
                                                "http://mud-kage.kakao.co.kr/dn/NTmhS/btqfEUdFAUf/FjKzkZsnoeE4o19klTOVI1/openlink_640x640s.jpg",
                                                LinkObject.newBuilder()
                                                        .setWebUrl("https://www.inha.ac.kr")
                                                        .setMobileWebUrl("https://www.inha.ac.kr")
                                                        .build())
                                                .setDescrption(content)
                                                .build())
                                        .setAddressTitle(title)
                                        .build();

                                Map<String, String> serverCallbackArgs = new HashMap<String, String>();
                                serverCallbackArgs.put("user_id", "${current_user_id}");
                                serverCallbackArgs.put("product_id", "${shared_product_id}");

                                KakaoLinkService.getInstance().sendDefault(getApplicationContext(), params, serverCallbackArgs, new ResponseCallback<KakaoLinkResponse>() {
                                    @Override
                                    public void onFailure(ErrorResult errorResult) {
                                        Logger.e(errorResult.toString());
                                    }

                                    @Override
                                    public void onSuccess(KakaoLinkResponse result) {
                                        // 템플릿 밸리데이션과 쿼터 체크가 성공적으로 끝남. 톡에서 정상적으로 보내졌는지 보장은 할 수 없다. 전송 성공 유무는 서버콜백 기능을 이용하여야 한다.
                                    }
                                });
                            }
                        });
                builder.setNegativeButton("아니오", // 아니오를 누르면 종료
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext(),"아니오를 선택했습니다.",Toast.LENGTH_LONG).show();
                            }
                        });
                builder.show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng)); // 최초 카메라 위치
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(7); // 최초 카메라 줌
        googleMap.animateCamera(zoom);
        mMap.addMarker(new MarkerOptions().position(latlng).title(title).snippet(content));
    }

    // 위경도를 주소로 변환
    public static String getAddress(Context mContext, double lat, double lng) {
        String nowAddress ="현재 위치를 확인 할 수 없습니다.";
        Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
        List<Address> address;
        try {
            if (geocoder != null) {
                //세번째 파라미터는 좌표에 대해 주소를 리턴 받는 갯수로
                //한좌표에 대해 두개이상의 이름이 존재할수있기에 주소배열을 리턴받기 위해 최대갯수 설정
                address = geocoder.getFromLocation(lat, lng, 1);

                if (address != null && address.size() > 0) {
                    // 주소 받아오기
                    String currentLocationAddress = address.get(0).getAddressLine(0).toString();
                    nowAddress  = currentLocationAddress;

                }
            }

        } catch (IOException e) {
            Toast.makeText(mContext, "주소를 가져 올 수 없습니다.", Toast.LENGTH_LONG).show();

            e.printStackTrace();
        }
        return nowAddress;
    }

    /*-----리스트를 삭제하는 AsyncTask-----*/
    class DeleteTask extends AsyncTask<String, Integer, JSONObject> {
        int id;
        public DeleteTask(int _id) {
            id=_id;
        }
        /*----전처리----*/
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        /*-----JSON화 후 통신-------*/
        @Override
        protected JSONObject doInBackground(String... params) {
            Log.e("url", url);
            JSONObject result;
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("id", id); // JSON 생성
                Log.e("json", jsonObject.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, jsonObject, "POST");
            Log.e("Async", "Async");
            return result;
        }
        /*----통신의 결과값을 이용----*/
        @Override
        protected void onPostExecute(JSONObject result) {
            super.onPostExecute(result);
        }
    }
}
