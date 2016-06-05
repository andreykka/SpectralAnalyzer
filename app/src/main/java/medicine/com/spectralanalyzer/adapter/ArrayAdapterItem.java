package medicine.com.spectralanalyzer.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import medicine.com.spectralanalyzer.R;
import medicine.com.spectralanalyzer.pojo.ItemData;

import java.util.List;


public class ArrayAdapterItem extends ArrayAdapter<ItemData> {

    Context mContext;
    int layoutResourceId;
    List<ItemData> data = null;

    public ArrayAdapterItem(Context mContext, int layoutResourceId, List<ItemData> data) {
        super(mContext, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.mContext = mContext;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            // inflate the layout
            LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
            convertView = inflater.inflate(layoutResourceId, parent, false);
        }

        // object item based on the position
        ItemData objectItem = data.get(position);

        // get the TextView and then set the text (item name) and tag (item ID) values
        TextView textViewItem = (TextView) convertView.findViewById(R.id.textViewItem);
        textViewItem.setTag(objectItem.getTag());
        textViewItem.setText(objectItem.getFilename());

        return convertView;

    }

}
