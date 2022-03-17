package easysoft.freebrowser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.*;
import androidx.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static easysoft.freebrowser.FileBrowser.*;


public class WebBrowserFragment extends Fragment {
    View view;
    FrameLayout webLayout;
    WebView webView;
    RelativeLayout mainRel, header;
    LinearLayout steerLin;
    ImageView[] steerImgs;
    EditText https;

    String uri = "";
    ArrayList<String> urlCollection;
    int urlCollectionCounter;
    float previousX, previousY;
    boolean ishandled = false;

    public WebBrowserFragment() {

    }

    public static WebBrowserFragment newInstance() {
        WebBrowserFragment fragment = new WebBrowserFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            uri = getArguments().getString("URL");
        }
        urlCollection = new ArrayList<>();
        urlCollectionCounter = -1;
        webLayout = fileBrowser.frameLy.get(8);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_web_browser, container, false);
        mainRel = (RelativeLayout) view.findViewById(R.id.webBrowserMainRel);
        //mainRel.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
        mainRel.addView(WebAction());
        mainRel.addView(createSwitcher());

        webLayout.bringToFront();
        return view;
    }

    private void popupSoftkeyboard() {
        int fact = displayHeight/18,
                fact01 = displayHeight/18;
        if(yfact < 0.625) {
            fact = displayHeight / 28;
            fact01 = 0;
        }
        if(yfact >= 0.8) {
            fact01 = displayHeight/12;
        }
        fileBrowser.fragmentStart(fileBrowser.softKeyBoard, 6, "softKeyBoard", null, 5, (int) (2 * displayHeight / 3 - fact),
                displayWidth - 10, (int) (displayHeight / 3 + fact01));
    }
    private void popdownSoftkeyboard(){
        if(fileBrowser.softKeyBoard != null && fileBrowser.softKeyBoard.isVisible())
            fileBrowser.fragmentShutdown(fileBrowser.softKeyBoard,6);
    }

    public static void initWebSetting(WebView webView) {
        WebSettings setting = webView.getSettings();
        setting.setJavaScriptEnabled(true);
        setting.setAllowFileAccess(true);
        setting.setAllowFileAccessFromFileURLs(true);
        setting.setAllowUniversalAccessFromFileURLs(true);
        setting.setAppCacheEnabled(true);
        setting.setDatabaseEnabled(true);
        setting.setDomStorageEnabled(true);
        setting.setCacheMode(WebSettings.LOAD_DEFAULT);
        setting.setAppCachePath(webView.getContext().getCacheDir().getAbsolutePath());
        setting.setUseWideViewPort(true);
        setting.setLoadWithOverviewMode(true);
        setting.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setting.setAllowFileAccessFromFileURLs(true);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public WebView WebAction(){

        webView = new WebView(fileBrowser);
        webView.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        initWebSetting(webView);
        webView.loadUrl(uri);

        webView.setWebViewClient(new WebViewClient() {
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                if (urlCollection.size() > 1) {
                    urlCollection.remove(urlCollectionCounter);
                    urlCollectionCounter = urlCollectionCounter - 1;
                }
                fileBrowser.changeIcon(steerImgs[0], "browserIcons", "open", "closed");
                steerImgs[0].setEnabled(false);
                fileBrowser.changeIcon(steerImgs[4], "browserIcons", "open", "closed");
                steerImgs[4].setEnabled(false);
                fileBrowser.messageStarter("notFoundWebPage", fileBrowser.docu_Loader("Language/" + fileBrowser.language + "/NoFoundWebSide.txt"),  6000);
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
            }

            public void onPageFinished(WebView view, String url) {

                boolean contain = false;
                if (urlCollectionCounter > -1 && (urlCollection.get(urlCollectionCounter)).startsWith(url.substring(0, url.length() - 10)))
                    contain = true;

                if (!urlCollection.contains(url)) {
                    if (contain) {
                        urlCollection.remove(urlCollectionCounter);
                        urlCollectionCounter = urlCollectionCounter - 1;
                    }
                    if(ishandled) {
                        for (int i = urlCollection.size() - 1; i > urlCollectionCounter; i--)
                            urlCollection.remove(i);
                        fileBrowser.changeIcon(steerImgs[4], "browserIcons", "open", "closed");
                        steerImgs[4].setEnabled(false);
                    }

                    urlCollection.add(webView.getUrl());
                    urlCollectionCounter = urlCollectionCounter + 1;
                    if (urlCollectionCounter > 0) {
                        fileBrowser.changeIcon(steerImgs[0], "browserIcons", "closed", "open");
                        steerImgs[0].setEnabled(true);
                        steerImgs[2].setEnabled(true);
                    }
                }

            }
        });

        return webView;
    }

    private RelativeLayout createSwitcher() {

        header = new RelativeLayout(fileBrowser);
        header.setLayoutParams(new RelativeLayout.LayoutParams(displayWidth, displayHeight/16));
        header.setY(displayHeight -displayHeight/11);
        header.setOnTouchListener(new View.OnTouchListener() {
            float newX = 0, newY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent e) {

                switch (e.getAction()) {
                    case (MotionEvent.ACTION_DOWN): {
                        previousX = e.getX();
                        previousY = e.getY();
                    }
                    case (MotionEvent.ACTION_UP): {
                        newX = e.getX();
                        newY = e.getY();
                    }
                }

                if ((previousX - newX) < -100) {
                    if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                        fileBrowser.fragmentShutdown(fileBrowser.showList, 3);
                    }

                    fileBrowser.startMovePanel(8);

                } else if(previousY < newY) {
                    header.setY(displayHeight - displayHeight / 22);
                } else if(previousY > newY) {
                    header.setY(displayHeight - displayHeight / 11);
                }

                return true;
            }
        });
        steerLin = new LinearLayout(fileBrowser);
        steerLin.setOrientation(LinearLayout.HORIZONTAL);
        RelativeLayout.LayoutParams steerLinRel = new RelativeLayout.LayoutParams(displayWidth/2, displayHeight/16);
        steerLinRel.addRule(RelativeLayout.CENTER_IN_PARENT);
        steerLin.setLayoutParams(steerLinRel);
        //steerLin.setPadding(5,5,5,5);

        steerImgs = new ImageView[0];
        String[] steerNames = new String[]{"webSideback_closed.png", "webSideSearch.png", "https_closed.png", "webSideMemory_closed.png", "webSideforward_closed.png"};
        int f=18;
        if(yfact < 0.5)
            f=20;
        for(int i=0; i<steerNames.length; i++) {
            steerImgs = Arrays.copyOf(steerImgs,steerImgs.length +1);
            steerImgs[steerImgs.length -1] = new ImageView(fileBrowser);
            steerImgs[steerImgs.length -1].setLayoutParams(new RelativeLayout.LayoutParams(displayHeight/f,
                    RelativeLayout.LayoutParams.MATCH_PARENT));
            steerImgs[steerImgs.length -1].setImageBitmap(fileBrowser.bitmapLoader("Icons/browserIcons/"+steerNames[i]));
            steerImgs[steerImgs.length -1].setTag(steerNames[i]);
            steerImgs[steerImgs.length -1].setPadding(10,0,10,0);
            if(urlCollection.size() == 0 && (i!=1 && i!=2 && i!=3))
                steerImgs[steerImgs.length -1].setEnabled(false);

            if(steerNames[i].contains("webSideMemory"))
                steerImgs[steerImgs.length -1].setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        steerPanelOnLongClick(v);

                        return true;
                    }
                });
            steerImgs[steerImgs.length -1].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    steerPanelOnClick(view);
                }
            });
            steerLin.addView(steerImgs[steerImgs.length -1]);
        }
        header.addView(steerLin);
        return header;
    }

    private void steerPanelOnClick(View v) {
        String tag = v.getTag().toString();
        if(tag.contains("webSideSearch")) {
            urlCollection = new ArrayList<>();
            urlCollectionCounter = -1;
            ishandled = false;
            fileBrowser.changeIcon(steerImgs[0], "browserIcons", "open", "closed");
            steerImgs[0].setEnabled(false);
            fileBrowser.changeIcon(steerImgs[4], "browserIcons", "open", "closed");
            steerImgs[4].setEnabled(false);

            webView.loadUrl(fileBrowser.searchMashineUrl);
        }
        else if(tag.contains("webSideMemory")) {
            if(fileBrowser.showList != null && fileBrowser.showList.isVisible()) {
                fileBrowser.changeIcon(steerImgs[3], "browserIcons", "open", "closed");
                fileBrowser.fragmentShutdown(fileBrowser.showList,3);
            } else {
                String[] webSideMem = fileBrowser.read_writeFileOnInternalStorage("read", "WebSideMemory", "WebSideMemory_Saved.txt", "");
                if(webSideMem.length ==  0) {
                    webSideMem = Arrays.copyOf(webSideMem, webSideMem.length + 1);
                    webSideMem[webSideMem.length - 1] = "";
                }
                if (webSideMem.length > 20)
                    webSideMem = Arrays.copyOfRange(webSideMem, 2, webSideMem.length);

                webSideMem = Arrays.copyOf(webSideMem, webSideMem.length + 1);
                webSideMem[webSideMem.length - 1] = webView.getUrl();
                String body = "";
                for (String s : webSideMem)
                    body = body + s + "\n";

                if (fileBrowser.read_writeFileOnInternalStorage("write", "WebSideMemory", "WebSideMemory_Saved.txt", body.substring(0, body.lastIndexOf("\n"))).length == 0)
                    fileBrowser.messageStarter("WebSideSaved", docu_Loader("Language/" + language + "/WebSideMemory_Save.txt"),  5000);
            }
        }
        else if(tag.contains("webSideback")) {
            ishandled = true;
            fileBrowser.changeIcon(steerImgs[4], "browserIcons", "closed", "open");
            steerImgs[4].setEnabled(true);
            urlCollectionCounter = urlCollectionCounter -1;
            if(urlCollectionCounter == 0) {
                fileBrowser.changeIcon(steerImgs[0], "browserIcons", "open", "closed");
                steerImgs[0].setEnabled(false);
            }
            webView.loadUrl(urlCollection.get(urlCollectionCounter));
        }
        else if(tag.contains("webSideforward")) {
            ishandled = true;
            fileBrowser.changeIcon(steerImgs[0], "browserIcons", "closed", "open");
            steerImgs[0].setEnabled(true);
            urlCollectionCounter = urlCollectionCounter +1;
            if(urlCollection.size() -1 == urlCollectionCounter) {
                fileBrowser.changeIcon(steerImgs[4], "browserIcons", "open", "closed");
                ishandled = false;
                steerImgs[4].setEnabled(false);
            }
            webView.loadUrl(urlCollection.get(urlCollectionCounter));
        }
        else if(tag.contains("https")) {
            if(tag.contains("closed")) {
                fileBrowser.changeIcon(steerImgs[2], "browserIcons", "closed", "open");
                createHttpsEditField();
            } else if(tag.contains("open")) {
                if((fileBrowser.showMessage != null && fileBrowser.showMessage.isVisible()))
                    fileBrowser.fragmentShutdown(fileBrowser.showMessage,0);
                fileBrowser.changeIcon(steerImgs[2], "browserIcons", "open", "closed");
                popdownSoftkeyboard();
            }
        }
    }

    private void steerPanelOnLongClick(View v) {
        if(fileBrowser.showList == null || !fileBrowser.showList.isVisible()) {
            String[] webSideMem = fileBrowser.read_writeFileOnInternalStorage("read", "WebSideMemory", "WebSideMemory_Saved.txt", "");
            if (webSideMem.length > 0) {
                fileBrowser.changeIcon(steerImgs[3], "browserIcons", "closed", "open");

                double f = 2.5;
                if(yfact <= 0.625)
                    f = 2;

                int[] iconpos = new int[2];
                v.getLocationOnScreen(iconpos);

                fileBrowser.createList("webSideMemoryList",1, "WebSideMemory WebSideMemory_Saved.txt",6,
                        (int)(iconpos[0]),(int)(iconpos[1] -displayHeight/22), (int)(displayWidth/f),"lo");

                fileBrowser.frameLy.get(3).bringToFront();
            }

        }
    }

    private void createHttpsEditField () {
        fileBrowser.messageStarter("httpsRequest", docu_Loader("Language/" + language + "/Instruction_Https.txt"),
                 0);
        if(fileBrowser.softKeyBoard == null || !fileBrowser.softKeyBoard.isVisible()) {
            popupSoftkeyboard();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView.canGoBack()){
            webView.goBack();
        }else {
            fileBrowser.fragmentShutdown(fileBrowser.webBrowserDisplay, 8);
        }
    }

}
