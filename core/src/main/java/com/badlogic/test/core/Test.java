package com.badlogic.test.core;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Gdx2DPixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import uk.co.caprica.vlcj.binding.LibVlc;
import uk.co.caprica.vlcj.player.MediaPlayerFactory;
import uk.co.caprica.vlcj.player.direct.BufferFormat;
import uk.co.caprica.vlcj.player.direct.BufferFormatCallback;
import uk.co.caprica.vlcj.player.direct.DirectMediaPlayer;
import uk.co.caprica.vlcj.player.direct.RenderCallback;
import uk.co.caprica.vlcj.runtime.RuntimeUtil;
import uk.co.caprica.vlcj.runtime.x.LibXUtil;

import java.nio.ByteBuffer;

public class Test implements ApplicationListener {

    private BitmapFont font;
    private SpriteBatch batch;

    float w = 854;
    float h = 480;

    private Pixmap pixmap;

    @Override
    public void create() {
        w = Gdx.graphics.getWidth();
        h = Gdx.graphics.getHeight();

        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, w, h);
        camera.update();

        font = new BitmapFont();
        batch = new SpriteBatch();

        LibXUtil.initialise();
        NativeLibrary.addSearchPath(RuntimeUtil.getLibVlcLibraryName(), "C:\\Program Files\\VideoLAN\\VLC");

        Native.loadLibrary(RuntimeUtil.getLibVlcLibraryName(), LibVlc.class);

        String[] args = {"--no-video-title-show", "--verbose=3"};
        String media = "D:\\big_buck_bunny_480p_h264.mov";

        MediaPlayerFactory factory = new MediaPlayerFactory(args);
        DirectMediaPlayer mediaPlayer = factory.newDirectMediaPlayer(new TestBufferFormatCallback(), new TestRenderCallback());
        boolean started = mediaPlayer.prepareMedia(media);
        if (started) mediaPlayer.play();

        System.out.println(LibVlc.INSTANCE.libvlc_get_version());
    }

    @Override
    public void dispose() {
        Gdx.app.exit();
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();
        if (pixmap != null) {
            Texture texture = new Texture(pixmap);
            batch.draw(texture, 0, 0, w, h);
        }

        font.draw(batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 10, 20);
        batch.end();
    }

    private final class TestRenderCallback implements RenderCallback {

        @Override
        public void display(DirectMediaPlayer mediaPlayer, Memory[] nativeBuffers, BufferFormat bufferFormat) {
            w = bufferFormat.getWidth();
            h = bufferFormat.getHeight();
            ByteBuffer buffer = nativeBuffers[0].getByteBuffer(0, (long)(h * w * Gdx2DPixmap.GDX2D_FORMAT_RGBA8888));
            long[] nativeData = new long[]{0, (long) w, (long) h, Gdx2DPixmap.GDX2D_FORMAT_RGBA8888};
            Gdx2DPixmap gdx2DPixmap = new Gdx2DPixmap(buffer, nativeData);
            pixmap = new Pixmap(gdx2DPixmap);
        }

    }

    private final class TestBufferFormatCallback implements BufferFormatCallback {

        @Override
        public BufferFormat getBufferFormat(int sourceWidth, int sourceHeight) {
            return new BufferFormat("RGBA", sourceWidth, sourceHeight, new int[] { sourceWidth * Gdx2DPixmap.GDX2D_FORMAT_RGBA8888 }, new int[] { sourceHeight });
        }

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}
