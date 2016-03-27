package angelcotes.manejo_imagenes;

import android.animation.AnimatorSet;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.Gallery;
import android.widget.ImageView;
import android.graphics.Matrix;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import angelcotes.Libreria_Estilos.BitmapFilter;

public class MainActivity extends AppCompatActivity {


    private String APP_DIRECTORY = "MyPhoto/";
    private String MEDIA_DIRECTORY = APP_DIRECTORY + "media";
    private String NOMBRE_TEMPORAL_IMAGEN;

    private final int PHOTO_CODE = 100;
    private final int SELECT_PICTURE = 200;

//------- Gallery code
    LinearLayout linLay;
    ImageView imageLayout;

    ImageView img;

    private Bitmap image;

    private ImageView imageView;
    private ImageView imageToLinear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        imageView = (ImageView) findViewById(R.id.img_ver);
        Button btn = (Button) findViewById(R.id.btn_opciones);
        linLay = (LinearLayout) findViewById(R.id.Linear1);

        btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Tomar Foto", "Buscar en Galeria", "Cancelar"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Elegir opcion");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int eleccion) {
                        if (options[eleccion] == "Tomar Foto") {
                            openCamera();
                        } else if (options[eleccion] == "Buscar en Galeria") {
                            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                            intent.setType("image/*");
                            startActivityForResult(intent.createChooser(intent, "Seleccionar imagen"), SELECT_PICTURE);
                        } else {
                            dialog.dismiss();
                        }
                    }
                });
                builder.show();
            }
        });
        imageView.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                final CharSequence[] options = {"Grayscale", "Old Photo", "Invert Color", "Recortar Imagen" , "Girar Imagen 90°", "Añadir Imagen"};
                final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                builder.setTitle("Estilos");
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int eleccion) {
                        if (image != null) {
                            Bitmap newBitmap = null;
                            switch(eleccion) {
                                case 0:
                                    newBitmap = BitmapFilter.changeStyle(image, BitmapFilter.GRAY_STYLE);
                                    break;
                                case 1:
                                    newBitmap = BitmapFilter.changeStyle(image, BitmapFilter.OLD_STYLE);
                                    break;
                                case 2:
                                    newBitmap = BitmapFilter.changeStyle(image, BitmapFilter.INVERT_STYLE);
                                    break;
                                case 3:
                                    int bitmapWidth = image.getWidth() / 2;
                                    int bitmapHeight = image.getHeight() / 2;
                                    newBitmap = Bitmap.createBitmap(image,0,0, bitmapWidth + bitmapWidth / 2, bitmapHeight + bitmapHeight / 2);
                                    image = newBitmap;
                                    break;
                                case 4:
                                    Matrix matrix = new Matrix();
                                    matrix.postRotate(90);
                                    newBitmap = Bitmap.createBitmap(image, 0, 0, image.getWidth(), image.getHeight(), matrix, true);
                                    image = newBitmap;
                                    break;
                                case 5:
                                    Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                    intent.setType("image/*");
                                    Uri path = intent.getData();
                                    imageLayout.setImageURI(path);
                                    linLay.addView(imageLayout);
                                    break;
                            }
                            imageView.setImageBitmap(newBitmap);
                        }
                    }
                });
                builder.show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode){
            case PHOTO_CODE:
                if (resultCode == RESULT_OK){
                    String dir = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + NOMBRE_TEMPORAL_IMAGEN;
                    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    File f = new File(dir);
                    Uri contentUri = Uri.fromFile(f);
                    mediaScanIntent.setData(contentUri);
                    this.sendBroadcast(mediaScanIntent);
                    decodeBitmap(dir);
                }
                break;
            case SELECT_PICTURE:
                if (resultCode == RESULT_OK){
                    Uri path = data.getData();
                    imageView.setImageURI(path);
                    decodeBitmap(getRealPathFromURI(path));
                    imageToLinear = new ImageView(this);
                    imageToLinear.setImageBitmap(image);
                    imageToLinear.setVisibility(ImageView.VISIBLE);
                    linLay.addView(imageToLinear);
                }
                break;
        }
    }

    private void decodeBitmap(String dir) {
        Bitmap bitmap;
        bitmap = BitmapFactory.decodeFile(dir);

        imageView.setImageBitmap(bitmap);
        image = bitmap;
    }

    private void openCamera() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "Img" + timeStamp;

        File file = new File(Environment.getExternalStorageDirectory(), MEDIA_DIRECTORY);
        file.mkdirs();
        NOMBRE_TEMPORAL_IMAGEN = imageFileName + ".jpg";
        String path = Environment.getExternalStorageDirectory() + File.separator + MEDIA_DIRECTORY + File.separator + NOMBRE_TEMPORAL_IMAGEN;
        File newFile = new File(path);
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(newFile));
        startActivityForResult(intent, PHOTO_CODE);
    }
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(this, contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }
}
