package nz.co.parhelion.magnify;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.vr.sdk.widgets.video.VrVideoView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

public class ShowVideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_video);

        Intent intent = getIntent();
        String url = intent.getStringExtra(MainActivity.CLICKED_VIDEO);
        String title = intent.getStringExtra(MainActivity.TITLE);

        // Capture the layout's TextView and set the string as its text
        TextView textView = (TextView) findViewById(R.id.videoTitle);
        textView.setText(title);
        textView.setTextColor(Color.WHITE);
        textView.setTextSize(16);

        VrVideoView videoView = (VrVideoView) findViewById(R.id.video_view);
        try {
            Uri uri = Uri.parse(url);
            System.out.println("Loading video "+url);
            videoView.loadVideo(uri, null);//new VrVideoView.Options());
            System.out.println("Playing video");
            videoView.playVideo();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        VrVideoView videoView = (VrVideoView) findViewById(R.id.video_view);
        videoView.pauseVideo();
    }

}
