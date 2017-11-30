package com.example.zainfo.liveness;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class VideoActivity extends AppCompatActivity {

    private static final int REQUEST_VIDEO_PERMISSIONS = 1;
    public static final String CAMERA_FRONT = "1";
    public static final String CAMERA_BACK = "0";
    private byte[] bytes;
    private int actionIndex;
    private final String[] types={"EyeBlink","MouthBlink","HeadNod","HeadLeft","HeadRight"};
    private final String[] typeTexts={"请眨眼","请张嘴","请点头","请向左转头","请向右转头"};
    private String judgeType;

    private boolean isRecording = false;
    private FloatingActionButton captureButton,switchButton,cancelButton;
    private int mCameraId;
    private RequestQueue mQueue;
    private RequestController mReqController;
    private SharedPreferences sp;
    private File mCurrentFile;
    private SurfaceView mSurfaceView;
    private Size mPreviewSize;
    private Camera.Size mSize;
    private FloatingActionButton mRecordButton;
    private FloatingActionButton mSwitchButton;
    private FloatingActionButton mCancelButton;
    private MediaRecorder mMediaRecorder;
    private SurfaceHolder mSurfaceHolder;
    private Camera mCamera;
    private String path;
    private TextView mCheckMark;
    private File mRotatedFile;
    private ProgressBar pb;
    private int mVideoTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent=getIntent();
        mVideoTime=Utils.getRecordTime();
        actionIndex=Utils.getActionIndex();
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.video_main);
        mReqController=RequestController.getInstance(this);
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
        mSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        mCheckMark = (TextView) findViewById(R.id.checkMark);
//        TextView mtextview=(TextView)findViewById(R.id.timeMark);
//        mtextview.setText(mVideoTime);
        pb=(ProgressBar)findViewById(R.id.progressBar);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(mSurfaceCallback);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mRecordButton=(FloatingActionButton)findViewById(R.id.recordButton);
        mSwitchButton=(FloatingActionButton)findViewById(R.id.switchCamera);
        mCancelButton=(FloatingActionButton)findViewById(R.id.cancelButton);
        mRecordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isRecording)
                    stopRecording();
                else{
                    initMediaRecorder();
                    startRecording();
                }
            }
        });
        mSwitchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                swtichCamera();
            }
        });
        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VideoActivity.this.finish();
            }
        });
    }
    @Override
    protected void onDestroy(){
        closeCamera();
        if(mCurrentFile!=null&&mCurrentFile.exists())
            mCurrentFile.delete();
        if(mRotatedFile!=null&&mRotatedFile.exists())
            mRotatedFile.delete();
        super.onDestroy();
    }
    private SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            openCamera(Integer.valueOf(CAMERA_BACK));
            mPreviewSize=new Size(width,height);
            startPreview();
        }
    };

    private void startPreview() {

        mCamera.setDisplayOrientation(90);
        Camera.Parameters parameters=mCamera.getParameters();
        Camera.Size size = getBestPreviewSize(mPreviewSize.getWidth(), mPreviewSize.getHeight(), parameters);
//        List<Camera.Size> mSupportedPreviewSizes=parameters.getSupportedPreviewSizes();
        if (size != null) {
            parameters.setPreviewSize(size.width, size.height);
        }
//        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
//        parameters.set("rotation",90);
//        mSize=Utils.getOptimalPreviewSize(mSupportedPreviewSizes,mSurfaceView.getWidth(),mSurfaceView.getHeight());
        parameters.setPreviewFrameRate(30);
        //设置相机预览方向
//        parameters.setPreviewSize(mSize.width,mSize.height);
        mCamera.setParameters(parameters);
        mSize=size;
        try {
            mCamera.setPreviewDisplay(mSurfaceHolder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        mCamera.startPreview();
    }
    private void openCamera(int position){
        if(mCamera!=null){
            Log.e("OpenCamera","cameraOpened");
            return;
        }
        mCamera=Camera.open(position);
        mCameraId=position;
        mCamera.setDisplayOrientation(90);
    }
    private Camera.Size getBestPreviewSize(int width, int height, Camera.Parameters parameters) {
        Camera.Size result = null;
        List<Camera.Size> sizes=parameters.getSupportedPreviewSizes();
        Camera.Size bestSize = sizes.get(0);
        int largestArea = bestSize.width * bestSize.height;
        for (Camera.Size s : sizes) {
            int area = s.width * s.height;
            if (area > largestArea) {
                bestSize = s;
                largestArea = area;
            }
        }
        return bestSize;
//        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
//            if (size.width <= width && size.height <= height) {
//                if (result == null) {
//                    result = size;
//                } else {
//                    int resultArea = result.width * result.height;
//                    int newArea = size.width * size.height;
//
//                    if (newArea > resultArea) {
//                        result = size;
//                    }
//                }
//            }
//        }
//
//        return result;
    }
    private void initMediaRecorder() {
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        File file = getVideoFile(this);
        if(mCurrentFile!=null&&mCurrentFile.exists())
            mCurrentFile.delete();
        mCurrentFile=file;
        mMediaRecorder.setOutputFile(file.getAbsolutePath());
        //mMediaRecorder.setOrientationHint(270);
        mMediaRecorder.setVideoSize(mSize.width,mSize.height);
        mMediaRecorder.setVideoEncodingBitRate(1600 * 1000);
        mMediaRecorder.setVideoFrameRate(30);
        mMediaRecorder.setMaxDuration(mVideoTime);
        mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());
        mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
            @Override
            public void onInfo(MediaRecorder mediaRecorder, int i, int i1) {
                if(i==MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED){
                    Log.e("MediaRecorder","3 seconds reached");
                    stopRecording();
                }
            }
        });
    }
    public File getVideoFile(Context context) {
        File file;
        //file=new File("/storage/emulated/0/DCIM/testv3.mp4");
        file = new File(context.getExternalFilesDir("video"),String.valueOf(new Date().getTime()) + ".mp4");
        return file;
    }
    private void startRecording() {
        if (mMediaRecorder != null) {
            try {
                mMediaRecorder.prepare();
                judgeType = types[actionIndex];
                mCheckMark.setText(typeTexts[actionIndex]);
                mCheckMark.setVisibility(View.VISIBLE);
                mCancelButton.setEnabled(false);
                mMediaRecorder.start();
            } catch (Exception e) {
                isRecording = false;
                Log.e("startRecording", e.getMessage());
            }

            isRecording = true;
        }
    }
    private void stopRecording(boolean flag) {
        if(isRecording) {
            isRecording = false;
            if (mMediaRecorder != null) {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
        }
        mCheckMark.setVisibility(View.INVISIBLE);
        mCancelButton.setEnabled(true);
        if(flag)
            rotateVideo();
        closeCamera();
        if(flag) {
            openCamera(mCameraId);
            startPreview();
        }
    }
    private void stopRecording(){
        stopRecording(true);
    }
    private void closeCamera(){
        if(mCamera!=null){
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera=null;
        }
    }
    private void swtichCamera(){
        if(isRecording){
            stopRecording();
        }
        if(mCamera==null)
            return;
        int position;
        if(mCameraId==0){
            position=1;
        }else{
            position=0;
        }
        closeCamera();
        openCamera(position);
        startPreview();
    }
    public void rotateVideo(){
        FFmpeg ffmpeg = FFmpeg.getInstance(this);
        mRotatedFile=getVideoFile(this);
//      如果需要左右反转前置摄像头，将下面一行代码中的"cclock"替换为"cclock_flip"即可
        final String[] cmd={"-i",mCurrentFile.getAbsolutePath(),"-c:v", "libx264" ,"-preset", "ultrafast","-vf","transpose="+(mCameraId==0?"clock":"cclock"), "-strict", "-2",mRotatedFile.getAbsolutePath()};
        try {
            // to execute "ffmpeg -version" command you just need to pass "-version"
            ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                @Override
                public void onStart() {
                    pb.setVisibility(View.VISIBLE);
                }

                @Override
                public void onProgress(String message) {}

                @Override
                public void onFailure(String message) {
                    mCurrentFile.delete();
                }

                @Override
                public void onSuccess(String message) {
                    System.out.println("test");
                    mReqController.addToRequestQueue(getCustomReq());
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegCommandAlreadyRunningException e) {
            // Handle if FFmpeg is already running
            e.printStackTrace();
        }
    }
    public CustomRequest getCustomReq() {


        final String url = "http://192.168.26.90:3249/";
        final String route = "ActionDetect";

        CustomRequest customReq = new CustomRequest(Request.Method.POST, url + route,

                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        String str = new String(response.data);
                        pb.setVisibility(View.INVISIBLE);

                        try {
                            JSONObject res = new JSONObject(str);
                            String result = res.getString("result");
                            String detail = res.getString("detail");

                            String show = " 检测结果 :" + result + "\n 详细信息 :" + detail;
                            new AlertDialog.Builder(VideoActivity.this).setMessage(show).setPositiveButton("确定", null).show();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                pb.setVisibility(View.INVISIBLE);
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                new AlertDialog.Builder(VideoActivity.this).setTitle("Error!").setMessage(errorMessage).setPositiveButton("确定",null).show();
                error.printStackTrace();
            }
        }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("type", judgeType);
                params.put("phone", "Android");
                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                try {
                    params.put("video", new DataPart("test.mp4", FileUtils.readFileToByteArray(mRotatedFile), "video/mpeg_4"));
                    mRotatedFile.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        return customReq;
    }
}
