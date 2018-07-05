package kast0013.zwinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Contacts;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class arrayAdapter extends ArrayAdapter<cards>{

    //Firebase Storage
    private FirebaseStorage storage;
    private StorageReference storageReference;

    //Butterknife Views
    @BindView(R.id.userName) TextView _userName;
    @BindView(R.id.userImage) ImageView _userImage;
    Context context;

    public arrayAdapter(Context context, int resourceId, List<cards> items){
        super(context, resourceId, items);
    }

    public View getView(final int position, View convertView, final ViewGroup parent){
        final cards card_item = getItem(position);

        //firebase storage instantiierungen
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item, parent, false);
        }
        ButterKnife.bind(this, convertView);

        //Setzen von Name und Profilbild
        _userName.setText(card_item.getName());
        Glide.with(convertView.getContext()).load(card_item.getUserImageUrl()).into(_userImage);

        return convertView;
    }
}
