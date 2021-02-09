package com.lairdtech.lairdtoolkit;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Responsible for handling the icons in the home screen
 * 
 * @author Kyriakos.Alexandrou
 * 
 */
public class HomeIconsAdapter extends BaseAdapter
{
	private Context mContext;
	private final List<String> names;
	private final List<Integer> images;

	public HomeIconsAdapter(Context c, List<String> names, List<Integer> images)
	{
		mContext = c;
		this.images = images;
		this.names = names;
	}

	@Override
	public int getCount()
	{
		return names.size();
	}

	@Override
	public Object getItem(int position)
	{
		return null;
	}

	@Override
	public long getItemId(int position)
	{
		return 0;
	}

	@SuppressLint("InflateParams")
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View grid;
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if (convertView == null)
		{
			grid = new View(mContext);
			grid = inflater.inflate(R.layout.item_apps_holder, null);
			TextView textView = grid.findViewById(R.id.label);
			ImageView imageView = grid.findViewById(R.id.image);
			textView.setText(names.get(position));
			imageView.setImageResource(images.get(position));
		}
		else
		{
			grid = convertView;
		}
		return grid;
	}
}