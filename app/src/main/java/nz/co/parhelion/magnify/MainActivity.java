package nz.co.parhelion.magnify;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.JsonReader;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import nz.co.parhelion.magnify.model.VRVideo;
import nz.co.parhelion.magnify.utils.DownloadImageTask;

public class MainActivity extends AppCompatActivity {

    public static final String CLICKED_VIDEO = "nz.co.parhelion.magnify.VIDEO";
    public static final String TITLE = "nz.co.parhelion.magnify.TITLE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        try {
            getVideos();
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
         * Sets up a SwipeRefreshLayout.OnRefreshListener that is invoked when the user
         * performs a swipe-to-refresh gesture.
         */
        final SwipeRefreshLayout swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);

        swipeLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        // This method performs the actual data-refresh operation.
                        // The method calls setRefreshing(false) when it's finished.
                        try {
                            getVideos();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        swipeLayout.setRefreshing(false);
                    }
                }
        );

    }



    private void getVideos() throws Exception {
        emptyLayout();
        Ion.with(this)
            .load("https://1819948887.rsc.cdn77.org/magnify.json")

//            .load("https://api.myjson.com/bins/1c6241")
//                .as(new TypeToken<List<VRVideo>>(){})
            .asJsonObject()
            .withResponse()
            .setCallback(new FutureCallback<Response<JsonObject>>() {
                @Override
                public void onCompleted(Exception e, Response<JsonObject> response) {
                    if (response == null) {
                        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
                        alertDialog.setTitle("No response");
                        alertDialog.setMessage("Unable to contact server. Please try again later");
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.show();
                        return;
                    }
                    JsonArray videos = response.getResult().getAsJsonArray("videos");
                    System.out.println("Callback completed");
                    Gson gson = new Gson();
                    Type typeOfT = new TypeToken<List<VRVideo>>(){}.getType();
                    List<VRVideo> list = gson.fromJson(videos, typeOfT);

                    TableLayout layout = (TableLayout) findViewById(R.id.tableLayout);
                    buildTable(layout, list);
                }
            });
    }

    private void emptyLayout() {
        TableLayout layout = (TableLayout) findViewById(R.id.tableLayout);
        layout.removeAllViews();
    }

    private void buildTable(TableLayout layout, List<VRVideo> videos) {

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int height = metrics.heightPixels;
        int width = metrics.widthPixels;

        int imageWidth = (int)(width * .7);
        int imageHeight = (int) (imageWidth / 2);

        int logoWidth = (int)(width * .2);
        int logoHeight = logoWidth;

        layout.setColumnShrinkable(0, true);
        int ROW_HEIGHT = 600;
        int numVideos = 0;
        for (VRVideo video : videos) {
            if (video == null) {
                continue;
            }
            numVideos++;
            TableRow row = new TableRow(this);

            TableLayout.LayoutParams tableRowParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);

            final int leftMargin = 10;
            final int topMargin = 12;
            final int rightMargin = 10;
            final int bottomMargin = 12;

            tableRowParams.setMargins(leftMargin, topMargin, rightMargin, bottomMargin);

            row.setLayoutParams(tableRowParams);
            row.setBackgroundResource(R.drawable.shaded_background);

            row.setGravity(Gravity.HORIZONTAL_GRAVITY_MASK | Gravity.VERTICAL_GRAVITY_MASK);
            ImageView photoImage = new ImageView(this);

            photoImage.setLayoutParams(new RelativeLayout.LayoutParams(imageWidth, imageHeight));
            photoImage.getLayoutParams().height = imageHeight;
            photoImage.getLayoutParams().width = imageWidth;
            photoImage.requestLayout();
            photoImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Ion.with(photoImage)
                .placeholder(R.drawable.magnifylogo)
//                .error(R.drawable.error_image)
//                .animateLoad(spinAnimation)
//                .animateIn(fadeInAnimation)
                .load(video.getPhoto());


            ImageView logo = new ImageView(this);
            RelativeLayout.LayoutParams logoParams = new RelativeLayout.LayoutParams(logoWidth, logoHeight);
            logoParams.addRule(RelativeLayout.CENTER_VERTICAL);
            logoParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

            logo.setLayoutParams(logoParams);
            Ion.with(logo)
                    .placeholder(R.drawable.magnifylogo)
//                .error(R.drawable.error_image)
//                .animateLoad(spinAnimation)
//                .animateIn(fadeInAnimation)
                    .load(video.getLogo());


            final VRVideo videoToShow = video;
            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, ShowVideoActivity.class);

                    intent.putExtra(CLICKED_VIDEO, videoToShow.getVideo());
                    intent.putExtra(TITLE, videoToShow.getCompany());
                    startActivity(intent);
                }
            });

            RelativeLayout rowHolder = new RelativeLayout(this);
            rowHolder.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));

            rowHolder.addView(photoImage);
            rowHolder.addView(logo);

            row.addView(rowHolder);

            layout.addView(row);
        }
        layout.setWeightSum(numVideos);
    }

}
