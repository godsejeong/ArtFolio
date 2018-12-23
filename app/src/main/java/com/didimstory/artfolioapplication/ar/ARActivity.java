/*
 * Copyright (c) 2017-present, Viro, Inc.
 * All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.didimstory.artfolioapplication.ar;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.didimstory.artfolioapplication.R;
import com.didimstory.artfolioapplication.model.Product;
import com.didimstory.artfolioapplication.model.ViroHelper;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Session;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import com.viro.core.ARAnchor;
import com.viro.core.ARHitTestListener;
import com.viro.core.ARHitTestResult;
import com.viro.core.ARNode;
import com.viro.core.ARScene;
import com.viro.core.AmbientLight;
import com.viro.core.AsyncObject3DListener;
import com.viro.core.ClickListener;
import com.viro.core.ClickState;
import com.viro.core.DragListener;
import com.viro.core.GesturePinchListener;
import com.viro.core.GestureRotateListener;
import com.viro.core.Material;
import com.viro.core.Node;
import com.viro.core.Object3D;
import com.viro.core.PinchState;
import com.viro.core.Portal;
import com.viro.core.PortalScene;
import com.viro.core.Texture;
import com.viro.core.ViroView;
import com.viro.core.RendererConfiguration;
import com.viro.core.RotateState;
import com.viro.core.Spotlight;
import com.viro.core.Surface;
import com.viro.core.Vector;
import com.viro.core.ViroMediaRecorder;
import com.viro.core.ViroViewARCore;

import java.io.File;
import java.util.Arrays;
import java.util.List;


public class ARActivity extends AppCompatActivity {
    private static final int RECORD_PERM_KEY = 50;
    private static final String TAG = ARActivity.class.getSimpleName();
    final public static String INTENT_PRODUCT_KEY = "product_key";

    private boolean installRequested;

    private ViroViewARCore mViroView;
    private ARScene mScene;
    private View mHudGroupView;
    private TextView mHUDInstructions, SubText;
    private ImageView mCameraButton;
    private View mIconShakeView, ARstateView;
    private float rotateStart, scaleStart;

    /*
     추적상태를 나타내는거
     3D 컨트롤과 HUD의 표시를 조정하는데 사용
     추적된 AR Scene을 둘러보는 UI.
     */
    private enum TRACK_STATUS {
        FINDING_SURFACE,
        SURFACE_NOT_FOUND,
        SURFACE_FOUND,
        SELECTED_SURFACE
    }

    private TRACK_STATUS mStatus = TRACK_STATUS.SURFACE_NOT_FOUND;
    private Product mSelectedProduct = null;
    private Node mProductModelGroup = null;
    private Node mCrosshairModel = null;
    private AmbientLight mMainLight = null;
    private Vector mLastProductRotation = new Vector();
    private Vector mSavedRotateToRotation = new Vector();
    private ARHitTestListenerCrossHair mCrossHairHitTest = null;
    private Session session;

    /*
     * ARNode를 통해 3D 가구 모델을 생성된다.
     * 사용자가 가구를 배치할 표면을 선택한 경우 사용하면 안됨.
     */

    private ARNode mHitARNode = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ARInstall();
        RendererConfiguration config = new RendererConfiguration();
        config.setShadowsEnabled(true);
        config.setBloomEnabled(true);
        config.setHDREnabled(true);
        config.setPBREnabled(true);

        try {
            mViroView = new ViroViewARCore(this, new ViroViewARCore.StartupListener() {
                @Override
                public void onSuccess() {
                    displayScene();
                }

                @Override
                public void onFailure(ViroViewARCore.StartupError error, String errorMessage) {
                    Log.e(TAG, "Failed to load AR Scene [" + errorMessage + "]");
                }
            }, config);
            setContentView(mViroView);

            Intent intent = getIntent();
            String key = intent.getStringExtra(INTENT_PRODUCT_KEY);
            ProductApplicationContext context = (ProductApplicationContext) getApplicationContext();
            mSelectedProduct = context.getProductDB().getProductByName(key);

            View.inflate(this, R.layout.activity_ar, ((ViewGroup) mViroView));
            mHudGroupView = (View) findViewById(R.id.main_hud_layout);
            mHudGroupView.setVisibility(View.GONE);

        } catch (com.viro.core.DeviceNotCompatibleException e) {
            Log.e("DeviceNotCompatibleException", String.valueOf(e));
        }
    }


    private void ARInstall() {

        if (session == null) {
            Exception exception = null;
            String message = null;
            try {
                switch (ArCoreApk.getInstance().requestInstall(this, !installRequested)) {
                    case INSTALL_REQUESTED:
                        installRequested = true;
                        return;
                    case INSTALLED:
                        break;
                }

                // Create the session.
                session = new Session(/* context= */ this);

            } catch (UnavailableArcoreNotInstalledException
                    | UnavailableUserDeclinedInstallationException e) {
                message = "Please install ARCore";
                exception = e;
            } catch (UnavailableApkTooOldException e) {
                message = "Please update ARCore";
                exception = e;
            } catch (UnavailableSdkTooOldException e) {
                message = "Please update this app";
                exception = e;
            } catch (UnavailableDeviceNotCompatibleException e) {
                message = "This device does not support AR";
                exception = e;
            } catch (NullPointerException e) {
                message = "Failed to create AR session";
                exception = e;
            } catch (Exception e) {
                message = "Failed to create AR session";
                exception = e;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mViroView.onActivityStarted(this);
        ARInstall();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViroView.onActivityResumed(this);
        ARInstall();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViroView.onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mViroView.onActivityStopped(this);
    }

    @Override
    protected void onDestroy() {
        ((ViroViewARCore) mViroView).setCameraARHitTestListener(null);
        mViroView.onActivityDestroyed(this);
        super.onDestroy();
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                RECORD_PERM_KEY);
    }

    private static boolean hasRecordingStoragePermissions(Context context) {
        boolean hasExternalStoragePerm = ContextCompat.checkSelfPermission(context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        return hasExternalStoragePerm;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (requestCode == RECORD_PERM_KEY && !hasRecordingStoragePermissions(ARActivity.this)) {
            Toast toast = Toast.makeText(ARActivity.this, "User denied permissions", Toast.LENGTH_LONG);
            toast.show();
        }
    }

    private void displayScene() {
        // AR을 로드할 Scene구현
        mScene = new ARScene();
        mMainLight = new AmbientLight(Color.parseColor("#606060"), 400);
        mMainLight.setInfluenceBitMask(3);
        mScene.getRootNode().addLight(mMainLight);

        //3D,HUD 배치 관련 컨트롤러들
        initARCrosshair();
        init3DModelProduct();
        initARHud();

        // Scene을 체크준비가 하면 UI체크를 함
        mScene.setListener(new ARSceneListener());

        // 마지막으로 렌더 arcore를 설정한다.
        mViroView.setScene(mScene);
    }

    private void initARHud() {
        mHUDInstructions = (TextView) mViroView.findViewById(R.id.ar_hud_instructions);
        mViroView.findViewById(R.id.bottom_frame_controls).setVisibility(View.VISIBLE);

        // Bind the back button on the top left of the layout
        ImageView view = (ImageView) findViewById(R.id.ar_back_button);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ARActivity.this.finish();
            }
        });

        // Bind the detail buttons on the top right of the layout.


        // Bind the camera button on the bottom, for taking images.
        mCameraButton = (ImageView) mViroView.findViewById(R.id.ar_photo_button);
        SubText = mViroView.findViewById(R.id.ar_hub_seb);
        final File photoFile = new File(getFilesDir(), "screenShot");
        mCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!hasRecordingStoragePermissions(getBaseContext())) {
                    requestPermissions();
                    return;
                }

                mViroView.getRecorder().takeScreenShotAsync("screenShot", true, new ViroMediaRecorder.ScreenshotFinishListener() {
                    @Override
                    public void onSuccess(Bitmap bitmap, String s) {
                        final Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/png");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(s));
                        startActivity(Intent.createChooser(shareIntent, "Share image using"));
                    }

                    @Override
                    public void onError(ViroMediaRecorder.Error error) {
                        Log.e("Viro", "onTaskFailed " + error.toString());
                    }
                });
            }
        });

        mIconShakeView = mViroView.findViewById(R.id.icon_shake_phone);
        ARstateView = mViroView.findViewById(R.id.instructions_group);
    }

    private void initARCrosshair() {
        if (mCrosshairModel != null) {
            return;
        }

        AmbientLight am = new AmbientLight();
        am.setInfluenceBitMask(2);
        am.setIntensity(1000);
        mScene.getRootNode().addLight(am);

        final Object3D crosshairModel = new Object3D();
        mScene.getRootNode().addChildNode(crosshairModel);

        //공간 인식 성공
        //ARModle를 배치할 곳에 tracking이미지를 띄움
        crosshairModel.loadModel(mViroView.getViroContext(), Uri.parse("file:///android_asset/tracking_1.vrx"), Object3D.Type.FBX, new AsyncObject3DListener() {
            @Override
            public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                mCrosshairModel = object3D;
                mCrosshairModel.setOpacity(0);
                object3D.setLightReceivingBitMask(2);
                mCrosshairModel.setScale(new Vector(0.175, 0.175, 0.175));
                mCrosshairModel.setClickListener(new ClickListener() {
                    @Override
                    public void onClick(int i, Node node, Vector vector) {
                        setTrackingStatus(TRACK_STATUS.SELECTED_SURFACE);
                    }

                    @Override
                    public void onClickState(int i, Node node, ClickState clickState, Vector vector) {
                        // No-op
                    }
                });
            }

            @Override
            public void onObject3DFailed(String error) {
                Log.e("Viro", " Model load failed : " + error);
            }
        });
    }

    private void init3DModelProduct() {
        // Create our group node containing the light, shadow plane, and 3D models
        mProductModelGroup = new Node();

        // Create a light to be shined on the model.
        Spotlight spotLight = new Spotlight();
        spotLight.setInfluenceBitMask(1);
        spotLight.setPosition(new Vector(0, 5, 0));
        spotLight.setCastsShadow(true);
        spotLight.setAttenuationEndDistance(7);
        spotLight.setAttenuationStartDistance(4);
        spotLight.setDirection(new Vector(0, -1, 0));
        spotLight.setIntensity(6000);
        spotLight.setShadowOpacity(0.35f);
        mProductModelGroup.addLight(spotLight);

        // Create a mock shadow plane in AR
        Node shadowNode = new Node();
        Surface shadowSurface = new Surface(20, 20);
        Material material = new Material();
        material.setShadowMode(Material.ShadowMode.TRANSPARENT);
        material.setLightingModel(Material.LightingModel.LAMBERT);
        shadowSurface.setMaterials(Arrays.asList(material));
        shadowNode.setGeometry(shadowSurface);
        shadowNode.setLightReceivingBitMask(1);
        shadowNode.setPosition(new Vector(0, -0.01, 0));
        shadowNode.setRotation(new Vector(-1.5708, 0, 0));
        // We want the shadow node to ignore all events because it contains a surface of size 20x20
        // meters and causes this to capture events which will bubble up to the mProductModelGroup node.
        shadowNode.setIgnoreEventHandling(true);
        mProductModelGroup.addChildNode(shadowNode);

        final Bitmap bot = ViroHelper.getBitmapFromAsset(ARActivity.this, "frame1.png");

        final Object3D productModel = new Object3D();

        // Load the model from the given mSelected Product
        productModel.loadModel(mViroView.getViroContext(), Uri.parse("file:///android_asset/frame_02.obj"), Object3D.Type.OBJ, new AsyncObject3DListener() {
            @Override
            public void onObject3DLoaded(Object3D object3D, Object3D.Type type) {
                Texture objectTexture = new Texture(bot, Texture.Format.RGBA8, false, false);
                Material material = new Material();
                material.setDiffuseTexture(objectTexture);
                object3D.getGeometry().setMaterials(Arrays.asList(material));
                object3D.setLightReceivingBitMask(1);
                mProductModelGroup.setOpacity(1);
                mProductModelGroup.setScale(new Vector(0.1, 0.1, 0.1));
                mLastProductRotation = object3D.getRotationEulerRealtime();
            }

            @Override
            public void onObject3DFailed(String error) {
                Log.e("Viro", " Model load failed : " + error);
            }
        });

        // Make this 3D Product object draggable.
        mProductModelGroup.setDragType(Node.DragType.FIXED_DISTANCE_ORIGIN);

        mProductModelGroup.setDragListener(new DragListener() {
            @Override
            public void onDrag(int i, Node node, Vector vector, Vector vector1) {
                Log.e("ondrag", String.valueOf(i));
                // No-op
            }
        });

        //크기조정 및 위치조정
        productModel.setGestureRotateListener(new GestureRotateListener() {
            @Override
            public void onRotate(int i, Node node, float rotation, RotateState rotateState) {
                if (rotateState == RotateState.ROTATE_START) {
                    rotateStart = productModel.getRotationEulerRealtime().y;
                }
                float totalRotationY = rotateStart + rotation;
                mProductModelGroup.setRotation(new Vector(0, totalRotationY, 0));
            }
        });


        productModel.setGesturePinchListener(new GesturePinchListener() {
            @Override
            public void onPinch(int i, Node node, float scale, PinchState pinchState) {
                if (pinchState == PinchState.PINCH_START) {
                    scaleStart = productModel.getScaleRealtime().x;
                } else {
                    productModel.setScale(new Vector(scaleStart * scale, scaleStart * scale, scaleStart * scale));
                }
            }
        });

        mProductModelGroup.setOpacity(0);
        mProductModelGroup.addChildNode(productModel);
    }

    private void setTrackingStatus(TRACK_STATUS status) {
        if (mStatus == TRACK_STATUS.SELECTED_SURFACE || mStatus == status) {
            return;
        }

        // If the surface has been selected, we no longer need our cross hair listener.
        if (status == TRACK_STATUS.SELECTED_SURFACE) {
            ((ViroViewARCore) mViroView).setCameraARHitTestListener(null);
        }

        mStatus = status;
        updateUIHud();
        update3DARCrosshair();
        update3DModelProduct();
    }

    private void updateUIHud() {
        switch (mStatus) {
            case FINDING_SURFACE:
//                mHUDInstructions.setText("Point the camera at the flat surface where you want to view your product.");
                mHUDInstructions.setText("카메라를 평평한 표면에 놓으세요");
                SubText.setText("Step 1");
                break;
            case SURFACE_NOT_FOUND:
//                mHUDInstructions.setText("We can’t seem to find a surface. Try moving your phone more in any direction.");
                mHUDInstructions.setText("인식 가능한 표면을 찾을수 없습니다. 카메라를 다른방향으로 움직여 주십시오");
                break;
            case SURFACE_FOUND:
//                mHUDInstructions.setText("Great! Now tap where you want to see the product.");
                mHUDInstructions.setText("그림을 배치하고 싶은곳을 터치하세요");
                SubText.setText("Step 2");
                break;
            case SELECTED_SURFACE:
//                mHUDInstructions.setText("Great! Use one finger to move and two fingers to rotate.");
                mHUDInstructions.setText("두손가락으로 크기와 위치를 조정하고 한손가락으로 위치를 조정하세요");
                SubText.setText("Step 3");
                break;
            default:
//                mHUDInstructions.setText("Initializing AR....");
                mHUDInstructions.setText("셋팅중입니다...");
        }

        // 카메라(공유) 버튼 나오게 하는거
        if (mStatus == TRACK_STATUS.SELECTED_SURFACE) {
            int a = (int) getResources().getDimension(R.dimen.arstatereplace_height);
            ARstateView.getLayoutParams().height = a;
            Log.e("state2", String.valueOf(a));
            mCameraButton.setVisibility(View.VISIBLE);
        } else {
            mCameraButton.setVisibility(View.GONE);
            ARstateView.getLayoutParams().height = (int) getResources().getDimension(R.dimen.arstatebasic_height);
            Log.e("state", String.valueOf(ARstateView.getHeight()));
        }

        // 흔드는 표시 나오게 하는거
        if (mStatus == TRACK_STATUS.SURFACE_NOT_FOUND) {
            mIconShakeView.setVisibility(View.VISIBLE);
//            365
        } else {
            mIconShakeView.setVisibility(View.GONE);
        }
    }

    private void update3DARCrosshair() {
        switch (mStatus) {
            case FINDING_SURFACE:
            case SURFACE_NOT_FOUND:
            case SELECTED_SURFACE:
                mCrosshairModel.setOpacity(0);
                break;
            case SURFACE_FOUND:
                mCrosshairModel.setOpacity(1);
                break;
        }

        if (mStatus == TRACK_STATUS.SELECTED_SURFACE && mCrossHairHitTest != null) {
            mCrossHairHitTest = null;
            ((ViroViewARCore) mViroView).setCameraARHitTestListener(null);
        } else if (mCrossHairHitTest == null) {
            mCrossHairHitTest = new ARHitTestListenerCrossHair();
            ((ViroViewARCore) mViroView).setCameraARHitTestListener(mCrossHairHitTest);
        }
    }

    private void update3DModelProduct() {
        // Hide the product if the user has not placed it yet.
        if (mStatus != TRACK_STATUS.SELECTED_SURFACE) {
            mProductModelGroup.setOpacity(0);
            return;
        }

        if (mHitARNode != null) {
            return;
        }

        mHitARNode = mScene.createAnchoredNode(mCrosshairModel.getPositionRealtime());
        mHitARNode.addChildNode(mProductModelGroup);
        mProductModelGroup.setOpacity(1);
    }

    private class ARHitTestListenerCrossHair implements ARHitTestListener {
        @Override
        public void onHitTestFinished(ARHitTestResult[] arHitTestResults) {
            if (arHitTestResults == null || arHitTestResults.length <= 0) {
                return;
            }

            // If we have found intersected AR Hit points, update views as needed, reset miss count.
            ViroViewARCore viewARView = (ViroViewARCore) mViroView;
            final Vector cameraPos = viewARView.getLastCameraPositionRealtime();

            // Grab the closest ar hit target
            float closestDistance = Float.MAX_VALUE;
            ARHitTestResult result = null;
            for (int i = 0; i < arHitTestResults.length; i++) {
                ARHitTestResult currentResult = arHitTestResults[i];

                float distance = currentResult.getPosition().distance(cameraPos);
                if (distance < closestDistance && distance > .3 && distance < 5) {
                    result = currentResult;
                    closestDistance = distance;
                }
            }

            // Update the cross hair target location with the closest target.
            if (result != null) {
                mCrosshairModel.setPosition(result.getPosition());
                mCrosshairModel.setRotation(result.getRotation());
            }

            // Update State based on hit target
            if (result != null) {
                setTrackingStatus(TRACK_STATUS.SURFACE_FOUND);
            } else {
                setTrackingStatus(TRACK_STATUS.FINDING_SURFACE);
            }
        }
    }

    protected class ARSceneListener implements ARScene.Listener {
        private boolean mInitialized;

        public ARSceneListener() {
            mInitialized = false;
        }

        @Override
        public void onTrackingInitialized() {
            // This method is deprecated.
        }

        @Override
        public void onTrackingUpdated(ARScene.TrackingState trackingState, ARScene.TrackingStateReason trackingStateReason) {
            if (trackingState == ARScene.TrackingState.NORMAL && !mInitialized) {
                // The Renderer is ready - turn everything visible.
                mHudGroupView.setVisibility(View.VISIBLE);

                // Update our UI views to the finding surface state.
                setTrackingStatus(TRACK_STATUS.FINDING_SURFACE);
                mInitialized = true;
            }
        }

        @Override
        public void onAmbientLightUpdate(float lightIntensity, Vector v) {
            // no-op
        }

        @Override
        public void onAnchorFound(ARAnchor anchor, ARNode arNode) {
            // no-op
        }

        @Override
        public void onAnchorUpdated(ARAnchor anchor, ARNode arNode) {
            // no-op
        }

        @Override
        public void onAnchorRemoved(ARAnchor anchor, ARNode arNode) {
            // no-op
        }
    }

}
