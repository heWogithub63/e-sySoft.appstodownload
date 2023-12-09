package easysoft.freebrowser;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.Arrays;

import static easysoft.freebrowser.FileBrowser.*;

public class fileBrowser_01Fragment extends Fragment {
    boolean select = false;
    private static final int INT_PARAM1 = -1;
    public View view;
    FrameLayout fileBrowserLayout;
    public RelativeLayout fragRel;
    public HorizontalScrollView scrView;
    static ScrollView[] detailScroll;

    public fileBrowser_01Fragment() {
    }

    public static fileBrowser_01Fragment newInstance() {
        fileBrowser_01Fragment fragment = new fileBrowser_01Fragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }

        fileBrowserLayout = fileBrowser.frameLy.get(1);

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_file_browser_01, container, false);
        fragRel = (RelativeLayout) view.findViewById(R.id.framRel);

        int f = 6;
        if(yfact>=0.625 && yfact<0.66)
            f=4;

        scrView = new HorizontalScrollView(getContext());
        scrView.setHorizontalScrollBarEnabled(false);
        scrView.setLayoutParams(new RelativeLayout.LayoutParams((int)(2*displayWidth/5), fileBrowserLayout.getHeight()));
        scrView.setBackgroundColor(getResources().getColor(R.color.white_overlay));
        scrView.setNestedScrollingEnabled(true);
        scrView.post(new Runnable() {
            public void run() {
                scrView.smoothScrollBy(mainscrPosX, 0);
            }
        });
        scrView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
            @Override
            public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                mainscrPosX = scrView.getScrollX();
            }
        });

        scrView.addView(createDetailFolder_Docu());
        fragRel.addView(scrView);

        return view;
    }

    @SuppressLint("ResourceType")
    @RequiresApi(api = Build.VERSION_CODES.M)
    public RelativeLayout createDetailFolder_Docu() {
        String dev = devicePath.substring(urldevice.length());
        String[] devArr = dev.split("/");
        dev ="";

        detailScroll = new ScrollView[0];
        LinearLayout[] folderLy = new LinearLayout[0];
        LinearLayout[][] detailFolderLy = new LinearLayout[0][0];

        RelativeLayout scRel = new RelativeLayout(getContext());
        scRel.setLayoutParams(new RelativeLayout.LayoutParams((paramList.size()-1)*fileBrowserLayout.getWidth(), fileBrowserLayout.getHeight()));
        scRel.setBackgroundColor(getResources().getColor(R.color.white_overlay));

        for(int ln=1;ln<paramList.size();ln++) {
            if(devArr.length > ln)
               dev = dev +"/" +devArr[ln];

            if(detscrPosY != null && detscrPosY.length < ln) {
                detscrPosY = Arrays.copyOf(detscrPosY, detscrPosY.length + 1);
                detscrPosY[detscrPosY.length - 1] = 0;
            }

            detailScroll = Arrays.copyOf(detailScroll, detailScroll.length +1);
            detailScroll[detailScroll.length -1] = new ScrollView(getContext());
            detailScroll[detailScroll.length -1].setVerticalScrollBarEnabled(true);
            detailScroll[detailScroll.length -1].setBackgroundColor(getResources().getColor(R.color.white_overlay));
            detailScroll[detailScroll.length -1].setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,fileBrowserLayout.getHeight()));
            if(paramList.get(paramList.size() -1) != null && detailScroll.length -1 < paramList.size() -2 && paramList.get(paramList.size() -1).length >0)
                detailScroll[detailScroll.length -1].setLayoutParams(new RelativeLayout.LayoutParams(fileBrowser.scrollView.getWidth(),fileBrowser.scrollView.getHeight()));
            detailScroll[detailScroll.length -1].setVerticalScrollBarEnabled(false);
            detailScroll[detailScroll.length -1].setTag(detailScroll.length -1);

            detailScroll[detailScroll.length -1].post(new Runnable() {
                public void run() {
                    for(int i=0; i<detailScroll.length; i++)
                        detailScroll[i].smoothScrollBy(0, detscrPosY[i]);
                }
            });


            if(detailScroll.length > 1) {
                detailScroll[detailScroll.length - 1].setX(detailScroll[detailScroll.length - 2].getX() + fileBrowser.scrollView.getWidth() + (int)(10*xfact));
            }


            folderLy = Arrays.copyOf(folderLy, folderLy.length +1);
            folderLy[folderLy.length -1] = new LinearLayout(getContext());
            folderLy[folderLy.length -1].setOrientation(LinearLayout.VERTICAL);
            folderLy[folderLy.length -1].setBackgroundResource(getResources().getColor(R.color.white_overlay));

            detailFolderLy = Arrays.copyOf(detailFolderLy, detailFolderLy.length +1);
            detailFolderLy[detailFolderLy.length -1] = new LinearLayout[0];

            if(paramList.get(ln) != null) {

                for (int i = 0; i < paramList.get(ln).length; i++) {
                    Arrays.sort(paramList.get(ln));
                    String docu = "";
                    if (paramList.get(ln)[i].substring(1).contains("."))
                        docu = "document_closed.png";
                    else
                        docu = "file_closed.png";

                    ImageView fold_docu = new ImageView(getContext());

                    if(selectedFile != null) {
                        for (String s : selectedFile) {
                            if (paramList.get(ln)[i].equals(s)) {
                                select = true;
                                docu = docu.replace("closed", "open");

                                break;
                            }
                        }
                    }

                    fold_docu.setImageBitmap(bitmapLoader("Icons/browserIcons/" + docu));
                    fold_docu.setLayoutParams(new RelativeLayout.LayoutParams((int) (80 * xfact), displayHeight/20));
                    fold_docu.setTag(ln + "_" + i + "-" + docu + "  " + urldevice+ dev + "/" + paramList.get(ln)[i]);
                    fold_docu.setBackgroundColor(getResources().getColor(R.color.white_overlay));
                    if(fold_docu.getTag().toString().contains("document"))
                        fold_docu.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                String extProgrUrl = v.getTag().toString().substring(v.getTag().toString().lastIndexOf("  ") + 2);
                                devicePath = extProgrUrl;


                                    if (extProgrUrl.endsWith(".html"))
                                        extProgrUrl = "file://" + extProgrUrl;

                                    fileBrowser.startExtApp(extProgrUrl);
                                //}

                                fileBrowser.reloadFileBrowserDisplay();
                                return true;
                            }
                        });
                    fold_docu.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            OnClick(v);
                        }
                    });

                    detailFolderLy[detailFolderLy.length - 1] = Arrays.copyOf(detailFolderLy[detailFolderLy.length - 1], detailFolderLy[detailFolderLy.length - 1].length + 1);
                    detailFolderLy[detailFolderLy.length - 1][detailFolderLy[detailFolderLy.length - 1].length - 1] = new LinearLayout(getContext());
                    detailFolderLy[detailFolderLy.length - 1][detailFolderLy[detailFolderLy.length - 1].length - 1].addView(fold_docu);

                    TextView folderTx = new TextView(getContext());
                    folderTx.setText(paramList.get(ln)[i]);
                    folderTx.setTextSize((float) (textSize));
                    folderTx.setBackgroundColor(getResources().getColor(R.color.white_overlay));
                    folderTx.setTextColor(getResources().getColor(R.color.white));

                    if (select) {
                        folderTx.setTextColor(getResources().getColor(R.color.green));
                        detscrPosY[ln -1] = (int)((i*0.3)*270 * yfact);
                        detailScroll[detailScroll.length -1].setScrollY((int)((i*0.3)*270 * yfact));
                        if(fold_docu.getTag().toString().contains("document")) {
                            detscrPosY[ln -1] = (int)((i*0.3)*250 * yfact);
                            detailScroll[detailScroll.length -1].setScrollY((int)((i*0.3)*250 * yfact));
                        }
                        select = false;
                    }

                    int sz = 1;
                    if(paramList.get(paramList.size() - 1) == null || paramList.get(paramList.size() - 1).length == 0)
                        sz = 2;
                    if (ln  == paramList.size() - sz) {
                        folderTx.setY((float) (textSize));
                        detailFolderLy[detailFolderLy.length - 1][detailFolderLy[detailFolderLy.length - 1].length - 1].addView(folderTx);
                    }
                    folderLy[folderLy.length - 1].addView(detailFolderLy[detailFolderLy.length - 1][detailFolderLy[detailFolderLy.length - 1].length - 1]);

                    if (ln < paramList.size() - sz) {
                        folderTx.setTextSize((float) (textSize -1));

                        //TxSplitting

                        String st = folderTx.getText().toString(), st1 = "", st2 = "";
                        String[] sc = new String[]{" ", "_", "-", "#", "*", "§", "$", "%", "&"};

                        while (((float) (st.length() / 12)) >= 1.0) {
                            int n = 0, n1 = 0;
                            st1 = st.substring(0, 12);
                            for (String c : sc) {
                                if (st1.substring(6).contains(c)) {
                                    n1++;
                                    break;
                                }
                                n++;
                            }
                            if (n1 > 0) {
                                st1 = st1.substring(0, 6+ st1.substring(6).indexOf(sc[n]) +1);
                                st2 = st2 + st1 + "\n";
                            } else
                                st2 = st2 + st1 + "\n";
                            st = st.substring(st1.length());
                        }
                            st2 = st2 + st;

                            folderTx.setText(st2);

                        //

                        folderLy[folderLy.length - 1].addView(folderTx);
                    }
                }
            }
            detailScroll[detailScroll.length -1].addView(folderLy[folderLy.length -1]);
            scRel.addView(detailScroll[detailScroll.length -1]);
        }

        return scRel;
    }
    public void OnClick(View view) {
        String tag = view.getTag().toString();
        String sPath = tag.substring(tag.lastIndexOf("  ") + 2);
        int lp = Integer.parseInt(tag.substring(0, tag.indexOf("_")));
        devicePath = sPath;
        // stop running Mediaplayer
            if (tag.contains("open") && fileBrowser.intendStarted) {

                if((fileBrowser.fragId == 7 && ((devicePath.endsWith(".txt") || (devicePath.endsWith("pdf"))))) &&
                        fileBrowser.createTxEditor != null && fileBrowser.createTxEditor.isVisible()) {
                    if(fileBrowser.headMenueIcon02[5].getTag().toString().contains("running") &&
                            (tag.contains(".pdf") || tag.contains(".txt"))) {
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "running", "closed");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon[5], "headMenueIcons", "running", "open");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon[6], "headMenueIcons", "running", "open");
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "open");
                        fileBrowser.fragmentShutdown(fileBrowser.createTxEditor,7);
                    } else if(fileBrowser.headMenueIcon02[5].getTag().toString().contains("open") &&
                            (tag.contains(".pdf") || tag.contains(".txt"))) {
                        fileBrowser.changeIcon(fileBrowser.headMenueIcon02[5], "sideRightMenueIcons", "open", "closed");
                        fileBrowser.fragmentShutdown(fileBrowser.createTxEditor,7);
                    }
                }
                if ((fileBrowser.fragId == 4 && ((!devicePath.endsWith(".txt") && !devicePath.endsWith("pdf")))) &&
                        fileBrowser.showMediaDisplay != null && fileBrowser.showMediaDisplay.isVisible()) {
                    //fileBrowser.showMediaDisplay.disrupt = true;

                    if (fileBrowser.runningMediaList != null && fileBrowser.runningMediaList.size() > 0)
                        fileBrowser.runningMediaList = null;

                    if(fileBrowser.showMediaDisplay.videoView.isPlaying()) {
                        //fileBrowser.showMediaDisplay.videoView.stopPlayback();
                        if(!fileBrowser.headMenueIcon02[3].getTag().toString().contains("One")) {
                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "open", "closed");
                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "running", "closed");
                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                        } else {

                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "openOne", "closed");
                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[3], "sideRightMenueIcons", "runningOne", "closed");
                            fileBrowser.changeIcon(fileBrowser.headMenueIcon02[2], "sideRightMenueIcons", "open", "closed");
                        }

                        runningMediaList = new ArrayList<>(0);
                        fileBrowser.showMediaDisplay.mP.stop();
                        fileBrowser.showMediaDisplay.mP.reset();
                        fileBrowser.showMediaDisplay.mP.release();
                    }


                    if(openFrags.equals("")) {
                        fileBrowser.closeListlinkedIcons(new ImageView[]{fileBrowser.headMenueIcon02[2], fileBrowser.headMenueIcon02[3]},
                                new String[]{"sideRightMenueIcons", "sideRightMenueIcons"});
                        fileBrowser.headMenueIcon02[2].setEnabled(false);
                    } else
                        fileBrowser.closeListlinkedIcons(new ImageView[]{fileBrowser.headMenueIcon02[3]},
                            new String[]{"sideRightMenueIcons"});


                    fileBrowser.fragmentShutdown(fileBrowser.showMediaDisplay,4);
                }
                if(fileBrowser.webBrowserDisplay != null && fileBrowser.webBrowserDisplay.isVisible()) {
                    if(openFrags.equals("")) {
                        fileBrowser.closeListlinkedIcons(new ImageView[]{headMenueIcon[5], headMenueIcon02[2]}, new String[]{"headMenueIcons", "sideRightMenueIcons"});
                        headMenueIcon02[2].setEnabled(false);
                    } else
                        fileBrowser.closeListlinkedIcons(new ImageView[]{headMenueIcon[5]}, new String[]{"headMenueIcons"});

                    fileBrowser.fragmentShutdown(fileBrowser.webBrowserDisplay, 8);
                }

                fileBrowser.intendStarted = false;
            }
        if (tag.contains("open") || tag.contains("running")) {
            devicePath = devicePath.substring(0, devicePath.lastIndexOf("/"));
        }

        detscrPosY[lp -1] = ((ScrollView)(view.getParent().getParent().getParent())).getScrollY();

        devicePath_trans = devicePath;
        String url = devicePath,
                urldevicePath = urldevice;
        if (!url.equals(urldevice))
            urldevicePath = url.substring(urldevice.length() + 1);

        paramList = fileBrowser.createArrayList(urldevicePath);

        if (urldevicePath.length() != 0 || urldevicePath.contains("/")) {
            fileBrowser.createFolder(urldevice);
            fileBrowser.fragmentStart(fileBrowser.filebrowser_01, 1,"fileBrowser01", null, (int) (250 * xfact), (int) (440 * yfact),
                    (int) (4 * displayWidth / 5 - 80 * yfact), (int) (5 * displayHeight / 7));
        } else
            fileBrowser.createFolder(urldevice);

    }
}