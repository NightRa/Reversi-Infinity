package nightra.reversi.image;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class Images {
    public static Bitmap resize(Bitmap original, int width, int height){
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();
        float scaleWidth = ((float) width) / originalWidth;
        float scaleHeight = ((float) height) / originalHeight;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(original, 0, 0, originalWidth, originalHeight, matrix, false);
        return resizedBitmap;
    }

    public static Bitmap loadImage(Resources resources, int resourceId, int width, int height){
        Bitmap original = BitmapFactory.decodeResource(resources,resourceId);
        return resize(original, width, height);
    }



}
