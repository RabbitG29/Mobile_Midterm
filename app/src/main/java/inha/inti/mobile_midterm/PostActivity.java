package inha.inti.mobile_midterm;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;


public class PostActivity extends FragmentActivity implements OnMapReadyCallback {
    final String url = "http://45.119.147.154:3001/insert";
    GoogleMap mMap;
    private GpsInfo gps;
    double latitude=0, longitude=0;

    //권한 변수
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private final int RESULT_PERMISSIONS = 100;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    Button postButton, pictureButton;
    LatLng resultLatlng;
    String imagepath="";
    ImageView imageView;

    boolean isPermission = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        // 위치 권한 체크
        if (!isPermission) {
            callPermission();
        }
        /*---저장소와 카메라 permission check---*/
        requestPermissionCamera();

        gps = new GpsInfo(this);
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            // 위도, 경도 구하기
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map2);
        mapFragment.getMapAsync(this);
        postButton = (Button) findViewById(R.id.postButton);
        pictureButton = (Button) findViewById(R.id.pictureButton);
        imageView = (ImageView) findViewById(R.id.postPicture);
        final EditText titleEdit = (EditText) findViewById(R.id.titleEdit);
        final EditText contentEdit = (EditText) findViewById(R.id.contentEdit);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(titleEdit.getText().toString()=="") {
                    Toast.makeText(getApplicationContext(),"제목을 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(contentEdit.getText().toString()=="") {
                    Toast.makeText(getApplicationContext(),"내용을 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(latitude==0&&longitude==0) {
                    Toast.makeText(getApplicationContext(),"마커를 입력해 주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(imagepath=="") {
                    Toast.makeText(getApplicationContext(),"사진을 지정해 주세요.",Toast.LENGTH_SHORT).show();
                    return;
                }
                PostTask postTask = new PostTask(titleEdit.getText().toString(), contentEdit.getText().toString(),resultLatlng.latitude,resultLatlng.longitude,imagepath);
                postTask.execute();
                finish();
            }
        });
        pictureButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(requestPermissionStorage())
                    ViewHelper.selectImage(PostActivity.this);
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng current = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current));
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(7); // 최초 카메라 줌
        googleMap.animateCamera(zoom);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(final LatLng latLng) {
                AlertDialog.Builder builder = new AlertDialog.Builder(PostActivity.this); // 다이얼로그를 띄운다.
                builder.setTitle("Marker");
                builder.setMessage("마커를 추가하시겠습니까?");
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) { // 예를 누른다면
                                EditText titleEdit = (EditText) findViewById(R.id.titleEdit);
                                EditText contentEdit = (EditText) findViewById(R.id.contentEdit);
                                mMap.addMarker(new MarkerOptions().position(latLng).title(titleEdit.getText().toString()).snippet(contentEdit.getText().toString()));
                                resultLatlng = latLng;
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
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_ACCESS_FINE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            isAccessFineLocation = true;

        } else if (requestCode == PERMISSIONS_ACCESS_COARSE_LOCATION
                && grantResults[0] == PackageManager.PERMISSION_GRANTED){

            isAccessCoarseLocation = true;
        }

        if (isAccessFineLocation && isAccessCoarseLocation) {
            isPermission = true;
        }
    }

    private void callPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_ACCESS_FINE_LOCATION);

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    PERMISSIONS_ACCESS_COARSE_LOCATION);
        } else {
            isPermission = true;
        }
    }

    // 이미지를 불러왔을 때 처리
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // 잘 불러왔다면
        if (resultCode == RESULT_OK) {
            Uri imageUri = ViewHelper.getPickImageResultUri(this,data);
            String path = ViewHelper.getRealPathFromURI(this,imageUri);
            Log.e("image", imageUri.toString());
            Log.e("image2", path);
            File uploadImage = new File(path);
            File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
            // 캐쉬 이미지 파일 혹은 갤러리 이미지 파일을 지정된 폴더로 복사
            if (pictureFile == null){
                Log.e("e", "Error creating media file, check storage permissions");
                return;
            }
            try {
                FileInputStream fis = new FileInputStream(uploadImage);
                FileOutputStream newfos = new FileOutputStream(pictureFile);
                int readcount=0;
                byte[] buffer = new byte[1024];

                while((readcount = fis.read(buffer,0,1024))!= -1){
                    newfos.write(buffer,0,readcount);
                }
                newfos.close();
                fis.close();
            } catch(IOException e) {

            }
            if(pictureFile.exists()) { // 해당하는 이미지가 있는지 확인
                Bitmap myBitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());
                imageView.setImageBitmap(myBitmap);
                imagepath = pictureFile.getAbsolutePath(); // 경로 저장
                Log.e("image3", imagepath);
            }
        }
    }

    /*---파일 저장---*/
    private static File getOutputMediaFile(int type){
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory(), "CameraApp2");
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                Log.d("CameraApp2", "failed to create directory");
                return null;
            }
        }
// Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else {
            return null;
        }
        return mediaFile;
    }

    /*-----리스트를 추가하는 AsyncTask-----*/
    class PostTask extends AsyncTask<String, Integer, JSONObject> {
        String title;
        String content;
        double latitude;
        double longitude;
        String imagepath;
        public PostTask(String _title, String _content, double _latitude, double _longitude, String _imagepath) {
            title=_title;
            content = _content;
            latitude = _latitude;
            longitude = _longitude;
            imagepath = _imagepath;
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
                jsonObject.put("title", title); // JSON 생성
                jsonObject.put("content", content); // JSON 생성
                jsonObject.put("latitude", latitude); // JSON 생성
                jsonObject.put("longitude", longitude); // JSON 생성
                jsonObject.put("imagepath", imagepath); // JSON 생성
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

    /*---퍼미션 체크---*/
    public boolean requestPermissionCamera(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) { // 버전이 마시멜로 이상인가?
            /*---카메라 체크---*/
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(PostActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        RESULT_PERMISSIONS);
            }else {
            }
        }else{  // version 6 이하일때
            return true;
        }
        return true;
    }

    /*---퍼미션 체크---*/
    public boolean requestPermissionStorage(){
        int sdkVersion = Build.VERSION.SDK_INT;
        if(sdkVersion >= Build.VERSION_CODES.M) { // 버전이 마시멜로 이상인가?
            /*---저장공간 체크---*/
            if(PackageManager.PERMISSION_GRANTED != checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                // 최초 권한 요청인지, 혹은 사용자에 의한 재요청인지 확인
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    // 사용자가 임의로 권한을 취소한 경우
                    // 권한 재요청
                    Log.d("p", "권한 재요청");
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_PERMISSIONS);
                }else {
                    // 최초로 권한을 요청하는 경우(첫실행)
                    Log.d("p", "권한 최초요청");
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, RESULT_PERMISSIONS);
                }
            } else { // 접근권한이 있을때
                Log.d("p", "접근 허용");
            }
        }else{  // version 6 이하일때
            return true;
        }
        return true;
    }

}
