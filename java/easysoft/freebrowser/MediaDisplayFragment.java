package easysoft.freebrowser;

import android.annotation.SuppressLint;
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

import static android.view.MotionEvent.ACTION_UP;
import static easysoft.freebrowser.FileBrowser.*;


public class MediaDisplayFragment extends Fragment {
    rundomTimer imgTimer;
    videoRunTime videoRunTimer;
    static ImageView[] contrButtons;
    static ImageView imgDisplay;
    static TextView titel;
    String kindOfMedia = "";
    String mediaURL = "";
    float previousX, pointer;
    double scaleFact = 1;
    int arrayPointer = 0, hours, minutes, seconds, smseconds;
    String tag = "";

    View view;
    FrameLayout mediaDisplayLayout;
    VideoView videoView;
    RelativeLayout mainRel;
    RelativeLayout.LayoutParams videoRel;
    LinearLayout contrLin;
    MediaPlayer mP;
    ImageView[] runImgs;
    TextView duration;
    boolean videoPause = false;

    boolean disrupt = false;

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
        mediaController.setAnchorView(view);

        // Init Video
        videoView.setMediaController(mediaController);

        if(kindOfMedia.equals("PICTURES")) {
            RelativeLayout.LayoutParams imgRelParam = new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams(mediaDisplayLayout.getWidth() -100,mediaDisplayLayout.getHeight()));
            imgRelParam.addRule(RelativeLayout.CENTER_IN_PARENT);
            imgDisplay = new ImageView(fileBrowser);
            imgDisplay.setLayoutParams(imgRelParam);

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

            } else {
                videoView.setOnTouchListener(new View.OnTouchListener() {
                    float x, y, preX, preY;
                    int pC;
                    @SuppressLint("ClickableViewAccessibility")
                    @Override
                    public boolean onTouch(View view, MotionEvent me) {
                        pC = me.getPointerCount();

                        switch (me.getAction()) {
                            case (MotionEvent.ACTION_DOWN): {

                                    x = me.getX();
                                    y = me.getY();

                            }
                            case (MotionEvent.ACTION_MOVE): {

                                if(pC == 1) {
                                    //videoView.setY(view.getY() + (me.getY() - y));
                                    videoView.setX(view.getX() + (me.getX() - x));
                                    break;
                                } else if(pC == 2 && ((videoView.getHeight()*scaleFact) <= displayHeight) && scaleFact >= 1)  {
                                    scaleFact = scaleFact +(-(me.getY() - y) *0.001);
                                    if((videoView.getHeight()*scaleFact) >= displayHeight && me.getY() < y)
                                        scaleFact = scaleFact +((me.getY() - y) *0.001);
                                    if(scaleFact <= 1 && me.getY() > y)
                                        scaleFact = scaleFact +((me.getY() - y) *0.001);
                                    videoView.setScaleX((float) scaleFact);
                                    videoView.setScaleY((float) scaleFact);
                                    break;
                                }
                            }
                            case (ACTION_UP): {
                                try {
                                    Thread.sleep(250);
                                } catch (InterruptedException ie) {}
                                break;
                            }
                        }

                        return true;
                    }
                });
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
                        break;
                    }
                    case (MotionEvent.ACTION_UP): {
                        newX = e.getX();
                        break;
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
        mediaURL = url;

        Uri url1 = Uri.parse(url);
        videoView.setVideoURI(url1);

        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                if(kindOfMedia.equals("VIDEO")) {
                    mainRel.addView(createVideoController(mp.getDuration()/1000));
                }

                mp.setScreenOnWhilePlaying(true);
                mP = mp;
                mP.start();
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.setScreenOnWhilePlaying(true);

                if(fileBrowser.runningMediaList != null && fileBrowser.runningMediaList.size() > 0) {
                    runmediaList();
                }
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

        ImageView Drucker = new ImageView(fileBrowser);
        Drucker.setLayoutParams(new RelativeLayout.LayoutParams(new RelativeLayout.LayoutParams((int)(100 *xfact), (int)(100 *xfact))));
        Drucker.setX(mediaDisplayLayout.getWidth() -mediaDisplayLayout.getWidth()/2);
        Drucker.setY(mediaDisplayLayout.getHeight() -mediaDisplayLayout.getHeight()/8);
        Drucker.setTag("Drucker_closed");
        Drucker.setImageBitmap(bitmapLoader("Icons/mediaIcons/Drucker.png"));
        Drucker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fileBrowser.doPrint(imgDisplay);
            }
        });
        mainRel.addView(Drucker);

    }

    public LinearLayout createScaleButtons() {

        LinearLayout scaleLin = new LinearLayout(fileBrowser);
        scaleLin.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT));
        scaleLin.setOrientation(LinearLayout.HORIZONTAL);
        scaleLin.setPadding(10,10,10,10);
        scaleLin.setX(displayWidth/2);
        scaleLin.setY(20);

        String[] scaleTx = new String[]{"lupe"};
        if(kindOfMedia.equals("PICTURES")) {
            scaleTx = new String[]{"minus","lupe","plus"};
        }

        ImageView[] scaleImg = new ImageView[scaleTx.length];
        for(int i=0;i<scaleTx.length;i++) {
            scaleImg[i] = new ImageView(fileBrowser);
            scaleImg[i].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/"+scaleTx[i]+".png"));
            scaleImg[i].setTag(scaleTx[i]);
            scaleImg[i].setLayoutParams(new RelativeLayout.LayoutParams((int)(xfact*displayWidth/16),(int)(xfact*displayWidth/16)));

            scaleImg[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = view.getTag().toString();

                    if(tag.equals("minus") && scaleFact > 1)
                        scaleFact = scaleFact -0.1;
                    else if(tag.equals("plus"))
                        scaleFact = scaleFact +0.1;
                    else if(tag.equals("lupe")) {
                        scaleFact = 1;
                        videoView.setX(10);
                        videoView.setY(displayHeight/2 - videoView.getHeight()/2);
                    }

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

    private LinearLayout createVideoController(int videoLength) {

        if(contrLin != null)
            mainRel.removeView(contrLin);

        hours = videoLength /3600;
        minutes = (videoLength /60) -(hours *60);
        seconds = videoLength - (hours *3600) -(minutes *60);
        String formatted = String.format("%d:%02d:%02d",hours,minutes,seconds);

        RelativeLayout.LayoutParams contrLinParam = new RelativeLayout.LayoutParams(displayWidth, displayHeight/14);
        contrLinParam.addRule(RelativeLayout.CENTER_HORIZONTAL);
        contrLin = new LinearLayout(fileBrowser);
        contrLin.setLayoutParams(contrLinParam);
        contrLin.setOrientation(LinearLayout.VERTICAL);
        contrLin.setY(mediaDisplayLayout.getHeight() -displayHeight/10);
        //contrLin.setBackgroundColor(getResources().getColor(R.color.white));

        LinearLayout contrLinButtons = new LinearLayout(fileBrowser);
        contrLinButtons.setOrientation(LinearLayout.HORIZONTAL);
        contrLinButtons.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth, displayHeight/16));
        //contrLinButtons.setBackgroundColor(getResources().getColor(R.color.white));

        String[] contrButtons = new String[]{"playBack", "runForeward", "playForeward"};
        ImageView[] contrImgs = new ImageView[0];

        for(int i=0;i<contrButtons.length;i++) {
            contrImgs = Arrays.copyOf(contrImgs, contrImgs.length +1);
            contrImgs[contrImgs.length -1] = new ImageView(fileBrowser);
            contrImgs[contrImgs.length -1].setPadding(25,5,25,5);
            contrImgs[contrImgs.length -1].setTag(contrButtons[i]);
            contrImgs[contrImgs.length -1].setLayoutParams(new RelativeLayout.LayoutParams(displayHeight/20,displayHeight/20));
            contrImgs[contrImgs.length -1].setImageBitmap(fileBrowser.bitmapLoader("Icons/videoContrIcons/"+contrButtons[i]+".png"));
            contrImgs[contrImgs.length -1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String tag = view.getTag().toString();
                    switch(tag) {
                        case ("playBack"): {

                            if(videoRunTimer.isAlive()) {
                                videoView.seekTo(0);
                                runImgs[1].setX(0);
                                smseconds = hours * 60 + minutes * 60 + seconds;
                            } else {
                                createMediaPlay(mediaURL);
                            }

                            break;
                        }
                        case ("runForeward"): {
                            view.setTag("playStop");
                            ((ImageView)view).setImageBitmap(fileBrowser.bitmapLoader("Icons/videoContrIcons/"+view.getTag().toString()+".png"));
                            videoView.pause();
                            videoPause = true;
                            break;
                        }
                        case ("playStop"): {
                            view.setTag("runForeward");
                            ((ImageView)view).setImageBitmap(fileBrowser.bitmapLoader("Icons/videoContrIcons/"+view.getTag().toString()+".png"));
                            videoView.start();
                            videoPause = false;
                            break;
                        }
                        case ("playForeward"): {
                            if(videoRunTimer.isAlive() && smseconds > 10) {
                                int sec = hours*60 + minutes*60 +seconds, runtime = 0;

                                runtime = (sec -(smseconds - 10)) *1000;
                                videoView.seekTo(runtime);
                                runImgs[1].setX(runImgs[1].getX() + (5*pointer));
                                smseconds = smseconds - 10;

                            }
                            break;
                        }
                    }
                }
            });
            contrLinButtons.addView(contrImgs[contrImgs.length -1]);
        }

        RelativeLayout contrRunRel = new RelativeLayout(fileBrowser);
        contrRunRel.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth/2, displayHeight/14));

        runImgs = new ImageView[0];
        String[] runLine = new String[]{"laufLeiste","playZeiger"};

        for(int i=0;i<runLine.length;i++) {
            runImgs = Arrays.copyOf(runImgs, runImgs.length + 1);
            runImgs[runImgs.length - 1] = new ImageView(fileBrowser);
            runImgs[runImgs.length - 1].setPadding(25, 5, 25, 5);
            runImgs[runImgs.length - 1].setTag(runLine[i]);
            if (i == 0) {
                runImgs[runImgs.length - 1].setLayoutParams(new RelativeLayout.LayoutParams(6*displayWidth/7, displayHeight / 20));
            } else
                runImgs[runImgs.length - 1].setLayoutParams(new RelativeLayout.LayoutParams(displayHeight / 20, displayHeight / 20));

            runImgs[runImgs.length - 1].setImageBitmap(fileBrowser.bitmapLoader("Icons/videoContrIcons/" + runLine[i] + ".png"));
            contrRunRel.addView(runImgs[runImgs.length - 1]);
        }
        contrLinButtons.addView(contrRunRel);

        LinearLayout durationTXLin = new LinearLayout(fileBrowser);
        durationTXLin.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth / 5, displayHeight / 20));
        durationTXLin.setOrientation(LinearLayout.HORIZONTAL);

        duration = new TextView(fileBrowser);
        duration.setText(formatted);
        duration.setTextColor(getResources().getColor(R.color.white));
        duration.setTextSize(textSize);

        durationTXLin.addView(duration);
        contrLinButtons.addView(durationTXLin);

        contrLin.addView(contrLinButtons);

        videoRunTimer = new videoRunTime((2*displayWidth/5));
        videoRunTimer.start();
        return contrLin;
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

    class videoRunTime extends Thread {
        int  bar;

        public videoRunTime(int line) {
            bar = line;
            smseconds = hours*60 + minutes*60 +seconds;
        }

        @Override
        public void run() {
            double speedFact = 1;
            if(smseconds < 100) speedFact = 0.75;
            else if(smseconds < 200) speedFact = 0.80;
            else if(smseconds < 300) speedFact = 0.90;
            else if(smseconds < 400) speedFact = 0.95;

            pointer = (float)((bar /smseconds) *speedFact* xfact);

            while (smseconds > 0) {
                runImgs[1].setX(runImgs[1].getX() + (float) (pointer));
                int hours = smseconds /3600;
                int minutes = (smseconds /60) -(hours *60);
                int seconds = smseconds - (hours *3600) -(minutes *60);
                String formatted = String.format("%d:%02d:%02d",hours,minutes,seconds);
                fileBrowser.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        duration.setText(formatted);

                    }
                });

                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                }
                while(videoPause) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ie) {
                    }
                }
                smseconds = smseconds -1;
            }

        }
    }
}