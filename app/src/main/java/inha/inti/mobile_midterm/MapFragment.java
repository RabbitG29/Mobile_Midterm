package inha.inti.mobile_midterm;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.LinkedList;
import java.util.List;



public class MapFragment extends Fragment implements OnMapReadyCallback {
    final String url = "http://45.119.147.154:3001/";
    private GoogleMap mMap;
    private MapView mapView = null;

    private GpsInfo gps;
    double latitude=-34, longitude=150;

    static final List<Post> myList = new LinkedList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_map, container, false);
        gps = new GpsInfo(getContext());
        // GPS 사용유무 가져오기
        if (gps.isGetLocation()) {
            // 위도, 경도 구하기
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
        } else {
            // GPS 를 사용할수 없으므로
            gps.showSettingsAlert();
        }
        mapView = (MapView)layout.findViewById(R.id.map);
        mapView.getMapAsync(this);

        //list 받아 옴
        GetTask getTask = new GetTask();
        getTask.execute();

        return layout;
    }

    @Override
    public void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    //일시정지 후 돌아왔을 때
    @Override
    public void onResume() {
        super.onResume();
        GetTask getTask = new GetTask();
        getTask.execute();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onLowMemory();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //액티비티가 처음 생성될 때 실행되는 함수
        if(mapView != null) {
            mapView.onCreate(savedInstanceState);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng current = new LatLng(latitude, longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(current)); // 최초 카메라 위치
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(7); // 최초 카메라 줌
        googleMap.animateCamera(zoom);
        mMap.clear();
        for(Post i : myList)
            mMap.addMarker(new MarkerOptions().position(i.getLatlng()).title(i.getTitle()).snippet(i.getSnippet()));
        /*---marker 클릭했을 때 상세 페이지로 이동---*/
        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                Log.e("Marker", marker.getPosition()+"");
                for(Post i : myList) {
                    Log.e("my", i.getLatlng()+"");
                    if(i.getLatlng().latitude==marker.getPosition().latitude&&i.getLatlng().longitude==marker.getPosition().longitude) { // 위경도가 같은 걸 찾았으면
                        Intent intent = new Intent(getContext(), DetailActivity.class);
                        intent.putExtra("object", i);
                        intent.putExtra("lat", i.getLatlng().latitude);
                        intent.putExtra("lon",i.getLatlng().longitude);
                        startActivity(intent);
                    }
                }

                return false;
            }
        });
    }

    public void addList(String title, String content, LatLng latlng, String imagepath, int id) {
        Post temp = new Post(title, content, latlng, imagepath, id);
        myList.add(temp);
    }

    /*-----리스트를 받아오는 받아오는 AsyncTask-----*/
    class GetTask extends AsyncTask<String, Integer, JSONObject> {
        public GetTask() {

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
            RequestHttpURLConnection requestHttpURLConnection = new RequestHttpURLConnection();
            result = requestHttpURLConnection.request(url, jsonObject, "POST");
            Log.e("Async", "Async");
            return result;
        }
        /*----통신의 결과값을 이용----*/
        @Override
        protected void onPostExecute(JSONObject result) {
            //TODO : List 갱신 필요
            super.onPostExecute(result);
            JSONArray jsonArray = new JSONArray();
            JSONObject post = new JSONObject();
            try {
                jsonArray = result.getJSONArray("result"); // 전체 JSONArray 가져오기
            }catch (JSONException e) {

            }
            myList.clear();
            for(int i = 0 ; i<jsonArray.length(); i++) {
                try {
                    post = jsonArray.getJSONObject(i); // 항목별로 가져오기
                    LatLng tempL = new LatLng(post.getDouble("latitude"), post.getDouble("longitude"));
                    addList(post.getString("title"), post.getString("content"), tempL, post.getString("imagepath"), post.getInt("id"));
                } catch (JSONException e) {

                }
            }
        }
    }


}
