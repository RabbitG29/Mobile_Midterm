package inha.inti.mobile_midterm;

import android.content.Context;
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
import android.widget.AdapterView;
import android.widget.ListView;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedList;
import java.util.List;

public class ListFragment extends Fragment {
    final String url = "http://45.119.147.154:3001/";
    ListAdapter adapter;
    ListView listview;
    static final List<Post> myList = new LinkedList<>();
    AdapterView.OnItemClickListener mCallback=null;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        adapter = new ListAdapter(getActivity(), myList);
        if(context instanceof AdapterView.OnItemClickListener) {
            mCallback = (AdapterView.OnItemClickListener)context;
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_list, container, false);
        listview = (ListView) layout.findViewById(R.id.listview);
        listview.setAdapter(adapter);
        GetTask getTask = new GetTask();
        getTask.execute();
        // 리스트 아이템 클릭했을 시 상세 페이지로 이동
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Post post = myList.get(position);
                Log.e("hi",""+post.getLatlng());
                Intent intent = new Intent(getContext(), DetailActivity.class);
                intent.putExtra("object", post);
                intent.putExtra("lat", post.getLatlng().latitude);
                intent.putExtra("lon",post.getLatlng().longitude);
                startActivity(intent);
            }
        });

        return layout;
    }

    public void addList(String title, String content, LatLng latlng, String imagePath, int id) {
        Post temp = new Post(title, content, latlng, imagePath, id);
        myList.add(temp);
    }

    @Override
    public void onResume() {
        super.onResume();
        GetTask getTask = new GetTask();
        getTask.execute();

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
            adapter.notifyDataSetChanged(); // 리스트의 변화를 바로 반영
        }
    }
}
