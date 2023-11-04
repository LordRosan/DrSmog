package com.jlu.drsmog;

import static androidx.core.content.PermissionChecker.PERMISSION_GRANTED;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.Size;
import android.util.SparseArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CamActivity extends AppCompatActivity {

    private static final int CAMERA_REQUEST_CODE = 100;
    private static final int READ_REQUEST_CODE = 200;
    private static final int MANAGE_REQUEST_CODE = 1024;

    private boolean isRefuse = false;

    final int PHOTO_HEIGHT = 1280; // 默认图片高度
    final int PHOTO_WIDTH = 720; // 默认图片宽度

    private TextureView cam_preview;

    private ImageButton btn_back;
    private ImageButton btn_capture;
    private ImageButton btn_import;

    private CameraDevice cameraDevice;
    private String cameraId;
    private CameraCaptureSession captureSession;
    private CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private CameraManager cameraManager;
    private File file;
    private ImageReader reader;
    private Cursor actualImageCursor;

    private static final SparseArray<Integer> ORIENTATIONS = new SparseArray<>();
    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 0);
        ORIENTATIONS.append(Surface.ROTATION_90, 90);
        ORIENTATIONS.append(Surface.ROTATION_180, 180);
        ORIENTATIONS.append(Surface.ROTATION_270, 270);
    }


    String imagePath;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] != PERMISSION_GRANTED) {
                    Toast.makeText(this, "Camera permission is required!", Toast.LENGTH_LONG).show();
                } else {
                    openCamera();
                }
                break;
            case READ_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] != PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read permission is required!", Toast.LENGTH_LONG).show();
                }
                break;
            case MANAGE_REQUEST_CODE:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if(Environment.isExternalStorageManager()) {
                        isRefuse = false; // 授权成功
                    } else {
                        isRefuse = true; // 授权失败
                    }
                }
            default:
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && !isRefuse) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1024);
            }
        }

        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        cam_preview = findViewById(R.id.cam_preview);
        cam_preview.setSurfaceTextureListener(textureListener);

        btn_back = findViewById(R.id.btn_back);
        btn_capture = findViewById(R.id.btn_capture);
        btn_import = findViewById(R.id.btn_import);

        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(CamActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        btn_capture.setOnClickListener(v -> {
            if (cameraDevice == null) {
                return;
            }

            try {
                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraDevice.getId());
                Size[] jpegSizes;
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                int height = PHOTO_HEIGHT;
                int width = PHOTO_WIDTH;
                if (jpegSizes != null && jpegSizes.length > 0) {
                    height = jpegSizes[0].getHeight();
                    width = jpegSizes[0].getWidth();
                }
                List<Surface> outputSurfaces;
                final CaptureRequest.Builder captureBuilder;
                reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
                outputSurfaces = new ArrayList<>(2);
                outputSurfaces.add(reader.getSurface());
                outputSurfaces.add(new Surface(cam_preview.getSurfaceTexture()));

                captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
                captureBuilder.addTarget(reader.getSurface());
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

                int rotation = getRotationCompensation(cameraId, this, getApplicationContext());
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

                long timestamp = System.currentTimeMillis();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                String formattedDateTime = simpleDateFormat.format(new Date(timestamp));

                File folder = new File(Environment.getExternalStorageDirectory() + "/DrSmog");
                if (!folder.exists()) {
                    folder.mkdirs();
                }
                imagePath = folder + "/image_" + formattedDateTime + ".jpg";
                file = new File(imagePath);

                ImageReader.OnImageAvailableListener readListener = new ImageReader.OnImageAvailableListener() {
                    @Override
                    public void onImageAvailable(ImageReader imageReader) {
                        Image image = null;
                        try {
                            image = imageReader.acquireLatestImage();
                            ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                            byte[] bytes = new byte[buffer.capacity()];
                            buffer.get(bytes);
                            save(bytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            if (image != null) {
                                image.close();
                            }
                        }
                    }

                    private void save(byte[] bytes) throws IOException {
                        OutputStream outputStream = null;
                        try {
                            outputStream = new FileOutputStream(file);
                            outputStream.write(bytes);
                        } finally {
                            if (outputStream != null) {
                                outputStream.close();
                            }
                        }
                    }
                };

                reader.setOnImageAvailableListener(readListener, null);

                final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);
                        int delayMillis = 1000; // 2000ms = 2s
                        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (imagePath != null) {
                                    Intent intent = new Intent(CamActivity.this, CropActivity.class);
                                    intent.putExtra("isPath", true);
                                    intent.putExtra("image_path", imagePath);
                                    startActivity(intent);
                                }
                            }
                        }, delayMillis);
                    }
                };

                cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                        try {
                            cameraCaptureSession.capture(captureBuilder.build(), captureListener, null);
                        } catch (CameraAccessException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                    }
                }, null);
            } catch (CameraAccessException e) {
                throw new RuntimeException(e);
            }
        });

        btn_import.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(getString(R.string.dialog_select_image_src));
            String[] options = {getString(R.string.opt_gallery), getString(R.string.opt_file_manager)};
            builder.setItems(options, (dialog, which) -> {
               if (which == 0) {
                   openGallery();
               } else {
                   openFilePicker();
               }
            });
            builder.show();
        });
    }

    private int getRotationCompensation(String cameraId, Activity activity, Context context) throws CameraAccessException {
        // 获取设备的方向
        int deviceRotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int rotationCompensation = ORIENTATIONS.get(deviceRotation);

        // 获取摄像头的方向
        CameraManager cameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        int sensorOrientation = cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.SENSOR_ORIENTATION);

        // 为前置摄像头做调整
        if (cameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
            rotationCompensation = (sensorOrientation + rotationCompensation) % 360;
        } else {  // 后置摄像头
            rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360;
        }

        return rotationCompensation;
    }

    private TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
        }

        @Override
        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
            closeCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
        }
    };

    private void openCamera() {
        try {
            cameraId = cameraManager.getCameraIdList()[0];
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];

            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_REQUEST_CODE);
                return;
            }

            cameraManager.openCamera(cameraId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    cameraDevice = camera;
                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    cameraDevice.close();
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    cameraDevice.close();
                    cameraDevice = null;
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startPreview() {
        if (cameraDevice == null || !cam_preview.isAvailable() || imageDimension == null) {
            return;
        }
        SurfaceTexture texture = cam_preview.getSurfaceTexture();
        texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
        Surface surface = new Surface(texture);
        try {
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    if (cameraDevice == null) return;
                    captureSession = session;
                    try {
                        captureSession.setRepeatingRequest(captureRequestBuilder.build(), null, null);
                    } catch (CameraAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (captureSession != null) {
            captureSession.close();
            captureSession = null;
        }
        if (cameraDevice != null) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }

    private static final int PICK_IMAGE_FROM_GALLERY = 1;
    private static final int PICK_IMAGE_FROM_FILE = 2;

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY);
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_FROM_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            Intent intent = new Intent(CamActivity.this, CropActivity.class);
            intent.putExtra("isPath", false);
            intent.putExtra("image_uri", selectedImageUri.toString());
            startActivity(intent);
        }
    }
}