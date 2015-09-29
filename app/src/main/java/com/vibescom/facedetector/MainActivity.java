package com.vibescom.facedetector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;


import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

import detection.Detector;

public class MainActivity extends AppCompatActivity {
    private Button buttonDetectFace,buttonGetImage,buttonOpenCamera;
    private ImageView imageViewShowImage;

    public static final int SELECT_PHOTO=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

        View.OnClickListener onClickListener=new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.btn_getImage: {
                        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                        photoPickerIntent.setType("image/*");
                        startActivityForResult(Intent.createChooser(photoPickerIntent,"Pick Image"), SELECT_PHOTO);
                    }
                    break;
                    case R.id.btn_detectFace: {
                        /*BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inMutable=true;
                        Bitmap myBitmap = BitmapFactory.decodeResource(
                                getApplicationContext().getResources(),
                                imageViewShowImage.getDrawable().getAlpha(),
                                options);*/
                        detectFace(((BitmapDrawable)imageViewShowImage.getDrawable()).getBitmap());
                    }
                    break;
                }
            }
        };
        buttonDetectFace.setOnClickListener(onClickListener);
        buttonGetImage.setOnClickListener(onClickListener);
        String imageUri = "drawable://" + R.drawable.face;
        getFaceCount(imageUri);
    }
    private void getFaceCount(String fileName)
    {
        Detector detector=Detector.create(getAssets().toString()+"/haarcascade_frontalface_default.xml");
        List res=detector.getFaces(fileName, 1.2f, 1.1f, .05f, 2, true);
        Toast.makeText(this,res.size()+"",Toast.LENGTH_LONG).show();
    }
    private void init() {
        buttonGetImage=(Button)findViewById(R.id.btn_detectFace);
        buttonDetectFace=(Button)findViewById(R.id.btn_getImage);
        buttonOpenCamera=(Button)findViewById(R.id.btn_openCamera);
        imageViewShowImage=(ImageView)findViewById(R.id.iv_showImage);

    }
    private void detectFace(Bitmap bitmap)
    {
        int countFace=0;
        //Create a Paint object for drawing with
        Paint myRectPaint = new Paint();
        myRectPaint.setStrokeWidth(5);
        myRectPaint.setColor(Color.RED);
        myRectPaint.setStyle(Paint.Style.STROKE);

        //Create a Canvas object for drawing on
        Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
        Canvas tempCanvas = new Canvas(tempBitmap);
        tempCanvas.drawBitmap(bitmap, 0, 0, null);

        //Create the Face Detector
        FaceDetector faceDetector = new
                FaceDetector.Builder(getApplicationContext()).setTrackingEnabled(false)
                .build();
        if(!faceDetector.isOperational()){
            Toast.makeText(MainActivity.this,"Could not set up the face detector!",Toast.LENGTH_SHORT).show();
            return;
        }
    else {
            //Detect the Faces
            Frame frame = new Frame.Builder().setBitmap(bitmap).build();
            SparseArray<Face> faces = faceDetector.detect(frame);
            Log.d("Face","Total face is  "+faces.size());
            //Draw Rectangles on the Faces
            for (int i = 0; i < faces.size(); i++) {
                Face thisFace = faces.valueAt(i);
                float x1 = thisFace.getPosition().x;
                float y1 = thisFace.getPosition().y;
                float x2 = x1 + thisFace.getWidth();
                float y2 = y1 + thisFace.getHeight();
                tempCanvas.drawRoundRect(new RectF(x1, y1, x2, y2), 2, 2, myRectPaint);
                countFace++;
            }
            Toast.makeText(MainActivity.this,countFace+" face detected!",Toast.LENGTH_LONG).show();
            imageViewShowImage.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case SELECT_PHOTO:
                if(resultCode == RESULT_OK){
                    try {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
                        final Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
                       ByteArrayOutputStream out = new ByteArrayOutputStream();
                       selectedImage.compress(Bitmap.CompressFormat.PNG, 100, out);
                        imageViewShowImage.setImageBitmap(selectedImage);
                        imageViewShowImage.invalidate();

                       // detectFace(selectedImage);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }

                }
        }
        super.onActivityResult(requestCode, resultCode, data);

    }

}
