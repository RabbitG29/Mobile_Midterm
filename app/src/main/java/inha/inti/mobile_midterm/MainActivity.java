package inha.inti.mobile_midterm;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity  {
    // FrameLayout에 각 메뉴의 Fragment를 바꿔 줌
    private FragmentManager fragmentManager = getSupportFragmentManager();
    // 3개의 메뉴에 들어갈 Fragment들
    private ListFragment listFragment = new ListFragment();
    private MapFragment mapFragment = new MapFragment();
    private ImageFragment imageFragment = new ImageFragment();
    // 뒤로가기 버튼 두 번 사이의 간격 체크 변수
    private long lastTimeBackPressed;
    // 권한 체크 변수
    private final int PERMISSIONS_ACCESS_FINE_LOCATION = 1000;
    private final int PERMISSIONS_ACCESS_COARSE_LOCATION = 1001;
    private boolean isAccessFineLocation = false;
    private boolean isAccessCoarseLocation = false;
    boolean isPermission = false;
    // 플로팅 액션 버튼
    FloatingActionButton btnFAB;

    //TODO 1 : 리스트뷰 -> own class 만들고 ListView 폼 만들어서 구현 ==> 완료
    //TODO 2 : 지도 ==> 완료
    //TODO 3 : 플로팅 액션 버튼 통해서 추가 페이지(액티비티가 좋을듯?) 구현, POST 버튼 눌렀을 때 리스트에 추가하고 지도에 마커 추가 ==> 완료
    //TODO 4 : 상세보기 페이지 구현, 카톡 공유 기능 구현 -> 마커랑 리스트뷰 누르면 상세페이지로 이동하게 구현 ==> 완료
    //TODO 3.5 : <<PICTURE 버튼 눌렀을 때 사진을 찍을 건지 갤러리에서 불러올건지 구현>> ==> 완료
    //TODO 5 : 영구저장 방법 + 삭제 기능 구현 -> 서버쪽 구현 완료, AsyncTask 구현 완료. List랑 연동해야함, 삭제 구현해야함 ==> 완료


    //TODO 6 : 이미지 플리퍼 구현(UI 생각 필요) 3탭 어케할지?
    //TODO 7 : 예외처리 및 디버그 + 리스트뷰 이미지 썸네일로 처리할까?
    //TODO 7-2 : UI 개선 가능하면 해 보자

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView bottomNavigationView = findViewById(R.id.navigation);
        // 플로팅 액션 버튼 지정
        btnFAB = (FloatingActionButton)findViewById(R.id.fab);
        btnFAB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(MainActivity.this, "POST", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getApplicationContext(), PostActivity.class);
                startActivity(intent);
            }
        });
        // 첫 화면 지정
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.frame_layout, listFragment).commitAllowingStateLoss();

        // 권한 요청을 해야 함
        if (!isPermission) {
            callPermission();
        }
        //프레그먼트 바꾸어 줌
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        transaction.replace(R.id.frame_layout, listFragment).commitAllowingStateLoss();
                        break;
                    case R.id.navigation_dashboard:
                        transaction.replace(R.id.frame_layout, mapFragment).commitAllowingStateLoss();
                        break;
                    case R.id.navigation_notifications: // 이미지 플리퍼 써보자
                        transaction.replace(R.id.frame_layout, imageFragment).commitAllowingStateLoss();
                        break;
                }
                return true;
            }
        });

    }

    /*---뒤로가기 두 번 누르면 앱 종료---*/
    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - lastTimeBackPressed < 1500) {
            finish();
            return;
        }
        Toast.makeText(this, "'뒤로' 버튼을 한 번 더 누르면 종료됩니다.", Toast.LENGTH_SHORT).show();
        lastTimeBackPressed = System.currentTimeMillis();
    }

    /*----GPS 권한이 있는지 확인----*/
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

    // GPS 권한 요청
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
}
