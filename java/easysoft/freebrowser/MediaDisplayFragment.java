package easysoft.freebrowser;

import android.app.Fragment;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.Arrays;
import java.util.Random;

import static easysoft.freebrowser.FileBrowser.*;


public class MediaDisplayFragment extends Fragment {
    rundomTimer imgTimer;
    static ImageView[] contrButtons;
    static ImageView imgDisplay;
    static TextView titel;
    String kindOfMedia = "";
    String mediaURL = "";
    float previousX;
    double scaleFact = 1;
    int arrayPointer = 0;
    String tag = "";

    View view;
    FrameLayout mediaDisplayLayout;
    VideoView videoView;
    RelativeLayout mainRel;
    RelativeLayout.LayoutParams videoRel;
    MediaPlayer mP;
    boolean disrupt = false;
    float videoViewPosX, videoViewPosY, videoViewWidth, videoViewHeight;

    public MediaDisplayFragment() {
    }

    public static MediaDisplayFragment newInstance() {
        MediaDisplayFragment fragment = new MediaDisplayFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            kindOfMedia = getArguments().getString("KIND_OF_MEDIA");
            mediaURL = getArguments().getString("URL");
            previousX = 0;
            mP = new MediaPlayer();
        }

        mediaDisplayLayout = fileBrowser.frameLy.get(4);
        if(kindOfMedia.equals("AUDIO")) {
            mediaDisplayLayout.setLayoutParams(new FrameLayout.LayoutParams(displayWidth, displayHeight / 16));
            mediaDisplayLayout.setY(displayHeight -displayHeight/11);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_media_display, container, false);
        mainRel = (RelativeLayout) view.findViewById(R.id.mainRel);

        videoRel = new RelativeLayout.LayoutParams(mediaDisplayLayout.getWidth(),
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        videoRel.addRule(RelativeLayout.CENTER_IN_PARENT);
        videoView = (VideoView) view.findViewById(R.id.videoView);
        videoView.setLayoutParams(videoRel);

        //MediaController
        MediaController mediaController = new MediaController(fileBrowser.context);
        mediaController.setVisibility(View.generateViewId());
        mediaController.setAnchorView(videoView);
        // Init Video
        videoView.setMediaController(mediaController);

        if(kindOfMedia.equals("PICTURES")) {
            imgDisplay = new ImageView(fileBrowser);
            imgDisplay.setLayoutParams(new RelativeLayout.LayoutParams(mediaDisplayLayout.getWidth() -100,mediaDisplayLayout.getHeight()));
            imgDisplay.setX(40);
            mainRel.addView(imgDisplay);

            if (runningMediaList != null && runningMediaList.size() > 0) {
                mediaURL = runningMediaList.get(0);
            }
            createRotateImg();
            mainRel.addView(createPlayChoosePanel());
            createImageShow(mediaURL);

        }
        else if(kindOfMedia.equals("VIDEO") || kindOfMedia.equals("AUDIO")) {
            if (kindOfMedia.equals("AUDIO")) {
                mainRel.setBackgroundColor(getResources().getColor(R.color.white_overlay));

                RelativeLayout videoRel = (RelativeLayout) view.findViewById(R.id.videoRel);
                videoRel.setLayoutParams(new RelativeLayout.LayoutParams(3 * mediaDisplayLayout.getWidth() / 5, mediaDisplayLayout.getHeight()));
                videoRel.setX(2*mediaDisplayLayout.getWidth() / 5);

            }
            if (runningMediaList != null && runningMediaList.size() > 0) {
                mediaURL = runningMediaList.get(0);
            }

            mainRel.addView(createPlayChoosePanel());
            createMediaPlay(mediaURL);
        }

        if(!kindOfMedia.equals("AUDIO"))
           mainRel.addView(createSwitcher());

        devicePath = mediaURL;
        fileBrowser.reloadFileBrowserDisplay();

        view.requestFocus();
        mediaDisplayLayout.bringToFront();
        return view;
    }
    private RelativeLayout createSwitcher() {
        RelativeLayout header = new RelativeLayout(fileBrowser);
        header.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth, displayHeight/18));

        header.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                float newX = 0;

                switch (e.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {
                        previousX = e.getX();
                    }
                    case (MotionEvent.ACTION_UP): {
                        newX = e.getX();
                    }
                }

                if ((previousX - newX) < -100) {
                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                    }
                    fileBrowser.startMovePanel(4);
                }
                return true;
            }
        });
        return header;
    }

    public void createMediaPlay(String url)  {

        //Uri url1 = Uri.parse(fileBrowser.uriFromFile(fileBrowser,new File(url)).toString());
        Uri url1 = Uri.parse(url);
        videoView.setVideoURI(url1);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.setScreenOnWhilePlaying(true);
                mP = mp;
                videoView.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.setScreenOnWhilePlaying(true);

                if(fileBrowser.runningMediaList != null && fileBrowser.runningMediaList.size() > 0)
                    runmediaList();
            }
        });

    }

    public void createImageShow(String url)  {
        calledBy = "mediaPlayer";
        imgDisplay.setImageBitmap(fileBrowser.bitmapLoader(url));

        if(tag.contains("Random")) {
            imgTimer = new rundomTimer();
            imgTimer.start();
        }
    }

    public LinearLayout createPlayChoosePanel() {
        int n = 3, contr = RelativeLayout.CENTER_HORIZONTAL, height = mediaDisplayLayout.getHeight()/10,
            icsz = 48;
        if(kindOfMedia.equals("AUDIO")) {
            n = 2;
            contr = RelativeLayout.ALIGN_PARENT_LEFT;
            height = mediaDisplayLayout.getHeight();
            icsz = 48;
        }
        RelativeLayout.LayoutParams linRel = new RelativeLayout.LayoutParams(n*mediaDisplayLayout.getWidth()/4,
                height);
        linRel.addRule(contr);

        String[] contrButStr = new String[]{"Back", "Empty", "Random", "Empty", "Forward"};
        LinearLayout controlButtonLin = new LinearLayout(fileBrowser);
        controlButtonLin.setOrientation(LinearLayout.VERTICAL);
        controlButtonLin.setLayoutParams(linRel);

        RelativeLayout.LayoutParams linContrRel = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        RelativeLayout steerRel = new RelativeLayout(fileBrowser);
        steerRel.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT));

        linContrRel.addRule(contr);
        LinearLayout steerContrLin = new LinearLayout(fileBrowser);
        steerContrLin.setOrientation(LinearLayout.HORIZONTAL);
        steerContrLin.setLayoutParams(linContrRel);

        if(!kindOfMedia.equals("AUDIO")) {
            controlButtonLin.setY(mediaDisplayLayout.getHeight() / 20);
            RelativeLayout.LayoutParams linTxRel = new RelativeLayout.LayoutParams(mediaDisplayLayout.getWidth() / 2, height/2);
            titel = new TextView(fileBrowser);
            titel.setTextColor(getResources().getColor(R.color.blue));
            titel.setText(setText(mediaURL));
            titel.setTextSize(textSize);
            titel.setLayoutParams(linTxRel);
            titel.setY(20);

            steerRel.addView(titel);
            steerRel.addView(createScaleButtons());

        } else {
            controlButtonLin.setPadding((int) (20*xfact),(int) (10*yfact),0,0);
        }
        if (runningMediaList != null && runningMediaList.size() > 0) {
            contrButtons = new ImageView[0];
            for (int i = 0; i < contrButStr.length; i++) {
                String iconStatus = "_closed";
                if (contrButStr[i].equals("Empty"))
                    iconStatus = "";
                if(i == contrButStr.length -1) {
                    iconStatus = "_open";
                    tag = contrButStr[i] + iconStatus + ".png";
                }

                contrButtons = Arrays.copyOf(contrButtons, contrButtons.length + 1);
                contrButtons[contrButtons.length - 1] = new ImageView(fileBrowser);
                contrButtons[contrButtons.length - 1].setTag(contrButStr[i] + iconStatus + ".png");
                contrButtons[contrButtons.length - 1].setLayoutParams(new RelativeLayout.LayoutParams((int) (icsz * xfact), (int) (icsz * xfact)));
                contrButtons[contrButtons.length - 1].setImageBitmap(bitmapLoader("Icons/mediaIcons/" +
                        contrButtons[contrButtons.length - 1].getTag().toString()));

                if (!contrButStr[i].equals("Empty"))
                    contrButtons[contrButtons.length - 1].setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tag = v.getTag().toString();
                            for (int i = 0; i < contrButtons.length; i++) {
                                contrButtons[i].setTag(contrButtons[i].getTag().toString().replace("open", "closed"));
                                contrButtons[i].setImageBitmap(bitmapLoader("Icons/mediaIcons/" +
                                        contrButtons[i].getTag().toString()));
                            }
                            v.setTag(v.getTag().toString().replace("closed", "open"));
                            ((ImageView) v).setImageBitmap(bitmapLoader("Icons/mediaIcons/" +
                                    v.getTag().toString()));
                            if (videoView.isPlaying()) {
                                videoView.stopPlayback();
                                mP.release();
                            }
                            runmediaList();
                        }
                    });

                steerContrLin.addView(contrButtons[contrButtons.length - 1]);
            }
            controlButtonLin.addView(steerContrLin);
        }
        controlButtonLin.addView(steerRel);
        return controlButtonLin;
    }
    public void runmediaList () {
        if(tag.contains("Forward") && arrayPointer < fileBrowser.runningMediaList.size() -1)
            arrayPointer++;
        else if(tag.contains("Back") && arrayPointer > 0)
            arrayPointer--;
        if (tag.contains("Random") && arrayPointer < fileBrowser.runningMediaList.size()) {
            Random r = new Random();
            arrayPointer = r.nextInt(fileBrowser.runningMediaList.size() - 0);
        }

        String path = fileBrowser.runningMediaList.get(arrayPointer);
        if(kindOfMedia.equals("VIDEO") || kindOfMedia.equals("AUDIO")) {
            createMediaPlay(fileBrowser.runningMediaList.get(arrayPointer));
            if(!kindOfMedia.equals("AUDIO"))
                titel.setText(setText(path));

            devicePath = path;
            fileBrowser.reloadFileBrowserDisplay();

        }
        else if (kindOfMedia.equals("PICTURES")) {
            createImageShow(fileBrowser.runningMediaList.get(arrayPointer));
            titel.setText(setText(path));
        }
    }

    private String setText(String path) {
        String tx = (path.substring(path.substring(0, path.lastIndexOf("/")).lastIndexOf("/") +1)).substring(
                0, (path.substring(path.substring(0, path.lastIndexOf("/")).lastIndexOf("/") +1)).lastIndexOf(".")).replace("/", "  -->  ");
        return tx;
    }

    public void createRotateImg () {
        ImageView Rotation = new ImageView(fileBrowser);
        Rotation.setLayoutParams(new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams((int)(100 *xfact), (int)(100 *xfact))));
        Rotation.setX(mediaDisplayLayout.getWidth() -mediaDisplayLayout.getWidth()/5);
        Rotation.setY(mediaDisplayLayout.getHeight() -mediaDisplayLayout.getHeight()/8);
        Rotation.setTag("Rotation_closed");
        Rotation.setImageBitmap(bitmapLoader("Icons/mediaIcons/Rotation_closed.png"));
        Rotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int angle = 0;
                if(v.getTag().toString().contains("closed")) {
                    v.setTag(v.getTag().toString().replace("closed", "open"));
                    angle = 90;
                }
                else {
                    v.setTag(v.getTag().toString().replace("open", "closed"));
                    angle = 0;
                }

                imgDisplay.setRotation(angle);
                ((ImageView) v).setImageBitmap(bitmapLoader("Icons/mediaIcons/" + v.getTag().toString() + ".png"));

            }
        });
        mainRel.addView(Rotation);
    }

    public LinearLayout createScaleButtons() {

        LinearLayout scaleLin = new LinearLayout(fileBrowser);
        scaleLin.setLayoutParams(new RelativeLayout.LayoutParams(3*displayHeight/9, displayHeight/26));
        scaleLin.setOrientation(LinearLayout.HORIZONTAL);
        scaleLin.setPadding(10,10,10,10);
        scaleLin.setX(displayWidth/2);

        String[] scaleTx = new String[]{"minus", "lupe", "plus"};
        ImageView[] scaleImg = new ImageView[scaleTx.length];
        for(int i=0;i<scaleTx.length;i++) {
            scaleImg[i] = new ImageView(fileBrowser);
            scaleImg[i].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/"+scaleTx[i]+".png"));
            scaleImg[i].setTag(scaleTx[i]);

            if(scaleTx.equals("lupe"))
                scaleImg[i].setEnabled(false);

            scaleImg[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = view.getTag().toString();

                    if(tag.equals("minus"))
                        scaleFact = scaleFact -0.1;
                    else
                        scaleFact = scaleFact +0.1;

                    if(kindOfMedia.equals("VIDEO")) {
                        videoView.setScaleX((float) scaleFact);
                        videoView.setScaleY((float) scaleFact);
                    }
                    else if (kindOfMedia.equals("PICTURES")) {
                        imgDisplay.setScaleX((float) scaleFact);
                        imgDisplay.setScaleY((float) scaleFact);
                    }
                }
            });
            scaleLin.addView(scaleImg[i]);
        }
        return scaleLin;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mP!=null) {
            try {
                mP.stop();
                mP.release();
                mP = null;
            } catch (java.lang.IllegalStateException ie) {}
        }
        if (runningMediaList != null && runningMediaList.size() > 0) {
            runningMediaList = null;
        }
    }

    class rundomTimer extends Thread {
        public rundomTimer() {
        }
        public void run() {
            try {
                Thread.sleep(6000);
            } catch (InterruptedException ie) {
            }
            fileBrowser.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (runningMediaList != null && runningMediaList.size() > 0)
                       runmediaList();
                }
            });
        }
    }
}