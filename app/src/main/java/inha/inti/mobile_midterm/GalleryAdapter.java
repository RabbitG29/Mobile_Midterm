package inha.inti.mobile_midterm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import java.io.File;
import java.util.List;

public class GalleryAdapter extends BaseAdapter { // 이미지 플리퍼를 위한 adapter

    private final Context mContext;
    LayoutInflater inflater;
    private List<Post> pages;
    public GalleryAdapter(Context c,List<Post> _pages) {
        mContext = c;
        inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pages = _pages;
    }

    public int getCount() {
        return pages.size();
    }

    public Object getItem(int position) {
        return position;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item, parent, false);
        }
        ImageView imageView = (ImageView) convertView.findViewById(R.id.items);
        File uploadImage = new File(pages.get(position).getImagePath());
        Bitmap myBitmap = BitmapFactory.decodeFile(uploadImage.getAbsolutePath());
        imageView.setImageBitmap(myBitmap); // 리스트에 있는 이미지로 띄움
        return convertView;
    }
}
