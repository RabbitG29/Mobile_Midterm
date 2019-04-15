package inha.inti.mobile_midterm;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

public class ListAdapter extends BaseAdapter {
    private List<Post> pages;
    private Activity context;
    public ListAdapter(Activity _context, List<Post> _pages) {
        pages = _pages;
        context = _context;
    }

    @Override
    public int getCount() {
        return pages.size();
    }

    @Override
    public Object getItem(int position) {
        return pages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View view= inflater.inflate(R.layout.list_item, null, true); // 각 list_item을 set
        TextView text1 = (TextView) view.findViewById(R.id.titleList); // 제목
        TextView text2 = (TextView) view.findViewById(R.id.contentList); // snippet
        ImageView image1 = (ImageView) view.findViewById(R.id.imageList); // 이미지
        File uploadImage = new File(pages.get(position).getImagePath());
        Bitmap myBitmap = BitmapFactory.decodeFile(uploadImage.getAbsolutePath());
        image1.setImageBitmap(myBitmap);

        text1.setText(pages.get(position).getTitle());
        text2.setText(pages.get(position).getSnippet());
        return view;
    }
}