package com.muziko.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.media.ExifInterface;
import android.support.v8.renderscript.RenderScript;
import android.widget.ImageView;

import com.crashlytics.android.Crashlytics;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.android.gms.wearable.Asset;
import com.jakewharton.picasso.OkHttp3Downloader;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.api.AcrCloud.TrackModel;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.SongModel;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.database.TrackRealmHelper;
import com.oasisfeng.condom.CondomContext;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static android.R.attr.tag;

/**
 * Created by dev on 10/08/2016.
 */
public class ImageManager {
    private static ImageManager instance;
    private final float maxHeight = 1280.0f;
    private final float maxWidth = 1280.0f;
    private Context mContext;

    //no outer class can initialize this class's object
    private ImageManager() {
    }

    public static ImageManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new ImageManager();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;

        Picasso.Builder picassoBuilder =
                new Picasso.Builder(CondomContext.wrap(mContext, "Picasso"))
                        .memoryCache(new LruCache(30 * 1024 * 1024))
                        .defaultBitmapConfig(Bitmap.Config.RGB_565)
                        .indicatorsEnabled(false);

        Picasso picasso = picassoBuilder
                .downloader(new OkHttp3Downloader(OkHttpManager.Instance().getOkHttpClient()))
                .build();
        try {
            Picasso.setSingletonInstance(picasso);
        } catch (IllegalStateException e) {
            Crashlytics.logException(e);
        }

        ImagePipelineConfig config =
                ImagePipelineConfig.newBuilder(mContext).setDownsampleEnabled(true).build();
        Fresco.initialize(mContext, config);
    }

    public Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;

        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }

        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }

    public Asset createAssetFromBitmap(Bitmap bitmap) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteStream);
        return Asset.createFromBytes(byteStream.toByteArray());
    }


    public Drawable createBlurredImageFromBitmap(Bitmap bitmap, int inSampleSize) {

        RenderScript rs = RenderScript.create(mContext);
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 10, stream);
        byte[] imageInByte = stream.toByteArray();
        ByteArrayInputStream bis = new ByteArrayInputStream(imageInByte);
        Bitmap blurTemplate = BitmapFactory.decodeStream(bis, null, options);

        final android.support.v8.renderscript.Allocation input = android.support.v8.renderscript.Allocation.createFromBitmap(rs, blurTemplate);
        final android.support.v8.renderscript.Allocation output = android.support.v8.renderscript.Allocation.createTyped(rs, input.getType());
        final android.support.v8.renderscript.ScriptIntrinsicBlur script = android.support.v8.renderscript.ScriptIntrinsicBlur.create(rs, android.support.v8.renderscript.Element.U8_4(rs));
        script.setRadius(20f);
        script.setInput(input);
        script.forEach(output);
        output.copyTo(blurTemplate);

        return new BitmapDrawable(mContext.getResources(), blurTemplate);
    }

    public byte[] compressImage(String imagePath) {
        Bitmap scaledBitmap = null;

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bmp = BitmapFactory.decodeFile(imagePath, options);

        int actualHeight = options.outHeight;
        int actualWidth = options.outWidth;
        float imgRatio = (float) actualWidth / (float) actualHeight;
        float maxRatio = maxWidth / maxHeight;

        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight;
                actualWidth = (int) (imgRatio * actualWidth);
                actualHeight = (int) maxHeight;
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth;
                actualHeight = (int) (imgRatio * actualHeight);
                actualWidth = (int) maxWidth;
            } else {
                actualHeight = (int) maxHeight;
                actualWidth = (int) maxWidth;

            }
        }

        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight);
        options.inJustDecodeBounds = false;
        options.inDither = false;
        options.inPurgeable = true;
        options.inInputShareable = true;
        options.inTempStorage = new byte[16 * 1024];

        try {
            bmp = BitmapFactory.decodeFile(imagePath, options);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();

        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
        } catch (OutOfMemoryError exception) {
            exception.printStackTrace();
        }

        float ratioX = actualWidth / (float) options.outWidth;
        float ratioY = actualHeight / (float) options.outHeight;
        float middleX = actualWidth / 2.0f;
        float middleY = actualHeight / 2.0f;

        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

        Canvas canvas = new Canvas(scaledBitmap);
        canvas.setMatrix(scaleMatrix);
        canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));

        ExifInterface exif;
        try {
            exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
            Matrix matrix = new Matrix();
            if (orientation == 6) {
                matrix.postRotate(90);
            } else if (orientation == 3) {
                matrix.postRotate(180);
            } else if (orientation == 8) {
                matrix.postRotate(270);
            }
            scaledBitmap = Bitmap.createBitmap(scaledBitmap, 0, 0, scaledBitmap.getWidth(), scaledBitmap.getHeight(), matrix, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 85, out);
        return out.toByteArray();
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
        }
        final float totalPixels = width * height;
        final float totalReqPixelsCap = reqWidth * reqHeight * 2;

        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++;
        }

        return inSampleSize;
    }

    public void invalidateImage(QueueItem queue) {
        String uri = "content://media/external/audio/albumart/" + queue.album;


        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.evictFromCache(Uri.parse(uri));

        Picasso.with(mContext).invalidate(uri);
    }

    public void cancelImage(ImageView imageView) {

        Picasso.with(mContext).cancelRequest(imageView);
    }

    public void loadImage(long albumId, final ImageView imageView) {
        QueueItem queue = TrackRealmHelper.getTrackByAlbum(albumId);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean prefShowArtwork = prefs.getBoolean("prefShowArtwork", false);

        String uri;
        uri = "content://media/external/audio/albumart/" + albumId;
        if (queue.noCover || !prefShowArtwork) {
            uri = null;
        }

        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                .centerCrop()
                .into(imageView);

    }

    public void loadImageSmallFresco(final QueueItem queue, final SimpleDraweeView imageView) {

        String uri;
        uri = "content://media/external/audio/albumart/" + queue.album;
        if (queue.noCover) {
            uri = null;
        }

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE))
                .build();
        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imageView.getController())
                .setImageRequest(request)
                .build();
        imageView.setController(controller);
    }

    public void loadImageFresco(final QueueItem queue, final SimpleDraweeView imageView) {

        String uri;
        uri = "content://media/external/audio/albumart/" + queue.album;
        if (queue.noCover) {
            uri = null;
        }

        ImageRequest imageRequest = null;

        switch ((int) queue.album) {
            case CloudManager.GOOGLEDRIVE:
                imageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.drive_large)
                        .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE))
                        .build();
                break;

            case CloudManager.DROPBOX:
                imageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.dropbox_large)
                        .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE))
                        .build();
                break;

            case CloudManager.BOX:
                imageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.box_large)
                        .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE))
                        .build();
                break;

            case CloudManager.ONEDRIVE:
                imageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.onedrive_large)
                        .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE))
                        .build();
                break;

            case CloudManager.AMAZON:
                imageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.amazon_large)
                        .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE))
                        .build();
                break;

            case CloudManager.FIREBASE:
                imageRequest = ImageRequestBuilder.newBuilderWithResourceId(R.drawable.firebase_large)
                        .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE))
                        .build();
                break;

            default:
                imageRequest = ImageRequestBuilder.newBuilderWithSource(Uri.parse(uri))
                        .setResizeOptions(new ResizeOptions(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE))
                        .build();
        }


        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setOldController(imageView.getController())
                .setImageRequest(imageRequest)
                .build();
        imageView.setController(controller);
    }

    public void loadImageListSmall(final CloudTrack cloudTrack, final ImageView imageView, String tag) {

        String uri;
        QueueItem queueItem = TrackRealmHelper.getTrackByMD5(cloudTrack.getMd5());

        if (queueItem == null || queueItem.noCover) {
            uri = null;
        } else {
            uri = "content://media/external/audio/albumart/" + queueItem.album;
        }

        if (queueItem.album == CloudManager.FIREBASE) {
            Picasso.with(mContext)
                    .load(R.drawable.firebase_large)
                    .tag(tag)
                    .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                    .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                    .centerCrop()
                    .into(imageView);
        } else {
            Picasso.with(mContext)
                    .load(uri)
                    .tag(tag)
                    .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                    .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                    .centerCrop()
                    .into(imageView);
        }
    }

    public void loadImageListSmall(final TrackModel trackModel, final ImageView imageView, String tag) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean prefShowArtwork = prefs.getBoolean("prefShowArtwork", false);

        String uri;
        uri = trackModel.getCoverUrl();
        if (!prefShowArtwork) {
            uri = null;
        }

        Picasso.with(mContext)
                .load(uri)
                .tag(tag)
                .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                .centerCrop()
                .into(imageView);


    }

    public void loadImageListSmall(final QueueItem queue, final ImageView imageView, String tag) {

        String uri;
        if (queue.noCover) {
            uri = null;

            Picasso.with(mContext)
                    .load(uri)
                    .tag(tag)
                    .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                    .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                    .centerCrop()
                    .into(imageView);
        } else {
            switch ((int) queue.album) {
                case CloudManager.GOOGLEDRIVE:
                    Picasso.with(mContext)
                            .load(R.drawable.drive_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.DROPBOX:
                    Picasso.with(mContext)
                            .load(R.drawable.dropbox_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.BOX:
                    Picasso.with(mContext)
                            .load(R.drawable.box_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.ONEDRIVE:
                    Picasso.with(mContext)
                            .load(R.drawable.onedrive_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.AMAZON:
                    Picasso.with(mContext)
                            .load(R.drawable.amazon_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.FIREBASE:
                    Picasso.with(mContext)
                            .load(R.drawable.firebase_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                default:
                    Picasso.with(mContext)
                            .load("content://media/external/audio/albumart/" + queue.album)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE)
                            .centerCrop()
                            .into(imageView);
            }

        }
    }

    public void loadImageList(TrackModel trackModel, final ImageView imageView, String tag) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean prefShowArtwork = prefs.getBoolean("prefShowArtwork", false);

        String uri;
        uri = trackModel.getCoverUrl();
        if (!prefShowArtwork) {
            uri = null;
        }

        Picasso.with(mContext)
                .load(uri)
                .tag(tag)
                .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                .centerCrop()
                .into(imageView);
    }

    public void loadImageList(QueueItem queue, final ImageView imageView, String tag) {

        String uri;
        if (queue.noCover) {
            uri = null;

            Picasso.with(mContext)
                    .load(uri)
                    .tag(tag)
                    .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                    .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                    .centerCrop()
                    .into(imageView);
        } else {
            switch ((int) queue.album) {
                case CloudManager.GOOGLEDRIVE:
                    Picasso.with(mContext)
                            .load(R.drawable.drive_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.DROPBOX:
                    Picasso.with(mContext)
                            .load(R.drawable.dropbox_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.BOX:
                    Picasso.with(mContext)
                            .load(R.drawable.box_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.ONEDRIVE:
                    Picasso.with(mContext)
                            .load(R.drawable.onedrive_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.AMAZON:
                    Picasso.with(mContext)
                            .load(R.drawable.amazon_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                case CloudManager.FIREBASE:
                    Picasso.with(mContext)
                            .load(R.drawable.firebase_large)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                            .centerCrop()
                            .into(imageView);
                    break;

                default:
                    Picasso.with(mContext)
                            .load("content://media/external/audio/albumart/" + queue.album)
                            .tag(tag)
                            .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                            .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                            .centerCrop()
                            .into(imageView);
            }

        }
    }

    public void loadImageLargeAlways(QueueItem queue, final ImageView imageView) {

        switch ((int) queue.album) {
            case CloudManager.GOOGLEDRIVE:
                Picasso.with(mContext)
                        .load(R.drawable.drive_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.DROPBOX:
                Picasso.with(mContext)
                        .load(R.drawable.dropbox_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.BOX:
                Picasso.with(mContext)
                        .load(R.drawable.box_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.ONEDRIVE:
                Picasso.with(mContext)
                        .load(R.drawable.onedrive_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.AMAZON:
                Picasso.with(mContext)
                        .load(R.drawable.amazon_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.FIREBASE:
                Picasso.with(mContext)
                        .load(R.drawable.firebase_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            default:
                Picasso.with(mContext)
                        .load("content://media/external/audio/albumart/" + queue.album)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                        .centerCrop()
                        .into(imageView);
        }
    }

    public void loadImageAlways(QueueItem queueItem, final ImageView imageView) {

        switch ((int) queueItem.album) {
            case CloudManager.GOOGLEDRIVE:
                Picasso.with(mContext)
                        .load(R.drawable.drive_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.DROPBOX:
                Picasso.with(mContext)
                        .load(R.drawable.dropbox_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.BOX:
                Picasso.with(mContext)
                        .load(R.drawable.box_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.ONEDRIVE:
                Picasso.with(mContext)
                        .load(R.drawable.onedrive_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.AMAZON:
                Picasso.with(mContext)
                        .load(R.drawable.amazon_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            case CloudManager.FIREBASE:
                Picasso.with(mContext)
                        .load(R.drawable.firebase_large)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView);
                break;

            default:
                Picasso.with(mContext)
                        .load("content://media/external/audio/albumart/" + queueItem.album)
                        .tag(tag)
                        .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                        .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                        .centerCrop()
                        .into(imageView);
        }
    }

    public void loadImage(SongModel songModel, final ImageView imageView) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);
        boolean prefShowArtwork = prefs.getBoolean("prefShowArtwork", false);

        String uri;
        uri = "content://media/external/audio/albumart/" + songModel.album;
        if (songModel.noCover || !prefShowArtwork) {
            uri = null;

        }

        Picasso.with(mContext)
                .load(uri)
                .placeholder(R.mipmap.placeholder).error(R.mipmap.placeholder)
                .resize(MyApplication.IMAGE_MEDIUM_SIZE, MyApplication.IMAGE_MEDIUM_SIZE)
                .centerCrop()
                .into(imageView);
    }
}