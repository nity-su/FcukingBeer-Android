/**
 * Created by Pongsakorn on 11/24/2015.
 */
package xyz.pongsakorn.fcukingbeer;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

public class FragmentMain extends Fragment {

    private static int TAKE_PHOTO = 0;
    private static int SELECT_PHOTO = 1;

    private String filename;
    private FloatingActionButton btnTake;
    private FloatingActionButton btnSelect;
    private RelativeLayout layoutWelcome;
    private RelativeLayout layoutWait;
    private RelativeLayout layoutResult;
    private ZoomableImageView imResult;
    private ImageView imRaw;
    private Server server;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode== Activity.RESULT_OK) {
            if (requestCode == TAKE_PHOTO) {

            }

            if (requestCode == SELECT_PHOTO) {
                try {
                    InputStream inputStream = getActivity().getContentResolver().openInputStream(data.getData());

                    File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/FcukBeer/");
                    if (!path.exists()) path.mkdirs();

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                    String currentDateandTime = sdf.format(new Date());
                    filename = "raw_"+currentDateandTime+".jpg";

                    FileOutputStream output = new FileOutputStream(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/FcukBeer/"+filename);
                    int bufferSize = 1024;
                    byte[] buffer = new byte[bufferSize];
                    int len = 0;
                    while ((len = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, len);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            startLoading();
            layoutWelcome.setVisibility(View.GONE);
            layoutResult.setVisibility(View.GONE);
            layoutWait.setVisibility(View.VISIBLE);
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_main, container, false);

        server = new Server();

        btnTake = (FloatingActionButton) root.findViewById(R.id.btn_take);
        btnSelect = (FloatingActionButton) root.findViewById(R.id.btn_select);
        layoutWelcome = (RelativeLayout) root.findViewById(R.id.layout_welcome);
        layoutWait = (RelativeLayout) root.findViewById(R.id.layout_wait);
        layoutResult = (RelativeLayout) root.findViewById(R.id.layout_result);

        imResult = (ZoomableImageView) root.findViewById(R.id.im_result);

        imRaw = (ImageView) root.findViewById(R.id.im_raw);

        btnTake.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent camera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FcukBeer");
                if (!path.exists()) path.mkdirs();

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String currentDateandTime = sdf.format(new Date());
                filename = "raw_" + currentDateandTime + ".jpg";
                File image = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FcukBeer", filename);
                camera.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                startActivityForResult(camera, TAKE_PHOTO);
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, SELECT_PHOTO);
            }
        });
        return root;
    }

    private void startLoading() {
        String impath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+"/FcukBeer/"+filename;

        getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(impath))));

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(impath, options);

        try {
            ExifInterface exif = new ExifInterface(impath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            }
            else if (orientation == 3) {
                matrix.postRotate(180);
            }
            else if (orientation == 8) {
                matrix.postRotate(270);
            }

            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap = Image.resize(bitmap, 980, 980);
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = new File(getContext().getCacheDir(), filename);
        try {
            f.createNewFile();
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90 , bos);
            byte[] bitmapdata = bos.toByteArray();

            FileOutputStream fos = new FileOutputStream(f);
            fos.write(bitmapdata);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        final Bitmap bs = bitmap;

        server.sendPhoto(f, new Server.Listener() {
            @Override
            public void onStart() {
                Bitmap preview = Image.blur(bs, 1.0f, 20);
                imRaw.setImageBitmap(preview);
            }

            @Override
            public void onSuccess(List<RectModel> res) {
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                options.inMutable = true;
                Bitmap im = BitmapFactory.decodeFile(getContext().getCacheDir()+"/"+filename, options);

                Bitmap blur = Image.blur(im, 1.0f, 25);
                Image.sectionBlur(im, blur, createMark(res, im.getHeight(), im.getWidth()));

                imResult.setImageBitmap(im);

                File f = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/FcukBeer", filename.replace("raw", "out"));
                try {
                    f.createNewFile();
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    im.compress(Bitmap.CompressFormat.JPEG, 90 , bos);
                    byte[] bitmapdata = bos.toByteArray();

                    FileOutputStream fos = new FileOutputStream(f);
                    fos.write(bitmapdata);
                    fos.flush();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                layoutWelcome.setVisibility(View.GONE);
                layoutResult.setVisibility(View.VISIBLE);
                layoutWait.setVisibility(View.GONE);

                getActivity().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));

            }

            @Override
            public void onFail(int status, String err) {
                Toast.makeText(getContext(), "Error " + status + " (" + err + ")", Toast.LENGTH_LONG).show();
                layoutWelcome.setVisibility(View.VISIBLE);
                layoutResult.setVisibility(View.GONE);
                layoutWait.setVisibility(View.GONE);
            }
        });
    }

    private int[][] createMark(List<RectModel> rects, int n, int m) {
        int[][] mark = new int[n][m];
        for (int i=0;i<n;i++) {
            for (int j=0;j<m;j++) {
                if (inRect(rects, j, i))
                    mark[i][j] = 1;
            }
        }
        return mark;
    }

    private boolean inRect(List<RectModel> rects, int x, int y) {
        for (int i=0;i<rects.size();i++) {
            RectModel rect = rects.get(i);
            if (x>=rect.x && x<=rect.x + rect.w && y>=rect.y && y<=rect.y + rect.h)
                return true;
        }
        return false;
    }
}
