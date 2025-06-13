package com.hucmuaf.locket_mobile;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
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
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.GestureDetector;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.impl.utils.CompareSizesByArea;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class TakeActivity extends AppCompatActivity {
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

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_layout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Tìm LinearLayout với id="friends" và thêm sự kiện onClick
        LinearLayout friendsLayout = findViewById(R.id.friends);
        if (friendsLayout != null) {
            Log.d(TAG, "Found friends layout, setting click listener");

            // Đảm bảo layout có thể click được
            friendsLayout.setClickable(true);
            friendsLayout.setFocusable(true);

            friendsLayout.setOnClickListener(v -> {
                Log.d(TAG, "Friends layout clicked, starting ListFriendActivity");
                try {
                    Intent intent = new Intent(TakeActivity.this, ListFriendActivity.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.e(TAG, "Error starting ListFriendActivity", e);
                }
            });
        } else {
            Log.e(TAG, "Could not find LinearLayout with id 'friends'");
        }

        View main = findViewById(R.id.main_layout);
        gestureDetector = new GestureDetector(this, new SwipeGestureListenerUp(this));
        main.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        startBackgroundThread();

        textureView = findViewById(R.id.camera_preview);


        textureView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            this.width = textureView.getWidth();
            this.height = textureView.getHeight();

            openCamera(width, height);
            Log.e("Check width", "Width textureView: " + width);
            Log.e("Check height", "Height textureView: " + height);

            ImageView cached = findViewById(R.id.cached);
            cached.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onPause();
                    isFrontCamera = !isFrontCamera; // đổi chiều
                    openCamera(width, height);
                }
            });


            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            textureView.setSurfaceTextureListener(surfaceTextureListener);

            take_layout = findViewById(R.id.take_layout);
            take_layout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    takePicture();
                }
            });
            LinearLayout history = findViewById(R.id.history);
            history.setOnClickListener(v -> {
                startActivityWithAnimation(this, ReactActivity.class, R.anim.slide_up);
            });

            ImageView flash = findViewById(R.id.flash);
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

    private void startActivityWithAnimation(Context context, Class<?> cls, int animEnter) {
        if (context == null) return;

        Intent intent = new Intent(context, cls);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
        if (context instanceof Activity) {
            ((Activity) context).overridePendingTransition(animEnter, R.anim.no_animation);
        }
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


    private String getCameraId(boolean useFrontCamera, int width, int height) throws CameraAccessException {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
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
                    openCamera(width, height);
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
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        try {
            cameraId = getCameraId(isFrontCamera, width, height);
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 101);
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
            assert texture != null;
            texture.setDefaultBufferSize(1080, 1920); // hoặc dùng kích thước camera output phù hợp
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);

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
                    Toast.makeText(TakeActivity.this, "Cấu hình camera thất bại!", Toast.LENGTH_SHORT).show();
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
        stopBackgroundThread();
        super.onPause();
    }

    @Override
    protected void onResume() {
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
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
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
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int jpegOrientation = getJpegOrientation(characteristics, rotation);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation);

            // Lưu ảnh
            final File file = new File(getExternalFilesDir(null), "pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = reader1 -> {
                Image image = null;
                try {
                    image = reader1.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.remaining()];
                    buffer.get(bytes);

                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    int actualRotation = getJpegOrientation(characteristics, getWindowManager().getDefaultDisplay().getRotation());

                    Matrix matrix = new Matrix();
                    if (actualRotation != 0) {
                        matrix.postRotate(actualRotation);
                    }
                    if (isFrontCamera) {
                        matrix.postScale(-1, 1);
                    }

                    bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                    saveBitmapToFile(bitmap, file);

                    runOnUiThread(() -> {
                        cameraDevice.close();
                        Intent intent = new Intent(TakeActivity.this, ShowImageActivity.class);
                        intent.putExtra("imagePath", file.getAbsolutePath());
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
                                Toast.makeText(TakeActivity.this, "Camera không hỗ trợ flash!", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(TakeActivity.this, "Cấu hình chụp ảnh thất bại", Toast.LENGTH_SHORT).show();
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


    private int getJpegOrientation(CameraCharacteristics c, int deviceRotation) {
        int sensorOrientation = c.get(CameraCharacteristics.SENSOR_ORIENTATION);
        int rotation = ORIENTATIONS.get(deviceRotation);

        Integer lensFacing = c.get(CameraCharacteristics.LENS_FACING);
        if (lensFacing == null) return 0;

        int jpegOrientation;
        if (lensFacing == CameraCharacteristics.LENS_FACING_FRONT) {
            jpegOrientation = (sensorOrientation + rotation) % 360;  // Chỉ tính cộng thôi
            Log.e("Xoay ảnh này", "Giá trị khi xoay ảnh camera trước: " + jpegOrientation);

        } else {
            jpegOrientation = (sensorOrientation - rotation + 360) % 360;
        }
        return jpegOrientation;
    }
}
