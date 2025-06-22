package com.hucmuaf.locket_mobile.fragment;

import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ExifInterface;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.hucmuaf.locket_mobile.R;
import com.hucmuaf.locket_mobile.activity.ListFriendActivity;
import com.hucmuaf.locket_mobile.activity.MessageActivity;
import com.hucmuaf.locket_mobile.activity.ShowImageActivity;
import com.hucmuaf.locket_mobile.inteface.FriendListApiService;
import com.hucmuaf.locket_mobile.model.FriendListResponse;
import com.hucmuaf.locket_mobile.service.ApiClient;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PageHomeFragment extends Fragment {
    private TextureView textureView;
    private CameraDevice cameraDevice;

    private CaptureRequest.Builder captureRequestBuilder;
    private CameraCaptureSession cameraCaptureSession;
    private String cameraId;
    private CameraCaptureSession captureSession;
    private CameraManager cameraManager;
    private LinearLayout take_layout;

    private boolean isFrontCamera = true; // true = trước, false = sau

    GestureDetector gestureDetector;

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private int width;
    private int height;
    private ImageReader reader;
    private Size previewSize;
    private boolean isFlashEnabled = false;
    private String userId;
    private Context context;
    private Activity activity;


    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        context = requireContext(); // hoặc getContext()
        activity = requireActivity(); // cần thiết nếu gọi hàm yêu cầu Activity

        assert getArguments() != null;
        userId = getArguments().getString("userId");
        Log.e("Take Activity", String.valueOf(userId));

        TextView quantity_friends = view.findViewById(R.id.count_friend);
        FriendListApiService friendListApiService = ApiClient.getFriendListApiService();
        Call<FriendListResponse> call = friendListApiService.getFriendList(userId);
        call.enqueue(new Callback<FriendListResponse>() {
            @Override
            public void onResponse(@NonNull Call<FriendListResponse> call, @NonNull Response<FriendListResponse> response) {
                String quantity = "0";
                if (response.body() != null) {
                    quantity = response.body().getTotalFriends() + "";
                }
                quantity_friends.setText(quantity);
            }

            @Override
            public void onFailure(@NonNull Call<FriendListResponse> call, @NonNull Throwable t) {
                Log.e("Take Activity error", t.toString());
            }
        });

        // Tìm LinearLayout với id="friends" và thêm sự kiện onClick
        LinearLayout friendsLayout = view.findViewById(R.id.friends);
        if (friendsLayout != null) {
            Log.d(TAG, "Found friends layout, setting click listener");

            // Đảm bảo layout có thể click được
            friendsLayout.setClickable(true);
            friendsLayout.setFocusable(true);

            friendsLayout.setOnClickListener(v -> {
                Log.d(TAG, "Friends layout clicked, starting ListFriendActivity");
                try {
                    Intent intent = new Intent(activity, ListFriendActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ListFriendActivity", e);
                }
            });
        } else {
            Log.e(TAG, "Could not find LinearLayout with id 'friends'");
        }

        startBackgroundThread();

        textureView = view.findViewById(R.id.camera_preview);
        textureView.setSurfaceTextureListener(surfaceTextureListener);


        textureView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            this.width = textureView.getWidth();
            this.height = textureView.getHeight();
//            openCamera(width, height);
            Log.e("Check width", "Width textureView: " + width);
            Log.e("Check height", "Height textureView: " + height);
            cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

            ImageView cached = view.findViewById(R.id.cached);
            cached.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPause();
                    isFrontCamera = !isFrontCamera; // đổi chiều
                    if (textureView.isAvailable()) {
                        openCamera(width, height);
                    }
                }
            });


            take_layout = view.findViewById(R.id.take_layout);
            take_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture();
                }
            });
            LinearLayout history = view.findViewById(R.id.history);
            history.setOnClickListener(v -> {
                ViewPager2 viewPager = requireActivity().findViewById(R.id.main_viewpager);
                viewPager.setCurrentItem(1, true); // sang trang kế tiếp
            });

            ImageView flash = view.findViewById(R.id.flash);
            flash.setOnClickListener(v -> {
                if (isFlashEnabled) {
                    isFlashEnabled = false;
                    flash.setImageResource(R.mipmap.no_flash);
                } else {
                    isFlashEnabled = true;
                    flash.setImageResource(R.mipmap.has_flash);
                }
            });
        });
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void closeCamera() {
        try {
            if (captureSession != null) {
                captureSession.close();
                captureSession = null;
            }
            if (cameraDevice != null) {
                cameraDevice.close();
                cameraDevice = null;
            }
        } catch (Exception e) {
            Log.e("Camera", "closeCamera error: " + e.getMessage());
        }
    }

    private String getCameraId(boolean useFrontCamera, int width, int height) throws CameraAccessException {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        for (String id : manager.getCameraIdList()) {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (useFrontCamera && facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                assert map != null;
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                return id;
            } else if (!useFrontCamera && facing != null && facing == CameraCharacteristics.LENS_FACING_BACK) {
                assert map != null;
                previewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), width, height);
                return id;
            }
        }
        return null; // Không tìm thấy camera
    }

    private final TextureView.SurfaceTextureListener surfaceTextureListener =
            new TextureView.SurfaceTextureListener() {
                @Override
                public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width, int height) {
                    if (textureView.isAvailable()) {
                        openCamera(width, height);
                    } else {
                        textureView.setSurfaceTextureListener(surfaceTextureListener);
                    }
                }

                @Override
                public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
                }

                @Override
                public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                    return false;
                }

                @Override
                public void onSurfaceTextureUpdated(SurfaceTexture surface) {
                }
            };

    private void openCamera(int width, int height) {
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = getCameraId(isFrontCamera, width, height);
            if (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.CAMERA}, 101);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private Size chooseOptimalSize(Size[] choices, int width, int height) {
        List<Size> bigEnough = new ArrayList<>();
        for (Size option : choices) {
            if (option.getHeight() == option.getWidth() * height / width && option.getWidth() >= width && option.getHeight() >= height) {
                bigEnough.add(option);
            }
        }

        if (bigEnough.size() > 0) {
            return Collections.min(bigEnough, (s1, s2) -> Long.compare((long) s1.getWidth() * s1.getHeight(), s2.getWidth() * s2.getHeight()));
        } else {
            return choices[0]; // fallback
        }
    }


    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            camera.close();
            cameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            camera.close();
            cameraDevice = null;
        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            if (texture == null) {
                Log.e("Camera", "SurfaceTexture is null, chưa sẵn sàng để tạo preview.");
                return; // hoặc retry sau một thời gian
            }
            texture.setDefaultBufferSize(1080, 1920); // hoặc dùng kích thước camera output phù hợp
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            if (surface.isValid()) {
                cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {
                        cameraCaptureSession = session;
                        try {
                            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                            session.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        Toast.makeText(activity, "Cấu hình camera thất bại!", Toast.LENGTH_SHORT).show();
                    }
                }, null);
            }

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        startBackgroundThread();

        if (cameraDevice == null && textureView.isAvailable()) {
            openCamera(textureView.getWidth(), textureView.getHeight());
        } else {
            textureView.setSurfaceTextureListener(surfaceTextureListener);
        }
    }

    public void takePicture() {
        if (cameraDevice == null) return;
        CameraManager manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;

            //Chọn size ảnh
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                        .getOutputSizes(ImageFormat.JPEG);
                Size selectedSize = null;
                for (Size size : jpegSizes) {
                    float ratio = (float) size.getWidth() / size.getHeight();
                    float viewRatio = (float) textureView.getWidth() / textureView.getHeight();
                    if (Math.abs(ratio - viewRatio) < 0.1f) {
                        selectedSize = size;
                        break;
                    }
                }
                if (selectedSize == null) selectedSize = jpegSizes[0];
                width = selectedSize.getWidth();
                height = selectedSize.getHeight();
            }
            //Tạo imageReader và Surface
            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>();
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);

            // Hướng ảnh
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            int jpegOrientation = getJpegOrientation(characteristics, rotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

            // Lưu ảnh
            final File file = new File(context.getExternalFilesDir(null), "pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = reader1 -> {
                Image image = null;
                try {
                    image = reader1.acquireLatestImage();
                    if (image == null) return;

                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    // Ghi ảnh tạm ra file
                    try (FileOutputStream fos = new FileOutputStream(file)) {
                        fos.write(bytes);
                    }

                    // Đọc Exif
                    ExifInterface exif = new ExifInterface(file.getAbsolutePath());
                    int orientation = exif.getAttributeInt(
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.ORIENTATION_NORMAL
                    );

                    Matrix matrix = new Matrix();

                    // Xoay trước
                    switch (orientation) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            matrix.postRotate(90);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            matrix.postRotate(180);
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            matrix.postRotate(270);
                            break;
                    }

                    // Lật gương nếu camera trước
                    if (isFrontCamera) {
                        matrix.postScale(-1, 1);
                    }

                    // Tạo bitmap mới
                    Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    Bitmap rotatedBitmap = Bitmap.createBitmap(
                            bitmap, 0, 0,
                            bitmap.getWidth(), bitmap.getHeight(),
                            matrix, true
                    );

                    saveBitmapToFile(rotatedBitmap, file);

                    activity.runOnUiThread(() -> {
                        closeCamera();
                        Intent intent = new Intent(activity, ShowImageActivity.class);
                        intent.putExtra("imagePath", file.getAbsolutePath());
                        intent.putExtra("userId", userId);
                        startActivity(intent);
                    });

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (image != null) image.close();
                }
            };


            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);

            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    captureSession = session;
                    Log.e("Cấu hình", "Cau hình thành công " + (captureSession != null));
                    try {
                        // Xử lý bật flash nếu có
                        Boolean hasFlash = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                        Log.e("Bat Flash", "Flash da bat: " + isFlashEnabled);
                        if (isFlashEnabled) {
                            if (hasFlash != null && hasFlash) {
                                CaptureRequest.Builder precaptureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                                precaptureBuilder.addTarget(reader.getSurface());
                                precaptureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                precaptureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                                Log.e("Cấu hình", "CaptureSession đã được tạo " + (captureSession != null));

                                session.capture(precaptureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                                    @Override
                                    public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                                                   @NonNull CaptureRequest request,
                                                                   @NonNull TotalCaptureResult result) {

                                        captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                                        captureBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER_START);
                                        Integer aeState = result.get(CaptureResult.CONTROL_AE_STATE);
                                        if (aeState == null || aeState == CaptureResult.CONTROL_AE_STATE_CONVERGED) {
                                            // AE đã ổn định, tiến hành chụp
                                            captureStillPicture(session, captureBuilder, characteristics);
                                        } else {
                                            // Nếu chưa ổn định, đợi thêm bằng cách gửi lại capture hoặc dùng handler delay
                                            if (mBackgroundHandler != null) {
                                                mBackgroundHandler.postDelayed(() -> {
                                                    captureStillPicture(session, captureBuilder, characteristics);
                                                }, 300);
                                            } else {
                                                // fallback: gọi trực tiếp (ít an toàn hơn)
                                                captureStillPicture(session, captureBuilder, characteristics);
                                            }
                                        }
                                    }
                                }, mBackgroundHandler);
                                captureStillPicture(session, captureBuilder, characteristics);
                            } else {
                                Toast.makeText(activity, "Camera không hỗ trợ flash!", Toast.LENGTH_SHORT).show();
                                captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                                captureStillPicture(session, captureBuilder, characteristics);
                            }
                        } else {
                            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                            captureStillPicture(session, captureBuilder, characteristics);
                        }
//                    } catch (CameraAccessException e) {
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Toast.makeText(activity, "Cấu hình chụp ảnh thất bại", Toast.LENGTH_SHORT).show();
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private void captureStillPicture(CameraCaptureSession session, CaptureRequest.Builder captureBuilder, CameraCharacteristics characteristics) {
        if (cameraDevice == null || session == null) {
            Log.e("Camera", "Không thể chụp vì camera đã đóng");
            return;
        }
        try {
            session.capture(captureBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                               @NonNull CaptureRequest request,
                                               @NonNull TotalCaptureResult result) {
                    Log.d("Camera", "Capture completed");
                }
            }, mBackgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    // Hàm hỗ trợ lưu ảnh
    private void saveBitmapToFile(Bitmap bitmap, File file) throws IOException {
        try (FileOutputStream out = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
        }
    }


    //    private int getJpegOrientation(CameraCharacteristics c, int deviceRotation) {
//        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);
//        int rotation = ORIENTATIONS.get(deviceRotation);
//
//        Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
//        if (lensFacing == null) return 0;
//
//        int jpegOrientation;
//        if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
//            jpegOrientation = (sensorOrientation + rotation) % 360;  // Chỉ tính cộng thôi
//            Log.e("Xoay ảnh này", "Giá trị khi xoay ảnh camera trước: " + jpegOrientation);
//
//        } else {
//            jpegOrientation = (sensorOrientation - rotation + 360) % 360;
//        }
//        return jpegOrientation;
//    }
    private int getJpegOrientation(CameraCharacteristics characteristics, int deviceRotation) {
        int sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
        Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
        if (facing == null) return 0;

        // map device rotation (0, 90, 180, 270)
        int rotation = ORIENTATIONS.get(deviceRotation);

        int orientation;
        if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
            orientation = (sensorOrientation - rotation + 360) % 360;
        } else {
            orientation = (sensorOrientation - rotation + 360) % 360;
        }

        return orientation;
    }
}
