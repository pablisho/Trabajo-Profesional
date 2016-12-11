package ar.uba.fi.prm.arbuy.tango;

import com.google.atap.tango.mesh.TangoMesh;
import com.google.atap.tangoservice.TangoCameraIntrinsics;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import ar.uba.fi.prm.arbuy.R;
import ar.uba.fi.prm.arbuy.loader.LoaderOBJ;
import ar.uba.fi.prm.arbuy.loader.ParsingException;
import ar.uba.fi.prm.arbuy.meshing.GridIndex;
import ar.uba.fi.prm.arbuy.meshing.MeshSegment;
import ar.uba.fi.prm.arbuy.opengl.DepthTexture;
import ar.uba.fi.prm.arbuy.opengl.Object3D;
import ar.uba.fi.prm.arbuy.opengl.OpenGlCameraPreview;
import ar.uba.fi.prm.arbuy.opengl.OpenGlSphere;

/**
 * An OpenGL renderer that renders the Tango RGB camera texture as a background and an earth sphere.
 * It also renders a depth texture to occlude the sphere if it is behind an object.
 */
public class OcclusionRenderer implements GLSurfaceView.Renderer {
    private static final String TAG = "OcclusionRenderer";

    /**
     * A small callback to allow the caller to introduce application-specific code to be executed
     * in the OpenGL thread.
     */
    public interface RenderCallback {
        void preRender();
    }

    @SuppressLint("UseSparseArrays")
    private final HashMap<GridIndex, MeshSegment> mMeshMap = new HashMap<GridIndex, MeshSegment>();

    private RenderCallback mRenderCallback;
    private DepthTexture mDepthTexture;
    private OpenGlCameraPreview mOpenGlCameraPreview;
    private OpenGlSphere mOpenGlSphere;
    private Context mContext;
    private boolean mProjectionMatrixConfigured;

    private float[] mViewMatrix = new float[16];
    private float[] mProjectionMatrix = new float[16];
    private float[] mVPMatrix = new float[16];

    List<Object3D> parsedObjects;

    public OcclusionRenderer(String path, Context context, RenderCallback callback) {
        mContext = context;
        mRenderCallback = callback;
        mOpenGlCameraPreview = new OpenGlCameraPreview();
        mOpenGlSphere = new OpenGlSphere(0.1f, 20, 20);

        float[] worldTsphere = new float[16];
        Matrix.setIdentityM(worldTsphere, 0);
        Matrix.translateM(worldTsphere, 0, 0, 0, -1);
        mOpenGlSphere.setModelMatrix(worldTsphere);
        //Matrix.translateM(worldTsphere, 0, 0, 0, -20);

        mDepthTexture = new DepthTexture();
        float[] worldTmesh = new float[16];
        Matrix.setIdentityM(worldTmesh, 0);
        Matrix.rotateM(worldTmesh, 0, -90, 1, 0, 0);
        mDepthTexture.setModelMatrix(worldTmesh);

        Log.d(TAG, "File obj path " + path);
        File file = new File(context.getExternalFilesDir(null) + File.separator + path);

        LoaderOBJ loader = new LoaderOBJ(context, file);
        try {
            parsedObjects = loader.parse();
        }catch(ParsingException e){
            Log.w(TAG, "Error Parsing OBJ");
            e.printStackTrace();
        }

        for(Object3D obj : parsedObjects){
            obj.setModelMatrix(worldTsphere);
        }

    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        // Enable depth test to discard fragments that are behind of another fragment.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST);
        // Enable face culling to discard back facing triangles.
        GLES20.glEnable(GLES20.GL_CULL_FACE);
        GLES20.glCullFace(GLES20.GL_BACK);
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);
        GLES20.glDepthMask(true);
        mOpenGlCameraPreview.setUpProgramAndBuffers();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap earthBitmap = BitmapFactory.decodeResource(mContext.getResources(),
                R.drawable.earth, options);
        mOpenGlSphere.setUpProgramAndBuffers(earthBitmap, mContext);
        for(Object3D obj : parsedObjects){
            obj.setUpProgramAndBuffers(mContext);
        }
        mDepthTexture.resetDepthTexture();
        mMeshMap.clear();
    }

    /**
     * Update background texture's UV coordinates when device orientation is changed. i.e change
     * between landscape and portrait mode.
     */
    public void updateColorCameraTextureUv(int rotation) {
        mOpenGlCameraPreview.updateTextureUv(rotation);
        mProjectionMatrixConfigured = false;
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        mDepthTexture.setTextureSize(width, height);
        mOpenGlSphere.setDepthTextureSize(width, height);
        for(Object3D obj : parsedObjects){
            obj.setDepthTextureSize(width, height);
        }
        mProjectionMatrixConfigured = false;
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

        // Call application-specific code that needs to run on the OpenGL thread.
        mRenderCallback.preRender();

        // Don't write depth buffer because we want to draw the camera as background.
        GLES20.glDepthMask(false);
        mOpenGlCameraPreview.drawAsBackground();
        // Enable depth buffer again for AR.
        GLES20.glDepthMask(true);

        updateVPMatrix();

        // Render depth texture.
        mDepthTexture.renderDepthTexture(mMeshMap, mVPMatrix);
        int depthTexture = mDepthTexture.getDepthTextureId();

        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        // Render objects.
        //mOpenGlSphere.drawSphere(mVPMatrix, depthTexture);
        for(Object3D obj : parsedObjects){
            obj.drawSphere(mVPMatrix, depthTexture);
        }
    }

    /**
     * Set the Projection matrix matching the Tango RGB camera in order to be able to do
     * Augmented Reality.
     */
    public void setProjectionMatrix(float[] matrixFloats, float nearPlane, float farPlane) {
        mProjectionMatrix = matrixFloats;
        mOpenGlSphere.configureCamera(nearPlane, farPlane);
        for(Object3D obj : parsedObjects){
            obj.configureCamera(nearPlane, farPlane);
        }
        mProjectionMatrixConfigured = true;
    }

    /**
     * Update the View matrix matching the pose of the Tango RGB camera.
     *
     * @param ssTcamera The transform from RGB camera to Start of Service.
     */
    public void updateViewMatrix(float[] ssTcamera) {
        float[] viewMatrix = new float[16];
        Matrix.invertM(viewMatrix, 0, ssTcamera, 0);
        mViewMatrix = viewMatrix;
    }

    /**
     * Composes the view and projection matrices into a single VP matrix.
     */
    private void updateVPMatrix() {
        Matrix.setIdentityM(mVPMatrix, 0);
        Matrix.multiplyMM(mVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
    }

    /**
     * Update the mesh segments given a new TangoMesh.
     */
    public void updateMesh(TangoMesh tangoMesh) {
        GridIndex key = new GridIndex(tangoMesh.index);
        if (!mMeshMap.containsKey(key)) {
            mMeshMap.put(key, new MeshSegment());
        }
        MeshSegment mesh = mMeshMap.get(key);
        mesh.update(tangoMesh);
        mMeshMap.put(key, mesh);
    }

    /**
     * It returns the ID currently assigned to the texture where the Tango color camera contents
     * should be rendered.
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public int getTextureId() {
        return mOpenGlCameraPreview == null ? -1 : mOpenGlCameraPreview.getTextureId();
    }

    /**
     * Updates the earth model matrix.
     */
    public void updateEarthTransform(float[] openGlTearth) {
        mOpenGlSphere.setModelMatrix(openGlTearth);
        for(Object3D obj : parsedObjects){
            obj.setModelMatrix(openGlTearth);
        }
    }

    public boolean isProjectionMatrixConfigured() {
        return mProjectionMatrixConfigured;
    }
}
